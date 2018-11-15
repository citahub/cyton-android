package org.nervos.neuron.item.response;

import org.nervos.neuron.item.transaction.TransactionItem;

import java.util.List;

public class EthTransactionResponse {

    public String status;
    public String message;
    public List<TransactionItem> result;

}
