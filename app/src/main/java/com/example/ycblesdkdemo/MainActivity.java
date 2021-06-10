package com.example.ycblesdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ycblesdkdemo.adapter.DeviceAdapter;
import com.example.ycblesdkdemo.dbmodels.History;
import com.example.ycblesdkdemo.model.ConnectEvent;
import com.yucheng.ycbtsdk.AITools;
import com.yucheng.ycbtsdk.Bean.ScanDeviceBean;
import com.yucheng.ycbtsdk.Constants;
import com.yucheng.ycbtsdk.Response.BleConnectResponse;
import com.yucheng.ycbtsdk.Response.BleDataResponse;
import com.yucheng.ycbtsdk.Response.BleDeviceToAppDataResponse;
import com.yucheng.ycbtsdk.Response.BleRealDataResponse;
import com.yucheng.ycbtsdk.Response.BleScanResponse;
import com.yucheng.ycbtsdk.YCBTClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String DEVICE_BATTERY_VALUE = null;
    private String DBP_VALUE = null;
    private String SBP_VALUE = null;
    private String HEART_VALUE = null;
    private ArrayList arrayList = new ArrayList();
    private ListView listView;
    private Button start_ecg;
    private TextView scan_status;
    private TextView device_name;
    private TextView device_mac;
    private TextView device_battery;
    private TextView DBP_tv;
    private TextView SBP_tv;
    private TextView heart_tv;
    private TextView preasure_tv;
    private TextView aha_tv;
    private WebView webView;
    LottieAnimationView scan_anim;
    LottieAnimationView ecg_anim;
    RelativeLayout con_device_rl;
    private Handler handler = new Handler();
    Handler handlerEcg = new Handler();

    private List<ScanDeviceBean> listModel = new ArrayList<>();
    private List<String> listVal = new ArrayList<>();
    DeviceAdapter deviceAdapter = new DeviceAdapter(MainActivity.this, listModel);

    private AITools aiTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        //z ligth Sleep time
        //zzz deep sleep time
        
        startService(new Intent(this, MyBleService.class));

        listView = findViewById(R.id.device_list_view);
        listView.setAdapter(deviceAdapter);
        scan_status = findViewById(R.id.scan_title_tv);
        scan_anim = findViewById(R.id.scan_anim);
        ecg_anim = findViewById(R.id.ecg_anim_la);
        con_device_rl = findViewById(R.id.con_device_rl);
        device_battery = findViewById(R.id.device_battery_tv);
        device_name = findViewById(R.id.item_name_view);
        device_mac = findViewById(R.id.item_mac_view);
        DBP_tv = findViewById(R.id.DBP_tv);
        SBP_tv = findViewById(R.id.SBP_tv);
        heart_tv = findViewById(R.id.heart_tv);
        preasure_tv = findViewById(R.id.preasure_status_tv);
        aha_tv = findViewById(R.id.aha_tv);
        webView = findViewById(R.id.metabase_wv);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("http://pic-metabase2.sa-east-1.elasticbeanstalk.com/");
        findViewById(R.id.bt_start_scan).setOnClickListener(this);
        findViewById(R.id.bt_stop_scan).setOnClickListener(this);
        findViewById(R.id.bt_connect_dev).setOnClickListener(this);
        start_ecg = findViewById(R.id.bt_write_test);
        start_ecg.setOnClickListener(this);
        findViewById(R.id.bt_write_real).setOnClickListener(this);
        findViewById(R.id.bt_ui_getinfo).setOnClickListener(this);
        findViewById(R.id.bt_get_history_data).setOnClickListener(this);
        findViewById(R.id.bt_delete_history_data).setOnClickListener(this);
        findViewById(R.id.open_real_temp).setOnClickListener(this);
        findViewById(R.id.read_real_temp).setOnClickListener(this);
        findViewById(R.id.close_real_temp).setOnClickListener(this);
        findViewById(R.id.bt_disconnect_dev).setOnClickListener(this);
        findViewById(R.id.send_message).setOnClickListener(this);
        findViewById(R.id.send_model).setOnClickListener(this);
        findViewById(R.id.get_scheduleInfo).setOnClickListener(this);
        findViewById(R.id.add_scheduleInfo).setOnClickListener(this);



//        String defaultMac = (String) SPHelper.get(MainActivity.this, "key", "no");
//
//        if (!defaultMac.equals("no")) {
//
//            Log.e("device", "default=" + defaultMac);
//
//
//            YCBTClient.connectBle(defaultMac, new BleConnectResponse() {
//                @Override
//                public void onConnectResponse(final int i) {
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            Toast.makeText(MainActivity.this, "main=" + i, Toast.LENGTH_SHORT).show();
//                            baseOrderSet();
//                        }
//                    });
//                }
//            });
//        }
        sendDataToServer();
        startScan();

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
        start_ecg.setAnimation(animation);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                YCBTClient.stopScanBle();

                scan_status.setText("...Conectando");

                ScanDeviceBean scanDeviceBean = (ScanDeviceBean) parent.getItemAtPosition(position);

                SPHelper.setParam(MainActivity.this, "key", scanDeviceBean.getDeviceMac());

                YCBTClient.connectBle(scanDeviceBean.getDeviceMac(), new BleConnectResponse() {
                    @Override
                    public void onConnectResponse(final int i) {

                        if (i == Constants.CODE.Code_OK){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (i == 10)
                                        baseOrderSet();
                                    //Toast.makeText(MainActivity.this, "i=" + i, Toast.LENGTH_SHORT).show();
                                    scan_status.setText("Conectado");
                                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
                                    scan_anim.setAnimation(animation);
                                    listView.setVisibility(view.GONE);
                                    con_device_rl.setVisibility(view.VISIBLE);
                                    device_name.setText(scanDeviceBean.getDeviceName());
                                    device_mac.setText(scanDeviceBean.getDeviceMac());
                                    getDeviceInfo();
                                    getHistoryData();
                                    start_ecg.setEnabled(true);
                                }
                            });
                        }
                        else if (i == Constants.CODE.Code_Failed){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scan_status.setText("Seleccione su dispositivo");
                                    Toast.makeText(MainActivity.this,"Fallo al conectar, intente de nuevo.",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        device_battery.setText(DEVICE_BATTERY_VALUE+"%");
                    }
                }, 5000);
            }
        });
    }

    public void sendDataToServer() {
        ArrayList arrayList1 = new ArrayList();
        for (int i = 1;i<1001;i++){
            arrayList1.add((int) (Math.random()*801)-400);
        }
        String json = "[\n";
        for (int i = 0; i < arrayList1.size();i++){
            if (i != arrayList1.size()-1){
                json += "{\"ecgValue\": "+arrayList1.get(i)+"},\n";
            }else{
                json += "{\"ecgValue\": "+arrayList1.get(i)+"}\n]";
            }
        }
        JSONArray jsonArray = new JSONArray(arrayList1);
        JSONObject post_dict = new JSONObject();
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        try {
            obj1.put("ecgVal",1);
            post_dict.put("ecgValue" , 10);
            post_dict.put("ecgValue2" , 12);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (post_dict.length() > 0) {
            new SendJsonDataToServer().execute(json);
            System.out.println("xxxxxxxxxxxx: "+json);
            //#call to async class
        }

    }
    Runnable runnableEcg = new Runnable() {

        public void run() {
            YCBTClient.appEcgTestEnd(new BleDataResponse() {
                @Override
                public void onDataResponse(int i, float v, HashMap hashMap) {

                }
            });
            handlerEcg.removeCallbacks(runnableEcg);
            System.out.println("xxxxxxxxxxxx: FIN DEL ECG");
            Toast.makeText(MainActivity.this, "Fin del ECG", Toast.LENGTH_SHORT).show();
            Animation animationecg = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
            ecg_anim.setAnimation(animationecg);
            ecg_anim.setVisibility(View.GONE);
        }
    };

    private void startScan() {
        YCBTClient.startScanBle(new BleScanResponse() {
            @Override
            public void onScanResponse(int i, ScanDeviceBean scanDeviceBean) {

                if (scanDeviceBean != null) {
                    if (!listVal.contains(scanDeviceBean.getDeviceMac())) {
                        listVal.add(scanDeviceBean.getDeviceMac());
                        deviceAdapter.addModel(scanDeviceBean);
                    }

                    Log.e("device", "mac=" + scanDeviceBean.getDeviceMac() + ";name=" + scanDeviceBean.getDeviceName() + "rssi=" + scanDeviceBean.getDeviceRssi());

                }
            }
        }, 6);
    }

    private void getHistoryData(){
        YCBTClient.healthHistoryData(0x0509, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                if (hashMap != null) {
                    ArrayList<HashMap> lists = (ArrayList) hashMap.get("data");
                            /*for (HashMap map : lists) {
                                System.out.println("chong----" + map.get("state"));
                            }*/
                    for (HashMap map : lists) {
                        int blood_oxygen = (int) map.get("OOValue");//Blood oxygen  if (blood_oxygen == 0)  no value
                        int tempIntValue = (int) map.get("tempIntValue");//Temp int value
                        int tempFloatValue = (int) map.get("tempFloatValue");//Temp float value. if (tempFloatValue == 15) the result is error
                        int hrv = (int) map.get("hrvValue");//hrv   if (hrv == 0)  no value
                        int cvrr = (int) map.get("cvrrValue");//cvrr   if (cvrr == 0)  no value
                        int respiratoryRateValue = (int) map.get("respiratoryRateValue");//Respiratory Rate  if (respiratoryRateValue == 0)  no value
                        long startTime = (long) map.get("startTime");
                        double temp = 0;
                        if (tempFloatValue != 15) {
                            temp = Double.parseDouble(tempIntValue + "." + tempFloatValue);
                        }
                        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime));
                        //createHistory(1,time,blood_oxygen,temp,hrv,cvrr,respiratoryRateValue);
                        insertHistoryToDB(1,time,blood_oxygen,temp,hrv,cvrr,respiratoryRateValue);
                        System.out.println("oxigeno: "+blood_oxygen+",temperatura: "+temp+", hrv:"+hrv+",cvrr: "+cvrr+", resp rate: "+respiratoryRateValue+", fecha: "+time);
                    }
                }
            }
        });
        Toast.makeText(this, "Historial sincronizado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_disconnect_dev:
                YCBTClient.disconnectBle();
                break;
            case R.id.bt_start_scan: {

                YCBTClient.startScanBle(new BleScanResponse() {
                    @Override
                    public void onScanResponse(int i, ScanDeviceBean scanDeviceBean) {

                        if (scanDeviceBean != null) {
                            if (!listVal.contains(scanDeviceBean.getDeviceMac())) {
                                listVal.add(scanDeviceBean.getDeviceMac());
                                deviceAdapter.addModel(scanDeviceBean);
                            }

                            Log.e("device", "mac=" + scanDeviceBean.getDeviceMac() + ";name=" + scanDeviceBean.getDeviceName() + "rssi=" + scanDeviceBean.getDeviceRssi());

                        }
                    }
                }, 6);

                break;
            }
            case R.id.bt_stop_scan: {
                YCBTClient.stopScanBle();
                break;
            }
            case R.id.bt_connect_dev: {
                //p3plus D8:D3:F7:1F:22:69
                //v5 F3:E8:04:E8:73:68
//                YCBTClient.connectBle("", new BleConnectResponse() {
//                    @Override
//                    public void onConnectResponse(int i) {
//
//                    }
//                });
                break;
            }
            case R.id.bt_write_test: {
                EcgDialog ecgDialog = new EcgDialog(MainActivity.this);
                ecgDialog.startLoadingDialog();
                AITools.getInstance().Init();

                //AITools.getInstance().getResult(new ArrayList<Integer>());

                YCBTClient.appEcgTestStart(new BleDataResponse() {
                    @Override
                    public void onDataResponse(int i, float v, HashMap hashMap) {

                    }
                }, new BleRealDataResponse() {
                    @Override
                    public void onRealDataResponse(int i, HashMap hashMap) {
                        if (hashMap != null) {
                            int dataType = (int) hashMap.get("dataType");
                            Log.e("qob", "onRealDataResponse " + i + " dataType " + dataType);
                            if (i == Constants.DATATYPE.Real_UploadECG) {
                                final List<Integer> tData = (List<Integer>) hashMap.get("data");
                                ecgDialog.dismissDialog();
                                handlerEcg.postDelayed(runnableEcg,60000);
                                //System.out.println("xxxxxxxxxxxxxxx: "+tData.size());
                                for (i = 0; i< tData.size();i++){
                                    arrayList.add(tData.get(i));
                                }
                                System.out.println("chong----------ecgData==" + tData.toString());
                            } else if (i == Constants.DATATYPE.Real_UploadECGHrv) {
                                float param = (float) hashMap.get("data");
                                Log.e("qob", "HRV: " + param);
                            } else if (i == Constants.DATATYPE.Real_UploadECGRR) {
                                float param = (float) hashMap.get("data");
                                Log.e("qob", "RR invo " + param);
                            } else if (i == Constants.DATATYPE.Real_UploadBlood) {
                                int heart = (int) hashMap.get("heartValue");//心率
                                int tDBP = (int) hashMap.get("bloodDBP");//高压
                                int tSBP = (int) hashMap.get("bloodSBP");//低压
                                DBP_VALUE = String.valueOf(tDBP);
                                //DBP_VALUE = "140";
                                SBP_VALUE = String.valueOf(tSBP);
                                //SBP_VALUE = "120";
                                HEART_VALUE = String.valueOf(heart);
                                System.out.println("xxxxxxxxxxx: "+DBP_VALUE+", "+SBP_VALUE+", "+HEART_VALUE);
                            }
                        }
                    }
                });
                start_ecg.setEnabled(false);
                ecg_anim.setVisibility(view.VISIBLE);
                updateValues.run();
                break;
            }
            case R.id.bt_write_real: {
                YCBTClient.deviceToApp(new BleDeviceToAppDataResponse() {
                    @Override
                    public void onDataResponse(int i, HashMap hashMap) {
                        if (hashMap != null) {
                            if (i == 0) {//
                                int dataType = (int) hashMap.get("dataType");
                                int data = -1;
                                if (hashMap.get("data") != null)
                                    data = (int) hashMap.get("data");
                                switch (dataType) {
                                    case Constants.DATATYPE.AppECGPPGStatus:
                                        int EcgStatus = (int) hashMap.get("EcgStatus");
                                        int PPGStatus = (int) hashMap.get("PPGStatus");
                                        if (PPGStatus == 0) {//0 :wear  1: no wear 2: error
                                        }
                                        break;
                                    case Constants.DATATYPE.DeviceFindMobile://寻找手机
                                        if (data == 0) {//0: 结束 1: 开始
                                        }
                                        break;
                                    case Constants.DATATYPE.DeviceLostReminder://防丢提醒
                                        if (data == 0) {//0: 结束 1: 开始
                                        }
                                        break;
                                    case Constants.DATATYPE.DeviceAnswerAndClosePhone://接听/拒接电话
                                        if (data == 0) {//0: 接听 1: 拒接
                                        }
                                        break;
                                    case Constants.DATATYPE.DeviceTakePhoto://相机拍照控制
                                        if (data == 0) {//0x00: 退出拍照模式 0x01: 进入拍照模式 0x02: 拍照
                                        }
                                        break;
                                    case Constants.DATATYPE.DeviceStartMusic://音乐控制
                                        if (data == 0) {//0: 音乐停止 1: 播放 2: 暂停 3: 上一曲 4: 下一曲
                                        }
                                        break;
                                    case Constants.DATATYPE.DeviceSos://开启一键呼救控制命令
                                        break;
                                    case Constants.DATATYPE.DeviceDrinkingPatterns://开启饮酒模式控制命令
                                        break;
                                }
                            }
                        }
                    }
                });

                break;
            }
            case R.id.bt_ui_getinfo: {
                YCBTClient.getDeviceInfo(new BleDataResponse() {
                    @Override
                    public void onDataResponse(int i, float v, HashMap hashMap) {
                        if(hashMap != null){
                            HashMap map = (HashMap) hashMap.get("data");
                            String deviceId = (String) map.get("deviceId");
                            String deviceVersion = (String) map.get("deviceVersion");
                            String deviceBatteryState = (String) map.get("deviceBatteryState");
                            String deviceBatteryValue = (String) map.get("deviceBatteryValue");
                        }
                    }
                });
                break;
            }
            case R.id.bt_delete_history_data:
                YCBTClient.deleteHealthHistoryData(0x0544, new BleDataResponse() {
                    @Override
                    public void onDataResponse(int i, float v, HashMap hashMap) {
                        if (i == 0) {//delete success

                        }
                    }
                });
                break;
            case R.id.read_real_temp://read real temp
                readRealTemp();
                break;
            case R.id.open_real_temp://open real temp
                openRealTemp();
                break;
            case R.id.close_real_temp://close real temp
                closeRealTemp();
                break;
            case R.id.send_message:
                YCBTClient.appSengMessageToDevice(0x03, "QQ", "测试信息", new BleDataResponse() {
                    @Override
                    public void onDataResponse(int i, float v, HashMap hashMap) {
                        if (i == 0) {
                            //success sendMessage
                        }
                    }
                });
                break;
            case R.id.send_model:
                YCBTClient.appMobileModel("OPPO A77", new BleDataResponse() {
                    @Override
                    public void onDataResponse(int i, float v, HashMap hashMap) {
                        if (i == 0) {
                            //success
                            int sleepNum = (int) hashMap.get("SleepNum");
                            int sleepTotalTime = (int) hashMap.get("SleepTotalTime");
                            int heartNum = (int) hashMap.get("HeartNum");
                            int sportNum = (int) hashMap.get("SportNum");
                            int bloodNum = (int) hashMap.get("BloodNum");
                            int bloodOxygenNum = (int) hashMap.get("BloodOxygenNum");
                            int tempHumidNum = (int) hashMap.get("TempHumidNum");
                            int tempNum = (int) hashMap.get("TempNum");
                            int ambientLightNum = (int) hashMap.get("AmbientLightNum");
                        }
                    }
                });
                break;
            case R.id.get_scheduleInfo:
                YCBTClient.getScheduleInfo(new BleDataResponse() {
                    @Override
                    public void onDataResponse(int i, float v, HashMap hashMap) {
                        if (i == 0) {
                            //success
                            int totalScheduleInfoValue = (int) hashMap.get("totalScheduleInfoValue");
                            System.out.println("chong---------count==" + totalScheduleInfoValue);
                            List<HashMap> lists = (List<HashMap>) hashMap.get("data");
                            System.out.println("chong---------count==" + lists.size());
                            for(HashMap hm : lists){
                                System.out.println("chong------------content==" + hm.get("incidentName"));
                            }
                        }
                    }
                });
                break;
            /**
             * 日程修改设置
             *
             * @param type           0x00:修改日程  0x01:增加日程  0x02:删除日程
             * @param scheduleIndex  修改的日程索引 1-20
             * @param scheduleEnable 0x00：禁止 0x01：使能
             * @param eventIndex     修改的事件 索引
             * @param eventEnable    0x00：禁止 0x01：使能
             * @param time           修改事件的时间 格式为yyyy-MM-dd HH:mm:ss
             * @param eventType      事件类型  0x00 起床 0x01 早饭 0x02 晒太阳 0x03 午饭 0x04 午休 0x05 运动 0x06 晚饭 0x07 睡觉 0x08 自定义
             * @param content        修改事件类型的名称
             * @param dataResponse
             */
            case R.id.add_scheduleInfo:
                YCBTClient.settingScheduleModification(1, 15, 1, 15, 1, "2020-07-10 20:00:00", 8, "chong", new BleDataResponse() {
                    @Override
                    public void onDataResponse(int i, float v, HashMap hashMap) {
                        System.out.println("chong-----------result==" + i);
                    }
                });
                break;
        }
    }

    private Runnable updateValues = new Runnable() {
        @Override
        public void run() {
            //System.out.println("XXXXXXXXXXXXXX: "+device_battery.getText());
            if (device_battery.getText().equals("null%") && DEVICE_BATTERY_VALUE != null){
                device_battery.setText(DEVICE_BATTERY_VALUE);
            }else{
                if (DEVICE_BATTERY_VALUE == null){
                    getDeviceInfo();
                }
            }
            if (DBP_VALUE != null){
                DBP_tv.setText(DBP_VALUE);
                SBP_tv.setText(SBP_VALUE);
                heart_tv.setText(HEART_VALUE);
                Integer DBP_int;
                Integer SBP_int;
                DBP_int = Integer.parseInt(DBP_VALUE);
                SBP_int = Integer.parseInt(SBP_VALUE);
                if (DBP_int < 120 && SBP_int < 80){
                    preasure_tv.setText("Presión arterial Normal*");
                    aha_tv.setVisibility(View.VISIBLE);
                    preasure_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.normal));
                }
                if (DBP_int >= 120 && DBP_int <= 129 && SBP_int < 80){
                    preasure_tv.setText("Presión arterial Elevada*");
                    aha_tv.setVisibility(View.VISIBLE);
                    preasure_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.elevated));
                }
                if ((DBP_int >= 129 && DBP_int <= 139) || (SBP_int >= 80 && SBP_int <= 89)){
                    preasure_tv.setText("Presión arterial Alta\n(Hipertensión etapa 1)*");
                    aha_tv.setVisibility(View.VISIBLE);
                    preasure_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.highStgOne));
                }
                if (DBP_int > 139 || SBP_int > 89){
                    preasure_tv.setText("Presión arterial Alta\n(Hipertensión etapa 2)*");
                    aha_tv.setVisibility(View.VISIBLE);
                    preasure_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.highStgTwo));
                }
                if (DBP_int >= 180 || SBP_int >= 120){
                    preasure_tv.setText("Crisis hipertensiva*");
                    aha_tv.setVisibility(View.VISIBLE);
                    preasure_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.crisis));
                }
            }else{
                preasure_tv.setText(". . . Espere");
            }

            handler.postDelayed(this,5000);
        }
    };

    private void insertHistoryToDB(final int user_id, final String time, final int blood_oxygen, final double temp, final int hrv, final int cvrr, final int respiratoryRateValue) {
        String url ="https://iot-medical.ml/insertHistory.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest sringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.d("response",s);
                //Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("error",""+volleyError);
            }
        }){
            protected HashMap<String,String> getParams() throws AuthFailureError{
                HashMap<String,String> map = new HashMap<>();
                map.put("user_id",String.valueOf(user_id));
                map.put("date",time);
                map.put("oxygen",String.valueOf(blood_oxygen));
                map.put("temperature",String.valueOf(temp));
                map.put("hrv",String.valueOf(hrv));
                map.put("cvrr",String.valueOf(cvrr));
                map.put("resp_rate",String.valueOf(respiratoryRateValue));
                return map;
            }
        };
        requestQueue.add(sringRequest);
    }

    private void getDeviceInfo() {

        YCBTClient.getDeviceInfo(new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                if(hashMap != null){
                    HashMap map = (HashMap) hashMap.get("data");
                    String deviceId = (String) String.valueOf(map.get("deviceId"));
                    String deviceVersion = (String) map.get("deviceVersion");
                    String deviceBatteryState = (String) String.valueOf(map.get("deviceBatteryState"));
                    String deviceBatteryValue = (String) String.valueOf(map.get("deviceBatteryValue"));
                    DEVICE_BATTERY_VALUE = deviceBatteryValue;
                    System.out.println("id: "+deviceId+", version: "+deviceVersion+", battery state: "+deviceBatteryState+", battery value: "+deviceBatteryValue);
                }
            }
        });
    }
    /**
     * After opening, the bracelet will be black. This is normal.
     */
    private void openRealTemp() {
        YCBTClient.appTemperatureMeasure(0x01, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                if (i == 0) {
                    //success
                }
            }
        });
    }

    /**
     * if your need more, you can loop through this method.
     * but it must be after start_real_temp method.
     */
    private void readRealTemp() {
        YCBTClient.getRealTemp(new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                if (i == 0) {
                    String temp = (String) hashMap.get("tempValue");
                }
            }
        });
    }

    /**
     * If you call the startRealTemp() method, you must call this method.
     * Otherwise, the bracelet will always be black and the temperature will be monitored in the background.
     */
    private void closeRealTemp() {
        YCBTClient.appTemperatureMeasure(0x00, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                if (i == 0) {
                    //success
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void connectEvent(ConnectEvent connectEvent) {
        baseOrderSet();
    }


    /***
     * 语言设置
     *   0x00:英语 0x01: 中文 0x02: 俄语 0x03: 德语 0x04: 法语
     * 0x05: 日语 0x06: 西班牙语 0x07: 意大利语 0x08: 葡萄牙文
     * 0x09: 韩文 0x0A: 波兰文 0x0B: 马来文 0x0C: 繁体中文 0xFF:其它
     * @param
     */
    //基础指令设置
    private void baseOrderSet() {


        /***
         * 语言设置
         * @param langType 0x00:英语 0x01: 中文 0x02: 俄语 0x03: 德语 0x04: 法语
         * 0x05: 日语 0x06: 西班牙语 0x07: 意大利语 0x08: 葡萄牙文
         * 0x09: 韩文 0x0A: 波兰文 0x0B: 马来文 0x0C: 繁体中文 0xFF:其它
         * @param dataResponse
         */
        YCBTClient.settingLanguage(0x01, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                Log.e("device", "同步语言结束");
            }
        });


//        setPhoneTime();


        //心率采集
        YCBTClient.settingHeartMonitor(0x01, 10, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                Log.e("device", "设置10分钟间隔采集心率");
            }
        });


        //无感检测
        YCBTClient.settingPpgCollect(0x01, 60, 60, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                Log.e("device", "设置无感数据采集");
            }
        });


        //同步心率
        syncHisHr();
        //同步睡眠
        syncHisSleep();

        syncHisStep();


    }


    private void setPhoneTime() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        int week2 = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        if (week == 1) {
            week += 5;
        } else {
            week -= 2;
        }


//        周 0-6(星期一~星期天)
        // 0 周一，1周二，2周三，3周四，4周五，5周六，6周日
        Log.e("device", "day of week jian=" + week);
        Log.e("device", "day of week week2=" + week2);

//        YCBTClient.getDeviceInfo(new BleDataResponse() {
//            @Override
//            public void onDataResponse(int i, float v, HashMap hashMap) {
//
//            }
//        });

    }


    //同步历史心率
    private void syncHisHr() {
        YCBTClient.healthHistoryData(Constants.DATATYPE.Health_HistoryHeart, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {

                if (hashMap != null) {


                    Log.e("history", "hashMap=" + hashMap.toString());

                    Log.e("history", "hr time=" + hashMap.get("heartStartTime"));

                    Log.e("history", "hr val=" + hashMap.get("heartValue"));
                } else {
                    Log.e("history", "no ..hr..data....");
                }

//                syncHisStep();
            }
        });
    }


    //同步历史睡眠
    private void syncHisSleep() {
        YCBTClient.healthHistoryData(Constants.DATATYPE.Health_HistorySleep, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {

                if (hashMap != null) {
                    Log.e("history", "hashMap=" + hashMap.toString());

                } else {
                    Log.e("history", "no ..sleep..data....");
                }

                //同步步数
//                syncHisStep();
//                hashMap.get();
            }
        });
    }

    //同步历史步数
    private void syncHisStep() {
        YCBTClient.healthHistoryData(Constants.DATATYPE.Health_HistorySport, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                if (hashMap != null) {

                    Log.e("history", "hashMap=" + hashMap.toString());

                    Log.e("history", "step start time=" + hashMap.get("sportStartTime"));
                    Log.e("history", "step end time=" + hashMap.get("sportEndTime"));
                    Log.e("history", "step num=" + hashMap.get("sportStep"));
                } else {
                    Log.e("history", "no...step ..data....");
                }
            }
        });
    }

    private void createHistory(final int user_id, final String time, final int blood_oxygen, final double temp, final int hrv, final int cvrr, final int respiratoryRateValue){
        if (new History(getApplicationContext(),
                0,
                user_id,
                time,
                blood_oxygen,
                temp,
                hrv,
                cvrr,
                respiratoryRateValue,
                false
        ).create()){
            //Toast.makeText(this,"Registrado",Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(this,"hora: "+globalVariable.time,Toast.LENGTH_LONG).show();
    }

    private void createOnServer() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
