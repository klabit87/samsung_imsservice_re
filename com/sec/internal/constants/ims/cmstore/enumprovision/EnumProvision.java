package com.sec.internal.constants.ims.cmstore.enumprovision;

public class EnumProvision {

    public enum NewUserOptInCase {
        DEFAULT(-1),
        ERR(1),
        DELETE(2);
        
        private final int mId;

        private NewUserOptInCase(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum ProvisionEventType {
        DEFAULT(0),
        CHK_INITIAL_STATE(1),
        CHK_PHONE_ACCOUNT(2),
        REQ_AUTH_ZCODE(3),
        REQ_ATS_TOKEN(4),
        REQ_SESSION_GEN(5),
        REQ_SERVICE_ACCOUNT(6),
        REQ_ACCOUNT_ELIGIBILITY(7),
        REQ_GET_TC(8),
        REQ_CREATE_ACCOUNT(9),
        REQ_DELETE_ACCOUNT(10),
        REQ_HUI_TOKEN(11),
        REQ_PAT(12),
        REQ_RETIRE_SESSION(13),
        READY_PAT(14),
        ACCOUNT_NOT_ELIGIBLE(15),
        REQ_INPUT_CTN(16),
        CPS_PROVISION_SHUTDOWN(17),
        PROVISION_ERR(18),
        AUTH_ERR(19),
        STOP_BACKUP_ERR(20),
        SYNC_ERR(21),
        ACCESS_ERR(22),
        DELETE_ACCOUNT_SUCCESS(23),
        LAST_RETRY_CREATE_ACCOUNT(24),
        EVENT_AUTH_ZCODE_TIMEOUT(25),
        CHECK_PHONE_STATE(26),
        RESTART_SERVICE(27),
        ZCODE_ERROR_201(28),
        MAILBOX_MIGRATION_RESET(29),
        EVENT_TYPE_END(Integer.MAX_VALUE);
        
        private final int mId;

        private ProvisionEventType(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }

        public static ProvisionEventType valueOf(int id) {
            for (ProvisionEventType r : values()) {
                if (r.mId == id) {
                    return r;
                }
            }
            return null;
        }
    }
}
