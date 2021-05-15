package com.sun.mail.dsn;

import java.io.IOException;
import java.util.Vector;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MultipartReport extends MimeMultipart {
    protected boolean constructed;

    public MultipartReport() throws MessagingException {
        super("report");
        setBodyPart(new MimeBodyPart(), 0);
        setBodyPart(new MimeBodyPart(), 1);
        this.constructed = true;
    }

    public MultipartReport(String text, DeliveryStatus status) throws MessagingException {
        super("report");
        ContentType ct = new ContentType(this.contentType);
        ct.setParameter("report-type", "delivery-status");
        this.contentType = ct.toString();
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setText(text);
        setBodyPart(mbp, 0);
        MimeBodyPart mbp2 = new MimeBodyPart();
        mbp2.setContent(status, "message/delivery-status");
        setBodyPart(mbp2, 1);
        this.constructed = true;
    }

    public MultipartReport(String text, DeliveryStatus status, MimeMessage msg) throws MessagingException {
        this(text, status);
        if (msg != null) {
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(msg, "message/rfc822");
            setBodyPart(mbp, 2);
        }
    }

    public MultipartReport(String text, DeliveryStatus status, InternetHeaders hdr) throws MessagingException {
        this(text, status);
        if (hdr != null) {
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(new MessageHeaders(hdr), "text/rfc822-headers");
            setBodyPart(mbp, 2);
        }
    }

    public MultipartReport(DataSource ds) throws MessagingException {
        super(ds);
        parse();
        this.constructed = true;
    }

    public synchronized String getText() throws MessagingException {
        try {
            BodyPart bp = getBodyPart(0);
            if (bp.isMimeType("text/plain")) {
                return (String) bp.getContent();
            }
            if (bp.isMimeType("multipart/alternative")) {
                Multipart mp = (Multipart) bp.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart bp2 = mp.getBodyPart(i);
                    if (bp2.isMimeType("text/plain")) {
                        return (String) bp2.getContent();
                    }
                }
            }
            return null;
        } catch (IOException ex) {
            throw new MessagingException("Exception getting text content", ex);
        }
    }

    public synchronized void setText(String text) throws MessagingException {
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setText(text);
        setBodyPart(mbp, 0);
    }

    public synchronized MimeBodyPart getTextBodyPart() throws MessagingException {
        return (MimeBodyPart) getBodyPart(0);
    }

    public synchronized void setTextBodyPart(MimeBodyPart mbp) throws MessagingException {
        setBodyPart(mbp, 0);
    }

    public synchronized DeliveryStatus getDeliveryStatus() throws MessagingException {
        if (getCount() < 2) {
            return null;
        }
        BodyPart bp = getBodyPart(1);
        if (!bp.isMimeType("message/delivery-status")) {
            return null;
        }
        try {
            return (DeliveryStatus) bp.getContent();
        } catch (IOException ex) {
            throw new MessagingException("IOException getting DeliveryStatus", ex);
        }
    }

    public synchronized void setDeliveryStatus(DeliveryStatus status) throws MessagingException {
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setContent(status, "message/delivery-status");
        setBodyPart(mbp, 2);
        ContentType ct = new ContentType(this.contentType);
        ct.setParameter("report-type", "delivery-status");
        this.contentType = ct.toString();
    }

    public synchronized MimeMessage getReturnedMessage() throws MessagingException {
        if (getCount() < 3) {
            return null;
        }
        BodyPart bp = getBodyPart(2);
        if (!bp.isMimeType("message/rfc822") && !bp.isMimeType("text/rfc822-headers")) {
            return null;
        }
        try {
            return (MimeMessage) bp.getContent();
        } catch (IOException ex) {
            throw new MessagingException("IOException getting ReturnedMessage", ex);
        }
    }

    public synchronized void setReturnedMessage(MimeMessage msg) throws MessagingException {
        if (msg == null) {
            BodyPart bodyPart = (BodyPart) this.parts.elementAt(2);
            super.removeBodyPart(2);
            return;
        }
        MimeBodyPart mbp = new MimeBodyPart();
        if (msg instanceof MessageHeaders) {
            mbp.setContent(msg, "text/rfc822-headers");
        } else {
            mbp.setContent(msg, "message/rfc822");
        }
        setBodyPart(mbp, 2);
    }

    private synchronized void setBodyPart(BodyPart part, int index) throws MessagingException {
        if (this.parts == null) {
            this.parts = new Vector();
        }
        if (index < this.parts.size()) {
            super.removeBodyPart(index);
        }
        super.addBodyPart(part, index);
    }

    public synchronized void setSubType(String subtype) throws MessagingException {
        throw new MessagingException("Can't change subtype of MultipartReport");
    }

    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        throw new MessagingException("Can't remove body parts from multipart/report");
    }

    public void removeBodyPart(int index) throws MessagingException {
        throw new MessagingException("Can't remove body parts from multipart/report");
    }

    public synchronized void addBodyPart(BodyPart part) throws MessagingException {
        if (!this.constructed) {
            super.addBodyPart(part);
        } else {
            throw new MessagingException("Can't add body parts to multipart/report 1");
        }
    }

    public synchronized void addBodyPart(BodyPart part, int index) throws MessagingException {
        throw new MessagingException("Can't add body parts to multipart/report 2");
    }
}
