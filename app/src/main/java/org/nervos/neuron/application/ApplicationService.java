package org.nervos.neuron.application;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.util.SensorIDRandomUtils;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/9/19.
 */
public class ApplicationService extends IntentService {

    private String SA_SERVER_URL_DEBUG = "https://banana.cryptape.com:8106/sa?project=default";
    private String SA_SERVER_URL = "https://banana.cryptape.com:8106/sa?project=production";
    private String SensorID = "sensor_id";

    public ApplicationService() {
        super("ApplicationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ZXingLibrary.initDisplayOpinion(this);
        WalletEntity.initWalletMnemonic(this);
        DBChainUtil.initChainData(this);
        EthRpcService.init(this);
        AESCrypt.init(this);

        if (org.nervos.neuron.BuildConfig.IS_DEBUG) {
            SensorsDataAPI.sharedInstance(this, SA_SERVER_URL_DEBUG, SensorsDataAPI.DebugMode.DEBUG_AND_TRACK);
            SensorsDataAPI.sharedInstance().trackAppCrash();
        } else {
            SensorsDataAPI.sharedInstance(this, SA_SERVER_URL, SensorsDataAPI.DebugMode.DEBUG_OFF);
        }
        if (!SharePrefUtil.getBoolean(SensorID, false)) {
            SharePrefUtil.putBoolean(SensorID, true);
            SensorsDataAPI.sharedInstance().identify(SensorIDRandomUtils.getID());
        }
        try {
            // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
            List<SensorsDataAPI.AutoTrackEventType> eventTypeList = new ArrayList<>();
            // $AppStart
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_START);
            // $AppEnd
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_END);
            // $AppViewScreen
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_VIEW_SCREEN);
            // $AppClick
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_CLICK);
            SensorsDataAPI.sharedInstance().enableAutoTrack(eventTypeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
