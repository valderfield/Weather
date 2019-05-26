package com.field.weather.gson;

/**
 * 天气预报
 */
public class Forecast {
    public String date;
    public Cond cond;
    public class Cond{
        public String txt_d;
    }
    public Tmp tmp;
    public class Tmp{
        public String max;
        public String min;
    }
}
