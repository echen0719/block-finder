package echen0719.blockfinder.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import com.mojang.blaze3d.platform.InputConstants;

import echen0719.blockfinder.screens.menuScreen;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class BlockFinderClient implements ClientModInitializer {
	public static menuScreen mainScreen;

	public static KeyMapping scanKey;
	private static final KeyMapping.Category category = null;

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

		LevelRenderEvents.END_MAIN.register(context -> {
			if (BlockScanner.foundBlocks != null) {
				synchronized (BlockScanner.foundBlocks) {
					for (BlockPos position : BlockScanner.foundBlocks) {
						BlockDrawer.drawOutline(context, position);
					}
				} // prevents ConcurrentModificationException
            }
		});
	}
}