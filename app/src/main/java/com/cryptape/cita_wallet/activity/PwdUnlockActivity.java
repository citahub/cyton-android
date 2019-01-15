package com.cryptape.cita_wallet.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.service.http.WalletService;
import com.cryptape.cita_wallet.view.SelectWalletPopupWindow;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;
import com.cryptape.cita_wallet.view.button.CommonButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/8/6.
 */
public class PwdUnlockActivity extends NBaseActivity implements View.OnClickListener {

    private List<Wallet> wallets = new ArrayList<>();
    private TextView cancelTv, walletNameTv, walletSelectTv;
    private AppCompatEditText walletPwdEt;
    private ImageView walletSelectImg, otherImg;
    private CommonButton authBtn;
    private Wallet wallet;
    private Boolean needToFinger = false;

    @Override
    protected int getContentLayout() {
        inLoginPage = true;
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
    }

    @Override
    protected void initData() {
        wallets = DBWalletUtil.getAllWallet(this);
        for (int i = 0; i < wallets.size(); i++) {
            if (wallets.get(i).name.equals(SharePrefUtil.getCurrentWalletName())) {
                if (i != 0) {
                    Collections.swap(wallets, 0, i);
                }
                break;
            }
        }
        wallet = wallets.get(0);
        walletNameTv.setText(wallet.name);
    }

    @Override
    protected void initAction() {
        cancelTv.setOnClickListener(this);
        walletSelectTv.setOnClickListener(this);
        walletSelectImg.setOnClickListener(this);
        otherImg.setOnClickListener(this);
        authBtn.setOnClickListener(this);
        walletNameTv.setOnClickListener(this);
        walletPwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(walletPwdEt.getText().toString().trim()) && walletPwdEt.getText().toString().length() >= 8) {
                    authBtn.setClickAble(true);
                } else {
                    authBtn.setClickAble(false);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_down_arrow:
            case R.id.tv_select_hint:
            case R.id.tv_wallet_name:
                SelectWalletPopupWindow popupWindow = new SelectWalletPopupWindow(this, wallets, walletItem -> {
                    this.wallet = walletItem;
                    walletNameTv.setText(walletItem.name);
                });
                popupWindow.showAsDropDown(walletNameTv, 0, 10);
                break;
            case R.id.iv_other:
                Intent intent = new Intent(this, FingerPrintActivity.class);
                intent.putExtra(SplashActivity.LOCK_TO_MAIN, getIntent().getBooleanExtra(SplashActivity.LOCK_TO_MAIN, false));
                startActivity(intent);
                finish();
                break;
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.password_button:
                if (!WalletService.checkPassword(mActivity, walletPwdEt.getText().toString().trim(), wallet)) {
                    Toast.makeText(mActivity, getResources().getString(R.string.pwd_auth_failed), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mActivity, getResources().getString(R.string.pwd_auth_success), Toast.LENGTH_LONG).show();
                    if (getIntent().getBooleanExtra(SplashActivity.LOCK_TO_MAIN, false))
                        startActivity(new Intent(mActivity, MainActivity.class));
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        needToFinger = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needToFinger) {
            startActivity(new Intent(this, FingerPrintActivity.class));
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }
        //        appTaskList.get(0).finishAndRemoveTask();
        System.exit(0);
    }
}
