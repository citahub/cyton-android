package org.nervos.neuron.webview;

import org.nervos.neuron.webview.item.Message;
import org.nervos.neuron.webview.item.TypedData;

public interface OnSignTypedMessageListener {
    void onSignTypedMessage(Message<TypedData[]> message);
}
