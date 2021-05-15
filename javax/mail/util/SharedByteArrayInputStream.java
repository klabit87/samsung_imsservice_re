package javax.mail.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.mail.internet.SharedInputStream;

public class SharedByteArrayInputStream extends ByteArrayInputStream implements SharedInputStream {
    protected int start = 0;

    public SharedByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    public SharedByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
        this.start = offset;
    }

    public long getPosition() {
        return (long) (this.pos - this.start);
    }

    public InputStream newStream(long start2, long end) {
        if (start2 >= 0) {
            if (end == -1) {
                end = (long) (this.count - this.start);
            }
            return new SharedByteArrayInputStream(this.buf, this.start + ((int) start2), (int) (end - start2));
        }
        throw new IllegalArgumentException("start < 0");
    }
}
