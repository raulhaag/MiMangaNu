package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import ar.rulosoft.mimanganu.componentes.readers.paged.PagedReader;

import static android.view.MotionEvent.ACTION_MASK;

public class UnScrolledViewPager extends ViewPager {

    public UnScrolledViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    static final int MIN_DISTANCE = 200;
    static final int MAX_TIME = 200;
    float mStartDragX;
    long mStartTime;
    OnSwipeOutListener mOnSwipeOutListener;

    public void setOnSwipeOutListener(OnSwipeOutListener listener) {
        mOnSwipeOutListener = listener;
    }

    private void onSwipeOutAtStart() {
        if (mOnSwipeOutListener != null) {
            mOnSwipeOutListener.onStartOver();
        }
    }

    private void onSwipeOutAtEnd() {
        if (mOnSwipeOutListener != null) {
            mOnSwipeOutListener.onEndOver();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mStartDragX = ev.getX();
                mStartTime = ev.getEventTime();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getCurrentItem() == 0 || getCurrentItem() == getAdapter().getCount() - 1) {
            final int action = ev.getAction();
            float x = ev.getX();
            switch (action & ACTION_MASK) {
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    long mEndTime = ev.getEventTime();
                    if (Math.abs(x - mStartDragX) > MIN_DISTANCE && (mEndTime - mStartTime) < MAX_TIME) {
                        if (getCurrentItem() == 0 && x > mStartDragX) {
                            onSwipeOutAtStart();
                            return true;
                        }
                        if (getCurrentItem() == getAdapter().getCount() - 1 && x < mStartDragX) {
                            onSwipeOutAtEnd();
                            return true;
                        }
                    }
                    break;
            }
        } else {
            mStartDragX = 0;
        }
        return super.onTouchEvent(ev);
    }


    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        try {
            return ((PagedReader.PageAdapter) getAdapter()).getCurrentPage().canScroll(dx);
        } catch (Exception ignored) {
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}