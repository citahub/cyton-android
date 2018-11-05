package org.nervos.neuron.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AboutUsActivity;
import org.nervos.neuron.activity.CurrencyActivity;
import org.nervos.neuron.activity.SimpleWebActivity;
import org.nervos.neuron.service.http.EthRpcService;
import org.nervos.neuron.service.http.HttpUrls;
import org.nervos.neuron.view.SettingButtonView;
import org.nervos.neuron.view.dialog.AuthFingerDialog;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.fingerprint.AuthenticateResultCallback;
import org.nervos.neuron.util.fingerprint.FingerPrintController;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.dialog.SelectorDialog;
import org.nervos.neuron.view.dialog.ToastSingleButtonDialog;

public class SettingsFragment extends NBaseFragment {

    private static final int Currency_Code = 10001;
    public static final String TAG = SettingsFragment.class.getName();
    private SettingButtonView mSbvCurrency, mSbvAboutUs, mSbvContactUs, mSbvFingerPrint, mSbvForums, mSbvSelectEth;
    private AuthFingerDialog mAuthFingerDialog = null;
    private SelectorDialog mEthNodeDialog = null;
    private SparseArray<String> ethNodeList = new SparseArray<>();
    private int ethNodeIndex;
    private EthNodeAdapter mEthNodeAdapter;
    private FingerPrintController mFingerPrintController;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_settings;
    }

    @Override
    public void initView() {
        mSbvCurrency = (SettingButtonView) findViewById(R.id.sbv_local_coin);
        mSbvAboutUs = (SettingButtonView) findViewById(R.id.sbv_about_us);
        mSbvContactUs = (SettingButtonView) findViewById(R.id.sbv_contact_us);
        mSbvFingerPrint = (SettingButtonView) findViewById(R.id.sbv_fingerprint);
        mSbvForums = (SettingButtonView) findViewById(R.id.sbv_forums);
        mSbvSelectEth = (SettingButtonView) findViewById(R.id.sbv_eth_select);
    }

    @Override
    public void initData() {
        initEthNode();
        mFingerPrintController = new FingerPrintController(getActivity());
        mSbvCurrency.setRightText(SharePrefUtil.getString(ConstUtil.CURRENCY, ConstUtil.DEFAULT_CURRENCY));
        if (mFingerPrintController.isSupportFingerprint()) {
            mSbvFingerPrint.setVisibility(View.VISIBLE);
            if (SharePrefUtil.getBoolean(ConstUtil.FINGERPRINT, false)) {
                mSbvFingerPrint.setSwitch(true);
            } else {
                SharePrefUtil.putBoolean(ConstUtil.FINGERPRINT, false);
                mSbvFingerPrint.setSwitch(false);
            }
        } else {
            mSbvFingerPrint.setVisibility(View.GONE);
        }
    }

    @Override
    public void initAction() {
        mSbvAboutUs.setOpenListener(() -> {
            Intent intent = new Intent(getActivity(), CurrencyActivity.class);
            startActivityForResult(intent, Currency_Code);
        });
        mSbvFingerPrint.setSwitchListener((chosen) -> {
            if (chosen) {
                //setting fingerprint
                if (mFingerPrintController.hasEnrolledFingerprints() && mFingerPrintController.getEnrolledFingerprints().size() > 0) {
                    if (mAuthFingerDialog == null) mAuthFingerDialog = new AuthFingerDialog(getActivity());
                    mAuthFingerDialog.setOnShowListener((dialogInterface) -> {
                        mFingerPrintController.authenticate(authenticateResultCallback);
                    });
                    mAuthFingerDialog.setOnDismissListener((dialog) -> {
                        mFingerPrintController.cancelAuth();
                    });
                    mAuthFingerDialog.show();
                } else {
                    ToastSingleButtonDialog dialog = ToastSingleButtonDialog.getInstance(getActivity(), getResources().getString(R.string
                            .dialog_finger_setting));
                    dialog.setOnCancelClickListener(view -> {
                        FingerPrintController.openFingerPrintSettingPage(getActivity());
                        view.dismiss();
                    });
                }
            } else {
                //close fingerprint
                SharePrefUtil.putBoolean(ConstUtil.FINGERPRINT, false);
                mSbvFingerPrint.setSwitch(false);
            }

        });
        mSbvAboutUs.setOpenListener(() -> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        });
        mSbvContactUs.setOpenListener(() -> {
            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("contact", "Nervos-Neuron");
            if (cm != null) {
                cm.setPrimaryClip(mClipData);
                Toast.makeText(getActivity(), R.string.copy_weixin_success, Toast.LENGTH_SHORT).show();
            }
        });
        mSbvForums.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(getActivity(), HttpUrls.NERVOS_FORUMS);
        });
        mSbvSelectEth.setOpenListener(() -> {
            mEthNodeDialog = new SelectorDialog(getActivity());
            mEthNodeDialog.setTitleText(getString(R.string.setting_select_eth_net));
            mEthNodeDialog.setRecyclerView(mEthNodeAdapter);
            mEthNodeDialog.setOkListener((view -> {
                ethNodeIndex = mEthNodeAdapter.mIndex;
                SharePrefUtil.putString(ConstUtil.ETH_NET, ethNodeList.get(ethNodeIndex));
                mEthNodeDialog.dismiss();
            }));
            mEthNodeDialog.setOnDissmissListener(dialogInterface -> {
                EthRpcService.initHttp();
                initEthNode();
                String node = SharePrefUtil.getString(ConstUtil.ETH_NET, ConstUtil.ETH_MAINNET);
                node = node.replace("_", " ");
                mSbvSelectEth.setRightText(node);
                mEthNodeDialog.dismiss();
            });
        });
    }

    private void initEthNode() {
        ethNodeList.put(0, ConstUtil.ETH_MAINNET);
        ethNodeList.put(1, ConstUtil.ETH_NET_ROPSTEN_TEST);
        ethNodeList.put(2, ConstUtil.ETH_NET_KOVAN_TEST);
        ethNodeList.put(3, ConstUtil.ETH_NET_RINKEBY_TEST);
        switch (SharePrefUtil.getString(ConstUtil.ETH_NET, ConstUtil.ETH_NET_MAIN)) {
            case ConstUtil.ETH_NET_MAIN:
            default:
                ethNodeIndex = 0;
                break;
            case ConstUtil.ETH_NET_ROPSTEN_TEST:
                ethNodeIndex = 1;
                break;
            case ConstUtil.ETH_NET_KOVAN_TEST:
                ethNodeIndex = 2;
                break;
            case ConstUtil.ETH_NET_RINKEBY_TEST:
                ethNodeIndex = 3;
                break;
        }
        mEthNodeAdapter = new EthNodeAdapter(ethNodeIndex);
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    AuthenticateResultCallback authenticateResultCallback = new AuthenticateResultCallback() {
        @Override
        public void onAuthenticationError(String errorMsg) {
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded() {
            mSbvFingerPrint.setSwitch(true);
            if (mAuthFingerDialog != null && mAuthFingerDialog.isShowing()) mAuthFingerDialog.dismiss();
            SharePrefUtil.putBoolean(ConstUtil.FINGERPRINT, true);
            Toast.makeText(getContext(), getResources().getString(R.string.fingerprint_setting_sucess), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(getContext(), getResources().getString(R.string.fingerprint_setting_failed), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Currency_Code:
                    mSbvCurrency.setRightText(SharePrefUtil.getString(ConstUtil.CURRENCY, "CNY"));
                    break;
            }
        }
    }

    class EthNodeAdapter extends RecyclerView.Adapter<ViewHolder> {

        public int mIndex;

        public EthNodeAdapter(int index) {
            mIndex = index;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (mIndex == position) holder.mSbvEthNode.setRightImageShow(true);
            else holder.mSbvEthNode.setRightImageShow(false);

            holder.mSbvEthNode.setNameText(ethNodeList.get(position).replace("_", " "));
            holder.mSbvEthNode.setOpenListener(() -> {
                mIndex = holder.getAdapterPosition();
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return ethNodeList.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SettingButtonView mSbvEthNode;

        public ViewHolder(View view) {
            super(view);
            mSbvEthNode = view.findViewById(R.id.currency);
        }
    }
}
