package com.cryptape.cita_wallet.constant;

import android.os.Environment;

import java.math.BigInteger;

/**
 * Created by duanyytop on 2018/11/12.
 */
public class ConstantUtil {

    public static final String DEFAULT_CURRENCY = "CNY";

    public static final String RPC_RESULT_ZERO = "0x";

    // ERC20 method hashes
    public static final String ZERO_16 = "000000000000000000000000";
    public static final String NAME_HASH = "06fdde03";
    public static final String SYMBOL_HASH = "95d89b41";
    public static final String DECIMALS_HASH = "313ce567";
    public static final String BALANCE_OF_HASH = "70a08231";

    public static final String FINGERPRINT_TIP = "FINGERPRINT_TIP";
    public static final String FINGERPRINT = "Fingerprint";
    public static final String CURRENCY = "Currency";
    public static final String PROTOCOL = "Protocol";
    public static final String SENSOR_IP_ID = "SENSOR_IP_ID";

    public static final String IMG_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";


    // CITA Constant
    public static final String QUOTA_PRICE_DEFAULT = "1";
    public static final BigInteger QUOTA_TOKEN = new BigInteger("21000");
    public static final BigInteger QUOTA_ERC20 = new BigInteger("200000");

    public static final String MBA_CHAIN_ID = "2";
    public static final String MBA_CHAIN_NAME = "mba-testnet";
    public static final String MBA_TOKEN_NAME = "Merchant Base Assert";
    public static final String MBA_TOKEN_SYMBOL = "MBA";
    public static final String MBA_TOKEN_AVATAR = "https://download.mba.cmbchina.biz/images/MBA-logo.jpg";
    public static final String MBA_HTTP_PROVIDER = "http://testnet.mba.cmbchina.biz:1337";

    public static final String DEFAULT_CHAIN_ID = "1";
    public static final String DEFAULT_CHAIN_NAME = "test-chain";
    public static final String DEFAULT_TOKEN_NAME = "CITA Test Token";
    public static final String DEFAULT_TOKEN_SYMBOL = "CTT";
    public static final String DEFAULT_TOKEN_AVATAR = "https://cdn.cryptape.com/icon_appchain.png";
    public static final String DEFAULT_HTTP_PROVIDER = "https://node.cryptape.com";

    public static final long VALID_BLOCK_NUMBER_DIFF = 80L;


    public static final String TYPE_ETH = "ETH";
    public static final String TYPE_CITA = "CITA";

    // Ether Constant
    public static final String ETH = "ETH";
    public static final String ETHEREUM = "ethereum";
    public static final String ETH_MAINNET = "Ethereum Mainnet";
    public static final String GWEI = "GWei";
    public static final double MIN_GWEI = 1;

    public static final String ETH_NET = "ETH_NET";
    public static final String ETH_NET_MAIN = "Main Ethereum Network";
    public static final String ETH_NET_ROPSTEN_TEST = "Ropsten Test Network";
    public static final String ETH_NET_KOVAN_TEST = "Kovan Test Network";
    public static final String ETH_NET_RINKEBY_TEST = "Rinkeby Test Network";

    public static final String ETHEREUM_MAIN_ID = "-1";
    public static final String ETHEREUM_ROPSTEN_ID = "-3";
    public static final String ETHEREUM_KOVAN_ID = "-42";
    public static final String ETHEREUM_RINKEBY_ID = "-4";


    public static final String ETH_MAIN_NAME = "Ethereum Mainnet";
    public static final String ETH_RINKEBY_NAME = "Ethereum Rinkeby";
    public static final String ETH_KOVAN_NAME = "Ethereum Kovan";
    public static final String ETH_ROPSTEN_NAME = "Ethereum Ropsten";

    // gas constant data
    public static final BigInteger GAS_LIMIT = new BigInteger("21000");        // default eth gas limit is 21000
    public static final BigInteger GAS_ERC20_LIMIT = new BigInteger("100000");  // default eth gas limit is 100000
    public static final BigInteger GAS_LIMIT_PARAMETER = BigInteger.valueOf(4);
}