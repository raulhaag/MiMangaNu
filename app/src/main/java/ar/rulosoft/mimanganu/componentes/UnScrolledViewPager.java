package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import ar.rulosoft.mimanganu.ActivityLector.PageFragment;
import ar.rulosoft.mimanganu.ActivityLector.SectionsPagerAdapter;

public class UnScrolledViewPager extends ViewPager {

    public UnScrolledViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        try {
            return ((PageFragment) ((SectionsPagerAdapter) getAdapter()).getCurrentFragment()).canScroll(dx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}