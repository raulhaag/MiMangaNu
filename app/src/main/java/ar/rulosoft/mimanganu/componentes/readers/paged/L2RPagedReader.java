package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Collections;
import java.util.List;

/**
 * Created by Raul on 26/06/2016.
 */
public class L2RPagedReader extends HorizontalPagedReader {

    public L2RPagedReader(Context context) {
        super(context);
    }

    public L2RPagedReader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public L2RPagedReader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void addOnPageChangeListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (pageChangeListener != null) {
                    pageChangeListener.onPageChanged(paths.size() - position - 1);
                }
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        if (!mPageAdapter.getCurrentPage().canScroll(Math.round(x - mStartDragX))) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartDragX = x;
                    firedListener = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mOnEndFlingListener != null && isFirstPage() && mStartDragX < x && !firedListener) {
                        mOnEndFlingListener.onEndFling();
                        firedListener = true;
                    } else if (mOnBeginFlingListener != null && isLastPage() && mStartDragX > x && !firedListener) {
                        mOnBeginFlingListener.onBeginFling();
                        firedListener = true;
                    }
                    break;
            }
        }
        return false;
    }

    public boolean isLastPage(){
        return (paths.size() - 1) == mViewPager.getCurrentItem();
    }

    public boolean isFirstPage(){
        return 0 == mViewPager.getCurrentItem();
    }

    @Override
    public void setPaths(List<String> paths) {
        Collections.reverse(paths);
        this.paths = paths;
        setPagerAdapter(new PageAdapter());
    }

    @Override
    public void goToPage(int aPage) {
        super.goToPage(paths.size() - aPage + 1);
    }

    @Override
    public int getCurrentPage() {
        return paths.size() - super.getCurrentPage() - 1;
    }
}
