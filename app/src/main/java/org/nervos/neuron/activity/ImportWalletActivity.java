package org.nervos.neuron.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.nervos.neuron.R;
import org.nervos.neuron.fragment.ImportKeystoreFragment;
import org.nervos.neuron.fragment.ImportMnemonicFragment;
import org.nervos.neuron.fragment.ImportPrivateKeyFragment;
import org.nervos.neuron.fragment.wallet.presenter.WalletFragmentPresenter;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.view.dialog.ToastSingleButtonDialog;
import org.objectweb.asm.Handle;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ImportWalletActivity extends NBaseActivity {

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
        tabTitles.add(getString(R.string.nmemonic));
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

        if (("QR").equals(getIntent().getStringExtra("from"))) {
            switch (getIntent().getIntExtra("type", 1)) {
                case 1:
                    KeyStore = getIntent().getStringExtra("value");
                    break;
                case 2:
                    PrivateKey = getIntent().getStringExtra("value");
                    viewPager.setCurrentItem(2);
                    break;
            }
        }

        tabLayout.setupWithViewPager(viewPager);
        ToastSingleButtonDialog.getInstance(this, getString(R.string.forbidden_screen_shoot), getString(R.string.forbidden_screen_shoot_import));
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
                    if (padding < 0)
                        padding = 0;

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

    public static void track(String id, boolean suc, String address) {
        try {
            JSONObject object = new JSONObject();
            object.put("input_type", id);
            object.put("input_result", suc);
            object.put("input_address", address);
            SensorsDataAPI.sharedInstance().track("inputWallet", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
