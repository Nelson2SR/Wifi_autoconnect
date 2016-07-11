package com.appliedmesh.merchantapp.view;

import android.app.Application;
import com.appliedmesh.merchantapp.module.OrderManager;
import com.appliedmesh.merchantapp.utils.Volley;

/**
 * Created by dongbin on 2015/6/12.
 */
public class MerchantAppOth extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Volley.init(this);
        OrderManager.getInstance().init(this);
    }
}
