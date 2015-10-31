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
        float acc = 0;
        for (int i = 0; i < pages.size(); i++) {
            Page d = pages.get(i);
            d.init_visibility = (float) Math.floor(acc);
            acc += d.scaled_height;
            acc = (float) Math.floor(acc);
            d.end_visibility = acc;
        }
        totalHeight = acc;
        pagesLoaded = true;
    }


    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
        stopAnimationsOnTouch = false;
        stopAnimationOnHorizontalOver = false;
        stopAnimationOnVerticalOver = false;
        if (mOnEndFlingListener != null && e1.getY() - e2.getY() > 100 && (YScroll == (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor)) {
            mOnEndFlingListener.onEndFling();
        }

        mHandler.post(new Runnable() {
            final int fps = 60;
            final float deceleration_rate = 0.90f;
            final int timeLapse = 1000 / fps;
            final float min_velocity = 250;
            float velocity_Y = velocityY * mScrollSensitive;
            float velocity_X = velocityX * mScrollSensitive;

            @Override
            public void run() {
                relativeScroll(-velocity_X / fps, -(velocity_Y / fps));
                velocity_Y = velocity_Y * deceleration_rate;
                velocity_X = velocity_X * deceleration_rate;
                invalidate();
                if (stopAnimationOnHorizontalOver) {
                    velocity_X = 0;
                }
                if (stopAnimationOnVerticalOver) {
                    velocity_Y = 0;
                }
                if ((Math.abs(velocity_Y) > min_velocity || Math.abs(velocity_X) > min_velocity) && !stopAnimationsOnTouch) {
                    mHandler.postDelayed(this, timeLapse);
                } else {
                    invalidate();
                }
            }
        });
        return false;
    }

    @Override
    public void relativeScroll(double distanceX, double distanceY) {
        if (YScroll + distanceY > (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
            YScroll = ((totalHeight * mScaleFactor) - screenHeight) / mScaleFactor;
            stopAnimationOnVerticalOver = true;
        } else if (YScroll + distanceY > 0) {
            YScroll += distanceY;
        } else {
            YScroll = 0;
            stopAnimationOnVerticalOver = true;
        }
        if (XScroll + distanceX > (((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            XScroll = ((screenWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (XScroll + distanceX < 0) {
            XScroll = 0;
        } else {
            XScroll += distanceX;
            stopAnimationOnHorizontalOver = true;
        }
    }

    @Override
    public void absoluteScroll(float x, float y) {
        if (y > (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor) {
            YScroll = ((totalHeight * mScaleFactor) - screenHeight) / mScaleFactor;
            stopAnimationOnVerticalOver = true;
        } else if (y > 0) {
            YScroll = y;
        } else {
            YScroll = 0;
            stopAnimationOnVerticalOver = true;
        }
        if (x > (((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
            XScroll = ((screenWidth * mScaleFactor) - screenWidth) / mScaleFactor;
            stopAnimationOnHorizontalOver = true;
        } else if (x < 0) {
            XScroll = 0;
        } else {
            XScroll = x;
            stopAnimationOnHorizontalOver = true;
        }
    }

    @Override
    public void seekPage(int index) {
        absoluteScroll(XScroll, getPagePosition(index));
        VerticalReader.this.invalidate();
    }

    public void goToPage(final int aPage) {
        if (pages != null) {
            final float finalScroll = getPagePosition(aPage) + 1;
            final ValueAnimator va = ValueAnimator.ofFloat(YScroll, finalScroll).setDuration(500);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    relativeScroll(0, (float) valueAnimator.getAnimatedValue() - YScroll);
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
        XScroll = 0;
        YScroll = 0;
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
            float visibleBottom = YScroll + screenHeight;
            boolean visible = (YScroll <= init_visibility && init_visibility <= visibleBottom) || (YScroll <= end_visibility && end_visibility <= visibleBottom);
            return visible || (init_visibility < YScroll && end_visibility >= visibleBottom);
        }

        @Override
        public boolean isNearToBeVisible() { // TODO check if ok, to preload images before the visibility reach
            float visibleBottomEx = YScroll + screenHeight + scaled_height / 2;
            float YsT = YScroll + scaled_height / 2;
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
                    m.postTranslate(-XScroll, init_visibility - YScroll);
                    m.postScale(mScaleFactor, mScaleFactor);
                    canvas.drawBitmap(image[idx], m, mPaint);
                }
            }
        }
    }


}
