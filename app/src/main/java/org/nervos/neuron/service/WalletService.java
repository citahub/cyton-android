package org.nervos.neuron.service;

import android.content.Context;
import android.text.TextUtils;

import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.db.DBChainUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WalletService {

    private static ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void getWalletTokenBalance(Context context, WalletItem walletItem,
                                                   OnGetWalletTokenListener listener) {
        if (walletItem == null || walletItem.tokenItems.size() == 0) return;
        List<TokenItem> tokenItemList = new ArrayList<>();
        executorService.execute(() -> {
            Iterator<TokenItem> iterator = walletItem.tokenItems.iterator();
            while(iterator.hasNext()){
                TokenItem tokenItem = iterator.next();
                iterator.remove();
                if (tokenItem.chainId < 0) {                // ethereum
                    if (ConstantUtil.ETH.equals(tokenItem.symbol)) {
                        tokenItem.balance = EthRpcService.getEthBalance(walletItem.address);
                        tokenItemList.add(tokenItem);
                    } else {
                        tokenItem.balance = EthRpcService.getERC20Balance(tokenItem.contractAddress, walletItem.address);;
                        tokenItemList.add(tokenItem);
                    }
                } else {                                    // nervos
                    ChainItem chainItem = DBChainUtil.getChain(context, tokenItem.chainId);
                    if (chainItem != null) {
                        String httpProvider = chainItem.httpProvider;
                        NervosRpcService.init(context, httpProvider);
                        if (!TextUtils.isEmpty(tokenItem.contractAddress)) {
                            tokenItem.balance = NervosRpcService.getErc20Balance(tokenItem, walletItem.address);
                            tokenItemList.add(tokenItem);
                        } else {
                            tokenItem.balance = NervosRpcService.getBalance(walletItem.address);
                            tokenItemList.add(tokenItem);
                        }
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
