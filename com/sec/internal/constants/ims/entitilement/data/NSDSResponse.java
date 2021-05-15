package com.sec.internal.constants.ims.entitilement.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class NSDSResponse implements Parcelable {
    @SerializedName("message-id")
    public int messageId;
    public String method;
    @SerializedName("response-code")
    public int responseCode;

    public NSDSResponse(Parcel in) {
        this.messageId = in.readInt();
        this.responseCode = in.readInt();
        this.method = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.messageId);
        dest.writeInt(this.responseCode);
        dest.writeString(this.method);
    }
}
