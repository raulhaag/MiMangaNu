package it.sephiroth.android.library;

import android.test.MoreAsserts;
import android.view.MotionEvent;

public interface TapListener {
    boolean onSingleTapConfirmed(MotionEvent e);
}