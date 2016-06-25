package ar.rulosoft.mimanganu.componentes.readers;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    protected OnBeginFlingListener mOnBeginFlingListener;
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
    protected Rect screen;
    float mScaleFactor = 1.f;
    Matrix m = new Matrix();
    int screenHeight, screenWidth;
    int screenHeightSS, screenWidthSS; // Sub scaled
    Handler mHandler;
    ArrayList<Page.Segment> toDraw = new ArrayList<>();
    boolean drawing = false, preparing = false, waiting = false;

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

    protected abstract void absoluteScroll(float x, float y);

    protected abstract void relativeScroll(double distanceX, double distanceY);

    protected abstract void calculateParticularScale();

    protected abstract void calculateParticularScale(Page page);

    protected abstract void calculateVisibilities();

    public abstract void goToPage(int aPage);

    protected abstract Page getNewPage();

    public abstract void reset();

    public abstract void seekPage(int index);

    public void freeMemory() {
        if (pages != null)
            for (Page p : pages) {
                p.freeMemory();
            }
    }

    public Page getPage(int page) {
        return pages.get(page - 1);
    }

    private void init(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(getContext(), this);
        mHandler = new Handler();
        ppi = context.getResources().getDisplayMetrics().density * 160.0f;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        screenHeight = Math.abs(bottom - top);
        screenWidth = Math.abs(right - left);
        screenWidthSS = screenWidth;
        screenHeightSS = screenHeight;
        if (pages != null) {
            calculateParticularScale();
            calculateVisibilities();
            layoutReady = true;
            generateDrawPool();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    protected void generateDrawPool() {
        if (!preparing) {
            preparing = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Page.Segment> _segments = new ArrayList<>();
                    if (viewReady) {
                        lastBestVisible = -1;
                        iniVisibility = false;
                        endVisibility = false;
                        lastPageBestPercent = 0f;
                        if (pages != null) {
                            int currentPageIdx = currentPage - 1;
                            boolean tested = false;
                            while (!tested) {
                                tested = true;
                                try {
                                    for (int i = currentPageIdx - 1; i >= 0; i--) {//pre
                                        Page page = pages.get(i);
                                        if (page.isVisible()) {
                                            iniVisibility = true;
                                            _segments.addAll(page.getVisibleSegments());
                                            if (page.getVisiblePercent() >= lastPageBestPercent) {
                                                lastPageBestPercent = page.getVisiblePercent();
                                                lastBestVisible = pages.indexOf(page);
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                    {//actual
                                        if (currentPageIdx >= 0 && currentPageIdx < pages.size()) {
                                            Page page = pages.get(currentPageIdx);
                                            if (page.isVisible()) {
                                                iniVisibility = true;
                                                _segments.addAll(page.getVisibleSegments());
                                                if (page.getVisiblePercent() >= lastPageBestPercent) {
                                                    lastPageBestPercent = page.getVisiblePercent();
                                                    lastBestVisible = pages.indexOf(page);
                                                }
                                            }
                                        }
                                    }
                                    for (int i = currentPageIdx + 1; i < pages.size(); i++) {//next
                                        Page page = pages.get(i);
                                        if (page.isVisible()) {
                                            iniVisibility = true;
                                            _segments.addAll(page.getVisibleSegments());
                                            if (page.getVisiblePercent() >= lastPageBestPercent) {
                                                lastPageBestPercent = page.getVisiblePercent();
                                                lastBestVisible = pages.indexOf(page);
                                            }
                                        } else {
                                            break;
                                        }
                                    }

                                    if (_segments.size() == 0) {//if none in range find...
                                        for (int i = 0; i < pages.size(); i++) {
                                            Page page = pages.get(i);
                                            if (page.isVisible()) {
                                                iniVisibility = true;
                                                _segments.addAll(page.getVisibleSegments());
                                                if (page.getVisiblePercent() >= lastPageBestPercent) {
                                                    lastPageBestPercent = page.getVisiblePercent();
                                                    lastBestVisible = pages.indexOf(page);
                                                }
                                            } else {
                                                if (iniVisibility) endVisibility = true;
                                            }
                                            if (iniVisibility && endVisibility)
                                                break;
                                        }
                                    }
                                } catch (Exception e) {
                                    tested = false;
                                }//catch errors caused for array concurrent modify

                            }
                            if (currentPage != lastBestVisible) {
                                setPage(lastBestVisible);
                            }
                        }
                    } else if (pagesLoaded) {
                        if (mViewReadyListener != null)
                            mViewReadyListener.onViewReady();
                        viewReady = true;
                        preparing = false;
                        generateDrawPool();
                    }
                    toDraw = _segments;
                    drawing = true;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void invalidate() {
        generateDrawPool();
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        waiting = false;
        if (drawing) {
            if (toDraw.size() > 0)
                for (Page.Segment s : toDraw) {
                    s.draw(canvas);
                }
            else
                waiting = true;
            preparing = false;
            drawing = false;
            if (waiting) {
                waiting = false;
                generateDrawPool();
            }
        } else {
            if (preparing)
                waiting = true;
            else
                generateDrawPool();
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
            generateDrawPool();
        }
    }

    public void changePath(int idx, String path) {
        Page page = initValues(path);
        calculateParticularScale(page);
        pages.set(idx, page);
        calculateVisibilities();
        generateDrawPool();
    }

    public void reloadImage(int idx) {
        if (pages != null) {
            /*Log.d("Reader", "idx: " + idx);
            Log.d("Reader", "pages.s: " + pages.size());*/
            if(idx < pages.size()) {
                int iniPage = getCurrentPage() - 1;
                Page page = initValues(pages.get(idx).path);
                pages.set(idx, page);
                calculateParticularScale(pages.get(idx));
                calculateVisibilities();
                if (iniPage >= idx)
                    seekPage(iniPage);
                generateDrawPool();
            }
        }
    }

    private Page initValues(String path) {
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
            } catch (IOException ignored) {
            }
            dimension.initValues();
        } else {
            try {
                dimension.error = true;
                InputStream inputStream = getBitmapFromAsset("broke.png");
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
                dimension.original_width = bitmapOptions.outWidth;
                dimension.original_height = bitmapOptions.outHeight;
                inputStream.close();
            } catch (IOException ignored) {
            }
            dimension.initValues();
        }
        return dimension;
    }

    protected void setPage(int page) {
        if (pageChangeListener != null)
            pageChangeListener.onPageChanged(page);
        currentPage = page;
        generateDrawPool();
    }

    public boolean isLastPageVisible() {
        return pages != null && pages.get(pages.size() - 1).isVisible();
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

    public void setOnBeginFlingListener(OnBeginFlingListener onBeginFlingListener) {
        this.mOnBeginFlingListener = onBeginFlingListener;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        stopAnimationsOnTouch = true;
        mScaleDetector.onTouchEvent(ev);
        if (!mScaleDetector.isInProgress())
            mGestureDetector.onTouchEvent(ev);
        generateDrawPool();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        distanceX = (distanceX * mScrollSensitive / mScaleFactor);
        distanceY = (distanceY * mScrollSensitive / mScaleFactor);
        relativeScroll(distanceX, distanceY);
        generateDrawPool();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
        stopAnimationsOnTouch = false;
        stopAnimationOnHorizontalOver = false;
        stopAnimationOnVerticalOver = false;
        mHandler.post(new Runnable() {
            final int fps = 50;
            final float deceleration_rate = 0.90f;
            final int timeLapse = 1000 / fps;
            final float min_velocity = 500;
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
                generateDrawPool();
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
                generateDrawPool();
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
    public abstract float getPagePosition(int page);

    private InputStream getBitmapFromAsset(String strName) {
        AssetManager assetManager = getContext().getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return istr;
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

    public interface OnBeginFlingListener {
        void onBeginFling();
    }

    public abstract class Page {

        String path;
        float original_width;
        float original_height;
        float init_visibility;
        float end_visibility;
        float unification_scale;
        float scaled_height;
        float scaled_width;
        boolean initialized = false;
        Segment[] segments;
        int pw, ph;
        int vp, hp, tp; //vertical and horizontal parts count and total
        boolean error = false;
        boolean lastVisibleState = false;

        public abstract boolean isVisible();

        public abstract Segment getNewSegment();

        public abstract float getVisiblePercent();

        public void initValues() {
            vp = (int) (original_height / mTextureMax) + 1;
            hp = (int) (original_width / mTextureMax) + 1;
            pw = (int) (original_width / hp);
            ph = (int) (original_height / vp);
            tp = vp * hp;
            segments = new Segment[tp];
            for (int i = 0; i < vp; i++) {
                for (int j = 0; j < hp; j++) {
                    int idx = (i * hp) + j;
                    segments[idx] = getNewSegment();
                    segments[idx].dy = i * ph;
                    segments[idx].dx = j * pw;
                }
            }
            initialized = true;
        }

        public String getPath() {
            return path;
        }

        public void freeMemory() {
            if (segments != null)
                for (Segment s : segments) {
                    s.freeMemory();
                }
        }

        public ArrayList<Segment> getVisibleSegments() {
            ArrayList<Segment> _segments = new ArrayList<>();
            if (segments != null)
                for (Segment s : segments) {
                    if (s.isVisible()) {
                        _segments.add(s);
                    }
                }
            return _segments;
        }

        public void showOnLoad(final Segment segment) {
            if (segment.isVisible()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ValueAnimator va = ValueAnimator.ofInt(0, 255);
                        va.setDuration(300);
                        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                segment.alpha = (int) valueAnimator.getAnimatedValue();
                                generateDrawPool();
                            }
                        });
                        va.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                segment.alpha = 255;
                                generateDrawPool();
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {
                                segment.alpha = 255;
                                generateDrawPool();
                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
                        va.start();

                    }
                });
            } else {
                segment.alpha = 255;
                generateDrawPool();
            }
        }

        public abstract class Segment {
            boolean visible = false;
            Bitmap segment;
            ImagesStates state;
            int dx, dy;
            Paint mPaint;
            int alpha;

            public Segment() {
                mPaint = new Paint();
                mPaint.setAlpha(255);
                mPaint.setFilterBitmap(true);
                alpha = 255;
                state = ImagesStates.NULL;
            }

            public boolean isVisible() {
                return visible;
            }

            public abstract boolean checkVisibility();

            public abstract void draw(Canvas canvas);

            public void visibilityChanged() {
                if (!animatingSeek) {
                    visible = !visible;
                    if (visible) {
                        loadBitmap();
                    } else {
                        freeMemory();
                    }
                }
            }


            public void freeMemory() {
                if (segment != null) {
                    visible = false;
                    state = ImagesStates.RECYCLED;
                    segment.recycle();
                    segment = null;
                    alpha = 0;
                }
                state = ImagesStates.NULL;
            }

            public void loadBitmap() {
                if (!animatingSeek)
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (state == ImagesStates.NULL) {
                                    state = ImagesStates.LOADING;
                                    alpha = 0;
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                                    if (!error) {
                                        if (tp == 1) {
                                            segment = BitmapDecoder.from(path).useBuiltInDecoder(false).config(Bitmap.Config.RGB_565).decode();
                                            if (segments == null) {
                                                segment = BitmapDecoder.from(path).useBuiltInDecoder(true).config(Bitmap.Config.RGB_565).decode();
                                            }

                                        } else {
                                            try {
                                                int right = dx + pw + 2, bottom = dy + ph + 2;
                                                if (right > original_width)
                                                    right = (int) original_width;
                                                if (bottom > original_height)
                                                    bottom = (int) original_height;
                                                segment = BitmapDecoder.from(path).region(dx, dy, right, bottom).useBuiltInDecoder(false).config(Bitmap.Config.RGB_565).decode();
                                                if (segment == null) {
                                                    segment = BitmapDecoder.from(path).region(dx, dy, right, bottom).useBuiltInDecoder(true).config(Bitmap.Config.RGB_565).decode();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        InputStream inputStream = getBitmapFromAsset("broke.png");
                                        if (tp == 1) {
                                            segment = BitmapFactory.decodeStream(inputStream, null, options);
                                        } else {
                                            try {
                                                int right = dx + pw + 2, bottom = dy + ph + 2;
                                                if (right > original_width)
                                                    right = (int) original_width;
                                                if (bottom > original_height)
                                                    bottom = (int) original_height;
                                                segment = BitmapDecoder.from(inputStream).region(dx, dy, right, bottom).useBuiltInDecoder(false).config(Bitmap.Config.RGB_565).decode();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        inputStream.close();
                                    }
                                    if (segment != null) {
                                        state = ImagesStates.LOADED;
                                        showOnLoad(Segment.this);
                                    } else {
                                        state = ImagesStates.NULL;
                                    }
                                }
                            } catch (Exception | OutOfMemoryError e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
            }
        }
    }

    protected class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float nScale = Math.max(.8f, Math.min(mScaleFactor * detector.getScaleFactor(), 3.0f));
            if ((nScale <= 3f && nScale >= 1f)) {//can be better, but how ?
                float final_x = (((((screenWidth * nScale) - screenWidth)) / nScale) - ((((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor)) * detector.getFocusX() / screenWidth;
                float final_y = (((((screenHeight * nScale) - screenHeight)) / nScale) - ((((screenHeight * mScaleFactor) - screenHeight)) / mScaleFactor)) * detector.getFocusX() / screenHeight;
                screenHeightSS = screenHeight;
                screenWidthSS = screenWidth;
                relativeScroll(final_x, final_y);
            } else if (nScale < 1) {
                screenHeightSS = (int) (nScale * screenHeight);
                screenWidthSS = (int) (nScale * screenWidth);
                relativeScroll(0, 0);
            }
            mScaleFactor = nScale;
            generateDrawPool();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }
}
