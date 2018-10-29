package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.nervos.neuron.R;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.RootUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.dialog.ToastSingleButtonDialog;

public class SplashActivity extends BaseActivity {

    public static final String EXTRA_FIRST = "extra_first";
    public static final String LOCK_TO_MAIN = "lock_to_main";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        inLoginPage = true;
        if (RootUtil.isDeviceRooted()) {
            ToastSingleButtonDialog toastSingleButtonDialog
                    = ToastSingleButtonDialog.getInstance(this, getString(R.string.safe_hint)
                    , getString(R.string.root_hint));
            toastSingleButtonDialog.setOnCancelClickListener((dialog ->
                    finish()));
            toastSingleButtonDialog.setCanceledOnTouchOutside(false);
            toastSingleButtonDialog.setCancelable(false);
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        sleep(1000);
                        if (!TextUtils.isEmpty(SharePrefUtil.getCurrentWalletName())) {
                            if (SharePrefUtil.getBoolean(ConstUtil.FINGERPRINT, false)) {
                                Intent intent = new Intent(mActivity, FingerPrintActivity.class);
                                intent.putExtra(LOCK_TO_MAIN, true);
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(mActivity, MainActivity.class));
                            }
                        } else {
                            Intent intent = new Intent(mActivity, AddWalletActivity.class);
                            startActivity(intent);
                        }
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
}
