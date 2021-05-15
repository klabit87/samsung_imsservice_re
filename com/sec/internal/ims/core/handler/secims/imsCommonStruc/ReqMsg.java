package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class ReqMsg {
    public static final byte NONE = 0;
    public static final String[] names = {"NONE", "request_update_common_config", "request_ua_creation", "request_ua_deletion", "request_registration", "request_update_aka_resp", "request_set_preferred_impu", "request_network_suspended", "request_delete_tcp_client_socket", "request_set_text_mode", "request_update_srvcc_version", "request_update_xq_enable", "request_update_feature_tag", "request_make_call", "request_end_call", "request_update_call", "request_accept_call", "request_hold_call", "request_resume_call", "request_transfer_call", "request_accept_transfer_call", "request_reject_call", "request_modify_call_type", "request_reply_modify_call_type", "request_progress_incoming_call", "request_hold_video", "request_resume_video", "request_reject_modify_call_type", "request_pulling_call", "request_cancel_transfer_call", "request_make_conf_call", "request_update_conf_call", "request_publish_dialog", "request_send_msg", "request_send_rp_ack_resp", "request_receive_sms_resp", "request_euc_send_response", "request_modify_video_quality", "request_msg_set_msg_app_info_to_sip_ua", "request_start_im_session", "request_accept_im_session", "request_close_im_session", "request_start_media", "request_send_im_message", "request_send_im_composing_status", "request_send_im_notification_status", "request_start_ft_session", "request_cancel_ft_session", "request_accept_ft_session", "request_send_im_slm_message", "request_send_slm_file", "request_reject_im_session", "request_chatbot_anonymize", "request_report_chatbot_as_spam", "request_update_participants", "request_group_list_subscribe", "request_group_info_subscribe", "request_accept_slm_lmm_session", "request_reject_slm_lmm_session", "request_im_set_more_info_to_sip_ua", "request_capability_exchange", "request_set_own_capabilities", "request_presence_publish", "request_presence_unpublish", "request_presence_subscribe", "request_presence_unsubscribe", "request_start_camera", "request_stop_camera", "request_update_pani", "request_dns_query", "request_update_audio_interface", "request_send_info", "request_send_cmc_info", "request_update_cmc_ext_call_count", "request_send_media_event", "request_send_relay_event", "request_options_send_response", "request_options_cap_exchange", "request_options_send_cmc_check_msg", "request_update_rat", "request_update_time_in_plani", "request_xdm_update_gba_data", "request_xdm_update_aka_resp", "request_xdm_update_gba_key", "request_xdm_fetch_document", "request_xdm_upload_pres_rules", "request_xdm_upload_contacts", "request_xdm_modify_contact", "request_update_geolocation", "request_start_video_earlymedia", "request_handle_cmc_csfb", "request_ish_start_session", "request_ish_accept_session", "request_ish_stop_session", "request_handle_dtmf", "request_send_text", "request_vsh_start_session", "request_vsh_accept_session", "request_vsh_stop_session", "request_update_vce_config", "request_send_message_revoke_request", "request_rtp_stats_to_stack", "request_send_sip", "request_open_sip_dialog", "request_start_local_ring_back_tone", "request_stop_local_ring_back_tone", "request_update_sim_info", "request_alarm_wake_up", "request_x509_cert_verify_result", "request_silent_log_enabled", "request_ntp_time_offset", "request_start_record", "request_stop_record", "request_clear_all_call_internal", "request_start_cmc_record"};
    public static final byte request_accept_call = 16;
    public static final byte request_accept_ft_session = 48;
    public static final byte request_accept_im_session = 40;
    public static final byte request_accept_slm_lmm_session = 57;
    public static final byte request_accept_transfer_call = 20;
    public static final byte request_alarm_wake_up = 107;
    public static final byte request_cancel_ft_session = 47;
    public static final byte request_cancel_transfer_call = 29;
    public static final byte request_capability_exchange = 60;
    public static final byte request_chatbot_anonymize = 52;
    public static final byte request_clear_all_call_internal = 113;
    public static final byte request_close_im_session = 41;
    public static final byte request_delete_tcp_client_socket = 8;
    public static final byte request_dns_query = 69;
    public static final byte request_end_call = 14;
    public static final byte request_euc_send_response = 36;
    public static final byte request_group_info_subscribe = 56;
    public static final byte request_group_list_subscribe = 55;
    public static final byte request_handle_cmc_csfb = 90;
    public static final byte request_handle_dtmf = 94;
    public static final byte request_hold_call = 17;
    public static final byte request_hold_video = 25;
    public static final byte request_im_set_more_info_to_sip_ua = 59;
    public static final byte request_ish_accept_session = 92;
    public static final byte request_ish_start_session = 91;
    public static final byte request_ish_stop_session = 93;
    public static final byte request_make_call = 13;
    public static final byte request_make_conf_call = 30;
    public static final byte request_modify_call_type = 22;
    public static final byte request_modify_video_quality = 37;
    public static final byte request_msg_set_msg_app_info_to_sip_ua = 38;
    public static final byte request_network_suspended = 7;
    public static final byte request_ntp_time_offset = 110;
    public static final byte request_open_sip_dialog = 103;
    public static final byte request_options_cap_exchange = 77;
    public static final byte request_options_send_cmc_check_msg = 78;
    public static final byte request_options_send_response = 76;
    public static final byte request_presence_publish = 62;
    public static final byte request_presence_subscribe = 64;
    public static final byte request_presence_unpublish = 63;
    public static final byte request_presence_unsubscribe = 65;
    public static final byte request_progress_incoming_call = 24;
    public static final byte request_publish_dialog = 32;
    public static final byte request_pulling_call = 28;
    public static final byte request_receive_sms_resp = 35;
    public static final byte request_registration = 4;
    public static final byte request_reject_call = 21;
    public static final byte request_reject_im_session = 51;
    public static final byte request_reject_modify_call_type = 27;
    public static final byte request_reject_slm_lmm_session = 58;
    public static final byte request_reply_modify_call_type = 23;
    public static final byte request_report_chatbot_as_spam = 53;
    public static final byte request_resume_call = 18;
    public static final byte request_resume_video = 26;
    public static final byte request_rtp_stats_to_stack = 101;
    public static final byte request_send_cmc_info = 72;
    public static final byte request_send_im_composing_status = 44;
    public static final byte request_send_im_message = 43;
    public static final byte request_send_im_notification_status = 45;
    public static final byte request_send_im_slm_message = 49;
    public static final byte request_send_info = 71;
    public static final byte request_send_media_event = 74;
    public static final byte request_send_message_revoke_request = 100;
    public static final byte request_send_msg = 33;
    public static final byte request_send_relay_event = 75;
    public static final byte request_send_rp_ack_resp = 34;
    public static final byte request_send_sip = 102;
    public static final byte request_send_slm_file = 50;
    public static final byte request_send_text = 95;
    public static final byte request_set_own_capabilities = 61;
    public static final byte request_set_preferred_impu = 6;
    public static final byte request_set_text_mode = 9;
    public static final byte request_silent_log_enabled = 109;
    public static final byte request_start_camera = 66;
    public static final byte request_start_cmc_record = 114;
    public static final byte request_start_ft_session = 46;
    public static final byte request_start_im_session = 39;
    public static final byte request_start_local_ring_back_tone = 104;
    public static final byte request_start_media = 42;
    public static final byte request_start_record = 111;
    public static final byte request_start_video_earlymedia = 89;
    public static final byte request_stop_camera = 67;
    public static final byte request_stop_local_ring_back_tone = 105;
    public static final byte request_stop_record = 112;
    public static final byte request_transfer_call = 19;
    public static final byte request_ua_creation = 2;
    public static final byte request_ua_deletion = 3;
    public static final byte request_update_aka_resp = 5;
    public static final byte request_update_audio_interface = 70;
    public static final byte request_update_call = 15;
    public static final byte request_update_cmc_ext_call_count = 73;
    public static final byte request_update_common_config = 1;
    public static final byte request_update_conf_call = 31;
    public static final byte request_update_feature_tag = 12;
    public static final byte request_update_geolocation = 88;
    public static final byte request_update_pani = 68;
    public static final byte request_update_participants = 54;
    public static final byte request_update_rat = 79;
    public static final byte request_update_sim_info = 106;
    public static final byte request_update_srvcc_version = 10;
    public static final byte request_update_time_in_plani = 80;
    public static final byte request_update_vce_config = 99;
    public static final byte request_update_xq_enable = 11;
    public static final byte request_vsh_accept_session = 97;
    public static final byte request_vsh_start_session = 96;
    public static final byte request_vsh_stop_session = 98;
    public static final byte request_x509_cert_verify_result = 108;
    public static final byte request_xdm_fetch_document = 84;
    public static final byte request_xdm_modify_contact = 87;
    public static final byte request_xdm_update_aka_resp = 82;
    public static final byte request_xdm_update_gba_data = 81;
    public static final byte request_xdm_update_gba_key = 83;
    public static final byte request_xdm_upload_contacts = 86;
    public static final byte request_xdm_upload_pres_rules = 85;

    private ReqMsg() {
    }

    public static String name(int e) {
        return names[e];
    }
}