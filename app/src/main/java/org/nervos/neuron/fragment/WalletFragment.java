package org.nervos.neuron.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AddWalletActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.TokenManageActivity;
import org.nervos.neuron.activity.TransferActivity;
import org.nervos.neuron.activity.WalletManageActivity;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.dialog.DialogUtil;
import org.nervos.neuron.dialog.TokenTransferDialog;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

public class WalletFragment extends BaseFragment {

    public static final String TAG = WalletFragment.class.getName();
    public static final String EXTRA_WALLET_ADDRESS = "EXTRA_WALLET_ADDRESS";
    public static final String EXTRA_TOKEN_NAME = "EXTRA_TOKEN_NAME";
    public static final String EXTRA_TOKEN_IMAGE = "EXTRA_TOKEN_IMAGE";
    public static final String EXTRA_TOKEN_AMOUNT = "EXTRA_TOKEN_AMOUNT";

    private TextView walletNameText;
    private TextView addressText;
    private FrameLayout receiveLayout;
    private FrameLayout tokenManageLayout;
    private TitleBar titleBar;
    private ImageView settingImage;
    private RecyclerView tokenRecycler;
    private TokenAdapter tokenAdapter = new TokenAdapter();

    private List<TokenItem> tokenItemList = new ArrayList<>();
    private List<String> walletNameList = new ArrayList<>();
    private WalletItem walletItem;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        walletNameText = view.findViewById(R.id.wallet_name);
        addressText = view.findViewById(R.id.wallet_address);
        tokenRecycler = view.findViewById(R.id.token_list);
        receiveLayout = view.findViewById(R.id.wallet_receive_layout);
        tokenManageLayout = view.findViewById(R.id.wallet_token_management_layout);
        titleBar = view.findViewById(R.id.title);
        settingImage = view.findViewById(R.id.wallet_setting);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initWalletData();
        initAdapter();
        initListener();
        initTitleBarListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        initWalletData();
    }

    private void initWalletData() {
        if (!TextUtils.isEmpty(SharePrefUtil.getWalletName())) {
            walletNameList = DBWalletUtil.getAllWalletName(getContext());
            showProgressBar();
            walletItem = DBWalletUtil.getWallet(getContext(), SharePrefUtil.getWalletName());
            if (walletItem != null) {
                walletNameText.setText(walletItem.name);
                addressText.setText(walletItem.address);
            }
            WalletService.getWalletTokenBalance(getContext(), walletItem, walletItem ->
                    walletNameText.post(() -> {
                    dismissProgressBar();
                    if (walletItem != null && walletItem.tokenItems != null) {
                        tokenItemList = walletItem.tokenItems;
                        tokenAdapter.notifyDataSetChanged();
                    }
                })
            );
        }
    }

    private void initListener() {
        receiveLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), ReceiveQrCodeActivity.class)));
        tokenManageLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), TokenManageActivity.class)));
        settingImage.setOnClickListener(v -> startActivity(new Intent(getActivity(), WalletManageActivity.class)));
    }


    private void initAdapter() {
        tokenRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        tokenRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        tokenRecycler.setAdapter(tokenAdapter);

        tokenAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TokenTransferDialog dialog = new TokenTransferDialog(getContext(), tokenItemList.get(position));
                dialog.setOnReceiveClickListener(v -> {
                    startActivity(new Intent(getActivity(), ReceiveQrCodeActivity.class));
                    dialog.dismiss();
                });
                dialog.setOnTransferClickListener(v -> {
                    Intent intent = new Intent(getActivity(), TransferActivity.class);
                    intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItemList.get(position));
                    startActivity(intent);
                    dialog.dismiss();
                });
                dialog.show();
            }
        });
    }

    private void initTitleBarListener() {
        titleBar.setOnRightClickListener(() -> startActivity(new Intent(getActivity(), AddWalletActivity.class)));

        titleBar.setOnLeftClickListener(() -> DialogUtil.showListDialog(getContext(), "切换当前钱包", walletNameList, which -> {
            SharePrefUtil.putWalletName(walletNameList.get(which));
            initWalletData();
        }));
    }


    class TokenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int VIEW_TYPE_ITEM = 1;
        public static final int VIEW_TYPE_EMPTY = 0;

        public OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            if (viewType == VIEW_TYPE_EMPTY) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_empty_view, parent, false);
                return new RecyclerView.ViewHolder(view){};
            }
            TokenViewHolder holder = new TokenViewHolder(LayoutInflater.from(
                    getActivity()).inflate(R.layout.item_token_list, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TokenViewHolder) {
                TokenViewHolder viewHolder = (TokenViewHolder)holder;
                viewHolder.tokenImage.setImageResource(R.drawable.ethereum);
                viewHolder.tokenName.setText(tokenItemList.get(position).symbol);
                viewHolder.tokenAmount.setText(String.valueOf(tokenItemList.get(position).balance));
                viewHolder.itemView.setTag(position);
            }
        }

        @Override
        public int getItemCount() {
            if (tokenItemList.size() == 0) {
                return 1;
            }
            return tokenItemList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (tokenItemList.size() == 0) {
                return VIEW_TYPE_EMPTY;
            }
            return VIEW_TYPE_ITEM;
        }

        class  TokenViewHolder extends RecyclerView.ViewHolder {
            SimpleDraweeView tokenImage;
            TextView tokenName;
            TextView tokenAmount;

            public TokenViewHolder (View view) {
                super(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(v, (int)v.getTag());
                        }
                    }
                });
                tokenImage = view.findViewById(R.id.token_image);
                tokenName = view.findViewById(R.id.token_name);
                tokenAmount = view.findViewById(R.id.token_amount);
            }
        }
    }

    private interface OnItemClickListener{
        void onItemClick(View view, int position);
    }


}
