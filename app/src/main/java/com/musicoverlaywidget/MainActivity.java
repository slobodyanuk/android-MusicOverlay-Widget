package com.musicoverlaywidget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PlayerWidget widget = WidgetBuilder
                .builder()
                .context(this).build()
                .build();

        widget.show(100, 100);
    }
}
