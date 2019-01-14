package com.cryptape.cita_wallet.view.webview;

import com.cryptape.cita_wallet.view.webview.item.Message;
import com.cryptape.cita_wallet.view.webview.item.TypedData;

public interface OnSignTypedMessageListener {
    void onSignTypedMessage(Message<TypedData[]> message);
}
