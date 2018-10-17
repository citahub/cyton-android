package org.nervos.neuron.item;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class AppChainTransactionDBItem {

    public String hash;
    public long timestamp;
    public String validUntilBlock;
    public boolean isNativeToken;
    public String contractAddress;
    public String chain;

    public String from;
    public String to;
    public String value;
    public String chainName;
    //0 failed 1 success 2 pending
    public int status;

    public String getDate() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
        return ft.format(timestamp);
    }
}
