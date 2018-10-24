package org.nervos.neuron.plugin;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;

import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.db.DBWalletUtil;

import java.util.ArrayList;
import java.util.List;

public class NeuronDAppPlugin {

    private Context mContext;

    public NeuronDAppPlugin(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public String getAccount() {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(mContext);
        return walletItem.address;
    }

    @JavascriptInterface
    public String getAccounts() {
        List<WalletItem> walletItems = DBWalletUtil.getAllWallet(mContext);
        List<String> walletNames = new ArrayList<>();
        for (WalletItem item : walletItems) {
            walletNames.add(item.address);
        }
        return new Gson().toJson(walletNames);
    }

}
