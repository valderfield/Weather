package com.field.weather.api;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface WeatherInterface {
    //和风天气注册的weather?cityid="+weatherId &key=ab09814f098240219ac71733297ff0d2
    //@GET("weather")
    //Observable<ResponseBody> getWeather(@Query("cityid") String weatherId,@Query("key") String key);

    //或者map集合
    @GET("weather")
    Observable<ResponseBody> getWeather(@QueryMap Map<String,String> options);

}
