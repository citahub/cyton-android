package org.nervos.neuron.application;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/9/19.
 */
public class ApplicationService extends IntentService {

    private String SA_SERVER_URL = "https://banana.cryptape.com:8106/sa?project=default";

    public ApplicationService() {
        super("ApplicationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SensorsDataAPI.sharedInstance(this, SA_SERVER_URL, SensorsDataAPI.DebugMode.DEBUG_OFF);
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
        SensorsDataAPI.sharedInstance().trackAppCrash();
    }
}
