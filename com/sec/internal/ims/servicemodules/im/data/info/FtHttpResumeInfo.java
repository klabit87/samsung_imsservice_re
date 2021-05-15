package com.sec.internal.ims.servicemodules.im.data.info;

import com.sec.internal.log.IMSLog;
import java.net.URL;

public class FtHttpResumeInfo {
    private final long mEnd;
    private final long mStart;
    private final URL mUrl;

    public FtHttpResumeInfo(long start, long end, URL url) {
        this.mStart = start;
        this.mEnd = end;
        this.mUrl = url;
    }

    public long getEnd() {
        return this.mEnd;
    }

    public URL getUrl() {
        return this.mUrl;
    }

    public String toString() {
        return "FtHttpResumeInfo [mStart=" + this.mStart + ", mEnd=" + this.mEnd + ", mUrl=" + IMSLog.checker(this.mUrl) + "]";
    }
}
