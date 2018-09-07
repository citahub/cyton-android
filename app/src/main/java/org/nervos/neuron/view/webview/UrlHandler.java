package org.nervos.neuron.view.webview;

import android.net.Uri;

public interface UrlHandler {

    String getScheme();

    String handle(Uri uri);
}