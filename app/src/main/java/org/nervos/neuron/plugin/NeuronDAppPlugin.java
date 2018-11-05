package org.nervos.neuron.plugin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.activity.AppWebActivity;
import org.nervos.neuron.item.NeuronDApp.TakePhotoItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.JSLoadUtils;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NeuronDAppPlugin {

    public static final String TAKE_PHOTO_QUALITY_LOW = "low";
    public static final String TAKE_PHOTO_QUALITY_NORMAL = "normal";
    public static final String TAKE_PHOTO_QUALITY_HIGH = "high";
    public static final String TAKE_PHOTO_TEMP_PATH = ConstUtil.IMG_SAVE_PATH + "photo.jpg";

    private Activity mContext;
    private WebView mWebView;
    private NeuronDAppPluginImpl mImpl = null;

    public NeuronDAppPlugin(Activity context, WebView webview) {
        mContext = context;
        mWebView = webview;
    }

    public void setImpl(NeuronDAppPluginImpl impl) {
        mImpl = impl;
    }

    @JavascriptInterface
    public String getAccount() {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(mContext);
        return walletItem.address;
    }

    @JavascriptInterface
    public String getAccounts() {
        List<WalletItem> walletItems = DBWalletUtil.getAllWallet(mContext);
        List<String> walletNames = new ArrayList<>();
        for (WalletItem item : walletItems) {
            walletNames.add(item.address);
        }
        return new Gson().toJson(walletNames);
    }

    @JavascriptInterface
    public void takePhoto(String quality, String callback) {
        if (!TextUtils.isEmpty(callback)) {
            String[] permissionList = new String[]{Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.CAMERA};
            AndPermission.with(mContext)
                    .runtime()
                    .permission(permissionList)
                    .rationale(new RuntimeRationale())
                    .onGranted(permissions -> {
                        Uri imageUri;
                        File file = new File(ConstUtil.IMG_SAVE_PATH + System.currentTimeMillis() + ".jpg");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            imageUri = FileProvider.getUriForFile(mContext, "org.nervos.neuron.fileprovider", file);
                        } else {
                            imageUri = Uri.fromFile(file);
                        }
                        Intent intent = new Intent();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        mContext.startActivityForResult(intent, AppWebActivity.RESULT_CODE_TAKE_PHOTO);
                        if (mImpl != null) mImpl.takePhoto(imageUri, quality, callback);
                    })
                    .onDenied(permissions -> {
                        PermissionUtil.showSettingDialog(mContext, permissions);
                        TakePhotoItem takePhotoItem = new TakePhotoItem("0", "0", "Permission Denied", "");
                        JSLoadUtils.loadFunc(mWebView, callback, new Gson().toJson(takePhotoItem));
                    })
                    .start();
        }
    }

    public interface NeuronDAppPluginImpl {
        void takePhoto(Uri imageUri, String quality, String callback);
    }
}
