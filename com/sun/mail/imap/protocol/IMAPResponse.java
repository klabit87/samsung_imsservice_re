package com.sun.mail.imap.protocol;

import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.util.ASCIIUtility;
import java.io.IOException;
import java.util.Vector;

public class IMAPResponse extends Response {
    private String key;
    private int number;

    public IMAPResponse(Protocol c) throws IOException, ProtocolException {
        super(c);
        if (isUnTagged() && !isOK() && !isNO() && !isBAD() && !isBYE()) {
            String readAtom = readAtom();
            this.key = readAtom;
            try {
                this.number = Integer.parseInt(readAtom);
                this.key = readAtom();
            } catch (NumberFormatException e) {
            }
        }
    }

    public IMAPResponse(IMAPResponse r) {
        super((Response) r);
        this.key = r.key;
        this.number = r.number;
    }

    public String[] readSimpleList() {
        skipSpaces();
        if (this.buffer[this.index] != 40) {
            return null;
        }
        this.index++;
        Vector v = new Vector();
        int start = this.index;
        while (this.buffer[this.index] != 41) {
            if (this.buffer[this.index] == 32) {
                v.addElement(ASCIIUtility.toString(this.buffer, start, this.index));
                start = this.index + 1;
            }
            this.index++;
        }
        if (this.index > start) {
            v.addElement(ASCIIUtility.toString(this.buffer, start, this.index));
        }
        this.index++;
        int size = v.size();
        if (size <= 0) {
            return null;
        }
        String[] s = new String[size];
        v.copyInto(s);
        return s;
    }

    public String getKey() {
        return this.key;
    }

    public boolean keyEquals(String k) {
        String str = this.key;
        if (str == null || !str.equalsIgnoreCase(k)) {
            return false;
        }
        return true;
    }

    public int getNumber() {
        return this.number;
    }

    public static IMAPResponse readResponse(Protocol p) throws IOException, ProtocolException {
        IMAPResponse r = new IMAPResponse(p);
        if (r.keyEquals("FETCH")) {
            return new FetchResponse(r);
        }
        return r;
    }
}
