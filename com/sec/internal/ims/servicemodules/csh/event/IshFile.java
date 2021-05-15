package com.sec.internal.ims.servicemodules.csh.event;

public class IshFile {
    protected String mMimeType;
    protected String mPath;
    protected long mSize;

    public IshFile() {
    }

    public IshFile(String path, long size, String mimeType) {
        this.mPath = path;
        this.mSize = size;
        this.mMimeType = mimeType;
    }

    public String getPath() {
        return this.mPath;
    }

    public long getSize() {
        return this.mSize;
    }

    public String getMimeType() {
        return this.mMimeType;
    }

    public String toString() {
        return this.mPath + " Size : " + this.mSize + " MimeType : " + this.mMimeType;
    }
}
