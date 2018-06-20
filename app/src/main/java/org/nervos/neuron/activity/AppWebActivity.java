package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
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
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.web.WebAppUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class AppWebActivity extends BaseActivity {

    public static final String EXTRA_PAYLOAD = "extra_payload";
    public static final String EXTRA_CHAIN = "extra_chain";
    public static final String EXTRA_URL = "extra_url";

    private WebView webView;
    private TextView titleText;
    private TextView collectText;
    private ProgressBar progressBar;

    private WalletItem walletItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_web);

        String url = getIntent().getStringExtra(EXTRA_URL);
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);

        initTitleView();
        initWebView();
        webView.loadUrl(url);

        WebAppUtil.getHttpManifest(webView, url);

    }

    private void initTitleView() {
        titleText = findViewById(R.id.title_bar_center);
        titleText.setText("浏览器");
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
                if (WebAppUtil.isCollectApp(mActivity)) {
                    WebAppUtil.cancelCollectApp(mActivity);
                } else {
                    WebAppUtil.collectApp(mActivity);
                }
                initCollectView();
            }
        });
        findViewById(R.id.menu_reload).setOnClickListener(v1 -> {
            webView.reload();
            closeMenuWindow();
        } );
        findViewById(R.id.menu_dapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, "dapp详情", Toast.LENGTH_SHORT).show();
                closeMenuWindow();
            }
        });
    }

    private void initCollectView() {
        collectText.setText(WebAppUtil.isCollectApp(mActivity)? "取消收藏":"收藏");
        closeMenuWindow();
    }

    private void closeMenuWindow() {
        findViewById(R.id.menu_layout).setVisibility(View.GONE);
        findViewById(R.id.menu_background).setVisibility(View.GONE);
    }


    private void initWebView() {
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webview);
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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
//                injectJs();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
    }


    /**
     * inject js file to webview
     */
    private void injectJs() {
//        webView.loadUrl(WebAppUtil.getInjectJs(mActivity));
//        webView.evaluateJavascript(WebAppUtil.getInjectTrust(mActivity), null);

        webView.evaluateJavascript(WebAppUtil.getInjectNervosWeb3(), null);
        webView.evaluateJavascript(WebAppUtil.getInjectTransactionJs(), null);
//        webView.evaluateJavascript(WebAppUtil.getInjectSignJs(), null);
    }


    private class AppHybrid {

        @JavascriptInterface
        public void sendTransaction(String tx) {
            if (walletItem == null) {
                Toast.makeText(mActivity, "您还没有钱包，请先创建或者导入钱包", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(mActivity, AddWalletActivity.class));
            } else {
                Intent intent = new Intent(mActivity, PayTokenActivity.class);
                intent.putExtra(EXTRA_PAYLOAD, tx);
                intent.putExtra(EXTRA_CHAIN, WebAppUtil.getChainItem());
                startActivity(intent);
            }
        }

        @JavascriptInterface
        public void signTransaction(String tx) {
            if (walletItem == null) {
                Toast.makeText(mActivity, "您还没有钱包，请先创建或者导入钱包", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(mActivity, AddWalletActivity.class));
            } else {
                showSignMessageDialog(tx);
            }
        }
    }


    private void showSignMessageDialog(String tx) {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(mActivity);
        sheetDialog.setCancelable(false);
        sheetDialog.setContentView(getSignMessageView(sheetDialog, tx));
        sheetDialog.show();
    }

    private View getSignMessageView(BottomSheetDialog sheetDialog, String tx) {
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
                Sign.SignatureData signatureData = Sign.signMessage(tx.getBytes(),
                        ECKeyPair.create(new BigInteger(walletItem.privateKey)), false);
            }
        });
        return view;
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
