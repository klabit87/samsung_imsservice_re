package com.sec.internal.ims.servicemodules.im.data.response;

import com.sec.internal.log.IMSLog;

public class FileResizeResponse {
    public final boolean isResizeSuccessful;
    public final String resizedFilePath;

    public FileResizeResponse(boolean isResizeSuccessful2, String resizedFilePath2) {
        this.isResizeSuccessful = isResizeSuccessful2;
        this.resizedFilePath = resizedFilePath2;
    }

    public String toString() {
        return "FileResizeResponse [isResizeSuccessful=" + this.isResizeSuccessful + ", resizedFilePath=" + IMSLog.checker(this.resizedFilePath) + "]";
    }
}
