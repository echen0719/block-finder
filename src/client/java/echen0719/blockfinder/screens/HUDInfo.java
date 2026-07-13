package echen0719.blockfinder.screens;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;

public class HUDInfo {
    // layout constants
    private static int boxWidth = 80;
    private static int boxHeight = 100;
    private static int margin = 5;
    
    private static int startX;
    private static int startY;

    // colors
    private static int white = 0xFFFFFFFF;
    private static int lightGray = 0xFF808080;
    private static int darkGray = 0xFF404040;
    private static int black = 0xFF000000;

    public static HudElement hudElement = (context, counter) -> {
        Minecraft client = Minecraft.getInstance();
        startX = context.guiWidth() - boxWidth - margin;
        startY = context.guiHeight() - boxHeight - margin;

        context.fill(startX - 1, startY - 1, startX + boxWidth + 1, startY + boxHeight + 1, black);
        context.fill(startX, startY, startX + boxWidth, startY + boxHeight, darkGray);
        context.centeredText(client.font, "Hello World!", startX + boxWidth / 2, startY + 5, 0xFFFFFFFF);
    };
}
