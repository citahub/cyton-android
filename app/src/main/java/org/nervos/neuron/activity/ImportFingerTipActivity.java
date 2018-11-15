package org.nervos.neuron.activity;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.fragment.wallet.view.WalletsFragment;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.fingerprint.AuthenticateResultCallback;
import org.nervos.neuron.util.fingerprint.FingerPrintController;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.button.CommonButton;
import org.nervos.neuron.view.dialog.AuthFingerDialog;
import org.nervos.neuron.view.dialog.ToastSingleButtonDialog;

/**
 * Created by BaojunCZ on 2018/10/12.
 */
public class ImportFingerTipActivity extends NBaseActivity {

    private CommonButton openBtn;
    private TextView cancelBtn;
    private AuthFingerDialog authFingerDialog = null;
    private FingerPrintController mFingerPrintController;

    @Override
    protected int getContentLayout() {
        return R.layout.acitivity_import_finger_tip;
    }

    @Override
    protected void initView() {
        openBtn = findViewById(R.id.cbtn_open);
        cancelBtn = findViewById(R.id.btn_cancel);
    }

    @Override
    protected void initData() {
        mFingerPrintController = new FingerPrintController(this);
    }

    @Override
    protected void initAction() {
        cancelBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_TAG, WalletsFragment.TAG);
            startActivity(intent);
            finish();
        });
        openBtn.setOnClickListener((v) -> {
            if (mFingerPrintController.hasEnrolledFingerprints() &&
                    mFingerPrintController.getEnrolledFingerprints().size() > 0) {
                if (authFingerDialog == null) authFingerDialog = new AuthFingerDialog(mActivity);
                authFingerDialog.setOnShowListener((dialogInterface) -> {
                    mFingerPrintController.authenticate(authenticateResultCallback);
                });
                authFingerDialog.setOnDismissListener((dialog) -> {
                    mFingerPrintController.cancelAuth();
                });
                authFingerDialog.show();
            } else {
                ToastSingleButtonDialog dialog = ToastSingleButtonDialog.getInstance(mActivity, getResources().getString(R.string
                        .dialog_finger_setting));
                dialog.setOnCancelClickListener(view -> {
                    FingerPrintController.openFingerPrintSettingPage(mActivity);
                    view.dismiss();
                });
            }
        });
    }

    AuthenticateResultCallback authenticateResultCallback = new AuthenticateResultCallback() {
        @Override
        public void onAuthenticationError(String errorMsg) {
            if (!errorMsg.contains("取消")) Toast.makeText(mActivity, errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded() {
            if (authFingerDialog != null && authFingerDialog.isShowing()) authFingerDialog.dismiss();
            SharePrefUtil.putBoolean(ConstantUtil.FINGERPRINT, true);
            Toast.makeText(mActivity, getResources().getString(R.string.fingerprint_setting_sucess), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(mActivity, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_TAG, WalletsFragment.TAG);
            startActivity(intent);
            finish();
        }

        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(mActivity, getResources().getString(R.string.fingerprint_lock_failed), Toast.LENGTH_SHORT).show();
        }
    };
}
