package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

public final class EucMsg {
    public static final byte NONE = 0;
    public static final byte com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__AckMessage = 3;
    public static final byte com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__NotificationMessage = 4;
    public static final byte com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__PersistentMessage = 1;
    public static final byte com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__SystemMessage = 5;
    public static final byte com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__VolatileMessage = 2;
    public static final String[] names = {"NONE", "com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__PersistentMessage", "com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__VolatileMessage", "com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__AckMessage", "com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__NotificationMessage", "com_sec_internal_ims_core_handler_secims_imsCommonStruc_Notify__EucMessage__SystemMessage"};

    private EucMsg() {
    }

    public static String name(int e) {
        return names[e];
    }
}
