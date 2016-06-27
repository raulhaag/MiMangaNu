package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import ar.rulosoft.mimanganu.R;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

/**
 * Created by Raul on 27/06/2016.
 */
public class VerticalPagedReader extends PagedReader {

    public VerticalViewPager mViewPager;

    public VerticalPagedReader(Context context) {
        super(context);
        init();
    }

    public VerticalPagedReader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalPagedReader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }



    @Override
    public void seekPage(int aPage) {
        currentPage = aPage;
        mViewPager.setCurrentItem(aPage);
        if (pageChangeListener != null) {
            pageChangeListener.onPageChanged(aPage);
        }
    }

    @Override
    public void goToPage(int aPage) {
        currentPage = aPage - 1;
        mViewPager.setCurrentItem(aPage - 1);
        if (pageChangeListener != null) {
            pageChangeListener.onPageChanged(aPage);
        }
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
                if (pageChangeListener != null) {
                    pageChangeListener.onPageChanged(position);
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
}