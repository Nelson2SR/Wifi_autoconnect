package com.appliedmesh.merchantapp.module;

import android.content.Context;
import android.content.SharedPreferences;
import com.appliedmesh.merchantapp.utils.MD5;

/**
 * Created by Home on 2015/2/6.
 */
public class RemoteServer {
    private static final String TAG = "RemoteServer";
    public static final String HOST = "http://am-merchant.dugeit.net/";
    public static final String PROTOCOL_GET_RECEIPT_URL = "merchant-get-receipt-url";
    public static final String RESPONSE_STATUS = "status";
    public static final String RESPONSE_RECEIPTS_URL = "receipt-url";
    public static final String RESPONSE_ERROR_MSG = "error_msg";
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private static RemoteServer sRS;

    private RemoteServer(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFENCE_NAME, mContext.MODE_PRIVATE);
    }

    public static RemoteServer getInstance(Context context) {
        if (sRS == null) {
            sRS = new RemoteServer(context);
        }
        return sRS;
    }

    public String genRequestReceiptUrl(String receiptNum) {
        if (receiptNum == null || receiptNum.equals(""))
            return null;

        String url = null;
        try {
            String merchant_id = mSharedPreferences.getString(Constants.NAME_MERCHANT_ID, "");
            String security_key = mSharedPreferences.getString(Constants.NAME_SECURITY_KEY, "");
            url = HOST + PROTOCOL_GET_RECEIPT_URL
                    + "/" + merchant_id
                    + "/" + MD5.getMD5(security_key + merchant_id)
                    + "/" + System.currentTimeMillis()/1000
                    + "/" + MD5.getMD5(security_key + System.currentTimeMillis()/1000)
                    + "/" + receiptNum
                    + "/" + MD5.getMD5(security_key + receiptNum);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }
}