package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.MotionEventCompat;

import ar.rulosoft.mimanganu.componentes.readers.paged.PagedReader;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

public class UnScrolledViewPagerVertical extends VerticalViewPager {

    static final int MIN_DISTANCE = 200;
    static final int MAX_TIME = 200;
    long mStartTime;
    float mStartDragY;
    OnSwipeOutListener mOnSwipeOutListener;

    public UnScrolledViewPagerVertical(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


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
        switch (ev.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mStartDragY = ev.getY();
                mStartTime = ev.getEventTime();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getCurrentItem() == 0 || getCurrentItem() == getAdapter().getCount() - 1) {
            final int action = ev.getAction();
            float y = ev.getY();
            switch (action & MotionEventCompat.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    long mEndTime = ev.getEventTime();
                    if (Math.abs(y - mStartDragY) > MIN_DISTANCE && (mEndTime - mStartTime) < MAX_TIME) {
                        if (getCurrentItem() == 0 && y > mStartDragY) {
                            onSwipeOutAtStart();
                            return true;
                        }
                        if (getCurrentItem() == getAdapter().getCount() - 1 && y < mStartDragY) {
                            onSwipeOutAtEnd();
                            return true;
                        }
                    }
                    break;
            }
        } else {
            mStartDragY = 0;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        try {
            return ((PagedReader.PageAdapter) getAdapter()).getCurrentPage().canScrollV(dx);
        } catch (Exception ignored) {
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}