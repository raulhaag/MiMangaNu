package ar.rulosoft.mimanganu.componentes.readers;

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
                dimension.unification_scale = (screenWidth / dimension.original_width);
                dimension.scaled_width = screenWidth;
                dimension.scaled_height = dimension.original_height * dimension.unification_scale;
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
        //Log.d("VertRe", "" + e1.getY() + " " + e2.getY() + " xS: " + xScroll + " yS: " + yScroll);
        if (mOnEndFlingListener != null && e1.getY() - e2.getY() > 100 && (yScroll == (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor)) {
            mOnEndFlingListener.onEndFling();
        } else if (mOnBeginFlingListener != null && yScroll < 0.1) {
            mOnBeginFlingListener.onBeginFling();
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
        if (mScaleFactor >= 1) {
            if (xScroll + distanceX > (((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
                xScroll = ((screenWidth * mScaleFactor) - screenWidth) / mScaleFactor;
                stopAnimationOnHorizontalOver = true;
            } else if (xScroll + distanceX < 0) {
                xScroll = 0;
            } else {
                xScroll += distanceX;
                stopAnimationOnHorizontalOver = true;
            }
        } else {
            xScroll = (screenWidthSS - screenWidth) / 2;
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
        if (mScaleFactor >= 1) {
            if (x > (((screenWidth * mScaleFactor) - screenWidth)) / mScaleFactor) {
                xScroll = ((screenWidth * mScaleFactor) - screenWidth) / mScaleFactor;
                stopAnimationOnHorizontalOver = true;
            } else if (x < 0) {
                xScroll = 0;
            } else {
                xScroll = x;
                stopAnimationOnHorizontalOver = true;
            }
        } else {
            xScroll = (screenWidthSS - screenWidth) / 2;
            stopAnimationOnHorizontalOver = true;
        }
    }

    @Override
    public void seekPage(int index) {
        absoluteScroll(xScroll, getPagePosition(index));
        generateDrawPool();
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
                            VerticalReader.this.generateDrawPool();
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
                    VerticalReader.this.generateDrawPool();
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

    @Override
    public float getPagePosition(int page) {
        if (pages != null && pages.size() > 1) {
            if (page < 0) {
                return pages.get(0).init_visibility;
            } else if (page < pages.size()) {
                if (pages.get(page).scaled_height * mScaleFactor > screenHeight) {
                    return pages.get(page).init_visibility;
                } else {
                    int add = (int) (pages.get(page).scaled_height * mScaleFactor - screenHeight) / 2;
                    return pages.get(page).init_visibility + add;
                }
            } else {
                return pages.get(pages.size() - 1).end_visibility;
            }
        } else {
            return 0;
        }
    }


    protected class VPage extends Page {
        @Override
        public boolean isVisible() {
            float visibleBottom = (yScroll * mScaleFactor + screenHeight);
            final boolean visibility = (yScroll * mScaleFactor <= init_visibility * mScaleFactor && init_visibility * mScaleFactor <= visibleBottom) ||
                    (yScroll * mScaleFactor <= end_visibility * mScaleFactor && end_visibility * mScaleFactor <= visibleBottom) ||
                    (init_visibility * mScaleFactor < yScroll * mScaleFactor && end_visibility * mScaleFactor >= visibleBottom);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (visibility != lastVisibleState) {
                        lastVisibleState = visibility;
                        if (!visibility) {
                            freeMemory();
                        }
                    }
                    if (visibility && segments != null)
                        for (Segment s : segments) {
                            s.checkVisibility();
                        }
                }
            }).start();
            return visibility;
        }

        @Override
        public Segment getNewSegment() {
            return new VSegment();
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

        public class VSegment extends Segment {
            @Override
            public boolean checkVisibility() {
                float visibleTop = yScroll * mScaleFactor;
                float visibleBottom = visibleTop + screenHeight;
                float _init_visibility = init_visibility + dy * unification_scale;
                float _end_visibility = _init_visibility + ph * unification_scale;
                boolean visibility = (visibleTop <= _init_visibility * mScaleFactor && _init_visibility * mScaleFactor <= visibleBottom) ||
                        (visibleTop <= _end_visibility * mScaleFactor && _end_visibility * mScaleFactor <= visibleBottom) ||
                        (_init_visibility * mScaleFactor < visibleTop && _end_visibility * mScaleFactor >= visibleBottom);
                if(visible != visibility){
                    visibilityChanged();
                }
                return visibility;
            }

            @Override
            public void draw(Canvas canvas) {
                if (state == ImagesStates.LOADED) {
                    mPaint.setAlpha(alpha);
                    m.reset();
                    m.postTranslate(dx, dy);
                    m.postScale(unification_scale, unification_scale);
                    m.postTranslate(-xScroll, init_visibility - yScroll);
                    m.postScale(mScaleFactor, mScaleFactor);
                    try {
                        canvas.drawBitmap(segment, m, mPaint);
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
