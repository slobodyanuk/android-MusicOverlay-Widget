package com.musicoverlaywidget.managers;

public interface TouchCallback {

        void onClick(float x, float y);

        void onTouchOutside();

        void onTouched(float x, float y);

        void onMoved(float diffX, float diffY);

        void onReleased(float x, float y);

        void onAnimationCompleted();
    }