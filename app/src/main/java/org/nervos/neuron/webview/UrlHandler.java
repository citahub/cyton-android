package org.nervos.neuron.webview;

import android.net.Uri;

public interface UrlHandler {

    String getScheme();

    String handle(Uri uri);
}