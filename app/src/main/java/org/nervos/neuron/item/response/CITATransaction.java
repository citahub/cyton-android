package org.nervos.neuron.item.response;

import org.nervos.neuron.item.transaction.RestTransaction;

import java.util.List;

public class CITATransaction {

    public CITATransactionResult result;

    public static class CITATransactionResult {
        public List<RestTransaction> transactions;
    }

}
