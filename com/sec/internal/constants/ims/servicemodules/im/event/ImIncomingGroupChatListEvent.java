package com.sec.internal.constants.ims.servicemodules.im.event;

import android.net.Uri;
import java.util.List;

public class ImIncomingGroupChatListEvent {
    public List<Entry> entryList;
    public String mOwnImsi;
    public int version;

    public ImIncomingGroupChatListEvent(int version2, List<Entry> entryList2, String ownImsi) {
        this.version = version2;
        this.entryList = entryList2;
        this.mOwnImsi = ownImsi;
    }

    public static class Entry {
        public String pConvID;
        public Uri sessionUri;
        public String subject;

        public Entry(Uri sessionUri2, String pConvID2, String subject2) {
            this.sessionUri = sessionUri2;
            this.pConvID = pConvID2;
            this.subject = subject2;
        }
    }
}
