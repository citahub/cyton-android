package org.nervos.neuron.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AddWalletActivity;
import org.nervos.neuron.activity.CreateWalletActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.TokenManageActivity;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.dialog.DialogUtil;
import org.nervos.neuron.service.WalletConfig;
import org.nervos.neuron.dialog.TokenTransferDialog;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;

import com.facebook.drawee.view.SimpleDraweeView;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;

public class WalletFragment extends Fragment {

    public static final String TAG = WalletFragment.class.getName();
    public static final String EXTRA_WALLET_ITEM = "extra_wallet_item";
    public static final String EXTRA_WALLET_ADDRESS = "EXTRA_WALLET_ADDRESS";
    public static final String EXTRA_WALLET_NAME = "EXTRA_WALLET_NAME";
    public static final String EXTRA_TOKEN_NAME = "EXTRA_TOKEN_NAME";
    public static final String EXTRA_TOKEN_IMAGE = "EXTRA_TOKEN_IMAGE";
    public static final String EXTRA_TOKEN_AMOUNT = "EXTRA_TOKEN_AMOUNT";

    private TextView walletText;
    private TextView addressText;
    private RecyclerView tokenRecycler;
    private TokenAdapter tokenAdapter;

    private FrameLayout receiveLayout;
    private FrameLayout tokenManageLayout;
    private TitleBar titleBar;

    private String walletAddress = WalletConfig.ADDRESS;

    private List<TokenItem> tokenItemList = new ArrayList<>();
    private List<WalletItem> walletList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        walletText = view.findViewById(R.id.wallet_name);
        addressText = view.findViewById(R.id.wallet_address);
        tokenRecycler = view.findViewById(R.id.token_list);
        receiveLayout = view.findViewById(R.id.wallet_receive_layout);
        tokenManageLayout = view.findViewById(R.id.wallet_token_management_layout);
        titleBar = view.findViewById(R.id.title);
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

        walletList.add(new WalletItem("钱包A", WalletConfig.ADDRESS, R.drawable.wallet_photo));
        walletList.add(new WalletItem("钱包B", WalletConfig.ADDRESS, R.drawable.wallet_photo));
        walletList.add(new WalletItem("钱包C", WalletConfig.ADDRESS, R.drawable.wallet_photo));
        walletList.add(new WalletItem("钱包D", WalletConfig.ADDRESS, R.drawable.wallet_photo));

        tokenItemList.add(new TokenItem("ETH", R.drawable.ethereum, 100.0f));
        tokenItemList.add(new TokenItem("ETH", R.drawable.ethereum, 200.0f));
        tokenItemList.add(new TokenItem("ETH", R.drawable.ethereum, 150.0f));
        tokenItemList.add(new TokenItem("ETH", R.drawable.ethereum, 123.45f));
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

        tokenAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TokenTransferDialog dialog = new TokenTransferDialog(getContext(), tokenItemList.get(position));
                dialog.setOnReceiveClickListener(new TokenTransferDialog.OnReceiveClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ReceiveQrCodeActivity.class);
                        intent.putExtra(EXTRA_WALLET_ITEM, walletList.get(0));
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
                dialog.setOnTransferClickListener(new TokenTransferDialog.OnTransferClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        receiveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReceiveQrCodeActivity.class);
                intent.putExtra(EXTRA_WALLET_ITEM, walletList.get(0));
                startActivity(intent);
            }
        });

        tokenManageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TokenManageActivity.class);
                startActivity(intent);
            }
        });

        titleBar.setOnRightClickListener(new TitleBar.OnRightClickListener() {
            @Override
            public void onRightClick() {
                startActivity(new Intent(getActivity(), AddWalletActivity.class));
            }
        });

        titleBar.setOnLeftClickListener(new TitleBar.OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                List<String> wallets = new ArrayList<>();
                for (WalletItem walletItem : walletList) {
                    wallets.add(walletItem.name);
                }
                DialogUtil.showListDialog(getContext(), "切换当前钱包", wallets, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void onItemClick(int which) {
                        Toast.makeText(getContext(), wallets.get(which), Toast.LENGTH_SHORT).show();
                    }
                });
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
            holder.tokenImage.setImageResource(R.drawable.ethereum);
            holder.tokenName.setText(tokenItemList.get(position).symbol);
            holder.tokenAmount.setText("" + tokenItemList.get(position).balance);
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
