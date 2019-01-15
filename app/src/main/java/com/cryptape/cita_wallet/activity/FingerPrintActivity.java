package com.cryptape.cita_wallet.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.view.dialog.AuthFingerDialog;
import com.cryptape.cita_wallet.util.fingerprint.AuthenticateResultCallback;
import com.cryptape.cita_wallet.util.fingerprint.FingerPrintController;
import com.cryptape.cita_wallet.view.dialog.ToastSingleButtonDialog;

import java.util.List;

/**
 * Created by BaojunCZ on 2018/8/6.
 */
public class FingerPrintActivity extends NBaseActivity implements View.OnClickListener {

    private ImageView fingerImg, otherImg;
    private AuthFingerDialog authFingerDialog = null;
    private FingerPrintController mFingerPrintController;

    @Override
    protected int getContentLayout() {
        inLoginPage = true;
        return R.layout.activity_fingerprint;
    }

    @Override
    protected void initView() {
        fingerImg = findViewById(R.id.iv_finger_print);
        otherImg = findViewById(R.id.iv_other);
    }

    @Override
    protected void initData() {
        mFingerPrintController = new FingerPrintController(this);
    }

    @Override
    protected void initAction() {
        fingerImg.setOnClickListener(this);
        otherImg.setOnClickListener(this);
        fingerImg.performClick();
    }

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.white);
    }

    AuthenticateResultCallback authenticateResultCallback = new AuthenticateResultCallback() {
        @Override
        public void onAuthenticationError(String errorMsg) {
            if (!errorMsg.contains("取消")) Toast.makeText(mActivity, errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded() {
            if (authFingerDialog != null && authFingerDialog.isShowing()) authFingerDialog.dismiss();
            if (getIntent().getBooleanExtra(SplashActivity.LOCK_TO_MAIN, false)) startActivity(new Intent(mActivity, MainActivity.class));
            finish();
        }

        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(mActivity, getResources().getString(R.string.fingerprint_lock_failed), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_finger_print:
                if (mFingerPrintController.hasEnrolledFingerprints() && mFingerPrintController.getEnrolledFingerprints().size() > 0) {
                    if (authFingerDialog == null) authFingerDialog = new AuthFingerDialog(this);
                    authFingerDialog.setOnShowListener((dialogInterface) -> {
                        mFingerPrintController.authenticate(authenticateResultCallback);
                    });
                    authFingerDialog.setOnDismissListener((dialog) -> {
                        mFingerPrintController.cancelAuth();
                    });
                    authFingerDialog.show();
                } else {
                    ToastSingleButtonDialog dialog = ToastSingleButtonDialog.getInstance(this, getString(R.string.dialog_finger_setting));
                    dialog.setOnCancelClickListener(dialog1 -> {
                        FingerPrintController.openFingerPrintSettingPage(this);
                        dialog1.dismiss();
                    });
                }
                break;
            case R.id.iv_other:
                Intent intent = new Intent(this, PwdUnlockActivity.class);
                intent.putExtra(SplashActivity.LOCK_TO_MAIN, getIntent().getBooleanExtra(SplashActivity.LOCK_TO_MAIN, false));
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authFingerDialog != null && authFingerDialog.isShowing()) authFingerDialog.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authFingerDialog != null && !authFingerDialog.isShowing()) authFingerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }
        //        appTaskList.get(0).finishAndRemoveTask();
        System.exit(0);
    }
}
