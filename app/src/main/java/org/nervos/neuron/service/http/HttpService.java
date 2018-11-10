package org.nervos.neuron.service.http;

import android.content.Context;

import com.google.gson.Gson;

import org.nervos.appchain.protocol.core.methods.response.AppMetaData;
import org.nervos.neuron.BuildConfig;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.item.response.AppChainERC20TransferResponse;
import org.nervos.neuron.item.response.EthTransactionResponse;
import org.nervos.neuron.item.response.AppChainTransactionResponse;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class HttpService {

    private static final int OFFSET = 20;

    private static OkHttpClient mOkHttpClient;

    public static OkHttpClient getHttpClient() {
        if (mOkHttpClient == null) {
            if (BuildConfig.IS_DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                mOkHttpClient = new OkHttpClient.Builder().addInterceptor(logging).build();
            } else {
                mOkHttpClient = new OkHttpClient.Builder().build();
            }
        }
        return mOkHttpClient;
    }

    public static Observable<List<TransactionItem>> getEtherTransactionList(Context context, int page) {
        return Observable.just(DBWalletUtil.getCurrentWallet(context))
                .flatMap(new Func1<WalletItem, Observable<List<TransactionItem>>>() {
                    @Override
                    public Observable<List<TransactionItem>> call(WalletItem walletItem) {
                        try {
                            String ethUrl = String.format(HttpEtherUrls.getEtherTransactionUrl(), walletItem.address, page, OFFSET);
                            final Request ethRequest = new Request.Builder().url(ethUrl).build();
                            Call ethCall = HttpService.getHttpClient().newCall(ethRequest);
                            EthTransactionResponse response = new Gson().fromJson(ethCall.execute().body().string(),
                                    EthTransactionResponse.class);
                            List<TransactionItem> transactionItemList = response.result;
                            for (TransactionItem item : transactionItemList) {
                                item.chainName = ConstUtil.ETH_MAINNET;
                                item.value = (NumberUtil.getEthFromWeiForStringDecimal8Sub(new BigInteger(item.value)));
                                item.symbol = ConstUtil.ETH;
                                item.nativeSymbol = ConstUtil.ETH;
                            }
                            return Observable.just(transactionItemList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return Observable.just(null);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<List<TransactionItem>> getEtherERC20TransactionList(Context context, TokenItem tokenItem, int page) {
        return Observable.just(DBWalletUtil.getCurrentWallet(context))
                .flatMap(new Func1<WalletItem, Observable<List<TransactionItem>>>() {
                    @Override
                    public Observable<List<TransactionItem>> call(WalletItem walletItem) {
                        try {
                            String ethUrl = String.format(HttpEtherUrls.getEtherERC20TransactionUrl(),
                                    tokenItem.contractAddress, walletItem.address, page, OFFSET);

                            final Request ethRequest = new Request.Builder().url(ethUrl).build();
                            Call ethCall = HttpService.getHttpClient().newCall(ethRequest);
                            EthTransactionResponse response = new Gson().fromJson(ethCall.execute().body().string(),
                                    EthTransactionResponse.class);
                            List<TransactionItem> transactionItemList = response.result;
                            for (TransactionItem item : transactionItemList) {
                                item.chainName = ConstUtil.ETH_MAINNET;
                                item.value = (NumberUtil.divideDecimal8Sub(
                                        new BigDecimal(item.value), tokenItem.decimals));
                                item.symbol = tokenItem.symbol;
                                item.nativeSymbol = ConstUtil.ETH;
                            }
                            return Observable.just(transactionItemList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return Observable.just(null);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<List<TransactionItem>> getAppChainTransactionList(Context context, int page) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable(new Callable<AppMetaData.AppMetaDataResult>() {
            @Override
            public AppMetaData.AppMetaDataResult call() {
                AppChainRpcService.init(context, HttpUrls.APPCHAIN_NODE_URL);
                return Objects.requireNonNull(AppChainRpcService.getMetaData()).getAppMetaDataResult();
            }
        }).flatMap(new Func1<AppMetaData.AppMetaDataResult, Observable<List<TransactionItem>>>() {
            @Override
            public Observable<List<TransactionItem>> call(AppMetaData.AppMetaDataResult result) {
                try {
                    String appChainUrl = String.format(HttpUrls.APPCHAIN_TRANSACTION_URL,
                            walletItem.address, page, OFFSET);
                    final Request appChainRequest = new Request.Builder().url(appChainUrl).build();
                    Call appChainCall = HttpService.getHttpClient().newCall(appChainRequest);

                    AppChainTransactionResponse response = new Gson().fromJson(appChainCall.execute().body().string(),
                            AppChainTransactionResponse.class);
                    for (TransactionItem item : response.result.transactions) {
                        item.chainName = result.chainName;
                        item.value = NumberUtil.getEthFromWeiForStringDecimal8Sub(Numeric.toBigInt(item.value));
                        item.symbol = result.tokenSymbol;
                        item.nativeSymbol = result.tokenSymbol;
                    }
                    return Observable.just(response.result.transactions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<List<TransactionItem>> getAppChainERC20TransactionList(Context context, TokenItem tokenItem, int page) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable(new Callable<AppMetaData.AppMetaDataResult>() {
            @Override
            public AppMetaData.AppMetaDataResult call() {
                AppChainRpcService.init(context, HttpUrls.APPCHAIN_NODE_URL);
                return AppChainRpcService.getMetaData().getAppMetaDataResult();
            }
        }).flatMap(new Func1<AppMetaData.AppMetaDataResult, Observable<List<TransactionItem>>>() {
            @Override
            public Observable<List<TransactionItem>> call(AppMetaData.AppMetaDataResult result) {
                try {
                    String appChainUrl = String.format(HttpUrls.APPCHAIN_ERC20_TRANSACTION_URL,
                            tokenItem.contractAddress, walletItem.address, page, OFFSET);
                    final Request appChainRequest = new Request.Builder().url(appChainUrl).build();
                    Call appChainCall = HttpService.getHttpClient().newCall(appChainRequest);

                    AppChainERC20TransferResponse response = new Gson().fromJson(appChainCall.execute().body().string(),
                            AppChainERC20TransferResponse.class);
                    for (TransactionItem item : response.result.transfers) {
                        item.value = (NumberUtil.divideDecimal8Sub(
                                new BigDecimal(item.value), tokenItem.decimals));
                        item.symbol = tokenItem.symbol;
                        item.nativeSymbol = result.tokenSymbol;
                    }
                    return Observable.just(response.result.transfers);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
