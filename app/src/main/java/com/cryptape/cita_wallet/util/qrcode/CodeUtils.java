package com.cryptape.cita_wallet.util.qrcode;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.cryptape.cita_wallet.fragment.CaptureFragment;

public class CodeUtils {

    public static final String RESULT_TYPE = "result_type";
    public static final String RESULT_STRING = "result_string";
    public static final String STRING_TYPE = "string_type";
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAILED = 2;
    public static final int STRING_UNVALID = 0;
    public static final int STRING_ADDRESS = 1;
    public static final int STRING_KEYSTORE = 3;
    public static final int STRING_WEB = 5;
    public static final int STRING_PRIVATE_KEY = 2;

    public static final String LAYOUT_ID = "layout_id";

    public interface AnalyzeCallback {

        void onAnalyzeSuccess(Bitmap bitmap, String result);

        void onAnalyzeFailed();
    }

    public static void setFragmentArgs(CaptureFragment captureFragment, int layoutId) {
        if (captureFragment == null || layoutId == -1) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_ID, layoutId);
        captureFragment.setArguments(bundle);
    }

}
