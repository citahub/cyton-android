package org.nervos.neuron.util;

import java.math.BigInteger;

public class ConstantUtil {

    public static final String ETH = "ETH";

    public static final BigInteger ETHDecimal = new BigInteger("1000000000000000000");
    public static final String ETH_MAIN_NET = "Ethereum Mainnet";
    public static final String SOURCE_CODE_GITHUB_URL = "https://github.com/cryptape/Neuron-Android";

    // transaction list page config information
    public static final String ETHER_SCAN_API_KEY = "T9GV1IF4V7YDXQ8F53U1FK2KHCE2KUUD8Z";
    public static final String NERVOS_NODE_URL = "http://47.97.171.140:4000";
    public static final String NERVOS_TRANSACTION_URL = NERVOS_NODE_URL + "/api/transactions";
    public static final String ETH_TRANSACTION_URL
            = "http://api.etherscan.io/api?apikey=" + ETHER_SCAN_API_KEY
            + "&module=account&action=txlist&sort=asc&address=";


    // discover page config information
    public static final String DISCOVER_URL = "http://47.97.171.140:8866/dapps";
    public static final String INNER_URL = "http://47.97.171.140:8866/";

    public static final BigInteger NervosDecimal = new BigInteger("100000000000000000");
    public static final String NERVOS_NODE_IP = "http://47.94.105.230:1337";
    public static final String ETH_NODE_IP = "https://mainnet.infura.io/h3iIzGIN6msu3KeUrdlt";
//    public static final String ETH_NODE_IP = "https://rinkeby.infura.io/llyrtzQ3YhkdESt2Fzrk";

}
