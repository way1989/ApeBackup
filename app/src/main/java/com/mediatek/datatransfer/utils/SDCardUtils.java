package com.mediatek.datatransfer.utils;



import java.io.File;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.datatransfer.R;
import com.mediatek.datatransfer.utils.Constants.ModulePath;


public class SDCardUtils {

    private static final String TAG = "SDCardUtils";

    public final static int MINIMUM_SIZE = 512;


    public static String getSavePath(Context context){
        final String[] paths = StorageDirManager.getInstance(context).getStoragePath();
        for (int i = paths.length -1; i >=0; i--) {
            final String path = paths[i];
            if (!TextUtils.isEmpty(path)) {
                Log.d(TAG, "getSavePath: path = " + path);
                return path;
            }
        }
        return null;
    }

	@SuppressLint("NewApi")
	public static boolean hasInternalStorage(Context context) {
        final String[] paths = StorageDirManager.getInstance(context).getStoragePath();
        final String internalPath = paths[StorageDirManager.SDM_INTERANL_STORAGE_INDEX];
        return !TextUtils.isEmpty(internalPath);
    }

    public static String getExternalStoragePath(Context context) {
        final String[] paths = StorageDirManager.getInstance(context).getStoragePath();
        for (int i = paths.length -1; i >= 1; i--) {
            final String path = paths[i];
            if (!TextUtils.isEmpty(path)) {
                Log.d(TAG, "getExternalStoragePath: path = " + path);
                return path;
            }
        }
        return null;
    }

    public static String getSDStatueMessage(Context context) {
        String message = context.getString(R.string.nosdcard_notice);
        //String storagePath = StorageManager.getExternalStoragePath();
        String storagePath = getSavePath(context);
        if (!StorageDirManager.getInstance(context).isWriteable(storagePath)) {
            message = context.getString(R.string.sdcard_busy_message);
        }
        return message;
    }

    public static String getStoragePath(Context context) {
        String storagePath = getExternalStoragePath(context);
        if (storagePath == null) {
            MyLogger.logD(TAG, "getStoragePath: storagePath = " + storagePath);
            return null;
        }
        storagePath = storagePath + File.separator + "backup";
        MyLogger.logD(TAG, "getStoragePath: path is " + storagePath);
        File file = new File(storagePath);
        if (file.exists() && file.isDirectory()) {
            return storagePath;
        }
        boolean result = file.mkdirs();
        Log.d(TAG, "getStoragePath: mkdir " + storagePath + " result = " + result);
        return  storagePath;
    }

    public static String getPersonalDataBackupPath(Context context) {
        String path = getStoragePath(context);
        if (path != null) {
            return path + File.separator + ModulePath.FOLDER_DATA;
        }

        return path;
    }

    public static String getAppsBackupPath(Context context) {
        String path = getStoragePath(context);
        MyLogger.logD(TAG, "getAppsBackupPath path = " + path);
        if (path != null) {
            return path + File.separator + ModulePath.FOLDER_APP;
        }
        return path;
    }

    public static boolean isSdCardAvailable(Context context) {
        return (getStoragePath(context) != null);
    }

    public static long getAvailableSize(String file) {
        android.os.StatFs stat = new android.os.StatFs(file);
        long count = stat.getAvailableBlocks();
        long size = stat.getBlockSize();
        long totalSize = count * size;
        MyLogger.logD(TAG, "file remain size = " + totalSize);
        return totalSize;
    }

    public static boolean isSdCardMissing(Context context) {
        boolean isSDCardMissing = false;
        String path = getStoragePath(context);
        if (path == null) {
            isSDCardMissing = true;
        } else {
            // create file to check for sure
            File temp = new File(path + File.separator + ".temp");
            if (temp.exists()) {
                if (!temp.delete()) {
                    isSDCardMissing = true;
                }
            } else {
                try {
                    if (!temp.createNewFile()) {
                        isSDCardMissing = true;;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MyLogger.logE(TAG, "Cannot create temp file");
                    isSDCardMissing = true;
                } finally {
                    temp.delete();
                }
            }
        }
        return isSDCardMissing;
    }

    /*
     * If SD card is removed or full, kill this process
     */
    public static void killProcessIfNecessary(Context context) {
        if (isSdCardMissing(context)) {
            Log.i(TAG, "SD card removed, kill process");
            Utils.killMyProcess();
        } else {
            String path = getStoragePath(context);
            if (getAvailableSize(path) <= MINIMUM_SIZE) {
                Log.i(TAG, "SD full, kill process");
                Utils.killMyProcess();
            }
        }
        Log.i(TAG, "SD card OK, no need to kill process");
    }
}

