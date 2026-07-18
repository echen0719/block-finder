package echen0719.blockfinder.screens;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;

public class blockConfig {
    public final Block block;
    public final ItemStack stack;
    public String radius = "";
    public String minY = "-64";
    public String maxY = "319";
    public Object[] color = {255, 0, 0, 0.5f};
    public boolean drawTracer = false;

    public blockConfig(Block block) {
        this.block = block;
        this.stack = new ItemStack(block);
    }
}