package org.nervos.neuron.item.response;

import org.nervos.neuron.item.TransactionItem;

import java.util.List;

public class AppChainTransactionResponse {

    public AppChainTransactionResult result;

    public static class AppChainTransactionResult {
        public List<TransactionItem> transactions;
    }

}
