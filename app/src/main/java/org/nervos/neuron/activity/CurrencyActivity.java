package org.nervos.neuron.activity;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.util.db.SharePrefUtil;

/**
 * Created by 包俊 on 2018/7/31.
 */
public class CurrencyActivity extends NBaseActivity {

    private RecyclerView currencyRecycler;
    private SparseArray<String> currencyArray = new SparseArray<>();
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
        return getResources().getColor(R.color.white);
    }

    @Override
    protected void initData() {
        title.setLeftImage(R.drawable.ic_toptitle_back_white);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        currencyRecycler.setLayoutManager(linearLayoutManager);
        setArray();
        Adapter adapter = new Adapter();
        currencyRecycler.setAdapter(adapter);
        title.setOnLeftClickListener(() -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    protected void initAction() {

    }

    private void setArray() {
        currencyArray.put(0, "CNY");
        currencyArray.put(1, "USD");
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
            String currency = currencyArray.get(position);
            if (SharePrefUtil.getString("Currency", "CNY").equals(currency)) {
                holder.chosenImage.setVisibility(View.VISIBLE);
            } else {
                holder.chosenImage.setVisibility(View.GONE);
            }
            holder.currencyText.setText(currency);
            holder.root.setOnClickListener((view) -> {
                SharePrefUtil.putString("Currency", currency);
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

}
