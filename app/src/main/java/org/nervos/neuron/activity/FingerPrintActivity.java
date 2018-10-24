package org.nervos.neuron.activity;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.view.dialog.AuthFingerDialog;
import org.nervos.neuron.util.FingerPrint.AuthenticateResultCallback;
import org.nervos.neuron.util.FingerPrint.FingerPrintController;
import org.nervos.neuron.view.dialog.ToastSingleButtonDialog;

/**
 * Created by BaojunCZ on 2018/8/6.
 */
public class FingerPrintActivity extends NBaseActivity implements View.OnClickListener {

    private ImageView fingerImg, otherImg;
    private AuthFingerDialog authFingerDialog = null;

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
        fingerImg.setOnClickListener(this);
        otherImg.setOnClickListener(this);
    }

    @Override
    protected void initAction() {
        fingerImg.performClick();
    }

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.white);
    }

    AuthenticateResultCallback authenticateResultCallback = new AuthenticateResultCallback() {
        @Override
        public void onAuthenticationError(String errorMsg) {
            if (!errorMsg.contains("取消"))
                Toast.makeText(mActivity, errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded() {
            if (authFingerDialog != null && authFingerDialog.isShowing())
                authFingerDialog.dismiss();
            if (getIntent().getBooleanExtra(SplashActivity.LOCK_TO_MAIN, false))
                startActivity(new Intent(mActivity, MainActivity.class));
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
                if (FingerPrintController.getInstance(this).hasEnrolledFingerprints() && FingerPrintController.getInstance(this).getEnrolledFingerprints().size() > 0) {
                    if (authFingerDialog == null)
                        authFingerDialog = new AuthFingerDialog(this);
                    authFingerDialog.setOnShowListener((dialogInterface) -> {
                        FingerPrintController.getInstance(this).authenticate(authenticateResultCallback);
                    });
                    authFingerDialog.setOnDismissListener((dialog) -> {
                        FingerPrintController.getInstance(this).cancelAuth();
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
        if (authFingerDialog != null && authFingerDialog.isShowing())
            authFingerDialog.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authFingerDialog != null && !authFingerDialog.isShowing())
            authFingerDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
