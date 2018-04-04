package com.cita.wallet.citawallet;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.account.Account;
import org.web3j.protocol.account.CompiledContract;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private Web3j service;
    private MainActivity mainActivity;
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private static final String ACCOUNT = "0dbd369a741319fa5107733e2c9db9929093e3c7";
    private static final String TO_ACCOUNT = "0x546226ed566d0abb215c9db075fc36476888b310";
    private static final String privateKey = "352416e1c910e413768c51390dfd791b414212b7b4fe6b1a18f58007fa894214";
    private static final String CONTRACT_ADDRESS = "0xbb9d906b92da402594be6a126a3e58a64fa5c3e5";

    private Account account;
    private CompiledContract mContract;
    private Subscription blockSubscription;
    private Subscription receiptSubscription;

    private TextView accountText;
    private TextView balanceText;
    private TextView blockNumberText;
    private EditText toAddressEdit;
    private EditText valueEdit;
    private Button sendButton;

    private static Random random;
    private static BigInteger quota;
    private static int version;

    static {
        random = new Random(System.currentTimeMillis());
        quota = BigInteger.valueOf(1000000);
        version = 0;
    }

    private static BigInteger randomNonce() {
        return BigInteger.valueOf(Math.abs(random.nextLong()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainActivity = this;

        service = Web3j.build(new HttpService("http://192.168.2.10:1337"));

        initView();
        filterNewBlock();
        initAccount();

    }


    private void initView() {
        accountText = findViewById(R.id.cita_account);
        balanceText = findViewById(R.id.account_balance);
        blockNumberText = findViewById(R.id.block_number);
        toAddressEdit = findViewById(R.id.edit_to_address);
        valueEdit = findViewById(R.id.edit_transfer_value);
        sendButton = findViewById(R.id.send);

        accountText.setText(String.format("账号： \n%s", ACCOUNT));
        toAddressEdit.setText(TO_ACCOUNT);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(toAddressEdit.getText().toString())) {
                    Toast.makeText(mainActivity, "目标地址不能为空", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(valueEdit.getText().toString())) {
                    Toast.makeText(mainActivity, "转账金额不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    setSendButtonTheme(ButtonTheme.SENDING);
                    String toAddress = toAddressEdit.getEditableText().toString();
                    long value = Long.parseLong(valueEdit.getEditableText().toString());
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            transfer(toAddress, value);
                        }
                    }.start();
                }
            }
        });
    }

    private void initAccount() {
        account = new Account(privateKey, service);
        cachedThreadPool.execute(() -> {
            try {
                String abi = account.getAbi(CONTRACT_ADDRESS);
                mContract = new CompiledContract(abi);
                getBalance();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void filterNewBlock() {
        cachedThreadPool.execute(() ->
            service.catchUpToLatestAndSubscribeToNewBlocksObservable(DefaultBlockParameter.valueOf("latest"), false)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<EthBlock>() {
                @Override
                public void onCompleted() {

                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }
                @Override
                public void onNext(EthBlock ethBlock) {
                    blockNumberText.setText(String.format("当前块高度： %s", ethBlock.getBlock().getHeader().getNumberDec()));
                }
            }));

    }

    private void getBalance() {
        cachedThreadPool.execute(() -> {
            try {
                AbiDefinition balanceOf = mContract.getFunctionAbi("balanceOf", 1);
                Object object = account.callContract(CONTRACT_ADDRESS, balanceOf, randomNonce(), quota, version, ACCOUNT);
                balanceText.post(() -> balanceText.setText(String.format("余额： %s CIT", object.toString())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void transfer(String address, long value) {
        try {
            AbiDefinition transfer = mContract.getFunctionAbi("transfer", 2);
            EthSendTransaction ethSendTransaction = (EthSendTransaction)account.callContract(CONTRACT_ADDRESS,
                    transfer, randomNonce(), quota, version, address, BigInteger.valueOf(value));
//            asyncGetReceipt(ethSendTransaction.getSendTransactionResult().getHash(), new ReceiptListener() {
//                @Override
//                public void getReceipt(EthGetTransactionReceipt receipt) {
//                    Toast.makeText(mainActivity, "Transfer success", Toast.LENGTH_SHORT).show();
//                    valueEdit.setText("");
//                    getBalance();
//                }
//                @Override
//                public void onError(Throwable e) {
//                    e.printStackTrace();
//                    setSendButtonTheme(ButtonTheme.NORMAL);
//                    Toast.makeText(mainActivity, "Transfer fail", Toast.LENGTH_SHORT).show();
//                }
//            });
            Thread.sleep(6000);
            service.ethGetTransactionReceipt(ethSendTransaction.getSendTransactionResult().getHash())
                .observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<EthGetTransactionReceipt>() {
                    @Override
                    public void onCompleted() {
                        setSendButtonTheme(ButtonTheme.NORMAL);
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        setSendButtonTheme(ButtonTheme.NORMAL);
                        Toast.makeText(mainActivity, "Transfer fail", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onNext(EthGetTransactionReceipt ethGetTransactionReceipt) {
                        if(ethGetTransactionReceipt.getTransactionReceipt() != null) {
                            Toast.makeText(mainActivity, "Transfer success", Toast.LENGTH_SHORT).show();
                            valueEdit.setText("");
                            getBalance();
                        } else {
                            Toast.makeText(mainActivity, "Transfer fail", Toast.LENGTH_SHORT).show();
                            valueEdit.setText("");
                            getBalance();
                        }
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private interface ReceiptListener {
        void getReceipt(EthGetTransactionReceipt receipt);
        void onError(Throwable e);
    }

    private void asyncGetReceipt(String hash, ReceiptListener listener) {
        cachedThreadPool.execute(() ->
             receiptSubscription = service.catchUpToLatestAndSubscribeToNewBlocksObservable(DefaultBlockParameter.valueOf("latest"), false)
            .filter(ethBlock -> ethBlock != null)
            .flatMap(ethBlock -> service.ethGetTransactionReceipt(hash).observable())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<EthGetTransactionReceipt>() {
                @Override
                public void onCompleted() {

                }
                @Override
                public void onError(Throwable e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                }
                @Override
                public void onNext(EthGetTransactionReceipt receipt) {
                    if (listener != null) {
                        if (receipt.getTransactionReceipt() != null &&
                                receipt.getTransactionReceipt().getBlockHash() != null) {
                            receiptSubscription.unsubscribe();
                            listener.getReceipt(receipt);
                        }
                    }
                }
            }));

    }


    enum ButtonTheme {
        NORMAL, SENDING
    }
    private void setSendButtonTheme(ButtonTheme theme) {
        if (theme == ButtonTheme.NORMAL) {
            sendButton.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorAccent));
            sendButton.setText(R.string.send);
            sendButton.setEnabled(true);
        } else {
            sendButton.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorAccentLight));
            sendButton.setText(R.string.sending);
            sendButton.setEnabled(false);
        }
    }


}