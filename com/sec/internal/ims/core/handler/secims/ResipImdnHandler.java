package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnResponseReceivedEvent;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnRecRoute;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImNotificationStatusReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImdnResponseReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendImNotiResponse;
import com.sec.internal.ims.translate.ResipTranslatorCollection;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.log.IMSLog;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResipImdnHandler extends Handler {
    private static final int EVENT_IMDN_NOTI = 2;
    private static final int EVENT_IMDN_RESPONSE = 1;
    private static final String LOG_TAG = ResipImdnHandler.class.getSimpleName();
    private final RegistrantList mImdnNotificationRegistrants = new RegistrantList();
    private final RegistrantList mImdnResponseRegistransts = new RegistrantList();
    private final IImsFramework mImsFramework;

    public ResipImdnHandler(Looper looper, IImsFramework imsFramework) {
        super(looper);
        this.mImsFramework = imsFramework;
        StackIF.getInstance().registerImdnHandler(this, 2, (Object) null);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            AsyncResult ar = (AsyncResult) msg.obj;
            handleSendImdnNotificationResponse((Message) ar.userObj, (SendImNotiResponse) ar.result);
        } else if (i == 2) {
            handleNotify((Notify) ((AsyncResult) msg.obj).result);
        }
    }

    private void handleNotify(Notify notify) {
        int notifyid = notify.notifyid();
        if (notifyid == 11006) {
            handleImdnReceivedNotify(notify);
        } else if (notifyid == 11015) {
            handleSendImdnResponseNotify(notify);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerForImdnNotification(Handler h, int what, Object obj) {
        this.mImdnNotificationRegistrants.add(new Registrant(h, what, obj));
    }

    /* access modifiers changed from: package-private */
    public void unregisterForImdnNotification(Handler h) {
        this.mImdnNotificationRegistrants.remove(h);
    }

    /* access modifiers changed from: package-private */
    public void registerForImdnResponse(Handler h, int what, Object obj) {
        this.mImdnResponseRegistransts.add(new Registrant(h, what, obj));
    }

    /* access modifiers changed from: package-private */
    public void unregisterForImdnResponse(Handler h) {
        this.mImdnResponseRegistransts.remove(h);
    }

    private int[] getImdnRecRouteOffsetArray(FlatBufferBuilder builder, List<ImImdnRecRoute> imImdnRecRouteList, int size) {
        int[] offsetArray = new int[size];
        int i = 0;
        for (ImImdnRecRoute route : imImdnRecRouteList) {
            int nameOffset = builder.createString((CharSequence) route.getRecordRouteDispName());
            int uriOffset = builder.createString((CharSequence) route.getRecordRouteUri());
            ImdnRecRoute.startImdnRecRoute(builder);
            ImdnRecRoute.addName(builder, nameOffset);
            ImdnRecRoute.addUri(builder, uriOffset);
            offsetArray[i] = ImdnRecRoute.endImdnRecRoute(builder);
            i++;
        }
        return offsetArray;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x02ae  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x02b4  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x02ba  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x02c0  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x02c6  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x02cc  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x02da  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x02f9  */
    /* JADX WARNING: Removed duplicated region for block: B:173:0x03ce  */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x03df  */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x0283 A[EDGE_INSN: B:175:0x0283->B:134:0x0283 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x016b A[SYNTHETIC, Splitter:B:76:0x016b] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendDispositionNotification(com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams r29, int r30, int r31) {
        /*
            r28 = this;
            r7 = r28
            r8 = r29
            r9 = r30
            r10 = r31
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "sendDispositionNotification(): service = "
            r1.append(r2)
            r1.append(r9)
            java.lang.String r2 = ", sessionHandle = "
            r1.append(r2)
            r1.append(r10)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "sendDispositionNotification(): "
            r1.append(r2)
            r1.append(r8)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            com.sec.internal.interfaces.ims.IImsFramework r0 = r7.mImsFramework
            com.sec.internal.interfaces.ims.core.IRegistrationManager r11 = r0.getRegistrationManager()
            if (r9 == 0) goto L_0x0061
            r0 = 2
            if (r9 == r0) goto L_0x0055
            java.lang.String r0 = r8.mOwnImsi
            java.lang.String r1 = "im"
            com.sec.internal.interfaces.ims.core.IUserAgent r0 = r11.getUserAgentByImsi(r1, r0)
            com.sec.internal.ims.core.handler.secims.UserAgent r0 = (com.sec.internal.ims.core.handler.secims.UserAgent) r0
            r12 = r0
            goto L_0x006d
        L_0x0055:
            java.lang.String r0 = r8.mOwnImsi
            java.lang.String r1 = "ft"
            com.sec.internal.interfaces.ims.core.IUserAgent r0 = r11.getUserAgentByImsi(r1, r0)
            com.sec.internal.ims.core.handler.secims.UserAgent r0 = (com.sec.internal.ims.core.handler.secims.UserAgent) r0
            r12 = r0
            goto L_0x006d
        L_0x0061:
            java.lang.String r0 = r8.mOwnImsi
            java.lang.String r1 = "slm"
            com.sec.internal.interfaces.ims.core.IUserAgent r0 = r11.getUserAgentByImsi(r1, r0)
            com.sec.internal.ims.core.handler.secims.UserAgent r0 = (com.sec.internal.ims.core.handler.secims.UserAgent) r0
            r12 = r0
        L_0x006d:
            if (r12 != 0) goto L_0x008a
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "sendDispositionNotification(): UserAgent not found."
            android.util.Log.e(r0, r1)
            android.os.Message r0 = r8.mCallback
            if (r0 == 0) goto L_0x0089
            android.os.Message r0 = r8.mCallback
            com.sec.internal.constants.ims.servicemodules.im.result.Result r1 = new com.sec.internal.constants.ims.servicemodules.im.result.Result
            com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.ENGINE_ERROR
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r3 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.ENGINE_ERROR
            r1.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r2, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r3)
            r7.sendCallback(r0, r1)
        L_0x0089:
            return
        L_0x008a:
            int r13 = r12.getHandle()
            com.google.flatbuffers.FlatBufferBuilder r0 = new com.google.flatbuffers.FlatBufferBuilder
            r1 = 0
            r0.<init>((int) r1)
            r14 = r0
            r2 = -1
            r3 = -1
            r4 = -1
            r5 = -1
            r6 = -1
            r15 = -1
            r16 = -1
            com.sec.ims.util.ImsUri r0 = r8.mUri     // Catch:{ NullPointerException -> 0x03a7 }
            java.lang.String r17 = ""
            if (r0 != 0) goto L_0x00a6
            r0 = r17
            goto L_0x00ac
        L_0x00a6:
            com.sec.ims.util.ImsUri r0 = r8.mUri     // Catch:{ NullPointerException -> 0x03a7 }
            java.lang.String r0 = r0.toString()     // Catch:{ NullPointerException -> 0x03a7 }
        L_0x00ac:
            int r18 = r14.createString((java.lang.CharSequence) r0)     // Catch:{ NullPointerException -> 0x03a7 }
            r15 = r18
            java.lang.String r1 = r8.mConversationId     // Catch:{ NullPointerException -> 0x03a7 }
            if (r1 == 0) goto L_0x00c2
            java.lang.String r1 = r8.mConversationId     // Catch:{ NullPointerException -> 0x00b9 }
            goto L_0x00c4
        L_0x00b9:
            r0 = move-exception
            r20 = r6
            r21 = r11
            r24 = r12
            goto L_0x03b0
        L_0x00c2:
            r1 = r17
        L_0x00c4:
            int r1 = r14.createString((java.lang.CharSequence) r1)     // Catch:{ NullPointerException -> 0x03a7 }
            r2 = r1
            java.lang.String r1 = r8.mContributionId     // Catch:{ NullPointerException -> 0x039b }
            if (r1 == 0) goto L_0x00d0
            java.lang.String r1 = r8.mContributionId     // Catch:{ NullPointerException -> 0x00b9 }
            goto L_0x00d2
        L_0x00d0:
            r1 = r17
        L_0x00d2:
            int r1 = r14.createString((java.lang.CharSequence) r1)     // Catch:{ NullPointerException -> 0x039b }
            r3 = r1
            java.lang.String r1 = r8.mDeviceId     // Catch:{ NullPointerException -> 0x038d }
            if (r1 == 0) goto L_0x00de
            java.lang.String r1 = r8.mDeviceId     // Catch:{ NullPointerException -> 0x00b9 }
            goto L_0x00e0
        L_0x00de:
            r1 = r17
        L_0x00e0:
            int r1 = r14.createString((java.lang.CharSequence) r1)     // Catch:{ NullPointerException -> 0x038d }
            r4 = r1
            java.util.Date r1 = r8.mCpimDate     // Catch:{ NullPointerException -> 0x037d }
            java.lang.String r1 = com.sec.internal.helper.Iso8601.formatMillis(r1)     // Catch:{ NullPointerException -> 0x037d }
            java.lang.String r1 = r7.parseStr(r1)     // Catch:{ NullPointerException -> 0x037d }
            int r1 = r14.createString((java.lang.CharSequence) r1)     // Catch:{ NullPointerException -> 0x037d }
            r17 = r0
            java.util.Map<java.lang.String, java.lang.String> r0 = r8.mImExtensionMNOHeaders     // Catch:{ NullPointerException -> 0x0369 }
            if (r0 == 0) goto L_0x014c
            java.util.Map<java.lang.String, java.lang.String> r0 = r8.mImExtensionMNOHeaders     // Catch:{ NullPointerException -> 0x013f }
            boolean r0 = r0.isEmpty()     // Catch:{ NullPointerException -> 0x013f }
            if (r0 != 0) goto L_0x014c
            java.lang.String r0 = LOG_TAG     // Catch:{ NullPointerException -> 0x013f }
            r19 = r5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ NullPointerException -> 0x0132 }
            r5.<init>()     // Catch:{ NullPointerException -> 0x0132 }
            r20 = r6
            java.lang.String r6 = "sendDispositionNotification()112312: headers "
            r5.append(r6)     // Catch:{ NullPointerException -> 0x0127 }
            java.util.Map<java.lang.String, java.lang.String> r6 = r8.mImExtensionMNOHeaders     // Catch:{ NullPointerException -> 0x0127 }
            r5.append(r6)     // Catch:{ NullPointerException -> 0x0127 }
            java.lang.String r5 = r5.toString()     // Catch:{ NullPointerException -> 0x0127 }
            android.util.Log.i(r0, r5)     // Catch:{ NullPointerException -> 0x0127 }
            java.util.Map<java.lang.String, java.lang.String> r0 = r8.mImExtensionMNOHeaders     // Catch:{ NullPointerException -> 0x0127 }
            int r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateStackImExtensionHeaders(r14, r0)     // Catch:{ NullPointerException -> 0x0127 }
            r5 = r0
            r6 = r5
            goto L_0x0152
        L_0x0127:
            r0 = move-exception
            r16 = r1
            r21 = r11
            r24 = r12
            r5 = r19
            goto L_0x03b0
        L_0x0132:
            r0 = move-exception
            r20 = r6
            r16 = r1
            r21 = r11
            r24 = r12
            r5 = r19
            goto L_0x03b0
        L_0x013f:
            r0 = move-exception
            r19 = r5
            r20 = r6
            r16 = r1
            r21 = r11
            r24 = r12
            goto L_0x03b0
        L_0x014c:
            r19 = r5
            r20 = r6
            r6 = r19
        L_0x0152:
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams$ImdnData> r0 = r8.mImdnDataList     // Catch:{ NullPointerException -> 0x0355 }
            int r0 = r0.size()     // Catch:{ NullPointerException -> 0x0355 }
            int[] r0 = new int[r0]     // Catch:{ NullPointerException -> 0x0355 }
            r5 = 0
            r16 = r5
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams$ImdnData> r5 = r8.mImdnDataList     // Catch:{ NullPointerException -> 0x0355 }
            java.util.Iterator r5 = r5.iterator()     // Catch:{ NullPointerException -> 0x0355 }
        L_0x0163:
            boolean r19 = r5.hasNext()     // Catch:{ NullPointerException -> 0x0355 }
            r21 = r11
            if (r19 == 0) goto L_0x0283
            java.lang.Object r19 = r5.next()     // Catch:{ NullPointerException -> 0x0272 }
            com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams$ImdnData r19 = (com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams.ImdnData) r19     // Catch:{ NullPointerException -> 0x0272 }
            r22 = r19
            r19 = -1
            r11 = r22
            r22 = r5
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute> r5 = r11.mImdnRecRouteList     // Catch:{ NullPointerException -> 0x0272 }
            if (r5 == 0) goto L_0x01a2
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute> r5 = r11.mImdnRecRouteList     // Catch:{ NullPointerException -> 0x019a }
            r24 = r12
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute> r12 = r11.mImdnRecRouteList     // Catch:{ NullPointerException -> 0x0194 }
            int r12 = r12.size()     // Catch:{ NullPointerException -> 0x0194 }
            int[] r5 = r7.getImdnRecRouteOffsetArray(r14, r5, r12)     // Catch:{ NullPointerException -> 0x0194 }
            int r12 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.createImdnRecRouteVector(r14, r5)     // Catch:{ NullPointerException -> 0x0194 }
            r19 = r12
            r5 = r19
            goto L_0x01a6
        L_0x0194:
            r0 = move-exception
            r16 = r1
            r5 = r6
            goto L_0x03b0
        L_0x019a:
            r0 = move-exception
            r24 = r12
            r16 = r1
            r5 = r6
            goto L_0x03b0
        L_0x01a2:
            r24 = r12
            r5 = r19
        L_0x01a6:
            java.lang.String r12 = r11.mImdnId     // Catch:{ NullPointerException -> 0x0263 }
            java.lang.String r12 = r7.parseStr(r12)     // Catch:{ NullPointerException -> 0x0263 }
            int r12 = r14.createString((java.lang.CharSequence) r12)     // Catch:{ NullPointerException -> 0x0263 }
            r19 = r6
            java.util.Date r6 = r11.mImdnDate     // Catch:{ NullPointerException -> 0x0256 }
            java.lang.String r6 = com.sec.internal.helper.Iso8601.formatMillis(r6)     // Catch:{ NullPointerException -> 0x0256 }
            java.lang.String r6 = r7.parseStr(r6)     // Catch:{ NullPointerException -> 0x0256 }
            int r6 = r14.createString((java.lang.CharSequence) r6)     // Catch:{ NullPointerException -> 0x0256 }
            r25 = -1
            r26 = r4
            java.lang.String r4 = r11.mImdnOriginalTo     // Catch:{ NullPointerException -> 0x0249 }
            if (r4 == 0) goto L_0x01de
            java.lang.String r4 = r11.mImdnOriginalTo     // Catch:{ NullPointerException -> 0x01d5 }
            java.lang.String r4 = r7.parseStr(r4)     // Catch:{ NullPointerException -> 0x01d5 }
            int r4 = r14.createString((java.lang.CharSequence) r4)     // Catch:{ NullPointerException -> 0x01d5 }
            r25 = r4
            goto L_0x01e0
        L_0x01d5:
            r0 = move-exception
            r16 = r1
            r5 = r19
            r4 = r26
            goto L_0x03b0
        L_0x01de:
            r4 = r25
        L_0x01e0:
            r25 = r3
            r7 = 1
            int[] r3 = new int[r7]     // Catch:{ NullPointerException -> 0x023c }
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r7 = r11.mStatus     // Catch:{ NullPointerException -> 0x023c }
            r27 = r2
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r2 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ NullPointerException -> 0x022f }
            if (r7 != r2) goto L_0x01f0
            r23 = 1
            goto L_0x01f2
        L_0x01f0:
            r23 = 0
        L_0x01f2:
            r2 = 0
            r3[r2] = r23     // Catch:{ NullPointerException -> 0x022f }
            int r7 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.createStatusVector(r14, r3)     // Catch:{ NullPointerException -> 0x022f }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.startImNotificationParam(r14)     // Catch:{ NullPointerException -> 0x022f }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addImdnMessageId(r14, r12)     // Catch:{ NullPointerException -> 0x022f }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addStatus(r14, r7)     // Catch:{ NullPointerException -> 0x022f }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addImdnDateTime(r14, r6)     // Catch:{ NullPointerException -> 0x022f }
            java.util.List<com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute> r2 = r11.mImdnRecRouteList     // Catch:{ NullPointerException -> 0x022f }
            if (r2 == 0) goto L_0x020c
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addImdnRecRoute(r14, r5)     // Catch:{ NullPointerException -> 0x022f }
        L_0x020c:
            java.lang.String r2 = r11.mImdnOriginalTo     // Catch:{ NullPointerException -> 0x022f }
            if (r2 == 0) goto L_0x0213
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.addImdnOriginalTo(r14, r4)     // Catch:{ NullPointerException -> 0x022f }
        L_0x0213:
            int r2 = r16 + 1
            int r23 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImNotificationParam.endImNotificationParam(r14)     // Catch:{ NullPointerException -> 0x022f }
            r0[r16] = r23     // Catch:{ NullPointerException -> 0x022f }
            r7 = r28
            r16 = r2
            r6 = r19
            r11 = r21
            r5 = r22
            r12 = r24
            r3 = r25
            r4 = r26
            r2 = r27
            goto L_0x0163
        L_0x022f:
            r0 = move-exception
            r16 = r1
            r5 = r19
            r3 = r25
            r4 = r26
            r2 = r27
            goto L_0x03b0
        L_0x023c:
            r0 = move-exception
            r27 = r2
            r16 = r1
            r5 = r19
            r3 = r25
            r4 = r26
            goto L_0x03b0
        L_0x0249:
            r0 = move-exception
            r27 = r2
            r25 = r3
            r16 = r1
            r5 = r19
            r4 = r26
            goto L_0x03b0
        L_0x0256:
            r0 = move-exception
            r27 = r2
            r25 = r3
            r26 = r4
            r16 = r1
            r5 = r19
            goto L_0x03b0
        L_0x0263:
            r0 = move-exception
            r27 = r2
            r25 = r3
            r26 = r4
            r19 = r6
            r16 = r1
            r5 = r19
            goto L_0x03b0
        L_0x0272:
            r0 = move-exception
            r27 = r2
            r25 = r3
            r26 = r4
            r19 = r6
            r24 = r12
            r16 = r1
            r5 = r19
            goto L_0x03b0
        L_0x0283:
            r27 = r2
            r25 = r3
            r26 = r4
            r19 = r6
            r24 = r12
            int r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.createNotificationsVector(r14, r0)     // Catch:{ NullPointerException -> 0x0340 }
            r0 = r2
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.startRequestSendImNotificationStatus(r14)
            long r2 = (long) r10
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addSessionId(r14, r2)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addNotifications(r14, r0)
            long r2 = (long) r13
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addRegistrationHandle(r14, r2)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addUri(r14, r15)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addService(r14, r9)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addCpimDateTime(r14, r1)
            java.lang.String r2 = r8.mConversationId
            if (r2 == 0) goto L_0x02b4
            r2 = r27
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addConversationId(r14, r2)
            goto L_0x02b6
        L_0x02b4:
            r2 = r27
        L_0x02b6:
            java.lang.String r3 = r8.mContributionId
            if (r3 == 0) goto L_0x02c0
            r3 = r25
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addContributionId(r14, r3)
            goto L_0x02c2
        L_0x02c0:
            r3 = r25
        L_0x02c2:
            java.lang.String r4 = r8.mDeviceId
            if (r4 == 0) goto L_0x02cc
            r4 = r26
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addDeviceId(r14, r4)
            goto L_0x02ce
        L_0x02cc:
            r4 = r26
        L_0x02ce:
            java.util.Map<java.lang.String, java.lang.String> r5 = r8.mImExtensionMNOHeaders
            if (r5 == 0) goto L_0x02f9
            java.util.Map<java.lang.String, java.lang.String> r5 = r8.mImExtensionMNOHeaders
            boolean r5 = r5.isEmpty()
            if (r5 != 0) goto L_0x02f9
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "sendDispositionNotification(): headers "
            r6.append(r7)
            java.util.Map<java.lang.String, java.lang.String> r7 = r8.mImExtensionMNOHeaders
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r5, r6)
            r6 = r19
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addExtension(r14, r6)
            goto L_0x02fb
        L_0x02f9:
            r6 = r19
        L_0x02fb:
            boolean r5 = r8.mIsGroupChat
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addIsGroupChat(r14, r5)
            boolean r5 = r8.mIsBotSessionAnonymized
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.addIsBotSessionAnonymized(r14, r5)
            int r7 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImNotificationStatus.endRequestSendImNotificationStatus(r14)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.startRequest(r14)
            r5 = 506(0x1fa, float:7.09E-43)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReqid(r14, r5)
            r5 = 45
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReqType(r14, r5)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReq(r14, r7)
            int r11 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.endRequest(r14)
            android.os.Message r12 = r8.mCallback
            r5 = 1
            r25 = r3
            r3 = r28
            android.os.Message r12 = r3.obtainMessage(r5, r12)
            r17 = r1
            r1 = r28
            r18 = r2
            r2 = r24
            r5 = r3
            r22 = r25
            r3 = 506(0x1fa, float:7.09E-43)
            r23 = r4
            r4 = r14
            r5 = r11
            r19 = r6
            r6 = r12
            r1.sendRequestToStack(r2, r3, r4, r5, r6)
            return
        L_0x0340:
            r0 = move-exception
            r17 = r1
            r22 = r25
            r23 = r26
            r18 = r27
            r16 = r17
            r2 = r18
            r5 = r19
            r3 = r22
            r4 = r23
            goto L_0x03b0
        L_0x0355:
            r0 = move-exception
            r17 = r1
            r18 = r2
            r22 = r3
            r23 = r4
            r19 = r6
            r21 = r11
            r24 = r12
            r16 = r17
            r5 = r19
            goto L_0x03b0
        L_0x0369:
            r0 = move-exception
            r17 = r1
            r18 = r2
            r22 = r3
            r23 = r4
            r19 = r5
            r20 = r6
            r21 = r11
            r24 = r12
            r16 = r17
            goto L_0x03b0
        L_0x037d:
            r0 = move-exception
            r18 = r2
            r22 = r3
            r23 = r4
            r19 = r5
            r20 = r6
            r21 = r11
            r24 = r12
            goto L_0x03b0
        L_0x038d:
            r0 = move-exception
            r18 = r2
            r22 = r3
            r19 = r5
            r20 = r6
            r21 = r11
            r24 = r12
            goto L_0x03b0
        L_0x039b:
            r0 = move-exception
            r18 = r2
            r19 = r5
            r20 = r6
            r21 = r11
            r24 = r12
            goto L_0x03b0
        L_0x03a7:
            r0 = move-exception
            r19 = r5
            r20 = r6
            r21 = r11
            r24 = r12
        L_0x03b0:
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "Discard sendDispositionNotification(): "
            r6.append(r7)
            java.lang.String r7 = r0.getMessage()
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r1, r6)
            android.os.Message r1 = r8.mCallback
            if (r1 == 0) goto L_0x03df
            android.os.Message r1 = r8.mCallback
            com.sec.internal.constants.ims.servicemodules.im.result.Result r6 = new com.sec.internal.constants.ims.servicemodules.im.result.Result
            com.sec.internal.constants.ims.servicemodules.im.ImError r7 = com.sec.internal.constants.ims.servicemodules.im.ImError.ENGINE_ERROR
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r11 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.ENGINE_ERROR
            r6.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r7, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r11)
            r7 = r28
            r7.sendCallback(r1, r6)
            goto L_0x03e1
        L_0x03df:
            r7 = r28
        L_0x03e1:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipImdnHandler.sendDispositionNotification(com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams, int, int):void");
    }

    private void handleSendImdnNotificationResponse(Message msg, SendImNotiResponse response) {
        Log.i(LOG_TAG, "handleSendImdnNotificationResponse()");
        Result result = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null);
        if (msg != null) {
            sendCallback(msg, result);
        }
    }

    private void handleImdnReceivedNotify(Notify notify) {
        Date cpimDate;
        Date imdnDate;
        if (notify.notiType() != 33) {
            Log.e(LOG_TAG, "handleImNotiReceivedNotify(): invalid notify");
            return;
        }
        ImNotificationStatusReceived proto = (ImNotificationStatusReceived) notify.noti(new ImNotificationStatusReceived());
        ImNotificationParam param = proto.status();
        if (param == null) {
            Log.e(LOG_TAG, "handleImNotiReceivedNotify(): param is null");
            return;
        }
        try {
            cpimDate = proto.cpimDateTime() != null ? Iso8601.parse(proto.cpimDateTime()) : new Date();
        } catch (ParseException e) {
            Log.e(LOG_TAG, e.toString());
            cpimDate = new Date();
        }
        try {
            imdnDate = param.imdnDateTime() != null ? Iso8601.parse(param.imdnDateTime()) : cpimDate;
        } catch (ParseException e2) {
            Log.e(LOG_TAG, e2.toString());
            imdnDate = cpimDate;
        }
        ImsUri remoteUri = ImsUri.parse(proto.uri());
        if (remoteUri == null) {
            String str = LOG_TAG;
            Log.i(str, "Invalid remote uri, return. uri=" + proto.uri());
            return;
        }
        ImdnNotificationEvent event = new ImdnNotificationEvent(param.imdnMessageId(), imdnDate, remoteUri, translateNotificationType(param.status(0)), cpimDate);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "handleImNotiReceivedNotify: " + event);
        this.mImdnNotificationRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
    }

    private void handleSendImdnResponseNotify(Notify notify) {
        if (notify.notiType() != 36) {
            Log.e(LOG_TAG, "handleSendImdnResponseNotify(): invalid notify");
            return;
        }
        ImdnResponseReceived response = (ImdnResponseReceived) notify.noti(new ImdnResponseReceived());
        Result result = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null);
        ArrayList<String> messageIds = new ArrayList<>();
        for (int i = 0; i < response.imdnMessageIdLength(); i++) {
            messageIds.add(response.imdnMessageId(i));
        }
        ImdnResponseReceivedEvent event = new ImdnResponseReceivedEvent(result, messageIds);
        String str = LOG_TAG;
        IMSLog.s(str, "handleSendImdnResponseNotify() Event : " + event);
        this.mImdnResponseRegistransts.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
    }

    private NotificationStatus translateNotificationType(int status) {
        if (status == 0) {
            return NotificationStatus.DELIVERED;
        }
        if (status == 1) {
            return NotificationStatus.DISPLAYED;
        }
        if (status == 2) {
            return NotificationStatus.INTERWORKING_SMS;
        }
        if (status != 3) {
            return NotificationStatus.DELIVERED;
        }
        return NotificationStatus.INTERWORKING_MMS;
    }

    private void sendRequestToStack(UserAgent ua, int id, FlatBufferBuilder builder, int offset, Message callback) {
        if (ua == null) {
            Log.e(LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            ua.sendRequestToStack(new ResipStackRequest(id, builder, offset, callback));
        }
    }

    private void sendCallback(Message callback, Object object) {
        AsyncResult.forMessage(callback, object, (Throwable) null);
        callback.sendToTarget();
    }

    private String parseStr(String str) {
        return str != null ? str : "";
    }
}
