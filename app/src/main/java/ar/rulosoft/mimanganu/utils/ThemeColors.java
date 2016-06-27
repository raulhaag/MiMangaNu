package ar.rulosoft.mimanganu.utils;

import android.content.SharedPreferences;
import android.graphics.Color;

/**
 * Get the colors from preference and set them here.
 * <p/>
 * Created by Raul on 02/05/2015.
 */
public class ThemeColors {
    /**
     * traer colores de la interfaz
     * [0] principal
     * [1] secundario
     * [2] backgrounds
     * [3] pressed
     * [4] dark
     */
    public static int[] getColors(SharedPreferences sp) {
        int[] colors = new int[5];
        colors[0] = sp.getInt("primario", Color.parseColor("#2A52BE"));
        colors[1] = sp.getInt("secundario", Color.parseColor("#1E90FF"));
        if (colors[0] == 0) {
            colors[0] = Color.parseColor("#2A52BE");
            colors[1] = Color.parseColor("#1E90FF");
        }
        colors[2] = (31 << 24) | (colors[0] & 0x00ffffff);
        colors[3] = (colors[1]) & 0xFFCCCCCC;
        colors[4] = brightnessColor(colors[0], 0.81f); // 700
        return colors;
    }

    /**
     * Returns reader bg color, which should be used separately from other colors
     * because if you read mangas, maybe you don't want bright colors unlike the menus colors
     *
     * @param sp Preferences
     * @return integer
     */
    public static int getReaderColor(SharedPreferences sp) {
        return sp.getInt("reader_bg_col", Color.parseColor("#100C08"));
    }

    /**
     * Returns the brightness of a color
     *
     * @param color integer
     * @return brightness (0 - 255)
     */
    public static int brightness(int color) {
        return (int) Math.sqrt(Color.red(color) * Color.red(color) * .241 +
                Color.green(color) * Color.green(color) * .691 +
                Color.blue(color) * Color.blue(color) * .068);
    }

    /**
     * Make use of HSV and brighten up or darken down the color
     *
     * @param color  integer
     * @param factor 0..1 to darken and 1..inf to brighten color
     * @return changed color
     */
    public static int brightnessColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }

    /**
     * Same as brightnessColor, but only change color, if color brightness is below tolerate value
     *
     * @param color    input color
     * @param tolerate tolerate value, usually 0..255
     * @return changed color, if below tolerate value
     */
    public static int brightenColor(int color, int tolerate) {
        int colorBright = brightness(color);
        if (colorBright < tolerate)
            return brightnessColor(color, (tolerate + 10.0f) / (colorBright + 10.0f));
        return color;
    }
}
