package org.nervos.neuron.activity;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nervos.neuron.R;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.SettingButtonView;

import java.util.List;

/**
 * Created by 包俊 on 2018/7/31.
 */
public class CurrencyActivity extends NBaseActivity {

    private RecyclerView currencyRecycler;
    private List<CurrencyItem> currencyArray;
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
            CurrencyItem currency = currencyArray.get(position);
            if (SharePrefUtil.getString(ConstantUtil.CURRENCY, ConstantUtil.DEFAULT_CURRENCY).equals(currency.getName())) {
                holder.currency.setRightImageShow(true);
            } else {
                holder.currency.setRightImageShow(false);
            }
            holder.currency.setNameText(currency.getName());
            holder.currency.setOpenListener(() -> {
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
        super.finish();
    }

}
