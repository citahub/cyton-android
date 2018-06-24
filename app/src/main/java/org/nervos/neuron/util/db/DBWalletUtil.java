package org.nervos.neuron.util.db;

import android.content.Context;
import android.text.TextUtils;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.R;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.crypto.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class DBWalletUtil extends DBUtil {

    private static final String DB_WALLET = "db_wallet";
    private static final String ETH = "ETH";

    public static WalletItem getWallet(Context context, String walletName) {
        if (TextUtils.isEmpty(walletName)) return null;
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            WalletItem walletItem = db.getObject(getDbKey(walletName), WalletItem.class);
            db.close();
            return walletItem;
        } catch (SnappydbException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static WalletItem getCurrentWallet(Context context) {
        return getWallet(context, SharePrefUtil.getCurrentWalletName());
    }

    public static List<String> getAllWalletName(Context context) {
        List<String> walletList = new ArrayList<>();
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            String[] keys = db.findKeys(DB_PREFIX);
            List<WalletItem> walletItems = new ArrayList<>();
            for(String key: keys) {
                walletItems.add(db.getObject(key, WalletItem.class));
            }
            db.close();
            Collections.sort(walletItems, new Comparator<WalletItem>() {
                @Override
                public int compare(WalletItem o1, WalletItem o2) {
                    return (int)(o2.timestamp - o1.timestamp);
                }
            });
            for (WalletItem walletItem : walletItems) {
                walletList.add(walletItem.name);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return walletList;
    }

    public static void saveWallet(Context context, WalletItem walletItem){
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            db.put(getDbKey(walletItem.name), walletItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static boolean updateWalletPassword(Context context, String name, String oldPassword, String newPassword) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            WalletItem walletItem = db.getObject(getDbKey(name), WalletItem.class);
            try {
                String privateKey = AESCrypt.decrypt(oldPassword, walletItem.cryptPrivateKey);
                walletItem.cryptPrivateKey = AESCrypt.encrypt(newPassword, privateKey);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                return false;
            }
            db.put(getDbKey(name), walletItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void updateWalletName(Context context, String name, String newName) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            WalletItem walletItem = db.getObject(getDbKey(name), WalletItem.class);
            db.del(getDbKey(name));
            walletItem.name = newName;
            db.put(getDbKey(newName), walletItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkWalletName(Context context, String name) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            boolean isKeyExist = db.exists(getDbKey(name));
            db.close();
            return isKeyExist;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkWalletAddress(Context context, String address) {
        boolean isKeyExist = false;
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            List<String> names = getAllWalletName(context);
            for(String name: names) {
                WalletItem walletItem = getWallet(context, name);
                isKeyExist = (walletItem != null && walletItem.address.equals(address));
            }
            db.close();
            return isKeyExist;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return isKeyExist;
    }

    public static void deleteWallet(Context context, String name) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            db.del(getDbKey(name));
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void addTokenToWallet(Context context, String walletName, TokenItem tokenItem){
        LogUtil.d("addTokenToWallet: " + walletName);
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
            if (token.symbol.equals(tokenItem.symbol)) {
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

    public static void addTokenToAllWallet(Context context, TokenItem tokenItem){
        LogUtil.d("addTokenToAllWallet");
        List<String> walletNames = getAllWalletName(context);
        for (String walletName: walletNames) {
            addTokenToWallet(context, walletName, tokenItem);
        }
    }

    public static void addTokenToCurrentWallet(Context context, TokenItem tokenItem){
        WalletItem walletItem = getCurrentWallet(context);
        addTokenToWallet(context, walletItem.name, tokenItem);
    }

    public static void deleteTokenFromWallet(Context context, String walletName, TokenItem tokenItem){
        WalletItem walletItem = getWallet(context, walletName);
        if (walletItem != null) {
            if (walletItem.tokenItems == null) {
                walletItem.tokenItems = new ArrayList<>();
            }
            Iterator<TokenItem> iterator = walletItem.tokenItems.iterator();
            while(iterator.hasNext()){
                TokenItem item = iterator.next();
                if(tokenItem.symbol.equals(item.symbol)){
                    iterator.remove();
                }
            }
            saveWallet(context, walletItem);
        }
    }

    public static void deleteTokenFromCurrentWallet(Context context, TokenItem tokenItem){
        WalletItem walletItem = getCurrentWallet(context);
        deleteTokenFromWallet(context, walletItem.name, tokenItem);
    }


    public static List<TokenItem> getAllTokenFromWallet(Context context, String walletName) {
        return Objects.requireNonNull(getWallet(context, walletName)).tokenItems;
    }


    /**
     * add origin token of ethereum and cita to wallet
     * @param context
     * @param walletItem
     * @return
     */
    public static WalletItem addOriginTokenToWallet(Context context, WalletItem walletItem) {
        List<TokenItem> tokenItemList = new ArrayList<>();
        tokenItemList.add(new TokenItem(ETH, R.drawable.ether_big, 0, -1));
        walletItem.tokenItems = tokenItemList;

        List<ChainItem> chainItemList = DBChainUtil.getAllChain(context);
        for (ChainItem chainItem : chainItemList) {
            if (!TextUtils.isEmpty(chainItem.tokenName)) {
                walletItem.tokenItems.add(new TokenItem(chainItem));
            }
        }
        return walletItem;
    }

}
