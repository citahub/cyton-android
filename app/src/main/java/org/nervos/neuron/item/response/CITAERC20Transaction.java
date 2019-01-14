package org.nervos.neuron.item.response;

import org.nervos.neuron.item.transaction.RestTransaction;

import java.util.List;

public class CITAERC20Transaction {

    public CITATransactionResult result;
    public int count;

    public static class CITATransactionResult {
        public List<RestTransaction> transfers;
    }

}
