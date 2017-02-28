package com.musicoverlaywidget.managers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.musicoverlaywidget.R;
import com.musicoverlaywidget.utils.DrawableUtils;
import com.musicoverlaywidget.views.animations.FlingGestureAnimator;
import com.musicoverlaywidget.views.animations.StickyEdgeAnimator;

/**
 * Created by Serhii Slobodianiuk on 27.02.2017.
 */

public class TouchManager implements View.OnTouchListener{

    private final View view;
    private final BoundsChecker boundsChecker;
    private final WindowManager windowManager;
    private final StickyEdgeAnimator stickyEdgeAnimator;
    private final FlingGestureAnimator velocityAnimator;

    private GestureListener gestureListener;
    private GestureDetector gestureDetector;
    private TouchCallback callback;
    private int screenWidth;
    private int screenHeight;
    private Float lastRawX, lastRawY;
    private boolean touchCanceled;

    public TouchManager(@NonNull View view, @NonNull BoundsChecker boundsChecker) {
        this.gestureDetector = new GestureDetector(view.getContext(), gestureListener = new GestureListener());
        gestureDetector.setIsLongpressEnabled(true);
        this.view = view;
        this.boundsChecker = boundsChecker;
        this.view.setOnTouchListener(this);
        Context context = view.getContext().getApplicationContext();
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels - context.getResources().getDimensionPixelSize(R.dimen.widget_status_bar_height);
        stickyEdgeAnimator = new StickyEdgeAnimator(callback, windowManager, view, screenWidth, screenHeight);
        velocityAnimator = new FlingGestureAnimator(callback, windowManager, view, boundsChecker, stickyEdgeAnimator, screenWidth);
    }

    public TouchManager screenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
        return this;
    }

    public TouchManager screenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
        return this;
    }

    public TouchManager callback(TouchCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean res = (!touchCanceled || event.getAction() == MotionEvent.ACTION_UP) && gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchCanceled = false;
            gestureListener.onDown(event);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!touchCanceled) {
                gestureListener.onUpEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!touchCanceled) {
                gestureListener.onMove(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            gestureListener.onTouchOutsideEvent(event);
            touchCanceled = false;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            touchCanceled = true;
        }
        return res;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private int prevX, prevY;
        private float velX, velY;
        private long lastEventTime;

        @Override
        public boolean onDown(MotionEvent e) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
            prevX = params.x;
            prevY = params.y;
            boolean result = !stickyEdgeAnimator.isAnimating();
            if (result) {
                if (callback != null) {
                    callback.onTouched(e.getX(), e.getY());
                }
            }
            return result;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (callback != null) {
                callback.onClick(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float diffX = e2.getRawX() - e1.getRawX();
            float diffY = e2.getRawY() - e1.getRawY();
            float l = prevX + diffX;
            float t = prevY + diffY;
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
            params.x = (int) l;
            params.y = (int) t;
            try {
                windowManager.updateViewLayout(view, params);
            } catch (IllegalArgumentException e) {
                // view not attached to window
            }
            if (callback != null) {
                callback.onMoved(distanceX, distanceY);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            velocityAnimator.animate(velX, velY);
            return true;
        }

        private void onMove(MotionEvent e2) {
            if (lastRawX != null && lastRawY != null) {
                long diff = e2.getEventTime() - lastEventTime;
                float dt = diff == 0 ? 0 : 1000f / diff;
                float newVelX = (e2.getRawX() - lastRawX) * dt;
                float newVelY = (e2.getRawY() - lastRawY) * dt;
                velX = DrawableUtils.smooth(velX, newVelX, 0.2f);
                velY = DrawableUtils.smooth(velY, newVelY, 0.2f);
            }
            lastRawX = e2.getRawX();
            lastRawY = e2.getRawY();
            lastEventTime = e2.getEventTime();
        }

        private void onUpEvent(MotionEvent e) {
            if (callback != null) {
                callback.onReleased(e.getX(), e.getY());
            }
            lastRawX = null;
            lastRawY = null;
            lastEventTime = 0;
            velX = velY = 0;
            if (!velocityAnimator.isAnimating()) {
                stickyEdgeAnimator.animate(boundsChecker);
            }
        }

        private void onTouchOutsideEvent(MotionEvent e) {
            if (callback != null) {
                callback.onTouchOutside();
            }
        }
    }

    public static class SimpleCallback implements TouchCallback {

        @Override
        public void onClick(float x, float y) {

        }

        @Override
        public void onTouchOutside() {

        }

        @Override
        public void onTouched(float x, float y) {

        }

        @Override
        public void onMoved(float diffX, float diffY) {

        }

        @Override
        public void onReleased(float x, float y) {

        }

        @Override
        public void onAnimationCompleted() {

        }

    }

    public void animateToBounds(BoundsChecker boundsChecker, @Nullable Runnable afterAnimation) {
        stickyEdgeAnimator.animate(boundsChecker, afterAnimation);
    }

    public void animateToBounds() {
        stickyEdgeAnimator.animate(boundsChecker, null);
    }

    public interface BoundsChecker {

        float stickyLeftSide(float screenWidth);

        float stickyRightSide(float screenWidth);

        float stickyTopSide(float screenHeight);

        float stickyBottomSide(float screenHeight);
    }
}
