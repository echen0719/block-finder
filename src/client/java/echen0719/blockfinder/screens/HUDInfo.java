package echen0719.blockfinder.screens;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import echen0719.blockfinder.screens.menuScreen;
import echen0719.blockfinder.client.BlockScanner;
import echen0719.blockfinder.screens.blockConfig;

public class HUDInfo {
    // toggle state
    public static boolean showHUD = true;

    // layout constants
    private static int startX;
    private static int startY;

    // colors
    private static int white = 0xFFFFFFFF;
    private static int lightGray = 0xFF808080;
    private static int darkGray = 0xFF404040;
    private static int black = 0xFF000000;

    public static HudElement hudElement = (context, counter) -> {
        Minecraft client = Minecraft.getInstance();

        if (!showHUD || menuScreen.activePool.isEmpty()) {
            return;
        }

        int itemHeight = 24;
        int headerHeight = 20; // for "Found Blocks: " text

        int boxHeight = headerHeight + (menuScreen.activePool.size() * itemHeight) + 5;
        int boxWidth = 100;

        for (blockConfig config : menuScreen.activePool) {
            String name = config.block.getName().getString();
            String foundCount = "0";

            List<BlockPos> positions = BlockScanner.foundBlocks.get(config.block);
            if (positions != null) {
                foundCount = String.valueOf(positions.size());
            }

            int rowWidth = 24 + client.font.width(name) + 16 + client.font.width(foundCount) + 4;
            if (rowWidth > boxWidth) { // find maximum width
                boxWidth = rowWidth;
            }
        }

        startX = context.guiWidth() - boxWidth - 5;
        startY = context.guiHeight() - boxHeight - 5;

        context.fill(startX - 1, startY - 1, startX + boxWidth + 1, startY + boxHeight + 1, black);
        context.fill(startX, startY, startX + boxWidth, startY + boxHeight, darkGray);
        context.centeredText(client.font, "Found Blocks: ", startX + boxWidth / 2, startY + 5, 0xFFFFFFFF);

        int currentY = startY + headerHeight;

        // pasted over from menuScreen
        for (blockConfig config : menuScreen.activePool) {
            String name = config.block.getName().getString();
            String foundCount = "0";

            List<BlockPos> positions = BlockScanner.foundBlocks.get(config.block);
            if (positions != null) {
                foundCount = String.valueOf(positions.size());
            }

            context.item(config.stack, startX + 4, currentY + 4);
            context.text(client.font, name, startX + 24, currentY + (itemHeight - 8) / 2, white);

            int countWidth = client.font.width(foundCount);
            context.text(client.font, foundCount, startX + boxWidth - countWidth - 6, currentY + (itemHeight - 8) / 2, white);

            currentY += itemHeight;
        }
    };
}
