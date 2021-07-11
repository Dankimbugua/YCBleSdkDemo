package com.example.ycblesdkdemo;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ycblesdkdemo.configs.Auth;
import com.yucheng.ycbtsdk.AITools;
import com.yucheng.ycbtsdk.Constants;
import com.yucheng.ycbtsdk.Response.BleDataResponse;
import com.yucheng.ycbtsdk.Response.BleRealDataResponse;
import com.yucheng.ycbtsdk.YCBTClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import static com.example.ycblesdkdemo.MyApplication.CHANNEL_ID;

public class ExampleService extends Service {

    private Timer timer = new Timer();
    private Auth auth;
    private static int tDBP = 0;
    private static int tSBP = 0;
    private static int heart = 0;
    private static String fecha_presion = "--";
    private Handler handlerEcg = new Handler();
    private Handler handlerHealth = new Handler();
    private String JSON_HEALTH = "[\n";
    private String JSON_SLEEP = "[\n";
    private int monitor_mins = 10;


    @Override
    public void onCreate() {
        super.onCreate();
        this.auth = new Auth(this);
        setAlarm(1,ExampleService.alarmTime(),this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String input = intent.getStringExtra("inputExtra");
        String device_mac = intent.getStringExtra("inputExtra_mac");
        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,notificationIntent,0);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Monitoreo de salud en segundo plano")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_baseline_favorite_24)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1,notification);


        return START_NOT_STICKY;

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static String getFecha_presion() {
        return fecha_presion;
    }

    public static void setFecha_presion(String fecha_presion) {
        ExampleService.fecha_presion = fecha_presion;
    }

    public static int gettDBP() {
        return tDBP;
    }

    public static void settDBP(int dpb) {
        tDBP = dpb;
    }

    public static int gettSBP() {
        return tSBP;
    }

    public static void settSBP(int sbp) {
        tSBP = sbp;
    }

    public static int getHeart() {
        return heart;
    }

    public static void setHeart(int hr) {
        heart = hr;
    }

    //Repite el ecg cada x minutos
    Runnable runnableEcg = new Runnable() {

        public void run() {
            System.out.println("inicio runnable Ecg");
            ecgTest();
            handlerEcg.postDelayed(runnableEcg,monitor_mins*60000);
        }
    };

    //Repite el metodo getHistoryData cada x minutos
    Runnable runnableHealth = new Runnable() {

        public void run() {
            System.out.println("inicio runnable health");
            getHistoryData();
            handlerHealth.postDelayed(runnableHealth,monitor_mins*60000);
        }
    };

    //metodo para ecg
    public void ecgTest(){
        AITools.getInstance().Init();

        YCBTClient.appEcgTestStart(new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {

            }
        }, new BleRealDataResponse() {
            @Override
            public void onRealDataResponse(int i, HashMap hashMap) {
                if (hashMap != null) {
                    int dataType = (int) hashMap.get("dataType");
                    Log.e("qob", "onRealDataResponse xxxxxxxxxxxxxxxxxxxxxxx " + i + " dataType " + dataType);
                    if (i == Constants.DATATYPE.Real_UploadECG) {
                        final List<Integer> tData = (List<Integer>) hashMap.get("data");
                        for (int j = 0; j< tData.size();j++){
                            //arrayList.add(tData.get(j));
                        }
                        //handlerEcg.postDelayed(runnableEcg,60000);

                        //System.out.println("chong----------ecgData==" + tData.toString());
                    } else if (i == Constants.DATATYPE.Real_UploadECGHrv) {
                        float param = (float) hashMap.get("data");
                        Log.e("qob", "HRV: " + param);
                    } else if (i == Constants.DATATYPE.Real_UploadECGRR) {
                        float param = (float) hashMap.get("data");
                        Log.e("qob", "RR invo " + param);
                    } else if (i == Constants.DATATYPE.Real_UploadBlood) {
                        heart = (int) hashMap.get("heartValue");//心率
                        tDBP = (int) hashMap.get("bloodDBP");//高压
                        tSBP = (int) hashMap.get("bloodSBP");//低压
                        if (heart != 0 && tDBP != 0 && tSBP != 0){
                            stopEcg();
                            System.out.println("xxxxxxxxxxxxxx "+heart+" "+tSBP+" "+tDBP);
                            insertHistoryToDB(auth.getUser_id(),tDBP,tSBP,heart);
                        }
                        //System.out.println("xxxxxxxxxxx: "+DBP_VALUE+", "+SBP_VALUE+", "+HEART_VALUE);
                    }
                }
            }
        });
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
            protected HashMap<String,String> getParams() throws AuthFailureError {
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

    private void stopEcg(){
        //detener ecg
        YCBTClient.appEcgTestEnd(new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {

            }
        });
    }

    //Datos de salud y de sueño
    private void getHistoryData(){
        JSON_HEALTH = "[\n";
        JSON_SLEEP = "[\n";
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
                            System.out.println("successfully health get data "+JSON_HEALTH);
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
                    }
                    if (JSON_HEALTH != "[\n"){
                        new SendJsonHhDataToServer().execute(JSON_HEALTH);
                        System.out.println("Historial de salud sincronizado");
                    }else{
                        System.out.println("JSON Vacio");
                    }
                }else{
                    System.out.println("No health data");
                }
            }
        });
    }

    private void getSleepData(){
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
                        }
                        if (JSON_SLEEP != "[\n"){
                            new SendJsonSleepDataToServer().execute(JSON_SLEEP);
                            System.out.println("Historial de sueño sincronizado");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("XXXXXXXXXXXXXXXXXXXXx no sleep data");
                }
            }
        });
    }

    public static void setAlarm(int i, Long timestamp, Context ctx) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, i, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
    }

    public static void cancelAlarm(int i, Context ctx) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, i, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        alarmManager.cancel(pendingIntent);
    }

    public static Long alarmTime(){
        Long horaAlarma = System.currentTimeMillis()+(3000);
        return horaAlarma;
    }
}
