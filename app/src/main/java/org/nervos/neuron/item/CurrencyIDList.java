package org.nervos.neuron.item;

import java.util.ArrayList;

/**
 * Created by BaojunCZ on 2018/8/3.
 */
public class CurrencyIDList {
    private ArrayList<CurrencyIDItem> data = new ArrayList<>();

    public ArrayList<CurrencyIDItem> getList() {
        return data;
    }

    public void setList(ArrayList<CurrencyIDItem> data) {
        this.data = data;
    }
}
