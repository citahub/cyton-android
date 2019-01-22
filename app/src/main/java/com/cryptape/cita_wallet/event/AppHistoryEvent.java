package com.cryptape.cita_wallet.event;

import com.cryptape.cita_wallet.item.App;

public class AppHistoryEvent {

    public App app;

    public AppHistoryEvent(App app, long timestamp) {
        this.app = app;
        this.app.timestamp = timestamp;
    }

}
