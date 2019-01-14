package com.cryptape.cita_wallet.item;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Collection implements Parcelable {

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
    @SerializedName("num_sales")
    public int numSales;
    public List<String> auctions;
    public List<TraitEntity> traits;


    public static class AssetContractEntity implements Parcelable {
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

        public static class DisplayDataEntity implements Parcelable {
            public List<String> images;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeStringList(this.images);
            }

            public DisplayDataEntity() {
            }

            protected DisplayDataEntity(Parcel in) {
                this.images = in.createStringArrayList();
            }

            public static final Creator<DisplayDataEntity> CREATOR = new Creator<DisplayDataEntity>() {
                @Override
                public DisplayDataEntity createFromParcel(Parcel source) {
                    return new DisplayDataEntity(source);
                }

                @Override
                public DisplayDataEntity[] newArray(int size) {
                    return new DisplayDataEntity[size];
                }
            };
        }

        public AssetContractEntity() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.address);
            dest.writeString(this.name);
            dest.writeString(this.symbol);
            dest.writeString(this.imageUrl);
            dest.writeString(this.featuredImageUrl);
            dest.writeByte(this.featured ? (byte) 1 : (byte) 0);
            dest.writeString(this.description);
            dest.writeString(this.externalLink);
            dest.writeString(this.wikiLink);
            dest.writeString(this.stats);
            dest.writeString(this.traits);
            dest.writeByte(this.hidden ? (byte) 1 : (byte) 0);
            dest.writeString(this.nftVersion);
            dest.writeString(this.schemaName);
            dest.writeParcelable(this.displayData, flags);
            dest.writeString(this.shortDescription);
            dest.writeInt(this.totalSupply);
            dest.writeInt(this.buyerFeeBasisPoints);
            dest.writeInt(this.sellerFeeBasisPoints);
        }

        protected AssetContractEntity(Parcel in) {
            this.address = in.readString();
            this.name = in.readString();
            this.symbol = in.readString();
            this.imageUrl = in.readString();
            this.featuredImageUrl = in.readString();
            this.featured = in.readByte() != 0;
            this.description = in.readString();
            this.externalLink = in.readString();
            this.wikiLink = in.readString();
            this.stats = in.readString();
            this.traits = in.readString();
            this.hidden = in.readByte() != 0;
            this.nftVersion = in.readString();
            this.schemaName = in.readString();
            this.displayData = in.readParcelable(DisplayDataEntity.class.getClassLoader());
            this.shortDescription = in.readString();
            this.totalSupply = in.readInt();
            this.buyerFeeBasisPoints = in.readInt();
            this.sellerFeeBasisPoints = in.readInt();
        }

        public static final Creator<AssetContractEntity> CREATOR = new Creator<AssetContractEntity>() {
            @Override
            public AssetContractEntity createFromParcel(Parcel source) {
                return new AssetContractEntity(source);
            }

            @Override
            public AssetContractEntity[] newArray(int size) {
                return new AssetContractEntity[size];
            }
        };
    }

    public static class OwnerEntity implements Parcelable {
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

        public static class UserEntity implements Parcelable {
            /**
             * username : fincho
             */

            public String username;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.username);
            }

            public UserEntity() {
            }

            protected UserEntity(Parcel in) {
                this.username = in.readString();
            }

            public static final Creator<UserEntity> CREATOR = new Creator<UserEntity>() {
                @Override
                public UserEntity createFromParcel(Parcel source) {
                    return new UserEntity(source);
                }

                @Override
                public UserEntity[] newArray(int size) {
                    return new UserEntity[size];
                }
            };
        }

        public OwnerEntity() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.user, flags);
            dest.writeString(this.profileImgUrl);
            dest.writeString(this.address);
            dest.writeString(this.config);
        }

        protected OwnerEntity(Parcel in) {
            this.user = in.readParcelable(UserEntity.class.getClassLoader());
            this.profileImgUrl = in.readString();
            this.address = in.readString();
            this.config = in.readString();
        }

        public static final Creator<OwnerEntity> CREATOR = new Creator<OwnerEntity>() {
            @Override
            public OwnerEntity createFromParcel(Parcel source) {
                return new OwnerEntity(source);
            }

            @Override
            public OwnerEntity[] newArray(int size) {
                return new OwnerEntity[size];
            }
        };
    }

    public static class TraitEntity implements Parcelable {
        @SerializedName("trait_type")
        public String traitType;
        public String value;
        @SerializedName("display_type")
        public String displayType;
        @SerializedName("max_value")
        public String maxValue;
        @SerializedName("trait_count")
        public String traitCount;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.traitType);
            dest.writeString(this.value);
            dest.writeString(this.displayType);
            dest.writeString(this.maxValue);
            dest.writeString(this.traitCount);
        }

        public TraitEntity() {
        }

        protected TraitEntity(Parcel in) {
            this.traitType = in.readString();
            this.value = in.readString();
            this.displayType = in.readString();
            this.maxValue = in.readString();
            this.traitCount = in.readString();
        }

        public static final Creator<TraitEntity> CREATOR = new Creator<TraitEntity>() {
            @Override
            public TraitEntity createFromParcel(Parcel source) {
                return new TraitEntity(source);
            }

            @Override
            public TraitEntity[] newArray(int size) {
                return new TraitEntity[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tokenId);
        dest.writeString(this.imageUrl);
        dest.writeString(this.imagePreviewUrl);
        dest.writeString(this.backgroundColor);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.externalLink);
        dest.writeParcelable(this.assetContract, flags);
        dest.writeParcelable(this.owner, flags);
        dest.writeInt(this.numSales);
        dest.writeStringList(this.auctions);
        dest.writeList(this.traits);
    }

    public Collection() {
    }

    protected Collection(Parcel in) {
        this.tokenId = in.readString();
        this.imageUrl = in.readString();
        this.imagePreviewUrl = in.readString();
        this.backgroundColor = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.externalLink = in.readString();
        this.assetContract = in.readParcelable(AssetContractEntity.class.getClassLoader());
        this.owner = in.readParcelable(OwnerEntity.class.getClassLoader());
        this.numSales = in.readInt();
        this.auctions = in.createStringArrayList();
        this.traits = new ArrayList<TraitEntity>();
        in.readList(this.traits, TraitEntity.class.getClassLoader());
    }

    public static final Parcelable.Creator<Collection> CREATOR = new Parcelable.Creator<Collection>() {
        @Override
        public Collection createFromParcel(Parcel source) {
            return new Collection(source);
        }

        @Override
        public Collection[] newArray(int size) {
            return new Collection[size];
        }
    };
}
