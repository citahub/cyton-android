package com.cryptape.cita_wallet.activity;

import android.content.pm.PackageManager;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.constant.url.HttpUrls;
import com.cryptape.cita_wallet.view.SettingButtonView;
import com.cryptape.cita_wallet.view.TitleBar;

/**
 * Created by BaojunCZ on 2018/7/30.
 */
public class AboutUsActivity extends NBaseActivity {

    private TextView versionText;
    private TitleBar title;
    private SettingButtonView sourceCodeSBV, serverPrivateSBV, NervosSBV, InfuraSBV, openSeaSBV, peckShieldSBV, citaSBV;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_about_us;
    }

    @Override
    protected void initView() {
        versionText = findViewById(R.id.app_version);
        sourceCodeSBV = findViewById(R.id.sbv_source_code);
        serverPrivateSBV = findViewById(R.id.sbv_server_private);
        NervosSBV = findViewById(R.id.sbv_nervos_network);
        InfuraSBV = findViewById(R.id.sbv_infura);
        openSeaSBV = findViewById(R.id.sbv_open_sea);
        title = findViewById(R.id.title);
        peckShieldSBV = findViewById(R.id.sbv_peck_shield);
        citaSBV = findViewById(R.id.sbv_cita);
    }

    @Override
    protected void initAction() {
        initListener();
    }

    @Override
    protected void initData() {
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(String.format("V %s", versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        title.setLeftImage(R.drawable.ic_toptitle_back_white);
    }

    private void initListener() {
        sourceCodeSBV.setOnClickListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.SOURCE_CODE_GITHUB_URL);
        });
        serverPrivateSBV.setOnClickListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.PRODUCT_AGREEMENT_URL);
        });
        NervosSBV.setOnClickListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.NERVOS_WEB_URL);
        });
        InfuraSBV.setOnClickListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.INFURA_URL);
        });
        openSeaSBV.setOnClickListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.OPEN_SEA_URL);
        });
        peckShieldSBV.setOnClickListener(() -> SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.PECKSHEILD_URL));
        citaSBV.setOnClickListener(() -> SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.CITA_GITHUB_URL));
    }
}
