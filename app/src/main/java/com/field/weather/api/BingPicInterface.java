package com.field.weather.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

public interface BingPicInterface {
    //请求bing图片 接口: http://guolin.tech/api/bing_pic
    @GET("bing_pic")
    Observable<ResponseBody> getBingPic();
}
