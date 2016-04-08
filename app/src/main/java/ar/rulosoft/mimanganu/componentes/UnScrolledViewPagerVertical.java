package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import ar.rulosoft.mimanganu.ActivityPagedReader;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public class UnScrolledViewPagerVertical extends VerticalViewPager {

    public UnScrolledViewPagerVertical(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        try {
            return ((ActivityPagedReader.PageAdapter) getAdapter()).getCurrentPage().canScrollV(dx);
        } catch (Exception ignored) {
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}