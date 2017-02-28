package com.musicoverlaywidget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

import lombok.Getter;
import lombok.ToString;

@lombok.Builder()
@ToString
@Getter
public class WidgetBuilder {

    private final Context context;

    @ColorInt
    private int darkColor;
    @ColorInt
    private int lightColor;
    @ColorInt
    private int progressColor;
    @ColorInt
    private int crossColor;
    @ColorInt
    private int crossOverlappedColor;
    @ColorInt
    private int shadowColor;
    @ColorInt
    private int expandWidgetColor;

    private int buttonPadding;

    private float crossStrokeWidth;
    private float progressStrokeWidth;
    private float shadowRadius;
    private float shadowDx;
    private float shadowDy;
    private float bubblesMinSize;
    private float bubblesMaxSize;

    private Drawable playDrawable;
    private Drawable prevDrawable;
    private Drawable nextDrawable;
    private Drawable playlistDrawable;
    private Drawable pauseDrawable;

    private boolean darkColorSet;
    private boolean lightColorSet;
    private boolean progressColorSet;
    private boolean crossColorSet;
    private boolean crossOverlappedColorSet;
    private boolean shadowColorSet;
    private boolean expandWidgetColorSet;
    private boolean buttonPaddingSet;
    private boolean crossStrokeWidthSet;
    private boolean progressStrokeWidthSet;
    private boolean shadowRadiusSet;
    private boolean shadowDxSet;
    private boolean shadowDySet;
    private boolean bubblesMinSizeSet;
    private boolean bubblesMaxSizeSet;

    private int edgeOffsetXCollapsed;
    private int edgeOffsetYCollapsed;
    private int edgeOffsetXExpanded;
    private int edgeOffsetYExpanded;

    private boolean edgeOffsetXCollapsedSet;
    private boolean edgeOffsetYCollapsedSet;
    private boolean edgeOffsetXExpandedSet;
    private boolean edgeOffsetYExpandedSet;

//    public WidgetBuilder(@NonNull Context context) {
//        this.context = context;
//    }

    public PlayerWidget build() {
        if (buttonPaddingSet) {
            checkOrThrow(buttonPadding, "Button padding");
        }
        if (shadowRadiusSet) {
            checkOrThrow(shadowRadius, "Shadow radius");
        }
        if (shadowDxSet) {
            checkOrThrow(shadowDx, "Shadow dx");
        }
        if (shadowDySet) {
            checkOrThrow(shadowDy, "Shadow dy");
        }
        if (bubblesMinSizeSet) {
            checkOrThrow(bubblesMinSize, "Bubbles min size");
        }
        if (bubblesMaxSizeSet) {
            checkOrThrow(bubblesMaxSize, "Bubbles max size");
        }
        if (bubblesMinSizeSet && bubblesMaxSizeSet && bubblesMaxSize < bubblesMinSize) {
            throw new IllegalArgumentException("Bubbles max size must be greater than bubbles min size");
        }
        if (crossStrokeWidthSet) {
            checkOrThrow(crossStrokeWidth, "Cross stroke width");
        }
        if (progressStrokeWidthSet) {
            checkOrThrow(progressStrokeWidth, "Progress stroke width");
        }
        return new PlayerWidget(this);
    }

    private void checkOrThrow(int number, String name) {
        if (number < 0)
            throw new IllegalArgumentException(name + " must be equals or greater zero.");
    }

    private void checkOrThrow(float number, String name) {
        if (number < 0)
            throw new IllegalArgumentException(name + " must be equals or greater zero.");
    }

    public WidgetBuilder darkColor(@ColorInt int darkColor) {
        this.darkColor = darkColor;
        darkColorSet = true;
        return this;
    }

    public WidgetBuilder lightColor(@ColorInt int lightColor) {
        this.lightColor = lightColor;
        lightColorSet = true;
        return this;
    }

    public WidgetBuilder progressColor(@ColorInt int progressColor) {
        this.progressColor = progressColor;
        progressColorSet = true;
        return this;
    }

    public WidgetBuilder crossColor(@ColorInt int crossColor) {
        this.crossColor = crossColor;
        crossColorSet = true;
        return this;
    }

    public WidgetBuilder crossOverlappedColor(@ColorInt int crossOverlappedColor) {
        this.crossOverlappedColor = crossOverlappedColor;
        crossOverlappedColorSet = true;
        return this;
    }

    public WidgetBuilder shadowColor(@ColorInt int shadowColor) {
        this.shadowColor = shadowColor;
        shadowColorSet = true;
        return this;
    }

    public WidgetBuilder expandWidgetColor(@ColorInt int expandWidgetColor) {
        this.expandWidgetColor = expandWidgetColor;
        expandWidgetColorSet = true;
        return this;
    }

    public WidgetBuilder buttonPadding(int buttonPadding) {
        this.buttonPadding = buttonPadding;
        buttonPaddingSet = true;
        return this;
    }

    public WidgetBuilder crossStrokeWidth(float crossStrokeWidth) {
        this.crossStrokeWidth = crossStrokeWidth;
        crossStrokeWidthSet = true;
        return this;
    }

    public WidgetBuilder progressStrokeWidth(float progressStrokeWidth) {
        this.progressStrokeWidth = progressStrokeWidth;
        progressStrokeWidthSet = true;
        return this;
    }

    public WidgetBuilder shadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
        shadowRadiusSet = true;
        return this;
    }

    public WidgetBuilder shadowDx(float shadowDx) {
        this.shadowDx = shadowDx;
        shadowDxSet = true;
        return this;
    }

    public WidgetBuilder shadowDy(float shadowDy) {
        this.shadowDy = shadowDy;
        shadowDySet = true;
        return this;
    }

    public WidgetBuilder bubblesMinSize(float bubblesMinSize) {
        this.bubblesMinSize = bubblesMinSize;
        bubblesMinSizeSet = true;
        return this;
    }

    public WidgetBuilder bubblesMaxSize(float bubblesMaxSize) {
        this.bubblesMaxSize = bubblesMaxSize;
        bubblesMaxSizeSet = true;
        return this;
    }

    public WidgetBuilder playDrawable(@android.support.annotation.NonNull Drawable playDrawable) {
        this.playDrawable = playDrawable;
        return this;
    }

    public WidgetBuilder nextTrackDrawable(@android.support.annotation.NonNull Drawable nextDrawable) {
        this.nextDrawable = nextDrawable;
        return this;
    }

    public WidgetBuilder playlistDrawable(@android.support.annotation.NonNull Drawable playlistDrawable) {
        this.playlistDrawable = playlistDrawable;
        return this;
    }

    public WidgetBuilder pauseDrawable(@android.support.annotation.NonNull Drawable pauseDrawable) {
        this.pauseDrawable = pauseDrawable;
        return this;
    }

    public WidgetBuilder edgeOffsetXCollapsed(int edgeOffsetX) {
        this.edgeOffsetXCollapsed = edgeOffsetX;
        edgeOffsetXCollapsedSet = true;
        return this;
    }

    public WidgetBuilder edgeOffsetYCollapsed(int edgeOffsetY) {
        this.edgeOffsetYCollapsed = edgeOffsetY;
        edgeOffsetYCollapsedSet = true;
        return this;
    }

    public WidgetBuilder edgeOffsetYExpanded(int edgeOffsetY) {
        this.edgeOffsetYExpanded = edgeOffsetY;
        edgeOffsetYExpandedSet = true;
        return this;
    }

    public WidgetBuilder edgeOffsetXExpanded(int edgeOffsetX) {
        this.edgeOffsetXExpanded = edgeOffsetX;
        edgeOffsetXExpandedSet = true;
        return this;
    }

}