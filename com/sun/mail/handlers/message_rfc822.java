package com.sun.mail.handlers;

import com.sec.internal.constants.ims.cmstore.adapter.DeviceConfigAdapterConstants;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessageAware;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import myjava.awt.datatransfer.DataFlavor;

public class message_rfc822 implements DataContentHandler {
    ActivationDataFlavor ourDataFlavor = new ActivationDataFlavor(Message.class, "message/rfc822", DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.MESSAGES);

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{this.ourDataFlavor};
    }

    public Object getTransferData(DataFlavor df, DataSource ds) throws IOException {
        if (this.ourDataFlavor.equals(df)) {
            return getContent(ds);
        }
        return null;
    }

    public Object getContent(DataSource ds) throws IOException {
        Session session;
        try {
            if (ds instanceof MessageAware) {
                session = ((MessageAware) ds).getMessageContext().getSession();
            } else {
                session = Session.getDefaultInstance(new Properties(), (Authenticator) null);
            }
            return new MimeMessage(session, ds.getInputStream());
        } catch (MessagingException me) {
            throw new IOException("Exception creating MimeMessage in message/rfc822 DataContentHandler: " + me.toString());
        }
    }

    public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
        if (obj instanceof Message) {
            try {
                ((Message) obj).writeTo(os);
            } catch (MessagingException me) {
                throw new IOException(me.toString());
            }
        } else {
            throw new IOException("unsupported object");
        }
    }
}
