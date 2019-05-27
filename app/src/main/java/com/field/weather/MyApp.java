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
        //初始化数据库
        LitePal.initialize(this);
    }
}
