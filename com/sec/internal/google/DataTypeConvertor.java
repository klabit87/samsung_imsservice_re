package com.sec.internal.google;

import android.os.Bundle;
import android.os.SemSystemProperties;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.text.TextUtils;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.ims.volte2.data.VolteConstants;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;

public class DataTypeConvertor {
    public static ImsReasonInfo convertToGoogleImsReason(int errorCode) {
        return new ImsReasonInfo(1000, 1, "");
    }

    public static int convertUrnToEccCat(String urn) {
        if (TextUtils.isEmpty(urn)) {
            return 0;
        }
        if ("urn:service:unspecified".equalsIgnoreCase(urn)) {
            return 254;
        }
        if ("urn:service:sos.mountain".equalsIgnoreCase(urn)) {
            return 16;
        }
        if ("urn:service:sos.marine".equalsIgnoreCase(urn)) {
            return 8;
        }
        if ("urn:service:sos.fire".equalsIgnoreCase(urn)) {
            return 4;
        }
        if ("urn:service:sos.ambulance".equalsIgnoreCase(urn)) {
            return 2;
        }
        if ("urn:service:sos.police".equalsIgnoreCase(urn)) {
            return 1;
        }
        if ("urn:service:sos.traffic".equalsIgnoreCase(urn)) {
            return 20;
        }
        if (ImsCallUtil.ECC_SERVICE_URN_DEFAULT.equalsIgnoreCase(urn)) {
            return 0;
        }
        return 254;
    }

    public static String convertEccCatToURN(String eccCatStr) {
        Mno mno = SimUtil.getSimMno(SimUtil.getDefaultPhoneId());
        if (TextUtils.isEmpty(eccCatStr)) {
            return ImsCallUtil.ECC_SERVICE_URN_DEFAULT;
        }
        int eccCat = Integer.parseInt(eccCatStr);
        if (eccCat == 254) {
            return "urn:service:unspecified";
        }
        if (eccCat == 16) {
            return "urn:service:sos.mountain";
        }
        if (eccCat == 8) {
            return "urn:service:sos.marine";
        }
        if (eccCat == 4) {
            return "urn:service:sos.fire";
        }
        if (eccCat == 2) {
            return "urn:service:sos.ambulance";
        }
        if (eccCat == 1) {
            return "urn:service:sos.police";
        }
        if (eccCat == 20) {
            return "urn:service:sos.traffic";
        }
        if (!mno.isJpn() || eccCat != 6) {
            return ImsCallUtil.ECC_SERVICE_URN_DEFAULT;
        }
        return "urn:service:sos.fire";
    }

    public static String convertEccCatToURNSpecificKor(String eccCatStr) {
        if (TextUtils.isEmpty(eccCatStr)) {
            return ImsCallUtil.ECC_SERVICE_URN_DEFAULT;
        }
        int eccCat = Integer.parseInt(eccCatStr);
        if (eccCat == 254) {
            return "urn:service:unspecified";
        }
        if (eccCat == 8) {
            return "urn:service:sos.marine";
        }
        if (eccCat == 4) {
            return "urn:service:sos.fire";
        }
        if (eccCat == 1) {
            return "urn:service:sos.police";
        }
        if (eccCat == 6 || eccCat == 7) {
            return "urn:service:sos.country-specific.kr.111";
        }
        if (eccCat == 3) {
            return "urn:service:sos.country-specific.kr.113";
        }
        if (eccCat == 18) {
            return "urn:service:sos.country-specific.kr.117";
        }
        if (eccCat == 19) {
            return "urn:service:sos.country-specific.kr.118";
        }
        if (eccCat == 9) {
            return "urn:service:sos.country-specific.kr.125";
        }
        return ImsCallUtil.ECC_SERVICE_URN_DEFAULT;
    }

    public static String convertToClirPrefix(int clirMode) {
        if (clirMode == 1) {
            return "#31#";
        }
        if (clirMode == 2) {
            return "*31#";
        }
        if (clirMode != 3) {
            return null;
        }
        return NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
    }

    public static int convertToSecCallType(int callType) {
        return convertToSecCallType(0, callType, false, false);
    }

    public static int convertToSecCallType(int serviceType, int callType, boolean isTtyMode, boolean isGroupCall) {
        switch (callType) {
            case 2:
                if (serviceType == 2) {
                    return 7;
                }
                if (isTtyMode) {
                    return 9;
                }
                if (isGroupCall) {
                    return 5;
                }
                return 1;
            case 4:
            case 8:
                if (serviceType == 2) {
                    return 8;
                }
                if (isGroupCall) {
                    return 6;
                }
                return 2;
            case 5:
            case 9:
                return 3;
            case 6:
            case 10:
                return 4;
            case 7:
                if (serviceType == 2) {
                    return 7;
                }
                return 1;
            default:
                return 0;
        }
    }

    public static int convertToGoogleCallType(int type) {
        switch (type) {
            case 1:
            case 5:
                return 2;
            case 2:
            case 6:
            case 8:
                return 4;
            case 3:
                return 5;
            case 4:
                return 6;
            default:
                return 2;
        }
    }

    public static MediaProfile convertToSecMediaProfile(ImsStreamMediaProfile profile) {
        MediaProfile convertedProfile = new MediaProfile();
        int videoQuality = -1;
        int videoOrientation = 0;
        int i = profile.mVideoQuality;
        if (i == 0) {
            videoQuality = 0;
        } else if (i == 1) {
            videoQuality = 12;
        } else if (i == 2) {
            videoQuality = 13;
            videoOrientation = 1;
        } else if (i == 4) {
            videoQuality = 13;
        } else if (i == 8) {
            videoQuality = 15;
            videoOrientation = 1;
        } else if (i == 16) {
            videoQuality = 15;
        }
        VolteConstants.AudioCodecType audioCodec = VolteConstants.AudioCodecType.AUDIO_CODEC_NONE;
        int i2 = profile.mAudioQuality;
        if (i2 == 1) {
            audioCodec = VolteConstants.AudioCodecType.AUDIO_CODEC_AMRNB;
        } else if (i2 == 2) {
            audioCodec = VolteConstants.AudioCodecType.AUDIO_CODEC_AMRWB;
        }
        convertedProfile.setVideoQuality(videoQuality);
        convertedProfile.setVideoOrientation(videoOrientation);
        convertedProfile.setAudioCodec(audioCodec);
        convertedProfile.setRttMode(profile.getRttMode());
        return convertedProfile;
    }

    public static ImsStreamMediaProfile convertToGoogleMediaProfile(MediaProfile profile) {
        ImsStreamMediaProfile convertedProfile = new ImsStreamMediaProfile();
        int videoQuality = 0;
        int videoQuality2 = profile.getVideoQuality();
        if (videoQuality2 == 0) {
            videoQuality = 0;
        } else if (videoQuality2 != 15) {
            if (videoQuality2 == 12) {
                videoQuality = 1;
            } else if (videoQuality2 == 13) {
                if (profile.getVideoOrientation() == 1) {
                    videoQuality = 2;
                } else {
                    videoQuality = 4;
                }
            }
        } else if (profile.getVideoOrientation() == 1) {
            videoQuality = 8;
        } else {
            videoQuality = 16;
        }
        int audioQuality = 1;
        switch (AnonymousClass1.$SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType[profile.getAudioCodec().ordinal()]) {
            case 1:
            case 2:
                audioQuality = 1;
                break;
            case 3:
                audioQuality = 2;
                break;
            case 4:
            case 5:
                audioQuality = 17;
                break;
            case 6:
                audioQuality = 18;
                break;
            case 7:
                audioQuality = 19;
                break;
            case 8:
                audioQuality = 20;
                break;
        }
        convertedProfile.mAudioQuality = audioQuality;
        convertedProfile.mVideoQuality = videoQuality;
        convertedProfile.mAudioDirection = 3;
        if (profile.getVideoPause()) {
            convertedProfile.mVideoDirection = 0;
        } else {
            convertedProfile.mVideoDirection = 3;
        }
        convertedProfile.mRttMode = profile.getRttMode();
        return convertedProfile;
    }

    /* renamed from: com.sec.internal.google.DataTypeConvertor$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType;

        static {
            int[] iArr = new int[VolteConstants.AudioCodecType.values().length];
            $SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType = iArr;
            try {
                iArr[VolteConstants.AudioCodecType.AUDIO_CODEC_NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType[VolteConstants.AudioCodecType.AUDIO_CODEC_AMRNB.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType[VolteConstants.AudioCodecType.AUDIO_CODEC_AMRWB.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType[VolteConstants.AudioCodecType.AUDIO_CODEC_EVS.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType[VolteConstants.AudioCodecType.AUDIO_CODEC_EVSNB.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType[VolteConstants.AudioCodecType.AUDIO_CODEC_EVSWB.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType[VolteConstants.AudioCodecType.AUDIO_CODEC_EVSSWB.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$ims$volte2$data$VolteConstants$AudioCodecType[VolteConstants.AudioCodecType.AUDIO_CODEC_EVSFB.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    public static CallProfile convertToSecCallProfile(int phoneId, ImsCallProfile profile, boolean isTtyMode) {
        CallProfile convertedProfile = new CallProfile();
        convertedProfile.setPhoneId(phoneId);
        Bundle oemExtras = profile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
        String eccCatStr = "";
        String emergencyRat = "";
        boolean isGroupCall = false;
        if (oemExtras != null) {
            eccCatStr = oemExtras.getString("EccCat");
            emergencyRat = oemExtras.getString("imsEmergencyRat");
            String letteringText = oemExtras.getString("DisplayText");
            String alertInfo = oemExtras.getString("com.samsung.telephony.extra.ALERT_INFO");
            if (!TextUtils.isEmpty(letteringText)) {
                convertedProfile.setLetteringText(letteringText);
            }
            if (!TextUtils.isEmpty(alertInfo)) {
                convertedProfile.setAlertInfo(alertInfo);
            }
            isGroupCall = oemExtras.getBoolean("com.samsung.telephony.extra.DIAL_CONFERENCE_CALL");
        }
        if (profile.getCallExtraBoolean("e_call", false) || profile.mServiceType == 2) {
            convertedProfile.setCallType(convertToSecCallType(2, profile.mCallType, isTtyMode, isGroupCall));
            convertedProfile.setEmergencyRat(emergencyRat);
            if (TextUtils.equals(emergencyRat, "VoWIFI")) {
                profile.setCallExtra("CallRadioTech", String.valueOf(18));
            } else {
                profile.setCallExtra("CallRadioTech", String.valueOf(14));
            }
            Mno mno = SimUtil.getSimMno(phoneId);
            String salesCode = SemSystemProperties.get(OmcCode.PERSIST_OMC_CODE_PROPERTY, "");
            if (TextUtils.isEmpty(salesCode)) {
                salesCode = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY);
            }
            if (mno.isKor() || (mno == Mno.DEFAULT && TextUtils.equals(salesCode, "KTC"))) {
                convertedProfile.setUrn(convertEccCatToURNSpecificKor(eccCatStr));
            } else {
                convertedProfile.setUrn(convertEccCatToURN(eccCatStr));
            }
        } else if (profile.getCallExtraInt("dialstring", 0) == 2) {
            convertedProfile.setCallType(12);
        } else {
            convertedProfile.setCallType(convertToSecCallType(profile.mServiceType, profile.mCallType, isTtyMode, isGroupCall));
            convertedProfile.setCLI(convertToClirPrefix(profile.getCallExtraInt("oir", 0)));
        }
        if (isGroupCall) {
            convertedProfile.setConferenceCall(2);
        }
        convertedProfile.setMediaProfile(convertToSecMediaProfile(profile.mMediaProfile));
        convertedProfile.setComposerData(processCallComposerInfo(profile));
        return convertedProfile;
    }

    private static Bundle processCallComposerInfo(ImsCallProfile imsprofile) {
        Bundle callExtras;
        Bundle cBundle = new Bundle();
        if (imsprofile != null) {
            Bundle callMainExtras = imsprofile.getCallExtras();
            if (callMainExtras != null) {
                callExtras = callMainExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
            } else {
                callExtras = null;
            }
            if (callExtras != null && !callExtras.isEmpty()) {
                if (callExtras.containsKey(ImsConstants.Intents.EXTRA_CALL_IMPORTANCE)) {
                    cBundle.putBoolean(CallConstants.ComposerData.IMPORTANCE, callExtras.getBoolean(ImsConstants.Intents.EXTRA_CALL_IMPORTANCE));
                }
                if (!TextUtils.isEmpty(callExtras.getString(ImsConstants.Intents.EXTRA_CALL_SUBJECT))) {
                    cBundle.putString("subject", callExtras.getString(ImsConstants.Intents.EXTRA_CALL_SUBJECT));
                }
                if (!TextUtils.isEmpty(callExtras.getString(ImsConstants.Intents.EXTRA_CALL_IMAGE))) {
                    cBundle.putString(CallConstants.ComposerData.IMAGE, callExtras.getString(ImsConstants.Intents.EXTRA_CALL_IMAGE));
                }
                if (!TextUtils.isEmpty(callExtras.getString(ImsConstants.Intents.EXTRA_CALL_LATITUDE)) && !TextUtils.isEmpty(callExtras.getString(ImsConstants.Intents.EXTRA_CALL_LONGITUDE))) {
                    cBundle.putString(CallConstants.ComposerData.LONGITUDE, callExtras.getString(ImsConstants.Intents.EXTRA_CALL_LONGITUDE));
                    cBundle.putString(CallConstants.ComposerData.LATITUDE, callExtras.getString(ImsConstants.Intents.EXTRA_CALL_LATITUDE));
                    if (!TextUtils.isEmpty(callExtras.getString(ImsConstants.Intents.EXTRA_CALL_RADIUS))) {
                        cBundle.putString(CallConstants.ComposerData.RADIUS, callExtras.getString(ImsConstants.Intents.EXTRA_CALL_RADIUS));
                    }
                }
            }
        }
        return cBundle;
    }
}
