package com.field.weather.gson;

import java.util.List;

public class Weather {
    public Basic basic;
    public String status;
    public Now now;
    public List<Forecast> daily_forecast;
    public Aqi aqi;
    public Suggestion suggestion;
}
