package com.field.weather.gson;

/**
 * 空气指数
 */
public class Aqi {
    public City city;
    public class City{
        public String aqi;
        public String pm25;
        //空气质量
        public String qlty;
    }
}
