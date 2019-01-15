package com.cryptape.cita_wallet.util.fingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/7/30.
 */

public class FingerPrintController {
    private FingerprintManager fpManager;
    private Activity activity;
    private CancellationSignal cancellationSignal;

    @TargetApi(Build.VERSION_CODES.M)
    @SuppressWarnings("ResourceType")
    public FingerPrintController(Activity activity) {
        this.activity = activity;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class.forName("android.hardware.fingerprint.FingerprintManager");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();

            }
            return;
        }
        this.fpManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
    }

    /**
     * check fingerprint support
     *
     * @return
     */
    public boolean isSupportFingerprint() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return false;
        return fpManager != null && fpManager.isHardwareDetected();
    }

    /**
     * check has fingerprints
     *
     * @return
     */
    public boolean hasEnrolledFingerprints() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return false;

        if (fpManager == null)
            throw new NotSupportFingerprintException("Your device is not support fingerprint");


        return fpManager.hasEnrolledFingerprints();
    }

    /**
     * authen
     *
     * @param callback
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void authenticate(final AuthenticateResultCallback callback) {
        if (fpManager == null)
            throw new NotSupportFingerprintException("Your device is not support fingerprint");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        cancellationSignal = new CancellationSignal();
        fpManager.authenticate(null, cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (callback != null)
                    callback.onAuthenticationError(errString.toString());
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
                Toast.makeText(activity, helpString.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (callback != null)
                    callback.onAuthenticationSucceeded();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (callback != null)
                    callback.onAuthenticationFailed();
            }
        }, null);
    }

    public void cancelAuth() {

        if (fpManager == null)
            throw new NotSupportFingerprintException("Your device is not support fingerprint");
        if (cancellationSignal != null && !cancellationSignal.isCanceled())
            cancellationSignal.cancel();
    }

    /**
     * get fingerprints
     *
     * @return
     */
    public List<Fingerprint> getEnrolledFingerprints() {
        if (fpManager == null)
            throw new NotSupportFingerprintException("Your device is not support fingerprint");
        List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
        Class fpManagerClass = fpManager.getClass();
        try {
            Method getEnrolledFingerprints = fpManagerClass.getMethod("getEnrolledFingerprints");
            Object fingerPrints = getEnrolledFingerprints.invoke(fpManager);
            if (fingerPrints != null) {
                ArrayList<Class<?>> fingerPrintList = (ArrayList<Class<?>>) fingerPrints;
                for (int i = 0; fingerPrintList != null && i < fingerPrintList.size(); i++) {
                    Fingerprint fingerprint = new Fingerprint(fingerPrintList.get(i));
                    fingerprints.add(fingerprint);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return fingerprints;
    }

    private static final String ACTION_SETTING = "android.settings.SETTINGS";

    //goto setting
    public static void openFingerPrintSettingPage(Context context) {
        Intent intent = new Intent(ACTION_SETTING);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }
}
