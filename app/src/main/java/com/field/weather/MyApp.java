package com.field.weather;

import android.app.Application;

import org.litepal.LitePal;

/**
 * 初始化用
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
