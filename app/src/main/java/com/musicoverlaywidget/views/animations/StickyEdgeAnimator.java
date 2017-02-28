package com.musicoverlaywidget.views.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.musicoverlaywidget.managers.TouchCallback;
import com.musicoverlaywidget.managers.TouchManager;

public class StickyEdgeAnimator {

    private static final long DEFAULT_ANIM_DURATION = 300;
    private final PropertyValuesHolder dxHolder;
    private final PropertyValuesHolder dyHolder;
    private final ValueAnimator edgeAnimator;
    private final Interpolator interpolator;

    private final TouchCallback callback;
    private final WindowManager windowManager;
    private final View view;

    private WindowManager.LayoutParams params;
    private float screenWidth;
    private float screenHeight;

    public StickyEdgeAnimator(TouchCallback callback, WindowManager windowManager,
                              View view, float screenWidth, float screenHeight) {
        this.callback = callback;
        this.windowManager = windowManager;
        this.view = view;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        interpolator = new OvershootInterpolator();
        dxHolder = PropertyValuesHolder.ofInt("x", 0, 0);
        dyHolder = PropertyValuesHolder.ofInt("y", 0, 0);
        dxHolder.setEvaluator(new IntEvaluator());
        dyHolder.setEvaluator(new IntEvaluator());
        edgeAnimator = ValueAnimator.ofPropertyValuesHolder(dxHolder, dyHolder);
        edgeAnimator.setInterpolator(interpolator);
        edgeAnimator.setDuration(DEFAULT_ANIM_DURATION);
        edgeAnimator.addUpdateListener(animation -> {
            int x = (int) animation.getAnimatedValue("x");
            int y = (int) animation.getAnimatedValue("y");
            if (this.callback != null) {
                this.callback.onMoved(x - params.x, y - params.y);
            }
            params.x = x;
            params.y = y;
            try {
                this.windowManager.updateViewLayout(this.view, params);
            } catch (IllegalArgumentException e) {
                // view not attached to window
                animation.cancel();
            }
        });
        edgeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (StickyEdgeAnimator.this.callback != null) {
                    StickyEdgeAnimator.this.callback.onAnimationCompleted();
                }
            }
        });
    }

    public void animate(TouchManager.BoundsChecker boundsChecker) {
        animate(boundsChecker, null);
    }

    public void animate(TouchManager.BoundsChecker boundsChecker, @Nullable Runnable afterAnimation) {
        if (edgeAnimator.isRunning()) {
            return;
        }
        params = (WindowManager.LayoutParams) view.getLayoutParams();
        float cx = params.x + view.getWidth() / 2f;
        float cy = params.y + view.getWidth() / 2f;
        int x;
        if (cx < screenWidth / 2f) {
            x = (int) boundsChecker.stickyLeftSide(screenWidth);
        } else {
            x = (int) boundsChecker.stickyRightSide(screenWidth);
        }
        int y = params.y;
        int top = (int) boundsChecker.stickyTopSide(screenHeight);
        int bottom = (int) boundsChecker.stickyBottomSide(screenHeight);
        if (params.y > bottom || params.y < top) {
            if (cy < screenHeight / 2f) {
                y = top;
            } else {
                y = bottom;
            }
        }
        dxHolder.setIntValues(params.x, x);
        dyHolder.setIntValues(params.y, y);
        edgeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                edgeAnimator.removeListener(this);
                if (afterAnimation != null) {
                    afterAnimation.run();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                edgeAnimator.removeListener(this);
                if (afterAnimation != null) {
                    afterAnimation.run();
                }
            }
        });
        edgeAnimator.start();
    }

    public boolean isAnimating() {
        return edgeAnimator.isRunning();
    }
}