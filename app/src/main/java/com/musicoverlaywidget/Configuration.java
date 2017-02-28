package com.musicoverlaywidget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import java.util.Random;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Audio widget configuration class.
 */
public class Configuration {

    public static final float FRAME_SPEED = 70.0f;

    public static final long LONG_CLICK_THRESHOLD = ViewConfiguration.getLongPressTimeout() + 128;
    public static final int STATE_STOPPED = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final long TOUCH_ANIMATION_DURATION = 100;

    private final int lightColor;
    private final int darkColor;
    private final int progressColor;
    private final int expandedColor;
    private final Random random;
    private final float width;
    private final float height;
    private final Drawable playDrawable;
    private final Drawable pauseDrawable;
    private final Drawable prevDrawable;
    private final Drawable nextDrawable;
    private final Drawable playlistDrawable;
    private final Drawable albumDrawable;
    private final Context context;
    private final PlaybackState playbackState;
    private final int buttonPadding;
    private final float crossStrokeWidth;
    private final float progressStrokeWidth;
    private final float shadowRadius;
    private final float shadowDx;
    private final float shadowDy;
    private final int shadowColor;
    private final float bubblesMinSize;
    private final float bubblesMaxSize;
    private final int crossColor;
    private final int crossOverlappedColor;
    private final Interpolator accDecInterpolator;
    private final int prevNextExtraPadding;

    private Configuration(Builder builder) {
        this.context = builder.context;
        this.random = builder.random;
        this.width = builder.width;
        this.height = builder.radius;
        this.lightColor = builder.lightColor;
        this.darkColor = builder.darkColor;
        this.progressColor = builder.progressColor;
        this.expandedColor = builder.expandedColor;
        this.playlistDrawable = builder.playlistDrawable;
        this.playDrawable = builder.playDrawable;
        this.pauseDrawable = builder.pauseDrawable;
        this.prevDrawable = builder.prevDrawable;
        this.nextDrawable = builder.nextDrawable;
        this.albumDrawable = builder.albumDrawable;
        this.playbackState = builder.playbackState;
        this.buttonPadding = builder.buttonPadding;
        this.crossStrokeWidth = builder.crossStrokeWidth;
        this.progressStrokeWidth = builder.progressStrokeWidth;
        this.shadowRadius = builder.shadowRadius;
        this.shadowDx = builder.shadowDx;
        this.shadowDy = builder.shadowDy;
        this.shadowColor = builder.shadowColor;
        this.bubblesMinSize = builder.bubblesMinSize;
        this.bubblesMaxSize = builder.bubblesMaxSize;
        this.crossColor = builder.crossColor;
        this.crossOverlappedColor = builder.crossOverlappedColor;
        this.accDecInterpolator = builder.accDecInterpolator;
        this.prevNextExtraPadding = builder.prevNextExtraPadding;
    }

    public Context context() {
        return context;
    }

    public Random random() {
        return random;
    }

    @ColorInt
    public int lightColor() {
        return lightColor;
    }

    @ColorInt
    public int darkColor() {
        return darkColor;
    }

    @ColorInt
    public int progressColor() {
        return progressColor;
    }

    @ColorInt
    public int expandedColor() {
        return expandedColor;
    }

    public float widgetWidth() {
        return width;
    }

    public float radius() {
        return height;
    }

    public Drawable playDrawable() {
        return playDrawable;
    }

    public Drawable pauseDrawable() {
        return pauseDrawable;
    }

    public Drawable prevDrawable() {
        return prevDrawable;
    }

    public Drawable nextDrawable() {
        return nextDrawable;
    }

    public Drawable playlistDrawable() {
        return playlistDrawable;
    }

    public Drawable albumDrawable() {
        return albumDrawable;
    }

    public PlaybackState playbackState() {
        return playbackState;
    }

    public float crossStrokeWidth() {
        return crossStrokeWidth;
    }

    public float progressStrokeWidth() {
        return progressStrokeWidth;
    }

    public int buttonPadding() {
        return buttonPadding;
    }

    public float shadowRadius() {
        return shadowRadius;
    }

    public float shadowDx() {
        return shadowDx;
    }

    public float shadowDy() {
        return shadowDy;
    }

    public int shadowColor() {
        return shadowColor;
    }

    public float bubblesMinSize() {
        return bubblesMinSize;
    }

    public float bubblesMaxSize() {
        return bubblesMaxSize;
    }

    public int crossColor() {
        return crossColor;
    }

    public int crossOverlappedColor() {
        return crossOverlappedColor;
    }

    public Interpolator accDecInterpolator() {
        return accDecInterpolator;
    }

    public int prevNextExtraPadding() {
        return prevNextExtraPadding;
    }

    @lombok.Builder
    @ToString
    @Setter
    public static class Builder {

        private int lightColor;
        private int darkColor;
        private int progressColor;
        private int expandedColor;
        private float width;
        private float radius;
        private Context context;
        private Random random;
        private Drawable playDrawable;
        private Drawable pauseDrawable;
        private Drawable prevDrawable;
        private Drawable nextDrawable;
        private Drawable playlistDrawable;
        private Drawable albumDrawable;
        private PlaybackState playbackState;
        private int buttonPadding;
        private float crossStrokeWidth;
        private float progressStrokeWidth;
        private float shadowRadius;
        private float shadowDx;
        private float shadowDy;
        private int shadowColor;
        private float bubblesMinSize;
        private float bubblesMaxSize;
        private int crossColor;
        private int crossOverlappedColor;
        private Interpolator accDecInterpolator;
        private int prevNextExtraPadding;

        public Configuration build() {
            return new Configuration(this);
        }
    }
}
