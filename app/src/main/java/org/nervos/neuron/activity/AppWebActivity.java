package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.nervos.neuron.R;
import org.nervos.neuron.dialog.SimpleDialog;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.HttpUrls;
import org.nervos.neuron.service.SignService;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.web.WebAppUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.webview.OnSignPersonalMessageListener;
import org.nervos.neuron.webview.item.Address;
import org.web3j.utils.Numeric;


import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.Subscriber;
import org.nervos.neuron.webview.OnSignMessageListener;
import org.nervos.neuron.webview.Web3View;
import org.nervos.neuron.webview.item.Message;
import org.nervos.neuron.webview.item.Transaction;

import java.util.ArrayList;
import java.util.List;

public class AppWebActivity extends BaseActivity {

    public static final String EXTRA_PAYLOAD = "extra_payload";
    public static final String EXTRA_CHAIN = "extra_chain";
    public static final String EXTRA_URL = "extra_url";
    private static final int REQUEST_CODE = 0x01;
    public static final int RESULT_CODE_SUCCESS = 0x02;
    public static final int RESULT_CODE_FAIL = 0x01;
    public static final int RESULT_CODE_CANCEL = 0x00;

    private Web3View webView;
    private TextView titleText;
    private TextView collectText;
    private ProgressBar progressBar;
    private BottomSheetDialog sheetDialog;
    private ImageView rightMenuView;

    private WalletItem walletItem;
    private Transaction signTransaction;
    private String url;
    private boolean isPersonalSign = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_web);

        initData();
        initView();
        initWebView();
        initInjectWebView();
        webView.loadUrl(url);
        initManifest();

    }

    private void initData() {
        url = getIntent().getStringExtra(EXTRA_URL);
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);

        if (walletItem == null || TextUtils.isEmpty(walletItem.address)) {
            Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mActivity, AddWalletActivity.class));
        }

        WebAppUtil.init();
    }

    private void initView() {
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webview);
        titleText = findViewById(R.id.title_bar_center);
        titleText.setText(R.string.dapp);
        collectText = findViewById(R.id.menu_collect);
        rightMenuView = findViewById(R.id.title_bar_right);
        findViewById(R.id.title_left_close).setOnClickListener(v -> finish());
        rightMenuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMenuView();
            }
        });
    }

    private void initWebView() {
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView webview, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                titleText.setText(title);
                initViewWhenWebFinish();
            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
    }

    private void initMenuView() {
        showMenuWindow();
        initCollectView();
        findViewById(R.id.menu_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenuWindow();
            }
        });
        findViewById(R.id.menu_collect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WebAppUtil.isCollectApp(webView)) {
                    WebAppUtil.cancelCollectApp(webView);
                } else {
                    WebAppUtil.collectApp(webView);
                }
                initCollectView();
                closeMenuWindow();
            }
        });
        findViewById(R.id.menu_reload).setOnClickListener(v1 -> {
            webView.reload();
            closeMenuWindow();
        } );
    }

    private void initViewWhenWebFinish() {
        showRightMenu();
        WebAppUtil.setAppItem(webView);
        WebAppUtil.addHistory();
    }

    private void showRightMenu() {
        rightMenuView.setVisibility(View.VISIBLE);
    }

    private void initCollectView() {
        collectText.setText(WebAppUtil.isCollectApp(webView)?
                getString(R.string.cancel_collect):getString(R.string.collect));
    }

    private void closeMenuWindow() {
        findViewById(R.id.menu_layout).setVisibility(View.GONE);
        findViewById(R.id.menu_background).setVisibility(View.GONE);
    }

    private void showMenuWindow() {
        findViewById(R.id.menu_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.menu_background).setVisibility(View.VISIBLE);
    }

    private void initManifest() {
        WebAppUtil.getHttpManifest(webView, url)
            .subscribe(new Subscriber<ChainItem>() {
                @Override
                public void onCompleted() { }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    LogUtil.e("manifest error: " + e.getMessage());
                }
                @Override
                public void onNext(ChainItem chainItem) {
                    if (TextUtils.isEmpty(chainItem.errorMessage)) {
                        WebAppUtil.addHistory();
                        DBChainUtil.saveChain(webView.getContext(), chainItem);
                        if (!TextUtils.isEmpty(chainItem.tokenName)) {
                            TokenItem tokenItem = new TokenItem(chainItem);
                            DBWalletUtil.addTokenToAllWallet(webView.getContext(), tokenItem);
                        }
                    } else {
                        Toast.makeText(webView.getContext(), chainItem.errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void signTxAction(Transaction transaction) {
        this.signTransaction = transaction;
        if (walletItem == null) {
            Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mActivity, AddWalletActivity.class));
        } else {
            Intent intent = new Intent(mActivity, PayTokenActivity.class);
            intent.putExtra(EXTRA_PAYLOAD, new Gson().toJson(transaction));
            intent.putExtra(EXTRA_CHAIN, WebAppUtil.getAppItem() == null?
                    new AppItem(url):WebAppUtil.getAppItem());
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    private void initInjectWebView() {
        webView.setChainId(1);
        webView.setRpcUrl(HttpUrls.ETH_NODE_IP);
        webView.setWalletAddress(new Address(walletItem.address));
        webView.addJavascriptInterface(new Neuron(), "neuron");
        webView.setOnSignTransactionListener(transaction -> {
            signTxAction(transaction);
        });
        webView.setOnSignMessageListener(new OnSignMessageListener() {
            @Override
            public void onSignMessage(Message<Transaction> message) {
                isPersonalSign = false;
                showSignMessageDialog(message);
            }
        });
        webView.setOnSignPersonalMessageListener(new OnSignPersonalMessageListener() {
            @Override
            public void onSignPersonalMessage(Message<Transaction> message) {
                isPersonalSign = true;
                showSignMessageDialog(message);
            }
        });
    }

    private class Neuron {

        @JavascriptInterface
        public String getAccount() {
            return walletItem.address;
        }

        @JavascriptInterface
        public String getAccounts() {
            List<WalletItem> walletItems = DBWalletUtil.getAllWallet(mActivity);
            List<String> walletNames = new ArrayList<>();
            for (WalletItem item : walletItems) {
                walletNames.add(item.address);
            }
            return new Gson().toJson(walletNames);
        }
    }


    @Override
    public void onDestroy() {
        if (sheetDialog != null && sheetDialog.isShowing()) {
            sheetDialog.dismiss();
        }
        super.onDestroy();
    }

    private void showSignMessageDialog(Message<Transaction> message) {
        if (walletItem == null) {
            Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mActivity, AddWalletActivity.class));
        } else {
            sheetDialog = new BottomSheetDialog(mActivity);
            sheetDialog.setCanceledOnTouchOutside(true);
            sheetDialog.setContentView(getSignMessageView(message));
            sheetDialog.show();
        }
    }

    private View getSignMessageView(Message<Transaction> message) {
        View view = getLayoutInflater().inflate(R.layout.dialog_sign_message, null);
        TextView walletNameText = view.findViewById(R.id.wallet_name);
        TextView walletAddressText = view.findViewById(R.id.wallet_address);
        TextView payOwnerText = view.findViewById(R.id.pay_owner);
        TextView payDataText = view.findViewById(R.id.pay_data);
        CircleImageView photoImage = view.findViewById(R.id.wallet_photo);
        ProgressBar progressBar = view.findViewById(R.id.sign_progress);

        payDataText.setMovementMethod(ScrollingMovementMethod.getInstance());

        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);
        payOwnerText.setText(WebAppUtil.getAppItem() == null?
                url:WebAppUtil.getAppItem().entry);
        payDataText.setText(message.value.data);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        if (WebAppUtil.getAppItem() != null) {
            payOwnerText.setText(WebAppUtil.getAppItem().provider);
        }
        view.findViewById(R.id.sign_hex_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.pay_data_left_line).setVisibility(View.VISIBLE);
                view.findViewById(R.id.pay_data_right_line).setVisibility(View.GONE);
                payDataText.setText(message.value.data);
            }
        });

        view.findViewById(R.id.sign_utf8_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.pay_data_left_line).setVisibility(View.GONE);
                view.findViewById(R.id.pay_data_right_line).setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(message.value.data) &&
                        Numeric.containsHexPrefix(message.value.data)) {
                    payDataText.setText(NumberUtil.hexToUtf8(message.value.data));
                }
            }
        });
        view.findViewById(R.id.pay_reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetDialog.dismiss();
                webView.onSignCancel(message);
            }
        });
        view.findViewById(R.id.pay_approve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordConfirmView(progressBar, message);
            }
        });
        return view;
    }

    private void showPasswordConfirmView(ProgressBar progressBar, Message<Transaction> message) {
        SimpleDialog simpleDialog = new SimpleDialog(mActivity);
        simpleDialog.setTitle(R.string.input_password_hint);
        simpleDialog.setMessageHint(R.string.input_password_hint);
        simpleDialog.setEditInputType(SimpleDialog.PASSWORD);
        simpleDialog.setOnOkClickListener(new SimpleDialog.OnOkClickListener() {
            @Override
            public void onOkClick() {
                String password = simpleDialog.getMessage();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
                    return;
                } else if (!AESCrypt.checkPassword(password, walletItem)) {
                    Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                simpleDialog.dismiss();
                if (Transaction.TYPE_ETH.equals(message.value.chainType)) {
                    actionSignEth(password, message);
                } else if (Transaction.TYPE_APPCHAIN.equals(message.value.chainType)){
                    actionSignNervos(password, message);
                }
            }
        });
        simpleDialog.setOnCancelClickListener(() -> simpleDialog.dismiss());
        simpleDialog.show();
    }

    private void actionSignEth(String password, Message<Transaction> message) {
        Observable<String> observable;
        if (isPersonalSign) {
            observable = SignService.signPersonalMessage(mActivity,
                    NumberUtil.hexToUtf8(message.value.data), password);
        } else {
            observable = SignService.signEthMessage(mActivity, message.value.data, password);
        }
        observable.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                sheetDialog.dismiss();
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                sheetDialog.dismiss();
                webView.onSignError(message, e.getMessage());
            }
            @Override
            public void onNext(String hexSign) {
                webView.onSignMessageSuccessful(message, hexSign);
            }
        });
    }

    private void actionSignNervos(String password, Message<Transaction> message) {
        SignService.signNervosMessage(mActivity, message.value.data, password)
            .subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {
                    sheetDialog.dismiss();
                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    sheetDialog.dismiss();
                    webView.onSignError(message, e.getMessage());
                }
                @Override
                public void onNext(String hexSign) {
                    webView.onSignMessageSuccessful(message, hexSign);
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_CODE_CANCEL:
                    webView.onSignCancel(signTransaction);
                    break;
                case RESULT_CODE_SUCCESS:
                    webView.onSignTransactionSuccessful(signTransaction,
                            data.getStringExtra(PayTokenActivity.EXTRA_HEX_HASH));
                    break;
                case RESULT_CODE_FAIL:
                    webView.onSignError(signTransaction,
                            data.getStringExtra(PayTokenActivity.EXTRA_PAY_ERROR));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
