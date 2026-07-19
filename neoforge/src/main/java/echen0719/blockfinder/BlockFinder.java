package echen0719.blockfinder;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

@Mod(BlockFinder.MOD_ID)
public class BlockFinder {
	public static final String MOD_ID = "block_finder";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogUtils.getLogger();

    // took this from example mod
	public BlockFinder(IEventBus modEventBus) {
        modEventBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }
}