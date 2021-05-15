package com.sec.internal.helper.httpclient;

import android.os.Parcel;
import android.os.Parcelable;
import java.net.HttpCookie;

public class HttpCookieParcelable implements Parcelable {
    public static final Parcelable.Creator<HttpCookieParcelable> CREATOR = new Parcelable.Creator<HttpCookieParcelable>() {
        public HttpCookieParcelable[] newArray(int size) {
            return new HttpCookieParcelable[size];
        }

        public HttpCookieParcelable createFromParcel(Parcel source) {
            return new HttpCookieParcelable(source);
        }
    };
    private HttpCookie cookie;

    public HttpCookieParcelable(HttpCookie cookie2) {
        this.cookie = cookie2;
    }

    public HttpCookieParcelable(Parcel source) {
        HttpCookie httpCookie = new HttpCookie(source.readString(), source.readString());
        this.cookie = httpCookie;
        httpCookie.setComment(source.readString());
        this.cookie.setCommentURL(source.readString());
        boolean z = true;
        this.cookie.setDiscard(source.readByte() != 0);
        this.cookie.setDomain(source.readString());
        this.cookie.setMaxAge(source.readLong());
        this.cookie.setPath(source.readString());
        this.cookie.setPortlist(source.readString());
        this.cookie.setSecure(source.readByte() == 0 ? false : z);
        this.cookie.setVersion(source.readInt());
    }

    public HttpCookie getCookie() {
        return this.cookie;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.cookie.getName());
        dest.writeString(this.cookie.getValue());
        dest.writeString(this.cookie.getComment());
        dest.writeString(this.cookie.getCommentURL());
        dest.writeByte(this.cookie.getDiscard() ? (byte) 1 : 0);
        dest.writeString(this.cookie.getDomain());
        dest.writeLong(this.cookie.getMaxAge());
        dest.writeString(this.cookie.getPath());
        dest.writeString(this.cookie.getPortlist());
        dest.writeByte(this.cookie.getSecure() ? (byte) 1 : 0);
        dest.writeInt(this.cookie.getVersion());
    }
}
