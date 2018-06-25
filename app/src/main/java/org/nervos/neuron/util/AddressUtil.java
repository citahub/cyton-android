package org.nervos.neuron.util;

public class AddressUtil {

    public static boolean isAddressValid(String address) {
        String regex = "^0x[a-fA-F0-9]{40}$";
        return address.matches(regex);
    }

}
