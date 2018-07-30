package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Window;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by 包俊 on 2018/7/30.
 */
public abstract class NBaseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedactivityState) {
        super.onCreate(savedactivityState);
        if (getContentLayout() != 0) {
            setContentView(getContentLayout());
        }
        initView();
        initData();
        initAction();
    }

    /**
     * 设置布局文件
     */
    protected abstract int getContentLayout();

    /**
     * 初始化UI
     */
    protected abstract void initView();

    /**
     * 初始化事件
     */
    protected abstract void initAction();

    /**
     * 初始化数据
     */
    protected abstract void initData();
}
