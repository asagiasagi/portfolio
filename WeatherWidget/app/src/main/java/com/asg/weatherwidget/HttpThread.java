package com.asg.weatherwidget;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class HttpThread {
    private static final String TAG = "HttpThread";

    //----------------------------------------------------------------------------------------------
    // Singleton
    //----------------------------------------------------------------------------------------------
    private static HttpThread sInstance;

    private HttpThread() {
        /* singleton */
    }

    public static HttpThread getInstance() {
        if (sInstance == null) {
            sInstance = new HttpThread();
        }
        return sInstance;
    }

    //----------------------------------------------------------------------------------------------
    // Field
    //----------------------------------------------------------------------------------------------
    private InnerThread mThread = null;

    public boolean startDownload(String requestURL, Callback callback) {
        boolean startAble = !isRunning();

        if (startAble) {
            mThread = new InnerThread(requestURL, callback);
            mThread.start();
        }

        return startAble;
    }

    public boolean isRunning() {
        return mThread != null && mThread.isAlive();
    }

    //----------------------------------------------------------------------------------------------
    // InnerThread
    //----------------------------------------------------------------------------------------------
    private static class InnerThread extends Thread {
        private String mRequestURL;
        private Callback mCallback;

        public InnerThread(String requestURL, Callback callback) {
            mCallback = callback;
            mRequestURL = requestURL;
        }

        @Override
        public void run() {
            Log.d(TAG, "run<" + this.getName() + ">");

            InputStream inputStream = null;
            try {
                inputStream = new URL(mRequestURL).openConnection().getInputStream();
                mCallback.finishDownload(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                mCallback.notifyError();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mCallback.finishRunning();
            Log.d(TAG, "interrupt<" + this.getName() + ">");

        }
    }

    public interface Callback {
        void finishDownload(InputStream dlDataInputStream);

        void finishRunning();

        void notifyError();
    }
}
