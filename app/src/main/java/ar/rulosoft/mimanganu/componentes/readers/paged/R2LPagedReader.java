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
                mPageAdapter.setCurrentPage(position);
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
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (readerListener != null)
            if (e.getX() < getWidth() / 4) {
                if (currentPage == 0) {
                    if (readerListener != null) {
                        readerListener.onStartOver();
                    }
                } else {
                    mViewPager.setCurrentItem(currentPage - 1);
                }
            } else if (e.getX() > getWidth() / 4 * 3) {
                if (currentPage == paths.size() - 1) {
                    if (readerListener != null) {
                        readerListener.onEndOver();
                    }
                } else {
                    mViewPager.setCurrentItem(currentPage + 1);
                }
            } else {
                readerListener.onMenuRequired();
            }
        return false;
    }

    @Override
    public void onStartOver() {
        if(readerListener != null){
            readerListener.onStartOver();
        }
    }

    @Override
    public void onEndOver() {
        if(readerListener != null){
            readerListener.onEndOver();
        }
    }
}