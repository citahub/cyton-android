package com.cryptape.cita_wallet.view.webview;

import com.cryptape.cita_wallet.view.webview.item.Message;
import com.cryptape.cita_wallet.view.webview.item.Transaction;

public interface OnSignPersonalMessageListener {
    void onSignPersonalMessage(Message<Transaction> message);
}
