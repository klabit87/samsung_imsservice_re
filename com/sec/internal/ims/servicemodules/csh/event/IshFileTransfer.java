package com.sec.internal.ims.servicemodules.csh.event;

import com.sec.internal.helper.Preconditions;

public class IshFileTransfer extends IshFile {
    private long mTransmittedBytes;

    public IshFileTransfer(String path, int size, String mimeType) {
        Preconditions.checkNotNull(path, "path can't be NULL");
        Preconditions.checkState(size >= 0);
        Preconditions.checkNotNull(mimeType, "mimeType can't be NULL");
        this.mTransmittedBytes = 0;
        this.mPath = path;
        this.mSize = (long) size;
        this.mMimeType = mimeType;
    }

    public String getPath() {
        return this.mPath;
    }

    public long getSize() {
        return this.mSize;
    }

    public String toString() {
        return "IshFileTransfer [mTransmittedBytes=" + this.mTransmittedBytes + ", mPath=" + this.mPath + ", mSize=" + this.mSize + ", mMimeType=" + this.mMimeType + "]";
    }
}
