package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;

public class DeviceSessionPartcptsUpdateParam {
    public String mChatId;
    public int mTableindex;
    public CloudMessageBufferDBConstants.ActionStatusFlag mUpdateType;

    public DeviceSessionPartcptsUpdateParam(int index, CloudMessageBufferDBConstants.ActionStatusFlag type, String chatId) {
        this.mTableindex = index;
        this.mUpdateType = type;
        this.mChatId = chatId;
    }

    public String toString() {
        return "DeviceSessionPartcptsUpdateParam [mTableindex=" + this.mTableindex + ", mUpdateType= " + this.mUpdateType + ", mChatId=" + this.mChatId + "]";
    }
}
