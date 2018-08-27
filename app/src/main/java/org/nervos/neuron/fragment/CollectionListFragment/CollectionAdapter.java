package org.nervos.neuron.fragment.CollectionListFragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.nervos.neuron.R;
import org.nervos.neuron.item.CollectionItem;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_ITEM = 1;
    public static final int VIEW_TYPE_EMPTY = 0;

    public OnItemClickListener listener;
    private Context context;
    private List<CollectionItem> collectionItemList;

    public CollectionAdapter(Context context, List<CollectionItem> collectionItemList) {
        this.context = context;
        this.collectionItemList = collectionItemList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void refresh(List<CollectionItem> collectionItemList) {
        this.collectionItemList = collectionItemList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_view, parent, false);
            ((TextView) view.findViewById(R.id.empty_text)).setText(R.string.empty_no_collection_data);
            return new RecyclerView.ViewHolder(view) {
            };
        }
        CollectionViewHolder holder = new CollectionViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_collection, parent,
                        false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CollectionViewHolder) {
            CollectionViewHolder holder = (CollectionViewHolder) viewHolder;
            CollectionItem collectionItem = collectionItemList.get(position);
            holder.collectionName.setText(collectionItem.name);
            holder.collectionContractName.setText(collectionItem.assetContract.name);
            Glide.with(context)
                    .load(collectionItem.assetContract.imageUrl)
                    .into(holder.collectionImage);
            holder.collectionId.setText(String.format(
                    context.getString(R.string.collection_id_place_holder), collectionItem.tokenId));
            holder.root.setOnClickListener((view) -> {
                if (listener != null)
                    listener.onItemClick(view, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        if (collectionItemList.size() == 0) {
            return 1;
        }
        return collectionItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (collectionItemList == null || collectionItemList.size() == 0) {
            return VIEW_TYPE_EMPTY;
        }
        return VIEW_TYPE_ITEM;
    }

    class CollectionViewHolder extends RecyclerView.ViewHolder {
        ImageView collectionImage;
        TextView collectionName;
        TextView collectionContractName;
        TextView collectionId;
        RelativeLayout root;

        public CollectionViewHolder(View view) {
            super(view);
            collectionImage = view.findViewById(R.id.collection_image);
            collectionName = view.findViewById(R.id.collection_name);
            collectionContractName = view.findViewById(R.id.collection_contract_name);
            collectionId = view.findViewById(R.id.collection_id);
            root = view.findViewById(R.id.root);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
