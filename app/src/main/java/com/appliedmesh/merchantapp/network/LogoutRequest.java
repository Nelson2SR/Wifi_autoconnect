package com.appliedmesh.merchantapp.network;

import android.content.Context;
import android.content.Intent;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LogoutRequest extends BasePostRequest {
    Context context;

    public LogoutRequest(final Context context, final JsonObjectRequestCallback callback) {
        super(context, ServerConfigs.API_LOGOUT, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                Logger.d("response", response);
                try {
                    JSONObject reply = new JSONObject(response);
                    if (reply.has(ServerConfigs.RESPONSE_STATUS) && reply.getString(ServerConfigs.RESPONSE_STATUS).equals(ServerConfigs.RESPONSE_STATUS_SUCCESS)) {
                        callback.onRequestSuccess(null);
                    }else if (reply.has(ServerConfigs.RESPONSE_MESSAGE) && reply.getString(ServerConfigs.RESPONSE_MESSAGE).contains(ServerConfigs.RESPONSE_TOKEN_INVALID)){
                        Intent i = new Intent(Constants.ACTION_TOKEN_INVALID);
                        context.sendBroadcast(i);
                    }else
                        callback.onRequestFailed(null);
                }catch (JSONException e) {
                    e.printStackTrace();
                    callback.onRequestFailed(ServerConfigs.ERROR_UNKNOWN);
                }
            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Logger.e("error", error.getMessage());
                callback.onRequestFailed(error.getMessage());

            }
        });
        this.context = context;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String,String> params = new HashMap<String, String>();
        params.put("dummy", "dummy");
        return params;
    }
}
