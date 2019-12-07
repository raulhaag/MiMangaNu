package ar.rulosoft.mimanganu.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class PostProcess {
    public static final String FLAG_PPL90 = "[L90]";

    public static boolean l90(String filename) {
        Bitmap myImg = BitmapFactory.decodeFile(filename);
        int maxH = myImg.getHeight();
        int maxW = myImg.getWidth();
        int x = 0;
        int y = 0;
        Bitmap result = Bitmap.createBitmap(maxW, maxH, Bitmap.Config.ARGB_8888);
        Canvas rstImage = new Canvas(result);
        Rect a;
        Rect b;
        while (y < (maxH - 200)) {
            a = new Rect(0, y, maxW, y + 100);
            b = new Rect(0, y + 100, maxW, y + 200);
            rstImage.drawBitmap(myImg, a, b, null);
            rstImage.drawBitmap(myImg, b, a, null);
            y += 200;
        }

        if (y >= maxH - 200) {
            a = new Rect(0, y, maxW, maxH);
            rstImage.drawBitmap(myImg, a, a, null);
        }
        myImg = result;
        result = Bitmap.createBitmap(maxW, maxH, Bitmap.Config.ARGB_8888);
        rstImage = new Canvas(result);
        while (x < (maxW - 200)) {
            a = new Rect(x, 0, x + 100, maxH);
            b = new Rect(x + 100, 0, x + 200, maxH);
            rstImage.drawBitmap(myImg, a, b, null);
            rstImage.drawBitmap(myImg, b, a, null);
            x += 200;
        }

        if (x >= maxW - 200) {
            a = new Rect(x, 0, maxW, maxH);
            rstImage.drawBitmap(myImg, a, a, null);
        }

        try {
            FileOutputStream f = new FileOutputStream(filename);
            result.compress(Bitmap.CompressFormat.JPEG, 100, f);
            f.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
