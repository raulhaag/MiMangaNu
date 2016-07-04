package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;

/**
 * Created by Raul on 27/06/2016.
 */
public class R2LPagedReader extends HorizontalPagedReader {

    public R2LPagedReader(Context context) {
        super(context);
    }

    @Override
    protected int transformPage(int page) {
        return 0;
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
                if (readerListener != null) {
                    readerListener.onPageChanged(position);
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
                    if (readerListener != null && isLastPage() && mStartDragX < x && !firedListener) {
                        readerListener.onStartOver();
                        firedListener = true;
                    } else if (readerListener != null && isFirstPage() && mStartDragX > x && !firedListener) {
                        readerListener.onEndOver();
                        firedListener = true;
                    }
                    break;
            }
        }
        return false;
    }



    @Override
    public void onLeftTap() {
        if(currentPage == 0){
            if(readerListener != null){
                readerListener.onStartOver();
            }
        }else{
            mViewPager.setCurrentItem(currentPage - 1);
        }
    }

    @Override
    public void onRightTap() {
        if(currentPage == paths.size() - 1){
            if(readerListener != null){
                readerListener.onEndOver();
            }
        }else{
            mViewPager.setCurrentItem(currentPage + 1);
        }
    }


}