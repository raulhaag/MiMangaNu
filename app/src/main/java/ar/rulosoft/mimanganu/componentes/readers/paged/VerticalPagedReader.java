package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.OnSwipeOutListener;
import ar.rulosoft.mimanganu.componentes.UnScrolledViewPagerVertical;

/**
 * Created by Raul on 27/06/2016.
 */
public class VerticalPagedReader extends PagedReader implements OnSwipeOutListener {
    public UnScrolledViewPagerVertical mViewPager;

    public VerticalPagedReader(Context context) {
        super(context);
        init();
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
    public void goToPage(int aPage) {
        int page = aPage - 1;
        mViewPager.setCurrentItem(page);
        if (readerListener != null) {
            readerListener.onPageChanged(aPage);
        }
        currentPage = aPage;
    }

    @Override
    public int getCurrentPage() {
        return mViewPager.getCurrentItem() + 1;
    }

    public void init() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        li.inflate(R.layout.view_paged_reader_vertical, this, true);
        mViewPager = findViewById(R.id.pager);
        addOnPageChangeListener();
        mViewPager.setOnSwipeOutListener(this);
    }

    protected void addOnPageChangeListener() {
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
    public void setPagerAdapter(PageAdapter nPageAdapter) {
        mPageAdapter = nPageAdapter;
        mViewPager.setAdapter(mPageAdapter);
    }

    @Override
    protected int getCurrentPosition() {
        return mViewPager.getCurrentItem();
    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        currentPage = getCurrentPage();
        if (readerListener != null)
            if (e.getX() < getWidth() / 4) {
                if (currentPage == 1) {
                    if (readerListener != null) {
                        readerListener.onStartOver();
                    }
                } else {
                    mViewPager.setCurrentItem(getCurrentPosition() - 1);
                }
            } else if (e.getX() > getWidth() / 4 * 3) {
                if (currentPage == paths.size()) {
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
    protected int transformPage(int page) {
        return page + 1;
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
}