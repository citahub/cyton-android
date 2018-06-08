package org.nervos.neuron.service;

import android.content.Context;

import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.infura.InfuraHttpService;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class EthRpcService {

    public static final String ETH = "ETH";
    static final BigInteger GAS_LIMIT = Numeric.toBigInt("0x15F90");

    private static final String MAIN_NODE_IP = "https://mainnet.infura.io/h3iIzGIN6msu3KeUrdlt";
    private static final String RINKEBY_NODE_IP = "https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk";

    static final BigInteger ETHDecimal = new BigInteger("1000000000000000000");
    static final BigInteger ETH_GAS_Decimal = new BigInteger("100000000");
    static final String ZERO_16 = "000000000000000000000000";

    static final String NAME_HASH = "06fdde03";
    static final String SYMBOL_HASH = "95d89b41";
    static final String DECIMALS_HASH = "313ce567";
    static final String BALANCEOF_HASH = "70a08231";        // function balanceOf
    static final String TRANSFER_HASH = "a9059cbb";

    protected static Web3j service;
    protected static WalletItem walletItem;

    public static void init(Context context) {
        service = Web3jFactory.build(new InfuraHttpService(RINKEBY_NODE_IP));
        walletItem = DBWalletUtil.getCurrentWallet(context);
    }

    static List<TypeReference<Type>> intTypes = new ArrayList<>();
    protected static void initIntTypes() {
        intTypes.clear();
        intTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Int64.class;
            }
        });
    }

    static List<TypeReference<Type>> stringTypes = new ArrayList<>();
    protected static void initStringTypes() {
        stringTypes.clear();
        stringTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Utf8String.class;
            }
        });
    }

}
