package com.appliedmesh.merchantapp.network;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.appliedmesh.merchantapp.module.Constants;
import com.appliedmesh.merchantapp.utils.SharedPrefHelper;
import com.appliedmesh.merchantapp.utils.Utilities;
import com.appliedmesh.merchantapp.utils.Utils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class BasePostRequest extends BaseRequest {
	private Context ctx;


	/**
	 * Instantiates a new base get request.
	 *
	 * @param context
	 *            the context
	 * @param url
	 *            the url
	 * @param listener
	 *            the listener
	 * @param errorListener
	 *            the error listener
	 */
	protected BasePostRequest(final Context context, String url, Listener<String> listener, ErrorListener errorListener) {
		super(context, Method.POST, SharedPrefHelper.getStringDefault(context, Constants.NAME_SERVER_URL, ServerConfigs.BASEURL_MYPOINT) + url,
				listener, errorListener);

		ctx = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.android.volley.Request#getHeaders()
	 */
	/**
	 * Gets the headers.
	 *
	 * @return the headers
	 * @throws AuthFailureError
	 *             the auth failure error
	 */
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = new HashMap<String, String>();
		/*
		 * Obtain the registration_id, then sha512 it to form a HMAC
		 */
		String regIdEncrypted = SharedPrefHelper.getString(mContext.get(), Constants.REGISTRATION_ID);
		String regSecretEncrypted = SharedPrefHelper.getString(mContext.get(), Constants.REGISTRATION_SECRET);
		/*
		 * If either the reg ID (encrypted) string or the reg Secret (encrypted) string is empty
		 * It will mean user is not authenticated previously, so no need to continue
		 */
		if (regSecretEncrypted.trim().length() == 0 || regSecretEncrypted.trim().length() == 0) {
			return headers;	// we return an empty header
		}
		String registrationId = Utils.decrypt(Utils.getDeviceId(mContext.get()), regIdEncrypted);
		String registrationSecret = Utils.decrypt(Utils.getDeviceId(mContext.get()), regSecretEncrypted);
		Date curDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		format.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
		String date_ = format.format(curDate);

		String token = "hmac " + Utils.generateAuth(get_http_params_in_json(), registrationId, registrationSecret, date_);
			/*
			 * Additional headers as required by Bo' server
			 */
		headers.put("Content-Type", "application/json");
		headers.put("Authentication", token);
		headers.put("Date", date_);

		return headers;
	}

	public void assertLoggedIn() {
		String regIdEncrypted = SharedPrefHelper.getString(mContext.get(), Constants.REGISTRATION_ID);
		/*
		 * If we cannot find any reg. ID and reg. secret, it may mean we are either not authenticated
		 * or has been locked out due to another login session elsewhere
		 */
		if (regIdEncrypted == null || regIdEncrypted.trim() == "") {
			Intent i = new Intent(Constants.ACTION_TOKEN_INVALID);
			mContext.get().sendBroadcast(i);
		}
			return;
	}

	protected void logout() {
		SharedPrefHelper.set(mContext.get(), Constants.REGISTRATION_ID, "");
		SharedPrefHelper.set(mContext.get(), Constants.REGISTRATION_SECRET, "");
	}

	protected int getVersion() {
		try {
			PackageManager manager = mContext.get().getPackageManager();
			PackageInfo info = manager.getPackageInfo(mContext.get().getPackageName(), 0);
			return info.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		    return 0;
		}
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		return get_http_params_in_json().getBytes();
	}

	protected String get_http_params_in_json() {
		try {
			JSONObject params_json = new JSONObject(getParams());
			String params = params_json.toString();

			return params;
		}
		catch (AuthFailureError e) {
			e.printStackTrace();
		}

		return "{}";
	}
}