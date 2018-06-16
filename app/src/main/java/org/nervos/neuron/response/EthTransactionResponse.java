package org.nervos.neuron.response;

import org.nervos.neuron.item.TransactionItem;

import java.util.List;

public class EthTransactionResponse {

    public String status;
    public String message;
    public List<TransactionItem> result;

}
