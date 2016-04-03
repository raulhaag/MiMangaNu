package it.sephiroth.android.library.imagezoom;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;

public class ImageViewTouch extends ImageViewTouchBase {

    protected static final float SCROLL_DELTA_THRESHOLD = 1.0f;
    protected ScaleGestureDetector mScaleDetector;
    protected GestureDetector mGestureDetector;
    protected int mTouchSlop;
    protected float mScaleFactor;
    protected OnGestureListener mGestureListener;
    protected OnScaleGestureListener mScaleListener;
    protected boolean mScaleEnabled = true;
    protected boolean mScaleModeAuto = true;
    //protected boolean mDoubleTapEnabled = true;
    //protected boolean mScrollEnabled = true;
    //private OnImageViewTouchDoubleTapListener mDoubleTapListener;
    //private OnImageViewTouchSingleTapListener mSingleTapListener;
    private TapListener mTapListener;

    public ImageViewTouch(Context context) {
        super(context);
    }

    public ImageViewTouch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewTouch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mGestureListener = getGestureListener();
        mScaleListener = getScaleListener();

        mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
        mGestureDetector = new GestureDetector(getContext(), mGestureListener, null, true);
    }

    /*
    public void setDoubleTapListener(OnImageViewTouchDoubleTapListener listener) {
        mDoubleTapListener = listener;
    }

    public void setSingleTapListener(OnImageViewTouchSingleTapListener listener) {
        mSingleTapListener = listener;
    }
    */

    public void setTapListener(TapListener nTapListener) {
        mTapListener = nTapListener;
    }

    public void setScaleEnabled(boolean value) {
        mScaleEnabled = value;
    }

    /*
    public void setScrollEnabled(boolean value) {
        mScrollEnabled = value;
    }

    public boolean getDoubleTapEnabled() {
        return mDoubleTapEnabled;
    }

    public void setDoubleTapEnabled(boolean value) {
        mDoubleTapEnabled = value;
    }
    */

    protected OnGestureListener getGestureListener() {
        return new GestureListener();
    }

    protected OnScaleGestureListener getScaleListener() {
        return new ScaleListener();
    }

    @Override
    protected void _setImageDrawable(
            final Drawable drawable, final Matrix initial_matrix, float min_zoom, float max_zoom) {
        super._setImageDrawable(drawable, initial_matrix, min_zoom, max_zoom);
        mScaleFactor = getMaxScale() / 3;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (getBitmapChanged()) return false;
        mScaleDetector.onTouchEvent(event);

        if (!mScaleDetector.isInProgress()) mGestureDetector.onTouchEvent(event);

        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                return onUp(event);
        }
        return true;
    }


    @Override
    protected void onZoomAnimationCompleted(float scale) {
        if (scale < getMinScale()) zoomTo(getMinScale(), 50);
    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (getScale() == 1f) return false;
        mUserScaled = true;
        scrollBy(-distanceX, -distanceY);
        invalidate();
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();

        if (Math.abs(velocityX) > 800 || Math.abs(velocityY) > 800) {
            mUserScaled = true;
            scrollBy(diffX / 2, diffY / 2, 300);
            invalidate();
            return true;
        }
        return false;
    }

    public boolean onDown(MotionEvent e) {
        return !getBitmapChanged();
    }

    public boolean onUp(MotionEvent e) {
        if (getBitmapChanged()) return false;
        if (getScale() < getMinScale()) {
            zoomTo(getMinScale(), 50);
        }
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return !getBitmapChanged();
    }

    /**
     * Determines whether this ImageViewTouch can be scrolled.
     *
     * @param direction - positive direction value means scroll from right to left,
     *                  negative value means scroll from left to right
     * @return true if there is some more place to scroll, false - otherwise.
     */
    public boolean canScroll(int direction) {
        RectF bitmapRect = getBitmapRect();
        updateRect(bitmapRect, mScrollRect);
        Rect imageViewRect = new Rect();
        getGlobalVisibleRect(imageViewRect);

        if (null == bitmapRect) {
            return false;
        }

        if (bitmapRect.right >= imageViewRect.right) {
            if (direction < 0) {
                return Math.abs(bitmapRect.right - imageViewRect.right) > SCROLL_DELTA_THRESHOLD;
            }
        }

        double bitmapScrollRectDelta = Math.abs(bitmapRect.left - mScrollRect.left);
        return bitmapScrollRectDelta > SCROLL_DELTA_THRESHOLD;
    }

    public boolean canScrollV(int direction) {
        RectF bitmapRect = getBitmapRect();
        updateRect(bitmapRect, mScrollRect);
        Rect imageViewRect = new Rect();
        getGlobalVisibleRect(imageViewRect);

        if (null == bitmapRect) {
            return false;
        }

        if (bitmapRect.bottom - bitmapRect.top < imageViewRect.bottom - imageViewRect.top) {
            return false;
        }

        if (bitmapRect.bottom >= imageViewRect.bottom) {
            if (direction < 0) {
                return Math.abs(bitmapRect.bottom - imageViewRect.bottom) > SCROLL_DELTA_THRESHOLD;
            }
        }

        double bitmapScrollRectDelta = Math.abs(bitmapRect.top - mScrollRect.top);
        return bitmapScrollRectDelta > SCROLL_DELTA_THRESHOLD;
    }

    /*
    public void scrollToOrigin() {
        RectF bitmapRect = getBitmapRect();
        scrollBy(-bitmapRect.right, -bitmapRect.top);
    }

    public interface OnImageViewTouchDoubleTapListener {
        void onDoubleTap();
    }


    public interface OnImageViewTouchSingleTapListener {
        void onSingleTapConfirmed();
    }
    */

    public interface TapListener {
        void onCenterTap();

        void onLeftTap();

        void onRightTap();
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            if (null != mTapListener) {
                try {
                    if (e.getX() < getWidth() / 4) {
                        mTapListener.onLeftTap();
                    } else if (e.getX() > getWidth() / 4 * 3) {
                        mTapListener.onRightTap();
                    } else {
                        mTapListener.onCenterTap();
                    }
                } catch (Exception ex) {
                    // nothing
                }
            }

            //if (null != mSingleTapListener) mSingleTapListener.onSingleTapConfirmed();
            return ImageViewTouch.this.onSingleTapConfirmed(e);
        }

        protected float onDoubleTapPost(float scale) {
            float defScale = getDefaultScale(mScaleType);
            if (scale <= 1.7f * defScale) {
                return 1.8f * defScale;
            } else if (scale <= 2.7f * defScale) {
                return 2.8f * defScale;
            } else {
                return defScale;
            }
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {

            if (mScaleModeAuto && e.getAction() == MotionEvent.ACTION_UP)
            {
                float targetScale = Math.min(getMaxScale(), Math.max(onDoubleTapPost(getScale()), getMinScale()));
                zoomTo(targetScale, e.getX(), e.getY(), DEFAULT_ANIMATION_DURATION);
                invalidate();
            }
            mScaleModeAuto = true;

            return super.onDoubleTapEvent(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (isLongClickable()) {
                if (!mScaleDetector.isInProgress()) {
                    setPressed(true);
                    performLongClick();
                }
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            //if (!mScrollEnabled) return false;
            if (e1 == null || e2 == null) return false;
            if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) return false;
            if (mScaleDetector.isInProgress()) return false;

            return ImageViewTouch.this.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            //if (!mScrollEnabled) return false;
            if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) return false;
            if (mScaleDetector.isInProgress()) return false;
            if (getScale() == 1f) return false;

            return ImageViewTouch.this.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return ImageViewTouch.this.onSingleTapUp(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return ImageViewTouch.this.onDown(e);
        }
    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        protected boolean mScaled = false;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float span = detector.getCurrentSpan() - detector.getPreviousSpan();
            float targetScale = getScale() * detector.getScaleFactor();

            if (mScaleEnabled) {
                mScaleModeAuto = false;

                if (mScaled && span != 0) {
                    mUserScaled = true;
                    targetScale = Math.min(getMaxScale(), Math.max(targetScale, getMinScale() - 0.1f));
                    zoomTo(targetScale, detector.getFocusX(), detector.getFocusY());
                    invalidate();
                    return true;
                }
                // This is to prevent a glitch the first time image is scaled.
                if (!mScaled) mScaled = true;
            }
            return true;
        }
    }
}
