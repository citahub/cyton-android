package com.cryptape.cita_wallet.item.transaction;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.exception.TransactionFormatException;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class AppTransaction implements Parcelable {

    private static final String TYPE_ETH = "ETH";
    private static final String TYPE_CITA = "CITA";

    public String from;
    public String to;
    public String nonce;
    private String quota;
    private String validUntilBlock;
    public String data;
    private String value;
    public String chainId;
    public int version;
    private String gasLimit;
    private String gasPrice;
    public String chainType;

    public AppTransaction(String to, String value) {
        this.to = to;
        this.value = NumberUtil.getWeiFromEth(value).toString();
    }

    public AppTransaction() {}

    public String getStringValue() {
        return NumberUtil.getEthFromWeiForString(getBigIntegerValue().toString(16));
    }

    public double getDoubleValue() {
        return NumberUtil.getEthFromWeiForDouble(getBigIntegerValue().toString(16));
    }

    public BigInteger getBigIntegerValue() {
        if (NumberUtil.isNumeric(value)) {
            return new BigInteger(value);
        }
        if (!TextUtils.isEmpty(value) && NumberUtil.isHex(value)) {
            return toBigInt(value);
        }
        return BigInteger.ZERO;
    }

    public BigInteger getValidUntilBlock() {
        if (NumberUtil.isNumeric(validUntilBlock)) {
            return new BigInteger(validUntilBlock);
        }
        if (NumberUtil.isHex(validUntilBlock)) {
            return toBigInt(validUntilBlock);
        }
        return BigInteger.ZERO;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public BigInteger getQuota() {
        if (NumberUtil.isNumeric(quota)) {
            return new BigInteger(quota);
        }
        if (!TextUtils.isEmpty(quota) && NumberUtil.isHex(quota)) {
            return toBigInt(quota);
        }
        return BigInteger.ZERO;
    }

    public double getDoubleQuota() {
        return NumberUtil.getEthFromWeiForDouble(getQuota().toString(16));
    }

    public BigInteger getGasLimit() {
        if (!TextUtils.isEmpty(gasLimit)) {
            if (NumberUtil.isNumeric(gasLimit)) {
                return new BigInteger(gasLimit);
            }
            if (!TextUtils.isEmpty(gasLimit) && NumberUtil.isHex(gasLimit)) {
                return toBigInt(gasLimit);
            }
        }
        return BigInteger.ZERO;
    }

    public void setGasLimit(BigInteger limit) {
        gasLimit = limit.toString();
    }

    public BigInteger getGasPrice() {
        if (NumberUtil.isNumeric(gasPrice)) {
            return new BigInteger(gasPrice);
        }
        if (!TextUtils.isEmpty(gasPrice) && NumberUtil.isHex(gasPrice)) {
            return toBigInt(gasPrice);
        }
        return BigInteger.ZERO;
    }

    public void setGasPrice(BigInteger price) {
        gasPrice = price.toString();
    }

    public double getGas() {
        return NumberUtil.getEthFromWei(getGasLimit().multiply(getGasPrice()));
    }

    public boolean isEthereum() {
        return !TextUtils.isEmpty(chainType) && TYPE_ETH.equals(chainType);
    }

    private BigInteger toBigInt(String hex) {
        try {
            return Numeric.toBigInt(hex);
        } catch (Exception e) {
            e.printStackTrace();
            return BigInteger.ZERO;
        }
    }

    public void checkTransactionFormat() throws TransactionFormatException {
        checkNumberFormat(value, "value");
        checkNumberFormat(quota, "quota");
        checkNumberFormat(validUntilBlock, "ValidUntilBlock");
        checkNumberFormat(gasPrice, "GasPrice");
        checkNumberFormat(gasLimit, "GasLimit");
    }

    private static final String TRANSACTION_FORMAT_ERROR = "Transaction's %s format error";
    private void checkNumberFormat(String str, String strName) throws TransactionFormatException {
        if (!TextUtils.isEmpty(str)) {
            if (!NumberUtil.isNumeric(str) && !Numeric.containsHexPrefix(str)) {
                throw new TransactionFormatException(String.format(TRANSACTION_FORMAT_ERROR, strName));
            } else if (Numeric.containsHexPrefix(str)) {
                try {
                    Numeric.toBigInt(str);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new TransactionFormatException(String.format(TRANSACTION_FORMAT_ERROR, strName));
                }
            }
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.from);
        dest.writeString(this.to);
        dest.writeString(this.nonce);
        dest.writeString(this.quota);
        dest.writeString(this.validUntilBlock);
        dest.writeString(this.data);
        dest.writeString(this.value);
        dest.writeString(this.chainId);
        dest.writeInt(this.version);
        dest.writeString(this.gasLimit);
        dest.writeString(this.gasPrice);
        dest.writeString(this.chainType);
    }

    protected AppTransaction(Parcel in) {
        this.from = in.readString();
        this.to = in.readString();
        this.nonce = in.readString();
        this.quota = in.readString();
        this.validUntilBlock = in.readString();
        this.data = in.readString();
        this.value = in.readString();
        this.chainId = in.readString();
        this.version = in.readInt();
        this.gasLimit = in.readString();
        this.gasPrice = in.readString();
        this.chainType = in.readString();
    }

    public static final Creator<AppTransaction> CREATOR = new Creator<AppTransaction>() {
        @Override
        public AppTransaction createFromParcel(Parcel source) {
            return new AppTransaction(source);
        }

        @Override
        public AppTransaction[] newArray(int size) {
            return new AppTransaction[size];
        }
    };
}
