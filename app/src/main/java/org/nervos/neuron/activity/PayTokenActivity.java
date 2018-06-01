package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.nervos.neuron.R;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.web.WebUtil;
import org.nervos.neuron.util.db.DBWalletUtil;

public class PayTokenActivity extends BaseActivity {

    private TransactionItem transactionItem;
    private WalletItem walletItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_token);

        String payload = getIntent().getStringExtra(WebActivity.EXTRA_PAYLOAD);
        transactionItem = new Gson().fromJson(payload, TransactionItem.class);
        walletItem = DBWalletUtil.getCurrentWallet(this);

        initView();
        initListener();
    }

    private void initView() {
        TextView walletNameText = findViewById(R.id.wallet_name);
        TextView walletAddressText = findViewById(R.id.wallet_address);
        TextView payNameText = findViewById(R.id.pay_owner);
        TextView payAddressText = findViewById(R.id.pay_address);
        TextView payAmountText = findViewById(R.id.pay_amount);
        TextView paySumText = findViewById(R.id.pay_sum);

        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);
        payNameText.setText(WebUtil.getChainItem().entry);
        if (WebUtil.getChainItem().chainId >= 0) {
            payAddressText.setText(transactionItem.to);
            payAmountText.setText(transactionItem.value);
            paySumText.setText(transactionItem.value);
        } else {
            payAddressText.setText(transactionItem.to);
            payAmountText.setText(transactionItem.value);
            paySumText.setText(transactionItem.gas);
        }

    }

    private void initListener() {
        findViewById(R.id.pay_reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.pay_approve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
