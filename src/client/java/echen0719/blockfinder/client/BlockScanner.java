import net.minecraft.client.MinecraftClient;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletableFuture;

public class BlockScanner {
    public static final List<BlockPos> foundBlocks = Collections.synchronizedList(new ArrayList<>()); // for concurrent scanning

    private static int chunkRadius = 62;
    private static Blocks wantedBlock = Blocks.DIAMOND_ORE;

    private static boolean isScanning = false;

    public static void scan(MinecraftClient client) {
        if (isScanning || client.level == null || client.player == null) {
            return;
        }

        isScanning = true;
        foundBlocks.clear();

        BlockPos playerCenter = client.player.getBlockPos();
        int playerCenterChunkX = playerPos.getX() >> 4;
        int playerCenterChunkZ = playerPos.getZ() >> 4;

        List<LevelChunk> chunksToScan = new ArrayList<>();

        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                LevelChunk chunk = Level.getChunk(playerCenterChunkX + x, playerCenterChunkZ + z);
                if (chunk != null) {
                    chunksToScan.add(chunk);
                }
            }
        }

        CompletableFuture.runAsync(() -> {

        }
    }
}