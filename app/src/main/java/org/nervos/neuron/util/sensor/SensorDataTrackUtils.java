package org.nervos.neuron.util.sensor;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by BaojunCZ on 2018/10/10.
 */
public class SensorDataTrackUtils {

    public static void transferAccount(String symbol, String value, String receiver, String sender, String chain,String type) {
        try {
            JSONObject object = new JSONObject();
            object.put("target_currency", symbol);
            object.put("target_currency_number", value);
            object.put("receive_address", receiver);
            object.put("outcome_address", sender);
            object.put("transfer_type", type);
            object.put("target_chain", chain);
            SensorsDataAPI.sharedInstance().track("transfer_accounts", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
