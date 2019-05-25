package com.field.weather.db;

import org.litepal.crud.LitePalSupport;

//创建县/区表
public class County extends LitePalSupport {
    //字段
    private int id;
    //县区名
    private String countyName;
    //县区对应的天气id
    private String weatherId;
    //县区所属的城市id
    private int cityId;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
