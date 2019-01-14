package com.cryptape.cita_wallet.item;

import java.util.ArrayList;

/**
 * Created by BaojunCZ on 2018/8/3.
 */
public class CurrencyIdList {
    private ArrayList<CurrencyId> data = new ArrayList<>();

    public ArrayList<CurrencyId> getList() {
        return data;
    }

    public void setList(ArrayList<CurrencyId> data) {
        this.data = data;
    }
}
