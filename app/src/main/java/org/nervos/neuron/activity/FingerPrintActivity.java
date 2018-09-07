package org.nervos.neuron.activity;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.view.dialog.AuthFingerDialog;
import org.nervos.neuron.util.FingerPrint.AuthenticateResultCallback;
import org.nervos.neuron.util.FingerPrint.FingerPrintController;

/**
 * Created by BaojunCZ on 2018/8/6.
 */
public class FingerPrintActivity extends NBaseActivity implements View.OnClickListener {

    private ImageView fingerImg, otherImg;
    private AuthFingerDialog authFingerDialog = null;

    @Override
    protected int getContentLayout() {
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
//            Toast.makeText(mActivity, getResources().getString(R.string.fingerprint_lock_success), Toast.LENGTH_SHORT).show();
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
                if (FingerPrintController.getInstance(mActivity).hasEnrolledFingerprints() && FingerPrintController.getInstance(mActivity).getEnrolledFingerprints().size() > 0) {
                    if (authFingerDialog == null)
                        authFingerDialog = new AuthFingerDialog(mActivity);
                    authFingerDialog.setOnShowListener((dialogInterface) -> {
                        FingerPrintController.getInstance(mActivity).authenticate(authenticateResultCallback);
                    });
                    authFingerDialog.setOnDismissListener((dialog) -> {
                        FingerPrintController.getInstance(mActivity).cancelAuth();
                    });
                    authFingerDialog.show();
                } else {
                    Toast.makeText(mActivity, getResources().getString(R.string.finger_print_no_touchID), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.iv_other:
                startActivity(new Intent(this, PwdUnlockActivity.class));
                finish();
                break;
        }
    }
}
