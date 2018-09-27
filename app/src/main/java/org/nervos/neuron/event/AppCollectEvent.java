package org.nervos.neuron.event;

import org.nervos.neuron.item.AppInfo;
import org.nervos.neuron.item.AppItem;

public class AppCollectEvent {

    public boolean isCollect;
    public AppInfo appInfo;

    public AppCollectEvent(boolean isCollect, AppItem appItem, long timestamp) {
        this.isCollect = isCollect;
        this.appInfo = new AppInfo(appItem, timestamp);
    }

}
