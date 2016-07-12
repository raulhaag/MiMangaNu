package ar.rulosoft.mimanganu.componentes.readers.paged;

import android.content.Context;
import android.view.LayoutInflater;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.OnSwipeOutListener;
import ar.rulosoft.mimanganu.componentes.UnScrolledViewPager;

/**
 * Created by Raul on 26/06/2016.
 */

public abstract class HorizontalPagedReader extends PagedReader implements OnSwipeOutListener {

    UnScrolledViewPager mViewPager;

    public HorizontalPagedReader(Context context) {
        super(context);
        init();
    }

    public abstract void addOnPageChangeListener();

    @Override
    public int getCurrentPage() {
        return mViewPager.getCurrentItem() + 1;
    }

    public void init() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater)getContext().getSystemService(infService);
        li.inflate(R.layout.view_paged_reader, this, true);
        mViewPager = (UnScrolledViewPager) findViewById(R.id.pager);
        addOnPageChangeListener();
        mViewPager.setOnSwipeOutListener(this);
    }

    @Override
    public void setPagerAdapter(PageAdapter nPageAdapter) {
        mPageAdapter = nPageAdapter;
        mViewPager.setAdapter(mPageAdapter);
    }
}
