package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Raul on 27/06/2016.
 */
public class R2LPagedReader extends HorizontalPagedReader {

    public R2LPagedReader(Context context) {
        super(context);
    }

    public R2LPagedReader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public R2LPagedReader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isLastPage() {
        return 0 == mViewPager.getCurrentItem();
    }

    public boolean isFirstPage() {
        return (paths.size() - 1) == mViewPager.getCurrentItem();
    }

    public void addOnPageChangeListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (pageChangeListener != null) {
                    pageChangeListener.onPageChanged(position);
                }
                currentPage = position;
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
                    if (mOnBeginFlingListener != null && isLastPage() && mStartDragX < x && !firedListener) {
                        mOnBeginFlingListener.onBeginFling();
                        firedListener = true;
                    } else if (mOnEndFlingListener != null && isFirstPage() && mStartDragX > x && !firedListener) {
                        mOnEndFlingListener.onEndFling();
                        firedListener = true;
                    }
                    break;
            }
        }
        return false;
    }
}
