package com.cryptape.cita_wallet.constant.url;

/**
 * Created by duanyytop on 2018/11/12.
 */
public class HttpAppChainUrls {

    // AppChain transaction list url
    public static final String APPCHAIN_NODE_URL = "http://121.196.200.225:1337";
    public static final String APPCHAIN_SERVER_URL = "https://microscope.cryptape.com:8888";
    public static final String APPCHAIN_TRANSACTION_URL = APPCHAIN_SERVER_URL
            + "/api/transactions?account=%s&page=%s&perPage=%s";
    public static final String APPCHAIN_ERC20_TRANSACTION_URL = APPCHAIN_SERVER_URL
            + "/api/erc20/transfers?address=%s&account=%s&page=%s&perPage=%s";

}
