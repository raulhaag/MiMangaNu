package ar.rulosoft.readers.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class VerticalReader extends Reader {

    private float totalHeight = 0;

    public VerticalReader(Context context) {
        super(context);
    }

    public VerticalReader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalReader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void calculateParticularScale() {
        for (Page dimension : pages) {
            if (dimension.state != ImagesStates.ERROR) {
                dimension.unification_scale = (screenWidth / dimension.original_width);
                dimension.scaled_width = screenWidth;
                dimension.scaled_height = dimension.original_height * dimension.unification_scale;
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
    public void calculateParticularScale(Page dimension) {
        dimension.unification_scale = (screenWidth / dimension.original_width);
        dimension.scaled_width = screenWidth;
        dimension.scaled_height = dimension.original_height * dimension.unification_scale;
    }

    @Override
    public void calculateVisibilities() {
        float scrollYAd = getPagePosition(currentPage);
        float acc = 0;
        for (int i = 0; i < pages.size(); i++) {
            Page d = pages.get(i);
            d.init_visibility = (float) Math.floor(acc);
            acc += d.scaled_height;
            acc = (float) Math.floor(acc);
            d.end_visibility = acc;
        }
        totalHeight = acc;
        scrollYAd = getPagePosition(currentPage) - scrollYAd;
        relativeScroll(0, scrollYAd);
        pagesLoaded = true;
    }


    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
        if (mOnEndFlingListener != null && e1.getY() - e2.getY() > 100 && (yScroll == (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor)) {
            mOnEndFlingListener.onEndFling();
        }
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void relativeScroll(double distanceX, double distanceY) {
        if (yScroll + distanceY > (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
            yScroll = ((totalHeight * mScaleFactor) - screenHeight) / mScaleFactor;
            stopAnimationOnVerticalOver = true;
        } else if (yScroll + distanceY > 0) {
            yScroll += distanceY;
        } else {
            yScroll = 0;
            stopAnimationOnVerticalOver = true;
        }
        if (xScroll + distanceX > (((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            xScroll = ((screenWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (xScroll + distanceX < 0) {
            xScroll = 0;
        } else {
            xScroll += distanceX;
            stopAnimationOnHorizontalOver = true;
        }
    }

    @Override
    public void absoluteScroll(float x, float y) {
        if (y > (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
            yScroll = ((totalHeight * mScaleFactor) - screenHeight) / mScaleFactor;
            stopAnimationOnVerticalOver = true;
        } else if (y > 0) {
            yScroll = y;
        } else {
            yScroll = 0;
            stopAnimationOnVerticalOver = true;
        }
        if (x > (((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            xScroll = ((screenWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (x < 0) {
            xScroll = 0;
        } else {
            xScroll = x;
            stopAnimationOnHorizontalOver = true;
        }
    }

    @Override
    public void seekPage(int index) {
        absoluteScroll(xScroll, getPagePosition(index));
        VerticalReader.this.invalidate();
    }

    public void goToPage(final int aPage) {
        if (pages != null) {
            final float finalScroll = getPagePosition(aPage - 1);
            final ValueAnimator va = ValueAnimator.ofFloat(yScroll, finalScroll).setDuration(500);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    relativeScroll(0, (float) valueAnimator.getAnimatedValue() - yScroll);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            VerticalReader.this.invalidate();
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
                    VerticalReader.this.invalidate();
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
        return new VPage();
    }

    @Override
    public void reset() {
        xScroll = 0;
        yScroll = 0;
        currentPage = 0;
        pages = null;
        pagesLoaded = false;
        viewReady = false;
        animatingSeek = false;
        totalHeight = 0;
    }

    protected class VPage extends Page {
        @Override
        public boolean isVisible() {
            float visibleBottom = yScroll + screenHeight;
            boolean visible = (yScroll <= init_visibility && init_visibility <= visibleBottom) || (yScroll <= end_visibility && end_visibility <= visibleBottom);
            return visible || (init_visibility < yScroll && end_visibility >= visibleBottom);
        }

        @Override
        public boolean isNearToBeVisible() { // TODO check if ok, to preload images before the visibility reach
            float visibleBottomEx = yScroll + screenHeight + scaled_height / 2;
            float YsT = yScroll + scaled_height / 2;
            return (YsT <= init_visibility && init_visibility <= visibleBottomEx) || (YsT <= end_visibility && end_visibility <= visibleBottomEx);
        }

        @Override
        public void draw(Canvas canvas) {
            mPaint.setAlpha(alpha);
            for (int idx = 0; idx < tp; idx++) {
                if (image[idx] != null) {
                    m.reset();
                    m.postTranslate(dx[idx], dy[idx]);
                    m.postScale(unification_scale, unification_scale);
                    m.postTranslate(-xScroll, init_visibility - yScroll);
                    m.postScale(mScaleFactor, mScaleFactor);
                    canvas.drawBitmap(image[idx], m, mPaint);
                }
            }
        }

        @Override
        public float getVisiblePercent() {
            if (init_visibility < yScroll) {
                if (end_visibility < yScroll + screenHeight) {
                    return (end_visibility - yScroll) / scaled_height;
                } else {
                    return screenHeight / scaled_height;
                }
            } else {
                if (end_visibility < yScroll + screenHeight) {
                    return 1;
                } else {
                    return (yScroll + screenHeight - init_visibility) / scaled_height;
                }
            }
        }
    }

}
