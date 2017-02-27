package com.musicoverlaywidget.controllers;

import android.support.annotation.NonNull;

public interface OnWidgetStateChangedListener {

        void onWidgetStateChanged(@NonNull State state);

        void onWidgetPositionChanged(int cx, int cy);
    }