package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Response;

public class Status {
    static final String[] standardItems = {"MESSAGES", "RECENT", "UNSEEN", "UIDNEXT", "UIDVALIDITY"};
    public String mbox = null;
    public int recent = -1;
    public int total = -1;
    public long uidnext = -1;
    public long uidvalidity = -1;
    public int unseen = -1;

    public Status(Response r) throws ParsingException {
        this.mbox = r.readAtomString();
        r.skipSpaces();
        if (r.readByte() == 40) {
            do {
                String attr = r.readAtom();
                if (attr.equalsIgnoreCase("MESSAGES")) {
                    this.total = r.readNumber();
                } else if (attr.equalsIgnoreCase("RECENT")) {
                    this.recent = r.readNumber();
                } else if (attr.equalsIgnoreCase("UIDNEXT")) {
                    this.uidnext = r.readLong();
                } else if (attr.equalsIgnoreCase("UIDVALIDITY")) {
                    this.uidvalidity = r.readLong();
                } else if (attr.equalsIgnoreCase("UNSEEN")) {
                    this.unseen = r.readNumber();
                }
            } while (r.readByte() != 41);
            return;
        }
        throw new ParsingException("parse error in STATUS");
    }

    public static void add(Status s1, Status s2) {
        int i = s2.total;
        if (i != -1) {
            s1.total = i;
        }
        int i2 = s2.recent;
        if (i2 != -1) {
            s1.recent = i2;
        }
        long j = s2.uidnext;
        if (j != -1) {
            s1.uidnext = j;
        }
        long j2 = s2.uidvalidity;
        if (j2 != -1) {
            s1.uidvalidity = j2;
        }
        int i3 = s2.unseen;
        if (i3 != -1) {
            s1.unseen = i3;
        }
    }
}
