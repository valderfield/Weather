package com.field.weather.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.field.weather.R;
import com.field.weather.api.AreaInterface;
import com.field.weather.db.City;
import com.field.weather.db.County;
import com.field.weather.db.Province;
import com.field.weather.gson.CityBean;
import com.field.weather.gson.CountyBean;
import com.field.weather.gson.ProvinceBean;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AreaFragment extends Fragment {
    //
    //log
    private static final String TAG = "AreaFragment";
    //常量
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    //init
    private Button mBackButton;
    private TextView mShowCurrentText;

    //view 数据源 适配器
    private ListView mAreaListView;
    private List<String> mData = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    //省市县数据表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> counties;
    //当前选中的省市
    private Province selectedProvince;
    private City selectedCity;
    //当前选中的等级
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_area,container,false);
        mAreaListView = view.findViewById(R.id.area_list_view);
        mBackButton = view.findViewById(R.id.back_button);
        mShowCurrentText = view.findViewById(R.id.show_current_area_text_view);

        mAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,mData);
        mAreaListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAreaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    //根据position 获取当前选中的 province
                    selectedProvince = provinceList.get(position);
                    //显示城市数据
                    showCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    //显示县列表  第三次刷新有问题
                    showCounties();
                }
            }
        });
        //backbutton 返回监听
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    showCities();
                }else if (currentLevel == LEVEL_CITY){
                    showProvinces();
                }
            }
        });
        //默认显示 省列表
        showProvinces();
    }

    //initdata() 显示省列表
    private void showProvinces(){
        mBackButton.setVisibility(View.GONE);
        mShowCurrentText.setText("中国");
        provinceList = LitePal.findAll(Province.class);
        //判读 如果长度为0 就网络请求 否则就获取本地数据
        if (provinceList.size() > 0){
            //清空 数据源
            mData.clear();
            //遍历
            for (Province province :provinceList) {
                //存入list 数据源
                mData.add(province.getProvinceName());
            }
            //通知listview 更新
            mAdapter.notifyDataSetChanged();
            mAreaListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            //网络请求数据
            queryProvinces();
        }
    }
    //显示市列表
    private void showCities(){
        mBackButton.setVisibility(View.VISIBLE);
        mShowCurrentText.setText(selectedProvince.getProvinceName());
        //查询数据库 获取市表 id 是 getCode 不是 字段 大小写忽略
        cityList = LitePal.where("provinceId=?", String.valueOf(selectedProvince.getProvinceCode()))
                .find(City.class);
        if (cityList.size() > 0){
            mData.clear();
            for (City city :cityList) {
                mData.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mAreaListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            queryCities(selectedProvince.getProvinceCode());
        }
    }

    //show Counties 不显示县? 要么存失败 要么代码写错了 为什么无线存储
    private void showCounties(){
        mShowCurrentText.setText(selectedCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        counties = LitePal.where("cityId=?",String.valueOf(selectedCity.getCityCode()))
                .find(County.class);
        if (counties.size() > 0){
            mData.clear();
            for (County county :counties) {
                mData.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mAreaListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            queryCounties(selectedProvince.getProvinceCode(),selectedCity.getCityCode());
        }
    }
    //rxjava + retrofit 网络请求 查询省数据
    private void queryProvinces(){
        //url
        String baseUrl = "http://guolin.tech/api/";
        //创建retrofit 对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        //获取网络请求接口对象
        AreaInterface areaInterface = retrofit.create(AreaInterface.class);
        //获取被观察者 调度线程  与 观察者 订阅关系
        final Observable<List<ProvinceBean>> observable = areaInterface.getProvinces();
        observable.subscribeOn(Schedulers.io())

                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                //绑定观察者
                .subscribe(new Observer<List<ProvinceBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //Log.d(TAG, "onSubscribe: ");
                        Log.d(TAG, "onSubscribe: 县数据开始.....");
                    }

                    @Override
                    public void onNext(List<ProvinceBean> provinceBeans) {
                        //接收 被观察者 发送过来的数据
                        for (ProvinceBean provinceBean : provinceBeans) {
                            Log.d(TAG, "onNext: " + provinceBean.getName());
                            //遍历后 存入litepal 数据库
                            Province province = new Province();
                            province.setProvinceName(provinceBean.getName());
                            province.setProvinceCode(provinceBean.getId());
                            province.save();
                        }
                        //存储数完毕后 回调show方法显示省列表
                        showProvinces();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    //查询市数据
    private void queryCities(final int provinceId){
        //url
        String baseUrl = "http://guolin.tech/api/";
        //创建retrofit 对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        //获取网络请求接口对象
        AreaInterface areaInterface = retrofit.create(AreaInterface.class);
        //请求有路径的数据
        final Observable<List<CityBean>> observable = areaInterface.getCities(provinceId);
        observable.subscribeOn(Schedulers.io())
                //observable.subscribeOn(Schedulers.newThread())
                //.unsubscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CityBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: ");
                    }
                    @Override
                    public void onNext(List<CityBean> cityBeans) {
                        for (CityBean cityBean : cityBeans) {
                            Log.d(TAG, "城市: " + cityBean.getName());
                            //存入city表
                            City city = new City();
                            city.setCityName(cityBean.getName());
                            city.setCityCode(cityBean.getId());
                            city.setProvinceId(provinceId);
                            city.save();
                        }
                        //回调 显示市列表
                        showCities();

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    //查询县数据
    private void queryCounties(int provinceId, final int cityId){
        //url
        String baseUrl = "http://guolin.tech/api/";
        //创建retrofit 对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        //获取网络请求接口对象
        AreaInterface areaInterface = retrofit.create(AreaInterface.class);
        //请求有路径的数据
        final Observable<List<CountyBean>> observable = areaInterface.getCounties(provinceId, cityId);
        areaInterface.getCounties(provinceId, cityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CountyBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<CountyBean> countyBeans) {

                        for (CountyBean countyBean : countyBeans) {
                            Log.d(TAG, "县: " + countyBean.getName() +" ---" + countyBean.getWeather_id());

                            County county = new County();
                            county.setCountyName(countyBean.getName());
                            county.setWeatherId(countyBean.getWeather_id());
                            county.setCityId(cityId);
                            county.save();
                        }
                        //回调显示县列表
                        showCounties();
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
