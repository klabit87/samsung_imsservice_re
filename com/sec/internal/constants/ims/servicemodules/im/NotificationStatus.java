package com.sec.internal.constants.ims.servicemodules.im;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import java.util.HashSet;
import java.util.Set;

public enum NotificationStatus implements IEnumerationWithId<NotificationStatus> {
    NONE(0),
    DELIVERED(1),
    DISPLAYED(2),
    INTERWORKING_SMS(3),
    INTERWORKING_MMS(4);
    
    private static final String LOG_TAG = null;
    private static final ReverseEnumMap<NotificationStatus> map = null;
    private final int id;

    static {
        Class<NotificationStatus> cls;
        LOG_TAG = cls.getSimpleName();
        map = new ReverseEnumMap<>(cls);
    }

    private NotificationStatus(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public NotificationStatus getFromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }

    public static NotificationStatus fromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }

    public static int encode(Set<NotificationStatus> disposition) {
        int dispositionNotificationMask = 0;
        for (NotificationStatus notification : disposition) {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[notification.ordinal()];
            if (i == 1 || i == 2) {
                dispositionNotificationMask |= notification.getId();
            } else {
                Log.e(LOG_TAG, "encode(): unsupported disposition notification!");
            }
        }
        return dispositionNotificationMask;
    }

    /* renamed from: com.sec.internal.constants.ims.servicemodules.im.NotificationStatus$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus = null;

        static {
            int[] iArr = new int[NotificationStatus.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus = iArr;
            try {
                iArr[NotificationStatus.DELIVERED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[NotificationStatus.DISPLAYED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public static Set<NotificationStatus> decode(int dispositionNotificationMask) {
        Set<NotificationStatus> result = new HashSet<>();
        if ((DELIVERED.getId() & dispositionNotificationMask) != 0) {
            result.add(DELIVERED);
        }
        if ((DISPLAYED.getId() & dispositionNotificationMask) != 0) {
            result.add(DISPLAYED);
        }
        return result;
    }

    public static Set<NotificationStatus> toSet(String disposition) {
        Set<NotificationStatus> dispositionNotification = new HashSet<>();
        Log.e(LOG_TAG, "toSet(): disposition :" + disposition);
        if (disposition == null) {
            dispositionNotification.add(DELIVERED);
            dispositionNotification.add(DISPLAYED);
            return dispositionNotification;
        }
        char c = 65535;
        int hashCode = disposition.hashCode();
        if (hashCode != 3387192) {
            if (hashCode != 823466996) {
                if (hashCode == 1671764162 && disposition.equals(ATTConstants.ATTDispositionType.DISPLAY)) {
                    c = 0;
                }
            } else if (disposition.equals(ATTConstants.ATTDispositionType.DELIVERY)) {
                c = 1;
            }
        } else if (disposition.equals(MessageContextValues.none)) {
            c = 2;
        }
        if (c == 0) {
            dispositionNotification.add(DISPLAYED);
        } else if (c == 1) {
            dispositionNotification.add(DELIVERED);
        } else if (c != 2) {
            dispositionNotification.add(DELIVERED);
            dispositionNotification.add(DISPLAYED);
        } else {
            dispositionNotification.add(NONE);
        }
        return dispositionNotification;
    }
}
