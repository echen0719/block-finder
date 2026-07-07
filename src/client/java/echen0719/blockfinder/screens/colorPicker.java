package echen0719.blockfinder.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import echen0719.blockfinder.utils.guiUtils;

public class colorPicker extends AbstractWidget {
    private Screen parent;

    // gui componenets
    private EditBox redBox;
    private EditBox greenBox;
    private EditBox blueBox;
    private EditBox alphaBox;

    // values
    private int redValue;
    private int greenValue;
    private int blueValue;
    private float alphaValue;

    // colors
    private static int white = 0xFFFFFFFF;

    private GuiGraphicsExtractor context;
    private Minecraft client = Minecraft.getInstance();

    public colorPicker(Screen parent, int x, int y, int width, int height, String message) {
        super(x, y, width, height, Component.literal(message));
        this.parent = parent;

        createInputs();
    }

    public void setContext(GuiGraphicsExtractor context) {
        this.context = context;
    }

    // got kinda of lazy with this
    public Object[] getColor() {
        setColor();

        int r = Math.max(0, Math.min(255, redValue));
        int g = Math.max(0, Math.min(255, greenValue));
        int b = Math.max(0, Math.min(255, blueValue));
        float a = Math.max(0.0f, Math.min(1.0f, alphaValue));

        return new Object[]{r, g, b, a};
    }

    public void createInputs() {
        redBox = guiUtils.createInputBox(parent, this.getX() + 40, this.getY(), this.width, 20, "0-255");
        greenBox = guiUtils.createInputBox(parent, this.getX() + 40, this.getY() + 30, this.width, 20, "0-255");
        blueBox = guiUtils.createInputBox(parent, this.getX() + 40, this.getY() + 60, this.width, 20, "0-255");
        alphaBox = guiUtils.createInputBox(parent, this.getX() + 40, this.getY() + 90, this.width, 20, "0-1");
    }

    public EditBox[] getInputBoxes() {
        return new EditBox[]{redBox, greenBox, blueBox, alphaBox};
    }

    private void setColor() {
        try {
            redValue = Integer.parseInt(redBox.getValue().trim());
            greenValue = Integer.parseInt(greenBox.getValue().trim());
            blueValue = Integer.parseInt(blueBox.getValue().trim());
            alphaValue = Float.parseFloat(alphaBox.getValue().trim());
        }
        catch (NumberFormatException e) {
            redBox.setValue("");
            greenBox.setValue("");
            blueBox.setValue("");
            alphaBox.setValue("");
        }
    }

    public boolean onMouseClick(double mouseX, double mouseY) {
        for (EditBox box : getInputBoxes()) {
            if (box != null && box.isMouseOver(mouseX, mouseY)) {
                parent.setFocused(box);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        for (EditBox box : getInputBoxes()) {
            if (box != null) {
                box.updateWidgetNarration(narrationElementOutput);
            }
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.text(client.font, "Red:", this.getX(), this.getY(), white);
        context.text(client.font, "Green:", this.getX(), this.getY() + 30, white);
        context.text(client.font, "Blue:", this.getX(), this.getY() + 60, white);
        context.text(client.font, "Alpha:", this.getX(), this.getY() + 90, white);
    }
}