package com.appliedmesh.merchantapp.utils;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class Volley {
	private static RequestQueue mQueue;
	public static final String TAG = "VolleyPatterns";
	static Volley instance;

	private Volley() {
		// no instances
	}

	public static Volley getInstance() {
		if (instance == null) {
			synchronized (Volley.class) {
				if (instance == null) {
					instance = new Volley();
				}
			}
		}

		return instance;
	}

	public static void init(Context context) {
		mQueue = com.android.volley.toolbox.Volley.newRequestQueue(context.getApplicationContext(), new HurlStack(null, newSslSocketFactory()));
//		mQueue = com.android.volley.toolbox.Volley.newRequestQueue(context);
	}

	public static RequestQueue getRequestQueue() {
		if (mQueue != null) {
			return mQueue;
		} else {
			throw new IllegalStateException("RequestQueue not initialized");
		}
	}

	private boolean isHttps(String url) {
		//Trust all sll if using https
		try {
			URL httpurl = new URL(url);
			if (httpurl.getProtocol().equals("https")) {
				return true;
			}
		}catch (MalformedURLException e){
			e.printStackTrace();
		}
		return false;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		// set the default tag if tag is empty
		req.setTag(TAG);

		getRequestQueue().add(req);
	}

	/**
	 * Cancels all pending requests by the specified TAG, it is important to
	 * specify a TAG so that the pending/ongoing requests can be cancelled.
	 * 
	 * @param tag
	 */
	public void cancelPendingRequests(Object tag) {
		if (mQueue != null) {
			mQueue.cancelAll(tag);
		}
	}

	private static SSLSocketFactory newSslSocketFactory() {
		TrustManager tm[] = {new PubKeyManager()};
		assert (null != tm);

		SSLContext context = null;
		try {
			context = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		assert (null != context);
		try {
			context.init(null, tm, null);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return context.getSocketFactory();
	}

}
