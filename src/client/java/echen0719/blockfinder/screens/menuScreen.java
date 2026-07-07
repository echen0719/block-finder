package echen0719.blockfinder.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import echen0719.blockfinder.client.BlockScanner;
import echen0719.blockfinder.utils.guiUtils;

public class menuScreen extends Screen {
    // gui componenets
    private EditBox chunkSizeBox;
    private searchableDropdown blockDropdown;
    private Button submitButton;

    // static values
    private static String savedChunkSize = "";
    private static Block savedBlock = null;

    public menuScreen() {
        super(Component.literal("Block Finder"));
    }

    public void createInputs() {
        chunkSizeBox = guiUtils.createInputBox(this, 10, 10, 100, 20, "Chunk size");
        blockDropdown = new searchableDropdown(this, 10, 40, 150, 20, "Block ID");

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

            try {
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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        blockDropdown.setContext(guiGraphics);

        if (submitButton != null && blockDropdown != null) {
            submitButton.setY(blockDropdown.getDropdownBottomY() + 10);
        }

        blockDropdown.handleMouseDrag(mouseY);

        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }
}
