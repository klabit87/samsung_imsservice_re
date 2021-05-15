package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class RespMsg {
    public static final byte NONE = 0;
    public static final byte call_response = 2;
    public static final byte close_session_resp = 6;
    public static final byte csh_general_response = 14;
    public static final byte general_response = 1;
    public static final String[] names = {"NONE", "general_response", "call_response", "subscribe_response", "send_sms_resp", "start_session_resp", "close_session_resp", "start_media_resp", "send_im_message_resp", "send_im_noti_resp", "update_participants_resp", "send_slm_resp", "send_message_revoke_internal_resp", "xdm_general_response", "csh_general_response", "send_euc_response_response", "sipdialog_general_response"};
    public static final byte send_euc_response_response = 15;
    public static final byte send_im_message_resp = 8;
    public static final byte send_im_noti_resp = 9;
    public static final byte send_message_revoke_internal_resp = 12;
    public static final byte send_slm_resp = 11;
    public static final byte send_sms_resp = 4;
    public static final byte sipdialog_general_response = 16;
    public static final byte start_media_resp = 7;
    public static final byte start_session_resp = 5;
    public static final byte subscribe_response = 3;
    public static final byte update_participants_resp = 10;
    public static final byte xdm_general_response = 13;

    private RespMsg() {
    }

    public static String name(int e) {
        return names[e];
    }
}
