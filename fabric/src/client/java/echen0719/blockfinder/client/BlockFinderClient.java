package echen0719.blockfinder.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.sdl.SDLScancode;

import net.fabricmc.api.ClientModInitializer;
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
	private static boolean pipelineRegistered = false;

	public static KeyMapping scanKey;
	private static final KeyMapping.Category category = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath("blockfinder", "menu")
	);

	@Override
	public void onInitializeClient() {
		ClientHooks.setShowHUD(BlockFinderClient::showHUD);
		File gameDir = Minecraft.getInstance().gameDirectory;
        File folder = new File(gameDir, "blockfinder");

		if (!folder.exists()) {
        	folder.mkdirs();
    	}

		scanKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.blockfinder.scan",
			InputConstants.Type.KEYBOARD,
			SDLScancode.SDL_SCANCODE_V, // for some reason this one maps to 'V' properly
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

		LevelRenderEvents.END_MAIN.register(context -> { // runs every frame
			Minecraft client = Minecraft.getInstance();

			if (!pipelineRegistered) {
				registerPipeline();
				pipelineRegistered = true;
			}

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
						BlockDrawer.drawOutline(context.poseStack(), visiblePositions, config.color);

						if (config.drawTracer) {
							BlockDrawer.drawTracerLines(context.poseStack(), visiblePositions, config.color);
						}
					}
				}
            }
		});
	}

	// [19:54:00] [Render thread/ERROR] (Iris) Missing program 
	// blockfinder:pipeline/see_through_lines in override list. This is 
	// not a critical problem, but it could lead to weird rendering.

	// prevents this issue
	// solution found by ChatGPT
	private static void registerPipeline() {
		try {
			Class<?> pipelinesClass = Class.forName("net.irisshaders.iris.pipeline.IrisPipelines");
			Class<?> shaderKeyClass = Class.forName("net.irisshaders.iris.pipeline.programs.ShaderKey");
			Object linesShader = Enum.valueOf((Class) shaderKeyClass, "LINES");
			
			pipelinesClass.getMethod("assignPipeline", RenderPipeline.class, shaderKeyClass).invoke(
				null, 
				BlockDrawer.getSeeThroughLinesPipeline(), 
				linesShader
			);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
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