package org.nervos.neuron.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
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
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReceiveQrCodeActivity extends NBaseActivity {

    private ImageView qrCodeImage;
    private TextView walletNameText;
    private TextView walletAddressText;
    private CircleImageView walletPhotoImage;
    private TitleBar title;
    private static final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NQrCode.png";

    @Override
    protected int getContentLayout() {
        return R.layout.activity_receive_qrcode;
    }

    @Override
    protected void initView() {
        qrCodeImage = findViewById(R.id.receive_qrcode_image);
        walletNameText = findViewById(R.id.qrcode_wallet_name);
        walletAddressText = findViewById(R.id.qrcode_wallet_address);
        walletPhotoImage = findViewById(R.id.wallet_photo);
        title = findViewById(R.id.title);
        title.setOnRightClickListener(() -> {
            showProgressBar();
            AndPermission.with(mActivity)
                    .runtime().permission(Permission.Group.STORAGE)
                    .rationale(new RuntimeRationale())
                    .onGranted(permissions -> {
                        try {
                            savePic();
                            String imagePath = savePath;
                            Uri imageUri = Uri.fromFile(new File(imagePath));
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            shareIntent.setType("image/*");
                            startActivity(Intent.createChooser(shareIntent, "分享到"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    })
                    .onDenied(permissions -> PermissionUtil.showSettingDialog(mActivity, permissions))
                    .start();
        });
    }

    @Override
    protected void initAction() {

    }

    @Override
    protected void initData() {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        walletPhotoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        findViewById(R.id.button_copy_receive_qrcode).setOnClickListener((view) -> {
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

    //save qrcode
    private void savePic() throws FileNotFoundException {
        Bitmap bitmap = getCacheBitmapFromView(findViewById(R.id.ll_qrcode));
        File file = new File(savePath);
        if (file.exists())
            file.delete();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
    }

    //get screenShoot
    private Bitmap getCacheBitmapFromView(View view) {
        final boolean drawingCacheEnabled = true;
        view.setDrawingCacheEnabled(drawingCacheEnabled);
        view.buildDrawingCache(drawingCacheEnabled);
        final Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache);
            view.setDrawingCacheEnabled(false);
        } else {
            bitmap = null;
        }
        return bitmap;
    }
}
