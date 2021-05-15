package com.sun.mail.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BASE64EncoderStream extends FilterOutputStream {
    private static byte[] newline = {13, 10};
    private static final char[] pem_array = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private byte[] buffer;
    private int bufsize;
    private int bytesPerLine;
    private int count;
    private int lineLimit;
    private boolean noCRLF;
    private byte[] outbuf;

    public BASE64EncoderStream(OutputStream out, int bytesPerLine2) {
        super(out);
        this.bufsize = 0;
        this.count = 0;
        this.noCRLF = false;
        this.buffer = new byte[3];
        if (bytesPerLine2 == Integer.MAX_VALUE || bytesPerLine2 < 4) {
            this.noCRLF = true;
            bytesPerLine2 = 76;
        }
        int bytesPerLine3 = (bytesPerLine2 / 4) * 4;
        this.bytesPerLine = bytesPerLine3;
        this.lineLimit = (bytesPerLine3 / 4) * 3;
        if (this.noCRLF) {
            this.outbuf = new byte[bytesPerLine3];
            return;
        }
        byte[] bArr = new byte[(bytesPerLine3 + 2)];
        this.outbuf = bArr;
        bArr[bytesPerLine3] = 13;
        bArr[bytesPerLine3 + 1] = 10;
    }

    public BASE64EncoderStream(OutputStream out) {
        this(out, 76);
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        int end = off + len;
        while (true) {
            if (this.bufsize == 0) {
                break;
            } else if (off >= end) {
                break;
            } else {
                write((int) b[off]);
                off++;
            }
        }
        int blen = ((this.bytesPerLine - this.count) / 4) * 3;
        if (off + blen < end) {
            int outlen = encodedSize(blen);
            if (!this.noCRLF) {
                int outlen2 = outlen + 1;
                this.outbuf[outlen] = 13;
                this.outbuf[outlen2] = 10;
                outlen = outlen2 + 1;
            }
            this.out.write(encode(b, off, blen, this.outbuf), 0, outlen);
            off += blen;
            this.count = 0;
        }
        while (this.lineLimit + off < end) {
            this.out.write(encode(b, off, this.lineLimit, this.outbuf));
            off += this.lineLimit;
        }
        if (off + 3 < end) {
            int blen2 = ((end - off) / 3) * 3;
            int outlen3 = encodedSize(blen2);
            this.out.write(encode(b, off, blen2, this.outbuf), 0, outlen3);
            off += blen2;
            this.count += outlen3;
        }
        while (off < end) {
            write((int) b[off]);
            off++;
        }
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public synchronized void write(int c) throws IOException {
        byte[] bArr = this.buffer;
        int i = this.bufsize;
        int i2 = i + 1;
        this.bufsize = i2;
        bArr[i] = (byte) c;
        if (i2 == 3) {
            encode();
            this.bufsize = 0;
        }
    }

    public synchronized void flush() throws IOException {
        if (this.bufsize > 0) {
            encode();
            this.bufsize = 0;
        }
        this.out.flush();
    }

    public synchronized void close() throws IOException {
        flush();
        if (this.count > 0 && !this.noCRLF) {
            this.out.write(newline);
            this.out.flush();
        }
        this.out.close();
    }

    private void encode() throws IOException {
        int osize = encodedSize(this.bufsize);
        this.out.write(encode(this.buffer, 0, this.bufsize, this.outbuf), 0, osize);
        int i = this.count + osize;
        this.count = i;
        if (i >= this.bytesPerLine) {
            if (!this.noCRLF) {
                this.out.write(newline);
            }
            this.count = 0;
        }
    }

    public static byte[] encode(byte[] inbuf) {
        if (inbuf.length == 0) {
            return inbuf;
        }
        return encode(inbuf, 0, inbuf.length, (byte[]) null);
    }

    private static byte[] encode(byte[] inbuf, int off, int size, byte[] outbuf2) {
        if (outbuf2 == null) {
            outbuf2 = new byte[encodedSize(size)];
        }
        int val = off;
        int outpos = 0;
        while (size >= 3) {
            int inpos = val + 1;
            int inpos2 = inpos + 1;
            int val2 = ((((inbuf[val] & 255) << 8) | (inbuf[inpos] & 255)) << 8) | (inbuf[inpos2] & 255);
            char[] cArr = pem_array;
            outbuf2[outpos + 3] = (byte) cArr[val2 & 63];
            int val3 = val2 >> 6;
            outbuf2[outpos + 2] = (byte) cArr[val3 & 63];
            int val4 = val3 >> 6;
            outbuf2[outpos + 1] = (byte) cArr[val4 & 63];
            outbuf2[outpos + 0] = (byte) cArr[(val4 >> 6) & 63];
            size -= 3;
            outpos += 4;
            val = inpos2 + 1;
        }
        if (size == 1) {
            int inpos3 = val + 1;
            int val5 = (inbuf[val] & 255) << 4;
            outbuf2[outpos + 3] = 61;
            outbuf2[outpos + 2] = 61;
            char[] cArr2 = pem_array;
            outbuf2[outpos + 1] = (byte) cArr2[val5 & 63];
            outbuf2[outpos + 0] = (byte) cArr2[(val5 >> 6) & 63];
            int val6 = inpos3;
        } else if (size == 2) {
            int inpos4 = val + 1;
            int val7 = (((inbuf[val] & 255) << 8) | (inbuf[inpos4] & 255)) << 2;
            outbuf2[outpos + 3] = 61;
            char[] cArr3 = pem_array;
            outbuf2[outpos + 2] = (byte) cArr3[val7 & 63];
            int val8 = val7 >> 6;
            outbuf2[outpos + 1] = (byte) cArr3[val8 & 63];
            outbuf2[outpos + 0] = (byte) cArr3[(val8 >> 6) & 63];
            int val9 = inpos4 + 1;
        }
        return outbuf2;
    }

    private static int encodedSize(int size) {
        return ((size + 2) / 3) * 4;
    }
}
