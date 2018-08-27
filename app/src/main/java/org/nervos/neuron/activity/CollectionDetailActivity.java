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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.nervos.neuron.R;
import org.nervos.neuron.item.CollectionItem;

/**
 * Created by BaojunCZ on 2018/8/21.
 */
public class CollectionDetailActivity extends NBaseActivity implements View.OnClickListener {

    private TextView nameText, tokenIdText, contractNameText, describeText, moreText;
    private SimpleDraweeView collectionImage;
    private Button checkBtn;
    private RelativeLayout imageRl;
    private CollectionItem collectionItem;
    private RecyclerView attrsRecycler;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_collection_detail;
    }

    @Override
    protected void initView() {
        nameText = findViewById(R.id.tv_name);
        tokenIdText = findViewById(R.id.tv_token_id);
        contractNameText = findViewById(R.id.tv_contract_name);
        describeText = findViewById(R.id.tv_collection_desc);
        moreText = findViewById(R.id.tv_desc_more);
        collectionImage = findViewById(R.id.iv);
        checkBtn = findViewById(R.id.btn);
        imageRl = findViewById(R.id.rl_image);
        attrsRecycler = findViewById(R.id.rv_attrs);
    }

    @Override
    protected void initData() {
        collectionItem = (CollectionItem) getIntent().getSerializableExtra("collection");
        nameText.setText(collectionItem.name);
        tokenIdText.setText("ID:" + collectionItem.tokenId);
        contractNameText.setText(collectionItem.assetContract.name);
        describeText.setText(collectionItem.description);
        if (!TextUtils.isEmpty(collectionItem.backgroundColor))
            imageRl.setBackgroundColor(Color.parseColor("#" + collectionItem.backgroundColor));
        collectionImage.setImageURI(collectionItem.imagePreviewUrl);
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
