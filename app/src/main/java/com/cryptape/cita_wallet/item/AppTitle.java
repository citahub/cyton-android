package com.cryptape.cita_wallet.item;

import android.os.Parcel;
import android.os.Parcelable;

public class AppTitle implements Parcelable {


    /**
     * title : {"name":"DApp","backgroundColor":"AAFFFFFF"}
     * left : {"name":"back","action":"jsFunction"}
     * right : {"isShow":true,"action":"jsFunction","type":"menu"}
     */

    public Title title;
    public Left left;
    public Right right;

    public static class Title implements Parcelable {
        /**
         * name : DApp
         * backgroundColor : AAFFFFFF
         */

        public String name;
        public String backgroundColor;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.backgroundColor);
        }

        public Title() {
        }

        protected Title(Parcel in) {
            this.name = in.readString();
            this.backgroundColor = in.readString();
        }

        public static final Creator<Title> CREATOR = new Creator<Title>() {
            @Override
            public Title createFromParcel(Parcel source) {
                return new Title(source);
            }

            @Override
            public Title[] newArray(int size) {
                return new Title[size];
            }
        };
    }


    public static String ACTION_BACK  = "back";
    public static String ACTION_CLOSE = "close";
    public static class Left implements Parcelable {
        /**
         * name : back
         * action : jsFunction
         */

        public String type;
        public String action;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.type);
            dest.writeString(this.action);
        }

        public Left() {
        }

        protected Left(Parcel in) {
            this.type = in.readString();
            this.action = in.readString();
        }

        public static final Creator<Left> CREATOR = new Creator<Left>() {
            @Override
            public Left createFromParcel(Parcel source) {
                return new Left(source);
            }

            @Override
            public Left[] newArray(int size) {
                return new Left[size];
            }
        };
    }


    public static String ACTION_MENU  = "menu";
    public static String ACTION_SHARE = "share";
    public static class Right implements Parcelable {
        /**
         * isShow : true
         * action : jsFunction
         * type : menu
         */

        public boolean isShow;
        public String action;
        public String type;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(this.isShow ? (byte) 1 : (byte) 0);
            dest.writeString(this.action);
            dest.writeString(this.type);
        }

        public Right() {
        }

        protected Right(Parcel in) {
            this.isShow = in.readByte() != 0;
            this.action = in.readString();
            this.type = in.readString();
        }

        public static final Creator<Right> CREATOR = new Creator<Right>() {
            @Override
            public Right createFromParcel(Parcel source) {
                return new Right(source);
            }

            @Override
            public Right[] newArray(int size) {
                return new Right[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.title, flags);
        dest.writeParcelable(this.left, flags);
        dest.writeParcelable(this.right, flags);
    }

    public AppTitle() {
    }

    protected AppTitle(Parcel in) {
        this.title = in.readParcelable(Title.class.getClassLoader());
        this.left = in.readParcelable(Left.class.getClassLoader());
        this.right = in.readParcelable(Right.class.getClassLoader());
    }

    public static final Creator<AppTitle> CREATOR = new Creator<AppTitle>() {
        @Override
        public AppTitle createFromParcel(Parcel source) {
            return new AppTitle(source);
        }

        @Override
        public AppTitle[] newArray(int size) {
            return new AppTitle[size];
        }
    };

}
