package org.nervos.neuron.item.response;

import org.nervos.neuron.item.transaction.TransactionResponse;

import java.util.List;

public class AppChainTransactionResponse {

    public AppChainTransactionResult result;

    public static class AppChainTransactionResult {
        public List<TransactionResponse> transactions;
    }

}
