package com.sec.internal.constants.ims.util;

import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import java.util.HashMap;
import java.util.Map;

public class CscParserConstants {
    private CscParserConstants() {
    }

    public static class CustomerSettingTable {
        private static final HashMap<String, String> mGlobalSettingsTable;

        public static class DeviceManagement {
            public static final String ENABLE_IMS = "EnableIMS";
            public static final String SUPPORT_VOWIFI = "EnableVoiceoverWIFI";
        }

        public static class RCS {
            public static final String ENABLE_RCS = "EnableRCS";
            public static final String ENABLE_RCS_CHATBOT = "EnableRCSchatbot";
            public static final String ENABLE_RCS_CHAT_SERVICE = "EnableRCSchat";
        }

        public static class VoLTE {
            public static final String DOMAIN_EMERGENCY_CALL = "Emregencycall_Domain";
            public static final String DOMAIN_EMERGENCY_CALL_FIX_TYPO = "Emergencycall_Domain";
            public static final String DOMAIN_SS = "SS_Domain_Preference";
            public static final String DOMAIN_USSD = "USSD_Domain_Preference";
            public static final String DOMAIN_VOICE_EUTRAN = "Voice_Domain_Preference_EUTRAN";
            public static final String ENABLE_CDPN = "EnableCdpn";
            public static final String ENABLE_SMS_IP = "EnableSMSoverIP";
            public static final String ENABLE_SS = "EnableSS";
            public static final String ENABLE_VIDEO_CALL = "EnableVideocall";
            public static final String ENABLE_VOLTE = "EnableVoLTE";
            public static final String EUTRAN_CSVOICEONLY = "CSVoiceOnly";
            public static final String EUTRAN_CSVOICEPREFERRED = "CSVoicePreferred";
            public static final String EUTRAN_IMSPSVOICEONLY = "IMSPSVoiceOnly";
            public static final String EUTRAN_IMSPSVOICEPREFERRED = "IMSPSVoicePreferred";
            public static final Map<String, String> MAP_DOMAINS_PSCS = new HashMap<String, String>() {
                {
                    put("PS", "1");
                    put("PS_ALWAYS", "1");
                    put("CS", "2");
                    put("CS_ALWAYS", "2");
                    put(DiagnosisConstants.PSCI_KEY_CALL_BEARER, DiagnosisConstants.RCSM_ORST_REGI);
                    put("PS_ONLY_VOLTEREGIED", DiagnosisConstants.RCSM_ORST_REGI);
                    put("PS_ONLY_PSREGIED", DiagnosisConstants.RCSM_ORST_HTTP);
                }
            };
            public static final Map<String, String> MAP_DOMAIN_EMERGENCY_CALL = new HashMap<String, String>() {
                {
                    put("CS", "0");
                    put("PS", "1");
                }
            };
            public static final Map<String, String> MAP_DOMAIN_VOICE_EUTRAN = new HashMap<String, String>() {
                {
                    put(VoLTE.EUTRAN_CSVOICEONLY, "1");
                    put(VoLTE.EUTRAN_CSVOICEPREFERRED, "2");
                    put(VoLTE.EUTRAN_IMSPSVOICEPREFERRED, DiagnosisConstants.RCSM_ORST_REGI);
                    put(VoLTE.EUTRAN_IMSPSVOICEONLY, DiagnosisConstants.RCSM_ORST_HTTP);
                }
            };
            public static final String SHOW_VOLTE_ICON_IN_USER = "EnableVoLTEindicator";
            public static final String SRVCC_VERSION = "SRVCCversion";
            public static final String SS_CSFB_IMS_ERROR = "SS_CSFBwithIMSerror";
        }

        static {
            HashMap<String, String> hashMap = new HashMap<>();
            mGlobalSettingsTable = hashMap;
            hashMap.put(VoLTE.SHOW_VOLTE_ICON_IN_USER, GlobalSettingsConstants.Registration.SHOW_VOLTE_REGI_ICON);
            mGlobalSettingsTable.put(VoLTE.SS_CSFB_IMS_ERROR, GlobalSettingsConstants.SS.CSFB_WITH_IMSERROR);
            mGlobalSettingsTable.put(VoLTE.DOMAIN_VOICE_EUTRAN, GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN);
            mGlobalSettingsTable.put(VoLTE.DOMAIN_EMERGENCY_CALL, GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN);
            mGlobalSettingsTable.put(VoLTE.DOMAIN_EMERGENCY_CALL_FIX_TYPO, GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN);
            mGlobalSettingsTable.put(VoLTE.DOMAIN_USSD, GlobalSettingsConstants.Call.USSD_DOMAIN);
            mGlobalSettingsTable.put(VoLTE.DOMAIN_SS, GlobalSettingsConstants.SS.DOMAIN);
            mGlobalSettingsTable.put(VoLTE.SRVCC_VERSION, GlobalSettingsConstants.Call.SRVCC_VERSION);
            mGlobalSettingsTable.put(DeviceManagement.ENABLE_IMS, GlobalSettingsConstants.Registration.IMS_ENABLED);
            mGlobalSettingsTable.put(DeviceManagement.SUPPORT_VOWIFI, GlobalSettingsConstants.Registration.SUPPORT_VOWIFI);
        }

        public static HashMap<String, String> getGlobalSettingsMappingTable() {
            return mGlobalSettingsTable;
        }
    }
}
