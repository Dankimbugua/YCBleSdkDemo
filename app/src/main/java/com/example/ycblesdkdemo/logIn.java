package com.example.ycblesdkdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ycblesdkdemo.configs.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class logIn extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Auth auth;
    private Button acceso_bt;
    private Button tosignup_bt;
    private EditText username_et;
    private EditText password_et;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.auth = new Auth(getApplicationContext());

        acceso_bt = findViewById(R.id.login_bt);
        tosignup_bt = findViewById(R.id.tosignup_bt);
        username_et = findViewById(R.id.username_et);
        password_et = findViewById(R.id.password_et);
        progressBar = findViewById(R.id.progress_pb);

        if (!auth.getUsername().isEmpty()){
            startActivity(new Intent(logIn.this,MainActivity.class));
            finish();
        }

        acceso_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String username = username_et.getText().toString();
                String password = password_et.getText().toString();
                if (!username.isEmpty() && !password.isEmpty()){
                    login(username,password);
                }else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(logIn.this, "Llene todos los campos", Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(logIn.this, "Hola! "+username+" "+password, Toast.LENGTH_SHORT).show();
            }
        });

        tosignup_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(logIn.this,signUp.class);
                startActivity(intent);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void login (final String username, final String password) {
        String url = "https://iot-medical.ml/login2.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest sringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("xxxxxx response "+response);
                if (!response.equalsIgnoreCase("Username or Password wrong")){
                    try {
                        JSONObject jsonObject = new JSONObject(response).optJSONObject("user");
                        if(jsonObject != null) {
                            sharedPreferences = getSharedPreferences("auth", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("user_id", Integer.parseInt(jsonObject.getString("user_id")));
                            editor.putString("fullname",jsonObject.getString("fullname"));
                            editor.putString("username",jsonObject.getString("username"));
                            editor.putString("email",jsonObject.getString("email"));
                            editor.commit();
                            //progressBar.setVisibility(View.GONE);
                            //Toast.makeText(getApplicationContext(), "¡Bienvenido! "+new Auth(getApplicationContext()).getUsername(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(logIn.this,MainActivity.class));
                            finish();
                        } else {
                            //progressBar.setVisibility(View.INVISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Acceso denegado", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Usuario o contraseña incorrecta", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.GONE);
                Log.d("error",""+volleyError);
            }
        }){
            protected HashMap<String,String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("username",String.valueOf(username));
                map.put("password",String.valueOf(password));
                return map;
            }
        };
        requestQueue.add(sringRequest);
    }
}