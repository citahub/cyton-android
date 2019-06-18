package com.cryptape.cita_wallet.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.activity.AboutUsActivity;
import com.cryptape.cita_wallet.activity.CurrencyActivity;
import com.cryptape.cita_wallet.activity.SimpleWebActivity;
import com.cryptape.cita_wallet.activity.WalletManageActivity;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.service.http.EthRpcService;
import com.cryptape.cita_wallet.util.Blockies;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;
import com.cryptape.cita_wallet.util.fingerprint.AuthenticateResultCallback;
import com.cryptape.cita_wallet.util.fingerprint.FingerPrintController;
import com.cryptape.cita_wallet.constant.url.HttpUrls;
import com.cryptape.cita_wallet.view.SettingButtonView;
import com.cryptape.cita_wallet.view.dialog.AuthFingerDialog;
import com.cryptape.cita_wallet.view.dialog.SimpleSelectDialog;
import com.cryptape.cita_wallet.view.dialog.ToastSingleButtonDialog;

import java.util.Arrays;
import java.util.List;

/**
 * Created by duanyytop on 2018/4/17
 */
public class SettingsFragment extends NBaseFragment {

    private static final int Currency_Code = 10001;
    public static final String TAG = SettingsFragment.class.getName();
    private SettingButtonView mSbvCurrency, mSbvAboutUs, mSbvContactUs, mSbvFingerPrint, mSbvForums, mSbvSelectEth;
    private TextView walletNameText, walletAddressText;
    private CircleImageView photoImage;
    private RelativeLayout walletLayout;
    private AuthFingerDialog mAuthFingerDialog = null;
    private List<String> ethNodeList = Arrays.asList(ConstantUtil.ETH_NET_MAIN, ConstantUtil.ETH_NET_ROPSTEN_TEST
            , ConstantUtil.ETH_NET_KOVAN_TEST, ConstantUtil.ETH_NET_RINKEBY_TEST);
    private int ethNodeIndex;
    private FingerPrintController mFingerPrintController;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_settings;
    }

    @Override
    public void initView() {
        mSbvCurrency = (SettingButtonView) findViewById(R.id.sbv_local_coin);
        mSbvAboutUs = (SettingButtonView) findViewById(R.id.sbv_about_us);
        mSbvContactUs = (SettingButtonView) findViewById(R.id.sbv_contact_us);
        mSbvFingerPrint = (SettingButtonView) findViewById(R.id.sbv_fingerprint);
        mSbvForums = (SettingButtonView) findViewById(R.id.sbv_forums);
        mSbvSelectEth = (SettingButtonView) findViewById(R.id.sbv_eth_select);
        walletNameText = (TextView) findViewById(R.id.text_wallet_name);
        walletAddressText = (TextView) findViewById(R.id.text_wallet_address);
        walletLayout = (RelativeLayout) findViewById(R.id.wallet_information_layout);
        photoImage = (CircleImageView) findViewById(R.id.wallet_photo);
    }

    @Override
    public void initData() {
        Wallet wallet = DBWalletUtil.getCurrentWallet(getContext());
        walletNameText.setText(wallet.name);
        walletAddressText.setText(wallet.address);
        photoImage.setImageBitmap(Blockies.createIcon(wallet.address));

        updateEthNode();

        mFingerPrintController = new FingerPrintController(getActivity());
        mSbvCurrency.setRightText(SharePrefUtil.getString(ConstantUtil.CURRENCY, ConstantUtil.DEFAULT_CURRENCY));
        if (mFingerPrintController.isSupportFingerprint()) {
            mSbvFingerPrint.setVisibility(View.VISIBLE);
            if (SharePrefUtil.getBoolean(ConstantUtil.FINGERPRINT, false)) {
                mSbvFingerPrint.setSwitchCheck(true);
            } else {
                SharePrefUtil.putBoolean(ConstantUtil.FINGERPRINT, false);
                mSbvFingerPrint.setSwitchCheck(false);
            }
        } else {
            mSbvFingerPrint.setVisibility(View.GONE);
        }
        String node = SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_MAINNET);
        if (node.contains("_")) node = node.replace("_", " ");
        mSbvSelectEth.setRightText(node);
    }

    @Override
    public void initAction() {
        mSbvCurrency.setOnClickListener(() -> {
            Intent intent = new Intent(getActivity(), CurrencyActivity.class);
            startActivityForResult(intent, Currency_Code);
        });
        mSbvFingerPrint.setSwitchCheckedListener(isChecked -> {
            if (isChecked) {
                //setting fingerprint
                if (mFingerPrintController.hasEnrolledFingerprints() && mFingerPrintController.getEnrolledFingerprints().size() > 0) {
                    if (mAuthFingerDialog == null)
                        mAuthFingerDialog = new AuthFingerDialog(getActivity());
                    mAuthFingerDialog.setOnShowListener((dialogInterface) -> {
                        mFingerPrintController.authenticate(authenticateResultCallback);
                    });
                    mAuthFingerDialog.setOnDismissListener((dialog) -> {
                        mFingerPrintController.cancelAuth();
                    });
                    mAuthFingerDialog.show();
                } else {
                    ToastSingleButtonDialog dialog = ToastSingleButtonDialog.getInstance(
                            getActivity(), getResources().getString(R.string.dialog_finger_setting));
                    dialog.setOnCancelClickListener(view -> {
                        FingerPrintController.openFingerPrintSettingPage(getActivity());
                        view.dismiss();
                    });
                }
            } else {
                //close fingerprint
                SharePrefUtil.putBoolean(ConstantUtil.FINGERPRINT, false);
                mSbvFingerPrint.setSwitchCheck(false);
            }
        });
        mSbvAboutUs.setOnClickListener(() -> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        });
        mSbvContactUs.setOnClickListener(() -> {
            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("contact", "Nervos-Neuron");
            if (cm != null) {
                cm.setPrimaryClip(mClipData);
                Toast.makeText(getActivity(), R.string.copy_weixin_success, Toast.LENGTH_SHORT).show();
            }
        });
        mSbvForums.setOnClickListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(getActivity(), HttpUrls.CITAHUB_TALK_URL);
        });
        mSbvSelectEth.setOnClickListener(() -> {
            SimpleSelectDialog dialog = new SimpleSelectDialog(getActivity(), ethNodeList, -1);
            dialog.setMSelected(ethNodeIndex);
            dialog.setOnOkListener((view -> {
                ethNodeIndex = dialog.getMSelected();
                SharePrefUtil.putString(ConstantUtil.ETH_NET, ethNodeList.get(ethNodeIndex));
                dialog.dismiss();
            }));
            dialog.setOnDismissListener(dialogInterface -> {
                EthRpcService.initNodeUrl();
                updateEthNode();
                String node = SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_NET_MAIN);
                if (node.contains("_")) node = node.replace("_", " ");
                mSbvSelectEth.setRightText(node);
                dialog.dismiss();
            });
        });
        walletLayout.setOnClickListener(v -> startActivity(new Intent(getContext(), WalletManageActivity.class)));
    }

    private void updateEthNode() {
        switch (SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_NET_MAIN)) {
            case ConstantUtil.ETH_NET_MAIN:
            default:
                ethNodeIndex = 0;
                break;
            case ConstantUtil.ETH_NET_ROPSTEN_TEST:
                ethNodeIndex = 1;
                break;
            case ConstantUtil.ETH_NET_KOVAN_TEST:
                ethNodeIndex = 2;
                break;
            case ConstantUtil.ETH_NET_RINKEBY_TEST:
                ethNodeIndex = 3;
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    AuthenticateResultCallback authenticateResultCallback = new AuthenticateResultCallback() {
        @Override
        public void onAuthenticationError(String errorMsg) {
            mSbvFingerPrint.setSwitchCheck(false);
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded() {
            mSbvFingerPrint.setSwitchCheck(true);
            if (mAuthFingerDialog != null && mAuthFingerDialog.isShowing())
                mAuthFingerDialog.dismiss();
            SharePrefUtil.putBoolean(ConstantUtil.FINGERPRINT, true);
            Toast.makeText(getContext(), getResources().getString(R.string.fingerprint_setting_sucess), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationFailed() {
            mSbvFingerPrint.setSwitchCheck(false);
            Toast.makeText(getContext(), getResources().getString(R.string.fingerprint_setting_failed), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Currency_Code:
                    mSbvCurrency.setRightText(SharePrefUtil.getString(ConstantUtil.CURRENCY, "CNY"));
                    break;
            }
        }
    }
}
