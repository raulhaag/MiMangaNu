package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import ar.rulosoft.mimanganu.ActivityPagedReader.PageAdapter;

public class UnScrolledViewPager extends ViewPager {

    public UnScrolledViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        try {
            return  ((PageAdapter) getAdapter()).getCurrentPage().canScroll(dx);
        } catch (Exception ignored) {
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}