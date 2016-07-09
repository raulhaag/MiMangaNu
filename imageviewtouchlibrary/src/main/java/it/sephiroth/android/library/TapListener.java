package it.sephiroth.android.library;

import android.test.MoreAsserts;
import android.view.MotionEvent;

public interface TapListener {
    boolean onSingleTapConfirmed(MotionEvent e);
    boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
}