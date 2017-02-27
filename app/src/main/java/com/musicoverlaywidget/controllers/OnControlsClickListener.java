package com.musicoverlaywidget.controllers;

public interface OnControlsClickListener {

    boolean onPlaylistClicked();

    void onPreviousClicked();

    boolean onPlayPauseClicked();

    void onNextClicked();

}