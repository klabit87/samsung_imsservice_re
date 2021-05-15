package com.sec.internal.ims.servicemodules.volte2.data;

public class TextInfo {
    private final int mSessionId;
    private final String mText;
    private final int mTextLen;

    public TextInfo(int sessionId, String text, int textLen) {
        this.mSessionId = sessionId;
        this.mText = text;
        this.mTextLen = textLen;
    }

    public String getText() {
        return this.mText;
    }

    public int getTextLen() {
        return this.mTextLen;
    }

    public int getSessionId() {
        return this.mSessionId;
    }
}
