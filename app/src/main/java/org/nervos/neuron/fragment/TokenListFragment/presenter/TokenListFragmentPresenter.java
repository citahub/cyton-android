package org.nervos.neuron.fragment.TokenListFragment.presenter;

import android.app.Activity;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.CurrencyActivity;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.SharePreConst;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/8/2.
 */
public class TokenListFragmentPresenter {

    private Activity activity;

    public TokenListFragmentPresenter(Activity activity) {
        this.activity = activity;
    }

    public String getTotalMoney(List<TokenItem> tokenItemList) {
        if (tokenItemList.size() == 0) {
            return "0";
        } else {
            double money = 0;
            for (TokenItem tokenItem : tokenItemList) {
                money += tokenItem.currencyPrice;
            }
            DecimalFormat df = new DecimalFormat("######0.00");
            return df.format(money);
        }
    }

    public CurrencyItem getCurrencyItem() {
        CurrencyItem currencyItem = null;
        ArrayList<CurrencyItem> list = CurrencyActivity.setArray(activity);
        String currencyName = SharePrefUtil.getString(SharePreConst.Currency, "CNY");
        for (CurrencyItem item : list) {
            if (item.getName().equals(currencyName)) {
                currencyItem = item;
                break;
            }
        }
        if (currencyItem == null)
            currencyItem = list.get(0);
        return currencyItem;
    }

}
