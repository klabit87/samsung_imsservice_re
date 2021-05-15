package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.log.IMSLog;
import java.net.URL;

public final class FtHttpFileInfo {
    private final FileInfo mFileInfo = new FileInfo();
    private final FileInfo mThumbnailInfo = new FileInfo();

    public static class FileInfo {
        /* access modifiers changed from: private */
        public String mBrandedUrl;
        /* access modifiers changed from: private */
        public String mContentType;
        /* access modifiers changed from: private */
        public Data mData;
        /* access modifiers changed from: private */
        public FileDisposition mFileDisposition;
        /* access modifiers changed from: private */
        public String mFileName;
        /* access modifiers changed from: private */
        public long mFileSize;
        /* access modifiers changed from: private */
        public int mPlayingLength;

        public String toString() {
            return "FileInfo [mFileSize=" + this.mFileSize + ", mFileName=" + IMSLog.checker(this.mFileName) + ", mContentType=" + this.mContentType + ", mData=" + this.mData + ", mBrandedUrl=" + IMSLog.checker(this.mBrandedUrl) + ", mFileDisposition=" + this.mFileDisposition + ", mPlayingLength=" + this.mPlayingLength + "]";
        }
    }

    public static class Data {
        /* access modifiers changed from: private */
        public final String mUntil;
        /* access modifiers changed from: private */
        public final URL mUrl;

        public Data(URL url, String until) {
            this.mUrl = url;
            this.mUntil = until;
        }

        public String toString() {
            return "Data [mUrl=" + this.mUrl + ", mUntil=" + this.mUntil + "]";
        }
    }

    public long getFileSize() {
        return this.mFileInfo.mFileSize;
    }

    public String getFileName() {
        return this.mFileInfo.mFileName;
    }

    public String getContentType() {
        return this.mFileInfo.mContentType;
    }

    public URL getDataUrl() {
        return this.mFileInfo.mData.mUrl;
    }

    public String getDataUntil() {
        return this.mFileInfo.mData.mUntil;
    }

    public String getBrandedUrl() {
        return this.mFileInfo.mBrandedUrl;
    }

    public FileDisposition getFileDisposition() {
        return this.mFileInfo.mFileDisposition;
    }

    public int getPlayingLength() {
        return this.mFileInfo.mPlayingLength;
    }

    public long getThumbnailFileSize() {
        return this.mThumbnailInfo.mFileSize;
    }

    public String getThumbnailContentType() {
        return this.mThumbnailInfo.mContentType;
    }

    public URL getThumbnailDataUrl() {
        return this.mThumbnailInfo.mData.mUrl;
    }

    public String getThumbnailDataUntil() {
        return this.mThumbnailInfo.mData.mUntil;
    }

    public void setFileSize(long size) {
        long unused = this.mFileInfo.mFileSize = size;
    }

    public void setFileName(String name) {
        String unused = this.mFileInfo.mFileName = name;
    }

    public void setContentType(String type) {
        String unused = this.mFileInfo.mContentType = type;
    }

    public void setData(Data data) {
        Data unused = this.mFileInfo.mData = data;
    }

    public void setBrandedUrl(String brandedUrl) {
        String unused = this.mFileInfo.mBrandedUrl = brandedUrl;
    }

    public void setFileDisposition(String fileDisposition) {
        if ("render".equals(fileDisposition)) {
            FileDisposition unused = this.mFileInfo.mFileDisposition = FileDisposition.RENDER;
        } else {
            FileDisposition unused2 = this.mFileInfo.mFileDisposition = FileDisposition.ATTACH;
        }
    }

    public void setPlayingLength(int playingLength) {
        int unused = this.mFileInfo.mPlayingLength = playingLength;
    }

    public void setThumbnailFileSize(long size) {
        long unused = this.mThumbnailInfo.mFileSize = size;
    }

    public void setThumbnailContentType(String type) {
        String unused = this.mThumbnailInfo.mContentType = type;
    }

    public void setThumbnailData(Data data) {
        Data unused = this.mThumbnailInfo.mData = data;
    }

    public boolean isThumbnailExist() {
        return this.mThumbnailInfo.mData != null;
    }

    public String toString() {
        return "FtHttpFileInfo [mFileInfo=" + this.mFileInfo + ", mThumbnailInfo=" + this.mThumbnailInfo + "]";
    }
}
