package com.appliedmesh.merchantapp.network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.HttpVersion;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

//import android.content.Context;
//import android.util.Log;


/**
 * It is the helper to get httpclient for http and https.
 * @author dong_bin
 *
 */
public class HttpClientHelper {
   private static final int SET_CONNECTION_TIMEOUT = 10 * 1000;
   private static final int SET_SOCKET_TIMEOUT = 30 * 1000;
   /* dongbin: if we have got the certificate file from server,
    * we can set hasCertificate true
    */
   private static final boolean hasCertificate = false;
   //private static HttpClient sHttpClient = null;

   public HttpClientHelper() {
   }

   public HttpClient getHttpClient(){
      HttpClient sHttpClient = null;
      if(sHttpClient == null) {
         try {
            SSLSocketFactory socketFactory;
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            socketFactory = new SSLSocketFactoryEx(trustStore);
            socketFactory.setHostnameVerifier(new HostnameVerifierEx());

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", socketFactory, 443));

            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, SET_CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, SET_SOCKET_TIMEOUT);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            sHttpClient = new DefaultHttpClient(ccm,params);
         } catch (Exception e) {
            sHttpClient = new DefaultHttpClient();
         }
      }
      return sHttpClient;
   }

   static class SSLSocketFactoryEx extends SSLSocketFactory {
      SSLContext sslContext = SSLContext.getInstance("TLS");
      private X509HostnameVerifier hostnameVerifier = new HostnameVerifierEx();

      public SSLSocketFactoryEx(KeyStore truststore)
            throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        TrustManager tm = new X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {

            }
        };

        this.sslContext.init(null, new TrustManager[] { tm }, null);
      }

      @Override
      public Socket createSocket(Socket socket, String host, int port,
            boolean autoClose) throws IOException, UnknownHostException {
         SSLSocket sslSocket = (SSLSocket) this.sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
         hostnameVerifier.verify(host, sslSocket);
         return sslSocket;
      }

      @Override
      public Socket createSocket() throws IOException {
         return this.sslContext.getSocketFactory().createSocket();
      }
   }

   static class HostnameVerifierEx extends AbstractVerifier {

      public final void verify(
            final String host,
            final String[] cns,
            final String[] subjectAlts) throws SSLException {

      }
   }
}
