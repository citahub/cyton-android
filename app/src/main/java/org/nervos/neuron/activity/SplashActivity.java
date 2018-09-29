package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.nervos.neuron.R;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

public class SplashActivity extends BaseActivity {

    public static final String EXTRA_FIRST = "extra_first";
    public static final String LOCK_TO_MAIN = "lock_to_main";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    sleep(1000);
                    if (!TextUtils.isEmpty(SharePrefUtil.getCurrentWalletName())) {
                        if (SharePrefUtil.getBoolean(ConstUtil.FingerPrint, false)) {
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
