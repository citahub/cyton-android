package com.cryptape.cita_wallet.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.List;

public class AntiHijackingUtil {

    public static final String TAG = "AntiHijackingUtil";

    /**
     * 检测当前Activity是否安全
     */
    public static boolean checkActivity(Context context) {
        boolean safe = false;

        String runningActivityPackageName = "";

        if (Build.VERSION.SDK_INT < 21) {
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> listRunningTaskInfo = null;
            try {
                listRunningTaskInfo = activityManager.getRunningTasks(1);
            } catch (SecurityException e) {
            }
            if (listRunningTaskInfo != null) {
                runningActivityPackageName = listRunningTaskInfo.get(0).topActivity.getPackageName();
            }
        } else {
            runningActivityPackageName = getTopPackageName(context);
        }

        if (context.getPackageName().equals(runningActivityPackageName)) {
            safe = true;
        }

        return safe;
    }

    /**
     * android 21 以上 getRunningTasks无法使用，使用此方法获取栈顶包名
     */
    private static String getTopPackageName(Context context) {
        String packageName = "";

        final int PROCESS_STATE_TOP = 2;
        ActivityManager.RunningAppProcessInfo currentInfo = null;

        Field field = null;
        try {
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception e) {

        }

        if (field != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
            if (appList != null)
                for (ActivityManager.RunningAppProcessInfo app : appList) {
                    if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                            && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                        Integer state = null;
                        try {
                            state = field.getInt(app);
                        } catch (Exception e) {

                        }

                        if (state != null && state == PROCESS_STATE_TOP) {
                            currentInfo = app;
                            break;
                        }
                    }
                }

            if (currentInfo != null) {
                packageName = currentInfo.processName;
            }
        }

        return packageName;
    }
}
