package ar.rulosoft.readers.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import rapid.decoder.BitmapDecoder;

/**
 * Created by Raul on 22/10/2015.
 */
public abstract class Reader extends View implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {
    public float mScrollSensitive = 1.f;
    protected int currentPage = 0, lastBestVisible = 0;
    protected float lastPageBestPercent = 0f;
    protected int mTextureMax = 1024;
    protected OnTapListener mTapListener;
    protected OnEndFlingListener mOnEndFlingListener;
    protected OnViewReadyListener mViewReadyListener;
    protected OnPageChangeListener pageChangeListener;
    protected boolean animatingSeek = false;
    protected boolean stopAnimationsOnTouch = false, stopAnimationOnVerticalOver = false, stopAnimationOnHorizontalOver = false;
    protected boolean iniVisibility, endVisibility;
    protected boolean pagesLoaded = false, viewReady = false, layoutReady = false;
    protected float xScroll = 0, yScroll = 0;
    protected ArrayList<Page> pages;
    protected ScaleGestureDetector mScaleDetector;
    protected GestureDetector mGestureDetector;
    float mScaleFactor = 1.f;
    Matrix m = new Matrix();
    int screenHeight, screenWidth;
    Handler mHandler;
    float ppi;

    public Reader(Context context) {
        super(context);
        init(context);
    }

    public Reader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Reader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public abstract void absoluteScroll(float x, float y);

    public abstract void relativeScroll(double distanceX, double distanceY);

    protected abstract void calculateParticularScale();

    protected abstract void calculateParticularScale(Page page);

    protected abstract void calculateVisibilities();

    public abstract void goToPage(int aPage);

    protected abstract Page getNewPage();

    public abstract void reset();

    public abstract void seekPage(int index);

    public void freeMemory() {
        for (Page p : pages) {
            if (p.partsLoaded > 0)
                p.freeMemory();
        }
    }

    public void init(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(getContext(), this);
        mHandler = new Handler();
        ppi = context.getResources().getDisplayMetrics().density * 160.0f;
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
        layoutReady = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewReady) {
            lastBestVisible = -1;
            iniVisibility = false;
            endVisibility = false;
            lastPageBestPercent = 0f;
            if (pages != null) {
                for (Page page : pages) {
                    if (page.state != ImagesStates.ERROR) {
                        if (page.isVisible()) {
                            iniVisibility = true;
                            if (page.state == ImagesStates.LOADED) {
                                page.draw(canvas);
                            } else {
                                if (page.state == ImagesStates.NULL) {
                                    page.loadBitmap();
                                }
                            }
                            if (page.getVisiblePercent() >= lastPageBestPercent) {
                                lastPageBestPercent = page.getVisiblePercent();
                                lastBestVisible = pages.indexOf(page);
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
                if (currentPage != lastBestVisible && !animatingSeek) {
                    setPage(lastBestVisible);
                }
            }
        } else if (pagesLoaded) {
            if (mViewReadyListener != null)
                mViewReadyListener.onViewReady();
            viewReady = true;
        }
    }


    public void setPaths(List<String> paths) {
        pages = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            pages.add(initValues(paths.get(i)));
        }
        if (layoutReady) {
            calculateParticularScale();
            calculateVisibilities();
            this.invalidate();
        }
    }

    public void changePath(int idx, String path) {
        Page page = initValues(path);
        calculateParticularScale(page);
        pages.set(idx, page);
        calculateVisibilities();
        postInvalidate();
    }

    public void reloadImage(int idx) {
        Page page = initValues(pages.get(idx).path);
        pages.set(idx, page);
        calculateParticularScale(pages.get(idx));
        calculateVisibilities();
        postInvalidate();
    }

    public Page initValues(String path) {
        Page dimension = getNewPage();
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

    protected void setPage(int page) {
        if (pageChangeListener != null)
            pageChangeListener.onPageChanged(page);
        currentPage = page;
    }

    public boolean isLastPageVisible() {
        return pages.get(pages.size() - 1).isVisible();
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

    public void setViewReadyListener(OnViewReadyListener mViewReadyListener) {
        this.mViewReadyListener = mViewReadyListener;
    }

    public void setPageChangeListener(OnPageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }

    public void setOnEndFlingListener(OnEndFlingListener onEndFlingListener) {
        this.mOnEndFlingListener = onEndFlingListener;
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
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        distanceX = (distanceX * mScrollSensitive / mScaleFactor);
        distanceY = (distanceY * mScrollSensitive / mScaleFactor);
        relativeScroll(distanceX, distanceY);
        invalidate();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
        stopAnimationsOnTouch = false;
        stopAnimationOnHorizontalOver = false;
        stopAnimationOnVerticalOver = false;
        mHandler.post(new Runnable() {
            final int fps = 60;
            final float deceleration_rate = 0.90f;
            final int timeLapse = 1000 / fps;
            final float min_velocity = 250;
            float velocity_Y = velocityY * mScrollSensitive;
            float velocity_X = velocityX * mScrollSensitive;

            @Override
            public void run() {
                relativeScroll(-velocity_X / fps, -(velocity_Y / fps));
                velocity_Y = velocity_Y * deceleration_rate;
                velocity_X = velocity_X * deceleration_rate;
                if (stopAnimationOnHorizontalOver) {
                    velocity_X = 0;
                }
                if (stopAnimationOnVerticalOver) {
                    velocity_Y = 0;
                }
                if ((Math.abs(velocity_Y) > min_velocity || Math.abs(velocity_X) > min_velocity) && !stopAnimationsOnTouch) {
                    mHandler.postDelayed(this, timeLapse);
                }
                invalidate();
            }
        });
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onDown(MotionEvent e) {
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
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            float nScale;
            float final_x = ((xScroll + e.getX() / ini)) - (screenWidth / 2) + (screenWidth * end - screenWidth) / (end * 2) - xScroll;
            float final_y = ((yScroll + e.getY() / ini)) - (screenHeight / 2) + (screenHeight * end - screenHeight) / (end * 2) - yScroll;
            float initial_x_scroll = xScroll;
            float initial_y_scroll = yScroll;
            float nPx, nPy, aP;

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                nScale = (float) valueAnimator.getAnimatedValue();
                aP = valueAnimator.getAnimatedFraction();
                nPx = initial_x_scroll + (final_x * aP);
                nPy = initial_y_scroll + (final_y * aP);
                mScaleFactor = nScale;
                absoluteScroll(nPx, nPy);
                invalidate();
            }
        });
        va.start();
        return false;
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

    public int getCurrentPage() {
        return (currentPage + 1);
    }

    /*
         * Starting from 0
         */
    public float getPagePosition(int page) {
        if (pages != null && pages.size() > 1)
            if (page < 0) {
                return pages.get(0).init_visibility;
            } else if (page < pages.size())
                return pages.get(page).init_visibility;
            else
                return pages.get(pages.size() - 1).init_visibility;
        else
            return 0;
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

    public interface OnViewReadyListener {
        void onViewReady();
    }

    public interface OnEndFlingListener {
        void onEndFling();
    }

    protected abstract class Page {

        String path;
        Paint mPaint;
        ImagesStates state = ImagesStates.NULL;
        float original_width;
        float original_height;
        float init_visibility;
        float end_visibility;
        float unification_scale;
        float scaled_height;
        float scaled_width;
        boolean initialized = false;
        boolean slowLoad = false;
        int alpha;
        Bitmap[] image;
        int pw, ph;
        float[] dx, dy; //dx & dy displacement in x and y axis | pw & ph heights and widths from segments | ls & le lines start and lines end
        int vp, hp, tp, partsLoaded; //vertical and horizontal parts count and total
        boolean bigImage = false;

        public abstract boolean isVisible();

        public abstract boolean isNearToBeVisible();

        public abstract float getVisiblePercent();

        public abstract void draw(Canvas canvas);

        public void loadBitmap() {
            if (!slowLoad) {
                for (int i = 0; i < tp; i++) {
                    loadBitmap(i);
                }
            } else {
                slowLoad();
            }

        }

        //sometimes region decoder don´t work
        public void slowLoad() {
            if (!animatingSeek)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            tp = 1;
                            bigImage = true;
                            try {
                                image[0] = BitmapFactory.decodeFile(path, options);
                            } catch (OutOfMemoryError e) {
                                //can do nothing to load :-( try to resample?
                                e.printStackTrace();
                            }
                            if (image[0] != null) {
                                partsLoaded++;
                                alpha = 255;
                                setLayerType(View.LAYER_TYPE_SOFTWARE, mPaint);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invalidate();
                                    }
                                });
                            } else {
                                state = ImagesStates.NULL;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
        }

        public void loadBitmap(final int pos) {
            if (!animatingSeek && !slowLoad)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (state == ImagesStates.NULL || partsLoaded < tp) {
                                state = ImagesStates.LOADING;
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.RGB_565;
                                if (tp == 1) {
                                    image[pos] = BitmapFactory.decodeFile(path, options);
                                } else {
                                    try {
                                        int right = (int) (dx[pos] + pw + 2), bottom = (int) (dy[pos] + ph + 2);
                                        if (right > original_width)
                                            right = (int) original_width;
                                        if (bottom > original_height)
                                            bottom = (int) original_height;
                                        image[pos] = BitmapDecoder.from(path).region((int) dx[pos], (int) dy[pos], right, bottom).useBuiltInDecoder(true).config(Bitmap.Config.RGB_565).decode();
                                    } catch (Exception e) {
                                        slowLoad = true;
                                    }
                                }
                                if (image[pos] != null) {
                                    partsLoaded++;
                                    showOnLoad();
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

        public void initValues() {
            vp = (int) (original_height / mTextureMax) + 1;
            hp = (int) (original_width / mTextureMax) + 1;
            pw = (int) (original_width / hp);
            ph = (int) (original_height / vp);
            tp = vp * hp;
            dy = new float[tp];
            dx = new float[tp];
            alpha = 0;
            image = new Bitmap[tp];
            for (int i = 0; i < vp; i++) {
                for (int j = 0; j < hp; j++) {
                    int idx = (i * hp) + j;
                    dy[idx] = i * ph;
                    dx[idx] = j * pw;
                }
            }
            mPaint = new Paint();
            mPaint.setFilterBitmap(true);
            initialized = true;
        }

        public void freeMemory() {
            partsLoaded = 0;
            state = ImagesStates.RECYCLED;
            image = null;
            image = new Bitmap[tp];
            state = ImagesStates.NULL;
        }

        public void showOnLoad() {
            if (tp == partsLoaded)
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        state = ImagesStates.LOADED;
                        if (Page.this.isVisible()) {
                            ValueAnimator va = ValueAnimator.ofInt(0, 255);
                            va.setDuration(300);
                            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    alpha = (int) valueAnimator.getAnimatedValue();
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
        }

    }

    protected class ScaleListener extends
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
