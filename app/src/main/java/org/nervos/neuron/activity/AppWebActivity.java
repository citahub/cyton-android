package org.nervos.neuron.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.nervos.neuron.R;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TitleItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.plugin.NeuronDAppPlugin;
import org.nervos.neuron.service.HttpUrls;
import org.nervos.neuron.service.NeuronSubscriber;
import org.nervos.neuron.service.SignService;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.web.WebAppUtil;
import org.nervos.neuron.view.WebErrorView;
import org.nervos.neuron.view.WebMenuPopupWindow;
import org.nervos.neuron.view.dialog.SignDialog;
import org.nervos.neuron.view.webview.NeuronWebView;
import org.nervos.neuron.view.webview.item.Address;
import org.nervos.neuron.view.webview.item.Message;
import org.nervos.neuron.view.webview.item.Transaction;

import rx.Observable;
import rx.Subscriber;

public class AppWebActivity extends NBaseActivity {

    public static final String EXTRA_PAYLOAD = "extra_payload";
    public static final String EXTRA_CHAIN = "extra_chain";
    public static final String EXTRA_URL = "extra_url";
    public static final String RECEIVER_WEBSITE = "RECEIVER_WEBSITE";
    private static final int REQUEST_CODE = 0x01;
    public static final int RESULT_CODE_SUCCESS = 0x02;
    public static final int RESULT_CODE_FAIL = 0x01;
    public static final int RESULT_CODE_CANCEL = 0x00;

    private NeuronWebView webView;
    private TextView titleText;
    private ProgressBar progressBar;
    private SignDialog mSignDialog;
    private ImageView rightMenuView;
    private ImageView leftView;
    private WebErrorView webErrorView;

    private WalletItem walletItem;
    private TitleItem titleItem;
    private Transaction signTransaction;
    private String url;
    private boolean isPersonalSign = false;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_app_web;
    }

    @Override
    protected void initView() {
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webview);
        titleText = findViewById(R.id.title_bar_center);
        rightMenuView = findViewById(R.id.title_bar_right);
        leftView = findViewById(R.id.title_left_close);
        webErrorView = findViewById(R.id.view_web_error);
    }

    @Override
    protected void initData() {
        url = getIntent().getStringExtra(EXTRA_URL);
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);

        if (walletItem == null || TextUtils.isEmpty(walletItem.address)) {
            Toast.makeText(mActivity, R.string.no_wallet_suggestion
                    , Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mActivity, AddWalletActivity.class));
        }
        WebAppUtil.init();
        webView.loadUrl(url);
        initManifest(url);
        initWebView();
    }

    @Override
    protected void initAction() {
        leftView.setOnClickListener(v -> {
            if (titleItem != null && TextUtils.equals(TitleItem.ACTION_BACK
                    , titleItem.left.type)) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        });
        rightMenuView.setOnClickListener(v -> initMenuView());
        webErrorView.setImpl(() -> {
            webView.reload();
            webView.setVisibility(View.VISIBLE);
            webErrorView.setVisibility(View.GONE);
        });
    }

    private void initWebView() {
        SensorsDataAPI.sharedInstance().showUpWebView(webView, false
                , true);
        initInjectWebView();
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView webview, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                    initTitleView();
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
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                titleItem = null;
                initManifest(url);
                if (url.startsWith("weixin://") || url.startsWith("alipay")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        PackageManager packageManager = getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent);
                            return true;
                        }
                    } catch (Exception e) {
                    }
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request
                    , WebResourceError error) {
                webView.post(() -> {
                    webErrorView.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                });
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description
                    , String failingUrl) {
                webView.post(() -> {
                    webErrorView.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                });
            }

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMenuView() {
        WebMenuPopupWindow popupWindow = new WebMenuPopupWindow(this);
        popupWindow.showAsDropDown(rightMenuView, 0, 10);
        popupWindow.setCollectText(WebAppUtil.isCollectApp(webView) ?
                getString(R.string.cancel_collect) : getString(R.string.collect));
        popupWindow.setListener(new WebMenuPopupWindow.WebMenuListener() {
            @Override
            public void reload(PopupWindow pop) {
                webView.reload();
                pop.dismiss();
            }

            @Override
            public void collect(WebMenuPopupWindow pop) {
                if (WebAppUtil.isCollectApp(webView)) {
                    WebAppUtil.cancelCollectApp(webView);
                } else {
                    WebAppUtil.collectApp(webView);
                }
                pop.setCollectText(WebAppUtil.isCollectApp(webView) ?
                        getString(R.string.cancel_collect) : getString(R.string.collect));
                pop.dismiss();
            }
        });
        findViewById(R.id.menu_background).setVisibility(View.VISIBLE);
        getWindow().setStatusBarColor(getResources().getColor(R.color.grey_background));
        popupWindow.setOnDismissListener(() -> {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            findViewById(R.id.menu_background).setVisibility(View.GONE);
        });
    }

    private void initViewWhenWebFinish() {
        WebAppUtil.setAppItem(webView);
        WebAppUtil.addHistory();
    }

    private void initTitleView() {
        if (titleItem == null) {
            leftView.setImageResource(R.drawable.title_close);
            rightMenuView.setVisibility(View.VISIBLE);
            rightMenuView.setImageResource(R.drawable.title_more);
        }
    }

    private void initManifest(String url) {
        WebAppUtil.getHttpManifest(webView, url)
                .subscribe(new NeuronSubscriber<ChainItem>() {
                    @Override
                    public void onError(Throwable e) {
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
                            Toast.makeText(webView.getContext(), chainItem.errorMessage
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signTxAction(Transaction transaction) {
        handler.post(() -> {
            this.signTransaction = transaction;
            if (walletItem == null) {
                Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(mActivity, AddWalletActivity.class));
            } else {
                Intent intent = new Intent(mActivity, PayTokenActivity.class);
                intent.putExtra(EXTRA_PAYLOAD, new Gson().toJson(transaction));
                intent.putExtra(EXTRA_CHAIN, WebAppUtil.getAppItem() == null ?
                        new AppItem(url) : WebAppUtil.getAppItem());
                intent.putExtra(RECEIVER_WEBSITE, webView.getUrl());
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    private void initInjectWebView() {
        webView.setChainId(1);
        webView.setRpcUrl(HttpUrls.getEthNodeIP());
        webView.setWalletAddress(new Address(walletItem.address));
        webView.addJavascriptInterface(new NeuronDAppPlugin(mActivity), "neuron");
        webView.addJavascriptInterface(new WebTitleBar(), "webTitleBar");
        webView.setOnSignTransactionListener(transaction -> {
            signTxAction(transaction);
        });
        webView.setOnSignMessageListener(message -> {
            isPersonalSign = false;
            showSignMessageDialog(message);
        });
        webView.setOnSignPersonalMessageListener(message -> {
            isPersonalSign = true;
            showSignMessageDialog(message);
        });
    }

    public class WebTitleBar {

        @JavascriptInterface
        public void getTitleBar(String data) {

            titleItem = new Gson().fromJson(data, TitleItem.class);

            if (titleItem.right != null) {
                rightMenuView.setVisibility(titleItem.right.isShow ? View.VISIBLE : View.INVISIBLE);
                if (TitleItem.ACTION_MENU.equals(titleItem.right.type)) {
                    rightMenuView.setImageResource(R.drawable.title_more);
                } else if (TitleItem.ACTION_SHARE.equals(titleItem.right.type)) {
                    rightMenuView.setImageResource(R.drawable.share);
                }
            }

            if (titleItem.left != null && !TextUtils.isEmpty(titleItem.left.type)) {
                if (TitleItem.ACTION_BACK.equals(titleItem.left.type)) {
                    leftView.setImageResource(R.drawable.black_back);
                } else if (TitleItem.ACTION_CLOSE.equals(titleItem.left.type)) {
                    leftView.setImageResource(R.drawable.title_close);
                }
            }

            if (titleItem.title != null) {
                if (!TextUtils.isEmpty(titleItem.title.name)) {
                    titleText.setText(titleItem.title.name);
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        if (mSignDialog != null && mSignDialog.isShowing()) {
            mSignDialog.dismiss();
        }
        webView = null;
        super.onDestroy();
    }

    private void showSignMessageDialog(Message<Transaction> message) {
        if (walletItem == null) {
            Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mActivity, AddWalletActivity.class));
        } else {
            mSignDialog = new SignDialog(mActivity, message, mSignListener);
        }
    }

    private SignDialog.OnSignDataListener mSignListener = new SignDialog.OnSignDataListener() {
        @Override
        public void send(String pwd, ProgressBar progressBar, Message<Transaction> message) {
            showPasswordConfirmView(pwd, progressBar, message);
        }

        @Override
        public void reject(Message<Transaction> message) {
            webView.onSignCancel(message);
        }
    };

    private void showPasswordConfirmView(String password, ProgressBar progressBar
            , Message<Transaction> message) {
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
            return;
        } else if (!WalletService.checkPassword(mActivity, password, walletItem)) {
            Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        if (Transaction.TYPE_ETH.equals(message.value.chainType)) {
            actionSignEth(password, message);
        } else if (Transaction.TYPE_APPCHAIN.equals(message.value.chainType)) {
            actionSignAppChain(password, message);
        }
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
                mSignDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mSignDialog.dismiss();
                webView.onSignError(message, e.getMessage());
            }

            @Override
            public void onNext(String hexSign) {
                webView.onSignMessageSuccessful(message, hexSign);
            }
        });
    }

    private void actionSignAppChain(String password, Message<Transaction> message) {
        SignService.signAppChainMessage(mActivity, message.value.data, password)
                .subscribe(new NeuronSubscriber<String>() {
                    @Override
                    public void onError(Throwable e) {
                        mSignDialog.dismiss();
                        webView.onSignError(message, e.getMessage());
                    }

                    @Override
                    public void onNext(String hexSign) {
                        mSignDialog.dismiss();
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
