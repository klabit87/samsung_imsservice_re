package com.sec.internal.ims.xdm.event;

import android.os.Handler;
import android.os.Message;
import com.sec.internal.ims.xdm.event.XdmFetchDocumentParams;

public final class XdmDownloadContactsParams extends XdmFetchDocumentParams {
    public final Handler mHandler;
    public final Object mUserObj;
    public final int mWhat;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public XdmDownloadContactsParams(String xui, XdmFetchDocumentParams.DocType type, String dirOrDocName, Message callback, XdmFetchDocumentParams.NodeSelector selector, Handler h, int what, Object userObj, String accessToken) {
        super(xui, type, dirOrDocName, callback, selector, accessToken);
        this.mHandler = h;
        this.mWhat = what;
        this.mUserObj = userObj;
    }

    public XdmDownloadContactsParams(String xui, XdmFetchDocumentParams.DocType type, String dirOrDocName, Message callback, XdmFetchDocumentParams.NodeSelector selector, Handler h, int what, Object userObj) {
        this(xui, type, dirOrDocName, callback, selector, h, what, userObj, (String) null);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("XdmDownloadContactsParams [mXui = ");
        sb.append(this.mXui);
        sb.append(", mType = ");
        sb.append(this.mType);
        sb.append(", mName = ");
        sb.append(this.mName);
        sb.append(", mCallback = ");
        sb.append(this.mCallback);
        sb.append(", mNodeSelector = ");
        sb.append(this.mNodeSelector);
        sb.append(", mAccessToken = ");
        sb.append(this.mAccessToken);
        sb.append(", mHandler = ");
        Handler handler = this.mHandler;
        sb.append(handler == null ? null : handler.getClass().getSimpleName());
        sb.append(", mWhat = ");
        sb.append(this.mWhat);
        sb.append("]");
        return sb.toString();
    }
}
