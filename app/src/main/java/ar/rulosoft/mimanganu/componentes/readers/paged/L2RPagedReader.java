package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.support.v4.view.ViewPager;
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


    @Override
    public void addOnPageChangeListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (readerListener != null) {
                    readerListener.onPageChanged(paths.size() - position - 1);
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
                    if (readerListener != null && isFirstPage() && mStartDragX < x && !firedListener) {
                        readerListener.onEndOver();
                        firedListener = true;
                    } else if (readerListener != null && isLastPage() && mStartDragX > x && !firedListener) {
                        readerListener.onStartOver();
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
    public void seekPage(int aPage) {
        goToPage(aPage + 1);
    }

    @Override
    public void goToPage(int aPage) {
        super.goToPage(paths.size() - aPage + 1);
    }

    @Override
    public int getCurrentPage() {
        return paths.size() - super.getCurrentPage() + 1;
    }

    @Override
    protected int transformPage(int page) {
        return page + 1;
    }

    @Override
    public void onLeftTap() {
        if(currentPage == 0){
            if(readerListener != null){
                readerListener.onEndOver();
            }
        }else{
            mViewPager.setCurrentItem(currentPage - 1);
        }
    }

    @Override
    public void onRightTap() {
        if(currentPage == paths.size() - 1){
            if(readerListener != null){
                readerListener.onStartOver();
            }
        }else{
            mViewPager.setCurrentItem(currentPage + 1);
        }
    }
}
