package org.nervos.neuron.util.db;

import android.content.Context;
import android.text.TextUtils;
import com.snappydb.SnappydbException;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by duanyytop on 2018/5/8
 */
public class DBWalletUtil extends DBUtil {

    private static final String DB_WALLET = "db_wallet";

    //==============================Current Wallet====================================
    public static WalletItem getCurrentWallet(Context context) {
        WalletItem walletItem = getWallet(context, SharePrefUtil.getCurrentWalletName());
        if (walletItem == null)
            return null;
        if (walletItem.chainItems == null || walletItem.chainItems.size() == 0) {
            walletItem.chainItems = DBChainUtil.getAllChain(context);
            saveWallet(context, walletItem);
        }
        return walletItem;
    }

    public static void addTokenToCurrentWallet(Context context, TokenItem tokenItem) {
        addTokenToWallet(context, SharePrefUtil.getCurrentWalletName(), tokenItem);
    }

    public static void updateTokenToCurrentWallet(Context context, TokenItem tokenItem) {
        updateTokenToWallet(context, SharePrefUtil.getCurrentWalletName(), tokenItem);
    }

    public static void deleteTokenFromCurrentWallet(Context context, TokenItem tokenItem) {
        deleteTokenFromWallet(context, SharePrefUtil.getCurrentWalletName(), tokenItem);
    }

    public static WalletItem initChainToCurrentWallet(Context context, WalletItem walletItem) {
        walletItem.chainItems.add(new ChainItem(ConstantUtil.ETHEREUM_MAIN_ID, ConstantUtil.ETH_MAINNET, ConstantUtil.ETH
                , ConstantUtil.ETH));
        walletItem.chainItems.add(new ChainItem(ConstantUtil.CMB_CHAIN_ID, ConstantUtil.CMB_CHAIN_NAME, ConstantUtil.CMB_HTTP_PROVIDER,
                ConstantUtil.CMB_TOKEN_NAME, ConstantUtil.CMB_TOKEN_SYMBOL, ConstantUtil.CMB_TOKEN_AVATAR));
        for (ChainItem chainItem : walletItem.chainItems) {
            if (!TextUtils.isEmpty(chainItem.tokenName)) {
                walletItem.tokenItems.add(new TokenItem(chainItem));
            }
        }
        return walletItem;
    }

    public static boolean checkChainInCurrentWallet(Context context, ChainItem chainItem) {
        return checkChainInWallet(getWallet(context, SharePrefUtil.getCurrentWalletName()), chainItem) == -1 ? false : true;
    }

    public static void saveChainInCurrentWallet(Context context, ChainItem chainItem) {
        saveChainAndToken(context, SharePrefUtil.getCurrentWalletName(), chainItem);
    }

    public static ChainItem getChainItemFromCurrentWallet(Context context, String chainId) {
        return getChainItem(context, SharePrefUtil.getCurrentWalletName(), chainId);
    }

    //==========================Check===============================================

    public static boolean checkWalletName(Context context, String name) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_WALLET);
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
            boolean isKeyExist = false;
            try {
                db = openDB(context, DB_WALLET);
                List<String> names = getAllWalletName(context);
                for (String name : names) {
                    WalletItem walletItem = getWallet(context, name);
                    isKeyExist = (walletItem != null && walletItem.address.equalsIgnoreCase(address));
                    if (isKeyExist) return true;
                }
                db.close();
                return false;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return false;
        }
    }

    private static int checkTokenInWallet(WalletItem walletItem, TokenItem tokenItem) {
        for (int i = 0; i < walletItem.tokenItems.size(); i++) {
            if (walletItem.tokenItems.get(i).symbol.equals(tokenItem.symbol) && walletItem.tokenItems.get(i).getChainId().equals(tokenItem.getChainId())) {
                return i;
            }
        }
        return -1;
    }

    public static boolean checkTokenInCurrentWallet(Context context, String symbol) {
        WalletItem walletItem = getCurrentWallet(context);
        for (TokenItem token : walletItem.tokenItems) {
            if (token.symbol.equals(symbol)) {
                return true;
            }
        }
        return false;
    }

    private static int checkChainInWallet(WalletItem walletItem, ChainItem chainItem) {
        for (int i = 0; i < walletItem.chainItems.size(); i++) {
            if (walletItem.chainItems.get(i).getChainId().equals(chainItem.getChainId())) {
                return i;
            }
        }
        return -1;
    }

    //========================================Wallet Name==================================

    public static boolean updateWalletName(Context context, String name, String newName) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_WALLET);
                WalletItem walletItem = db.getObject(getDbKey(name), WalletItem.class);
                db.del(getDbKey(name));
                walletItem.name = newName;
                db.put(getDbKey(newName), walletItem);
                db.close();
                return true;
            } catch (SnappydbException e) {
                handleException(db, e);
                return false;
            }
        }
    }

    public static List<String> getAllWalletName(Context context) {
        synchronized (dbObject) {
            List<String> walletList = new ArrayList<>();
            try {
                db = openDB(context, DB_WALLET);
                String[] keys = db.findKeys(DB_PREFIX);
                List<WalletItem> walletItems = new ArrayList<>();
                for (String key : keys) {
                    walletItems.add(db.getObject(key, WalletItem.class));
                }
                db.close();
                Collections.sort(walletItems, new Comparator<WalletItem>() {
                    @Override
                    public int compare(WalletItem o1, WalletItem o2) {
                        return (int) (o2.timestamp - o1.timestamp);
                    }
                });
                for (WalletItem walletItem : walletItems) {
                    walletList.add(walletItem.name);
                }
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return walletList;
        }
    }

    //==================================Wallet==============================================

    public static WalletItem getWallet(Context context, String walletName) {
        synchronized (dbObject) {
            if (TextUtils.isEmpty(walletName)) return null;
            try {
                db = openDB(context, DB_WALLET);
                WalletItem walletItem = db.getObject(getDbKey(walletName), WalletItem.class);
                db.close();
                return walletItem;
            } catch (SnappydbException e) {
                handleException(db, e);
                return null;
            }
        }
    }

    public static List<WalletItem> getAllWallet(Context context) {
        synchronized (dbObject) {
            List<WalletItem> walletItems = new ArrayList<>();
            try {
                db = openDB(context, DB_WALLET);
                String[] keys = db.findKeys(DB_PREFIX);
                for (String key : keys) {
                    walletItems.add(db.getObject(key, WalletItem.class));
                }
                db.close();
                Collections.sort(walletItems, new Comparator<WalletItem>() {
                    @Override
                    public int compare(WalletItem o1, WalletItem o2) {
                        return (int) (o2.timestamp - o1.timestamp);
                    }
                });
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return walletItems;
        }
    }

    public static void saveWallet(Context context, WalletItem walletItem) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_WALLET);
                db.put(getDbKey(walletItem.name), walletItem);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static boolean updateWalletPassword(Context context, String name, String oldPassword, String newPassword) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_WALLET);
                WalletItem walletItem = db.getObject(getDbKey(name), WalletItem.class);
                try {
                    WalletEntity walletEntity = WalletEntity.fromKeyStore(oldPassword, walletItem.keystore);
                    BigInteger privateKey = Numeric.toBigInt(walletEntity.getPrivateKey());
                    walletItem.keystore = WalletEntity.fromPrivateKey(privateKey, newPassword).getKeystore();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                db.put(getDbKey(name), walletItem);
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
                db = openDB(context, DB_WALLET);
                db.del(getDbKey(name));
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    //================================Token======================================================

    public static void addTokenToWallet(Context context, String walletName, TokenItem tokenItem) {
        WalletItem walletItem = getWallet(context, walletName);
        if (walletItem != null) {
            if (walletItem.tokenItems == null) {
                walletItem.tokenItems = new ArrayList<>();
            }
            if (checkTokenInWallet(walletItem, tokenItem) == -1) {
                walletItem.tokenItems.add(tokenItem);
            }
            saveWallet(context, walletItem);
        }
    }

    public static void updateTokenToWallet(Context context, String walletName, TokenItem tokenItem) {
        WalletItem walletItem = getWallet(context, walletName);
        if (walletItem != null) {
            if (walletItem.tokenItems == null) {
                walletItem.tokenItems = new ArrayList<>();
            }
            for (int i = 0; i < walletItem.tokenItems.size(); i++) {
                if (walletItem.tokenItems.get(i).symbol.equals(tokenItem.symbol)
                        && walletItem.tokenItems.get(i).getChainId().equals(tokenItem.getChainId())
                        && walletItem.tokenItems.get(i).name.equals(tokenItem.name)) {
                    walletItem.tokenItems.set(i, tokenItem);
                    break;
                }
            }
            saveWallet(context, walletItem);
        }
    }

    public static void addTokenToAllWallet(Context context, TokenItem tokenItem) {
        List<String> walletNames = getAllWalletName(context);
        for (String walletName : walletNames) {
            addTokenToWallet(context, walletName, tokenItem);
        }
    }

    public static void deleteTokenFromWallet(Context context, String walletName, TokenItem tokenItem) {
        WalletItem walletItem = getWallet(context, walletName);
        if (walletItem != null) {
            if (walletItem.tokenItems == null) {
                walletItem.tokenItems = new ArrayList<>();
            }
            Iterator<TokenItem> iterator = walletItem.tokenItems.iterator();
            while (iterator.hasNext()) {
                TokenItem item = iterator.next();
                if (tokenItem.symbol.equals(item.symbol)) {
                    iterator.remove();
                }
            }
            saveWallet(context, walletItem);
        }
    }

    public static List<TokenItem> getAllTokenFromWallet(Context context, String walletName) {
        return Objects.requireNonNull(getWallet(context, walletName)).tokenItems;
    }

    //================================Chain====================================

    public static void saveChain(Context context, String walletName, ChainItem chainItem) {
        WalletItem walletItem = getWallet(context, walletName);
        if (walletItem != null) {
            if (walletItem.tokenItems == null) {
                walletItem.tokenItems = new ArrayList<>();
            }
            if (checkChainInWallet(walletItem, chainItem) == -1) {
                walletItem.chainItems.add(chainItem);
            }
            saveWallet(context, walletItem);
        }
    }

    public static void saveChainAndToken(Context context, String walletName, ChainItem chainItem) {
        WalletItem walletItem = getWallet(context, walletName);
        if (walletItem != null) {
            if (walletItem.chainItems == null) {
                walletItem.chainItems = new ArrayList<>();
            }
            int index = checkChainInWallet(walletItem, chainItem);
            if (index == -1) {
                walletItem.chainItems.add(chainItem);
                walletItem.tokenItems.add(new TokenItem(chainItem));
            } else {
                ChainItem item = walletItem.chainItems.get(index);
                TokenItem tokenItem = new TokenItem(chainItem);
                walletItem.chainItems.remove(index);
                walletItem.chainItems.add(index, chainItem);
                int tokenIndex = checkTokenInWallet(walletItem, new TokenItem(item));
                if (tokenIndex == -1) {
                    walletItem.tokenItems.add(tokenItem);
                } else {
                    walletItem.tokenItems.remove(tokenIndex);
                    walletItem.tokenItems.add(tokenIndex, tokenItem);
                }
            }
            saveWallet(context, walletItem);
        }
    }

    public static ChainItem getChainItem(Context context, String walletName, String chainId) {
        WalletItem walletItem = getWallet(context, walletName);
        if (walletItem != null && walletItem.chainItems != null) {
            for (ChainItem chainItem : walletItem.chainItems) {
                if (chainId.equals(chainItem.getChainId())) {
                    return chainItem;
                }
            }
        }
        return null;
    }

}
