package com.appliedmesh.merchantapp.utils;

import android.content.Context;

/**
 * Created by Home on 2015/2/12.
 */
public class DisplayUtil {
    public static int dp2px(Context context, float dp){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }
}
