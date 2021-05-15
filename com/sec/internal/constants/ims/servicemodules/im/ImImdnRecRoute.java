package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.log.IMSLog;

public class ImImdnRecRoute {
    private int mId;
    private final String mImdnMsgId;
    private int mMessageId;
    private final String mUri;
    private final String mUserAlias;

    public ImImdnRecRoute(int id, int messageId, String imdnMsgId, String uri, String userAlias) {
        this.mId = id;
        this.mMessageId = messageId;
        this.mImdnMsgId = imdnMsgId;
        this.mUserAlias = userAlias;
        this.mUri = uri;
    }

    public ImImdnRecRoute(String imdnMsgId, String uri, String userAlias) {
        this.mImdnMsgId = imdnMsgId;
        this.mUserAlias = userAlias;
        this.mUri = uri;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getMessageId() {
        return this.mMessageId;
    }

    public void setMessageId(int messageId) {
        this.mMessageId = messageId;
    }

    public String getImdnMsgId() {
        return this.mImdnMsgId;
    }

    public String getRecordRouteUri() {
        return this.mUri;
    }

    public String getRecordRouteDispName() {
        return this.mUserAlias;
    }

    public String toString() {
        return "ImImdnRecRoute [mId=" + this.mId + ", mMessageId=" + this.mMessageId + ", mImdnMsgId=" + this.mImdnMsgId + ", mUri=" + this.mUri + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + "]";
    }

    public int hashCode() {
        int i = 31 * 1;
        String str = this.mImdnMsgId;
        int i2 = 0;
        int result = 31 * (i + (str == null ? 0 : str.hashCode()));
        String str2 = this.mUri;
        int result2 = 31 * (result + (str2 == null ? 0 : str2.hashCode()));
        String str3 = this.mUserAlias;
        if (str3 != null) {
            i2 = str3.hashCode();
        }
        return result2 + i2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImImdnRecRoute other = (ImImdnRecRoute) obj;
        String str = this.mImdnMsgId;
        if (str == null) {
            if (other.mImdnMsgId != null) {
                return false;
            }
        } else if (!str.equals(other.mImdnMsgId)) {
            return false;
        }
        String str2 = this.mUri;
        if (str2 != null) {
            return str2.equals(other.mUri);
        }
        if (other.mUri == null) {
            return true;
        }
        return false;
    }
}
