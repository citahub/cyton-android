package org.nervos.neuron.item;

public class RecordItem {

    public String id;
    public String time;
    public String amount;

    public RecordItem(){}

    public RecordItem(String id, String time, String amount) {
        this.id = id;
        this.time = time;
        this.amount = amount;
    }

}
