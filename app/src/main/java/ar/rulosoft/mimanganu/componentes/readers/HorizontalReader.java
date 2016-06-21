package ar.rulosoft.mimanganu.componentes.readers;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Raul on 21/06/2016.
 */
public abstract class HorizontalReader extends Reader {

    protected float totalWidth = 0;

    public HorizontalReader(Context context) {
        super(context);
    }

    public HorizontalReader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalReader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void calculateParticularScale() {
        for (Page dimension : pages) {
            if (!dimension.error) {
                dimension.unification_scale = (screenHeight / dimension.original_height);
                dimension.scaled_width = dimension.original_width * dimension.unification_scale;
                dimension.scaled_height = screenHeight;
            } else {
                dimension.unification_scale = (screenWidth / dimension.original_width);
                dimension.scaled_width = screenWidth;
                dimension.scaled_height = screenHeight * dimension.unification_scale;

            }
        }
    }

    @Override
    protected void calculateParticularScale(Page dimension) {
        if (!dimension.error) {
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
    public void seekPage(int index) {
        absoluteScroll(getPagePosition(index), yScroll);
        generateDrawPool();
    }

    @Override
    public void goToPage(final int aPage) {
        if (pages != null) {
            final float finalScroll = getPagePosition(aPage - 1);
            final ValueAnimator va = ValueAnimator.ofFloat(xScroll, finalScroll).setDuration(500);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    relativeScroll((float) valueAnimator.getAnimatedValue() - xScroll, 0);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            generateDrawPool();
                        }
                    });
                }
            });
            va.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (Math.abs(aPage - currentPage - 1) > 1)
                        animatingSeek = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animatingSeek = false;
                    currentPage = aPage;
                    generateDrawPool();
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
    public void reset() {
        xScroll = 0;
        yScroll = 0;
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
            float visibleRight = (xScroll * mScaleFactor + screenWidth);
            final boolean visibility = (xScroll * mScaleFactor <= init_visibility * mScaleFactor && init_visibility * mScaleFactor <= visibleRight) ||
                    (xScroll * mScaleFactor <= end_visibility * mScaleFactor && end_visibility * mScaleFactor <= visibleRight) ||
                    (init_visibility * mScaleFactor < xScroll * mScaleFactor && end_visibility * mScaleFactor >= visibleRight);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(visibility != lastVisibleState){
                        lastVisibleState = visibility;
                        if(!visibility){
                            freeMemory();
                        }
                    }
                    if(visibility && segments != null)
                        for(Segment s: segments){
                            s.checkVisibility();
                        }
                }
            }).start();
            return visibility;
        }

        @Override
        public Segment getNewSegment() {
            return new HSegment();
        }

        @Override
        public float getVisiblePercent() {
            if (init_visibility < xScroll) {
                if (end_visibility < xScroll + screenWidth) {
                    return (end_visibility - xScroll) / scaled_width;
                } else {
                    return screenWidth / scaled_width;
                }
            } else {
                if (end_visibility < xScroll + screenWidth) {
                    return 1;
                } else {
                    return (xScroll + screenWidth - init_visibility) / scaled_width;
                }
            }
        }
        public class HSegment extends Segment{
            @Override
            public boolean checkVisibility() {
                float visibleLeft = xScroll * mScaleFactor;
                float visibleRight = visibleLeft + screenWidth;
                float _init_visibility = init_visibility + dx * unification_scale;
                float _end_visibility = _init_visibility + pw * unification_scale;
                boolean visibility = (visibleLeft <= _init_visibility * mScaleFactor && _init_visibility * mScaleFactor <= visibleRight) ||
                        (visibleLeft <= _end_visibility * mScaleFactor && _end_visibility * mScaleFactor <= visibleRight) ||
                        (_init_visibility * mScaleFactor < visibleLeft && _end_visibility * mScaleFactor >= visibleRight);
                if(visible != visibility){
                    visibilityChanged();
                }
                return visibility;
            }

            @Override
            public void draw(Canvas canvas) {
                if(state == ImagesStates.LOADED) {
                    m.reset();
                    mPaint.setAlpha(alpha);
                    m.postTranslate(dx, dy);
                    m.postScale(unification_scale, unification_scale);
                    m.postTranslate(init_visibility - xScroll, -yScroll);
                    m.postScale(mScaleFactor, mScaleFactor);
                    try {
                        canvas.drawBitmap(segment, m, mPaint);
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
