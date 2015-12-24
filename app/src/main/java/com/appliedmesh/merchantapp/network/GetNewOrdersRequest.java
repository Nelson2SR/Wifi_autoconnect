package com.appliedmesh.merchantapp.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.utils.Logger;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;
import com.appliedmesh.merchantapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GetNewOrdersRequest extends BasePostRequest {

	public GetNewOrdersRequest(final Context context, final JsonObjectRequestCallback callback) {
		super(context, ServerConfigs.API_GET_NEW_ORDERS, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				Logger.d("response", response);
				try {
					JSONObject reply = new JSONObject(response);
					if (reply.has(ServerConfigs.RESPONSE_STATUS) && reply.getString(ServerConfigs.RESPONSE_STATUS).equals(ServerConfigs.RESPONSE_STATUS_SUCCESS)) {
						callback.onRequestSuccess(reply.getJSONObject(ServerConfigs.RESPONSE_DATA));
					}else if (reply.has(ServerConfigs.RESPONSE_MESSAGE) && reply.getString(ServerConfigs.RESPONSE_MESSAGE).contains(ServerConfigs.RESPONSE_TOKEN_INVALID)){
						SharedPrefHelper.set(context,Constants.REGISTRATION_ID, "");
                        Intent i = new Intent(Constants.ACTION_TOKEN_INVALID);
                        context.sendBroadcast(i);
					}else
						callback.onRequestFailed(reply.getString(ServerConfigs.RESPONSE_MESSAGE));
				}catch (JSONException e) {
					e.printStackTrace();
					callback.onRequestFailed(ServerConfigs.ERROR_UNKNOWN);
				}
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				try {
					String error_json_string = new String(error.networkResponse.data);
					JSONObject error_json = new JSONObject(error_json_string);
					String error_message = error_json.getString("message");

					if (error_message.equalsIgnoreCase("authentication failed.")) {
						SharedPrefHelper.set(context, Constants.REGISTRATION_ID, "");
						SharedPrefHelper.set(context, Constants.REGISTRATION_SECRET, "");

						Intent i = new Intent(Constants.ACTION_TOKEN_INVALID);
						context.sendBroadcast(i);
					}
					else {
						callback.onRequestFailed(error_message);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Map<String, String> params_map = new HashMap<String, String>();

		params_map.put("page", "0");
		params_map.put("page_size", "10");

		return params_map;
	}
}
