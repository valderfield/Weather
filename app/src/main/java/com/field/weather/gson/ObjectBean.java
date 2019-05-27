package com.field.weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;
//放回的json对象数据 起名为 objectBean
public class ObjectBean {
    //key 映射
    @SerializedName("HeWeather")
    public List<Weather> weatherList ;
}
