package com.sun.mail.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class UUEncoderStream extends FilterOutputStream {
    private byte[] buffer;
    private int bufsize;
    protected int mode;
    protected String name;
    private boolean wrotePrefix;

    public UUEncoderStream(OutputStream out) {
        this(out, "encoder.buf", 644);
    }

    public UUEncoderStream(OutputStream out, String name2) {
        this(out, name2, 644);
    }

    public UUEncoderStream(OutputStream out, String name2, int mode2) {
        super(out);
        this.bufsize = 0;
        this.wrotePrefix = false;
        this.name = name2;
        this.mode = mode2;
        this.buffer = new byte[45];
    }

    public void setNameMode(String name2, int mode2) {
        this.name = name2;
        this.mode = mode2;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write((int) b[off + i]);
        }
    }

    public void write(byte[] data) throws IOException {
        write(data, 0, data.length);
    }

    public void write(int c) throws IOException {
        byte[] bArr = this.buffer;
        int i = this.bufsize;
        int i2 = i + 1;
        this.bufsize = i2;
        bArr[i] = (byte) c;
        if (i2 == 45) {
            writePrefix();
            encode();
            this.bufsize = 0;
        }
    }

    public void flush() throws IOException {
        if (this.bufsize > 0) {
            writePrefix();
            encode();
        }
        writeSuffix();
        this.out.flush();
    }

    public void close() throws IOException {
        flush();
        this.out.close();
    }

    private void writePrefix() throws IOException {
        if (!this.wrotePrefix) {
            PrintStream ps = new PrintStream(this.out);
            ps.println("begin " + this.mode + " " + this.name);
            ps.flush();
            this.wrotePrefix = true;
        }
    }

    private void writeSuffix() throws IOException {
        PrintStream ps = new PrintStream(this.out);
        ps.println(" \nend");
        ps.flush();
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r3v3, types: [byte] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void encode() throws java.io.IOException {
        /*
            r11 = this;
            r0 = 0
            java.io.OutputStream r1 = r11.out
            int r2 = r11.bufsize
            r2 = r2 & 63
            int r2 = r2 + 32
            r1.write(r2)
        L_0x000d:
            int r1 = r11.bufsize
            if (r0 < r1) goto L_0x0019
            java.io.OutputStream r1 = r11.out
            r2 = 10
            r1.write(r2)
            return
        L_0x0019:
            byte[] r2 = r11.buffer
            int r3 = r0 + 1
            byte r0 = r2[r0]
            if (r3 >= r1) goto L_0x002f
            int r4 = r3 + 1
            byte r3 = r2[r3]
            if (r4 >= r1) goto L_0x002c
            int r1 = r4 + 1
            byte r2 = r2[r4]
            goto L_0x0034
        L_0x002c:
            r2 = 1
            r1 = r4
            goto L_0x0034
        L_0x002f:
            r1 = 1
            r2 = 1
            r10 = r3
            r3 = r1
            r1 = r10
        L_0x0034:
            int r4 = r0 >>> 2
            r4 = r4 & 63
            int r5 = r0 << 4
            r5 = r5 & 48
            int r6 = r3 >>> 4
            r6 = r6 & 15
            r5 = r5 | r6
            int r6 = r3 << 2
            r6 = r6 & 60
            int r7 = r2 >>> 6
            r7 = r7 & 3
            r6 = r6 | r7
            r7 = r2 & 63
            java.io.OutputStream r8 = r11.out
            int r9 = r4 + 32
            r8.write(r9)
            java.io.OutputStream r8 = r11.out
            int r9 = r5 + 32
            r8.write(r9)
            java.io.OutputStream r8 = r11.out
            int r9 = r6 + 32
            r8.write(r9)
            java.io.OutputStream r8 = r11.out
            int r9 = r7 + 32
            r8.write(r9)
            r0 = r1
            goto L_0x000d
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.UUEncoderStream.encode():void");
    }
}
