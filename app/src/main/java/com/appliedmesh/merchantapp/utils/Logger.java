package com.appliedmesh.merchantapp.utils;

import android.util.Log;

public class Logger {

	static boolean enableLOG = true;

	public static void i(String a, String b) {
		if (enableLOG)
			Log.i(a, buildString(b));
	}

	public static void i(String a) {
		if (enableLOG)
			Log.i("LOG", buildString(a));
	}

	public static void w(String a) {
		if (enableLOG)
			Log.i("LOG", buildString(a));
	}

	public static void w(String a, String b) {
		if (enableLOG)
			Log.e(a, buildString(b));
	}

	public static void e(String a, String b) {
		if (enableLOG)
			Log.e(a, buildString(b));
	}

	public static void e(String a, boolean b) {
		if (enableLOG)
			Log.e(a, buildString(b + ""));
	}

	public static void e(String a, int b) {
		if (enableLOG)
			Log.e(a, buildString(b));
	}

	public static void d(String a, String b) {
		if (enableLOG)
			Log.d(a, buildString(b));
	}

	public static void d(String a, long b) {
		if (enableLOG)
			Log.d(a, buildString(b));
	}

	public static void e(String a) {
		if (enableLOG)
			Log.e("FITNESS_LOG", buildString(a));
	}

	public static void e(int a) {
		if (enableLOG)
			Log.e("FITNESS_LOG", buildString(a));
	}

	private static String buildString(Object... strings) {
		StringBuilder sb = new StringBuilder();
		if (strings != null) {
			for (Object string : strings) {
				sb.append(string);
			}
		}
		return sb.toString();
	}
}
