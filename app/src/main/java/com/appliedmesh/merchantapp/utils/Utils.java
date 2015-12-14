package com.appliedmesh.merchantapp.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Utils {
    private static String TAG = Utils.class.getName();
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();


    public static String getDeviceId(Context context){
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }


    public static String generateAuth(String params, String registrationId, String secreteKey, String date_){
        String randomString = getRandomString(6);
        String digest = hashWithMacSha512(params, randomString, date_, secreteKey);
        return registrationId +":"+ randomString +":["+ digest +"]";
    }


    public static String getRandomString(int len){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }


    public static String hashWithMacSha512(String param, String randomString, String date, String key) {
        try {
            return getHMACSHA512(param+randomString+date, key);
        } catch (SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


    public static String getHMACSHA512(String data, String key) throws java.security.SignatureException
    {
        String result;
        try {

            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(signingKey);


            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = bytesToHex(rawHmac);

            // base64-encode the hmac
            //result = Base64.encodeBase64String(rawHmac);

        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }


    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }






    public static String sha1(String password, String salt){
        StringBuilder result = new StringBuilder();
        try{
            SecretKeySpec sha1Key = new SecretKeySpec(salt.toLowerCase().getBytes("UTF-8"),
                    "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(sha1Key);
            byte[] sigBytes = mac.doFinal(password.getBytes("UTF-8"));

            for (int i = 0; i < sigBytes.length; i++) {
                result.append(Integer.toString((sigBytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
        }catch (Exception e){
            //Log.d("sha1 failed");
        }
        return result.toString();
    }


    public static boolean isValidPhoneNumber(Object phoneNumber){
        if(phoneNumber==null || phoneNumber.toString().trim().length() <= 0){
            return false;
        }
        if((phoneNumber.toString().substring(0, 1).equals("8") ||
                phoneNumber.toString().substring(0, 1).equals("9")) &&
                phoneNumber.toString().length() == 8){
            return true;
        }
        return false;
    }


    public static boolean isValidEmail(Object email){
        if(email==null || email.toString().trim().length() <= 0){
            return false;
        }
        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        return pattern.matcher(email.toString()).matches();
    }

    public static boolean isEmpty(Object str){
        if(str==null) return true;
        return str.toString().trim().length() <= 0;
    }

    public static boolean isContainNumber(String str){
        return (str.matches(".*\\d.*"));
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("\\d+(\\.\\d+)?");
    }



    public static int countUpperCase(String s){
        int caps = 0;
        for (int i=0; i<s.length(); i++){
            if (Character.isUpperCase(s.charAt(i)))
                caps++;
        }
        return caps;
    }

    public static int countNumeric(String s){
        int digits = 0;
        for (int i=0; i<s.length(); i++){
            if (Character.isDigit(s.charAt(i)))
                digits++;
        }
        return digits;
    }

    public static boolean isAlpha(String name) {
        return name.matches("[a-zA-Z]+");
    }


    public static String getRandomAlphaNum(int length){
        String alphanum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (length-- != 0) {
            int character = (int)(Math.random()*alphanum.length());
            builder.append(alphanum.charAt(character));
        }
        return builder.toString();

    }

    public static void hideKeyboard(Context context, View view){
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static void showToast(final Context context, String msg){
        Toast toast= Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 15);
        toast.show();
    }


    public static Bitmap getBitmap(Context c, String name)
    {
        try{
            AssetManager asset = c.getAssets();
            InputStream is = asset.open(name);
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            return bitmap;
        }catch(IOException e){

        }
        return null;
    }


    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String formatCreditCardNumber(String ccNumber){
        if(ccNumber!=null &&  ccNumber.length()>12){
            ccNumber = new StringBuilder(ccNumber).insert(4, " ").toString();
            ccNumber = new StringBuilder(ccNumber).insert(9, " ").toString();
            ccNumber = new StringBuilder(ccNumber).insert(14, " ").toString();
        }
        return ccNumber;
    }

    public static String formatCreditCardNumberForBilling(String ccNumber){
        if(ccNumber!=null && ccNumber.length()>12){
            return ccNumber.substring(0, 6) + "******" + ccNumber.substring(12);
        }
        return "";
    }

    public static String formatCreditCardMasked(String ccNumber){
        if(ccNumber!=null &&  ccNumber.length()>12){
            ccNumber = ccNumber.substring(0, 4) + "********" + ccNumber.substring(12);
            ccNumber = new StringBuilder(ccNumber).insert(4, " ").toString();
            ccNumber = new StringBuilder(ccNumber).insert(9, " ").toString();
            ccNumber = new StringBuilder(ccNumber).insert(14, " ").toString();



        }
        return ccNumber;
    }

    public static String formatExpiryDate(String expiryDate){
        if(expiryDate.length()==4)
            expiryDate = new StringBuilder(expiryDate).insert(2, "/").toString();
        return expiryDate;
    }


    public static String add(String val1, String val2){
        try{
            BigDecimal amount1 = new BigDecimal(val1);
            BigDecimal amount2 = new BigDecimal(val2);

            return amount1.add(amount2).toString();
        }catch(Exception e){
        }
        return "";
    }



    /**======================================
     *      Encryption
     *=======================================*/

    private static String validateKey(String key){
        if(key.length()==32) return key;
        if(key.length() > 32)
            key = key.substring(0, 32);
        else{
            for(int i=key.length(); i<32;i++){
                key += "0";
            }
        }
        return key;
    }

    public static String encrypt(String key, String text) {

        try {
            key = validateKey(key);
            byte[] vectorBytes = "ABCDEF1234567890".getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(vectorBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(text.getBytes());
            String encryptedString = Base64.encodeToString(encrypted, Base64.DEFAULT);
            Log.d(TAG, "Encrypted clear String: " + text);
            return encryptedString;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decrypt(String key, String cipherText)
    {
        try {
            key = validateKey(key);
            byte[] vectorBytes = "ABCDEF1234567890".getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(vectorBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT));
            String decryptedString =  new String(decrypted, "UTF-8");
            Log.d(TAG, "decryptedString String: "+decryptedString);
            return decryptedString;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
