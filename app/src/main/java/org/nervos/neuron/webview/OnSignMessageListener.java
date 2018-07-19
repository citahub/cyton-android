package org.nervos.neuron.webview;


import org.nervos.neuron.webview.item.Message;
import org.nervos.neuron.webview.item.Transaction;

public interface OnSignMessageListener {
    void onSignMessage(Message<Transaction> message);
}
