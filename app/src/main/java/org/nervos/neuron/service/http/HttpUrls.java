package org.nervos.neuron.service.http;

public class HttpUrls {

    // AppChain transaction list url
    public static final String APPCHAIN_NODE_URL = "http://121.196.200.225:1337";
    public static final String APPCHAIN_SERVER_URL = "https://microscope.cryptape.com:8888";
    public static final String APPCHAIN_TRANSACTION_URL = APPCHAIN_SERVER_URL
            + "/api/transactions?account=%s&page=%s&perPage=%s";
    public static final String APPCHAIN_ERC20_TRANSACTION_URL = APPCHAIN_SERVER_URL
            + "/api/erc20/transfers?address=%s&account=%s&page=%s&perPage=%s";

    // setting module's url list
    public static final String SOURCE_CODE_GITHUB_URL = "https://github.com/cryptape/neuron-android";
    public static final String PRODUCT_AGREEMENT_URL = "https://docs.nervos.org/neuron-android/#/product-agreement";
    public static final String NERVOS_WEB_URL = "https://www.nervos.org/";
    public static final String OPEN_SEA_URL = "https://opensea.io/";
    public static final String INFURA_URL = "https://infura.io/";
    public static final String NERVOS_TALK_URL = "https://forums.nervos.org/";
    public static final String PECKSHEILD_URL = "https://peckshield.com/";
    public static final String CITA_GITHUB_URL = "https://github.com/cryptape/cita";

    // Discover page config information
    public static final String DISCOVER_URL = "https://dapp.cryptape.com";

    //Token Currency ID Price
    public static final String TOKEN_ID = "https://api.coinmarketcap.com/v2/listings/";
    public static final String TOKEN_CURRENCY = "https://api.coinmarketcap.com/v2/ticker/@ID/?convert=@Currency";
    public static final String COLLECTION_LIST_URL = "https://api.opensea.io/api/v1/assets/?owner=";

    // Image url
    public static final String DEFAULT_WEB_IMAGE_URL = "https://cdn.cryptape.com/neuron/default_web_icon.png";

    //Token Describe
    public static final String TOKEN_LOGO = "https://github.com/consenlabs/token-profile/blob/master/images/@address.png?raw=true";
    public static final String TOKEN_DESC = "https://raw.githubusercontent.com/consenlabs/token-profile/master/erc20/@address.json";
    public static final String TOKEN_ERC20_DETAIL = "https://ntp.staging.cryptape.com?token=@address";
    public static final String TOKEN_DETAIL = "https://ntp.staging.cryptape.com?coin=@address";

}
