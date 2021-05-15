package com.sun.mail.imap;

public class AppendUID {
    public long uid = -1;
    public long uidvalidity = -1;

    public AppendUID(long uidvalidity2, long uid2) {
        this.uidvalidity = uidvalidity2;
        this.uid = uid2;
    }
}
