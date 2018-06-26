package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import org.nervos.neuron.R;
import org.nervos.neuron.fragment.ImportKeystoreFragment;
import org.nervos.neuron.fragment.ImportMnemonicFragment;
import org.nervos.neuron.fragment.ImportPrivateKeyFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;
import java.util.List;

public class ImportWalletActivity extends BaseActivity {

    private ViewPager viewPager;
    private ImportMnemonicFragment importMnemonicFragment;
    private ImportPrivateKeyFragment importPrivateKeyFragment;
    private ImportKeystoreFragment importKeystoreFragment;

    private List<Fragment> importFragments = new ArrayList<>();
    private List<String> tabTitles = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_wallet);

        initView();
    }


    private void initView() {
        SmartTabLayout tabLayout = findViewById(R.id.viewpager_tab);
        viewPager = findViewById(R.id.viewpager);

        importKeystoreFragment = new ImportKeystoreFragment();
        importMnemonicFragment = new ImportMnemonicFragment();
        importPrivateKeyFragment = new ImportPrivateKeyFragment();
        importFragments.add(importKeystoreFragment);
        importFragments.add(importMnemonicFragment);
        importFragments.add(importPrivateKeyFragment);

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
        tabLayout.setViewPager(viewPager);

    }



}
