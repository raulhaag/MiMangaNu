package ar.rulosoft.mimanganu.componentes.readers.continuos;

import android.content.Context;
import android.view.MotionEvent;

/**
 * Created by Raul on 25/10/2015.
 */
public class L2RReader extends HorizontalReader {

    float totalWidth = 0;
    boolean firstTime = false;

    public L2RReader(Context context) {
        super(context);
    }

    @Override
    public void relativeScroll(double distanceX, double distanceY) {
        if (xScroll + distanceX > (((totalWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            xScroll = ((totalWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (xScroll + distanceX > 0) {
            xScroll += distanceX;
        } else {
            xScroll = 0;
            stopAnimationOnHorizontalOver = true;
        }
        if (mScaleFactor >= 1) {
            if (yScroll + distanceY > (((screenHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
                yScroll = ((screenHeight * mScaleFactor) - screenHeight) / mScaleFactor;
                stopAnimationOnVerticalOver = true;
            } else if (yScroll + distanceY < 0) {
                yScroll = 0;
            } else {
                yScroll += distanceY;
                stopAnimationOnVerticalOver = true;
            }
        } else {
            yScroll = (screenHeightSS - screenHeight) / 2;
            stopAnimationOnVerticalOver = true;
        }
    }

    @Override
    public void absoluteScroll(float x, float y) {
        if (x > (((totalWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            xScroll = ((totalWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (x > 0) {
            xScroll = x;
        } else {
            xScroll = 0;
            stopAnimationOnHorizontalOver = true;
        }
        if (mScaleFactor >= 1) {
            if (y > (((screenHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
                yScroll = ((screenHeight * mScaleFactor) - screenHeight) / mScaleFactor;
                stopAnimationOnVerticalOver = true;
            } else if (y < 0) {
                yScroll = 0;
            } else {
                yScroll = y;
                stopAnimationOnVerticalOver = true;
            }
        } else {
            yScroll = (screenHeightSS - screenHeight) / 2;
            stopAnimationOnVerticalOver = true;
        }
    }

    @Override
    protected void calculateVisibilities() {
        float acc = 0;
        for (int i = pages.size() - 1; i >= 0; i--) {
            Page d = pages.get(i);
            d.init_visibility = (float) Math.floor(acc);
            acc += d.scaled_width;
            acc = (float) Math.floor(acc);
            d.end_visibility = acc;
        }
        totalWidth = acc;
        if (firstTime) {
            xScroll = getPagePosition(0);
            firstTime = false;
        }
        pagesLoaded = true;
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
        //Log.d("L2RRe", "" + e1.getX() + " " + e2.getX() + " xS: " + xScroll + " yS: " + yScroll);
        if (readerListener != null && e2.getX() - e1.getX() > 100 && (xScroll < 0.1)) {
            readerListener.onEndOver();
            return true;
        } else if (readerListener != null && e1.getX() - e2.getX() > 100 && (xScroll == (((totalWidth * mScaleFactor) - screenWidth)) / mScaleFactor)) {
            readerListener.onStartOver();
            return true;
        }
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    /*
     * Starting from 0
    */
    @Override
    public float getPagePosition(int page) {
        if (pages != null && pages.size() > 1) {
            if (page < 0) {
                return pages.get(0).end_visibility;
            } else if (page < pages.size()) {
                if (pages.get(page).scaled_width * mScaleFactor > screenWidth) {
                    return (pages.get(page).end_visibility - (screenWidth / mScaleFactor));
                } else {
                    int add = (int) (pages.get(page).scaled_width * mScaleFactor - screenWidth) / 2;
                    return (pages.get(page).end_visibility - (screenWidth / mScaleFactor)) - add;
                }
            } else {
                return pages.get(pages.size() - 1).end_visibility - (screenWidth / mScaleFactor);
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (readerListener != null)
            if (e.getX() < getWidth() / 4) {
                if (isLastPageVisible())
                    readerListener.onEndOver();
                else
                    goToPage(currentPage + 1);
            } else if (e.getX() > getWidth() / 4 * 3) {
                if (currentPage == 1)
                    readerListener.onStartOver();
                else
                    goToPage(currentPage - 1);
            } else {
                readerListener.onMenuRequired();
            }
        return false;
    }
}