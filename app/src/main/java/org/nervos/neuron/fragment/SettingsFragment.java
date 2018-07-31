package org.nervos.neuron.fragment;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AboutUsActivity;
import org.nervos.neuron.activity.CurrencyActivity;
import org.nervos.neuron.activity.SimpleWebActivity;
import org.nervos.neuron.custom.SettingButtonView;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.FingerPrint.AuthenticateResultCallback;
import org.nervos.neuron.util.FingerPrint.FingerPrintController;
import org.nervos.neuron.util.SharePreConst;
import org.nervos.neuron.util.db.SharePrefUtil;

public class SettingsFragment extends NBaseFragment {

    public static final String TAG = SettingsFragment.class.getName();
    private SettingButtonView currencySBV, aboutUsSBV, contactUsSBV, fingerPrintSBV;
    private static final int Currency_Code = 10001;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_settings;
    }

    @Override
    public void initView() {
        currencySBV = (SettingButtonView) findViewById(R.id.sbv_local_coin);
        aboutUsSBV = (SettingButtonView) findViewById(R.id.sbv_about_us);
        contactUsSBV = (SettingButtonView) findViewById(R.id.sbv_contact_us);
        fingerPrintSBV = (SettingButtonView) findViewById(R.id.sbv_fingerprint);
    }

    @Override
    public void initData() {
        currencySBV.setOther1Text(SharePrefUtil.getString(SharePreConst.Currency, "CNY"));
        if (SharePrefUtil.getBoolean(SharePreConst.FingerPrint, false)) {
            fingerPrintSBV.setSwitch(true);
        } else {
            fingerPrintSBV.setSwitch(false);
        }
    }

    @Override
    public void initAction() {
        currencySBV.setOpenListener(() -> {
            Intent intent = new Intent(getActivity(), CurrencyActivity.class);
            startActivityForResult(intent, Currency_Code);
        });
        fingerPrintSBV.setSwitchListener((is) -> {
            if (is) {
                //setting fingerprint
                if (FingerPrintController.getInstance(getActivity()).hasEnrolledFingerprints() && FingerPrintController.getInstance(getActivity()).getEnrolledFingerprints().size() > 0) {
                    FingerPrintController.getInstance(getActivity()).authenticate(authenticateResultCallback);
                } else {
                    Toast.makeText(getActivity(), "您尚未设置Touch ID，请在手机系统“设置>Touch ID与密码”中添加指纹", Toast.LENGTH_SHORT).show();
                }
            } else {
                //close fingerprint
                SharePrefUtil.putBoolean(SharePreConst.FingerPrint, false);
                fingerPrintSBV.setSwitch(false);
            }

        });
        aboutUsSBV.setOpenListener(() -> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        });
        contactUsSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(getContext(), ConstUtil.CONTACT_US_RUL);
        });
    }

    AuthenticateResultCallback authenticateResultCallback = new AuthenticateResultCallback() {
        @Override
        public void onAuthenticationError(String errorMsg) {
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded() {
            fingerPrintSBV.setSwitch(true);
            SharePrefUtil.putBoolean(SharePreConst.FingerPrint, true);
        }

        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(getContext(), "指纹认证失败！", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Currency_Code:
                    currencySBV.setOther1Text(SharePrefUtil.getString(SharePreConst.Currency, "CNY"));
                    break;
            }
        }
    }
}
