package org.nervos.neuron.util;

import android.content.Context;

import com.google.gson.Gson;

import org.nervos.neuron.R;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.CurrencyListItem;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.text.DecimalFormat;
import java.util.List;

public class CurrencyUtil {

    public static List<CurrencyItem> getCurrencyList(Context context) {
        String data = StreamUtils.get(context, R.raw.currency);
        Gson gson = new Gson();
        return gson.fromJson(data, CurrencyListItem.class).getCurrency();
    }

    public static CurrencyItem getCurrencyItem(Context context) {
        CurrencyItem currencyItem = null;
        List<CurrencyItem> list = getCurrencyList(context);
        String currencyName = SharePrefUtil.getString(ConstantUtil.CURRENCY, ConstantUtil.DEFAULT_CURRENCY);
        for (CurrencyItem item : list) {
            if (item.getName().equals(currencyName)) {
                currencyItem = item;
                break;
            }
        }
        if (currencyItem == null)
            currencyItem = list.get(0);
        return currencyItem;
    }

    public static String formatCurrency(Double currency) {
        DecimalFormat df = new DecimalFormat("######0.00");
        return fmtMicrometer(df.format(currency));
    }

    public static String fmtMicrometer(String text) {
        DecimalFormat df = new DecimalFormat("###,##0.########");;
        double number = 0.0;
        try {
            number = Double.parseDouble(text);
        } catch (Exception e) {
            number = 0.0;
        }
        return df.format(number);
    }

}
