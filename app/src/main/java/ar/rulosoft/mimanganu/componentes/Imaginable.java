package ar.rulosoft.mimanganu.componentes;

import android.graphics.Bitmap;

public interface Imaginable {
    void setImageBitmap(Bitmap b);

    void setImageResource(int id);

    void setAlpha(float alpha);
}
