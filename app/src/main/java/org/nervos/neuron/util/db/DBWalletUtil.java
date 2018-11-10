package org.nervos.neuron.util.db;

import android.content.Context;
import android.text.TextUtils;

import com.snappydb.SnappydbException;

import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by duanyytop on 2018/5/8
 */
public class DBWalletUtil extends DBUtil {

    private static final String DB_WALLET = "db_wallet";

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

    public static WalletItem getCurrentWallet(Context context) {
        return getWallet(context, SharePrefUtil.getCurrentWalletName());
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

    public static void addTokenToWallet(Context context, String walletName, TokenItem tokenItem) {
        WalletItem walletItem = getWallet(context, walletName);
        if (walletItem != null) {
            if (walletItem.tokenItems == null) {
                walletItem.tokenItems = new ArrayList<>();
            }
            if (!checkTokenInWallet(walletItem, tokenItem)) {
                walletItem.tokenItems.add(tokenItem);
            }
            saveWallet(context, walletItem);
        }
    }

    private static boolean checkTokenInWallet(WalletItem walletItem, TokenItem tokenItem) {
        for (TokenItem token : walletItem.tokenItems) {
            if (token.symbol.equals(tokenItem.symbol) && token.chainId == tokenItem.chainId) {
                return true;
            }
        }
        return false;
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

    public static void addTokenToAllWallet(Context context, TokenItem tokenItem) {
        List<String> walletNames = getAllWalletName(context);
        for (String walletName : walletNames) {
            addTokenToWallet(context, walletName, tokenItem);
        }
    }

    public static void addTokenToCurrentWallet(Context context, TokenItem tokenItem) {
        WalletItem walletItem = getCurrentWallet(context);
        addTokenToWallet(context, walletItem.name, tokenItem);
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

    public static void deleteTokenFromCurrentWallet(Context context, TokenItem tokenItem) {
        WalletItem walletItem = getCurrentWallet(context);
        deleteTokenFromWallet(context, walletItem.name, tokenItem);
    }


    public static List<TokenItem> getAllTokenFromWallet(Context context, String walletName) {
        return Objects.requireNonNull(getWallet(context, walletName)).tokenItems;
    }


    /**
     * add origin token of ethereum and cita to wallet
     *
     * @param context
     * @param walletItem
     * @return
     */
    public static WalletItem addOriginTokenToWallet(Context context, WalletItem walletItem) {
        List<ChainItem> chainItemList = DBChainUtil.getAllChain(context);
        for (ChainItem chainItem : chainItemList) {
            if (!TextUtils.isEmpty(chainItem.tokenName)) {
                walletItem.tokenItems.add(new TokenItem(chainItem));
            }
        }
        return walletItem;
    }

}
