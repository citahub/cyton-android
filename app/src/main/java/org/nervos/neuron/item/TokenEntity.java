package org.nervos.neuron.item;

public class TokenEntity {

    /**
     * symbol : $FFC
     * address : 0x4E84E9e5fb0A972628Cf4568c403167EF1D40431
     * decimals : 18
     * name : $Fluzcoin
     * ens_address :
     * website : https://fluzcoin.io/
     * logo : {"src":"https://i.imgur.com/ar18ECx.png","width":"358","height":"373","ipfs_hash":""}
     * support : {"email":"info@fluzcoin.io","url":"https://fluzcoin.io/"}
     * social : {"blog":"https://medium.com/@fluzcoin","chat":"","facebook":"https://www.facebook.com/fluzcoin/","forum":"https://bitcointalk.org/index.php?topic=3794410.0","github":"https://github.com/Fluzcoin","gitter":"","instagram":"https://www.instagram.com/fluzcoin.official/","linkedin":"https://www.linkedin.com/company/fluzcoin/","reddit":"https://www.reddit.com/r/fluzcoin/","slack":"","telegram":"https://t.me/Fluzcoin_Foundation","twitter":"https://twitter.com/fluzcoin","youtube":"https://www.youtube.com/channel/UCdK-HoZdmvmC-9bS5TeJT0g"}
     */

    public String symbol;
    public String address;
    public int decimals;
    public String name;
    public String ens_address;
    public String website;
    public LogoEntity logo;
    public SupportEntity support;
    public SocialEntity social;
    public boolean isSelected = false;

    public static class LogoEntity {
        /**
         * src : https://i.imgur.com/ar18ECx.png
         * width : 358
         * height : 373
         * ipfs_hash :
         */

        public String src;
        public String width;
        public String height;
        public String ipfs_hash;

    }

    public static class SupportEntity {
        /**
         * email : info@fluzcoin.io
         * url : https://fluzcoin.io/
         */

        public String email;
        public String url;

    }

    public static class SocialEntity {
        /**
         * blog : https://medium.com/@fluzcoin
         * chat :
         * facebook : https://www.facebook.com/fluzcoin/
         * forum : https://bitcointalk.org/index.php?topic=3794410.0
         * github : https://github.com/Fluzcoin
         * gitter :
         * instagram : https://www.instagram.com/fluzcoin.official/
         * linkedin : https://www.linkedin.com/company/fluzcoin/
         * reddit : https://www.reddit.com/r/fluzcoin/
         * slack :
         * telegram : https://t.me/Fluzcoin_Foundation
         * twitter : https://twitter.com/fluzcoin
         * youtube : https://www.youtube.com/channel/UCdK-HoZdmvmC-9bS5TeJT0g
         */

        public String blog;
        public String chat;
        public String facebook;
        public String forum;
        public String github;
        public String gitter;
        public String instagram;
        public String linkedin;
        public String reddit;
        public String slack;
        public String telegram;
        public String twitter;
        public String youtube;

    }
}
