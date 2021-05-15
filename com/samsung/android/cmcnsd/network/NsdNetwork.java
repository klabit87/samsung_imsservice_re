package com.samsung.android.cmcnsd.network;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SemSystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NsdNetwork implements Parcelable {
    public static final Parcelable.Creator<NsdNetwork> CREATOR = new Parcelable.Creator<NsdNetwork>() {
        public NsdNetwork createFromParcel(Parcel in) {
            return new NsdNetwork(in);
        }

        public NsdNetwork[] newArray(int size) {
            return new NsdNetwork[size];
        }
    };
    private static final String TAG = NsdNetwork.class.getSimpleName();
    private static boolean USER_BINARY = "user".equals(SemSystemProperties.get("ro.build.type", "user"));
    /* access modifiers changed from: private */
    public String mAuthenticationToken;
    /* access modifiers changed from: private */
    public NsdNetworkCapabilities mCapabilities;
    /* access modifiers changed from: private */
    public String mHostAddress;
    /* access modifiers changed from: private */
    public ArrayList<String> mInterfaceNameList;
    /* access modifiers changed from: private */
    public boolean mIsConnected;

    protected NsdNetwork() {
        this.mInterfaceNameList = new ArrayList<>();
    }

    protected NsdNetwork(Parcel in) {
        readFromParcel(in);
    }

    public boolean isConnected() {
        return this.mIsConnected;
    }

    public int getTransport() {
        return this.mCapabilities.getTransport();
    }

    public boolean hasTransport(int transport) {
        return this.mCapabilities.hasTransport(transport);
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
        NsdNetwork rhs = (NsdNetwork) obj;
        if (this.mIsConnected != rhs.mIsConnected || !Objects.equals(this.mHostAddress, rhs.mHostAddress) || !Objects.equals(this.mAuthenticationToken, rhs.mAuthenticationToken) || !this.mCapabilities.equals(rhs.mCapabilities)) {
            return false;
        }
        if (Objects.equals(this.mInterfaceNameList, rhs.mInterfaceNameList)) {
            return true;
        }
        String str = TAG;
        Log.e(str, "NotEquals " + this.mInterfaceNameList + " with " + rhs.mInterfaceNameList);
        return false;
    }

    public String toString() {
        String str;
        StringBuilder builder = new StringBuilder();
        builder.append(hashCode());
        builder.append(" {isConnected=");
        builder.append(this.mIsConnected);
        builder.append(" cap=");
        builder.append(this.mCapabilities);
        builder.append(" hostAddress=[");
        builder.append(this.mHostAddress);
        builder.append("]");
        builder.append(" infNames=");
        builder.append(this.mInterfaceNameList);
        if (!USER_BINARY || (str = this.mAuthenticationToken) == null || str.isEmpty()) {
            builder.append(" token=[");
            builder.append(this.mAuthenticationToken);
            builder.append("]}");
        } else {
            builder.append(" token=[");
            String str2 = this.mAuthenticationToken;
            builder.append(str2.substring(0, str2.length() / 3));
            builder.append("xxx");
            builder.append("]}");
        }
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (out == null) {
            Log.e(TAG, "failed to write to Parcel. out is null");
            return;
        }
        out.writeBoolean(this.mIsConnected);
        out.writeString(this.mHostAddress);
        out.writeString(this.mAuthenticationToken);
        out.writeParcelable(this.mCapabilities, 0);
        out.writeStringList(this.mInterfaceNameList);
    }

    private void readFromParcel(Parcel in) {
        this.mIsConnected = in.readBoolean();
        this.mHostAddress = in.readString();
        this.mAuthenticationToken = in.readString();
        this.mCapabilities = (NsdNetworkCapabilities) in.readParcelable(NsdNetworkCapabilities.class.getClassLoader());
        this.mInterfaceNameList = in.createStringArrayList();
    }

    public static class Builder {
        private NsdNetwork mNetwork = new NsdNetwork();

        public Builder from(NsdNetwork network) {
            if (network != null) {
                this.mNetwork = network;
            }
            return this;
        }

        public Builder setConnected(boolean isConnected) {
            boolean unused = this.mNetwork.mIsConnected = isConnected;
            return this;
        }

        public Builder setHostAddress(String hostAddress) {
            String unused = this.mNetwork.mHostAddress = hostAddress;
            return this;
        }

        public Builder setAuthenticationToken(String token) {
            String unused = this.mNetwork.mAuthenticationToken = token;
            return this;
        }

        public Builder setInterfaceNameList(List<String> infNames) {
            this.mNetwork.mInterfaceNameList.addAll(infNames);
            return this;
        }

        public Builder setCapabilities(NsdNetworkCapabilities capabilities) {
            NsdNetworkCapabilities unused = this.mNetwork.mCapabilities = capabilities;
            return this;
        }

        public NsdNetwork build() {
            return this.mNetwork;
        }
    }
}
