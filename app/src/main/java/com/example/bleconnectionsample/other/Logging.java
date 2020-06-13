
/*
 * Copyright (c) 2020 by Jainesh Desai
 * Created by Jainesh desai on 2020.
 */

package com.example.bleconnectionsample.other;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jainesh Desai on 1/2/2020
 */
@SuppressWarnings("ALL")
public class Logging {

    private static final String TAG = "Logging";
    private static final boolean LOG_ENABLE = true;
    private static final boolean DETAIL_ENABLE = true;
    private static boolean storeData = true;
    private static String timeStamp;
    private static Logging logging;
    private String APP_FOLDER_NAME = "AppName";
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    private Logging() {
    }

    public static synchronized Logging getInstance() {
        if (logging == null) {
            timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            return logging = new Logging();
        } else return logging;
    }

    public synchronized void logToFile(boolean isSendData, String data) {
        if (storeData) {
            try {
                File saveDir = new File(Environment.getExternalStorageDirectory() + "/" + APP_FOLDER_NAME + "/log");
                if (!saveDir.exists())
                    if (!saveDir.mkdirs()) {
                        Log.i(TAG, "logToFile: Log not stored to file as unable to create directory");
                    }
                File saveFile = new File(saveDir, String.format("log_%s.txt", timeStamp));
                //Log.e("tag", "filePath = " + saveDir.getAbsolutePath() + "/" + "log.txt");
                BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(saveFile, true));
                String currentDateAndTime = sdf.format(new Date());

                writer.write("\n\n==================\n".getBytes());

                writer.write(isSendData ? "Sent data: ".getBytes() : "Received data: ".getBytes());

                String currentFrameTime = currentDateAndTime + " : \n";
                writer.write(currentFrameTime.getBytes());

                writer.write(data.getBytes());

                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void firmwareUpdateLogToFile(String data) {
        logToFile("Firmware update flow", data);
    }

    public synchronized void logToFile(String label, String data) {
        if (storeData) {
            try {
                File saveDir = new File(Environment.getExternalStorageDirectory() + "/" + APP_FOLDER_NAME + "/log");
                Log.e(TAG, "logToFile: " + saveDir.getAbsolutePath());
                if (!saveDir.exists())
                    if (!saveDir.mkdirs()) {
                        Log.i(TAG, "logToFile: Log not stored to file as unable to create directory");
                    }
                File saveFile = new File(saveDir, String.format("log_%s.txt", timeStamp));
                //Log.e("tag", "filePath = " + saveDir.getAbsolutePath() + "/" + "log.txt");
                BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(saveFile, true));
                String currentDateAndTime = sdf.format(new Date());

                writer.write("\n\n==================\n".getBytes());
                writer.write(label.concat(" : ").getBytes());

                String currentFrameTime = currentDateAndTime + " : \n";
                writer.write(currentFrameTime.getBytes());

                writer.write(data.getBytes());

                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void v(String TAG, String msg) {
        try {
            if (LOG_ENABLE) {
                Log.v(TAG, buildMsg(msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void d(String TAG, String msg) {
        try {
            if (LOG_ENABLE) {
                Log.d(TAG, buildMsg(msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void i(String TAG, String msg) {
        try {
            if (LOG_ENABLE && Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, buildMsg(msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void w(String TAG, String msg) {
        try {
            if (LOG_ENABLE && Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, buildMsg(msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void w(String TAG, String msg, Exception e) {
        try {
            if (LOG_ENABLE && Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, buildMsg(msg), e);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void e(String TAG, String msg) {
        try {
            if (LOG_ENABLE && Log.isLoggable(TAG, Log.ERROR)) {
                Log.e(TAG, buildMsg(msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void e(String TAG, String msg, Exception e) {
        if (LOG_ENABLE && Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, buildMsg(msg), e);
        }
    }

    private String buildMsg(String msg) {
        StringBuilder buffer = new StringBuilder();

        if (DETAIL_ENABLE) {
            final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];

            buffer.append("[ ");
            buffer.append(Thread.currentThread().getName());
            buffer.append(": ");
            buffer.append(stackTraceElement.getFileName());
            buffer.append(": ");
            buffer.append(stackTraceElement.getLineNumber());
            buffer.append(": ");
            buffer.append(stackTraceElement.getMethodName());
        }

        buffer.append("() ] _____ ");
        buffer.append(msg);
        return buffer.toString();
    }
}