package com.lockdemo;

import com.lockdemo.util.LockUtil;
import com.lockdemo.widget.CustomLockView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置手势密码页面
 * Created by apple on 4/11/15.
 */
public class MainActivity extends BaseActivity {
    private ImageView iva,ivb,ivc,ivd,ive,ivf,ivg,ivh,ivi;
    private List<ImageView> list=new ArrayList<ImageView>();
    private TextView tvWarn;
    private int times=0;
    private int[] mIndexs=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        final CustomLockView cl=(CustomLockView)findViewById(R.id.cl);
        cl.setOnCompleteListener(new CustomLockView.OnCompleteListener() {
            @Override
            public void onComplete(int[] indexs) {
                mIndexs=indexs;
                //显示次数
                if(times==0){
                    for(int i=0;i<indexs.length;i++){
                        list.get(indexs[i]).setImageDrawable(getResources().getDrawable(R.mipmap.gesturecirlebrownsmall));
                    }
                    tvWarn.setText("再次绘制解锁图案");
                    tvWarn.setTextColor(getResources().getColor(R.color.white));
                    times++;
                }else if(times==1){
                    //将密码设置在本地
                    LockUtil.setPwdToDisk(MainActivity.this, mIndexs);
                    LockUtil.setPwdStatus(MainActivity.this, true);
                    //会员验证
                    invalidateUser();
                }
            }

            @Override
            public void onError() {
                tvWarn.setText("与上一次绘制不一致，请重新绘制");
                tvWarn.setTextColor(getResources().getColor(R.color.red));
            }
        });
    }

    /**
     * 初始化控件
     */
    private void initView(){
        //初始化9个小圆
        iva=(ImageView)findViewById(R.id.iva);
        ivb=(ImageView)findViewById(R.id.ivb);
        ivc=(ImageView)findViewById(R.id.ivc);
        ivd=(ImageView)findViewById(R.id.ivd);
        ive=(ImageView)findViewById(R.id.ive);
        ivf=(ImageView)findViewById(R.id.ivf);
        ivg=(ImageView)findViewById(R.id.ivg);
        ivh=(ImageView)findViewById(R.id.ivh);
        ivi=(ImageView)findViewById(R.id.ivi);
        list.add(iva);
        list.add(ivb);
        list.add(ivc);
        list.add(ivd);
        list.add(ive);
        list.add(ivf);
        list.add(ivg);
        list.add(ivh);
        list.add(ivi);
        tvWarn=getViewById(R.id.tvWarn);
    }

    /**
     * 会员验证
     */
    private void invalidateUser( ){
        Intent i=new Intent();
        i.setClass(this,LoginLockActivity.class);
        startActivity(i);
    }
}
