package com.lubecki.crowddj.application;

import android.app.Application;

public final class DJApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.setDefaultFont(this, "DEFAULT", "DIN Condensed Bold.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "DIN Condensed Bold.ttf");
        FontsOverride.setDefaultFont(this, "SERIF", "DIN Condensed Bold.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "DIN Condensed Bold.ttf");
    }
}