package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import ar.rulosoft.mimanganu.ActivityLector.PlaceholderFragment;
import ar.rulosoft.mimanganu.ActivityLector.SectionsPagerAdapter;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public class UnescroledViewPagerVertical extends VerticalViewPager {

    public UnescroledViewPagerVertical(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        try {
            return ((PlaceholderFragment) ((SectionsPagerAdapter) getAdapter()).getCurrentFragment()).canScrollV(dx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}