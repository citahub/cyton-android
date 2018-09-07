package org.nervos.neuron.view.webview;

import org.nervos.neuron.view.webview.item.Message;
import org.nervos.neuron.view.webview.item.TypedData;

public interface OnSignTypedMessageListener {
    void onSignTypedMessage(Message<TypedData[]> message);
}
