package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import ar.rulosoft.mimanganu.MainActivity;
import ar.rulosoft.mimanganu.R;

public class Shortcuts {
    public static void addShortCuts(Manga m, Context ctx) {
        if (m.getVault().isEmpty()) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                ShortcutManager sm = ctx.getSystemService(ShortcutManager.class);
                List<ShortcutInfo> sl = sm.getDynamicShortcuts();
                String icdir = prefs.getString("directorio",
                        Environment.getExternalStorageDirectory().getAbsolutePath()) + "/MiMangaNu/";
                icdir = icdir + "cache/";
                Bitmap bm = decodeFile(new File(icdir + m.getImages().hashCode()));
                Icon icon = null;

                if (bm == null) {
                    icon = Icon.createWithResource(ctx, R.drawable.noimage);
                } else {
                    icon = Icon.createWithAdaptiveBitmap(bm);
                }
                Intent i = new Intent(ctx, MainActivity.class);
                i.putExtra("manga_id", m.getId());
                i.setAction(Intent.ACTION_VIEW);
                ShortcutInfo s = new ShortcutInfo.Builder(ctx, m.getTitle() + m.getServerId())
                        .setShortLabel(m.getTitle())
                        .setIcon(icon)
                        .setIntent(i)
                        .build();
                if (!sl.contains(s)) {
                    sl.add(s);
                    sm.addDynamicShortcuts(Arrays.asList(s));
                }
                if (sl.size() > 4) {
                    sm.removeDynamicShortcuts(Arrays.asList(sl.get(0).getId()));
                }
            }
        }
    }

    private static Bitmap decodeFile(File put_file) {
        // if file not exist, skip everything
        if (!put_file.exists())
            return null;
        // We want Image to be equal or smaller than 200px height
        int tempSampleSize = 1, requiredSize = 400;
        try {
            BitmapFactory.Options bmpOpts = new BitmapFactory.Options();
            bmpOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(put_file.getAbsolutePath(), bmpOpts);
            while ((bmpOpts.outHeight / tempSampleSize) >= requiredSize) {
                tempSampleSize *= 2;
            }
            bmpOpts.inSampleSize = tempSampleSize;
            bmpOpts.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(put_file.getAbsolutePath(), bmpOpts);
        } catch (Exception e) {
            // usually file not found, but just ignore it
            return null;
        }
    }
}
