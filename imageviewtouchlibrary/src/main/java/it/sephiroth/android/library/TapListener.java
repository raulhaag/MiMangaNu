package it.sephiroth.android.library;

import android.view.MotionEvent;

public interface TapListener {
    boolean onSingleTapConfirmed(MotionEvent e);
}