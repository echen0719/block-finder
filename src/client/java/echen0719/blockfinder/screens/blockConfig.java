package echen0719.blockfinder.screens;

import net.minecraft.world.level.block.Block;

public class blockConfig {
    public final Block block;
    public String radius = "";
    public String minY = "";
    public String maxY = "";
    public Object[] color = {255, 0, 0, 0.5f};

    public blockConfig(Block block) {
        this.block = block;
    }
}