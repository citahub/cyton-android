package org.nervos.neuron.util;

import android.hardware.Camera;

/**
 * Created by BaojunCZ on 2018/9/20.
 */
public class PermissionUtils {
    //判断摄像头是否可用
    public static boolean isCameraCanUse() {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            canUse = false;
        }
        if (canUse) {
            mCamera.release();
            mCamera = null;
        }
        return canUse;
    }
}
