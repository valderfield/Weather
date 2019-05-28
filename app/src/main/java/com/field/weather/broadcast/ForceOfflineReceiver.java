package com.field.weather.broadcast;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.field.weather.LoginActivity;

//强制下线 广播接收处理逻辑
public class ForceOfflineReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        //弹出dialog 然后 进入login 界面
        Toast.makeText(context, "offline", Toast.LENGTH_SHORT).show();
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage("账号在别处登陆")
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent loginIntent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //退出程序
                    }
                }).create();
        dialog.show();
    }
}
