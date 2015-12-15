package com.appliedmesh.merchantapp.utils;

import android.util.Base64;
import android.util.Log;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

// Many thanks to Nikolay Elenkov for feedback.
// Shamelessly based upon Moxie's example code (AOSP/Google did not offer code)
// http://www.thoughtcrime.org/blog/authenticity-is-broken-in-ssl-but-your-app-ha/
// Fixed the problem with encoding by using solution in StackOverFlow:
// http://stackoverflow.com/questions/28667575/pinning-public-key-in-my-app
public final class PubKeyManager implements X509TrustManager {

    private static final String TAG = "PubKeyManager";

    // DER encoded public key
    private static String PUB_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo1AYeAnUs7DCowAt8p+FV/729ahW5IRj85HgrZ0a/m/oY17Nfao/633sfiTi5gcsLfQSmYDnadYKaYEbxhWfFXzJZBcL8t1wPzZKkQAKWIAEmnvmN6tLgGTdPxwaz72yl7EbwzshRpNFqSeuXN8RuBlI+vZXGi40mdqf3pNUoWtoFC09NWrM/Iy6u2dsbrtRmkT7iuEjvzbA5IbTR8xlMC3g7u+AJUfh0mpoIxJylZYzURmDTGFakTnQR56GnBOrUvmXzxzO20ABMcJLaGO1goNjF+XisgF0rmi9H0H18P4ATDRQIcSbkk4aAE5Xbx6c0Sd7K2GIVFD9W+e6VMrJ9wIDAQAB";

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

        assert (chain != null);
        if (chain == null) {
            throw new IllegalArgumentException(
                    "checkServerTrusted: X509Certificate array is null");
        }

        assert (chain.length > 0);
        if (!(chain.length > 0)) {
            throw new IllegalArgumentException(
                    "checkServerTrusted: X509Certificate is empty");
        }

        assert (null != authType && authType.equalsIgnoreCase("ECDHE_RSA"));
        if (!(null != authType && authType.equalsIgnoreCase("ECDHE_RSA"))) {
            throw new CertificateException(
                    "checkServerTrusted: AuthType is not ECDHE_RSA");
        }

        // Perform customary SSL/TLS checks
        TrustManagerFactory tmf;
        try {
            tmf = TrustManagerFactory.getInstance("X509");
            tmf.init((KeyStore) null);

            for (TrustManager trustManager : tmf.getTrustManagers()) {
                ((X509TrustManager) trustManager).checkServerTrusted(
                        chain, authType);
            }

        } catch (Exception e) {
            throw new CertificateException(e);
        }

        RSAPublicKey pubkey = (RSAPublicKey) chain[0].getPublicKey();

        // Solution found in
        // http://stackoverflow.com/questions/28667575/pinning-public-key-in-my-app
        String base64Encoded = Base64.encodeToString(pubkey.getEncoded(), Base64.DEFAULT)
                .replace("\n", "");

        // Pin it!
        final boolean expected = PUB_KEY.equalsIgnoreCase(base64Encoded);
        Log.v(TAG, "got encoded key as " + base64Encoded);

        assert(expected);
        if (!expected) {
            throw new CertificateException(
                    "checkServerTrusted: Expected public key: " + PUB_KEY
                            + ", got public key:" + base64Encoded);
        }
    }

    public void checkClientTrusted(X509Certificate[] xcs, String string) {
        // throw new
        // UnsupportedOperationException("checkClientTrusted: Not supported yet.");
    }

    public X509Certificate[] getAcceptedIssuers() {
        // throw new
        // UnsupportedOperationException("getAcceptedIssuers: Not supported yet.");
        return null;
    }
}
