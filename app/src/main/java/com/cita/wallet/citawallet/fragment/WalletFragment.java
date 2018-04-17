package com.cita.wallet.citawallet.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cita.wallet.citawallet.R;
import com.cita.wallet.citawallet.config.WalletConfig;
import com.cita.wallet.citawallet.activity.AddTokenActivity;
import com.cita.wallet.citawallet.activity.TransactionListActivity;
import com.cita.wallet.citawallet.item.TokenItem;
import com.facebook.drawee.view.SimpleDraweeView;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;

public class WalletFragment extends Fragment {

    public static final String TAG = WalletFragment.class.getName();
    public static final String EXTRA_WALLET_ADDRESS = "EXTRA_WALLET_ADDRESS";
    public static final String EXTRA_WALLET_NAME = "EXTRA_WALLET_NAME";
    public static final String EXTRA_TOKEN_NAME = "EXTRA_TOKEN_NAME";
    public static final String EXTRA_TOKEN_IMAGE = "EXTRA_TOKEN_IMAGE";
    public static final String EXTRA_TOKEN_AMOUNT = "EXTRA_TOKEN_AMOUNT";

    private AppCompatSpinner chainSpinner;
    private TextView walletText;
    private TextView addressText;
    private ImageView addWalletImage;
    private RecyclerView tokenRecycler;
    private TokenAdapter tokenAdapter;
    private FloatingActionButton addFab;

    private String walletAddress = WalletConfig.ADDRESS;

    private List<TokenItem> tokenItemList = new ArrayList<>();
    private List<CharSequence> walletList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        chainSpinner = view.findViewById(R.id.chain_spinner);
        walletText = view.findViewById(R.id.wallet_name);
        addressText = view.findViewById(R.id.wallet_address);
        tokenRecycler = view.findViewById(R.id.token_list);
        addWalletImage = view.findViewById(R.id.image_add_wallet);
        addFab = view.findViewById(R.id.fab_add);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        tokenRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        tokenRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        tokenAdapter = new TokenAdapter();
        tokenRecycler.setAdapter(tokenAdapter);
        initListener();

        addressText.setText(String.format("%s", walletAddress));
    }

    private void initData() {
        tokenItemList.add(new TokenItem("http://7xq40y.com1.z0.glb.clouddn.com/%E6%AF%94%E7%89%B9%E5%B8%81.png", "Bitcore", "100"));
        tokenItemList.add(new TokenItem("http://7xq40y.com1.z0.glb.clouddn.com/%E6%AF%94%E7%89%B9%E5%B8%81.png", "Ethereum", "100"));
        tokenItemList.add(new TokenItem("http://7xq40y.com1.z0.glb.clouddn.com/%E6%AF%94%E7%89%B9%E5%B8%81.png", "Bitcore", "100"));
        tokenItemList.add(new TokenItem("http://7xq40y.com1.z0.glb.clouddn.com/%E6%AF%94%E7%89%B9%E5%B8%81.png", "Ethereum", "100"));

        walletList.add("钱包A");
        walletList.add("钱包B");
        walletList.add("钱包C");
        walletList.add("钱包D");
    }

    private void initListener() {
        walletText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_wallet_info, null);
                TextView walletNameText = view.findViewById(R.id.wallet_name);
                TextView walletAddressText = view.findViewById(R.id.wallet_address);
                walletNameText.setText("钱包A");
                walletAddressText.setText(walletAddress);
                final AlertDialog dialog = builder.setView(view)
                        .create();
                view.findViewById(R.id.button_copy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied text", walletAddress);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getActivity(), "复制成功", Toast.LENGTH_SHORT).show();
                    }
                });
                view.findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                ((ImageView)view.findViewById(R.id.qrcode_address)).setImageBitmap(CodeUtils.createImage(walletAddress, 400, 400, null));
                dialog.show();
            }
        });

        addWalletImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                CharSequence[] wallets = new CharSequence[walletList.size()];
                wallets = walletList.toArray(wallets);
                final CharSequence[] walletArray = wallets;
                builder.setTitle(R.string.select_wallet)
                        .setItems(wallets, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getActivity(), "select " + walletArray[which], Toast.LENGTH_SHORT).show();
                            }
                        }).create().show();
            }
        });

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddTokenActivity.class);
                intent.putExtra(EXTRA_WALLET_ADDRESS, walletAddress);
                intent.putExtra(EXTRA_WALLET_NAME, walletList.get(0));
                startActivity(intent);
            }
        });

        tokenAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), TransactionListActivity.class);
                intent.putExtra(EXTRA_WALLET_ADDRESS, walletAddress);
                intent.putExtra(EXTRA_TOKEN_NAME, tokenItemList.get(position).name);
                intent.putExtra(EXTRA_TOKEN_IMAGE, tokenItemList.get(position).image);
                intent.putExtra(EXTRA_TOKEN_AMOUNT, tokenItemList.get(position).amount);
                startActivity(intent);
            }
        });

    }


    class TokenAdapter extends RecyclerView.Adapter<TokenAdapter.TokenViewHolder> {

        public OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public TokenViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            TokenViewHolder holder = new TokenViewHolder(LayoutInflater.from(
                    getActivity()).inflate(R.layout.item_token_list, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull TokenViewHolder holder, int position) {
            holder.tokenImage.setImageURI(tokenItemList.get(position).image);
            holder.tokenName.setText(tokenItemList.get(position).name);
            holder.tokenAmount.setText(tokenItemList.get(position).amount);
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return tokenItemList.size();
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
