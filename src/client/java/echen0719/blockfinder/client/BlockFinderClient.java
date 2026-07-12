package echen0719.blockfinder.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import com.mojang.blaze3d.platform.InputConstants;

import echen0719.blockfinder.screens.menuScreen;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import echen0719.blockfinder.screens.blockConfig;

public class BlockFinderClient implements ClientModInitializer {
	public static menuScreen mainScreen;

	public static KeyMapping scanKey;
	private static final KeyMapping.Category category = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath("blockfinder", "menu")
	);

	@Override
	public void onInitializeClient() {
		scanKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.blockfinder.scan", 
			InputConstants.Type.KEYSYM, 
			GLFW.GLFW_KEY_V,
			category
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (scanKey.consumeClick()) {
				mainScreen = new menuScreen();
				client.setScreenAndShow(mainScreen);
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
					java.util.List<BlockPos> positions = BlockScanner.foundBlocks.get(config.block);
        			if (positions == null) continue;

					BlockDrawer.setColor(config.color);

					synchronized (positions) {
						for (BlockPos position : positions) {
							int blockChunkX = position.getX() >> 4;
							int blockChunkZ = position.getZ() >> 4;

							if (Math.abs(blockChunkX - playerChunkX) > renderDistance || 
								Math.abs(blockChunkZ - playerChunkZ) > renderDistance) { // absolute peakness
								continue; 
							}

							BlockDrawer.drawOutline(context, position);
						}
					} // prevents ConcurrentModificationException
				}
            }
		});
	}
}