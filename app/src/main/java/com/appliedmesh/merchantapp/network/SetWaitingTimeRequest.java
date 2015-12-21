package com.appliedmesh.merchantapp.network;

import android.content.Context;
import android.content.Intent;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.utils.Logger;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SetWaitingTimeRequest extends BasePostRequest {
	Context context;
	String mWaitingTime;
	String mOrderId;

	public SetWaitingTimeRequest(final Context context, String waitingTime, String orderId, final JsonObjectRequestCallback callback) {
		super(context, ServerConfigs.API_SET_WAITING_TIME, new Listener<String>() {

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

					if (error_message != null) {

					}
					callback.onRequestFailed(error_message);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		this.context = context;
		this.mWaitingTime = waitingTime;
		this.mOrderId = orderId;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Map<String,String> params = new HashMap<String, String>();
		params.put(ServerConfigs.PARAM_WAITING_TIME, mWaitingTime);
		params.put(ServerConfigs.PARAM_ORDER_ID, mOrderId);
		return params;
	}
}
