package org.nervos.neuron.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.nervos.neuron.fragment.AppFragment;
import org.nervos.neuron.fragment.SettingsFragment;
import org.nervos.neuron.R;
import org.nervos.neuron.fragment.TransactionFragment;
import org.nervos.neuron.fragment.WalletFragment;
import org.nervos.neuron.util.SharePrefUtil;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_TAG = "extra_tag";

    private RadioGroup navigation;
    private AppFragment appFragment;
    private WalletFragment walletFragment;
    private SettingsFragment settingsFragment;
    private TransactionFragment transactionFragment;
    private FragmentManager fMgr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();

    }


    private void initView(){
        navigation = findViewById(R.id.navigation);
        navigation.check(RadioGroup.NO_ID);
        fMgr = getSupportFragmentManager();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setNavigationItem(intent.getStringExtra(EXTRA_TAG));
    }

    private void initListener() {
        navigation.setOnCheckedChangeListener((group, checkedId) -> {
            FragmentTransaction transaction = fMgr.beginTransaction();
            hideFragments(transaction);
            switch (checkedId) {
                case R.id.navigation_application:
                    if(appFragment == null){
                        appFragment = new AppFragment();
                        transaction.add(R.id.fragment, appFragment);
                    } else {
                        transaction.show(appFragment);
                    }
                    break;
                case R.id.navigation_wallet:
                    if (TextUtils.isEmpty(SharePrefUtil.getWalletName())) {
                        startActivity(new Intent(MainActivity.this, CreateWalletActivity.class));
                    } else {
                        if(walletFragment == null){
                            walletFragment = new WalletFragment();
                            transaction.add(R.id.fragment, walletFragment);
                        } else {
                            transaction.show(walletFragment);
                        }
                    }
                    break;
                case R.id.navigation_settings:
                    if(settingsFragment == null){
                        settingsFragment = new SettingsFragment();
                        transaction.add(R.id.fragment, settingsFragment);
                    } else {
                        transaction.show(settingsFragment);
                    }
                    break;
                case R.id.navigation_transaction:
                    if(transactionFragment == null){
                        transactionFragment = new TransactionFragment();
                        transaction.add(R.id.fragment, transactionFragment);
                    } else {
                        transaction.show(transactionFragment);
                    }
                    break;
                default:
                    if(walletFragment == null){
                        walletFragment = new WalletFragment();
                        transaction.add(R.id.fragment, walletFragment);
                    } else {
                        transaction.show(walletFragment);
                    }
                    break;
            }
            transaction.commitAllowingStateLoss();
        });

        setNavigationItem(AppFragment.TAG);

    }

    /**
     * 根据各个Fragment的TAG判断跳转到何处
     *
     * @param tag Fragment标签值
     */
    public void setNavigationItem(String tag) {
        if (TextUtils.equals(tag, AppFragment.TAG)) {
            navigation.check(R.id.navigation_application);
        } else if (TextUtils.equals(tag, WalletFragment.TAG)) {
            navigation.check(R.id.navigation_wallet);
        } else if (TextUtils.equals(tag, TransactionFragment.TAG)) {
            navigation.check(R.id.navigation_transaction);
        } else if (TextUtils.equals(tag, SettingsFragment.TAG)) {
            navigation.check(R.id.navigation_settings);
        }
    }


    /**
     * 隐藏所有的fragment
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (appFragment != null) {
            transaction.hide(appFragment);
        }
        if (walletFragment != null) {
            transaction.hide(walletFragment);
        }
        if (transactionFragment != null) {
            transaction.hide(transactionFragment);
        }
        if (settingsFragment != null) {
            transaction.hide(settingsFragment);
        }
    }



    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (fMgr.findFragmentByTag(WalletFragment.TAG) != null && !fMgr.findFragmentByTag(WalletFragment.TAG).isVisible()) {
                FragmentTransaction fragmentTransaction = fMgr.beginTransaction();
                hideFragments(fragmentTransaction);
                setNavigationItem(WalletFragment.TAG);
                return true;
            } else if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
                return false;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
