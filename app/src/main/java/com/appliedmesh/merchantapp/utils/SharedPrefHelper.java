package com.appliedmesh.merchantapp.utils;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {

	private static String FILE_NAME = "MERCHANT_DB";

	public static void deleteData(Context context, String key) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sf.edit();
		editor.remove(key);
		editor.commit();
	}

	public static void set(Context context, String key, String data) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sf.edit();
		editor.putString(key, data);
		editor.commit();
	}

	public static void set(Context context, String key, JSONObject data) {
		if (data == null) {
			return;
		}

		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sf.edit();
		editor.putString(key, data.toString());
		editor.commit();
	}

	public static void set(Context context, String key, boolean data) {

		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sf.edit();
		editor.putBoolean(key, data);
		editor.commit();
	}

	public static void set(Context context, String key, float data) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sf.edit();
		editor.putFloat(key, data);
		editor.commit();
	}

	public static void set(Context context, String key, int data) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sf.edit();
		editor.putInt(key, data);
		editor.commit();
	}

	public static void set(Context context, String key, long data) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sf.edit();
		editor.putLong(key, data);
		editor.commit();
	}

	public static String getString(Context context, String key) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		return sf.getString(key, "");
	}

	public static String getStringDefault(Context context, String key, String defaultVal) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		return sf.getString(key, defaultVal);
	}

	public static boolean getBoolean(Context context, String key) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		return sf.getBoolean(key, false);
	}

	public static float getFloat(Context context, String key) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		return sf.getFloat(key, -1);
	}

	public static int getInt(Context context, String key) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		return sf.getInt(key, -1);
	}

	public static long getLongData(Context context, String key) {
		final SharedPreferences sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		return sf.getLong(key, -1);
	}

}
