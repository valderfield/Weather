package com.field.weather.gson;

import java.util.List;
//返回数组中 单个天气情况
public class Weather {
    public Basic basic;
    public String status;
    public Now now;
    public List<Forecast> daily_forecast;
    public Aqi aqi;
    public Suggestion suggestion;
}
