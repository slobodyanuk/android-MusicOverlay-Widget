package com.musicoverlaywidget.views.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.musicoverlaywidget.managers.TouchCallback;
import com.musicoverlaywidget.managers.TouchManager;

public class FlingGestureAnimator {
    private static final long DEFAULT_ANIM_DURATION = 200;
    private final ValueAnimator flingGestureAnimator;
    private final PropertyValuesHolder dxHolder;
    private final PropertyValuesHolder dyHolder;
    private final Interpolator interpolator;
    private final TouchCallback callback;
    private final WindowManager windowManager;
    private final View view;
    private WindowManager.LayoutParams params;
    private final TouchManager.BoundsChecker boundsChecker;
    private StickyEdgeAnimator stickyEdgeAnimator;
    private float screenWidth;

    public FlingGestureAnimator(TouchCallback callback, WindowManager windowManager,
                                View view, TouchManager.BoundsChecker boundsChecker,
                                StickyEdgeAnimator stickyEdgeAnimator, float screenWidth) {
        this.callback = callback;
        this.windowManager = windowManager;
        this.view = view;
        this.params = (WindowManager.LayoutParams) view.getLayoutParams();
        this.boundsChecker = boundsChecker;
        this.stickyEdgeAnimator = stickyEdgeAnimator;
        this.screenWidth = screenWidth;
        interpolator = new DecelerateInterpolator();
        dxHolder = PropertyValuesHolder.ofFloat("x", 0, 0);
        dyHolder = PropertyValuesHolder.ofFloat("y", 0, 0);
        dxHolder.setEvaluator(new FloatEvaluator());
        dyHolder.setEvaluator(new FloatEvaluator());
        flingGestureAnimator = ValueAnimator.ofPropertyValuesHolder(dxHolder, dyHolder);
        flingGestureAnimator.setInterpolator(interpolator);
        flingGestureAnimator.setDuration(DEFAULT_ANIM_DURATION);
        flingGestureAnimator.addUpdateListener(animation -> {
            float newX = (float) animation.getAnimatedValue("x");
            float newY = (float) animation.getAnimatedValue("y");
            if (this.callback != null) {
                this.callback.onMoved(newX - this.params.x, newY - this.params.y);
            }
            this.params.x = (int) newX;
            this.params.y = (int) newY;

            try {
                this.windowManager.updateViewLayout(this.view, this.params);
            } catch (IllegalArgumentException e) {
                animation.cancel();
            }
        });
        flingGestureAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                FlingGestureAnimator.this.stickyEdgeAnimator.animate(FlingGestureAnimator.this.boundsChecker);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                FlingGestureAnimator.this.stickyEdgeAnimator.animate(FlingGestureAnimator.this.boundsChecker);
            }
        });
    }

    public void animate(float velocityX, float velocityY) {
        if (isAnimating()) {
            return;
        }

        params = (WindowManager.LayoutParams) view.getLayoutParams();

        float dx = velocityX / 1000f * DEFAULT_ANIM_DURATION;
        float dy = velocityY / 1000f * DEFAULT_ANIM_DURATION;

        final float newX, newY;

        if (dx + params.x > screenWidth / 2f) {
            newX = boundsChecker.stickyRightSide(screenWidth) + Math.min(view.getWidth(), view.getHeight()) / 2f;
        } else {
            newX = boundsChecker.stickyLeftSide(screenWidth) - Math.min(view.getWidth(), view.getHeight()) / 2f;
        }

        newY = params.y + dy;

        dxHolder.setFloatValues(params.x, newX);
        dyHolder.setFloatValues(params.y, newY);

        flingGestureAnimator.start();
    }

    public boolean isAnimating() {
        return flingGestureAnimator.isRunning();
    }
}