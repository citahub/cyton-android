package org.nervos.neuron.dialog;

import android.app.AlertDialog;
import android.content.Context;

import java.util.List;

public class DialogUtil {

    public static void showListDialog(Context context, String title, List<String> list,
                                      OnItemClickListener onItemClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        String[] contents = new String[list.size()];
        list.toArray(contents);
        builder.setItems(contents, (dialog, which) -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(which);
            }
        });
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public interface OnItemClickListener{
        void onItemClick(int which);
    }

}
