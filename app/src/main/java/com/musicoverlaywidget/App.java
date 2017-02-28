package com.musicoverlaywidget;

import android.app.Application;

import com.thefinestartist.Base;

/**
 * Created by Serhii Slobodianiuk on 28.02.2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Base.initialize(getApplicationContext());
    }
}
