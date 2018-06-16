package org.nervos.neuron.response;

import org.nervos.neuron.item.TransactionItem;

import java.util.List;

public class NervosTransactionResponse {

    public NervosTransactionResult result;

    public static class NervosTransactionResult {
        public List<TransactionItem> transactions;
    }

}
