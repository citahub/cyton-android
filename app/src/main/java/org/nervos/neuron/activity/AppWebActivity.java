package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.dialog.SimpleDialog;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.web.WebAppUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AppWebActivity extends BaseActivity {

    public static final String EXTRA_PAYLOAD = "extra_payload";
    public static final String EXTRA_CHAIN = "extra_chain";
    public static final String EXTRA_URL = "extra_url";
    private static final int REQUEST_CODE = 0x01;
    public static final int RESULT_CODE_SUCCESS = 0x02;
    public static final int RESULT_CODE_FAIL = 0x01;
    public static final int RESULT_CODE_CANCEL = 0x00;

    private WebView webView;
    private TextView titleText;
    private TextView collectText;
    private ProgressBar progressBar;
    private BottomSheetDialog sheetDialog;

    private WalletItem walletItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_web);

        String url = getIntent().getStringExtra(EXTRA_URL);
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);

        initView();
        initWebView();
        webView.loadUrl(url);

        WebAppUtil.getHttpManifest(webView, url);

    }

    private void initView() {
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webview);
        titleText = findViewById(R.id.title_bar_center);
        titleText.setText(R.string.dapp);
        collectText = findViewById(R.id.menu_collect);
        initCollectView();
        findViewById(R.id.title_left_close).setOnClickListener(v -> finish());
        findViewById(R.id.title_bar_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMenuView();
            }
        });
    }

    private void initMenuView() {
        findViewById(R.id.menu_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.menu_background).setVisibility(View.VISIBLE);
        findViewById(R.id.menu_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.menu_layout).setVisibility(View.GONE);
                findViewById(R.id.menu_background).setVisibility(View.GONE);
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
            }
        });
        findViewById(R.id.menu_reload).setOnClickListener(v1 -> {
            webView.reload();
            closeMenuWindow();
        } );
    }

    private void initCollectView() {
        collectText.setText(WebAppUtil.isCollectApp(webView)? getString(R.string.cancel_collect):getString(R.string.collect));
        closeMenuWindow();
    }

    private void closeMenuWindow() {
        findViewById(R.id.menu_layout).setVisibility(View.GONE);
        findViewById(R.id.menu_background).setVisibility(View.GONE);
    }

    private void initWebView() {
        WebAppUtil.initWebSettings(webView.getSettings());
        webView.addJavascriptInterface(new AppHybrid(), "appHybrid");
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView webview, int newProgress) {
                if (newProgress > 20 && newProgress < 80) {
                    injectJs();
                }
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
                LogUtil.d("web progress: " + newProgress);
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                titleText.setText(title);
            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initCollectView();
                injectJs();
            }
        });
    }


    /**
     * inject js file to webview
     */
    private void injectJs() {
//        webView.loadUrl(WebAppUtil.getInjectJs(mActivity));
//        webView.evaluateJavascript(WebAppUtil.getInjectTrust(mActivity), null);

        webView.evaluateJavascript(WebAppUtil.getInjectNervosWeb3(mActivity), null);
        webView.evaluateJavascript(WebAppUtil.getInjectTransactionJs(), null);
    }


    private class AppHybrid {

        @JavascriptInterface
        public void sendTransaction(String tx) {
            if (walletItem == null) {
                Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(mActivity, AddWalletActivity.class));
            } else {
                Intent intent = new Intent(mActivity, PayTokenActivity.class);
                intent.putExtra(EXTRA_PAYLOAD, tx);
                intent.putExtra(EXTRA_CHAIN, WebAppUtil.getChainItem());
                startActivityForResult(intent, REQUEST_CODE);
            }
        }

        @JavascriptInterface
        public void signTransaction(String tx) {
            if (walletItem == null) {
                Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(mActivity, AddWalletActivity.class));
            } else {
                showSignMessageDialog(tx);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (sheetDialog != null && sheetDialog.isShowing()) {
            sheetDialog.dismiss();
        }
        super.onDestroy();
    }

    private void showSignMessageDialog(String tx) {
        sheetDialog = new BottomSheetDialog(mActivity);
        sheetDialog.setCanceledOnTouchOutside(true);
        sheetDialog.setContentView(getSignMessageView(tx));
        sheetDialog.show();
    }

    private View getSignMessageView(String tx) {
        View view = getLayoutInflater().inflate(R.layout.dialog_sign_message, null);
        TextView walletNameText = view.findViewById(R.id.wallet_name);
        TextView walletAddressText = view.findViewById(R.id.wallet_address);
        TextView payOwnerText = view.findViewById(R.id.pay_owner);
        TextView payDataText = view.findViewById(R.id.pay_data);
        CircleImageView photoImage = view.findViewById(R.id.wallet_photo);
        ProgressBar progressBar = view.findViewById(R.id.sign_progress);

        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);
        payOwnerText.setText(WebAppUtil.getChainItem().entry);
        payDataText.setText(tx);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        if (WebAppUtil.getChainItem() != null) {
            payOwnerText.setText(WebAppUtil.getChainItem().provider);
            payDataText.setText(tx);
        }
        view.findViewById(R.id.sign_hex_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.pay_data_left_line).setVisibility(View.VISIBLE);
                view.findViewById(R.id.pay_data_right_line).setVisibility(View.GONE);
                payDataText.setText(tx);
            }
        });

        view.findViewById(R.id.sign_utf8_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.pay_data_left_line).setVisibility(View.GONE);
                view.findViewById(R.id.pay_data_right_line).setVisibility(View.VISIBLE);
                if (Numeric.containsHexPrefix(tx)) {
                    payDataText.setText(NumberUtil.hexToUtf8(tx));
                }
            }
        });
        view.findViewById(R.id.pay_reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetDialog.dismiss();
            }
        });
        view.findViewById(R.id.pay_approve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordConfirmView(progressBar, tx);
            }
        });
        return view;
    }

    private void showPasswordConfirmView(ProgressBar progressBar, String tx) {
        SimpleDialog simpleDialog = new SimpleDialog(mActivity);
        simpleDialog.setTitle(R.string.input_password_hint);
        simpleDialog.setMessageHint("password");
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
                actionSignNervos(password, tx);
                simpleDialog.dismiss();

            }
        });
        simpleDialog.setOnCancelClickListener(() -> simpleDialog.dismiss());
        simpleDialog.show();
    }

    private void actionSignEth(String password, String tx) {
        Observable.fromCallable(new Callable<Sign.SignatureData>() {
            @Override
            public Sign.SignatureData call() {
                try {
                    String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);
                    return Sign.signMessage(tx.getBytes(),
                            ECKeyPair.create(Numeric.toBigInt(privateKey)));
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Sign.SignatureData>() {
            @Override
            public void onCompleted() {
                sheetDialog.dismiss();
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                sheetDialog.dismiss();
            }
            @Override
            public void onNext(Sign.SignatureData signatureData) {
                LogUtil.d("signatureData: " + new String(signatureData.getR()));
            }
        });
    }

    private void actionSignNervos(String password, String tx) {
        Observable.fromCallable(new Callable<org.nervos.web3j.crypto.Sign.SignatureData>() {
            @Override
            public org.nervos.web3j.crypto.Sign.SignatureData call() {
                try {
                    String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);
                    return org.nervos.web3j.crypto.Sign.signMessage(tx.getBytes(),
                            org.nervos.web3j.crypto.ECKeyPair.create(Numeric.toBigInt(privateKey)));
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<org.nervos.web3j.crypto.Sign.SignatureData>() {
            @Override
            public void onCompleted() {
                sheetDialog.dismiss();
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                sheetDialog.dismiss();
            }
            @Override
            public void onNext(org.nervos.web3j.crypto.Sign.SignatureData signatureData) {
                LogUtil.d("signatureData: " + new String(signatureData.get_signature()));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_CODE_CANCEL:
                    webView.evaluateJavascript("javascript:cancelled()", null);
                    break;
                case RESULT_CODE_SUCCESS:
                    LogUtil.d("onSignSuccessful: " + data.getStringExtra(PayTokenActivity.EXTRA_HEX_HASH));
                    webView.evaluateJavascript("javascript:onSignSuccessful('"
                            + data.getStringExtra(PayTokenActivity.EXTRA_HEX_HASH)
                            + "')", null);
                    break;
                case RESULT_CODE_FAIL:
                    webView.evaluateJavascript("javascript:onSignFail('"
                            + data.getStringExtra(PayTokenActivity.EXTRA_PAY_ERROR)
                            + "')", null);
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
