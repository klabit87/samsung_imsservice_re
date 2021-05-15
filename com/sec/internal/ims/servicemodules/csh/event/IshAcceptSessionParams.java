package com.sec.internal.ims.servicemodules.csh.event;

import android.os.Message;

public class IshAcceptSessionParams extends CshAcceptSessionParams {
    public String mPath;

    public IshAcceptSessionParams(int sessionId, String path, Message callback) {
        super(sessionId, callback);
        this.mPath = path;
    }
}
