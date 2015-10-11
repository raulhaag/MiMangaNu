package ar.rulosoft.verticalreader.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class VerticalReader extends View implements OnGestureListener,
        OnDoubleTapListener {

    public Paint paint = new Paint();
    public int currentPage = 0, lastVisiblePage = 0;
    public float mScrollSensitive = 1.f;
    float mScaleFactor = 1.f;
    Matrix m = new Matrix();
    int screenHeight, screenWidth;
    Handler mHandler;
    float ppi;
    boolean pagesLoaded = false, viewReady = false;
    boolean animatingSeek = false;
    boolean stopAnimationsOnTouch = false, stopAnimationOnVerticalOver = false, stopAnimationOnHorizontalOver = false;
    boolean iniVisibility, endVisibility;
    private int mTextureMax = 1024;
    private float totalHeight = 0;
    private float XScroll = 0, YScroll = 0;
    private ArrayList<Page> pages;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private OnPageChangeListener pageChangeListener;
    private OnTapListener mTapListener;
    private ViewReady mViewReadyListener;

    public VerticalReader(Context context) {
        super(context);
        init(context);
    }

    public VerticalReader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VerticalReader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(getContext(), this);
        mHandler = new Handler();
        paint.setFilterBitmap(true);
        ppi = context.getResources().getDisplayMetrics().density * 160.0f;
    }

    public void setPath(List<String> paths) {
        pages = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            pages.add(initValues(paths.get(i)));
        }

    }

    public void changePath(int idx, String path) {
        Page page = initValues(path);
        calculateParticularScale(page);
        pages.set(idx, page);
        calculateVisibilities();
        VerticalReader.this.postInvalidate();
    }

    public void reloadImage(int idx) {
        Page page = initValues(pages.get(idx).path);
        pages.set(idx, page);
        calculateParticularScale(pages.get(idx));
        calculateVisibilities();
        VerticalReader.this.postInvalidate();
    }

    public Page initValues(String path) {
        Page dimension = new Page();
        dimension.path = path;
        File f = new File(path);
        if (f.exists()) {
            try {
                dimension.path = path;
                InputStream inputStream = new FileInputStream(path);
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
                dimension.original_width = bitmapOptions.outWidth;
                dimension.original_height = bitmapOptions.outHeight;
                inputStream.close();
            } catch (IOException e) {
                //Nothing to do
            }
            dimension.initValues();
        } else {
            dimension.state = ImagesStates.ERROR;
        }
        return dimension;
    }

    public void calculateParticularScale() {
        for (Page dimension : pages) {
            if (dimension.state != ImagesStates.ERROR) {
                dimension.unification_scale = (screenWidth / dimension.original_width);
                dimension.scaled_width = screenWidth;
                dimension.scaled_height = dimension.original_height * dimension.unification_scale;
            } else {
                dimension.original_width = screenWidth;
                dimension.original_height = screenHeight / 4;
                dimension.unification_scale = 1;
                dimension.scaled_width = screenWidth;
                dimension.scaled_height = screenHeight / 4;
            }
        }
    }

    public void calculateParticularScale(Page dimension) {
        dimension.unification_scale = (screenWidth / dimension.original_width);
        dimension.scaled_width = screenWidth;
        dimension.scaled_height = dimension.original_height * dimension.unification_scale;
    }

    private void setPage(int page) {
        if (pageChangeListener != null)
            pageChangeListener.onPageChanged(page);
        currentPage = page;
    }

    public void calculateVisibilities() {
        float acc = 0;
        for (int i = 0; i < pages.size(); i++) {
            Page d = pages.get(i);
            d.init_visibility = acc;
            acc += d.scaled_height;
            d.end_visibility = acc;
        }
        totalHeight = acc;
        pagesLoaded = true;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        stopAnimationsOnTouch = true;
        mScaleDetector.onTouchEvent(ev);
        if (!mScaleDetector.isInProgress())
            mGestureDetector.onTouchEvent(ev);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        screenHeight = Math.abs(bottom - top);
        screenWidth = Math.abs(right - left);
        if (pages != null) {
            calculateParticularScale();
            calculateVisibilities();
            this.postInvalidateDelayed(100);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewReady) {
            iniVisibility = false;
            endVisibility = false;
            if (pages != null) {
                for (Page page : pages) {
                    if (page.state != ImagesStates.ERROR) {
                        if (page.isVisible()) {
                            iniVisibility = true;
                            lastVisiblePage = pages.indexOf(page);
                            if (page.state == ImagesStates.LOADED) {
                                page.draw(canvas);
                            } else {
                                if (page.state == ImagesStates.NULL) {
                                    page.loadBitmap();
                                }
                            }
                        } else {
                            if (iniVisibility) endVisibility = true;
                            if (page.state == ImagesStates.LOADED) {
                                page.freeMemory();
                            }
                        }
                        if (iniVisibility && endVisibility)
                            break;
                    }
                }
                if (currentPage != lastVisiblePage && !animatingSeek) {
                    setPage(lastVisiblePage);
                }
            }
        } else if (pagesLoaded) {
            if (mViewReadyListener != null)
                mViewReadyListener.onViewReady();
            viewReady = true;
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle b = new Bundle();
        b.putParcelable("state", super.onSaveInstanceState());
        return b;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            super.onRestoreInstanceState(((Bundle) state).getParcelable("state"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        final float ini = mScaleFactor, end;
        if (mScaleFactor < 1.8) {
            end = 2f;
        } else if (mScaleFactor < 2.8) {
            end = 3f;
        } else {
            end = 1f;
        }

        ValueAnimator va = ValueAnimator.ofFloat(ini, end);
        va.setDuration(300);
        va.setInterpolator(new LinearInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            float nScale;
            float final_x = ((XScroll + e.getX() / ini)) - (screenWidth / 2) + (screenWidth * end - screenWidth) / (end * 2) - XScroll;
            float final_y = ((YScroll + e.getY() / ini)) - (screenHeight / 2) + (screenHeight * end - screenHeight) / (end * 2) - YScroll;
            float initial_x_scroll = XScroll;
            float initial_y_scroll = YScroll;
            float nPx, nPy;

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                nScale = (float) valueAnimator.getAnimatedValue();
                nPx = initial_x_scroll + (final_x * valueAnimator.getAnimatedFraction());
                nPy = initial_y_scroll + (final_y * valueAnimator.getAnimatedFraction());
                mScaleFactor = nScale;
                absoluteScroll(nPx, nPy);
                VerticalReader.this.postInvalidate();
            }
        });
        va.start();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mTapListener != null) {
            if (e.getX() < getWidth() / 4) {
                mTapListener.onLeftTap();
            } else if (e.getX() > getWidth() / 4 * 3) {
                mTapListener.onRightTap();
            } else {
                mTapListener.onCenterTap();
            }
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
        stopAnimationsOnTouch = false;
        stopAnimationOnHorizontalOver = false;
        stopAnimationOnVerticalOver = false;
        mHandler.post(new Runnable() {
            final int fps = 120;
            final float deceleration_rate = 0.95f;
            final int timeLapse = 1000 / fps;
            final float min_velocity = 250;
            float velocity_Y = velocityY * mScrollSensitive;
            float velocity_X = velocityX * mScrollSensitive;

            @Override
            public void run() {
                relativeScroll(-velocity_X / fps, -(velocity_Y / fps));
                velocity_Y = velocity_Y * deceleration_rate;
                velocity_X = velocity_X * deceleration_rate;
                invalidate();
                if (stopAnimationOnHorizontalOver) {
                    velocity_X = 0;
                }
                if (stopAnimationOnVerticalOver) {
                    velocity_Y = 0;
                }
                if ((Math.abs(velocity_Y) > min_velocity || Math.abs(velocity_X) > min_velocity) && !stopAnimationsOnTouch) {
                    mHandler.postDelayed(this, timeLapse);
                } else {
                    invalidate();
                }
            }
        });
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    public void relativeScroll(double distanceX, double distanceY) {
        if (YScroll + distanceY > (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
            YScroll = ((totalHeight * mScaleFactor) - screenHeight) / mScaleFactor;
            stopAnimationOnVerticalOver = true;
        } else if (YScroll + distanceY > 0) {
            YScroll += distanceY;
        } else {
            YScroll = 0;
            stopAnimationOnVerticalOver = true;
        }
        if (XScroll + distanceX > (((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            XScroll = ((screenWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (XScroll + distanceX < 0) {
            XScroll = 0;
        } else {
            XScroll += distanceX;
            stopAnimationOnHorizontalOver = true;
        }
    }

    public void absoluteScroll(float x, float y) {
        if (y > (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
            YScroll = ((totalHeight * mScaleFactor) - screenHeight) / mScaleFactor;
            stopAnimationOnVerticalOver = true;
        } else if (y > 0) {
            YScroll = y;
        } else {
            YScroll = 0;
            stopAnimationOnVerticalOver = true;
        }
        if (x > (((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            XScroll = ((screenWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (x < 0) {
            XScroll = 0;
        } else {
            XScroll = x;
            stopAnimationOnHorizontalOver = true;
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        distanceX = (distanceX * mScrollSensitive / mScaleFactor);
        distanceY = (distanceY * mScrollSensitive / mScaleFactor);
        relativeScroll(distanceX, distanceY);
        invalidate();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    /*
     * Starting from 0
     */
    public float getPagePosition(int page) {
        return pages.get(page).init_visibility;
    }

    public void seekPage(int index) {
        absoluteScroll(XScroll, getPagePosition(index));
    }

    public void goToPage(final int index) {
        if (pages != null) {
            final float finalScroll = getPagePosition(index);
            final ValueAnimator va = ValueAnimator.ofFloat(YScroll, finalScroll).setDuration(500);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    relativeScroll(0, (float) valueAnimator.getAnimatedValue() - YScroll);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            VerticalReader.this.invalidate();
                        }
                    });
                }
            });
            va.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    animatingSeek = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animatingSeek = false;
                    currentPage = index;
                    VerticalReader.this.invalidate();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            va.start();
        }
    }

    public void setPageChangeListener(OnPageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }

    public void setScrollSensitive(float mScrollSensitive) {
        this.mScrollSensitive = mScrollSensitive;
    }

    public void setMaxTexture(int mTextureMax) {
        if (mTextureMax > 0)
            this.mTextureMax = mTextureMax;
    }

    public void setTapListener(OnTapListener mTapListener) {
        this.mTapListener = mTapListener;
    }

    public void setViewReadyListener(ViewReady mViewReadyListener) {
        this.mViewReadyListener = mViewReadyListener;
    }

    public enum ImagesStates {NULL, RECYCLED, ERROR, LOADING, LOADED}

    public interface OnPageChangeListener {
        void onPageChanged(int page);
    }

    public interface OnTapListener {
        void onCenterTap();

        void onLeftTap();

        void onRightTap();
    }

    public interface ViewReady {
        void onViewReady();
    }

    private class Page {

        String path;
        ImagesStates state = ImagesStates.NULL;
        float original_width;
        float original_height;
        float init_visibility;
        float end_visibility;
        float unification_scale;
        float scaled_height;
        float scaled_width;
        boolean initialized = false;
        int[] alphas;
        Bitmap[] image;
        float pw, ph;
        float[] dx, dy; //dx & dy displacement in x and y axis | pw & ph heights and widths from segments | ls & le lines start and lines end
        int vp, hp, tp, partsLoaded; //vertical and horizontal parts count and total

        public void loadBitmap() {
            for (int i = 0; i < tp; i++) {
                loadBitmap(i);
            }
        }

        public void loadBitmap(final int pos) {
            if (!animatingSeek)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (state == ImagesStates.NULL || partsLoaded < tp) {
                                partsLoaded++;
                                state = ImagesStates.LOADING;
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.RGB_565;
                                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(path, false);
                                image[pos] = decoder.decodeRegion(new Rect((int) dx[pos], (int) dy[pos], (int) (dx[pos] + pw + 2), (int) (dy[pos] + ph + 2)), options);
                                if (image != null) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            state = ImagesStates.LOADED;
                                            if (Page.this.isVisible()) {
                                                ValueAnimator va = ValueAnimator.ofInt(0, 255);
                                                va.setDuration(150);
                                                va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                    @Override
                                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                        alphas[pos] = (int) valueAnimator.getAnimatedValue();
                                                        invalidate();
                                                    }
                                                });
                                                va.addListener(new Animator.AnimatorListener() {
                                                    @Override
                                                    public void onAnimationStart(Animator animator) {

                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animator animator) {
                                                        invalidate();
                                                    }

                                                    @Override
                                                    public void onAnimationCancel(Animator animator) {

                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animator animator) {

                                                    }
                                                });
                                                va.start();
                                            }
                                        }
                                    });
                                } else {
                                    state = ImagesStates.NULL;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
        }

        public void draw(Canvas canvas) {
            for (int i = 0; i < vp; i++) {
                for (int j = 0; j < hp; j++) {
                    int idx = i * hp + j;
                    if (image[idx] != null) {
                        paint.setAlpha(alphas[idx]);
                        m.reset();
                        m.reset();
                        m.postTranslate(dx[idx], dy[idx]);
                        m.postScale(unification_scale, unification_scale);
                        m.postTranslate(-XScroll, init_visibility - YScroll);
                        m.postScale(mScaleFactor, mScaleFactor);
                        canvas.drawBitmap(image[idx], m, paint);
                    }
                }
            }
        }

        public void initValues() {
            vp = (int) (original_height / mTextureMax) + 1;
            hp = (int) (original_width / mTextureMax) + 1;
            pw = (original_width / hp);
            ph = (original_height / vp);
            tp = vp * hp;
            dy = new float[tp];
            dx = new float[tp];
            alphas = new int[tp];
            image = new Bitmap[tp];
            for (int i = 0; i < vp; i++) {
                for (int j = 0; j < hp; j++) {
                    int idx = (i * hp) + j;
                    dy[idx] = i * ph;
                    dx[idx] = j * pw;
                }
            }
            initialized = true;
        }

        public void freeMemory() {
            partsLoaded = 0;
            state = ImagesStates.RECYCLED;
            image = null;
            image = new Bitmap[tp];
            state = ImagesStates.NULL;
        }

        public boolean isVisible() {
            float visibleBottom = YScroll + screenHeight;
            boolean visible = (YScroll <= init_visibility && init_visibility <= visibleBottom) || (YScroll <= end_visibility && end_visibility <= visibleBottom);
            return visible || (init_visibility < YScroll && end_visibility >= visibleBottom);
        }

        public boolean isNearToBeVisible() { // TODO check if ok, to preload images before the visibility reach
            float visibleBottomEx = YScroll + screenHeight + scaled_height / 2;
            float YsT = YScroll + scaled_height / 2;
            return (YsT <= init_visibility && init_visibility <= visibleBottomEx) || (YsT <= end_visibility && end_visibility <= visibleBottomEx);
        }
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float nScale = mScaleFactor * detector.getScaleFactor();
            if ((nScale <= 3f && nScale >= 1f)) {//can be better, but how ?
                float final_x = (((((screenWidth * nScale) - screenWidth)) / nScale) - ((((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor)) * detector.getFocusX() / screenWidth;
                float final_y = (((((screenHeight * nScale) - screenHeight)) / nScale) - ((((screenHeight * mScaleFactor) - screenHeight)) / mScaleFactor)) * detector.getFocusX() / screenHeight;
                relativeScroll(final_x, final_y);
            }
            mScaleFactor = nScale;
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 3.0f));
            invalidate();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }

}
