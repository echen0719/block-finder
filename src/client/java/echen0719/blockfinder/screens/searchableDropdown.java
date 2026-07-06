package echen0719.blockfinder.screens;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import echen0719.blockfinder.utils.guiUtils;

public class searchableDropdown extends AbstractWidget {
    private Screen parent;

    // gui componenets
    private EditBox searchBox;

    // layout constants
    private int startX = 0;
    private int startY = 0;

    // values
    private boolean isDropdownOpen = false;
    private Block selectedBlock = null;

    // lists
    private List<String> allBlocks;
    private List<String> filteredBlocks;

    public searchableDropdown(Screen parent, int x, int y, int width, int height, String message) {
        super(height, height, width, height, Component.literal(message));

        this.parent = parent;
        this.startX = x;
        this.startY = y;

        for (Block block : BuiltInRegistries.BLOCK) {
            this.allBlocks.add(block.getDescriptionId());
        }

        this.filteredBlocks = new ArrayList<>(this.allBlocks);
    }

    public void createSearchBox() {
        searchBox = guiUtils.createInputBox(parent, startX, startY, 150, 20, "Enter block name...");
        this.searchBox.setResponder(this::changeSearchTerm);
    }

    public void changeSearchTerm(String searchTerm) {
        if (searchTerm.isEmpty()) {
            filteredBlocks = new ArrayList<String>(allBlocks); // if no term, then reset
        }
        else {
            filteredBlocks = allBlocks.stream().filter(block -> 
                block.toLowerCase().contains(searchTerm.toLowerCase())
            ).toList(); // filter

            isDropdownOpen = true;
        }
    }

    public Block getSelectedBlock() {
        return this.selectedBlock;
    }

    // more implements

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // something
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int x, int y, float delta) {
        super.extractRenderState(context, x, y, delta);
    }
}
