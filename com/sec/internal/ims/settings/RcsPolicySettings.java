package com.sec.internal.ims.settings;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.imsservice.R;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.servicemodules.im.strategy.MnoStrategyCreator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RcsPolicySettings {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = RcsPolicySettings.class.getSimpleName();
    private static final String RCS_AS_POLICY = "rcs_as_policy";
    private Context mContext = null;
    private SimpleEventLog mEventLog;
    private int mPhoneId = 0;
    private JsonElement mRcsPolicy = JsonNull.INSTANCE;

    public static class RcsPolicy {
        public static final String ALLOW_LIST_CAPEX = "allow_list_capex";
        public static final String ALLOW_ONLY_OPENGROUPCHAT = "allow_only_opengroupchat";
        public static final String ALWAYS_RCS_ON = "always_rcs_on";
        public static final String AUTH_BASED_SESSION_CONTROL = "auth_based_session_control";
        public static final String AUTO_ACCEPT_FT_RESUME = "auto_accept_ft_resume";
        public static final String AUTO_ACCEPT_GLS = "auto_accept_gls";
        public static final String AUTO_RESEND_FAILED_FT = "auto_resend_failed_ft";
        public static final String BLOCK_FT_AUTO_DOWNLOAD_FOR_GC = "block_ft_auto_download_for_gc";
        public static final String BLOCK_MSG = "block_msg";
        public static final String CANCEL_FOR_DEREGI_PROMPTLY = "cancel_for_deregi_promptly";
        public static final String CANCEL_FT_WIFI_DISCONNECTED = "cancel_ft_wifi_disconnected";
        public static final String CAPA_SKIP_NOTIFY_FORCE_REFRESH_SYNC = "capa_skip_notify_force_refresh_sync";
        public static final String CENTRAL_MSG_STORE = "central_msg_store";
        public static final String CHECK_BYECAUSE = "check_byecause";
        public static final String CHECK_GONEMEMBERS_OF_FULLNOTIFY = "check_gonemembers_of_fullnotify";
        public static final String CHECK_IMSIBASED_REGI = "check_imsibased_regi";
        public static final String CHECK_INITIATOR_SESSIONURI = "check_initiator_sessionuri";
        public static final String CHECK_MSGAPP_IMSESSION_REJECT = "check_msgapp_imsession_reject";
        public static final String CHECK_PARTICIPANT_OF_PARTIAL_STATE = "check_participant_of_partial_state";
        public static final String CHECK_PRESENCE_RULES = "check_presence_rules";
        public static final String CHECK_P_ASSERTED_IDENTITY = "check_p_asserted_identity";
        public static final String COMPOSING_NOTIFICATION_IDLE_INTERVAL = "compsing_noti_idle_interval";
        public static final String CONFINFO_UPDATE_NOT_ALLOWED = "confinfo_update_not_allowed";
        public static final String CONTENTLENGTH_IN_BYTE = "contentlength_in_byte";
        public static final String DELAY_TO_CANCEL_FOR_DEREGI = "delay_to_cancel_for_deregi";
        public static final String DELAY_TO_DEREGI_FOR_A2P_SESSION = "delay_to_deregi_for_a2p_session";
        public static final String DISPLAY_FT_IN_GALLERY = "display_ft_in_gallery";
        public static final String DISPLAY_INVITED_SYSTEMMESSAGE = "display_invited_systemmessage";
        public static final String DUAL_SIMHANDLING = "dual_simhandling";
        public static final String EXTRA_FT_FOR_NS = "extra_ft_for_ns";
        public static final String FILE_NAME_LENGTH_LIMIT_IN_SERVER = "file_name_length_limit_In_server";
        public static final String FIRSTMSG_GROUPCHAT_INVITE = "firstmsg_groupchat_invite";
        public static final String FORCE_AUTO_ACCEPT = "force_auto_accept";
        public static final String FTHTTP_FORCE_AUTO_ACCEPT_ON_WIFI = "fthttp_force_auto_accept_on_wifi";
        public static final String FTHTTP_IGNORE_WHEN_UNTRUSTED_URL = "fthttp_ignore_when_untrusted_url";
        public static final String FTHTTP_NON_STANDARD_URLS = "fthttp_non_standard_urls";
        public static final String FTHTTP_UPLOAD_RESUME_FROM_THE_START = "fthttp_upload_resume_from_the_start";
        public static final String FT_FALLBACK_DIRECTLY_OFFLINE = "ft_fallback_directly_offline";
        public static final String FT_INTERNET_PDN = "ft_internet_pdn";
        public static final String FT_NET_CAPABILITY = "ft_net_capability";
        public static final String FT_WITH_GBA = "ft_with_gba";
        public static final String GONE_SHOULD_ENDSESSION = "gone_should_endsession";
        public static final String GROUPCHAT_AUTO_REJOIN = "groupchat_auto_rejoin";
        public static final String GROUPCHAT_INVITATIONUI_USED = "groupchat_invitationui_used";
        public static final String HANDLE_LEAVE_OGC_FAILURE = "handle_leave_ogc_failure";
        public static final String IGNORE_WIFI_WARNSIZE = "ignore_wifi_warnsize";
        public static final String IS_EAP_SUPPORTED = "is_eap_supported";
        public static final String LIST_SUB_URI_TRANSLATION = "list_sub_uri_translation";
        public static final String MAX_SIPINVITE_ATONCE = "max_sipinvite_atonce";
        public static final String NOTIFY_RCS_MSG = "notify_rcs_msg";
        public static final String NUM_OF_DISPLAY_NOTIFICATION_ATONCE = "num_of_display_notification_atonce";
        public static final String ONEKEY_REPORT_PSI = "onekey_report_psi";
        public static final String PARTICIPANTBASED_CLOSED_GROUPCHAT = "participantbased_closed_groupchat";
        public static final String PENDING_FOR_REGI = "pending_for_regi";
        public static final String POLL_ALLOWED = "poll_allowed";
        public static final String PS_ONLY_NETWORK = "ps_only_network";
        public static final String REMOVE_FAILED_PARTICIPANT_GROUPCHAT = "remove_failed_participant_groupchat";
        public static final String REMOVE_FT_THUMBNAIL = "remove_ft_thumbnail";
        public static final String REPLACE_SPECIALCHARACTER = "replace_specialcharacter";
        public static final String RESUME_WITH_COMPLETE_FILE = "resume_with_complete_file";
        public static final String SENDMSG_RESP_TIMEOUT = "sendmsg_resp_timeout";
        public static final String SESSION_ESTABLISH_TIMER = "session_establish_timer";
        public static final String SKIP_BLOCK_CHATBOT_MSG = "skip_block_chatbot_msg";
        public static final String START_SESSION_WHEN_CREATE_GROUPCHAT = "start_session_when_create_groupchat";
        public static final String SUPPORT_7DIGIT_MSG = "support_7digit_msg";
        public static final String SUPPORT_AUTO_REJOIN_FOR_BYE = "support_auto_rejoin_for_bye";
        public static final String SUPPORT_CHAT_CLOSE_BY_SERVER = "support_chat_close_by_server";
        public static final String SUPPORT_ENCODING_FILENAME_BY_SERVER = "support_encoding_filename_by_server";
        public static final String SUPPORT_FTHTTP_CONTENTLENGTH = "support_fthttp_contentlength";
        public static final String SUPPORT_HIGHRESOLUTIONVIDEO_THUMBNAIL = "support_highresolutionvideo_thumbnail";
        public static final String SUPPORT_LARGE_MSG_RESIZING = "support_large_msg_resizing";
        public static final String SUPPORT_OFFLINE_GC_INVITATION = "support_offline_gc_invitation";
        public static final String SUPPORT_QUICKFT = "support_quickft";
        public static final String SUPPORT_REVOKE_MSG_FOR_486_RESP = "support_revoke_msg_for_486_resp";
        public static final String SUPPORT_SENDMSG_RESP_TIMEOUT = "support_sendmsg_resp_timeout";
        public static final String TRIGGER_CAPEX_WHEN_STARTTYPING = "trigger_capex_when_starttyping";
        public static final String TRIGGER_INVITE_AFTER_18X = "trigger_invite_after_18x";
        public static final String UPDATE_ACSREADY_NETWORK = "update_acsready_network";
        public static final String UPDATE_SESSION_AFTER_REGISTRATION = "update_session_after_registration";
        public static final String USERAGENT_HAS_MSGAPPVERSION = "useragent_has_msgappversion";
        public static final String USE_AGGREGATION_DISPLAYED_IMDN = "use_aggregation_displayed_imdn";
        public static final String USE_CAPCACHE_EXPIRY = "use_capcache_expiry";
        public static final String USE_CHATBOT_MANUALACCEPT = "use_chatbot_manualaccept";
        public static final String USE_INDIVIDUAL_REFER = "use_individual_refer";
        public static final String USE_MSRP = "use_msrp";
        public static final String USE_PROVISIONAL_RESPONSE_ASSENT = "use_provisional_response_assent";
        public static final String USE_RAND_DELAY_PERIODIC_POLL = "use_rand_delay_periodic_poll";
        public static final String USE_SIPURI_FOR_URIGENERATOR = "use_sipuri_for_urigenerator";
        public static final String USE_TEMPFILE_WHEN_DOWNLOAD = "use_tempfile_when_download";
        public static final String USE_USERIDENTITY_FOR_FTHTTP = "use_useridentity_for_fthttp";
        public static final String WAIT_DEACTVAING_DELETE_CHAT = "wait_deactvaing_delete_chat";
    }

    public enum RcsPolicyType {
        DEFAULT_RCS("DEFAULT_RCS"),
        CMCC("CMCC"),
        RJIL("RJIL"),
        SINGTEL("SINGTEL"),
        VODAFONE_IN_UP("VODAFONE_IN_UP"),
        TMOBILE("TMOBILE"),
        VODA("VODA"),
        ORANGE("ORANGE"),
        TELENOR("TELENOR"),
        TELIA("TELIA"),
        TELSTRA("TELSTRA"),
        ATT("ATT"),
        TMOUS("TMOUS"),
        VZW("VZW"),
        SPR("SPR"),
        USCC("USCC"),
        BMC("BMC"),
        BMC_UP("BMC_UP"),
        TCE("TCE"),
        DEFAULT_UP("DEFAULT_UP"),
        JIBE_UP("JIBE_UP"),
        SEC_UP("SEC_UP"),
        KT_UP("KT_UP"),
        RJIL_UP("RJIL_UP"),
        ORANGE_UP("ORANGE_UP"),
        VODA_UP("VODA_UP"),
        SWISSCOM_UP("SWISSCOM_UP"),
        TMOBILE_UP("TMOBILE_UP"),
        SPR_UP("SPR_UP"),
        VZW_UP("VZW_UP");
        
        private String mTypeName;

        private RcsPolicyType(String typeName) {
            this.mTypeName = typeName;
        }

        public String getTypeName() {
            return this.mTypeName;
        }

        public boolean isUp() {
            return this != BMC_UP && getTypeName().toUpperCase().contains("_UP");
        }

        public static RcsPolicyType fromString(String name) {
            if (name == null) {
                Log.d(RcsPolicySettings.LOG_TAG, "Warning: RcsPolicyType invalid parameter, name is null");
                return DEFAULT_RCS;
            }
            for (RcsPolicyType type : values()) {
                if (type.mTypeName.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            if (name.toUpperCase().contains("_UP")) {
                Log.d(RcsPolicySettings.LOG_TAG, "Warning: RcsPolicyType " + name + " not defined use DEFAULT_UP");
                return DEFAULT_UP;
            }
            Log.d(RcsPolicySettings.LOG_TAG, "Warning: RcsPolicyType " + name + " not defined use DEFAULT_RCS");
            return DEFAULT_RCS;
        }

        public boolean isOneOf(RcsPolicyType... types) {
            for (RcsPolicyType type : types) {
                if (this == type) {
                    return true;
                }
            }
            return false;
        }
    }

    public RcsPolicySettings(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 300);
        load(false);
    }

    /* Debug info: failed to restart local var, previous not found, register: 22 */
    public boolean load(boolean forceReload) {
        Throwable th;
        RcsPolicyType newPolicyType = MnoStrategyCreator.getPolicyType(SimUtil.getSimMno(this.mPhoneId), this.mPhoneId, this.mContext);
        String newPolicyTypeName = newPolicyType.getTypeName();
        if (!TextUtils.equals(this.mRcsPolicy.isJsonNull() ? "" : this.mRcsPolicy.getAsJsonObject().get(ImsAutoUpdate.TAG_POLICY_NAME).getAsString(), newPolicyTypeName) || forceReload) {
            String currentPolicyName = newPolicyTypeName;
            SimpleEventLog simpleEventLog = this.mEventLog;
            int i = this.mPhoneId;
            simpleEventLog.logAndAdd(i, "load from rcspolicy.json " + currentPolicyName);
            try {
                InputStream inputStream = this.mContext.getResources().openRawResource(R.raw.rcspolicy);
                try {
                    JsonParser parser = new JsonParser();
                    JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
                    JsonElement element = parser.parse(reader);
                    reader.close();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e = e;
                            RcsPolicyType rcsPolicyType = newPolicyType;
                            String str = newPolicyTypeName;
                        }
                    }
                    JsonObject object = element.getAsJsonObject();
                    JsonElement defaultRcsElement = object.get(ImsAutoUpdate.TAG_DEFAULT_RCS_POLICY);
                    JsonElement defaultUpElement = object.get(ImsAutoUpdate.TAG_DEFAULT_UP_POLICY);
                    if (defaultRcsElement.isJsonNull()) {
                        RcsPolicyType rcsPolicyType2 = newPolicyType;
                        String str2 = newPolicyTypeName;
                        JsonElement jsonElement = defaultRcsElement;
                    } else if (defaultUpElement.isJsonNull()) {
                        JsonObject jsonObject = object;
                        RcsPolicyType rcsPolicyType3 = newPolicyType;
                        String str3 = newPolicyTypeName;
                        JsonElement jsonElement2 = defaultRcsElement;
                    } else {
                        JsonElement parentPolicy = defaultRcsElement;
                        String defaultRcsPolicyTag = ImsAutoUpdate.TAG_DEFAULT_RCS_POLICY;
                        if (RcsPolicyType.fromString(currentPolicyName).isUp()) {
                            parentPolicy = defaultUpElement;
                            defaultRcsPolicyTag = ImsAutoUpdate.TAG_DEFAULT_UP_POLICY;
                        }
                        JsonArray imstrategyArray = object.getAsJsonArray(ImsAutoUpdate.TAG_RCS_POLICY);
                        JsonElement currentPolicy = JsonNull.INSTANCE;
                        Iterator it = imstrategyArray.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            JsonElement elem = (JsonElement) it.next();
                            if (TextUtils.equals(currentPolicyName, elem.getAsJsonObject().get(ImsAutoUpdate.TAG_POLICY_NAME).getAsString())) {
                                currentPolicy = elem;
                                break;
                            }
                        }
                        JsonElement rcsAsPolicy = JsonNull.INSTANCE;
                        String rcsAsPolicyName = "";
                        if (currentPolicy == JsonNull.INSTANCE) {
                            SimpleEventLog simpleEventLog2 = this.mEventLog;
                            int i2 = this.mPhoneId;
                            JsonObject jsonObject2 = object;
                            StringBuilder sb = new StringBuilder();
                            String str4 = newPolicyTypeName;
                            sb.append("No policy for ");
                            sb.append(currentPolicyName);
                            sb.append("in rcspolicy.json");
                            simpleEventLog2.logAndAdd(i2, sb.toString());
                            JsonElement jsonElement3 = defaultRcsElement;
                        } else {
                            String str5 = newPolicyTypeName;
                            if (currentPolicy.getAsJsonObject().has(RCS_AS_POLICY)) {
                                rcsAsPolicyName = currentPolicy.getAsJsonObject().get(RCS_AS_POLICY).getAsString();
                                SimpleEventLog simpleEventLog3 = this.mEventLog;
                                int i3 = this.mPhoneId;
                                StringBuilder sb2 = new StringBuilder();
                                JsonElement jsonElement4 = defaultRcsElement;
                                sb2.append("use RCS AS policy ");
                                sb2.append(rcsAsPolicyName);
                                simpleEventLog3.logAndAdd(i3, sb2.toString());
                                Iterator it2 = imstrategyArray.iterator();
                                while (true) {
                                    if (!it2.hasNext()) {
                                        break;
                                    }
                                    JsonElement elem2 = (JsonElement) it2.next();
                                    if (TextUtils.equals(rcsAsPolicyName, elem2.getAsJsonObject().get(ImsAutoUpdate.TAG_POLICY_NAME).getAsString())) {
                                        rcsAsPolicy = elem2;
                                        break;
                                    }
                                }
                            }
                        }
                        if (rcsAsPolicy == JsonNull.INSTANCE) {
                            this.mEventLog.logAndAdd(this.mPhoneId, "No rcsAsPolicy in rcspolicy.json");
                        }
                        ImsAutoUpdate autoUpdate = ImsAutoUpdate.getInstance(this.mContext, this.mPhoneId);
                        JsonElement updatedParentPolicy = autoUpdate.getRcsDefaultPolicyUpdate(parentPolicy, newPolicyType.isUp());
                        JsonElement updatedAsPolicy = autoUpdate.getRcsPolicyUpdate(rcsAsPolicy, rcsAsPolicyName);
                        JsonElement updatedPolicy = autoUpdate.getRcsPolicyUpdate(currentPolicy, currentPolicyName);
                        if (JsonUtil.isValidJsonElement(updatedPolicy)) {
                            SimpleEventLog simpleEventLog4 = this.mEventLog;
                            ImsAutoUpdate imsAutoUpdate = autoUpdate;
                            int i4 = this.mPhoneId;
                            RcsPolicyType rcsPolicyType4 = newPolicyType;
                            StringBuilder sb3 = new StringBuilder();
                            JsonElement jsonElement5 = parentPolicy;
                            sb3.append("policy updated: ");
                            sb3.append(currentPolicyName);
                            simpleEventLog4.logAndAdd(i4, sb3.toString());
                            if (JsonUtil.isValidJsonElement(updatedAsPolicy)) {
                                updatedPolicy = JsonUtil.merge(updatedAsPolicy, updatedPolicy);
                            }
                            this.mRcsPolicy = JsonUtil.merge(updatedParentPolicy, updatedPolicy);
                            return true;
                        }
                        RcsPolicyType rcsPolicyType5 = newPolicyType;
                        JsonElement jsonElement6 = parentPolicy;
                        SimpleEventLog simpleEventLog5 = this.mEventLog;
                        int i5 = this.mPhoneId;
                        simpleEventLog5.logAndAdd(i5, "policy not valid " + currentPolicyName + " use updated default policy " + defaultRcsPolicyTag);
                        this.mRcsPolicy = updatedParentPolicy;
                        return true;
                    }
                    this.mEventLog.logAndAdd(this.mPhoneId, "load: No default_rcs_policy or default_up_policy. load failed");
                    return false;
                } catch (IOException e2) {
                    e = e2;
                    e.printStackTrace();
                    return false;
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            } catch (IOException e3) {
                e = e3;
                RcsPolicyType rcsPolicyType6 = newPolicyType;
                String str6 = newPolicyTypeName;
                e.printStackTrace();
                return false;
            }
        } else {
            this.mEventLog.logAndAdd(this.mPhoneId, "policy not changed skip reloading");
            return false;
        }
        throw th;
    }

    public boolean readBool(String name) {
        JsonObject policy = this.mRcsPolicy.getAsJsonObject();
        if (policy.has(name)) {
            boolean ret = policy.get(name).getAsBoolean();
            String str = LOG_TAG;
            Log.d(str, "readBool:" + name + " : " + ret);
            return ret;
        }
        String str2 = LOG_TAG;
        Log.d(str2, "readBool: " + name + "not exist");
        return false;
    }

    public int readInt(String name) {
        JsonObject policy = this.mRcsPolicy.getAsJsonObject();
        if (policy.has(name)) {
            int ret = policy.get(name).getAsInt();
            String str = LOG_TAG;
            Log.d(str, "readInt:" + name + " : " + ret);
            return ret;
        }
        String str2 = LOG_TAG;
        Log.d(str2, "readInt: " + name + "not exist");
        return 0;
    }

    public String readString(String name) {
        JsonObject policy = this.mRcsPolicy.getAsJsonObject();
        if (policy.has(name)) {
            String ret = policy.get(name).getAsString();
            String str = LOG_TAG;
            Log.d(str, "readString:" + name + " : " + ret);
            return ret;
        }
        String str2 = LOG_TAG;
        Log.d(str2, "readString: " + name + "not exist");
        return "";
    }

    public List<String> readStringArray(String name) {
        List<String> ret = new ArrayList<>();
        JsonObject policy = this.mRcsPolicy.getAsJsonObject();
        if (policy.has(name)) {
            String str = LOG_TAG;
            Log.d(str, "readStringArray: " + name + " exists");
            Iterator it = policy.get(name).getAsJsonArray().iterator();
            while (it.hasNext()) {
                ret.add(((JsonElement) it.next()).getAsString());
            }
        }
        String str2 = LOG_TAG;
        Log.d(str2, "readStringArray: " + name + " : " + ret);
        return ret;
    }
}
