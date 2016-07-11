package com.appliedmesh.merchantapp.network;

import android.content.Context;

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

public class GetStoreSettingsRequest extends BasePostRequest {

	public GetStoreSettingsRequest(final Context context, final JsonObjectRequestCallback callback) {
		super(context,  ServerConfigs.API_GET_STORE_SETTINGS, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				Logger.d("response", response);
				try {
					JSONObject reply = new JSONObject(response);
					if (reply.has(ServerConfigs.RESPONSE_STATUS) && reply.getString(ServerConfigs.RESPONSE_STATUS).equals(ServerConfigs.RESPONSE_STATUS_SUCCESS)) {
						callback.onRequestSuccess(reply.getJSONObject(ServerConfigs.RESPONSE_DATA));
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
				Logger.e("error", error.getMessage());
				callback.onRequestFailed(error.getMessage());
			}
		});
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Map<String,String> params = new HashMap<String, String>();

		String username = SharedPrefHelper.getString(mContext.get(), Constants.NAME_USERNAME);
		params.put(ServerConfigs.PARAM_USER_ID, username);
		params.put(ServerConfigs.PARAM_MERCHANT_CODE, ServerConfigs.MERCHANT_CODE);
		params.put(ServerConfigs.PARAM_STORE_CODE, ServerConfigs.STORE_CODE);
		return params;
	}

}
