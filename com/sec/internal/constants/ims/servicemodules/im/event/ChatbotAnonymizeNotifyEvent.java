package com.sec.internal.constants.ims.servicemodules.im.event;

public class ChatbotAnonymizeNotifyEvent {
    public String mChatbotUri;
    public String mCommandId;
    public String mResult;

    public ChatbotAnonymizeNotifyEvent(String chatbotUri, String result, String commandId) {
        this.mChatbotUri = chatbotUri;
        this.mResult = result;
        this.mCommandId = commandId;
    }

    public String toString() {
        return "ChatbotAnonymizeNotifyEvent   [mResult = " + this.mResult + ", mCommandId = " + this.mCommandId + "]";
    }
}
