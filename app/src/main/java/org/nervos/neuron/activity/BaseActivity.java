package org.nervos.neuron.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.nervos.neuron.R;
import org.nervos.neuron.util.AntiHijackingUtil;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.dialog.ProgressCircleDialog;
import org.nervos.neuron.view.dialog.ProgressingDialog;

public class BaseActivity extends AppCompatActivity {

    private ProgressingDialog dialog = null;
    private ProgressCircleDialog circleDialog = null;
    protected boolean mIsSafeLast;
    protected Activity mActivity;
    Handler handler = new Handler();
    private long TimeOut = 1000 * 10 * 6;
    public boolean inLoginPage = false;
    private boolean needLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && getStatusBarColor() == getResources().getColor(R.color.white)) {
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

    private boolean isShouldTimeOut() {
        return !inLoginPage && SharePrefUtil.getBoolean(ConstUtil.FingerPrint, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isShouldTimeOut())
            handler.postDelayed(runnable, TimeOut);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isShouldTimeOut() && needLogin) {
            handler.removeCallbacks(runnable);
            gotoLogin();
            needLogin = false;
        }
    }

    @Override
    protected void onPause() {
        if (isShouldTimeOut()) {
            mIsSafeLast = AntiHijackingUtil.checkActivity(this);
            if (!mIsSafeLast) {
                needLogin = true;
            }
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (isShouldTimeOut()) {
            mIsSafeLast = AntiHijackingUtil.checkActivity(this);
            if (!mIsSafeLast) {
                needLogin = true;
            }
        }
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isShouldTimeOut())
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacks(runnable);
                    break;
                case MotionEvent.ACTION_UP:
                    handler.postDelayed(runnable, TimeOut);
                    break;
            }
        return super.dispatchTouchEvent(event);
    }

    private Runnable runnable = () -> {
        needLogin = false;
        gotoLogin();
    };

    private void gotoLogin() {
        if (!inLoginPage && SharePrefUtil.getBoolean(ConstUtil.FingerPrint, false))
            startActivity(new Intent(mActivity, FingerPrintActivity.class));
    }

    /**
     * 显示Progress Bar
     */
    protected void showProgressBar() {
        if (dialog == null)
            dialog = new ProgressingDialog(this);
        dialog.show();
    }

    protected void showProgressBar(@StringRes int message) {
        if (dialog == null)
            dialog = new ProgressingDialog(this);
        dialog.show();
        dialog.setMsg(getString(message));
    }

    protected void showProgressBar(String message) {
        if (dialog == null)
            dialog = new ProgressingDialog(this);
        dialog.show();
        dialog.setMsg(message);
    }

    /**
     * 隐藏Progress Bar
     */
    protected void dismissProgressBar() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }


    /**
     * show Progress circle
     */

    protected void showProgressCircle() {
        if (circleDialog == null)
            circleDialog = new ProgressCircleDialog(this);
        circleDialog.show();
    }

    /**
     * hide Progress circle
     */
    protected void dismissProgressCircle() {
        if (circleDialog != null && circleDialog.isShowing())
            circleDialog.dismiss();
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
}
