package com.sec.internal.ims.entitlement.softphone.requests;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SendSMSRequest {
    @SerializedName("to")
    public List<String> mCalleeNumber;
    @SerializedName("messageContent")
    public List<MessageContent> mMessageContent = new ArrayList();
    @SerializedName("replyAll")
    public boolean mReplyAll;

    public static class MessageContent {
        @SerializedName("body")
        public String mContent;
        @SerializedName("contentType")
        public String mContentType = "text/plain";
        @SerializedName("contentTransferEncoding")
        public String mEncoding = "8bit";

        public MessageContent(String text) {
            this.mContent = text;
        }

        public String toString() {
            return "MessageContent [mContentType = " + this.mContentType + ", mEncoding = " + this.mEncoding + ", mContent = " + this.mContent + "]";
        }
    }

    public SendSMSRequest(boolean replyAll, String text, String calleeNumber) {
        ArrayList arrayList = new ArrayList();
        this.mCalleeNumber = arrayList;
        this.mReplyAll = replyAll;
        arrayList.add(calleeNumber);
        this.mMessageContent.add(new MessageContent(text));
    }

    public String toString() {
        return "SendSMSRequest [mReplyAll = " + this.mReplyAll + ", mMessageContent.size = " + this.mMessageContent.size() + ", mCalleeNumber.size = " + this.mCalleeNumber.size() + "]";
    }
}
