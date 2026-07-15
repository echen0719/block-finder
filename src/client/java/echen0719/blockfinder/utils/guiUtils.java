package echen0719.blockfinder.utils;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Checkbox.Builder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class guiUtils {
    public static EditBox createInputBox(Screen screen, int x, int y, int width, int height, String hint) {
        EditBox box = new EditBox(screen.getFont(), x, y, width, height, Component.literal(""));
        box.setHint(Component.literal(hint));
        return box;
    }

    public static Button createButton(Screen screen, String label, int x, int y, int width, int height, Button.OnPress action) {
        Button button = Button.builder(Component.literal(label), action).bounds(x, y, width, height).build();
        return button;
    }

    public static Checkbox createCheckbox(Screen screen, String label, int x, int y, boolean value, Checkbox.OnValueChange onValueChange) {
        Builder build = Checkbox.builder(Component.literal(label), screen.getFont()).pos(x, y);
        Checkbox checkbox = build.selected(value).onValueChange(onValueChange).build();
        return checkbox;
    }

    public static class Slider extends AbstractSliderButton {
        private String label;
        private int maxVal;
        private Runnable onChange;

        public Slider(int x, int y, int width, int height, String label, int initialValue, int maxVal, Runnable onChange) {
            super(x, y, width, height, Component.literal(label), (double) initialValue / maxVal);
            this.label = label;
            this.maxVal = maxVal;
            this.onChange = onChange;
            this.updateMessage();
        }
        
        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(label));
        }

        @Override
        protected void applyValue() {
            if (this.onChange != null) {
                this.onChange.run();
            }
        }

        public int getIntValue() {
            return (int) Math.round(value * maxVal);
        }

        public void setIntValue(int val) {
            double newValue = (double) Math.max(0, Math.min(maxVal, val)) / maxVal;
            if (this.value != newValue) {
                this.value = newValue;
                this.updateMessage();
            }
        }
    }

    public static Slider createSlider(int x, int y, int width, int height, String label, int initialValue, int maxVal, Runnable onChange) {
        return new Slider(x, y, width, height, label, initialValue, maxVal, onChange);
    }
}