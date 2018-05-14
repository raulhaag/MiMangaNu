package ar.rulosoft.mimanganu.componentes.readers;

import android.content.Context;
import android.widget.FrameLayout;

import java.util.List;

import ar.rulosoft.mimanganu.componentes.readers.continuos.L2RReader;
import ar.rulosoft.mimanganu.componentes.readers.continuos.R2LReader;
import ar.rulosoft.mimanganu.componentes.readers.continuos.VerticalReader;
import ar.rulosoft.mimanganu.componentes.readers.paged.L2RPagedReader;
import ar.rulosoft.mimanganu.componentes.readers.paged.R2LPagedReader;
import ar.rulosoft.mimanganu.componentes.readers.paged.VerticalPagedReader;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

/**
 * Created by Raul on 24/06/2016.
 * <p>
 * Expects
 * Fully manage their own view (page change) letting visible only the next listeners and methods
 * Listeners onMenuRequired, onPageChanged, onEndOver, onStartOver,
 * Method setPaths(paths,startPage
 * 0, reloadPage(position), getCurrentPagePosition, goToPage, goToPageAnimated
 * <p>
 * returned pages ever go to be number 1 to n, being n the array paths size
 */
public abstract class Reader extends FrameLayout {
    protected int mTextureMax;
    protected float mScrollSensitive = 1.f;
    protected Direction mDirection = Direction.R2L;
    protected ReaderListener readerListener;

    public Reader(Context context) {
        super(context);
    }

    public static Reader getNewReader(Context context, Direction mDirection, Type mType) {
        if (mDirection == Direction.L2R) {
            if (mType == Type.CONTINUOUS) {
                return new L2RReader(context);
            } else {
                return new L2RPagedReader(context);
            }
        } else if (mDirection == Direction.R2L) {
            if (mType == Type.CONTINUOUS) {
                return new R2LReader(context);
            } else {
                return new R2LPagedReader(context);
            }
        } else {
            if (mType == Type.CONTINUOUS) {
                return new VerticalReader(context);
            } else {
                return new VerticalPagedReader(context);
            }
        }
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

    public abstract int getPages();

    protected abstract int transformPage(int page);

    public abstract void setBlueFilter(float bf);

    public void setBlueFilter(){
        setBlueFilter(1f);
    }

    public void setScrollSensitive(float mScrollSensitive) {
        this.mScrollSensitive = mScrollSensitive;
    }

    public void setReaderListener(ReaderListener readerListener) {
        this.readerListener = readerListener;
    }

    public void setMaxTexture(int mTextureMax) {
        this.mTextureMax = mTextureMax;
    }

    public Direction getDirection() {
        return mDirection;
    }

    public void setDirection(Direction direction) {
        this.mDirection = direction;
    }

    public boolean hasFitFeature() {
        return false;
    }

    public void setScreenFit(DisplayType displayType) {
    }

    public int limitValidPage(int idx) {
        if (idx < 0) return 0;
        else if (idx < getPages()) return idx;
        else return getPages() - 1;
    }

    public boolean isValidIdx(int idx) {
        if (idx < 0) return false;
        else if (idx < getPages()) return true;
        else return false;
    }

    public enum Direction {L2R, R2L, VERTICAL}

    public enum Type {CONTINUOUS, PAGED}

    public interface ReaderListener {
        void onPageChanged(int page);

        void onEndOver();

        void onStartOver();

        void onMenuRequired();
    }
}
