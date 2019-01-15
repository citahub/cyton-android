package com.cryptape.cita_wallet.item;

/**
 * Created by BaojunCZ on 2018/8/3.
 */
public class Currency {
    private String name, unit, symbol;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
