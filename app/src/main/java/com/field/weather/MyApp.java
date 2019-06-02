package com.field.weather;

import android.Manifest;
import android.app.Application;
import android.os.Build;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

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
