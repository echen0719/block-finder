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

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent; // HUD
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import echen0719.blockfinder.screens.HUDInfo;
import echen0719.blockfinder.screens.menuScreen;
import echen0719.blockfinder.screens.blockConfig;

// https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/

@Mod(BlockFinderClient.MOD_ID)
public class BlockFinderClient {
	public static final String MOD_ID = "block_finder";

	public static menuScreen mainScreen;
	public static boolean hudRegistered = false;

	private static final KeyMapping.Category category = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath("blockfinder", "menu")
	);
	public static KeyMapping scanKey = new KeyMapping(
		"key.blockfinder.scan", 
		InputConstants.Type.KEYSYM, 
		GLFW.GLFW_KEY_V,
		category
	);

	public BlockFinderClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onInitializeClient);
		modEventBus.addListener(this::registerKeys);
		modEventBus.addListener((RegisterGuiLayersEvent event) -> showHUD(event));

		NeoForge.EVENT_BUS.register(this);
    }

	public void registerKeys(RegisterKeyMappingsEvent event) {
    	event.register(scanKey);
	}

	public void onInitializeClient(FMLClientSetupEvent event) {
		File gameDir = FMLPaths.GAMEDIR.get().toAbsolutePath().toFile();
        File folder = new File(gameDir, "blockfinder");

		if (!folder.exists()) {
        	folder.mkdirs();
    	}
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent.Post event) { // runs every tick or 20x/s
		Minecraft client = Minecraft.getInstance();

		if (!client.hasSingleplayerServer()) {
				return;
			} // prevent multiplayer access by restricting to singleplayer

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
		}
	}

	@SubscribeEvent
	public void playerBlockBreak(BreakBlockEvent event) {
		// remove block from positions if it is broken
		BlockPos position = event.getPos();
		Block brokenBlock = event.getState().getBlock();

		List<BlockPos> positions = BlockScanner.foundBlocks.get(brokenBlock);
		
		if (positions != null) {
			positions.remove(position);
		}
	}

	@SubscribeEvent
	public void renderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) { // runs every frame
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
					BlockDrawer.drawOutline(event, visiblePositions, config.color);

					if (config.drawTracer) {
						// BlockDrawer.drawTracerLines(event, visiblePositions, config.color);
						// until I implement this...
					}
				}
			}
		}
	} // after translucent blocks so it renders after all blocks but before particles

	public static void showHUD(RegisterGuiLayersEvent event) {
		if (!hudRegistered) {
			event.registerAboveAll(Identifier.fromNamespaceAndPath(MOD_ID, "hud_info"), (graphics, deltaTracker) -> {
            	HUDInfo.render(graphics, menuScreen.getActivePool());
        	});

			hudRegistered = true;
    	}
	}

	public static void showHUD() {
        HUDInfo.showHUD = true;
    }
}