package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.ParsingException;
import java.io.ByteArrayInputStream;

public class BODY implements Item {
    static final char[] name = {'B', 'O', 'D', 'Y'};
    public ByteArray data;
    public int msgno;
    public int origin = 0;
    public String section;

    public BODY(FetchResponse r) throws ParsingException {
        int b;
        this.msgno = r.getNumber();
        r.skipSpaces();
        do {
            int readByte = r.readByte();
            b = readByte;
            if (readByte == 93) {
                if (r.readByte() == 60) {
                    this.origin = r.readNumber();
                    r.skip(1);
                }
                this.data = r.readByteArray();
                return;
            }
        } while (b != 0);
        throw new ParsingException("BODY parse error: missing ``]'' at section end");
    }

    public ByteArray getByteArray() {
        return this.data;
    }

    public ByteArrayInputStream getByteArrayInputStream() {
        ByteArray byteArray = this.data;
        if (byteArray != null) {
            return byteArray.toByteArrayInputStream();
        }
        return null;
    }
}
