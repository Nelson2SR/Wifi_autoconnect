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
import com.appliedmesh.merchantapp.utils.Logger;

public class SignUpRequest extends BasePostRequest {
	Context context;
	String mUserID;
	String mPassword;
	String mMerchantcode;
	String mStorecode;
	String mEmail;

	public SignUpRequest(final Context context, String userID, String password, String merchantcode,String storecode,String email, final JsonObjectRequestCallback callback) {
		super(context, ServerConfigs.API_SIGNUP, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				Logger.e("response", response);
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
		this.mPassword = password;
		this.mMerchantcode=merchantcode;
		this.mStorecode = storecode;
		this.mEmail = email;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Map<String,String> params = new HashMap<String, String>();
		if(mUserID==null)mUserID="";
		if(mPassword==null)mPassword="";
		if(mMerchantcode==null)mMerchantcode="";
		if(mStorecode==null)mStorecode="";
		if(mEmail==null)mEmail="";
		params.put("user-id", mUserID);
		params.put("passwd", mPassword);
		params.put("merchant-code", mMerchantcode);
		params.put("email", mEmail);
		params.put("store-code", mStorecode);
		return params;
	}

}