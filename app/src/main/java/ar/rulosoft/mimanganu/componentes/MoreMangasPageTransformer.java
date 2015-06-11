package ar.rulosoft.mimanganu.componentes;

import android.support.v4.view.ViewPager;
import android.view.View;

public class MoreMangasPageTransformer implements ViewPager.PageTransformer {
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        if (position < -1) { // [-Infinity,-1)
            view.setAlpha(0);
        } else if (position <= 0) { // [-1,0]
            view.setTranslationX(pageWidth * -position);
        } else if (position <= 1) { // (0,1]
        } else { // (1,+Infinity]
            view.setAlpha(0);
        }
    }
}