package com.cita.wallet.citawallet.item;

public class TransactionItem {

    public String id;
    public String time;
    public String amount;

    public TransactionItem(){}

    public TransactionItem(String id, String time, String amount) {
        this.id = id;
        this.time = time;
        this.amount = amount;
    }

}
