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

    public abstract void addOnPageChangeListener();

    @Override
    public void seekPage(int aPage) {
        mViewPager.setCurrentItem(aPage);
        if(readerListener != null){
            readerListener.onPageChanged(aPage);
        }
        currentPage = aPage;
    }

    @Override
    public void goToPage(int aPage) {
        mViewPager.setCurrentItem(aPage - 1);
        if(readerListener != null){
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
