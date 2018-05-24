package org.nervos.neuron.fragment;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.ChainManageActivity;
import org.nervos.neuron.activity.QuickBackupActivity;
import org.nervos.neuron.activity.QuickRestoreActivity;
import org.nervos.neuron.activity.WalletManageActivity;
import org.nervos.neuron.activity.WebActivity;

public class SettingsFragment extends Fragment {

    public static final String TAG = SettingsFragment.class.getName();

    private TextView backupText;
    private TextView restoreText;
    private TextView aboutText;
    private TextView contactText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        backupText = view.findViewById(R.id.setting_backup);
        restoreText = view.findViewById(R.id.setting_restore);
        aboutText = view.findViewById(R.id.setting_about);
        contactText = view.findViewById(R.id.setting_contact);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initListener();
    }

    private void initListener() {
        backupText.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuickBackupActivity.class)));

        restoreText.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuickRestoreActivity.class)));

        aboutText.setOnClickListener(v -> startActivity(new Intent(getActivity(), WebActivity.class)));
    }
}
