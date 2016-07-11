package com.appliedmesh.merchantapp.view;

import android.content.BroadcastReceiver;  
import android.content.Context;  
import android.content.Intent;

import com.appliedmesh.merchantapp.module.Constants;

public class MerchantBroadcastReceiver extends BroadcastReceiver {
    @Override  
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.ACTION_TOKEN_INVALID)) {
            Intent i = new Intent(context, ActivityLoginOption.class);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
  
} 