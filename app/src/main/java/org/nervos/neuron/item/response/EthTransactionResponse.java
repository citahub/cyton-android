package org.nervos.neuron.item.response;

import org.nervos.neuron.item.transaction.TransactionResponse;

import java.util.List;

public class EthTransactionResponse {

    public String status;
    public String message;
    public List<TransactionResponse> result;

}
