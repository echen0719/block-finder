package echen0719.blockfinder.screens;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Checkbox; // HOW DID I NOT KNOW THIS EXISTED?!
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import echen0719.blockfinder.client.BlockDrawer;
import echen0719.blockfinder.client.BlockScanner;
import echen0719.blockfinder.client.BlockFinderClient;
import echen0719.blockfinder.utils.guiUtils;
import echen0719.blockfinder.utils.colorUtils;

public class menuScreen extends Screen {
    // gui componenets
    private EditBox radiusSizeBox;
    private EditBox minYBox;
    private EditBox maxYBox;
    private searchableDropdown blockDropdown;
    private Button submitButton;
    private Button clearButton;
    private Button loadButton;
    private Button saveButton;
    private Checkbox autoRescanCheckbox;
    private Checkbox showHUDCheckbox;

    // colors
    private static int white = 0xFFFFFFFF;
    private static int lightGray = 0xFF808080;
    private static int darkGray = 0xFF404040;
    private static int darkTranslucentGray = 0xAA202020;
    private static int midGray = 0xAA303030;
    private static int black = 0xFF000000;

    // pool values
    public static final java.util.List<blockConfig> activePool = new java.util.ArrayList<>();
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
        
        blockDropdown = new searchableDropdown(this, 10, 30, 225, 20, "Block name");

        autoRescanCheckbox = guiUtils.createCheckbox(this, "Auto Rescan", this.width - 170, 30, BlockScanner.autoRescan, (checkbox, selected) -> {
            BlockScanner.autoRescan = selected;
        });

        showHUDCheckbox = guiUtils.createCheckbox(this, "Show HUD", this.width - 80, 30, HUDInfo.showHUD, (checbox, selected) -> {
            HUDInfo.showHUD = selected;
        });

        this.addRenderableWidget(radiusSizeBox);

        this.addRenderableWidget(minYBox);
        this.addRenderableWidget(maxYBox);

        this.addRenderableWidget(autoRescanCheckbox);
        this.addRenderableWidget(showHUDCheckbox);

        this.addRenderableWidget(blockDropdown);
        this.addRenderableWidget(blockDropdown.getSearchBox());
    }

    private boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(value.trim());
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean generateErrorMessage() {
        java.util.List<String> missingFields = new java.util.ArrayList<>();
        for (blockConfig config : activePool) {
            if (!isValid(config.radius) && !missingFields.contains("radius")) {
                missingFields.add("radius");
            }
            if (!isValid(config.minY) && !missingFields.contains("minimum Y")) {
                missingFields.add("minimum Y");
            }
            if (!isValid(config.maxY) && !missingFields.contains("maximum Y")) {
                missingFields.add("maximum Y");
            }
        }

        if (!missingFields.isEmpty()) {
            String missingString = "";
            if (missingFields.size() == 1) {
                missingString = missingFields.get(0);
            } 
            else if (missingFields.size() == 2) {
                missingString = missingFields.get(0) + " and " + missingFields.get(1);
            } 
            else {
                missingString = missingFields.get(0) + ", " + missingFields.get(1) + ", and " + missingFields.get(2);
            }

            HUDInfo.errorMessage = "Fill in/Check the values for " + missingString + " and resubmit.";
            
            onClose();
            BlockFinderClient.showHUD();
            return true; // had error
        }
        return false; // no error
    }

    public void createButtons() {
        submitButton = guiUtils.createButton(this, "Submit", this.width / 2 - 110, this.height - 40, 100, 20, button -> {
            if (activePool.isEmpty()) return;

            if (generateErrorMessage()) {
                return;
            }

            HUDInfo.errorMessage = null; // clear message after submit
            BlockScanner.autoRescanReady = true;

            for (blockConfig config : activePool) {
                int radius = Integer.parseInt(config.radius.trim());
                int minY = Integer.parseInt(config.minY.trim());
                int maxY = Integer.parseInt(config.maxY.trim());

                if (minY <= -64 || minY > 320) minY = -64;
                if (maxY <= -64 || maxY > 320) maxY = 319;

                BlockScanner.scan(radius, config.block, minY, maxY);
            }

            onClose();
            BlockFinderClient.showHUD();
        });

        clearButton = guiUtils.createButton(this, "Clear All", this.width / 2 + 10, this.height - 40, 100, 20, button -> {
            activePool.clear();
            selectedConfig = null;
            BlockDrawer.clear();
            
            BlockScanner.foundBlocks.clear();
            BlockScanner.autoRescanReady = false;
        });

        loadButton = guiUtils.createButton(this, "↑", 5, this.height - 25, 20, 20, button -> {
            
        });

        saveButton = guiUtils.createButton(this, "↓", 30, this.height - 25, 20, 20, button -> {
            Minecraft.getInstance().setScreenAndShow(new confirmationScreen(this)); // create file
        });

        this.addRenderableWidget(submitButton);
        this.addRenderableWidget(clearButton);
        this.addRenderableWidget(loadButton);
        this.addRenderableWidget(saveButton);
    }

    public static java.util.List<blockConfig> getActivePool() {
        return activePool;
    }

    public void saveToFile(String fileName) {
        File gameDir = FabricLoader.getInstance().getGameDirectory();
        File folder = new File(gameDir, "blockfinder");

        File outputFile = new File(folder, fileName);
        JsonArray outputArray = new JsonArray();

        try {
            for (blockConfig config : activePool) {
                JsonObject configJson = new JsonObject();
                
                String blockID = BuiltInRegistries.BLOCK.getKey(config.block).toString();
                configJson.addProperty("block", blockID);
                configJson.addProperty("radius", config.radius);
                configJson.addProperty("minY", config.minY);
                configJson.addProperty("maxY", config.maxY);

                JsonArray colorArray = new Gson().toJsonTree(config.color).getAsJsonArray();
                configJson.add("color", colorArray);

                outputArray.add(configJson);
            }

            String jsonString = new Gson().toJson(outputArray);

            java.io.FileWriter writer = new java.io.FileWriter(outputFile);
            writer.write(jsonString);
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
            int rowHeight = 28;
            int horizontalPadding = 6;

            int currentX = startX;
            int currentY = startY + 20;
            int maxWidth = this.width - 10;

            for (int i = 0; i < activePool.size(); i++) {
                blockConfig config = activePool.get(i);
                String name = config.block.getName().getString();
                int textWidth = this.font.width(name);
                int closeWidth = this.font.width("x");

                int itemWidth = 24 + textWidth + 24 + closeWidth + 4;

                if (currentX + itemWidth > maxWidth) {
                    currentX = startX;
                    currentY += rowHeight;
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
        int rowHeight = 28;
        int horizontalPadding = 6;

        int currentX = startX;
        int currentY = startY;
        int maxWidth = this.width - 10;

        context.text(this.font, Component.literal("Active Finders:"), startX, startY, white);
        currentY += 20;

        for (int i = 0; i < activePool.size(); i++) {
            blockConfig config = activePool.get(i);
            String name = config.block.getName().getString();

            int textWidth = this.font.width(name);
            int closeWidth = this.font.width("x");
            int itemWidth = 24 + textWidth + 24 + closeWidth + 4;

            if (currentX + itemWidth > maxWidth) {
                currentX = startX;
                currentY += rowHeight;
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
        
        // these are hidden when submenu opens
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
