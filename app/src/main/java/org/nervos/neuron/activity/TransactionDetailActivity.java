package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.nervos.neuron.R;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.db.DBWalletUtil;

import de.hdodenhof.circleimageview.CircleImageView;

public class TransactionDetailActivity extends BaseActivity {

    private WalletItem walletItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        walletItem = DBWalletUtil.getCurrentWallet(mActivity);

        initData();
        initView();
    }

    private void initView() {
        CircleImageView photoImage = findViewById(R.id.wallet_photo);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
    }

    private void initData() {


    }



}
