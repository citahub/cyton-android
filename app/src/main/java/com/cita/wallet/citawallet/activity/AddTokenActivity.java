package com.cita.wallet.citawallet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cita.wallet.citawallet.config.BlockChainConfig;
import com.cita.wallet.citawallet.R;
import com.cita.wallet.citawallet.fragment.WalletFragment;
import com.cita.wallet.citawallet.item.TokenItem;
import com.facebook.drawee.view.SimpleDraweeView;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTokenActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0x01;
    public static final String CONTRACT_ADDRESS = "0xbd51c4669a21df5afd1fb661d5aab67171fbec35";

    private ImageView qrcodeImage;
    private SimpleDraweeView tokenImage;
    private TextView tokenAmountText;
    private AppCompatButton saveButton;
    private AppCompatEditText addressEdit;

    private String walletName;
    private String walletAddress;

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_token);

        initView();
        initListener();
        initData();
        BlockChainConfig.init();
    }

    private void initData() {
        Intent intent = getIntent();
        walletName = intent.getStringExtra(WalletFragment.EXTRA_WALLET_NAME);
        walletAddress = intent.getStringExtra(WalletFragment.EXTRA_WALLET_ADDRESS);
        ((TextView)findViewById(R.id.wallet_address)).setText(String.format("%s", walletAddress));
        ((TextView)findViewById(R.id.wallet_name)).setText(walletName);
    }

    private void initView() {
        qrcodeImage = findViewById(R.id.qrcode_scan_image);
        tokenImage = findViewById(R.id.token_image);
        tokenAmountText = findViewById(R.id.token_amount);
        saveButton = findViewById(R.id.token_save);
        addressEdit = findViewById(R.id.address_edit);
    }

    private void initListener() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        qrcodeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddTokenActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        addressEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cachedThreadPool.execute(() -> {
                    TokenItem tokenItem = BlockChainConfig.getTokenInfo(s.toString());
                    tokenAmountText.post(() -> {
                        tokenAmountText.setText(String.format("%s %s", tokenItem.amount, tokenItem.name));
                        tokenImage.setImageURI(tokenItem.image);
                    });
                });

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    addressEdit.setText(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(AddTokenActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
