package com.example.ycblesdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class preparation extends AppCompatActivity {
    
    private FloatingActionButton floatingActionButton;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preparation);
        
        floatingActionButton = findViewById(R.id.floatingActionButton);
        editText = findViewById(R.id.bpm_et);

    }

    public void bpm(View view) {

        if (!editText.getText().toString().isEmpty()){
            Intent i = new Intent(preparation.this,MainActivity.class);
            startActivity(i);
            finish();
        }else{
            Toast.makeText(this, "Ingrese el valor de BPM en su brazalete", Toast.LENGTH_LONG).show();
        }
    }
}