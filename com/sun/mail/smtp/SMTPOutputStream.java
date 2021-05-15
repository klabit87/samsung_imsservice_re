package com.sun.mail.smtp;

import com.sun.mail.util.CRLFOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SMTPOutputStream extends CRLFOutputStream {
    public SMTPOutputStream(OutputStream os) {
        super(os);
    }

    public void write(int b) throws IOException {
        if ((this.lastb == 10 || this.lastb == 13 || this.lastb == -1) && b == 46) {
            this.out.write(46);
        }
        super.write(b);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: byte} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void write(byte[] r7, int r8, int r9) throws java.io.IOException {
        /*
            r6 = this;
            int r0 = r6.lastb
            r1 = 10
            r2 = -1
            if (r0 != r2) goto L_0x0009
            r0 = r1
            goto L_0x000b
        L_0x0009:
            int r0 = r6.lastb
        L_0x000b:
            r2 = r8
            int r9 = r9 + r8
            r3 = r8
        L_0x000e:
            if (r3 < r9) goto L_0x001a
            int r1 = r9 - r2
            if (r1 <= 0) goto L_0x0019
            int r1 = r9 - r2
            super.write(r7, r2, r1)
        L_0x0019:
            return
        L_0x001a:
            if (r0 == r1) goto L_0x0020
            r4 = 13
            if (r0 != r4) goto L_0x0031
        L_0x0020:
            byte r4 = r7[r3]
            r5 = 46
            if (r4 != r5) goto L_0x0031
            int r4 = r3 - r2
            super.write(r7, r2, r4)
            java.io.OutputStream r4 = r6.out
            r4.write(r5)
            r2 = r3
        L_0x0031:
            byte r0 = r7[r3]
            int r3 = r3 + 1
            goto L_0x000e
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPOutputStream.write(byte[], int, int):void");
    }

    public void flush() {
    }

    public void ensureAtBOL() throws IOException {
        if (!this.atBOL) {
            super.writeln();
        }
    }
}
