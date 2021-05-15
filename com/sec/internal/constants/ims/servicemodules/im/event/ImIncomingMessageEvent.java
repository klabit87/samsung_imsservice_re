package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ImIncomingMessageEvent {
    public String mBody;
    public List<ImsUri> mCcParticipants;
    public String mChatId;
    public String mContentType;
    public ImCpimNamespaces mCpimNamespaces;
    public String mDeviceId;
    public String mDeviceName;
    public Set<NotificationStatus> mDispositionNotification;
    public boolean mExtraFt;
    public String mImdnMessageId;
    public List<ImImdnRecRoute> mImdnRecRouteList;
    public Date mImdnTime;
    public boolean mIsRoutingMsg;
    public String mOriginalToHdr;
    public ImsUri mPAssertedId;
    public Object mRawHandle;
    public ImsUri mReceiver;
    public String mReliableMessage;
    public ImsUri mRequestUri;
    public RoutingType mRoutingType;
    public ImsUri mSender;
    public String mUserAlias;

    public String toString() {
        return "ImIncomingMessageEvent [mRawHandle=" + this.mRawHandle + ", mChatId=" + this.mChatId + ", mSender=" + IMSLog.numberChecker(this.mSender) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mDeviceId=" + this.mDeviceId + ", mBody=" + IMSLog.checker(this.mBody) + ", mContentType=" + this.mContentType + ", mImdnMessageId=" + this.mImdnMessageId + ", mImdnTime=" + this.mImdnTime + ", mDispositionNotification=" + this.mDispositionNotification + ", mOriginalToHdr=" + IMSLog.checker(this.mOriginalToHdr) + ", mImdnRecRouteList=" + this.mImdnRecRouteList + ", mDeviceName=" + this.mDeviceName + ", mReliableMessage=" + this.mReliableMessage + ", mExtraFt=" + this.mExtraFt + ", mCpimNamespaces=" + this.mCpimNamespaces + ", mIsRoutingMsg=" + this.mIsRoutingMsg + ", mRequestUri= " + IMSLog.checker(this.mRequestUri) + ", mPAssertedId= " + IMSLog.checker(this.mPAssertedId) + ", mReceiver= " + IMSLog.numberChecker(this.mReceiver) + ", mRoutingType= " + this.mRoutingType + "]";
    }
}
