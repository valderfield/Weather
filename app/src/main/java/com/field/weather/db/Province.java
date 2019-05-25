package com.field.weather.db;


import org.litepal.crud.LitePalSupport;

//创建表对应的实体数据 province 继承litepalSupport 为了save
public class Province extends LitePalSupport {
    //key field
    private int id;
    //省名

    private String provinceName;
    //省对应的代号
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
