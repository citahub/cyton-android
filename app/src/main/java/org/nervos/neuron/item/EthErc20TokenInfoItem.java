package org.nervos.neuron.item;

/**
 * Created by BaojunCZ on 2018/10/9.
 */
public class EthErc20TokenInfoItem {
    public String symbol;
    public String address;
    public overview overview;
    public String email;
    public String website;
    public String published_on;
    public links links;

    public class overview {
        public String en, zh;
    }

    public class links {
        public String blog;
        public String twitter;
        public String telegram;
        public String github;
        public String facebook;
    }

}
