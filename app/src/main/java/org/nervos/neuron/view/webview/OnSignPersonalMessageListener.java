package org.nervos.neuron.view.webview;

import org.nervos.neuron.view.webview.item.Message;
import org.nervos.neuron.view.webview.item.Transaction;

public interface OnSignPersonalMessageListener {
    void onSignPersonalMessage(Message<Transaction> message);
}
