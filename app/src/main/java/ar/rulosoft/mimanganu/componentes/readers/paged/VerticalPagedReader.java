package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import ar.rulosoft.mimanganu.R;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Created by Raul on 27/06/2016.
 *
 */
public class VerticalPagedReader extends PagedReader {

    public VerticalViewPager mViewPager;

    public VerticalPagedReader(Context context) {
        super(context);
        init();
    }

    @Override
    public void seekPage(int aPage) {
        mViewPager.setCurrentItem(aPage);
        if (readerListener != null) {
            readerListener.onPageChanged(aPage);
        }
        currentPage = aPage;
    }

    @Override
    public void goToPage(int aPage) {
        mViewPager.setCurrentItem(aPage - 1);
        if (readerListener != null) {
            readerListener.onPageChanged(aPage);
        }
        currentPage = aPage - 1;
    }

    @Override
    public void reset() {

    }

    @Override
    public int getCurrentPage() {
        return mViewPager.getCurrentItem() + 1;
    }

    @Override
    public void init() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        li.inflate(R.layout.view_paged_reader_vertical, this, true);
        mViewPager = (VerticalViewPager) findViewById(R.id.pager);
        addOnPageChangeListener();
    }

    protected void addOnPageChangeListener() {
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

    public boolean isLastPage() {
        return (paths.size() - 1) == mViewPager.getCurrentItem();
    }

    public boolean isFirstPage() {
        return 0 == mViewPager.getCurrentItem();
    }

    @Override
    public void setPagerAdapter(PageAdapter nPageAdapter) {
        mPageAdapter = nPageAdapter;
        mViewPager.setAdapter(mPageAdapter);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float y = ev.getY();
        if (!mPageAdapter.getCurrentPage().canScrollV(Math.round(y - mStartDragX))) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartDragX = y; //is x only to use the same for v & h
                    firedListener = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (readerListener != null && isLastPage() && mStartDragX > y && !firedListener) {
                        readerListener.onEndOver();
                        firedListener = true;
                    } else if (readerListener != null && isFirstPage() && mStartDragX < y && !firedListener) {
                        readerListener.onStartOver();
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

    @Override
    protected int transformPage(int page) {
        return page + 1;
    }
}