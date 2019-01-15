package com.cryptape.cita_wallet.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by BaojunCZ on 2018/8/6.
 */
public class SharePicUtils {

    public static void savePic(String path, Bitmap bitmap) throws IOException {
        //save qrcode
        File file = new File(path);
        if (file.exists())
            file.delete();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        //if we use CompressFormat.JPEG,bitmap will have black background
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        bos.flush();
        bos.close();
        bitmap.recycle();
    }

    //get screenShoot
    public static Bitmap getCacheBitmapFromView(View view) {
        final boolean drawingCacheEnabled = true;
        view.setDrawingCacheEnabled(drawingCacheEnabled);
        view.buildDrawingCache(drawingCacheEnabled);
        final Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache);
            view.setDrawingCacheEnabled(false);
        } else {
            bitmap = null;
        }
        return bitmap;
    }

    public static void SharePic(Activity activity, String savePath) {
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= 24) {
            File file = new File(savePath);
            imageUri = FileProvider.getUriForFile(activity, "com.cryptape.cita_wallet.fileprovider", file);
        } else {
            imageUri = Uri.fromFile(new File(savePath));
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        activity.startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

}
