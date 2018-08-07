package org.nervos.neuron.activity;

import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.SelectWalletPopupwWindow;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/8/6.
 */
public class PwdUnlockActivity extends NBaseActivity implements View.OnClickListener {

    private List<WalletItem> walletItems = new ArrayList<>();
    private TextView cancelTv, walletNameTv, walletSelectTv;
    private AppCompatEditText walletPwdEt;
    private ImageView walletSelectImg, otherImg, arrowImg;
    private AppCompatButton authBtn;
    private WalletItem walletItem;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_pwd_unlock;
    }

    @Override
    protected void initView() {
        cancelTv = findViewById(R.id.tv_cancel);
        walletNameTv = findViewById(R.id.tv_wallet_name);
        walletSelectTv = findViewById(R.id.tv_select_hint);
        walletPwdEt = findViewById(R.id.et_pwd);
        walletSelectImg = findViewById(R.id.iv_down_arrow);
        otherImg = findViewById(R.id.iv_other);
        authBtn = findViewById(R.id.password_button);
        arrowImg = findViewById(R.id.iv_down_arrow);
    }

    @Override
    protected void initData() {
        walletItems = DBWalletUtil.getAllWallet(this);
        for (int i = 0; i < walletItems.size(); i++) {
            if (walletItems.get(i).name.equals(SharePrefUtil.getCurrentWalletName())) {
                if (i != 0) {
                    Collections.swap(walletItems, 0, i);
                }
                break;
            }
        }
        walletItem = walletItems.get(0);
        walletNameTv.setText(walletItem.name);
    }

    @Override
    protected void initAction() {
        cancelTv.setOnClickListener(this);
        walletSelectTv.setOnClickListener(this);
        walletSelectImg.setOnClickListener(this);
        otherImg.setOnClickListener(this);
        authBtn.setOnClickListener(this);
        arrowImg.setOnClickListener(this);
    }

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.white);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_down_arrow:
            case R.id.tv_select_hint:
                SelectWalletPopupwWindow popupwWindow = new SelectWalletPopupwWindow(this, walletItems, walletItem -> {
                    this.walletItem = walletItem;
                    walletNameTv.setText(walletItem.name);
                });
                popupwWindow.showAsDropDown(walletNameTv, 0, 10);
                break;
        }
    }
}
