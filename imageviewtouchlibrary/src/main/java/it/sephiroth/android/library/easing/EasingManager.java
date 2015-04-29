package it.sephiroth.android.library.easing;

import android.os.Handler;
import android.os.SystemClock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper class to manage the Easing process.<br />
 * Usage:<br />
 * <pre>
 * // Create the easing manager instance
 * EasingManager manager = new EasingManager( this );
 * // start a Linear easing animation using the easeOut method, from 0.0 to 1.0
 * // and the duration of 200 ms
 * manager.start( Linear.class, EaseType.EaseOut, 0.0, 1.0, 200 );
 * </pre>
 *
 * @author alessandro
 */
public final class EasingManager {

    static final int FPS = 60;
    static final int FRAME_TIME = 1000 / FPS;
    static final Handler mHandler = new Handler();
    Easing mEasing;
    Method mMethod;
    boolean mRunning;
    long mBase;
    int mDuration;
    double mStartValue;
    double mEndValue;
    double mValue;
    boolean mInverted;
    EasingCallback mEasingCallback;
    String mToken;
    Ticker mTicker;

    public EasingManager(EasingCallback callback) {
        mEasingCallback = callback;
        mToken = String.valueOf(System.currentTimeMillis());
    }

    public void start(Class<? extends Easing> clazz, EaseType type, double fromValue, double endValue, int durationMillis) {
        start(clazz, type, fromValue, endValue, durationMillis, 0);
    }

    /**
     * Start the easing with a delay
     *
     * @param clazz          the Easing class to be used for the interpolation
     * @param type           the Easing Type
     * @param fromValue      the start value of the easing
     * @param endValue       the end value of the easing
     * @param durationMillis the duration in ms of the easing
     * @param delayMillis    the delay
     */
    public void start(Class<? extends Easing> clazz, EaseType type, double fromValue, double endValue, int durationMillis, long delayMillis) {
        if (!mRunning) {
            mEasing = createInstance(clazz);

            if (null == mEasing) {
                return;
            }

            mMethod = getEasingMethod(mEasing, type);
            if (mMethod == null) {
                return;
            }

            mInverted = fromValue > endValue;

            if (mInverted) {
                mStartValue = endValue;
                mEndValue = fromValue;
            } else {
                mStartValue = fromValue;
                mEndValue = endValue;
            }
            mValue = mStartValue;

            mDuration = durationMillis;
            mBase = SystemClock.uptimeMillis() + delayMillis;
            mRunning = true;
            mTicker = new Ticker();
            long next = SystemClock.uptimeMillis() + FRAME_TIME + delayMillis;

            if (delayMillis == 0) {
                mEasingCallback.onEasingStarted(fromValue);
            } else {
                mHandler.postAtTime(new TickerStart(fromValue), mToken, next - FRAME_TIME);
            }

            mHandler.postAtTime(mTicker, mToken, next);
        }
    }

    /**
     * Stop the current easing process. onEasingFinished will not be invoked
     */
    public void stop() {
        mRunning = false;
        mHandler.removeCallbacks(mTicker, mToken);
    }

    Easing createInstance(Class<? extends Easing> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    Method getEasingMethod(Easing instance, EaseType type) {

        String methodName = getMethodName(type);
        if (null != methodName) {
            Method m;
            try {
                m = instance.getClass().getMethod(methodName, double.class, double.class, double.class, double.class);
            } catch (SecurityException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            }
            return m;
        }
        return null;
    }

    String getMethodName(EaseType type) {
        switch (type) {
            case EaseIn:
                return "easeIn";
            case EaseInOut:
                return "easeInOut";
            case EaseNone:
                return "easeNone";
            case EaseOut:
                return "easeOut";
        }
        return null;
    }

    /**
     * Note that easeNone is valid only used
     * with easing Linear
     */
    public enum EaseType {
        EaseIn, EaseOut, EaseInOut, EaseNone
    }

    /**
     * Implement this callback in order to get updates from
     * the running easing
     */
    public interface EasingCallback {
        void onEasingValueChanged(double value, double oldValue);

        void onEasingStarted(double value);

        void onEasingFinished(double value);
    }

    class TickerStart implements Runnable {

        double mValue;

        public TickerStart(double value) {
            mValue = value;
        }

        @Override
        public void run() {
            mEasingCallback.onEasingStarted(mValue);
        }
    }

    class Ticker implements Runnable {

        @Override
        public void run() {
            long base = mBase;
            long now = SystemClock.uptimeMillis();
            long diff = now - base;

            double old = mValue;
            double value;
            try {
                value = (Double) mMethod.invoke(mEasing, diff, mStartValue, mEndValue, mDuration);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return;
            }

            mValue = value;

            int frame = (int) (diff / FRAME_TIME);
            long next = base + ((frame + 1) * FRAME_TIME);

            if (diff < mDuration) {
                mEasingCallback.onEasingValueChanged(mInverted ? mEndValue - value : value, old);
                mHandler.postAtTime(this, mToken, next);
            } else {
                mEasingCallback.onEasingFinished(mInverted ? mEndValue : mStartValue);
                mRunning = false;
            }
        }
    }

}
