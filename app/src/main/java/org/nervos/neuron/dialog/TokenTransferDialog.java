package org.nervos.neuron.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.nervos.neuron.R;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.NumberUtil;

public class TokenTransferDialog extends Dialog {

    private TextView tokenNameText;
    private TextView tokenBalanceText;
    private SimpleDraweeView tokenImage;
    private AppCompatButton transferButton;
    private AppCompatButton receiveButton;

    private TokenItem tokenItem;

    private OnTransferClickListener onTransferClickListener;
    private OnReceiveClickListener onReceiveClickListener;


    public TokenTransferDialog(Context context, TokenItem tokenItem) {
        super(context, R.style.TransferDialog);
        this.tokenItem = tokenItem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_token_transfer);

        initView();
        initListener();
    }

    private void initView() {
        tokenNameText = findViewById(R.id.dialog_token_name);
        tokenImage = findViewById(R.id.dialog_token_image);
        tokenBalanceText = findViewById(R.id.dialog_token_balance);
        transferButton = findViewById(R.id.dialog_button_token_transfer);
        receiveButton = findViewById(R.id.dialog_button_token_receive);

        tokenNameText.setText(tokenItem.symbol);
        if (TextUtils.isEmpty(tokenItem.avatar)) {
            tokenImage.setImageResource(tokenItem.chainId < 0?
                    R.drawable.ether_small:R.mipmap.ic_launcher);
        } else {
            tokenImage.setImageURI(tokenItem.avatar);
        }
        tokenBalanceText.setText(NumberUtil.getDecimal_4(tokenItem.balance));
    }

    private void initListener() {
        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTransferClickListener != null) {
                    onTransferClickListener.onClick(v);
                }
            }
        });
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onReceiveClickListener != null) {
                    onReceiveClickListener.onClick(v);
                }
            }
        });
    }

    public void setOnReceiveClickListener(OnReceiveClickListener onReceiveClickListener) {
        this.onReceiveClickListener = onReceiveClickListener;
    }

    public void setOnTransferClickListener(OnTransferClickListener onTransferClickListener) {
        this.onTransferClickListener = onTransferClickListener;
    }

    public interface OnTransferClickListener{
        void onClick(View v);
    }

    public interface OnReceiveClickListener{
        void onClick(View v);
    }
}
