package com.example.adminfoodapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.example.adminfoodapp.utils.NetworkChangeReceiver;
import com.example.adminfoodapp.utils.NetworkUtils;

public abstract class BaseNetworkActivity extends AppCompatActivity {
    private NetworkChangeReceiver networkChangeReceiver;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Vui lòng chờ...");
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!NetworkUtils.isNetworkConnected(this)) {
            navigateToDisconnected();
            return;
        }
        setupNetworkReceiver();
    }

    private void setupNetworkReceiver() {
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    protected void navigateToDisconnected() {
        Intent intent = new Intent(this, DisconnectedActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    protected void showProgressDialog(String message) {
        if (progressDialog != null) {
            progressDialog.setMessage(message);
            if (!progressDialog.isShowing() && !isFinishing()) {
                progressDialog.show();
            }
        }
    }

    protected void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}