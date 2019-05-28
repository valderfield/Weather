package com.field.weather;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Priority;

//账号注册界面 第一次登陆注册 第二次登陆
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private Button mLoginButton;
    private TextInputEditText mPhoneEditText;
    private TextInputEditText mCodeEditText;
    private Button mCodeButton;
    //模拟固定一个验证码
    public static final String  CODE = "65535";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //init view
        mLoginButton = findViewById(R.id.login_button);
        mPhoneEditText = findViewById(R.id.phone_edit_text);
        mCodeEditText = findViewById(R.id.code_edit_text);
        mCodeButton = findViewById(R.id.code_button);

        //注意 sp参数的 作用域
        SharedPreferences preferences = getSharedPreferences("isFirst",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean isFirst = preferences.getBoolean("is_first",true);

        if (isFirst){
            Toast.makeText(this,"第一次登陆 要注册",Toast.LENGTH_LONG).show();
            editor.putBoolean("is_first",false);
            //editor.commit();//
            editor.apply();
            //startActivity(new Intent(this,MainActivity.class));
            //finish();
        }else {
            Toast.makeText(this, "不是第一次启动", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,WelcomeActivity.class));
            finish();
        }
        //根据输入的验证码对不对 选择登陆注册 并保存在sp里面 缓存
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断验证码是否正确 如果正确就登陆 否则弹出dialog
                String code = mCodeEditText.getText().toString();
                Log.d(TAG, "onClick: " + code);
                //比较字符串 用 equals
                if (code.equals(CODE)){
                    //将手机号 存入 sp中
                    SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
                    sharedPreferences.edit()
                            .putString("user",mPhoneEditText.getText().toString())
                            .apply();

                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();
                }
            }
        });
        mCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建通知 8.0后有 通知渠道
                //失去焦点
                //mCodeEditText.setFocusable(false);

                Notification notification = new NotificationCompat.Builder(LoginActivity.this,"1")
                        .setContentTitle("验证码")
                        .setContentText(CODE)
                        .setShowWhen(true)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .build();
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(1,notification);
            }
        });
        //注册监听器
        mPhoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
