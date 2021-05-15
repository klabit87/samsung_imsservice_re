package org.xbill.DNS;

public class NameTooLongException extends WireParseException {
    public NameTooLongException() {
    }

    public NameTooLongException(String s) {
        super(s);
    }
}
