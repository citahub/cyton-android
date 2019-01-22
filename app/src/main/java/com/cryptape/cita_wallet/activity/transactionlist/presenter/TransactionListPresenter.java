package com.cryptape.cita_wallet.activity.transactionlist.presenter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.item.Token;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.item.transaction.RestTransaction;
import com.cryptape.cita_wallet.item.transaction.RpcTransaction;
import com.cryptape.cita_wallet.service.http.HttpService;
import com.cryptape.cita_wallet.util.LogUtil;
import com.cryptape.cita_wallet.util.db.CITATransactionsUtil;
import com.cryptape.cita_wallet.util.db.DBEtherTransactionUtil;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.ether.EtherUtil;

import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by BaojunCZ on 2018/10/9.
 */
public class TransactionListPresenter {

    private Activity activity;
    private TransactionListPresenterImpl listener;
    private Token token;

    public TransactionListPresenter(Activity activity, Token token, TransactionListPresenterImpl listener) {
        this.activity = activity;
        this.listener = listener;
        this.token = token;
    }

    public void getTransactionList(int page) {
        Observable<List<RestTransaction>> observable;
        if (isNativeToken(token)) {
            if (Numeric.toBigInt(token.getChainId()).longValue() >
                    1) {       // Now only support chainId = 1 (225 NATT), not support other chainId (> 1)
                getUnofficialNoneData();
                return;
            }
            observable = EtherUtil.isEther(token) ? HttpService.getEtherTransactionList(activity, page) : HttpService.getCITATransactionList(activity, page);
        } else {
            observable = EtherUtil.isEther(token) ? HttpService.getEtherERC20TransactionList(activity, token, page) : HttpService.getCITAERC20TransactionList(activity, token, page);
        }
        observable.subscribe(new Subscriber<List<RestTransaction>>() {
            @Override
            public void onCompleted() {
                listener.hideProgressBar();
                listener.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Toast.makeText(activity, R.string.network_error, Toast.LENGTH_SHORT).show();
                listener.hideProgressBar();
                listener.setRefreshing(false);
            }

            @Override
            public void onNext(List<RestTransaction> list) {
                if (list.size() == 0) {
                    listener.noMoreLoading();
                    return;
                }
                if (EtherUtil.isEther(token)) {
                    list = removeRepetition(list);
                    for (RestTransaction item : list) {
                        item.chainId = EtherUtil.getEtherId();
                        item.chainName = EtherUtil.getEthNodeName();
                        item.status = TextUtils.isEmpty(item.errorMessage) ? RpcTransaction.SUCCESS : RpcTransaction.FAILED;
                    }
                    list = getEtherTransactionList(activity, EtherUtil.getEtherId(), list);
                    if (page == 0) {
                        listener.updateNewList(list);
                    } else {
                        listener.refreshList(list);
                    }
                } else {
                    for (RestTransaction item : list) {
                        item.status = TextUtils.isEmpty(item.errorMessage) ? RpcTransaction.SUCCESS : RpcTransaction.FAILED;
                    }
                    list = getCITATransactionList(activity, token.getChainId(), list);
                    if (page == 0) {
                        listener.updateNewList(list);
                    } else {
                        listener.refreshList(list);
                    }
                }
            }
        });
    }


    private List<RestTransaction> getEtherTransactionList(Context context, String chainId, List<RestTransaction> list) {
        Wallet wallet = DBWalletUtil.getCurrentWallet(context);
        List<RpcTransaction> itemList = DBEtherTransactionUtil.getAllTransactionsWithToken(context, chainId, token.contractAddress);
        if (itemList.size() > 0) {
            Iterator<RpcTransaction> iterator = itemList.iterator();
            while (iterator.hasNext()) {
                RpcTransaction dbItem = iterator.next();
                for (RestTransaction item : list) {
                    if (!wallet.address.equals(dbItem.from) && !wallet.address.equals(dbItem.to)) {
                        iterator.remove();
                        break;
                    }
                    if (item.hash.equalsIgnoreCase(dbItem.hash) || dbItem.getTimestamp() < list.get(list.size() - 1).getTimestamp()) {
                        iterator.remove();
                        break;
                    }
                }
            }
            for (RpcTransaction item : itemList) {
                list.add(new RestTransaction(item));
            }
        }
        Collections.sort(list, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        return list;
    }

    private List<RestTransaction> getCITATransactionList(Context context, String chainId, List<RestTransaction> list) {
        Wallet wallet = DBWalletUtil.getCurrentWallet(context);
        List<RpcTransaction> itemList = CITATransactionsUtil.getAllTransactionsWithToken(context, chainId, token.contractAddress);
        if (itemList.size() > 0) {
            Iterator<RpcTransaction> iterator = itemList.iterator();
            while (iterator.hasNext()) {
                RpcTransaction dbItem = iterator.next();
                for (RestTransaction item : list) {
                    if (!wallet.address.equals(dbItem.from) && !wallet.address.equals(dbItem.to)) {
                        iterator.remove();
                        break;
                    }
                    if (item.hash.equalsIgnoreCase(dbItem.hash) || dbItem.getTimestamp() < list.get(list.size() - 1).getTimestamp()) {
                        iterator.remove();
                        break;
                    }
                }
            }
            for (RpcTransaction item : itemList) {
                list.add(new RestTransaction(item));
            }
        }
        Collections.sort(list, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        return list;
    }


    public static List<RestTransaction> removeRepetition(List<RestTransaction> list) {
        List<RestTransaction> tempList = new ArrayList<>();
        for (RestTransaction item : list) {
            if (!tempList.contains(item)) {
                tempList.add(item);
            }
        }
        return tempList;
    }

    private void getUnofficialNoneData() {
        listener.hideProgressBar();
        listener.setRefreshing(false);
    }

    public boolean isNativeToken(Token token) {
        return TextUtils.isEmpty(token.contractAddress);
    }

    public interface TransactionListPresenterImpl {
        void hideProgressBar();

        void setRefreshing(boolean refreshing);

        void updateNewList(List<RestTransaction> list);

        void refreshList(List<RestTransaction> list);

        void noMoreLoading();
    }

}
