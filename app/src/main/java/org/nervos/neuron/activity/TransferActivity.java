package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.service.CITAJsonRpcService;
import org.nervos.neuron.R;
import org.nervos.neuron.fragment.WalletFragment;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.item.TokenItem;
import com.facebook.drawee.view.SimpleDraweeView;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0x01;
    private static final String CONTRACT_ADDRESS = "0xbd51c4669a21df5afd1fb661d5aab67171fbec35";

    private SimpleDraweeView tokenImageView;
    private TextView tokenNameText;
    private TextView tokenAmountText;
    private TitleBar titleBar;

    private AppCompatEditText receiveAddressEdit;
    private AppCompatEditText transferValueEdit;
    private AppCompatEditText walletPasswordEdit;
    private AppCompatButton transferButton;
    private ProgressBar progressBar;

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private String walletAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        CITAJsonRpcService.init();

        initView();
        initListener();

    }

    private void initView() {
        titleBar = findViewById(R.id.title);
        tokenImageView = findViewById(R.id.transfer_token_image);
        tokenNameText = findViewById(R.id.transfer_token_name);
        tokenAmountText = findViewById(R.id.transfer_token_amount);

        String tokenName = getIntent().getStringExtra(WalletFragment.EXTRA_TOKEN_NAME);
        String tokenImage = getIntent().getStringExtra(WalletFragment.EXTRA_TOKEN_IMAGE);
        String tokenAmount = getIntent().getStringExtra(WalletFragment.EXTRA_TOKEN_AMOUNT);
        walletAddress = getIntent().getStringExtra(WalletFragment.EXTRA_WALLET_ADDRESS);

        tokenNameText.setText(tokenName);
        tokenAmountText.setText(tokenAmount);
        tokenImageView.setImageURI(tokenImage);

        receiveAddressEdit = findViewById(R.id.edit_receive_address);
        transferValueEdit = findViewById(R.id.edit_transfer_value);
        walletPasswordEdit = findViewById(R.id.edit_wallet_password);
        transferButton = findViewById(R.id.transfer_button);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initListener() {
        titleBar.setOnRightClickListener(() -> {
            Intent intent = new Intent(TransferActivity.this, CaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        });

        transferButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            cachedThreadPool.execute(() -> {
                String value = transferValueEdit.getText().toString().trim();
                CITAJsonRpcService.transfer(CONTRACT_ADDRESS,
                    receiveAddressEdit.getText().toString().trim(),
                    Long.parseLong(value),
                    new CITAJsonRpcService.OnTransferResultListener() {
                        @Override
                        public void onSuccess(EthGetTransactionReceipt receipt) {
                            Toast.makeText(TransferActivity.this,
                                    "Transfer success", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            getBalance();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(TransferActivity.this,
                                    "Transfer fail because of " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
            });
        });
    }

    private void getBalance() {
        cachedThreadPool.execute(() -> {
            TokenItem tokenItem = CITAJsonRpcService.getTokenInfo(CONTRACT_ADDRESS);
            tokenAmountText.post(() -> {
                if (tokenItem != null) {
                    tokenAmountText.setText(tokenItem.amount);
                }
            });

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
                    receiveAddressEdit.setText(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(TransferActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
