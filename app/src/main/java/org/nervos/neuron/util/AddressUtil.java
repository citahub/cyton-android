package org.nervos.neuron.util;

import org.web3j.crypto.Keys;

public class AddressUtil {

    public static boolean isAddressValid(String address) {
        String regex = "^0x[a-f0-9]{40}$";
        if (address.matches(regex)) {
            return true;
        } else {
            return Keys.toChecksumAddress(address).equals(address);
        }
    }

}
