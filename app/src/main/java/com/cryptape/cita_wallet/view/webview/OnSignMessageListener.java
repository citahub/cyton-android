package com.cryptape.cita_wallet.view.webview;


import com.cryptape.cita_wallet.view.webview.item.Message;
import com.cryptape.cita_wallet.view.webview.item.Transaction;

public interface OnSignMessageListener {
    void onSignMessage(Message<Transaction> message);
}
