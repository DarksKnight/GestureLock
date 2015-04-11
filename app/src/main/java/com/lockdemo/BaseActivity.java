package com.lockdemo;

import com.lockdemo.util.LockUtil;
import com.lockdemo.util.ScreenObserver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by apple on 4/11/15.
 */
public class BaseActivity extends Activity {

    public Context context;

    public <K extends View> K getViewById(int id) {
        return (K) getWindow().findViewById(id);
    }

    private ScreenObserver mScreenObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context=this;

        mScreenObserver = new ScreenObserver(this);
        mScreenObserver.requestScreenStateUpdate(new ScreenObserver.ScreenStateListener() {
            @Override
            public void onScreenStateChange(boolean isScreenOn) {
                if (!isScreenOn&& LockUtil.getPwdStatus(context)&&LockUtil.getPwd(context).length>0) {
                    doSomethingOnScreenOff();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(mScreenObserver!=null){
            mScreenObserver.stopScreenStateUpdate();
        }
        super.onDestroy();
    }

    /**
     * 打开验证手势
     */
    private void doSomethingOnScreenOff() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), LoginLockActivity.class);
        intent.putExtra("current","resume");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
