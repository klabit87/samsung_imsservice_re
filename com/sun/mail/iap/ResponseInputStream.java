package com.sun.mail.iap;

import com.sun.mail.util.ASCIIUtility;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xbill.DNS.KEYRecord;

public class ResponseInputStream {
    private static final int incrementSlop = 16;
    private static final int maxIncrement = 262144;
    private static final int minIncrement = 256;
    private BufferedInputStream bin;

    public ResponseInputStream(InputStream in) {
        this.bin = new BufferedInputStream(in, KEYRecord.Flags.FLAG4);
    }

    public ByteArray readResponse() throws IOException {
        return readResponse((ByteArray) null);
    }

    public ByteArray readResponse(ByteArray ba) throws IOException {
        if (ba == null) {
            ba = new ByteArray(new byte[128], 0, 128);
        }
        byte[] buffer = ba.getBytes();
        int idx = 0;
        while (true) {
            int b = 0;
            boolean gotCRLF = false;
            while (!gotCRLF) {
                int read = this.bin.read();
                b = read;
                if (read == -1) {
                    break;
                }
                if (b == 10 && idx > 0 && buffer[idx - 1] == 13) {
                    gotCRLF = true;
                }
                if (idx >= buffer.length) {
                    int incr = buffer.length;
                    if (incr > maxIncrement) {
                        incr = maxIncrement;
                    }
                    ba.grow(incr);
                    buffer = ba.getBytes();
                }
                buffer[idx] = (byte) b;
                idx++;
            }
            if (b != -1) {
                if (idx < 5 || buffer[idx - 3] != 125) {
                    break;
                }
                int i = idx - 4;
                while (i >= 0 && buffer[i] != 123) {
                    i--;
                }
                if (i < 0) {
                    break;
                }
                try {
                    int count = ASCIIUtility.parseInt(buffer, i + 1, idx - 3);
                    if (count > 0) {
                        int avail = buffer.length - idx;
                        if (count + 16 > avail) {
                            int i2 = 256;
                            if (256 <= (count + 16) - avail) {
                                i2 = (count + 16) - avail;
                            }
                            ba.grow(i2);
                            buffer = ba.getBytes();
                        }
                        while (count > 0) {
                            int actual = this.bin.read(buffer, idx, count);
                            count -= actual;
                            idx += actual;
                        }
                    }
                } catch (NumberFormatException e) {
                }
            } else {
                throw new IOException();
            }
        }
        ba.setCount(idx);
        return ba;
    }
}
