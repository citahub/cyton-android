package org.nervos.neuron.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.utils.Numeric;

import de.hdodenhof.circleimageview.CircleImageView;

public class TransactionDetailActivity extends BaseActivity {

    public static final String EXTRA_TRANSACTION = "extra_transaction";
    private WalletItem walletItem;
    private TransactionItem transactionItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        transactionItem = getIntent().getParcelableExtra(EXTRA_TRANSACTION);

        initView();
    }

    private void initView() {
        CircleImageView photoImage = findViewById(R.id.wallet_photo);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));

        TextView walletNameText = findViewById(R.id.wallet_name);
        TextView walletAddressText = findViewById(R.id.wallet_address);
        TextView transactionHashText = findViewById(R.id.transaction_hash);
        TextView transactionValueText = findViewById(R.id.transaction_amount);
        TextView transactionFromText = findViewById(R.id.transaction_from_address);
        TextView transactionToText = findViewById(R.id.transaction_to_address);
        TextView transactionBlockNumberText = findViewById(R.id.transaction_block_number);
        TextView transactionBlockTimeText = findViewById(R.id.transaction_block_time);


        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);
        transactionHashText.setText(transactionItem.hash);
        transactionValueText.setText(transactionItem.getValue());
        transactionFromText.setText(transactionItem.from);
        transactionToText.setText(transactionItem.to);
        int blockNumber = Integer.parseInt(
                Numeric.cleanHexPrefix(transactionItem.blockNumber), 16 );
        transactionBlockNumberText.setText(String.valueOf(blockNumber));
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
            Toast.makeText(mActivity, "复制成功", Toast.LENGTH_SHORT).show();
        }
    }

}
