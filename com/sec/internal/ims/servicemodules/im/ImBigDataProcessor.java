package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.diagnosis.RcsHqmAgent;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ChatbotUriUtil;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImBigDataProcessor {
    private static final String LOG_TAG = ImBigDataProcessor.class.getSimpleName();
    Context mContext;
    ImModule mImModule;

    ImBigDataProcessor(Context context, ImModule imModule) {
        this.mContext = context;
        this.mImModule = imModule;
    }

    public boolean sendRCSMInfoToHQM(MessageBase msg, String orst, String cause, Result.Type resultType, IMnoStrategy.StatusCode statusCode) {
        MessageBase messageBase = msg;
        String str = orst;
        String str2 = cause;
        ImSession session = this.mImModule.getImSession(msg.getChatId());
        if (session == null) {
            return false;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(msg.getOwnIMSI());
        Map<String, String> rcsmKeys = new LinkedHashMap<>();
        boolean isChatBot = !session.isGroupChat() && ChatbotUriUtil.hasChatbotUri(session.getParticipantsUri(), phoneId);
        String msgType = getMessageType(messageBase, isChatBot);
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_ORST, str);
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MDIR, getMessageDirection(msg));
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MGRP, session.isGroupChat() ? "1" : "0");
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MTYP, msgType);
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MCID, msg.getChatId());
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MIID, msg.getImdnId());
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MSIZ, getMessageSize(msg));
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_PTCN, String.valueOf(session.getParticipantsSize()));
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MRAT, getRegiRat(phoneId));
        if (messageBase instanceof FtMessage) {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_FTYP, getFileExtention((FtMessage) messageBase));
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_FTRC, String.valueOf(((FtMessage) messageBase).getRetryCount()));
        }
        char c = 65535;
        switch (orst.hashCode()) {
            case 48:
                if (str.equals("0")) {
                    c = 0;
                    break;
                }
                break;
            case 49:
                if (str.equals("1")) {
                    c = 1;
                    break;
                }
                break;
            case 50:
                if (str.equals("2")) {
                    c = 2;
                    break;
                }
                break;
            case 51:
                if (str.equals(DiagnosisConstants.RCSM_ORST_REGI)) {
                    c = 4;
                    break;
                }
                break;
            case 52:
                if (str.equals(DiagnosisConstants.RCSM_ORST_HTTP)) {
                    c = 3;
                    break;
                }
                break;
            case 53:
                if (str.equals(DiagnosisConstants.RCSM_ORST_ITER)) {
                    c = 5;
                    break;
                }
                break;
        }
        if (c != 0) {
            if (c == 1) {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_SIPR, str2);
            } else if (c == 2) {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MSRP, str2);
            } else if (c != 3) {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_ITER, str2);
            } else {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_HTTP, str2);
            }
        }
        if (statusCode != null) {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_SRSC, statusCode.toString());
        }
        if (msg.getReferenceType() != null) {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MRTY, msg.getReferenceType());
        }
        if (msg.getReferenceValue() != null) {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MRVA, msg.getReferenceValue());
        }
        String str3 = msgType;
        boolean z = isChatBot;
        storeDRCSInfoToImsLogAgent(phoneId, msg.getDirection(), isChatBot, orst, msgType, resultType, statusCode, ImConstants.ChatbotTrafficType.NONE);
        return RcsHqmAgent.sendRCSInfoToHQM(this.mContext, DiagnosisConstants.FEATURE_RCSM, phoneId, rcsmKeys);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void storeDRCSInfoToImsLogAgent(int r19, com.sec.internal.constants.ims.servicemodules.im.ImDirection r20, boolean r21, java.lang.String r22, java.lang.String r23, com.sec.internal.constants.ims.servicemodules.im.result.Result.Type r24, com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode r25, com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotTrafficType r26) {
        /*
            r18 = this;
            r0 = r18
            r1 = r20
            r2 = r21
            r3 = r22
            r4 = r24
            android.content.ContentValues r5 = new android.content.ContentValues
            r5.<init>()
            r6 = 1
            java.lang.Integer r7 = java.lang.Integer.valueOf(r6)
            java.lang.String r8 = "send_mode"
            r5.put(r8, r7)
            java.lang.String r8 = "overwrite_mode"
            r5.put(r8, r7)
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r8 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING
            if (r1 != r8) goto L_0x013b
            r8 = r23
            java.lang.String r9 = r0.getMessageTypeForILA(r8, r2)
            java.lang.String r11 = "5"
            java.lang.String r12 = "4"
            java.lang.String r13 = "3"
            java.lang.String r14 = "2"
            java.lang.String r15 = "1"
            java.lang.String r10 = "0"
            r16 = -1
            r6 = 2
            if (r2 != 0) goto L_0x00b5
            int r17 = r22.hashCode()
            switch(r17) {
                case 48: goto L_0x006b;
                case 49: goto L_0x0063;
                case 50: goto L_0x005b;
                case 51: goto L_0x0053;
                case 52: goto L_0x004b;
                case 53: goto L_0x0043;
                default: goto L_0x0042;
            }
        L_0x0042:
            goto L_0x0073
        L_0x0043:
            boolean r10 = r3.equals(r11)
            if (r10 == 0) goto L_0x0042
            r10 = 5
            goto L_0x0075
        L_0x004b:
            boolean r10 = r3.equals(r12)
            if (r10 == 0) goto L_0x0042
            r10 = 3
            goto L_0x0075
        L_0x0053:
            boolean r10 = r3.equals(r13)
            if (r10 == 0) goto L_0x0042
            r10 = 4
            goto L_0x0075
        L_0x005b:
            boolean r10 = r3.equals(r14)
            if (r10 == 0) goto L_0x0042
            r10 = r6
            goto L_0x0075
        L_0x0063:
            boolean r10 = r3.equals(r15)
            if (r10 == 0) goto L_0x0042
            r10 = 1
            goto L_0x0075
        L_0x006b:
            boolean r10 = r3.equals(r10)
            if (r10 == 0) goto L_0x0042
            r10 = 0
            goto L_0x0075
        L_0x0073:
            r10 = r16
        L_0x0075:
            if (r10 == 0) goto L_0x00a5
            java.lang.String r11 = "RCOF"
            r12 = 1
            if (r10 == r12) goto L_0x009c
            if (r10 == r6) goto L_0x009c
            r6 = 3
            if (r10 == r6) goto L_0x009c
            r6 = 4
            if (r10 == r6) goto L_0x0093
            r6 = 5
            if (r10 == r6) goto L_0x0088
            goto L_0x00b3
        L_0x0088:
            r5.put(r11, r7)
            java.lang.String r6 = r0.getFailTypeForILA(r4, r2)
            r5.put(r6, r7)
            goto L_0x00b3
        L_0x0093:
            r5.put(r11, r7)
            java.lang.String r6 = "ROFT"
            r5.put(r6, r7)
            goto L_0x00b3
        L_0x009c:
            r5.put(r11, r7)
            java.lang.String r6 = "ROFN"
            r5.put(r6, r7)
            goto L_0x00b3
        L_0x00a5:
            java.lang.String r6 = "RCOS"
            r5.put(r6, r7)
            boolean r6 = r9.isEmpty()
            if (r6 != 0) goto L_0x00b3
            r5.put(r9, r7)
        L_0x00b3:
            goto L_0x012d
        L_0x00b5:
            int r17 = r22.hashCode()
            switch(r17) {
                case 48: goto L_0x00e5;
                case 49: goto L_0x00dd;
                case 50: goto L_0x00d5;
                case 51: goto L_0x00cd;
                case 52: goto L_0x00c5;
                case 53: goto L_0x00bd;
                default: goto L_0x00bc;
            }
        L_0x00bc:
            goto L_0x00ed
        L_0x00bd:
            boolean r10 = r3.equals(r11)
            if (r10 == 0) goto L_0x00bc
            r10 = 5
            goto L_0x00ef
        L_0x00c5:
            boolean r10 = r3.equals(r12)
            if (r10 == 0) goto L_0x00bc
            r10 = 3
            goto L_0x00ef
        L_0x00cd:
            boolean r10 = r3.equals(r13)
            if (r10 == 0) goto L_0x00bc
            r10 = 4
            goto L_0x00ef
        L_0x00d5:
            boolean r10 = r3.equals(r14)
            if (r10 == 0) goto L_0x00bc
            r10 = r6
            goto L_0x00ef
        L_0x00dd:
            boolean r10 = r3.equals(r15)
            if (r10 == 0) goto L_0x00bc
            r10 = 1
            goto L_0x00ef
        L_0x00e5:
            boolean r10 = r3.equals(r10)
            if (r10 == 0) goto L_0x00bc
            r10 = 0
            goto L_0x00ef
        L_0x00ed:
            r10 = r16
        L_0x00ef:
            if (r10 == 0) goto L_0x011f
            java.lang.String r11 = "MPOF"
            r12 = 1
            if (r10 == r12) goto L_0x0116
            if (r10 == r6) goto L_0x0116
            r6 = 3
            if (r10 == r6) goto L_0x0116
            r6 = 4
            if (r10 == r6) goto L_0x010d
            r6 = 5
            if (r10 == r6) goto L_0x0102
            goto L_0x012d
        L_0x0102:
            r5.put(r11, r7)
            java.lang.String r6 = r0.getFailTypeForILA(r4, r2)
            r5.put(r6, r7)
            goto L_0x012d
        L_0x010d:
            r5.put(r11, r7)
            java.lang.String r6 = "MOFT"
            r5.put(r6, r7)
            goto L_0x012d
        L_0x0116:
            r5.put(r11, r7)
            java.lang.String r6 = "MOFN"
            r5.put(r6, r7)
            goto L_0x012d
        L_0x011f:
            java.lang.String r6 = "MPOS"
            r5.put(r6, r7)
            boolean r6 = r9.isEmpty()
            if (r6 != 0) goto L_0x012d
            r5.put(r9, r7)
        L_0x012d:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r6 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY
            r10 = r25
            if (r10 != r6) goto L_0x0138
            java.lang.String r6 = "SMFB"
            r5.put(r6, r7)
        L_0x0138:
            r6 = r26
            goto L_0x0164
        L_0x013b:
            r8 = r23
            r10 = r25
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r6 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            if (r1 != r6) goto L_0x0162
            if (r2 != 0) goto L_0x014d
            java.lang.String r6 = "RCMT"
            r5.put(r6, r7)
            r6 = r26
            goto L_0x0164
        L_0x014d:
            r6 = r26
            java.lang.String r9 = r0.getChatBotTrafficType(r6)
            boolean r11 = r9.isEmpty()
            if (r11 != 0) goto L_0x015c
            r5.put(r9, r7)
        L_0x015c:
            java.lang.String r11 = "MPMT"
            r5.put(r11, r7)
            goto L_0x0164
        L_0x0162:
            r6 = r26
        L_0x0164:
            android.content.Context r7 = r0.mContext
            java.lang.String r9 = "DRCS"
            r11 = r19
            com.sec.internal.ims.diagnosis.ImsLogAgentUtil.storeLogToAgent(r11, r7, r9, r5)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImBigDataProcessor.storeDRCSInfoToImsLogAgent(int, com.sec.internal.constants.ims.servicemodules.im.ImDirection, boolean, java.lang.String, java.lang.String, com.sec.internal.constants.ims.servicemodules.im.result.Result$Type, com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode, com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotTrafficType):void");
    }

    private String getMessageDirection(MessageBase msg) {
        if (msg.getDirection() == ImDirection.OUTGOING) {
            return "0";
        }
        return "1";
    }

    private String getMessageType(MessageBase msg, boolean isChatBot) {
        String msgType;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$Type[msg.getType().ordinal()];
        if (i == 1) {
            msgType = "FT";
        } else if (i == 2) {
            msgType = DiagnosisConstants.RCSM_MTYP_GLS;
        } else if (msg.getIsSlmSvcMsg()) {
            msgType = DiagnosisConstants.RCSM_MTYP_SLM;
        } else {
            msgType = "IM";
        }
        if (!isChatBot) {
            return msgType;
        }
        return msgType + DiagnosisConstants.RCSM_MTYP_CHATBOT_POSTFIX;
    }

    private String getMessageSize(MessageBase msg) {
        if (msg instanceof FtMessage) {
            return String.valueOf(((FtMessage) msg).getFileSize());
        }
        try {
            return String.valueOf(msg.getBody().getBytes(StandardCharsets.UTF_8).length);
        } catch (NullPointerException e) {
            return "0";
        }
    }

    private String getRegiRat(int phoneId) {
        String regiRat = "-1";
        if (this.mImModule.getImsRegistration(phoneId) != null) {
            regiRat = String.valueOf(this.mImModule.getImsRegistration(phoneId).getCurrentRat());
        }
        if (!this.mImModule.isWifiConnected()) {
            return regiRat;
        }
        return regiRat + DiagnosisConstants.RCSM_MRAT_WIFI_POSTFIX;
    }

    private String getFileExtention(FtMessage ftMsg) {
        int i;
        String fileName = ftMsg.getFileName();
        if (fileName == null || (i = fileName.lastIndexOf(46)) <= -1) {
            return "";
        }
        return fileName.substring(i + 1);
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0066  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getMessageTypeForILA(java.lang.String r6, boolean r7) {
        /*
            r5 = this;
            int r0 = r6.hashCode()
            r1 = 2254(0x8ce, float:3.159E-42)
            r2 = 3
            r3 = 2
            r4 = 1
            if (r0 == r1) goto L_0x0038
            r1 = 2340(0x924, float:3.279E-42)
            if (r0 == r1) goto L_0x002e
            r1 = 70670(0x1140e, float:9.903E-41)
            if (r0 == r1) goto L_0x0024
            r1 = 82196(0x14114, float:1.15181E-40)
            if (r0 == r1) goto L_0x001a
        L_0x0019:
            goto L_0x0042
        L_0x001a:
            java.lang.String r0 = "SLM"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x0019
            r0 = r3
            goto L_0x0043
        L_0x0024:
            java.lang.String r0 = "GLS"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x0019
            r0 = r2
            goto L_0x0043
        L_0x002e:
            java.lang.String r0 = "IM"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x0019
            r0 = 0
            goto L_0x0043
        L_0x0038:
            java.lang.String r0 = "FT"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x0019
            r0 = r4
            goto L_0x0043
        L_0x0042:
            r0 = -1
        L_0x0043:
            if (r0 == 0) goto L_0x0066
            if (r0 == r4) goto L_0x005e
            if (r0 == r3) goto L_0x0056
            if (r0 == r2) goto L_0x004e
            java.lang.String r0 = ""
            return r0
        L_0x004e:
            if (r7 == 0) goto L_0x0053
            java.lang.String r0 = "MGOS"
            goto L_0x0055
        L_0x0053:
            java.lang.String r0 = "RGOS"
        L_0x0055:
            return r0
        L_0x0056:
            if (r7 == 0) goto L_0x005b
            java.lang.String r0 = "MSOS"
            goto L_0x005d
        L_0x005b:
            java.lang.String r0 = "RSOS"
        L_0x005d:
            return r0
        L_0x005e:
            if (r7 == 0) goto L_0x0063
            java.lang.String r0 = "MFOS"
            goto L_0x0065
        L_0x0063:
            java.lang.String r0 = "RFOS"
        L_0x0065:
            return r0
        L_0x0066:
            if (r7 == 0) goto L_0x006b
            java.lang.String r0 = "MIOS"
            goto L_0x006d
        L_0x006b:
            java.lang.String r0 = "RIOS"
        L_0x006d:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImBigDataProcessor.getMessageTypeForILA(java.lang.String, boolean):java.lang.String");
    }

    private String getFailTypeForILA(Result.Type resultType, boolean isChatBot) {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[(resultType != null ? resultType : Result.Type.UNKNOWN_ERROR).ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return isChatBot ? DiagnosisConstants.DRCS_KEY_MAAP_MO_FAIL_NETWORK : DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL_NETWORK;
            default:
                return isChatBot ? DiagnosisConstants.DRCS_KEY_MAAP_MO_FAIL_TERMINAL : DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL_TERMINAL;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImBigDataProcessor$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$Type;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type;

        static {
            int[] iArr = new int[ImConstants.ChatbotTrafficType.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType = iArr;
            try {
                iArr[ImConstants.ChatbotTrafficType.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType[ImConstants.ChatbotTrafficType.ADVERTISEMENT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType[ImConstants.ChatbotTrafficType.PAYMENT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType[ImConstants.ChatbotTrafficType.SUBSCRIPTION.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType[ImConstants.ChatbotTrafficType.PREMIUM.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            int[] iArr2 = new int[Result.Type.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type = iArr2;
            try {
                iArr2[Result.Type.SIP_ERROR.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.MSRP_ERROR.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.SESSION_RELEASE.ordinal()] = 3;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.NETWORK_ERROR.ordinal()] = 4;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.DEDICATED_BEARER_ERROR.ordinal()] = 5;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.REMOTE_PARTY_CANCELED.ordinal()] = 6;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.DEVICE_UNREGISTERED.ordinal()] = 7;
            } catch (NoSuchFieldError e12) {
            }
            int[] iArr3 = new int[ImConstants.Type.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$Type = iArr3;
            try {
                iArr3[ImConstants.Type.MULTIMEDIA.ordinal()] = 1;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$Type[ImConstants.Type.LOCATION.ordinal()] = 2;
            } catch (NoSuchFieldError e14) {
            }
        }
    }

    private String getChatBotTrafficType(ImConstants.ChatbotTrafficType trafficType) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$ChatbotTrafficType[trafficType.ordinal()];
        if (i == 1) {
            return DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_NONE;
        }
        if (i == 2) {
            return DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_ADVERTISEMENT;
        }
        if (i == 3) {
            return DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_PAYMENT;
        }
        if (i == 4) {
            return DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_SUBSCRIPTION;
        }
        if (i != 5) {
            return "";
        }
        return DiagnosisConstants.DRCS_KEY_MAAP_TRAFFIC_TYPE_PREMIUM;
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        sendRCSMInfoToHQM(msg, getOrst(result.getType()), getCause(result), result.getType(), strategyResponse != null ? strategyResponse.getStatusCode() : null);
    }

    private String getOrst(Result.Type type) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[type.ordinal()];
        if (i == 1) {
            return "1";
        }
        if (i == 2) {
            return "2";
        }
        if (i != 7) {
            return DiagnosisConstants.RCSM_ORST_ITER;
        }
        return DiagnosisConstants.RCSM_ORST_REGI;
    }

    private String getCause(Result result) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[result.getType().ordinal()];
        if (i != 1) {
            if (i != 2) {
                return result.getType().toString();
            }
            if (result.getMsrpResponse() != null) {
                return String.valueOf(result.getMsrpResponse().getId());
            }
            return null;
        } else if (result.getSipResponse() != null) {
            return String.valueOf(result.getSipResponse().getId());
        } else {
            return null;
        }
    }
}
