package org.xbill.DNS.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class base32 {
    private String alphabet;
    private boolean lowercase;
    private boolean padding;

    public static class Alphabet {
        public static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567=";
        public static final String BASE32HEX = "0123456789ABCDEFGHIJKLMNOPQRSTUV=";

        private Alphabet() {
        }
    }

    public base32(String alphabet2, boolean padding2, boolean lowercase2) {
        this.alphabet = alphabet2;
        this.padding = padding2;
        this.lowercase = lowercase2;
    }

    private static int blockLenToPadding(int blocklen) {
        if (blocklen == 1) {
            return 6;
        }
        if (blocklen == 2) {
            return 4;
        }
        if (blocklen == 3) {
            return 3;
        }
        if (blocklen == 4) {
            return 1;
        }
        if (blocklen != 5) {
            return -1;
        }
        return 0;
    }

    private static int paddingToBlockLen(int padlen) {
        if (padlen == 0) {
            return 5;
        }
        if (padlen == 1) {
            return 4;
        }
        if (padlen == 3) {
            return 3;
        }
        if (padlen != 4) {
            return padlen != 6 ? -1 : 1;
        }
        return 2;
    }

    public String toString(byte[] b) {
        byte[] bArr = b;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (int i = 0; i < (bArr.length + 4) / 5; i++) {
            short[] s = new short[5];
            int[] t = new int[8];
            int blocklen = 5;
            for (int j = 0; j < 5; j++) {
                if ((i * 5) + j < bArr.length) {
                    s[j] = (short) (bArr[(i * 5) + j] & 255);
                } else {
                    s[j] = 0;
                    blocklen--;
                }
            }
            int j2 = blockLenToPadding(blocklen);
            t[0] = (byte) ((s[0] >> 3) & 31);
            t[1] = (byte) (((s[0] & 7) << 2) | ((s[1] >> 6) & 3));
            t[2] = (byte) ((s[1] >> 1) & 31);
            t[3] = (byte) (((s[1] & 1) << 4) | ((s[2] >> 4) & 15));
            t[4] = (byte) (((s[2] & 15) << 1) | (1 & (s[3] >> 7)));
            t[5] = (byte) ((s[3] >> 2) & 31);
            t[6] = (byte) (((s[4] >> 5) & 7) | ((s[3] & 3) << 3));
            t[7] = (byte) (s[4] & 31);
            for (int j3 = 0; j3 < t.length - j2; j3++) {
                char c = this.alphabet.charAt(t[j3]);
                if (this.lowercase) {
                    c = Character.toLowerCase(c);
                }
                os.write(c);
            }
            if (this.padding != 0) {
                for (int j4 = t.length - j2; j4 < t.length; j4++) {
                    os.write(61);
                }
            }
        }
        return new String(os.toByteArray());
    }

    public byte[] fromString(String str) {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte[] raw = str.getBytes();
        for (byte b : raw) {
            char c = (char) b;
            if (!Character.isWhitespace(c)) {
                bs.write((byte) Character.toUpperCase(c));
            }
        }
        char c2 = '=';
        if (this.padding == 0) {
            while (bs.size() % 8 != 0) {
                bs.write(61);
            }
        } else if (bs.size() % 8 != 0) {
            return null;
        }
        byte[] in = bs.toByteArray();
        bs.reset();
        DataOutputStream ds = new DataOutputStream(bs);
        int i = 0;
        while (i < in.length / 8) {
            short[] s = new short[8];
            int[] t = new int[5];
            int padlen = 8;
            int j = 0;
            while (j < 8 && ((char) in[(i * 8) + j]) != c2) {
                s[j] = (short) this.alphabet.indexOf(in[(i * 8) + j]);
                if (s[j] < 0) {
                    return null;
                }
                padlen--;
                j++;
                c2 = '=';
            }
            int blocklen = paddingToBlockLen(padlen);
            if (blocklen < 0) {
                return null;
            }
            t[0] = (s[0] << 3) | (s[1] >> 2);
            t[1] = ((s[1] & 3) << 6) | (s[2] << 1) | (s[3] >> 4);
            t[2] = ((s[3] & 15) << 4) | ((s[4] >> 1) & 15);
            t[3] = (s[4] << 7) | (s[5] << 2) | (s[6] >> 3);
            t[4] = ((s[6] & 7) << 5) | s[7];
            int j2 = 0;
            while (j2 < blocklen) {
                try {
                    ds.writeByte((byte) (t[j2] & 255));
                    j2++;
                } catch (IOException e) {
                }
            }
            i++;
            c2 = '=';
        }
        return bs.toByteArray();
    }
}
