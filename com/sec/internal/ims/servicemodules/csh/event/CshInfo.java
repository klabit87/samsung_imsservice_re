package com.sec.internal.ims.servicemodules.csh.event;

import com.sec.ims.util.ImsUri;
import java.util.Observable;

public class CshInfo extends Observable {
    public String dataPath = "";
    public long dataProgress = 0;
    public long dataSize = 0;
    public String mimeType = "";
    public int reasonCode = 0;
    public ImsUri shareContactUri = ImsUri.EMPTY;
    public int shareDirection = 0;
    public long shareId = 0;
    public int shareState = 0;
    public int shareType = 0;
    public long timeStamp;
    public int videoHeight = 0;
    public int videoWidth = 0;

    public String toString() {
        return "id: " + this.shareId + " type: " + this.shareType;
    }
}
