package com.appliedmesh.merchantapp.network;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.appliedmesh.merchantapp.utils.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jiangdongbin on 7/6/15.
 */
public class DownloadManager {
    private static final String TAG = "DownloadManager";
    public final static String ACTION_DOWNLOAD_PROGRESS = "com.example.myapplication.action.DOWNLOAD_PROGRESS";
    public final static String EXTRA_PROGRESS = "com.example.myapplication.EXTRA_PROGRESS";
    public final static int BUFFER_SIZE= 4*1024; //4k

    private Context mContext;
    private static DownloadManager sDownloadManager;

    private DownloadManager(Context context){
        this.mContext = context;
    }

    public static DownloadManager getInstance(Context context){
        if (sDownloadManager==null){
            sDownloadManager = new DownloadManager(context);
        }
        return sDownloadManager;
    }

    public void DownloadFile(String Url, String targetPath){

        Logger.d(TAG, "download to:" + targetPath);
        new DownloadThread(Url,targetPath).start();
    }

    public class DownloadThread extends Thread{

        String mUrl;
        String mTargetPath;
        public DownloadThread(String url, String targetPath) {
            mUrl = url;
            mTargetPath = targetPath;
        }

        public void reportDownloadProgress(int progress) {
            Intent intent = new Intent(ACTION_DOWNLOAD_PROGRESS);
            intent.putExtra(EXTRA_PROGRESS,progress);
//            mContext.sendBroadcast(intent);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

        @Override
        public void run() {
            HttpClientHelper httpClientHelper = new HttpClientHelper();
            HttpClient client = httpClientHelper.getHttpClient();
            HttpGet request = new HttpGet(mUrl);
            FileOutputStream fos = null;
            InputStream is = null;
            try {
                HttpResponse hr = client.execute(request);
                Logger.d(TAG, "get response: " + hr.getStatusLine());

                long contentSize = hr.getEntity().getContentLength();
                Logger.d(TAG,"content size : "+contentSize);

                //if content size is 0, leave the file empty
                if (contentSize == 0) {
                    return;
                }

                reportDownloadProgress(0);
                long downloadSize = 0;
                fos = new FileOutputStream(mTargetPath,false);
                is = hr.getEntity().getContent();
                byte buff[] = new byte[BUFFER_SIZE];
                //Do download
                while(downloadSize < contentSize) {
                    int numread = is.read(buff);
                    if (numread <= 0) {
                        break;
                    }
                    downloadSize += numread ;
                    fos.write(buff, 0, numread);
                    reportDownloadProgress((int)((downloadSize*100)/contentSize));
                }

                Logger.d(TAG,"file size:" + downloadSize);
            } catch (Exception e) {
                reportDownloadProgress(100);
                Logger.e(TAG," Download error url : "+ mUrl);
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                        is = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
