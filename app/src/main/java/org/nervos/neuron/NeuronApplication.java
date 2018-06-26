package org.nervos.neuron;

import android.app.Application;

import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
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
        DBChainUtil.initChainData(this);
        SharePrefUtil.init(this);
        EthRpcService.init(this);
        AESCrypt.init(this);
    }
}
