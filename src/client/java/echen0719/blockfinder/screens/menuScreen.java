package echen0719.blockfinder.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import echen0719.blockfinder.client.BlockDrawer;
import echen0719.blockfinder.client.BlockScanner;
import echen0719.blockfinder.utils.guiUtils;
import echen0719.blockfinder.utils.colorUtils;

public class menuScreen extends Screen {
    // gui componenets
    private EditBox chunkSizeBox;
    private searchableDropdown blockDropdown;
    private Button submitButton;

    // colors
    private final int white = 0xFFFFFFFF;

    // layout constants
    private int colorStartX = 220;
    private int colorStartY = 60;
    private int colorSize = 20;

    // static values
    private static String savedChunkSize = "";
    private static Block savedBlock = null;
    private static Object[] savedColor = {255, 0, 0, 0.5f};

    public menuScreen() {
        super(Component.literal("Block Finder"));
    }

    public void createInputs() {
        chunkSizeBox = guiUtils.createInputBox(this, 10, 30, 100, 20, "Enter chunk size...");
        blockDropdown = new searchableDropdown(this, 10, 60, 200, 20, "Block name");

        this.addRenderableWidget(chunkSizeBox);
        this.addRenderableWidget(blockDropdown);
        this.addRenderableWidget(blockDropdown.getSearchBox());
    }

    public void createButtons() {
        submitButton = guiUtils.createButton(this, "Submit", 10, 70, 100, 20, button -> {
            String chunkSize = chunkSizeBox.getValue().trim();
            Block block = blockDropdown.getSelectedBlock();

            if (chunkSize.isEmpty() || block == null) {
				System.out.println("Fill in the fields before scanning.");
                return;
	    	}

            savedChunkSize = chunkSize;
            savedBlock = block;
            // colors

            try {
                BlockDrawer.setColor(savedColor);
                BlockScanner.scan(Integer.parseInt(chunkSize), block);
            }
            catch (NumberFormatException e) {
                chunkSizeBox.setValue("");
            }
        });

        this.addRenderableWidget(submitButton);
    }

    // using previous code from serverscan
    private boolean onMouseScroll(Screen screen, double mouseX, double mouseY, double deltaX, double deltaY, boolean consumed) {
        blockDropdown.handleScroll(mouseX, mouseY, deltaY);
        return true;
    }

    private boolean onMouseClick(Screen screen, MouseButtonEvent event, boolean consumed) {
        if (event.button() == 0) {
            if (event.x() >= colorStartX && event.x() <= colorStartX + colorSize &&
                event.y() >= colorStartY && event.y() <= colorStartY + colorSize) {
                
                Minecraft.getInstance().setScreenAndShow(new colorPicker(this, savedColor));
                return true;
            }
            return blockDropdown.onItemClick(event.x(), event.y()) || consumed;
        }
        return consumed;
    }

    private boolean onMouseRelease(Screen screen, MouseButtonEvent event, boolean consumed) {
        if (event.button() == 0) {
            blockDropdown.handleMouseRelease();
        }
        return consumed;
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        createInputs();
        createButtons();

        chunkSizeBox.setValue(savedChunkSize);
        if (savedBlock != null) {
            blockDropdown.setSelectedBlock(savedBlock);
        }

        ScreenMouseEvents.afterMouseScroll(this).register((ScreenMouseEvents.AfterMouseScroll) this::onMouseScroll);
        ScreenMouseEvents.afterMouseClick(this).register((ScreenMouseEvents.AfterMouseClick) this::onMouseClick);
        ScreenMouseEvents.afterMouseRelease(this).register((ScreenMouseEvents.AfterMouseRelease) this::onMouseRelease);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        
        blockDropdown.setContext(context);

        if (submitButton != null && blockDropdown != null) {
            submitButton.setY(blockDropdown.getDropdownBottomY() + 10);
        }

        blockDropdown.handleMouseDrag(mouseY);

        context.centeredText(this.font, Component.literal("Block Finder"), this.width / 2, 10, white);

        // color picker
        context.fill(colorStartX - 1, colorStartY - 1, colorStartX + colorSize + 1, colorStartY + colorSize + 1, 0xFF000000);
        context.fill(colorStartX, colorStartY, colorStartX + colorSize, colorStartY + colorSize, colorUtils.arrayToInt(savedColor));
    }
}
