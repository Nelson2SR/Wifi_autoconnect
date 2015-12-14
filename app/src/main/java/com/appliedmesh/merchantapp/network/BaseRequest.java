package com.appliedmesh.merchantapp.network;

import java.lang.ref.WeakReference;
import java.util.Map;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.appliedmesh.merchantapp.utils.Logger;
import com.appliedmesh.merchantapp.utils.Utilities;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseRequest.
 */
public abstract class BaseRequest extends StringRequest {

	/** The m context. */
	protected WeakReference<Context> mContext;

	/**
	 * Instantiates a new base request.
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
	protected BaseRequest(final Context context, int method, String url,
			Listener<String> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
		Logger.d("Request URL", url);

		setRetryPolicy(new DefaultRetryPolicy(ServerConfigs.TIMEOUT,
				ServerConfigs.MAX_RETRIES, ServerConfigs.BACKOFF_MULT));
		this.mContext = new WeakReference<Context>(context);

	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return Utilities.getGeneralRequestHeader(mContext);
	}
}
