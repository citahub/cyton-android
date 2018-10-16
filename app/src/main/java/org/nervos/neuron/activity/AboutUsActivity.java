package org.nervos.neuron.activity;

import android.content.pm.PackageManager;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.view.SettingButtonView;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.service.HttpUrls;

/**
 * Created by 包俊 on 2018/7/30.
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
        sourceCodeSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.SOURCE_CODE_GITHUB_URL);
        });
        serverPrivateSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.PRODUCT_AGREEMENT_URL);
        });
        NervosSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.NERVOS_NETWORK);
        });
        InfuraSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.INFURA);
        });
        openSeaSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.OPEN_SEA);
        });
        peckShieldSBV.setOpenListener(() -> SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.PECKSHEILD));
        citaSBV.setOpenListener(() -> SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, HttpUrls.CITA));
    }
}
