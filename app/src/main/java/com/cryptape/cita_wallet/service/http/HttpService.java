package com.cryptape.cita_wallet.service.http;

import android.content.Context;

import com.cryptape.cita.protocol.core.methods.response.AppMetaData;
import com.google.gson.Gson;

import com.cryptape.cita_wallet.BuildConfig;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.item.Token;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.item.response.CITAERC20Transaction;
import com.cryptape.cita_wallet.item.response.CITATransaction;
import com.cryptape.cita_wallet.item.response.EthTransaction;
import com.cryptape.cita_wallet.item.response.EthTransactionStatus;
import com.cryptape.cita_wallet.item.transaction.RestTransaction;
import com.cryptape.cita_wallet.util.CurrencyUtil;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.ether.EtherUtil;
import com.cryptape.cita_wallet.util.url.HttpCITAUrls;

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

/**
 * Created by duanyytop on 2018/10/15
 */
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

    public static Observable<List<RestTransaction>> getEtherTransactionList(Context context, int page) {
        return Observable.just(DBWalletUtil.getCurrentWallet(context)).flatMap(new Func1<Wallet, Observable<List<RestTransaction>>>() {
            @Override
            public Observable<List<RestTransaction>> call(Wallet wallet) {
                try {
                    String ethUrl = String.format(EtherUtil.getEtherTransactionUrl(), wallet.address, page, OFFSET);
                    final Request ethRequest = new Request.Builder().url(ethUrl).build();
                    Call ethCall = HttpService.getHttpClient().newCall(ethRequest);
                    EthTransaction response = new Gson().fromJson(ethCall.execute().body().string(), EthTransaction.class);
                    List<RestTransaction> transactionRepons = response.result;
                    for (RestTransaction item : transactionRepons) {
                        item.chainName = ConstantUtil.ETH_MAINNET;
                        item.value = (NumberUtil.getEthFromWeiForStringDecimal8(new BigInteger(item.value)));
                        item.symbol = ConstantUtil.ETH;
                        item.nativeSymbol = ConstantUtil.ETH;
                    }
                    return Observable.just(transactionRepons);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<List<RestTransaction>> getEtherERC20TransactionList(Context context, Token token, int page) {
        return Observable.just(DBWalletUtil.getCurrentWallet(context)).flatMap(new Func1<Wallet, Observable<List<RestTransaction>>>() {
            @Override
            public Observable<List<RestTransaction>> call(Wallet wallet) {
                try {
                    String ethUrl = String.format(EtherUtil.getEtherERC20TransactionUrl(), token.contractAddress, wallet.address, page, OFFSET);

                    final Request ethRequest = new Request.Builder().url(ethUrl).build();
                    Call ethCall = HttpService.getHttpClient().newCall(ethRequest);
                    EthTransaction response = new Gson().fromJson(ethCall.execute().body().string(), EthTransaction.class);
                    List<RestTransaction> transactionRepons = response.result;
                    for (RestTransaction item : transactionRepons) {
                        item.chainName = ConstantUtil.ETH_MAINNET;
                        item.value = (NumberUtil.divideDecimalSub(new BigDecimal(item.value), token.decimals));
                        item.symbol = token.symbol;
                        item.nativeSymbol = ConstantUtil.ETH;
                    }
                    return Observable.just(transactionRepons);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<List<RestTransaction>> getCITATransactionList(Context context, int page) {
        Wallet wallet = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable((Callable<AppMetaData.AppMetaDataResult>) () -> {
            CITARpcService.init(context, HttpCITAUrls.CITA_NODE_URL);
            return Objects.requireNonNull(CITARpcService.getMetaData()).getAppMetaDataResult();
        }).flatMap(new Func1<AppMetaData.AppMetaDataResult, Observable<List<RestTransaction>>>() {
            @Override
            public Observable<List<RestTransaction>> call(AppMetaData.AppMetaDataResult result) {
                try {
                    String citaUrl = String.format(HttpCITAUrls.CITA_TRANSACTION_URL, wallet.address, page + 1, OFFSET);
                    final Request citaRequest = new Request.Builder().url(citaUrl).build();
                    Call citaCall = HttpService.getHttpClient().newCall(citaRequest);

                    String res = citaCall.execute().body().string();

                    CITATransaction response = new Gson().fromJson(res, CITATransaction.class);
                    for (RestTransaction item : response.result.transactions) {
                        item.chainName = result.getChainName();
                        item.value = CurrencyUtil.fmtMicrometer(NumberUtil.getEthFromWeiForStringDecimal8(Numeric.toBigInt(item.value)));
                        item.symbol = result.getTokenSymbol();
                        item.nativeSymbol = result.getTokenSymbol();
                    }
                    return Observable.just(response.result.transactions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<List<RestTransaction>> getCITAERC20TransactionList(Context context, Token token, int page) {
        Wallet wallet = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable(new Callable<AppMetaData.AppMetaDataResult>() {
            @Override
            public AppMetaData.AppMetaDataResult call() {
                CITARpcService.init(context, HttpCITAUrls.CITA_NODE_URL);
                return CITARpcService.getMetaData().getAppMetaDataResult();
            }
        }).flatMap(new Func1<AppMetaData.AppMetaDataResult, Observable<List<RestTransaction>>>() {
            @Override
            public Observable<List<RestTransaction>> call(AppMetaData.AppMetaDataResult result) {
                try {
                    String citaUrl = String.format(HttpCITAUrls.CITA_ERC20_TRANSACTION_URL, token.contractAddress, wallet.address, page, OFFSET);
                    final Request citaRequest = new Request.Builder().url(citaUrl).build();
                    Call citaCall = HttpService.getHttpClient().newCall(citaRequest);

                    CITAERC20Transaction response = new Gson().fromJson(citaCall.execute().body().string(), CITAERC20Transaction.class);
                    for (RestTransaction item : response.result.transfers) {
                        item.value = (NumberUtil.divideDecimalSub(new BigDecimal(item.value), token.decimals));
                        item.symbol = token.symbol;
                        item.nativeSymbol = result.getTokenSymbol();
                    }
                    return Observable.just(response.result.transfers);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public static EthTransactionStatus getEthTransactionStatus(String hash) {
        String citaUrl = String.format(EtherUtil.getEtherTransactionStatusUrl(), hash);
        final Request citaRequest = new Request.Builder().url(citaUrl).build();
        Call citaCall = HttpService.getHttpClient().newCall(citaRequest);
        try {
            return new Gson().fromJson(citaCall.execute().body().string(), EthTransactionStatus.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
