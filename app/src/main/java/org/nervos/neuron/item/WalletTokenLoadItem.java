package org.nervos.neuron.item;

import android.os.Parcelable;

/**
 * Created by BaojunCZ on 2018/11/19.
 */
public class WalletTokenLoadItem extends TokenItem {

    public boolean loaded = false;

    public WalletTokenLoadItem(TokenItem tokenItem) {
        this.amount = tokenItem.amount;
        this.avatar = tokenItem.avatar;
        this.balance = tokenItem.balance;
        this.chainId = tokenItem.chainId;
        this.chainName = tokenItem.chainName;
        this.contractAddress = tokenItem.contractAddress;
        this.currencyPrice = tokenItem.currencyPrice;
        this.decimals = tokenItem.decimals;
        this.image = tokenItem.image;
        this.name = tokenItem.name;
        this.symbol = tokenItem.symbol;
    }
}
