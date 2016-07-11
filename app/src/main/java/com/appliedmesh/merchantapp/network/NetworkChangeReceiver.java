package com.appliedmesh.merchantapp.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.appliedmesh.merchantapp.view.MainActivity;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public boolean onConnection1;
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (activeNetwork != null) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if(isWiFi) {
                Log.d("Test Connection1","I have connected");
                onConnection1 = true;
                Intent wifiReconnect = new Intent(context,MainActivity.class);
                wifiReconnect.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                // wifiReconnect.putExtra("wifiReconnect",true);

                context.startActivity(wifiReconnect);

            } else {
                Log.d("Test Connection1", "I have not connected");
                onConnection1 = false;
            }
        }


    }
}
