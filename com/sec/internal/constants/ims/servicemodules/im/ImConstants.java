package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.PhoneConstants;

public interface ImConstants {

    public interface AutoAcceptFt {
        public static final int ALLOWED_HOME = 1;
        public static final int ALLOWED_ROAMING = 2;
        public static final int NOT_ALLOWED = 0;
    }

    public interface ChatDirection {
        public static final int INCOMING = 0;
        public static final int IRRELEVANT = 2;
        public static final int OUTGOING = 1;
    }

    public interface ChatState {
        public static final int ACTIVE = 1;
        public static final int CLOSED_BY_USER = 2;
        public static final int CLOSED_INVOLUNTARILY = 3;
        public static final int CLOSED_VOLUNTARILY = 4;
        public static final int INACTIVE = 0;
        public static final int NONE = -1;
    }

    public enum ChatbotMessagingTech {
        UNKNOWN,
        NONE,
        NOT_AVAILABLE,
        SESSION,
        STANDALONE_MESSAGING
    }

    public enum ChatbotMsgTechConfig {
        DISABLED,
        SESSION_ONLY,
        BOTH_SESSION_AND_SLM,
        SLM_ONLY
    }

    public interface ErrorReason {
        public static final int NO_SESSION = 4;
    }

    public enum FtMech {
        MSRP,
        HTTP
    }

    public enum ImMsgTech {
        SIMPLE_IM {
            public String toString() {
                return "SIMPLE";
            }
        },
        CPM
    }

    public enum ImSessionStart {
        WHEN_OPENS_CHAT_WINDOW,
        WHEN_STARTS_TYPING,
        WHEN_PRESSES_SEND_BUTTON
    }

    public interface MessageNotificationStatus {
        public static final int DELIVERED = 1;
        public static final int DISPLAYED = 2;
        public static final int INTERWORKING_MMS = 4;
        public static final int INTERWORKING_SMS = 3;
        public static final int NONE = 0;
    }

    public interface MessageStatus {
        public static final int BLOCKED = 6;
        public static final int FAILED = 4;
        public static final int IRRELEVANT = 8;
        public static final int QUEUED = 7;
        public static final int READ = 1;
        public static final int SENDING = 2;
        public static final int SENT = 3;
        public static final int TO_SEND = 5;
        public static final int UNREAD = 0;
    }

    public interface MessageType {
        public static final int LOCATION = 2;
        public static final int MULTIMEDIA = 0;
        @Deprecated
        public static final int MULTIMEDIA_BURN = 9;
        public static final int MULTIMEDIA_PUBLICACCOUNT = 11;
        public static final int SYSTEM = 3;
        public static final int SYSTEM_LEADER_CHANGED = 8;
        public static final int SYSTEM_LEADER_INFORMED = 13;
        public static final int SYSTEM_USER_JOINED = 6;
        public static final int SYSTEM_USER_KICKOUT = 14;
        public static final int SYSTEM_USER_LEFT = 4;
        public static final int TEXT = 1;
        @Deprecated
        public static final int TEXT_BURN = 10;
        public static final int TEXT_PUBLICACCOUNT = 12;
    }

    public enum MessagingUX {
        SEAMLESS,
        INTEGRATED
    }

    public interface ParticipantStatus {
        public static final int ACCEPTED = 2;
        public static final int DECLINED = 3;
        public static final int FAILED = 7;
        public static final int GONE = 5;
        public static final int INITIAL = 0;
        public static final int INVITED = 1;
        public static final int PENDING = 8;
        public static final int TIMEOUT = 4;
        public static final int TO_INVITE = 6;
    }

    public interface RequiredAction {
        public static final int DISPLAY_ERROR = 0;
        public static final int DISPLAY_ERROR_CFS = 3;
        public static final int FALLBACK_TO_LEGACY = 1;
        public static final int FALLBACK_TO_LEGACY_CFS = 2;
    }

    public interface ServiceTag {
        public static final String SERVICE_FT = "FT";
        public static final String SERVICE_IM = "IM";
    }

    public enum SlmAuth {
        DISABLED,
        ENABLED,
        RECEIVING_ONLY
    }

    public interface TransferMech {
        public static final int HTTP = 1;
        public static final int MSRP = 0;
    }

    public interface TransferState {
        public static final int ATTACHED = 6;
        public static final int BLOCKED = 8;
        public static final int CANCELED = 4;
        public static final int CANCELED_NEED_TO_NOTIFY = 10;
        public static final int CANCELLING = 7;
        public static final int COMPLETED = 3;
        public static final int CREATED = 0;
        public static final int IN_PROGRESS = 2;
        public static final int PENDING = 1;
        public static final int QUEUED = 5;
        public static final int SENDING = 9;
    }

    public enum Status implements IEnumerationWithId<Status> {
        UNREAD(0),
        READ(1),
        SENDING(2),
        SENT(3),
        FAILED(4),
        TO_SEND(5),
        BLOCKED(6),
        QUEUED(7),
        IRRELEVANT(8);
        
        private static final ReverseEnumMap<Status> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(Status.class);
        }

        private Status(int id2) {
            this.id = id2;
        }

        public static Status fromId(int id2) {
            return map.get(Integer.valueOf(id2));
        }

        public int getId() {
            return this.id;
        }

        public Status getFromId(int id2) {
            return map.get(Integer.valueOf(id2));
        }
    }

    public enum RevocationStatus implements IEnumerationWithId<RevocationStatus> {
        NONE(0),
        AVAILABLE(1),
        PENDING(2),
        SENDING(3),
        SENT(4),
        SUCCESS(5),
        FAILED(6);
        
        private static final ReverseEnumMap<RevocationStatus> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(RevocationStatus.class);
        }

        private RevocationStatus(int id2) {
            this.id = id2;
        }

        public static RevocationStatus fromId(int id2) {
            RevocationStatus status = NONE;
            try {
                return map.get(Integer.valueOf(id2));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return status;
            }
        }

        public int getId() {
            return this.id;
        }

        public RevocationStatus getFromId(int id2) {
            return fromId(id2);
        }
    }

    public enum MessagingTech implements IEnumerationWithId<MessagingTech> {
        NORMAL(0),
        SLM_PAGER_MODE(1),
        SLM_LARGE_MODE(2);
        
        private static final ReverseEnumMap<MessagingTech> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(MessagingTech.class);
        }

        private MessagingTech(int id2) {
            this.id = id2;
        }

        public static MessagingTech fromId(int id2) {
            MessagingTech type = NORMAL;
            try {
                return map.get(Integer.valueOf(id2));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return type;
            }
        }

        public int getId() {
            return this.id;
        }

        public MessagingTech getFromId(int id2) {
            return fromId(id2);
        }
    }

    public enum Type implements IEnumerationWithId<Type> {
        MULTIMEDIA(0),
        TEXT(1),
        LOCATION(2),
        SYSTEM(3),
        SYSTEM_USER_LEFT(4),
        SYSTEM_USER_KICKOUT(14),
        SYSTEM_USER_JOINED(6),
        SYSTEM_LEADER_CHANGED(8),
        MULTIMEDIA_PUBLICACCOUNT(11),
        TEXT_PUBLICACCOUNT(12),
        SYSTEM_LEADER_INFORMED(13);
        
        private static final ReverseEnumMap<Type> map = null;
        private final int id;

        static {
            map = new ReverseEnumMap<>(Type.class);
        }

        private Type(int id2) {
            this.id = id2;
        }

        public static Type fromId(int id2) {
            return map.get(Integer.valueOf(id2));
        }

        public int getId() {
            return this.id;
        }

        public Type getFromId(int id2) {
            return map.get(Integer.valueOf(id2));
        }
    }

    public enum ChatbotTrafficType {
        NONE(MessageContextValues.none),
        ADVERTISEMENT("advertisement"),
        PAYMENT("payment"),
        PREMIUM("premium"),
        SUBSCRIPTION(PhoneConstants.SUBSCRIPTION_KEY),
        UNKNOWN(NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
        
        private String trafficType;

        private ChatbotTrafficType(String arg) {
            this.trafficType = arg;
        }

        public static ChatbotTrafficType fromString(String name) {
            if (name == null) {
                return NONE;
            }
            for (ChatbotTrafficType type : values()) {
                if (type.trafficType.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
