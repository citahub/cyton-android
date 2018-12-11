package org.nervos.neuron.util.db;

import android.content.Context;
import android.text.TextUtils;

import com.esotericsoftware.kryo.Kryo;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
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
    public static WalletItem getCurrentWallet(Context context) {
        WalletItem walletItem = getWallet(context, SharePrefUtil.getCurrentWalletName());
        if (walletItem == null) return null;
        if (walletItem.chainItems == null || walletItem.chainItems.size() == 0) {
            walletItem.chainItems = DBChainUtil.getAllChain(context);
            saveWallet(context, walletItem);
        }
        return walletItem;
    }

    public static WalletItem initChainToCurrentWallet(Context context, WalletItem walletItem) {
        walletItem.chainItems.add(new ChainItem(ConstantUtil.ETHEREUM_MAIN_ID, ConstantUtil.ETH_MAINNET, ConstantUtil.ETH, ConstantUtil.ETH));
        walletItem.chainItems.add(new ChainItem(ConstantUtil.CMB_CHAIN_ID, ConstantUtil.CMB_CHAIN_NAME, ConstantUtil.CMB_HTTP_PROVIDER, ConstantUtil.CMB_TOKEN_NAME, ConstantUtil.CMB_TOKEN_SYMBOL, ConstantUtil.CMB_TOKEN_AVATAR));
        walletItem.chainItems.add(new ChainItem(ConstantUtil.NATT_CHAIN_ID, ConstantUtil.NATT_CHAIN_NAME, ConstantUtil.NATT_HTTP_PROVIDER, ConstantUtil.NATT_TOKEN_NAME, ConstantUtil.NATT_TOKEN_SYMBOL, ConstantUtil.NATT_TOKEN_AVATAR));
        for (ChainItem chainItem : walletItem.chainItems) {
            if (!TextUtils.isEmpty(chainItem.tokenName)) {
                walletItem.tokenItems.add(new TokenItem(chainItem));
            }
        }
        return walletItem;
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
                List<WalletItem> walletItemList = getAllWallet(context);
                for (WalletItem item : walletItemList) {
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

    private static int checkTokenInWallet(WalletItem walletItem, TokenItem tokenItem) {
        for (int i = 0; i < walletItem.tokenItems.size(); i++) {
            if (TextUtils.isEmpty(walletItem.tokenItems.get(i).symbol) || walletItem.tokenItems.get(i).symbol.equals(tokenItem.symbol) &&
                    walletItem.tokenItems.get(i).getChainId().equals(tokenItem.getChainId())) {
                return i;
            }
        }
        return -1;
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
                db = openDB(context);
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
        List<String> walletList = new ArrayList<>();
        List<WalletItem> walletItems = getAllWallet(context);
        for (WalletItem walletItem : walletItems) {
            walletList.add(walletItem.name);
        }
        return walletList;
    }

    //==================================Wallet==============================================

    public static WalletItem getWallet(Context context, String walletName) {
        synchronized (dbObject) {
            if (TextUtils.isEmpty(walletName)) return null;
            try {
                db = openDB(context);
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
                    WalletItem item = db.getObject(key, WalletItem.class);
                    if (!TextUtils.isEmpty(item.address)) {
                        walletItems.add(db.getObject(key, WalletItem.class));
                    }
                }
                compare(walletItems);
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return walletItems;
        }
    }

    public static void saveWallet(Context context, WalletItem walletItem) {
        synchronized (dbObject) {
            try {
                db = openDB(context);
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
                db = openDB(context);
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
                db = openDB(context);
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
                if (walletItem.tokenItems.get(i).symbol.equals(tokenItem.symbol) &&
                        walletItem.tokenItems.get(i).getChainId().equals(tokenItem.getChainId()) &&
                        walletItem.tokenItems.get(i).name.equals(tokenItem.name)) {
                    walletItem.tokenItems.set(i, tokenItem);
                    break;
                }
            }
            saveWallet(context, walletItem);
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
                if (tokenItem.symbol.equals(iterator.next().symbol)) {
                    iterator.remove();
                }
            }
            saveWallet(context, walletItem);
        }
    }

    //================================Chain====================================


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

    private static void compare(List<WalletItem> walletItems) {
        Collections.sort(walletItems, new Comparator<WalletItem>() {
            @Override
            public int compare(WalletItem o1, WalletItem o2) {
                return (int) (o2.timestamp - o1.timestamp);
            }
        });
    }

    private static DB openDB(Context context) throws SnappydbException {
        db = openDB(context, DB_WALLET);
        Kryo kryo = db.getKryoInstance();
        kryo.register(WalletItem.class);
        kryo.register(ChainItem.class);
        kryo.register(TokenItem.class);
        kryo.register(List.class);
        return db;
    }

}
