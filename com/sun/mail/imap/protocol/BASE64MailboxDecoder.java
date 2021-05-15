package com.sun.mail.imap.protocol;

import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class BASE64MailboxDecoder {
    static final char[] pem_array = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', ','};
    private static final byte[] pem_convert_array = new byte[256];

    public static String decode(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        boolean changedString = false;
        int copyTo = 0;
        char[] chars = new char[original.length()];
        StringCharacterIterator iter = new StringCharacterIterator(original);
        for (char c = iter.first(); c != 65535; c = iter.next()) {
            if (c == '&') {
                changedString = true;
                copyTo = base64decode(chars, copyTo, iter);
            } else {
                chars[copyTo] = c;
                copyTo++;
            }
        }
        if (changedString) {
            return new String(chars, 0, copyTo);
        }
        return original;
    }

    protected static int base64decode(char[] buffer, int offset, CharacterIterator iter) {
        int leftover;
        int leftover2 = -1;
        boolean firsttime = true;
        int offset2 = offset;
        while (true) {
            byte orig_0 = (byte) iter.next();
            if (orig_0 == -1) {
                return offset2;
            }
            if (orig_0 != 45) {
                firsttime = false;
                byte orig_1 = (byte) iter.next();
                if (orig_1 == -1 || orig_1 == 45) {
                    return offset2;
                }
                byte[] bArr = pem_convert_array;
                byte a = bArr[orig_0 & 255];
                byte b = bArr[orig_1 & 255];
                int current = (byte) (((a << 2) & 252) | ((b >>> 4) & 3));
                if (leftover2 != -1) {
                    buffer[offset2] = (char) ((leftover2 << 8) | (current & 255));
                    leftover2 = -1;
                    offset2++;
                } else {
                    leftover2 = current & 255;
                }
                byte orig_2 = (byte) iter.next();
                if (orig_2 != 61) {
                    if (orig_2 == -1 || orig_2 == 45) {
                        return offset2;
                    }
                    byte a2 = b;
                    byte b2 = pem_convert_array[orig_2 & 255];
                    int current2 = (byte) (((a2 << 4) & Id.REQUEST_STOP_RECORD) | ((b2 >>> 2) & 15));
                    if (leftover2 != -1) {
                        buffer[offset2] = (char) ((leftover2 << 8) | (current2 & 255));
                        leftover = -1;
                        offset2++;
                    } else {
                        leftover = current2 & 255;
                    }
                    byte orig_3 = (byte) iter.next();
                    if (orig_3 == 61) {
                        continue;
                    } else if (orig_3 == -1 || orig_3 == 45) {
                        return offset2;
                    } else {
                        int current3 = (byte) (((b2 << 6) & 192) | (pem_convert_array[orig_3 & 255] & 63));
                        if (leftover2 != -1) {
                            char testing = (char) ((leftover2 << 8) | (current3 & 255));
                            buffer[offset2] = (char) ((leftover2 << 8) | (current3 & 255));
                            leftover2 = -1;
                            offset2++;
                        } else {
                            leftover2 = current3 & 255;
                        }
                    }
                }
            } else if (!firsttime) {
                return offset2;
            } else {
                buffer[offset2] = '&';
                return offset2 + 1;
            }
        }
    }

    static {
        for (int i = 0; i < 255; i++) {
            pem_convert_array[i] = -1;
        }
        int i2 = 0;
        while (true) {
            char[] cArr = pem_array;
            if (i2 < cArr.length) {
                pem_convert_array[cArr[i2]] = (byte) i2;
                i2++;
            } else {
                return;
            }
        }
    }
}
