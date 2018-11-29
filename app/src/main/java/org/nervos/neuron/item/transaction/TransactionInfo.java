package org.nervos.neuron.item.transaction;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.exception.TransactionFormatException;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TransactionInfo implements Parcelable {

    private static final String TYPE_ETH = "ETH";
    private static final String TYPE_APPCHAIN = "AppChain";

    public String from;
    public String to;
    public String nonce;
    private String quota;
    private String validUntilBlock;
    public String data;
    private String value;
    public String chainId;
    public int version;
    public String gasLimit;
    public String gasPrice;
    public String chainType;

    public TransactionInfo(String to, String value) {
        this.to = to;
        this.value = NumberUtil.getWeiFromEth(value).toString();
    }

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
        if (Numeric.containsHexPrefix(value)) {
            return toBigInt(value);
        }
        return BigInteger.ZERO;
    }

    public BigInteger getValidUntilBlock() {
        if (NumberUtil.isNumeric(validUntilBlock)) {
            return new BigInteger(validUntilBlock);
        }
        if (Numeric.containsHexPrefix(validUntilBlock)) {
            return toBigInt(validUntilBlock);
        }
        return BigInteger.ZERO;
    }

    public BigInteger getQuota() {
        if (NumberUtil.isNumeric(quota)) {
            return new BigInteger(quota);
        }
        if (Numeric.containsHexPrefix(quota)) {
            return toBigInt(quota);
        }
        return BigInteger.ZERO;
    }

    public double getDoubleQuota() {
        return NumberUtil.getEthFromWeiForDouble(getQuota().toString(16));
    }


    public double getGas() {
        BigInteger limitBig = BigInteger.ZERO;
        if (NumberUtil.isNumeric(gasLimit)) {
            limitBig = new BigInteger(gasLimit);
        }
        if (Numeric.containsHexPrefix(gasLimit)) {
            limitBig = toBigInt(gasLimit);
        }

        BigInteger priceBig = BigInteger.ZERO;
        if (NumberUtil.isNumeric(gasPrice)) {
            priceBig = new BigInteger(gasPrice);
        }
        if (Numeric.containsHexPrefix(gasPrice)) {
            priceBig = toBigInt(gasPrice);
        }
        return NumberUtil.getEthFromWei(limitBig.multiply(priceBig));
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
        checkNumberFormat(quota, "quota", false);
        checkNumberFormat(validUntilBlock, "ValidUntilBlock");
        checkNumberFormat(gasPrice, "GasPrice");
        checkNumberFormat(gasLimit, "GasLimit");
    }

    private void checkNumberFormat(String str, String strName) throws TransactionFormatException {
        checkNumberFormat(str, strName, true);
    }

    private static final String TRANSACTION_FORMAT_ERROR = "Transaction's %s format error";
    private void checkNumberFormat(String str, String strName, boolean allowNull) throws TransactionFormatException {
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
        } else if (!allowNull) {
            throw new TransactionFormatException(String.format(TRANSACTION_FORMAT_ERROR, strName));
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
        dest.writeString(this.data);
        dest.writeString(this.value);
        dest.writeString(this.chainId);
        dest.writeInt(this.version);
        dest.writeString(this.gasLimit);
        dest.writeString(this.gasPrice);
    }

    protected TransactionInfo(Parcel in) {
        this.from = in.readString();
        this.to = in.readString();
        this.nonce = in.readString();
        this.quota = in.readString();
        this.data = in.readString();
        this.value = in.readString();
        this.chainId = in.readString();
        this.version = in.readInt();
        this.gasLimit = in.readString();
        this.gasPrice = in.readString();
    }

    public static final Creator<TransactionInfo> CREATOR = new Creator<TransactionInfo>() {
        @Override
        public TransactionInfo createFromParcel(Parcel source) {
            return new TransactionInfo(source);
        }

        @Override
        public TransactionInfo[] newArray(int size) {
            return new TransactionInfo[size];
        }
    };
}
