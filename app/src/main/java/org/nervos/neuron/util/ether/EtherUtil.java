package org.nervos.neuron.util.ether;

import android.text.TextUtils;
import org.nervos.appchain.utils.Numeric;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.util.url.HttpEtherUrls;

import java.math.BigInteger;

/**
 * Created by duanyytop on 2018/11/12.
 */
public class EtherUtil {

    public static boolean isEther(TokenItem tokenItem) {
        return Numeric.toBigInt(tokenItem.getChainId()).compareTo(BigInteger.ZERO) < 0;
    }

    public static boolean isNative(TokenItem tokenItem) {
        return TextUtils.isEmpty(tokenItem.contractAddress);
    }

    public static String getEtherId() {
        switch (SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_NET_MAIN)) {
            case ConstantUtil.ETH_NET_RINKEBY_TEST:
                return ConstantUtil.ETHEREUM_RINKEBY_ID;
            case ConstantUtil.ETH_NET_KOVAN_TEST:
                return ConstantUtil.ETHEREUM_KOVAN_ID;
            case ConstantUtil.ETH_NET_ROPSTEN_TEST:
                return ConstantUtil.ETHEREUM_ROPSTEN_ID;
            case ConstantUtil.ETH_NET_MAIN:
            default:
                return ConstantUtil.ETHEREUM_MAIN_ID;
        }
    }


    public static String getEthNodeName() {
        switch (SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_NET_MAIN)) {
            case ConstantUtil.ETH_NET_RINKEBY_TEST:
                return ConstantUtil.ETH_RINKEBY_NAME;
            case ConstantUtil.ETH_NET_KOVAN_TEST:
                return ConstantUtil.ETH_KOVAN_NAME;
            case ConstantUtil.ETH_NET_ROPSTEN_TEST:
                return ConstantUtil.ETH_ROPSTEN_NAME;
            case ConstantUtil.ETH_NET_MAIN:
            default:
                return ConstantUtil.ETH_MAIN_NAME;
        }
    }


    public static String getEthNodeUrl() {
        switch (SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_NET_MAIN)) {
            case ConstantUtil.ETH_NET_RINKEBY_TEST:
                return HttpEtherUrls.ETH_NODE_URL_RINKEBY;
            case ConstantUtil.ETH_NET_KOVAN_TEST:
                return HttpEtherUrls.ETH_NODE_URL_KOVAN;
            case ConstantUtil.ETH_NET_ROPSTEN_TEST:
                return HttpEtherUrls.ETH_NODE_URL_ROPSTEN;
            case ConstantUtil.ETH_NET_MAIN:
            default:
                return HttpEtherUrls.ETH_NODE_MAIN_URL;
        }
    }

    public static String getEthNodeUrl(String id) {
        switch (id) {
            case ConstantUtil.ETHEREUM_RINKEBY_ID:
                return HttpEtherUrls.ETH_NODE_URL_RINKEBY;
            case ConstantUtil.ETHEREUM_KOVAN_ID:
                return HttpEtherUrls.ETH_NODE_URL_KOVAN;
            case ConstantUtil.ETHEREUM_ROPSTEN_ID:
                return HttpEtherUrls.ETH_NODE_URL_ROPSTEN;
            case ConstantUtil.ETHEREUM_MAIN_ID:
            default:
                return HttpEtherUrls.ETH_NODE_MAIN_URL;
        }
    }

    private static String getEtherBaseUrl() {
        switch (SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_NET_MAIN)) {
            case ConstantUtil.ETH_NET_RINKEBY_TEST:
                return HttpEtherUrls.ETH_RINKEBY_BASE_URL;
            case ConstantUtil.ETH_NET_KOVAN_TEST:
                return HttpEtherUrls.ETH_KOVAN_BASE_URL;
            case ConstantUtil.ETH_NET_ROPSTEN_TEST:
                return HttpEtherUrls.ETH_ROPSTEN_BASE_URL;
            case ConstantUtil.ETH_NET_MAIN:
            default:
                return HttpEtherUrls.ETH_MAIN_BASE_URL;
        }
    }

    public static String getEtherTransactionUrl() {
        return getEtherBaseUrl() + HttpEtherUrls.END_URL;
    }

    public static String getEtherERC20TransactionUrl() {
        return getEtherBaseUrl() + HttpEtherUrls.ERC20_END_URL;
    }

}
