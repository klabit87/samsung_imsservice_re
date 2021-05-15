package com.sec.internal.ims.servicemodules.csh.event;

import android.os.Message;

public class IshStartSessionParams extends CshStartSessionParams {
    public IshFile mfile;

    public IshStartSessionParams(String receiver, IshFile file, Message callback) {
        super(receiver, callback);
        this.mfile = file;
    }

    public String toString() {
        return "IshStartSessionParams " + super.toString() + " " + this.mfile.toString();
    }
}
