package com.samsung.android.cmcnsd.network;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SemSystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NsdNetwork implements Parcelable {
    public static final Parcelable.Creator<NsdNetwork> CREATOR = new Parcelable.Creator<NsdNetwork>() {
        public NsdNetwork createFromParcel(Parcel parcel) {
            return new NsdNetwork(parcel);
        }

        public NsdNetwork[] newArray(int i) {
            return new NsdNetwork[i];
        }
    };
    public static final String TAG = NsdNetwork.class.getSimpleName();
    public static final boolean USER_BINARY = "user".equals(SemSystemProperties.get("ro.build.type", "user"));
    public String mAuthenticationToken;
    public NsdNetworkCapabilities mCapabilities;
    public String mHostAddress;
    public final ArrayList<String> mInterfaceNameList = new ArrayList<>();
    public boolean mIsConnected;

    public int describeContents() {
        return 0;
    }

    public NsdNetwork() {
    }

    public NsdNetwork(Parcel parcel) {
        readFromParcel(parcel);
    }

    public boolean isConnected() {
        return this.mIsConnected;
    }

    public int getTransport() {
        return this.mCapabilities.getTransport();
    }

    public boolean hasTransport(int i) {
        return this.mCapabilities.hasTransport(i);
    }

    public String getHostAddress() {
        return this.mHostAddress;
    }

    public final String getAuthenticationToken() {
        return this.mAuthenticationToken;
    }

    public final NsdNetworkCapabilities getCapabilities() {
        return this.mCapabilities;
    }

    public final ArrayList<String> getInterfaceNameList() {
        return this.mInterfaceNameList;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NsdNetwork)) {
            return false;
        }
        NsdNetwork nsdNetwork = (NsdNetwork) obj;
        if (this.mIsConnected != nsdNetwork.mIsConnected || !Objects.equals(this.mHostAddress, nsdNetwork.mHostAddress) || !Objects.equals(this.mAuthenticationToken, nsdNetwork.mAuthenticationToken) || !this.mCapabilities.equals(nsdNetwork.mCapabilities) || this.mInterfaceNameList.size() != nsdNetwork.mInterfaceNameList.size() || !this.mInterfaceNameList.containsAll(nsdNetwork.mInterfaceNameList)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Boolean.valueOf(this.mIsConnected), this.mHostAddress, this.mAuthenticationToken, this.mCapabilities, this.mInterfaceNameList});
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(hashCode());
        sb.append(" {isConnected=");
        sb.append(this.mIsConnected);
        sb.append(" cap=");
        sb.append(this.mCapabilities);
        sb.append(" hostAddress=[");
        sb.append(this.mHostAddress);
        sb.append("]");
        sb.append(" infNames=");
        sb.append(this.mInterfaceNameList);
        if (!USER_BINARY || (str = this.mAuthenticationToken) == null || str.isEmpty()) {
            sb.append(" token=[");
            sb.append(this.mAuthenticationToken);
            sb.append("]}");
        } else {
            sb.append(" token=[");
            String str2 = this.mAuthenticationToken;
            sb.append(str2.substring(0, str2.length() / 3));
            sb.append("xxx");
            sb.append("]}");
        }
        return sb.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        if (parcel == null) {
            Log.e(TAG, "failed to write to Parcel. out is null");
            return;
        }
        parcel.writeBoolean(this.mIsConnected);
        parcel.writeString(this.mHostAddress);
        parcel.writeString(this.mAuthenticationToken);
        parcel.writeParcelable(this.mCapabilities, 0);
        parcel.writeStringList(this.mInterfaceNameList);
    }

    private void readFromParcel(Parcel parcel) {
        this.mIsConnected = parcel.readBoolean();
        this.mHostAddress = parcel.readString();
        this.mAuthenticationToken = parcel.readString();
        this.mCapabilities = (NsdNetworkCapabilities) parcel.readParcelable(NsdNetworkCapabilities.class.getClassLoader());
        this.mInterfaceNameList.addAll(parcel.createStringArrayList());
    }

    public static class Builder {
        public String mAuthenticationToken;
        public NsdNetworkCapabilities mCapabilities;
        public String mHostAddress;
        public final ArrayList<String> mInterfaceNameList = new ArrayList<>();
        public boolean mIsConnected;

        public Builder setConnected(boolean z) {
            this.mIsConnected = z;
            return this;
        }

        public Builder setHostAddress(String str) {
            this.mHostAddress = str;
            return this;
        }

        public Builder setAuthenticationToken(String str) {
            this.mAuthenticationToken = str;
            return this;
        }

        public Builder setInterfaceNameList(List<String> list) {
            if (list != null) {
                this.mInterfaceNameList.addAll(list);
                Collections.sort(this.mInterfaceNameList);
            }
            return this;
        }

        public Builder setCapabilities(NsdNetworkCapabilities nsdNetworkCapabilities) {
            this.mCapabilities = nsdNetworkCapabilities;
            return this;
        }

        public NsdNetwork build() {
            NsdNetwork nsdNetwork = new NsdNetwork();
            boolean unused = nsdNetwork.mIsConnected = this.mIsConnected;
            String unused2 = nsdNetwork.mHostAddress = this.mHostAddress;
            String unused3 = nsdNetwork.mAuthenticationToken = this.mAuthenticationToken;
            NsdNetworkCapabilities unused4 = nsdNetwork.mCapabilities = this.mCapabilities;
            nsdNetwork.mInterfaceNameList.addAll(this.mInterfaceNameList);
            return nsdNetwork;
        }
    }
}
