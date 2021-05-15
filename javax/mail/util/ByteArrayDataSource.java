package javax.mail.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import org.xbill.DNS.KEYRecord;

public class ByteArrayDataSource implements DataSource {
    private byte[] data;
    private int len = -1;
    private String name = "";
    private String type;

    static class DSByteArrayOutputStream extends ByteArrayOutputStream {
        DSByteArrayOutputStream() {
        }

        public byte[] getBuf() {
            return this.buf;
        }

        public int getCount() {
            return this.count;
        }
    }

    public ByteArrayDataSource(InputStream is, String type2) throws IOException {
        DSByteArrayOutputStream os = new DSByteArrayOutputStream();
        byte[] buf = new byte[KEYRecord.Flags.FLAG2];
        while (true) {
            int read = is.read(buf);
            int len2 = read;
            if (read <= 0) {
                break;
            }
            os.write(buf, 0, len2);
        }
        this.data = os.getBuf();
        int count = os.getCount();
        this.len = count;
        if (this.data.length - count > 262144) {
            byte[] byteArray = os.toByteArray();
            this.data = byteArray;
            this.len = byteArray.length;
        }
        this.type = type2;
    }

    public ByteArrayDataSource(byte[] data2, String type2) {
        this.data = data2;
        this.type = type2;
    }

    public ByteArrayDataSource(String data2, String type2) throws IOException {
        String charset = null;
        try {
            charset = new ContentType(type2).getParameter("charset");
        } catch (ParseException e) {
        }
        this.data = data2.getBytes(charset == null ? MimeUtility.getDefaultJavaCharset() : charset);
        this.type = type2;
    }

    public InputStream getInputStream() throws IOException {
        byte[] bArr = this.data;
        if (bArr != null) {
            if (this.len < 0) {
                this.len = bArr.length;
            }
            return new SharedByteArrayInputStream(this.data, 0, this.len);
        }
        throw new IOException("no data");
    }

    public OutputStream getOutputStream() throws IOException {
        throw new IOException("cannot do this");
    }

    public String getContentType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }
}
