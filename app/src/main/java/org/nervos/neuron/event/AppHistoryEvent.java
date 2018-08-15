package org.nervos.neuron.event;

import org.nervos.neuron.item.AppItem;

public class AppHistoryEvent {

    public AppItem appItem;

    public AppHistoryEvent(AppItem appItem) {
        this.appItem = appItem;
    }

}
