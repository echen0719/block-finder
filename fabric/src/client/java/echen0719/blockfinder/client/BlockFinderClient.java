package echen0719.blockfinder.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import com.mojang.blaze3d.platform.InputConstants;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import echen0719.blockfinder.screens.HUDInfo;
import echen0719.blockfinder.screens.menuScreen;
import echen0719.blockfinder.screens.blockConfig;

public class BlockFinderClient implements ClientModInitializer {
	public static menuScreen mainScreen;
	public static boolean hudRegistered = false;

	public static KeyMapping scanKey;
	private static final KeyMapping.Category category = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath("blockfinder", "menu")
	);

	@Override
	public void onInitializeClient() {
		File gameDir = FabricLoader.getInstance().getGameDirectory();
        File folder = new File(gameDir, "blockfinder");

		if (!folder.exists()) {
        	folder.mkdirs();
    	}

		scanKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.blockfinder.scan", 
			InputConstants.Type.KEYSYM, 
			GLFW.GLFW_KEY_V,
			category
		));

		// runs every tick or 20x/s
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (scanKey.consumeClick()) {
				mainScreen = new menuScreen();
				client.setScreenAndShow(mainScreen);
			}

			if (BlockScanner.autoRescan && BlockScanner.autoRescanReady && client.player != null &&
			!BlockScanner.isScanning && BlockScanner.lastPlayerCenter != null) {
				BlockPos currentPos = client.player.blockPosition();
				
				// check to make sure coords changed before scanning
				if (!currentPos.equals(BlockScanner.lastPlayerCenter)) {
					// get values to scan
					for (blockConfig config : menuScreen.getActivePool()) {
						try {
							int radius = Integer.parseInt(config.radius.trim());
							int minY = Integer.parseInt(config.minY.trim());
							int maxY = Integer.parseInt(config.maxY.trim());

							if (minY <= -64 || minY > 320) minY = -64;
							if (maxY <= -64 || maxY > 320) maxY = 319;

							BlockScanner.scan(radius, config.block, minY, maxY);
						} 
						catch (NumberFormatException e) {
							// System.out.println("Bruh");
						}
            		}
				}
			} // don't know if this will lag yet
		});

		// remove block from positions if it is broken
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			Block brokenBlock = state.getBlock();
			List<BlockPos> positions = BlockScanner.foundBlocks.get(brokenBlock);
			
			if (positions != null) {
				positions.remove(pos);
			}
		});

		LevelRenderEvents.END_MAIN.register(context -> { // runs every frame
			Minecraft client = Minecraft.getInstance();

			if (BlockScanner.foundBlocks != null) {
				int renderDistance = client.options.getEffectiveRenderDistance();

				BlockPos playerPos = client.player.blockPosition();
        		int playerChunkX = playerPos.getX() >> 4;
        		int playerChunkZ = playerPos.getZ() >> 4;

				for (blockConfig config : menuScreen.getActivePool()) {
					List<BlockPos> positions = BlockScanner.foundBlocks.get(config.block);
        			if (positions == null) continue;

					List<BlockPos> visiblePositions = new ArrayList<>();
					synchronized (positions) {
						for (BlockPos position : positions) {
							int blockChunkX = position.getX() >> 4;
							int blockChunkZ = position.getZ() >> 4;

							if (Math.abs(blockChunkX - playerChunkX) <= renderDistance && 
							Math.abs(blockChunkZ - playerChunkZ) <= renderDistance) { // absolute peakness
								visiblePositions.add(position);
							}
						}
					} // prevents ConcurrentModificationException

					if (!visiblePositions.isEmpty()) {
						BlockDrawer.drawOutline(context, visiblePositions, config.color);

						if (config.drawTracer) {
							// BlockDrawer.drawTracerLines(context, visiblePositions, config.color);
							// until I implement this...
						}
					}
				}
            }
		});
	}

	public static void showHUD() {
		if (!hudRegistered) {
			HudElementRegistry.addLast(
				Identifier.fromNamespaceAndPath("blockfinder", "hud_info"), (context, deltaTracker) -> {
					HUDInfo.render(context, menuScreen.getActivePool());
				}
			);
				
			hudRegistered = true;
    	}
	}
}