package com.sec.internal.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.IVoIPInterface;
import android.os.ServiceManager;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.VolteConstants;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorVzw;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.RegistrationEvents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.log.IMSLog;
import java.util.Locale;

public class ImsCallUtil {
    public static final String ECC_SERVICE_URN_DEFAULT = "urn:service:sos";
    private static final String LOG_TAG = ImsCallUtil.class.getSimpleName();

    public static class NOTIFY_CALL_END_MODE {
        public static final int ENDCALL = 1;
        public static final int LOCAL_RELEASE_CALL = 3;
        public static final int REJECTCALL = 2;
    }

    public static String validatePhoneNumber(String phonenumber, String countryCode) {
        String str = LOG_TAG;
        Log.d(str, "validatePhoneNumber: " + IMSLog.checker(phonenumber));
        String validPhoneNumber = "";
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber msisdnPhoneNumber = phoneUtil.parse(phonenumber, countryCode.toUpperCase(Locale.US));
            if (phoneUtil.isValidNumber(msisdnPhoneNumber)) {
                validPhoneNumber = phoneUtil.format(msisdnPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (NumberParseException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "NumberParseException : " + e.toString());
        } catch (NullPointerException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "NullPointerException : " + e2.toString());
        }
        if (TextUtils.isEmpty(validPhoneNumber)) {
            String str4 = LOG_TAG;
            Log.w(str4, "validatePhoneNumber: phonenumber " + IMSLog.checker(phonenumber) + " is not valid");
        }
        return validPhoneNumber;
    }

    public static int convertDeregiReason(int reason) {
        if (reason != 33) {
            return 14;
        }
        return 10;
    }

    public static int convertCallEndReasonToFramework(int endCallMode, int reason) {
        if (endCallMode == 2) {
            if (reason == 7) {
                return 1108;
            }
            if (reason == 11) {
                return NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE;
            }
            if (reason != 13) {
                return 200;
            }
            return 1802;
        } else if (reason == 4) {
            return 1107;
        } else {
            if (reason == 11) {
                return Id.REQUEST_SIP_DIALOG_OPEN;
            }
            if (reason == 12) {
                return 2503;
            }
            if (reason == 14) {
                return 1115;
            }
            if (reason == 15) {
                return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS;
            }
            if (reason == 20) {
                return 6007;
            }
            if (reason == 21) {
                return 1703;
            }
            if (reason == 26) {
                return 6009;
            }
            if (reason != 27) {
                return 200;
            }
            return 6008;
        }
    }

    public static boolean isImsOutageError(SipError error) {
        if (error != null && SipErrorVzw.IMS_OUTAGE.getCode() == error.getCode() && !TextUtils.isEmpty(error.getReason()) && error.getReason().toLowerCase(Locale.US).contains("Outage".toLowerCase())) {
            return true;
        }
        return false;
    }

    public static boolean isImsForbiddenError(SipError error) {
        if (error != null && SipErrorBase.FORBIDDEN.getCode() == error.getCode() && !TextUtils.isEmpty(error.getReason()) && error.getReason().toLowerCase(Locale.US).contains("Forbidden".toLowerCase()) && !error.getReason().toLowerCase(Locale.US).contains(RegistrationConstants.REASON_REGISTERED.toLowerCase())) {
            return true;
        }
        return false;
    }

    public static boolean isTimerVzwExpiredError(SipError error) {
        return 2501 == error.getCode();
    }

    public static boolean isVideoCall(int callType) {
        if (callType == 2 || callType == 3 || callType == 4 || callType == 6 || callType == 8) {
            return true;
        }
        return false;
    }

    public static boolean isEmergencyAudioCall(int callType) {
        return callType == 7 || callType == 18 || callType == 13;
    }

    public static boolean isEmergencyVideoCall(int callType) {
        return callType == 8 || callType == 19;
    }

    public static boolean isCameraUsingCall(int callType) {
        return isVideoCall(callType) && callType != 4;
    }

    public static boolean isTtyCall(int callType) {
        switch (callType) {
            case 9:
            case 10:
            case 11:
                return true;
            default:
                return false;
        }
    }

    public static boolean isRttCall(int callType) {
        switch (callType) {
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                return true;
            default:
                return false;
        }
    }

    public static boolean isRttEmergencyCall(int callType) {
        if (callType == 18 || callType == 19) {
            return true;
        }
        return false;
    }

    public static String convertSpecialChar(String uri) {
        if (uri == null) {
            return null;
        }
        if (uri.contains("%23")) {
            return uri.replaceAll("%23", "#");
        }
        return uri;
    }

    public static String removeUriPlusPrefix(String uri, boolean hidePrivateInfo) {
        String trimUri = uri;
        if (uri != null && uri.length() >= 3 && uri.startsWith("+1")) {
            trimUri = uri.substring(2);
        }
        if (hidePrivateInfo) {
            Log.v(LOG_TAG, "removeUriPlusPrefix : [ xxxxxxxxxxx ] -> : [ xxxxxxxxxxx ]");
        } else {
            String str = LOG_TAG;
            Log.v(str, "removeUriPlusPrefix : [" + uri + "] -> : [" + trimUri + "]");
        }
        return trimUri;
    }

    public static String removeUriPlusPrefix(String uri, String prefix, String replace, boolean hidePrivateInfo) {
        String trimUri = uri;
        if (uri != null && uri.length() >= prefix.length() + 1 && uri.startsWith(prefix)) {
            trimUri = uri.replace(prefix, replace);
        }
        if (hidePrivateInfo) {
            Log.v(LOG_TAG, "removeUriPlusPrefix : [ xxxxxxxxxxx ] -> : [ xxxxxxxxxxx ]");
        } else {
            String str = LOG_TAG;
            Log.v(str, "removeUriPlusPrefix : [" + uri + "] -> : [" + trimUri + "]");
        }
        return trimUri;
    }

    public static String getRemoteCallerId(NameAddr addr, Mno mno, boolean hidePrivateInfo) {
        String callerId = null;
        if (addr != null) {
            ImsUri uri = addr.getUri();
            if (mno != null) {
                if (mno == Mno.KDDI || mno == Mno.CTC || mno == Mno.CTCMO || mno == Mno.MDMN) {
                    callerId = addr.getDisplayName();
                } else if (uri != null && mno == Mno.VZW) {
                    callerId = removeUriPlusPrefix(uri.getMsisdn(), hidePrivateInfo);
                }
            }
            if (TextUtils.isEmpty(callerId) && uri != null) {
                if (uri.getUriType() == ImsUri.UriType.URN) {
                    Log.d(LOG_TAG, "getRemoteCallerId: dialing number for Urn from display name");
                    callerId = addr.getDisplayName();
                } else {
                    callerId = uri.getMsisdn();
                    if (mno == Mno.TELKOM_SOUTHAFRICA && uri.getPhoneContext() != null && uri.getUriType() == ImsUri.UriType.TEL_URI) {
                        callerId = uri.getPhoneContext() + callerId;
                    }
                }
            }
        }
        if (TextUtils.isEmpty(callerId)) {
            Log.d(LOG_TAG, "getRemoteCallerId: indefinite.");
            callerId = "anonymous";
        }
        return convertSpecialChar(callerId);
    }

    public static boolean isCSFBbySIPErrorCode(int errorCode) {
        String str = LOG_TAG;
        Log.e(str, "isCSFBbySIPErrorCode: " + errorCode);
        switch (errorCode) {
            case 380:
            case 400:
            case 403:
            case 404:
            case AECNamespace.HttpResponseCode.METHOD_NOT_ALLOWED:
            case RegistrationEvents.EVENT_DISCONNECT_PDN_BY_HD_VOICE_ROAMING_OFF /*406*/:
            case 408:
            case AECNamespace.HttpResponseCode.UNSUPPORTED_MEDIA_TYPE:
            case NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE:
            case 484:
            case 488:
            case 500:
            case 503:
            case Id.REQUEST_UPDATE_TIME_IN_PLANI /*603*/:
            case 604:
            case 606:
            case 1112:
                return true;
            default:
                return false;
        }
    }

    public static boolean isE911Call(int callType) {
        if (callType == 7 || callType == 8 || callType == 13 || callType == 18 || callType == 19) {
            return true;
        }
        return false;
    }

    public static boolean isUpgradeCall(int oldCallType, int newCallType) {
        if (oldCallType == 1 || oldCallType == 9 || oldCallType == 10 || oldCallType == 11) {
            return newCallType == 2 || newCallType == 4 || newCallType == 3;
        }
        if (oldCallType == 2) {
            return false;
        }
        return oldCallType == 4 ? newCallType == 2 : oldCallType == 3 ? newCallType == 2 : oldCallType == 7 ? newCallType == 8 : oldCallType == 5 && newCallType == 6;
    }

    public static boolean isOneWayVideoCall(int callType) {
        if (callType == 4 || callType == 3) {
            return true;
        }
        return false;
    }

    public static boolean isSamsungFmcConnected() {
        boolean ret = false;
        try {
            IVoIPInterface voipCall = IVoIPInterface.Stub.asInterface(ServiceManager.checkService("voip"));
            if (voipCall != null) {
                ret = voipCall.isVoIPActivated();
            }
            String str = LOG_TAG;
            Log.d(str, "isSamsungFmcConnected - " + ret);
        } catch (Exception e) {
            String str2 = LOG_TAG;
            Log.e(str2, "isSamsungFmcConnected - " + e);
        }
        return ret;
    }

    public static String convertEccCatToUrn(int eccCat) {
        if (eccCat == 1) {
            return "urn:service:sos.police";
        }
        if (eccCat == 2) {
            return "urn:service:sos.ambulance";
        }
        if (eccCat == 4) {
            return "urn:service:sos.fire";
        }
        if (eccCat == 8) {
            return "urn:service:sos.marine";
        }
        if (eccCat == 16) {
            return "urn:service:sos.mountain";
        }
        if (eccCat == 20) {
            return "urn:service:sos.traffic";
        }
        if (eccCat == 254) {
            return "urn:service:unspecified";
        }
        return ECC_SERVICE_URN_DEFAULT;
    }

    public static String convertEccCatToUrnSpecificKor(int eccCat) {
        if (eccCat == 1) {
            return "urn:service:sos.police";
        }
        if (eccCat == 4) {
            return "urn:service:sos.fire";
        }
        if (eccCat == 8) {
            return "urn:service:sos.marine";
        }
        if (eccCat == 254) {
            return "urn:service:unspecified";
        }
        if (eccCat == 18) {
            return "urn:service:sos.country-specific.kr.117";
        }
        if (eccCat == 3) {
            return "urn:service:sos.country-specific.kr.113";
        }
        if (eccCat == 7 || eccCat == 6) {
            return "urn:service:sos.country-specific.kr.111";
        }
        if (eccCat == 19) {
            return "urn:service:sos.country-specific.kr.118";
        }
        if (eccCat == 9) {
            return "urn:service:sos.country-specific.kr.125";
        }
        return ECC_SERVICE_URN_DEFAULT;
    }

    public static int convertUrnToEccCat(String eccCatURN) {
        if (eccCatURN.equals("urn:service:sos.police")) {
            return 1;
        }
        if (eccCatURN.equals("urn:service:sos.ambulance")) {
            return 2;
        }
        if (eccCatURN.equals("urn:service:sos.fire")) {
            return 4;
        }
        if (eccCatURN.equals("urn:service:sos.marine")) {
            return 8;
        }
        if (eccCatURN.equals("urn:service:sos.mountain")) {
            return 16;
        }
        if (eccCatURN.equals("urn:service:sos.traffic")) {
            return 20;
        }
        if (eccCatURN.equals(ECC_SERVICE_URN_DEFAULT)) {
            return 0;
        }
        return 254;
    }

    public static boolean isMultiPdnRat(int rat) {
        if (rat == 1 || rat == 2 || rat == 16 || rat == 3 || rat == 8 || rat == 9 || rat == 10 || rat == 14 || rat == 15 || rat == 17) {
            return true;
        }
        return false;
    }

    public static int getCallTypeForRtt(int currCallType, boolean mode) {
        if (mode) {
            if (currCallType == 1) {
                return 14;
            }
            if (currCallType == 2) {
                return 15;
            }
            if (currCallType == 5) {
                return 16;
            }
            if (currCallType == 6) {
                return 17;
            }
            if (currCallType == 7) {
                return 18;
            }
            if (currCallType == 8) {
                return 19;
            }
            if (currCallType == 18) {
                return 7;
            }
            if (currCallType == 14) {
                return 1;
            }
            if (currCallType == 15) {
                return 2;
            }
            if (currCallType == 17) {
                return 6;
            }
            if (currCallType == 16) {
                return 5;
            }
            if (currCallType == 19) {
                return 8;
            }
            return 0;
        } else if (currCallType == 14) {
            return 1;
        } else {
            if (currCallType == 15) {
                return 2;
            }
            if (currCallType == 18) {
                return 7;
            }
            if (currCallType == 19) {
                return 8;
            }
            if (currCallType == 16) {
                return 5;
            }
            if (currCallType == 17) {
                return 6;
            }
            return 0;
        }
    }

    public static String getAudioMode(int direction) {
        if (direction == 0) {
            return "SAE";
        }
        if (direction == 1) {
            return "CPVE";
        }
        if (direction == 2) {
            return "STOP";
        }
        if (direction == 4) {
            return "CMC_AUTO";
        }
        if (direction == 5) {
            return "CMC_CS_RELAY";
        }
        if (direction == 7) {
            return "DELAYED_MEDIA";
        }
        if (direction != 8) {
            return "AUTO";
        }
        return "DELAYED_MEDIA_CMC";
    }

    /* renamed from: com.sec.internal.helper.ImsCallUtil$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE;

        static {
            int[] iArr = new int[CallConstants.STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE = iArr;
            try {
                iArr[CallConstants.STATE.Idle.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.ReadyToCall.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.IncomingCall.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.OutGoingCall.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.AlertingCall.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.InCall.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.HoldingCall.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.HeldCall.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.ResumingCall.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.ModifyingCall.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.ModifyRequested.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.HoldingVideo.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.VideoHeld.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.ResumingVideo.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.EndingCall.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.EndedCall.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
        }
    }

    public static int convertImsCallStateToDialogState(CallConstants.STATE callstate) {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[callstate.ordinal()]) {
            case 1:
            case 2:
            case 3:
                return 3;
            case 4:
            case 5:
                return 1;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                return 2;
            case 15:
            case 16:
                return 3;
            default:
                return 0;
        }
    }

    public static int convertCsCallStateToDialogState(int callstate) {
        switch (callstate) {
            case -1:
            case 0:
            case 5:
            case 6:
            case 7:
            case 8:
                return 3;
            case 1:
            case 2:
                return 2;
            case 3:
            case 4:
                return 1;
            default:
                return 0;
        }
    }

    public static int convertRecordEventForCmcInfo(int recordEvent) {
        if (recordEvent == 1) {
            return 100;
        }
        if (recordEvent == 701) {
            return 1;
        }
        if (recordEvent == 702) {
            return 2;
        }
        if (recordEvent == 800) {
            return 3;
        }
        if (recordEvent != 801) {
            return 0;
        }
        return 4;
    }

    public static boolean isOngoingCallState(CallConstants.STATE callstate) {
        return isDialingCallState(callstate) || isDuringCallState(callstate);
    }

    public static boolean isDialingCallState(CallConstants.STATE callstate) {
        return callstate == CallConstants.STATE.OutGoingCall || callstate == CallConstants.STATE.AlertingCall || callstate == CallConstants.STATE.IncomingCall;
    }

    public static boolean isDuringCallState(CallConstants.STATE callstate) {
        return callstate == CallConstants.STATE.InCall || callstate == CallConstants.STATE.HoldingCall || callstate == CallConstants.STATE.HeldCall || callstate == CallConstants.STATE.ResumingCall || callstate == CallConstants.STATE.ModifyingCall || callstate == CallConstants.STATE.ModifyRequested || callstate == CallConstants.STATE.HoldingVideo || callstate == CallConstants.STATE.ResumingVideo || callstate == CallConstants.STATE.VideoHeld;
    }

    public static boolean isEndCallState(CallConstants.STATE callstate) {
        return callstate == CallConstants.STATE.EndingCall || callstate == CallConstants.STATE.EndedCall;
    }

    public static boolean isHoldCallState(CallConstants.STATE callstate) {
        return callstate == CallConstants.STATE.HoldingCall || callstate == CallConstants.STATE.HeldCall;
    }

    public static boolean isActiveCallState(CallConstants.STATE callstate) {
        return callstate == CallConstants.STATE.InCall || callstate == CallConstants.STATE.ResumingCall;
    }

    public static boolean isTPhoneMode(Context context) {
        if ("com.skt.prod.dialer".equals(((TelecomManager) context.getSystemService("telecom")).getDefaultDialerPackage())) {
            return true;
        }
        return false;
    }

    public static boolean isTPhoneRelaxMode(Context context, String dialingNumber) {
        if (!isTPhoneMode(context)) {
            return false;
        }
        int result = 0;
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://com.skt.prod.dialer.sktincallscreen.provider" + "/" + "get_relaxation"), (String[]) null, (String) null, new String[]{dialingNumber}, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    result = cursor.getInt(0);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if (result == 1) {
            return true;
        }
        return false;
        throw th;
    }

    public static boolean isSrvccAvailable(int srvccVersion, Mno mno, boolean isEpdgCall, CallConstants.STATE CallState, boolean isConferenceCall) {
        if (isEpdgCall) {
            Log.d(LOG_TAG, "SRVCC during EPDG connected, ignore");
            return false;
        }
        String str = LOG_TAG;
        Log.d(str, "SRVCC ver = " + srvccVersion);
        if (srvccVersion == 0) {
            return false;
        }
        if (mno.isEur() || mno.isMea() || mno.isSea() || mno.isOce() || mno.isSwa()) {
            return true;
        }
        if (srvccVersion == 8 || srvccVersion == 9) {
            if (CallState != CallConstants.STATE.InCall || isConferenceCall) {
                return false;
            }
        } else if (srvccVersion == 10) {
            if (CallState != CallConstants.STATE.OutGoingCall) {
                return true;
            }
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0102  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getPEmergencyInfoOfAtt(android.content.Context r21, java.lang.String r22) {
        /*
            java.lang.String r1 = "0000:0000:0000:0000"
            java.lang.String r2 = "IEEE-802.11;i-wlan-node-id="
            java.lang.String r0 = ""
            com.sec.internal.interfaces.ims.core.IGeolocationController r3 = com.sec.internal.ims.registry.ImsRegistry.getGeolocationController()
            if (r3 == 0) goto L_0x001a
            com.sec.internal.constants.ims.gls.LocationInfo r4 = r3.getGeolocation()
            if (r4 == 0) goto L_0x001a
            com.sec.internal.constants.ims.gls.LocationInfo r4 = r3.getGeolocation()
            java.lang.String r0 = r4.mCountry
            r4 = r0
            goto L_0x001b
        L_0x001a:
            r4 = r0
        L_0x001b:
            boolean r0 = com.sec.internal.helper.SimUtil.isSoftphoneEnabled()
            if (r0 == 0) goto L_0x007c
            android.net.Uri r11 = com.sec.internal.constants.ims.entitilement.SoftphoneContract.SoftphoneAddress.buildGetCurrentAddressUriByImpi(r22)
            android.content.ContentResolver r5 = r21.getContentResolver()
            r7 = 0
            r8 = 0
            r9 = 0
            r10 = 0
            r6 = r11
            android.database.Cursor r5 = r5.query(r6, r7, r8, r9, r10)
            if (r5 == 0) goto L_0x006a
            boolean r0 = r5.moveToFirst()     // Catch:{ all -> 0x005c }
            if (r0 == 0) goto L_0x006a
            java.lang.String r0 = "E911AID"
            int r0 = r5.getColumnIndex(r0)     // Catch:{ all -> 0x005c }
            java.lang.String r0 = r5.getString(r0)     // Catch:{ all -> 0x005c }
            r1 = r0
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x005c }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x005c }
            r6.<init>()     // Catch:{ all -> 0x005c }
            java.lang.String r7 = "current address e911aid:"
            r6.append(r7)     // Catch:{ all -> 0x005c }
            r6.append(r1)     // Catch:{ all -> 0x005c }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x005c }
            android.util.Log.d(r0, r6)     // Catch:{ all -> 0x005c }
            goto L_0x006a
        L_0x005c:
            r0 = move-exception
            r6 = r0
            if (r5 == 0) goto L_0x0069
            r5.close()     // Catch:{ all -> 0x0064 }
            goto L_0x0069
        L_0x0064:
            r0 = move-exception
            r7 = r0
            r6.addSuppressed(r7)
        L_0x0069:
            throw r6
        L_0x006a:
            if (r5 == 0) goto L_0x006f
            r5.close()
        L_0x006f:
            if (r1 == 0) goto L_0x007a
            boolean r0 = android.text.TextUtils.isEmpty(r4)
            if (r0 == 0) goto L_0x007a
            java.lang.String r0 = "0000000000000000"
            r1 = r0
        L_0x007a:
            goto L_0x0114
        L_0x007c:
            boolean r0 = android.text.TextUtils.isEmpty(r4)
            if (r0 != 0) goto L_0x0114
            java.lang.String r11 = "is_native = ?"
            java.lang.String r0 = "1"
            java.lang.String[] r9 = new java.lang.String[]{r0}
            java.lang.String r12 = "_id"
            java.lang.String r13 = "msisdn"
            java.lang.String r14 = "location_status"
            java.lang.String r15 = "tc_status"
            java.lang.String r16 = "e911_address_id"
            java.lang.String r17 = "e911_aid_expiration"
            java.lang.String r18 = "e911_server_data"
            java.lang.String r19 = "e911_server_url"
            java.lang.String r20 = "type"
            java.lang.String[] r7 = new java.lang.String[]{r12, r13, r14, r15, r16, r17, r18, r19, r20}
            android.content.ContentResolver r5 = r21.getContentResolver()
            android.net.Uri r6 = com.sec.internal.constants.ims.ImsConstants.Uris.LINES_CONTENT_URI
            r10 = 0
            r8 = r11
            android.database.Cursor r5 = r5.query(r6, r7, r8, r9, r10)
            if (r5 == 0) goto L_0x00f9
            boolean r0 = r5.moveToFirst()     // Catch:{ all -> 0x0106 }
            if (r0 == 0) goto L_0x00f9
            r0 = 4
            java.lang.String r0 = r5.getString(r0)     // Catch:{ all -> 0x0106 }
            java.lang.String r6 = LOG_TAG     // Catch:{ all -> 0x0106 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0106 }
            r8.<init>()     // Catch:{ all -> 0x0106 }
            java.lang.String r10 = "temp e911Aid = "
            r8.append(r10)     // Catch:{ all -> 0x0106 }
            r8.append(r0)     // Catch:{ all -> 0x0106 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0106 }
            android.util.Log.d(r6, r8)     // Catch:{ all -> 0x0106 }
            boolean r6 = android.text.TextUtils.isEmpty(r0)     // Catch:{ all -> 0x0106 }
            if (r6 != 0) goto L_0x00e1
            java.lang.String r6 = "null"
            boolean r6 = r6.equalsIgnoreCase(r0)     // Catch:{ all -> 0x0106 }
            if (r6 != 0) goto L_0x00e1
            r1 = r0
        L_0x00e1:
            java.lang.String r6 = LOG_TAG     // Catch:{ all -> 0x0106 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0106 }
            r8.<init>()     // Catch:{ all -> 0x0106 }
            java.lang.String r10 = "final e911Aid = "
            r8.append(r10)     // Catch:{ all -> 0x0106 }
            r8.append(r1)     // Catch:{ all -> 0x0106 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0106 }
            android.util.Log.d(r6, r8)     // Catch:{ all -> 0x0106 }
            goto L_0x0100
        L_0x00f9:
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0106 }
            java.lang.String r6 = "invalid cursor"
            android.util.Log.d(r0, r6)     // Catch:{ all -> 0x0106 }
        L_0x0100:
            if (r5 == 0) goto L_0x0114
            r5.close()
            goto L_0x0114
        L_0x0106:
            r0 = move-exception
            r6 = r0
            if (r5 == 0) goto L_0x0113
            r5.close()     // Catch:{ all -> 0x010e }
            goto L_0x0113
        L_0x010e:
            r0 = move-exception
            r8 = r0
            r6.addSuppressed(r8)
        L_0x0113:
            throw r6
        L_0x0114:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r2)
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.ImsCallUtil.getPEmergencyInfoOfAtt(android.content.Context, java.lang.String):java.lang.String");
    }

    public static String getConferenceUri(ImsProfile profile, String operator, String domain, Mno mno) {
        String mcc = "";
        String mnc = "";
        try {
            mcc = operator.substring(0, 3);
            mnc = operator.substring(3);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        String confUri = profile.getConferenceUri();
        Log.d(LOG_TAG, "getConferenceUri, confUri=" + confUri + ", mcc=" + mcc + ", mnc=" + mnc + ", sim=" + domain);
        int confUriType = profile.GetConferenceUriMccmncType();
        if (confUri.endsWith("ims.mncXXX.mccXXX.3gppnetwork.org")) {
            if (confUriType == 0 || confUriType == 1) {
                mnc = profile.getMnc();
                mcc = profile.getMcc();
            }
            if ((confUriType == 0 || confUriType == 2) && mnc.length() == 2) {
                mnc = "0" + mnc;
            }
            return confUri.replace("mncXXX", "mnc" + mnc).replace("mccXXX", "mcc" + mcc);
        } else if (mno != Mno.ATT) {
            return confUri;
        } else {
            if (!TextUtils.isEmpty(domain)) {
                String confUri2 = "sip:n-way_voice@" + domain;
                Log.d(LOG_TAG, "ATT confUri=" + confUri2);
                return confUri2;
            } else if ((!"313".equals(mcc) || !"100".equals(mnc)) && (!"312".equals(mcc) || !"670".equals(mnc))) {
                return confUri;
            } else {
                return "sip:n-way_voice@firstnet.com";
            }
        }
    }

    public static VolteConstants.AudioCodecType getAudioCodec(String codec) {
        if (codec == null) {
            return VolteConstants.AudioCodecType.AUDIO_CODEC_NONE;
        }
        if ("AMR-WB".equals(codec)) {
            return VolteConstants.AudioCodecType.AUDIO_CODEC_AMRWB;
        }
        if ("AMR-NB".equals(codec)) {
            return VolteConstants.AudioCodecType.AUDIO_CODEC_AMRNB;
        }
        if ("EVS-FB".equals(codec)) {
            return VolteConstants.AudioCodecType.AUDIO_CODEC_EVSFB;
        }
        if ("EVS-SWB".equals(codec)) {
            return VolteConstants.AudioCodecType.AUDIO_CODEC_EVSSWB;
        }
        if ("EVS-WB".equals(codec)) {
            return VolteConstants.AudioCodecType.AUDIO_CODEC_EVSWB;
        }
        if ("EVS-NB".equals(codec)) {
            return VolteConstants.AudioCodecType.AUDIO_CODEC_EVSNB;
        }
        if ("EVS".equals(codec)) {
            return VolteConstants.AudioCodecType.AUDIO_CODEC_EVS;
        }
        return VolteConstants.AudioCodecType.AUDIO_CODEC_NONE;
    }

    public static SipError onConvertSipErrorReason(CallStateEvent event) {
        SipError error = event.getErrorCode();
        if (!SipErrorBase.ALTERNATIVE_SERVICE.equals(error) || !DeviceUtil.getGcfMode()) {
            return error;
        }
        String type = event.getAlternativeServiceType();
        String reason = event.getAlternativeServiceReason();
        String serviceUrn = event.getAlternativeServiceUrn();
        String str = LOG_TAG;
        Log.d(str, "type : " + type + ", reason : " + reason + ", serviceUrn : " + serviceUrn);
        if (TextUtils.isEmpty(type) || !"emergency".equals(type)) {
            return error;
        }
        if (TextUtils.isEmpty(serviceUrn)) {
            Log.d(LOG_TAG, "serviceUrn is Empty");
            if (event.getAlternativeService() == CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY_REGISTRATION) {
                SipError error2 = SipErrorBase.ALTERNATIVE_SERVICE_EMERGENCY;
                error2.setReason(ECC_SERVICE_URN_DEFAULT);
                return error2;
            }
            Log.d(LOG_TAG, "action is Empty");
            return error;
        } else if (event.getAlternativeService() == CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY) {
            SipError error3 = SipErrorBase.ALTERNATIVE_SERVICE_EMERGENCY_CSFB;
            error3.setReason(serviceUrn);
            return error3;
        } else {
            SipError error4 = SipErrorBase.ALTERNATIVE_SERVICE_EMERGENCY;
            error4.setReason(serviceUrn);
            return error4;
        }
    }

    public static int getIdForString(String idString) {
        try {
            return Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            return idString.hashCode();
        }
    }
}
