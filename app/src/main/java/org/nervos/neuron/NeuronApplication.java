package org.nervos.neuron;

import android.app.Application;

import org.nervos.neuron.util.SharePrefUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

public class NeuronApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ZXingLibrary.initDisplayOpinion(this);
        Fresco.initialize(this);
        WalletEntity.initWalletMnemonic(this);
        SharePrefUtil.init(this);
    }
}
