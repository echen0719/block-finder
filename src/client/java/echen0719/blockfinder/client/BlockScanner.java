package echen0719.blockfinder.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class BlockScanner {
    private static final Minecraft client = Minecraft.getInstance();

    public static java.util.Map<Block, List<BlockPos>> foundBlocks = new java.util.concurrent.ConcurrentHashMap<>(); // for concurrent scanning safety
    private static ExecutorService executor = Executors.newCachedThreadPool();

    // auto rescan states
    public static boolean autoRescan = false;
    public static boolean autoRescanReady = false;
    public static BlockPos lastPlayerCenter = null;
    public static boolean isScanning = false;

    public static void scan(int blockRadius, Block targetBlock, int minY, int maxY) {
        if (client.level == null || client.player == null || isScanning) {
            System.out.print("Ended the scan!");
            return;
        }

        isScanning = true;
        System.out.print("Started the scan!");

        lastPlayerCenter = client.player.blockPosition();
        foundBlocks.put(targetBlock, java.util.Collections.synchronizedList(new java.util.ArrayList<>()));

        BlockPos playerCenter = client.player.blockPosition();
        int playerX = playerCenter.getX();
        int playerZ = playerCenter.getZ();

        int minChunkX = (playerX - blockRadius) >> 4;
        int maxChunkX = (playerX + blockRadius) >> 4;
        int minChunkZ = (playerZ - blockRadius) >> 4;
        int maxChunkZ = (playerZ + blockRadius) >> 4;

        int playerCenterChunkX = playerCenter.getX() >> 4;
        int playerCenterChunkZ = playerCenter.getZ() >> 4; // seems like 4 bit shift is better than /16

        List<LevelChunk> chunksToScan = new ArrayList<>();

        for (int x = minChunkX; x <= maxChunkX; x++) { // should improve n^3 to something
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                if (client.level.hasChunk(x, z)) {
                    LevelChunk chunk = client.level.getChunk(x, z);
                    if (chunk != null && !chunk.isEmpty()) {
                        chunksToScan.add(chunk);
                    }
                }
            }
        }

        executor.submit(() -> {
            for (LevelChunk chunk : chunksToScan) {
                scanInChunk(chunk, targetBlock, playerCenter, blockRadius, minY, maxY);
            }

            int totalFound = foundBlocks.values().stream().mapToInt(List::size).sum();
            System.out.println("Completed! Found " + totalFound + " blocks.");

            isScanning = false;
        });
    }

    public static void scanInChunk(LevelChunk chunk, Block targetBlock, BlockPos playerCenter, int blockRadius, int minY, int maxY) {
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();

        int playerX = playerCenter.getX();
        int playerZ = playerCenter.getZ();

        // the docs say that this is better when performing many computations at a time
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) { // this is like n^3, idk what else to do though
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = chunkMinX + x;
                    int worldZ = chunkMinZ + z;

                    // skips if not within blockRadius before getting info
                    if (Math.abs(worldX - playerX) > blockRadius) continue;
                    if (Math.abs(worldZ - playerZ) > blockRadius) continue;

                    position.set(worldX, y, worldZ);
                    BlockState state = chunk.getBlockState(position);

                    if (state.is(targetBlock)) {
                        List<BlockPos> list = foundBlocks.get(targetBlock);
                        if (list != null) {
                            list.add(position.immutable());
                        } // world position and thread safe?
                    }
                }
            }
        }

        System.out.println("Done for " + chunk.getPos().x() + ", " + chunk.getPos().z() + "!");
    }

    public static void remove(Block block) {
        foundBlocks.remove(block);
    }   
}