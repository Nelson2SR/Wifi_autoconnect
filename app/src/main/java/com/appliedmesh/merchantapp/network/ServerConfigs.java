package com.appliedmesh.merchantapp.network;

/**
 * Here hold the config for the application
 * 
 * 
 * 
 */
public class ServerConfigs {

	//host urls
	public static final String BASEURL_MYPOINT = "https://merchant.mypointofpurchase.com/";
//	public static final String BASEURL_MYPOINT = "https://merchantstg.mypointofpurchase.com/";
//	public static final String BASEURL_MYPOINT = "http://54.173.51.71:9090/";
	//api
	public static final String API_LOGIN = "api/v1/login";
	public static final String API_LOGOUT = "api/v1/logout";
	public static final String API_SET_WAITING_TIME = "api/v1/ack_order";
	public static final String API_GET_NEW_ORDERS = "api/v1/get_order_list";
	public static final String API_SIGNUP = "Api/signup/";
	public static final String API_GET_MOBILE_APP_INFO = "Api/get-mobile-app-info/";
	public static final String API_GET_STORE_SETTINGS = "Api/get-store-settings/";

	//params name
	public static final String PARAM_USER_ID = "user_id";
	public static final String PARAM_MERCHANT_CODE = "merchant_code";
	public static final String PARAM_STORE_CODE = "store_code";
	public static final String PARAM_WAITING_TIME = "waiting_time";
	public static final String PARAM_ORDER_ID = "order_id";
	public static final String PARAM_APP_NAME = "app_name";
	public static final String PARAM_APP_PLATFORM = "app_platform";

	//
	public static final String APP_NAME = "merchant-app";

	//response
	public static final String RESPONSE_STATUS = "status";
	public static final String RESPONSE_STATUS_SUCCESS = "success";
	public static final String RESPONSE_DATA = "data";
	public static final String RESPONSE_MESSAGE = "msg";
	public static final String RESPONSE_MOBILE_APP_VER = "ver";
	public static final String RESPONSE_MOBILE_APP_URL = "download-url";
	public static final String RESPONSE_TOKEN_INVALID = "Your login token is not valid";
	public static final String RESPONSE_OPENNG_HOUR = "opening-hour";
	public static final String RESPONSE_CLOSING_HOUR = "closing-hour";

	//predefine user-id/mercherant-code/store-code
	public static final String USER_ID = "othmanager";
	public static final String MERCHANT_CODE = "old-tea-hut";
	public static final String STORE_CODE = "mbfc";

    //token_key
    public static final String KEY_TOKEN="token";

	//url protocol
	public static final String HTTP_PROTOCOL_HTTPS = "https";

	//error message
	public static final String ERROR_UNKNOWN = "unknown error";

	//connection related
	public static final int TIMEOUT = 3000;
	public static final int BACKOFF_MULT = 2;
	public static final int MAX_RETRIES = 3;
}
