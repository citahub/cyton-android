package org.nervos.neuron.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.nervos.neuron.R;
import org.nervos.neuron.fragment.ImportKeystoreFragment;
import org.nervos.neuron.fragment.ImportMnemonicFragment;
import org.nervos.neuron.fragment.ImportPrivateKeyFragment;
import org.nervos.neuron.view.dialog.ToastSingleButtonDialog;
import org.objectweb.asm.Handle;

import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;
import java.util.List;

public class ImportWalletActivity extends NBaseActivity {

    private ViewPager viewPager;
    private SmartTabLayout tabLayout;
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

        tabLayout.setViewPager(viewPager);

        try {
            JSONObject object = new JSONObject();
            object.put("title", "禁止截屏");
            object.put("info", getString(R.string.forbidden_screen_shoot_backup));
            ToastSingleButtonDialog.getInstance(this, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initAction() {

    }
}
