package com.cryptape.cita_wallet.activity;

import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by BaojunCZ on 2018/7/30.
 */
public abstract class NBaseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedActivityState) {
        super.onCreate(savedActivityState);
        if (getContentLayout() != 0) {
            setContentView(getContentLayout());
        }
        EventBus.getDefault().register(this);
        initView();
        initData();
        initAction();
    }

    /**
     * 设置布局文件
     */
    protected abstract int getContentLayout();

    /**
     * init ui
     */
    protected abstract void initView();

    /**
     * init data
     */
    protected abstract void initData();

    /**
     * init action
     */
    protected abstract void initAction();
}
