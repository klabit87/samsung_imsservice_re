package com.sec.internal.ims.core;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegistrationEvents {
    protected static final int DATAUSAGE_REACH_TO_LIMIT = 712;
    public static final int EVENT_BLOCK_REGISTRATION_HYS_TIMER = 806;
    public static final int EVENT_BLOCK_REGISTRATION_ROAMING_TIMER = 144;
    public static final int EVENT_BOOT_COMPLETED = 150;
    public static final int EVENT_CARRIER_CONFIG_UPDATED = 408;
    protected static final int EVENT_CELL_LOCATION_CHANGED = 24;
    public static final int EVENT_CHATBOT_AGREEMENT_CHANGED = 56;
    public static final int EVENT_CHECK_UNPROCESSED_OMADM_CONFIG = 407;
    public static final int EVENT_CONFIG_UPDATED = 35;
    public static final int EVENT_CONTACT_ACTIVATED = 809;
    protected static final int EVENT_DDS_CHANGED = 702;
    public static final int EVENT_DEFAULT_NETWORK_CHANGED = 706;
    public static final int EVENT_DELAYED_DEREGISTER = 128;
    public static final int EVENT_DELAYED_DEREGISTERINTERNAL = 145;
    public static final int EVENT_DELAYED_STOP_PDN = 133;
    public static final int EVENT_DEREGISTERED = 101;
    public static final int EVENT_DEREGISTER_BY_PENDED_DEFAULT_NET_CHANGED = 18;
    public static final int EVENT_DEREGISTER_FOR_DCN = 807;
    public static final int EVENT_DEREGISTER_TIMEOUT = 107;
    public static final int EVENT_DEREGISTRATION_REQUESTED = 120;
    public static final int EVENT_DISCONNECT_PDN_BY_HD_VOICE_ROAMING_OFF = 406;
    public static final int EVENT_DISCONNECT_PDN_BY_TIMEOUT = 404;
    public static final int EVENT_DM_CONFIG_COMPLETE = 29;
    public static final int EVENT_DM_CONFIG_TIMEOUT = 43;
    public static final int EVENT_DNS_RESPONSE = 57;
    public static final int EVENT_DO_PENDING_UPDATE_REGISTRATION = 32;
    public static final int EVENT_DO_RECOVERY_ACTION = 134;
    public static final int EVENT_DSAC_MODE_CHANGED = 146;
    public static final int EVENT_DYNAMIC_CONFIG_UPDATED = 149;
    public static final int EVENT_EMERGENCY_READY = 119;
    protected static final int EVENT_EPDG_CONNECTED = 26;
    public static final int EVENT_EPDG_DEREGISTER_REQUESTED = 124;
    public static final int EVENT_EPDG_DISCONNECTED = 27;
    public static final int EVENT_EPDG_EVENT_TIMEOUT = 135;
    protected static final int EVENT_EPDG_IKEERROR = 52;
    protected static final int EVENT_EPDG_IPSECDISCONNECTED = 54;
    public static final int EVENT_EPDG_VOICE_PREFERENCE_CHANGED = 123;
    public static final int EVENT_FINISH_OMADM_PROVISIONING_UPDATE = 39;
    public static final int EVENT_FLIGHT_MODE_CHANGED = 12;
    public static final int EVENT_FORCED_UPDATE_REGISTRATION_REQUESTED = 140;
    public static final int EVENT_FORCE_SMS_PUSH = 143;
    public static final int EVENT_GEO_LOCATION_UPDATED = 40;
    public static final int EVENT_GLOBAL_SETTINGS_UPDATED = 16;
    public static final int EVENT_HANDOFF_EVENT_TIMEOUT = 136;
    public static final int EVENT_IMS_PROFILE_UPDATED = 15;
    public static final int EVENT_IMS_SWITCH_UPDATED = 17;
    public static final int EVENT_LOCAL_IP_CHANGED = 5;
    public static final int EVENT_LOCATION_CACHE_EXPIRY = 803;
    public static final int EVENT_LOCATION_TIMEOUT = 800;
    public static final int EVENT_LTE_DATA_NETWORK_MODE_CHAGED = 139;
    public static final int EVENT_MANUAL_DEREGISTER = 10;
    public static final int EVENT_MANUAL_REGISTER = 9;
    public static final int EVENT_MNOMAP_UPDATED = 148;
    public static final int EVENT_MOBILE_DATA_CHANGED = 34;
    public static final int EVENT_MOBILE_DATA_PRESSED_CHANGED = 153;
    public static final int EVENT_NETWORK_EVENT_CHANGED = 701;
    public static final int EVENT_NETWORK_MODE_CHANGE_TIMEOUT = 49;
    public static final int EVENT_NETWORK_SUSPENDED = 151;
    public static final int EVENT_NETWORK_TYPE = 3;
    public static final int EVENT_OWN_CAPABILITIES_CHANGED = 31;
    public static final int EVENT_PCO_INFO = 703;
    public static final int EVENT_PDN_CONNECTED = 22;
    public static final int EVENT_PDN_DISCONNECTED = 23;
    protected static final int EVENT_PDN_FAILED = 129;
    public static final int EVENT_RCS_ALLOWED_CHANGED = 53;
    public static final int EVENT_RCS_DELAYED_DEREGISTER = 142;
    public static final int EVENT_RCS_USER_SETTING_CHANGED = 147;
    public static final int EVENT_REFRESH_REGISTRATION = 141;
    public static final int EVENT_REGEVENT_CONTACT_URI_NOTIFIED = 810;
    public static final int EVENT_REGISTERED = 100;
    public static final int EVENT_REGISTER_ERROR = 104;
    public static final int EVENT_REQUEST_DM_CONFIG = 28;
    public static final int EVENT_REQUEST_LOCATION = 801;
    public static final int EVENT_REQUEST_NOTIFY_VOLTE_SETTINGS_OFF = 131;
    public static final int EVENT_REQUEST_X509_CERT_VERIFY = 30;
    public static final int EVENT_ROAMING_DATA_CHANGED = 44;
    public static final int EVENT_ROAMING_LTE_CHANGED = 50;
    public static final int EVENT_ROAMING_SETTINGS_CHANGED = 46;
    public static final int EVENT_RTTMODE_UPDATED = 705;
    public static final int EVENT_SETUP_WIZARD_COMPLETED = 811;
    public static final int EVENT_SET_THIRDPARTY_FEATURE_TAGS = 126;
    public static final int EVENT_SHUTDOWN = 130;
    public static final int EVENT_SIM_READY = 20;
    public static final int EVENT_SIM_REFRESH = 36;
    public static final int EVENT_SIM_REFRESH_TIMEOUT = 42;
    protected static final int EVENT_SIM_SUBSCRIBE_ID_CHANGED = 707;
    public static final int EVENT_SSAC_REREGISTER = 121;
    public static final int EVENT_START_GEO_LOCATION_UPDATE = 51;
    public static final int EVENT_START_OMADM_PROVISIONING_UPDATE = 38;
    public static final int EVENT_SUBSCRIBE_ERROR = 108;
    public static final int EVENT_TELEPHONY_CALL_STATUS_CHANGED = 33;
    public static final int EVENT_TIMS_ESTABLISHMENT_TIMEOUT = 132;
    public static final int EVENT_TIMS_ESTABLISHMENT_TIMEOUT_RCS = 152;
    public static final int EVENT_TRY_EMERGENCY_REGISTER = 118;
    public static final int EVENT_TRY_REGISTER = 2;
    public static final int EVENT_TRY_REGISTER_TIMER = 4;
    public static final int EVENT_TTYMODE_UPDATED = 37;
    protected static final int EVENT_UICC_CHANGED = 21;
    public static final int EVENT_UPDATE_CHAT_SERVICE = 137;
    public static final int EVENT_UPDATE_REGISTRATION = 25;
    public static final int EVENT_USER_SWITCHED = 1000;
    public static final int EVENT_VIDEO_SETTING_CHANGED = 127;
    public static final int EVENT_VOLTE_ROAMING_SETTING_CHANGED = 138;
    public static final int EVENT_VOLTE_SETTING_CHANGED = 125;
    public static final int EVENT_VOWIFI_SETTING_CHANGED = 122;
    public static final int EVENT_WFC_SWITCH_PROFILE = 704;
    private static final Map<Integer, String> msgToStringMap = new HashMap();

    static {
        Arrays.stream(RegistrationEvents.class.getDeclaredFields()).filter($$Lambda$RegistrationEvents$M5gCxB0Gq43JjzJXJ3zGNQvIwU.INSTANCE).filter($$Lambda$RegistrationEvents$XQRhlIuebyxD9BrsaAiuVmp8nN4.INSTANCE).filter($$Lambda$RegistrationEvents$6khMHTLplOoXis_hIpOeqk_YCI.INSTANCE).forEach($$Lambda$RegistrationEvents$uR91aQPKD7r1pBmunfEU5SH1Ok.INSTANCE);
    }

    static /* synthetic */ void lambda$static$3(Field f) {
        try {
            msgToStringMap.put(Integer.valueOf(f.getInt((Object) null)), f.getName());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static String msgToString(int msg) {
        Map<Integer, String> map = msgToStringMap;
        Integer valueOf = Integer.valueOf(msg);
        return map.getOrDefault(valueOf, "UNKNOWN(" + msg + ")");
    }

    /* JADX WARNING: Removed duplicated region for block: B:149:0x0366  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean handleEvent(android.os.Message r6, com.sec.internal.ims.core.RegistrationManagerHandler r7, com.sec.internal.ims.core.RegistrationManagerBase r8, com.sec.internal.ims.core.NetworkEventController r9, com.sec.internal.ims.core.UserEventController r10) {
        /*
            int r0 = r6.what
            r1 = 2
            r2 = 1
            if (r0 == r1) goto L_0x0457
            r1 = 3
            java.lang.String r3 = "phoneId"
            r4 = 0
            if (r0 == r1) goto L_0x043c
            r1 = 4
            if (r0 == r1) goto L_0x0430
            r1 = 5
            if (r0 == r1) goto L_0x0428
            r1 = 9
            if (r0 == r1) goto L_0x041e
            r1 = 10
            if (r0 == r1) goto L_0x0406
            r1 = 56
            if (r0 == r1) goto L_0x0400
            r1 = 57
            if (r0 == r1) goto L_0x03f4
            r1 = 100
            if (r0 == r1) goto L_0x03eb
            r1 = 101(0x65, float:1.42E-43)
            if (r0 == r1) goto L_0x03e4
            r1 = 107(0x6b, float:1.5E-43)
            if (r0 == r1) goto L_0x03db
            r1 = 108(0x6c, float:1.51E-43)
            if (r0 == r1) goto L_0x03d4
            java.lang.String r1 = "mode"
            switch(r0) {
                case 12: goto L_0x03ca;
                case 20: goto L_0x03b9;
                case 21: goto L_0x03a8;
                case 22: goto L_0x039f;
                case 23: goto L_0x0396;
                case 24: goto L_0x038f;
                case 25: goto L_0x0384;
                case 26: goto L_0x037d;
                case 27: goto L_0x0376;
                case 28: goto L_0x036c;
                case 29: goto L_0x0360;
                case 30: goto L_0x0357;
                case 31: goto L_0x034c;
                case 32: goto L_0x0347;
                case 33: goto L_0x033e;
                case 34: goto L_0x0335;
                case 35: goto L_0x032a;
                case 36: goto L_0x0319;
                case 37: goto L_0x0308;
                case 38: goto L_0x02fb;
                case 39: goto L_0x02ee;
                case 40: goto L_0x02e9;
                case 46: goto L_0x02e0;
                case 104: goto L_0x02d9;
                case 131: goto L_0x02d0;
                case 132: goto L_0x02c7;
                case 133: goto L_0x02be;
                case 134: goto L_0x02b5;
                case 135: goto L_0x02a1;
                case 136: goto L_0x029a;
                case 137: goto L_0x0293;
                case 138: goto L_0x0287;
                case 139: goto L_0x027b;
                case 140: goto L_0x0272;
                case 141: goto L_0x0265;
                case 142: goto L_0x0260;
                case 144: goto L_0x0257;
                case 145: goto L_0x0249;
                case 146: goto L_0x0244;
                case 147: goto L_0x0235;
                case 148: goto L_0x022e;
                case 149: goto L_0x0227;
                case 150: goto L_0x0222;
                case 151: goto L_0x0214;
                case 152: goto L_0x02c7;
                case 153: goto L_0x020b;
                case 404: goto L_0x0202;
                case 406: goto L_0x01f9;
                case 407: goto L_0x01f4;
                case 408: goto L_0x0227;
                case 701: goto L_0x01e9;
                case 702: goto L_0x01e4;
                case 703: goto L_0x01d7;
                case 704: goto L_0x01cc;
                case 705: goto L_0x01bb;
                case 706: goto L_0x01b4;
                case 707: goto L_0x01a7;
                case 712: goto L_0x019b;
                case 800: goto L_0x0192;
                case 801: goto L_0x018d;
                case 803: goto L_0x0184;
                case 807: goto L_0x017b;
                case 809: goto L_0x0168;
                case 810: goto L_0x0161;
                case 811: goto L_0x0457;
                case 1000: goto L_0x015c;
                default: goto L_0x0038;
            }
        L_0x0038:
            switch(r0) {
                case 15: goto L_0x0155;
                case 16: goto L_0x014e;
                case 17: goto L_0x0141;
                case 18: goto L_0x0129;
                default: goto L_0x003b;
            }
        L_0x003b:
            switch(r0) {
                case 42: goto L_0x0120;
                case 43: goto L_0x0360;
                case 44: goto L_0x0114;
                default: goto L_0x003e;
            }
        L_0x003e:
            switch(r0) {
                case 49: goto L_0x010f;
                case 50: goto L_0x0102;
                case 51: goto L_0x00f4;
                case 52: goto L_0x00ed;
                case 53: goto L_0x00e8;
                case 54: goto L_0x00e1;
                default: goto L_0x0041;
            }
        L_0x0041:
            switch(r0) {
                case 118: goto L_0x00d8;
                case 119: goto L_0x00d1;
                case 120: goto L_0x00bc;
                case 121: goto L_0x00aa;
                case 122: goto L_0x00a3;
                case 123: goto L_0x008b;
                case 124: goto L_0x0084;
                case 125: goto L_0x0075;
                case 126: goto L_0x006e;
                case 127: goto L_0x005f;
                case 128: goto L_0x0056;
                case 129: goto L_0x0045;
                default: goto L_0x0044;
            }
        L_0x0044:
            return r4
        L_0x0045:
            int r0 = r6.arg1
            int r1 = r6.arg2
            com.sec.internal.ims.util.ImsUtil$PdnFailReason r3 = com.sec.internal.ims.util.ImsUtil.PdnFailReason.valueOf((int) r1)
            java.lang.String r3 = r3.toString()
            r9.onPdnFailed(r0, r3)
            goto L_0x0463
        L_0x0056:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r8.onDelayedDeregister(r0)
            goto L_0x0463
        L_0x005f:
            java.lang.Object r0 = r6.obj
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            boolean r0 = r0.booleanValue()
            int r1 = r6.arg1
            r10.onVideoCallServiceSettingChanged(r0, r1)
            goto L_0x0463
        L_0x006e:
            int r0 = r6.arg1
            r7.onThirdParyFeatureTagsUpdated(r0)
            goto L_0x0463
        L_0x0075:
            java.lang.Object r0 = r6.obj
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            boolean r0 = r0.booleanValue()
            int r1 = r6.arg1
            r10.onVolteServiceSettingChanged(r0, r1)
            goto L_0x0463
        L_0x0084:
            int r0 = r6.arg1
            r9.onEpdgDeregisterRequested(r0)
            goto L_0x0463
        L_0x008b:
            int r0 = r6.arg1
            int r1 = r6.arg2
            if (r1 != r2) goto L_0x0092
            r4 = r2
        L_0x0092:
            r1 = r4
            boolean r3 = r8.isCdmaAvailableForVoice(r0)
            if (r3 == r1) goto L_0x0463
            r8.setCdmaAvailableForVoice(r0, r1)
            int r3 = r6.arg1
            r9.onVoicePreferredChanged(r3)
            goto L_0x0463
        L_0x00a3:
            int r0 = r6.arg1
            r10.onVowifiServiceSettingChanged(r0, r7)
            goto L_0x0463
        L_0x00aa:
            java.lang.Object r0 = r6.obj
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
            int r1 = r6.arg1
            if (r1 != r2) goto L_0x00b7
            r4 = r2
        L_0x00b7:
            r7.onSSACRegiRequested(r0, r4)
            goto L_0x0463
        L_0x00bc:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            int r1 = r6.arg1
            if (r1 != r2) goto L_0x00c6
            r1 = r2
            goto L_0x00c7
        L_0x00c6:
            r1 = r4
        L_0x00c7:
            int r3 = r6.arg2
            if (r3 != r2) goto L_0x00cc
            r4 = r2
        L_0x00cc:
            r7.onDeregistrationRequest(r0, r1, r4)
            goto L_0x0463
        L_0x00d1:
            int r0 = r6.arg1
            r8.onEmergencyReady(r0)
            goto L_0x0463
        L_0x00d8:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r8.tryEmergencyRegister(r0)
            goto L_0x0463
        L_0x00e1:
            int r0 = r6.arg1
            r9.onIpsecDisconnected(r0)
            goto L_0x0463
        L_0x00e8:
            r7.onRCSAllowedChangedbyMDM()
            goto L_0x0463
        L_0x00ed:
            int r0 = r6.arg1
            r9.onEpdgIkeError(r0)
            goto L_0x0463
        L_0x00f4:
            java.lang.Object r0 = r6.obj
            com.sec.internal.constants.ims.gls.LocationInfo r0 = (com.sec.internal.constants.ims.gls.LocationInfo) r0
            int r1 = r6.arg1
            if (r1 != r2) goto L_0x00fd
            r4 = r2
        L_0x00fd:
            r8.updateGeolocation(r0, r4)
            goto L_0x0463
        L_0x0102:
            java.lang.Object r0 = r6.obj
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            boolean r0 = r0.booleanValue()
            r10.onRoamingLteChanged(r0)
            goto L_0x0463
        L_0x010f:
            r8.tryRegister()
            goto L_0x0463
        L_0x0114:
            int r0 = r6.arg1
            if (r0 != r2) goto L_0x0119
            r4 = r2
        L_0x0119:
            int r0 = r6.arg2
            r10.onRoamingDataChanged(r4, r0)
            goto L_0x0463
        L_0x0120:
            r0 = 42
            int r1 = r6.arg1
            r8.sendDeregister((int) r0, (int) r1)
            goto L_0x0463
        L_0x0129:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            int r1 = r0.getPhoneId()
            r9.isPreferredPdnForRCSRegister(r0, r1, r4)
            com.sec.internal.ims.core.RegistrationManagerHandler r1 = r8.mHandler
            int r3 = r0.getPhoneId()
            r4 = 2000(0x7d0, double:9.88E-321)
            r1.sendTryRegister(r3, r4)
            goto L_0x0463
        L_0x0141:
            java.lang.Object r0 = r6.obj
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
            r8.onImsSwitchUpdated(r0)
            goto L_0x0463
        L_0x014e:
            int r0 = r6.arg1
            r7.handleGlobalSettingsUpdated(r0)
            goto L_0x0463
        L_0x0155:
            int r0 = r6.arg1
            r8.onImsProfileUpdated(r0)
            goto L_0x0463
        L_0x015c:
            r10.onUserSwitched()
            goto L_0x0463
        L_0x0161:
            java.lang.Object r0 = r6.obj
            r7.onRegEventContactUriNotified(r0)
            goto L_0x0463
        L_0x0168:
            int r0 = r6.arg2
            int r1 = r6.arg1
            com.sec.internal.ims.core.RegisterTask r0 = r8.getRegisterTaskByProfileId(r0, r1)
            java.util.Optional r0 = java.util.Optional.ofNullable(r0)
            com.sec.internal.ims.core.-$$Lambda$RegistrationEvents$Vp4uFqqTWWQqyyuQrM_L_-eSEhU r1 = com.sec.internal.ims.core.$$Lambda$RegistrationEvents$Vp4uFqqTWWQqyyuQrM_L_eSEhU.INSTANCE
            r0.ifPresent(r1)
            goto L_0x0463
        L_0x017b:
            r0 = 807(0x327, float:1.131E-42)
            int r1 = r6.arg1
            r8.sendDeregister((int) r0, (int) r1)
            goto L_0x0463
        L_0x0184:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r7.onLocationCacheExpired(r0)
            goto L_0x0463
        L_0x018d:
            r7.onRequestLocation()
            goto L_0x0463
        L_0x0192:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r7.onLocationTimerExpired(r0)
            goto L_0x0463
        L_0x019b:
            int r0 = r6.arg1
            if (r0 != r2) goto L_0x01a0
            r4 = r2
        L_0x01a0:
            int r0 = r6.arg2
            r10.onDataUsageLimitReached(r4, r0)
            goto L_0x0463
        L_0x01a7:
            java.lang.Object r0 = r6.obj
            com.sec.internal.helper.AsyncResult r0 = (com.sec.internal.helper.AsyncResult) r0
            java.lang.Object r1 = r0.result
            android.telephony.SubscriptionInfo r1 = (android.telephony.SubscriptionInfo) r1
            r7.onSimSubscribeIdChanged(r1)
            goto L_0x0463
        L_0x01b4:
            int r0 = r6.arg1
            r9.onDefaultNetworkStateChanged(r0)
            goto L_0x0463
        L_0x01bb:
            java.lang.Object r0 = r6.obj
            android.os.Bundle r0 = (android.os.Bundle) r0
            int r3 = r0.getInt(r3)
            boolean r1 = r0.getBoolean(r1)
            r10.onRTTmodeUpdated(r3, r1)
            goto L_0x0463
        L_0x01cc:
            java.lang.Object r0 = r6.obj
            byte[] r0 = (byte[]) r0
            int r1 = r6.arg1
            r7.onWfcSwitchProfile(r0, r1)
            goto L_0x0463
        L_0x01d7:
            java.lang.Object r0 = r6.obj
            java.lang.String r0 = (java.lang.String) r0
            int r1 = r6.arg1
            int r3 = r6.arg2
            r7.onPcoInfo(r0, r1, r3)
            goto L_0x0463
        L_0x01e4:
            r8.onDefaultDataSubscriptionChanged()
            goto L_0x0463
        L_0x01e9:
            java.lang.Object r0 = r6.obj
            com.sec.internal.constants.ims.os.NetworkEvent r0 = (com.sec.internal.constants.ims.os.NetworkEvent) r0
            int r1 = r6.arg1
            r9.onNetworkEventChanged(r0, r1)
            goto L_0x0463
        L_0x01f4:
            r9.onCheckUnprocessedOmadmConfig()
            goto L_0x0463
        L_0x01f9:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r7.onDisconnectPdnByHDvoiceRoamingOff(r0)
            goto L_0x0463
        L_0x0202:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r7.onDisconnectPdnByTimeout(r0)
            goto L_0x0463
        L_0x020b:
            int r0 = r6.arg1
            int r1 = r6.arg2
            r10.onMobileDataPressedChanged(r0, r1, r9)
            goto L_0x0463
        L_0x0214:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            int r1 = r6.arg1
            if (r1 != r2) goto L_0x021d
            r4 = r2
        L_0x021d:
            r8.suspended(r0, r4)
            goto L_0x0463
        L_0x0222:
            r7.onBootCompleted()
            goto L_0x0463
        L_0x0227:
            int r0 = r6.arg1
            r7.handleDynamicConfigUpdated(r0)
            goto L_0x0463
        L_0x022e:
            int r0 = r6.arg1
            r7.handleMnoMapUpdated(r0)
            goto L_0x0463
        L_0x0235:
            java.lang.Object r0 = r6.obj
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
            int r1 = r6.arg1
            r10.onRcsUserSettingChanged(r0, r1)
            goto L_0x0463
        L_0x0244:
            r7.onDsacModeChanged()
            goto L_0x0463
        L_0x0249:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            int r1 = r6.arg1
            if (r1 != r2) goto L_0x0252
            r4 = r2
        L_0x0252:
            r7.onDelayedDeregisterInternal(r0, r4)
            goto L_0x0463
        L_0x0257:
            int r0 = r6.arg1
            int r1 = r6.arg2
            r7.onBlockRegistrationRoamingTimer(r0, r1)
            goto L_0x0463
        L_0x0260:
            r7.onRcsDelayedDeregister()
            goto L_0x0463
        L_0x0265:
            int r0 = r6.arg1
            com.sec.internal.ims.core.RegisterTask r1 = r8.getRegisterTaskByRegHandle(r0)
            if (r1 == 0) goto L_0x0463
            r8.onRefreshRegistration(r1, r0)
            goto L_0x0463
        L_0x0272:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r8.onForcedUpdateRegistrationRequested(r0)
            goto L_0x0463
        L_0x027b:
            int r0 = r6.arg1
            if (r0 != r2) goto L_0x0280
            r4 = r2
        L_0x0280:
            int r0 = r6.arg2
            r10.onLteDataNetworkModeSettingChanged(r4, r0)
            goto L_0x0463
        L_0x0287:
            int r0 = r6.arg1
            if (r0 != r2) goto L_0x028c
            r4 = r2
        L_0x028c:
            int r0 = r6.arg2
            r10.onVolteRoamingServiceSettingChanged(r4, r0)
            goto L_0x0463
        L_0x0293:
            int r0 = r6.arg1
            r7.enableIpme(r0)
            goto L_0x0463
        L_0x029a:
            int r0 = r6.arg1
            r9.handOffEventTimeout(r0)
            goto L_0x0463
        L_0x02a1:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            if (r0 == 0) goto L_0x0463
            java.lang.String r1 = "ePDG timeout"
            r0.setReason(r1)
            java.lang.Object r1 = r6.obj
            com.sec.internal.ims.core.RegisterTask r1 = (com.sec.internal.ims.core.RegisterTask) r1
            r8.updateRegistration(r1, r4)
            goto L_0x0463
        L_0x02b5:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r7.doRecoveryAction(r0)
            goto L_0x0463
        L_0x02be:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r7.handleDelayedStopPdn(r0)
            goto L_0x0463
        L_0x02c7:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r7.onTimsTimerExpired(r0)
            goto L_0x0463
        L_0x02d0:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r7.onRequestNotifyVolteSettingsOff(r0)
            goto L_0x0463
        L_0x02d9:
            java.lang.Object r0 = r6.obj
            r7.onRegisterError(r0)
            goto L_0x0463
        L_0x02e0:
            int r0 = r6.arg1
            int r1 = r6.arg2
            r10.onRoamingSettingsChanged(r0, r1)
            goto L_0x0463
        L_0x02e9:
            r7.onGeoLocationUpdated()
            goto L_0x0463
        L_0x02ee:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r1 = r0.getGovernor()
            r1.finishOmadmProvisioningUpdate()
            goto L_0x0463
        L_0x02fb:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r1 = r0.getGovernor()
            r1.startOmadmProvisioningUpdate()
            goto L_0x0463
        L_0x0308:
            java.lang.Object r0 = r6.obj
            android.os.Bundle r0 = (android.os.Bundle) r0
            int r3 = r0.getInt(r3)
            boolean r1 = r0.getBoolean(r1)
            r10.onTTYmodeUpdated(r3, r1)
            goto L_0x0463
        L_0x0319:
            java.lang.Object r0 = r6.obj
            com.sec.internal.helper.AsyncResult r0 = (com.sec.internal.helper.AsyncResult) r0
            java.lang.Object r1 = r0.result
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            r8.onSimRefresh(r1)
            goto L_0x0463
        L_0x032a:
            java.lang.Object r0 = r6.obj
            java.lang.String r0 = (java.lang.String) r0
            int r1 = r6.arg1
            r7.onConfigUpdated(r0, r1)
            goto L_0x0463
        L_0x0335:
            int r0 = r6.arg1
            int r1 = r6.arg2
            r10.onMobileDataChanged(r0, r1, r9)
            goto L_0x0463
        L_0x033e:
            int r0 = r6.arg1
            int r1 = r6.arg2
            r7.onTelephonyCallStatusChanged(r0, r1)
            goto L_0x0463
        L_0x0347:
            r8.onPendingUpdateRegistration()
            goto L_0x0463
        L_0x034c:
            int r0 = r6.arg1
            java.lang.Object r1 = r6.obj
            com.sec.ims.options.Capabilities r1 = (com.sec.ims.options.Capabilities) r1
            r8.onOwnCapabilitiesChanged(r0, r1)
            goto L_0x0463
        L_0x0357:
            java.lang.Object r0 = r6.obj
            java.security.cert.X509Certificate[] r0 = (java.security.cert.X509Certificate[]) r0
            r7.verifyX509Certificate(r0)
            goto L_0x0463
        L_0x0360:
            int r0 = r6.what
            r1 = 29
            if (r0 != r1) goto L_0x0367
            r4 = r2
        L_0x0367:
            r9.onDmConfigCompleted(r4)
            goto L_0x0463
        L_0x036c:
            com.sec.internal.ims.core.RegistrationManager$OmadmConfigState r0 = com.sec.internal.ims.core.RegistrationManager.OmadmConfigState.IDLE
            r8.setOmadmState(r0)
            r9.triggerOmadmConfig()
            goto L_0x0463
        L_0x0376:
            int r0 = r6.arg1
            r9.onEpdgDisconnected(r0)
            goto L_0x0463
        L_0x037d:
            int r0 = r6.arg1
            r9.onEpdgConnected(r0)
            goto L_0x0463
        L_0x0384:
            java.lang.Object r0 = r6.obj
            com.sec.ims.settings.ImsProfile r0 = (com.sec.ims.settings.ImsProfile) r0
            int r1 = r6.arg1
            r7.onUpdateRegistration(r0, r1)
            goto L_0x0463
        L_0x038f:
            int r0 = r6.arg1
            r9.onCellLocationChanged(r0)
            goto L_0x0463
        L_0x0396:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r9.onPdnDisconnected(r0)
            goto L_0x0463
        L_0x039f:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r9.onPdnConnected(r0)
            goto L_0x0463
        L_0x03a8:
            java.lang.Object r0 = r6.obj
            com.sec.internal.helper.AsyncResult r0 = (com.sec.internal.helper.AsyncResult) r0
            java.lang.Object r1 = r0.userObj
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            r7.handleUiccChanged(r1)
            goto L_0x0463
        L_0x03b9:
            java.lang.Object r0 = r6.obj
            com.sec.internal.helper.AsyncResult r0 = (com.sec.internal.helper.AsyncResult) r0
            java.lang.Object r1 = r0.result
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            r7.handleSimReady(r1, r0)
            goto L_0x0463
        L_0x03ca:
            int r0 = r6.arg1
            if (r0 != r2) goto L_0x03cf
            r4 = r2
        L_0x03cf:
            r7.onFlightModeChanged(r4)
            goto L_0x0463
        L_0x03d4:
            java.lang.Object r0 = r6.obj
            r7.onSubscribeError(r0)
            goto L_0x0463
        L_0x03db:
            java.lang.Object r0 = r6.obj
            com.sec.internal.interfaces.ims.core.IRegisterTask r0 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r0
            r7.onDeregisterTimeout(r0)
            goto L_0x0463
        L_0x03e4:
            java.lang.Object r0 = r6.obj
            r7.onDeregistered(r0)
            goto L_0x0463
        L_0x03eb:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r7.onRegistered(r0)
            goto L_0x0463
        L_0x03f4:
            java.lang.Object r0 = r6.obj
            java.util.List r0 = (java.util.List) r0
            int r1 = r6.arg1
            int r3 = r6.arg2
            r8.onDnsResponse(r0, r1, r3)
            goto L_0x0463
        L_0x0400:
            int r0 = r6.arg1
            r10.onChatbotAgreementChanged(r0)
            goto L_0x0463
        L_0x0406:
            java.lang.Object r0 = r6.obj
            android.os.Bundle r0 = (android.os.Bundle) r0
            java.lang.String r1 = "id"
            int r1 = r0.getInt(r1)
            java.lang.String r4 = "explicitDeregi"
            boolean r4 = r0.getBoolean(r4)
            int r3 = r0.getInt(r3)
            r8.onManualDeregister(r1, r4, r3)
            goto L_0x0463
        L_0x041e:
            java.lang.Object r0 = r6.obj
            com.sec.ims.settings.ImsProfile r0 = (com.sec.ims.settings.ImsProfile) r0
            int r1 = r6.arg1
            r8.onManualRegister(r0, r1)
            goto L_0x0463
        L_0x0428:
            java.lang.Object r0 = r6.obj
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            r9.onLocalIpChanged(r0)
            goto L_0x0463
        L_0x0430:
            java.lang.Object r0 = r6.obj
            com.sec.internal.interfaces.ims.core.IRegisterTask r0 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r0
            int r0 = r0.getPhoneId()
            r8.tryRegister((int) r0)
            goto L_0x0463
        L_0x043c:
            java.lang.Object r0 = r6.obj
            android.os.Bundle r0 = (android.os.Bundle) r0
            java.lang.String r1 = "networkType"
            int r1 = r0.getInt(r1)
            java.lang.String r5 = "isWifiConnected"
            int r5 = r0.getInt(r5)
            if (r5 != r2) goto L_0x044f
            r4 = r2
        L_0x044f:
            int r3 = r0.getInt(r3)
            r9.onNetworkChanged(r1, r4, r3)
            goto L_0x0463
        L_0x0457:
            java.lang.Object r0 = r6.obj
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
            r8.tryRegister((int) r0)
        L_0x0463:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationEvents.handleEvent(android.os.Message, com.sec.internal.ims.core.RegistrationManagerHandler, com.sec.internal.ims.core.RegistrationManagerBase, com.sec.internal.ims.core.NetworkEventController, com.sec.internal.ims.core.UserEventController):boolean");
    }
}
