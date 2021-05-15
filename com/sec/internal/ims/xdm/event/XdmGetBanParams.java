package com.sec.internal.ims.xdm.event;

import android.os.Message;

public final class XdmGetBanParams extends XdmBaseParams {
    public final String mAccessToken;

    public XdmGetBanParams(String xui, Message callback, String accessToken) {
        super(xui, callback);
        this.mAccessToken = accessToken;
    }

    public String toString() {
        return "XdmGetBanParams [mXui = " + this.mXui + ", mCallback = " + this.mCallback + ", mAccessToken = " + this.mAccessToken + "]";
    }
}
