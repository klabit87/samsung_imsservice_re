package com.sec.internal.ims.servicemodules.tapi.service.utils;

import android.util.SparseArray;
import com.gsma.services.rcs.capability.Capabilities;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.ims.presence.PresenceInfo;

public class ContactInfo {
    public static final int NOT_RCS = 1;
    public static final int NO_INFO = 8;
    public static final int RCS_ACTIVE = 2;
    public static final int REGISTRATION_STATUS_OFFLINE = 2;
    public static final int REGISTRATION_STATUS_ONLINE = 1;
    public static final int REGISTRATION_STATUS_UNKNOWN = 0;
    private Capabilities capabilities = null;
    private ContactId contact = null;
    private String displayName = null;
    private BlockingState mBlockingState = BlockingState.NOT_BLOCKED;
    private long mBlockingTs;
    private PresenceInfo presenceInfo = null;
    private int rcsStatus = 1;
    private long rcsStatusTimestamp = 0;
    private int registrationState = 0;

    public enum BlockingState {
        NOT_BLOCKED(0),
        BLOCKED(1);
        
        private static SparseArray<BlockingState> mValueToEnum;
        private int mValue;

        static {
            int i;
            mValueToEnum = new SparseArray<>();
            for (BlockingState entry : values()) {
                mValueToEnum.put(entry.toInt(), entry);
            }
        }

        private BlockingState(int value) {
            this.mValue = value;
        }

        public final int toInt() {
            return this.mValue;
        }
    }

    public void setCapabilities(Capabilities capabilities2) {
        this.capabilities = capabilities2;
    }

    public Capabilities getCapabilities() {
        return this.capabilities;
    }

    public void setPresenceInfo(PresenceInfo info) {
        this.presenceInfo = info;
    }

    public void setContact(ContactId contact2) {
        this.contact = contact2;
    }

    public ContactId getContact() {
        return this.contact;
    }

    public void setRcsStatus(int rcsStatus2) {
        this.rcsStatus = rcsStatus2;
    }

    public int getRcsStatus() {
        return this.rcsStatus;
    }

    public void setRegistrationState(int registrationState2) {
        this.registrationState = registrationState2;
    }

    public int getRegistrationState() {
        return this.registrationState;
    }

    public void setRcsStatusTimestamp(long timestamp) {
        this.rcsStatusTimestamp = timestamp;
    }

    public void setRcsDisplayName(String displayname) {
        this.displayName = displayname;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setBlockingState(BlockingState state) {
        this.mBlockingState = state;
    }

    public BlockingState getBlockingState() {
        return this.mBlockingState;
    }

    public long getBlockingTimestamp() {
        return this.mBlockingTs;
    }

    public void setBlockingTimestamp(long ts) {
        this.mBlockingTs = ts;
    }

    public String toString() {
        String result = "Contact=" + this.contact + ", Status=" + this.rcsStatus + ", State=" + this.registrationState + ", Timestamp=" + this.rcsStatusTimestamp;
        if (this.capabilities != null) {
            result = result + ", Capabilities=" + this.capabilities.toString();
        }
        if (this.presenceInfo == null) {
            return result;
        }
        return result + ", Presence=" + this.presenceInfo.toString();
    }
}
