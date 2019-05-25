package com.field.weather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * 1.创建数据库 litepal 要初始化数据库
 * 2.gson 实体类 ProvinceBean CityBean CountyBean
 * 3.retrofit 导入依赖
 * 4.rxjava 导入依赖
 * 5.listview
 * 6.修改没有actionbar  目前问题 无线请求 但是不显示数据
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
