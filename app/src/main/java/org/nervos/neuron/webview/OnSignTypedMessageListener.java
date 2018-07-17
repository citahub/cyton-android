package org.nervos.neuron.webview;

import org.nervos.neuron.webview.item.Message;

import trust.core.entity.TypedData;

public interface OnSignTypedMessageListener {
    void onSignTypedMessage(Message<TypedData[]> message);
}
