package org.xbill.DNS;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.xbill.DNS.utils.base16;
import org.xbill.DNS.utils.base32;
import org.xbill.DNS.utils.base64;

public class Tokenizer {
    public static final int COMMENT = 5;
    public static final int EOF = 0;
    public static final int EOL = 1;
    public static final int IDENTIFIER = 3;
    public static final int QUOTED_STRING = 4;
    public static final int WHITESPACE = 2;
    private static String delim = " \t\n;()\"";
    private static String quotes = "\"";
    private Token current;
    private String delimiters;
    private String filename;
    private PushbackInputStream is;
    private int line;
    private int multiline;
    private boolean quoting;
    private StringBuffer sb;
    private boolean ungottenToken;
    private boolean wantClose;

    public static class Token {
        public int type;
        public String value;

        private Token() {
            this.type = -1;
            this.value = null;
        }

        /* access modifiers changed from: private */
        public Token set(int type2, StringBuffer value2) {
            if (type2 >= 0) {
                this.type = type2;
                this.value = value2 == null ? null : value2.toString();
                return this;
            }
            throw new IllegalArgumentException();
        }

        public String toString() {
            int i = this.type;
            if (i == 0) {
                return "<eof>";
            }
            if (i == 1) {
                return "<eol>";
            }
            if (i == 2) {
                return "<whitespace>";
            }
            if (i == 3) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("<identifier: ");
                stringBuffer.append(this.value);
                stringBuffer.append(">");
                return stringBuffer.toString();
            } else if (i == 4) {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("<quoted_string: ");
                stringBuffer2.append(this.value);
                stringBuffer2.append(">");
                return stringBuffer2.toString();
            } else if (i != 5) {
                return "<unknown>";
            } else {
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append("<comment: ");
                stringBuffer3.append(this.value);
                stringBuffer3.append(">");
                return stringBuffer3.toString();
            }
        }

        public boolean isString() {
            int i = this.type;
            return i == 3 || i == 4;
        }

        public boolean isEOL() {
            int i = this.type;
            return i == 1 || i == 0;
        }
    }

    static class TokenizerException extends TextParseException {
        String message;

        /* JADX WARNING: Illegal instructions before constructor call */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public TokenizerException(java.lang.String r3, int r4, java.lang.String r5) {
            /*
                r2 = this;
                java.lang.StringBuffer r0 = new java.lang.StringBuffer
                r0.<init>()
                r0.append(r3)
                java.lang.String r1 = ":"
                r0.append(r1)
                r0.append(r4)
                java.lang.String r1 = ": "
                r0.append(r1)
                r0.append(r5)
                java.lang.String r0 = r0.toString()
                r2.<init>(r0)
                r2.message = r5
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Tokenizer.TokenizerException.<init>(java.lang.String, int, java.lang.String):void");
        }

        public String getBaseMessage() {
            return this.message;
        }
    }

    public Tokenizer(InputStream is2) {
        this.is = new PushbackInputStream(!(is2 instanceof BufferedInputStream) ? new BufferedInputStream(is2) : is2, 2);
        this.ungottenToken = false;
        this.multiline = 0;
        this.quoting = false;
        this.delimiters = delim;
        this.current = new Token();
        this.sb = new StringBuffer();
        this.filename = "<none>";
        this.line = 1;
    }

    public Tokenizer(String s) {
        this((InputStream) new ByteArrayInputStream(s.getBytes()));
    }

    public Tokenizer(File f) throws FileNotFoundException {
        this((InputStream) new FileInputStream(f));
        this.wantClose = true;
        this.filename = f.getName();
    }

    private int getChar() throws IOException {
        int c = this.is.read();
        if (c == 13) {
            int next = this.is.read();
            if (next != 10) {
                this.is.unread(next);
            }
            c = 10;
        }
        if (c == 10) {
            this.line++;
        }
        return c;
    }

    private void ungetChar(int c) throws IOException {
        if (c != -1) {
            this.is.unread(c);
            if (c == 10) {
                this.line--;
            }
        }
    }

    private int skipWhitespace() throws IOException {
        int c;
        int skipped = 0;
        while (true) {
            c = getChar();
            if (c == 32 || c == 9 || (c == 10 && this.multiline > 0)) {
                skipped++;
            }
        }
        ungetChar(c);
        return skipped;
    }

    private void checkUnbalancedParens() throws TextParseException {
        if (this.multiline > 0) {
            throw exception("unbalanced parentheses");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:103:0x0113 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x011f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public org.xbill.DNS.Tokenizer.Token get(boolean r13, boolean r14) throws java.io.IOException {
        /*
            r12 = this;
            boolean r0 = r12.ungottenToken
            r1 = 5
            r2 = 2
            r3 = 1
            r4 = 0
            if (r0 == 0) goto L_0x002e
            r12.ungottenToken = r4
            org.xbill.DNS.Tokenizer$Token r0 = r12.current
            int r0 = r0.type
            if (r0 != r2) goto L_0x0015
            if (r13 == 0) goto L_0x002e
            org.xbill.DNS.Tokenizer$Token r0 = r12.current
            return r0
        L_0x0015:
            org.xbill.DNS.Tokenizer$Token r0 = r12.current
            int r0 = r0.type
            if (r0 != r1) goto L_0x0020
            if (r14 == 0) goto L_0x002e
            org.xbill.DNS.Tokenizer$Token r0 = r12.current
            return r0
        L_0x0020:
            org.xbill.DNS.Tokenizer$Token r0 = r12.current
            int r0 = r0.type
            if (r0 != r3) goto L_0x002b
            int r0 = r12.line
            int r0 = r0 + r3
            r12.line = r0
        L_0x002b:
            org.xbill.DNS.Tokenizer$Token r0 = r12.current
            return r0
        L_0x002e:
            int r0 = r12.skipWhitespace()
            r5 = 0
            if (r0 <= 0) goto L_0x003e
            if (r13 == 0) goto L_0x003e
            org.xbill.DNS.Tokenizer$Token r1 = r12.current
            org.xbill.DNS.Tokenizer$Token r1 = r1.set(r2, r5)
            return r1
        L_0x003e:
            r2 = 3
            java.lang.StringBuffer r6 = r12.sb
            r6.setLength(r4)
        L_0x0044:
            int r6 = r12.getChar()
            r7 = 10
            r8 = -1
            if (r6 == r8) goto L_0x0083
            java.lang.String r9 = r12.delimiters
            int r9 = r9.indexOf(r6)
            if (r9 == r8) goto L_0x0056
            goto L_0x0083
        L_0x0056:
            r9 = 92
            if (r6 != r9) goto L_0x006e
            int r6 = r12.getChar()
            if (r6 == r8) goto L_0x0066
            java.lang.StringBuffer r7 = r12.sb
            r7.append(r9)
            goto L_0x007c
        L_0x0066:
            java.lang.String r1 = "unterminated escape sequence"
            org.xbill.DNS.TextParseException r1 = r12.exception(r1)
            throw r1
        L_0x006e:
            boolean r8 = r12.quoting
            if (r8 == 0) goto L_0x007c
            if (r6 == r7) goto L_0x0075
            goto L_0x007c
        L_0x0075:
            java.lang.String r1 = "newline in quoted string"
            org.xbill.DNS.TextParseException r1 = r12.exception(r1)
            throw r1
        L_0x007c:
            java.lang.StringBuffer r7 = r12.sb
            char r8 = (char) r6
            r7.append(r8)
            goto L_0x0044
        L_0x0083:
            if (r6 != r8) goto L_0x00a8
            boolean r1 = r12.quoting
            if (r1 != 0) goto L_0x00a1
            java.lang.StringBuffer r1 = r12.sb
            int r1 = r1.length()
            if (r1 != 0) goto L_0x0098
            org.xbill.DNS.Tokenizer$Token r1 = r12.current
            org.xbill.DNS.Tokenizer$Token r1 = r1.set(r4, r5)
            return r1
        L_0x0098:
            org.xbill.DNS.Tokenizer$Token r1 = r12.current
            java.lang.StringBuffer r3 = r12.sb
            org.xbill.DNS.Tokenizer$Token r1 = r1.set(r2, r3)
            return r1
        L_0x00a1:
            java.lang.String r1 = "EOF in quoted string"
            org.xbill.DNS.TextParseException r1 = r12.exception(r1)
            throw r1
        L_0x00a8:
            java.lang.StringBuffer r9 = r12.sb
            int r9 = r9.length()
            r10 = 4
            if (r9 != 0) goto L_0x0148
            if (r2 == r10) goto L_0x0148
            r9 = 40
            if (r6 != r9) goto L_0x00c0
            int r7 = r12.multiline
            int r7 = r7 + r3
            r12.multiline = r7
            r12.skipWhitespace()
            goto L_0x0044
        L_0x00c0:
            r9 = 41
            if (r6 != r9) goto L_0x00d8
            int r7 = r12.multiline
            if (r7 <= 0) goto L_0x00d1
            int r7 = r7 + -1
            r12.multiline = r7
            r12.skipWhitespace()
            goto L_0x0044
        L_0x00d1:
            java.lang.String r1 = "invalid close parenthesis"
            org.xbill.DNS.TextParseException r1 = r12.exception(r1)
            throw r1
        L_0x00d8:
            r9 = 34
            if (r6 != r9) goto L_0x00f4
            boolean r7 = r12.quoting
            if (r7 != 0) goto L_0x00e9
            r12.quoting = r3
            java.lang.String r7 = quotes
            r12.delimiters = r7
            r2 = 4
            goto L_0x0044
        L_0x00e9:
            r12.quoting = r4
            java.lang.String r7 = delim
            r12.delimiters = r7
            r12.skipWhitespace()
            goto L_0x0044
        L_0x00f4:
            if (r6 != r7) goto L_0x00fd
            org.xbill.DNS.Tokenizer$Token r1 = r12.current
            org.xbill.DNS.Tokenizer$Token r1 = r1.set(r3, r5)
            return r1
        L_0x00fd:
            r9 = 59
            if (r6 != r9) goto L_0x0142
        L_0x0101:
            int r6 = r12.getChar()
            if (r6 == r7) goto L_0x0111
            if (r6 != r8) goto L_0x010a
            goto L_0x0111
        L_0x010a:
            java.lang.StringBuffer r9 = r12.sb
            char r11 = (char) r6
            r9.append(r11)
            goto L_0x0101
        L_0x0111:
            if (r14 == 0) goto L_0x011f
            r12.ungetChar(r6)
            org.xbill.DNS.Tokenizer$Token r3 = r12.current
            java.lang.StringBuffer r4 = r12.sb
            org.xbill.DNS.Tokenizer$Token r1 = r3.set(r1, r4)
            return r1
        L_0x011f:
            if (r6 != r8) goto L_0x012d
            if (r2 == r10) goto L_0x012d
            r12.checkUnbalancedParens()
            org.xbill.DNS.Tokenizer$Token r1 = r12.current
            org.xbill.DNS.Tokenizer$Token r1 = r1.set(r4, r5)
            return r1
        L_0x012d:
            int r7 = r12.multiline
            if (r7 <= 0) goto L_0x013b
            r12.skipWhitespace()
            java.lang.StringBuffer r7 = r12.sb
            r7.setLength(r4)
            goto L_0x0044
        L_0x013b:
            org.xbill.DNS.Tokenizer$Token r1 = r12.current
            org.xbill.DNS.Tokenizer$Token r1 = r1.set(r3, r5)
            return r1
        L_0x0142:
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException
            r1.<init>()
            throw r1
        L_0x0148:
            r12.ungetChar(r6)
            java.lang.StringBuffer r1 = r12.sb
            int r1 = r1.length()
            if (r1 != 0) goto L_0x0160
            if (r2 == r10) goto L_0x0160
            r12.checkUnbalancedParens()
            org.xbill.DNS.Tokenizer$Token r1 = r12.current
            org.xbill.DNS.Tokenizer$Token r1 = r1.set(r4, r5)
            return r1
        L_0x0160:
            org.xbill.DNS.Tokenizer$Token r1 = r12.current
            java.lang.StringBuffer r3 = r12.sb
            org.xbill.DNS.Tokenizer$Token r1 = r1.set(r2, r3)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Tokenizer.get(boolean, boolean):org.xbill.DNS.Tokenizer$Token");
    }

    public Token get() throws IOException {
        return get(false, false);
    }

    public void unget() {
        if (!this.ungottenToken) {
            if (this.current.type == 1) {
                this.line--;
            }
            this.ungottenToken = true;
            return;
        }
        throw new IllegalStateException("Cannot unget multiple tokens");
    }

    public String getString() throws IOException {
        Token next = get();
        if (next.isString()) {
            return next.value;
        }
        throw exception("expected a string");
    }

    private String _getIdentifier(String expected) throws IOException {
        Token next = get();
        if (next.type == 3) {
            return next.value;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("expected ");
        stringBuffer.append(expected);
        throw exception(stringBuffer.toString());
    }

    public String getIdentifier() throws IOException {
        return _getIdentifier("an identifier");
    }

    public long getLong() throws IOException {
        String next = _getIdentifier("an integer");
        if (Character.isDigit(next.charAt(0))) {
            try {
                return Long.parseLong(next);
            } catch (NumberFormatException e) {
                throw exception("expected an integer");
            }
        } else {
            throw exception("expected an integer");
        }
    }

    public long getUInt32() throws IOException {
        long l = getLong();
        if (l >= 0 && l <= 4294967295L) {
            return l;
        }
        throw exception("expected an 32 bit unsigned integer");
    }

    public int getUInt16() throws IOException {
        long l = getLong();
        if (l >= 0 && l <= 65535) {
            return (int) l;
        }
        throw exception("expected an 16 bit unsigned integer");
    }

    public int getUInt8() throws IOException {
        long l = getLong();
        if (l >= 0 && l <= 255) {
            return (int) l;
        }
        throw exception("expected an 8 bit unsigned integer");
    }

    public long getTTL() throws IOException {
        try {
            return TTL.parseTTL(_getIdentifier("a TTL value"));
        } catch (NumberFormatException e) {
            throw exception("expected a TTL value");
        }
    }

    public long getTTLLike() throws IOException {
        try {
            return TTL.parse(_getIdentifier("a TTL-like value"), false);
        } catch (NumberFormatException e) {
            throw exception("expected a TTL-like value");
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public Name getName(Name origin) throws IOException {
        try {
            Name name = Name.fromString(_getIdentifier("a name"), origin);
            if (name.isAbsolute()) {
                return name;
            }
            throw new RelativeNameException(name);
        } catch (TextParseException e) {
            throw exception(e.getMessage());
        }
    }

    public byte[] getAddressBytes(int family) throws IOException {
        String next = _getIdentifier("an address");
        byte[] bytes = Address.toByteArray(next, family);
        if (bytes != null) {
            return bytes;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Invalid address: ");
        stringBuffer.append(next);
        throw exception(stringBuffer.toString());
    }

    public InetAddress getAddress(int family) throws IOException {
        try {
            return Address.getByAddress(_getIdentifier("an address"), family);
        } catch (UnknownHostException e) {
            throw exception(e.getMessage());
        }
    }

    public void getEOL() throws IOException {
        Token next = get();
        if (next.type != 1 && next.type != 0) {
            throw exception("expected EOL or EOF");
        }
    }

    private String remainingStrings() throws IOException {
        StringBuffer buffer = null;
        while (true) {
            Token t = get();
            if (!t.isString()) {
                break;
            }
            if (buffer == null) {
                buffer = new StringBuffer();
            }
            buffer.append(t.value);
        }
        unget();
        if (buffer == null) {
            return null;
        }
        return buffer.toString();
    }

    public byte[] getBase64(boolean required) throws IOException {
        String s = remainingStrings();
        if (s != null) {
            byte[] array = base64.fromString(s);
            if (array != null) {
                return array;
            }
            throw exception("invalid base64 encoding");
        } else if (!required) {
            return null;
        } else {
            throw exception("expected base64 encoded string");
        }
    }

    public byte[] getBase64() throws IOException {
        return getBase64(false);
    }

    public byte[] getHex(boolean required) throws IOException {
        String s = remainingStrings();
        if (s != null) {
            byte[] array = base16.fromString(s);
            if (array != null) {
                return array;
            }
            throw exception("invalid hex encoding");
        } else if (!required) {
            return null;
        } else {
            throw exception("expected hex encoded string");
        }
    }

    public byte[] getHex() throws IOException {
        return getHex(false);
    }

    public byte[] getHexString() throws IOException {
        byte[] array = base16.fromString(_getIdentifier("a hex string"));
        if (array != null) {
            return array;
        }
        throw exception("invalid hex encoding");
    }

    public byte[] getBase32String(base32 b32) throws IOException {
        byte[] array = b32.fromString(_getIdentifier("a base32 string"));
        if (array != null) {
            return array;
        }
        throw exception("invalid base32 encoding");
    }

    public TextParseException exception(String s) {
        return new TokenizerException(this.filename, this.line, s);
    }

    public void close() {
        if (this.wantClose) {
            try {
                this.is.close();
            } catch (IOException e) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        close();
    }
}
