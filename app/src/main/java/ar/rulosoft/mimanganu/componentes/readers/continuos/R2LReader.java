package ar.rulosoft.mimanganu.componentes.readers.continuos;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.IOverScrollUpdateListener;
import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter;

import static me.everything.android.ui.overscroll.IOverScrollState.STATE_IDLE;

/**
 * Created by Raul on 25/10/2015.
 */
public class R2LReader extends HorizontalReader {

    public R2LReader(Context context) {
        super(context);
        final R2LReader v = this;
        final HorizontalOverScrollBounceEffectDecorator hOSBED = new HorizontalOverScrollBounceEffectDecorator(new IOverScrollDecoratorAdapter() {

            @Override
            public View getView() {
                return v;
            }

            @Override
            public boolean isInAbsoluteStart() {
                return v.isStart() && canOS;
            }

            @Override
            public boolean isInAbsoluteEnd() {
                return v.isEnd() && canOS;
            }
        });


        hOSBED.setOverScrollStateListener(new IOverScrollStateListener() {
            @Override
            public void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState) {
                if (newState == STATE_IDLE) {
                    canOS = true;
                }
            }
        });

        hOSBED.setOverScrollUpdateListener(new IOverScrollUpdateListener() {
            @Override
            public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
                if (canOS) {
                    if (Math.abs(offset) > overScrollLimit) {
                        canOS = false;
                        if (v.isStart()) {
                            readerListener.onStartOver();
                        } else {
                            readerListener.onEndOver();
                        }
                    }
                }
            }
        });
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
        for (int i = 0; i < pages.size(); i++) {
            Page d = pages.get(i);
            d.init_visibility = (float) Math.floor(acc);
            acc += d.scaled_width;
            acc = (float) Math.floor(acc);
            d.end_visibility = acc;
        }
        totalWidth = acc;
        pagesLoaded = true;
        if (seekOnLoad != -1) {
            seekPage(seekOnLoad);
            seekOnLoad = -1;
        }
    }

    boolean isStart() {
        return (xScroll < 0.1);
    }

    boolean isEnd() {
        return (xScroll == (((totalWidth * mScaleFactor) - screenWidth)) / mScaleFactor);
    }

    /*
     * Starting from 0
     */

    @Override
    public float getPagePosition(int page) {
        if (pages != null && pages.size() > 1) {
            if (page < 0) {
                return pages.get(0).init_visibility;
            } else if (page < pages.size()) {
                if (pages.get(page).scaled_width * mScaleFactor > screenWidth) {
                    return pages.get(page).init_visibility;
                } else {
                    int add = (int) (pages.get(page).scaled_width * mScaleFactor - screenWidth) / 2;
                    return pages.get(page).init_visibility + add;
                }
            } else {
                return pages.get(pages.size() - 1).end_visibility;
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (e.getX() < getWidth() / 4 && canOS) {
            if (currentPage == 1)
                readerListener.onStartOver();
            else
                goToPage(currentPage - 1);
        } else if (e.getX() > getWidth() / 4 * 3) {
            if (isLastPageVisible())
                readerListener.onEndOver();
            else
                goToPage(currentPage + 1);
        } else {
            readerListener.onMenuRequired();
        }
        return false;
    }
}
