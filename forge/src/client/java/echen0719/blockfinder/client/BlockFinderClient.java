package echen0719.blockfinder.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import com.mojang.blaze3d.platform.InputConstants;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;

import echen0719.blockfinder.screens.HUDInfo;
import echen0719.blockfinder.screens.menuScreen;
import echen0719.blockfinder.screens.blockConfig;

// https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/

@Mod(BlockFinderClient.MOD_ID)
public class BlockFinderClient {
	public static final String MOD_ID = "block_finder";

	public static menuScreen mainScreen;

	private static final String category = "key.categories.blockfinder";
	public static KeyMapping scanKey = new KeyMapping(
		"key.blockfinder.scan", 
		InputConstants.Type.KEYSYM, 
		GLFW.GLFW_KEY_V,
		category
	);

	public BlockFinderClient(FMLJavaModLoadingContext context) {
		ClientHooks.setShowHUD(BlockFinderClient::showHUD);

		var modEventBus = context.getModEventBus();
		modEventBus.addListener(this::onInitializeClient);
		modEventBus.addListener(this::registerKeys);
		MinecraftForge.EVENT_BUS.register(this);
    }

	public void registerKeys(RegisterKeyMappingsEvent event) {
    	event.register(scanKey);
	}

	public void onInitializeClient(FMLClientSetupEvent event) {
		File gameDir = Minecraft.getInstance().gameDirectory;
        File folder = new File(gameDir, "blockfinder");

		if (!folder.exists()) {
        	folder.mkdirs();
    	}
	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent.Post event) { // runs every tick or 20x/s
		Minecraft client = Minecraft.getInstance();

		while (scanKey.consumeClick()) {
			mainScreen = new menuScreen();
			client.setScreen(mainScreen);
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
	public void renderLevel(RenderLevelStageEvent event) { // runs every frame
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

		Minecraft client = Minecraft.getInstance();

		if (client.player != null && BlockScanner.foundBlocks != null) {
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
					BlockDrawer.drawOutline(null, visiblePositions, config.color);

					if (config.drawTracer) {
						BlockDrawer.drawTracerLines(null, visiblePositions, config.color);
					}
				}
			}
		}
	} // after translucent blocks so it renders after all blocks but before particles

	@SubscribeEvent
	public void showHUD(CustomizeGuiOverlayEvent.Chat event) {
		HUDInfo.render(event.getGuiGraphics(), menuScreen.getActivePool());
	} // not sure why this is called "Chat" but you know...if it works, it works :D

	public static void showHUD() {
        // HUDInfo.showHUD = true;
    }
}