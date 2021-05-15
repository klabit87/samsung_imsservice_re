package org.xbill.DNS.utils;

public class hexdump {
    private static final char[] hex = "0123456789ABCDEF".toCharArray();

    public static String dump(String description, byte[] b, int offset, int length) {
        StringBuffer sb = new StringBuffer();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(length);
        stringBuffer.append("b");
        sb.append(stringBuffer.toString());
        if (description != null) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append(" (");
            stringBuffer2.append(description);
            stringBuffer2.append(")");
            sb.append(stringBuffer2.toString());
        }
        sb.append(':');
        int prefixlen = (sb.toString().length() + 8) & -8;
        sb.append(9);
        int perline = (80 - prefixlen) / 3;
        for (int i = 0; i < length; i++) {
            if (i != 0 && i % perline == 0) {
                sb.append(10);
                for (int j = 0; j < prefixlen / 8; j++) {
                    sb.append(9);
                }
            }
            int value = b[i + offset] & 255;
            sb.append(hex[value >> 4]);
            sb.append(hex[value & 15]);
            sb.append(' ');
        }
        sb.append(10);
        return sb.toString();
    }

    public static String dump(String s, byte[] b) {
        return dump(s, b, 0, b.length);
    }
}
