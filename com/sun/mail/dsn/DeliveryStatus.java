package com.sun.mail.dsn;

import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sun.mail.util.LineOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

public class DeliveryStatus {
    private static boolean debug;
    protected InternetHeaders messageDSN;
    protected InternetHeaders[] recipientDSN;

    static {
        boolean z = false;
        debug = false;
        try {
            String s = System.getProperty("mail.dsn.debug");
            if (s != null && !s.equalsIgnoreCase(ConfigConstants.VALUE.INFO_COMPLETED)) {
                z = true;
            }
            debug = z;
        } catch (SecurityException e) {
        }
    }

    public DeliveryStatus() throws MessagingException {
        this.messageDSN = new InternetHeaders();
        this.recipientDSN = new InternetHeaders[0];
    }

    public DeliveryStatus(InputStream is) throws MessagingException, IOException {
        this.messageDSN = new InternetHeaders(is);
        if (debug) {
            System.out.println("DSN: got messageDSN");
        }
        Vector v = new Vector();
        while (is.available() > 0) {
            try {
                InternetHeaders h = new InternetHeaders(is);
                if (debug) {
                    System.out.println("DSN: got recipientDSN");
                }
                v.addElement(h);
            } catch (EOFException e) {
                if (debug) {
                    System.out.println("DSN: got EOFException");
                }
            }
        }
        if (debug) {
            PrintStream printStream = System.out;
            printStream.println("DSN: recipientDSN size " + v.size());
        }
        InternetHeaders[] internetHeadersArr = new InternetHeaders[v.size()];
        this.recipientDSN = internetHeadersArr;
        v.copyInto(internetHeadersArr);
    }

    public InternetHeaders getMessageDSN() {
        return this.messageDSN;
    }

    public void setMessageDSN(InternetHeaders messageDSN2) {
        this.messageDSN = messageDSN2;
    }

    public int getRecipientDSNCount() {
        return this.recipientDSN.length;
    }

    public InternetHeaders getRecipientDSN(int n) {
        return this.recipientDSN[n];
    }

    public void addRecipientDSN(InternetHeaders h) {
        InternetHeaders[] internetHeadersArr = this.recipientDSN;
        InternetHeaders[] rh = new InternetHeaders[(internetHeadersArr.length + 1)];
        System.arraycopy(internetHeadersArr, 0, rh, 0, internetHeadersArr.length);
        this.recipientDSN = rh;
        rh[rh.length - 1] = h;
    }

    public void writeTo(OutputStream os) throws IOException, MessagingException {
        LineOutputStream los;
        if (os instanceof LineOutputStream) {
            los = (LineOutputStream) os;
        } else {
            los = new LineOutputStream(os);
        }
        writeInternetHeaders(this.messageDSN, los);
        los.writeln();
        int i = 0;
        while (true) {
            InternetHeaders[] internetHeadersArr = this.recipientDSN;
            if (i < internetHeadersArr.length) {
                writeInternetHeaders(internetHeadersArr[i], los);
                los.writeln();
                i++;
            } else {
                return;
            }
        }
    }

    private static void writeInternetHeaders(InternetHeaders h, LineOutputStream los) throws IOException {
        Enumeration e = h.getAllHeaderLines();
        while (e.hasMoreElements()) {
            try {
                los.writeln((String) e.nextElement());
            } catch (MessagingException mex) {
                Exception ex = mex.getNextException();
                if (ex instanceof IOException) {
                    throw ((IOException) ex);
                }
                throw new IOException("Exception writing headers: " + mex);
            }
        }
    }

    public String toString() {
        return "DeliveryStatus: Reporting-MTA=" + this.messageDSN.getHeader("Reporting-MTA", (String) null) + ", #Recipients=" + this.recipientDSN.length;
    }
}
