package javax.mail.internet;

public class HeaderTokenizer {
    private static final Token EOFToken = new Token(-4, (String) null);
    public static final String MIME = "()<>@,;:\\\"\t []/?=";
    public static final String RFC822 = "()<>@,;:\\\"\t .[]";
    private int currentPos;
    private String delimiters;
    private int maxPos;
    private int nextPos;
    private int peekPos;
    private boolean skipComments;
    private String string;

    public static class Token {
        public static final int ATOM = -1;
        public static final int COMMENT = -3;
        public static final int EOF = -4;
        public static final int QUOTEDSTRING = -2;
        private int type;
        private String value;

        public Token(int type2, String value2) {
            this.type = type2;
            this.value = value2;
        }

        public int getType() {
            return this.type;
        }

        public String getValue() {
            return this.value;
        }
    }

    public HeaderTokenizer(String header, String delimiters2, boolean skipComments2) {
        String str = header == null ? "" : header;
        this.string = str;
        this.skipComments = skipComments2;
        this.delimiters = delimiters2;
        this.peekPos = 0;
        this.nextPos = 0;
        this.currentPos = 0;
        this.maxPos = str.length();
    }

    public HeaderTokenizer(String header, String delimiters2) {
        this(header, delimiters2, true);
    }

    public HeaderTokenizer(String header) {
        this(header, RFC822);
    }

    public Token next() throws ParseException {
        this.currentPos = this.nextPos;
        Token tk = getNext();
        int i = this.currentPos;
        this.peekPos = i;
        this.nextPos = i;
        return tk;
    }

    public Token peek() throws ParseException {
        this.currentPos = this.peekPos;
        Token tk = getNext();
        this.peekPos = this.currentPos;
        return tk;
    }

    public String getRemainder() {
        return this.string.substring(this.nextPos);
    }

    private Token getNext() throws ParseException {
        char c;
        String s;
        String s2;
        if (this.currentPos >= this.maxPos) {
            return EOFToken;
        }
        if (skipWhiteSpace() == -4) {
            return EOFToken;
        }
        boolean filter = false;
        char c2 = this.string.charAt(this.currentPos);
        while (c2 == '(') {
            int start = this.currentPos + 1;
            this.currentPos = start;
            int nesting = 1;
            while (nesting > 0) {
                int i = this.currentPos;
                if (i >= this.maxPos) {
                    break;
                }
                char c3 = this.string.charAt(i);
                if (c3 == '\\') {
                    this.currentPos++;
                    filter = true;
                } else if (c3 == 13) {
                    filter = true;
                } else if (c3 == '(') {
                    nesting++;
                } else if (c3 == ')') {
                    nesting--;
                }
                this.currentPos++;
            }
            if (nesting != 0) {
                throw new ParseException("Unbalanced comments");
            } else if (!this.skipComments) {
                if (filter) {
                    s2 = filterToken(this.string, start, this.currentPos - 1);
                } else {
                    s2 = this.string.substring(start, this.currentPos - 1);
                }
                return new Token(-3, s2);
            } else if (skipWhiteSpace() == -4) {
                return EOFToken;
            } else {
                c2 = this.string.charAt(this.currentPos);
            }
        }
        if (c2 == '\"') {
            int start2 = this.currentPos + 1;
            this.currentPos = start2;
            while (true) {
                int i2 = this.currentPos;
                if (i2 < this.maxPos) {
                    char c4 = this.string.charAt(i2);
                    if (c4 == '\\') {
                        this.currentPos++;
                        filter = true;
                    } else if (c4 == 13) {
                        filter = true;
                    } else if (c4 == '\"') {
                        int i3 = this.currentPos + 1;
                        this.currentPos = i3;
                        if (filter) {
                            s = filterToken(this.string, start2, i3 - 1);
                        } else {
                            s = this.string.substring(start2, i3 - 1);
                        }
                        return new Token(-2, s);
                    }
                    this.currentPos++;
                } else {
                    throw new ParseException("Unbalanced quoted string");
                }
            }
        } else if (c2 < ' ' || c2 >= 127 || this.delimiters.indexOf(c2) >= 0) {
            this.currentPos++;
            return new Token(c2, new String(new char[]{c2}));
        } else {
            int start3 = this.currentPos;
            while (true) {
                int i4 = this.currentPos;
                if (i4 < this.maxPos && (c = this.string.charAt(i4)) >= ' ' && c < 127 && c != '(' && c != ' ' && c != '\"' && this.delimiters.indexOf(c) < 0) {
                    this.currentPos++;
                }
            }
            return new Token(-1, this.string.substring(start3, this.currentPos));
        }
    }

    private int skipWhiteSpace() {
        while (true) {
            int i = this.currentPos;
            if (i >= this.maxPos) {
                return -4;
            }
            char charAt = this.string.charAt(i);
            char c = charAt;
            if (charAt != ' ' && c != 9 && c != 13 && c != 10) {
                return this.currentPos;
            }
            this.currentPos++;
        }
    }

    private static String filterToken(String s, int start, int end) {
        StringBuffer sb = new StringBuffer();
        boolean gotEscape = false;
        boolean gotCR = false;
        for (int i = start; i < end; i++) {
            char c = s.charAt(i);
            if (c != 10 || !gotCR) {
                gotCR = false;
                if (gotEscape) {
                    sb.append(c);
                    gotEscape = false;
                } else if (c == '\\') {
                    gotEscape = true;
                } else if (c == 13) {
                    gotCR = true;
                } else {
                    sb.append(c);
                }
            } else {
                gotCR = false;
            }
        }
        return sb.toString();
    }
}
