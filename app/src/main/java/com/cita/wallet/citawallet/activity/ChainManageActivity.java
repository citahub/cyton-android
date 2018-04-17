package com.cita.wallet.citawallet.activity;

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

import com.cita.wallet.citawallet.R;
import com.cita.wallet.citawallet.custom.TitleBar;
import com.cita.wallet.citawallet.item.ChainItem;

import java.util.ArrayList;
import java.util.List;

public class ChainManageActivity extends AppCompatActivity {

    private RecyclerView chainRecycler;
    private ChainAdapter chainAdapter;
    private AppCompatButton createChainButton;
    private AppCompatButton importChainButton;
    private List<ChainItem> chainItemList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chain_manage);

        initView();
        initData();
        initRecycler();
        initListener();
    }

    private void initView() {
        chainRecycler = findViewById(R.id.chain_list);
        createChainButton = findViewById(R.id.button_create_chain);
        importChainButton = findViewById(R.id.button_import_chain);
    }

    private void initData() {
        chainItemList.add(new ChainItem("区块链A", "北京创意文化公司", "web3.abc.com"));
        chainItemList.add(new ChainItem("区块链B", "北京创意文化公司", "web3.abc.com"));
        chainItemList.add(new ChainItem("区块链C", "北京创意文化公司", "web3.abc.com"));
        chainItemList.add(new ChainItem("区块链D", "北京创意文化公司", "web3.abc.com"));

    }

    private void initRecycler() {
        chainRecycler.setLayoutManager(new LinearLayoutManager(this));
        chainRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        chainAdapter = new ChainAdapter();
        chainRecycler.setAdapter(chainAdapter);
    }

    private void initListener() {
        importChainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChainManageActivity.this, ImportChainActivity.class));
            }
        });

        ((TitleBar)findViewById(R.id.title)).setOnRightClickListener(new TitleBar.OnRightClickListener() {
            @Override
            public void onRightClick() {
                startActivity(new Intent(ChainManageActivity.this, ExportChainActivity.class));
            }
        });
    }

    class ChainAdapter extends RecyclerView.Adapter<ChainAdapter.ChainHolder> {

        public OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public ChainHolder onCreateViewHolder(ViewGroup parent, int viewType){
            ChainAdapter.ChainHolder holder = new ChainAdapter.ChainHolder(LayoutInflater.from(
                    ChainManageActivity.this).inflate(R.layout.item_chain_manage, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ChainAdapter.ChainHolder holder, int position) {
            holder.chainNameText.setText(chainItemList.get(position).name);
            holder.chainCompanyText.setText(chainItemList.get(position).company);
            holder.chainWebsiteText.setText(chainItemList.get(position).website);
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return chainItemList.size();
        }

        class  ChainHolder extends RecyclerView.ViewHolder {
            TextView chainNameText;
            TextView chainCompanyText;
            TextView chainWebsiteText;

            public ChainHolder (View view) {
                super(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(v, (int)v.getTag());
                        }
                    }
                });
                chainNameText = view.findViewById(R.id.chain_name);
                chainCompanyText = view.findViewById(R.id.chain_company);
                chainWebsiteText = view.findViewById(R.id.chain_website);
            }
        }
    }

    private interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

}
