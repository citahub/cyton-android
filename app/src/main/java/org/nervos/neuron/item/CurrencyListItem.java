package org.nervos.neuron.item;

import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Created by BaojunCZ on 2018/8/3.
 */
public class CurrencyListItem {
    private ArrayList<CurrencyItem> currency = new ArrayList<>();

    public ArrayList<CurrencyItem> getCurrency() {
        return currency;
    }

    public void setCurrency(ArrayList<CurrencyItem> currency) {
        this.currency = currency;
    }
}
