package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.constants.ims.servicemodules.im.ImError;

public class ChatbotAnonymizeRespEvent {
    public String mChatbotUri;
    public String mCommandId;
    public ImError mError;
    public int mRetryAfter;

    public ChatbotAnonymizeRespEvent(String chatbotUri, ImError error, String commandId, int retryAfter) {
        this.mChatbotUri = chatbotUri;
        this.mError = error;
        this.mCommandId = commandId;
        this.mRetryAfter = retryAfter;
    }

    public String toString() {
        return "ChatbotAnonymizeRespEvent [mError = " + this.mError + ", mCommandId = " + this.mCommandId + ", mretryAfter = " + this.mRetryAfter + "]";
    }
}
