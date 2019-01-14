package com.cryptape.cita_wallet.view.webview;

import android.net.Uri;

public interface UrlHandler {

    String getScheme();

    String handle(Uri uri);
}