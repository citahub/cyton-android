package org.nervos.neuron.service;

public class HttpUrls {

    public static final String ETHER_SCAN_API_KEY = "T9GV1IF4V7YDXQ8F53U1FK2KHCE2KUUD8Z";
    public static final String NERVOS_SERVER_URL = "http://47.97.171.140:4000";
    public static final String NERVOS_TRANSACTION_URL = NERVOS_SERVER_URL + "/api/transactions?account=";
    public static final String ETH_TRANSACTION_URL = "http://api.etherscan.io/api?apikey="
            + ETHER_SCAN_API_KEY + "&module=account&action=txlist&sort=asc&address=";


    // setting module url list
    public static final String SOURCE_CODE_GITHUB_URL = "https://github.com/cryptape/neuron-android";
    public static final String PRODUCT_AGREEMENT_URL = "https://docs.nervos.org/neuron-android/#/product-agreement";
    public static final String CONTACT_US_RUL = "http://appchain.nervos.org//#/contact-us";
    public static final String NERVOS_NETWORK = "https://www.nervos.org/";
    public static final String OPEN_SEA = "https://opensea.io/";
    public static final String INFURA = "https://infura.io/";


    // node host
    public static final String NERVOS_NODE_IP = "http://121.196.200.225:1337";
    public static final String ETH_NODE_IP = "https://mainnet.infura.io/h3iIzGIN6msu3KeUrdlt";
//    public static final String ETH_NODE_IP = "https://rinkeby.infura.io/llyrtzQ3YhkdESt2Fzrk";


    // discover page config information
    public static final String DISCOVER_URL = "https://dapp.cryptape.com/dapps";
    public static final String INNER_URL = "https://dapp.cryptape.com/dapps";

    //Token Currency ID Price
    public static final String TOKEN_ID = "https://api.coinmarketcap.com/v2/listings/";
    public static final String Token_CURRENCY = "https://api.coinmarketcap.com/v2/ticker/@ID/?convert=@Currency";

    public static final String COLLECTION_LIST_URL = "https://api.opensea.io/api/v1/assets/?owner=";


    public static final String DEFAULT_WEB_IMAGE_URL = "https://cdn.cryptape.com/neuron/default_web_icon.png";

}
