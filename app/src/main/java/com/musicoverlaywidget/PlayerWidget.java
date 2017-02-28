package com.musicoverlaywidget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.musicoverlaywidget.controllers.Controller;
import com.musicoverlaywidget.controllers.OnControlsClickListener;
import com.musicoverlaywidget.controllers.OnWidgetStateChangedListener;
import com.musicoverlaywidget.controllers.PlaybackState;
import com.musicoverlaywidget.controllers.State;
import com.musicoverlaywidget.managers.TouchManager;
import com.musicoverlaywidget.utils.DrawableUtils;
import com.musicoverlaywidget.views.ExpandCollapseWidget;
import com.musicoverlaywidget.views.PlayPauseButton;
import com.musicoverlaywidget.views.RemoveWidgetView;
import com.thefinestartist.utils.ui.DisplayUtil;

import java.util.Random;

/**
 * Created by Serhii Slobodianiuk on 27.02.2017.
 */

public class PlayerWidget {

    private static final long VIBRATION_DURATION = 100;

    private final PlayPauseButton playPauseButton;
    private final ExpandCollapseWidget expandCollapseWidget;
    private final RemoveWidgetView removeWidgetView;
    private final Controller controller;
    private final WindowManager windowManager;
    private final Vibrator vibrator;
    private final Handler handler;
    private final Point screenSize;
    private final Context context;
    private final TouchManager playPauseButtonManager;
    private final TouchManager expandedWidgetManager;
    private final TouchManager.BoundsChecker ppbToExpBoundsChecker;
    private final TouchManager.BoundsChecker expToPpbBoundsChecker;
    private final RectF removeBounds;
    private final Point visibleRemWidPos;
    private final OnControlsClickListenerWrapper onControlsClickListener;
    private final Point hiddenRemWidPos;
    private PlaybackState playbackState;
    private int animatedRemBtnYPos = -1;
    private float widgetWidth, widgetHeight, radius;
    private boolean shown;
    private boolean released;
    private boolean removeWidgetShown;
    private OnWidgetStateChangedListener onWidgetStateChangedListener;


    public PlayerWidget(@NonNull WidgetBuilder builder) {
        this.context = builder.getContext().getApplicationContext();
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.handler = new Handler();
        this.screenSize = new Point();
        this.removeBounds = new RectF();
        this.hiddenRemWidPos = new Point();
        this.visibleRemWidPos = new Point();
        this.controller = newController();
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            windowManager.getDefaultDisplay().getSize(screenSize);
        } else {
            screenSize.x = DisplayUtil.getWidth();
            screenSize.y = DisplayUtil.getHeight();
        }
        screenSize.y -= DisplayUtil.getStatusBarHeight() + navigationBarHeight();

        Configuration configuration = prepareConfiguration(builder);
        playPauseButton = new PlayPauseButton(configuration);
        expandCollapseWidget = new ExpandCollapseWidget(configuration);
        removeWidgetView = new RemoveWidgetView(configuration);
        int offsetCollapsed = context.getResources().getDimensionPixelOffset(R.dimen.widget_edge_offset_collapsed);
        int offsetExpanded = context.getResources().getDimensionPixelOffset(R.dimen.widget_edge_offset_expanded);

        playPauseButtonManager = new TouchManager(playPauseButton, playPauseButton.newBoundsChecker(
                builder.isEdgeOffsetXCollapsedSet() ? builder.getEdgeOffsetXCollapsed() : offsetCollapsed,
                builder.isEdgeOffsetYCollapsedSet() ? builder.getEdgeOffsetYCollapsed() : offsetCollapsed
        ))
                .screenWidth(screenSize.x)
                .screenHeight(screenSize.y);

        expandedWidgetManager = new TouchManager(expandCollapseWidget, expandCollapseWidget.newBoundsChecker(
                builder.isEdgeOffsetXExpandedSet() ? builder.getEdgeOffsetXExpanded() : offsetExpanded,
                builder.isEdgeOffsetYExpandedSet() ? builder.getEdgeOffsetYExpanded() : offsetExpanded
        ))
                .screenWidth(screenSize.x)
                .screenHeight(screenSize.y);

        playPauseButtonManager.callback(new PlayPauseButtonCallback());
        expandedWidgetManager.callback(new ExpandCollapseWidgetCallback());
        expandCollapseWidget.onWidgetStateChangedListener(new OnWidgetStateChangedListener() {
            @Override
            public void onWidgetStateChanged(@NonNull State state) {
                if (state == State.COLLAPSED) {
                    playPauseButton.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    try {
                        windowManager.removeView(expandCollapseWidget);
                    } catch (IllegalArgumentException e) {
                        // view not attached to window
                    }
                    playPauseButton.enableProgressChanges(true);
                }
                if (onWidgetStateChangedListener != null) {
                    onWidgetStateChangedListener.onWidgetStateChanged(state);
                }
            }

            @Override
            public void onWidgetPositionChanged(int cx, int cy) {

            }
        });
        onControlsClickListener = new OnControlsClickListenerWrapper();
        expandCollapseWidget.onControlsClickListener(onControlsClickListener);

        ppbToExpBoundsChecker = playPauseButton.newBoundsChecker(
                builder.isEdgeOffsetXExpandedSet() ? builder.getEdgeOffsetXExpanded() : offsetExpanded,
                builder.isEdgeOffsetYExpandedSet() ? builder.getEdgeOffsetYExpanded() : offsetExpanded
        );

        expToPpbBoundsChecker = expandCollapseWidget.newBoundsChecker(
                builder.isEdgeOffsetXCollapsedSet() ? builder.getEdgeOffsetXCollapsed() : offsetCollapsed,
                builder.isEdgeOffsetYCollapsedSet() ? builder.getEdgeOffsetYCollapsed() : offsetCollapsed
        );
    }

    private Configuration prepareConfiguration(@NonNull WidgetBuilder builder) {
        int darkColor = builder.isDarkColorSet() ?
                builder.getDarkColor() :
                ContextCompat.getColor(context, R.color.widget_dark);
        int lightColor = builder.isLightColorSet() ?
                builder.getLightColor() :
                ContextCompat.getColor(context, R.color.widget_light);
        int progressColor = builder.isProgressColorSet() ?
                builder.getProgressColor() :
                ContextCompat.getColor(context, R.color.widget_progress);
        int expandColor = builder.isExpandWidgetColorSet() ?
                builder.getExpandWidgetColor() :
                ContextCompat.getColor(context, R.color.widget_expanded);
        int crossColor = builder.isCrossColorSet() ?
                builder.getCrossColor() :
                ContextCompat.getColor(context, R.color.widget_cross_default);
        int crossOverlappedColor = builder.isCrossOverlappedColorSet() ?
                builder.getCrossOverlappedColor() :
                ContextCompat.getColor(context, R.color.widget_cross_overlapped);
        int shadowColor = builder.isShadowColorSet() ?
                builder.getShadowColor() :
                ContextCompat.getColor(context, R.color.widget_shadow);

        Drawable playDrawable = builder.getPlayDrawable() != null ?
                builder.getPlayDrawable() :
                ContextCompat.getDrawable(context, R.drawable.widget_ic_play);
        Drawable pauseDrawable = builder.getPauseDrawable() != null ?
                builder.getPauseDrawable() :
                ContextCompat.getDrawable(context, R.drawable.widget_ic_pause);
        Drawable prevDrawable = builder.getPrevDrawable() != null ?
                builder.getPrevDrawable() :
                ContextCompat.getDrawable(context, R.drawable.widget_ic_prev);
        Drawable nextDrawable = builder.getNextDrawable() != null ?
                builder.getNextDrawable() :
                ContextCompat.getDrawable(context, R.drawable.widget_ic_next);
        Drawable playlistDrawable = builder.getPlaylistDrawable() != null ?
                builder.getPlaylistDrawable() :
                ContextCompat.getDrawable(context, R.drawable.widget_ic_playlist);

        int buttonPadding = builder.isButtonPaddingSet() ?
                builder.getButtonPadding() :
                context.getResources().getDimensionPixelSize(R.dimen.widget_button_padding);
        float crossStrokeWidth = builder.isCrossStrokeWidthSet() ?
                builder.getCrossStrokeWidth() :
                context.getResources().getDimension(R.dimen.widget_cross_stroke_width);
        float progressStrokeWidth = builder.isProgressStrokeWidthSet() ?
                builder.getProgressStrokeWidth() :
                context.getResources().getDimension(R.dimen.widget_progress_stroke_width);
        float shadowRadius = builder.isShadowRadiusSet() ?
                builder.getShadowRadius() :
                context.getResources().getDimension(R.dimen.widget_shadow_radius);
        float shadowDx = builder.isShadowDxSet() ?
                builder.getShadowDx() :
                context.getResources().getDimension(R.dimen.widget_shadow_dx);
        float shadowDy = builder.isShadowDySet() ?
                builder.getShadowDy() :
                context.getResources().getDimension(R.dimen.widget_shadow_dy);
        float bubblesMinSize = builder.isBubblesMinSizeSet() ?
                builder.getBubblesMinSize() :
                context.getResources().getDimension(R.dimen.widget_bubbles_min_size);
        float bubblesMaxSize = builder.isBubblesMaxSizeSet() ?
                builder.getBubblesMaxSize() :
                context.getResources().getDimension(R.dimen.widget_bubbles_max_size);
        int prevNextExtraPadding = context.getResources()
                .getDimensionPixelSize(R.dimen.widget_prev_next_button_extra_padding);

        widgetHeight = context.getResources().getDimensionPixelSize(R.dimen.widget_player_height);
        widgetWidth = context.getResources().getDimensionPixelSize(R.dimen.widget_player_width);
        radius = widgetHeight / 2f;
        playbackState = new PlaybackState();

        Configuration.Builder configBuilder = Configuration.Builder
                .builder()
                .context(context)
                .playbackState(playbackState)
                .random(new Random())
                .accDecInterpolator(new AccelerateDecelerateInterpolator())
                .darkColor(darkColor)
                .lightColor(lightColor)
                .progressColor(progressColor)
                .expandedColor(expandColor)
                .width(widgetWidth)
                .radius(radius)
                .playlistDrawable(playlistDrawable)
                .playDrawable(playDrawable)
                .prevDrawable(prevDrawable)
                .nextDrawable(nextDrawable)
                .pauseDrawable(pauseDrawable)
                .buttonPadding(buttonPadding)
                .prevNextExtraPadding(prevNextExtraPadding)
                .crossStrokeWidth(crossStrokeWidth)
                .progressStrokeWidth(progressStrokeWidth)
                .shadowRadius(shadowRadius)
                .shadowDx(shadowDx)
                .shadowDy(shadowDy)
                .shadowColor(shadowColor)
                .bubblesMinSize(bubblesMinSize)
                .bubblesMaxSize(bubblesMaxSize)
                .crossColor(crossColor)
                .crossOverlappedColor(crossOverlappedColor)
                .build();

        return configBuilder.build();
    }

    private void show(View view, int left, int top) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = left;
        params.y = top;
        windowManager.addView(view, params);
    }

    public void show(int cx, int cy) {
        if (shown) {
            return;
        }
        shown = true;
        float remWidX = screenSize.x / 2f - radius * RemoveWidgetView.SCALE_LARGE;
        hiddenRemWidPos.set((int)remWidX, (int) (screenSize.y + widgetHeight + navigationBarHeight()));
        visibleRemWidPos.set((int)remWidX, (int) (screenSize.y - radius - (hasNavigationBar() ? 0 : widgetHeight)));
        try {
            show(removeWidgetView, hiddenRemWidPos.x, hiddenRemWidPos.y);
        } catch (IllegalArgumentException e) {
            // widget not removed yet, animation in progress
        }
        show(playPauseButton, (int) (cx - widgetHeight), (int) (cy - widgetHeight));
        playPauseButtonManager.animateToBounds();
    }

    /**
     * Hide widget.
     */
    public void hide() {
        hideInternal(true);
    }

    private void hideInternal(boolean byPublic) {
        if (!shown) {
            return;
        }
        shown = false;
        released = true;
        try {
            windowManager.removeView(playPauseButton);
        } catch (IllegalArgumentException e) {
            // view not attached to window
        }
        if (byPublic) {
            try {
                windowManager.removeView(removeWidgetView);
            } catch (IllegalArgumentException e) {
                // view not attached to window
            }
        }
        try {
            windowManager.removeView(expandCollapseWidget);
        } catch (IllegalArgumentException e) {
            // widget not added to window yet
        }
        if (onWidgetStateChangedListener != null) {
            onWidgetStateChangedListener.onWidgetStateChanged(State.REMOVED);
        }
    }


    public void expand() {
        removeWidgetShown = false;
        playPauseButton.enableProgressChanges(false);
        playPauseButton.postDelayed(this::checkSpaceAndShowExpanded, PlayPauseButton.PROGRESS_CHANGES_DURATION);
    }

    public void collapse() {
        expandCollapseWidget.setCollapseListener(playPauseButton::setAlpha);

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) expandCollapseWidget.getLayoutParams();
        int cx = params.x + expandCollapseWidget.getWidth() / 2;
        if(cx > screenSize.x / 2) {
            expandCollapseWidget.expandDirection(ExpandCollapseWidget.DIRECTION_LEFT);
        } else {
            expandCollapseWidget.expandDirection(ExpandCollapseWidget.DIRECTION_RIGHT);
        }
        updatePlayPauseButtonPosition();
        if (expandCollapseWidget.collapse()) {
            playPauseButtonManager.animateToBounds();
            expandedWidgetManager.animateToBounds(expToPpbBoundsChecker, null);
        }
    }

    private void checkSpaceAndShowExpanded() {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
        int x = params.x;
        int y = params.y;
        int expandDirection;
        if (x + widgetHeight > screenSize.x / 2) {
            expandDirection = ExpandCollapseWidget.DIRECTION_LEFT;
        } else {
            expandDirection = ExpandCollapseWidget.DIRECTION_RIGHT;
        }

        playPauseButtonManager.animateToBounds(ppbToExpBoundsChecker, () -> {
            WindowManager.LayoutParams params1 = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
            int x1 = params1.x;
            int y1 = params1.y;
            if (expandDirection == ExpandCollapseWidget.DIRECTION_LEFT) {
                x1 -= widgetWidth - widgetHeight * 1.5f;
            } else {
                x1 += widgetHeight / 2f;
            }
            show(expandCollapseWidget, x1, y1);
            playPauseButton.setLayerType(View.LAYER_TYPE_NONE, null);

            expandCollapseWidget.setExpandListener(percent -> playPauseButton.setAlpha(1f - percent));
            expandCollapseWidget.expand(expandDirection);
        });
    }

    private void updatePlayPauseButtonPosition() {
        WindowManager.LayoutParams widgetParams = (WindowManager.LayoutParams) expandCollapseWidget.getLayoutParams();
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
        if (expandCollapseWidget.expandDirection() == ExpandCollapseWidget.DIRECTION_RIGHT) {
            params.x = (int) (widgetParams.x - radius);
        } else {
            params.x = (int) (widgetParams.x + widgetWidth - widgetHeight - radius);
        }
        params.y = widgetParams.y;
        try {
            windowManager.updateViewLayout(playPauseButton, params);
        } catch (IllegalArgumentException e) {
            // view not attached to window
        }
        if (onWidgetStateChangedListener != null) {
            onWidgetStateChangedListener.onWidgetPositionChanged((int) (params.x + widgetHeight), (int) (params.y + widgetHeight));
        }
    }

    @NonNull
    public Controller controller() {
        return controller;
    }

    @NonNull
    private Controller newController() {
        return new Controller() {

            @Override
            public void start() {
                playbackState.start(this);
            }

            @Override
            public void pause() {
                playbackState.pause(this);
            }

            @Override
            public void stop() {
                playbackState.stop(this);
            }

            @Override
            public int duration() {
                return playbackState.duration();
            }

            @Override
            public void duration(int duration) {
                playbackState.duration(duration);
            }

            @Override
            public int position() {
                return playbackState.position();
            }

            @Override
            public void position(int position) {
                playbackState.position(position);
            }

            @Override
            public void onControlsClickListener(@Nullable OnControlsClickListener onControlsClickListener) {
                PlayerWidget.this.onControlsClickListener.onControlsClickListener(onControlsClickListener);
            }

            @Override
            public void onWidgetStateChangedListener(@Nullable OnWidgetStateChangedListener onWidgetStateChangedListener) {
                PlayerWidget.this.onWidgetStateChangedListener = onWidgetStateChangedListener;
            }
        };
    }

    private boolean hasNavigationBar() {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return !hasBackKey && !hasHomeKey || id > 0 && context.getResources().getBoolean(id);
    }

    private int navigationBarHeight() {
        if (hasNavigationBar()) {
            int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return context.getResources().getDimensionPixelSize(resourceId);
            }
            return context.getResources().getDimensionPixelSize(R.dimen.widget_navigation_bar_height);
        }
        return 0;
    }

    private class OnControlsClickListenerWrapper implements OnControlsClickListener {

        private OnControlsClickListener onControlsClickListener;

        public OnControlsClickListenerWrapper onControlsClickListener(OnControlsClickListener inner) {
            this.onControlsClickListener = inner;
            return this;
        }

        @Override
        public boolean onPlaylistClicked() {
            if (onControlsClickListener == null || !onControlsClickListener.onPlaylistClicked()) {
                collapse();
                return true;
            }
            return false;
        }

        @Override
        public void onPreviousClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onPreviousClicked();
            }
        }

        @Override
        public boolean onPlayPauseClicked() {
            if (onControlsClickListener == null || !onControlsClickListener.onPlayPauseClicked()) {
                if (playbackState.state() != Configuration.STATE_PLAYING) {
                    playbackState.start(PlayerWidget.this);
                } else {
                    playbackState.pause(PlayerWidget.this);
                }
                return true;
            }
            return false;
        }

        @Override
        public void onNextClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onNextClicked();
            }
        }

    }

    private class PlayPauseButtonCallback extends TouchManager.SimpleCallback {

        private static final long REMOVE_BTN_ANIM_DURATION = 200;
        private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener;
        private boolean readyToRemove;

        PlayPauseButtonCallback() {
            animatorUpdateListener = animation -> {
                if (!removeWidgetShown) {
                    return;
                }
                animatedRemBtnYPos = (int)((float) animation.getAnimatedValue());
                updateRemoveBtnPosition();
            };
        }

        @Override
        public void onClick(float x, float y) {
            playPauseButton.onClick();
            if (onControlsClickListener != null) {
                onControlsClickListener.onPlayPauseClicked();
            }
        }

        @Override
        public void onTouched(float x, float y) {
            super.onTouched(x, y);
            released = false;
            handler.postDelayed(() -> {
                if (!released) {
                    removeWidgetShown = true;
                    ValueAnimator animator = ValueAnimator.ofFloat(hiddenRemWidPos.y, visibleRemWidPos.y);
                    animator.setDuration(REMOVE_BTN_ANIM_DURATION);
                    animator.addUpdateListener(animatorUpdateListener);
                    animator.start();
                }
            }, Configuration.LONG_CLICK_THRESHOLD);
            playPauseButton.onTouchDown();
        }

        @Override
        public void onMoved(float diffX, float diffY) {
            super.onMoved(diffX, diffY);
            boolean curReadyToRemove = isReadyToRemove();
            if (curReadyToRemove != readyToRemove) {
                readyToRemove = curReadyToRemove;
                removeWidgetView.setOverlapped(readyToRemove);
                if (readyToRemove && vibrator.hasVibrator()) {
                    vibrator.vibrate(VIBRATION_DURATION);
                }
            }
            updateRemoveBtnPosition();
        }

        private void updateRemoveBtnPosition() {
            if(removeWidgetShown) {
                WindowManager.LayoutParams playPauseBtnParams = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
                WindowManager.LayoutParams removeBtnParams = (WindowManager.LayoutParams) removeWidgetView.getLayoutParams();

                double tgAlpha = (screenSize.x / 2. - playPauseBtnParams.x) / (visibleRemWidPos.y - playPauseBtnParams.y);
                double rotationDegrees = 360 - Math.toDegrees(Math.atan(tgAlpha));

                float distance = (float) Math.sqrt(Math.pow(animatedRemBtnYPos - playPauseBtnParams.y, 2) +
                        Math.pow(visibleRemWidPos.x - hiddenRemWidPos.x, 2));
                float maxDistance = (float) Math.sqrt(Math.pow(screenSize.x, 2) + Math.pow(screenSize.y, 2));
                distance /= maxDistance;

                if (animatedRemBtnYPos == -1) {
                    animatedRemBtnYPos = visibleRemWidPos.y;
                }

                removeBtnParams.x = (int) DrawableUtils.rotateX(
                        visibleRemWidPos.x, animatedRemBtnYPos - radius * distance,
                        hiddenRemWidPos.x, animatedRemBtnYPos, (float) rotationDegrees);
                removeBtnParams.y = (int) DrawableUtils.rotateY(
                        visibleRemWidPos.x, animatedRemBtnYPos - radius * distance,
                        hiddenRemWidPos.x, animatedRemBtnYPos, (float) rotationDegrees);

                try {
                    windowManager.updateViewLayout(removeWidgetView, removeBtnParams);
                } catch (IllegalArgumentException e) {
                    // view not attached to window
                }
            }
        }

        @Override
        public void onReleased(float x, float y) {
            super.onReleased(x, y);
            playPauseButton.onTouchUp();
            released = true;
            if (removeWidgetShown) {
                ValueAnimator animator = ValueAnimator.ofFloat(visibleRemWidPos.y, hiddenRemWidPos.y);
                animator.setDuration(REMOVE_BTN_ANIM_DURATION);
                animator.addUpdateListener(animatorUpdateListener);
                animator.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        removeWidgetShown = false;
                        if (!shown) {
                            try {
                                windowManager.removeView(removeWidgetView);
                            } catch (IllegalArgumentException e) {
                                // view not attached to window
                            }
                        }
                    }
                });
                animator.start();
            }
            if (isReadyToRemove()) {
                hideInternal(false);
            } else {
                if (onWidgetStateChangedListener != null) {
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
                    onWidgetStateChangedListener.onWidgetPositionChanged((int) (params.x + widgetHeight), (int) (params.y + widgetHeight));
                }
            }
        }

        @Override
        public void onAnimationCompleted() {
            super.onAnimationCompleted();
            if (onWidgetStateChangedListener != null) {
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
                onWidgetStateChangedListener.onWidgetPositionChanged((int) (params.x + widgetHeight), (int) (params.y + widgetHeight));
            }
        }

        private boolean isReadyToRemove() {
            WindowManager.LayoutParams removeParams = (WindowManager.LayoutParams) removeWidgetView.getLayoutParams();
            removeBounds.set(removeParams.x, removeParams.y, removeParams.x + widgetHeight, removeParams.y + widgetHeight);
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
            float cx = params.x + widgetHeight;
            float cy = params.y + widgetHeight;
            return removeBounds.contains(cx, cy);
        }
    }

    private class ExpandCollapseWidgetCallback extends TouchManager.SimpleCallback {

        @Override
        public void onTouched(float x, float y) {
            super.onTouched(x, y);
            expandCollapseWidget.onTouched(x, y);
        }

        @Override
        public void onReleased(float x, float y) {
            super.onReleased(x, y);
            expandCollapseWidget.onReleased(x, y);
        }

        @Override
        public void onClick(float x, float y) {
            super.onClick(x, y);
            expandCollapseWidget.onClick(x, y);
        }

        @Override
        public void onTouchOutside() {
            if(!expandCollapseWidget.isAnimationInProgress()) {
                collapse();
            }
        }

        @Override
        public void onMoved(float diffX, float diffY) {
            super.onMoved(diffX, diffY);
            updatePlayPauseButtonPosition();
        }

        @Override
        public void onAnimationCompleted() {
            super.onAnimationCompleted();
            updatePlayPauseButtonPosition();
        }
    }

}
