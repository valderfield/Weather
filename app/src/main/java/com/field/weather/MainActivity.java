package com.field.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * 1.创建数据库 litepal 要初始化数据库
 * 2.gson 实体类 ProvinceBean CityBean CountyBean
 * 3.retrofit 导入依赖
 * 4.rxjava 导入依赖
 * 5.listview
 * 6.修改没有actionbar  目前问题 无线请求 但是不显示数据
 *
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //用于判断缓存 如果有 就直接显示本地
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weather = preferences.getString("weather", null);
        if (weather != null){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
