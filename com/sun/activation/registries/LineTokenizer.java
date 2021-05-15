package com.sun.activation.registries;

import java.util.NoSuchElementException;
import java.util.Vector;

/* compiled from: MimeTypeFile */
class LineTokenizer {
    private static final String singles = "=";
    private int currentPosition = 0;
    private int maxPosition;
    private Vector stack = new Vector();
    private String str;

    public LineTokenizer(String str2) {
        this.str = str2;
        this.maxPosition = str2.length();
    }

    private void skipWhiteSpace() {
        while (true) {
            int i = this.currentPosition;
            if (i < this.maxPosition && Character.isWhitespace(this.str.charAt(i))) {
                this.currentPosition++;
            } else {
                return;
            }
        }
    }

    public boolean hasMoreTokens() {
        if (this.stack.size() > 0) {
            return true;
        }
        skipWhiteSpace();
        if (this.currentPosition < this.maxPosition) {
            return true;
        }
        return false;
    }

    public String nextToken() {
        int size = this.stack.size();
        if (size > 0) {
            String t = (String) this.stack.elementAt(size - 1);
            this.stack.removeElementAt(size - 1);
            return t;
        }
        skipWhiteSpace();
        if (this.currentPosition < this.maxPosition) {
            int start = this.currentPosition;
            char c = this.str.charAt(start);
            if (c != '\"') {
                if ("=".indexOf(c) < 0) {
                    while (true) {
                        int i = this.currentPosition;
                        if (i >= this.maxPosition || "=".indexOf(this.str.charAt(i)) >= 0 || Character.isWhitespace(this.str.charAt(this.currentPosition))) {
                            break;
                        }
                        this.currentPosition++;
                    }
                } else {
                    this.currentPosition++;
                }
            } else {
                this.currentPosition++;
                boolean filter = false;
                while (true) {
                    int i2 = this.currentPosition;
                    if (i2 >= this.maxPosition) {
                        break;
                    }
                    String str2 = this.str;
                    this.currentPosition = i2 + 1;
                    char c2 = str2.charAt(i2);
                    if (c2 == '\\') {
                        this.currentPosition++;
                        filter = true;
                    } else if (c2 == '\"') {
                        if (!filter) {
                            return this.str.substring(start + 1, this.currentPosition - 1);
                        }
                        StringBuffer sb = new StringBuffer();
                        for (int i3 = start + 1; i3 < this.currentPosition - 1; i3++) {
                            char c3 = this.str.charAt(i3);
                            if (c3 != '\\') {
                                sb.append(c3);
                            }
                        }
                        return sb.toString();
                    }
                }
            }
            return this.str.substring(start, this.currentPosition);
        }
        throw new NoSuchElementException();
    }

    public void pushToken(String token) {
        this.stack.addElement(token);
    }
}
