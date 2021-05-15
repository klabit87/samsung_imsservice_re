package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class ImsMsg {
    public static final byte NONE = 0;
    public static final byte com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify = 3;
    public static final byte com_sec_internal_ims_core_handler_secims_imsCommonStruc_Request = 1;
    public static final byte com_sec_internal_ims_core_handler_secims_imsCommonStruc_Response = 2;
    public static final String[] names = {"NONE", "com_sec_internal_ims_core_handler_secims_imsCommonStruc_Request", "com_sec_internal_ims_core_handler_secims_imsCommonStruc_Response", "com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify"};

    private ImsMsg() {
    }

    public static String name(int e) {
        return names[e];
    }
}
