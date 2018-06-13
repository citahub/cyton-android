package org.nervos.neuron.event;

import org.nervos.neuron.item.AppItem;

public class AppCollectEvent {

    public boolean isCollect;
    public AppItem appItem;

    public AppCollectEvent(boolean isCollect, AppItem appItem) {
        this.isCollect = isCollect;
        this.appItem = appItem;
    }

}
