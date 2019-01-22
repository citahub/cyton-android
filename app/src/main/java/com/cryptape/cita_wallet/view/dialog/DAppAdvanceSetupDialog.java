package com.cryptape.cita_wallet.view.dialog;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.view.button.CommonButton;
import org.web3j.utils.Numeric;

public class DAppAdvanceSetupDialog {

    private BottomSheetDialog mDialog;
    private Context mContext;
    private TextView gasPriceDefaultText, gasFeeTokenText, gasFeeSumText, tvTransactionData;
    private EditText mGasPriceEdit;
    private OnOkClickListener mListener;
    private String mTransactionData;

    public DAppAdvanceSetupDialog(Context context, OnOkClickListener listener) {
        mContext = context;
        mDialog = new BottomSheetDialog(context);
        mListener = listener;

        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_dapp_advance_setup, null);
        mGasPriceEdit = view.findViewById(R.id.edit_dapp_gas_price);
        gasPriceDefaultText = view.findViewById(R.id.default_dapp_gas_price_text);
        gasFeeTokenText = view.findViewById(R.id.tv_gas_fee_token);
        gasFeeSumText = view.findViewById(R.id.tv_gas_fee_sum);
        tvTransactionData = view.findViewById(R.id.tv_transaction_data);
        CommonButton confirmButton = view.findViewById(R.id.advanced_setup_button);
        view.findViewById(R.id.close_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        view.findViewById(R.id.hex_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.hex_selector).setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.colorPrimary));
                view.findViewById(R.id.utf8_selector).setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.line_form));
                if (!TextUtils.isEmpty(mTransactionData)) {
                    tvTransactionData.setText(mTransactionData);
                }
            }
        });
        view.findViewById(R.id.utf8_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.utf8_selector).setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.colorPrimary));
                view.findViewById(R.id.hex_selector).setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.line_form));
                if (!TextUtils.isEmpty(mTransactionData)) {
                    tvTransactionData.setText(NumberUtil.hexToUtf8(mTransactionData));
                }
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onOkClick(v, mGasPriceEdit.getText().toString().trim());
                mDialog.dismiss();
            }
        });
        mDialog.setContentView(view);
    }

    public void setGasPriceDefault(String value) {
        gasPriceDefaultText.setText(String.format(mContext.getString(R.string.default_eth_gas_price), value));
    }

    public void setGasFeeDefault(String gasLimit, String gasPrice, double gasFee) {
        gasFeeTokenText.setText(String.format(mContext.getString(R.string.gas_fee_token_value), gasFee));
        String gasLimitValue = NumberUtil.isHex(gasLimit)? Numeric.toBigInt(gasLimit).toString() : gasLimit;
        gasFeeSumText.setText(String.format(mContext.getString(R.string.gas_fee_sum_value),
                gasLimitValue, gasPrice));
    }

    public void setTransactionData(String data) {
        mTransactionData = data;
        tvTransactionData.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvTransactionData.setText(data);
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
