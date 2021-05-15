package com.sec.internal.ims.servicemodules.euc.data;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.Preconditions;

public class EucResponseData {
    private final Message mCallback;
    private final String mId;
    private final String mOwnIdentity;
    private final String mPin;
    private final ImsUri mRemoteUri;
    private final EucType mType;
    private final Response mValue;

    public enum Response {
        ACCEPT,
        DECLINE
    }

    public EucResponseData(String id, EucType type, String pin, ImsUri remoteUri, String ownIdentity, Response value, Message callback) {
        boolean z = type == EucType.PERSISTENT || type == EucType.VOLATILE;
        Preconditions.checkArgument(z, "EucType " + type + " is not applicable for EucResponseData");
        this.mId = id;
        this.mType = type;
        this.mPin = pin;
        this.mRemoteUri = remoteUri;
        this.mOwnIdentity = ownIdentity;
        this.mValue = value;
        this.mCallback = callback;
    }

    public String getId() {
        return this.mId;
    }

    public EucType getType() {
        return this.mType;
    }

    public String getPin() {
        return this.mPin;
    }

    public ImsUri getRemoteUri() {
        return this.mRemoteUri;
    }

    public String getOwnIdentity() {
        return this.mOwnIdentity;
    }

    public Response getValue() {
        return this.mValue;
    }

    public Message getCallback() {
        return this.mCallback;
    }

    public String toString() {
        return getClass().getSimpleName() + " [mId=" + this.mId + ", mType=" + this.mType + ", mPin=" + this.mPin + ", mRemoteUri=" + this.mRemoteUri + ", mOwnIdentity=" + this.mOwnIdentity + ", mValue=" + this.mValue + ", mCallback=" + this.mCallback + "]";
    }
}
