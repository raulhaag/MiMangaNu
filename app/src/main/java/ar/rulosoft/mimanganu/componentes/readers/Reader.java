package ar.rulosoft.mimanganu.componentes.readers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import it.sephiroth.android.library.TapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

/**
 * Created by Raul on 24/06/2016.
 */
public abstract class Reader extends LinearLayout implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    protected TapListener mTapListener;
    protected OnEndFlingListener mOnEndFlingListener;
    protected OnBeginFlingListener mOnBeginFlingListener;
    protected OnViewReadyListener mViewReadyListener;
    protected OnPageChangeListener pageChangeListener;

    public enum Direction {VERTICAL, R2L, L2R}

    protected int mTextureMax;
    protected float mScrollSensitive = 1.f;
    protected Direction mDirection = Direction.R2L;

    public Reader(Context context) {
        super(context);
    }

    public Reader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Reader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public abstract void goToPage(int aPage);
    public abstract void reset();
    public abstract void seekPage(int index);
    public abstract void setPaths(List<String> paths);
    public abstract void changePath(int idx, String path);
    public abstract void freeMemory();
    public abstract void freePage(int idx);
    public abstract String getPath(int idx);
    public abstract void reloadImage(int idx);
    public abstract boolean isLastPageVisible();
    public abstract int getCurrentPage();

    public void setScrollSensitive(float mScrollSensitive) {
        this.mScrollSensitive = mScrollSensitive;
    }

    public void setMaxTexture(int mTextureMax){
        this.mTextureMax = mTextureMax;
    }

    public void setTapListener(TapListener mTapListener) {
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

    public Direction getDirection() {
        return mDirection;
    }

    public void setDirection(Direction direction) {
        this.mDirection = direction;
    }

    public boolean hasFitFeature(){
        return false;
    }

    public void setScreenFit(DisplayType displayType){}

    public interface OnPageChangeListener {
        void onPageChanged(int page);
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
}
