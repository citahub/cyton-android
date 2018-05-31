package org.nervos.neuron.service;

import android.content.Context;
import android.util.Log;

import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.db.DBChainUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WalletService {

    private static ExecutorService executorService = Executors.newFixedThreadPool(1);

    private static final String ETH = "ETH";
    private static final String CITA = "CITA";

    public static void getWalletTokenBalance(Context context, WalletItem walletItem,
                                                   OnGetWalletTokenListener listener) {
        if (walletItem == null || walletItem.tokenItems.size() == 0) return;
        List<TokenItem> tokenItemList = new ArrayList<>();
        Log.d("wallet", "len: " + walletItem.tokenItems.size());
        executorService.execute(() -> {
            for (int i = 0; i < walletItem.tokenItems.size(); i++) {
                TokenItem tokenItem = walletItem.tokenItems.get(i);
                Log.d("wallet", "symbol: " + tokenItem.symbol);
                if (tokenItem.chainId < 0) {
                    if (ETH.equals(tokenItem.symbol)) {
                        tokenItem = EthRpcService.getDefaultEth(walletItem.address);
                        tokenItemList.add(i, tokenItem);
                    } else {
                        double balance = EthRpcService.getERC20Balance(tokenItem.contractAddress, walletItem.address);
                        tokenItem.balance = balance;
                        tokenItemList.add(i, tokenItem);
                    }
                } else {
                    ChainItem chainItem = DBChainUtil.getChain(context, tokenItem.chainId);
                    if (chainItem != null) {
                        String httpProvider = chainItem.httpProvider;
                        CitaRpcService.init(httpProvider);
                        tokenItem = CitaRpcService.getTokenInfo(tokenItem.contractAddress, walletItem.address);
                        tokenItemList.add(i, tokenItem);
                    }
                }
            }
            walletItem.tokenItems = tokenItemList;
            if (listener != null) {
                listener.onGetWalletToken(walletItem);
            }
        });
    }

    public interface OnGetWalletTokenListener {
        void onGetWalletToken(WalletItem walletItem);
    }

}
