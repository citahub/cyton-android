package org.nervos.neuron.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.fragment.WalletFragment;
import org.nervos.neuron.item.TransactionItem;
import com.facebook.drawee.view.SimpleDraweeView;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionListActivity extends AppCompatActivity {

    private SimpleDraweeView tokenImageView;
    private TextView tokenNameText;
    private TextView tokenAmountText;
    private RecyclerView transactionRecycler;
    private TransactionAdapter transactionAdapter;

    private String walletAddress;
    private String tokenName;
    private String tokenImage;
    private String tokenAmount;

    private List<TransactionItem> transactionItemList= new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        initData();
        initView();
        initRecycler();
        initListener();
    }

    private void initView() {
        tokenImageView = findViewById(R.id.transaction_token_image);
        tokenNameText = findViewById(R.id.transaction_token_name);
        tokenAmountText = findViewById(R.id.transaction_token_amount);
        transactionRecycler = findViewById(R.id.transaction_list);

        tokenName = getIntent().getStringExtra(WalletFragment.EXTRA_TOKEN_NAME);
        tokenImage = getIntent().getStringExtra(WalletFragment.EXTRA_TOKEN_IMAGE);
        tokenAmount = getIntent().getStringExtra(WalletFragment.EXTRA_TOKEN_AMOUNT);
        walletAddress = getIntent().getStringExtra(WalletFragment.EXTRA_WALLET_ADDRESS);

        tokenNameText.setText(tokenName);
        tokenAmountText.setText(tokenAmount);
        tokenImageView.setImageURI(tokenImage);
    }

    private void initData() {

        transactionItemList.add(new TransactionItem("0x987654321def", "2018.02.12", "450.5"));
        transactionItemList.add(new TransactionItem("0x123456789abc", "2018.01.12", "10.5"));
        transactionItemList.add(new TransactionItem("0x123456789def", "2018.03.12", "1.9"));
        transactionItemList.add(new TransactionItem("0x987654321abc", "2018.03.25", "35.1"));

    }

    private void initRecycler() {
        transactionRecycler.setLayoutManager(new LinearLayoutManager(this));
        transactionRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        transactionAdapter = new TransactionAdapter();
        transactionRecycler.setAdapter(transactionAdapter);
    }

    private void initListener() {
        findViewById(R.id.button_transaction_receive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TransactionListActivity.this);
                View view = getLayoutInflater().inflate(R.layout.dialog_wallet_info, null);
                TextView walletNameText = view.findViewById(R.id.wallet_name);
                TextView walletAddressText = view.findViewById(R.id.wallet_address);
                walletNameText.setText("钱包A");
                walletAddressText.setText(walletAddress);
                final AlertDialog dialog = builder.setView(view)
                        .create();
                view.findViewById(R.id.button_copy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied text", walletAddress);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(TransactionListActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                    }
                });
                view.findViewById(R.id.button_ok).setOnClickListener(v1 -> dialog.dismiss());
                ((ImageView)view.findViewById(R.id.qrcode_address)).setImageBitmap(CodeUtils.createImage(walletAddress, 400, 400, null));
                dialog.show();
            }
        });

        findViewById(R.id.button_transaction_transfer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionListActivity.this, TransferActivity.class);
                intent.putExtra(WalletFragment.EXTRA_TOKEN_IMAGE, tokenImage);
                intent.putExtra(WalletFragment.EXTRA_TOKEN_NAME, tokenName);
                intent.putExtra(WalletFragment.EXTRA_TOKEN_AMOUNT, tokenAmount);
                intent.putExtra(WalletFragment.EXTRA_WALLET_ADDRESS, walletAddress);
                startActivity(intent);
            }
        });
    }


    class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

        public OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            TransactionViewHolder holder = new TransactionViewHolder(LayoutInflater.from(
                    TransactionListActivity.this).inflate(R.layout.item_transaction_list, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            holder.transactionImageView.setImageResource(R.drawable.input);
            holder.transactionIdText.setText(transactionItemList.get(position).id);
            holder.transactionTimeText.setText(transactionItemList.get(position).time);
            holder.transactionAmountText.setText(transactionItemList.get(position).amount);
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return transactionItemList.size();
        }

        class  TransactionViewHolder extends RecyclerView.ViewHolder {
            ImageView transactionImageView;
            TextView transactionIdText;
            TextView transactionTimeText;
            TextView transactionAmountText;

            public TransactionViewHolder (View view) {
                super(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(v, (int)v.getTag());
                        }
                    }
                });
                transactionImageView = view.findViewById(R.id.transaction_inout_image);
                transactionIdText = view.findViewById(R.id.transaction_id_text);
                transactionTimeText = view.findViewById(R.id.transaction_time_text);
                transactionAmountText = view.findViewById(R.id.transaction_amount_text);
            }
        }
    }

    private interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

}
