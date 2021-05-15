package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import java.util.Vector;

public class Namespaces {
    public Namespace[] otherUsers;
    public Namespace[] personal;
    public Namespace[] shared;

    public static class Namespace {
        public char delimiter;
        public String prefix;

        public Namespace(Response r) throws ProtocolException {
            if (r.readByte() == 40) {
                this.prefix = BASE64MailboxDecoder.decode(r.readString());
                r.skipSpaces();
                if (r.peekByte() == 34) {
                    r.readByte();
                    char readByte = (char) r.readByte();
                    this.delimiter = readByte;
                    if (readByte == '\\') {
                        this.delimiter = (char) r.readByte();
                    }
                    if (r.readByte() != 34) {
                        throw new ProtocolException("Missing '\"' at end of QUOTED_CHAR");
                    }
                } else {
                    String s = r.readAtom();
                    if (s == null) {
                        throw new ProtocolException("Expected NIL, got null");
                    } else if (s.equalsIgnoreCase("NIL")) {
                        this.delimiter = 0;
                    } else {
                        throw new ProtocolException("Expected NIL, got " + s);
                    }
                }
                if (r.peekByte() != 41) {
                    r.skipSpaces();
                    r.readString();
                    r.skipSpaces();
                    r.readStringList();
                }
                if (r.readByte() != 41) {
                    throw new ProtocolException("Missing ')' at end of Namespace");
                }
                return;
            }
            throw new ProtocolException("Missing '(' at start of Namespace");
        }
    }

    public Namespaces(Response r) throws ProtocolException {
        this.personal = getNamespaces(r);
        this.otherUsers = getNamespaces(r);
        this.shared = getNamespaces(r);
    }

    private Namespace[] getNamespaces(Response r) throws ProtocolException {
        r.skipSpaces();
        if (r.peekByte() == 40) {
            Vector v = new Vector();
            r.readByte();
            do {
                v.addElement(new Namespace(r));
            } while (r.peekByte() != 41);
            r.readByte();
            Namespace[] nsa = new Namespace[v.size()];
            v.copyInto(nsa);
            return nsa;
        }
        String s = r.readAtom();
        if (s == null) {
            throw new ProtocolException("Expected NIL, got null");
        } else if (s.equalsIgnoreCase("NIL")) {
            return null;
        } else {
            throw new ProtocolException("Expected NIL, got " + s);
        }
    }
}
