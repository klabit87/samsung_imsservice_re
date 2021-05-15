package com.sec.internal.ims.servicemodules.euc.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.sec.ims.util.ImsUri;

public final class EucMessageKey implements Parcelable {
    public static final Parcelable.Creator<EucMessageKey> CREATOR = new Parcelable.Creator<EucMessageKey>() {
        public EucMessageKey createFromParcel(Parcel in) {
            return new EucMessageKey(in);
        }

        public EucMessageKey[] newArray(int size) {
            return new EucMessageKey[size];
        }
    };
    private String mEucId;
    private EucType mEucType;
    private String mOwnIdentity;
    private ImsUri mRemoteUri;

    public String getEucId() {
        return this.mEucId;
    }

    public String getOwnIdentity() {
        return this.mOwnIdentity;
    }

    public EucType getEucType() {
        return this.mEucType;
    }

    public ImsUri getRemoteUri() {
        return this.mRemoteUri;
    }

    public EucMessageKey(String eucId, String ownIdentity, EucType eucType, ImsUri remoteUri) {
        this.mEucId = eucId;
        this.mOwnIdentity = ownIdentity;
        this.mEucType = eucType;
        this.mRemoteUri = remoteUri;
    }

    public EucMessageKey(Parcel in) {
        this.mEucId = in.readString();
        this.mOwnIdentity = in.readString();
        this.mEucType = (EucType) in.readSerializable();
        this.mRemoteUri = (ImsUri) in.readTypedObject(ImsUri.CREATOR);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EucMessageKey that = (EucMessageKey) o;
        if (!this.mEucId.equals(that.mEucId) || !this.mOwnIdentity.equals(that.mOwnIdentity) || this.mEucType != that.mEucType || !this.mRemoteUri.equals(that.mRemoteUri)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((((this.mEucId.hashCode() * 31) + this.mOwnIdentity.hashCode()) * 31) + this.mEucType.hashCode()) * 31) + this.mRemoteUri.hashCode();
    }

    public String toString() {
        return "EucMessageKey[mEucId='" + this.mEucId + '\'' + ", mOwnIdentity='" + this.mOwnIdentity + '\'' + ", mEucType=" + this.mEucType + ", mRemoteUri='" + this.mRemoteUri + '\'' + ']';
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mEucId);
        dest.writeString(this.mOwnIdentity);
        dest.writeSerializable(this.mEucType);
        dest.writeTypedObject(this.mRemoteUri, flags);
    }

    public byte[] marshall() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        byte[] marshalledEucMessageKey = parcel.marshall();
        parcel.recycle();
        return marshalledEucMessageKey;
    }

    public static EucMessageKey unmarshall(byte[] marshalledEucMessageKey) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(marshalledEucMessageKey, 0, marshalledEucMessageKey.length);
        parcel.setDataPosition(0);
        EucMessageKey eucMessageKey = CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return eucMessageKey;
    }
}
