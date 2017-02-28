package com.musicoverlaywidget;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import com.musicoverlaywidget.controllers.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.musicoverlaywidget.controllers.Controller;
import com.musicoverlaywidget.controllers.OnControlsClickListener;
import com.musicoverlaywidget.controllers.OnWidgetStateChangedListener;
import com.musicoverlaywidget.controllers.State;
import com.musicoverlaywidget.managers.TouchManager;
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

    private PlaybackState playbackState;

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
    private int animatedRemBtnYPos = -1;
    private float widgetWidth, widgetHeight, radius;
    private final OnControlsClickListenerWrapper onControlsClickListener;
    private boolean shown;
    private boolean released;
    private boolean removeWidgetShown;
    private OnWidgetStateChangedListener onWidgetStateChangedListener;

    private final Point hiddenRemWidPos;



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
        return new Configuration.Builder()
                .setContext(context)
                .playbackState(playbackState)
                .random(new Random())
                .accDecInterpolator(new AccelerateDecelerateInterpolator())
                .darkColor(darkColor)
                .playColor(lightColor)
                .progressColor(progressColor)
                .expandedColor(expandColor)
                .widgetWidth(widgetWidth)
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
    }

    public void show(int cx, int cy){

    }

    public void hide() {

    }

    public void expand() {
    }

    public void collapse() {
    }


    private void updatePlayPauseButtonPosition() {

    }

    @NonNull
    public Controller controller() {
        return controller;
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
                    playbackState.start(AudioWidget.this);
                } else {
                    playbackState.pause(AudioWidget.this);
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


}
