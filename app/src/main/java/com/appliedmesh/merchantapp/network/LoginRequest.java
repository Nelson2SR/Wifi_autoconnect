package com.appliedmesh.merchantapp.network;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.utils.Logger;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;
import com.appliedmesh.merchantapp.utils.Utilities;
import com.appliedmesh.merchantapp.utils.Utils;

public class LoginRequest extends BasePostRequest {
	Context context;
	String mUserID;
	String mPassword;
	String mMerchantcode;

	public LoginRequest(final Context context, String userID, String password, String merchantcode, final JsonObjectRequestCallback callback) {
		super(context, ServerConfigs.API_LOGIN, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				Logger.d("response", response);
				try {
					JSONObject reply = new JSONObject(response);
					if (reply.has("status") && reply.getString("status").equals("success")) {
						callback.onRequestSuccess(reply.getJSONObject("data"));
					}else
						callback.onRequestFailed(response);
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
		this.mUserID = userID;
		this.mPassword = Utils.bytesToHex(Utils.getSha512(password));
		this.mMerchantcode=merchantcode;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Map<String,String> params = new HashMap<String, String>();
		if(mUserID==null)mUserID="";
		if(mPassword==null)mPassword="";
		if(mMerchantcode==null)mMerchantcode="";
		params.put("username", mUserID);
		params.put("password", mPassword);
		params.put("merchant-code", mMerchantcode);
		return params;
	}
}