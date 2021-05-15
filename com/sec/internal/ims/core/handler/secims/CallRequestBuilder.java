package com.sec.internal.ims.core.handler.secims;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.Dialog;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReqMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestAcceptCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestAcceptTransferCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestCancelTransferCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestClearAllCallInternal;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestEndCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestEndCall_.EndReason;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestHandleCmcCsfb;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestHandleDtmf;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestHoldCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestHoldVideo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestMakeCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestMakeConfCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestModifyCallType;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestModifyVideoQuality;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestProgressIncomingCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestPublishDialog;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestPullingCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRejectModifyCallType;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestReplyModifyCallType;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestResumeCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestResumeVideo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendCmcInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendText;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartCamera;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartCmcRecord;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartLocalRingBackTone;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartRecord;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartVideoEarlymedia;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStopCamera;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStopLocalRingBackTone;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStopRecord;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestTransferCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateAudioInterface;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCall;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCmcExtCallCount;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateConfCall;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CallRequestBuilder {
    private static final String LOG_TAG = CallRequestBuilder.class.getSimpleName();

    static StackRequest makeUpdateAudioInterface(int handle, String mode) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int modeOffset = builder.createString((CharSequence) mode);
        RequestUpdateAudioInterface.startRequestUpdateAudioInterface(builder);
        RequestUpdateAudioInterface.addMode(builder, modeOffset);
        RequestUpdateAudioInterface.addHandle(builder, (long) handle);
        int reqOffset = RequestUpdateAudioInterface.endRequestUpdateAudioInterface(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 225);
        Request.addReqType(builder, (byte) 70);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeStartLocalRingBackTone(int handle, int streamType, int volume, int toneType) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestStartLocalRingBackTone.startRequestStartLocalRingBackTone(builder);
        RequestStartLocalRingBackTone.addStreamType(builder, (long) streamType);
        RequestStartLocalRingBackTone.addHandle(builder, (long) handle);
        RequestStartLocalRingBackTone.addVolume(builder, (long) volume);
        RequestStartLocalRingBackTone.addToneType(builder, (long) toneType);
        int reqOffset = RequestStartLocalRingBackTone.endRequestStartLocalRingBackTone(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 231);
        Request.addReqType(builder, ReqMsg.request_start_local_ring_back_tone);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeStopLocalRingBackTone(int handle) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestStopLocalRingBackTone.startRequestStopLocalRingBackTone(builder);
        RequestStopLocalRingBackTone.addHandle(builder, (long) handle);
        int reqOffset = RequestStopLocalRingBackTone.endRequestStopLocalRingBackTone(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 232);
        Request.addReqType(builder, ReqMsg.request_stop_local_ring_back_tone);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeStartRecord(int handle, int sessionId, String filePath) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int filePathOffset = -1;
        if (!TextUtils.isEmpty(filePath)) {
            filePathOffset = builder.createString((CharSequence) filePath);
        }
        RequestStartRecord.startRequestStartRecord(builder);
        RequestStartRecord.addHandle(builder, (long) handle);
        RequestStartRecord.addSession(builder, (long) sessionId);
        RequestStartRecord.addFilepath(builder, filePathOffset);
        int reqOffset = RequestStartRecord.endRequestStartRecord(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_START_RECORD);
        Request.addReqType(builder, (byte) 111);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeStopRecord(int handle, int sessionId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestStopRecord.startRequestStopRecord(builder);
        RequestStopRecord.addHandle(builder, (long) handle);
        RequestStopRecord.addSession(builder, (long) sessionId);
        int reqOffset = RequestStopRecord.endRequestStopRecord(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_STOP_RECORD);
        Request.addReqType(builder, (byte) 112);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeClearAllCallInternal(int cmcType) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestClearAllCallInternal.startRequestClearAllCallInternal(builder);
        RequestClearAllCallInternal.addCmcType(builder, (long) cmcType);
        int reqOffset = RequestClearAllCallInternal.endRequestClearAllCallInternal(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_CLEAR_ALL_CALL_INTERNAL);
        Request.addReqType(builder, ReqMsg.request_clear_all_call_internal);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeStartCmcRecord(int handle, int sessionId, int audioSource, int outputFormat, long maxFileSize, int maxDuration, String outputPath, int audioEncodingBR, int audioChannels, int audioSamplingR, int audioEncoder, int durationInterval, long fileSizeInterval, String author) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int outputPathOffset = -1;
        if (!TextUtils.isEmpty(outputPath)) {
            outputPathOffset = builder.createString((CharSequence) outputPath);
        } else {
            String str = outputPath;
        }
        int authorOffset = -1;
        if (!TextUtils.isEmpty(author)) {
            authorOffset = builder.createString((CharSequence) author);
        } else {
            String str2 = author;
        }
        RequestStartCmcRecord.startRequestStartCmcRecord(builder);
        RequestStartCmcRecord.addHandle(builder, (long) handle);
        RequestStartCmcRecord.addSession(builder, (long) sessionId);
        RequestStartCmcRecord.addAudioSource(builder, (long) audioSource);
        RequestStartCmcRecord.addOutputFormat(builder, (long) outputFormat);
        RequestStartCmcRecord.addMaxFileSize(builder, maxFileSize);
        RequestStartCmcRecord.addMaxDuration(builder, (long) maxDuration);
        RequestStartCmcRecord.addOutputPath(builder, outputPathOffset);
        RequestStartCmcRecord.addAudioEncodingBr(builder, (long) audioEncodingBR);
        RequestStartCmcRecord.addAudioChannels(builder, (long) audioChannels);
        int i = outputPathOffset;
        RequestStartCmcRecord.addAudioSamplingRate(builder, (long) audioSamplingR);
        RequestStartCmcRecord.addAudioEncoder(builder, (long) audioEncoder);
        RequestStartCmcRecord.addDurationInterval(builder, (long) durationInterval);
        RequestStartCmcRecord.addFileSizeInterval(builder, fileSizeInterval);
        RequestStartCmcRecord.addAuthor(builder, authorOffset);
        int reqOffset = RequestStartCmcRecord.endRequestStartCmcRecord(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_START_CMC_RECORD);
        Request.addReqType(builder, ReqMsg.request_start_cmc_record);
        Request.addReq(builder, reqOffset);
        int i2 = reqOffset;
        return new StackRequest(builder, Request.endRequest(builder));
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int prepareComposerData(android.os.Bundle r16, com.google.flatbuffers.FlatBufferBuilder r17) {
        /*
            r0 = r16
            r1 = r17
            r2 = -1
            if (r0 == 0) goto L_0x00c1
            boolean r3 = r16.isEmpty()
            if (r3 != 0) goto L_0x00c1
            r3 = -1
            r4 = -1
            r5 = -1
            r6 = -1
            r7 = -1
            java.util.Set r8 = r16.keySet()
            java.util.Iterator r8 = r8.iterator()
        L_0x001a:
            boolean r9 = r8.hasNext()
            java.lang.String r10 = "importance"
            if (r9 == 0) goto L_0x0092
            java.lang.Object r9 = r8.next()
            java.lang.String r9 = (java.lang.String) r9
            boolean r10 = r9.equals(r10)
            if (r10 != 0) goto L_0x0091
            java.lang.String r10 = r0.getString(r9)
            boolean r12 = android.text.TextUtils.isEmpty(r10)
            if (r12 != 0) goto L_0x0091
            int r12 = r1.createString((java.lang.CharSequence) r10)
            int r13 = r9.hashCode()
            r15 = 3
            r11 = 2
            r14 = 1
            switch(r13) {
                case -1867885268: goto L_0x0070;
                case -1439978388: goto L_0x0066;
                case -938578798: goto L_0x005b;
                case 100313435: goto L_0x0051;
                case 137365935: goto L_0x0047;
                default: goto L_0x0046;
            }
        L_0x0046:
            goto L_0x007b
        L_0x0047:
            java.lang.String r13 = "longitude"
            boolean r13 = r9.equals(r13)
            if (r13 == 0) goto L_0x0046
            r13 = 4
            goto L_0x007c
        L_0x0051:
            java.lang.String r13 = "image"
            boolean r13 = r9.equals(r13)
            if (r13 == 0) goto L_0x0046
            r13 = 0
            goto L_0x007c
        L_0x005b:
            java.lang.String r13 = "radius"
            boolean r13 = r9.equals(r13)
            if (r13 == 0) goto L_0x0046
            r13 = r11
            goto L_0x007c
        L_0x0066:
            java.lang.String r13 = "latitude"
            boolean r13 = r9.equals(r13)
            if (r13 == 0) goto L_0x0046
            r13 = r15
            goto L_0x007c
        L_0x0070:
            java.lang.String r13 = "subject"
            boolean r13 = r9.equals(r13)
            if (r13 == 0) goto L_0x0046
            r13 = r14
            goto L_0x007c
        L_0x007b:
            r13 = -1
        L_0x007c:
            if (r13 == 0) goto L_0x0090
            if (r13 == r14) goto L_0x008e
            if (r13 == r11) goto L_0x008c
            if (r13 == r15) goto L_0x008a
            r11 = 4
            if (r13 == r11) goto L_0x0088
            goto L_0x0091
        L_0x0088:
            r6 = r12
            goto L_0x0091
        L_0x008a:
            r5 = r12
            goto L_0x0091
        L_0x008c:
            r7 = r12
            goto L_0x0091
        L_0x008e:
            r3 = r12
            goto L_0x0091
        L_0x0090:
            r4 = r12
        L_0x0091:
            goto L_0x001a
        L_0x0092:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData.startComposerData(r17)
            r8 = -1
            if (r4 == r8) goto L_0x009b
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData.addImage(r1, r4)
        L_0x009b:
            if (r3 == r8) goto L_0x00a0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData.addSubject(r1, r3)
        L_0x00a0:
            if (r5 == r8) goto L_0x00a5
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData.addLatitude(r1, r5)
        L_0x00a5:
            if (r6 == r8) goto L_0x00aa
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData.addLongitude(r1, r6)
        L_0x00aa:
            if (r7 == r8) goto L_0x00af
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData.addRadius(r1, r7)
        L_0x00af:
            boolean r8 = r0.containsKey(r10)
            if (r8 == 0) goto L_0x00bd
            boolean r8 = r0.getBoolean(r10)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData.addImportance(r1, r8)
        L_0x00bd:
            int r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData.endComposerData(r17)
        L_0x00c1:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.CallRequestBuilder.prepareComposerData(android.os.Bundle, com.google.flatbuffers.FlatBufferBuilder):int");
    }

    static StackRequest makeMakeCall(int handle, String destUri, String origUri, int type, String dispName, String dialedNumber, String ecscf, int port, AdditionalContents ac, String cli, String pEmergencyInfoOfAtt, HashMap<String, String> additionalSipHeaders, String alertInfo, boolean isLteEpsOnlyAttached, List<String> p2p, int cmcBoundSessionId, Bundle composerData, String replaceCallId) {
        int ecscfOffset;
        int cliOffset;
        int dispNameOffset;
        int dialedNumberOffset;
        int pEmergencyInfoOfAttOffset;
        int composerOffset;
        int p2pListOffset;
        int mimeOffset;
        int xmlStringOffset;
        String str = destUri;
        String str2 = origUri;
        String dispName2 = dispName;
        String str3 = dialedNumber;
        String str4 = ecscf;
        String str5 = cli;
        String str6 = pEmergencyInfoOfAtt;
        HashMap<String, String> hashMap = additionalSipHeaders;
        String str7 = alertInfo;
        List<String> list = p2p;
        String str8 = replaceCallId;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int origUriOffset = -1;
        if (str2 != null) {
            origUriOffset = builder.createString((CharSequence) str2);
        }
        int destUriOffset = -1;
        if (str != null) {
            destUriOffset = builder.createString((CharSequence) str);
        }
        int acoffset = -1;
        if (ac != null) {
            if (ac.mimeType() != null) {
                mimeOffset = builder.createString((CharSequence) ac.mimeType());
            } else {
                mimeOffset = -1;
            }
            if (ac.contents() != null) {
                xmlStringOffset = builder.createString((CharSequence) ac.contents());
            } else {
                xmlStringOffset = -1;
            }
            AdditionalContents.startAdditionalContents(builder);
            if (mimeOffset != -1) {
                AdditionalContents.addMimeType(builder, mimeOffset);
            }
            if (xmlStringOffset != -1) {
                AdditionalContents.addContents(builder, xmlStringOffset);
            }
            acoffset = AdditionalContents.endAdditionalContents(builder);
        }
        int composerOffset2 = prepareComposerData(composerData, builder);
        int pairOffset = -1;
        if (hashMap != null) {
            Log.i(LOG_TAG, "additional header present");
            List<Integer> pairList = StackRequestBuilderUtil.translateExtraHeader(builder, hashMap);
            int[] pairOffsetArr = new int[pairList.size()];
            int i = 0;
            for (Integer intValue : pairList) {
                pairOffsetArr[i] = intValue.intValue();
                i++;
            }
            pairOffset = ExtraHeader.createPairVector(builder, pairOffsetArr);
        }
        int siphdrOffset = -1;
        if (hashMap != null) {
            ExtraHeader.startExtraHeader(builder);
            ExtraHeader.addPair(builder, pairOffset);
            siphdrOffset = ExtraHeader.endExtraHeader(builder);
        }
        if (str4 != null) {
            ecscfOffset = RequestMakeCall.createEcscfListVector(builder, new int[]{builder.createString((CharSequence) str4)});
        } else {
            ecscfOffset = -1;
        }
        if (str5 != null) {
            cliOffset = builder.createString((CharSequence) str5);
        } else {
            cliOffset = -1;
        }
        if (dispName2 == null || dispName.length() <= 0) {
            dispNameOffset = -1;
        } else if ("<PhotoRing>".equals(dispName2)) {
            Log.i(LOG_TAG, "PhotoRing is set");
            int dispNameOffset2 = builder.createString((CharSequence) dispName2);
            dispName2 = null;
            dispNameOffset = dispNameOffset2;
        } else {
            dispNameOffset = builder.createString((CharSequence) dispName2);
        }
        int alertInfoOffset = -1;
        if (str7 != null && alertInfo.length() > 0) {
            alertInfoOffset = builder.createString((CharSequence) str7);
        }
        if (str3 == null || dialedNumber.length() <= 0) {
            String str9 = dispName2;
            dialedNumberOffset = -1;
        } else {
            String str10 = dispName2;
            dialedNumberOffset = builder.createString((CharSequence) str3);
        }
        if (str6 == null || pEmergencyInfoOfAtt.length() <= 0) {
            pEmergencyInfoOfAttOffset = -1;
        } else {
            pEmergencyInfoOfAttOffset = builder.createString((CharSequence) str6);
        }
        if (list == null || p2p.size() <= 0) {
            composerOffset = composerOffset2;
            int i2 = pairOffset;
            p2pListOffset = -1;
        } else {
            int[] p2pArr = new int[p2p.size()];
            String str11 = LOG_TAG;
            int i3 = pairOffset;
            StringBuilder sb = new StringBuilder();
            composerOffset = composerOffset2;
            sb.append("p2p.size():");
            sb.append(p2p.size());
            Log.i(str11, sb.toString());
            for (int i4 = 0; i4 < p2pArr.length; i4++) {
                p2pArr[i4] = builder.createString((CharSequence) list.get(i4));
            }
            p2pListOffset = RequestMakeCall.createP2pListVector(builder, p2pArr);
        }
        int replaceCallIdOffset = -1;
        if (str8 != null && replaceCallId.length() > 0) {
            replaceCallIdOffset = builder.createString((CharSequence) str8);
        }
        RequestMakeCall.startRequestMakeCall(builder);
        RequestMakeCall.addHandle(builder, (long) handle);
        if (destUriOffset != -1) {
            RequestMakeCall.addPeeruri(builder, destUriOffset);
        }
        RequestMakeCall.addCallType(builder, type);
        RequestMakeCall.addMode(builder, 1);
        RequestMakeCall.addCodec(builder, 1);
        RequestMakeCall.addDirection(builder, 0);
        RequestMakeCall.addIsLteEpsOnlyAttached(builder, isLteEpsOnlyAttached);
        if (origUriOffset != -1) {
            RequestMakeCall.addOrigUri(builder, origUriOffset);
        }
        if (acoffset != -1) {
            RequestMakeCall.addAdditionalContents(builder, acoffset);
        }
        if (ecscfOffset != -1) {
            RequestMakeCall.addEcscfList(builder, ecscfOffset);
        }
        RequestMakeCall.addEcscfPort(builder, port);
        int i5 = ecscfOffset;
        int cliOffset2 = cliOffset;
        if (cliOffset2 != -1) {
            RequestMakeCall.addCli(builder, cliOffset2);
        }
        if (dispNameOffset != -1) {
            RequestMakeCall.addDispName(builder, dispNameOffset);
        }
        if (alertInfoOffset != -1) {
            RequestMakeCall.addAlertInfo(builder, alertInfoOffset);
        }
        RequestMakeCall.addCmcBoundSessionId(builder, cmcBoundSessionId);
        int i6 = cliOffset2;
        if (p2pListOffset != -1) {
            RequestMakeCall.addP2pList(builder, p2pListOffset);
        }
        if (dialedNumberOffset != -1) {
            RequestMakeCall.addDialedNumber(builder, dialedNumberOffset);
        }
        if (pEmergencyInfoOfAttOffset != -1) {
            RequestMakeCall.addPEmergencyInfoOfAtt(builder, pEmergencyInfoOfAttOffset);
        }
        if (siphdrOffset != -1) {
            RequestMakeCall.addAdditionalSipHeaders(builder, siphdrOffset);
        }
        int i7 = siphdrOffset;
        int composerOffset3 = composerOffset;
        if (composerOffset3 != -1) {
            RequestMakeCall.addComposerData(builder, composerOffset3);
        }
        if (replaceCallIdOffset != -1) {
            RequestMakeCall.addReplaceCallId(builder, replaceCallIdOffset);
        }
        int callReq = RequestMakeCall.endRequestMakeCall(builder);
        Request.startRequest(builder);
        int i8 = composerOffset3;
        Request.addReqid(builder, 201);
        Request.addReqType(builder, (byte) 13);
        Request.addReq(builder, callReq);
        int i9 = p2pListOffset;
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeEndCall(int handle, int sessionId, SipReason reason) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int endReasonOffset = -1;
        if (reason != null) {
            int protoOffset = builder.createString((CharSequence) reason.getProtocol());
            int textOffset = -1;
            if (reason.getText() != null) {
                textOffset = builder.createString((CharSequence) reason.getText());
            }
            int extOffset = -1;
            if (reason.getExtensions() != null) {
                List<String> list = Arrays.asList(reason.getExtensions());
                extOffset = EndReason.createExtensionVector(builder, StackRequestBuilderUtil.getStringOffsetArray(builder, list, list.size()));
            }
            EndReason.startEndReason(builder);
            EndReason.addIsLocalRelease(builder, reason.isLocalRelease());
            EndReason.addProtocol(builder, protoOffset);
            EndReason.addCause(builder, (long) reason.getCause());
            if (textOffset != -1) {
                EndReason.addText(builder, textOffset);
            }
            if (extOffset != -1) {
                EndReason.addExtension(builder, extOffset);
            }
            endReasonOffset = EndReason.endEndReason(builder);
        }
        RequestEndCall.startRequestEndCall(builder);
        RequestEndCall.addSession(builder, (long) sessionId);
        if (endReasonOffset != -1) {
            RequestEndCall.addEndReason(builder, endReasonOffset);
        }
        int reqOffset = RequestEndCall.endRequestEndCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 202);
        Request.addReqType(builder, (byte) 14);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeAnswerCall(int handle, int sessionId, int callType, String cmcCallTime) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int cmcCallTimeOffset = -1;
        if (!TextUtils.isEmpty(cmcCallTime)) {
            cmcCallTimeOffset = builder.createString((CharSequence) cmcCallTime);
        }
        RequestAcceptCall.startRequestAcceptCall(builder);
        RequestAcceptCall.addSession(builder, (long) sessionId);
        RequestAcceptCall.addCallType(builder, callType);
        if (cmcCallTimeOffset != -1) {
            RequestAcceptCall.addCmcCallTime(builder, cmcCallTimeOffset);
        }
        int reqOffset = RequestAcceptCall.endRequestAcceptCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 205);
        Request.addReqType(builder, (byte) 16);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeHoldCall(int handle, int sessionId) {
        String str = LOG_TAG;
        Log.i(str, "holdCall: handle " + handle + " sessionId " + sessionId);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestHoldCall.startRequestHoldCall(builder);
        RequestHoldCall.addHandle(builder, (long) handle);
        RequestHoldCall.addSession(builder, (long) sessionId);
        int reqOffset = RequestHoldCall.endRequestHoldCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 203);
        Request.addReqType(builder, (byte) 17);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeResumeCall(int handle, int sessionId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestResumeCall.startRequestResumeCall(builder);
        RequestHoldCall.addHandle(builder, (long) handle);
        RequestHoldCall.addSession(builder, (long) sessionId);
        int reqOffset = RequestResumeCall.endRequestResumeCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 204);
        Request.addReqType(builder, (byte) 18);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeHoldVideo(int handle, int sessionId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestHoldVideo.startRequestHoldVideo(builder);
        RequestHoldVideo.addHandle(builder, (long) handle);
        RequestHoldVideo.addSession(builder, (long) sessionId);
        int reqOffset = RequestHoldVideo.endRequestHoldVideo(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 219);
        Request.addReqType(builder, (byte) 25);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeResumeVideo(int handle, int sessionId) {
        String str = LOG_TAG;
        Log.i(str, "resumeVideo: handle " + handle + " sessionId " + sessionId);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestResumeVideo.startRequestResumeVideo(builder);
        RequestResumeVideo.addHandle(builder, (long) handle);
        RequestResumeVideo.addSession(builder, (long) sessionId);
        int reqOffset = RequestResumeVideo.endRequestResumeVideo(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 220);
        Request.addReqType(builder, (byte) 26);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeStartCamera(int handle, int sessionId, int cameraId) {
        String str = LOG_TAG;
        Log.i(str, "startCamera: handle " + handle + ", sessionId: " + sessionId + ", cameraId: " + cameraId);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestStartCamera.startRequestStartCamera(builder);
        RequestStartCamera.addHandle(builder, (long) handle);
        RequestStartCamera.addSession(builder, (long) sessionId);
        RequestStartCamera.addCamera(builder, (long) cameraId);
        int reqOffset = RequestStartCamera.endRequestStartCamera(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 223);
        Request.addReqType(builder, (byte) 66);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeStopCamera(int handle) {
        String str = LOG_TAG;
        Log.i(str, "stopCamera: handle " + handle);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestStopCamera.startRequestStopCamera(builder);
        RequestStopCamera.addHandle(builder, (long) handle);
        int reqOffset = RequestStopCamera.endRequestStopCamera(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 226);
        Request.addReqType(builder, (byte) 67);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeMergeCall(int handle, int sessionId1, int sessionId2, String confUri, int callType, String eventSubscribe, String dialogType, String origUri, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd, HashMap<String, String> extraHeaders) {
        int removeReferUriTypeOffset;
        int referUriAssertedOffset;
        int useAnonymousUpdateOffset;
        int pairOffset;
        int extraHeaderOffset;
        String str = confUri;
        String str2 = eventSubscribe;
        String str3 = dialogType;
        String str4 = origUri;
        String str5 = referUriType;
        String str6 = removeReferUriType;
        String str7 = referUriAsserted;
        String str8 = useAnonymousUpdate;
        HashMap<String, String> hashMap = extraHeaders;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int confUriOffset = -1;
        if (str != null) {
            confUriOffset = builder.createString((CharSequence) str);
        }
        int eventSubscribeOffset = -1;
        if (str2 != null) {
            eventSubscribeOffset = builder.createString((CharSequence) str2);
        }
        int dialogTypeOffset = -1;
        if (str3 != null) {
            dialogTypeOffset = builder.createString((CharSequence) str3);
        }
        int origUriOffset = -1;
        if (str4 != null) {
            origUriOffset = builder.createString((CharSequence) str4);
        }
        int referUriTypeOffset = -1;
        if (str5 != null) {
            referUriTypeOffset = builder.createString((CharSequence) str5);
        }
        if (str6 != null) {
            removeReferUriTypeOffset = builder.createString((CharSequence) str6);
        } else {
            removeReferUriTypeOffset = -1;
        }
        if (str7 != null) {
            referUriAssertedOffset = builder.createString((CharSequence) str7);
        } else {
            referUriAssertedOffset = -1;
        }
        if (str8 != null) {
            useAnonymousUpdateOffset = builder.createString((CharSequence) str8);
        } else {
            useAnonymousUpdateOffset = -1;
        }
        int sessionIdOffset = RequestMakeConfCall.createSessionIdVector(builder, new int[]{sessionId2, sessionId1});
        if (hashMap != null) {
            List<Integer> pairList = StackRequestBuilderUtil.translateExtraHeader(builder, hashMap);
            int[] pairOffsetArr = new int[pairList.size()];
            int i = 0;
            for (Integer intValue : pairList) {
                pairOffsetArr[i] = intValue.intValue();
                i++;
            }
            pairOffset = ExtraHeader.createPairVector(builder, pairOffsetArr);
        } else {
            pairOffset = -1;
        }
        if (hashMap != null) {
            ExtraHeader.startExtraHeader(builder);
            ExtraHeader.addPair(builder, pairOffset);
            extraHeaderOffset = ExtraHeader.endExtraHeader(builder);
        } else {
            extraHeaderOffset = -1;
        }
        RequestMakeConfCall.startRequestMakeConfCall(builder);
        if (extraHeaderOffset != -1) {
            RequestMakeConfCall.addExtraHeaders(builder, extraHeaderOffset);
        }
        int i2 = extraHeaderOffset;
        int extraHeaderOffset2 = useAnonymousUpdateOffset;
        if (extraHeaderOffset2 != -1) {
            RequestMakeConfCall.addUseAnonymousUpdate(builder, extraHeaderOffset2);
        }
        int i3 = extraHeaderOffset2;
        int referUriAssertedOffset2 = referUriAssertedOffset;
        if (referUriAssertedOffset2 != -1) {
            RequestMakeConfCall.addReferuriAsserted(builder, referUriAssertedOffset2);
        }
        if (referUriTypeOffset != -1) {
            RequestMakeConfCall.addReferuriType(builder, referUriTypeOffset);
        }
        int i4 = referUriAssertedOffset2;
        int removeReferUriTypeOffset2 = removeReferUriTypeOffset;
        if (removeReferUriTypeOffset2 != -1) {
            RequestMakeConfCall.addRemoveReferuriType(builder, removeReferUriTypeOffset2);
        }
        if (origUriOffset != -1) {
            RequestMakeConfCall.addOrigUri(builder, origUriOffset);
        }
        RequestMakeConfCall.addSessionId(builder, sessionIdOffset);
        if (dialogTypeOffset != -1) {
            RequestMakeConfCall.addDialogType(builder, dialogTypeOffset);
        }
        if (eventSubscribeOffset != -1) {
            RequestMakeConfCall.addEventSubscribe(builder, eventSubscribeOffset);
        }
        RequestMakeConfCall.addConfType(builder, 0);
        RequestMakeConfCall.addCallType(builder, callType);
        int i5 = sessionIdOffset;
        RequestMakeConfCall.addSupportPrematureEnd(builder, supportPrematureEnd);
        if (confUriOffset != -1) {
            RequestMakeConfCall.addConfuri(builder, confUriOffset);
        }
        int i6 = removeReferUriTypeOffset2;
        RequestMakeConfCall.addHandle(builder, (long) handle);
        int makeConfCallOffset = RequestMakeConfCall.endRequestMakeConfCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 209);
        Request.addReqType(builder, (byte) 30);
        Request.addReq(builder, makeConfCallOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeConference(int handle, String confuri, int callType, String eventSubscribe, String dialogType, String[] participants, String origUri, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd) {
        int referUriAssertedOffset;
        int useAnonymousUpdateOffset;
        String str = confuri;
        String str2 = eventSubscribe;
        String str3 = dialogType;
        String[] strArr = participants;
        String str4 = origUri;
        String str5 = referUriType;
        String str6 = removeReferUriType;
        String str7 = referUriAsserted;
        String str8 = useAnonymousUpdate;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int confUriOffset = -1;
        if (str != null) {
            confUriOffset = builder.createString((CharSequence) str);
        }
        int eventSubscribeOffset = -1;
        if (str2 != null) {
            eventSubscribeOffset = builder.createString((CharSequence) str2);
        }
        int dialogTypeOffset = -1;
        if (str3 != null) {
            dialogTypeOffset = builder.createString((CharSequence) str3);
        }
        int origUriOffset = -1;
        if (str4 != null) {
            origUriOffset = builder.createString((CharSequence) str4);
        }
        int referUriTypeOffset = -1;
        if (str5 != null) {
            referUriTypeOffset = builder.createString((CharSequence) str5);
        }
        int removeReferUriTypeOffset = -1;
        if (str6 != null) {
            removeReferUriTypeOffset = builder.createString((CharSequence) str6);
        }
        if (str7 != null) {
            referUriAssertedOffset = builder.createString((CharSequence) str7);
        } else {
            referUriAssertedOffset = -1;
        }
        if (str8 != null) {
            useAnonymousUpdateOffset = builder.createString((CharSequence) str8);
        } else {
            useAnonymousUpdateOffset = -1;
        }
        int lenOfParticipants = strArr.length;
        int[] participantOffsetArray = new int[lenOfParticipants];
        int i = 0;
        while (i < lenOfParticipants) {
            participantOffsetArray[i] = builder.createString((CharSequence) strArr[i]);
            i++;
            lenOfParticipants = lenOfParticipants;
        }
        int participantsOffset = RequestMakeConfCall.createParticipantsVector(builder, participantOffsetArray);
        RequestMakeConfCall.startRequestMakeConfCall(builder);
        if (useAnonymousUpdateOffset != -1) {
            RequestMakeConfCall.addUseAnonymousUpdate(builder, useAnonymousUpdateOffset);
        }
        if (referUriAssertedOffset != -1) {
            RequestMakeConfCall.addReferuriAsserted(builder, referUriAssertedOffset);
        }
        if (referUriTypeOffset != -1) {
            RequestMakeConfCall.addReferuriType(builder, referUriTypeOffset);
        }
        if (removeReferUriTypeOffset != -1) {
            RequestMakeConfCall.addRemoveReferuriType(builder, removeReferUriTypeOffset);
        }
        if (origUriOffset != -1) {
            RequestMakeConfCall.addOrigUri(builder, origUriOffset);
        }
        RequestMakeConfCall.addParticipants(builder, participantsOffset);
        if (dialogTypeOffset != -1) {
            RequestMakeConfCall.addDialogType(builder, dialogTypeOffset);
        }
        if (eventSubscribeOffset != -1) {
            RequestMakeConfCall.addEventSubscribe(builder, eventSubscribeOffset);
        }
        RequestMakeConfCall.addConfType(builder, 1);
        RequestMakeConfCall.addCallType(builder, callType);
        int i2 = referUriAssertedOffset;
        RequestMakeConfCall.addSupportPrematureEnd(builder, supportPrematureEnd);
        if (confUriOffset != -1) {
            RequestMakeConfCall.addConfuri(builder, confUriOffset);
        }
        int i3 = useAnonymousUpdateOffset;
        int i4 = participantsOffset;
        RequestMakeConfCall.addHandle(builder, (long) handle);
        int makeConfCallOffset = RequestMakeConfCall.endRequestMakeConfCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 209);
        Request.addReqType(builder, (byte) 30);
        Request.addReq(builder, makeConfCallOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeExtendToConfCall(int handle, String confuri, int callType, String eventSubscribe, String dialogType, String[] participants, int sessId, String origUri, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd) {
        int removeReferUriTypeOffset;
        int referUriAssertedOffset;
        int useAnonymousUpdateOffset;
        String str = confuri;
        String str2 = eventSubscribe;
        String str3 = dialogType;
        String[] strArr = participants;
        String str4 = origUri;
        String str5 = referUriType;
        String str6 = removeReferUriType;
        String str7 = referUriAsserted;
        String str8 = useAnonymousUpdate;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int confUriOffset = -1;
        if (str != null) {
            confUriOffset = builder.createString((CharSequence) str);
        }
        int eventSubscribeOffset = -1;
        if (str2 != null) {
            eventSubscribeOffset = builder.createString((CharSequence) str2);
        }
        int dialogTypeOffset = -1;
        if (str3 != null) {
            dialogTypeOffset = builder.createString((CharSequence) str3);
        }
        int origUriOffset = -1;
        if (str4 != null) {
            origUriOffset = builder.createString((CharSequence) str4);
        }
        int referUriTypeOffset = -1;
        if (str5 != null) {
            referUriTypeOffset = builder.createString((CharSequence) str5);
        }
        if (str6 != null) {
            removeReferUriTypeOffset = builder.createString((CharSequence) str6);
        } else {
            removeReferUriTypeOffset = -1;
        }
        if (str7 != null) {
            referUriAssertedOffset = builder.createString((CharSequence) str7);
        } else {
            referUriAssertedOffset = -1;
        }
        if (str8 != null) {
            useAnonymousUpdateOffset = builder.createString((CharSequence) str8);
        } else {
            useAnonymousUpdateOffset = -1;
        }
        int lenOfParticipants = strArr.length;
        int[] participantOffsetArray = new int[lenOfParticipants];
        int i = 0;
        while (i < lenOfParticipants) {
            participantOffsetArray[i] = builder.createString((CharSequence) strArr[i]);
            i++;
            String str9 = dialogType;
        }
        int participantsOffset = RequestMakeConfCall.createParticipantsVector(builder, participantOffsetArray);
        int[] iArr = participantOffsetArray;
        int sessionIdOffset = RequestMakeConfCall.createSessionIdVector(builder, new int[]{sessId});
        RequestMakeConfCall.startRequestMakeConfCall(builder);
        int useAnonymousUpdateOffset2 = useAnonymousUpdateOffset;
        if (useAnonymousUpdateOffset2 != -1) {
            RequestMakeConfCall.addUseAnonymousUpdate(builder, useAnonymousUpdateOffset2);
        }
        int i2 = useAnonymousUpdateOffset2;
        int referUriAssertedOffset2 = referUriAssertedOffset;
        if (referUriAssertedOffset2 != -1) {
            RequestMakeConfCall.addReferuriAsserted(builder, referUriAssertedOffset2);
        }
        if (referUriTypeOffset != -1) {
            RequestMakeConfCall.addReferuriType(builder, referUriTypeOffset);
        }
        int i3 = referUriAssertedOffset2;
        int removeReferUriTypeOffset2 = removeReferUriTypeOffset;
        if (removeReferUriTypeOffset2 != -1) {
            RequestMakeConfCall.addRemoveReferuriType(builder, removeReferUriTypeOffset2);
        }
        if (origUriOffset != -1) {
            RequestMakeConfCall.addOrigUri(builder, origUriOffset);
        }
        RequestMakeConfCall.addParticipants(builder, participantsOffset);
        RequestMakeConfCall.addSessionId(builder, sessionIdOffset);
        if (dialogTypeOffset != -1) {
            RequestMakeConfCall.addDialogType(builder, dialogTypeOffset);
        }
        if (eventSubscribeOffset != -1) {
            RequestMakeConfCall.addEventSubscribe(builder, eventSubscribeOffset);
        }
        RequestMakeConfCall.addConfType(builder, 1);
        RequestMakeConfCall.addCallType(builder, callType);
        int i4 = sessionIdOffset;
        RequestMakeConfCall.addSupportPrematureEnd(builder, supportPrematureEnd);
        if (confUriOffset != -1) {
            RequestMakeConfCall.addConfuri(builder, confUriOffset);
        }
        int i5 = participantsOffset;
        RequestMakeConfCall.addHandle(builder, (long) handle);
        int makeConfCallOffset = RequestMakeConfCall.endRequestMakeConfCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 222);
        Request.addReqType(builder, (byte) 30);
        Request.addReq(builder, makeConfCallOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeUpdateConfCall(int handle, int session, int cmd, int participantId, String participant) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int parOffset = builder.createString((CharSequence) participant);
        RequestUpdateConfCall.startRequestUpdateConfCall(builder);
        RequestUpdateConfCall.addSession(builder, (long) session);
        RequestUpdateConfCall.addCmd(builder, (long) cmd);
        RequestUpdateConfCall.addParticipantId(builder, (long) participantId);
        RequestUpdateConfCall.addParticipant(builder, parOffset);
        int reqOffset = RequestUpdateConfCall.endRequestUpdateConfCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 215);
        Request.addReqType(builder, (byte) 31);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeTransferCall(int handle, int sessionId, String targetUri, int replacingSessionId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uriOffset = builder.createString((CharSequence) targetUri);
        RequestTransferCall.startRequestTransferCall(builder);
        RequestTransferCall.addHandle(builder, (long) handle);
        RequestTransferCall.addSession(builder, (long) sessionId);
        RequestTransferCall.addTargetUri(builder, uriOffset);
        if (replacingSessionId > 0) {
            RequestTransferCall.addReplacingSession(builder, (long) replacingSessionId);
        }
        int reqOffset = RequestTransferCall.endRequestTransferCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 212);
        Request.addReqType(builder, (byte) 19);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeCancelTransferCall(int handle, int sessionId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestCancelTransferCall.startRequestCancelTransferCall(builder);
        RequestCancelTransferCall.addHandle(builder, (long) handle);
        RequestCancelTransferCall.addSession(builder, (long) sessionId);
        int reqOffset = RequestCancelTransferCall.endRequestCancelTransferCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 227);
        Request.addReqType(builder, (byte) 29);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makePullingCall(int handle, String pullingUri, String targetUri, String origUri, Dialog targetDialog, List<String> p2p) {
        String str = pullingUri;
        String str2 = targetUri;
        String str3 = origUri;
        List<String> list = p2p;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int pullingUriOffset = -1;
        if (str != null) {
            pullingUriOffset = builder.createString((CharSequence) str);
        }
        int targetUriOffset = -1;
        if (str2 != null) {
            targetUriOffset = builder.createString((CharSequence) str2);
        }
        int origUriOffset = -1;
        if (str3 != null) {
            origUriOffset = builder.createString((CharSequence) str3);
        }
        int mdmnExtNumberOffset = -1;
        if (targetDialog.getMdmnExtNumber() != null) {
            mdmnExtNumberOffset = builder.createString((CharSequence) targetDialog.getMdmnExtNumber());
        }
        int callIdOffset = -1;
        if (targetDialog.getSipCallId() != null) {
            callIdOffset = builder.createString((CharSequence) targetDialog.getSipCallId());
        }
        int RemoteTagOffset = -1;
        if (targetDialog.getSipRemoteTag() != null) {
            RemoteTagOffset = builder.createString((CharSequence) targetDialog.getSipRemoteTag());
        }
        int LocalTagOffset = -1;
        if (targetDialog.getSipLocalTag() != null) {
            LocalTagOffset = builder.createString((CharSequence) targetDialog.getSipLocalTag());
        }
        int p2pListOffset = -1;
        if (list != null && p2p.size() > 0) {
            int[] p2pArr = new int[p2p.size()];
            String str4 = LOG_TAG;
            Log.i(str4, "p2p.size():" + p2p.size());
            for (int i = 0; i < p2pArr.length; i++) {
                p2pArr[i] = builder.createString((CharSequence) list.get(i));
            }
            p2pListOffset = RequestPullingCall.createP2pListVector(builder, p2pArr);
        }
        RequestPullingCall.startRequestPullingCall(builder);
        if (mdmnExtNumberOffset != -1) {
            RequestPullingCall.addMdmnExtNumber(builder, mdmnExtNumberOffset);
        }
        RequestPullingCall.addVideoDirection(builder, (long) targetDialog.getVideoDirection());
        RequestPullingCall.addAudioDirection(builder, (long) targetDialog.getAudioDirection());
        RequestPullingCall.addCodec(builder, 1);
        RequestPullingCall.addCallType(builder, targetDialog.getCallType());
        if (RemoteTagOffset != -1) {
            RequestPullingCall.addRemoteTag(builder, RemoteTagOffset);
        }
        if (LocalTagOffset != -1) {
            RequestPullingCall.addLocalTag(builder, LocalTagOffset);
        }
        if (callIdOffset != -1) {
            RequestPullingCall.addCallId(builder, callIdOffset);
        }
        if (origUriOffset != -1) {
            RequestPullingCall.addOrigUri(builder, origUriOffset);
        }
        if (targetUriOffset != -1) {
            RequestPullingCall.addTargetUri(builder, targetUriOffset);
        }
        if (pullingUriOffset != -1) {
            RequestPullingCall.addPullingUri(builder, pullingUriOffset);
        }
        if (p2pListOffset != -1) {
            RequestPullingCall.addP2pList(builder, p2pListOffset);
        }
        RequestPullingCall.addHandle(builder, (long) handle);
        RequestPullingCall.addIsVideoPortZero(builder, targetDialog.isVideoPortZero());
        int pullingCallOffset = RequestPullingCall.endRequestPullingCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 224);
        Request.addReqType(builder, (byte) 28);
        Request.addReq(builder, pullingCallOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makePublishDialog(int handle, String origUri, String dispName, String xmlBody, int expireTime) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int dispNameOffset = -1;
        if (dispName != null) {
            dispNameOffset = builder.createString((CharSequence) dispName);
        }
        int origUriOffset = -1;
        if (origUri != null) {
            origUriOffset = builder.createString((CharSequence) origUri);
        }
        int xmlBodyOffset = -1;
        if (xmlBody != null) {
            xmlBodyOffset = builder.createString((CharSequence) xmlBody);
        }
        RequestPublishDialog.startRequestPublishDialog(builder);
        RequestPublishDialog.addHandle(builder, (long) handle);
        if (dispNameOffset != -1) {
            RequestPublishDialog.addDispName(builder, dispNameOffset);
        }
        if (origUriOffset != -1) {
            RequestPublishDialog.addOrigUri(builder, origUriOffset);
        }
        if (xmlBodyOffset != -1) {
            RequestPublishDialog.addXmlBody(builder, xmlBodyOffset);
        }
        RequestPublishDialog.addExpireTime(builder, expireTime);
        int requestPublisgDialog = RequestPublishDialog.endRequestPublishDialog(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 228);
        Request.addReqType(builder, (byte) 32);
        Request.addReq(builder, requestPublisgDialog);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeAcceptCallTransfer(int handle, int sessionId, boolean accept, int status, String reason) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int reasonOffset = -1;
        if (!TextUtils.isEmpty(reason)) {
            reasonOffset = builder.createString((CharSequence) reason);
        }
        RequestAcceptTransferCall.startRequestAcceptTransferCall(builder);
        RequestAcceptTransferCall.addSession(builder, (long) sessionId);
        RequestAcceptTransferCall.addHandle(builder, (long) handle);
        RequestAcceptTransferCall.addAccept(builder, accept);
        if (status > 0) {
            if (reasonOffset != -1) {
                RequestAcceptTransferCall.addReasonPhrase(builder, reasonOffset);
            }
            RequestAcceptTransferCall.addStatusCode(builder, (long) status);
        }
        int acceptCallTransferOffset = RequestAcceptTransferCall.endRequestAcceptTransferCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 213);
        Request.addReqType(builder, (byte) 20);
        Request.addReq(builder, acceptCallTransferOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeHandleDtmf(int handle, int sessionId, int code, int mode, int operation) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestHandleDtmf.startRequestHandleDtmf(builder);
        RequestHandleDtmf.addHandle(builder, (long) handle);
        RequestHandleDtmf.addSession(builder, (long) sessionId);
        RequestHandleDtmf.addCode(builder, (long) code);
        RequestHandleDtmf.addMode(builder, (long) mode);
        RequestHandleDtmf.addOperation(builder, (long) operation);
        int reqOffset = RequestHandleDtmf.endRequestHandleDtmf(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 207);
        Request.addReqType(builder, ReqMsg.request_handle_dtmf);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeSendText(int handle, int sessionId, String text, int len) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int textOffset = builder.createString((CharSequence) text);
        RequestSendText.startRequestSendText(builder);
        RequestSendText.addHandle(builder, (long) handle);
        RequestSendText.addSession(builder, (long) sessionId);
        RequestSendText.addText(builder, textOffset);
        RequestSendText.addLen(builder, (long) len);
        int reqOffset = RequestSendText.endRequestSendText(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 234);
        Request.addReqType(builder, (byte) 95);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeModifyCallType(int sessionId, int oldType, int newType) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestModifyCallType.startRequestModifyCallType(builder);
        RequestModifyCallType.addSession(builder, (long) sessionId);
        RequestModifyCallType.addOldType(builder, oldType);
        RequestModifyCallType.addNewType(builder, newType);
        int reqOffset = RequestModifyCallType.endRequestModifyCallType(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 216);
        Request.addReqType(builder, (byte) 22);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeModifyVideoQuality(int sessionId, int oldQual, int newQual) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestModifyVideoQuality.startRequestModifyVideoQuality(builder);
        RequestModifyVideoQuality.addSession(builder, (long) sessionId);
        RequestModifyVideoQuality.addOldQual(builder, oldQual);
        RequestModifyVideoQuality.addNewQual(builder, newQual);
        int reqOffset = RequestModifyVideoQuality.endRequestModifyVideoQuality(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 233);
        Request.addReqType(builder, (byte) 37);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeReplyModifyCallType(int sessionId, int reqType, int curType, int repType, String cmcCallTime) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int cmcCallTimeOffset = -1;
        if (cmcCallTime != null) {
            cmcCallTimeOffset = builder.createString((CharSequence) cmcCallTime);
        }
        RequestReplyModifyCallType.startRequestReplyModifyCallType(builder);
        RequestReplyModifyCallType.addSession(builder, (long) sessionId);
        RequestReplyModifyCallType.addReqType(builder, reqType);
        RequestReplyModifyCallType.addCurType(builder, curType);
        RequestReplyModifyCallType.addRepType(builder, repType);
        if (cmcCallTimeOffset != -1) {
            RequestReplyModifyCallType.addCmcCallTime(builder, cmcCallTimeOffset);
        }
        int reqOffset = RequestReplyModifyCallType.endRequestReplyModifyCallType(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 217);
        Request.addReqType(builder, (byte) 23);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeRejectModifyCallType(int sessionId, int reason) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestRejectModifyCallType.startRequestRejectModifyCallType(builder);
        RequestRejectModifyCallType.addSession(builder, (long) sessionId);
        RequestRejectModifyCallType.addReason(builder, reason);
        int reqOffset = RequestRejectModifyCallType.endRequestRejectModifyCallType(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 221);
        Request.addReqType(builder, (byte) 27);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeUpdateCall(int sessionId, int action, int codecType, int cause, String reasonText) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int reasonOffset = -1;
        if (!TextUtils.isEmpty(reasonText)) {
            reasonOffset = builder.createString((CharSequence) reasonText);
        }
        RequestUpdateCall.startRequestUpdateCall(builder);
        if (reasonOffset != -1) {
            RequestUpdateCall.addReasonText(builder, reasonOffset);
        }
        RequestUpdateCall.addCause(builder, (long) cause);
        RequestUpdateCall.addCodecType(builder, codecType);
        RequestUpdateCall.addAction(builder, action);
        RequestUpdateCall.addSession(builder, (long) sessionId);
        int updateCallOffset = RequestUpdateCall.endRequestUpdateCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 206);
        Request.addReqType(builder, (byte) 15);
        Request.addReq(builder, updateCallOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeSendInfo(int handle, int sessionId, int callType, int ussdType, AdditionalContents ac) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int acOffset = -1;
        if (ac != null) {
            int mimeOffset = -1;
            if (ac.mimeType() != null) {
                mimeOffset = builder.createString((CharSequence) ac.mimeType());
            }
            int xmlStringOffset = -1;
            if (ac.contents() != null) {
                xmlStringOffset = builder.createString((CharSequence) ac.contents());
            }
            AdditionalContents.startAdditionalContents(builder);
            if (mimeOffset != -1) {
                AdditionalContents.addMimeType(builder, mimeOffset);
            }
            if (xmlStringOffset != -1) {
                AdditionalContents.addContents(builder, xmlStringOffset);
            }
            acOffset = AdditionalContents.endAdditionalContents(builder);
        }
        RequestSendInfo.startRequestSendInfo(builder);
        if (acOffset != -1) {
            RequestSendInfo.addAdditionalContents(builder, acOffset);
        }
        RequestSendInfo.addUssdType(builder, (long) ussdType);
        RequestSendInfo.addCallType(builder, callType);
        RequestSendInfo.addSession(builder, (long) sessionId);
        RequestSendInfo.addHandle(builder, (long) handle);
        int sendInfoOffset = RequestSendInfo.endRequestSendInfo(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 229);
        Request.addReqType(builder, (byte) 71);
        Request.addReq(builder, sendInfoOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeSendCmcInfo(int handle, int sessionId, AdditionalContents ac) {
        Log.i(LOG_TAG, "makeSendCmcInfo");
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int acOffset = -1;
        if (ac != null) {
            int mimeOffset = -1;
            if (ac.mimeType() != null) {
                mimeOffset = builder.createString((CharSequence) ac.mimeType());
            }
            int xmlStringOffset = -1;
            if (ac.contents() != null) {
                xmlStringOffset = builder.createString((CharSequence) ac.contents());
            }
            AdditionalContents.startAdditionalContents(builder);
            if (mimeOffset != -1) {
                AdditionalContents.addMimeType(builder, mimeOffset);
            }
            if (xmlStringOffset != -1) {
                AdditionalContents.addContents(builder, xmlStringOffset);
            }
            acOffset = AdditionalContents.endAdditionalContents(builder);
        }
        RequestSendCmcInfo.startRequestSendCmcInfo(builder);
        if (acOffset != -1) {
            RequestSendCmcInfo.addAdditionalContents(builder, acOffset);
        }
        RequestSendCmcInfo.addSession(builder, (long) sessionId);
        RequestSendCmcInfo.addHandle(builder, (long) handle);
        int sendInfoOffset = RequestSendCmcInfo.endRequestSendCmcInfo(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 243);
        Request.addReqType(builder, (byte) 72);
        Request.addReq(builder, sendInfoOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeUpdateCmcExtCallCount(int phoneId, int callCnt) {
        Log.d(LOG_TAG, "makeUpdateCmcExtCallCount");
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestUpdateCmcExtCallCount.startRequestUpdateCmcExtCallCount(builder);
        RequestUpdateCmcExtCallCount.addPhoneId(builder, (long) phoneId);
        RequestUpdateCmcExtCallCount.addCallCount(builder, (long) callCnt);
        int updateCmcExtCallCountOffset = RequestUpdateCmcExtCallCount.endRequestUpdateCmcExtCallCount(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_UPDATE_CMC_EXT_CALL_COUNT);
        Request.addReqType(builder, (byte) 73);
        Request.addReq(builder, updateCmcExtCallCountOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeStartVideoEarlyMedia(int handle, int sessionId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestStartVideoEarlymedia.startRequestStartVideoEarlymedia(builder);
        RequestStartVideoEarlymedia.addSession(builder, (long) sessionId);
        RequestStartVideoEarlymedia.addHandle(builder, (long) handle);
        int reqOffset = RequestStartVideoEarlymedia.endRequestStartVideoEarlymedia(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 235);
        Request.addReqType(builder, ReqMsg.request_start_video_earlymedia);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeHandleCmcCsfb(int handle, int sessionId) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestHandleCmcCsfb.startRequestHandleCmcCsfb(builder);
        RequestHandleCmcCsfb.addSession(builder, (long) sessionId);
        RequestHandleCmcCsfb.addHandle(builder, (long) handle);
        int reqOffset = RequestHandleCmcCsfb.endRequestHandleCmcCsfb(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_HANDLE_CMC_CSFB);
        Request.addReqType(builder, ReqMsg.request_handle_cmc_csfb);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }

    static StackRequest makeProgressIncomingCall(int handle, int sessionId, HashMap<String, String> headers) {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int pairOffset = -1;
        if (headers != null) {
            List<Integer> pairList = StackRequestBuilderUtil.translateExtraHeader(builder, headers);
            int[] pairOffsetArr = new int[pairList.size()];
            int i = 0;
            for (Integer intValue : pairList) {
                pairOffsetArr[i] = intValue.intValue();
                i++;
            }
            String str = LOG_TAG;
            Log.i(str, "Adding extra headers " + pairList.size());
            pairOffset = ExtraHeader.createPairVector(builder, pairOffsetArr);
        }
        int extraHeaderOffset = -1;
        if (headers != null) {
            ExtraHeader.startExtraHeader(builder);
            ExtraHeader.addPair(builder, pairOffset);
            extraHeaderOffset = ExtraHeader.endExtraHeader(builder);
        }
        RequestProgressIncomingCall.startRequestProgressIncomingCall(builder);
        RequestProgressIncomingCall.addSession(builder, (long) sessionId);
        if (extraHeaderOffset != -1) {
            RequestProgressIncomingCall.addExtraHeader(builder, extraHeaderOffset);
        }
        int reqOffset = RequestProgressIncomingCall.endRequestProgressIncomingCall(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 218);
        Request.addReqType(builder, (byte) 24);
        Request.addReq(builder, reqOffset);
        return new StackRequest(builder, Request.endRequest(builder));
    }
}
