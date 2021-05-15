package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Set;

public class SendMessageParams {
    public String mBody;
    public Message mCallback;
    public String mContentType;
    public String mDeviceName;
    public Set<NotificationStatus> mDispositionNotification;
    public boolean mExtraFt;
    public Set<ImsUri> mGroupCcList;
    public String mImdnMessageId;
    public Date mImdnTime;
    public String mMaapTrafficType;
    public Object mRawHandle;
    public String mReferenceId;
    public String mReferenceType;
    public String mReferenceValue;
    public String mReliableMessage;
    public String mUserAlias;
    public String mXmsMessage;

    public SendMessageParams(Object rawHandle, String body, String userAlias, String contentType, String imdnMessageId, Date imdnTime, Set<NotificationStatus> dispositionNotification, String deviceName, String reliableMessage, boolean extraFt, String xmsMessage, Set<ImsUri> groupCcList, Message callback, String maapTrafficType, String referenceId, String referenceType, String referenceValue) {
        this.mRawHandle = rawHandle;
        this.mBody = body;
        this.mUserAlias = userAlias;
        this.mContentType = contentType;
        this.mImdnMessageId = imdnMessageId;
        this.mImdnTime = imdnTime;
        this.mCallback = callback;
        this.mDispositionNotification = dispositionNotification;
        this.mDeviceName = deviceName;
        this.mReliableMessage = reliableMessage;
        this.mExtraFt = extraFt;
        this.mXmsMessage = xmsMessage;
        this.mGroupCcList = groupCcList;
        this.mMaapTrafficType = maapTrafficType;
        this.mReferenceId = referenceId;
        this.mReferenceType = referenceType;
        this.mReferenceValue = referenceValue;
    }

    public String toString() {
        return "SendMessageParams [mRawHandle=" + this.mRawHandle + ", mBody=" + IMSLog.checker(this.mBody) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mContentType=" + this.mContentType + ", mImdnMessageId=" + this.mImdnMessageId + ", mImdnTime=" + this.mImdnTime + ", mDispositionNotification=" + this.mDispositionNotification + ", mDeviceName=" + this.mDeviceName + ", mReliableMessage=" + this.mReliableMessage + ", mExtraFt=" + this.mExtraFt + ", mXmsMessage=" + this.mXmsMessage + ", mMaapTrafficType=" + this.mMaapTrafficType + ", mReferenceId=" + this.mReferenceId + ", mReferenceType=" + this.mReferenceType + ", mReferenceValue=" + this.mReferenceValue + "]";
    }
}
