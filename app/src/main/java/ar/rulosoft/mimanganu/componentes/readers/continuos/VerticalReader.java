package ar.rulosoft.mimanganu.componentes.readers.continuos;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.IOverScrollUpdateListener;
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter;

import static me.everything.android.ui.overscroll.IOverScrollState.STATE_IDLE;

public class VerticalReader extends ContinuousReader {

    private float totalHeight = 0;
    boolean canOS = true;
    private float overScrollLimit = 200;
    private int seekOnLoad = -1;

    public VerticalReader(Context context) {
        super(context);
        final VerticalReader v = this;
        final VerticalOverScrollBounceEffectDecorator vOSBED = new VerticalOverScrollBounceEffectDecorator(new IOverScrollDecoratorAdapter() {

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


        vOSBED.setOverScrollStateListener(new IOverScrollStateListener() {
            @Override
            public void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState) {
                if (newState == STATE_IDLE) {
                    canOS = true;
                }
            }
        });

        vOSBED.setOverScrollUpdateListener(new IOverScrollUpdateListener() {
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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        overScrollLimit = getHeight() / 6;
    }


    @Override
    public void calculateParticularScale() {
        for (int i = 0; i < pages.size(); i++) {
            Page dimension = pages.get(i);
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
        if (seekOnLoad != -1) {
            seekPage(seekOnLoad);
            seekOnLoad = -1;
        }
    }

    public void reloadImage(int idx) {
        int pageIdx;
        if (idx == 0)
            pageIdx = idx;
        else
            pageIdx = idx - 1;
        if (pages != null && pageIdx < pages.size() && pageIdx >= 0) {
            int cPage = currentPage - 1;
            if (pages.size() < cPage || cPage < 0)
                cPage = 0;
            if (cPage >= pages.size())
                cPage = pages.size() - 1;
            float value = 0;
            if (pages.get(cPage) != null)
                value = pages.get(cPage).init_visibility;
            Page page = initValues(pages.get(pageIdx).path);
            pages.set(pageIdx, page);
            calculateParticularScale(pages.get(pageIdx));
            calculateVisibilities();
            if (pages.get(cPage) != null)
                value = value - pages.get(cPage).init_visibility;
            relativeScroll(0, -value);
            generateDrawPool();

        }
    }

    boolean isStart() {
        return (yScroll < 0.1);
    }

    boolean isEnd() {
        return (yScroll == (((totalHeight * mScaleFactor) - screenHeight)) / mScaleFactor);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (readerListener != null && canOS)
            if (e.getX() < getWidth() / 4) {
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

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
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
    public void postLayout() {
        if (yScroll == 0)
            absoluteScroll(xScroll, getPagePosition(currentPage - 1));
        generateDrawPool();
        if (readerListener != null) {
            readerListener.onPageChanged(currentPage);
        }
    }

    @Override
    public void seekPage(int index) {
        int page = index - 1;
        if (page < 0)
            page = 0;
        else if (index >= pages.size())
            page = pages.size() - 1;
        if (pagesLoaded) {
            absoluteScroll(xScroll, getPagePosition(page));
            generateDrawPool();
        } else {
            seekOnLoad = index;
        }
        if (readerListener != null) {
            readerListener.onPageChanged(index);
        }
        currentPage = transformPage(page);
    }

    @Override
    protected int transformPage(int page) {
        return page + 1;
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
                    currentPage = transformPage(aPage);
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
        currentPage = 1;
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
                if (visible != visibility) {
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
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
    }
}
