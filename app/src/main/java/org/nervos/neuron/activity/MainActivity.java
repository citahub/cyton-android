package org.nervos.neuron.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.fragment.AppFragment;
import org.nervos.neuron.fragment.SettingsFragment;
import org.nervos.neuron.fragment.wallet.view.WalletsFragment;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.response.AppChainTransactionResponse;
import org.nervos.neuron.service.AppChainRpcService;
import org.nervos.neuron.service.AppChainRpcService;
import org.nervos.neuron.service.HttpUrls;
import org.nervos.neuron.service.intentService.TransactionListService;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.QRUtils.CodeUtils;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

public class MainActivity extends NBaseActivity {

    public static final String EXTRA_TAG = "extra_tag";

    private RadioGroup navigation;
    private AppFragment appFragment;
    private WalletsFragment walletsFragment;
    private SettingsFragment settingsFragment;
    private FragmentManager fMgr;
    public static final int REQUEST_CODE_SCAN = 0x01;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.colorPrimary);
    }

    @Override
    protected void initView() {
        navigation = findViewById(R.id.navigation);
        navigation.check(RadioGroup.NO_ID);

    }

    @Override
    protected void initData() {
        fMgr = getSupportFragmentManager();
        if (SharePrefUtil.getFirstIn()) {
            SharePrefUtil.putFirstIn(false);
            new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.dialog_title_tip)
                    .setMessage(R.string.dialog_tip_message)
                    .setPositiveButton(R.string.have_known, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        startCheckTransaction();
    }

    @Override
    protected void initAction() {
        navigation.setOnCheckedChangeListener((group, checkedId) -> {
            FragmentTransaction transaction = fMgr.beginTransaction();
            hideFragments(transaction);
            switch (checkedId) {
                case R.id.navigation_application:
                    if (appFragment == null) {
                        appFragment = new AppFragment();
                        transaction.add(R.id.fragment, appFragment);
                    } else {
                        transaction.show(appFragment);
                    }
                    break;
                case R.id.navigation_wallet:
                    if (DBWalletUtil.getCurrentWallet(mActivity) == null) {
                        startActivity(new Intent(mActivity, AddWalletActivity.class));
                    } else {
                        if (walletsFragment == null) {
                            walletsFragment = new WalletsFragment();
                            transaction.add(R.id.fragment, walletsFragment);
                        } else {
                            transaction.show(walletsFragment);
                        }
                    }
                    break;
                case R.id.navigation_settings:
                    if (settingsFragment == null) {
                        settingsFragment = new SettingsFragment();
                        transaction.add(R.id.fragment, settingsFragment);
                    } else {
                        transaction.show(settingsFragment);
                    }
                    break;
                default:
                    if (appFragment == null) {
                        appFragment = new AppFragment();
                        transaction.add(R.id.fragment, appFragment);
                    } else {
                        transaction.show(appFragment);
                    }
                    break;
            }
            transaction.commitAllowingStateLoss();
        });

        setNavigationItem(AppFragment.TAG);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            setNavigationItem(getIntent().getStringExtra(EXTRA_TAG));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setNavigationItem(intent.getStringExtra(EXTRA_TAG));
    }

    private void startCheckTransaction() {
        AppChainRpcService.init(this, HttpUrls.APPCHAIN_NODE_URL);
        Intent serverIntent = new Intent(this, TransactionListService.class);
        startService(serverIntent);
        TransactionListService.impl = () -> {
            handler.postDelayed(() -> startCheckTransaction(), 3000);
        };
    }

    /**
     * 根据各个Fragment的TAG判断跳转到何处
     *
     * @param tag Fragment标签值
     */
    public void setNavigationItem(String tag) {
        if (TextUtils.isEmpty(tag)) return;
        if (TextUtils.equals(tag, AppFragment.TAG)) {
            navigation.check(R.id.navigation_application);
        } else if (TextUtils.equals(tag, WalletsFragment.TAG)) {
            navigation.check(R.id.navigation_wallet);
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
        if (walletsFragment != null) {
            transaction.hide(walletsFragment);
        }
        if (settingsFragment != null) {
            transaction.hide(settingsFragment);
        }
    }


    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (appFragment.isVisible()) {
                if (appFragment.canGoBack()) {
                    appFragment.goBack();
                    return true;
                } else {
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        Toast.makeText(getApplicationContext(), R.string.press_back_finish,
                                Toast.LENGTH_SHORT).show();
                        exitTime = System.currentTimeMillis();
                        return false;
                    } else {
                        finish();
                        return true;
                    }
                }
            } else {
                FragmentTransaction fragmentTransaction = fMgr.beginTransaction();
                hideFragments(fragmentTransaction);
                setNavigationItem(AppFragment.TAG);
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case REQUEST_CODE_SCAN:
                    if (null != data) {
                        Bundle bundle = data.getExtras();
                        if (bundle == null) return;
                        if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                            String result = bundle.getString(CodeUtils.RESULT_STRING);
                            Intent intent;
                            switch (bundle.getInt(CodeUtils.STRING_TYPE)) {
                                case CodeUtils.STRING_UNVALID:
                                    Toast.makeText(this, R.string.address_error, Toast.LENGTH_LONG).show();
                                    break;
                                case CodeUtils.STRING_ADDRESS:
                                    TokenItem tokenItem = new TokenItem(ConstUtil.ETH_MAINNET, ConstUtil.ETH, ConstUtil.ETHEREUM_ID);
                                    intent = new Intent(mActivity, TransferActivity.class);
                                    intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItem);
                                    intent.putExtra(TransferActivity.EXTRA_ADDRESS, result);
                                    startActivity(intent);
                                    break;
                                case CodeUtils.STRING_KEYSTORE:
                                    intent = new Intent(this, ImportWalletActivity.class);
                                    intent.putExtra("from", "QR");
                                    intent.putExtra("type", 1);
                                    intent.putExtra("value", result);
                                    startActivity(intent);
                                    break;
                                case CodeUtils.STRING_WEB:
                                    SimpleWebActivity.gotoSimpleWeb(this, result);
                                    break;
                                case CodeUtils.STRING_PRIVATE_KEY:
                                    intent = new Intent(this, ImportWalletActivity.class);
                                    intent.putExtra("from", "QR");
                                    intent.putExtra("type", 2);
                                    intent.putExtra("value", result);
                                    startActivity(intent);
                                    break;
                            }
                        } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                            QrCodeActivity.track("0", false);
                            Toast.makeText(this, R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                default:
                    break;
            }
    }

    @Override
    public void finish() {
        System.exit(0);
        super.finish();
    }
}
