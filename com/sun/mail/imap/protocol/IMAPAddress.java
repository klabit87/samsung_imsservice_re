package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Response;
import java.util.Vector;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/* compiled from: ENVELOPE */
class IMAPAddress extends InternetAddress {
    private static final long serialVersionUID = -3835822029483122232L;
    private boolean group = false;
    private InternetAddress[] grouplist;
    private String groupname;

    IMAPAddress(Response r) throws ParsingException {
        r.skipSpaces();
        if (r.readByte() == 40) {
            this.encodedPersonal = r.readString();
            r.readString();
            String mb = r.readString();
            String host = r.readString();
            if (r.readByte() != 41) {
                throw new ParsingException("ADDRESS parse error");
            } else if (host == null) {
                this.group = true;
                this.groupname = mb;
                if (mb != null) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(this.groupname);
                    sb.append(':');
                    Vector v = new Vector();
                    while (r.peekByte() != 41) {
                        IMAPAddress a = new IMAPAddress(r);
                        if (a.isEndOfGroup()) {
                            break;
                        }
                        if (v.size() != 0) {
                            sb.append(',');
                        }
                        sb.append(a.toString());
                        v.addElement(a);
                    }
                    sb.append(';');
                    this.address = sb.toString();
                    IMAPAddress[] iMAPAddressArr = new IMAPAddress[v.size()];
                    this.grouplist = iMAPAddressArr;
                    v.copyInto(iMAPAddressArr);
                }
            } else if (mb == null || mb.length() == 0) {
                this.address = host;
            } else if (host.length() == 0) {
                this.address = mb;
            } else {
                this.address = String.valueOf(mb) + "@" + host;
            }
        } else {
            throw new ParsingException("ADDRESS parse error");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isEndOfGroup() {
        return this.group && this.groupname == null;
    }

    public boolean isGroup() {
        return this.group;
    }

    public InternetAddress[] getGroup(boolean strict) throws AddressException {
        InternetAddress[] internetAddressArr = this.grouplist;
        if (internetAddressArr == null) {
            return null;
        }
        return (InternetAddress[]) internetAddressArr.clone();
    }
}
