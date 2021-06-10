package com.example.ycblesdkdemo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.ycblesdkdemo.model.ConnectEvent;
import com.yucheng.ycbtsdk.Response.BleConnectResponse;
import com.yucheng.ycbtsdk.YCBTClient;

import org.greenrobot.eventbus.EventBus;

public class MyApplication extends Application {

    private static MyApplication instance = null;




    public static MyApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        String currentProcessName = getCurProcessName(this);

        if ("com.example.ycblesdkdemo".equals(currentProcessName)) {

            Log.e("device","...onCreate.....");

            instance = this;
            YCBTClient.initClient(this,true);
            YCBTClient.registerBleStateChange(bleConnectResponse);


        }
    }


    BleConnectResponse bleConnectResponse = new BleConnectResponse() {
        @Override
        public void onConnectResponse(int var1) {

            //Toast.makeText(MyApplication.this, "i222=" + var1, Toast.LENGTH_SHORT).show();

            Log.e("device","...connect..state....." + var1);

            if(var1 == 0){
                EventBus.getDefault().post(new ConnectEvent());
            }
        }
    };

    /**
     * 获得当前进程名
     *
     * @param context
     * @return
     */
    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }






}
