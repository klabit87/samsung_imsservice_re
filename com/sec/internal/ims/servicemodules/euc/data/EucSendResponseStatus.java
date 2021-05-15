package com.sec.internal.ims.servicemodules.euc.data;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.Preconditions;

public class EucSendResponseStatus {
    private final String mId;
    private final String mOwnIdentity;
    private final ImsUri mRemoteUri;
    private final Status mStatus;
    private final EucType mType;

    public enum Status {
        SUCCESS,
        FAILURE_INTERNAL,
        FAILURE_NETWORK
    }

    public EucSendResponseStatus(String id, EucType type, ImsUri remoteUri, String ownIdentity, Status status) {
        boolean z = type == EucType.PERSISTENT || type == EucType.VOLATILE;
        Preconditions.checkArgument(z, "EucType " + type + " is not applicable for EucSendResponseStatus");
        this.mId = id;
        this.mType = type;
        this.mRemoteUri = remoteUri;
        this.mOwnIdentity = ownIdentity;
        this.mStatus = status;
    }

    public String getId() {
        return this.mId;
    }

    public ImsUri getRemoteUri() {
        return this.mRemoteUri;
    }

    public String getOwnIdentity() {
        return this.mOwnIdentity;
    }

    public Status getStatus() {
        return this.mStatus;
    }

    public EucMessageKey getKey() {
        return new EucMessageKey(this.mId, this.mOwnIdentity, this.mType, this.mRemoteUri);
    }

    public String toString() {
        return getClass().getSimpleName() + " [mId=" + this.mId + ", mType=" + this.mType + ", mRemoteUri=" + this.mRemoteUri + ", mOwnIdentity=" + this.mOwnIdentity + ", mStatus=" + this.mStatus + "]";
    }
}
