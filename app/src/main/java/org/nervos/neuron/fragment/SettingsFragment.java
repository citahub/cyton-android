package org.nervos.neuron.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AboutUsActivity;
import org.nervos.neuron.activity.CurrencyActivity;
import org.nervos.neuron.activity.SimpleWebActivity;
import org.nervos.neuron.custom.SettingButtonView;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends NBaseFragment {

    public static final String TAG = SettingsFragment.class.getName();
    private SettingButtonView currencySBV, aboutUsSBV, contactUsSBV;
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
    }

    @Override
    public void initData() {
        currencySBV.setOther1Text(SharePrefUtil.getString("Currency", "CNY"));
    }

    @Override
    public void initAction() {
        currencySBV.setOpenListener(() -> {
            Intent intent = new Intent(getActivity(), CurrencyActivity.class);
            startActivityForResult(intent, Currency_Code);
        });
        aboutUsSBV.setOpenListener(() -> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        });
        contactUsSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(getContext(), ConstUtil.CONTACT_US_RUL);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Currency_Code:
                    currencySBV.setOther1Text(SharePrefUtil.getString("Currency", "CNY"));
                    break;
            }
        }
    }
}
