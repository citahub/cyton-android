package org.nervos.neuron.application;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils;

import org.json.JSONObject;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.sensor.SensorIDRandomUtils;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/9/19.
 */
public class ApplicationService extends IntentService {

    private static final String SENSOR_IP_ID = "ip_id";
    private static final String SENSOR_CHANNEL_KEY = "DownloadChannel";
    private static final String CHANNEL_META = "SD_CHANNEL_ID";
    private static final String EVENT_CHANNEL_META = "AppInstall";

    private String SA_SERVER_URL_DEBUG = "https://banana.cryptape.com:8106/sa?project=default";
    private String SA_SERVER_URL = "https://banana.cryptape.com:8106/sa?project=production";
    private String SensorID = "sensor_id";

    public ApplicationService() {
        super("ApplicationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (org.nervos.neuron.BuildConfig.IS_DEBUG) {
            SensorsDataAPI.sharedInstance(this, SA_SERVER_URL_DEBUG, SensorsDataAPI.DebugMode.DEBUG_OFF);
            SensorsDataAPI.sharedInstance().trackAppCrash();
        } else {
            SensorsDataAPI.sharedInstance(this, SA_SERVER_URL, SensorsDataAPI.DebugMode.DEBUG_OFF);
        }
        if (!SharePrefUtil.getBoolean(SensorID, false)) {
            SharePrefUtil.putBoolean(SensorID, true);
            SensorsDataAPI.sharedInstance().identify(SensorIDRandomUtils.INSTANCE.getId());
        }
        try {
            JSONObject properties = new JSONObject();
            String ipID = SharePrefUtil.getString(ConstUtil.SENSOR_IP_ID, "");
            if (ipID.isEmpty()) {
                ipID = SensorIDRandomUtils.INSTANCE.getIpId();
                SharePrefUtil.putString(ConstUtil.SENSOR_IP_ID, ipID);
            }
            properties.put(SENSOR_IP_ID, ipID);
            SensorsDataAPI.sharedInstance().registerSuperProperties(properties);

            String downloadChannel = SensorsDataUtils.getApplicationMetaData(this, CHANNEL_META);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SENSOR_CHANNEL_KEY, downloadChannel);
            SensorsDataAPI.sharedInstance().trackInstallation(EVENT_CHANNEL_META, properties);

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
