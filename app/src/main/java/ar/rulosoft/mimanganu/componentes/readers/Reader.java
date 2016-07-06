package ar.rulosoft.mimanganu.componentes.readers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.TapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

/**
 * Created by Raul on 24/06/2016.
 *
 * Expects
 * Fully manage their own view (page change) letting visible only the next listeners and methods
 * Listeners onMenuRequired, onPageChanged, onEndOver, onStartOver,
 * Method setPaths(paths,startPage
 * 0, reloadPage(position), getCurrentPagePosition, goToPage, goToPageAnimated
 *
 * returned pages ever go to be number 1 to n, being n the array paths size
 *
 */
public abstract class Reader extends LinearLayout implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    public enum Direction {VERTICAL, R2L, L2R}

    protected int mTextureMax;
    protected float mScrollSensitive = 1.f;
    protected Direction mDirection = Direction.R2L;
    protected ReaderListener readerListener;
    private ArrayList<String> paths;

    public Reader(Context context) {
        super(context);
    }

    public abstract void goToPage(int aPage);
    public abstract void reset();
    public abstract void seekPage(int index);
    public abstract void setPaths(List<String> paths);
    public abstract void freeMemory();
    public abstract void freePage(int idx);
    public abstract String getPath(int idx);
    public abstract void reloadImage(int idx);
    public abstract boolean isLastPageVisible();
    public abstract int getCurrentPage();
    protected abstract int transformPage(int page);

    public void setScrollSensitive(float mScrollSensitive) {
        this.mScrollSensitive = mScrollSensitive;
    }

    public void setReaderListener(ReaderListener readerListener) {
        this.readerListener = readerListener;
    }

    public void setMaxTexture(int mTextureMax){
        this.mTextureMax = mTextureMax;
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

    public interface ReaderListener {
        void onPageChanged(int page);
        void onEndOver();
        void onStartOver();
        void onMenuRequired();
    }
}
