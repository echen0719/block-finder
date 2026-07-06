package echen0719.blockfinder.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import echen0719.blockfinder.client.BlockScanner;
import echen0719.blockfinder.utils.guiUtils;

public class menuScreen extends Screen {
    // gui componenets
    private EditBox chunkSizeBox;
    private EditBox blockBox;
    private Button submitButton;

    // static values
    private static String savedChunkSize = "";
    private static String savedBlock = "";

    public menuScreen() {
        super(Component.literal("Block Finder"));
    }

    public void createInputs() {
        chunkSizeBox = guiUtils.createInputBox(this, 10, 10, 100, 20, "Chunk size");
        blockBox = guiUtils.createInputBox(this, 10, 40, 100, 20, "Block ID");
    
        this.addRenderableWidget(chunkSizeBox);
        this.addRenderableWidget(blockBox);
    }

    public void createButtons() {
        submitButton = guiUtils.createButton(this, "Submit", 10, 70, 100, 20, button -> {
            String chunkSize = chunkSizeBox.getValue().trim();
            String block = blockBox.getValue().trim();

            if (chunkSize.isEmpty() || block.isEmpty()) {
				System.out.println("Fill in the fields before scanning.");
                return;
	    	}

            savedChunkSize = chunkSize;
            savedBlock = block;

            try {
                BlockScanner.scan(Integer.parseInt(chunkSize), Blocks.DEEPSLATE_DIAMOND_ORE);
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
        blockBox.setValue(savedBlock);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }
}
