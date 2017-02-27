package com.musicoverlaywidget.controllers;

import android.support.annotation.Nullable;

public interface Controller {

    void start();

    void pause();

    void stop();

    int duration();

    void duration(int duration);

    int position();

    void position(int position);

    void onControlsClickListener(@Nullable OnControlsClickListener onControlsClickListener);

    void onWidgetStateChangedListener(@Nullable OnWidgetStateChangedListener onWidgetStateChangedListener);

}