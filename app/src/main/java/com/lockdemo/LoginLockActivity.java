package com.lockdemo;

import com.lockdemo.util.LockUtil;
import com.lockdemo.widget.CustomLockView;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 登陆验证页面
 * Created by apple on 4/11/15.
 */
public class LoginLockActivity extends BaseActivity {
    private TextView tvWarn;
    private int[] mIndexs;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_loginlock);
        initView();
        mIndexs= LockUtil.getPwd(this);
        //判断当前是否设置过密码，没有设置过，直接跳转到设置手势密码页面
        if(mIndexs.length>1){
            final CustomLockView cl=(CustomLockView)findViewById(R.id.cl);
            cl.setmIndexs(mIndexs);
            cl.setErrorTimes(4);
            cl.setStatus(1);
            cl.setShow(false);
            cl.setOnCompleteListener(new CustomLockView.OnCompleteListener() {
                @Override
                public void onComplete(int[] indexs) {
                    Toast.makeText(LoginLockActivity.this,"正确",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError() {
                    if (cl.getErrorTimes() > 0) {
                        tvWarn.setText("密码错误，还可以再输入" + cl.getErrorTimes() + "次");
                        tvWarn.setTextColor(getResources().getColor(R.color.red));
                    }
                }
            });
        }
    }

    /**
     * 初始化控件
     */
    private void initView(){
        tvWarn=getViewById(R.id.tvWarn);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }
}
