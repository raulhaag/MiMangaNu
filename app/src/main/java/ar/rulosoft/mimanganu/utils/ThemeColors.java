package ar.rulosoft.mimanganu.utils;

import android.content.Context;
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
     * [2] secundario
     * [3] backgrounds
     * [4] pressed
     */
    public static int[] getColors(SharedPreferences sp, Context context) {
        int[] colors = new int[4];
        colors[0] = sp.getInt("primario", Color.parseColor("#2A52BE"));
        colors[1] = sp.getInt("secundario", Color.parseColor("#1E90FF"));
        if (colors[0] == 0) {
            colors[0] = Color.parseColor("#2A52BE");
            colors[1] = Color.parseColor("#1E90FF");
        }
        colors[2] = (31 << 24) | (colors[0] & 0x00ffffff);
        colors[3] = (colors[1]) & 0xFFCCCCCC;
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
}