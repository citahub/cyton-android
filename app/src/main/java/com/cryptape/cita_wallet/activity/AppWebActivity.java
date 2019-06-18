package com.cryptape.cita_wallet.activity;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.jetbrains.annotations.NotNull;
import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.constant.CytonDAppCallback;
import com.cryptape.cita_wallet.item.App;
import com.cryptape.cita_wallet.item.AppTitle;
import com.cryptape.cita_wallet.item.Chain;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.item.dapp.BaseCytonDAppCallback;
import com.cryptape.cita_wallet.item.dapp.QrCode;
import com.cryptape.cita_wallet.item.transaction.AppTransaction;
import com.cryptape.cita_wallet.plugin.CytonDAppPlugin;
import com.cryptape.cita_wallet.service.http.CytonSubscriber;
import com.cryptape.cita_wallet.service.http.SignService;
import com.cryptape.cita_wallet.service.http.WalletService;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.util.JSLoadUtils;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.PickPicUtils;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.ether.EtherUtil;
import com.cryptape.cita_wallet.util.exception.TransactionFormatException;
import com.cryptape.cita_wallet.util.permission.PermissionUtil;
import com.cryptape.cita_wallet.util.permission.RuntimeRationale;
import com.cryptape.cita_wallet.util.qrcode.CodeUtils;
import com.cryptape.cita_wallet.util.web.WebAppUtil;
import com.cryptape.cita_wallet.view.WebErrorView;
import com.cryptape.cita_wallet.view.WebMenuPopupWindow;
import com.cryptape.cita_wallet.view.dialog.SignDialog;
import com.cryptape.cita_wallet.view.webview.CytonWebView;
import com.cryptape.cita_wallet.view.webview.SimpleWebViewClient;
import com.cryptape.cita_wallet.view.webview.item.Address;
import com.cryptape.cita_wallet.view.webview.item.Message;
import com.cryptape.cita_wallet.view.webview.item.Transaction;

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
    public static final int REQUEST_CODE_TAKE_PHOTO = 0x03;
    public static final int REQUEST_CODE_INPUT_FILE_CHOOSE = 0x04;
    public static final int RESULT_CODE_SCAN_QRCODE = 0x05;
    public static ValueCallback<Uri[]> mFilePathCallbacks;

    private CytonWebView webView;
    private TextView titleText;
    private ProgressBar progressBar;
    private SignDialog mSignDialog;
    private RelativeLayout mRlTitle;
    private ImageView rightMenuView;
    private ImageView leftView;
    private WebErrorView webErrorView;

    private Wallet wallet;
    private AppTitle appTitle;
    private Transaction signTransaction;
    private String url;
    private boolean isPersonalSign = false;
    private CytonDAppPlugin mCytonDAppPlugin = null;
    private String mCallback, mPhotoPath;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_app_web;
    }

    @Override
    protected void initView() {
        mRlTitle = findViewById(R.id.title_layout);
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
        wallet = DBWalletUtil.getCurrentWallet(mActivity);

        if (wallet == null || TextUtils.isEmpty(wallet.address)) {
            Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mActivity, AddWalletActivity.class));
        }
        mCytonDAppPlugin = new CytonDAppPlugin(this, webView);
        mCytonDAppPlugin.setImpl(mCytonDAppPluginImpl);
        WebAppUtil.init();
        WebAppUtil.loadUrl(webView, url);
        initManifest(url);
        initWebView();
    }

    @Override
    protected void initAction() {
        leftView.setOnClickListener(v -> {
            backAction();
        });
        rightMenuView.setOnClickListener(v -> initMenuView());
        webErrorView.setImpl((reloadUrl) -> {
            WebAppUtil.loadUrl(webView, reloadUrl);
            webView.setVisibility(View.VISIBLE);
            webErrorView.setVisibility(View.GONE);
        });
    }

    @Override
    public void onBackPressed() {
        backAction();
    }

    private void initWebView() {
        webView.init();
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
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mFilePathCallbacks = filePathCallback;
                chooseImage();
                return true;
            }
        });
        webView.setWebViewClient(new SimpleWebViewClient(this, webErrorView) {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                appTitle = null;
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
        popupWindow.setCollectText(WebAppUtil.isApp(webView) ? getString(R.string.cancel_collect) : getString(R.string.collect));
        popupWindow.setListener(new WebMenuPopupWindow.WebMenuListener() {
            @Override
            public void reload(PopupWindow pop) {
                webView.reload();
                pop.dismiss();
            }

            @Override
            public void collect(WebMenuPopupWindow pop) {
                if (WebAppUtil.isApp(webView)) {
                    WebAppUtil.cancelApp(webView);
                } else {
                    WebAppUtil.collectApp(webView);
                }
                pop.setCollectText(WebAppUtil.isApp(webView) ? getString(R.string.cancel_collect) : getString(R.string.collect));
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
        if (appTitle == null) {
            leftView.setImageResource(R.drawable.title_close);
            rightMenuView.setVisibility(View.VISIBLE);
            rightMenuView.setImageResource(R.drawable.title_more);
        }
    }

    private void initManifest(String url) {
        WebAppUtil.getHttpManifest(webView, url).subscribe(new CytonSubscriber<Chain>() {
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Chain chain) {
                if (TextUtils.isEmpty(chain.errorMessage)) {
                    WebAppUtil.addHistory();
                    DBWalletUtil.saveChainInCurrentWallet(webView.getContext(), chain);
                } else {
                    Toast.makeText(webView.getContext(), chain.errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signTxAction(Transaction transaction) {
        webView.post(() -> {
            this.signTransaction = transaction;
            if (wallet == null) {
                Toast.makeText(mActivity, R.string.no_wallet_suggestion, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(mActivity, AddWalletActivity.class));
            } else {
                Gson gson = new Gson();
                AppTransaction appTransaction = gson.fromJson(gson.toJson(transaction), AppTransaction.class);
                if (DBWalletUtil.getChainItemFromCurrentWallet(mActivity, appTransaction.chainId) == null) return;
                try {
                    appTransaction.checkTransactionFormat();
                    Intent intent = new Intent(mActivity, PayTokenActivity.class);
                    intent.putExtra(EXTRA_PAYLOAD, new Gson().toJson(transaction));
                    intent.putExtra(EXTRA_CHAIN, WebAppUtil.getAppItem() == null ? new App(url) : WebAppUtil.getAppItem());
                    intent.putExtra(RECEIVER_WEBSITE, webView.getUrl());
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (TransactionFormatException e) {
                    e.printStackTrace();
                    new android.app.AlertDialog.Builder(mActivity)
                            .setTitle(e.getMessage())
                            .setPositiveButton(R.string.have_known, (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                }
            }
        });
    }

    private void initInjectWebView() {
        webView.setChainId(1);
        webView.setRpcUrl(EtherUtil.getEthNodeUrl());
        webView.setWalletAddress(new Address(wallet.address));
        webView.addJavascriptInterface(mCytonDAppPlugin, "cyton");
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

    private void backAction() {
        if (appTitle != null) {
            if (!TextUtils.isEmpty(appTitle.left.action)) {
                JSLoadUtils.INSTANCE.loadFunc(webView, appTitle.left.action);
            } else if (TextUtils.equals(AppTitle.ACTION_CLOSE, appTitle.left.type)) {
                finish();
            } else {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        } else {
            finish();
        }
    }

    public class WebTitleBar {

        @JavascriptInterface
        public void getTitleBar(String data) {
            mRlTitle.post(() -> {
                if (!TextUtils.isEmpty(data)) {
                    appTitle = new Gson().fromJson(data, AppTitle.class);
                    mRlTitle.setVisibility(View.VISIBLE);
                    if (appTitle.right != null) {
                        rightMenuView.setVisibility(appTitle.right.isShow ? View.VISIBLE : View.INVISIBLE);
                        if (AppTitle.ACTION_MENU.equals(appTitle.right.type)) {
                            rightMenuView.setImageResource(R.drawable.title_more);
                        } else if (AppTitle.ACTION_SHARE.equals(appTitle.right.type)) {
                            rightMenuView.setImageResource(R.drawable.share);
                        }
                    }

                    if (appTitle.left != null && !TextUtils.isEmpty(appTitle.left.type)) {
                        if (AppTitle.ACTION_BACK.equals(appTitle.left.type)) {
                            leftView.setImageResource(R.drawable.black_back);
                        } else if (AppTitle.ACTION_CLOSE.equals(appTitle.left.type)) {
                            leftView.setImageResource(R.drawable.title_close);
                        }
                    }

                    if (appTitle.title != null) {
                        if (!TextUtils.isEmpty(appTitle.title.name)) {
                            titleText.setText(appTitle.title.name);
                        }
                    }
                } else {
                    mRlTitle.setVisibility(View.GONE);
                }
            });
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
        if (wallet == null) {
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
        } else if (!WalletService.checkPassword(mActivity, password, wallet)) {
            Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        if (Transaction.TYPE_ETH.equals(message.value.chainType)) {
            actionSignEth(password, message);
        } else if (Transaction.TYPE_CITA.equals(message.value.chainType)) {
            actionSignCITA(password, message);
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
                                    imageUri = FileProvider.getUriForFile(this, "com.cryptape.cita_wallet.fileprovider", file);
                                } else {
                                    imageUri = Uri.fromFile(file);
                                }
                                Intent intent = new Intent();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(intent, AppWebActivity.REQUEST_CODE_TAKE_PHOTO);
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
                    String[] choosePermissionList = new String[]{Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE};
                    AndPermission.with(this)
                            .runtime()
                            .permission(choosePermissionList)
                            .rationale(new RuntimeRationale())
                            .onGranted(permissions -> {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_CODE_INPUT_FILE_CHOOSE);
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
                default:
                    break;
            }
        });
        builder.create().show();
    }

    private void actionSignCITA(String password, Message<Transaction> message) {
        SignService.signCITAMessage(mActivity, message.value.data, password).subscribe(new CytonSubscriber<String>() {
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
        switch (requestCode) {
            case REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_CODE_CANCEL:
                        webView.onSignCancel(signTransaction);
                        break;
                    case RESULT_CODE_SUCCESS:
                        webView.onSignTransactionSuccessful(signTransaction, data.getStringExtra(PayTokenActivity.EXTRA_HEX_HASH));
                        break;
                    case RESULT_CODE_FAIL:
                        webView.onSignError(signTransaction, data.getStringExtra(PayTokenActivity.EXTRA_PAY_ERROR));
                        break;
                    default:
                        break;
                }
                break;
            case REQUEST_CODE_TAKE_PHOTO:
                switch (resultCode) {
                    case RESULT_OK:
                        if (mFilePathCallbacks != null) {
                            if (mPhotoPath != null) {
                                Uri picUri = Uri.fromFile(new File(mPhotoPath));
                                mFilePathCallbacks.onReceiveValue(new Uri[]{picUri});
                            }
                        }
                        mFilePathCallbacks = null;
                        break;
                    default:
                        if (mFilePathCallbacks != null) {
                            mFilePathCallbacks.onReceiveValue(null);
                        }
                        mFilePathCallbacks = null;
                        break;
                }
                break;
            case REQUEST_CODE_INPUT_FILE_CHOOSE:
                switch (resultCode) {
                    case RESULT_OK:
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
                    default:
                        if (mFilePathCallbacks != null) {
                            mFilePathCallbacks.onReceiveValue(null);
                        }
                        mFilePathCallbacks = null;
                        break;
                }
                break;
            case RESULT_CODE_SCAN_QRCODE:
                switch (resultCode) {
                    case RESULT_OK:
                        if (null != data) {
                            boolean fail = true;
                            Bundle bundle = data.getExtras();
                            if (null != bundle && bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                                if (!TextUtils.isEmpty(mCallback)) {
                                    fail = false;
                                    QrCode qrCodeItem = new QrCode(bundle.getString(CodeUtils.RESULT_STRING));
                                    JSLoadUtils.INSTANCE.loadFunc(webView, mCallback, new Gson().toJson(qrCodeItem));
                                }
                            }
                            if (fail) {
                                if (!TextUtils.isEmpty(mCallback)) {
                                    BaseCytonDAppCallback errorItem = new BaseCytonDAppCallback(CytonDAppCallback.ERROR_CODE,
                                            CytonDAppCallback.USER_CANCEL_CODE, CytonDAppCallback.USER_CANCEL);
                                    JSLoadUtils.INSTANCE.loadFunc(webView, mCallback, new Gson().toJson(errorItem));
                                }
                            }
                        }
                        break;
                    default:
                        if (!TextUtils.isEmpty(mCallback)) {
                            BaseCytonDAppCallback errorItem = new BaseCytonDAppCallback(CytonDAppCallback.ERROR_CODE,
                                    CytonDAppCallback.UNKNOWN_ERROR_CODE, CytonDAppCallback.UNKNOWN_ERROR);
                            JSLoadUtils.INSTANCE.loadFunc(webView, mCallback, new Gson().toJson(errorItem));
                        }
                        break;
                }
                break;
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

    private CytonDAppPlugin.CytonDAppPluginImpl mCytonDAppPluginImpl = new CytonDAppPlugin.CytonDAppPluginImpl() {
        @Override
        public void scanCode(@NotNull String callback) {
            mCallback = callback;
        }
    };

}
