package com.sun.mail.imap;

import java.io.IOException;
import java.io.OutputStream;
import org.xbill.DNS.KEYRecord;

/* compiled from: IMAPFolder */
class LengthCounter extends OutputStream {
    private byte[] buf = new byte[KEYRecord.Flags.FLAG2];
    private int maxsize;
    private int size = 0;

    public LengthCounter(int maxsize2) {
        this.maxsize = maxsize2;
    }

    public void write(int b) {
        int newsize = this.size + 1;
        if (this.buf != null) {
            int i = this.maxsize;
            if (newsize <= i || i < 0) {
                byte[] bArr = this.buf;
                if (newsize > bArr.length) {
                    byte[] newbuf = new byte[Math.max(bArr.length << 1, newsize)];
                    System.arraycopy(this.buf, 0, newbuf, 0, this.size);
                    this.buf = newbuf;
                    newbuf[this.size] = (byte) b;
                } else {
                    bArr[this.size] = (byte) b;
                }
            } else {
                this.buf = null;
            }
        }
        this.size = newsize;
    }

    public void write(byte[] b, int off, int len) {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len != 0) {
            int newsize = this.size + len;
            if (this.buf != null) {
                int i = this.maxsize;
                if (newsize <= i || i < 0) {
                    byte[] bArr = this.buf;
                    if (newsize > bArr.length) {
                        byte[] newbuf = new byte[Math.max(bArr.length << 1, newsize)];
                        System.arraycopy(this.buf, 0, newbuf, 0, this.size);
                        this.buf = newbuf;
                        System.arraycopy(b, off, newbuf, this.size, len);
                    } else {
                        System.arraycopy(b, off, bArr, this.size, len);
                    }
                } else {
                    this.buf = null;
                }
            }
            this.size = newsize;
        }
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public int getSize() {
        return this.size;
    }

    public byte[] getBytes() {
        return this.buf;
    }
}
