package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenEntity;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.http.EthRpcService;
import org.nervos.neuron.util.ether.EtherUtil;
import org.nervos.neuron.util.url.HttpAppChainUrls;
import org.nervos.neuron.util.url.HttpUrls;
import org.nervos.neuron.service.http.AppChainRpcService;
import org.nervos.neuron.R;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.AddressUtil;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.FileUtil;
import org.nervos.neuron.util.qrcode.CodeUtils;
import org.nervos.neuron.util.db.DBTokenUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by duanyytop on 2018/5/17
 */
public class AddTokenActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0x01;

    private AppCompatEditText contractAddressEdit;
    private AppCompatEditText tokenNameEdit;
    private AppCompatEditText tokenSymbolEdit;
    private AppCompatEditText tokenDecimalEdit;
    private AppCompatSpinner blockChainSpinner;

    private List<String> chainNameList;
    private List<ChainItem> chainItemList;
    private ChainItem chainItem;
    private TokenItem tokenItem;
    private WalletItem walletItem;

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_token);

        initView();
        initData();
        initListener();
        AppChainRpcService.init(this, HttpAppChainUrls.APPCHAIN_NODE_URL);
    }

    private void initView() {
        contractAddressEdit = findViewById(R.id.edit_add_token_contract_address);
        tokenNameEdit = findViewById(R.id.edit_add_token_name);
        tokenSymbolEdit = findViewById(R.id.edit_add_token_symbol);
        tokenDecimalEdit = findViewById(R.id.edit_add_token_decimal);
        blockChainSpinner = findViewById(R.id.spinner_add_token_block_chain);
    }

    private void initData() {
        walletItem = DBWalletUtil.getCurrentWallet(this);
        chainItemList = DBChainUtil.getAllChain(this);
        chainNameList = DBChainUtil.getAllChainName(this);
        chainItem = chainItemList.get(0);

        String[] chainNames = new String[chainNameList.size()];
        chainNames = chainNameList.toArray(chainNames);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, chainNames);
        blockChainSpinner.setAdapter(adapter);
    }

    private void initListener() {
        // add token data into local database
        findViewById(R.id.add_token_button).setOnClickListener(v -> {
            String address = contractAddressEdit.getText().toString();
            if (!AddressUtil.isAddressValid(address)) {
                Toast.makeText(mActivity, R.string.contract_address_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (tokenItem == null || TextUtils.isEmpty(tokenItem.contractAddress) || TextUtils.isEmpty(tokenItem.name) ||
                    TextUtils.isEmpty(tokenItem.symbol)) {
                Toast.makeText(mActivity, R.string.input_token_info, Toast.LENGTH_SHORT).show();
            } else {
                if (checkRepetitionContract(tokenItem)) {
                    DBWalletUtil.addTokenToWallet(mActivity, walletItem.name, tokenItem);
                    DBTokenUtil.saveToken(mActivity, tokenItem);
                    setResult(TokenManageActivity.RESULT_CODE);
                    finish();
                } else Toast.makeText(this, R.string.exist_erc20_token, Toast.LENGTH_LONG).show();
            }
        });

        // scan qrcode to get contract address
        findViewById(R.id.add_token_contract_address_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndPermission.with(mActivity)
                        .runtime().permission(Permission.Group.CAMERA)
                        .rationale(new RuntimeRationale())
                        .onGranted(permissions -> {
                            Intent intent = new Intent(mActivity, QrCodeActivity.class);
                            startActivityForResult(intent, REQUEST_CODE);
                        })
                        .onDenied(permissions -> PermissionUtil.showSettingDialog(mActivity, permissions))
                        .start();
            }
        });

        // select the type of blockchain
        blockChainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chainItem = chainItemList.get(position);
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
                if (!AddressUtil.isAddressValid(s.toString())) {
                    return;
                }
                showProgressBar();
                cachedThreadPool.execute(() -> {
                    if (chainItem.getChainId().equals(ConstantUtil.ETHEREUM_MAIN_ID)) {
                        tokenItem = EthRpcService.getTokenInfo(s.toString(), walletItem.address);
                    } else {
                        tokenItem = AppChainRpcService.getErc20TokenInfo(s.toString());
                    }
                    if (chainItem != null && tokenItem != null) {
                        tokenItem.setChainId(chainItem.getChainId());
                    }
                    tokenNameEdit.post(() -> {
                        if (tokenItem != null) {
                            tokenNameEdit.setText(tokenItem.name);
                            tokenSymbolEdit.setText(tokenItem.symbol);
                            tokenDecimalEdit.setText(String.valueOf(tokenItem.decimals));
                        }
                        dismissProgressBar();
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
                if (bundle == null) return;
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    switch (bundle.getInt(CodeUtils.STRING_TYPE)) {
                        case CodeUtils.STRING_ADDRESS:
                            String result = bundle.getString(CodeUtils.RESULT_STRING);
                            contractAddressEdit.setText(result);
                            break;
                        default:
                            Toast.makeText(this, R.string.address_error, Toast.LENGTH_LONG).show();
                            break;
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    QrCodeActivity.track("1", false);
                    Toast.makeText(mActivity, R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean checkRepetitionContract(TokenItem tokenItem) {
        if (EtherUtil.isEther(tokenItem)) {
            String tokens = FileUtil.loadRawFile(mActivity, R.raw.tokens_eth);
            Type type = new TypeToken<List<TokenEntity>>() {}.getType();
            ArrayList<TokenEntity> tokenList = new Gson().fromJson(tokens, type);
            for (TokenEntity item : tokenList) {
                if (item.address.equalsIgnoreCase(tokenItem.contractAddress)) return false;
            }
        }
        List<TokenItem> customList = DBTokenUtil.getAllTokens(mActivity);
        if (customList != null && customList.size() > 0) {
            for (TokenItem item : customList) {
                if (item.contractAddress.equalsIgnoreCase(tokenItem.contractAddress)) return false;
            }
        }
        return true;
    }
}
