package com.appliedmesh.merchantapp.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

public class Utilities {
	// This is for getting api request header
	public static Map<String, String> getGeneralRequestHeader(WeakReference<Context> mContext) {
		Map<String, String> headers = new HashMap<String, String>();
		return headers;
	}

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	public static boolean isEmailValid(String email) {
		boolean isValid = false;

		if (!TextUtils.isEmpty(email)) {
			CharSequence inputStr = email;
			Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(inputStr);
			if (matcher.matches()) {
				isValid = true;
			}
		}
		return isValid;
	}

	public static String serialize(Serializable obj) {
		if (obj == null)
			return "";
		try {
			ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
			ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
			objStream.writeObject(obj);
			objStream.close();
			return encodeBytes(serialObj.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String formatCurrency(float price) {
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		String pattern = ((DecimalFormat) nf).toPattern();
		String newPattern = pattern.replace("\u00A4", "").trim();
		NumberFormat newFormat = new DecimalFormat(newPattern);

		return "$" + newFormat.format(price);
	}

	public static String formatCurrency(String price) {
		if (TextUtils.isEmpty(price))
			return "";
		price = price.replace("$", "");
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		String pattern = ((DecimalFormat) nf).toPattern();
		String newPattern = pattern.replace("\u00A4", "").trim();
		NumberFormat newFormat = new DecimalFormat(newPattern);

		return "$" + newFormat.format(Float.parseFloat(price));
	}

	public static Object deserialize(String str) {
		if (str == null || str.length() == 0)
			return null;
		try {
			ByteArrayInputStream serialObj = new ByteArrayInputStream(decodeBytes(str));
			ObjectInputStream objStream = new ObjectInputStream(serialObj);
			return objStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String encodeBytes(byte[] bytes) {
		StringBuffer strBuf = new StringBuffer();

		for (int i = 0; i < bytes.length; i++) {
			strBuf.append((char) (((bytes[i] >> 4) & 0xF) + ('a')));
			strBuf.append((char) (((bytes[i]) & 0xF) + ('a')));
		}

		return strBuf.toString();
	}

	public static byte[] decodeBytes(String str) {
		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < str.length(); i += 2) {
			char c = str.charAt(i);
			bytes[i / 2] = (byte) ((c - 'a') << 4);
			c = str.charAt(i + 1);
			bytes[i / 2] += (c - 'a');
		}
		return bytes;
	}

	public static String readTextFileFromAssets(final Context context, String fileName) {
		final AssetManager am = context.getAssets();
		StringBuilder total = new StringBuilder();
		try {
			final InputStream is = am.open(fileName);
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
		return total.toString();
	}
}
