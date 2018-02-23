package ar.rulosoft.mimanganu.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import io.codetail.animation.ViewAnimationUtils;


public class AnimationUtils {
    public static void registerCircularRevealAnimation(final Context context, final View view, final RevealAnimationSetting revealSettings, final int startColor, final int endColor) {
            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    int cx = revealSettings.getCenterX();
                    int cy = revealSettings.getCenterY();
                    int width = revealSettings.getWidth();
                    int height = revealSettings.getHeight();
                    int duration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);

                    //Simply use the diagonal of the view
                    float finalRadius = (float) Math.sqrt(width * width + height * height);
                    Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius).setDuration(duration);
                    anim.setInterpolator(new FastOutSlowInInterpolator());
                    anim.start();
                    startColorAnimation(view, startColor, endColor, duration);
                }
            });

    }

    static void startColorAnimation(final View view, final int startColor, final int endColor, int duration) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(startColor, endColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });
        anim.setDuration(duration);
        anim.start();
    }

    public static void startCircularExitAnimation(final Context context, final View view, final RevealAnimationSetting revealSettings, final int startColor, final int endColor, final Dismissible.OnDismissedListener listener) {
            int cx = revealSettings.getCenterX();
            int cy = revealSettings.getCenterY();
            int width = revealSettings.getWidth();
            int height = revealSettings.getHeight();
            int duration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

            float initRadius = (float) Math.sqrt(width * width + height * height);
            final Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initRadius,  revealSettings.getInitialRadius());
            anim.setDuration(duration);
            anim.setInterpolator(new FastOutSlowInInterpolator());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    listener.onDismissed();
                }

            });
            anim.start();
            startColorAnimation(view, startColor, endColor, duration);

    }
}


