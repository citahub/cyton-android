package org.nervos.neuron.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.item.CollectionItem;

/**
 * Created by BaojunCZ on 2018/8/21.
 */
public class CollectionDetailActivity extends NBaseActivity implements View.OnClickListener {

    private TextView nameText, tokenIdText, contractNameText, describeText, moreText, collectionDescTitleText, descMoreText, collectionDescText;
    private ImageView collectionImage;
    private Button checkBtn;
    private RelativeLayout imageRl;
    private CollectionItem collectionItem;
    private RecyclerView attrsRecycler;
    private View line;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_collection_detail;
    }

    @Override
    protected void initView() {
        TitleBar titleBar = findViewById(R.id.title);
        titleBar.setFocusable(true);
        titleBar.setFocusableInTouchMode(true);
        titleBar.requestFocus();
        nameText = findViewById(R.id.tv_name);
        tokenIdText = findViewById(R.id.tv_token_id);
        contractNameText = findViewById(R.id.tv_contract_name);
        describeText = findViewById(R.id.tv_collection_desc);
        moreText = findViewById(R.id.tv_desc_more);
        collectionImage = findViewById(R.id.iv);
        checkBtn = findViewById(R.id.btn);
        imageRl = findViewById(R.id.rl_image);
        attrsRecycler = findViewById(R.id.rv_attrs);
        collectionDescTitleText = findViewById(R.id.tv_collection_desc_title);
        descMoreText = findViewById(R.id.tv_desc_more);
        collectionDescText = findViewById(R.id.tv_collection_desc);
        line = findViewById(R.id.view_line2);
    }

    @Override
    protected void initData() {
        collectionItem = getIntent().getParcelableExtra("collection");
        nameText.setText(collectionItem.name);
        tokenIdText.setText("ID:" + collectionItem.tokenId);
        contractNameText.setText(collectionItem.assetContract.name);
        if (!TextUtils.isEmpty(collectionItem.description))
            describeText.setText(collectionItem.description);
        else {
            collectionDescTitleText.setVisibility(View.GONE);
            descMoreText.setVisibility(View.GONE);
            collectionDescText.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(collectionItem.backgroundColor))
            imageRl.setBackgroundColor(Color.parseColor("#" + collectionItem.backgroundColor));
        Glide.with(this)
                .load(collectionItem.imagePreviewUrl)
                .into(collectionImage);
        attrsRecycler.setLayoutManager(new GridLayoutManager(mActivity, 3));
        Adapter adapter = new Adapter();
        attrsRecycler.setAdapter(adapter);
    }

    @Override
    protected void initAction() {
        moreText.setOnClickListener(this);
        checkBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_desc_more:
                if (moreText.getText().toString().trim().equals(getString(R.string.more))) {
                    describeText.setEllipsize(null);
                    describeText.setMaxLines(100);
                    moreText.setText(getString(R.string.rollback));
                } else {
                    describeText.setEllipsize(TextUtils.TruncateAt.END);
                    describeText.setMaxLines(3);
                    moreText.setText(getString(R.string.more));
                }
                break;
            case R.id.btn:
                Intent intent = new Intent(mActivity, SimpleWebActivity.class);
                intent.putExtra(SimpleWebActivity.EXTRA_URL, collectionItem.externalLink);
                startActivity(intent);
                break;
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(CollectionDetailActivity.this).inflate(R.layout.item_collection_attr, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.key.setText(collectionItem.traits.get(position).traitType);
            holder.value.setText(collectionItem.traits.get(position).value);
        }

        @Override
        public int getItemCount() {
            return collectionItem.traits.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private TextView key, value;

        public ViewHolder(View itemView) {
            super(itemView);
            key = itemView.findViewById(R.id.key);
            value = itemView.findViewById(R.id.value);
        }
    }
}
