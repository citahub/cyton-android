package org.nervos.neuron.custom;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.item.WalletItem;

import java.util.List;

/**
 * Created by BaojunCZ on 2018/8/7.
 */
public class SelectWalletPopupwWindow extends PopupWindow {

    private RecyclerView recyclerView;
    private List<WalletItem> walletItems;
    private onClickImpl listener;

    public SelectWalletPopupwWindow(Activity activity, List<WalletItem> walletItems, onClickImpl listener) {
        super(activity);
        this.walletItems = walletItems;
        this.listener = listener;
        View pop = LayoutInflater.from(activity).inflate(R.layout.popupwindow_pwd_unlock_wallet, null);
        recyclerView = pop.findViewById(R.id.recycler);
        this.setContentView(pop);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x000000);
        this.setBackgroundDrawable(dw);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(new Adapter());
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popupwindow_wallet_unlock, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.nameTv.setText(walletItems.get(position).name);
            holder.nameTv.setOnClickListener((view) -> {
                listener.click(walletItems.get(position));
            });
        }

        @Override
        public int getItemCount() {
            return walletItems.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTv;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.tv_name);
        }
    }

    public interface onClickImpl {
        void click(WalletItem walletItem);
    }

}
