package com.cryptape.cita_wallet.activity;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.fragment.ImportKeystoreFragment;
import com.cryptape.cita_wallet.fragment.ImportMnemonicFragment;
import com.cryptape.cita_wallet.fragment.ImportPrivateKeyFragment;
import com.cryptape.cita_wallet.view.TitleBar;
import com.cryptape.cita_wallet.view.dialog.ToastSingleButtonDialog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanyytop on 2018/5/8
 */
public class ImportWalletActivity extends NBaseActivity {

    public static final String INTENT_FROM = "FROM";
    public static final String INTENT_FROM_VALUE = "QR";
    public static final String QR_CODE_TYPE = "TYPE";
    public static final String QR_CODE_TYPE_KEYSTORE = "KEYSTORE";
    public static final String QR_CODE_TYPE_PRIVATEKEY = "PRIVATEKEY";
    public static final String QR_CODE_VALUE = "VALUE";

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ImportMnemonicFragment importMnemonicFragment;
    private ImportPrivateKeyFragment importPrivateKeyFragment;
    private ImportKeystoreFragment importKeystoreFragment;
    private List<Fragment> importFragments = new ArrayList<>();
    private List<String> tabTitles = new ArrayList<>();
    public static String KeyStore = "";
    public static String PrivateKey = "";

    @Override
    protected int getContentLayout() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        return R.layout.activity_import_wallet;
    }

    @Override
    protected void initView() {
        tabLayout = findViewById(R.id.viewpager_tab);
        viewPager = findViewById(R.id.viewpager);

        importKeystoreFragment = new ImportKeystoreFragment();
        importMnemonicFragment = new ImportMnemonicFragment();
        importPrivateKeyFragment = new ImportPrivateKeyFragment();
        importFragments.add(importKeystoreFragment);
        importFragments.add(importMnemonicFragment);
        importFragments.add(importPrivateKeyFragment);
    }

    @Override
    protected void initData() {
        ((TitleBar) findViewById(R.id.title)).setOnLeftClickListener(() -> {
            startActivity(new Intent(this, AddWalletActivity.class));
            finish();
        });

        tabLayout.post(() -> setTabWidth(tabLayout));

        tabTitles.add(getString(R.string.keystore));
        tabTitles.add(getString(R.string.mnemonic));
        tabTitles.add(getString(R.string.private_key));
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return importFragments.get(position);
            }

            @Override
            public int getCount() {
                return importFragments.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitles.get(position);
            }

        };

        viewPager.setAdapter(adapter);

        if ((INTENT_FROM_VALUE).equals(getIntent().getStringExtra(INTENT_FROM))) {
            switch (getIntent().getStringExtra(QR_CODE_TYPE)) {
                case QR_CODE_TYPE_KEYSTORE:
                    KeyStore = getIntent().getStringExtra(QR_CODE_VALUE);
                    break;
                case QR_CODE_TYPE_PRIVATEKEY:
                    PrivateKey = getIntent().getStringExtra(QR_CODE_VALUE);
                    viewPager.setCurrentItem(2);
                    break;
            }
        }

        tabLayout.setupWithViewPager(viewPager);
        ToastSingleButtonDialog
                .getInstance(this, getString(R.string.forbidden_screen_shoot), getString(R.string.forbidden_screen_shoot_import));
    }

    @Override
    protected void initAction() {

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, AddWalletActivity.class));
        finish();
    }

    public static void setTabWidth(final TabLayout tabLayout) {
        tabLayout.post(() -> {
            try {
                LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);


                for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                    View tabView = mTabStrip.getChildAt(i);

                    Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                    mTextViewField.setAccessible(true);

                    TextView mTextView = (TextView) mTextViewField.get(tabView);

                    tabView.setPadding(0, 0, 0, 0);

                    int tabWidth = tabView.getWidth();
                    if (tabWidth == 0) {
                        mTextView.measure(0, 0);
                        tabWidth = mTextView.getMeasuredWidth();
                    }

                    int width = 0;
                    width = mTextView.getWidth();
                    if (width == 0) {
                        mTextView.measure(0, 0);
                        width = mTextView.getMeasuredWidth();
                    }

                    int padding = (tabWidth - width - 40) / 2;
                    if (padding < 0) padding = 0;

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                    params.width = width;
                    params.leftMargin = padding;
                    params.rightMargin = padding;
                    tabView.setLayoutParams(params);

                    tabView.invalidate();
                }

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

    }
}
