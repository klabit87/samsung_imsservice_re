package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Observable;

public class ChatData extends Observable {
    private final String mChatId;
    private ChatMode mChatMode = ChatMode.OFF;
    private ChatType mChatType = ChatType.ONE_TO_ONE_CHAT;
    private String mContributionId;
    private String mConversationId;
    private ImDirection mDirection;
    private ImIconData mIconData;
    private final String mIconPath;
    private int mId;
    private long mInsertedTimeStamp;
    private boolean mIsChatbotRole;
    private boolean mIsIconUpdateRequiredOnSessionEstablished;
    private boolean mIsMuted;
    private boolean mIsReusable = true;
    private final int mMaxParticipantCount;
    private String mOwnGroupAlias;
    private String mOwnIMSI;
    private String mOwnNumber;
    private ImsUri mSessionUri;
    private State mState = State.NONE;
    private String mSubject;
    private ImSubjectData mSubjectData;

    public ChatData(String chatId, String ownNumber, String alias, String subject, ChatType chatType, ImDirection direction, String conversationId, String contributionId, String ownIMSI, String iconPath, ChatMode chatMode, ImsUri sessionUri) {
        this.mChatId = chatId;
        this.mChatType = chatType;
        this.mOwnNumber = ownNumber;
        this.mOwnGroupAlias = alias;
        this.mSubject = subject;
        this.mIconPath = iconPath;
        this.mMaxParticipantCount = 100;
        this.mDirection = direction;
        this.mConversationId = conversationId;
        this.mContributionId = contributionId;
        this.mInsertedTimeStamp = System.currentTimeMillis();
        this.mOwnIMSI = ownIMSI;
        this.mChatMode = chatMode;
        this.mSessionUri = sessionUri;
        if (subject != null) {
            this.mSubjectData = new ImSubjectData(subject, (ImsUri) null, (Date) null);
        }
    }

    public ChatData(int id, String chatId, String ownNumber, String alias, ChatType chatType, State state, String subject, boolean isMuted, int maxParticipantCount, ImDirection direction, String conversationId, String contributionId, ImsUri sessionUri, boolean isReusable, long timeStamp, String ownIMSI, ImsUri subjectParticipant, Date subjectTimestamp, String iconPath, ImsUri iconParticipant, Date iconTimestamp, String iconUri, boolean isChatbotRole, ChatMode chatMode) {
        String str = subject;
        ImsUri imsUri = subjectParticipant;
        Date date = subjectTimestamp;
        String str2 = iconPath;
        this.mId = id;
        this.mChatId = chatId;
        this.mOwnNumber = ownNumber;
        this.mOwnGroupAlias = alias;
        this.mChatType = chatType;
        this.mState = state;
        this.mSubject = str;
        this.mIsMuted = isMuted;
        this.mMaxParticipantCount = maxParticipantCount;
        this.mDirection = direction;
        this.mConversationId = conversationId;
        this.mContributionId = contributionId;
        this.mSessionUri = sessionUri;
        this.mIsReusable = isReusable;
        this.mInsertedTimeStamp = timeStamp;
        this.mOwnIMSI = ownIMSI;
        this.mIconPath = str2;
        this.mIsChatbotRole = isChatbotRole;
        this.mChatMode = chatMode;
        if (!(str == null && imsUri == null && date == null)) {
            this.mSubjectData = new ImSubjectData(str, imsUri, date);
        }
        if (str2 != null || iconParticipant != null || iconTimestamp != null) {
            ImIconData imIconData = r4;
            ImIconData imIconData2 = new ImIconData(iconUri == null ? ImIconData.IconType.ICON_TYPE_FILE : ImIconData.IconType.ICON_TYPE_URI, iconParticipant, iconTimestamp, iconPath, iconUri);
            this.mIconData = imIconData2;
        }
    }

    public enum ChatType implements IEnumerationWithId<ChatType> {
        ONE_TO_ONE_CHAT(0),
        REGULAR_GROUP_CHAT(1),
        PARTICIPANT_BASED_GROUP_CHAT(3),
        ONE_TO_MANY_CHAT(4);
        
        private static final ReverseEnumMap<ChatType> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(ChatType.class);
        }

        private ChatType(int id2) {
            this.id = id2;
        }

        public int getId() {
            return this.id;
        }

        public ChatType getFromId(int id2) {
            return fromId(id2);
        }

        public static ChatType fromId(int id2) {
            ChatType chatType = ONE_TO_ONE_CHAT;
            try {
                return map.get(Integer.valueOf(id2));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return chatType;
            }
        }

        public static boolean isGroupChat(ChatType chatType) {
            return chatType != ONE_TO_ONE_CHAT;
        }

        public static boolean isGroupChatIdBasedGroupChat(ChatType chatType) {
            return chatType == REGULAR_GROUP_CHAT;
        }

        public static boolean isClosedGroupChat(ChatType chatType) {
            return chatType == PARTICIPANT_BASED_GROUP_CHAT;
        }
    }

    public ChatType getChatType() {
        return this.mChatType;
    }

    public void updateChatType(ChatType chatType) {
        if (this.mChatType != chatType) {
            this.mChatType = chatType;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public ChatMode getChatMode() {
        return this.mChatMode;
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public ImDirection getDirection() {
        return this.mDirection;
    }

    public String getChatId() {
        return this.mChatId;
    }

    public String getOwnPhoneNum() {
        return this.mOwnNumber;
    }

    public String getOwnIMSI() {
        return this.mOwnIMSI;
    }

    public String getConversationId() {
        return this.mConversationId;
    }

    public String getContributionId() {
        return this.mContributionId;
    }

    public ImsUri getSessionUri() {
        return this.mSessionUri;
    }

    public boolean isGroupChat() {
        return ChatType.isGroupChat(this.mChatType);
    }

    public void updateIsMuted(boolean value) {
        if (this.mIsMuted != value) {
            this.mIsMuted = value;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public String getOwnGroupAlias() {
        return this.mOwnGroupAlias;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }

    public void setContributionId(String contributionId) {
        this.mContributionId = contributionId;
    }

    public void setConversationId(String conversationId) {
        this.mConversationId = conversationId;
    }

    public void setSessionUri(ImsUri uri) {
        this.mSessionUri = uri;
    }

    public void setOwnPhoneNum(String ownNumber) {
        this.mOwnNumber = ownNumber;
    }

    public void setOwnIMSI(String ownIMSI) {
        if (ownIMSI != null && !ownIMSI.equals(this.mOwnIMSI)) {
            this.mOwnIMSI = ownIMSI;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void setDirection(ImDirection direction) {
        if (direction != null && direction != this.mDirection) {
            this.mDirection = direction;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public State getState() {
        return this.mState;
    }

    public String getSubject() {
        return this.mSubject;
    }

    public ImSubjectData getSubjectData() {
        return this.mSubjectData;
    }

    public String getIconPath() {
        return this.mIconPath;
    }

    public ImIconData getIconData() {
        return this.mIconData;
    }

    public void setIconUpdatedRequiredOnSessionEstablished(boolean iconUpdateRequired) {
        this.mIsIconUpdateRequiredOnSessionEstablished = iconUpdateRequired;
    }

    public boolean isIconUpdatedRequiredOnSessionEstablished() {
        return this.mIsIconUpdateRequiredOnSessionEstablished;
    }

    public boolean isMuted() {
        return this.mIsMuted;
    }

    public boolean isReusable() {
        return this.mIsReusable;
    }

    public boolean isChatbotRole() {
        return this.mIsChatbotRole;
    }

    public enum State implements IEnumerationWithId<State> {
        NONE(-1),
        ACTIVE(1),
        INACTIVE(0),
        CLOSED_BY_USER(2),
        CLOSED_INVOLUNTARILY(3),
        CLOSED_VOLUNTARILY(4);
        
        private static final ReverseEnumMap<State> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(State.class);
        }

        private State(int id2) {
            this.id = id2;
        }

        public int getId() {
            return this.id;
        }

        public State getFromId(int id2) {
            return fromId(id2);
        }

        public static State fromId(int id2) {
            State state = CLOSED_BY_USER;
            try {
                return map.get(Integer.valueOf(id2));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return state;
            }
        }
    }

    public void updateState(State state) {
        if (state != this.mState) {
            this.mState = state;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateSubject(String subject) {
        if (subject != null && !subject.equals(this.mSubject)) {
            this.mSubject = subject;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateSubjectData(ImSubjectData subjectData) {
        if (subjectData != null) {
            this.mSubjectData = subjectData;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateIconData(ImIconData iconData) {
        this.mIconData = iconData;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public void updateOwnGroupAlias(String alias) {
        if (alias != null && !alias.equals(this.mOwnGroupAlias)) {
            this.mOwnGroupAlias = alias;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateIsReusable(boolean isReusable) {
        if (isReusable != this.mIsReusable) {
            this.mIsReusable = isReusable;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateIsChatbotRole(boolean isChatbotRole) {
        if (isChatbotRole != this.mIsChatbotRole) {
            this.mIsChatbotRole = isChatbotRole;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void triggerObservers(ImCacheAction action) {
        setChanged();
        notifyObservers(action);
    }

    public int getMaxParticipantsCount() {
        return this.mMaxParticipantCount;
    }

    public int hashCode() {
        int i = 31 * 1;
        String str = this.mChatId;
        return (31 * (i + (str == null ? 0 : str.hashCode()))) + this.mId;
    }

    public String toString() {
        return "ChatData [mId=" + this.mId + ", mChatId=" + this.mChatId + ", mOwnNumber=" + IMSLog.checker(this.mOwnNumber) + ", mChatType=" + this.mChatType + ", mState=" + this.mState + ", mSubject=" + IMSLog.checker(this.mSubject) + ", mIsMuted=" + this.mIsMuted + ", mMaxParticipantCount=" + this.mMaxParticipantCount + ", mConversationId=" + this.mConversationId + ", mContributionId=" + this.mContributionId + ", mDirection=" + this.mDirection + ", mIsReusable=" + this.mIsReusable + ", mInsertedTimeStamp=" + this.mInsertedTimeStamp + ", mOwnIMSI=" + IMSLog.checker(this.mOwnIMSI) + ", mIsChatbotRole=" + this.mIsChatbotRole + ", mChatMode=" + this.mChatMode + "]";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChatData other = (ChatData) obj;
        String str = this.mChatId;
        if (str == null) {
            if (other.mChatId != null) {
                return false;
            }
        } else if (!str.equals(other.mChatId)) {
            return false;
        }
        if (this.mId == other.mId) {
            return true;
        }
        return false;
    }
}
