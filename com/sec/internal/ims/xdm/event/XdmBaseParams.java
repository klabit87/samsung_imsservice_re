package com.sec.internal.ims.xdm.event;

import android.os.Message;

public class XdmBaseParams {
    public final Message mCallback;
    public final String mXui;

    public XdmBaseParams(String xui, Message callback) {
        this.mXui = xui;
        this.mCallback = callback;
    }
}
