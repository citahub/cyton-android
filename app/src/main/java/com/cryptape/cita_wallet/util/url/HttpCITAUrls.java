package com.cryptape.cita_wallet.util.url;

/**
 * Created by duanyytop on 2018/11/12.
 */
public class HttpCITAUrls {

    // CITA transaction list url
    public static final String CITA_NODE_URL = "http://121.196.200.225:1337";
    public static final String CITA_SERVER_URL = "https://microscope.cryptape.com:8888";
    public static final String CITA_TRANSACTION_URL = CITA_SERVER_URL
            + "/api/transactions?account=%s&page=%s&perPage=%s";
    public static final String CITA_ERC20_TRANSACTION_URL = CITA_SERVER_URL
            + "/api/erc20/transfers?address=%s&account=%s&page=%s&perPage=%s";

}
