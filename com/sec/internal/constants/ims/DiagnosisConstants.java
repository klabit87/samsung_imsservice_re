package com.sec.internal.constants.ims;

import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.core.PaniConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.xbill.DNS.KEYRecord;

public class DiagnosisConstants {
    public static final String CALL_METHOD_LOGANDADD = "logAndAdd";
    public static final String COMMON_KEY_MNO_NAME = "MNON";
    public static final String COMMON_KEY_OMC_NW_CODE = "OMNW";
    public static final String COMMON_KEY_PLMN = "PLMN";
    public static final String COMMON_KEY_SIM_SLOT = "SLOT";
    public static final String COMMON_KEY_SPEC_REVISION = "SREV";
    public static final String COMMON_KEY_VIDEO_SETTINGS = "VILS";
    public static final String COMMON_KEY_VOLTE_SETTINGS = "VLTS";
    public static final String COMPONENT_ID = "Telephony";
    public static final int CS_CALL_EMERGENCY = 3;
    public static final int CS_CALL_VOICE = 1;
    public static final int CS_STATE_INCOMING = 2;
    public static final int CS_STATE_OUTGOING = 1;
    public static final int DIMS_FEATURE_ACTIVE = 2;
    public static final int DIMS_FEATURE_AVAILABLE = 1;
    public static final int DIMS_FEATURE_DISABLED = 0;
    public static final String DMUI_KEY_CALLER_INFO = "USRC";
    public static final String DMUI_KEY_SETTING_TYPE = "DMST";
    public static final int DOWNGRADE_BY_CAMERAFAIL = 3;
    public static final int DOWNGRADE_BY_RTPTIMEOUT = 2;
    public static final int DOWNGRADE_BY_USER = 1;
    public static final String DRCS_KEY_MAAP_FT_MO_SUCCESS = "MFOS";
    public static final String DRCS_KEY_MAAP_GLS_MO_SUCCESS = "MGOS";
    public static final String DRCS_KEY_MAAP_IM_MO_SUCCESS = "MIOS";
    public static final String DRCS_KEY_MAAP_MO_FAIL = "MPOF";
    public static final String DRCS_KEY_MAAP_MO_FAIL_NETWORK = "MOFN";
    public static final String DRCS_KEY_MAAP_MO_FAIL_TERMINAL = "MOFT";
    public static final String DRCS_KEY_MAAP_MO_SUCCESS = "MPOS";
    public static final String DRCS_KEY_MAAP_MT = "MPMT";
    public static final String DRCS_KEY_MAAP_SLM_MO_SUCCESS = "MSOS";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_ADVERTISEMENT = "MPAD";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_NONE = "MPNO";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_PAYMENT = "MPPA";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_PREMIUM = "MPPR";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_SUBSCRIPTION = "MPSU";
    public static final String DRCS_KEY_RACC = "RACC";
    public static final String DRCS_KEY_RACF = "RACF";
    public static final String DRCS_KEY_RACV = "RACV";
    public static final String DRCS_KEY_RCPC = "RCPC";
    public static final String DRCS_KEY_RCPF = "RCPF";
    public static final String DRCS_KEY_RCSC = "RCSC";
    public static final String DRCS_KEY_RCSF = "RCSF";
    public static final String DRCS_KEY_RCS_EC_MO_SUCCESS = "REOS";
    public static final String DRCS_KEY_RCS_FT_MO_SUCCESS = "RFOS";
    public static final String DRCS_KEY_RCS_GLS_MO_SUCCESS = "RGOS";
    public static final String DRCS_KEY_RCS_IM_MO_SUCCESS = "RIOS";
    public static final String DRCS_KEY_RCS_MO_FAIL = "RCOF";
    public static final String DRCS_KEY_RCS_MO_FAIL_NETWORK = "ROFN";
    public static final String DRCS_KEY_RCS_MO_FAIL_TERMINAL = "ROFT";
    public static final String DRCS_KEY_RCS_MO_SUCCESS = "RCOS";
    public static final String DRCS_KEY_RCS_MT = "RCMT";
    public static final String DRCS_KEY_RCS_REGI_STATUS = "RCRS";
    public static final String DRCS_KEY_RCS_SLM_MO_SUCCESS = "RSOS";
    public static final String DRCS_KEY_SMS_FALLBACK = "SMFB";
    public static final String DRPT_KEY_CMC_END_FAIL_COUNT = "CMCF";
    public static final String DRPT_KEY_CMC_END_TOTAL_COUNT = "CMCE";
    public static final String DRPT_KEY_CMC_INCOMING_FAIL = "CMMT";
    public static final String DRPT_KEY_CMC_OUTGOING_FAIL = "CMMO";
    public static final String DRPT_KEY_CMC_START_TOTAL_COUNT = "CMCS";
    public static final String DRPT_KEY_CSCALL_END_FAIL_COUNT = "CEFC";
    public static final String DRPT_KEY_CSCALL_END_TOTAL_COUNT = "CETC";
    public static final String DRPT_KEY_CSCALL_INCOMING_FAIL = "CSMT";
    public static final String DRPT_KEY_CSCALL_OUTGOING_FAIL = "CSMO";
    public static final String DRPT_KEY_CSFB_COUNT = "CFCT";
    public static final String DRPT_KEY_DOWNGRADE_TO_VOICE_COUNT = "DWCT";
    public static final String DRPT_KEY_DUAL_IMS_ACTIVE = "DIMS";
    public static final String DRPT_KEY_EXPERIENCE_AUDIO_CONFERENCE_COUNT = "EXAC";
    public static final String DRPT_KEY_EXPERIENCE_EMERGENCY_COUNT = "EXEM";
    public static final String DRPT_KEY_EXPERIENCE_RTT_COUNT = "EXRT";
    public static final String DRPT_KEY_EXPERIENCE_TOTAL_COUNT = "EXTC";
    public static final String DRPT_KEY_EXPERIENCE_TTY_COUNT = "EXTY";
    public static final String DRPT_KEY_EXPERIENCE_VIDEO_CONFERENCE_COUNT = "EXVC";
    public static final String DRPT_KEY_EXPERIENCE_VIDEO_COUNT = "EXVI";
    public static final String DRPT_KEY_EXPERIENCE_VOICE_COUNT = "EXVO";
    public static final String DRPT_KEY_MULTIDEVICE_MEP_COUNT = "MDMP";
    public static final String DRPT_KEY_MULTIDEVICE_SOFTPHONE_COUNT = "MDSF";
    public static final String DRPT_KEY_MULTIDEVICE_TOTAL_COUNT = "MDTC";
    public static final String DRPT_KEY_OMADM_UPDATE_COUNT = "DMUC";
    public static final String DRPT_KEY_SIM_MOBILITY_ENABLED = "SMMO";
    public static final String DRPT_KEY_SMK_VERSION = "SMKV";
    public static final String DRPT_KEY_SRVCC_COUNT = "SRCT";
    public static final String DRPT_KEY_UPGRADE_TO_VIDEO_COUNT = "UPCT";
    public static final String DRPT_KEY_VOLTE_END_EMERGENCY_COUNT = "VEEM";
    public static final String DRPT_KEY_VOLTE_END_FAIL_COUNT = "VEFC";
    public static final String DRPT_KEY_VOLTE_END_TOTAL_COUNT = "VETC";
    public static final String DRPT_KEY_VOLTE_END_VIDEO_COUNT = "VEVI";
    public static final String DRPT_KEY_VOLTE_END_VOICE_COUNT = "VEVO";
    public static final String DRPT_KEY_VOLTE_INCOMING_FAIL = "PSMT";
    public static final String DRPT_KEY_VOLTE_OUTGOING_FAIL = "PSMO";
    public static final String DRPT_KEY_VOLTE_START_EMERGENCY_COUNT = "VSEM";
    public static final String DRPT_KEY_VOLTE_START_TOTAL_COUNT = "VSTC";
    public static final String DRPT_KEY_VOLTE_START_VIDEO_COUNT = "VSVI";
    public static final String DRPT_KEY_VOLTE_START_VOICE_COUNT = "VSVO";
    public static final String DRPT_KEY_VOWIFI_ENABLE_SETTINGS = "VWES";
    public static final String DRPT_KEY_VOWIFI_END_EMERGENCY_COUNT = "WEEM";
    public static final String DRPT_KEY_VOWIFI_END_FAIL_COUNT = "WEFC";
    public static final String DRPT_KEY_VOWIFI_END_TOTAL_COUNT = "WETC";
    public static final String DRPT_KEY_VOWIFI_END_VIDEO_COUNT = "WEVI";
    public static final String DRPT_KEY_VOWIFI_END_VOICE_COUNT = "WEVO";
    public static final String DRPT_KEY_VOWIFI_INCOMING_FAIL = "VWMT";
    public static final String DRPT_KEY_VOWIFI_OUTGOING_FAIL = "VWMO";
    public static final String DRPT_KEY_VOWIFI_PREF_SETTINGS = "VWPS";
    public static final String DRPT_KEY_VOWIFI_START_EMERGENCY_COUNT = "WSEM";
    public static final String DRPT_KEY_VOWIFI_START_TOTAL_COUNT = "WSTC";
    public static final String DRPT_KEY_VOWIFI_START_VIDEO_COUNT = "WSVI";
    public static final String DRPT_KEY_VOWIFI_START_VOICE_COUNT = "WSVO";
    public static final int EXTERNAL_FEATURE_CEND = 0;
    public static final int EXTERNAL_FEATURE_DROP = 1;
    public static final String FEATURE_DMUI = "DMUI";
    public static final String FEATURE_DRCS = "DRCS";
    public static final String FEATURE_DRPT = "DRPT";
    public static final String FEATURE_PSCI = "PSCI";
    public static final String FEATURE_RCSA = "RCSA";
    public static final String FEATURE_RCSC = "RCSC";
    public static final String FEATURE_RCSL = "RCSL";
    public static final String FEATURE_RCSM = "RCSM";
    public static final String FEATURE_RCSP = "RCSP";
    public static final String FEATURE_REGI = "REGI";
    public static final String FEATURE_SIMI = "SIMI";
    public static final String FEATURE_UNKNOWN = "UNKNOWN";
    public static final String KEY_FEATURE = "feature";
    public static final String KEY_NEXT_DRPT_SCHEDULE = "next_drpt_schedule";
    public static final String KEY_OVERWRITE_MODE = "overwrite_mode";
    public static final String KEY_SEND_MODE = "send_mode";
    public static final int MAX_INT = 999999;
    public static final int OVERWRITE_MODE_ADD = 1;
    public static final int OVERWRITE_MODE_REPLACE = 0;
    public static final int OVERWRITE_MODE_REPLACE_IF_BIGGER = 2;
    public static final String PSCI_KEY_CALL_BEARER = "PSCS";
    public static final String PSCI_KEY_CALL_DOWNGRADE = "DWGD";
    public static final String PSCI_KEY_CALL_STATE = "STAT";
    public static final String PSCI_KEY_CALL_TIME = "CTME";
    public static final String PSCI_KEY_CALL_TYPE = "TYPE";
    public static final String PSCI_KEY_DATA_ROAMING = "ROAM";
    public static final String PSCI_KEY_EPDG_STATUS = "EPDG";
    public static final String PSCI_KEY_FAIL_CODE = "FLCD";
    public static final String PSCI_KEY_MO_MT = "MOMT";
    public static final String PSCI_KEY_PARTICIPANT_NUMBER = "PARN";
    public static final String PSCI_KEY_SIP_FLOW = "SPFW";
    public static final String RCSA_KEY_ARST = "ARST";
    public static final String RCSA_KEY_ATRE = "ATRE";
    public static final String RCSA_KEY_AVER = "AVER";
    public static final String RCSA_KEY_ERRC = "ERRC";
    public static final String RCSA_KEY_PROF = "PROF";
    public static final String RCSC_KEY_NCAP = "NCAP";
    public static final String RCSC_KEY_NRCS = "NRCS";
    public static final String RCSL_KEY_LTCH = "LTCH";
    public static final String RCSM_KEY_FTRC = "FTRC";
    public static final String RCSM_KEY_FTYP = "FTYP";
    public static final String RCSM_KEY_HTTP = "HTTP";
    public static final String RCSM_KEY_ITER = "ITER";
    public static final String RCSM_KEY_MCID = "MCID";
    public static final String RCSM_KEY_MDIR = "MDIR";
    public static final String RCSM_KEY_MGRP = "MGRP";
    public static final String RCSM_KEY_MIID = "MIID";
    public static final String RCSM_KEY_MRAT = "MRAT";
    public static final String RCSM_KEY_MRTY = "MRTY";
    public static final String RCSM_KEY_MRVA = "MRVA";
    public static final String RCSM_KEY_MSIZ = "MSIZ";
    public static final String RCSM_KEY_MSRP = "MSRP";
    public static final String RCSM_KEY_MTYP = "MTYP";
    public static final String RCSM_KEY_ORST = "ORST";
    public static final String RCSM_KEY_PTCN = "PTCN";
    public static final String RCSM_KEY_SIPR = "SIPR";
    public static final String RCSM_KEY_SRSC = "SRSC";
    public static final String RCSM_MDIR_MO = "0";
    public static final String RCSM_MDIR_MT = "1";
    public static final String RCSM_MGRP_1_TO_1 = "0";
    public static final String RCSM_MGRP_GROUP = "1";
    public static final String RCSM_MRAT_WIFI_POSTFIX = "_WIFI";
    public static final String RCSM_MTYP_CHATBOT_POSTFIX = "_CHATBOT";
    public static final String RCSM_MTYP_EC = "EC";
    public static final String RCSM_MTYP_FT = "FT";
    public static final String RCSM_MTYP_GLS = "GLS";
    public static final String RCSM_MTYP_IM = "IM";
    public static final String RCSM_MTYP_SLM = "SLM";
    public static final String RCSM_ORST_HTTP = "4";
    public static final String RCSM_ORST_ITER = "5";
    public static final String RCSM_ORST_MSRP = "2";
    public static final String RCSM_ORST_PASS = "0";
    public static final String RCSM_ORST_REGI = "3";
    public static final String RCSM_ORST_SIP = "1";
    public static final String RCSP_KEY_ERES = "ERES";
    public static final String RCSP_KEY_ERRC = "ERRC";
    public static final String RCSP_KEY_SERR = "SERR";
    public static final String REGI_KEY_DATA_RAT_TYPE = "DRAT";
    public static final String REGI_KEY_DATA_ROAMING = "ROAM";
    public static final String REGI_KEY_FAIL_COUNT = "FALC";
    public static final String REGI_KEY_FAIL_REASON = "FRSN";
    public static final String REGI_KEY_PANI_PREFIX = "PNPR";
    public static final String REGI_KEY_PCSCF_ORDINAL = "PCOD";
    public static final String REGI_KEY_PDN_TYPE = "PDTY";
    public static final String REGI_KEY_REQUEST_CODE = "REQC";
    public static final String REGI_KEY_SERVICE_SET_ALL = "SVCA";
    public static final String REGI_KEY_SERVICE_SET_REGISTERED = "SVCR";
    public static final String REGI_KEY_SIGNAL_STRENGTH = "SIGS";
    public static final int SEND_MODE_INSTANT = 0;
    public static final int SEND_MODE_PENDING = 1;
    public static final String SIMI_KEY_EVENT_TYPE = "EVTT";
    public static final String SIMI_KEY_GID1 = "GID1";
    public static final String SIMI_KEY_ISIM_EXISTS = "ISIM";
    public static final String SIMI_KEY_ISIM_VALIDITY = "ISVL";
    public static final String SIMI_KEY_SIM_VALIDITY = "SMVL";
    public static final String SIMI_KEY_SUBSCRIPTION_ID = "SCID";
    public static final int SMMO_FEATURE_DISABLED = 0;
    public static final int SMMO_FEATURE_ENABLED = 1;
    public static final int SMMO_MOBILITY_AVAILABLE = 4;
    public static final int SMMO_MOBILITY_STATUS = 2;
    public static final int SPEC_VERSION = 16;
    public static final List<String> sRegiCountKey = new ArrayList<String>() {
        {
            add("RGS4");
            add("RGSL");
            add("RGSW");
            add("RGF4");
            add("RGFL");
            add("RGFW");
            add("RRS4");
            add("RRSL");
            add("RRSW");
            add("RRF4");
            add("RRFL");
            add("RRFW");
        }
    };

    public enum RCSA_ATRE {
        INIT,
        FROM_REGI,
        FROM_APP,
        SIM_SWAP,
        CHANGE_MSG_APP,
        REJECT_LTE,
        VERSION_ZERO,
        CHANGE_SWVERSION,
        CHANGE_AIRPLANE,
        PUSH_SMS,
        EXPIRE_VALIDITY
    }

    /* renamed from: com.sec.internal.constants.ims.DiagnosisConstants$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$core$SimConstants$SIM_STATE;

        static {
            int[] iArr = new int[SimConstants.SIM_STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$core$SimConstants$SIM_STATE = iArr;
            try {
                iArr[SimConstants.SIM_STATE.UNKNOWN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$core$SimConstants$SIM_STATE[SimConstants.SIM_STATE.ABSENT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public static int getEventType(SimConstants.SIM_STATE lastState, boolean isRefresh, boolean isSameMno) {
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$core$SimConstants$SIM_STATE[lastState.ordinal()];
        if (i != 1) {
            if (i != 2) {
                return 0;
            }
            if (isSameMno) {
                return 2;
            }
            return 3;
        } else if (isRefresh) {
            return 4;
        } else {
            return 1;
        }
    }

    public enum REGI_REQC {
        UNKNOWN(0),
        INITIAL(1),
        REFRESH(2),
        HAND_OVER(3),
        RE_REGI(4),
        DE_REGI(9);
        
        private int mCode;

        private REGI_REQC(int code) {
            this.mCode = code;
        }

        public int getCode() {
            return this.mCode;
        }
    }

    public enum REGI_FRSN {
        UNKNOWN(0),
        OK(200),
        OK_AFTER_FAIL(201),
        CSC_DISABLED(1000),
        VOPS_OFF(1001),
        USER_SETTINGS_OFF(1002),
        MAIN_SWITCHES_OFF(1003),
        CS_TTY(1004),
        ROAMING_NOT_SUPPORTED(1005),
        LOCATION_NOT_LOADED(1006),
        PDN_ESTABLISH_TIMEOUT(1007),
        ONGOING_NW_MODE_CHANGE(1008),
        EMPTY_PCSCF(1009),
        REGI_THROTTLED(1010),
        FLIGHT_MODE_ON(1011),
        PENDING_RCS_REGI(1012),
        HIGHER_PRIORITY(1013),
        GVN_NOT_READY(1014),
        SIMMANAGER_NULL(1015),
        ENTITLEMENT_NOT_READY(1016),
        RCS_ROAMING(1017),
        TRY_RCS_CONFIG(1018),
        DM_TRIGGERED(1019),
        KDDI_EMERGENCY(1020),
        NETWORK_UNKNOWN(1021),
        NETWORK_SUSPENDED(1022),
        IP4ADDR_NOT_EXIST(SoftphoneNamespaces.SoftphoneEvents.EVENT_SEND_MESSAGE_DONE),
        RCS_ONLY_NEEDED(1024),
        ALREADY_REGISTERING(1025),
        DATA_RAT_IS_NOT_LTE(1100),
        NW_MODE_CHANGE(Id.REQUEST_VSH_START_SESSION),
        ONGOING_RCS_SESSION(Id.REQUEST_VSH_ACCEPT_SESSION),
        PS_ONLY_OR_CS_ROAMING(Id.REQUEST_VSH_STOP_SESSION),
        DM_EUTRAN_OFF(1104),
        ONGOING_OTA(1105),
        ROAMING_ON_NET_CUSTOM(1106),
        NO_MMTEL_IMS_SWITCH_OFF(Id.REQUEST_SIP_DIALOG_SEND_SIP),
        NO_MMTEL_DM_OFF(Id.REQUEST_SIP_DIALOG_OPEN),
        NO_MMTEL_VOPS_OFF(1202),
        NO_MMTEL_SSAC_BARRING(1203),
        NO_MMTEL_USER_SETTINGS_OFF(1204),
        NO_MMTEL_DSAC(1205),
        NO_MMTEL_INVITE_403(1206),
        NO_MMTEL_VOWIFI_CELLULAR_PREF(1207),
        NO_MMTEL_LIMITED_MODE(1208),
        NO_MMTEL_MPS_DISABLED(1209),
        NO_MMTEL_3G_PREFERRED_MODE(1210),
        NO_MMTEL_EPS_ONLY(1211),
        NO_MMTEL_CS_TTY(1212),
        RECOVERY_UA_CREATION_FAIL(ImSessionEvent.ADD_PARTICIPANTS),
        RECOVERY_UA_MISMATCH(ImSessionEvent.ADD_PARTICIPANTS_DONE),
        RECOVERY_UA_MISSING(ImSessionEvent.EXTEND_TO_GROUP_CHAT),
        OFFSET_DEREGI_REASON(ImSessionEvent.MESSAGING_EVENT);
        
        private int mCode;

        private REGI_FRSN(int code) {
            this.mCode = code;
        }

        public int getCode() {
            return this.mCode;
        }

        public boolean isOneOf(REGI_FRSN... reasons) {
            for (REGI_FRSN frsn : reasons) {
                if (this == frsn) {
                    return true;
                }
            }
            return false;
        }

        public static REGI_FRSN valueOf(int code) {
            for (REGI_FRSN frsn : values()) {
                if (frsn.getCode() == code) {
                    return frsn;
                }
            }
            return UNKNOWN;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getPdnType(java.lang.String r6) {
        /*
            r0 = 0
            int r1 = r6.hashCode()
            r2 = 4
            r3 = 3
            r4 = 2
            r5 = 1
            switch(r1) {
                case 104399: goto L_0x0036;
                case 3649301: goto L_0x002b;
                case 570410817: goto L_0x0021;
                case 1544803905: goto L_0x0017;
                case 1629013393: goto L_0x000d;
                default: goto L_0x000c;
            }
        L_0x000c:
            goto L_0x0040
        L_0x000d:
            java.lang.String r1 = "emergency"
            boolean r1 = r6.equals(r1)
            if (r1 == 0) goto L_0x000c
            r1 = r2
            goto L_0x0041
        L_0x0017:
            java.lang.String r1 = "default"
            boolean r1 = r6.equals(r1)
            if (r1 == 0) goto L_0x000c
            r1 = 0
            goto L_0x0041
        L_0x0021:
            java.lang.String r1 = "internet"
            boolean r1 = r6.equals(r1)
            if (r1 == 0) goto L_0x000c
            r1 = r3
            goto L_0x0041
        L_0x002b:
            java.lang.String r1 = "wifi"
            boolean r1 = r6.equals(r1)
            if (r1 == 0) goto L_0x000c
            r1 = r4
            goto L_0x0041
        L_0x0036:
            java.lang.String r1 = "ims"
            boolean r1 = r6.equals(r1)
            if (r1 == 0) goto L_0x000c
            r1 = r5
            goto L_0x0041
        L_0x0040:
            r1 = -1
        L_0x0041:
            if (r1 == 0) goto L_0x0054
            if (r1 == r5) goto L_0x0052
            if (r1 == r4) goto L_0x0050
            if (r1 == r3) goto L_0x004e
            if (r1 == r2) goto L_0x004c
            goto L_0x0056
        L_0x004c:
            r0 = 5
            goto L_0x0056
        L_0x004e:
            r0 = 4
            goto L_0x0056
        L_0x0050:
            r0 = 3
            goto L_0x0056
        L_0x0052:
            r0 = 2
            goto L_0x0056
        L_0x0054:
            r0 = 1
        L_0x0056:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.constants.ims.DiagnosisConstants.getPdnType(java.lang.String):int");
    }

    public static int getPaniPrefix(String pani) {
        if (TextUtils.isEmpty(pani)) {
            return 0;
        }
        if (pani.contains(PaniConstants.LTE_PANI_PREFIX)) {
            return 4;
        }
        if (pani.contains(PaniConstants.IWLAN_PANI_PREFIX)) {
            return 6;
        }
        if (pani.contains(PaniConstants.NR_PANI_PREFIX)) {
            return 7;
        }
        if (pani.contains(PaniConstants.NR_PANI_PREFIX_FDD)) {
            return 8;
        }
        if (pani.contains(PaniConstants.NR_PANI_PREFIX_TDD)) {
            return 9;
        }
        if (pani.contains(PaniConstants.UMTS_PANI_PREFIX)) {
            return 3;
        }
        if (pani.contains(PaniConstants.TDLTE_PANI_PREFIX)) {
            return 5;
        }
        if (pani.contains(PaniConstants.EDGE_PANI_PREFIX)) {
            return 1;
        }
        if (pani.contains(PaniConstants.EHRPD_PANI_PREFIX)) {
            return 2;
        }
        return 0;
    }

    public enum REGI_PROFILE {
        UNKNOWN(0),
        SMSIP(1),
        MMTEL(2),
        MMTEL_VIDEO(4),
        PRESENCE(8),
        IM(16),
        FT(32),
        FT_HTTP(64),
        OPTIONS(128),
        IS(256),
        VS(512),
        EC(1024),
        GLS(KEYRecord.Flags.FLAG4),
        SLM(KEYRecord.Flags.EXTEND),
        EUC(KEYRecord.Flags.FLAG2),
        PROFILE(16384),
        CDPN(32768),
        LASTSEEN(65536),
        CHATBOT_COMMUNICATION(131072);
        
        private int mValue;

        private REGI_PROFILE(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }

        public boolean compare(String svc) {
            return !TextUtils.isEmpty(svc) && normalizeSvcName(svc).equalsIgnoreCase(normalizeSvcName(toString()));
        }

        public static REGI_PROFILE fromService(String svc) {
            for (REGI_PROFILE profile : values()) {
                if (profile.compare(svc)) {
                    return profile;
                }
            }
            return UNKNOWN;
        }

        private String normalizeSvcName(String svc) {
            return svc.replaceAll("[\\W_]", "");
        }
    }

    public static String convertServiceSetToHex(Set<String> svc) {
        int rtn = 0;
        if (CollectionUtils.isNullOrEmpty((Collection<?>) svc)) {
            return intToHexStr(0);
        }
        for (String volteSvc : ImsProfile.getVoLteServiceList()) {
            if (svc.contains(volteSvc)) {
                rtn |= REGI_PROFILE.fromService(volteSvc).getValue();
            }
        }
        for (String rcsSvc : ImsProfile.getRcsServiceList()) {
            if (svc.contains(rcsSvc)) {
                rtn |= REGI_PROFILE.fromService(rcsSvc).getValue();
            }
        }
        return intToHexStr(rtn);
    }

    public static String intToHexStr(int num) {
        return String.format("0x%8s", new Object[]{Integer.toHexString(num).toUpperCase()}).replace(' ', '0');
    }
}
