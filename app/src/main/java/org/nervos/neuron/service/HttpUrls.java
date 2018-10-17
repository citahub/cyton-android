package org.nervos.neuron.service;

public class HttpUrls {

    public static final String ETHER_SCAN_API_KEY = "T9GV1IF4V7YDXQ8F53U1FK2KHCE2KUUD8Z";
    public static final String APPCHAIN_SERVER_URL = "https://microscope.cryptape.com:8888";
    public static final String APPCHAIN_TRANSACTION_URL = APPCHAIN_SERVER_URL + "/api/transactions?account=";
    public static final String APPCHAIN_ERC20_TRANSACTION_URL = APPCHAIN_SERVER_URL + "/api/erc20/transfers?address=@address&account=@account";
    public static final String APPCHAIN_SERVER_URL_STAGING = "http://47.97.171.140:18090";
    public static final String APPCHAIN_TRANSACTION_URL_STAGING = APPCHAIN_SERVER_URL_STAGING + "/api/transactions?account=";
    public static final String ETH_TRANSACTION_URL = "http://api.etherscan.io/api?apikey="
            + ETHER_SCAN_API_KEY + "&module=account&action=txlist&sort=asc&address=";
    public static final String ETH_ERC20_TRANSACTION_URL = "https://api.etherscan.io/api?apikey="
            + ETHER_SCAN_API_KEY + "&module=account&action=tokentx&sort=asc";


    // setting module url list
    public static final String SOURCE_CODE_GITHUB_URL = "https://github.com/cryptape/neuron-android";
    public static final String PRODUCT_AGREEMENT_URL = "https://docs.nervos.org/neuron-android/#/product-agreement";
    public static final String CONTACT_US_RUL = "http://appchain.nervos.org//#/contact-us";
    public static final String NERVOS_NETWORK = "https://www.nervos.org/";
    public static final String OPEN_SEA = "https://opensea.io/";
    public static final String INFURA = "https://infura.io/";
    public static final String NERVOS_FORUMS = "https://forums.nervos.org/";
    public static final String PECKSHEILD = "https://peckshield.com/";
    public static final String CITA = "https://github.com/cryptape/cita";


    // node host
    public static final String APPCHAIN_NODE_IP = "http://121.196.200.225:1337";
    public static final String ETH_NODE_IP = "https://mainnet.infura.io/h3iIzGIN6msu3KeUrdlt";
//    public static final String ETH_NODE_IP = "https://rinkeby.infura.io/llyrtzQ3YhkdESt2Fzrk";


    // discover page config information
    public static final String DISCOVER_URL = "https://dapp.cryptape.com";

    //Token Currency ID Price
    public static final String TOKEN_ID = "https://api.coinmarketcap.com/v2/listings/";
    public static final String TOKEN_CURRENCY = "https://api.coinmarketcap.com/v2/ticker/@ID/?convert=@Currency";

    public static final String COLLECTION_LIST_URL = "https://api.opensea.io/api/v1/assets/?owner=";


    public static final String DEFAULT_WEB_IMAGE_URL = "https://cdn.cryptape.com/neuron/default_web_icon.png";

    //Token Describe
    public static final String TOKEN_LOGO = "https://github.com/consenlabs/token-profile/blob/master/images/@address.png?raw=true";
    public static final String TOKEN_DESC = "https://raw.githubusercontent.com/consenlabs/token-profile/master/erc20/@address.json";
}
