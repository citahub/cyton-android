package com.cryptape.cita_wallet.constant.url;

/**
 * Created by duanyytop on 2018/8/6
 */
public class HttpUrls {

    // setting module's url list
    public static final String SOURCE_CODE_GITHUB_URL = "https://github.com/cryptape/neuron-android";
    public static final String PRODUCT_AGREEMENT_URL = "https://docs.nervos.org/cyton-android/#/product-agreement";
    public static final String NERVOS_WEB_URL = "https://www.nervos.org/";
    public static final String OPEN_SEA_URL = "https://opensea.io/";
    public static final String INFURA_URL = "https://infura.io/";
    public static final String CITAHUB_TALK_URL = "https://talk.citahub.com/";
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
    public static final String TOKEN_LOGO = "https://github.com/consenlabs/token-profile/blob/master/images/%s.png?raw=true";
    public static final String TOKEN_DESC = "https://raw.githubusercontent.com/consenlabs/token-profile/master/erc20/%s.json";
    public static final String TOKEN_ERC20_DETAIL = "https://ntp.staging.cryptape.com?token=%s";
    public static final String TOKEN_DETAIL = "https://ntp.cryptape.com?coin=%s";

    //Eth Transaction Detail
    public static final String ETH_MAINNET_TRANSACTION_DETAIL = "https://etherscan.io/tx/%s";
    public static final String ETH_RINKEBY_TRANSACTION_DETAIL = "https://rinkeby.etherscan.io/tx/%s";
    public static final String ETH_ROPSTEN_TRANSACTION_DETAIL = "https://ropsten.etherscan.io/tx/%s";
    public static final String ETH_KOVAN_TRANSACTION_DETAIL = "https://kovan.etherscan.io/tx/%s";

    //CITA Transaction Detail
    public static final String CITA_TEST_TRANSACTION_DETAIL = "http://microscope.cryptape.com/#/transaction/%s";
}
