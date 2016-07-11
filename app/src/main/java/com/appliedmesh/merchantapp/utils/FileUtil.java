package com.appliedmesh.merchantapp.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * Created by dongbin on 2015/6/11.
 */
public class FileUtil {
    private static int bufferd = 1024;

    private FileUtil() {
    }


    // =================get SDCard information===================
    public static boolean isSdcardAvailable() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public static long getSDAllSizeKB() {
        // get path of sdcard
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        // get single block size(Byte)
        long blockSize = sf.getBlockSize();
        // ��ȡ������ݿ���
        long allBlocks = sf.getBlockCount();
        // ����SD����С
        return (allBlocks * blockSize) / 1024; // KB
    }

    /**
     * free size for normal application
     *
     * @return
     */
    public static long getSDAvalibleSizeKB() {
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        long blockSize = sf.getBlockSize();
        long avaliableSize = sf.getAvailableBlocks();
        return (avaliableSize * blockSize) / 1024;// KB
    }

    // =====================File Operation==========================
    public static boolean isFileExist(String director) {
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + director);
        return file.exists();
    }

    /**
     * create multiple director
     *
     * @param director
     * @return
     */
    public static boolean createFile(String director) {
        if (isFileExist(director)) {
            return true;
        } else {
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + director);
            if (!file.mkdirs()) {
                return false;
            }
            return true;
        }
    }

    private static void listFiles(String directory) {

        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + directory);
        File[] files = file.listFiles();
        if (files.length>0) {
            for (int i=0; i<files.length;i++) {
                //TODO list file here
            }
        }
    }

    /**
     * this url point to image(jpg)
     *
     * @param url
     * @return image name
     */
    public static String getUrlLastString(String url) {
        String[] str = url.split("/");
        int size = str.length;
        return str[size - 1];
    }

}
