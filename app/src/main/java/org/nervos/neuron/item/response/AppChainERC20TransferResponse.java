package org.nervos.neuron.item.response;

import org.nervos.neuron.item.transaction.TransactionResponse;

import java.util.List;

public class AppChainERC20TransferResponse {

    public AppChainTransactionResult result;
    public int count;

    public static class AppChainTransactionResult {
        public List<TransactionResponse> transfers;
    }

}
