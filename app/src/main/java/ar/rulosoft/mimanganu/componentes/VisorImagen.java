package ar.rulosoft.mimanganu.componentes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class VisorImagen extends View implements OnDoubleTapListener {

    public final int INVALID_POINTER_ID = -1;
    public final int TAMANO_ORIGINAL = 0;
    public final int EXTRECHAR_ANCHO = 1;
    public final int EXTRECHAR_ALTO = 2;
    public boolean capturada = true;
    public Bitmap actual = null;
    public Paint paint = new Paint();
    float mScaleFactor = 1.f;
    int pantallaAlto, pantallaAncho;
    int maxX, maxY, minX, minY, cX, cY;
    Matrix m = new Matrix();
    private int mActivePointerId = INVALID_POINTER_ID;
    private int escalaPorDefecto = EXTRECHAR_ANCHO;
    private ScaleGestureDetector mScaleDetector;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mPosX, mFposX;
    private float mPosY, mFposY;
    private float mTPosX, mTPosY;

    public VisorImagen(Context context) {
        super(context);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        paint.setAntiAlias(true);
    }

    public VisorImagen(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        paint.setAntiAlias(true);
    }

    public VisorImagen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
//		gestos = new GestureDetector(getContext(), this);
        paint.setAntiAlias(true);
    }

    public Bitmap getImage() {
        return actual;
    }

    public void setImage(Bitmap actual) {
        if (actual != null) {
            this.actual = actual;
            mPosX = 0;
            mFposX = 0;
            mPosY = 0;
            mFposY = 0;
            mTPosX = 0;
            mTPosY = 0;

            if (escalaPorDefecto == EXTRECHAR_ALTO) {
                mScaleFactor = scaleFitHeight();
            }
            if (escalaPorDefecto == EXTRECHAR_ANCHO) {
                mScaleFactor = scaleFitWidth();
            }
            if (escalaPorDefecto == TAMANO_ORIGINAL) {
                mScaleFactor = 1;
            }

            calcularTopes();

            if (actual.getWidth() * mScaleFactor < pantallaAncho) {
                mPosX = cX;
            }

            if (actual.getHeight() * mScaleFactor < pantallaAlto) {
                mPosY = cY;
            }

            invalidate();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (actual != null) {
            mScaleDetector.onTouchEvent(ev);
            final int action = ev.getAction();

            switch (action & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN: {
                    final float x = ev.getX();
                    final float y = ev.getY();
                    mLastTouchX = x;
                    mLastTouchY = y;
                    mActivePointerId = ev.getPointerId(0);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float x = ev.getX(pointerIndex);
                    final float y = ev.getY(pointerIndex);

                    if (!mScaleDetector.isInProgress()) {
                        final float dx = x - mLastTouchX;
                        final float dy = y - mLastTouchY;
                        mPosX += dx;
                        mPosY += dy;
                        mFposX = mPosX + mTPosX;
                        invalidate();
                    }
                    mLastTouchX = x;
                    mLastTouchY = y;
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    mActivePointerId = INVALID_POINTER_ID;
                /*
                 * if(mPosX != cX && pantallaAncho > actual.getWidth() *
				 * mScaleFactor){ (new RangeAnimator((int)mPosX, cX, 200) {
				 * 
				 * @Override public void repetir(int actual) { mPosX = actual;
				 * postInvalidate(); } }).start(); }
				 * 
				 * if(mPosY != cY && pantallaAlto > actual.getHeight() *
				 * mScaleFactor){ (new RangeAnimator((int)mPosY, cY, 200) {
				 * 
				 * @Override public void repetir(int actual) { mPosY = actual;
				 * postInvalidate(); } }).start(); }/
				 */

                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    final int pointerId = ev.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchX = ev.getX(newPointerIndex);
                        mLastTouchY = ev.getY(newPointerIndex);
                        mActivePointerId = ev.getPointerId(newPointerIndex);
                    }
                    break;
                }
            }
        }
        return true;
    }

    public void reset() {
        requestLayout();
        calcularTopes();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if (escalaPorDefecto == EXTRECHAR_ALTO) {
            mScaleFactor = scaleFitHeight();
        }
        if (escalaPorDefecto == EXTRECHAR_ANCHO) {
            mScaleFactor = scaleFitWidth();
        }
        if (escalaPorDefecto == TAMANO_ORIGINAL) {
            mScaleFactor = 1;
        }
        pantallaAlto = Math.abs(bottom - top);
        pantallaAncho = Math.abs(right - left);

        if (actual != null) {
            calcularTopes();
            if (actual.getWidth() * mScaleFactor < pantallaAncho) {
                mPosX = cX;
            }

            if (actual.getHeight() * mScaleFactor < pantallaAlto) {
                mPosY = cY;
            }
        }
        invalidate();
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (actual != null) {

			/*
             * translacion de zoom
			 */

            mFposY = mPosY + mTPosY;
            mFposX = mPosX + mTPosX;

			/*
             * Topes
			 */

            if (mFposX < -maxX) {
                mFposX = -maxX;
            }

            if (mFposY < -maxY) {
                mFposY = -maxY;
            }

            if (mFposY > minY)
                mFposY = minY;

            if (mFposX > minX)
                mFposX = minX;

            m.reset();
            m.postScale(mScaleFactor, mScaleFactor);
            m.postTranslate(mFposX, mFposY);
            canvas.drawBitmap(actual, m, paint);
        }
    }

    public float getScale() {
        return mScaleFactor;
    }

    public void setScale(float scale) {
        mScaleFactor = scale;
        invalidate();
    }

    public float scaleFitHeight() {
        if (actual != null)
            return (float) getMeasuredHeight() / actual.getHeight();
        return 1.f;
    }

    public float scaleFitWidth() {
        if (actual != null) {
            return (float) getMeasuredWidth() / actual.getWidth();
        }
        return 1.f;
    }

    public int getEscalaPorDefecto() {
        return escalaPorDefecto;
    }

    public void setEscalaPorDefecto(int escalaPorDefecto) {
        this.escalaPorDefecto = escalaPorDefecto;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        boolean returnVal = false;
        if (mScaleFactor == scaleFitWidth()) {
            setScale(scaleFitHeight());
            returnVal = true;
        } else if (mScaleFactor == scaleFitHeight()) {
            setScale(scaleFitWidth());
            returnVal = true;
        } else {
            setScale(1);
            returnVal = true;
        }
        calcularTopes();
        invalidate();
        return returnVal;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    public void calcularTopes() {
		/*
		 * escalas y laterales
		 */
        if (actual != null) {
            if (pantallaAncho < (actual.getWidth() * mScaleFactor)) {
                minX = 0;
                maxX = (int) Math.floor(actual.getWidth() * mScaleFactor - pantallaAncho);
            } else {
                minX = (int) Math.ceil(pantallaAncho - (actual.getWidth() * mScaleFactor));
                maxX = 0;
            }

            if (pantallaAlto < (actual.getHeight() * mScaleFactor)) {
                minY = 0;
                maxY = (int) Math.floor(actual.getHeight() * mScaleFactor - pantallaAlto);
            } else {
                minY = (int) Math.ceil(pantallaAlto - ((actual.getHeight() * mScaleFactor)));
                maxY = 0;
            }

            cX = (int) (pantallaAncho - (actual.getWidth() * mScaleFactor)) / 2;
            cY = (int) (pantallaAlto - (actual.getHeight() * mScaleFactor)) / 2;
        }
    }

    public boolean isBordeDerecho() {
        boolean borde = true;
        if (actual != null && getWidth() < (actual.getWidth() * mScaleFactor))
            borde = ((mFposX + maxX) < 3);
        return borde;
    }

    public boolean isBordeIzquierdo() {
        boolean borde = true;
        if (actual != null && getWidth() < (actual.getWidth() * mScaleFactor))
            borde = ((-mFposX - 3) <= minX);
        return borde;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.5f));// TODO
            // Check

            // trasportar al escalar
            if (actual != null) {
                mTPosX = ((int) (actual.getWidth() - (mScaleFactor * actual.getWidth())) / 2);
                mTPosY = ((int) (actual.getHeight() - (mScaleFactor * actual.getHeight())) / 2);
            } else {
                mTPosX = 0;
                mTPosY = 0;
            }
            calcularTopes();
            invalidate();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }

}
