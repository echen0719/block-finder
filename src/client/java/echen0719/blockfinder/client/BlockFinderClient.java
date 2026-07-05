package echen0719.blockfinder.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.level.block.Blocks;

import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class BlockFinderClient implements ClientModInitializer {
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
				BlockScanner.scan(7, Blocks.DIAMOND_ORE);
			}
		});
	}
}