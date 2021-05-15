package com.sun.mail.iap;

public class ConnectionException extends ProtocolException {
    private static final long serialVersionUID = 5749739604257464727L;
    private transient Protocol p;

    public ConnectionException() {
    }

    public ConnectionException(String s) {
        super(s);
    }

    public ConnectionException(Protocol p2, Response r) {
        super(r);
        this.p = p2;
    }

    public Protocol getProtocol() {
        return this.p;
    }
}
