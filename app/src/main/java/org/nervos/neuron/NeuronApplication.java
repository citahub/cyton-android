package org.nervos.neuron;

import android.app.Application;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

public class NeuronApplication extends Application {

    final String SA_SERVER_URL = "YOUR_SERVER_URL";

    @Override
    public void onCreate() {
        super.onCreate();

        ZXingLibrary.initDisplayOpinion(this);
        WalletEntity.initWalletMnemonic(this);
        DBChainUtil.initChainData(this);
        SharePrefUtil.init(this);
        EthRpcService.init(this);
        AESCrypt.init(this);
        SensorsDataAPI.sharedInstance(this, SA_SERVER_URL, SensorsDataAPI.DebugMode.DEBUG_OFF);
    }
}
