package org.nervos.neuron.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AboutUsActivity;
import org.nervos.neuron.activity.CurrencyActivity;
import org.nervos.neuron.activity.SimpleWebActivity;
import org.nervos.neuron.custom.SettingButtonView;
import org.nervos.neuron.dialog.AuthFingerDialog;
import org.nervos.neuron.service.HttpUrls;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.FingerPrint.AuthenticateResultCallback;
import org.nervos.neuron.util.FingerPrint.FingerPrintController;
import org.nervos.neuron.util.db.SharePrefUtil;

public class SettingsFragment extends NBaseFragment {

    public static final String TAG = SettingsFragment.class.getName();
    private SettingButtonView currencySBV, aboutUsSBV, contactUsSBV, fingerPrintSBV;
    private static final int Currency_Code = 10001;
    private AuthFingerDialog authFingerDialog = null;

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
        currencySBV.setOther1Text(SharePrefUtil.getString(ConstUtil.Currency, "CNY"));
        if (FingerPrintController.getInstance(getActivity()).isSupportFingerprint()) {
            fingerPrintSBV.setVisibility(View.VISIBLE);
            if (SharePrefUtil.getBoolean(ConstUtil.FingerPrint, false)) {
                fingerPrintSBV.setSwitch(true);
            } else {
                SharePrefUtil.putBoolean(ConstUtil.FingerPrint, false);
                fingerPrintSBV.setSwitch(false);
            }
        } else {
            fingerPrintSBV.setVisibility(View.GONE);
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
                    if (authFingerDialog == null)
                        authFingerDialog = new AuthFingerDialog(getActivity());
                    authFingerDialog.setOnShowListener((dialogInterface) -> {
                        FingerPrintController.getInstance(getActivity()).authenticate(authenticateResultCallback);
                    });
                    authFingerDialog.setOnDismissListener((dialog) -> {
                        FingerPrintController.getInstance(getActivity()).cancelAuth();
                    });
                    authFingerDialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.dialog_title_tip));
                    builder.setMessage(getResources().getString(R.string.dialog_finger_setting));
                    builder.setPositiveButton(getResources().getString(R.string.ok), (view, i) -> {
                        FingerPrintController.openFingerPrintSettingPage(getActivity());
                        view.dismiss();
                    });
                    builder.show();
                }
            } else {
                //close fingerprint
                SharePrefUtil.putBoolean(ConstUtil.FingerPrint, false);
                fingerPrintSBV.setSwitch(false);
            }

        });
        aboutUsSBV.setOpenListener(() -> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        });
        contactUsSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(getContext(), HttpUrls.CONTACT_US_RUL);
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
            if (authFingerDialog != null && authFingerDialog.isShowing())
                authFingerDialog.dismiss();
            SharePrefUtil.putBoolean(ConstUtil.FingerPrint, true);
            Toast.makeText(getContext(), getResources().getString(R.string.fingerprint_setting_sucess), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(getContext(), getResources().getString(R.string.fingerprint_setting_failed), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Currency_Code:
                    currencySBV.setOther1Text(SharePrefUtil.getString(ConstUtil.Currency, "CNY"));
                    break;
            }
        }
    }
}
