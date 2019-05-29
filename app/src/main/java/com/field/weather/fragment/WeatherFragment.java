package com.field.weather.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.field.weather.R;
import com.field.weather.WeatherActivity;
import com.field.weather.api.BingPicInterface;
import com.field.weather.api.WeatherInterface;
import com.field.weather.db.County;
import com.field.weather.gson.Forecast;
import com.field.weather.gson.ObjectBean;
import com.field.weather.gson.Weather;
import com.field.weather.service.AutoUpdateService;
import com.google.gson.Gson;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

/**
 * A simple {@link Fragment} subclass.
 * 显示具体天气信息界面 fragment
 */
public class WeatherFragment extends Fragment {
    List<String> questionList = new ArrayList<>();

    private static final String TAG = "WeatherFragment";
    //api key
    public static final String HF_KEY = "ab09814f098240219ac71733297ff0d2";
    //init
    private ScrollView mWeatherLayout;
    private TextView mTitleCity,mTittleUpdateTime,
            mDegreeText,mWeatherInfoText,mAqiTex,mPm25Text,mComfortText,
            mCarWashText,mSportText;
    private LinearLayout mForecastLayout;
    //背景图片
    private ImageView mBingPicImage;
    //下拉刷新
    public SwipeRefreshLayout mSwipeRefresh;
    //刷新需要全局的 天气id;
    private String mWeatherId;
    //侧滑
    //public DrawerLayout mDrawerLayout;
    //搜索框
    private SearchView mSearchView;

    public WeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        //init view
        mWeatherLayout = view.findViewById(R.id.weather_layout);
        mTitleCity = view.findViewById(R.id.title_city);
        mTittleUpdateTime = view.findViewById(R.id.title_update_time);
        mDegreeText = view.findViewById(R.id.degree_text);
        mWeatherInfoText = view.findViewById(R.id.weather_info_text);
        mAqiTex = view.findViewById(R.id.aqi_text);
        mComfortText = view.findViewById(R.id.comfort_text);
        mCarWashText = view.findViewById(R.id.car_wash_text);
        mSportText = view.findViewById(R.id.sport_text);
        mForecastLayout = view.findViewById(R.id.forecast_layout);
        mBingPicImage = view.findViewById(R.id.bing_pic_img);
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh);
        //mDrawerLayout = view.findViewById(R.id.drawer_layout);
        mSearchView = view.findViewById(R.id.area_search_view);
        //设置searchview 属性

        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        return view;
    }
    //此处处理逻辑
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //先读取本地 如果没有就 网络请求然后回调
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String weatherResponse = preferences.getString("weather",null);
        if (weatherResponse != null){
            //获取weather
            //{}表示对象 要取名字 如果数组有名字 那么返回的list集合的名字也要与之对应
            ObjectBean objectBean = new Gson().fromJson(weatherResponse, ObjectBean.class);
            Weather weather = objectBean.weatherList.get(0);
            //获取weatherid 用于更新
            mWeatherId = weather.basic.id;
            showWeather(weather);
        }else {
            //String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherId = getActivity().getIntent().getStringExtra("weather_id");
            //requestWeather(weatherId);
            requestWeather(mWeatherId);
        }
        //更新背景 先读缓存 没有再获取网络请求 然后存入sp 最后回调更新
        String bingPic = preferences.getString("bingPic", null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(mBingPicImage);
        }else {
            loadBingPic();
        }
        //下拉刷新注册监听器
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        //搜索框设置监听器
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //当点击搜索提交时
                //先获取所有的城市名字存入string数组
                //然后判断 s是否包含在 string数组中


                LitePal.where("countyName=?",s).findFirstAsync(County.class)
                        .listen(new FindCallback<County>() {
                            @Override
                            public void onFinish(County county) {
                                requestWeather(county.getWeatherId());

                            }
                        });

                //查询完毕后 清除搜索内容
                mSearchView.setQuery("",false);
                //查询完毕默认失去焦点
                mSearchView.setFocusable(false);
                return false;
            }
            //当搜索内容更新时
            @Override
            public boolean onQueryTextChange(String s) {
                //只需要将获取的字符串 传入条件 查询的必然是想要的地区
                //List<County> counties = LitePal.where("countyName=?", s).find(County.class);
                //因为知道列表中每个地区是唯一的 所以查询结果只需要一个就行
                //County countySingle = LitePal.where("countyName=?", s).findFirst(County.class);
//                for (County county :counties) {
////                    Log.d(TAG, "onQueryTextChange: " + county.getCountyName() +":" + county.getWeatherId());
////                    requestWeather(county.getWeatherId());
////                }
                //查询数据更新是个异步任务

                return false;
            }
        });
    }

    //网络请求bingPic
    public void loadBingPic() {
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
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            SharedPreferences.Editor edit = preferences.edit();
                            edit.putString("bingPic",bingPicResponse);
                            edit.apply();
                            if (bingPicResponse != null){
                                Glide.with(WeatherFragment.this).load(bingPicResponse).into(mBingPicImage);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        mSwipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onComplete() {
                        mSwipeRefresh.setRefreshing(false);
                    }
                });


    }
    //根据weatherid 获取天气
    public void requestWeather(final String weatherId){
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
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("weather",weatherResponse);
                                editor.apply();
                                //获取天气id
                                mWeatherId = weather.basic.id;
                                //更新UI
                                showWeather(weather);
                            }
                            //关闭刷新
                            mSwipeRefresh.setRefreshing(false);
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
            View view = LayoutInflater.from(getContext()).inflate(R.layout.forecast_item,mForecastLayout,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond.txt_d);
            maxText.setText(forecast.tmp.max);
            minText.setText(forecast.tmp.min);
            mForecastLayout.addView(view);
            //开启后台更新
            getActivity().startService(new Intent(getContext(), AutoUpdateService.class));
        }
        mAqiTex.setText(weather.aqi.city.aqi);
        mComfortText.setText(weather.suggestion.comf.txt);
        mSportText.setText(weather.suggestion.sport.txt);
        mCarWashText.setText(weather.suggestion.cw.txt);
    }
}
