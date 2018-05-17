package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import org.nervos.neuron.service.CITAJsonRpcService;
import org.nervos.neuron.R;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.service.ETHJsonRpcService;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTokenActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0x01;

    private ImageView qrcodeImage;
    private AppCompatButton addTokenButton;
    private AppCompatEditText contractAddressEdit;
    private AppCompatEditText tokenNameEdit;
    private AppCompatEditText tokenSymbolEdit;
    private AppCompatEditText tokenDecimalEdit;
    private AppCompatSpinner blockChainSpinner;

    private String walletName;
    private String walletAddress;
    private List<String> chainList;
    private String chainItem;

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_token);

        initView();
        initData();
        initListener();
        CITAJsonRpcService.init();
        ETHJsonRpcService.init();
    }

    private void initView() {
        qrcodeImage = findViewById(R.id.add_token_contract_address_scan);
        addTokenButton = findViewById(R.id.add_token_button);
        contractAddressEdit = findViewById(R.id.edit_add_token_contract_address);
        tokenNameEdit = findViewById(R.id.edit_add_token_name);
        tokenSymbolEdit = findViewById(R.id.edit_add_token_symbol);
        tokenDecimalEdit = findViewById(R.id.edit_add_token_decimal);
        blockChainSpinner = findViewById(R.id.spinner_add_token_block_chain);
    }

    private void initData() {
        chainList = Arrays.asList(getResources().getStringArray(R.array.chain_items));
        chainItem = chainList.get(0);
    }

    private void initListener() {
        addTokenButton.setOnClickListener(new View.OnClickListener() {
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

        blockChainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chainItem = chainList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        contractAddressEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                showProgressBar();
                cachedThreadPool.execute(() -> {
                    TokenItem tokenItem;
                    if (TextUtils.equals(chainItem, chainList.get(1))) {
                        tokenItem = CITAJsonRpcService.getTokenInfo(s.toString());
                    } else {
                        tokenItem = ETHJsonRpcService.getTokenInfo(s.toString());
                    }
                    tokenNameEdit.post(() -> {
                        if (tokenItem != null) {
                            tokenNameEdit.setText(tokenItem.name);
                            tokenSymbolEdit.setText(tokenItem.symbol);
                            tokenDecimalEdit.setText(String.valueOf(tokenItem.decimals));
                            dismissProgressBar();
                        }
                    });
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    contractAddressEdit.setText(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(AddTokenActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
