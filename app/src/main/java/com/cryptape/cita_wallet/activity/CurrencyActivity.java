package com.cryptape.cita_wallet.activity;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.event.CurrencyRefreshEvent;
import com.cryptape.cita_wallet.item.Currency;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.util.CurrencyUtil;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;
import com.cryptape.cita_wallet.view.SettingButtonView;
import com.cryptape.cita_wallet.view.TitleBar;

import java.util.List;

/**
 * Created by BaojunCZ on 2018/7/31.
 */
public class CurrencyActivity extends NBaseActivity {

    private RecyclerView currencyRecycler;
    private List<Currency> currencyArray;
    private TitleBar title;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_currency;
    }

    @Override
    protected void initView() {
        currencyRecycler = findViewById(R.id.rv_currency);
        title = findViewById(R.id.title);
    }

    @Override
    protected void initData() {
        title.setLeftImage(R.drawable.ic_toptitle_back_white);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        currencyRecycler.setLayoutManager(linearLayoutManager);
        currencyArray = CurrencyUtil.getCurrencyList(this);
        Adapter adapter = new Adapter();
        currencyRecycler.setAdapter(adapter);
        title.setOnLeftClickListener(() -> finish());
    }

    @Override
    protected void initAction() {

    }

    class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Currency currency = currencyArray.get(position);
            if (SharePrefUtil.getString(ConstantUtil.CURRENCY, ConstantUtil.DEFAULT_CURRENCY).equals(currency.getName())) {
                holder.currency.setRightImageShow(true);
            } else {
                holder.currency.setRightImageShow(false);
            }
            holder.currency.setNameText(currency.getName());
            holder.currency.setOnClickListener(() -> {
                SharePrefUtil.putString(ConstantUtil.CURRENCY, currency.getName());
                notifyDataSetChanged();
                finish();
            });
        }

        @Override
        public int getItemCount() {
            return currencyArray.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ConstraintLayout root;
            SettingButtonView currency;

            public ViewHolder(View view) {
                super(view);
                currency = view.findViewById(R.id.currency);
                root = view.findViewById(R.id.root);
            }
        }
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        EventBus.getDefault().post(new CurrencyRefreshEvent());
        super.finish();
    }

}
