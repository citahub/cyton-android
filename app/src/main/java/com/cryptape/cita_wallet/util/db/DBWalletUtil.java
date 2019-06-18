package com.cryptape.cita_wallet.util.db;

import android.content.Context;
import android.text.TextUtils;

import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.item.Chain;
import com.cryptape.cita_wallet.item.Token;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.crypto.WalletEntity;
import com.esotericsoftware.kryo.Kryo;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by duanyytop on 2018/5/8
 */
public class DBWalletUtil extends DBUtil {

    private static final String DB_WALLET = "db_wallet";

    //==============================Current Wallet====================================
    public static Wallet getCurrentWallet(Context context) {
        Wallet wallet = getWallet(context, SharePrefUtil.getCurrentWalletName());
        if (wallet == null) {
            return null;
        }
        if (wallet.chains == null || wallet.chains.size() == 0) {
            wallet.chains = DBChainUtil.getAllChain(context);
            saveWallet(context, wallet);
        }
        return wallet;
    }

    public static Wallet initChainToCurrentWallet(Context context, Wallet wallet) {
        wallet.chains.add(new Chain(ConstantUtil.ETHEREUM_MAIN_ID, ConstantUtil.ETH_MAINNET, ConstantUtil.ETH, ConstantUtil.ETH));
        wallet.chains.add(new Chain(ConstantUtil.MBA_CHAIN_ID, ConstantUtil.MBA_CHAIN_NAME, ConstantUtil.MBA_HTTP_PROVIDER,
                ConstantUtil.MBA_TOKEN_NAME, ConstantUtil.MBA_TOKEN_SYMBOL, ConstantUtil.MBA_TOKEN_AVATAR));
        wallet.chains.add(new Chain(ConstantUtil.DEFAULT_CHAIN_ID, ConstantUtil.DEFAULT_CHAIN_NAME, ConstantUtil.DEFAULT_HTTP_PROVIDER,
                ConstantUtil.DEFAULT_TOKEN_NAME, ConstantUtil.DEFAULT_TOKEN_SYMBOL, ConstantUtil.DEFAULT_TOKEN_AVATAR));
        for (Chain chain : wallet.chains) {
            if (!TextUtils.isEmpty(chain.tokenName)) {
                wallet.tokens.add(new Token(chain));
            }
        }
        return wallet;
    }

    public static void saveChainInCurrentWallet(Context context, Chain chain) {
        saveChainAndToken(context, SharePrefUtil.getCurrentWalletName(), chain);
    }

    public static Chain getChainItemFromCurrentWallet(Context context, String chainId) {
        return getChainItem(context, SharePrefUtil.getCurrentWalletName(), chainId);
    }

    //==========================Check===============================================

    public static boolean checkWalletName(Context context, String name) {
        synchronized (dbObject) {
            try {
                db = openDB(context);
                boolean isKeyExist = db.exists(getDbKey(name));
                db.close();
                return isKeyExist;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return false;
        }
    }

    public static boolean checkWalletAddress(Context context, String address) {
        synchronized (dbObject) {
            try {
                db = openDB(context);
                List<Wallet> walletList = getAllWallet(context);
                for (Wallet item : walletList) {
                    if (item != null && item.address.equalsIgnoreCase(address)) {
                        return true;
                    }
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return false;
        }
    }

    private static int checkTokenInWallet(Wallet wallet, Token token) {
        for (int i = 0; i < wallet.tokens.size(); i++) {
            if (TextUtils.isEmpty(wallet.tokens.get(i).symbol) || wallet.tokens.get(i).symbol.equals(token.symbol)
                    && wallet.tokens.get(i).getChainId().equals(token.getChainId())) {
                return i;
            }
        }
        return -1;
    }


    private static int checkChainInWallet(Wallet wallet, Chain chain) {
        for (int i = 0; i < wallet.chains.size(); i++) {
            if (wallet.chains.get(i).getChainId().equals(chain.getChainId())) {
                return i;
            }
        }
        return -1;
    }

    //========================================Wallet Name==================================

    public static boolean updateWalletName(Context context, String name, String newName) {
        synchronized (dbObject) {
            try {
                db = openDB(context);
                Wallet wallet = db.getObject(getDbKey(name), Wallet.class);
                db.del(getDbKey(name));
                wallet.name = newName;
                db.put(getDbKey(newName), wallet);
                db.close();
                return true;
            } catch (SnappydbException e) {
                handleException(db, e);
                return false;
            }
        }
    }

    public static List<String> getAllWalletName(Context context) {
        List<String> walletList = new ArrayList<>();
        List<Wallet> wallets = getAllWallet(context);
        for (Wallet wallet : wallets) {
            walletList.add(wallet.name);
        }
        return walletList;
    }

    //==================================Wallet==============================================

    public static Wallet getWallet(Context context, String walletName) {
        synchronized (dbObject) {
            if (TextUtils.isEmpty(walletName))
                return null;
            try {
                db = openDB(context);
                Wallet wallet = db.getObject(getDbKey(walletName), Wallet.class);
                db.close();
                return wallet;
            } catch (SnappydbException e) {
                handleException(db, e);
                return null;
            }
        }
    }

    public static List<Wallet> getAllWallet(Context context) {
        synchronized (dbObject) {
            List<Wallet> wallets = new ArrayList<>();
            try {
                db = openDB(context, DB_WALLET);
                String[] keys = db.findKeys(DB_PREFIX);
                for (String key : keys) {
                    Wallet item = db.getObject(key, Wallet.class);
                    if (!TextUtils.isEmpty(item.address)) {
                        wallets.add(db.getObject(key, Wallet.class));
                    }
                }
                compare(wallets);
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return wallets;
        }
    }

    public static void saveWallet(Context context, Wallet wallet) {
        synchronized (dbObject) {
            try {
                db = openDB(context);
                db.put(getDbKey(wallet.name), wallet);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static boolean updateWalletPassword(Context context, String name, String oldPassword, String newPassword) {
        synchronized (dbObject) {
            try {
                db = openDB(context);
                Wallet wallet = db.getObject(getDbKey(name), Wallet.class);
                try {
                    WalletEntity walletEntity = WalletEntity.fromKeyStore(oldPassword, wallet.keystore);
                    BigInteger privateKey = Numeric.toBigInt(walletEntity.getPrivateKey());
                    wallet.keystore = WalletEntity.fromPrivateKey(privateKey, newPassword).getKeystore();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                db.put(getDbKey(name), wallet);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
                return false;
            }
            return true;
        }
    }

    public static void deleteWallet(Context context, String name) {
        synchronized (dbObject) {
            try {
                db = openDB(context);
                db.del(getDbKey(name));
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    //================================Token======================================================

    public static void addTokenToWallet(Context context, String walletName, Token token) {
        Wallet wallet = getWallet(context, walletName);
        if (wallet != null) {
            if (wallet.tokens == null) {
                wallet.tokens = new ArrayList<>();
            }
            if (checkTokenInWallet(wallet, token) == -1) {
                wallet.tokens.add(token);
            }
            saveWallet(context, wallet);
        }
    }

    public static void saveTokenBalanceCacheToWallet(Context context, String walletName, Token token) {
        Wallet wallet = getWallet(context, walletName);
        if (wallet != null) {
            if (wallet.tokens == null) {
                wallet.tokens = new ArrayList<>();
            }
            int index = checkTokenInWallet(wallet, token);
            if (index != -1) {
                wallet.tokens.set(index, token);
                saveWallet(context, wallet);
            }
        }
    }

    public static void updateTokenToWallet(Context context, String walletName, Token token) {
        Wallet wallet = getWallet(context, walletName);
        if (wallet != null) {
            if (wallet.tokens == null) {
                wallet.tokens = new ArrayList<>();
            }
            for (int i = 0; i < wallet.tokens.size(); i++) {
                if (wallet.tokens.get(i).symbol.equals(token.symbol)
                        && wallet.tokens.get(i).getChainId().equals(token.getChainId())
                        && wallet.tokens.get(i).name.equals(token.name)) {
                    wallet.tokens.set(i, token);
                    break;
                }
            }
            saveWallet(context, wallet);
        }
    }

    public static void deleteTokenFromWallet(Context context, String walletName, Token token) {
        Wallet wallet = getWallet(context, walletName);
        if (wallet != null) {
            if (wallet.tokens == null) {
                wallet.tokens = new ArrayList<>();
            }
            Iterator<Token> iterator = wallet.tokens.iterator();
            while (iterator.hasNext()) {
                if (token.symbol.equals(iterator.next().symbol)) {
                    iterator.remove();
                }
            }
            saveWallet(context, wallet);
        }
    }

    //================================Chain====================================


    public static void saveChainAndToken(Context context, String walletName, Chain chain) {
        Wallet wallet = getWallet(context, walletName);
        if (wallet != null) {
            if (wallet.chains == null) {
                wallet.chains = new ArrayList<>();
            }
            if(NumberUtil.isHex(chain.getChainId())){
               Long chainIdLong= NumberUtil.hexToLong(chain.getChainId());
               chain.setChainId(chainIdLong.toString());
            }
            int index = checkChainInWallet(wallet, chain);
            if (index == -1) {
                wallet.chains.add(chain);
                wallet.tokens.add(new Token(chain));
            } else {
                Chain item = wallet.chains.get(index);
                Token token = new Token(chain);
                wallet.chains.remove(index);
                wallet.chains.add(index, chain);
                int tokenIndex = checkTokenInWallet(wallet, new Token(item));
                if (tokenIndex == -1) {
                    wallet.tokens.add(token);
                } else {
                    wallet.tokens.remove(tokenIndex);
                    wallet.tokens.add(tokenIndex, token);
                }
            }
            saveWallet(context, wallet);
        }
    }

    public static Chain getChainItem(Context context, String walletName, String chainId) {
        Wallet wallet = getWallet(context, walletName);
        if (wallet != null && wallet.chains != null) {
            for (Chain chain : wallet.chains) {
                if (chainId.equals(chain.getChainId())) {
                    return chain;
                }
            }
        }
        return null;
    }

    private static void compare(List<Wallet> wallets) {
        Collections.sort(wallets, new Comparator<Wallet>() {
            @Override
            public int compare(Wallet o1, Wallet o2) {
                return (int) (o2.timestamp - o1.timestamp);
            }
        });
    }

    private static DB openDB(Context context) throws SnappydbException {
        db = openDB(context, DB_WALLET);
        Kryo kryo = db.getKryoInstance();
        kryo.register(Wallet.class);
        kryo.register(Chain.class);
        kryo.register(Token.class);
        kryo.register(List.class);
        return db;
    }

}
