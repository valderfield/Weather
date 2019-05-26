package com.field.weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ObjectBean {
    @SerializedName("HeWeather")
    public List<Weather> weatherList ;
}
