package com.appliedmesh.merchantapp.network;

import java.util.Map;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.appliedmesh.merchantapp.utils.Utilities;

class BaseGetRequest extends BaseRequest {

	/**
	 * Instantiates a new base get request.
	 *
	 * @param context
	 *            the context
	 * @param method
	 *            the method
	 * @param url
	 *            the url
	 * @param listener
	 *            the listener
	 * @param errorListener
	 *            the error listener
	 */
	protected BaseGetRequest(final Context context, int method, String url, Listener<String> listener, ErrorListener errorListener) {
		super(context, method, url, listener, errorListener);

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
	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return Utilities.getGeneralRequestHeader(mContext);
	}
}