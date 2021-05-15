package com.sun.mail.smtp;

import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;

public class SMTPAddressFailedException extends SendFailedException {
    private static final long serialVersionUID = 804831199768630097L;
    protected InternetAddress addr;
    protected String cmd;
    protected int rc;

    public SMTPAddressFailedException(InternetAddress addr2, String cmd2, int rc2, String err) {
        super(err);
        this.addr = addr2;
        this.cmd = cmd2;
        this.rc = rc2;
    }

    public InternetAddress getAddress() {
        return this.addr;
    }

    public String getCommand() {
        return this.cmd;
    }

    public int getReturnCode() {
        return this.rc;
    }
}
