package com.cryptape.cita_wallet.item;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by BaojunCZ on 2018/10/9.
 */
public class EthErc20Token implements Parcelable {
    public String symbol;
    public String address;
    @SerializedName("overview")
    public OverView overView;
    public String email;
    public String website;
    @SerializedName("published_on")
    public String publishedOn;
    public Links links;

    public class OverView implements Parcelable {
        public String en, zh;

        protected OverView(Parcel in) {
            en = in.readString();
            zh = in.readString();
        }

        public final Creator<OverView> CREATOR = new Creator<OverView>() {
            @Override
            public OverView createFromParcel(Parcel in) {
                return new OverView(in);
            }

            @Override
            public OverView[] newArray(int size) {
                return new OverView[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(en);
            parcel.writeString(zh);
        }
    }

    public class Links implements Parcelable {
        public String blog;
        public String twitter;
        public String telegram;
        public String github;
        public String facebook;

        protected Links(Parcel in) {
            blog = in.readString();
            twitter = in.readString();
            telegram = in.readString();
            github = in.readString();
            facebook = in.readString();
        }

        public final Creator<Links> CREATOR = new Creator<Links>() {
            @Override
            public Links createFromParcel(Parcel in) {
                return new Links(in);
            }

            @Override
            public Links[] newArray(int size) {
                return new Links[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(blog);
            parcel.writeString(twitter);
            parcel.writeString(telegram);
            parcel.writeString(github);
            parcel.writeString(facebook);
        }
    }

    protected EthErc20Token(Parcel in) {
        symbol = in.readString();
        address = in.readString();
        overView = in.readParcelable(OverView.class.getClassLoader());
        email = in.readString();
        website = in.readString();
        publishedOn = in.readString();
        links = in.readParcelable(Links.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(symbol);
        dest.writeString(address);
        dest.writeParcelable(overView, flags);
        dest.writeString(email);
        dest.writeString(website);
        dest.writeString(publishedOn);
        dest.writeParcelable(links, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EthErc20Token> CREATOR = new Creator<EthErc20Token>() {
        @Override
        public EthErc20Token createFromParcel(Parcel in) {
            return new EthErc20Token(in);
        }

        @Override
        public EthErc20Token[] newArray(int size) {
            return new EthErc20Token[size];
        }
    };
}
