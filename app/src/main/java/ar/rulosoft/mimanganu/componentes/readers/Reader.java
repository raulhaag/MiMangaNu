package ar.rulosoft.mimanganu.componentes.readers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.View;

import java.util.List;

/**
 * Created by Raul on 24/06/2016.
 */
public abstract class Reader extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    protected OnTapListener mTapListener;
    protected OnEndFlingListener mOnEndFlingListener;
    protected OnBeginFlingListener mOnBeginFlingListener;
    protected OnViewReadyListener mViewReadyListener;
    protected OnPageChangeListener pageChangeListener;

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
    public abstract void setScrollSensitive(float mScrollSensitive);
    public abstract void setMaxTexture(int mTextureMax);
    public abstract int getCurrentPage();

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
}
