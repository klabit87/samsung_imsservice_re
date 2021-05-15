package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;

public final class SendMessageRevokeParams {
    public final Message mCallback;
    public String mContributionId;
    public String mConversationId;
    public final String mImdnId;
    public String mOwnImsi;
    public final ImsUri mUri;

    public SendMessageRevokeParams(ImsUri uri, String imdnId, Message callback, String conversationId, String contributionId, String ownImsi) {
        this.mUri = uri;
        this.mImdnId = imdnId;
        this.mCallback = callback;
        this.mConversationId = conversationId;
        this.mContributionId = contributionId;
        this.mOwnImsi = ownImsi;
    }

    public String toString() {
        return "SendRevokeParams [mUri=" + this.mUri + ", mImdnId=" + this.mImdnId + ", mCallback=" + this.mCallback + ", mConversationId=" + this.mConversationId + ", mContributionId=" + this.mContributionId + "]";
    }
}
