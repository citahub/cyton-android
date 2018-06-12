package org.nervos.neuron.service;

import android.content.Context;
import android.util.Log;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.web3j.protocol.Web3j;
import org.nervos.web3j.protocol.account.Account;
import org.nervos.web3j.protocol.account.CompiledContract;
import org.nervos.web3j.protocol.core.DefaultBlockParameter;
import org.nervos.web3j.protocol.core.methods.request.Transaction;
import org.nervos.web3j.protocol.core.methods.response.AbiDefinition;
import org.nervos.web3j.protocol.core.methods.response.EthBlockNumber;
import org.nervos.web3j.protocol.core.methods.response.EthGetBalance;
import org.nervos.web3j.protocol.core.methods.response.EthMetaData;
import org.nervos.web3j.protocol.core.methods.response.EthSendTransaction;
import org.nervos.web3j.protocol.http.HttpService;


import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.security.auth.callback.Callback;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CitaRpcService {

    static final BigInteger NervosDecimal = new BigInteger("1000000000000000000");
    public static final String NODE_IP = "http://47.94.105.230:1337";

    private static Web3j service;
    private static Account account;
    private static CompiledContract mContract;

    private static Random random;
    private static BigInteger quota = BigInteger.valueOf(1000000);
    private static int version = 0;
    private static int chainId = 1;
    private static long chainValue = 0;
    private static WalletItem walletItem;

    public static void init(Context context, String httpProvider) {
        HttpService.setDebug(true);
        service = Web3j.build(new HttpService(httpProvider));
        walletItem = DBWalletUtil.getCurrentWallet(context);
        account = new Account(walletItem.privateKey, service);
    }

    private static BigInteger randomNonce() {
        random = new Random(System.currentTimeMillis());
        return BigInteger.valueOf(Math.abs(random.nextLong()));
    }


    public static TokenItem getTokenInfo(String contractAddress, String address) {
        try {
            String abi = account.getAbi(contractAddress);
            mContract = new CompiledContract(abi);

            TokenItem tokenItem = new TokenItem();
            tokenItem.contractAddress = contractAddress;

            AbiDefinition symbol = mContract.getFunctionAbi("symbol", 0);
            tokenItem.symbol = account.callContract(contractAddress, symbol, randomNonce(),
                    quota, version, chainId, chainValue).toString();

            AbiDefinition decimals = mContract.getFunctionAbi("decimals", 0);
            String decimal = account.callContract(contractAddress, decimals, randomNonce(),
                    quota, version, chainId, chainValue).toString();
            tokenItem.decimals = Integer.parseInt(decimal);

            AbiDefinition nameAbi = mContract.getFunctionAbi("name", 0);
            tokenItem.name = account.callContract(contractAddress, nameAbi, randomNonce(),
                    quota, version, chainId, chainValue).toString();

            AbiDefinition balanceOfAbi = mContract.getFunctionAbi("balanceOf", 1);
            String balance = account.callContract(contractAddress, balanceOfAbi, randomNonce(),
                    quota, version, chainId, chainValue, address).toString();
            tokenItem.balance = Float.valueOf(balance);

            Log.d("wallet", "cita erc20 balance: " + tokenItem.balance);

            return tokenItem;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static EthMetaData getMetaData() {
        try {
            return service.ethMetaData(DefaultBlockParameter.valueOf("latest")).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static double getBalance(String address) {
        try {
            EthGetBalance ethGetBalance =
                    service.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).send();
            return ethGetBalance.getBalance().multiply(BigInteger.valueOf(10000))
                    .divide(NervosDecimal).doubleValue()/10000.0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    public static Observable<EthSendTransaction> transferErc20(TokenItem tokenItem,
                           String contractAddress, String address, double value) throws Exception {
        String abi = account.getAbi(contractAddress);
        mContract = new CompiledContract(abi);
        AbiDefinition transfer = mContract.getFunctionAbi("transfer", 2);
        return Observable.just((EthSendTransaction)account.callContract(contractAddress,
                transfer, randomNonce(), quota, version, chainId, chainValue, address,
                getTransferValue(tokenItem, value)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public static Observable<EthSendTransaction> transferNervos(String toAddress, double value) {
        BigInteger transferValue = NervosDecimal
                .multiply(BigInteger.valueOf((long)(10000*value))).divide(BigInteger.valueOf(10000));
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() {
                return getValidUntilBlock();
            }
        }).flatMap(new Func1<BigInteger, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(BigInteger validUntilBlock) {
                Transaction transaction = Transaction.createFunctionCallTransaction(toAddress, randomNonce(), quota.longValue(),
                        validUntilBlock.longValue(), version, chainId, transferValue.intValue(), "");
                String rawTx = transaction.sign(walletItem.privateKey);
                try {
                    return Observable.just(service.ethSendRawTransaction(rawTx).send());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }

    private static BigInteger getValidUntilBlock() {
        try {
            return BigInteger.valueOf((service.ethBlockNumber().send()).getBlockNumber().longValue() + 80L);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BigInteger.ZERO;
    }

    private static BigInteger getTransferValue(TokenItem tokenItem, double value) {
        StringBuilder sb = new StringBuilder("1");
        for(int i = 0; i < tokenItem.decimals; i++) {
            sb.append("0");
        }
        BigInteger ERC20Decimal = new BigInteger(sb.toString());
        return ERC20Decimal.multiply(BigInteger.valueOf((long)(10000*value)))
                .divide(BigInteger.valueOf(10000));
    }

}
