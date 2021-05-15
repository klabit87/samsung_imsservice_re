package javax.mail.internet;

import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.httpclient.HttpPostBody;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownServiceException;
import javax.activation.DataSource;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;

public class MimePartDataSource implements DataSource, MessageAware {
    private static boolean ignoreMultipartEncoding;
    private MessageContext context;
    protected MimePart part;

    static {
        boolean z = true;
        ignoreMultipartEncoding = true;
        try {
            String s = System.getProperty("mail.mime.ignoremultipartencoding");
            if (s != null && s.equalsIgnoreCase(ConfigConstants.VALUE.INFO_COMPLETED)) {
                z = false;
            }
            ignoreMultipartEncoding = z;
        } catch (SecurityException e) {
        }
    }

    public MimePartDataSource(MimePart part2) {
        this.part = part2;
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public InputStream getInputStream() throws IOException {
        InputStream is;
        try {
            if (this.part instanceof MimeBodyPart) {
                is = ((MimeBodyPart) this.part).getContentStream();
            } else if (this.part instanceof MimeMessage) {
                is = ((MimeMessage) this.part).getContentStream();
            } else {
                throw new MessagingException("Unknown part");
            }
            String encoding = restrictEncoding(this.part.getEncoding(), this.part);
            if (encoding != null) {
                return MimeUtility.decode(is, encoding);
            }
            return is;
        } catch (MessagingException mex) {
            throw new IOException(mex.getMessage());
        }
    }

    private static String restrictEncoding(String encoding, MimePart part2) throws MessagingException {
        String type;
        if (!ignoreMultipartEncoding || encoding == null || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit") || encoding.equalsIgnoreCase("binary") || (type = part2.getContentType()) == null) {
            return encoding;
        }
        try {
            ContentType cType = new ContentType(type);
            if (cType.match("multipart/*") || cType.match("message/*")) {
                return null;
            }
            return encoding;
        } catch (ParseException e) {
        }
    }

    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException();
    }

    public String getContentType() {
        try {
            return this.part.getContentType();
        } catch (MessagingException e) {
            return HttpPostBody.CONTENT_TYPE_DEFAULT;
        }
    }

    public String getName() {
        try {
            if (this.part instanceof MimeBodyPart) {
                return ((MimeBodyPart) this.part).getFileName();
            }
            return "";
        } catch (MessagingException e) {
            return "";
        }
    }

    public synchronized MessageContext getMessageContext() {
        if (this.context == null) {
            this.context = new MessageContext(this.part);
        }
        return this.context;
    }
}
