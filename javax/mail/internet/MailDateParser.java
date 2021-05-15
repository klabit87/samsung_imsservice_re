package javax.mail.internet;

import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.text.ParseException;

/* compiled from: MailDateFormat */
class MailDateParser {
    int index = 0;
    char[] orig = null;

    public MailDateParser(char[] orig2) {
        this.orig = orig2;
    }

    public void skipUntilNumber() throws ParseException {
        while (true) {
            try {
                switch (this.orig[this.index]) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        return;
                    default:
                        this.index++;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new ParseException("No Number Found", this.index);
            }
        }
    }

    public void skipWhiteSpace() {
        int len = this.orig.length;
        while (true) {
            int i = this.index;
            if (i < len) {
                char c = this.orig[i];
                if (c == 9 || c == 10 || c == 13 || c == ' ') {
                    this.index++;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    public int peekChar() throws ParseException {
        int i = this.index;
        char[] cArr = this.orig;
        if (i < cArr.length) {
            return cArr[i];
        }
        throw new ParseException("No more characters", this.index);
    }

    public void skipChar(char c) throws ParseException {
        int i = this.index;
        char[] cArr = this.orig;
        if (i >= cArr.length) {
            throw new ParseException("No more characters", this.index);
        } else if (cArr[i] == c) {
            this.index = i + 1;
        } else {
            throw new ParseException("Wrong char", this.index);
        }
    }

    public boolean skipIfChar(char c) throws ParseException {
        int i = this.index;
        char[] cArr = this.orig;
        if (i >= cArr.length) {
            throw new ParseException("No more characters", this.index);
        } else if (cArr[i] != c) {
            return false;
        } else {
            this.index = i + 1;
            return true;
        }
    }

    public int parseNumber() throws ParseException {
        int length = this.orig.length;
        boolean gotNum = false;
        int result = 0;
        while (true) {
            int i = this.index;
            if (i < length) {
                switch (this.orig[i]) {
                    case '0':
                        result *= 10;
                        gotNum = true;
                        break;
                    case '1':
                        gotNum = true;
                        result = (result * 10) + 1;
                        break;
                    case '2':
                        gotNum = true;
                        result = (result * 10) + 2;
                        break;
                    case '3':
                        gotNum = true;
                        result = (result * 10) + 3;
                        break;
                    case '4':
                        gotNum = true;
                        result = (result * 10) + 4;
                        break;
                    case '5':
                        gotNum = true;
                        result = (result * 10) + 5;
                        break;
                    case '6':
                        gotNum = true;
                        result = (result * 10) + 6;
                        break;
                    case '7':
                        gotNum = true;
                        result = (result * 10) + 7;
                        break;
                    case '8':
                        gotNum = true;
                        result = (result * 10) + 8;
                        break;
                    case '9':
                        gotNum = true;
                        result = (result * 10) + 9;
                        break;
                    default:
                        if (gotNum) {
                            return result;
                        }
                        throw new ParseException("No Number found", this.index);
                }
                this.index++;
            } else if (gotNum) {
                return result;
            } else {
                throw new ParseException("No Number found", this.index);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:103:?, code lost:
        return 9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:?, code lost:
        return 9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:?, code lost:
        return 10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:?, code lost:
        return 10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:?, code lost:
        return 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:?, code lost:
        return 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:?, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:?, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004e, code lost:
        r0 = r14.orig;
        r1 = r14.index;
        r14.index = r1 + 1;
        r0 = r0[r1];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0058, code lost:
        if (r0 == 'C') goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005a, code lost:
        if (r0 != 'c') goto L_0x01a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005c, code lost:
        r1 = r14.orig;
        r2 = r14.index;
        r14.index = r2 + 1;
        r0 = r1[r2];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0069, code lost:
        if (r0 == 'T') goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006d, code lost:
        if (r0 != 't') goto L_0x01a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0072, code lost:
        r0 = r14.orig;
        r1 = r14.index;
        r14.index = r1 + 1;
        r0 = r0[r1];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007e, code lost:
        if (r0 == 'O') goto L_0x0084;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0082, code lost:
        if (r0 != 'o') goto L_0x01a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0084, code lost:
        r1 = r14.orig;
        r2 = r14.index;
        r14.index = r2 + 1;
        r0 = r1[r2];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0091, code lost:
        if (r0 == 'V') goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0095, code lost:
        if (r0 != 'v') goto L_0x01a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009a, code lost:
        r0 = r14.orig;
        r3 = r14.index;
        r14.index = r3 + 1;
        r0 = r0[r3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a4, code lost:
        if (r0 == 'A') goto L_0x00a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a6, code lost:
        if (r0 != 'a') goto L_0x01a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00a8, code lost:
        r3 = r14.orig;
        r4 = r14.index;
        r14.index = r4 + 1;
        r0 = r3[r4];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b3, code lost:
        if (r0 == 'R') goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00b5, code lost:
        if (r0 != 'r') goto L_0x00b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00ba, code lost:
        if (r0 == 'Y') goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00be, code lost:
        if (r0 != 'y') goto L_0x01a5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int parseMonth() throws java.text.ParseException {
        /*
            r14 = this;
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r1 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r1 + 1
            r14.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r1]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r1 = 114(0x72, float:1.6E-43)
            r2 = 82
            r3 = 112(0x70, float:1.57E-43)
            r4 = 117(0x75, float:1.64E-43)
            r5 = 85
            r6 = 80
            r7 = 65
            if (r0 == r7) goto L_0x016b
            r8 = 68
            r9 = 99
            r10 = 67
            r11 = 101(0x65, float:1.42E-43)
            r12 = 69
            if (r0 == r8) goto L_0x014b
            r8 = 70
            if (r0 == r8) goto L_0x0128
            r8 = 74
            r13 = 97
            if (r0 == r8) goto L_0x00e4
            r8 = 83
            if (r0 == r8) goto L_0x00c4
            if (r0 == r13) goto L_0x016b
            r8 = 100
            if (r0 == r8) goto L_0x014b
            r8 = 102(0x66, float:1.43E-43)
            if (r0 == r8) goto L_0x0128
            r8 = 106(0x6a, float:1.49E-43)
            if (r0 == r8) goto L_0x00e4
            r4 = 115(0x73, float:1.61E-43)
            if (r0 == r4) goto L_0x00c4
            switch(r0) {
                case 77: goto L_0x009a;
                case 78: goto L_0x0072;
                case 79: goto L_0x004e;
                default: goto L_0x0049;
            }     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
        L_0x0049:
            switch(r0) {
                case 109: goto L_0x009a;
                case 110: goto L_0x0072;
                case 111: goto L_0x004e;
                default: goto L_0x004c;
            }     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
        L_0x004c:
            goto L_0x01a5
        L_0x004e:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r1 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r1 + 1
            r14.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r1]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            if (r0 == r10) goto L_0x005c
            if (r0 != r9) goto L_0x01a5
        L_0x005c:
            char[] r1 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r3 = r2 + 1
            r14.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r0 = r1
            r1 = 84
            if (r0 == r1) goto L_0x006f
            r1 = 116(0x74, float:1.63E-43)
            if (r0 != r1) goto L_0x01a5
        L_0x006f:
            r1 = 9
            return r1
        L_0x0072:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r1 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r1 + 1
            r14.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r1]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r1 = 79
            if (r0 == r1) goto L_0x0084
            r1 = 111(0x6f, float:1.56E-43)
            if (r0 != r1) goto L_0x01a5
        L_0x0084:
            char[] r1 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r3 = r2 + 1
            r14.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r0 = r1
            r1 = 86
            if (r0 == r1) goto L_0x0097
            r1 = 118(0x76, float:1.65E-43)
            if (r0 != r1) goto L_0x01a5
        L_0x0097:
            r1 = 10
            return r1
        L_0x009a:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r3 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r4 = r3 + 1
            r14.index = r4     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            if (r0 == r7) goto L_0x00a8
            if (r0 != r13) goto L_0x01a5
        L_0x00a8:
            char[] r3 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r4 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r5 = r4 + 1
            r14.index = r5     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r3 = r3[r4]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r0 = r3
            if (r0 == r2) goto L_0x00c2
            if (r0 != r1) goto L_0x00b8
            goto L_0x00c2
        L_0x00b8:
            r1 = 89
            if (r0 == r1) goto L_0x00c0
            r1 = 121(0x79, float:1.7E-43)
            if (r0 != r1) goto L_0x01a5
        L_0x00c0:
            r1 = 4
            return r1
        L_0x00c2:
            r1 = 2
            return r1
        L_0x00c4:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r1 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r1 + 1
            r14.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r1]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            if (r0 == r12) goto L_0x00d2
            if (r0 != r11) goto L_0x01a5
        L_0x00d2:
            char[] r1 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r4 = r2 + 1
            r14.index = r4     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r0 = r1
            if (r0 == r6) goto L_0x00e1
            if (r0 != r3) goto L_0x01a5
        L_0x00e1:
            r1 = 8
            return r1
        L_0x00e4:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r1 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r1 + 1
            r14.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r1]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r1 = 110(0x6e, float:1.54E-43)
            r2 = 78
            if (r0 == r7) goto L_0x0118
            if (r0 == r5) goto L_0x00fd
            if (r0 == r13) goto L_0x0118
            if (r0 != r4) goto L_0x00fb
            goto L_0x00fd
        L_0x00fb:
            goto L_0x01a5
        L_0x00fd:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r3 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r4 = r3 + 1
            r14.index = r4     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            if (r0 == r2) goto L_0x0116
            if (r0 != r1) goto L_0x010c
            goto L_0x0116
        L_0x010c:
            r1 = 76
            if (r0 == r1) goto L_0x0114
            r1 = 108(0x6c, float:1.51E-43)
            if (r0 != r1) goto L_0x01a5
        L_0x0114:
            r1 = 6
            return r1
        L_0x0116:
            r1 = 5
            return r1
        L_0x0118:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r3 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r4 = r3 + 1
            r14.index = r4     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            if (r0 == r2) goto L_0x0126
            if (r0 != r1) goto L_0x01a5
        L_0x0126:
            r1 = 0
            return r1
        L_0x0128:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r1 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r1 + 1
            r14.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r1]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            if (r0 == r12) goto L_0x0136
            if (r0 != r11) goto L_0x01a5
        L_0x0136:
            char[] r1 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r3 = r2 + 1
            r14.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r0 = r1
            r1 = 66
            if (r0 == r1) goto L_0x0149
            r1 = 98
            if (r0 != r1) goto L_0x01a5
        L_0x0149:
            r1 = 1
            return r1
        L_0x014b:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r1 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r1 + 1
            r14.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r1]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            if (r0 == r12) goto L_0x0159
            if (r0 != r11) goto L_0x01a5
        L_0x0159:
            char[] r1 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r3 = r2 + 1
            r14.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r0 = r1
            if (r0 == r10) goto L_0x0168
            if (r0 != r9) goto L_0x01a5
        L_0x0168:
            r1 = 11
            return r1
        L_0x016b:
            char[] r0 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r7 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r8 = r7 + 1
            r14.index = r8     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r0 = r0[r7]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            if (r0 == r6) goto L_0x0193
            if (r0 != r3) goto L_0x017a
            goto L_0x0193
        L_0x017a:
            if (r0 == r5) goto L_0x017e
            if (r0 != r4) goto L_0x01a5
        L_0x017e:
            char[] r1 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r2 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r3 = r2 + 1
            r14.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r0 = r1
            r1 = 71
            if (r0 == r1) goto L_0x0191
            r1 = 103(0x67, float:1.44E-43)
            if (r0 != r1) goto L_0x01a5
        L_0x0191:
            r1 = 7
            return r1
        L_0x0193:
            char[] r3 = r14.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r4 = r14.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            int r5 = r4 + 1
            r14.index = r5     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            char r3 = r3[r4]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x01a4 }
            r0 = r3
            if (r0 == r2) goto L_0x01a2
            if (r0 != r1) goto L_0x01a5
        L_0x01a2:
            r1 = 3
            return r1
        L_0x01a4:
            r0 = move-exception
        L_0x01a5:
            java.text.ParseException r0 = new java.text.ParseException
            int r1 = r14.index
            java.lang.String r2 = "Bad Month"
            r0.<init>(r2, r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MailDateParser.parseMonth():int");
    }

    public int parseTimeZone() throws ParseException {
        int i = this.index;
        char[] cArr = this.orig;
        if (i < cArr.length) {
            char test = cArr[i];
            if (test == '+' || test == '-') {
                return parseNumericTimeZone();
            }
            return parseAlphaTimeZone();
        }
        throw new ParseException("No more characters", this.index);
    }

    public int parseNumericTimeZone() throws ParseException {
        boolean switchSign = false;
        char[] cArr = this.orig;
        int i = this.index;
        this.index = i + 1;
        char first = cArr[i];
        if (first == '+') {
            switchSign = true;
        } else if (first != '-') {
            throw new ParseException("Bad Numeric TimeZone", this.index);
        }
        int tz = parseNumber();
        int offset = ((tz / 100) * 60) + (tz % 100);
        if (switchSign) {
            return -offset;
        }
        return offset;
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    public int parseAlphaTimeZone() throws ParseException {
        int result;
        boolean foundCommon = false;
        try {
            char[] cArr = this.orig;
            int i = this.index;
            int i2 = i + 1;
            this.index = i2;
            switch (cArr[i]) {
                case 'C':
                case 'c':
                    result = 360;
                    foundCommon = true;
                    break;
                case 'E':
                case 'e':
                    result = 300;
                    foundCommon = true;
                    break;
                case 'G':
                case 'g':
                    char[] cArr2 = this.orig;
                    this.index = i2 + 1;
                    char curr = cArr2[i2];
                    if (curr == 'M' || curr == 'm') {
                        char[] cArr3 = this.orig;
                        int i3 = this.index;
                        this.index = i3 + 1;
                        char curr2 = cArr3[i3];
                        if (curr2 != 'T') {
                            if (curr2 == 't') {
                            }
                        }
                        result = 0;
                        break;
                    }
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                case 'M':
                case 'm':
                    result = 420;
                    foundCommon = true;
                    break;
                case 'P':
                case 'p':
                    result = NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE;
                    foundCommon = true;
                    break;
                case 'U':
                case 'u':
                    char[] cArr4 = this.orig;
                    this.index = i2 + 1;
                    char curr3 = cArr4[i2];
                    if (curr3 != 'T') {
                        if (curr3 != 't') {
                            throw new ParseException("Bad Alpha TimeZone", this.index);
                        }
                    }
                    result = 0;
                    break;
                default:
                    throw new ParseException("Bad Alpha TimeZone", this.index);
            }
            if (!foundCommon) {
                return result;
            }
            char[] cArr5 = this.orig;
            int i4 = this.index;
            this.index = i4 + 1;
            char curr4 = cArr5[i4];
            if (curr4 == 'S' || curr4 == 's') {
                char[] cArr6 = this.orig;
                int i5 = this.index;
                this.index = i5 + 1;
                char curr5 = cArr6[i5];
                if (curr5 == 'T' || curr5 == 't') {
                    return result;
                }
                throw new ParseException("Bad Alpha TimeZone", this.index);
            } else if (curr4 != 'D' && curr4 != 'd') {
                return result;
            } else {
                char[] cArr7 = this.orig;
                int i6 = this.index;
                this.index = i6 + 1;
                char curr6 = cArr7[i6];
                if (curr6 == 'T' || curr6 != 't') {
                    return result - 60;
                }
                throw new ParseException("Bad Alpha TimeZone", this.index);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParseException("Bad Alpha TimeZone", this.index);
        }
    }

    /* access modifiers changed from: package-private */
    public int getIndex() {
        return this.index;
    }
}
