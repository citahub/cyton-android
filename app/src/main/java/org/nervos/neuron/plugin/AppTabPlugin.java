package org.nervos.neuron.plugin;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import org.nervos.neuron.activity.AddWebsiteActivity;

public class AppTabPlugin {

    private Context mContext;

    public AppTabPlugin(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void startAddWebsitePage() {
        mContext.startActivity(new Intent(mContext, AddWebsiteActivity.class));
    }

}
