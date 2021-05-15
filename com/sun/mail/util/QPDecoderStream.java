package com.sun.mail.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class QPDecoderStream extends FilterInputStream {
    protected byte[] ba = new byte[2];
    protected int spaces = 0;

    public QPDecoderStream(InputStream in) {
        super(new PushbackInputStream(in, 2));
    }

    public int read() throws IOException {
        int c;
        int i = this.spaces;
        if (i > 0) {
            this.spaces = i - 1;
            return 32;
        }
        int c2 = this.in.read();
        if (c2 == 32) {
            while (true) {
                int read = this.in.read();
                c = read;
                if (read != 32) {
                    break;
                }
                this.spaces++;
            }
            if (c == 13 || c == 10 || c == -1) {
                this.spaces = 0;
                return c;
            }
            ((PushbackInputStream) this.in).unread(c);
            return 32;
        } else if (c2 != 61) {
            return c2;
        } else {
            int a = this.in.read();
            if (a == 10) {
                return read();
            }
            if (a == 13) {
                int b = this.in.read();
                if (b != 10) {
                    ((PushbackInputStream) this.in).unread(b);
                }
                return read();
            } else if (a == -1) {
                return -1;
            } else {
                byte[] bArr = this.ba;
                bArr[0] = (byte) a;
                bArr[1] = (byte) this.in.read();
                try {
                    return ASCIIUtility.parseInt(this.ba, 0, 2, 16);
                } catch (NumberFormatException e) {
                    ((PushbackInputStream) this.in).unread(this.ba);
                    return c2;
                }
            }
        }
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        int i = 0;
        while (i < len) {
            int read = read();
            int c = read;
            if (read != -1) {
                buf[off + i] = (byte) c;
                i++;
            } else if (i == 0) {
                return -1;
            } else {
                return i;
            }
        }
        return i;
    }

    public boolean markSupported() {
        return false;
    }

    public int available() throws IOException {
        return this.in.available();
    }
}
