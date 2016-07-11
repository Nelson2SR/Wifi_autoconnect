package com.appliedmesh.merchantapp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Home on 2015/4/4.
 */
public class MD5 {
    public static String getMD5(String value) throws NoSuchAlgorithmException{
        MessageDigest digester = MessageDigest.getInstance("MD5");
        digester.update(value.getBytes());
        byte[] digest = digester.digest();
        StringBuffer sb = new StringBuffer();
        for (byte b : digest) {
            if ((b & 0xFF) < 0x10) sb.append("0");
            sb.append(Integer.toHexString(b & 0xFF));
        }
        return sb.toString();
    }
}
