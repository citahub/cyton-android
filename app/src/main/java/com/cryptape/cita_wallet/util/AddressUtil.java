package com.cryptape.cita_wallet.util;

import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

public class AddressUtil {

    public static boolean isAddressValid(String address) {
        if (address.length() != 40 && address.length() != 42) return false;
        address = Numeric.prependHexPrefix(address);
        String smallRegex = "^0x[a-f0-9]{40}$";
        String mixRegex = "^0x[a-fA-F0-9]{40}$";
        if (address.matches(smallRegex)) {
            return true;
        } else {
            return address.matches(mixRegex) && Keys.toChecksumAddress(address).equals(address);
        }
    }

}
