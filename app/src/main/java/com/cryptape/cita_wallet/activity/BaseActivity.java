package com.cryptape.cita_wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.util.AntiHijackingUtil;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;
import com.cryptape.cita_wallet.view.dialog.ProgressCircleDialog;
import com.cryptape.cita_wallet.view.dialog.ProgressingDialog;

/**
 * Created by duanyytop on 2018/5/17
 */
public class BaseActivity extends AppCompatActivity {

    private static final int INTERVAL_TIME = 20000;
    private static final int INTERVAL_DOWN_TIME = 1000;
    private ProgressingDialog dialog = null;
    private ProgressCircleDialog circleDialog = null;
    protected boolean mIsSafeLast;
    protected Activity mActivity;
    public boolean inLoginPage = false;
    private boolean needLogin = false;
    private TimeCount timeCount = new TimeCount();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getStatusBarColor() == getResources().getColor(R.color.white)) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getStatusBarColor());
        }
    }

    /**
     * set statusBarColor
     */
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.white);
    }

    private boolean shouldTimeOut() {
        return !inLoginPage && SharePrefUtil.getBoolean(ConstantUtil.FINGERPRINT, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldTimeOut()) {
            if (needLogin) {
                gotoLogin();
                needLogin = false;
            } else {
                timeCount.cancel();
            }
        }
    }

    @Override
    protected void onStop() {
        if (shouldTimeOut()) {
            mIsSafeLast = AntiHijackingUtil.checkActivity(this);
            if (!mIsSafeLast) {
                timeCount.start();
            }
        }
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void gotoLogin() {
        if (!inLoginPage && SharePrefUtil.getBoolean(ConstantUtil.FINGERPRINT, false)) {
            Intent intent = new Intent(mActivity, FingerPrintActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 显示Progress Bar
     */
    protected void showProgressBar() {
        if (dialog == null) {
            dialog = new ProgressingDialog(this);
        }
        dialog.show();
    }

    protected void showProgressBar(@StringRes int message) {
        if (dialog == null) dialog = new ProgressingDialog(this);
        dialog.show();
        dialog.setMsg(getString(message));
    }

    protected void showProgressBar(String message) {
        if (dialog == null) dialog = new ProgressingDialog(this);
        dialog.show();
        dialog.setMsg(message);
    }

    /**
     * 隐藏Progress Bar
     */
    protected void dismissProgressBar() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }


    /**
     * show Progress circle
     */

    protected void showProgressCircle() {
        if (circleDialog == null) circleDialog = new ProgressCircleDialog(this);
        circleDialog.show();
    }

    /**
     * hide Progress circle
     */
    protected void dismissProgressCircle() {
        if (circleDialog != null && circleDialog.isShowing()) circleDialog.dismiss();
    }

    private long lastClickTime;

    public boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    @Subscribe
    public void onEvent(Object object) {
    }

    private class TimeCount extends CountDownTimer {

        public TimeCount() {
            super(INTERVAL_TIME, INTERVAL_DOWN_TIME);
        }

        @Override
        public void onTick(long l) {
        }

        @Override
        public void onFinish() {
            needLogin = true;
        }
    }
}
