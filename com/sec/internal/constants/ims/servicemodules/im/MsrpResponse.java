package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;

public enum MsrpResponse implements IEnumerationWithId<MsrpResponse> {
    MSRP_200_SUCCESSFUL_TRANSACTION(200),
    MSRP_400_REQUEST_UNINTELLIGIBLE(400),
    MSRP_403_ACTION_NOT_ALLOWED(403),
    MSRP_408_TRANSACTION_TIMED_OUT(408),
    MSRP_413_DO_NOT_SEND_THIS_MESSAGE(413),
    MSRP_415_UNKNOWN_CONTENT_TYPE(AECNamespace.HttpResponseCode.UNSUPPORTED_MEDIA_TYPE),
    MSRP_423_PARAMETERS_OUT_OF_BOUND(423),
    MSRP_481_SESSION_DOES_NOT_EXIST(481),
    MSRP_501_UNKNOWN_METHOD(501),
    MSRP_503_OUT_OF_SERVICE(503),
    MSRP_506_SESSION_ON_OTHER_CONNECTION(Id.REQUEST_IM_SEND_NOTI_STATUS),
    MSRP_UNKNOWN_RESPONSE(999);
    
    private static final ReverseEnumMap<MsrpResponse> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(MsrpResponse.class);
    }

    private MsrpResponse(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public MsrpResponse getFromId(int id2) {
        return fromId(id2);
    }

    public static MsrpResponse fromId(int id2) {
        MsrpResponse response = MSRP_UNKNOWN_RESPONSE;
        try {
            return map.get(Integer.valueOf(id2));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return response;
        }
    }
}
