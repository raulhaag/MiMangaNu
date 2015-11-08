package ar.rulosoft.readers.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Raul on 25/10/2015.
 */
public class R2LReader extends Reader {

    protected float totalWidth = 0;

    public R2LReader(Context context) {
        super(context);
    }

    public R2LReader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public R2LReader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void relativeScroll(double distanceX, double distanceY) {
        if (XScroll + distanceX > (((totalWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            XScroll = ((totalWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (XScroll + distanceX > 0) {
            XScroll += distanceX;
        } else {
            XScroll = 0;
            stopAnimationOnHorizontalOver = true;
        }
        if (YScroll + distanceY > (((screenHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
            YScroll = ((screenHeight * mScaleFactor) - screenHeight) / mScaleFactor;
            stopAnimationOnVerticalOver = true;
        } else if (YScroll + distanceY < 0) {
            YScroll = 0;
        } else {
            YScroll += distanceY;
            stopAnimationOnVerticalOver = true;
        }
    }

    @Override
    public void absoluteScroll(float x, float y) {
        if (x > (((totalWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            XScroll = ((totalWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (x > 0) {
            XScroll = x;
        } else {
            XScroll = 0;
            stopAnimationOnHorizontalOver = true;
        }
        if (y > (((screenHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
            YScroll = ((screenHeight * mScaleFactor) - screenHeight) / mScaleFactor;
            stopAnimationOnVerticalOver = true;
        } else if (y < 0) {
            YScroll = 0;
        } else {
            YScroll = y;
            stopAnimationOnVerticalOver = true;
        }
    }

    @Override
    protected void calculateParticularScale() {
        for (Page dimension : pages) {
            if (dimension.state != ImagesStates.ERROR) {
                dimension.unification_scale = (screenHeight / dimension.original_height);
                dimension.scaled_width = dimension.original_width * dimension.unification_scale;
                dimension.scaled_height = screenHeight;
            } else {
                dimension.original_width = screenWidth;
                dimension.original_height = screenHeight;
                dimension.unification_scale = 1;
                dimension.scaled_width = screenWidth;
                dimension.scaled_height = screenHeight;
            }
        }
    }

    @Override
    protected void calculateParticularScale(Page dimension) {
        if (dimension.state != ImagesStates.ERROR) {
            dimension.unification_scale = (screenHeight / dimension.original_height);
            dimension.scaled_width = dimension.original_width * dimension.unification_scale;
            dimension.scaled_height = screenHeight;
        } else {
            dimension.original_width = screenWidth;
            dimension.original_height = screenHeight;
            dimension.unification_scale = 1;
            dimension.scaled_width = screenWidth;
            dimension.scaled_height = screenHeight;
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
    }

    @Override
    public void seekPage(int index) {
        absoluteScroll(getPagePosition(index), YScroll);
        invalidate();
    }

    @Override
    public void goToPage(final int aPage) {
        if (pages != null) {
            final float finalScroll = getPagePosition(aPage) + 1;
            final ValueAnimator va = ValueAnimator.ofFloat(XScroll, finalScroll).setDuration(500);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    relativeScroll((float) valueAnimator.getAnimatedValue() - XScroll, 0);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
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
                    currentPage = aPage;
                    invalidate();
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

    @Override
    protected Page getNewPage() {
        return new HPage();
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
        if (mOnEndFlingListener != null && e1.getX() - e2.getX() > 100 && (XScroll == (((totalWidth * mScaleFactor) - screenWidth)) / mScaleFactor)) {
            mOnEndFlingListener.onEndFling();
        }
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void reset() {
        XScroll = 0;
        YScroll = 0;
        currentPage = 0;
        pages = null;
        pagesLoaded = false;
        viewReady = false;
        animatingSeek = false;
        totalWidth = 0;
    }

    protected class HPage extends Page {
        @Override
        public boolean isVisible() {
            float visibleRight = XScroll + screenWidth;
            boolean visible = (XScroll <= init_visibility && init_visibility <= visibleRight) || (XScroll <= end_visibility && end_visibility <= visibleRight);
            return visible || (init_visibility < XScroll && end_visibility >= visibleRight);
        }

        @Override
        public boolean isNearToBeVisible() { // TODO check if ok, to preload images before the visibility reach
            float visibleRightEx = XScroll + screenWidth + scaled_width / 2;
            float XsT = XScroll + scaled_width / 2;
            return (XsT <= init_visibility && init_visibility <= visibleRightEx) || (XsT <= end_visibility && end_visibility <= visibleRightEx);
        }

        @Override
        public void draw(Canvas canvas) {
            mPaint.setAlpha(alpha);
            for (int idx = 0; idx < tp; idx++) {
                if (image[idx] != null) {
                    m.reset();
                    m.postTranslate(dx[idx], dy[idx]);
                    m.postScale(unification_scale, unification_scale);
                    m.postTranslate(init_visibility - XScroll, -YScroll);
                    m.postScale(mScaleFactor, mScaleFactor);
                    canvas.drawBitmap(image[idx], m, mPaint);
                }
            }
        }
    }

}
