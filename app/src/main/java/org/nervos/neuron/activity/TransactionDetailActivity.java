package org.nervos.neuron.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.SharePicUtils;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;

public class TransactionDetailActivity extends NBaseActivity {

    public static final String EXTRA_TRANSACTION = "extra_transaction";
    private WalletItem walletItem;
    private TransactionItem transactionItem;
    private TitleBar title;
    private TextView transactionHashText, transactionValueText, transactionFromText, transactionToText, transactionBlockNumberText,
            transactionBlockTimeText, transactionGas, transactionGasPrice, transactionChainName;
    private static final String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";

    @Override
    protected int getContentLayout() {
        return R.layout.activity_transaction_detail;
    }

    @Override
    protected void initView() {
        title = findViewById(R.id.title);
        transactionHashText = findViewById(R.id.tv_transaction_number);
        transactionValueText = findViewById(R.id.transaction_amount);
        transactionFromText = findViewById(R.id.tv_transaction_sender);
        transactionToText = findViewById(R.id.tv_transaction_receiver);
        transactionBlockNumberText = findViewById(R.id.tv_transaction_blockchain_no);
        transactionBlockTimeText = findViewById(R.id.tv_transaction_blockchain_time);
        transactionGas = findViewById(R.id.tv_transaction_gas);
        transactionGasPrice = findViewById(R.id.tv_transaction_gas_price);
        transactionChainName = findViewById(R.id.tv_chain_name);
    }

    @Override
    protected void initData() {
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        transactionItem = getIntent().getParcelableExtra(EXTRA_TRANSACTION);

        transactionHashText.setText(lineFeedHex(transactionItem.hash));
        transactionFromText.setText(lineFeedAddress(transactionItem.from));
        transactionToText.setText(lineFeedAddress(transactionItem.to));
        if (!TextUtils.isEmpty(transactionItem.gasPrice)) {
            transactionChainName.setText(ConstUtil.ETH_MAINNET);
            BigInteger gasPriceBig = new BigInteger(transactionItem.gasPrice);
            BigInteger gasUsedBig = new BigInteger(transactionItem.gasUsed);
            transactionGas.setText(NumberUtil.getEthFromWeiForStringDecimal8(gasPriceBig.multiply(gasUsedBig)) + "eth");
            transactionGasPrice.setText(Convert.fromWei(gasPriceBig.toString(), Convert.Unit.GWEI) + " Gwei");
            String value = (transactionItem.from.equalsIgnoreCase(walletItem.address) ?
                    "-" : "+") + transactionItem.value;
            transactionValueText.setText(value);
            transactionBlockNumberText.setText(transactionItem.blockNumber);
        } else {
            String value = (transactionItem.from.equalsIgnoreCase(walletItem.address) ?
                    "-" : "+") + transactionItem.value;
            transactionValueText.setText(value);
            int blockNumber = Integer.parseInt(Numeric.cleanHexPrefix(transactionItem.blockNumber), 16);
            transactionBlockNumberText.setText(String.valueOf(blockNumber));
        }

        transactionBlockTimeText.setText(transactionItem.getDate());

        transactionToText.setOnClickListener(v -> copyText(transactionItem.to));
        transactionFromText.setOnClickListener(v -> copyText(transactionItem.from));
        transactionHashText.setOnClickListener(v -> copyText(transactionItem.hash));
    }

    @Override
    protected void initAction() {
        title.setOnRightClickListener(() -> {
            AndPermission.with(mActivity)
                    .runtime().permission(Permission.Group.STORAGE)
                    .rationale(new RuntimeRationale())
                    .onGranted(permissions -> {
                        try {
                            SharePicUtils.savePic(savePath + transactionItem.blockNumber + ".png", SharePicUtils.getCacheBitmapFromView(findViewById(R.id.ll_screenshot)));
                            SharePicUtils.SharePic(this, savePath + transactionItem.blockNumber + ".png");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .onDenied(permissions -> {
                        dismissProgressBar();
                        PermissionUtil.showSettingDialog(mActivity, permissions);
                    })
                    .start();
        });
        title.setOnLeftClickListener(() -> {
            finish();
        });
    }

    private void copyText(String value) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("value", value);
        if (cm != null) {
            cm.setPrimaryClip(mClipData);
            Toast.makeText(mActivity, R.string.copy_success, Toast.LENGTH_SHORT).show();
        }
    }

    private String lineFeedAddress(String address) {
        int singleLine = address.length() / 2;
        StringBuffer buffer = new StringBuffer();
        buffer.append(address.substring(0, singleLine));
        buffer.append("\n");
        buffer.append(address.substring(singleLine));
        return buffer.toString();
    }

    private String lineFeedHex(String hex) {
        int singleLine = hex.length() / 3;
        StringBuffer buffer = new StringBuffer();
        buffer.append(hex.substring(0, singleLine));
        buffer.append("\n");
        buffer.append(hex.substring(singleLine, singleLine * 2));
        buffer.append("\n");
        buffer.append(hex.substring(singleLine * 2));
        return buffer.toString();
    }

}
