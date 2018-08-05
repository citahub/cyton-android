package org.nervos.neuron.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class TransactionDetailActivity extends BaseActivity {

    public static final String EXTRA_TRANSACTION = "extra_transaction";
    private WalletItem walletItem;
    private TransactionItem transactionItem;
    private TitleBar title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transac_detail);

        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        transactionItem = getIntent().getParcelableExtra(EXTRA_TRANSACTION);

        initView();
    }

    private void initView() {

        TextView transactionHashText = findViewById(R.id.tv_transaction_number);
        TextView transactionValueText = findViewById(R.id.transaction_amount);
        TextView transactionFromText = findViewById(R.id.tv_transaction_sender);
        TextView transactionToText = findViewById(R.id.tv_transaction_receiver);
        TextView transactionBlockNumberText = findViewById(R.id.tv_transaction_blockchain_no);
        TextView transactionBlockTimeText = findViewById(R.id.tv_transaction_blockchain_time);
        TextView transactionGas = findViewById(R.id.tv_transaction_gas);
        TextView transactionGasPrice = findViewById(R.id.tv_transaction_gas_price);
        TextView transactionChainName = findViewById(R.id.tv_chain_name);
        title = findViewById(R.id.title);
        title.setOnRightClickListener(() -> {

        });

        transactionHashText.setText(transactionItem.hash);
        transactionFromText.setText(transactionItem.from);
        transactionToText.setText(transactionItem.to);
        if (!TextUtils.isEmpty(transactionItem.gasPrice)) {
            transactionChainName.setText(R.string.ethereum_main_net);
            BigInteger gasPriceBig = new BigInteger(transactionItem.gasPrice);
            BigInteger gasUsedBig = new BigInteger(transactionItem.gasUsed);
            transactionGas.setText(NumberUtil.getEthFromWeiForStringDecimal6(gasPriceBig.multiply(gasUsedBig)) + "eth");
            transactionGasPrice.setText(Convert.fromWei(gasPriceBig.toString(), Convert.Unit.GWEI) + " Gwei");
            String value = (transactionItem.from.equalsIgnoreCase(walletItem.address) ?
                    "-" : "+") + transactionItem.value;
            transactionValueText.setText(value);
            transactionBlockNumberText.setText(transactionItem.blockNumber);
        } else {
            String value = (transactionItem.from.equalsIgnoreCase(walletItem.address) ?
                    "-" : "+") + transactionItem.value;
            transactionValueText.setText(value);
            int blockNumber = Integer.parseInt(
                    Numeric.cleanHexPrefix(transactionItem.blockNumber), 16);
            transactionBlockNumberText.setText(String.valueOf(blockNumber));
        }

        transactionBlockTimeText.setText(transactionItem.getDate());

        transactionToText.setOnClickListener(v -> copyText(transactionItem.to));
        transactionFromText.setOnClickListener(v -> copyText(transactionItem.from));
        transactionHashText.setOnClickListener(v -> copyText(transactionItem.hash));
    }

    private void copyText(String value) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("value", value);
        if (cm != null) {
            cm.setPrimaryClip(mClipData);
            Toast.makeText(mActivity, R.string.copy_success, Toast.LENGTH_SHORT).show();
        }
    }

}
