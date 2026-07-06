package echen0719.blockfinder.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

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
                if (false) {
                    System.out.println("I'm not rendering air");
                    return;
                }

                BlockScanner.scan(Integer.parseInt(chunkSize), block);
            }
            catch (NumberFormatException e) {
                chunkSizeBox.setValue("");
            }
        });

        this.addRenderableWidget(submitButton);
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        createInputs();
        createButtons();

        chunkSizeBox.setValue(savedChunkSize);
        // ...setValue(savedBlock);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }
}
