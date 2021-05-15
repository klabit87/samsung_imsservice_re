package org.xbill.DNS.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class base64 {
    private static final String Base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    private base64() {
    }

    public static String toString(byte[] b) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (int i = 0; i < (b.length + 2) / 3; i++) {
            short[] s = new short[3];
            short[] t = new short[4];
            for (int j = 0; j < 3; j++) {
                if ((i * 3) + j < b.length) {
                    s[j] = (short) (b[(i * 3) + j] & 255);
                } else {
                    s[j] = -1;
                }
            }
            t[0] = (short) (s[0] >> 2);
            if (s[1] == -1) {
                t[1] = (short) ((s[0] & 3) << 4);
            } else {
                t[1] = (short) (((s[0] & 3) << 4) + (s[1] >> 4));
            }
            if (s[1] == -1) {
                t[3] = 64;
                t[2] = 64;
            } else if (s[2] == -1) {
                t[2] = (short) ((s[1] & 15) << 2);
                t[3] = 64;
            } else {
                t[2] = (short) (((s[1] & 15) << 2) + (s[2] >> 6));
                t[3] = (short) (s[2] & 63);
            }
            for (int j2 = 0; j2 < 4; j2++) {
                os.write(Base64.charAt(t[j2]));
            }
        }
        return new String(os.toByteArray());
    }

    public static String formatString(byte[] b, int lineLength, String prefix, boolean addClose) {
        String s = toString(b);
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (i < s.length()) {
            sb.append(prefix);
            if (i + lineLength >= s.length()) {
                sb.append(s.substring(i));
                if (addClose) {
                    sb.append(" )");
                }
            } else {
                sb.append(s.substring(i, i + lineLength));
                sb.append("\n");
            }
            i += lineLength;
        }
        return sb.toString();
    }

    public static byte[] fromString(String str) {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte[] raw = str.getBytes();
        for (int i = 0; i < raw.length; i++) {
            if (!Character.isWhitespace((char) raw[i])) {
                bs.write(raw[i]);
            }
        }
        byte[] in = bs.toByteArray();
        if (in.length % 4 != 0) {
            return null;
        }
        bs.reset();
        DataOutputStream ds = new DataOutputStream(bs);
        for (int i2 = 0; i2 < (in.length + 3) / 4; i2++) {
            short[] s = new short[4];
            short[] t = new short[3];
            for (int j = 0; j < 4; j++) {
                s[j] = (short) Base64.indexOf(in[(i2 * 4) + j]);
            }
            t[0] = (short) ((s[0] << 2) + (s[1] >> 4));
            if (s[2] == 64) {
                t[2] = -1;
                t[1] = -1;
                if ((s[1] & 15) != 0) {
                    return null;
                }
            } else if (s[3] == 64) {
                t[1] = (short) (((s[1] << 4) + (s[2] >> 2)) & 255);
                t[2] = -1;
                if ((s[2] & 3) != 0) {
                    return null;
                }
            } else {
                t[1] = (short) (((s[1] << 4) + (s[2] >> 2)) & 255);
                t[2] = (short) (((s[2] << 6) + s[3]) & 255);
            }
            int j2 = 0;
            while (j2 < 3) {
                try {
                    if (t[j2] >= 0) {
                        ds.writeByte(t[j2]);
                    }
                    j2++;
                } catch (IOException e) {
                }
            }
        }
        return bs.toByteArray();
    }
}
