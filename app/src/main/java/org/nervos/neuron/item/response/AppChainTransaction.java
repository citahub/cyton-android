package org.nervos.neuron.item.response;

import org.nervos.neuron.item.transaction.RestTransaction;

import java.util.List;

public class AppChainTransaction {

    public AppChainTransactionResult result;

    public static class AppChainTransactionResult {
        public List<RestTransaction> transactions;
    }

}
