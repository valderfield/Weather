package com.field.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.field.weather.api.BingPicInterface;
import com.field.weather.api.WeatherInterface;
import com.field.weather.fragment.AreaFragment;
import com.field.weather.fragment.MyselfFragment;
import com.field.weather.fragment.WeatherFragment;
import com.field.weather.gson.Forecast;
import com.field.weather.gson.ObjectBean;
import com.field.weather.gson.Weather;
import com.field.weather.service.AutoUpdateService;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

/***
 * 1.创建描述接口的api
 * 2.创建gson对应的实体类
 * 3.创建网络请求 re+rx
 * 4.创建布局界面
 * 5.尝试用eventbus
 * 6.将网络请求数据 用sp缓存 然后显示在组件上
 * 7.获取没日bing图 导入glide 包
 * 8.融合状态栏
 * 9.手动刷新 下拉刷新
 * 10.切换天气
 * 11.后台自动更新天气 和 图片
 *
 *  */
public class WeatherActivity extends AppCompatActivity {
//    //log
//    private static final String TAG = "WeatherActivity";
//    //api key
//    public static final String HF_KEY = "ab09814f098240219ac71733297ff0d2";
//    //init
//    private ScrollView mWeatherLayout;
//    private TextView mTitleCity,mTittleUpdateTime,
//    mDegreeText,mWeatherInfoText,mAqiTex,mPm25Text,mComfortText,
//    mCarWashText,mSportText;
//    private LinearLayout mForecastLayout;
//    //背景图片
//    private ImageView mBingPicImage;
//    //下拉刷新
//    public SwipeRefreshLayout mSwipeRefresh;
//    //刷新需要全局的 天气id;
//    private String mWeatherId;
//    //侧滑
//    public DrawerLayout mDrawerLayout;

    /**
     *
     * 修改后 使用 viewpager
     */
    public ViewPager mFragmentViewPager;
    public BottomNavigationView mBottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //融合状态栏
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_weather);

//        //init view
//        mWeatherLayout = findViewById(R.id.weather_layout);
//        mTitleCity = findViewById(R.id.title_city);
//        mTittleUpdateTime = findViewById(R.id.title_update_time);
//        mDegreeText = findViewById(R.id.degree_text);
//        mWeatherInfoText = findViewById(R.id.weather_info_text);
//        mAqiTex = findViewById(R.id.aqi_text);
//        mComfortText = findViewById(R.id.comfort_text);
//        mCarWashText = findViewById(R.id.car_wash_text);
//        mSportText = findViewById(R.id.sport_text);
//        mForecastLayout = findViewById(R.id.forecast_layout);
//        mBingPicImage = findViewById(R.id.bing_pic_img);
//        mSwipeRefresh = findViewById(R.id.swipe_refresh);
//        mDrawerLayout = findViewById(R.id.drawer_layout);
//        //先读取本地 如果没有就 网络请求然后回调
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        String weatherResponse = preferences.getString("weather",null);
//        if (weatherResponse != null){
//            //获取weather
//            //{}表示对象 要取名字 如果数组有名字 那么返回的list集合的名字也要与之对应
//            ObjectBean objectBean = new Gson().fromJson(weatherResponse, ObjectBean.class);
//            Weather weather = objectBean.weatherList.get(0);
//            //获取weatherid 用于更新
//            mWeatherId = weather.basic.id;
//            showWeather(weather);
//        }else {
//            //String weatherId = getIntent().getStringExtra("weather_id");
//            mWeatherId = getIntent().getStringExtra("weather_id");
//            //requestWeather(weatherId);
//            requestWeather(mWeatherId);
//        }
//        //更新背景 先读缓存 没有再获取网络请求 然后存入sp 最后回调更新
//        String bingPic = preferences.getString("bingPic", null);
//        if (bingPic != null){
//            Glide.with(this).load(bingPic).into(mBingPicImage);
//        }else {
//            loadBingPic();
//        }
//        //下拉刷新注册监听器
//        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                requestWeather(mWeatherId);
//            }
//        });

        //修该后代码
        mFragmentViewPager = findViewById(R.id.fragment_view_pager);
        mBottomNav = findViewById(R.id.bottom_nav);
        final List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new WeatherFragment());
        fragmentList.add(new AreaFragment());
        fragmentList.add(new MyselfFragment());
        FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragmentList.get(i);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        };
        mFragmentViewPager.setAdapter(pagerAdapter);

        mFragmentViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mBottomNav.getMenu().getItem(i).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_home:
                        mFragmentViewPager.setCurrentItem(0);
                        break;
                    case R.id.nav_area:
                        mFragmentViewPager.setCurrentItem(1);
                        break;
                    case R.id.nav_myself:
                        mFragmentViewPager.setCurrentItem(2);
                        break;
                        default:
                            break;
                }
                return false;
            }
        });

    }
//    //网络请求bingPic
//    private void loadBingPic() {
//        //http://guolin.tech/api/bing_pic
//        String bingUrl = "http://guolin.tech/api/";
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(bingUrl)
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .build();
//        BingPicInterface bingPicInterface = retrofit.create(BingPicInterface.class);
//        bingPicInterface.getBingPic()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<ResponseBody>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//                        //返回的网路数据保存到sp
//                        try {
//                            String bingPicResponse = responseBody.string();
//                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
//                            SharedPreferences.Editor edit = preferences.edit();
//                            edit.putString("bingPic",bingPicResponse);
//                            edit.apply();
//                            if (bingPicResponse != null){
//                                Glide.with(WeatherActivity.this).load(bingPicResponse).into(mBingPicImage);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        mSwipeRefresh.setRefreshing(false);
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        mSwipeRefresh.setRefreshing(false);
//                    }
//                });
//
//
//    }
//    //根据weatherid 获取天气
//    public void requestWeather(final String weatherId){
//        //用集合的方式
//        Map<String,String> options = new HashMap<>();
//        options.put("cityid",weatherId);
//        options.put("key",HF_KEY);
//
//        String url = "http://guolin.tech/api/";
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(url)
//                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .build();
//        WeatherInterface weatherInterface = retrofit.create(WeatherInterface.class);
//        Observable<ResponseBody> weather = weatherInterface.getWeather(options);
//        weather.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<ResponseBody>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//                    //主线程
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//                        try {
//                            //获取weather
//                            String weatherResponse = responseBody.string();
//                            //{}表示对象 要取名字 如果数组有名字 那么返回的list集合的名字也要与之对应
//                            ObjectBean objectBean = new Gson().fromJson(weatherResponse, ObjectBean.class);
//                            Weather weather = objectBean.weatherList.get(0);
//
//                            //存入数据库
//                            if (weather.status.equals("ok") && weather != null){
//                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
//                                SharedPreferences.Editor editor = preferences.edit();
//                                editor.putString("weather",weatherResponse);
//                                editor.apply();
//                                //获取天气id
//                            mWeatherId = weather.basic.id;
//                                //更新UI
//                                showWeather(weather);
//                            }
//                            //关闭刷新
//                            mSwipeRefresh.setRefreshing(false);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }
//    //根据weather 类 显示到界面上
//    private void showWeather(Weather weather) {
//        mTitleCity.setText(weather.basic.location);
//        mTittleUpdateTime.setText(weather.basic.update.loc);
//        mDegreeText.setText(weather.now.tmp);
//        mWeatherInfoText.setText(weather.now.cond_txt);
//
//        mForecastLayout.removeAllViews();
//        for (Forecast forecast :weather.daily_forecast) {
//            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,mForecastLayout,false);
//            TextView dateText = view.findViewById(R.id.date_text);
//            TextView infoText = view.findViewById(R.id.info_text);
//            TextView maxText = view.findViewById(R.id.max_text);
//            TextView minText = view.findViewById(R.id.min_text);
//            dateText.setText(forecast.date);
//            infoText.setText(forecast.cond.txt_d);
//            maxText.setText(forecast.tmp.max);
//            minText.setText(forecast.tmp.min);
//            mForecastLayout.addView(view);
//            //开启后台更新
//            startService(new Intent(this, AutoUpdateService.class));
//        }
//        mAqiTex.setText(weather.aqi.city.aqi);
//        mComfortText.setText(weather.suggestion.comf.txt);
//        mSportText.setText(weather.suggestion.sport.txt);
//        mCarWashText.setText(weather.suggestion.cw.txt);
//    }
}
