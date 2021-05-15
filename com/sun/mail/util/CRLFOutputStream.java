package com.sun.mail.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CRLFOutputStream extends FilterOutputStream {
    private static final byte[] newline = {13, 10};
    protected boolean atBOL = true;
    protected int lastb = -1;

    public CRLFOutputStream(OutputStream os) {
        super(os);
    }

    public void write(int b) throws IOException {
        if (b == 13) {
            writeln();
        } else if (b != 10) {
            this.out.write(b);
            this.atBOL = false;
        } else if (this.lastb != 13) {
            writeln();
        }
        this.lastb = b;
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        int start = off;
        int len2 = len + off;
        for (int i = start; i < len2; i++) {
            if (b[i] == 13) {
                this.out.write(b, start, i - start);
                writeln();
                start = i + 1;
            } else if (b[i] == 10) {
                if (this.lastb != 13) {
                    this.out.write(b, start, i - start);
                    writeln();
                }
                start = i + 1;
            }
            this.lastb = b[i];
        }
        if (len2 - start > 0) {
            this.out.write(b, start, len2 - start);
            this.atBOL = false;
        }
    }

    public void writeln() throws IOException {
        this.out.write(newline);
        this.atBOL = true;
    }
}
