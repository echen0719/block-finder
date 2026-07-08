package echen0719.blockfinder.utils;

public class colorUtils {
    public static int arrayToInt(Object[] color) {
        int r = (Integer) color[0];
        int g = (Integer) color[1];
        int b = (Integer) color[2];

        float alpha = (Float) color[3];
        int a = Math.round(alpha * 255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
