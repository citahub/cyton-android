package org.nervos.neuron.view.webview;

import org.nervos.neuron.view.webview.item.Transaction;

public interface OnSignTransactionListener {
    void onSignTransaction(Transaction transaction);
}
