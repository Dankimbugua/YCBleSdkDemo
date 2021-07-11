package com.example.ycblesdkdemo;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ycblesdkdemo.configs.Auth;

public class webView extends AppCompatActivity {
    private WebView webView;
    private Auth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.metabase_wv);

        this.auth = new Auth(this);
        Integer user_id = auth.getUser_id();
        String URL = "http://pic-metabase.sa-east-1.elasticbeanstalk.com/dashboard/2?user_id="+user_id;

        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl(URL);
    }
}