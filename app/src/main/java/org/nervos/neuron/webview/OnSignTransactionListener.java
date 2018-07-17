package org.nervos.neuron.webview;

import org.nervos.neuron.webview.item.Transaction;

public interface OnSignTransactionListener {
    void onSignTransaction(Transaction transaction);
}
