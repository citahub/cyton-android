package org.nervos.neuron.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.MainActivity;
import org.nervos.neuron.fragment.wallet.view.WalletsFragment;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.button.CommonButton;
import org.nervos.neuron.view.dialog.SelectorDialog;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportMnemonicFragment extends BaseFragment {

    List<String> formats;
    List<String> paths;
    int currentIndex;
    private TextView pathText, formatText;
    private SelectorDialog selectorDialog;
    private LinearLayout pathLL;
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private AppCompatEditText walletNameEdit;
    private AppCompatEditText passwordEdit;
    private AppCompatEditText rePasswordEdit;
    private AppCompatEditText mnemonicEdit;
    private CommonButton importButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_mnemonic, container, false);
        pathText = view.findViewById(R.id.tv_path);
        formatText = view.findViewById(R.id.tv_format);
        importButton = view.findViewById(R.id.import_mnemonic_button);
        walletNameEdit = view.findViewById(R.id.edit_wallet_name);
        passwordEdit = view.findViewById(R.id.edit_wallet_password);
        rePasswordEdit = view.findViewById(R.id.edit_wallet_repassword);
        mnemonicEdit = view.findViewById(R.id.edit_wallet_mnemonic);
        pathLL = view.findViewById(R.id.ll_path);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        checkWalletStatus();
        initListener();
    }

    private void initView() {
        formats = Arrays.asList(getResources().getStringArray(R.array.mnemonic_format));
        paths = Arrays.asList(getResources().getStringArray(R.array.mnemonic_path));
        currentIndex = 0;
        pathText.setText(paths.get(0));
        formatText.setText(formats.get(0));
    }

    private void initListener() {
        importButton.setOnClickListener(v -> {
            if (!NumberUtil.isPasswordOk(passwordEdit.getText().toString().trim())) {
                Toast.makeText(getContext(), R.string.password_weak, Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.equals(passwordEdit.getText().toString().trim(),
                    rePasswordEdit.getText().toString().trim())) {
                Toast.makeText(getContext(), R.string.password_not_same, Toast.LENGTH_SHORT).show();
            } else if (DBWalletUtil.checkWalletName(getContext(), walletNameEdit.getText().toString().trim())) {
                Toast.makeText(getContext(), R.string.wallet_name_exist, Toast.LENGTH_SHORT).show();
            } else {
                cachedThreadPool.execute(() -> generateAndSaveWallet());
            }
        });

        pathLL.setOnClickListener(view -> {
            showSelectDialog();
        });

    }

    private void showSelectDialog() {
        selectorDialog = new SelectorDialog(getActivity());
        selectorDialog.setTitleText(getString(R.string.path));
        selectorDialog.setRecyclerView(new RecyclerAdapter());
        selectorDialog.setOkListner(view1 -> {
            pathText.setText(paths.get(currentIndex));
            formatText.setText(formats.get(currentIndex));
            selectorDialog.dismiss();
        });
    }

    private void generateAndSaveWallet() {
        passwordEdit.post(() -> showProgressBar(R.string.wallet_importing));
        WalletEntity walletEntity;
        try {
            walletEntity = WalletEntity.fromMnemonic(
                    mnemonicEdit.getText().toString().trim(), paths.get(currentIndex));
        } catch (Exception e) {
            e.printStackTrace();
            passwordEdit.post(() -> {
                dismissProgressBar();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return;
        }
        if (walletEntity == null || DBWalletUtil.checkWalletAddress(getContext(), walletEntity.getAddress())) {
            passwordEdit.post(() -> {
                dismissProgressBar();
                Toast.makeText(getContext(), R.string.wallet_name_exist, Toast.LENGTH_SHORT).show();
            });
            return;
        }
        WalletItem walletItem = WalletItem.fromWalletEntity(
                passwordEdit.getText().toString().trim(), walletEntity);
        walletItem.name = walletNameEdit.getText().toString().trim();
        walletItem = DBWalletUtil.addOriginTokenToWallet(getContext(), walletItem);
        DBWalletUtil.saveWallet(getContext(), walletItem);
        SharePrefUtil.putCurrentWalletName(walletItem.name);
        passwordEdit.post(() -> {
            Toast.makeText(getContext(), R.string.wallet_export_success, Toast.LENGTH_SHORT).show();
            dismissProgressBar();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_TAG, WalletsFragment.TAG);
            getActivity().startActivity(intent);
            getActivity().finish();
        });
    }

    private boolean isWalletValid() {
        return check1 && check2 && check3 && check4;
    }

    private boolean check1 = false, check2 = false, check3 = false, check4 = false;

    private void checkWalletStatus() {
        walletNameEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check1 = !TextUtils.isEmpty(walletNameEdit.getText().toString().trim());
                importButton.setClickAble(isWalletValid());
            }
        });
        passwordEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check2 = !TextUtils.isEmpty(passwordEdit.getText().toString().trim())
                        && passwordEdit.getText().toString().trim().length() >= 8;
                importButton.setClickAble(isWalletValid());
            }
        });
        rePasswordEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check3 = !TextUtils.isEmpty(rePasswordEdit.getText().toString().trim())
                        && rePasswordEdit.getText().toString().trim().length() >= 8;
                importButton.setClickAble(isWalletValid());
            }
        });
        mnemonicEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check4 = !TextUtils.isEmpty(mnemonicEdit.getText().toString().trim());
                importButton.setClickAble(isWalletValid());
            }
        });
    }


    private static class WalletTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    class RecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_path, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position == 0) {
                holder.tv.setText(paths.get(position) + "\n" + formats.get(position));
            } else {
                holder.tv.setText(paths.get(position) + " " + formats.get(position));
            }
            if (currentIndex == position)
                holder.iv.setVisibility(View.VISIBLE);
            else
                holder.iv.setVisibility(View.GONE);
            holder.root.setOnClickListener(view -> {
                currentIndex = position;
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return formats.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tv;
        private ConstraintLayout root;
        private ImageView iv;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
            root = itemView.findViewById(R.id.root);
            iv = itemView.findViewById(R.id.iv);
        }
    }
}
