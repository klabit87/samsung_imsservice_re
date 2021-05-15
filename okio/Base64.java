package okio;

import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReqMsg;
import java.io.UnsupportedEncodingException;

final class Base64 {
    private static final byte[] MAP = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, ReqMsg.request_xdm_update_aka_resp, ReqMsg.request_xdm_update_gba_key, ReqMsg.request_xdm_fetch_document, ReqMsg.request_xdm_upload_pres_rules, ReqMsg.request_xdm_upload_contacts, ReqMsg.request_xdm_modify_contact, ReqMsg.request_update_geolocation, ReqMsg.request_start_video_earlymedia, ReqMsg.request_handle_cmc_csfb, 97, 98, 99, 100, 101, ReqMsg.request_send_sip, ReqMsg.request_open_sip_dialog, ReqMsg.request_start_local_ring_back_tone, ReqMsg.request_stop_local_ring_back_tone, ReqMsg.request_update_sim_info, ReqMsg.request_alarm_wake_up, ReqMsg.request_x509_cert_verify_result, ReqMsg.request_silent_log_enabled, ReqMsg.request_ntp_time_offset, 111, 112, ReqMsg.request_clear_all_call_internal, ReqMsg.request_start_cmc_record, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    private static final byte[] URL_MAP = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, ReqMsg.request_xdm_update_aka_resp, ReqMsg.request_xdm_update_gba_key, ReqMsg.request_xdm_fetch_document, ReqMsg.request_xdm_upload_pres_rules, ReqMsg.request_xdm_upload_contacts, ReqMsg.request_xdm_modify_contact, ReqMsg.request_update_geolocation, ReqMsg.request_start_video_earlymedia, ReqMsg.request_handle_cmc_csfb, 97, 98, 99, 100, 101, ReqMsg.request_send_sip, ReqMsg.request_open_sip_dialog, ReqMsg.request_start_local_ring_back_tone, ReqMsg.request_stop_local_ring_back_tone, ReqMsg.request_update_sim_info, ReqMsg.request_alarm_wake_up, ReqMsg.request_x509_cert_verify_result, ReqMsg.request_silent_log_enabled, ReqMsg.request_ntp_time_offset, 111, 112, ReqMsg.request_clear_all_call_internal, ReqMsg.request_start_cmc_record, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};

    private Base64() {
    }

    public static byte[] decode(String in) {
        int bits;
        int limit = in.length();
        while (limit > 0 && ((c = in.charAt(limit - 1)) == '=' || c == 10 || c == 13 || c == ' ' || c == 9)) {
            limit--;
        }
        byte[] out = new byte[((int) ((((long) limit) * 6) / 8))];
        int outCount = 0;
        int inCount = 0;
        int word = 0;
        for (int pos = 0; pos < limit; pos++) {
            char c = in.charAt(pos);
            if (c >= 'A' && c <= 'Z') {
                bits = c - 'A';
            } else if (c >= 'a' && c <= 'z') {
                bits = c - 'G';
            } else if (c >= '0' && c <= '9') {
                bits = c + 4;
            } else if (c == '+' || c == '-') {
                bits = 62;
            } else if (c == '/' || c == '_') {
                bits = 63;
            } else {
                if (!(c == 10 || c == 13 || c == ' ' || c == 9)) {
                    return null;
                }
            }
            word = (word << 6) | ((byte) bits);
            inCount++;
            if (inCount % 4 == 0) {
                int outCount2 = outCount + 1;
                out[outCount] = (byte) (word >> 16);
                int outCount3 = outCount2 + 1;
                out[outCount2] = (byte) (word >> 8);
                out[outCount3] = (byte) word;
                outCount = outCount3 + 1;
            }
        }
        int lastWordChars = inCount % 4;
        if (lastWordChars == 1) {
            return null;
        }
        if (lastWordChars == 2) {
            out[outCount] = (byte) ((word << 12) >> 16);
            outCount++;
        } else if (lastWordChars == 3) {
            int word2 = word << 6;
            int outCount4 = outCount + 1;
            out[outCount] = (byte) (word2 >> 16);
            outCount = outCount4 + 1;
            out[outCount4] = (byte) (word2 >> 8);
        }
        if (outCount == out.length) {
            return out;
        }
        byte[] prefix = new byte[outCount];
        System.arraycopy(out, 0, prefix, 0, outCount);
        return prefix;
    }

    public static String encode(byte[] in) {
        return encode(in, MAP);
    }

    public static String encodeUrl(byte[] in) {
        return encode(in, URL_MAP);
    }

    private static String encode(byte[] in, byte[] map) {
        byte[] out = new byte[(((in.length + 2) * 4) / 3)];
        int index = 0;
        int end = in.length - (in.length % 3);
        for (int i = 0; i < end; i += 3) {
            int index2 = index + 1;
            out[index] = map[(in[i] & 255) >> 2];
            int index3 = index2 + 1;
            out[index2] = map[((in[i] & 3) << 4) | ((in[i + 1] & 255) >> 4)];
            int index4 = index3 + 1;
            out[index3] = map[((in[i + 1] & 15) << 2) | ((in[i + 2] & 255) >> 6)];
            index = index4 + 1;
            out[index4] = map[in[i + 2] & 63];
        }
        int length = in.length % 3;
        if (length == 1) {
            int index5 = index + 1;
            out[index] = map[(in[end] & 255) >> 2];
            int index6 = index5 + 1;
            out[index5] = map[(in[end] & 3) << 4];
            int index7 = index6 + 1;
            out[index6] = 61;
            out[index7] = 61;
            index = index7 + 1;
        } else if (length == 2) {
            int index8 = index + 1;
            out[index] = map[(in[end] & 255) >> 2];
            int index9 = index8 + 1;
            out[index8] = map[((in[end] & 3) << 4) | ((in[end + 1] & 255) >> 4)];
            int index10 = index9 + 1;
            out[index9] = map[(in[end + 1] & 15) << 2];
            index = index10 + 1;
            out[index10] = 61;
        }
        try {
            return new String(out, 0, index, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
