package ar.rulosoft.mimanganu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import ar.rulosoft.mimanganu.R;

/**
 * Get the colors from preference and set them here.
 * <p/>
 * Created by Raul on 02/05/2015.
 */
public class ThemeColors {
    /**
     * traer colores de la interfaz
     * [0]principal
     * [2]secundario
     * [3]backgrounds
     * [4]pressed
     */
    public static int[] getColors(SharedPreferences sp, Context context) {
        String[] colorCode = context.getResources().getStringArray(R.array.color_codes);
        int[] colors = new int[4];
        colors[0] = Color.parseColor(colorCode[sp.getInt("primario", 5)]);
        colors[1] = Color.parseColor(colorCode[sp.getInt("secundario", 6)]);
        if (colors[0] == 0) {
            colors[0] = Color.parseColor(colorCode[5]);
            colors[1] = Color.parseColor(colorCode[6]);
        }
        colors[2] = (31 << 24) | (colors[0] & 0x00ffffff);
        colors[3] = (colors[1]) & 0xFFCCCCCC;
        return colors;
    }
}