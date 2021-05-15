package com.sec.internal.log;

import java.io.PrintWriter;
import java.io.Writer;

public class IndentingPrintWriter extends PrintWriter {
    private char[] mCurrentIndent;
    private int mCurrentLength;
    private boolean mEmptyLine;
    private StringBuilder mIndentBuilder;
    private char[] mSingleChar;
    private final String mSingleIndent;
    private final int mWrapLength;

    public IndentingPrintWriter(Writer writer, String singleIndent) {
        this(writer, singleIndent, -1);
    }

    public IndentingPrintWriter(Writer writer, String singleIndent, int wrapLength) {
        super(writer);
        this.mIndentBuilder = new StringBuilder();
        this.mEmptyLine = true;
        this.mSingleChar = new char[1];
        this.mSingleIndent = singleIndent;
        this.mWrapLength = wrapLength;
    }

    public void increaseIndent() {
        this.mIndentBuilder.append(this.mSingleIndent);
        this.mCurrentIndent = null;
    }

    public void decreaseIndent() {
        this.mIndentBuilder.delete(0, this.mSingleIndent.length());
        this.mCurrentIndent = null;
    }

    public void println() {
        write(10);
    }

    public void write(int c) {
        char[] cArr = this.mSingleChar;
        cArr[0] = (char) c;
        write(cArr, 0, 1);
    }

    public void write(String s, int off, int len) {
        char[] buf = new char[len];
        s.getChars(off, len - off, buf, 0);
        write(buf, 0, len);
    }

    public void write(char[] buf, int offset, int count) {
        int indentLength = this.mIndentBuilder.length();
        int bufferEnd = offset + count;
        int lineStart = offset;
        int lineEnd = offset;
        while (lineEnd < bufferEnd) {
            int lineEnd2 = lineEnd + 1;
            char ch = buf[lineEnd];
            this.mCurrentLength++;
            if (ch == 10) {
                maybeWriteIndent();
                super.write(buf, lineStart, lineEnd2 - lineStart);
                lineStart = lineEnd2;
                this.mEmptyLine = true;
                this.mCurrentLength = 0;
            }
            int i = this.mWrapLength;
            if (i > 0 && this.mCurrentLength >= i - indentLength) {
                if (!this.mEmptyLine) {
                    super.write(10);
                    this.mEmptyLine = true;
                    this.mCurrentLength = lineEnd2 - lineStart;
                } else {
                    maybeWriteIndent();
                    super.write(buf, lineStart, lineEnd2 - lineStart);
                    super.write(10);
                    this.mEmptyLine = true;
                    lineStart = lineEnd2;
                    this.mCurrentLength = 0;
                }
            }
            lineEnd = lineEnd2;
        }
        if (lineStart != lineEnd) {
            maybeWriteIndent();
            super.write(buf, lineStart, lineEnd - lineStart);
        }
    }

    private void maybeWriteIndent() {
        if (this.mEmptyLine) {
            this.mEmptyLine = false;
            if (this.mIndentBuilder.length() != 0) {
                if (this.mCurrentIndent == null) {
                    this.mCurrentIndent = this.mIndentBuilder.toString().toCharArray();
                }
                char[] cArr = this.mCurrentIndent;
                super.write(cArr, 0, cArr.length);
            }
        }
    }
}
