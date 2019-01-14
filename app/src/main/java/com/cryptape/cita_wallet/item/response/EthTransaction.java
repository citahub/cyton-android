package com.cryptape.cita_wallet.item.response;

import com.cryptape.cita_wallet.item.transaction.RestTransaction;

import java.util.List;

public class EthTransaction {

    public String status;
    public String message;
    public List<RestTransaction> result;

}
