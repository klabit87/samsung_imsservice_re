package org.xbill.DNS.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class base16 {
    private static final String Base16 = "0123456789ABCDEF";

    private base16() {
    }

    public static String toString(byte[] b) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (byte b2 : b) {
            short value = (short) (b2 & 255);
            os.write(Base16.charAt((byte) (value >> 4)));
            os.write(Base16.charAt((byte) (value & 15)));
        }
        return new String(os.toByteArray());
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
        if (in.length % 2 != 0) {
            return null;
        }
        bs.reset();
        DataOutputStream ds = new DataOutputStream(bs);
        for (int i2 = 0; i2 < in.length; i2 += 2) {
            try {
                ds.writeByte((((byte) Base16.indexOf(Character.toUpperCase((char) in[i2]))) << 4) + ((byte) Base16.indexOf(Character.toUpperCase((char) in[i2 + 1]))));
            } catch (IOException e) {
            }
        }
        return bs.toByteArray();
    }
}
