package com.field.weather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.field.weather.WeatherActivity;
import com.field.weather.api.BingPicInterface;
import com.field.weather.api.WeatherInterface;
import com.field.weather.gson.ObjectBean;
import com.field.weather.gson.Weather;
import com.google.gson.Gson;

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
import static com.field.weather.fragment.WeatherFragment.HF_KEY;

/**
 * 后台自动更新天气和bing图片
 */
public class AutoUpdateService extends Service {

    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //更新天气 更新bing
        updateWeather();
        updateBingPic();
        //创建定时器
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,i,0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }
    //更新天气
    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        ObjectBean objectBean = new Gson().fromJson(weatherString, ObjectBean.class);
        Weather weather1 = objectBean.weatherList.get(0);
        Map<String,String> options = new HashMap<>();
        options.put("cityid",weather1.basic.id);
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
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("weather",weatherResponse);
                                editor.apply();

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
    //更新bing
    private void updateBingPic() {
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
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                            SharedPreferences.Editor edit = preferences.edit();
                            edit.putString("bingPic",bingPicResponse);
                            edit.apply();

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
}
