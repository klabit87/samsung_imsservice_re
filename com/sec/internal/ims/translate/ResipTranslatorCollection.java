package com.sec.internal.ims.translate;

import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.SupportedFeature;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.translate.MapTranslator;
import com.sec.internal.ims.core.RegistrationEvents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImExtension;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.volte2.CallStateMachine;
import com.sec.internal.ims.settings.RcsPolicySettings;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResipTranslatorCollection {
    private static final String LOG_TAG = "ResipTranslatorCollection";
    private static final MapTranslator<String, SupportedFeature> mAcceptContentTranslator = new MapTranslator<>(new HashMap<String, SupportedFeature>() {
        {
            put(MIMEContentType.CPIM, SupportedFeature.TEXT_PLAIN);
            put(MIMEContentType.COMPOSING, SupportedFeature.ISCOMPOSING_TYPE);
            put(MIMEContentType.IMDN, SupportedFeature.IMDN);
            put("*", SupportedFeature.MULTIMEDIA);
            put(MIMEContentType.GROUP_MGMT, SupportedFeature.GROUP_SESSION_MANAGEMENT);
        }
    });

    public static SupportedFeature translateAcceptContent(String s) {
        Log.d(LOG_TAG, "translateAcceptContent " + s);
        return mAcceptContentTranslator.translate(s);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0031  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.sec.internal.constants.ims.servicemodules.im.result.Result translateResult(com.sec.internal.constants.ims.servicemodules.im.ImError r13, com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError r14, java.lang.Object r15) {
        /*
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r7 = translateResultType(r14)
            r0 = 0
            r1 = 0
            r2 = 0
            r3 = 0
            if (r14 == 0) goto L_0x002d
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r4 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.SIP_PROVISIONAL
            if (r7 == r4) goto L_0x0022
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r4 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.SIP_ERROR
            if (r7 != r4) goto L_0x0013
            goto L_0x0022
        L_0x0013:
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r4 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.MSRP_ERROR
            if (r7 != r4) goto L_0x002d
            int r4 = r14.errorCode()
            com.sec.internal.constants.ims.servicemodules.im.MsrpResponse r1 = com.sec.internal.constants.ims.servicemodules.im.MsrpResponse.fromId(r4)
            r8 = r0
            r9 = r1
            goto L_0x002f
        L_0x0022:
            int r4 = r14.errorCode()
            com.sec.internal.constants.ims.servicemodules.im.SipResponse r0 = com.sec.internal.constants.ims.servicemodules.im.SipResponse.fromId(r4)
            r8 = r0
            r9 = r1
            goto L_0x002f
        L_0x002d:
            r8 = r0
            r9 = r1
        L_0x002f:
            if (r15 == 0) goto L_0x0062
            boolean r0 = r15 instanceof com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr
            if (r0 == 0) goto L_0x0049
            r0 = r15
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr r0 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr) r0
            com.sec.internal.constants.ims.servicemodules.im.result.Result$WarningHeader r1 = new com.sec.internal.constants.ims.servicemodules.im.result.Result$WarningHeader
            int r4 = getWarningCode(r0)
            java.lang.String r5 = r0.text()
            r1.<init>(r4, r5)
            r2 = r1
            r10 = r2
            r11 = r3
            goto L_0x0064
        L_0x0049:
            boolean r0 = r15 instanceof com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr
            if (r0 == 0) goto L_0x0062
            r0 = r15
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr r0 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr) r0
            com.sec.internal.constants.ims.servicemodules.im.result.Result$ReasonHeader r1 = new com.sec.internal.constants.ims.servicemodules.im.result.Result$ReasonHeader
            long r4 = r0.code()
            int r4 = (int) r4
            java.lang.String r5 = r0.text()
            r1.<init>(r4, r5)
            r3 = r1
            r10 = r2
            r11 = r3
            goto L_0x0064
        L_0x0062:
            r10 = r2
            r11 = r3
        L_0x0064:
            com.sec.internal.constants.ims.servicemodules.im.result.Result r12 = new com.sec.internal.constants.ims.servicemodules.im.result.Result
            r0 = r12
            r1 = r13
            r2 = r7
            r3 = r8
            r4 = r9
            r5 = r10
            r6 = r11
            r0.<init>(r1, r2, r3, r4, r5, r6)
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.translate.ResipTranslatorCollection.translateResult(com.sec.internal.constants.ims.servicemodules.im.ImError, com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError, java.lang.Object):com.sec.internal.constants.ims.servicemodules.im.result.Result");
    }

    private static Result.Type translateResultType(ImError error) {
        Result.Type type = Result.Type.UNKNOWN_ERROR;
        if (error == null) {
            return type;
        }
        switch (error.errorType()) {
            case 0:
                return Result.Type.SUCCESS;
            case 1:
                return Result.Type.SIP_ERROR;
            case 2:
                return Result.Type.MSRP_ERROR;
            case 3:
                return Result.Type.ENGINE_ERROR;
            case 4:
                return Result.Type.SESSION_RELEASE;
            case 5:
                return Result.Type.NETWORK_ERROR;
            case 6:
                return Result.Type.SESSION_RSRC_UNAVAILABLE;
            case 8:
                return Result.Type.DEVICE_UNREGISTERED;
            case 9:
                return Result.Type.SIP_PROVISIONAL;
            case 11:
                return Result.Type.DEDICATED_BEARER_ERROR;
            default:
                return Result.Type.UNKNOWN_ERROR;
        }
    }

    public static Result translateImResult(ImError error, Object hdr) {
        return translateResult(translateImError(error, hdr), error, hdr);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v10, resolved type: com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr} */
    /* JADX WARNING: type inference failed for: r2v2 */
    /* JADX WARNING: type inference failed for: r2v20 */
    /* JADX WARNING: type inference failed for: r2v21 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.sec.internal.constants.ims.servicemodules.im.ImError translateImError(com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError r4, java.lang.Object r5) {
        /*
            if (r4 != 0) goto L_0x0005
            com.sec.internal.constants.ims.servicemodules.im.ImError r0 = com.sec.internal.constants.ims.servicemodules.im.ImError.UNKNOWN_ERROR
            return r0
        L_0x0005:
            int r0 = r4.errorType()
            int r1 = r4.errorCode()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "translateImError "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "ResipTranslatorCollection"
            android.util.Log.d(r3, r2)
            r2 = 0
            switch(r0) {
                case 0: goto L_0x005d;
                case 1: goto L_0x0050;
                case 2: goto L_0x004b;
                case 3: goto L_0x0048;
                case 4: goto L_0x003c;
                case 5: goto L_0x0039;
                case 6: goto L_0x0036;
                case 7: goto L_0x0028;
                case 8: goto L_0x0033;
                case 9: goto L_0x002e;
                case 10: goto L_0x0028;
                case 11: goto L_0x002b;
                default: goto L_0x0028;
            }
        L_0x0028:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.UNKNOWN_ERROR
            return r2
        L_0x002b:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.DEDICATED_BEARER_ERROR
            return r2
        L_0x002e:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = translateSIPError(r1, r2)
            return r2
        L_0x0033:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.DEVICE_UNREGISTERED
            return r2
        L_0x0036:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_RSRC_UNAVAILABLE
            return r2
        L_0x0039:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.NETWORK_ERROR
            return r2
        L_0x003c:
            boolean r3 = r5 instanceof com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr
            if (r3 == 0) goto L_0x0043
            r2 = r5
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr r2 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr) r2
        L_0x0043:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = translateImSessionReleaseError(r2)
            return r2
        L_0x0048:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.ENGINE_ERROR
            return r2
        L_0x004b:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = translateMSRPError(r1)
            return r2
        L_0x0050:
            boolean r3 = r5 instanceof com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr
            if (r3 == 0) goto L_0x0058
            r2 = r5
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr r2 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr) r2
        L_0x0058:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = translateSIPError(r1, r2)
            return r2
        L_0x005d:
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.SUCCESS
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.translate.ResipTranslatorCollection.translateImError(com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError, java.lang.Object):com.sec.internal.constants.ims.servicemodules.im.ImError");
    }

    public static com.sec.internal.constants.ims.servicemodules.im.ImError translateSIPError(int errorCode, WarningHdr warningHdr) {
        int warningCode = getWarningCode(warningHdr);
        Log.d(LOG_TAG, "translateSIPError: SIP: " + errorCode + "warningCode: " + warningCode);
        if (errorCode == 180) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.RINGING;
        }
        if (errorCode == 181) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.CALL_IS_BEING_FORWARDED;
        }
        if (errorCode == 420) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.BAD_EXTENSION;
        }
        if (errorCode == 421) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.EXTENSION_REQUIRED;
        }
        if (errorCode == 493) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.UNDECEIPHERABLE;
        }
        if (errorCode == 494) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.SECURITY_AGREEMENT_REQD;
        }
        if (errorCode == 603) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_PARTY_DECLINED;
        }
        if (errorCode == 604) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.NOTEXIST_ANYWHERE;
        }
        switch (errorCode) {
            case 100:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.TRYING;
            case MNO.MOVISTAR_MEXICO:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_PROGRESS;
            case CallStateMachine.ON_DUMMY_DNS_TIMER_EXPIRED:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.USE_PROXY;
            case 380:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.ALTERNATE_SERVICE;
            case 400:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.INVALID_REQUEST;
            case 408:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_TIMED_OUT;
            case 410:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.GONE;
            case 423:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.INTERVAL_TOO_BRIEF;
            case 491:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.REQUEST_PENDING;
            case 513:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.MESSAGE_TOO_LARGE;
            case 600:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.BUSY_EVERYWHERE;
            case 606:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.SERVER_NOT_ACCEPTABLE;
            case 703:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.NO_DNS_RESULTS;
            case 709:
                return com.sec.internal.constants.ims.servicemodules.im.ImError.NO_RESPONSE;
            default:
                switch (errorCode) {
                    case 300:
                        return com.sec.internal.constants.ims.servicemodules.im.ImError.MULTIPLE_CHOICES;
                    case CallStateMachine.ON_TIMER_VZW_EXPIRED:
                        return com.sec.internal.constants.ims.servicemodules.im.ImError.MOVED_PERMANENTLY;
                    case CallStateMachine.ON_REINVITE_TIMER_EXPIRED:
                        return com.sec.internal.constants.ims.servicemodules.im.ImError.MOVED_TEMPORARILY;
                    default:
                        switch (errorCode) {
                            case 403:
                                if (warningCode == 105) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_RETRY_FALLBACK;
                                }
                                if (warningCode == 119) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED;
                                }
                                if (warningCode == 127) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED;
                                }
                                if (warningCode == 129) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_NO_DESTINATIONS;
                                }
                                if (warningCode == 381) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_MAX_GROUP_NUMBER;
                                }
                                if (warningCode == 488) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_CHATBOT_CONVERSATION_NEEDED;
                                }
                                if (warningCode == 122) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_RESTART_GC_CLOSED;
                                }
                                if (warningCode == 123) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK;
                                }
                                if (warningCode == 132) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_VERSION_NOT_SUPPORTED;
                                }
                                if (warningCode == 133) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_SIZE_EXCEEDED;
                                }
                                switch (warningCode) {
                                    case 204:
                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_TOKEN_NOT_FOUND;
                                    case 205:
                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_CHATBOT_DECLINED;
                                    case 206:
                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_SPAM_SENDER;
                                    default:
                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_NO_WARNING_HEADER;
                                }
                            case 404:
                                if (warningCode != 123) {
                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_USER_INVALID;
                                }
                                return com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_DOESNT_EXIST;
                            case AECNamespace.HttpResponseCode.METHOD_NOT_ALLOWED:
                                return com.sec.internal.constants.ims.servicemodules.im.ImError.METHOD_NOT_ALLOWED;
                            case RegistrationEvents.EVENT_DISCONNECT_PDN_BY_HD_VOICE_ROAMING_OFF:
                                return com.sec.internal.constants.ims.servicemodules.im.ImError.NOT_ACCEPTABLE;
                            default:
                                switch (errorCode) {
                                    case 413:
                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.REQUEST_ENTITY_TOO_LARGE;
                                    case 414:
                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.REQUEST_URI_TOO_LARGE;
                                    case AECNamespace.HttpResponseCode.UNSUPPORTED_MEDIA_TYPE:
                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.UNSUPPORTED_MEDIA_TYPE;
                                    case 416:
                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.UNSUPPORTED_URI_SCHEME;
                                    default:
                                        switch (errorCode) {
                                            case NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE:
                                                return com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_TEMPORARILY_UNAVAILABLE;
                                            case 481:
                                                if (warningCode != 123) {
                                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.TRANSACTION_DOESNT_EXIST;
                                                }
                                                return com.sec.internal.constants.ims.servicemodules.im.ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK;
                                            case 482:
                                                return com.sec.internal.constants.ims.servicemodules.im.ImError.LOOP_DETECTED;
                                            case 483:
                                                return com.sec.internal.constants.ims.servicemodules.im.ImError.TOO_MANY_HOPS;
                                            case 484:
                                                return com.sec.internal.constants.ims.servicemodules.im.ImError.ADDRESS_INCOMPLETE;
                                            case 485:
                                                return com.sec.internal.constants.ims.servicemodules.im.ImError.AMBIGUOUS;
                                            case NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE:
                                                if (warningCode != 102) {
                                                    return com.sec.internal.constants.ims.servicemodules.im.ImError.BUSY_HERE;
                                                }
                                                return com.sec.internal.constants.ims.servicemodules.im.ImError.EXCEED_MAXIMUM_RECIPIENTS;
                                            case 487:
                                                return com.sec.internal.constants.ims.servicemodules.im.ImError.CONNECTION_RELEASED;
                                            case 488:
                                                return com.sec.internal.constants.ims.servicemodules.im.ImError.NOT_ACCEPTABLE_HERE;
                                            default:
                                                switch (errorCode) {
                                                    case 500:
                                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.INTERNAL_SERVER_ERROR;
                                                    case 501:
                                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.NOT_IMPLEMENTED;
                                                    case 502:
                                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.BAD_GATEWAY;
                                                    case 503:
                                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.SERVICE_UNAVAILABLE;
                                                    case Id.REQUEST_IM_SENDMSG:
                                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.SERVER_TIMEOUT;
                                                    case Id.REQUEST_IM_SEND_COMPOSING_STATUS:
                                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.SIP_VERSION_NOT_SUPPORTED;
                                                    default:
                                                        return com.sec.internal.constants.ims.servicemodules.im.ImError.SIP_UNKNOWN_ERROR;
                                                }
                                        }
                                }
                        }
                }
        }
    }

    public static com.sec.internal.constants.ims.servicemodules.im.ImError translateMSRPError(int errorCode) {
        Log.d(LOG_TAG, "translateMSRPError: MSRP: " + errorCode);
        if (errorCode == 400) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_REQUEST_UNINTELLIGIBLE;
        }
        if (errorCode == 403) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_ACTION_NOT_ALLOWED;
        }
        if (errorCode == 408) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_TRANSACTION_TIMED_OUT;
        }
        if (errorCode == 413) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE;
        }
        if (errorCode == 415) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_UNKNOWN_CONTENT_TYPE;
        }
        if (errorCode == 423) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_PARAMETERS_OUT_OF_BOUND;
        }
        if (errorCode == 481) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_SESSION_DOES_NOT_EXIST;
        }
        if (errorCode == 501) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_UNKNOWN_METHOD;
        }
        if (errorCode == 503) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.OUTOFSERVICE;
        }
        if (errorCode != 506) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_UNKNOWN_ERROR;
        }
        return com.sec.internal.constants.ims.servicemodules.im.ImError.MSRP_SESSION_ON_OTHER_CONNECTION;
    }

    public static com.sec.internal.constants.ims.servicemodules.im.ImError translateImSessionReleaseError(ReasonHdr reasonHdr) {
        int causeCode = -1;
        String causeText = "";
        if (reasonHdr != null) {
            causeCode = (int) reasonHdr.code();
            causeText = reasonHdr.text();
        }
        Log.d(LOG_TAG, "translateImSessionReleaseError: cause: " + causeCode + ", causeText=" + causeText);
        if (causeCode == 200) {
            if (RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()).boolSetting(RcsPolicySettings.RcsPolicy.CHECK_BYECAUSE)) {
                if ("Booted".equals(causeText)) {
                    return com.sec.internal.constants.ims.servicemodules.im.ImError.CONFERENCE_PARTY_BOOTED;
                }
                if ("Call Completed".equals(causeText)) {
                    return com.sec.internal.constants.ims.servicemodules.im.ImError.CONFERENCE_CALL_COMPLETED;
                }
            }
            return com.sec.internal.constants.ims.servicemodules.im.ImError.NORMAL_RELEASE;
        } else if (causeCode == 408) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_TIMED_OUT;
        } else {
            if (causeCode == 410) {
                return com.sec.internal.constants.ims.servicemodules.im.ImError.NORMAL_RELEASE_GONE;
            }
            if (causeCode != 480) {
                return com.sec.internal.constants.ims.servicemodules.im.ImError.NORMAL_RELEASE;
            }
            return com.sec.internal.constants.ims.servicemodules.im.ImError.NORMAL_RELEASE_BEARER_UNAVAILABLE;
        }
    }

    public static Result translateFtResult(ImError error, Object hdr) {
        return translateResult(translateFtError(error, hdr), error, hdr);
    }

    public static com.sec.internal.constants.ims.servicemodules.im.ImError translateFtError(ImError error, Object hdr) {
        if (error == null) {
            return com.sec.internal.constants.ims.servicemodules.im.ImError.UNKNOWN_ERROR;
        }
        int errorType = error.errorType();
        Log.d(LOG_TAG, "translateFtError " + errorType);
        if (errorType != 4) {
            return translateImError(error, hdr);
        }
        return translateFtSessionReleaseError(hdr instanceof ReasonHdr ? (ReasonHdr) hdr : null);
    }

    public static com.sec.internal.constants.ims.servicemodules.im.ImError translateFtSessionReleaseError(ReasonHdr reasonHdr) {
        int causeCode = -1;
        if (reasonHdr != null) {
            causeCode = (int) reasonHdr.code();
        }
        Log.d(LOG_TAG, "translateFtSessionReleaseError: cause: " + causeCode);
        if (causeCode != 200) {
            if (causeCode == 408) {
                return com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_TIMED_OUT;
            }
            if (causeCode == 480) {
                return com.sec.internal.constants.ims.servicemodules.im.ImError.NETWORK_ERROR;
            }
            if (causeCode == 503) {
                return com.sec.internal.constants.ims.servicemodules.im.ImError.SERVICE_UNAVAILABLE;
            }
            if (causeCode != 603) {
                return com.sec.internal.constants.ims.servicemodules.im.ImError.NORMAL_RELEASE;
            }
        }
        return com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_PARTY_CANCELED;
    }

    public static int getWarningCode(WarningHdr warningHdr) {
        String warningStr;
        if (warningHdr == null) {
            return -1;
        }
        int warningCode = warningHdr.code();
        if (warningCode != 399 || (warningStr = warningHdr.text()) == null) {
            return warningCode;
        }
        try {
            return Integer.parseInt(warningStr.split(" ")[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return warningCode;
        }
    }

    public static FtTransferProgressEvent.State translateFtProgressState(int state) {
        if (state == 0) {
            return FtTransferProgressEvent.State.TRANSFERRING;
        }
        if (state == 1) {
            return FtTransferProgressEvent.State.INTERRUPTED;
        }
        if (state == 2) {
            return FtTransferProgressEvent.State.CANCELED;
        }
        if (state == 3) {
            return FtTransferProgressEvent.State.COMPLETED;
        }
        Log.e(LOG_TAG, "translateFtProgressState(): unsupported state! Use TRANSFERRING instead!");
        return FtTransferProgressEvent.State.TRANSFERRING;
    }

    public static Set<NotificationStatus> translateStackImdnNoti(List<Integer> notifications) {
        Log.d(LOG_TAG, "translateStackImdnNoti(): notifications = " + notifications);
        Set<NotificationStatus> result = new HashSet<>();
        for (Integer noti : notifications) {
            Log.d(LOG_TAG, "translateStackImdnNoti(): " + noti);
            int intValue = noti.intValue();
            if (intValue == 0) {
                result.add(NotificationStatus.DELIVERED);
            } else if (intValue == 1) {
                result.add(NotificationStatus.DISPLAYED);
            } else if (intValue == 2) {
                result.add(NotificationStatus.INTERWORKING_SMS);
            } else if (intValue == 3) {
                result.add(NotificationStatus.INTERWORKING_MMS);
            }
        }
        return result;
    }

    public static int[] translateFwImdnNoti(Set<NotificationStatus> notifications) {
        Log.d(LOG_TAG, "translateFwImdnNoti(): notifications = " + notifications);
        int[] result = new int[notifications.size()];
        int i = 0;
        Arrays.fill(result, -1);
        for (NotificationStatus noti : notifications) {
            int i2 = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[noti.ordinal()];
            if (i2 == 1) {
                result[i] = 0;
                i++;
            } else if (i2 == 2) {
                result[i] = 1;
                i++;
            } else if (i2 == 3) {
                result[i] = 2;
                i++;
            } else if (i2 == 4) {
                result[i] = 3;
                i++;
            }
        }
        return result;
    }

    /* renamed from: com.sec.internal.ims.translate.ResipTranslatorCollection$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus;

        static {
            int[] iArr = new int[NotificationStatus.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus = iArr;
            try {
                iArr[NotificationStatus.DELIVERED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[NotificationStatus.DISPLAYED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[NotificationStatus.INTERWORKING_SMS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[NotificationStatus.INTERWORKING_MMS.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public static String adjustMessageBody(String body, String contentType) {
        if (body == null || contentType == null) {
            Log.e(LOG_TAG, "adjustMessageBody(): invalid data, skip the message!");
            return null;
        }
        String charSet = "UTF-8";
        if (!contentType.isEmpty()) {
            String[] parts = contentType.split("charset=");
            if (parts.length > 1) {
                charSet = parts[1].split(";")[0];
                Log.d(LOG_TAG, "adjustMessageBody(): charset = " + charSet);
            }
        }
        try {
            String adjustedBody = new String(body.getBytes(charSet), charSet);
            IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority());
            boolean replaceSpecialCharacter = true;
            if (mnoStrategy != null) {
                replaceSpecialCharacter = mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.REPLACE_SPECIALCHARACTER);
            }
            if (replaceSpecialCharacter) {
                return adjustedBody.replace(164, 8364);
            }
            return adjustedBody;
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "adjustMessageBody(): unsupported charset!");
            return null;
        }
    }

    public static Map<String, String> translateFwImExtensionHeaders(ImExtension extension) {
        Map<String, String> result = new HashMap<>();
        if (extension.sipExtensions() != null) {
            ExtraHeader extraHeader = extension.sipExtensions();
            for (int i = 0; i < extraHeader.pairLength(); i++) {
                if (extraHeader.pair(i) != null) {
                    Log.d(LOG_TAG, "ImExtension Header: " + extraHeader.pair(i).key() + " Value: " + extraHeader.pair(i).value());
                    result.put(extraHeader.pair(i).key(), extraHeader.pair(i).value());
                }
            }
        }
        return result;
    }

    public static int translateStackImExtensionHeaders(FlatBufferBuilder builder, Map<String, String> headers) {
        int[] pairOffset = new int[headers.size()];
        int i = 0;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            int KeyOffset = builder.createString((CharSequence) header.getKey());
            int ValueOffset = builder.createString((CharSequence) header.getValue());
            Pair.startPair(builder);
            Pair.addKey(builder, KeyOffset);
            Pair.addValue(builder, ValueOffset);
            pairOffset[i] = Pair.endPair(builder);
            i++;
        }
        int pairVectorOffset = ExtraHeader.createPairVector(builder, pairOffset);
        ExtraHeader.startExtraHeader(builder);
        ExtraHeader.addPair(builder, pairVectorOffset);
        int extraHeaderOffset = ExtraHeader.endExtraHeader(builder);
        ImExtension.startImExtension(builder);
        ImExtension.addSipExtensions(builder, extraHeaderOffset);
        return ImExtension.endImExtension(builder);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus translateToImConferenceParticipantStatus(java.lang.String r1) {
        /*
            int r0 = r1.hashCode()
            switch(r0) {
                case -1549847968: goto L_0x005a;
                case -1381388741: goto L_0x0050;
                case -1372333075: goto L_0x0046;
                case -800640653: goto L_0x003c;
                case -682587753: goto L_0x0030;
                case -579210487: goto L_0x0026;
                case 71022711: goto L_0x001c;
                case 126626246: goto L_0x0012;
                case 1615413510: goto L_0x0008;
                default: goto L_0x0007;
            }
        L_0x0007:
            goto L_0x0064
        L_0x0008:
            java.lang.String r0 = "alerting"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 2
            goto L_0x0065
        L_0x0012:
            java.lang.String r0 = "disconnecting"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 5
            goto L_0x0065
        L_0x001c:
            java.lang.String r0 = "muted-via-focus"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 6
            goto L_0x0065
        L_0x0026:
            java.lang.String r0 = "connected"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 0
            goto L_0x0065
        L_0x0030:
            java.lang.String r0 = "pending"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 8
            goto L_0x0065
        L_0x003c:
            java.lang.String r0 = "dialing-out"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 4
            goto L_0x0065
        L_0x0046:
            java.lang.String r0 = "on-hold"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 7
            goto L_0x0065
        L_0x0050:
            java.lang.String r0 = "disconnected"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 1
            goto L_0x0065
        L_0x005a:
            java.lang.String r0 = "dialing-in"
            boolean r0 = r1.equals(r0)
            if (r0 == 0) goto L_0x0007
            r0 = 3
            goto L_0x0065
        L_0x0064:
            r0 = -1
        L_0x0065:
            switch(r0) {
                case 0: goto L_0x0083;
                case 1: goto L_0x0080;
                case 2: goto L_0x007d;
                case 3: goto L_0x007a;
                case 4: goto L_0x0077;
                case 5: goto L_0x0074;
                case 6: goto L_0x0071;
                case 7: goto L_0x006e;
                case 8: goto L_0x006b;
                default: goto L_0x0068;
            }
        L_0x0068:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.INVALID
            return r0
        L_0x006b:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.PENDING
            return r0
        L_0x006e:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.ON_HOLD
            return r0
        L_0x0071:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.MUTED_VIA_FOCUS
            return r0
        L_0x0074:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.DISCONNECTING
            return r0
        L_0x0077:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.DIALING_OUT
            return r0
        L_0x007a:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.DIALING_IN
            return r0
        L_0x007d:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.ALERTING
            return r0
        L_0x0080:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.DISCONNECTED
            return r0
        L_0x0083:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.CONNECTED
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.translate.ResipTranslatorCollection.translateToImConferenceParticipantStatus(java.lang.String):com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus");
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceDisconnectionReason translateToImConferenceDisconnectionReason(java.lang.String r4) {
        /*
            int r0 = r4.hashCode()
            r1 = 3
            r2 = 2
            r3 = 1
            switch(r0) {
                case -1383378159: goto L_0x0029;
                case -1281977283: goto L_0x001f;
                case 3035641: goto L_0x0015;
                case 930490259: goto L_0x000b;
                default: goto L_0x000a;
            }
        L_0x000a:
            goto L_0x0033
        L_0x000b:
            java.lang.String r0 = "departed"
            boolean r0 = r4.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = r3
            goto L_0x0034
        L_0x0015:
            java.lang.String r0 = "busy"
            boolean r0 = r4.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = r2
            goto L_0x0034
        L_0x001f:
            java.lang.String r0 = "failed"
            boolean r0 = r4.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = r1
            goto L_0x0034
        L_0x0029:
            java.lang.String r0 = "booted"
            boolean r0 = r4.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = 0
            goto L_0x0034
        L_0x0033:
            r0 = -1
        L_0x0034:
            if (r0 == 0) goto L_0x0047
            if (r0 == r3) goto L_0x0044
            if (r0 == r2) goto L_0x0041
            if (r0 == r1) goto L_0x003e
            r0 = 0
            return r0
        L_0x003e:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceDisconnectionReason r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceDisconnectionReason.FAILED
            return r0
        L_0x0041:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceDisconnectionReason r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceDisconnectionReason.BUSY
            return r0
        L_0x0044:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceDisconnectionReason r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceDisconnectionReason.DEPARTED
            return r0
        L_0x0047:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceDisconnectionReason r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceDisconnectionReason.BOOTED
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.translate.ResipTranslatorCollection.translateToImConferenceDisconnectionReason(java.lang.String):com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceDisconnectionReason");
    }

    public static ImConferenceParticipantInfo.ImConferenceUserElemState translateImConferenceUserElemState(String state) {
        if (state == null) {
            return ImConferenceParticipantInfo.ImConferenceUserElemState.FULL;
        }
        char c = 65535;
        int hashCode = state.hashCode();
        if (hashCode != -792934015) {
            if (hashCode == 1550463001 && state.equals("deleted")) {
                c = 0;
            }
        } else if (state.equals("partial")) {
            c = 1;
        }
        if (c == 0) {
            return ImConferenceParticipantInfo.ImConferenceUserElemState.DELETED;
        }
        if (c != 1) {
            return ImConferenceParticipantInfo.ImConferenceUserElemState.FULL;
        }
        return ImConferenceParticipantInfo.ImConferenceUserElemState.PARTIAL;
    }
}
