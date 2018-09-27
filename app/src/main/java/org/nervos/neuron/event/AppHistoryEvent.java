package org.nervos.neuron.event;

import org.nervos.neuron.item.AppInfo;
import org.nervos.neuron.item.AppItem;

public class AppHistoryEvent {

    public AppInfo appInfo;

    public AppHistoryEvent(AppItem appItem, long timestamp) {
        this.appInfo = new AppInfo(appItem, timestamp);
    }

}
