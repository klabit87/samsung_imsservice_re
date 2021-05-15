package com.sec.internal.constants.ims.servicemodules.im.params;

import com.sec.ims.util.ImsUri;

public class ChatbotAnonymizeParams {
    public String mAliasXml;
    public ImsUri mChatbotUri;
    public String mCommandId;
    public int mPhoneId;

    public ChatbotAnonymizeParams(int phoneId, ImsUri chatbotUri, String aliasXml, String commandId) {
        this.mPhoneId = phoneId;
        this.mChatbotUri = chatbotUri;
        this.mAliasXml = aliasXml;
        this.mCommandId = commandId;
    }

    public String toString() {
        return "ChatbotAnonymizeParams [chatbotURL=" + this.mChatbotUri.toString() + ", PhoneId = " + this.mPhoneId + ", mChatbotUri = " + this.mChatbotUri + ", mAliasXml = " + this.mAliasXml + ", mCommandId = " + this.mCommandId + "]";
    }
}
