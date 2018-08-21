package org.nervos.neuron.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CollectionItem implements Serializable {
    private static final long serialVersionUID = -7060210544600464481L;

    /**
     * token_id : 209
     * image_url : https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe/209.png
     * image_preview_url : https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe/209.png
     * background_color : 0xffffff
     * name : Digital Art 209
     * description : Digital Art 209
     * external_link :
     * asset_contract : {"address":"0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe","name":"DigitalArtChain","symbol":"DAC","image_url":"https://storage.googleapis.com/opensea-static/digitalartchain-logo.png","featured_image_url":"https://storage.googleapis.com/opensea-static/Category-Thumb-DigitalArtChain.png","featured":false,"description":"Publish your own digital art on the blockchain. Digital Art is published on IPFS and associated to Ethereum ERC721 token which enables you own, sell, and purchase published Digital Art.","external_link":"http://digitalartchain.com/","wiki_link":null,"stats":null,"traits":null,"hidden":false,"nft_version":"3.0","schema_name":"ERC721","display_data":{"images":["https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F33","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F29","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F36","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F32","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F31","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F25"]},"short_description":"ERC721","total_supply":10000,"buyer_fee_basis_points":0,"seller_fee_basis_points":125}
     * owner : {"user":{"username":"fincho"},"profile_img_url":"https://storage.googleapis.com/opensea-static/opensea-profile/18.png","address":"0x0239769a1adf4def9f07da824b80b9c4fcb59593","config":""}
     * auctions : []
     * traits : []
     * last_sale : null
     * num_sales : 0
     */

    @SerializedName("token_id")
    public String tokenId;
    @SerializedName("image_url")
    public String imageUrl;
    @SerializedName("image_preview_url")
    public String imagePreviewUrl;
    @SerializedName("background_color")
    public String backgroundColor;
    public String name;
    public String description;
    @SerializedName("external_link")
    public String externalLink;
    @SerializedName("asset_contract")
    public AssetContractEntity assetContract;
    public OwnerEntity owner;
    @SerializedName("last_sale")
    public Object lastSale;
    @SerializedName("num_sales")
    public int numSales;
    public List<String> auctions;
    public List<TraitEntity> traits;


    public static class AssetContractEntity implements Serializable {
        private static final long serialVersionUID = -7060210544600464481L;
        /**
         * address : 0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe
         * name : DigitalArtChain
         * symbol : DAC
         * image_url : https://storage.googleapis.com/opensea-static/digitalartchain-logo.png
         * featured_image_url : https://storage.googleapis.com/opensea-static/Category-Thumb-DigitalArtChain.png
         * featured : false
         * description : Publish your own digital art on the blockchain. Digital Art is published on IPFS and associated to Ethereum ERC721 token which enables you own, sell, and purchase published Digital Art.
         * external_link : http://digitalartchain.com/
         * wiki_link : null
         * stats : null
         * traits : null
         * hidden : false
         * nft_version : 3.0
         * schema_name : ERC721
         * display_data : {"images":["https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F33","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F29","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F36","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F32","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F31","https://storage.googleapis.com/opensea-prod.appspot.com/0x323a3e1693e7a0959f65972f3bf2dfcb93239dfe%2F25"]}
         * short_description : ERC721
         * total_supply : 10000
         * buyer_fee_basis_points : 0
         * seller_fee_basis_points : 125
         */

        public String address;
        public String name;
        public String symbol;
        @SerializedName("image_url")
        public String imageUrl;
        @SerializedName("featured_image_url")
        public String featuredImageUrl;
        public boolean featured;
        public String description;
        @SerializedName("external_link")
        public String externalLink;
        @SerializedName("wiki_link")
        public String wikiLink;
        public String stats;
        public String traits;
        public boolean hidden;
        @SerializedName("nft_version")
        public String nftVersion;
        @SerializedName("schema_name")
        public String schemaName;
        @SerializedName("display_data")
        public DisplayDataEntity displayData;
        @SerializedName("short_description")
        public String shortDescription;
        @SerializedName("total_supply")
        public int totalSupply;
        @SerializedName("buyer_fee_basis_points")
        public int buyerFeeBasisPoints;
        @SerializedName("seller_fee_basis_points")
        public int sellerFeeBasisPoints;

        public static class DisplayDataEntity implements Serializable {
            public List<String> images;
            private static final long serialVersionUID = -7060210544600464481L;
        }
    }

    public static class OwnerEntity implements Serializable {
        private static final long serialVersionUID = -7060210544600464481L;
        /**
         * user : {"username":"fincho"}
         * profile_img_url : https://storage.googleapis.com/opensea-static/opensea-profile/18.png
         * address : 0x0239769a1adf4def9f07da824b80b9c4fcb59593
         * config :
         */

        public UserEntity user;
        @SerializedName("profile_img_url")
        public String profileImgUrl;
        public String address;
        public String config;

        public static class UserEntity implements Serializable {
            private static final long serialVersionUID = -7060210544600464481L;
            /**
             * username : fincho
             */

            public String username;
        }
    }

    public static class TraitEntity implements Serializable {
        private static final long serialVersionUID = -7060210544600464481L;
        @SerializedName("trait_type")
        public String traitType;
        public String value;
        @SerializedName("display_type")
        public String displayType;
        @SerializedName("max_value")
        public String maxValue;
        @SerializedName("trait_count")
        public String traitCount;
    }
}
