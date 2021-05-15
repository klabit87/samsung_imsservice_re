package com.sec.internal.ims.servicemodules.csh.event;

import android.os.Message;

public class VshStartSessionParams extends CshStartSessionParams {
    public VshStartSessionParams(String receiver, Message callback) {
        super(receiver, callback);
    }
}
