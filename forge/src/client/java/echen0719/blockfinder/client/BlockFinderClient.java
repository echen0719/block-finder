package echen0719.blockfinder.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.platform.InputConstants;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.FramePassManager;
import net.minecraftforge.client.event.AddFramePassEvent;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

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

	public BlockFinderClient(FMLJavaModLoadingContext context) {
		ClientHooks.setShowHUD(BlockFinderClient::showHUD);

		// all of the below is from the clean MDK setup

		var modBusGroup = context.getModBusGroup();
        FMLClientSetupEvent.getBus(modBusGroup).addListener(this::onInitializeClient);
		RegisterKeyMappingsEvent.BUS.addListener(this::registerKeys);
		AddGuiOverlayLayersEvent.BUS.addListener(BlockFinderClient::showHUD);
		AddFramePassEvent.BUS.addListener(this::addFramePass);
		TickEvent.ClientTickEvent.Post.BUS.addListener(this::clientTick);
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

	public void addFramePass(AddFramePassEvent event) {
		event.addPass(Identifier.fromNamespaceAndPath(MOD_ID, "blockfinder_lines"), new FramePassManager.PassDefinition() {
			@Override
			public void extracts(LevelTargetBundle bundle, FramePass pass) {
				pass.readsAndWrites(bundle.main);
			}

			@Override
			public void executes(LevelRenderState state) {
				renderLevel();
			}
		});
	} // Forge 26.2 does not have RenderLevelStageEvent so I need to use Minecraft's instead

	private void renderLevel() { // runs every frame
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
					BlockDrawer.drawOutline(null, visiblePositions, config.color);

					if (config.drawTracer) {
						BlockDrawer.drawTracerLines(null, visiblePositions, config.color);
					}
				}
			}
		}
	} // after translucent blocks so it renders after all blocks but before particles

	public static void showHUD(AddGuiOverlayLayersEvent event) {
		if (!hudRegistered) {
			event.getLayeredDraw().add(Identifier.fromNamespaceAndPath(MOD_ID, "hud_info"), (graphics, deltaTracker) -> {
            	HUDInfo.render(graphics, menuScreen.getActivePool());
        	});

			hudRegistered = true;
    	}
	}

	public static void showHUD() {
        // HUDInfo.showHUD = true;
    }
}