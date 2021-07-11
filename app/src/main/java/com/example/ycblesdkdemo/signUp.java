package com.example.ycblesdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;

public class signUp extends AppCompatActivity {

    private Button registro_bt;
    private TextView signup_tv;
    private EditText username_et;
    private EditText password_et;
    private EditText password_conf_et;
    private EditText fullname_et;
    private EditText email_et;
    private ProgressBar progressBar;
    JSONObject jsonObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        registro_bt = findViewById(R.id.signup_bt);
        signup_tv = findViewById(R.id.signup_tv);
        fullname_et = findViewById(R.id.fullname_et);
        email_et = findViewById(R.id.email_et);
        username_et = findViewById(R.id.username_et);
        password_et = findViewById(R.id.password_et);
        password_conf_et = findViewById(R.id.password_conf_et);
        progressBar = findViewById(R.id.progress_pb);

        registro_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullname = fullname_et.getText().toString();
                String email = email_et.getText().toString();
                String username = username_et.getText().toString();
                String password = password_et.getText().toString();
                String password_conf = password_conf_et.getText().toString();
                if (fullname.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()){
                    Toast.makeText(signUp.this, "Ingrese todos los campos"+username+" "+password, Toast.LENGTH_SHORT).show();
                }else{
                    if (password.equals(password_conf_et)){
                        progressBar.setVisibility(View.VISIBLE);
                        signup(fullname,username,email,password);
                    }else{
                        Toast.makeText(signUp.this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        signup_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signUp.this,logIn.class);
                startActivity(intent);
            }
        });
    }

    private void signup (final String fullname, final String username, final String email, final String password) {
        String url ="https://iot-medical.ml/signup.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest sringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Toast.makeText(signUp.this,s, Toast.LENGTH_SHORT).show();
                Log.d("response",s);
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(signUp.this,logIn.class));
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("error",""+volleyError);
                progressBar.setVisibility(View.GONE);
            }
        }){
            protected HashMap<String,String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("fullname",String.valueOf(fullname));
                map.put("email",String.valueOf(email));
                map.put("username",String.valueOf(username));
                map.put("password",String.valueOf(password));
                return map;
            }
        };
        requestQueue.add(sringRequest);
    }
}