package echen0719.blockfinder.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import echen0719.blockfinder.client.BlockDrawer;
import echen0719.blockfinder.client.BlockScanner;
import echen0719.blockfinder.utils.guiUtils;
import echen0719.blockfinder.utils.colorUtils;
import echen0719.blockfinder.screens.blockConfig;

public class menuScreen extends Screen {
    // gui componenets
    private EditBox radiusSizeBox;
    private EditBox minYBox;
    private EditBox maxYBox;
    private searchableDropdown blockDropdown;
    private Button submitButton;
    private Button clearButton;

    // colors
    private static int white = 0xFFFFFFFF;
    private static int lightGray = 0xFF808080;
    private static int darkGray = 0xFF404040;
    private static int darkTranslucentGray = 0xAA202020;
    private static int midGray = 0xAA303030;
    private static int black = 0xFF000000;

    // pool values
    private static final java.util.List<blockConfig> activePool = new java.util.ArrayList<>();
    private static blockConfig selectedConfig = null;

    public menuScreen() {
        super(Component.literal("Block Finder"));
    }

    public void createInputs() {
        radiusSizeBox = guiUtils.createInputBox(this, 10, 30, 120, 20, "Enter block radius...");
        minYBox = guiUtils.createInputBox(this, 140, 30, 40, 20, "Min Y");
        maxYBox = guiUtils.createInputBox(this, 190, 30, 40, 20, "Max Y");

        // when values are typed, they are stored
        radiusSizeBox.setResponder(value -> {if (selectedConfig != null) selectedConfig.radius = value;});
        minYBox.setResponder(value -> {if (selectedConfig != null) selectedConfig.minY = value;});
        maxYBox.setResponder(value -> {if (selectedConfig != null) selectedConfig.maxY = value;});
        
        blockDropdown = new searchableDropdown(this, 10, 30, 200, 20, "Block name");

        this.addRenderableWidget(radiusSizeBox);

        this.addRenderableWidget(minYBox);
        this.addRenderableWidget(maxYBox);

        this.addRenderableWidget(blockDropdown);
        this.addRenderableWidget(blockDropdown.getSearchBox());
    }

    public void createButtons() {
        submitButton = guiUtils.createButton(this, "Submit", this.width / 2 - 110, this.height - 40, 100, 20, button -> {
            if (activePool.isEmpty()) return;

            for (blockConfig config : activePool) {
                try {
                    int radius = Integer.parseInt(config.radius.trim());
                    int minY = Integer.parseInt(config.minY.trim());
                    int maxY = Integer.parseInt(config.maxY.trim());

                    if (minY <= -64 || minY > 320) minY = -64;
                    if (maxY <= -64 || maxY > 320) maxY = 319;

                    BlockDrawer.setColor(config.color);
                    BlockScanner.scan(radius, config.block, minY, maxY);
                } 
                catch (NumberFormatException e) {
                    System.out.println("Bruh");
                }
            }

            onClose();
        });

        clearButton = guiUtils.createButton(this, "Clear All", this.width / 2 + 10, this.height - 40, 100, 20, button -> {
            activePool.clear();
            selectedConfig = null;
            BlockDrawer.clear();
            BlockScanner.foundBlocks.clear();
        });

        this.addRenderableWidget(submitButton);
        this.addRenderableWidget(clearButton);
    }

    public static java.util.List<blockConfig> getActivePool() {
        return activePool;
    }

    // using previous code from serverscan
    private boolean onMouseScroll(Screen screen, double mouseX, double mouseY, double deltaX, double deltaY, boolean consumed) {
        blockDropdown.handleScroll(mouseX, mouseY, deltaY);
        return true;
    }

    private boolean onMouseClick(Screen screen, MouseButtonEvent event, boolean consumed) {
        if (event.button() == 0) {
            double x = event.x();
            double y = event.y();

            if (blockDropdown.onItemClick(x, y)) {
                return true;
            }

            int startX = 10;
            int startY = 60;
            int itemHeight = 24;
            int rowSpacing = 28;
            int horizontalPadding = 6;

            int currentX = startX;
            int currentY = startY;
            int maxWidth = this.width - 10;

            for (int i = 0; i < activePool.size(); i++) {
                blockConfig config = activePool.get(i);
                String name = config.block.getName().getString();
                int textWidth = this.font.width(name);
                int closeWidth = this.font.width("x");

                int itemWidth = 24 + textWidth + 24 + closeWidth + 4;

                if (currentX + itemWidth > maxWidth) {
                    currentX = startX;
                    currentY += rowSpacing;
                }

                if (currentY + itemHeight > this.height - 40) break;

                if (x >= currentX && x <= currentX + itemWidth && y >= currentY && y <= currentY + itemHeight) {
                    int colorX = currentX + 24 + textWidth + 6;
                    int closeX = colorX + 18;

                    // slight bigger than close 'x' itself
                    if (x >= closeX - 2 && x <= closeX + closeWidth + 2) {
                        activePool.remove(i);
                        if (selectedConfig == config) {
                            selectedConfig = null;
                        }

                        BlockScanner.remove(config.block);

                        return true;
                    }

                    if (x >= colorX && x <= colorX + 12 && y >= currentY + 4 && y <= currentY + 16) {
                        Minecraft.getInstance().setScreenAndShow(new colorPicker(this, config.color));
                        return true;
                    }

                    if (selectedConfig == config) {
                        selectedConfig = null;
                    } else {
                        selectedConfig = config;
                        radiusSizeBox.setValue(config.radius);
                        minYBox.setValue(config.minY);
                        maxYBox.setValue(config.maxY);
                    }
                    return true;
                }

                currentX += itemWidth + horizontalPadding;
            }

            if (selectedConfig != null) {
                int panelWidth = 260;
                int panelHeight = 110;
                int panelX = (this.width - panelWidth) / 2;
                int panelY = 90;
                boolean insideSubmenu = x >= panelX && x <= panelX + panelWidth && y >= panelY && y <= panelY + panelHeight;
                
                if (!insideSubmenu) {
                    selectedConfig = null;
                    return true;
                }
            }
        }
        return consumed;
    }

    private boolean onMouseRelease(Screen screen, MouseButtonEvent event, boolean consumed) {
        if (event.button() == 0) {
            blockDropdown.handleMouseRelease();
        }
        return consumed;
    }

    private void renderActivePool(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int startX = 10;
        int startY = 60;
        int itemHeight = 24;
        int rowSpacing = 28;
        int horizontalPadding = 6;

        int currentX = startX;
        int currentY = startY;
        int maxWidth = this.width - 10;

        context.text(this.font, Component.literal("Active Finders:"), startX, startY, white);

        for (int i = 0; i < activePool.size(); i++) {
            blockConfig config = activePool.get(i);
            String name = config.block.getName().getString();

            int textWidth = this.font.width(name);
            int closeWidth = this.font.width("x");
            int itemWidth = 24 + textWidth + 24 + closeWidth + 4;

            if (currentX + itemWidth > maxWidth) {
                currentX = startX;
                currentY += rowSpacing;
            }

            if (currentY + itemHeight > this.height - 40) break; // prevent overflow

            int backgroundColor = darkGray;
            if (config == selectedConfig) {
                backgroundColor = lightGray;
            }

            context.fill(currentX, currentY, currentX + itemWidth, currentY + itemHeight, backgroundColor);

            context.item(new ItemStack(config.block), currentX + 4, currentY + 4);
            context.text(this.font, name, currentX + 24, currentY + (itemHeight - 8) / 2, white);

            int colorX = currentX + 24 + textWidth + 6; // auto calc based on length of name
            context.fill(colorX, currentY + 6, colorX + 12, currentY + 18, colorUtils.arrayToInt(config.color));

            int closeX = colorX + 18;
            context.text(this.font, "x", closeX, currentY + (itemHeight - 8) / 2, 0xFFFF5555);

            currentX += itemWidth + horizontalPadding;
        }
    }

    private void renderSubmenuBackground(GuiGraphicsExtractor context) {
        int panelWidth = 260;
        int panelHeight = 100;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 100;

        // background for submenu
        context.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, black);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, darkTranslucentGray);
    }

    private void renderSubmenu(GuiGraphicsExtractor context) {
        if (selectedConfig != null) {
            radiusSizeBox.setX(this.width / 2 - 120); radiusSizeBox.setY(125);
            minYBox.setX(this.width / 2 + 20); minYBox.setY(125);
            maxYBox.setX(this.width / 2 + 70); maxYBox.setY(125);

            context.centeredText(this.font, Component.literal("Editing: " + selectedConfig.block.getName().getString()), this.width / 2, 210, 0xFFFFFF55);
            context.centeredText(this.font, Component.literal("Radius:"), this.width / 2 - 60, 110, white);
            context.centeredText(this.font, Component.literal("Min Y  /  Max Y:"), this.width / 2 + 65, 110, white);
        }
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        createInputs();
        createButtons();

        if (selectedConfig != null) {
            radiusSizeBox.setValue(selectedConfig.radius);
            minYBox.setValue(selectedConfig.minY);
            maxYBox.setValue(selectedConfig.maxY);
        }

        ScreenMouseEvents.afterMouseScroll(this).register((ScreenMouseEvents.AfterMouseScroll) this::onMouseScroll);
        ScreenMouseEvents.afterMouseClick(this).register((ScreenMouseEvents.AfterMouseClick) this::onMouseClick);
        ScreenMouseEvents.afterMouseRelease(this).register((ScreenMouseEvents.AfterMouseRelease) this::onMouseRelease);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.centeredText(this.font, Component.literal("Block Finder"), this.width / 2, 10, white);

        renderActivePool(context, mouseX, mouseY);

        blockDropdown.setContext(context);
        Block dropdownBlock = blockDropdown.getSelectedBlock();
        if (dropdownBlock != null) { // prevent duplicate
            boolean exists = activePool.stream().anyMatch(config -> config.block == dropdownBlock);
            if (!exists) {
                activePool.add(new blockConfig(dropdownBlock));
            }
            blockDropdown.setSelectedBlock(null); // reset for next
        }

        boolean usingSubmenu = selectedConfig != null;

        if (usingSubmenu) {
            renderSubmenuBackground(context);

            radiusSizeBox.extractRenderState(context, mouseX, mouseY, delta);
        }

        radiusSizeBox.setVisible(usingSubmenu); // seen in submenu
        minYBox.setVisible(usingSubmenu);
        maxYBox.setVisible(usingSubmenu);

        if (submitButton != null) {
            submitButton.visible = !usingSubmenu;
        }
        if (clearButton != null) {
            clearButton.visible = !usingSubmenu;
        }

        renderSubmenu(context);

        if (blockDropdown != null) {
            blockDropdown.extractWidgetRenderState(context, mouseX, mouseY, delta);
        }

        blockDropdown.handleMouseDrag(mouseY);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }
}
