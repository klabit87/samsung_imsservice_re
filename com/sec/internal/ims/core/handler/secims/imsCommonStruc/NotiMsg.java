package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class NotiMsg {
    public static final byte NONE = 0;
    public static final byte alarm_wake_up = 72;
    public static final byte call_send_cmc_info = 81;
    public static final byte call_status = 6;
    public static final byte cancel_alarm = 73;
    public static final byte cdpn_info = 14;
    public static final byte conf_call_changed = 8;
    public static final byte contact_activated = 5;
    public static final byte contact_download_notify = 60;
    public static final byte contact_uri_info = 80;
    public static final byte dedicated_bearer_event = 18;
    public static final byte dialog_event = 74;
    public static final byte dialog_subscribe_status = 77;
    public static final byte dns_response = 76;
    public static final byte dtmf_param_data = 20;
    public static final byte dump_message = 78;
    public static final byte echolocate_msg = 56;
    public static final byte euc_message = 26;
    public static final byte ft_incoming_session = 41;
    public static final byte ft_progress = 40;
    public static final byte group_chat_info_updated = 44;
    public static final byte group_chat_list_updated = 43;
    public static final byte group_chat_subscribe_status = 42;
    public static final byte im_composing_status_received = 32;
    public static final byte im_conf_info_updated = 70;
    public static final byte im_conference_info_updated = 34;
    public static final byte im_message_received = 31;
    public static final byte im_message_report_received = 35;
    public static final byte im_notification_status_received = 33;
    public static final byte im_session_invited = 30;
    public static final byte imdn_response_received = 36;
    public static final byte incoming_call = 7;
    public static final byte ish_incoming_session = 62;
    public static final byte ish_session_established = 63;
    public static final byte ish_session_terminated = 64;
    public static final byte ish_transfer_progress = 65;
    public static final byte message_noti = 25;
    public static final byte message_revoke_response_received = 45;
    public static final byte modify_call_data = 13;
    public static final byte modify_video_data = 15;
    public static final String[] names = {"NONE", "registration_status", "registration_auth", "subscribe_status", "registration_impu", "contact_activated", "call_status", "incoming_call", "conf_call_changed", "reg_info_changed", "refer_received", "refer_status", "update_route_table", "modify_call_data", "cdpn_info", "modify_video_data", "notify_video_event_data", "notify_cmc_record_event_data", "dedicated_bearer_event", "rrc_connection_event", "dtmf_param_data", "rtp_loss_rate_noti", "text_param_data", "sms_rp_ack", "sms_noti", "message_noti", "euc_message", "session_started", "session_closed", "session_established", "im_session_invited", "im_message_received", "im_composing_status_received", "im_notification_status_received", "im_conference_info_updated", "im_message_report_received", "imdn_response_received", "slm_sip_response_received", "slm_progress", "slm_lmm_invited", "ft_progress", "ft_incoming_session", "group_chat_subscribe_status", "group_chat_list_updated", "group_chat_info_updated", "message_revoke_response_received", "send_message_revoke_response", "request_chatbot_anonymize_response", "request_chatbot_anonymize_response_received", "report_chatbot_as_spam_response", "new_remote_capabilities", "new_presence_info", "presence_publish_status", "presence_subscribe_status", "xcap_message", "sip_message", "echolocate_msg", "xdm_req_gba_data", "xdm_auth", "xdm_store_gba_data", "contact_download_notify", "options_received_info", "ish_incoming_session", "ish_session_established", "ish_session_terminated", "ish_transfer_progress", "vsh_incoming_session", "vsh_session_established", "vsh_session_terminated", "slm_message_incoming", "im_conf_info_updated", "ss_get_gba_key", "alarm_wake_up", "cancel_alarm", "dialog_event", "x509_cert_verify_request", "dns_response", "dialog_subscribe_status", "dump_message", "xq_message", "contact_uri_info", "call_send_cmc_info"};
    public static final byte new_presence_info = 51;
    public static final byte new_remote_capabilities = 50;
    public static final byte notify_cmc_record_event_data = 17;
    public static final byte notify_video_event_data = 16;
    public static final byte options_received_info = 61;
    public static final byte presence_publish_status = 52;
    public static final byte presence_subscribe_status = 53;
    public static final byte refer_received = 10;
    public static final byte refer_status = 11;
    public static final byte reg_info_changed = 9;
    public static final byte registration_auth = 2;
    public static final byte registration_impu = 4;
    public static final byte registration_status = 1;
    public static final byte report_chatbot_as_spam_response = 49;
    public static final byte request_chatbot_anonymize_response = 47;
    public static final byte request_chatbot_anonymize_response_received = 48;
    public static final byte rrc_connection_event = 19;
    public static final byte rtp_loss_rate_noti = 21;
    public static final byte send_message_revoke_response = 46;
    public static final byte session_closed = 28;
    public static final byte session_established = 29;
    public static final byte session_started = 27;
    public static final byte sip_message = 55;
    public static final byte slm_lmm_invited = 39;
    public static final byte slm_message_incoming = 69;
    public static final byte slm_progress = 38;
    public static final byte slm_sip_response_received = 37;
    public static final byte sms_noti = 24;
    public static final byte sms_rp_ack = 23;
    public static final byte ss_get_gba_key = 71;
    public static final byte subscribe_status = 3;
    public static final byte text_param_data = 22;
    public static final byte update_route_table = 12;
    public static final byte vsh_incoming_session = 66;
    public static final byte vsh_session_established = 67;
    public static final byte vsh_session_terminated = 68;
    public static final byte x509_cert_verify_request = 75;
    public static final byte xcap_message = 54;
    public static final byte xdm_auth = 58;
    public static final byte xdm_req_gba_data = 57;
    public static final byte xdm_store_gba_data = 59;
    public static final byte xq_message = 79;

    private NotiMsg() {
    }

    public static String name(int e) {
        return names[e];
    }
}
