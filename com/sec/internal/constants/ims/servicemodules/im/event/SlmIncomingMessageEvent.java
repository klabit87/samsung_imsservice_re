package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SlmIncomingMessageEvent {
    public String mBody;
    public String mContentType;
    public String mContributionId;
    public String mConversationId;
    public ImCpimNamespaces mCpimNamespaces;
    public String mDeviceName;
    public Set<NotificationStatus> mDispositionNotification;
    public boolean mExtraFt;
    public Map<String, String> mImExtensionMNOHeaders;
    public String mImdnMessageId;
    public List<ImImdnRecRoute> mImdnRecRouteList;
    public Date mImdnTime;
    public boolean mIsChatbotRole;
    public boolean mIsLMM;
    public boolean mIsPublicAccountMsg;
    public boolean mIsRoutingMsg;
    public boolean mIsTokenUsed;
    public String mOriginalToHdr;
    public String mOwnImsi;
    public ImsUri mPAssertedId;
    public List<ImsUri> mParticipants;
    public ImsUri mReceiver;
    public String mReliableMessage;
    public ImsUri mRequestUri;
    public RoutingType mRoutingType;
    public ImsUri mSender;
    public String mUserAlias;

    public String toString() {
        return "SlmIncomingMessageEvent [mSender=" + IMSLog.checker(this.mSender) + ", mParticipants=" + IMSLog.checker(this.mParticipants) + ", mContributionId=" + this.mContributionId + ", mConversationId=" + this.mConversationId + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mBody=" + IMSLog.checker(this.mBody) + ", mContentType=" + this.mContentType + ", mImdnMessageId=" + this.mImdnMessageId + ", mImdnTime=" + this.mImdnTime + ", mDispositionNotification=" + this.mDispositionNotification + ", mOriginalToHdr=" + IMSLog.checker(this.mOriginalToHdr) + ", mImdnRecRouteList=" + this.mImdnRecRouteList + ", mImExtensionMNOHeaders=" + this.mImExtensionMNOHeaders + ", mIsPublicAccountMsg=" + this.mIsPublicAccountMsg + ", mDeviceName=" + this.mDeviceName + ", mReliableMessage=" + this.mReliableMessage + ", mExtraFt=" + this.mExtraFt + ", mCpimNamespaces=" + this.mCpimNamespaces + ", mIsRoutingMsg= " + this.mIsRoutingMsg + ", mRequestUri= " + IMSLog.checker(this.mRequestUri) + ", mPAssertedId= " + IMSLog.checker(this.mPAssertedId) + ", mReceiver= " + IMSLog.checker(this.mReceiver) + ", mRoutingType= " + this.mRoutingType + ", mIsLMM= " + this.mIsLMM + ", mIsChatbotRole= " + this.mIsChatbotRole + ", mIsTokenUsed= " + this.mIsTokenUsed + "]";
    }
}
