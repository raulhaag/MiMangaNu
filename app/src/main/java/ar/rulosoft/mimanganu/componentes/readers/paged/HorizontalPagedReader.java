package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import ar.rulosoft.mimanganu.R;

/**
 * Created by Raul on 26/06/2016.
 */

public abstract class HorizontalPagedReader extends PagedReader {

    ViewPager mViewPager;

    public HorizontalPagedReader(Context context) {
        super(context);
        init();
    }

    public HorizontalPagedReader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalPagedReader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public abstract void addOnPageChangeListener();

    @Override
    public void seekPage(int aPage) {
        mViewPager.setCurrentItem(aPage);
        if(pageChangeListener != null){
            pageChangeListener.onPageChanged(aPage);
        }
    }

    @Override
    public void goToPage(int aPage) {
        mViewPager.setCurrentItem(aPage - 1);
        if(pageChangeListener != null){
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
        LayoutInflater li = (LayoutInflater)getContext().getSystemService(infService);
        li.inflate(R.layout.view_paged_reader, this, true);
        mViewPager = (ViewPager)findViewById(R.id.pager);
        addOnPageChangeListener();
    }

    @Override
    public void setPagerAdapter(PageAdapter nPageAdapter) {
        mPageAdapter = nPageAdapter;
        mViewPager.setAdapter(mPageAdapter);
    }
}
