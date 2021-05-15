package com.sun.mail.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TraceInputStream extends FilterInputStream {
    private boolean quote = false;
    private boolean trace = false;
    private OutputStream traceOut;

    public TraceInputStream(InputStream in, OutputStream traceOut2) {
        super(in);
        this.traceOut = traceOut2;
    }

    public void setTrace(boolean trace2) {
        this.trace = trace2;
    }

    public void setQuote(boolean quote2) {
        this.quote = quote2;
    }

    public int read() throws IOException {
        int b = this.in.read();
        if (this.trace && b != -1) {
            if (this.quote) {
                writeByte(b);
            } else {
                this.traceOut.write(b);
            }
        }
        return b;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int count = this.in.read(b, off, len);
        if (this.trace && count != -1) {
            if (this.quote) {
                for (int i = 0; i < count; i++) {
                    writeByte(b[off + i]);
                }
            } else {
                this.traceOut.write(b, off, count);
            }
        }
        return count;
    }

    private final void writeByte(int b) throws IOException {
        int b2 = b & 255;
        if (b2 > 127) {
            this.traceOut.write(77);
            this.traceOut.write(45);
            b2 &= 127;
        }
        if (b2 == 13) {
            this.traceOut.write(92);
            this.traceOut.write(114);
        } else if (b2 == 10) {
            this.traceOut.write(92);
            this.traceOut.write(110);
            this.traceOut.write(10);
        } else if (b2 == 9) {
            this.traceOut.write(92);
            this.traceOut.write(116);
        } else if (b2 < 32) {
            this.traceOut.write(94);
            this.traceOut.write(b2 + 64);
        } else {
            this.traceOut.write(b2);
        }
    }
}
