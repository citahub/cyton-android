package org.nervos.neuron.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import org.json.JSONException;
import org.json.JSONObject;
import org.nervos.neuron.R;
import org.nervos.neuron.constant.SensorDataCons;
import org.nervos.neuron.fragment.CaptureFragment;
import org.nervos.neuron.util.qrcode.CodeUtils;
import org.nervos.neuron.util.qrcode.QRResultCheck;

/**
 * Created by duanyytop on 2018/5/28
 */
public class QrCodeActivity extends NBaseActivity {

    private CaptureFragment captureFragment;
    public static String SHOW_RIGHT = "showRight";


    @Override
    protected int getContentLayout() {
        return R.layout.activity_qr_code;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        captureFragment = new CaptureFragment();
        CodeUtils.setFragmentArgs(captureFragment, R.layout.capture_camera);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.capture_container, captureFragment).commit();
        captureFragment.setRight(getIntent().getBooleanExtra(SHOW_RIGHT, true));
    }

    @Override
    protected void initAction() {

    }

    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            track(QRResultCheck.check(result) + "", true);
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            bundle.putInt(CodeUtils.STRING_TYPE, QRResultCheck.check(result));
            resultIntent.putExtras(bundle);
            mActivity.setResult(RESULT_OK, resultIntent);
            mActivity.finish();
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            mActivity.setResult(RESULT_OK, resultIntent);
            mActivity.finish();
        }
    };

    public static void track(String type, boolean suc) {
        try {
            JSONObject object = new JSONObject();
            object.put(SensorDataCons.TAG_SCAN_TYPE, type);
            object.put(SensorDataCons.TAG_SCAN_RESULT, suc);
            SensorsDataAPI.sharedInstance().track(SensorDataCons.TRACK_SCAN_QR, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
        bundle.putString(CodeUtils.RESULT_STRING, "");
        resultIntent.putExtras(bundle);
        mActivity.setResult(RESULT_OK, resultIntent);
        mActivity.finish();
        super.onBackPressed();
    }
}
