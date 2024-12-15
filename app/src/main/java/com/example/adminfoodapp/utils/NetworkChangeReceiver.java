package com.example.adminfoodapp.utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;
import com.example.foodorderingapp.utils.NetworkUtils;

import com.example.adminfoodapp.DisconnectedActivity;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = NetworkUtils.isNetworkConnected(context);
        if (!isConnected)
        {
            Toast.makeText(context, "Mất kết nối mạng!, Vui lòng kiểm tra lại kết nối.", Toast.LENGTH_SHORT).show();
            Intent disconnectedIntent = new Intent(context, DisconnectedActivity.class);
            disconnectedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(disconnectedIntent);
        }
    }
}
