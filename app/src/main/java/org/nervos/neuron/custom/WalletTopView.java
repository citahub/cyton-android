package org.nervos.neuron.custom;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.ChangeWalletActivity;
import org.nervos.neuron.activity.QrCodeActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by BaojunCZ on 2018/8/1.
 */
public class WalletTopView extends ConstraintLayout implements View.OnClickListener {

    private ImageView leftImage, centerImage, rightImage, copyImage;
    private CircleImageView walletPhoto;
    private TextView walletName, walletAddress;
    private Activity context;
    private WalletItem walletItem;

    public WalletTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_wallet_top, this);
        initView();
        initAction();
    }

    public void setActivity(Activity activity) {
        this.context = activity;
    }

    private void initView() {
        leftImage = findViewById(R.id.iv_left);
        centerImage = findViewById(R.id.iv_center);
        rightImage = findViewById(R.id.iv_right);
        copyImage = findViewById(R.id.iv_copy);
        walletPhoto = findViewById(R.id.wallet_photo);
        walletName = findViewById(R.id.qrcode_wallet_name);
        walletAddress = findViewById(R.id.qrcode_wallet_address);
    }

    private void initData() {

    }

    private void initAction() {
        leftImage.setOnClickListener(this);
        rightImage.setOnClickListener(this);
        copyImage.setOnClickListener(this);
        centerImage.setOnClickListener(this);
    }

    public void setWalletItem(WalletItem walletItem) {
        initData();
        this.walletItem = walletItem;
        walletName.setText(walletItem.name);
        walletAddress.setText(walletItem.address);
        walletPhoto.setImageBitmap(Blockies.createIcon(walletItem.address));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left:
                Intent intent = new Intent(context, ReceiveQrCodeActivity.class);
                context.startActivity(intent);
                break;
            case R.id.iv_right:
                Intent intent1 = new Intent(context, QrCodeActivity.class);
                context.startActivity(intent1);
                break;
            case R.id.iv_center:
                Intent intent2 = new Intent(context, ChangeWalletActivity.class);
                context.startActivity(intent2);
                context.overridePendingTransition(R.anim.wallet_activity_in, 0);
                break;
            case R.id.iv_copy:
                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("qrCode", walletItem.address);
                if (cm != null) {
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
