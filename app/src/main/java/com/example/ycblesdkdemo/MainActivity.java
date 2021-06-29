package com.example.ycblesdkdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ycblesdkdemo.adapter.DeviceAdapter;
import com.example.ycblesdkdemo.configs.Auth;
import com.example.ycblesdkdemo.dbmodels.History;
import com.example.ycblesdkdemo.model.ConnectEvent;
import com.yucheng.ycbtsdk.AITools;
import com.yucheng.ycbtsdk.Bean.ScanDeviceBean;
import com.yucheng.ycbtsdk.Constants;
import com.yucheng.ycbtsdk.Response.BleConnectResponse;
import com.yucheng.ycbtsdk.Response.BleDataResponse;
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
    private Auth auth;
    private String DEVICE_BATTERY_VALUE = null;
    private String JSON_HEALTH = "[\n";
    private String JSON_SLEEP = "[\n";
    private String DBP_VALUE = null;
    private String SBP_VALUE = null;
    private String HEART_VALUE = null;
    private Integer progress_count = 0;
    private Boolean ecg_start = false;
    private ArrayList arrayList = new ArrayList();
    private ListView listView;
    private Button start_ecg;
    private ImageButton logout_bt;
    private ImageButton web_view;
    private SwipeRefreshLayout refresh_layout;
    private TextView username_tv;
    private TextView scan_status;
    private TextView device_name;
    private TextView device_mac;
    private TextView device_battery;
    private TextView DBP_tv;
    private TextView SBP_tv;
    private TextView heart_tv;
    private TextView preasure_tv;
    private TextView aha_tv;
    private ProgressBar progressIndicator;
    private ObjectAnimator progressAnimator;
    private CardView bloodPreasure_cv;
    LottieAnimationView scan_anim;
    LottieAnimationView ecg_anim;
    RelativeLayout con_device_rl;
    private Handler handler = new Handler();
    Handler handlerEcg = new Handler();
    Handler handlerProg = new Handler();

    private List<ScanDeviceBean> listModel = new ArrayList<>();
    private List<String> listVal = new ArrayList<>();
    DeviceAdapter deviceAdapter = new DeviceAdapter(MainActivity.this, listModel);

    private AITools aiTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        EventBus.getDefault().register(this);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            startScan();
        }else{
            startScan();
        }

        //z ligth Sleep time
        //zzz deep sleep time
        this.auth = new Auth(getApplicationContext());
        startService(new Intent(this, MyBleService.class));

        username_tv = findViewById(R.id.username_tv);
        progressIndicator = findViewById(R.id.progress_horizontal_lpi);
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
        logout_bt = findViewById(R.id.logout_bt);
        start_ecg = findViewById(R.id.bt_write_test);
        start_ecg.setOnClickListener(this);
        web_view = findViewById(R.id.web_view_bt);
        refresh_layout = findViewById(R.id.refresh_layout);
        bloodPreasure_cv = findViewById(R.id.blood_preasure_cv);
        web_view.setOnClickListener(this);
        logout_bt.setOnClickListener(this);

        username_tv.setText(auth.getUsername());
        start_ecg.setEnabled(false);

        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                YCBTClient.disconnectBle();
                Intent i = new Intent(MainActivity.this,MainActivity.class);
                startActivity(i);
                finish();
                /*listView.setVisibility(View.VISIBLE);
                bloodPreasure_cv.setVisibility(View.GONE);
                con_device_rl.setVisibility(View.GONE);
                scan_status.setText("Seleccione su dispositivo");
                start_ecg.setEnabled(false);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
                scan_anim.setAnimation(animation);
                startScan();
                listView.setAdapter(deviceAdapter);
                refresh_layout.setRefreshing(false);*/
            }
        });

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
                                    Toast.makeText(MainActivity.this, "i=" + i, Toast.LENGTH_SHORT);
                                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
                                    scan_anim.setAnimation(animation);
                                    listView.setVisibility(view.GONE);
                                    con_device_rl.setVisibility(view.VISIBLE);
                                    device_name.setText(scanDeviceBean.getDeviceName());
                                    device_mac.setText(scanDeviceBean.getDeviceMac());
                                    scan_status.setText("Conectado");
                                    bloodPreasure_cv.setVisibility(View.VISIBLE);
                                    getDeviceInfo();
                                    getHistoryData();
                                    settingUnit(0x00,0x00,0x00,0x01);
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

        progressAnimator = ObjectAnimator.ofInt(progressIndicator,"progress",0,100);
        //fin del ecg

        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
                progressIndicator.setIndeterminate(true);
            }
        });

        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                YCBTClient.appEcgTestEnd(new BleDataResponse() {
                    @Override
                    public void onDataResponse(int i, float v, HashMap hashMap) {

                    }
                });
                updateBloodPressureVal();
                sendHhDataToServer();
                sendDataToServer();
                new SendJsonSleepDataToServer().execute(JSON_SLEEP);
                System.out.println("xxxxxxxxxxxx: FIN DEL ECG");
                Integer DBP_int, SBP_int,HR_int;
                /*DBP_int = Integer.parseInt(DBP_VALUE);
                SBP_int = Integer.parseInt(SBP_VALUE);
                HR_int = Integer.parseInt(HEART_VALUE);*/
                Animation animationecg = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
                ecg_anim.setAnimation(animationecg);
                ecg_anim.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "FIN DEL ECG", Toast.LENGTH_SHORT).show();
                progressIndicator.setVisibility(View.GONE);
            }
        });
    }
    public void sendHhDataToServer() {
        /*JSONArray jsonArray = new JSONArray(arrayList1);
        JSONObject post_dict = new JSONObject();
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        try {
            obj1.put("ecgVal",1);
            post_dict.put("ecgValue" , 10);
            post_dict.put("ecgValue2" , 12);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        new SendJsonHhDataToServer().execute(JSON_HEALTH);
        //System.out.println("xxxxxxxxxxxx: "+JSON_HEALTH);
    }
    public void sendDataToServer() {
        /*ArrayList arrayList1 = new ArrayList();
        for (int i = 1;i<1001;i++){
            arrayList1.add((int) (Math.random()*801)-400);
        }*/
        String json = "[\n";
        for (int i = 0; i < arrayList.size();i++){
            if (i != arrayList.size()-1){
                json += "{  \n" +
                        "    \"user_id\": "+auth.getUser_id()+",  \n" +
                        "    \"ecgValue\": "+arrayList.get(i)+"  \n" +
                        "  },\n";
            }else{
                json += "{  \n" +
                        "    \"user_id\": "+auth.getUser_id()+",  \n" +
                        "    \"ecgValue\": "+arrayList.get(i)+"  \n" +
                        "  }\n]";
            }
        }
        /*JSONArray jsonArray = new JSONArray(arrayList1);
        JSONObject post_dict = new JSONObject();
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        try {
            obj1.put("ecgVal",1);
            post_dict.put("ecgValue" , 10);
            post_dict.put("ecgValue2" , 12);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        new SendJsonDataToServer().execute(json);
        //System.out.println("xxxxxxxxxxxx: "+json);
    }
    Runnable runnableEcg = new Runnable() {

        public void run() {
            YCBTClient.appEcgTestEnd(new BleDataResponse() {
                @Override
                public void onDataResponse(int i, float v, HashMap hashMap) {

                }
            });
            handlerEcg.removeCallbacks(runnableEcg);
            sendHhDataToServer();
            sendDataToServer();
            new SendJsonSleepDataToServer().execute(JSON_SLEEP);
            System.out.println("xxxxxxxxxxxx: FIN DEL ECG");
            Integer DBP_int, SBP_int,HR_int;
            /*DBP_int = Integer.parseInt(DBP_VALUE);
            SBP_int = Integer.parseInt(SBP_VALUE);
            HR_int = Integer.parseInt(HEART_VALUE);*/
            Animation animationecg = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
            ecg_anim.setAnimation(animationecg);
            ecg_anim.setVisibility(View.GONE);
        }
    };

    Runnable runnableProg = new Runnable() {

        public void run() {
            if (ecg_start){
                handlerProg.removeCallbacks(runnableProg);
                prog();
                System.out.println("xxxxxxxxxxxx: inicio progress bar");
            }else{
                handlerProg.postDelayed(runnableProg,2000);
            }

        }
    };

    private void settingUnit(int distanceUnit,int weightUnit,int temperatureUnit,int timeFormat) {
        YCBTClient.settingUnit(distanceUnit, weightUnit, temperatureUnit, timeFormat, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {

            }
        });
    }


    private void startScan() {
        YCBTClient.startScanBle(new BleScanResponse() {
            @Override
            public void onScanResponse(int i, ScanDeviceBean scanDeviceBean) {

                if (scanDeviceBean != null) {
                    if (!listVal.contains(scanDeviceBean.getDeviceMac())) {
                        listVal.add(scanDeviceBean.getDeviceMac());
                        deviceAdapter.addModel(scanDeviceBean);
                        deviceAdapter.notifyDataSetChanged();
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
                    int s = lists.size();
                    int count = 0;
                    for (HashMap map : lists) {
                        count++;
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
                        @SuppressLint("SimpleDateFormat") String time;
                        time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime));
                        if (count == s){
                            JSON_HEALTH += "{  \n" +
                                    "    \"user_id\": "+auth.getUser_id()+",  \n" +
                                    "    \"date\": \""+time+"\",  \n" +
                                    "    \"oxygen\": "+blood_oxygen+",  \n" +
                                    "    \"temperature\": "+temp+",  \n" +
                                    "    \"hrv\": "+hrv+",  \n" +
                                    "    \"cvrr\": "+cvrr+",  \n" +
                                    "    \"resp_rate\": "+respiratoryRateValue+"  \n" +
                                    "  }\n]";
                            //System.out.println("xxxxxxxxxxxxx: "+JSON_HEALTH);
                        }else{
                            JSON_HEALTH += "{  \n" +
                                    "    \"user_id\": "+auth.getUser_id()+",  \n" +
                                    "    \"date\": \""+time+"\",  \n" +
                                    "    \"oxygen\": "+blood_oxygen+",  \n" +
                                    "    \"temperature\": "+temp+",  \n" +
                                    "    \"hrv\": "+hrv+",  \n" +
                                    "    \"cvrr\": "+cvrr+",  \n" +
                                    "    \"resp_rate\": "+respiratoryRateValue+"  \n" +
                                    "  },\n";
                        }
                        //System.out.println("oxigeno: "+blood_oxygen+",temperatura: "+temp+", hrv:"+hrv+",cvrr: "+cvrr+", resp rate: "+respiratoryRateValue+", fecha: "+time);

                    }
                }
            }
        });
        //syncHisSleep();
        YCBTClient.healthHistoryData(Constants.DATATYPE.Health_HistorySleep, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {

                if (hashMap != null) {
                    JSONObject jsonObject = new JSONObject(hashMap);
                    try {
                        JSONArray data = jsonObject.getJSONArray("data");
                        for (int j = 0; j < data.length(); j++){
                            JSONObject sleepData = data.getJSONObject(j);
                            Long startTime = sleepData.getLong("startTime");
                            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime));
                            Double lightSleepTotal = sleepData.getDouble("lightSleepTotal")/60;
                            Double deepSleepTotal = sleepData.getDouble("deepSleepTotal")/60;
                            if (j == data.length()-1){
                                JSON_SLEEP += "{  \n" +
                                        "    \"user_id\": "+auth.getUser_id()+",  \n" +
                                        "    \"fecha\": \""+dateString+"\",  \n" +
                                        "    \"sueno_ligero\": "+lightSleepTotal+",  \n" +
                                        "    \"sueno_profundo\": "+deepSleepTotal+"  \n" +
                                        "  }\n]";
                                //System.out.println("xxxxxxxxxxxxx: "+JSON_HEALTH);
                            }else{
                                JSON_SLEEP += "{  \n" +
                                        "    \"user_id\": "+auth.getUser_id()+",  \n" +
                                        "    \"fecha\": \""+dateString+"\",  \n" +
                                        "    \"sueno_ligero\": "+lightSleepTotal+",  \n" +
                                        "    \"sueno_profundo\": "+deepSleepTotal+"  \n" +
                                        "  },\n";
                            }
                            System.out.println(JSON_SLEEP);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXx no sleep data");
                }

                //同步步数
//                syncHisStep();
//                hashMap.get();
            }
        });
        /*YCBTClient.healthHistoryData(Constants.DATATYPE.Health_HistorySport, new BleDataResponse() {
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
        });*/
        Toast.makeText(this, "Historial sincronizado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logout_bt:
                this.auth.logout();
                YCBTClient.disconnectBle();
                Intent i = new Intent(this, logIn.class);
                startActivity(i);
                finish();
                break;

            case R.id.web_view_bt: {
                Intent intent = new Intent(MainActivity.this,webView.class);
                startActivity(intent);
                break;
            }

            case R.id.bt_write_test: {
                EcgDialog ecgDialog = new EcgDialog(MainActivity.this);
                ecgDialog.startLoadingDialog();
                //runnableProg.run();
                prog();
                AITools.getInstance().Init();
                //AITools.getInstance().getResult(new ArrayList<Integer>());

                YCBTClient.appEcgTestStart(new BleDataResponse() {
                    @Override
                    public void onDataResponse(int i, float v, HashMap hashMap) {

                    }
                }, new BleRealDataResponse() {
                    @Override
                    public void onRealDataResponse(int i, HashMap hashMap) {
                        YCBTClient.getElectrodeLocationInfo(new BleDataResponse() {
                            @Override
                            public void onDataResponse(int code, float ratio, HashMap resultMap) {
                                if (resultMap != null){
                                    System.out.println(resultMap.toString());
                                }
                            }
                        });
                        if (hashMap != null) {
                            int dataType = (int) hashMap.get("dataType");
                            Log.e("qob", "onRealDataResponse xxxxxxxxxxxxxxxxxxxxxxx " + i + " dataType " + dataType);
                            if (i == Constants.DATATYPE.Real_UploadECG) {
                                final List<Integer> tData = (List<Integer>) hashMap.get("data");
                                for (int j = 0; j< tData.size();j++){
                                    arrayList.add(tData.get(j));
                                }
                                ecgDialog.dismissDialog();
                                //handlerEcg.postDelayed(runnableEcg,60000);
                                ecg_start = true;

                                //System.out.println("chong----------ecgData==" + tData.toString());
                            } else if (i == Constants.DATATYPE.Real_UploadECGHrv) {
                                float param = (float) hashMap.get("data");
                                Log.e("qob", "HRV: " + param);
                            } else if (i == Constants.DATATYPE.Real_UploadECGRR) {
                                float param = (float) hashMap.get("data");
                                Log.e("qob", "RR invo " + param);
                            } else if (i == Constants.DATATYPE.Real_UploadBlood) {
                                //System.out.println("xxxxxxxxxxxxxxxxxxxx vuelva a colorcar el dedo");
                                int heart = (int) hashMap.get("heartValue");//心率
                                int tDBP = (int) hashMap.get("bloodDBP");//高压
                                int tSBP = (int) hashMap.get("bloodSBP");//低压
                                DBP_VALUE = String.valueOf(tDBP);
                                //DBP_VALUE = "140";
                                SBP_VALUE = String.valueOf(tSBP);
                                //SBP_VALUE = "120";
                                HEART_VALUE = String.valueOf(heart);
                                //System.out.println("xxxxxxxxxxx: "+DBP_VALUE+", "+SBP_VALUE+", "+HEART_VALUE);
                            }
                        }
                    }
                });
                start_ecg.setEnabled(false);
                ecg_anim.setVisibility(view.VISIBLE);
                //updateValues.run();
                break;
            }
        }
    }

    public void prog(){
        //progressAnimator = ObjectAnimator.ofInt(progressIndicator,"progress",0,100);
        progressIndicator.setVisibility(View.VISIBLE);
        progressAnimator.setDuration(60000);
        progressAnimator.start();
        /*final Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                progress_count++;
                progressIndicator.setProgress(progress_count);
                if (progress_count == 100 || ecg_start == false){
                    progressIndicator.setProgress(0);
                    t.cancel();
                }
            }
        };
        t.schedule(tt,0,600);*/
    }

    private Runnable updateValues = new Runnable() {
        @Override
        public void run() {
            //System.out.println("XXXXXXXXXXXXXX: "+device_battery.getText());
            if (device_battery.getText().equals("null%") && DEVICE_BATTERY_VALUE != null){
                device_battery.setText(DEVICE_BATTERY_VALUE+"%");
                scan_status.setText("Conectado");
            }else{
                if (DEVICE_BATTERY_VALUE == null){
                    getDeviceInfo();
                }
            }
        }
    };

    private void updateBloodPressureVal(){
        if (DBP_VALUE != null){
            DBP_tv.setText(DBP_VALUE);
            SBP_tv.setText(SBP_VALUE);
            heart_tv.setText(HEART_VALUE);
            Integer DBP_int;
            Integer SBP_int;
            Integer HR_int;
            DBP_int = Integer.parseInt(DBP_VALUE);
            SBP_int = Integer.parseInt(SBP_VALUE);
            HR_int = Integer.parseInt(HEART_VALUE);
            insertHistoryToDB(auth.getUser_id(),DBP_int,SBP_int,HR_int);
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
            preasure_tv.setText("Vuelva a intentarlo");
        }
    }

    private void insertHistoryToDB(final int user_id, final int dbp, final int sbp, final int hr) {
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
                map.put("dbp",String.valueOf(sbp));
                map.put("sbp",String.valueOf(dbp));
                map.put("hr",String.valueOf(hr));
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
                    System.out.println("XXXXXXXXXXXXXXXXXx history sleep " + hashMap.toString());

                } else {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXx no sleep data");
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
