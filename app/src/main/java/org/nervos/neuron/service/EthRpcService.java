package org.nervos.neuron.service;


import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionInfo;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.infura.InfuraHttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class EthRpcService {

    private static WalletItem walletItem;
    private static Web3j service;

    public static void init(Context context) {
        service = Web3jFactory.build(new InfuraHttpService(HttpUrls.ETH_NODE_IP));
        walletItem = DBWalletUtil.getCurrentWallet(context);
    }

    public static double getEthBalance(String address) throws Exception {
        EthGetBalance ethGetBalance = service.ethGetBalance(address,
                DefaultBlockParameterName.LATEST).send();
        if (ethGetBalance != null) {
            return NumberUtil.getEthFromWei(ethGetBalance.getBalance());
        }
        return 0.0;
    }

    public static Observable<BigInteger> getEthGasPrice() {
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() {
                BigInteger gasPrice = ConstUtil.GAS_PRICE;
                try {
                    gasPrice = service.ethGasPrice().send().getGasPrice();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return gasPrice;
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<BigInteger> getEthGasLimit(TransactionInfo transactionInfo) {
        String data = TextUtils.isEmpty(transactionInfo.data)? "" : Numeric.prependHexPrefix(transactionInfo.data);
        Transaction transaction = new Transaction(walletItem.address, null, null,
                null, Numeric.prependHexPrefix(transactionInfo.to),
                transactionInfo.getBigIntegerValue(), data);
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() {
                BigInteger gasLimit = ConstUtil.GAS_ERC20_LIMIT;
                try {
                    EthEstimateGas ethEstimateGas = service.ethEstimateGas(transaction).send();
                    gasLimit = ethEstimateGas.getAmountUsed();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return gasLimit;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<EthSendTransaction> transferEth(String address, double value,
                                       BigInteger gasPrice, BigInteger gasLimit, String data, String password) {
        gasLimit = gasLimit.equals(BigInteger.ZERO) ? ConstUtil.GAS_LIMIT : gasLimit;
        BigInteger finalGasLimit = gasLimit;
        String sdata = data == null? "" : data;
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                EthGetTransactionCount ethGetTransactionCount = service
                        .ethGetTransactionCount(walletItem.address, DefaultBlockParameterName.LATEST).send();
                return ethGetTransactionCount.getTransactionCount();
            }
        }).flatMap(new Func1<BigInteger, Observable<String>>() {
            @Override
            public Observable<String> call(BigInteger nonce) {
                try {
                    String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);
                    Credentials credentials = Credentials.create(privateKey);
                    RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,
                            gasPrice, finalGasLimit, address, NumberUtil.getWeiFromEth(value), sdata);
                    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                    return Observable.just(Numeric.toHexString(signedMessage));
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).flatMap(new Func1<String, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(String hexValue){
                try {
                    EthSendTransaction ethSendTransaction =
                            service.ethSendRawTransaction(hexValue).sendAsync().get();
                    return Observable.just(ethSendTransaction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }


    public static void getTransactionReceipt(String hash) {
        Observable.fromCallable(new Callable<EthGetTransactionReceipt>() {
            @Override
            public EthGetTransactionReceipt call() {
                try {
                    return service.ethGetTransactionReceipt(hash).send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<EthGetTransactionReceipt>() {
            @Override
            public void onCompleted() {

            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
            @Override
            public void onNext(EthGetTransactionReceipt ethGetTransactionReceipt) {
                LogUtil.d("transaction receipt: " + ethGetTransactionReceipt.getTransactionReceipt());
            }
        });
    }


    /**
     * get standard erc20 token info through function hash and parameters
     */
    public static TokenItem getTokenInfo(String contractAddress, String address) {
        try {
            return new TokenItem(getErc20Name(address, contractAddress),
                    getErc20Symbol(address, contractAddress), getErc20Decimal(address, contractAddress),
                    contractAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double getERC20Balance(String contractAddress, String address) throws Exception {
        long decimal = getErc20Decimal(address, contractAddress);
        Transaction balanceCall = Transaction.createEthCallTransaction(address, contractAddress,
                ConstUtil.BALANCEOF_HASH + ConstUtil.ZERO_16 + Numeric.cleanHexPrefix(address));
        String balanceOf = service.ethCall(balanceCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (!TextUtils.isEmpty(balanceOf) && ! ConstUtil.RPC_RESULT_ZERO.equals(balanceOf)) {
            initIntTypes();
            Int256 balance = (Int256) FunctionReturnDecoder.decode(balanceOf, intTypes).get(0);
            double balances = balance.getValue().doubleValue();
            if (decimal == 0) return balance.getValue().doubleValue();
            else return balances/(Math.pow(10, decimal));
        }
        return 0.0;
    }


    public static Observable<EthSendTransaction> transferErc20(TokenItem tokenItem, String address,
                           double value, BigInteger gasPrice, String password) {
        return transferErc20(tokenItem, address, value, gasPrice, ConstUtil.GAS_ERC20_LIMIT, password);
    }


    public static Observable<EthSendTransaction> transferErc20(TokenItem tokenItem, String address,
                    double value, BigInteger gasPrice, BigInteger gasLimit, String password) {
        BigInteger transferValue = getTransferValue(tokenItem, value);
        String data = createTokenTransferData(address, transferValue);
        gasLimit = gasLimit.equals(BigInteger.ZERO) ? ConstUtil.GAS_ERC20_LIMIT : gasLimit;
        BigInteger finalGasLimit = gasLimit;
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                EthGetTransactionCount ethGetTransactionCount = service
                        .ethGetTransactionCount(walletItem.address, DefaultBlockParameterName.LATEST).send();
                return ethGetTransactionCount.getTransactionCount();
            }
        }).flatMap(new Func1<BigInteger, Observable<String>>() {
            @Override
            public Observable<String> call(BigInteger nonce) {
                try {
                    String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);
                    Credentials credentials = Credentials.create(privateKey);
                    RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,
                            gasPrice, finalGasLimit, tokenItem.contractAddress, data);
                    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                    return Observable.just(Numeric.toHexString(signedMessage));
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).flatMap(new Func1<String, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(String hexValue){
                try {
                    EthSendTransaction ethSendTransaction =
                            service.ethSendRawTransaction(hexValue).sendAsync().get();
                    return Observable.just(ethSendTransaction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }


    private static BigInteger getTransferValue(TokenItem tokenItem, double value) {
        StringBuilder sb = new StringBuilder("1");
        for(int i = 0; i < tokenItem.decimals; i++) {
            sb.append("0");
        }
        BigInteger ERC20Decimal = new BigInteger(sb.toString());
        return ERC20Decimal.multiply(BigInteger.valueOf((long)(ConstUtil.LONG_6*value)))
                .divide(BigInteger.valueOf(ConstUtil.LONG_6));
    }


    private static String createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.<Type>asList(new Address(to), new Uint256(tokenAmount));
        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {});
        Function function = new Function("transfer", params, returnTypes);
        return FunctionEncoder.encode(function);
    }

    private static String getErc20Name(String address, String contractAddress) throws IOException {
        Transaction nameCall = Transaction.createEthCallTransaction(address, contractAddress, ConstUtil.NAME_HASH);
        String name = service.ethCall(nameCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (TextUtils.isEmpty(name) || ConstUtil.RPC_RESULT_ZERO.equals(name)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(name, stringTypes).get(0).toString();
    }

    private static String getErc20Symbol(String address, String contractAddress) throws IOException {
        Transaction symbolCall = Transaction.createEthCallTransaction(address, contractAddress, ConstUtil.SYMBOL_HASH);
        String symbol = service.ethCall(symbolCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (TextUtils.isEmpty(symbol) || ConstUtil.RPC_RESULT_ZERO.equals(symbol)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(symbol, stringTypes).get(0).toString();
    }

    private static int getErc20Decimal(String address, String contractAddress) throws IOException {
        Transaction decimalsCall = Transaction.createEthCallTransaction(address, contractAddress, ConstUtil.DECIMALS_HASH);
        String decimals = service.ethCall(decimalsCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (!TextUtils.isEmpty(decimals) && !ConstUtil.RPC_RESULT_ZERO.equals(decimals)) {
            initIntTypes();
            Int256 type = (Int256) FunctionReturnDecoder.decode(decimals, intTypes).get(0);
            return type.getValue().intValue();
        }
        return 0;
    }


    private static List<TypeReference<Type>> intTypes = new ArrayList<>();
    private static void initIntTypes() {
        intTypes.clear();
        intTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Int256.class;
            }
        });
    }

    private static List<TypeReference<Type>> stringTypes = new ArrayList<>();
    private static void initStringTypes() {
        stringTypes.clear();
        stringTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Utf8String.class;
            }
        });
    }



}
