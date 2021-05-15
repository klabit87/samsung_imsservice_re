package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class SendImdnParams {
    public final Message mCallback;
    public final String mChatId;
    public String mContributionId;
    public String mConversationId;
    public final Date mCpimDate;
    public final String mDeviceId;
    public Map<String, String> mImExtensionMNOHeaders;
    public final List<ImdnData> mImdnDataList;
    public boolean mIsBotSessionAnonymized;
    public boolean mIsGroupChat;
    public String mOwnImsi;
    public final Object mRawHandle;
    public final ImsUri mUri;

    public static class ImdnData {
        public final Date mImdnDate;
        public final String mImdnId;
        public final String mImdnOriginalTo;
        public final List<ImImdnRecRoute> mImdnRecRouteList;
        public final NotificationStatus mStatus;

        public ImdnData(NotificationStatus status, String imdnId, Date receivedMessageDate, List<ImImdnRecRoute> imdnRecRouteList, String imdnOriginalTo) {
            this.mStatus = status;
            this.mImdnId = imdnId;
            this.mImdnDate = receivedMessageDate;
            this.mImdnRecRouteList = imdnRecRouteList;
            this.mImdnOriginalTo = imdnOriginalTo;
        }

        public String toString() {
            return "ImdnData [mStatus=" + this.mStatus + ", mImdnId=" + this.mImdnId + ", mImdnDate=" + this.mImdnDate + ", mImdnRecRouteList=" + this.mImdnRecRouteList + ", mImdnOriginalTo=" + IMSLog.checker(this.mImdnOriginalTo) + "]";
        }
    }

    public SendImdnParams(Object rawHandle, ImsUri uri, String chatId, String conversationId, String contributionId, String ownImsi, Message callback, String deviceId, ImdnData singleImdnData, boolean isGroupChat, Date cpimDate, boolean isBotSessionAnonymized) {
        this(rawHandle, uri, chatId, conversationId, contributionId, ownImsi, callback, deviceId, (List<ImdnData>) Collections.singletonList(singleImdnData), isGroupChat, cpimDate, isBotSessionAnonymized);
    }

    public SendImdnParams(Object rawHandle, ImsUri uri, String chatId, String conversationId, String contributionId, String ownImsi, Message callback, String deviceId, List<ImdnData> imdnDataList, boolean isGroupChat, Date cpimDate, boolean isBotSessionAnonymized) {
        this.mRawHandle = rawHandle;
        this.mUri = uri;
        this.mChatId = chatId;
        this.mConversationId = conversationId;
        this.mContributionId = contributionId;
        this.mOwnImsi = ownImsi;
        this.mCallback = callback;
        this.mDeviceId = deviceId;
        this.mImdnDataList = imdnDataList;
        this.mIsGroupChat = isGroupChat;
        this.mIsBotSessionAnonymized = isBotSessionAnonymized;
        this.mCpimDate = cpimDate;
    }

    public void addImExtensionMNOHeaders(Map<String, String> headers) {
        this.mImExtensionMNOHeaders = headers;
    }

    public String toString() {
        return "SendImdnParams [mRawHandle=" + this.mRawHandle + ", mUri=" + IMSLog.checker(this.mUri) + ", mChatId=" + this.mChatId + ", mConversationId=" + this.mConversationId + ", mContributionId=" + this.mContributionId + ", mImdnDataList=" + this.mImdnDataList + ", mDeviceId=" + IMSLog.checker(this.mDeviceId) + ", mImExtensionMNOHeaders=" + this.mImExtensionMNOHeaders + ", mCallback=" + this.mCallback + ", mIsGroupChat=" + this.mIsGroupChat + ", mCpimDate=" + this.mCpimDate + "]";
    }
}
