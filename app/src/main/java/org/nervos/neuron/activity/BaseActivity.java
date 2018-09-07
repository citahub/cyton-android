package org.nervos.neuron.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.nervos.neuron.R;
import org.nervos.neuron.view.dialog.ProgressCircleDialog;
import org.nervos.neuron.view.dialog.ProgressingDialog;

public class BaseActivity extends AppCompatActivity {

    private ProgressingDialog dialog = null;
    private ProgressCircleDialog circleDialog = null;

    protected Activity mActivity;

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

    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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

    @Subscribe
    public void onEvent(Object object) {
    }
}
