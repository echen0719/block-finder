package echen0719.blockfinder.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import echen0719.blockfinder.utils.guiUtils;

public class colorPicker extends Screen {
    private Screen parent;

    // gui componenets
    private EditBox redBox;
    private EditBox greenBox;
    private EditBox blueBox;
    private EditBox alphaBox;

    // values
    private Object[] color;

    // colors
    private static int white = 0xFFFFFFFF;

    private GuiGraphicsExtractor context;
    private Minecraft client = Minecraft.getInstance();

    public colorPicker(Screen parent, Object[] color) {
        super(Component.literal("Color Picker"));
        this.parent = parent;
        this.color = color;
    }

    public void createInputs() {
        redBox = guiUtils.createInputBox(this, this.width / 2 + 15, 40, 50, 20, "0-255");
        greenBox = guiUtils.createInputBox(this, this.width / 2 + 15, 70, 50, 20, "0-255");
        blueBox = guiUtils.createInputBox(this, this.width / 2 + 15, 100, 50, 20, "0-255");
        alphaBox = guiUtils.createInputBox(this, this.width / 2 + 15, 130, 50, 20, "0-1");

        this.addRenderableWidget(redBox);
        this.addRenderableWidget(greenBox);
        this.addRenderableWidget(blueBox);
        this.addRenderableWidget(alphaBox);

        if (color != null && color.length == 4) {
            redBox.setValue(String.valueOf(color[0]));
            greenBox.setValue(String.valueOf(color[1]));
            blueBox.setValue(String.valueOf(color[2]));
            alphaBox.setValue(String.valueOf(color[3]));
        }
    }

    private void setColor() {
        try { // should update menuScreen's savedColor as well
            int r = Math.max(0, Math.min(255, Integer.parseInt(redBox.getValue().trim())));
            int g = Math.max(0, Math.min(255, Integer.parseInt(greenBox.getValue().trim())));
            int b = Math.max(0, Math.min(255, Integer.parseInt(blueBox.getValue().trim())));
            float a = Math.max(0.0f, Math.min(1.0f, Float.parseFloat(alphaBox.getValue().trim())));
            
            color[0] = r;
            color[1] = g;
            color[2] = b;
            color[3] = a;
        } catch (NumberFormatException e) { // if not rgba then it returns to past value
            redBox.setValue(String.valueOf(color[0]));
            greenBox.setValue(String.valueOf(color[1]));
            blueBox.setValue(String.valueOf(color[2]));
            alphaBox.setValue(String.valueOf(color[3]));
        }
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        createInputs();

        Button doneButton = guiUtils.createButton(this, "Done", this.width / 2, 160, 100, 20,
        button -> {
            setColor();
            Minecraft.getInstance().setScreenAndShow(parent);
        });
        this.addRenderableWidget(doneButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        context.centeredText(client.font, "Red:", this.width / 2 - 15, 40, white);
        context.centeredText(client.font, "Green:", this.width / 2 - 15, 70, white);
        context.centeredText(client.font, "Blue:", this.width / 2 - 15, 100, white);
        context.centeredText(client.font, "Alpha:", this.width / 2 - 15, 130, white);
    }
}