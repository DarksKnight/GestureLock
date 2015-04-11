package com.lockdemo.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * 屏幕变暗
 * Created by apple on 4/11/15.
 */
public class ScreenObserver {
    private static String TAG = "ScreenObserver";
    private Context mContext;
    private ScreenBroadcastReceiver mScreenReceiver;
    private ScreenStateListener mScreenStateListener;
    private static Method mReflectScreenState;

    public ScreenObserver(Context context) {
        mContext = context;
        mScreenReceiver = new ScreenBroadcastReceiver();
        try {
            mReflectScreenState = PowerManager.class.getMethod("isScreenOn",new Class[] {});
        } catch (NoSuchMethodException nsme) {
            Log.d(TAG, "API < 7," + nsme);
        }
    }

    /**
     * screen状态广播接收者
     */
    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenStateListener.onScreenStateChange(true);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenStateListener.onScreenStateChange(false);
            }
        }
    }

    /**
     * 请求screen状态更新
     */
    public void requestScreenStateUpdate(ScreenStateListener listener) {
        mScreenStateListener = listener;
        startScreenBroadcastReceiver();
        firstGetScreenState();
    }

    /**
     * 第一次请求screen状态
     */
    private void firstGetScreenState() {
        PowerManager manager = (PowerManager) mContext
                .getSystemService(Activity.POWER_SERVICE);
        if (isScreenOn(manager)) {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenStateChange(true);
            }
        } else {
            if (mScreenStateListener != null) {
                mScreenStateListener.onScreenStateChange(false);
            }
        }
    }

    /**
     * 停止screen状态更新
     */
    public void stopScreenStateUpdate() {
        mContext.unregisterReceiver(mScreenReceiver);
    }

    /**
     * 启动screen状态广播接收器
     */
    private void startScreenBroadcastReceiver() {
        try{
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mContext.registerReceiver(mScreenReceiver, filter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * screen是否打开状态
     *
     * @param pm
     * @return
     */
    private static boolean isScreenOn(PowerManager pm) {
        boolean screenState;
        try {
            screenState = (Boolean) mReflectScreenState.invoke(pm);
        } catch (Exception e) {
            screenState = false;
        }
        return screenState;
    }

    public interface ScreenStateListener {
        public void onScreenStateChange(boolean isScreenOn);
    }
}