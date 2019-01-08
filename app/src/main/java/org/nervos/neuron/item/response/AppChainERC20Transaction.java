package org.nervos.neuron.item.response;

import org.nervos.neuron.item.transaction.RestTransaction;

import java.util.List;

public class AppChainERC20Transaction {

    public AppChainTransactionResult result;
    public int count;

    public static class AppChainTransactionResult {
        public List<RestTransaction> transfers;
    }

}
