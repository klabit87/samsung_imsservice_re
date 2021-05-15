package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.Observable;

public class ImParticipant extends Observable {
    public static final String NO_ALIAS = "";
    private final String mChatId;
    private int mId;
    private Status mStatus = Status.INITIAL;
    private Type mType = Type.REGULAR;
    private final ImsUri mUri;
    private String mUserAlias = "";

    public ImParticipant(String chatId, ImsUri uri) {
        this.mChatId = chatId;
        this.mUri = uri;
    }

    public ImParticipant(String chatId, Status status, ImsUri uri) {
        this.mChatId = chatId;
        this.mStatus = status;
        this.mUri = uri;
    }

    public ImParticipant(String chatId, Status status, Type type, ImsUri uri, String userAlias) {
        this.mChatId = chatId;
        this.mStatus = status;
        this.mType = type;
        this.mUri = uri;
        this.mUserAlias = userAlias;
    }

    public ImParticipant(int id, String chatId, Status status, Type type, ImsUri uri, String userAlias) {
        this.mId = id;
        this.mChatId = chatId;
        this.mStatus = status;
        this.mType = type;
        this.mUri = uri;
        this.mUserAlias = userAlias;
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getChatId() {
        return this.mChatId;
    }

    public Type getType() {
        return this.mType;
    }

    public void setType(Type type) {
        this.mType = type;
    }

    public Status getStatus() {
        return this.mStatus;
    }

    public void setStatus(Status status) {
        this.mStatus = status;
    }

    public ImsUri getUri() {
        return this.mUri;
    }

    public String getUserAlias() {
        if (this.mUserAlias == null) {
            this.mUserAlias = "";
        }
        return this.mUserAlias;
    }

    public void setUserAlias(String alias) {
        this.mUserAlias = alias;
    }

    public enum Type implements IEnumerationWithId<Type> {
        REGULAR(0),
        INITIATOR(1),
        CHAIRMAN(2);
        
        private static final ReverseEnumMap<Type> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(Type.class);
        }

        private Type(int id2) {
            this.id = id2;
        }

        public int getId() {
            return this.id;
        }

        public Type getFromId(int id2) {
            return map.get(Integer.valueOf(id2));
        }

        public static Type fromId(int id2) {
            return map.get(Integer.valueOf(id2));
        }
    }

    public enum Status implements IEnumerationWithId<Status> {
        INITIAL(0),
        INVITED(1),
        ACCEPTED(2),
        DECLINED(3),
        TIMEOUT(4),
        GONE(5),
        TO_INVITE(6),
        FAILED(7),
        PENDING(8);
        
        private static final ReverseEnumMap<Status> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(Status.class);
        }

        private Status(int id2) {
            this.id = id2;
        }

        public int getId() {
            return this.id;
        }

        public Status getFromId(int id2) {
            return map.get(Integer.valueOf(id2));
        }

        public static Status fromId(int id2) {
            return map.get(Integer.valueOf(id2));
        }
    }

    public String toString() {
        return "ImParticipant [mId=" + this.mId + ", mChatId=" + this.mChatId + ", mType=" + this.mType + ", mStatus=" + this.mStatus + ", mUri=" + IMSLog.numberChecker(this.mUri) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + "]";
    }

    public int hashCode() {
        int i = 31 * 1;
        String str = this.mChatId;
        int i2 = 0;
        int result = 31 * (i + (str == null ? 0 : str.hashCode()));
        ImsUri imsUri = this.mUri;
        if (imsUri != null) {
            i2 = imsUri.hashCode();
        }
        return result + i2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImParticipant other = (ImParticipant) obj;
        String str = this.mChatId;
        if (str == null) {
            if (other.mChatId != null) {
                return false;
            }
        } else if (!str.equals(other.mChatId)) {
            return false;
        }
        ImsUri imsUri = this.mUri;
        if (imsUri != null) {
            return imsUri.equals(other.mUri);
        }
        if (other.mUri == null) {
            return true;
        }
        return false;
    }
}
