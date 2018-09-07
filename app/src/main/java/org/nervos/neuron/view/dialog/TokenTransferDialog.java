package org.nervos.neuron.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.nervos.neuron.R;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.NumberUtil;

public class TokenTransferDialog extends Dialog {

    private TextView tokenNameText;
    private TextView tokenBalanceText;
    private ImageView tokenImage;
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
            int id = tokenItem.chainId < 0 ?
                    R.drawable.ether_small : R.mipmap.ic_launcher;
            Glide.with(getContext())
                    .load(id)
                    .into(tokenImage);
        } else {
            Glide.with(getContext())
                    .load(Uri.parse(tokenItem.avatar))
                    .into(tokenImage);
        }
        tokenBalanceText.setText(NumberUtil.getDecimal8ENotation(tokenItem.balance));
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

    public interface OnTransferClickListener {
        void onClick(View v);
    }

    public interface OnReceiveClickListener {
        void onClick(View v);
    }
}
