package com.sec.internal.constants.ims.cmstore;

import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;

public class ATTConstants {

    public static class ATTDispositionStatus {
        public static final String DELIVERED = "delivered";
        public static final String DISPLAYED = "displayed";
    }

    public static class ATTDispositionType {
        public static final String DELIVERY = "delivery";
        public static final String DISPLAY = "display";
    }

    public static class ATTErrorNames {
        public static final String CPS_PROVISION_SHUTDOWN = "CPS.SVC-1015";
        public static final String CPS_TC_ERROR_1007 = "CPS.SVC-1007";
        public static final String CPS_TC_ERROR_1008 = "CPS.SVC-1008";
        public static final String ERROR_CODE_201 = "201";
        public static final String ERROR_CODE_202 = "202";
        public static final String ERR_ACCOUNT_NOT_ELIGIBLE = "not eligible";
        public static final String ERR_CPS_DEFAULT = "CpsDefaultError";
        public static final String ERR_ENCORE_METASWITCH_ACCOUNT_NOT_PROVISIONED = "ENCOREESB.SECURITY-6014";
        public static final String ERR_HUI_JSON = "ERR_HUITOKEN_JSON";
        public static final String ERR_RETRY_AFTER = "RetryAfterRule";
        public static final String ERR_SESSION_ID = "wrong sessionID";
        public static final String LAST_RETRY_CREATE_ACCOUNT = "Last_Retry_Create_Account";
        public static final String encoreesb = "ENCOREESB.";
        public static final String security = "SECURITY";
    }

    public static class ATTMessageContextValues extends MessageContextValues {
        public static final String chatMessage = "chat-message";
        public static final String fileMessage = "file-message";
        public static final String imdnMessage = "imdn-message";
        public static final String standaloneMessage = "standalone-message";
    }

    public enum AttAmbsUIScreenNames {
        NewUserOptIn_PrmptMsg1(101),
        ExistingUserOptInWithTerms_PrmptMsg3(103),
        ExistingUserOptInWoTerms_PrmpMsg4(104),
        SteadyState_PrmptMsg5(105),
        Synchronizing_PrmptMsg6(106),
        DontTurnOn_PrmptMsg7(107),
        Provisioning_PrmptMsg8(108),
        Settings_PrmptMsg9(109),
        Settings_PrmptMsg10(100),
        Settings_PrmptMsg11(111),
        NotDefault_PrmptMsg12(112),
        StopBackup_PrmptMsg13(113),
        StopConfirmation_PrmptMsg14(114),
        EligibilityError_ErrMsg1(201),
        AuthenticationError_ErrMsg2(202),
        ProvisioningError_ErrMsg4(204),
        SteadyStateError_ErrMsg5(205),
        MsisdnEntry_ErrMsg6(206),
        SteadyStateError_ErrMsg7(207),
        ProvisioningBlockedError_ErrMsg8(208),
        StopBackupError_ErrMsg10(210);
        
        private final int mId;

        private AttAmbsUIScreenNames(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }

        public static AttAmbsUIScreenNames valueOf(int id) {
            for (AttAmbsUIScreenNames r : values()) {
                if (r.mId == id) {
                    return r;
                }
            }
            return null;
        }
    }
}
