package org.nervos.neuron.item.response;

import org.nervos.neuron.item.transaction.RestTransaction;

import java.util.List;

public class EthTransaction {

    public String status;
    public String message;
    public List<RestTransaction> result;

}
