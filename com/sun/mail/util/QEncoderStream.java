package com.sun.mail.util;

import java.io.IOException;
import java.io.OutputStream;

public class QEncoderStream extends QPEncoderStream {
    private static String TEXT_SPECIALS = "=_?";
    private static String WORD_SPECIALS = "=_?\"#$%&'(),.:;<>@[\\]^`{|}~";
    private String specials;

    public QEncoderStream(OutputStream out, boolean encodingWord) {
        super(out, Integer.MAX_VALUE);
        this.specials = encodingWord ? WORD_SPECIALS : TEXT_SPECIALS;
    }

    public void write(int c) throws IOException {
        int c2 = c & 255;
        if (c2 == 32) {
            output(95, false);
        } else if (c2 < 32 || c2 >= 127 || this.specials.indexOf(c2) >= 0) {
            output(c2, true);
        } else {
            output(c2, false);
        }
    }

    public static int encodedLength(byte[] b, boolean encodingWord) {
        int len = 0;
        String specials2 = encodingWord ? WORD_SPECIALS : TEXT_SPECIALS;
        for (byte b2 : b) {
            int c = b2 & 255;
            if (c < 32 || c >= 127 || specials2.indexOf(c) >= 0) {
                len += 3;
            } else {
                len++;
            }
        }
        return len;
    }
}
