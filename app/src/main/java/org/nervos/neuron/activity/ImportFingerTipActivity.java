package org.nervos.neuron.activity;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.fragment.wallet.view.WalletsFragment;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.FingerPrint.AuthenticateResultCallback;
import org.nervos.neuron.util.FingerPrint.FingerPrintController;
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
                ToastSingleButtonDialog dialog = ToastSingleButtonDialog.getInstance(mActivity, getResources().getString(R.string.dialog_finger_setting));
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
            if (!errorMsg.contains("取消"))
                Toast.makeText(mActivity, errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded() {
            if (authFingerDialog != null && authFingerDialog.isShowing())
                authFingerDialog.dismiss();
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

    @Override
    public void finish() {
        SharePrefUtil.putBoolean(ConstUtil.Fingerprint, true);
        super.finish();
    }
}
