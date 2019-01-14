package com.cryptape.cita_wallet.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;

import java.util.List;

public class DialogUtil {

    private static AlertDialog dialog;

    public static void showListDialog(Context context, @StringRes int title, List<String> list,
                                      String currentName, OnItemClickListener onItemClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View titleView = layoutInflater.inflate(R.layout.dialog_list_title_view, null);
        ((TextView)titleView.findViewById(R.id.dialog_title)).setText(title);
        titleView.findViewById(R.id.dialog_close_image).setOnClickListener(v -> {
            if (dialog != null) dialog.dismiss();
        });
        builder.setCustomTitle(titleView);
        String[] contents = new String[list.size()];
        list.toArray(contents);
        DialogAdapter adapter = new DialogAdapter(context, list, currentName);
        builder.setAdapter(adapter, (dialog, which) -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(which);
            }
        });
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        initDialogAttributes(context);
    }

    private static void initDialogAttributes(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int mScreenWidth = dm.widthPixels;
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams wmlp = dialogWindow.getAttributes();
        wmlp.gravity = Gravity.CENTER;
        wmlp.width = (int) (mScreenWidth * 0.75);
        dialogWindow.setAttributes(wmlp);
    }

    private static class DialogAdapter extends BaseAdapter {

        private List<String> list;
        private LayoutInflater layoutInflater;
        private String currentName;
        private Context context;

        public DialogAdapter(Context context, List<String> list, String currentName) {
            this.list = list;
            this.layoutInflater = LayoutInflater.from(context);
            this.currentName = currentName;
            this.context = context;
        }
        private class ViewHolder {
            TextView itemText;
        }
        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public String getItem(int position) {
            return list.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.item_dialog_list, null);
                holder.itemText = convertView.findViewById(R.id.dialog_item_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.itemText.setText(list.get(position));
            if (currentName.equals(list.get(position))) {
                holder.itemText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            } else {
                holder.itemText.setTextColor(ContextCompat.getColor(context, R.color.font_title));
            }
            return convertView;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int which);
    }

}
