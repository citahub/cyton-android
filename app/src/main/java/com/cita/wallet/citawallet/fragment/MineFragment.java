package com.cita.wallet.citawallet.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cita.wallet.citawallet.R;
import com.cita.wallet.citawallet.activity.ChainManageActivity;
import com.cita.wallet.citawallet.activity.ImportWalletActivity;
import com.cita.wallet.citawallet.activity.WalletManageActivity;

public class MineFragment extends Fragment {

    public static final String TAG = MineFragment.class.getName();

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
