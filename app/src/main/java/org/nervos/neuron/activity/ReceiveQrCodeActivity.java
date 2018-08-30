package org.nervos.neuron.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.SharePicUtils;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReceiveQrCodeActivity extends NBaseActivity {

    private ImageView qrCodeImage;
    private TextView walletNameText;
    private TextView walletAddressText;
    private CircleImageView walletPhotoImage;
    private TitleBar title;
    private TextView copyAddressText;

    private static final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/NQrCode.png";

    @Override
    protected int getContentLayout() {
        return R.layout.activity_receive_qrcode;
    }

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.transparent_color2);
    }

    @Override
    protected void initView() {
        qrCodeImage = findViewById(R.id.receive_qrcode_image);
        walletNameText = findViewById(R.id.qrcode_wallet_name);
        walletAddressText = findViewById(R.id.qrcode_wallet_address);
        walletPhotoImage = findViewById(R.id.wallet_photo);
        title = findViewById(R.id.title);
        copyAddressText = findViewById(R.id.button_copy_receive_qrcode);

    }

    @Override
    protected void initData() {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        walletPhotoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        copyAddressText.setOnClickListener((view) -> {
            ClipboardManager cm = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("qrCode", walletItem.address);
            if (cm != null) {
                cm.setPrimaryClip(mClipData);
                Toast.makeText(mActivity, R.string.copy_success, Toast.LENGTH_SHORT).show();
            }
        });

        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);

        Bitmap bitmap = CodeUtils.createImage(walletItem.address, 400, 400, null);
        qrCodeImage.setImageBitmap(bitmap);
    }

    @Override
    protected void initAction() {
        title.setOnRightClickListener(() -> {
            showProgressBar();
            AndPermission.with(mActivity)
                    .runtime().permission(Permission.Group.STORAGE)
                    .rationale(new RuntimeRationale())
                    .onGranted(permissions -> {
                        try {
                            copyAddressText.setVisibility(View.GONE);
                            SharePicUtils.savePic(savePath, SharePicUtils.getCacheBitmapFromView(findViewById(R.id.ll_qrcode)));
                            copyAddressText.setVisibility(View.VISIBLE);
                            SharePicUtils.SharePic(this, savePath);
                            dismissProgressBar();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            dismissProgressBar();
                        } catch (IOException e) {
                            e.printStackTrace();
                            dismissProgressBar();
                        }
                    })
                    .onDenied(permissions -> {
                        dismissProgressBar();
                        PermissionUtil.showSettingDialog(mActivity, permissions);
                    })
                    .start();
        });
    }


}
