package org.nervos.neuron.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.ChainManageActivity;
import org.nervos.neuron.activity.WalletManageActivity;

public class SettingsFragment extends Fragment {

    public static final String TAG = SettingsFragment.class.getName();

    private TextView walletManageText;
    private TextView chainManageText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        walletManageText = view.findViewById(R.id.wallet_manage_text);
        chainManageText = view.findViewById(R.id.chain_manage_text);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initListener();
    }

    private void initListener() {
        walletManageText.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), WalletManageActivity.class));
        });

        chainManageText.setOnClickListener(v -> startActivity(new Intent(getActivity(), ChainManageActivity.class)));
    }
}
