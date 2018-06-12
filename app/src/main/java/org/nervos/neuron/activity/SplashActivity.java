package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.nervos.neuron.R;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

public class SplashActivity extends BaseActivity {

    public static final String EXTRA_FIRST = "extra_first";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    long time = System.currentTimeMillis();
                    sleep(1000);
                    WalletEntity.initWalletMnemonic(mActivity);
                    DBChainUtil.initChainData(mActivity);
                    SharePrefUtil.init(mActivity);
                    EthRpcService.init(mActivity);
                    if (!TextUtils.isEmpty(SharePrefUtil.getCurrentWalletName())) {
                        startActivity(new Intent(mActivity, MainActivity.class));
                    } else {
                        Intent intent = new Intent(mActivity, CreateWalletActivity.class);
                        intent.putExtra(EXTRA_FIRST, true);
                        startActivity(intent);
                    }
                    finish();
                    Log.d("wallet", "time: " + (System.currentTimeMillis() - time));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
