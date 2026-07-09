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
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import echen0719.blockfinder.utils.guiUtils;

public class searchableDropdown extends AbstractWidget {
    private Screen parent;

    // gui componenets
    private EditBox searchBox;

    // positioning and size
    private int tableX;
    private int tableY;
    private int tableWidth;
    private int tableHeight;

    // layout constants
    private int itemHeight = 20;
    private int maxDisplayedItems = 7;
    private int scrollBarWidth = 5;

    // colors
    private static int white = 0xFFFFFFFF;
    private static int lightGray = 0xFF808080;
    private static int darkGray = 0xFF404040;
    private int scrollBarColor = 0xFF4A4A4A;
    private int scrollBarHoverColor = 0xFF8A8A8A;

    // values
    private boolean isDropdownOpen = false;
    private Block selectedBlock = null;

    // scrolling vars
    private int scrollPos = 0;
    private int scrollMax = 0;
    private boolean isScrollDragging = false;

    // lists
    private List<Block> allBlocks = new ArrayList<Block>();
    private List<Block> filteredBlocks = new ArrayList<Block>();

    private GuiGraphicsExtractor context;
    private Minecraft client = Minecraft.getInstance();

    public searchableDropdown(Screen parent, int x, int y, int width, int height, String message) {
        super(x, y, width, height, Component.literal(message));
        this.parent = parent;

        for (Block block : BuiltInRegistries.BLOCK) {
            this.allBlocks.add(block);
        }

        this.filteredBlocks = new ArrayList<>(this.allBlocks);
    }

    public void setContext(GuiGraphicsExtractor context) {
        this.context = context;
    }

    public int getDropdownBottomY() { // for shifting submit button
        if (isDropdownOpen && !filteredBlocks.isEmpty()) {
            return tableY + tableHeight;
        }
        return this.getY() + this.getHeight();
    }

    public EditBox getSearchBox() {
        if (searchBox == null) {
            int startX = this.getX();
            int startY = this.getY();

            searchBox = guiUtils.createInputBox(parent, startX, startY, this.getWidth(), this.getHeight(), "Enter block name...");
            searchBox.setResponder(this::changeSearchTerm);
            searchBox.setVisible(false);
        }
        return searchBox;
    }

    private void changeSearchTerm(String searchTerm) {
        if (searchTerm.isEmpty()) {
            filteredBlocks = new ArrayList<Block>(allBlocks); // if no term, then reset
        }
        else {
            String term = searchTerm.toLowerCase();

            filteredBlocks = allBlocks.stream().filter(block -> 
                block.getName().getString().toLowerCase().contains(term)
            ).toList(); // filter
        }

        scrollPos = 0;
        recalculateDimensions();
    }

    private void recalculateDimensions() {
        int maxWidth = 0;

        for (Block block : filteredBlocks) {
            int textWidth = client.font.width(block.getName().getString());
            if (textWidth > maxWidth) {
                maxWidth = textWidth;
            }
        }

        maxWidth += 40;

        if (this.getWidth() != maxWidth) {
            this.setWidth(maxWidth);
            if (searchBox != null) {
                searchBox.setWidth(maxWidth);
            }
        }

        tableX = this.getX();
        tableY = this.getY() + this.getHeight();
        tableWidth = this.getWidth();

        int availableHeight = parent.height - tableY - 10; // padding
        int maximumHeight = Math.min(filteredBlocks.size(), maxDisplayedItems) * itemHeight;

        int rawHeight = Math.min(maximumHeight, availableHeight);
        tableHeight = Math.max(itemHeight, (rawHeight / itemHeight) * itemHeight);

        recalculateMaxScroll();
    }

    private void recalculateMaxScroll() {
        int maxDisplayed = tableHeight / itemHeight;
        scrollMax = Math.max(0, (filteredBlocks.size() - maxDisplayed) * itemHeight);
    }

    public Block getSelectedBlock() {
        return selectedBlock;
    }

    public void setSelectedBlock(Block block) {
        selectedBlock = block;
        if (block != null && searchBox != null) {
            searchBox.setValue(block.getName().getString());
        }
    }

    private void renderDropdownItems(double mouseX, double mouseY) {
        context.fill(tableX, tableY, tableX + tableWidth, tableY + tableHeight, darkGray);

        int startRow = scrollPos / itemHeight;
        int maxDisplayed = tableHeight / itemHeight;
        int visibleRows = Math.min(filteredBlocks.size() - startRow, maxDisplayed);

        for (int i = 0; i < visibleRows; i++) {
            int rowIndex = startRow + i;
            if (rowIndex >= filteredBlocks.size()) break;

            Block block = filteredBlocks.get(rowIndex);
            String inGameName = block.getName().getString();
            int itemY = tableY + (i * itemHeight);

            // highlight
            if (mouseX >= tableX && mouseX <= tableX + tableWidth && mouseY >= itemY && mouseY < itemY + itemHeight) {
                context.fill(tableX, itemY, tableX + tableWidth, itemY + itemHeight, lightGray);
            }

            int iconX = tableX + 2; // some little padding
            int iconY = itemY + 2;

            ItemStack stack = new ItemStack(block);
            context.item(stack, iconX, iconY); // seems to draw icon

            context.text(client.font, inGameName, tableX + 25, itemY + (itemHeight - 8) / 2, white);
        }
    }

    // all of this borrowed from my serverscan mod
    private int[] calcScrollBarAttr() {
        int totalRows = filteredBlocks.size();
        int maxDisplayed = tableHeight / itemHeight;
        if (totalRows <= maxDisplayed) return null;

        int scrollBarX = tableX + tableWidth - scrollBarWidth;
        int scrollBarHeight = Math.max(5, (int)(tableHeight * ((float) maxDisplayed / totalRows)));
        int scrollableHeight = tableHeight - scrollBarHeight;
        
        int scrollBarY = tableY;
        if (scrollMax > 0) {
            scrollBarY = tableY + (int)(scrollableHeight * ((float) scrollPos / scrollMax));
        }

        return new int[] {scrollBarX, scrollBarY, scrollBarHeight};
    }

    private void renderScrollBar(double mouseX, double mouseY) {
        int[] scrollBarInfo = calcScrollBarAttr();
        if (scrollBarInfo == null) return;

        int scrollBarX = scrollBarInfo[0]; 
        int scrollBarY = scrollBarInfo[1]; 
        int scrollBarHeight = scrollBarInfo[2];

        int color = scrollBarColor;
        if (isMouseOverScrollbar(mouseX, mouseY)) {
            color = scrollBarHoverColor;
        }

        context.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, color);
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        int[] scrollBarInfo = calcScrollBarAttr();
        if (scrollBarInfo == null) return false;

        int scrollBarX = scrollBarInfo[0]; 
        int scrollBarY = scrollBarInfo[1]; 
        int scrollBarHeight = scrollBarInfo[2];

        return mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarWidth && mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight;
    }

    public boolean onItemClick(double mouseX, double mouseY) {
        if (mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight()) {
            if (!isDropdownOpen) {
                isDropdownOpen = true;
                if (searchBox != null) {
                    searchBox.setVisible(true);
                    parent.setFocused(searchBox);
                    searchBox.setValue("");

                }

                recalculateDimensions();
                return true;
            }

            return false;
        }

        // if mouse is within the dropdown window
        if (isDropdownOpen && mouseX >= tableX && mouseX <= tableX + tableWidth && mouseY >= tableY && mouseY <= tableY + tableHeight) {
            int[] bar = calcScrollBarAttr();
            if (bar != null && mouseX >= bar[0] && mouseX <= bar[0] + scrollBarWidth) {
                isScrollDragging = true;
                return true;
            }

            int clickedIndex = (int)((mouseY - tableY + scrollPos) / itemHeight);
            if (clickedIndex < filteredBlocks.size()) {
                selectedBlock = filteredBlocks.get(clickedIndex);
            }

            closeDropdown();
            return true;
        }

        if (isDropdownOpen) { // clicking outside
            closeDropdown();
            return true;
        }

        return false;
    }

    private void closeDropdown() {
        isDropdownOpen = false;
        if (searchBox != null) {
            searchBox.setVisible(false);
            if (parent.getFocused() == searchBox) {
                parent.setFocused(null); // unfocus
            }
            if (selectedBlock != null) {
                searchBox.setValue(selectedBlock.getName().getString()); // restore value
            }
        }
    }

    public void handleMouseDrag(double mouseY) {
        if (!isScrollDragging) return; // update scroll while dragging

        int[] scrollBarInfo = calcScrollBarAttr();
        if (scrollBarInfo == null) return;

        int scrollBarHeight = scrollBarInfo[2];
        int scrollableHeight = tableHeight - scrollBarHeight;
        if (scrollableHeight <= 0) return;
    
        int relativeY = (int)(mouseY - tableY - scrollBarHeight / 2);
        relativeY = Math.max(0, Math.min(scrollableHeight, relativeY));
    
        scrollPos = (int)(relativeY * scrollMax / (float)scrollableHeight);
        scrollPos = Math.max(0, Math.min(scrollMax, scrollPos));
    }

    public void handleScroll(double mouseX, double mouseY, double delta) {
        if (mouseX >= tableX && mouseX <= tableX + tableWidth &&
            mouseY >= tableY && mouseY <= tableY + tableHeight) {
            scrollPos += (int)(-delta * itemHeight);
            scrollPos = Math.max(0, Math.min(scrollMax, scrollPos));
        }
    }

    public void handleMouseRelease() {
        isScrollDragging = false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        if (searchBox != null) {
            searchBox.updateWidgetNarration(narrationElementOutput);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), darkGray);

        String displayedText = ""; // for dropdown text
        if (selectedBlock != null) {
            displayedText = selectedBlock.getName().getString();
        }
        else {
            displayedText = this.getMessage().getString();
        }
        
        context.text(client.font, displayedText, this.getX() + 5, this.getY() + (this.getHeight() - 8) / 2, white);
        context.text(client.font, isDropdownOpen ? "▲" : "▼", this.getX() + this.getWidth() - 15, this.getY() + (this.getHeight() - 8) / 2, white);

        if (!isDropdownOpen || filteredBlocks.isEmpty()) {
            return;
        }

        recalculateDimensions();
        renderDropdownItems(mouseX, mouseY);
        renderScrollBar(mouseX, mouseY);
    }
}
