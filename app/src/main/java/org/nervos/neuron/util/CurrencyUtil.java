package org.nervos.neuron.util;

import android.content.Context;

import com.google.gson.Gson;

import org.nervos.neuron.R;
import org.nervos.neuron.item.Currency;
import org.nervos.neuron.item.CurrencyList;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.text.DecimalFormat;
import java.util.List;

public class CurrencyUtil {

    public static List<Currency> getCurrencyList(Context context) {
        String data = StreamUtils.get(context, R.raw.currency);
        Gson gson = new Gson();
        return gson.fromJson(data, CurrencyList.class).getCurrency();
    }

    public static Currency getCurrencyItem(Context context) {
        Currency currency = null;
        List<Currency> list = getCurrencyList(context);
        String currencyName = SharePrefUtil.getString(ConstantUtil.CURRENCY, ConstantUtil.DEFAULT_CURRENCY);
        for (Currency item : list) {
            if (item.getName().equals(currencyName)) {
                currency = item;
                break;
            }
        }
        if (currency == null) {
            currency = list.get(0);
        }
        return currency;
    }

    public static String formatCurrency(Double currency) {
        DecimalFormat df = new DecimalFormat("######0.00");
        return fmtMicrometer(df.format(currency));
    }

    public static String fmtMicrometer(String text) {
        if(text.contains("E")||text.contains("2")){
            return text;
        }
        DecimalFormat df = new DecimalFormat("###,##0.########");
        double number;
        try {
            number = Double.parseDouble(text);
        } catch (Exception e) {
            number = 0.0;
        }
        return df.format(number);
    }

}
