package org.nervos.neuron.event;

import org.nervos.neuron.item.App;

public class AppHistoryEvent {

    public App app;

    public AppHistoryEvent(App app, long timestamp) {
        this.app = app;
        this.app.timestamp = timestamp;
    }

}
