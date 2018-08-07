package org.nervos.neuron.activity;

import android.content.Context;
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

import com.google.gson.Gson;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.CurrencyListItem;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.StreamUtils;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.util.ArrayList;
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
    protected int getStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            return getResources().getColor(R.color.white, null);
        } else {
            return super.getStatusBarColor();
        }
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
        title.setOnLeftClickListener(() -> {
            finish();
        });
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
            if (SharePrefUtil.getString(ConstUtil.Currency, "CNY").equals(currency.getName())) {
                holder.chosenImage.setVisibility(View.VISIBLE);
            } else {
                holder.chosenImage.setVisibility(View.GONE);
            }
            holder.currencyText.setText(currency.getName());
            holder.root.setOnClickListener((view) -> {
                SharePrefUtil.putString(ConstUtil.Currency, currency.getName());
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return currencyArray.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView currencyText;
            ImageView chosenImage;
            ConstraintLayout root;

            public ViewHolder(View view) {
                super(view);
                currencyText = view.findViewById(R.id.tv_currency);
                chosenImage = view.findViewById(R.id.iv_chosen);
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
