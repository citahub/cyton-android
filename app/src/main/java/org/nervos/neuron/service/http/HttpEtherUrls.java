package org.nervos.neuron.service.http;

import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

/**
 * Created by duanyytop on 2018/11/18
 */
public class HttpEtherUrls {

    // Ether node host
    private static final String ETH_NODE_MAIN_URL = "https://mainnet.infura.io/h3iIzGIN6msu3KeUrdlt";
    private static final String ETH_NODE_URL_RINKEBY = "https://rinkeby.infura.io/llyrtzQ3YhkdESt2Fzrk";
    private static final String ETH_NODE_URL_KOVAN = "https://kovan.infura.io/llyrtzQ3YhkdESt2Fzrk";
    private static final String ETH_NODE_URL_ROPSTEN = "https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk";

    public static String getEthNodeUrl() {
        switch (SharePrefUtil.getString(ConstUtil.ETH_NET, ConstUtil.ETH_NET_MAIN)) {
            case ConstUtil.ETH_NET_RINKEBY_TEST:
                return ETH_NODE_URL_RINKEBY;
            case ConstUtil.ETH_NET_KOVAN_TEST:
                return ETH_NODE_URL_KOVAN;
            case ConstUtil.ETH_NET_ROPSTEN_TEST:
                return ETH_NODE_URL_ROPSTEN;
            case ConstUtil.ETH_NET_MAIN:
            default:
                return ETH_NODE_MAIN_URL;
        }
    }


    // Ether transaction list url
    private static final String ETHER_SCAN_API_KEY = "T9GV1IF4V7YDXQ8F53U1FK2KHCE2KUUD8Z";
    private static final String ETH_MAIN_BASE_URL = "https://api.etherscan.io/api";
    private static final String ETH_RINKEBY_BASE_URL = "https://api-rinkeby.etherscan.io/api";
    private static final String ETH_KOVAN_BASE_URL = "https://api-kovan.etherscan.io/api";
    private static final String ETH_ROPSTEN_BASE_URL = "https://api-ropsten.etherscan.io/api";

    private static final String END_URL = "?apikey=" + ETHER_SCAN_API_KEY
            + "&module=account&action=txlist&sort=asc&address=%s&page=%s&offset=%s";
    private static final String ERC20_END_URL = "?apikey=" + ETHER_SCAN_API_KEY
            + "&module=account&action=tokentx&sort=asc&contractaddress=%s&address=%s&page=%s&offset=%s";

    private static String getEtherBaseUrl() {
        switch (SharePrefUtil.getString(ConstUtil.ETH_NET, ConstUtil.ETH_NET_MAIN)) {
            case ConstUtil.ETH_NET_RINKEBY_TEST:
                return ETH_RINKEBY_BASE_URL;
            case ConstUtil.ETH_NET_KOVAN_TEST:
                return ETH_KOVAN_BASE_URL;
            case ConstUtil.ETH_NET_ROPSTEN_TEST:
                return ETH_ROPSTEN_BASE_URL;
            case ConstUtil.ETH_NET_MAIN:
            default:
                return ETH_MAIN_BASE_URL;
        }
    }

    static String getEtherTransactionUrl() {
        return getEtherBaseUrl() + END_URL;
    }

    static String getEtherERC20TransactionUrl() {
        return getEtherBaseUrl() + ERC20_END_URL;
    }

}
