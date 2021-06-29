package com.example.ycblesdkdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class EcgDialog {
    Activity activity;
    AlertDialog dialog;

    EcgDialog(Activity myActivity){
        activity = myActivity;
    }

    void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.ecg_dialog,null));
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }

    void dismissDialog(){
        dialog.dismiss();
    }
}
