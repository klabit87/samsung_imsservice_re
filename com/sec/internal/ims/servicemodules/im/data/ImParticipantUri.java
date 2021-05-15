package com.sec.internal.ims.servicemodules.im.data;

import com.sec.ims.util.ImsUri;

public class ImParticipantUri {
    private final ImsUri mImsUri;

    public ImParticipantUri(ImsUri imsUri) {
        this.mImsUri = imsUri;
    }

    public ImsUri getImsUri() {
        return this.mImsUri;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            return getImsUri().equals(((ImParticipantUri) obj).getImsUri());
        }
        return false;
    }

    public int hashCode() {
        String str;
        if (this.mImsUri.getUriType() != ImsUri.UriType.SIP_URI) {
            return toString().hashCode() + 31;
        }
        String user = this.mImsUri.getUser();
        String host = this.mImsUri.getHost();
        int port = this.mImsUri.getPort();
        StringBuilder sb = new StringBuilder();
        sb.append("sip:");
        String str2 = "";
        if (user == null) {
            str = str2;
        } else {
            str = "@";
            String user2 = str;
        }
        sb.append(str);
        sb.append(host);
        if (port != -1) {
            str2 = ":" + port;
        }
        sb.append(str2);
        return sb.toString().hashCode() + 31;
    }

    public String toString() {
        return this.mImsUri.toString();
    }
}
