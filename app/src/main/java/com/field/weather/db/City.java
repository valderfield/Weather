package com.field.weather.db;

import org.litepal.crud.LitePalSupport;

//创建城市表
public class City extends LitePalSupport {
    //字段 key
    private int id;
    //城市名
    private String cityName;
    //城市对应的代号
    private int cityCode;
    //当前城市所属省的id
    private int provinceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
