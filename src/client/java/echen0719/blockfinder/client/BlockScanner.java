package echen0719.blockfinder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class BlockScanner {
    private static final Minecraft client = Minecraft.getInstance();

    private static List<BlockPos> foundBlocks = Collections.synchronizedList(new ArrayList<>()); // for concurrent scanning safety

    private static boolean isScanning = false;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    public static void scan(int chunkRadius, Block targetBlock) {
        System.out.print("Started the scan!");

        if (isScanning || client.level == null || client.player == null) {
            System.out.print("Ended the scan!");
            return;
        }

        isScanning = true;
        foundBlocks.clear();

        BlockPos playerCenter = client.player.blockPosition();
        int playerCenterChunkX = playerCenter.getX() >> 4;
        int playerCenterChunkZ = playerCenter.getZ() >> 4; // seems like 4 bit shift is better than /16

        List<LevelChunk> chunksToScan = new ArrayList<>();

        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                LevelChunk chunk = client.level.getChunk(playerCenterChunkX + x, playerCenterChunkZ + z);
                if (chunk != null && !chunk.isEmpty()) {
                    chunksToScan.add(chunk);
                }
            }
        }

        executor.submit(() -> {
            for (LevelChunk chunk : chunksToScan) {
                scanInChunk(chunk, targetBlock);
            }

            client.execute(() -> {
                for (BlockPos position : foundBlocks) {
                    // draw
                }
            });

            isScanning = false;
        });
    }

    public static void scanInChunk(LevelChunk chunk, Block targetBlock) {
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();

        // the docs say that this is better when performing many computations at a time
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

        for (int y = -64; y < 320; y++) { // 1.18+ versions for now
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    position.set(chunkMinX + x, y, chunkMinZ + z);
                    BlockState state = chunk.getBlockState(position);

                    if (state.is(targetBlock)) {
                        foundBlocks.add(position.immutable()); // world position and thread safe?
                    }
                }
            }
        }

        System.out.println("Done for " + chunk.getPos().x() + ", " + chunk.getPos().z() + "!");
    }
}