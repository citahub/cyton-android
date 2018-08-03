package org.nervos.neuron.fragment.TokenListFragment.presenter;

import android.app.Activity;

import org.nervos.neuron.R;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.SharePreConst;
import org.nervos.neuron.util.db.SharePrefUtil;

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
            int money = 0;
            for (TokenItem tokenItem : tokenItemList) {
                money += tokenItem.balance;
            }
            return money + "";
        }
    }

    public String getTotalMoneyTitle() {
        String str = activity.getResources().getString(R.string.wallet_token_unit_CNY);
        switch (SharePrefUtil.getString(SharePreConst.Currency, "CNY")) {
            case "CNY":
                str = activity.getResources().getString(R.string.wallet_token_unit_CNY);
                break;
            case "USD":
                str = activity.getResources().getString(R.string.wallet_token_unit_USD);
                break;
        }
        return "（" + str + "）";
    }

}
