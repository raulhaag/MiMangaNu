package ar.rulosoft.mimanganu.componentes.readers.continuos;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.componentes.readers.Reader;
import rapid.decoder.BitmapDecoder;

/**
 * Created by Raul on 22/10/2015.
 */

public abstract class ContinuousReader extends Reader implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    protected int currentPage = 0, lastBestVisible = 0;
    protected float lastPageBestPercent = 0f;
    protected int mTextureMax = 1024;

    protected boolean animatingSeek = false;
    protected boolean stopAnimationsOnTouch = false, stopAnimationOnVerticalOver = false, stopAnimationOnHorizontalOver = false;
    protected boolean iniVisibility, endVisibility;
    protected boolean pagesLoaded = false, viewReady = false, layoutReady = false;
    protected float xScroll = 0, yScroll = 0;
    protected ArrayList<Page> pages = new ArrayList<>();
    protected ScaleGestureDetector mScaleDetector;
    protected GestureDetector mGestureDetector;
    Canvas cache;
    float mScaleFactor = 1.f;
    Matrix m = new Matrix();
    Paint mPaint = new Paint();
    int screenHeight, screenWidth;
    int screenHeightSS, screenWidthSS; // Sub scaled
    Handler mHandler;
    ArrayList<Page.Segment> toDraw = new ArrayList<>();
    boolean drawing = false, preparing = false;

    float ppi;


    public ContinuousReader(Context context) {
        super(context);
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

    public abstract void postLayout();

    public abstract void reloadImage(int idx);

    @Override
    public void setBlueFilter(float bf) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{1, 0, 0, 0, 0,
                0, (0.6f + 0.4f * bf), 0, 0, 0,
                0f, 0f, (0.1f + 0.9f * bf), 0, 0,
                0, 0, 0, 1f, 0});
        mPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        this.postInvalidate();
    }

    @Override
    public int getPages() {
        if (pages != null)
            return pages.size();
        return 0;
    }

    @Override
    protected int transformPage(int page) {
        return page + 1;
    }

    public void freeMemory() {
        if (pages != null)
            for (Page p : pages) {
                p.freeMemory();
            }
    }

    public void freePage(int idx) {
        if (isValidIdx(idx))
            getPage(idx).freeMemory();
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    public String getPath(int idx) {
        int iIdx = idx - 1;
        Page p = getPage(iIdx);
        if (p != null) {
            return p.getPath();
        } else {
            return "";
        }
    }

    public Page getPage(int page) {
        if (isValidIdx(page))
            return pages.get(page);
        return null;
    }

    private void init(Context context) {
        mPaint.setFilterBitmap(true);
        setWillNotDraw(false);
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
        postLayout();
        super.onLayout(changed, left, top, right, bottom);
    }

    protected synchronized void generateDrawPool() {
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
                            if (currentPage != transformPage(lastBestVisible)) {
                                currentPage = transformPage(lastBestVisible);
                                readerListener.onPageChanged(currentPage);
                            }
                        }
                    } else if (pagesLoaded) {
                        //TODO if (mViewReadyListener != null)
                        viewReady = true;
                        preparing = false;
                        absoluteScroll(xScroll, yScroll);
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
    protected void onDraw(Canvas canvas) {
        if (toDraw.size() > 0) {
            for (Page.Segment s : toDraw) {
                s.draw(canvas);
            }
            cache = canvas;
        } else {
            generateDrawPool();
            if (cache != null)
                canvas = cache;
        }
        preparing = false;
        drawing = false;
    }

    public void setPaths(final List<String> paths) {
        pages = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < paths.size(); i++) {
                    pages.add(initValues(paths.get(i)));
                }
                if (layoutReady) {
                    calculateParticularScale();
                    calculateVisibilities();
                    generateDrawPool();
                }
            }
        }).start();
    }

    protected Page initValues(String path) {
        Page dimension = getNewPage();
        dimension.path = path;
        if (fileExist(path)) {
            try {
                dimension.path = path;
                InputStream inputStream = getInputStream(path);
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
                dimension.original_width = bitmapOptions.outWidth;
                dimension.original_height = bitmapOptions.outHeight;
                inputStream.close();
            } catch (Exception ignored) {
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
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }
            dimension.initValues();
        }
        return dimension;
    }

    protected void setPage(int page) {
        if (readerListener != null)
            readerListener.onPageChanged(page);
        currentPage = page;
        generateDrawPool();
    }

    public boolean isLastPageVisible() {
        return pages != null && !pages.isEmpty() && pages.get(pages.size() - 1).isVisible();
    }

    public void setScrollSensitive(float mScrollSensitive) {
        this.mScrollSensitive = mScrollSensitive;
    }

    public void setMaxTexture(int mTextureMax) {
        if (mTextureMax > 0)
            this.mTextureMax = mTextureMax;
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

    /*
     * Starting from 0
     */
    public abstract float getPagePosition(int page);

    private InputStream getBitmapFromAsset(String strName) {
        AssetManager assetManager = getContext().getAssets();
        InputStream iStr = null;
        try {
            iStr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return iStr;
    }


    public enum ImagesStates {NULL, RECYCLED, ERROR, LOADING, LOADED}

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
            int alpha;

            public Segment() {
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
                                        InputStream is = getInputStream(path);
                                        if (tp == 1) {
                                            segment = BitmapDecoder.from(is).useBuiltInDecoder(false).config(Bitmap.Config.RGB_565).decode();
                                            if (segments == null) {
                                                segment = BitmapDecoder.from(is).useBuiltInDecoder(true).config(Bitmap.Config.RGB_565).decode();
                                            }

                                        } else {
                                            try {
                                                int right = dx + pw + 2, bottom = dy + ph + 2;
                                                if (right > original_width)
                                                    right = (int) original_width;
                                                if (bottom > original_height)
                                                    bottom = (int) original_height;
                                                segment = BitmapDecoder.from(is).region(dx, dy, right, bottom).useBuiltInDecoder(false).config(Bitmap.Config.RGB_565).decode();
                                                if (segment == null) {
                                                    segment = BitmapDecoder.from(is).region(dx, dy, right, bottom).useBuiltInDecoder(true).config(Bitmap.Config.RGB_565).decode();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        is.close();
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
                float final_y = (((((screenHeight * nScale) - screenHeight)) / nScale) - ((((screenHeight * mScaleFactor) - screenHeight)) / mScaleFactor)) * detector.getFocusY() / screenHeight;
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
