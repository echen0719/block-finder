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
    private int itemHeight = 15;
    private int displayedItems = 5;

    // values
    private boolean isDropdownOpen = false;
    private Block selectedBlock = null;

    // lists
    private List<String> allBlocks = new ArrayList<String>();
    private List<String> filteredBlocks = new ArrayList<String>();

    public searchableDropdown(Screen parent, int x, int y, int width, int height, String message) {
        super(x, y, width, height, Component.literal(message));

        this.parent = parent;

        for (Block block : BuiltInRegistries.BLOCK) {
            this.allBlocks.add(block.getDescriptionId());
        }

        this.filteredBlocks = new ArrayList<>(this.allBlocks);
    }

    public EditBox getSearchBox() {
        if (this.searchBox == null) {
            int startX = this.getX();
            int startY = this.getY();

            searchBox = guiUtils.createInputBox(parent, startX, startY, 150, 20, "Enter block name...");
            this.searchBox.setResponder(this::changeSearchTerm);
        }
        return this.searchBox;
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
        this.searchBox.updateWidgetNarration(narrationElementOutput);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int x, int y, float delta) {
        // idk
    }
}
