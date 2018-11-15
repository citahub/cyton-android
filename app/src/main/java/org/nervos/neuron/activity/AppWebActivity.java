package org.nervos.neuron.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.jetbrains.annotations.NotNull;
import org.nervos.neuron.R;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.dapp.BaseNeuronDAppCallbackItem;
import org.nervos.neuron.item.dapp.QrCodeItem;
import org.nervos.neuron.item.TitleItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.plugin.NeuronDAppPlugin;
import org.nervos.neuron.util.ether.EtherUtil;
import org.nervos.neuron.util.url.HttpEtherUrls;
import org.nervos.neuron.service.http.NeuronSubscriber;
import org.nervos.neuron.service.http.SignService;
import org.nervos.neuron.service.http.WalletService;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.JSLoadUtils;
import org.nervos.neuron.constant.NeuronDAppCallback;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.PickPicUtils;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.util.qrcode.CodeUtils;
import org.nervos.neuron.util.web.WebAppUtil;
import org.nervos.neuron.view.WebErrorView;
import org.nervos.neuron.view.WebMenuPopupWindow;
import org.nervos.neuron.view.dialog.SignDialog;
import org.nervos.neuron.view.webview.NeuronWebView;
import org.nervos.neuron.view.webview.SimpleWebViewClient;
import org.nervos.neuron.view.webview.item.Address;
import org.nervos.neuron.view.webview.item.Message;
import org.nervos.neuron.view.webview.item.Transaction;

import java.io.File;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by duanyytop on 2018/5/28
 */
public class AppWebActivity extends NBaseActivity {
    public static final String EXTRA_PAYLOAD = "extra_payload";
    public static final String EXTRA_CHAIN = "extra_chain";
    public static final String EXTRA_URL = "extra_url";
    public static final String RECEIVER_WEBSITE = "RECEIVER_WEBSITE";
    private static final int REQUEST_CODE = 0x01;
    public static final int RESULT_CODE_SUCCESS = 0x02;
    public static final int RESULT_CODE_FAIL = 0x01;
    public static final int RESULT_CODE_CANCEL = 0x00;
    public static final int RESULT_CODE_TAKE_PHOTO = 0x03;
    public static final int RESULT_CODE_INPUT_FILE_CHOOSE = 0x04;
    public static final int RESULT_CODE_SCAN_QRCODE = 0x05;
    public static ValueCallback<Uri[]> mFilePathCallbacks;

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
    private NeuronDAppPlugin mNeuronDAppPlugin = null;
    private String mCallback, mPhotoPath;

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
            Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mActivity, AddWalletActivity.class));
        }
        mNeuronDAppPlugin = new NeuronDAppPlugin(this, webView);
        mNeuronDAppPlugin.setImpl(mNeuronDAppPluginImpl);
        WebAppUtil.init();
        webView.loadUrl(url);
        initManifest(url);
        initWebView();
    }

    @Override
    protected void initAction() {
        leftView.setOnClickListener(v -> {
            if (titleItem != null && TextUtils.equals(TitleItem.ACTION_BACK, titleItem.left.type)) {
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
        webErrorView.setImpl((reloadUrl) -> {
            webView.loadUrl(reloadUrl);
            webView.setVisibility(View.VISIBLE);
            webErrorView.setVisibility(View.GONE);
        });
    }

    private void initWebView() {
        SensorsDataAPI.sharedInstance().showUpWebView(webView, false, true);
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

            // For Lollipop 5.0+ Devices
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mFilePathCallbacks = filePathCallback;
                chooseImage();
                return true;
            }
        });
        webView.setWebViewClient(new SimpleWebViewClient(this, webErrorView) {
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
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    private void initMenuView() {
        WebMenuPopupWindow popupWindow = new WebMenuPopupWindow(this);
        popupWindow.showAsDropDown(rightMenuView, 0, 10);
        popupWindow.setCollectText(WebAppUtil.isCollectApp(webView) ? getString(R.string.cancel_collect) : getString(R.string.collect));
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
                pop.setCollectText(WebAppUtil.isCollectApp(webView) ? getString(R.string.cancel_collect) : getString(R.string.collect));
                pop.dismiss();
            }
        });
        findViewById(R.id.menu_background).setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.grey_background));
            popupWindow.setOnDismissListener(() -> {
                getWindow().setStatusBarColor(getResources().getColor(R.color.white));
                findViewById(R.id.menu_background).setVisibility(View.GONE);
            });
        }
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
        WebAppUtil.getHttpManifest(webView, url).subscribe(new NeuronSubscriber<ChainItem>() {
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
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
        webView.post(() -> {
            this.signTransaction = transaction;
            if (walletItem == null) {
                Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(mActivity, AddWalletActivity.class));
            } else {
                Intent intent = new Intent(mActivity, PayTokenActivity.class);
                intent.putExtra(EXTRA_PAYLOAD, new Gson().toJson(transaction));
                intent.putExtra(EXTRA_CHAIN, WebAppUtil.getAppItem() == null ? new AppItem(url) : WebAppUtil.getAppItem());
                intent.putExtra(RECEIVER_WEBSITE, webView.getUrl());
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    private void initInjectWebView() {
        webView.setChainId(1);
        webView.setRpcUrl(EtherUtil.getEthNodeUrl());
        webView.setWalletAddress(new Address(walletItem.address));
        webView.addJavascriptInterface(mNeuronDAppPlugin, "neuron");
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

    private void showPasswordConfirmView(String password, ProgressBar progressBar, Message<Transaction> message) {
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
            observable = SignService.signPersonalMessage(mActivity, NumberUtil.hexToUtf8(message.value.data), password);
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

    private void chooseImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pic_source);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            if (mFilePathCallbacks != null) {
                mFilePathCallbacks.onReceiveValue(null);
            }
            mFilePathCallbacks = null;
        });
        builder.setCancelable(false);
        builder.setItems(new String[]{getResources().getString(R.string.take_photo), getResources().getString(R.string.photo_album)}, (dialog, which) -> {
            switch (which) {
                case 0:
                    String[] permissionList = new String[]{Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.CAMERA};
                    AndPermission.with(this)
                            .runtime()
                            .permission(permissionList)
                            .rationale(new RuntimeRationale())
                            .onGranted(permissions -> {
                                mPhotoPath = ConstantUtil.IMG_SAVE_PATH + System.currentTimeMillis() + ".jpg";
                                File file = new File(mPhotoPath);
                                Uri imageUri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    imageUri = FileProvider.getUriForFile(this, "org.nervos.neuron.fileprovider", file);
                                } else {
                                    imageUri = Uri.fromFile(file);
                                }
                                Intent intent = new Intent();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(intent, AppWebActivity.RESULT_CODE_TAKE_PHOTO);
                            })
                            .onDenied(permissions -> {
                                PermissionUtil.showSettingDialog(this, permissions);
                                if (mFilePathCallbacks != null) {
                                    mFilePathCallbacks.onReceiveValue(null);
                                }
                                mFilePathCallbacks = null;
                            })
                            .start();
                    break;
                case 1:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "File Chooser"), RESULT_CODE_INPUT_FILE_CHOOSE);
                    break;
                default:
                    break;
            }
        });
        builder.create().show();
    }

    private void actionSignAppChain(String password, Message<Transaction> message) {
        SignService.signAppChainMessage(mActivity, message.value.data, password).subscribe(new NeuronSubscriber<String>() {
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
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_CODE_CANCEL:
                    webView.onSignCancel(signTransaction);
                    break;
                case RESULT_CODE_SUCCESS:
                    webView.onSignTransactionSuccessful(signTransaction, data.getStringExtra(PayTokenActivity.EXTRA_HEX_HASH));
                    break;
                case RESULT_CODE_FAIL:
                    webView.onSignError(signTransaction, data.getStringExtra(PayTokenActivity.EXTRA_PAY_ERROR));
                    break;
                case RESULT_CODE_TAKE_PHOTO:
                    if (mFilePathCallbacks != null) {
                        if (mPhotoPath != null) {
                            Uri picUri = Uri.fromFile(new File(mPhotoPath));
                            mFilePathCallbacks.onReceiveValue(new Uri[]{picUri});
                        }
                    }
                    mFilePathCallbacks = null;
                    break;
                case RESULT_CODE_INPUT_FILE_CHOOSE:
                    if (mFilePathCallbacks != null) {
                        Uri result = data == null ? null : data.getData();
                        if (result != null) {
                            String path = PickPicUtils.getPath(getApplicationContext(), result);
                            Uri picUri = Uri.fromFile(new File(path));
                            mFilePathCallbacks.onReceiveValue(new Uri[]{picUri});
                        }
                    }
                    mFilePathCallbacks = null;
                    break;
                case RESULT_CODE_SCAN_QRCODE:
                    if (null != data) {
                        boolean fail = true;
                        Bundle bundle = data.getExtras();
                        if (null != bundle && bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                            if (!TextUtils.isEmpty(mCallback)) {
                                fail = false;
                                QrCodeItem qrCodeItem = new QrCodeItem(bundle.getString(CodeUtils.RESULT_STRING));
                                JSLoadUtils.INSTANCE.loadFunc(webView, mCallback, new Gson().toJson(qrCodeItem));
                            }
                        }
                        if (fail) {
                            if (!TextUtils.isEmpty(mCallback)) {
                                BaseNeuronDAppCallbackItem errorItem = new BaseNeuronDAppCallbackItem(NeuronDAppCallback.ERROR_CODE, NeuronDAppCallback.INSTANCE.USER_CANCEL_CODE, NeuronDAppCallback.INSTANCE.USER_CANCEL);
                                JSLoadUtils.INSTANCE.loadFunc(webView, mCallback, new Gson().toJson(errorItem));
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        } else {
            switch (resultCode) {
                case RESULT_CODE_TAKE_PHOTO:
                case RESULT_CODE_INPUT_FILE_CHOOSE:
                    if (mFilePathCallbacks != null) {
                        mFilePathCallbacks.onReceiveValue(null);
                    }
                    mFilePathCallbacks = null;
                    break;
                case RESULT_CODE_SCAN_QRCODE:
                    if (!TextUtils.isEmpty(mCallback)) {
                        BaseNeuronDAppCallbackItem errorItem = new BaseNeuronDAppCallbackItem(NeuronDAppCallback.ERROR_CODE, NeuronDAppCallback.UNKNOWN_ERROR_CODE, NeuronDAppCallback.UNKNOWN_ERROR);
                        JSLoadUtils.INSTANCE.loadFunc(webView, mCallback, new Gson().toJson(errorItem));
                    }
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

    private NeuronDAppPlugin.NeuronDAppPluginImpl mNeuronDAppPluginImpl = new NeuronDAppPlugin.NeuronDAppPluginImpl() {
        @Override
        public void scanCode(@NotNull String callback) {
            mCallback = callback;
        }
    };

}
