package com.example.ycblesdkdemo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ycblesdkdemo.configs.Auth;
import com.example.ycblesdkdemo.configs.Connection;
import com.yucheng.ycbtsdk.AITools;
import com.yucheng.ycbtsdk.Constants;
import com.yucheng.ycbtsdk.Response.BleConnectResponse;
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

import static com.example.ycblesdkdemo.MyApplication.CHANNEL_ID;

public class AlarmReceiver extends BroadcastReceiver {

    private Connection con;
    private NotificationManagerCompat notificationManagerCompat;
    int monitor_mins = 2;
    private Auth auth;
    private int tDBP = 0, tSBP = 0, heart = 0;
    private String JSON_HEALTH = "[\n";
    private String JSON_SLEEP = "[\n";
    private String JSON_HR = "[\n";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.auth = new Auth(context);
        con = new Connection();
        notificationManagerCompat = NotificationManagerCompat.from(context);

        Log.d("Alarm manager", " ALARM RECEIVED!!!");
        int bleState = YCBTClient.connectState();
        System.out.println("xxxxxxxxxxxxxxxx blestate: "+bleState);

        if (con.checkConnection(context)){
            if (bleState == 10){ //connected success
                //Datos de salud

                getHistoryData(context);
                getSleepData();
                //ecgTest(context);
                //sendOnChannel1(context,"Escaner realizado","Sus variables de salud se han actualizado");

            }else{
                conDevice(context, auth.getDevice_mac(), auth.getDeviece_name());
            }
        }else{
            System.out.println("No conexión internet");
            sendOnChannelHigh(context,"No hay conexión a internet","No se pudó sincronizar sus datos");
        }
        ExampleService.setAlarm(1,System.currentTimeMillis()+(monitor_mins*60000), context.getApplicationContext());
    }

    public void sendOnChannelLow(Context context, String title, String text){
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_favorite_24)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setNotificationSilent()
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        notificationManagerCompat.notify(1,notification);
    }
    public void sendOnChannelHigh(Context context,String title,String text){
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_favorite_24)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        notificationManagerCompat.notify(1,notification);
    }

    private void conDevice(Context context, String deviceMac, String deviceName){
        YCBTClient.connectBle(deviceMac, new BleConnectResponse() {
            @Override
            public void onConnectResponse(final int i) {

                if (i == Constants.CODE.Code_OK){
                    sendMessage("Notificación","Dispositivo reconectado");
                    ExampleService.cancelAlarm(1, context);
                    ExampleService.setAlarm(1,System.currentTimeMillis()+(5000), context.getApplicationContext());
                }
                else if (i == Constants.CODE.Code_Failed){
                    System.out.println("No conectado");
                    sendOnChannelHigh(context,"No hay conexión con la manilla","Dispositivo "+auth.getDeviece_name());
                    ExampleService.cancelAlarm(1, context);
                    ExampleService.setAlarm(1,System.currentTimeMillis()+(60000), context.getApplicationContext());
                }
            }
        });
        /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 7000);*/
    }

    private void sendMessage(String title, String msg){
        YCBTClient.appSengMessageToDevice(0x01, title, msg, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {

            }
        });
    }

    public void ecgTest(Context context){
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
                            insertHistoryToDB(context,auth.getUser_id(),tDBP,tSBP,heart);
                        }
                        //System.out.println("xxxxxxxxxxx: "+DBP_VALUE+", "+SBP_VALUE+", "+HEART_VALUE);
                    }
                }else{
                    stopEcg();
                    System.out.println("xxxxxxxxxxxx no se puede acceder al ecg");
                }
            }
        });
    }

    private void insertHistoryToDB(Context context, final int user_id, final int dbp, final int sbp, final int hr) {
        String url ="https://iot-medical.ml/insertHistory.php";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
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

    private void getHistoryData(Context context){
        JSON_HEALTH = "[\n";
        JSON_HR = "[\n";
        JSON_SLEEP = "[\n";
        //String dateString = DateFormat.format("yyyy-MM-dd k:mm:ss", new Date(System.currentTimeMillis())).toString();
        YCBTClient.healthHistoryData(0x0509, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                if (hashMap != null) {
                    ArrayList<HashMap> lists = (ArrayList) hashMap.get("data");
                    int s = lists.size();
                    //System.out.println("xxxxxx lists: "+lists);
                    int count = 0;
                    for (HashMap map : lists) {
                        count++;
                        int DBP = (int) map.get("DBPValue");
                        int SBP = (int) map.get("SBPValue");
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
                                    "    \"sbp\": "+DBP+",  \n" +
                                    "    \"dbp\": "+SBP+",  \n" +
                                    "    \"hrv\": "+hrv+",  \n" +
                                    "    \"cvrr\": "+cvrr+",  \n" +
                                    "    \"resp_rate\": "+respiratoryRateValue+"  \n" +
                                    "  }\n]";
                            ExampleService.settDBP(DBP);
                            ExampleService.settSBP(SBP);
                            ExampleService.setFecha_presion(time);
                            sendOnChannelLow(context,"Última revisión: "+time,"Presión "+DBP+"/"+SBP+" mmHg, Oxigeno "+blood_oxygen+"%, Temp. "+temp+"°C");
                        }else{
                            JSON_HEALTH += "{  \n" +
                                    "    \"user_id\": "+auth.getUser_id()+",  \n" +
                                    "    \"date\": \""+time+"\",  \n" +
                                    "    \"oxygen\": "+blood_oxygen+",  \n" +
                                    "    \"temperature\": "+temp+",  \n" +
                                    "    \"sbp\": "+DBP+",  \n" +
                                    "    \"dbp\": "+SBP+",  \n" +
                                    "    \"hrv\": "+hrv+",  \n" +
                                    "    \"cvrr\": "+cvrr+",  \n" +
                                    "    \"resp_rate\": "+respiratoryRateValue+"  \n" +
                                    "  },\n";
                        }
                    }
                    //System.out.println("successfully health get data "+JSON_HEALTH);
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
        //health history HR
        YCBTClient.healthHistoryData(0x0506, new BleDataResponse() {
            @Override
            public void onDataResponse(int i, float v, HashMap hashMap) {
                if (hashMap != null) {
                    ArrayList<HashMap> lists = (ArrayList) hashMap.get("data");
                    int s = lists.size();
                    //System.out.println("xxxxxx lists HR: "+lists);
                    int count = 0;
                    for (HashMap map : lists) {
                        count++;
                        int HR = (int) map.get("heartValue");
                        long startTime = (long) map.get("heartStartTime");
                        @SuppressLint("SimpleDateFormat") String time;
                        time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime));
                        if (count == s){
                            JSON_HR += "{  \n" +
                                    "    \"user_id\": "+auth.getUser_id()+",  \n" +
                                    "    \"date\": \""+time+"\",  \n" +
                                    "    \"hr\": "+HR+"  \n" +
                                    "  }\n]";
                            ExampleService.setHeart(HR);
                            System.out.println("Último valor de HR: Fecha"+time+" HR: "+HR);
                        }else{
                            JSON_HR += "{  \n" +
                                    "    \"user_id\": "+auth.getUser_id()+",  \n" +
                                    "    \"date\": \""+time+"\",  \n" +
                                    "    \"hr\": "+HR+"  \n" +
                                    "  },\n";
                        }
                    }
                    if (JSON_HR != "[\n"){
                        new SendJsonHRDataToServer().execute(JSON_HR);
                        System.out.println("Historial de salud HR sincronizado");
                    }else{
                        System.out.println("JSON HR vacio");
                    }
                }else{
                    System.out.println("No health data HR");
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
                                System.out.println("xxxxxxxxxxxxx sueño: "+JSON_SLEEP);
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
}
