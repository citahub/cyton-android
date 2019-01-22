package com.cryptape.cita_wallet.util.ether;

import android.text.TextUtils;

import com.cryptape.cita.utils.Numeric;

import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.constant.url.HttpEtherUrls;
import com.cryptape.cita_wallet.constant.url.HttpUrls;
import com.cryptape.cita_wallet.item.Chain;
import com.cryptape.cita_wallet.item.Token;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;

import java.math.BigInteger;

/**
 * Created by duanyytop on 2018/11/12.
 */
public class EtherUtil {

    public static boolean isEther(Token token) {
        return Numeric.toBigInt(token.getChainId()).compareTo(BigInteger.ZERO) < 0;
    }

    public static boolean isEther(String chainID) {
        return Numeric.toBigInt(chainID).compareTo(BigInteger.ZERO) < 0;
    }

    public static boolean isNative(Token token) {
        return TextUtils.isEmpty(token.contractAddress);
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

    public static Chain getEthChainItem() {
        switch (SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_NET_MAIN)) {
            case ConstantUtil.ETH_NET_RINKEBY_TEST:
                return new Chain(ConstantUtil.ETHEREUM_RINKEBY_ID, ConstantUtil.ETH_RINKEBY_NAME, ConstantUtil.ETH, ConstantUtil.ETH);
            case ConstantUtil.ETH_NET_KOVAN_TEST:
                return new Chain(ConstantUtil.ETHEREUM_KOVAN_ID, ConstantUtil.ETH_KOVAN_NAME, ConstantUtil.ETH, ConstantUtil.ETH);
            case ConstantUtil.ETH_NET_ROPSTEN_TEST:
                return new Chain(ConstantUtil.ETHEREUM_ROPSTEN_ID, ConstantUtil.ETH_ROPSTEN_NAME, ConstantUtil.ETH, ConstantUtil.ETH);
            case ConstantUtil.ETH_NET_MAIN:
            default:
                return new Chain(ConstantUtil.ETHEREUM_MAIN_ID, ConstantUtil.ETH_MAIN_NAME, ConstantUtil.ETH, ConstantUtil.ETH);
        }
    }

    public static boolean isMainNet() {
        return SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_NET_MAIN).equals(ConstantUtil.ETH_NET_MAIN);
    }

    public static String getEtherTransactionDetailUrl() {
        switch (SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_NET_MAIN)) {
            case ConstantUtil.ETH_NET_RINKEBY_TEST:
                return HttpUrls.ETH_RINKEBY_TRANSACTION_DETAIL;
            case ConstantUtil.ETH_NET_KOVAN_TEST:
                return HttpUrls.ETH_KOVAN_TRANSACTION_DETAIL;
            case ConstantUtil.ETH_NET_ROPSTEN_TEST:
                return HttpUrls.ETH_ROPSTEN_TRANSACTION_DETAIL;
            case ConstantUtil.ETH_NET_MAIN:
            default:
                return HttpUrls.ETH_MAINNET_TRANSACTION_DETAIL;
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


    public static String getEtherTransactionStatusUrl() {
        return getEtherBaseUrl() + HttpEtherUrls.ETH_TRANSACTION_STATUS_URL;
    }

}
