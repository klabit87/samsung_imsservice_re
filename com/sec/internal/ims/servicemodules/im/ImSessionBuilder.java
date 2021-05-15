package com.sec.internal.ims.servicemodules.im;

import android.os.Looper;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.im.interfaces.IGetter;
import com.sec.internal.ims.servicemodules.im.listener.ImSessionListener;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImSessionBuilder {
    public List<String> mAcceptTypes = new ArrayList();
    public List<String> mAcceptWrappedTypes = new ArrayList();
    public ChatData mChatData;
    public String mChatId;
    public ChatMode mChatMode;
    public ChatData.ChatType mChatType;
    public ImConfig mConfig;
    public String mContributionId;
    public String mConversationId;
    public ImDirection mDirection = ImDirection.IRRELEVANT;
    public IGetter mGetter;
    public String mIconPath;
    public IImServiceInterface mImsService;
    public ImSessionListener mListener;
    public Looper mLooper;
    public final Map<String, Integer> mNeedToRevokeMessages = new HashMap();
    public String mOwnGroupAlias;
    public String mOwnIMSI;
    public String mOwnNumber;
    public final Map<ImsUri, ImParticipant> mParticipants = new HashMap();
    public final Set<ImsUri> mParticipantsUri = new HashSet();
    public Object mRawHandle;
    public String mRequestMessageId;
    public String mSdpContentType;
    public String mServiceId;
    public ImIncomingSessionEvent.ImSessionType mSessionType;
    public ImsUri mSessionUri;
    public ISlmServiceInterface mSlmService;
    public String mSubject;
    public int mThreadId = -1;
    public UriGenerator mUriGenerator;

    public ImSessionBuilder listener(ImSessionListener listener) {
        this.mListener = listener;
        return this;
    }

    public ImSessionBuilder looper(Looper looper) {
        this.mLooper = looper;
        return this;
    }

    public ImSessionBuilder imsService(IImServiceInterface imsService) {
        this.mImsService = imsService;
        return this;
    }

    public ImSessionBuilder slmService(ISlmServiceInterface slmService) {
        this.mSlmService = slmService;
        return this;
    }

    public ImSessionBuilder config(ImConfig config) {
        this.mConfig = config;
        return this;
    }

    public ImSessionBuilder uriGenerator(UriGenerator uriGenerator) {
        this.mUriGenerator = uriGenerator;
        return this;
    }

    public ImSessionBuilder chatId(String chatId) {
        this.mChatId = chatId;
        return this;
    }

    public ImSessionBuilder chatData(ChatData chatData) {
        this.mChatData = chatData;
        return this;
    }

    public ImSessionBuilder chatType(ChatData.ChatType chatType) {
        this.mChatType = chatType;
        return this;
    }

    public ImSessionBuilder chatMode(ChatMode chatMode) {
        this.mChatMode = chatMode;
        return this;
    }

    public ImSessionBuilder sessionType(ImIncomingSessionEvent.ImSessionType type) {
        this.mSessionType = type;
        return this;
    }

    public ImSessionBuilder participantsUri(Collection<ImsUri> participants) {
        this.mParticipantsUri.addAll(participants);
        return this;
    }

    public ImSessionBuilder participants(Map<ImsUri, ImParticipant> participants) {
        this.mParticipants.putAll(participants);
        return this;
    }

    public ImSessionBuilder contributionId(String contributionId) {
        this.mContributionId = contributionId;
        return this;
    }

    public ImSessionBuilder conversationId(String conversationId) {
        this.mConversationId = conversationId;
        return this;
    }

    public ImSessionBuilder rawHandle(Object rawHandle) {
        this.mRawHandle = rawHandle;
        return this;
    }

    public ImSessionBuilder subject(String subject) {
        this.mSubject = subject;
        return this;
    }

    public ImSessionBuilder iconPath(String iconPath) {
        this.mIconPath = iconPath;
        return this;
    }

    public ImSessionBuilder threadId(int threadId) {
        this.mThreadId = threadId;
        return this;
    }

    public ImSessionBuilder ownPhoneNum(String ownNum) {
        this.mOwnNumber = ownNum;
        return this;
    }

    public ImSessionBuilder ownSimIMSI(String imsi) {
        this.mOwnIMSI = imsi;
        return this;
    }

    public ImSessionBuilder ownGroupAlias(String alias) {
        this.mOwnGroupAlias = alias;
        return this;
    }

    public ImSessionBuilder sdpContentType(String sdpContentType) {
        this.mSdpContentType = sdpContentType;
        return this;
    }

    public ImSessionBuilder requestMessageId(String requestMessageId) {
        this.mRequestMessageId = requestMessageId;
        return this;
    }

    public ImSessionBuilder direction(ImDirection direction) {
        this.mDirection = direction;
        return this;
    }

    public ImSessionBuilder getter(IGetter getter) {
        this.mGetter = getter;
        return this;
    }

    public ImSessionBuilder serviceId(String serviceId) {
        this.mServiceId = serviceId;
        return this;
    }

    public ImSessionBuilder acceptTypes(List<String> acceptTypes) {
        this.mAcceptTypes = acceptTypes;
        return this;
    }

    public ImSessionBuilder acceptWrappedTypes(List<String> acceptTypes) {
        this.mAcceptWrappedTypes = acceptTypes;
        return this;
    }

    public ImSessionBuilder needToRevokeMessages(Map<String, Integer> needToRevokeMessages) {
        this.mNeedToRevokeMessages.putAll(needToRevokeMessages);
        return this;
    }

    public ImSessionBuilder sessionUri(ImsUri sessionUri) {
        this.mSessionUri = sessionUri;
        return this;
    }

    public ImSession build() {
        Preconditions.checkNotNull(this.mLooper);
        Preconditions.checkNotNull(this.mListener);
        Preconditions.checkNotNull(this.mGetter);
        Preconditions.checkNotNull(this.mImsService);
        Preconditions.checkNotNull(this.mSlmService);
        Preconditions.checkNotNull(this.mConfig);
        if (this.mChatId == null && this.mChatData == null) {
            throw new IllegalArgumentException("mChatId is null");
        }
        if (this.mChatType == null && this.mChatData == null) {
            boolean isGroupChat = true;
            if (this.mParticipantsUri.size() + this.mParticipants.size() <= 1 && this.mSessionType != ImIncomingSessionEvent.ImSessionType.CONFERENCE) {
                isGroupChat = false;
            }
            this.mChatType = isGroupChat ? ChatData.ChatType.REGULAR_GROUP_CHAT : ChatData.ChatType.ONE_TO_ONE_CHAT;
        }
        return new ImSession(this);
    }
}
