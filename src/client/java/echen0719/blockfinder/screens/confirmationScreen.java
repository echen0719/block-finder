package echen0719.blockfinder.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import echen0719.blockfinder.utils.guiUtils;

public class confirmationScreen extends Screen {
    private menuScreen parent;

    // gui components
    private EditBox renameInputBox;
    private Button cancelButton;
    private Button confirmButton;

    // layouts constants
    private int padding = 16;
    private int widgetHeight = 20;

    // colors
    private final int white = 0xFFFFFFFF;
    private final int gray = 0xFFAAAAAA;
    private final int black = 0xFF000000;

    public confirmationScreen(menuScreen parent) {
        super(Component.literal("Edit Save File"));
        this.parent = parent;
    }

    private void createDialogue() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 100;
        int totalWidth = buttonWidth * 2 + padding;

        renameInputBox = guiUtils.createInputBox(this, centerX - totalWidth / 2, centerY - padding, totalWidth - 40, widgetHeight, "Enter a new file name...");
        this.addRenderableWidget(renameInputBox);

        cancelButton = guiUtils.createButton(this, "Cancel", centerX - totalWidth / 2, centerY + padding, buttonWidth, widgetHeight, button -> {
            Minecraft.getInstance().setScreenAndShow(parent);
        });
        this.addRenderableWidget(cancelButton);

        confirmButton = guiUtils.createButton(this, "Confirm", centerX + padding / 2, centerY + padding, buttonWidth, widgetHeight, button -> {
            String newFileName = renameInputBox.getValue().trim();

            if (newFileName.isEmpty()) {
                return;
            }

            parent.saveToFile(newFileName + ".json");
            
            Minecraft.getInstance().setScreenAndShow(parent);
        });
        this.addRenderableWidget(confirmButton);
    } // next thing to do is check for overwrite

    @Override
    public void init() {
        super.init();
        this.clearWidgets();
        
        createDialogue();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) { 
        super.extractRenderState(context, mouseX, mouseY, delta);

        // don't know why i am completely basing off of renameInputBox but whatever
        context.centeredText(this.font, ".json", renameInputBox.getX() + renameInputBox.getWidth() + 20, renameInputBox.getY() + 5, white);
        context.text(this.font, "Enter a file name to save as: ", renameInputBox.getX(), renameInputBox.getY() - padding, white);
    }
}