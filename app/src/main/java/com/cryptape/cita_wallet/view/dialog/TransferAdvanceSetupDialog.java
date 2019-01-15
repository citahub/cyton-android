package com.cryptape.cita_wallet.view.dialog;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.view.button.CommonButton;


public class TransferAdvanceSetupDialog {

    private BottomSheetDialog mDialog;
    private Context mContext;
    private TextView gasPriceDefaultText, gasLimitDefaultText;
    private EditText mGasPriceEdit;
    private OnOkClickListener mListener;

    public TransferAdvanceSetupDialog(Context context, OnOkClickListener listener) {
        mContext = context;
        mDialog = new BottomSheetDialog(context);
        mListener = listener;

        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_transfer_advance_setup, null);
        mGasPriceEdit = view.findViewById(R.id.edit_gas_price);
        gasPriceDefaultText = view.findViewById(R.id.default_gas_price_text);
        gasLimitDefaultText = view.findViewById(R.id.default_gas_limit_text);
        CommonButton confirmButton = view.findViewById(R.id.advanced_setup_button);
        view.findViewById(R.id.close_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        confirmButton.setOnClickListener(v -> {
            String gas = mGasPriceEdit.getText().toString().trim();
            if (Double.parseDouble(gas) < 1) {
                Toast.makeText(mContext, R.string.gas_price_too_low, Toast.LENGTH_LONG).show();
            } else {
                mListener.onOkClick(v, mGasPriceEdit.getText().toString().trim());
                mDialog.dismiss();
            }
        });
        mDialog.setContentView(view);
    }

    public void setGasPriceDefault(String value) {
        gasPriceDefaultText.setText(value);
    }

    public void setGasLimitDefault(String value) {
        gasLimitDefaultText.setText(value);
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    public interface OnOkClickListener {
        void onOkClick(View v, String gasPriceDefault);
    }

}
