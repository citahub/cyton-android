package org.nervos.neuron.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import org.nervos.neuron.R;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.RootUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.dialog.ToastSingleButtonDialog;

/**
 * Created by duanyytop on 2018/6/12
 */
public class SplashActivity extends BaseActivity {

    public static final String EXTRA_FIRST = "extra_first";
    public static final String LOCK_TO_MAIN = "lock_to_main";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
        inLoginPage = true;
        if (RootUtil.isDeviceRooted()) {
            new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.safe_hint)
                    .setMessage(R.string.root_hint)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).setNegativeButton(R.string.go_on, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            gotoMainPage();
                        }
                    }).setCancelable(false)
                    .create().show();
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        sleep(1000);
                        gotoMainPage();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    private void gotoMainPage() {
        if (!TextUtils.isEmpty(SharePrefUtil.getCurrentWalletName())) {
            if (SharePrefUtil.getBoolean(ConstantUtil.FINGERPRINT, false)) {
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
    }
}
