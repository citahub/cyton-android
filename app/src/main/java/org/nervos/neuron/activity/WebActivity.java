package org.nervos.neuron.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.util.Log;
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
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.web.WebUtil;
import org.nervos.neuron.util.db.DBWalletUtil;

import de.hdodenhof.circleimageview.CircleImageView;

public class WebActivity extends BaseActivity {

    public static final String EXTRA_PAYLOAD = "extra_payload";

    private WebView webView;
    private TextView titleText;
    private TextView collectText;

    private WalletItem walletItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        String url = getIntent().getStringExtra(AddWebsiteActivity.EXTRA_URL);
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);

        initTitleView();
        initWebView();
        webView.loadUrl(url);
        WebUtil.getHtmlManifest(this, url);

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
                if (WebUtil.isCollectApp(mActivity)) {
                    WebUtil.cancelCollectApp(mActivity);
                } else {
                    WebUtil.collectApp(mActivity);
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
        collectText.setText(WebUtil.isCollectApp(mActivity)? "取消收藏":"收藏");
        closeMenuWindow();
    }

    private void closeMenuWindow() {
        findViewById(R.id.menu_layout).setVisibility(View.GONE);
        findViewById(R.id.menu_background).setVisibility(View.GONE);
    }


    private void initWebView() {
        webView = findViewById(R.id.webview);
        WebUtil.initWebSettings(webView.getSettings());
        webView.addJavascriptInterface(new AppHybrid(), "appHybrid");
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView webview, int newProgress) {
                if (newProgress <= 25) {
                    injectJs();
                }
                Log.d("Web", "progress: " + newProgress);
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
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                injectJs();
            }
        });
    }


    /**
     * inject js file to webview
     */
    private void injectJs() {
        webView.evaluateJavascript(WebUtil.getWeb3Js(this), null);
        webView.evaluateJavascript(WebUtil.getInjectJs(), null);
    }


    private class AppHybrid {

        @JavascriptInterface
        public void showTransaction(String payload) {
            if (walletItem == null) {
                Toast.makeText(mActivity, "您还没有钱包，请先创建或者导入钱包", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(mActivity, AddWalletActivity.class));
            } else {
                Intent intent = new Intent(mActivity, PayTokenActivity.class);
                intent.putExtra(EXTRA_PAYLOAD, payload);
                startActivity(intent);
            }
        }
    }


    private void showSignMessageDialog() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(mActivity);
        sheetDialog.setCancelable(false);
        sheetDialog.setContentView(getSignMessageView(sheetDialog));
        sheetDialog.show();
    }

    private View getSignMessageView(BottomSheetDialog sheetDialog) {
        View view = getLayoutInflater().inflate(R.layout.dialog_sign_message, null);
        TextView walletNameText = view.findViewById(R.id.wallet_name);
        TextView walletAddressText = view.findViewById(R.id.wallet_address);
        TextView payOwnerText = view.findViewById(R.id.pay_owner);
        TextView payDataText = view.findViewById(R.id.pay_data);
        CircleImageView photoImage = view.findViewById(R.id.wallet_photo);
        ProgressBar progressBar = view.findViewById(R.id.sign_progress);

        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        ChainItem chainItem = WebUtil.getChainItem();
        if (chainItem != null) {
            payOwnerText.setText(chainItem.provider);
            payDataText.setText("");
        }

        view.findViewById(R.id.pay_reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetDialog.dismiss();
            }
        });

        view.findViewById(R.id.pay_approve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
