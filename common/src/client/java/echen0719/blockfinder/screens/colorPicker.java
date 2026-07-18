package echen0719.blockfinder.screens;

import echen0719.blockfinder.utils.guiUtils;
import echen0719.blockfinder.utils.guiUtils.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class colorPicker extends Screen {
    private Screen parent;

    // gui componenets
    private EditBox redBox;
    private EditBox greenBox;
    private EditBox blueBox;
    private EditBox alphaBox;

    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;
    private Slider alphaSlider;

    // values
    private Object[] color;

    // colors
    private static int white = 0xFFFFFFFF;

    // layout constants
    private int centerX;
    private int centerY;
    private int labelX;
    private int sliderX;
    private int boxX;

    private GuiGraphicsExtractor context;
    private Minecraft client = Minecraft.getInstance();

    public colorPicker(Screen parent, Object[] color) {
        super(Component.literal("Color Picker"));
        this.parent = parent;
        this.color = color;
    }

    public void createInputs() {
        int alphaPercentage = 50; // middle down the road
        if (color != null && color.length == 4 || color[3] != null) {
            alphaPercentage = (int) (((Number) color[3]).floatValue() * 100);
        }

        redSlider = guiUtils.createSlider(sliderX, centerY - 45, 100, 20, "Red", (int) color[0], 255, () -> {
            color[0] = redSlider.getIntValue();
            redBox.setValue(String.valueOf(color[0]));
        });
        greenSlider = guiUtils.createSlider(sliderX, centerY - 15, 100, 20, "Green", (int) color[1], 255, () -> {
            color[1] = greenSlider.getIntValue();
            greenBox.setValue(String.valueOf(color[1]));
        });
        blueSlider = guiUtils.createSlider(sliderX, centerY + 15, 100, 20, "Blue", (int) color[2], 255, () -> {
            color[2] = blueSlider.getIntValue();
            blueBox.setValue(String.valueOf(color[2]));
        });
        alphaSlider = guiUtils.createSlider(sliderX, centerY + 45, 100, 20, "Alpha", (int) alphaPercentage, 100, () -> {
            color[3] = alphaSlider.getIntValue() / 100.0f;
            alphaBox.setValue(String.valueOf(color[3]));
        });

        redBox = guiUtils.createInputBox(this, boxX, centerY - 45, 50, 20, "0-255");
        greenBox = guiUtils.createInputBox(this, boxX, centerY - 15, 50, 20, "0-255");
        blueBox = guiUtils.createInputBox(this, boxX, centerY + 15, 50, 20, "0-255");
        alphaBox = guiUtils.createInputBox(this, boxX, centerY + 45, 50, 20, "0-1");

        this.addRenderableWidget(redSlider);
        this.addRenderableWidget(greenSlider);
        this.addRenderableWidget(blueSlider);
        this.addRenderableWidget(alphaSlider);

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

        centerX = this.width / 2;
        centerY = this.height / 2 - 30;
        labelX = centerX - 140;
        sliderX = centerX - 100;
        boxX = centerX + 10;

        createInputs();

        Button doneButton = guiUtils.createButton(this, "Done", centerX - 50, centerY + 90, 100, 20,
        button -> {
            setColor();
            Minecraft.getInstance().setScreenAndShow(parent);
        });
        this.addRenderableWidget(doneButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        if (color != null && color[0] != null && color[1] != null && color[2] != null && color[3] != null) {
            int r = ((Number) color[0]).intValue();
            int g = ((Number) color[1]).intValue();
            int b = ((Number) color[2]).intValue();
            int a = (int) (((Number) color[3]).floatValue() * 255f);

            int previewColor = (a << 24) | (r << 16) | (g << 8) | b; // create some integer from rgba

            int previewX = centerX + 100;
            int previewY = centerY - 25;
            context.fill(previewX, previewY, previewX + 50, previewY + 50, previewColor);
        }

        context.text(client.font, "Red:", labelX, centerY - 45, white);
        context.text(client.font, "Green:", labelX, centerY - 15, white);
        context.text(client.font, "Blue:", labelX, centerY + 15, white);
        context.text(client.font, "Alpha:", labelX, centerY + 45, white);
    }
}