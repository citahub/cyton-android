package com.cryptape.cita_wallet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import com.zhy.view.flowlayout.TagView;

import org.greenrobot.eventbus.EventBus;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.event.CloseWalletInfoEvent;
import com.cryptape.cita_wallet.event.TokenRefreshEvent;
import com.cryptape.cita_wallet.event.WalletSaveEvent;
import com.cryptape.cita_wallet.fragment.wallet.WalletFragment;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;
import com.cryptape.cita_wallet.util.fingerprint.FingerPrintController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by duanyytop on 2018/5/17
 */
public class ConfirmMnemonicActivity extends BaseActivity {

    private TagFlowLayout selectedTagLayout;
    private TagFlowLayout unselectTagLayout;

    private TagAdapter<String> selectedAdapter;
    private TagAdapter<String> unselectAdapter;

    private String[] mnemonics;
    private List<String> shuffleList = new ArrayList<>();
    private List<String> originList = new ArrayList<>();
    private List<String> confirmList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_mnemonic);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        mnemonics = getIntent().getStringArrayExtra(CreateWalletActivity.EXTRA_MNEMONIC);
        originList = Arrays.asList(mnemonics);
        shuffleList.addAll(originList);
        Collections.shuffle(shuffleList);

        initView();
        initAdapter();
        backupComplete();
    }

    private void initView() {
        selectedTagLayout = findViewById(R.id.mnemonic_text_layout);
        unselectTagLayout = findViewById(R.id.mnemonic_tag_layout);
    }

    private void initAdapter() {
        unselectAdapter = new TagAdapter<String>(shuffleList) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                TextView mnemonicView = (TextView) getLayoutInflater().inflate(R.layout.item_mnemonic_tag, null);
                mnemonicView.setText(s);
                return mnemonicView;
            }

            @Override
            public void onSelected(int position, View view) {
                super.onSelected(position, view);
                confirmList.add(shuffleList.get(position));
                selectedAdapter.notifyDataChanged();
            }

            @Override
            public void unSelected(int position, View view) {
                super.unSelected(position, view);
                confirmList.remove(shuffleList.get(position));
                selectedAdapter.notifyDataChanged();
            }

        };

        selectedAdapter = new TagAdapter<String>(confirmList) {
            @Override
            public View getView(FlowLayout parent, int position, String str) {
                TextView mnemonicView = (TextView) getLayoutInflater().inflate(R.layout.item_mnemonic_text, null);
                mnemonicView.setText(str);
                return mnemonicView;
            }
        };

        selectedTagLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                int viewIndex = shuffleList.indexOf(confirmList.get(position));
                TagView tagView = (TagView) unselectTagLayout.getChildAt(viewIndex);
                tagView.setChecked(false);

                confirmList.remove(confirmList.get(position));
                selectedAdapter.notifyDataChanged();

                return true;
            }
        });

        unselectTagLayout.setAdapter(unselectAdapter);
        selectedTagLayout.setAdapter(selectedAdapter);

    }

    private void backupComplete() {
        findViewById(R.id.backup_complete).setOnClickListener(v -> {
            if (confirmList.equals(originList)) {
                EventBus.getDefault().post(new WalletSaveEvent());
                Toast.makeText(ConfirmMnemonicActivity.this, R.string.backup_successful, Toast.LENGTH_SHORT).show();
                if (new FingerPrintController(this).isSupportFingerprint() && !SharePrefUtil.getBoolean(ConstantUtil.FINGERPRINT, false) &&
                        !SharePrefUtil.getBoolean(ConstantUtil.FINGERPRINT_TIP, false)) {
                    Intent intent = new Intent(ConfirmMnemonicActivity.this, ImportFingerTipActivity.class);
                    startActivity(intent);
                    SharePrefUtil.putBoolean(ConstantUtil.FINGERPRINT_TIP, true);
                } else {
                    EventBus.getDefault().post(new TokenRefreshEvent());
                    Intent intent = new Intent(ConfirmMnemonicActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                EventBus.getDefault().post(new CloseWalletInfoEvent());
                finish();
            } else {
                Toast.makeText(ConfirmMnemonicActivity.this, R.string.mnemonic_auth_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
