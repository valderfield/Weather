package com.field.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.field.weather.api.BingPicInterface;
import com.field.weather.api.WeatherInterface;
import com.field.weather.gson.Forecast;
import com.field.weather.gson.ObjectBean;
import com.field.weather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/***
 * 1.创建描述接口的api
 * 2.创建gson对应的实体类
 * 3.创建网络请求 re+rx
 * 4.创建布局界面
 * 5.尝试用eventbus
 * 6.将网络请求数据 用sp缓存 然后显示在组件上
 * 7.获取没日bing图 导入glide 包
 * 8.融合状态栏
 *
 *  */
public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";
    //private String mWeatherId;
    public static final String HF_KEY = "ab09814f098240219ac71733297ff0d2";
    //天气类 映射 json

    private ScrollView mWeatherLayout;
    private TextView mTitleCity,mTittleUpdateTime,
    mDegreeText,mWeatherInfoText,mAqiTex,mPm25Text,mComfortText,
    mCarWashText,mSportText;
    private LinearLayout mForecastLayout;

    //背景图片
    private ImageView mBingPicImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //融合状态栏
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        //init view
        mWeatherLayout = findViewById(R.id.weather_layout);
        mTitleCity = findViewById(R.id.title_city);
        mTittleUpdateTime = findViewById(R.id.title_update_time);
        mDegreeText = findViewById(R.id.degree_text);
        mWeatherInfoText = findViewById(R.id.weather_info_text);
        mAqiTex = findViewById(R.id.aqi_text);
        //mPm25Text = findViewById(R.id.)
        mComfortText = findViewById(R.id.comfort_text);
        mCarWashText = findViewById(R.id.car_wash_text);
        mSportText = findViewById(R.id.sport_text);

        mForecastLayout = findViewById(R.id.forecast_layout);

        //bing
        mBingPicImage = findViewById(R.id.bing_pic_img);
        //先读取本地 如果没有就 网络请求然后回调
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherResponse = preferences.getString("weather",null);
        if (weatherResponse != null){
            //获取weather
            //{}表示对象 要取名字 如果数组有名字 那么返回的list集合的名字也要与之对应
            ObjectBean objectBean = new Gson().fromJson(weatherResponse, ObjectBean.class);
            Weather weather = objectBean.weatherList.get(0);
            showWeather(weather);
        }else {
            String weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }

        //更新背景 先读缓存 没有再获取网络请求 然后存入sp 最后回调更新
        String bingPic = preferences.getString("bingPic", null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(mBingPicImage);
        }else {
            loadBingPic();
        }
    }
    //网络请求bingPic
    private void loadBingPic() {
        //http://guolin.tech/api/bing_pic
        String bingUrl = "http://guolin.tech/api/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(bingUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        BingPicInterface bingPicInterface = retrofit.create(BingPicInterface.class);
        bingPicInterface.getBingPic()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        //返回的网路数据保存到sp
                        try {
                            String bingPicResponse = responseBody.string();
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor edit = preferences.edit();
                            edit.putString("bingPic",bingPicResponse);
                            edit.apply();
                            if (bingPicResponse != null){
                                Glide.with(WeatherActivity.this).load(bingPicResponse).into(mBingPicImage);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }
    //根据weatherid 获取天气

    private void requestWeather(String weatherId){
        //用集合的方式
        Map<String,String> options = new HashMap<>();
        options.put("cityid",weatherId);
        options.put("key",HF_KEY);

        String url = "http://guolin.tech/api/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        WeatherInterface weatherInterface = retrofit.create(WeatherInterface.class);
        Observable<ResponseBody> weather = weatherInterface.getWeather(options);
        weather.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    //主线程
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            //获取weather
                            String weatherResponse = responseBody.string();
                            //{}表示对象 要取名字 如果数组有名字 那么返回的list集合的名字也要与之对应
                            ObjectBean objectBean = new Gson().fromJson(weatherResponse, ObjectBean.class);
                            Weather weather = objectBean.weatherList.get(0);

                            //存入数据库
                            if (weather.status.equals("ok") && weather != null){
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("weather",weatherResponse);
                                editor.apply();

                                //更新UI
                                showWeather(weather);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    //根据weather 类 显示到界面上
    private void showWeather(Weather weather) {
        mTitleCity.setText(weather.basic.location);
        mTittleUpdateTime.setText(weather.basic.update.loc);
        mDegreeText.setText(weather.now.tmp);
        mWeatherInfoText.setText(weather.now.cond_txt);

        mForecastLayout.removeAllViews();
        for (Forecast forecast :weather.daily_forecast) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,mForecastLayout,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond.txt_d);
            maxText.setText(forecast.tmp.max);
            minText.setText(forecast.tmp.min);
            mForecastLayout.addView(view);
        }

        mAqiTex.setText(weather.aqi.city.aqi);

        mComfortText.setText(weather.suggestion.comf.txt);
        mSportText.setText(weather.suggestion.sport.txt);
        mCarWashText.setText(weather.suggestion.cw.txt);
    }

    //json
    private void toGSON(ResponseBody responseBody){
        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            JSONArray heWeather = jsonObject.getJSONArray("HeWeather");
            String weather = heWeather.getJSONObject(0).toString();
            Weather weather1 = new Gson().fromJson(weather, Weather.class);
            Log.d(TAG, "onNext: " + weather1.now.cond_txt);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
