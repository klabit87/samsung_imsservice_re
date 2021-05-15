package com.sec.internal.constants.ims.servicemodules.im.params;

import android.net.Uri;

public class GroupChatInfoParams {
    private final String mOwnImsi;
    private final Uri mUri;

    public GroupChatInfoParams(Uri uri, String ownImsi) {
        this.mUri = uri;
        this.mOwnImsi = ownImsi;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public String getOwnImsi() {
        return this.mOwnImsi;
    }
}
