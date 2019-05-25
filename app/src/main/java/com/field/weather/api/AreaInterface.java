package com.field.weather.api;

import com.field.weather.gson.CityBean;
import com.field.weather.gson.CountyBean;
import com.field.weather.gson.ProvinceBean;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AreaInterface {
    //请求省的数据 http://guolin.tech/api/china
    //获取省列表
    @GET("china")
    Observable<List<ProvinceBean>> getProvinces();

    //市列表
    @GET("china/{provinceId}")
    Observable<List<CityBean>> getCities(@Path("provinceId")int provinceId);

    //县列表  或者可以这样写
    //@GET({cityid})
    @GET("china/{provinceId}/{cityId}")
    Observable<List<CountyBean>> getCounties(@Path("provinceId")int provinceId, @Path("cityId")int cityId);
}
