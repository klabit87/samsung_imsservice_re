package com.sec.internal.ims.xdm.event;

import android.os.Message;

public final class XdmDeleteContactParams extends XdmBaseParams {
    public final String mAccessToken;
    public final String mAssociatedIds;
    public final String mContactId;
    public final String mEtag;
    public final String mUuid;

    public XdmDeleteContactParams(String xui, String contactId, String uuid, String etag, Message callback, String accessToken, String associatedIds) {
        super(xui, callback);
        this.mContactId = contactId;
        this.mUuid = uuid;
        this.mEtag = etag;
        this.mAccessToken = accessToken;
        this.mAssociatedIds = associatedIds;
    }

    public String toString() {
        return "XdmDeleteContactParams [mXui = " + this.mXui + ", mContactId =" + this.mContactId + ", mUuid = " + this.mUuid + ", mEtag = " + this.mEtag + ", mCallback = " + this.mCallback + ", mAccessToken = " + this.mAccessToken + ", mAssociatedIds = " + this.mAssociatedIds + "]";
    }
}
