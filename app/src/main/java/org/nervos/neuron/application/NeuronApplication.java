package org.nervos.neuron.application;

import android.app.Application;

import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.nervos.neuron.service.http.EthRpcService;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.SharePrefUtil;

/**
 * Created by duanyytop on 2018/4/17
 */
public class NeuronApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ZXingLibrary.initDisplayOpinion(this);
        WalletEntity.initWalletMnemonic(this);
        SharePrefUtil.init(this);
        EthRpcService.init(this);
        AESCrypt.init(this);
    }
}
