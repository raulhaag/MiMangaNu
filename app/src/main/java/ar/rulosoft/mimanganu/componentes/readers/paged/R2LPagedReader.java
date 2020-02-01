package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * Created by Raul on 27/06/2016.
 */
public class R2LPagedReader extends HorizontalPagedReader {

    public R2LPagedReader(Context context) {
        super(context);
    }

    public void addOnPageChangeListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (readerListener != null) {
                    readerListener.onPageChanged(transformPage(position));
                }
                currentPage = position + 1;
                if (mPageAdapter != null)
                    mPageAdapter.setCurrentPage(position);
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position + 1;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void seekPage(int aPage) {
        goToPage(aPage);
    }

    @Override
    public boolean isLastPageVisible() {
        return paths != null && !paths.isEmpty() && mViewPager.getCurrentItem() == (paths.size() - 1);
    }

    @Override
    public int getCurrentPage() {
        return getCurrentPosition() + 1;
    }

    @Override
    public void goToPage(int aPage) {
        int page = aPage - 1;
        mViewPager.setCurrentItem(page);
        if (readerListener != null) {
            readerListener.onPageChanged(transformPage(page));
        }
        currentPage = page;
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
                    mViewPager.setCurrentItem(getCurrentPosition() - 1);
                }
            } else if (e.getX() > getWidth() / 4 * 3) {
                if (currentPage == paths.size() - 1) {
                    if (readerListener != null) {
                        readerListener.onEndOver();
                    }
                } else {
                    mViewPager.setCurrentItem(getCurrentPosition() + 1);
                }
            } else {
                readerListener.onMenuRequired();
            }
        return false;
    }

    @Override
    public void onStartOver() {
        if (readerListener != null) {
            readerListener.onStartOver();
        }
    }

    @Override
    public void onEndOver() {
        if (readerListener != null) {
            readerListener.onEndOver();
        }
    }

    @Override
    protected int transformPage(int page) {
        return page + 1;
    }
}