package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.item.WalletItem;

import java.util.ArrayList;
import java.util.List;

public class WalletManageActivity extends AppCompatActivity {

    private RecyclerView walletRecycler;
    private WalletAdapter walletAdapter;
    private AppCompatButton createWalletButton;
    private AppCompatButton importWalletButton;

    private List<WalletItem> walletItemList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_manage);

        initView();
        initData();
        initRecycler();
        initListener();
    }

    private void initView() {
        walletRecycler = findViewById(R.id.wallet_list);
        createWalletButton = findViewById(R.id.button_create_wallet);
        importWalletButton = findViewById(R.id.button_import_wallet);
    }

    private void initData() {
    }

    private void initRecycler() {
        walletRecycler.setLayoutManager(new LinearLayoutManager(this));
        walletRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        walletAdapter = new WalletAdapter();
        walletRecycler.setAdapter(walletAdapter);
    }

    private void initListener() {
        createWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalletManageActivity.this, CreateWalletActivity.class);
                startActivity(intent);
            }
        });

        importWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WalletManageActivity.this, ImportWalletActivity.class));
            }
        });
    }

    class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletViewHolder> {

        public OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public WalletAdapter.WalletViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            WalletAdapter.WalletViewHolder holder = new WalletAdapter.WalletViewHolder(LayoutInflater.from(
                    WalletManageActivity.this).inflate(R.layout.item_wallet_manage, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull WalletAdapter.WalletViewHolder holder, int position) {
            holder.walletNameText.setText(walletItemList.get(position).name);
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return walletItemList.size();
        }

        class  WalletViewHolder extends RecyclerView.ViewHolder {
            TextView walletNameText;
            TextView walletAddressText;
            TextView tokenNameAmountText;

            public WalletViewHolder (View view) {
                super(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(v, (int)v.getTag());
                        }
                    }
                });
                walletNameText = view.findViewById(R.id.wallet_name);
                walletAddressText = view.findViewById(R.id.wallet_address);
                tokenNameAmountText = view.findViewById(R.id.token_name_amount);
            }
        }
    }

    private interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

}
