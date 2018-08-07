package org.nervos.neuron.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.nervos.neuron.R;

/**
 * Created by 包俊 on 2018/7/30.
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
