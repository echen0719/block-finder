package echen0719.blockfinder.screens;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Checkbox; // HOW DID I NOT KNOW THIS EXISTED?!
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import echen0719.blockfinder.client.BlockDrawer;
import echen0719.blockfinder.client.BlockScanner;
import echen0719.blockfinder.client.BlockFinderClient;
import echen0719.blockfinder.screens.searchableDropdown;
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
    private Checkbox drawLinesCheckbox;

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

        drawLinesCheckbox = guiUtils.createCheckbox(this, "Draw Lines to Block (beta)", this.width / 2 - 60, 160, selectedConfig != null && selectedConfig.drawTracer, (checkbox, selected) -> {
            if (selectedConfig != null) {
                selectedConfig.drawTracer = selected;
            }
        });

        this.addRenderableWidget(radiusSizeBox);

        this.addRenderableWidget(minYBox);
        this.addRenderableWidget(maxYBox);

        this.addRenderableWidget(drawLinesCheckbox);

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
        List<String> missingFields = new ArrayList<>();
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
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(1);
                filters.put(stack.UTF8("*.json"));
                filters.flip();
                
                File gameDir = FabricLoader.getInstance().getGameDirectory();
                File folder = new File(gameDir, "blockfinder");
                if (!folder.exists()) {
                    folder.mkdirs(); // Ensure the directory exists before opening the dialog
                }
                
                String selectedPath = TinyFileDialogs.tinyfd_openFileDialog(
                    "Load Config", folder.getAbsolutePath() + File.separator,
                    filters, "JSON Files", false
                ); // only one select at a time
                
                if (selectedPath != null) {
                    loadFromFile(selectedPath);
                }
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        saveButton = guiUtils.createButton(this, "↓", 30, this.height - 25, 20, 20, button -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(1);
                filters.put(stack.UTF8("*.json"));
                filters.flip();
                
                File gameDir = FabricLoader.getInstance().getGameDirectory();
                File folder = new File(gameDir, "blockfinder");

                String selectedPath = TinyFileDialogs.tinyfd_saveFileDialog(
                    "Save Config", folder.getAbsolutePath() + File.separator, // open in dir instead of outside
                    filters, "JSON Files"
                );

                if (selectedPath != null) {
                    saveToFile(selectedPath);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.addRenderableWidget(submitButton);
        this.addRenderableWidget(clearButton);
        this.addRenderableWidget(loadButton);
        this.addRenderableWidget(saveButton);
    }

    public static List<blockConfig> getActivePool() {
        return activePool;
    }

    // more checks here since people could have manually written them
    public void loadFromFile(String filePath) {
        File inputFile = new File(filePath);
        if (!inputFile.exists()) return;
        
        try (FileReader reader = new FileReader(inputFile)) {
            JsonArray inputArray = new Gson().fromJson(reader, JsonArray.class);
            if (inputArray == null) return;

            for (int i = 0; i < inputArray.size(); i++) {
                JsonElement element = inputArray.get(i);
                if (!element.isJsonObject()) continue;
            
                JsonObject configJson = element.getAsJsonObject();

                if (!configJson.has("block")) continue;
                String blockID = configJson.get("block").getAsString();
                Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockID));
                blockConfig config = new blockConfig(block);

                // using conditional to save some lines
                config.radius = configJson.has("radius") ? configJson.get("radius").getAsString() : "";
                config.minY = configJson.has("minY") ? configJson.get("minY").getAsString() : "";
                config.maxY = configJson.has("maxY") ? configJson.get("maxY").getAsString() : "";
                
                if (configJson.has("color")) {
                    JsonArray colorJson = configJson.getAsJsonArray("color");

                    config.color = new Object[]{
                        colorJson.get(0).getAsInt(),
                        colorJson.get(1).getAsInt(),
                        colorJson.get(2).getAsInt(),
                        colorJson.get(3).getAsFloat()
                    };
                }

                activePool.add(config);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToFile(String filePath) {
        File outputFile = new File(filePath);
    
        JsonArray outputArray = new JsonArray();
        try {
            for (blockConfig config : activePool) {
                if (config == null || config.block == null) continue; // safety

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

            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(jsonString);
                writer.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        blockDropdown.handleScroll(mouseX, mouseY, scrollY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (selectedConfig != null) {
                int panelWidth = 260;
                int panelHeight = 110;
                int panelX = (this.width - panelWidth) / 2;
                int panelY = 90;
                boolean insideSubmenu = mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= panelY && mouseY <= panelY + panelHeight;
                
                if (!insideSubmenu) {
                    selectedConfig = null;
                    return true;
                }
                
                if (super.mouseClicked(mouseX, mouseY, button)) {
                    return true; // to fix some random ahh bug
                }
            }

            if (blockDropdown.onItemClick(mouseX, mouseY)) {
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

                if (mouseX >= currentX && mouseX <= currentX + itemWidth && mouseY >= currentY && mouseY <= currentY + itemHeight) {
                    int colorX = currentX + 24 + textWidth + 6;
                    int closeX = colorX + 18;

                    // slight bigger than close 'x' itself
                    if (mouseX >= closeX - 2 && mouseX <= closeX + closeWidth + 2) {
                        activePool.remove(i);
                        if (selectedConfig == config) {
                            selectedConfig = null;
                        }

                        BlockScanner.remove(config.block);

                        return true;
                    }

                    if (mouseX >= colorX && mouseX <= colorX + 12 && mouseY >= currentY + 4 && mouseY <= currentY + 16) {
                        Minecraft.getInstance().setScreen(new colorPicker(this, config.color));
                        return true;
                    }

                    if (selectedConfig == config) {
                        selectedConfig = null;
                    } 
                    else {
                        selectedConfig = config;
                        radiusSizeBox.setValue(config.radius);
                        minYBox.setValue(config.minY);
                        maxYBox.setValue(config.maxY);

                        this.removeWidget(drawLinesCheckbox);
                        drawLinesCheckbox = guiUtils.createCheckbox(this, "Draw Lines to Block (beta)", this.width / 2 - 60, 160, selectedConfig.drawTracer, (checkbox, selected) -> {
                            if (selectedConfig != null) {
                                selectedConfig.drawTracer = selected;
                            }
                        });
                        this.addRenderableWidget(drawLinesCheckbox);
                    }
                    return true;
                }

                currentX += itemWidth + horizontalPadding;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            blockDropdown.handleMouseRelease();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void renderActivePool(GuiGraphics context, int mouseX, int mouseY) {
        int startX = 10;
        int startY = 60;
        int itemHeight = 24;
        int rowHeight = 28;
        int horizontalPadding = 6;

        int currentX = startX;
        int currentY = startY;
        int maxWidth = this.width - 10;

        context.drawString(this.font, Component.literal("Active Finders:"), startX, startY, white);
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

            context.renderItem(new ItemStack(config.block), currentX + 4, currentY + 4);
            context.drawString(this.font, name, currentX + 24, currentY + (itemHeight - 8) / 2, white);

            int colorX = currentX + 24 + textWidth + 6; // auto calc based on length of name
            context.fill(colorX, currentY + 6, colorX + 12, currentY + 18, colorUtils.arrayToInt(config.color));

            int closeX = colorX + 18;
            context.drawString(this.font, "x", closeX, currentY + (itemHeight - 8) / 2, 0xFFFF5555);

            currentX += itemWidth + horizontalPadding;
        }
    }

    private void renderSubmenuBackground(GuiGraphics context) {
        int panelWidth = 260;
        int panelHeight = 100;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 100;

        // background for submenu
        context.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, black);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, darkTranslucentGray);
    }

    private void renderSubmenu(GuiGraphics context) {
        if (selectedConfig != null) {
            radiusSizeBox.setX(this.width / 2 - 120); radiusSizeBox.setY(125);
            minYBox.setX(this.width / 2 + 20); minYBox.setY(125);
            maxYBox.setX(this.width / 2 + 70); maxYBox.setY(125);

            context.drawCenteredString(this.font, Component.literal("Editing: " + selectedConfig.block.getName().getString()), this.width / 2, 210, 0xFFFFFF55);
            context.drawCenteredString(this.font, Component.literal("Radius:"), this.width / 2 - 60, 110, white);
            context.drawCenteredString(this.font, Component.literal("Min Y  /  Max Y:"), this.width / 2 + 65, 110, white);

            drawLinesCheckbox.setX(this.width / 2 - 60);
            drawLinesCheckbox.setY(160);
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

            this.removeWidget(drawLinesCheckbox);
            drawLinesCheckbox = guiUtils.createCheckbox(this, "Draw Lines to Block (beta)", this.width / 2 - 60, 160, selectedConfig.drawTracer, (checkbox, selected) -> {
                if (selectedConfig != null) {
                    selectedConfig.drawTracer = selected;
                }
            });
            this.addRenderableWidget(drawLinesCheckbox);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // head hurts from background convering up GuiGraphics render components
        // found this solution where you remove .super()'s auto blur background
        // and call it first and then flush it and start new
        renderBackground(context, mouseX, mouseY, delta);
        context.flush(); 

        context.drawCenteredString(this.font, Component.literal("Block Finder"), this.width / 2, 10, white);

        renderActivePool(context, mouseX, mouseY);
        context.flush();

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

        radiusSizeBox.setVisible(usingSubmenu); // seen in submenu
        minYBox.setVisible(usingSubmenu);
        maxYBox.setVisible(usingSubmenu);
        
        if (drawLinesCheckbox != null) {
            drawLinesCheckbox.visible = usingSubmenu;
        }
        
        // these are hidden when submenu opens
        if (submitButton != null) {
            submitButton.visible = !usingSubmenu;
        }
        if (clearButton != null) {
            clearButton.visible = !usingSubmenu;
        }

        if (usingSubmenu) {
            context.pose().pushPose();
            context.pose().translate(0, 0, 300); // moves submenu background higher on z-axis

            renderSubmenuBackground(context);

            context.pose().popPose();
            context.flush();

            context.pose().pushPose();
            context.pose().translate(0, 0, 400);

            renderSubmenu(context);

            context.pose().popPose();
            context.flush();

            // not calling super.render() so I have to manually add these
            context.pose().pushPose();
            context.pose().translate(0, 0, 500);

            radiusSizeBox.render(context, mouseX, mouseY, delta);
            minYBox.render(context, mouseX, mouseY, delta);
            maxYBox.render(context, mouseX, mouseY, delta);
            drawLinesCheckbox.render(context, mouseX, mouseY, delta);

            context.pose().popPose();
            context.flush();
        }

        context.pose().pushPose();
        context.pose().translate(0, 0, 500);

        if (submitButton != null) {
            submitButton.render(context, mouseX, mouseY, delta);
        }
        if (clearButton != null) {
            clearButton.render(context, mouseX, mouseY, delta);
        }

        if (autoRescanCheckbox != null) {
            autoRescanCheckbox.render(context, mouseX, mouseY, delta);
        }
        if (showHUDCheckbox != null) {
            showHUDCheckbox.render(context, mouseX, mouseY, delta);
        }
        if (loadButton != null) {
            loadButton.render(context, mouseX, mouseY, delta);
        }
        if (saveButton != null) {
            saveButton.render(context, mouseX, mouseY, delta);
        }

        context.pose().popPose();
        context.flush();

        // dropdown should probably be absolute highest z-index
        if (blockDropdown != null) {
            context.pose().pushPose();
            context.pose().translate(0, 0, 600);

            blockDropdown.render(context, mouseX, mouseY, delta);

            context.pose().popPose();
            context.flush();
        }

        if (blockDropdown.getSearchBox() != null) {
            context.pose().pushPose();
            context.pose().translate(0, 0, 700);

            // fix search box not showing flashing cursor
            blockDropdown.getSearchBox().render(context, mouseX, mouseY, delta);

            context.pose().popPose();
            context.flush();
        }

        blockDropdown.handleMouseDrag(mouseY);

        // super.render(context, mouseX, mouseY, delta);
    }
}
