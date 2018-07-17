package org.nervos.neuron.webview;

import org.nervos.neuron.webview.item.Message;

public interface OnSignPersonalMessageListener {
    void onSignPersonalMessage(Message<String> message);
}
