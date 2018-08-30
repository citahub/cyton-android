package org.nervos.neuron.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.nervos.neuron.R;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/8/3.
 */
public class ChangeWalletActivity extends NBaseActivity {

    private ImageView pullImage;
    private RecyclerView recyclerView;
    private List<WalletItem> walletItems = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_change_wallet;
    }

    @Override
    protected void initView() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        pullImage = findViewById(R.id.iv_pull);
        recyclerView = findViewById(R.id.wallet_recycler);
    }

    @Override
    protected void initData() {
        walletItems = DBWalletUtil.getAllWallet(this);
        for (int i = 0; i < walletItems.size(); i++) {
            if (walletItems.get(i).name.equals(SharePrefUtil.getCurrentWalletName())) {
                if (i != 0) {
                    Collections.swap(walletItems, 0, i);
                }
                break;
            }
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        recyclerView.addItemDecoration(new SpaceItemDecoration(60));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initAction() {
        pullImage.setOnClickListener((view) -> {
            finish();
        });
    }

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.transparent_color2);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.wallet_activity_out);
    }

    class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private int Wallet = 0;
        private int Add = 1;

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == Wallet) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
                return new WalletHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_add, parent, false);
                return new AddHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof WalletHolder) {
                WalletHolder walletHolder = (WalletHolder) holder;
                WalletItem walletItem = walletItems.get(position);
                walletHolder.nameText.setText(walletItem.name);
                walletHolder.addressText.setText(walletItem.address);
                walletHolder.photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
                if (position == 0)
                    walletHolder.defaultImage.setVisibility(View.VISIBLE);
                else
                    walletHolder.defaultImage.setVisibility(View.GONE);
                ((WalletHolder) holder).root.setOnClickListener((view) -> {
                    SharePrefUtil.putCurrentWalletName(walletItem.name);
                    EventBus.getDefault().post(new TokenRefreshEvent());
                    finish();
                    overridePendingTransition(0, R.anim.wallet_activity_out);
                });
            } else if (holder instanceof AddHolder) {
                ((AddHolder) holder).root.setOnClickListener((view) -> {
                    startActivity(new Intent(ChangeWalletActivity.this, AddWalletActivity.class));
                });
            }
        }

        @Override
        public int getItemCount() {
            return walletItems.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == walletItems.size()) {
                return Add;
            } else {
                return Wallet;
            }
        }

        class WalletHolder extends RecyclerView.ViewHolder {
            ImageView photoImage, defaultImage;
            TextView nameText, addressText;
            ConstraintLayout root;

            public WalletHolder(View itemView) {
                super(itemView);
                photoImage = itemView.findViewById(R.id.iv_photo);
                defaultImage = itemView.findViewById(R.id.iv_default);
                nameText = itemView.findViewById(R.id.tv_name);
                addressText = itemView.findViewById(R.id.tv_address);
                root = itemView.findViewById(R.id.root);
            }
        }

        class AddHolder extends RecyclerView.ViewHolder {
            ConstraintLayout root;

            public AddHolder(View itemView) {
                super(itemView);
                root = itemView.findViewById(R.id.root);
            }
        }
    }

    class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            if (parent.getChildPosition(view) != 0)
                outRect.top = space;
        }
    }

}
