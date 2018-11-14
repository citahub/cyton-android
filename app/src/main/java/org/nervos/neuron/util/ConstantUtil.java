package org.nervos.neuron.util;

import android.os.Environment;

import org.nervos.neuron.util.db.SharePrefUtil;
import org.web3j.utils.Numeric;

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


    // AppChain Constant
    public static final String QUOTA_PRICE_DEFAULT = "1";
    public static final BigInteger QUOTA_TOKEN = new BigInteger("21000");
    public static final BigInteger QUOTA_ERC20 = new BigInteger("100000");

    public static final int CMB_CHAIN_ID = 2;
    public static final String CMB_CHAIN_NAME = "mba-testnet";
    public static final String CMB_TOKEN_NAME = "Merchant Base Assert";
    public static final String CMB_TOKEN_SYMBOL = "MBA";
    public static final String CMB_TOKEN_AVATAR = "https://download.mba.cmbchina.biz/images/MBA-logo.jpg";
    public static final String CMB_HTTP_PROVIDER = "http://testnet.mba.cmbchina.biz:1337";

    public static final long VALID_BLOCK_NUMBER_DIFF = 80L;



    // Ether Constant
    public static final String ETH = "ETH";
    public static final String ETHEREUM = "ethereum";
    public static final String ETH_MAINNET = "Ethereum Mainnet";
    public static final String GWEI = "GWei";

    public static final String ETH_NET = "ETH_NET";
    public static final String ETH_NET_MAIN = "Main_Ethereum_Network";
    public static final String ETH_NET_ROPSTEN_TEST = "Ropsten_Test_Network";
    public static final String ETH_NET_KOVAN_TEST = "Kovan_Test_Network";
    public static final String ETH_NET_RINKEBY_TEST = "Rinkeby_Test_Network";

    public static final int ETHEREUM_MAIN_ID = -1;
    public static final int ETHEREUM_ROPSTEN_ID = -3;
    public static final int ETHEREUM_KOVAN_ID = -42;
    public static final int ETHEREUM_RINKEBY_ID = -4;


    public static final String ETH_MAIN_NAME = "Ethereum Mainnet";
    public static final String ETH_RINKEBY_NAME = "Ethereum Rinkeby";
    public static final String ETH_KOVAN_NAME = "Ethereum Kovan";
    public static final String ETH_ROPSTEN_NAME = "Ethereum Ropsten";

    // gas constant data
    public static final BigInteger GAS_LIMIT = new BigInteger("21000");        // default eth gas limit is 21000
    public static final BigInteger GAS_ERC20_LIMIT = new BigInteger("100000");  // default eth gas limit is 100000
    public static final BigInteger GAS_LIMIT_PARAMETER = BigInteger.valueOf(4);
}