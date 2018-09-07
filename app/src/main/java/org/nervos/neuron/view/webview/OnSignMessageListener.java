package org.nervos.neuron.view.webview;


import org.nervos.neuron.view.webview.item.Message;
import org.nervos.neuron.view.webview.item.Transaction;

public interface OnSignMessageListener {
    void onSignMessage(Message<Transaction> message);
}
