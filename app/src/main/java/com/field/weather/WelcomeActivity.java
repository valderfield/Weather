package com.field.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.IInterface;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 欢迎界面 5秒后自动跳转到主界面 或者跳过 注意主界面要singleTask
 * 模式 否则会重复创建2个activity
 *
 */
public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";
    //init
    //private WebView mWebView;
    private TextView mTimeText;
    private Button mSkipButton;
    private int mRecLen = 5;//五秒跳过
    private Timer mTimer = new Timer();
    private Handler mHandler;
    private Runnable mRunnable;

    //计时器
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_welcom);

        //判断是不是第一次登陆

        //isFirst()
        //init view
        //mWebView = findViewById(R.id.web_view);
        mTimeText = findViewById(R.id.time_text);
        mSkipButton= findViewById(R.id.skip_button);
        //register listener
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    //intent to MainActivity
                    Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                    //取消线程
                    if (mRunnable != null){
                        mHandler.removeCallbacks(mRunnable);
                    }
            }
        });

        /**
         * 不点击跳过
         */
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecLen --;
                        mTimeText.setText("跳过 " +mRecLen);
                        if (mRecLen <0){
                            mTimer.cancel();
                            mTimeText.setVisibility(View.GONE);
                        }
                    }
                });
            }
        },1000,1000);

        mHandler = new Handler();
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {

                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
            }
        },5000);

    }

}
