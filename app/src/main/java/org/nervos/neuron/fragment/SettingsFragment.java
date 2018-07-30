package org.nervos.neuron.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AboutUsActivity;
import org.nervos.neuron.activity.SimpleWebActivity;
import org.nervos.neuron.custom.SettingButtonView;
import org.nervos.neuron.util.ConstUtil;

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends NBaseFragment {

    public static final String TAG = SettingsFragment.class.getName();
    private SettingButtonView localCoinSBV, aboutUsSBV, contactUsSBV;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_settings;
    }

    @Override
    public void initView() {
        localCoinSBV = (SettingButtonView) findViewById(R.id.sbv_local_coin);
        aboutUsSBV = (SettingButtonView) findViewById(R.id.sbv_about_us);
        contactUsSBV = (SettingButtonView) findViewById(R.id.sbv_contact_us);
    }

    @Override
    public void initData() {
        localCoinSBV.setOther1Text("CNY");
    }

    @Override
    public void initAction() {
        localCoinSBV.setOpenListener(() -> {
            Toast.makeText(getActivity(), "open", Toast.LENGTH_LONG).show();
        });
        aboutUsSBV.setOpenListener(() -> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        });
        contactUsSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(getContext(), ConstUtil.CONTACT_US_RUL);
        });
    }
}
