package com.cryptape.cita_wallet.util;

import android.content.Context;

import com.google.gson.Gson;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.item.Currency;
import com.cryptape.cita_wallet.item.CurrencyList;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;

import java.math.BigDecimal;
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
        if (NumberUtil.checkDecimal8(new BigDecimal(text).doubleValue())) {
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
