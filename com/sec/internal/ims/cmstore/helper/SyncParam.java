package com.sec.internal.ims.cmstore.helper;

import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.log.IMSLog;

public class SyncParam {
    public String mLine;
    public SyncMsgType mType;

    public SyncParam(String line, SyncMsgType type) {
        this.mLine = line;
        this.mType = type;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof SyncParam) || !((SyncParam) o).mType.equals(this.mType) || !((SyncParam) o).mLine.equals(this.mLine)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.mType.hashCode() + this.mLine.hashCode();
    }

    public String toString() {
        return "SyncParam = [ mLine = " + IMSLog.checker(this.mLine) + " ], [ mType = " + this.mType + " ].";
    }
}
