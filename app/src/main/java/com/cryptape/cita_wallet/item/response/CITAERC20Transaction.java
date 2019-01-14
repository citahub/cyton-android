package com.cryptape.cita_wallet.item.response;

import com.cryptape.cita_wallet.item.transaction.RestTransaction;

import java.util.List;

public class CITAERC20Transaction {

    public CITATransactionResult result;
    public int count;

    public static class CITATransactionResult {
        public List<RestTransaction> transfers;
    }

}
