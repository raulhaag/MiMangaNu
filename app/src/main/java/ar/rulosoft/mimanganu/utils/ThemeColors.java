package ar.rulosoft.mimanganu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/**
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
    public static int[] getColors( SharedPreferences sp, Context context ) {
        int[] colors = new int[4];
        colors[0] = sp.getInt( "primario", Color.parseColor( "#607D8B" ) );
        colors[1] = sp.getInt( "secundario", Color.parseColor( "#009688" ) );
        if ( colors[0] == 0 ) {
            colors[0] = Color.parseColor( "#607D8B" );
            colors[1] = Color.parseColor( "#009688" );
        }
        colors[2] = ( 31 << 24 ) | ( colors[0] & 0x00ffffff );
        colors[3] = ( colors[1] ) & 0xFFCCCCCC;
        return colors;
    }
}