package com.sec.internal.google;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.aidl.IImsSmsListener;
import android.util.Log;
import com.android.internal.telephony.TelephonyFeatures;
import com.android.internal.telephony.uicc.IccUtils;
import com.sec.ims.sms.ISmsServiceEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.sms.SmsMessage;
import com.sec.internal.constants.ims.servicemodules.sms.SmsResponse;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.sms.SmsLogger;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.xbill.DNS.Type;

public final class ImsSmsImpl {
    private static final int CDMA_NETWORK_TYPE = 1;
    private static final String CONTENT_TYPE_3GPP = "application/vnd.3gpp.sms";
    private static final String CONTENT_TYPE_3GPP2 = "application/vnd.3gpp2.sms";
    private static final int DELIVER_STATUS_ERROR_GENERIC = 2;
    private static final int DELIVER_STATUS_ERROR_NO_MEMORY = 3;
    private static final int DELIVER_STATUS_ERROR_REQUEST_NOT_SUPPORTED = 4;
    private static final int DELIVER_STATUS_OK = 1;
    private static final int EVENT_SMS_DELIVER_REPORT_RETRY = 4;
    private static final int EVENT_SMS_NO_RESPONSE_TIMEOUT = 2;
    private static final int EVENT_SMS_RETRY = 1;
    private static final int EVENT_SMS_SEND_DELAYED_MESSAGE = 3;
    private static final int GSM_NETWORK_TYPE = 2;
    private static final String IMS_CALL_PERMISSION = "android.permission.ACCESS_IMS_CALL_SERVICE";
    private static final String LOG_TAG_HEAD = "ImsSmsImpl";
    private static final String MAP_KEY_CONTENT_TYPE = "contentType";
    private static final String MAP_KEY_DEST_ADDR = "destAddr";
    private static final String MAP_KEY_MESSAGE_ID = "messageId";
    private static final String MAP_KEY_PDU = "pdu";
    private static final String MAP_KEY_RETRY_COUNT = "retryCount";
    private static final String MAP_KEY_STATUS_REPORT = "statusReport";
    private static final String MAP_KEY_TOKEN = "token";
    private static final int MAX_SEND_RETRIES_1 = 1;
    private static final int MAX_SEND_RETRIES_2 = 2;
    private static final int MAX_SEND_RETRIES_4 = 4;
    public static final int RESULT_NO_NETWORK_ERROR = -1;
    private static final int RIL_CODE_RP_ERROR = 32768;
    private static final int RIL_CODE_RP_ERROR_END = 33023;
    private static final int RP_CAUSE_CONGESTION = 42;
    private static final int RP_CAUSE_DESTINATION_OUT_OF_ORDER = 27;
    private static final int RP_CAUSE_MEMORY_CAP_EXCEEDED = 22;
    private static final int RP_CAUSE_NETWORK_OUT_OF_ORDER = 38;
    private static final int RP_CAUSE_NONE_ERROR = 0;
    private static final int RP_CAUSE_NOT_COMPATIBLE_PROTOCOL = 98;
    private static final int RP_CAUSE_PROTOCOL_ERROR = 111;
    private static final int RP_CAUSE_REQUESTED_FACILITY_NOT_IMPLEMENTED = 69;
    private static final int RP_CAUSE_RESOURCES_UNAVAILABLE = 47;
    private static final int RP_CAUSE_SMS_TRANSFER_REJECTED = 21;
    private static final int RP_CAUSE_TEMPORARY_FAILURE = 41;
    private static final int RP_CAUSE_UNIDENTIFIED_SUBSCRIBER = 28;
    private static final int RP_CAUSE_UNKNOWN_SUBSCRIBER = 30;
    private static final int SEND_RETRY_DELAY = 30000;
    private static final int SEND_STATUS_ERROR = 2;
    private static final int SEND_STATUS_ERROR_FALLBACK = 4;
    private static final int SEND_STATUS_ERROR_RETRY = 3;
    private static final int SEND_STATUS_OK = 1;
    public static final int STATUS_REPORT_STATUS_ERROR = 2;
    public static final int STATUS_REPORT_STATUS_OK = 1;
    private static final int TIMER_STATE = 130000;
    private static final int TP_CAUSE_INVALID_SME_ADDRESS = 195;
    private static final int TP_CAUSE_SM_REJECTED_OR_DUPLICATE = 197;
    /* access modifiers changed from: private */
    public String LOG_TAG;
    private Context mContext = null;
    /* access modifiers changed from: private */
    public int mCurrentNetworkType;
    private final ArrayList<ImsSmsTracker> mDeliveryPendingList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private Map<Integer, ImsSmsTracker> mImsSmsTrackers = new ConcurrentHashMap();
    private int mLastRetryCount;
    private int mLastRxStatusMsgId;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    /* access modifiers changed from: private */
    public LastSentDeliveryAck mSentDeliveryAck;
    private SmsEventListener mSmsEventListener = new SmsEventListener();
    /* access modifiers changed from: private */
    public IImsSmsListener mSmsListener = null;
    /* access modifiers changed from: private */
    public SmsLogger mSmsLogger = SmsLogger.getInstance();
    private ISmsServiceModule mSmsServiceModule = null;
    private int mTpmr;

    public ImsSmsImpl(Context context, int phoneId, IImsSmsListener listener) {
        String str = "";
        this.LOG_TAG = str;
        StringBuilder sb = new StringBuilder();
        sb.append(LOG_TAG_HEAD);
        sb.append(phoneId != 0 ? "2" : str);
        this.LOG_TAG = sb.toString();
        this.mPhoneId = phoneId;
        this.mSmsListener = listener;
        this.mContext = context;
        this.mTpmr = -1;
        ISmsServiceModule smsServiceModule = ImsRegistry.getServiceModuleManager().getSmsServiceModule();
        this.mSmsServiceModule = smsServiceModule;
        if (smsServiceModule != null) {
            try {
                registerSmsEventListener(phoneId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        HandlerThread handlerThread = new HandlerThread(LOG_TAG_HEAD);
        this.mHandlerThread = handlerThread;
        handlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                String access$100 = ImsSmsImpl.this.LOG_TAG;
                Log.d(access$100, "handleMessage: event " + msg.what);
                int i = msg.what;
                if (i == 1) {
                    ImsSmsImpl.this.handleSmsRetry((ImsSmsTracker) msg.obj);
                } else if (i == 2) {
                    ImsSmsImpl.this.handleNoResponseTimeout((ImsSmsTracker) msg.obj);
                } else if (i == 3) {
                    ImsSmsImpl.this.handleSendDelayedMessage();
                } else if (i == 4) {
                    ImsSmsImpl.this.handleRetryDeliveryReportAck((LastSentDeliveryAck) msg.obj);
                }
            }
        };
    }

    public void close() {
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            handlerThread.quit();
        }
    }

    private void registerSmsEventListener(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "registerSmsEventListener");
        ISmsServiceModule iSmsServiceModule = this.mSmsServiceModule;
        if (iSmsServiceModule != null) {
            iSmsServiceModule.registerForSMSStateChange(phoneId, this.mSmsEventListener);
        }
    }

    private class SmsEventListener extends ISmsServiceEventListener.Stub {
        private SmsEventListener() {
        }

        public void onReceiveIncomingSMS(int messageId, String contentType, byte[] pdu) throws RemoteException {
            if (contentType != null) {
                if (ImsSmsImpl.this.mSmsListener != null) {
                    SmsMessage msg = new SmsMessage();
                    if (contentType.equals("application/vnd.3gpp.sms")) {
                        int unused = ImsSmsImpl.this.mCurrentNetworkType = 2;
                        msg.parseDeliverPdu(pdu, SmsMessage.FORMAT_3GPP);
                        if (msg.getMessageType() == 1) {
                            ImsSmsImpl.this.mSmsListener.onSmsReceived(messageId, SmsMessage.FORMAT_3GPP, pdu);
                        } else if (msg.getMessageType() == 2) {
                            ImsSmsImpl.this.handleStatusReport(msg.getMessageRef(), messageId, SmsMessage.FORMAT_3GPP, pdu);
                        }
                    } else if (contentType.equals("application/vnd.3gpp2.sms")) {
                        int unused2 = ImsSmsImpl.this.mCurrentNetworkType = 1;
                        byte[] convertedPdu = msg.convertToFrameworkSmsFormat(pdu);
                        int token = msg.getMsgID();
                        if (msg.getMessageType() == 4) {
                            ImsSmsImpl.this.handleStatusReport(msg.getMsgID(), messageId, SmsMessage.FORMAT_3GPP2, convertedPdu);
                        } else {
                            ImsSmsImpl.this.mSmsListener.onSmsReceived(token, SmsMessage.FORMAT_3GPP2, convertedPdu);
                        }
                    }
                    SmsLogger access$800 = ImsSmsImpl.this.mSmsLogger;
                    String access$100 = ImsSmsImpl.this.LOG_TAG;
                    access$800.logAndAdd(access$100, "< NEW_SMS : contentType = " + contentType + " messageId = " + messageId);
                    if (!TelephonyFeatures.SHIP_BUILD) {
                        String access$1002 = ImsSmsImpl.this.LOG_TAG;
                        Log.d(access$1002, "pdu = " + IccUtils.bytesToHexString(pdu));
                        return;
                    }
                    return;
                }
                throw new RuntimeException("Sms not ready.");
            }
        }

        public void onReceiveSMSAck(int messageId, int reasonCode, String contentType, byte[] pdu, int retryAfter) throws RemoteException {
            if (ImsSmsImpl.this.mSmsListener != null) {
                int type = "application/vnd.3gpp2.sms".equals(contentType) ? 1 : 2;
                int token = ImsSmsImpl.this.getTokenByMessageId(messageId);
                if (token == -1) {
                    String access$100 = ImsSmsImpl.this.LOG_TAG;
                    Log.i(access$100, "messageID = " + messageId + " cannot find token");
                    return;
                }
                SmsResponse response = new SmsResponse(messageId, reasonCode, pdu, type);
                ImsSmsImpl imsSmsImpl = ImsSmsImpl.this;
                imsSmsImpl.onReceiveSMSSuccssAcknowledgment(imsSmsImpl.mPhoneId, token, messageId, reasonCode, retryAfter, response);
                return;
            }
            throw new RuntimeException("Sms not ready.");
        }

        public void onReceiveSMSDeliveryReportAck(int messageId, int reasonCode, int retryAfter) throws RemoteException {
            Mno mno = SimUtil.getSimMno(ImsSmsImpl.this.mPhoneId);
            ImsSmsImpl.this.mSmsLogger.logAndAdd(ImsSmsImpl.this.LOG_TAG, "< SMS_ACK : mno " + mno + " messageId " + messageId + " reasonCode " + reasonCode + " retryAfter " + retryAfter);
            if (ImsSmsImpl.this.mSmsListener == null) {
                throw new RuntimeException("Sms not ready.");
            } else if (mno != Mno.KDDI || retryAfter == -1 || ImsSmsImpl.this.mSentDeliveryAck == null || ImsSmsImpl.this.mSentDeliveryAck.mRetryCount >= 4) {
                ImsSmsImpl.this.mSmsListener.onReceiveSmsDeliveryReportAck(messageId, reasonCode);
            } else {
                ImsSmsImpl.this.mHandler.sendMessageDelayed(ImsSmsImpl.this.mHandler.obtainMessage(4, ImsSmsImpl.this.mSentDeliveryAck), (long) (retryAfter * 1000));
                ImsSmsImpl.this.mSentDeliveryAck.mRetryCount++;
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSendDelayedMessage() {
        if (this.mImsSmsTrackers.size() > 0) {
            Iterator<Map.Entry<Integer, ImsSmsTracker>> iter = this.mImsSmsTrackers.entrySet().iterator();
            if (iter.hasNext()) {
                int token = iter.next().getValue().getToken();
                ImsSmsTracker imsSmsTracker = this.mImsSmsTrackers.remove(Integer.valueOf(token));
                if (imsSmsTracker != null && !imsSmsTracker.mSentComplete) {
                    this.mImsSmsTrackers.put(Integer.valueOf(token), imsSmsTracker);
                    sendSmsOverIms(imsSmsTracker, true);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSmsRetry(ImsSmsTracker imsSmsTracker) {
        Handler handler;
        int token = imsSmsTracker.mToken;
        try {
            if (!this.mImsSmsTrackers.containsKey(Integer.valueOf(token))) {
                this.mImsSmsTrackers.put(Integer.valueOf(token), imsSmsTracker);
            }
            if (imsSmsTracker.mContentType.equals("application/vnd.3gpp.sms")) {
                setTPRDintoTPDU(imsSmsTracker.mPdu);
            }
            sendSmsOverIms(imsSmsTracker, false);
            handler = this.mHandler;
            if (handler == null) {
                return;
            }
        } catch (Throwable th) {
            Handler handler2 = this.mHandler;
            if (handler2 != null) {
                handler2.sendMessageDelayed(handler2.obtainMessage(2, imsSmsTracker), 130000);
            }
            throw th;
        }
        handler.sendMessageDelayed(handler.obtainMessage(2, imsSmsTracker), 130000);
    }

    /* access modifiers changed from: private */
    public void handleNoResponseTimeout(ImsSmsTracker imsSmsTracker) {
        int statusError;
        int token = imsSmsTracker.mToken;
        int messageId = imsSmsTracker.mMessageId;
        try {
            if (this.mImsSmsTrackers.containsKey(Integer.valueOf(token))) {
                if (canFallbackForTimeout()) {
                    statusError = 4;
                } else {
                    statusError = 2;
                }
                if ("application/vnd.3gpp.sms".equals(imsSmsTracker.mContentType)) {
                    this.mSmsListener.onSendSmsResult(token, messageId, statusError, 1, -1);
                    this.mSmsLogger.logAndAdd(this.LOG_TAG, "handleNoResponseTimeout: onSendSmsResult token = " + token + " messageId = " + messageId + " reason = timeOut");
                } else {
                    this.mSmsListener.onSendSmsResponse(token, messageId, statusError, 1, 31, 2);
                    this.mSmsLogger.logAndAdd(this.LOG_TAG, "handleNoResponseTimeout: onSendSmsResponse token = " + token + " messageId = " + messageId + " reason = timeOut");
                }
                this.mImsSmsTrackers.remove(Integer.valueOf(token));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void handleRetryDeliveryReportAck(LastSentDeliveryAck sentDeliveryAck) {
        if (sentDeliveryAck == null) {
            Log.e(this.LOG_TAG, "sentDeliveryAck is null");
        } else if (sentDeliveryAck.mNetworkType == 2) {
            this.mSmsServiceModule.sendDeliverReport(this.mPhoneId, sentDeliveryAck.mPdu);
        }
    }

    /* access modifiers changed from: private */
    public void handleStatusReport(int messageRef, int messageId, String format, byte[] pdu) throws RemoteException {
        String str = this.LOG_TAG;
        Log.d(str, "handleStatusReport messageRef = " + messageRef + " mDeliveryPendingList.size() = " + this.mDeliveryPendingList.size());
        boolean statusReportMatched = false;
        int i = 0;
        int count = this.mDeliveryPendingList.size();
        while (true) {
            if (i >= count) {
                break;
            }
            ImsSmsTracker tracker = this.mDeliveryPendingList.get(i);
            if (tracker.mMessageId == messageRef) {
                statusReportMatched = true;
                this.mLastRxStatusMsgId = messageId;
                this.mSmsListener.onSmsStatusReportReceived(tracker.mToken, format, pdu);
                this.mDeliveryPendingList.remove(i);
                break;
            }
            i++;
        }
        if (!statusReportMatched) {
            Log.d(this.LOG_TAG, "statusReport is not matched. But, the messageId is forcibly saved.");
            this.mLastRxStatusMsgId = messageId;
            this.mSmsListener.onSmsStatusReportReceived(0, format, pdu);
        }
    }

    /* access modifiers changed from: private */
    public int getTokenByMessageId(int messageId) {
        for (Map.Entry<Integer, ImsSmsTracker> entry : this.mImsSmsTrackers.entrySet()) {
            ImsSmsTracker value = entry.getValue();
            if (messageId == value.getMessageId()) {
                return value.getToken();
            }
        }
        return -1;
    }

    public void setRetryCount(int phoneId, int token, int retryCount) {
        this.mLastRetryCount = retryCount;
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x0165  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01a4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendSms(int r31, int r32, int r33, java.lang.String r34, java.lang.String r35, byte[] r36) throws android.os.RemoteException {
        /*
            r30 = this;
            r9 = r30
            r15 = r32
            r14 = r33
            r13 = r34
            r11 = r36
            com.sec.internal.constants.ims.servicemodules.sms.SmsMessage r0 = new com.sec.internal.constants.ims.servicemodules.sms.SmsMessage
            r0.<init>()
            r10 = r0
            r22 = 0
            r1 = 0
            r2 = 0
            r3 = 0
            r4 = 0
            int r12 = r9.mLastRetryCount
            java.lang.String r8 = "3gpp"
            boolean r0 = r8.equals(r13)
            if (r0 == 0) goto L_0x0023
            java.lang.String r0 = "application/vnd.3gpp.sms"
            goto L_0x0025
        L_0x0023:
            java.lang.String r0 = "application/vnd.3gpp2.sms"
        L_0x0025:
            r6 = r0
            boolean r0 = r8.equals(r13)     // Catch:{ RuntimeException -> 0x013b }
            if (r0 == 0) goto L_0x0080
            r0 = 2
            r9.mCurrentNetworkType = r0     // Catch:{ RuntimeException -> 0x0076 }
            byte[] r0 = com.android.internal.telephony.uicc.IccUtils.hexStringToBytes(r35)     // Catch:{ RuntimeException -> 0x0076 }
            int r5 = r0.length     // Catch:{ RuntimeException -> 0x0076 }
            int r7 = r11.length     // Catch:{ RuntimeException -> 0x0076 }
            int r5 = r5 + r7
            byte[] r5 = new byte[r5]     // Catch:{ RuntimeException -> 0x0076 }
            r2 = r5
            int r5 = r0.length     // Catch:{ RuntimeException -> 0x0076 }
            r7 = 0
            java.lang.System.arraycopy(r0, r7, r2, r7, r5)     // Catch:{ RuntimeException -> 0x0076 }
            int r5 = r0.length     // Catch:{ RuntimeException -> 0x0076 }
            int r7 = r11.length     // Catch:{ RuntimeException -> 0x0076 }
            r18 = r0
            r0 = 0
            java.lang.System.arraycopy(r11, r0, r2, r5, r7)     // Catch:{ RuntimeException -> 0x0076 }
            r10.parseSubmitPdu(r2, r13)     // Catch:{ RuntimeException -> 0x0076 }
            java.lang.String r5 = r10.getDestinationAddress()     // Catch:{ RuntimeException -> 0x0076 }
            r1 = r5
            boolean r5 = r10.getStatusReportRequested()     // Catch:{ RuntimeException -> 0x0076 }
            r3 = r5
            boolean r5 = r9.isTPRDset(r2)     // Catch:{ RuntimeException -> 0x0076 }
            r7 = 1
            if (r5 != r7) goto L_0x0065
            byte r5 = r9.getTPMR(r2)     // Catch:{ RuntimeException -> 0x00ae }
            r5 = r5 & 255(0xff, float:3.57E-43)
            r9.mTpmr = r5     // Catch:{ RuntimeException -> 0x00ae }
            r7 = r31
            goto L_0x006a
        L_0x0065:
            r7 = r31
            r9.setTPMRintoTPDU(r2, r7)     // Catch:{ RuntimeException -> 0x00ae }
        L_0x006a:
            int r5 = r9.mTpmr     // Catch:{ RuntimeException -> 0x00ae }
            r4 = r5
            r23 = r1
            r24 = r2
            r25 = r3
            r26 = r4
            goto L_0x00b8
        L_0x0076:
            r0 = move-exception
            r7 = r31
        L_0x0079:
            r27 = r8
            r7 = r10
            r5 = r12
            r8 = r13
            goto L_0x0141
        L_0x0080:
            r7 = r31
            r0 = 0
            java.lang.String r5 = "3gpp2"
            boolean r5 = r5.equals(r13)     // Catch:{ RuntimeException -> 0x013b }
            if (r5 == 0) goto L_0x00b0
            r5 = 1
            r9.mCurrentNetworkType = r5     // Catch:{ RuntimeException -> 0x00ae }
            r10.parseSubmitPdu(r11, r13)     // Catch:{ RuntimeException -> 0x00ae }
            int r5 = r10.getMsgID()     // Catch:{ RuntimeException -> 0x00ae }
            r4 = r5
            byte[] r5 = r10.getTpdu()     // Catch:{ RuntimeException -> 0x00ae }
            r2 = r5
            java.lang.String r5 = r10.getDestinationAddress()     // Catch:{ RuntimeException -> 0x00ae }
            r1 = r5
            boolean r5 = r10.getStatusReportRequested()     // Catch:{ RuntimeException -> 0x00ae }
            r3 = r5
            r23 = r1
            r24 = r2
            r25 = r3
            r26 = r4
            goto L_0x00b8
        L_0x00ae:
            r0 = move-exception
            goto L_0x0079
        L_0x00b0:
            r23 = r1
            r24 = r2
            r25 = r3
            r26 = r4
        L_0x00b8:
            r1 = r30
            r2 = r32
            r3 = r26
            r4 = r23
            r5 = r24
            r7 = r12
            r27 = r8
            r8 = r25
            java.util.HashMap r1 = r1.getImsSmsTrackerMap(r2, r3, r4, r5, r6, r7, r8)     // Catch:{ RuntimeException -> 0x012e }
            r5 = r12
            r12 = r1
            com.sec.internal.google.ImsSmsImpl$ImsSmsTracker r1 = new com.sec.internal.google.ImsSmsImpl$ImsSmsTracker     // Catch:{ RuntimeException -> 0x0122 }
            r20 = 0
            r21 = 0
            r7 = r10
            r10 = r1
            r11 = r31
            r8 = r13
            r13 = r32
            r2 = r14
            r14 = r5
            r3 = r15
            r15 = r26
            r16 = r24
            r17 = r23
            r18 = r6
            r19 = r25
            r10.<init>(r11, r12, r13, r14, r15, r16, r17, r18, r19, r20)     // Catch:{ RuntimeException -> 0x0116 }
            java.util.Map<java.lang.Integer, com.sec.internal.google.ImsSmsImpl$ImsSmsTracker> r4 = r9.mImsSmsTrackers     // Catch:{ RuntimeException -> 0x0108 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r32)     // Catch:{ RuntimeException -> 0x0108 }
            boolean r4 = r4.containsKey(r10)     // Catch:{ RuntimeException -> 0x0108 }
            if (r4 != 0) goto L_0x00ff
            java.util.Map<java.lang.Integer, com.sec.internal.google.ImsSmsImpl$ImsSmsTracker> r4 = r9.mImsSmsTrackers     // Catch:{ RuntimeException -> 0x0108 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r32)     // Catch:{ RuntimeException -> 0x0108 }
            r4.put(r10, r1)     // Catch:{ RuntimeException -> 0x0108 }
        L_0x00ff:
            r9.sendSmsOverIms(r1, r0)     // Catch:{ RuntimeException -> 0x0108 }
            r22 = r1
            r13 = r2
            r1 = r3
            goto L_0x01f4
        L_0x0108:
            r0 = move-exception
            r22 = r1
            r14 = r2
            r15 = r3
            r1 = r23
            r2 = r24
            r3 = r25
            r4 = r26
            goto L_0x0141
        L_0x0116:
            r0 = move-exception
            r14 = r2
            r15 = r3
            r1 = r23
            r2 = r24
            r3 = r25
            r4 = r26
            goto L_0x0141
        L_0x0122:
            r0 = move-exception
            r7 = r10
            r8 = r13
            r1 = r23
            r2 = r24
            r3 = r25
            r4 = r26
            goto L_0x0141
        L_0x012e:
            r0 = move-exception
            r7 = r10
            r5 = r12
            r8 = r13
            r1 = r23
            r2 = r24
            r3 = r25
            r4 = r26
            goto L_0x0141
        L_0x013b:
            r0 = move-exception
            r27 = r8
            r7 = r10
            r5 = r12
            r8 = r13
        L_0x0141:
            java.lang.String r10 = r9.LOG_TAG
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "Can not send sms: "
            r11.append(r12)
            java.lang.String r12 = r0.getMessage()
            r11.append(r12)
            java.lang.String r11 = r11.toString()
            android.util.Log.e(r10, r11)
            r10 = r27
            boolean r10 = r8.equals(r10)
            java.lang.String r13 = " messageId = "
            if (r10 == 0) goto L_0x01a4
            android.telephony.ims.aidl.IImsSmsListener r10 = r9.mSmsListener
            r16 = 2
            r17 = 1
            r18 = 2
            r11 = r32
            r12 = r33
            r28 = r13
            r13 = r16
            r14 = r17
            r17 = r1
            r1 = r15
            r15 = r18
            r10.onSendSmsResult(r11, r12, r13, r14, r15)
            com.sec.internal.ims.servicemodules.sms.SmsLogger r10 = r9.mSmsLogger
            java.lang.String r11 = r9.LOG_TAG
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "onSendSmsResult token = "
            r12.append(r13)
            r12.append(r1)
            r15 = r28
            r12.append(r15)
            r14 = r33
            r12.append(r14)
            java.lang.String r12 = r12.toString()
            r10.logAndAdd(r11, r12)
            r13 = r14
            goto L_0x01e3
        L_0x01a4:
            r17 = r1
            r1 = r15
            r15 = r13
            android.telephony.ims.aidl.IImsSmsListener r10 = r9.mSmsListener
            r13 = 2
            r16 = 1
            r18 = 31
            r19 = 2
            r11 = r32
            r12 = r33
            r14 = r16
            r29 = r15
            r15 = r18
            r16 = r19
            r10.onSendSmsResponse(r11, r12, r13, r14, r15, r16)
            com.sec.internal.ims.servicemodules.sms.SmsLogger r10 = r9.mSmsLogger
            java.lang.String r11 = r9.LOG_TAG
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "onSendSmsResponse token = "
            r12.append(r13)
            r12.append(r1)
            r13 = r29
            r12.append(r13)
            r13 = r33
            r12.append(r13)
            java.lang.String r12 = r12.toString()
            r10.logAndAdd(r11, r12)
        L_0x01e3:
            java.util.Map<java.lang.Integer, com.sec.internal.google.ImsSmsImpl$ImsSmsTracker> r10 = r9.mImsSmsTrackers
            java.lang.Integer r11 = java.lang.Integer.valueOf(r32)
            r10.remove(r11)
            r24 = r2
            r25 = r3
            r26 = r4
            r23 = r17
        L_0x01f4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.google.ImsSmsImpl.sendSms(int, int, int, java.lang.String, java.lang.String, byte[]):void");
    }

    private void sendSmsOverIms(ImsSmsTracker imsSmsTracker, boolean delayedMsg) {
        HashMap<String, Object> map = imsSmsTracker.getData();
        byte[] pdu = (byte[]) map.get(MAP_KEY_PDU);
        String destAddr = (String) map.get(MAP_KEY_DEST_ADDR);
        String contentType = (String) map.get(MAP_KEY_CONTENT_TYPE);
        int messageId = ((Integer) map.get("messageId")).intValue();
        if (!delayedMsg && this.mImsSmsTrackers.size() <= 1) {
            this.mSmsServiceModule.sendSMSOverIMS(imsSmsTracker.mPhoneId, pdu, destAddr, contentType, messageId, false);
        }
        if (delayedMsg) {
            this.mSmsServiceModule.sendSMSOverIMS(imsSmsTracker.mPhoneId, pdu, destAddr, contentType, messageId, false);
        }
        SmsLogger smsLogger = this.mSmsLogger;
        String str = this.LOG_TAG;
        smsLogger.logAndAdd(str, "> SEND_SMS : token = " + imsSmsTracker.mToken + " " + imsSmsTracker.mContentType + " destAddr = " + IMSLog.checker(destAddr) + " messageId = " + messageId + " statusReportRequested = " + imsSmsTracker.mStatusReportRequested);
        if (!TelephonyFeatures.SHIP_BUILD) {
            String str2 = this.LOG_TAG;
            Log.d(str2, "pdu = " + IccUtils.bytesToHexString(pdu));
        }
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendMessageDelayed(handler.obtainMessage(2, imsSmsTracker), 130000);
        }
    }

    public void sendRpSmma(int phoneId, String smsc) {
        try {
            this.mSmsServiceModule.sendSMSOverIMS(phoneId, (byte[]) null, smsc, "application/vnd.3gpp.sms", Type.CAA, true);
            Log.i(this.LOG_TAG, "sendRpSmma");
        } catch (RuntimeException e) {
            String str = this.LOG_TAG;
            Log.e(str, "Can not send RP Smma: " + e.getMessage());
        }
    }

    public void acknowledgeSms(int phoneId, int token, int messageRef, int result) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "acknowledgeSms");
        byte[] pdu = new byte[4];
        int errorCause = 0;
        if (this.mCurrentNetworkType == 2) {
            if (result == 1) {
                int index = 0 + 1;
                pdu[0] = 0;
                int index2 = index + 1;
                pdu[index] = 0;
                int index3 = index2 + 1;
                pdu[index2] = (byte) messageRef;
                int i = index3 + 1;
                pdu[index3] = 0;
            } else {
                errorCause = resultToCause(result);
                int index4 = 0 + 1;
                pdu[0] = (byte) ((errorCause >> 8) & 255);
                int index5 = index4 + 1;
                pdu[index4] = (byte) (errorCause & 255);
                int index6 = index5 + 1;
                pdu[index5] = (byte) messageRef;
                int i2 = index6 + 1;
                pdu[index6] = 0;
            }
            this.mSmsServiceModule.sendDeliverReport(phoneId, pdu);
            if (this.mSentDeliveryAck != null) {
                this.mSentDeliveryAck = null;
            }
            this.mSentDeliveryAck = new LastSentDeliveryAck(pdu, errorCause, 2);
            SmsLogger smsLogger = this.mSmsLogger;
            String str = this.LOG_TAG;
            smsLogger.logAndAdd(str, "> SMS_ACK : messageRef = " + messageRef);
        }
    }

    public void acknowledgeSmsWithPdu(int phoneId, int token, int messageRef, byte[] data) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "acknowledgeSms");
        byte[] smsAckPdu = new byte[(data.length + 4)];
        if (this.mCurrentNetworkType == 2) {
            int index = 0 + 1;
            smsAckPdu[0] = (byte) ((0 >> 8) & 255);
            int index2 = index + 1;
            smsAckPdu[index] = (byte) (0 & 255);
            int index3 = index2 + 1;
            smsAckPdu[index2] = (byte) messageRef;
            smsAckPdu[index3] = (byte) data.length;
            System.arraycopy(data, 0, smsAckPdu, index3 + 1, data.length);
            this.mSmsServiceModule.sendDeliverReport(phoneId, smsAckPdu);
            SmsLogger smsLogger = this.mSmsLogger;
            String str = this.LOG_TAG;
            smsLogger.logAndAdd(str, "> SMS_ACK_WITH_PDU : messageRef = " + messageRef);
        }
    }

    public void acknowledgeSmsReport(int phoneId, int token, int messageRef, int result) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "acknowledgeSmsReport");
        acknowledgeSms(phoneId, token, this.mLastRxStatusMsgId, result);
    }

    public String getSmsFormat(int phoneId) throws RemoteException {
        String smsFormat = "3GPP";
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri.Builder buildUpon = Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/SMS_FORMAT").buildUpon();
        Cursor cursor = contentResolver.query(buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + phoneId).build(), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    smsFormat = cursor.getString(1);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if ("3GPP2".equals(smsFormat)) {
            return SmsMessage.FORMAT_3GPP2;
        }
        return SmsMessage.FORMAT_3GPP;
        throw th;
    }

    private void setTPMRintoTPDU(byte[] pdu, int phoneId) {
        byte scaLen;
        if (pdu != null && pdu.length > 0 && (scaLen = pdu[0]) > 0 && pdu.length > scaLen + 2 && (pdu[scaLen + 1] & 1) == 1) {
            if (this.mTpmr == -1) {
                updateTPMR(phoneId);
            }
            int i = this.mTpmr & 255;
            this.mTpmr = i;
            if (i >= 255) {
                this.mTpmr = 0;
            } else {
                this.mTpmr = i + 1;
            }
            setTelephonyProperty(phoneId, "persist.radio.tpmr_sms", String.valueOf(this.mTpmr));
            pdu[scaLen + 2] = (byte) this.mTpmr;
            String str = this.LOG_TAG;
            Log.d(str, "setTPMRintoTPDU mTpmr : " + this.mTpmr);
        }
    }

    public void updateTPMR(int phoneId) {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        String tpmr = TelephonyManager.getTelephonyProperty(phoneId, "persist.radio.tpmr_sms", "0");
        if (tpmr == null || tpmr.isEmpty()) {
            this.mTpmr = 0;
        } else {
            this.mTpmr = Integer.parseInt(tpmr) & 255;
        }
    }

    private void setTelephonyProperty(int phoneId, String property, String value) {
        StringBuffer propBuf = new StringBuffer("");
        String[] p = null;
        String prop = SystemProperties.get(property);
        if (value == null) {
            value = "";
        }
        String value2 = value.replace(',', ' ');
        if (prop != null) {
            p = prop.split(",");
        }
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            for (int i = 0; i < phoneId; i++) {
                String str = "";
                if (p != null && i < p.length) {
                    str = p[i];
                }
                propBuf.append(str);
                propBuf.append(",");
            }
            propBuf.append(value2);
            if (p != null) {
                for (int i2 = phoneId + 1; i2 < p.length; i2++) {
                    propBuf.append(",");
                    propBuf.append(p[i2]);
                }
            }
            String propVal = propBuf.toString();
            int propValLen = propVal.length();
            try {
                propValLen = propVal.getBytes("utf-8").length;
            } catch (UnsupportedEncodingException e) {
                Log.e(this.LOG_TAG, "setTelephonyProperty: utf-8 not supported");
            }
            if (propValLen > 91) {
                String str2 = this.LOG_TAG;
                Log.e(str2, "setTelephonyProperty: property too long phoneId=" + phoneId + " property=" + property + " value: " + value2 + " propVal=" + propVal);
                return;
            }
            SystemProperties.set(property, propVal);
        }
    }

    private void setTPRDintoTPDU(byte[] pdu) {
        byte scaLen;
        if (pdu != null && pdu.length > 0 && (scaLen = pdu[0]) > 0 && pdu.length > scaLen + 1 && (pdu[scaLen + 1] & 1) == 1) {
            pdu[scaLen + 1] = (byte) (pdu[scaLen + 1] | 4);
        }
    }

    private boolean isTPRDset(byte[] pdu) {
        if (pdu == null || pdu.length <= 0) {
            return false;
        }
        byte scaLen = pdu[0];
        if (scaLen <= 0 || pdu.length <= scaLen + 1 || (pdu[scaLen + 1] & 1) != 1) {
            Log.e(this.LOG_TAG, "isTPRDset() sca is wrong: return false");
            return false;
        } else if ((pdu[scaLen + 1] & 4) == 4) {
            return true;
        } else {
            return false;
        }
    }

    private byte getTPMR(byte[] pdu) {
        byte scaLen;
        if (pdu == null || pdu.length <= 0 || (scaLen = pdu[0]) <= 0 || pdu.length <= scaLen + 2 || (pdu[scaLen + 1] & 1) != 1) {
            return 0;
        }
        return pdu[scaLen + 2];
    }

    private boolean getSmsFallback() {
        ISmsServiceModule iSmsServiceModule = this.mSmsServiceModule;
        if (iSmsServiceModule == null) {
            return false;
        }
        return iSmsServiceModule.getSmsFallback(this.mPhoneId);
    }

    private int resultToCause(int result) {
        if (result == 1) {
            return 0;
        }
        if (result == 3) {
            return 22;
        }
        if (result != 4) {
            return 41;
        }
        return 111;
    }

    /* access modifiers changed from: private */
    public void onReceiveSMSSuccssAcknowledgment(int phoneId, int token, int messageId, int nReasonCode, int nRetryAfter, SmsResponse response) throws RemoteException {
        int i = messageId;
        int i2 = nReasonCode;
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        String str = this.LOG_TAG;
        Log.d(str, "onReceiveSMSAck: mno = " + mno.getName() + " messageId = " + i + " reasonCode = " + i2 + " retryAfter = " + nRetryAfter);
        boolean isCdmaContentType = response.getContentType() == 1;
        ImsSmsTracker imsSmsTracker = this.mImsSmsTrackers.remove(Integer.valueOf(token));
        if (imsSmsTracker != null) {
            imsSmsTracker.mSentComplete = true;
            if (imsSmsTracker.mStatusReportRequested && !mno.isKor()) {
                this.mDeliveryPendingList.add(imsSmsTracker);
            }
            if (this.mHandler != null && this.mImsSmsTrackers.size() > 0) {
                Handler handler = this.mHandler;
                handler.sendMessage(handler.obtainMessage(3));
            }
        }
        response.setMessageRef(i);
        if (10000 < i2 && i2 < 11000) {
            handleInternalError(token, messageId, nReasonCode, response, isCdmaContentType);
        } else if (32768 >= i2 || i2 >= RIL_CODE_RP_ERROR_END) {
            handleAck(mno, token, messageId, nReasonCode, response, imsSmsTracker, isCdmaContentType, nRetryAfter);
        } else {
            ImsSmsTracker imsSmsTracker2 = imsSmsTracker;
            handleRPError(mno, token, messageId, nReasonCode, response, phoneId);
        }
    }

    private void handleAck(Mno mno, int token, int messageId, int nReasonCode, SmsResponse response, ImsSmsTracker imsSmsTracker, boolean isCdmaContentType, int nRetryAfter) throws RemoteException {
        Mno mno2 = mno;
        int i = token;
        int i2 = messageId;
        int i3 = nReasonCode;
        SmsResponse smsResponse = response;
        if (mno2 == Mno.VZW) {
            handleVzwAck(token, messageId, nReasonCode, response, imsSmsTracker, isCdmaContentType);
        } else if (mno2 == Mno.SPRINT) {
            handleSprAck(token, i2, i3, smsResponse);
        } else if (mno2 == Mno.BELL) {
            handleBellAck(token, i2, i3, smsResponse);
        } else if (mno2 == Mno.UPC_CH) {
            handleUpcChAck(token, i2, i3, smsResponse);
        } else if (mno2 == Mno.CTC) {
            handleCTCAck(token, messageId, nReasonCode, response, imsSmsTracker);
        } else if (mno2 == Mno.SWISSCOM) {
            handleSwisscomAck(token, messageId, nReasonCode, response, imsSmsTracker);
        } else if (mno2 == Mno.DOCOMO) {
            handleDocomoAck(token, messageId, nReasonCode, response, imsSmsTracker, nRetryAfter);
        } else if (mno2 == Mno.SOFTBANK) {
            handleSbmAck(token, i2, i3, smsResponse);
        } else {
            if (mno.isOneOf(Mno.KDDI, Mno.RAKUTEN_JAPAN)) {
                handleKddiRakutenAck(token, messageId, nReasonCode, response, imsSmsTracker, nRetryAfter);
                return;
            }
            if (mno.isOneOf(Mno.ORANGE, Mno.ORANGE_POLAND, Mno.ORANGE_SPAIN, Mno.ORANGE_ROMANIA, Mno.ORANGE_SLOVAKIA)) {
                handleOrangeAck(token, i2, i3, smsResponse);
                return;
            }
            if (mno.isOneOf(Mno.CMCC, Mno.CU, Mno.CMHK)) {
                handleCmccCuCmhkAck(token, i2, i3, smsResponse);
            } else if (i3 == 0 || !getSmsFallback()) {
                handleResult(token, messageId, nReasonCode, 1, response);
            } else {
                smsResponse.setErrorClass(0);
                smsResponse.setErrorCause(19);
                handleResult(token, messageId, nReasonCode, 4, response);
            }
        }
    }

    private void handleInternalError(int token, int messageId, int nReasonCode, SmsResponse response, boolean isCdmaContentType) throws RemoteException {
        int status = 2;
        if (nReasonCode != 10001) {
            if (nReasonCode != 10002) {
                if (nReasonCode == 10004) {
                    response.setErrorClass(0);
                    response.setErrorCause(19);
                    status = 4;
                } else if (isCdmaContentType) {
                    response.setErrorClass(3);
                    response.setErrorCause(107);
                } else {
                    response.setErrorClass(0);
                    response.setErrorCause(9);
                }
            } else if (isCdmaContentType) {
                response.setErrorClass(9);
            } else {
                response.setErrorClass(0);
                response.setErrorCause(19);
            }
        } else if (isCdmaContentType) {
            response.setErrorClass(3);
            response.setErrorCause(105);
        } else {
            response.setErrorClass(0);
            response.setErrorCause(4);
        }
        handleResult(token, messageId, nReasonCode, status, response);
    }

    private void handleRPError(Mno mno, int token, int messageId, int nReasonCode, SmsResponse response, int phoneId) throws RemoteException {
        int tpCause;
        int status;
        int status2;
        Mno mno2 = mno;
        SmsResponse smsResponse = response;
        byte[] pdu = response.getTpdu();
        if (pdu.length > 3) {
            tpCause = pdu[3];
        } else {
            tpCause = 0;
        }
        int rpCause = nReasonCode - 32768;
        if ((mno2.isOneOf(Mno.ORANGE, Mno.ORANGE_POLAND, Mno.ORANGE_SPAIN, Mno.ORANGE_ROMANIA, Mno.ORANGE_SLOVAKIA) && rpCause == 38) || rpCause == 41 || rpCause == 42 || rpCause == 69) {
            TelephonyManager mTelephonyMgr = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
            int subId = -1;
            int[] subIdList = SubscriptionManager.getSubId(phoneId);
            if (subIdList != null) {
                subId = subIdList[0];
            }
            if (!mTelephonyMgr.isNetworkRoaming(subId) || mTelephonyMgr.getDataNetworkType(subId) != 18) {
                smsResponse.setErrorClass(0);
                smsResponse.setErrorCause(19);
                Log.d(this.LOG_TAG, "orange, set errorcause as fallbackIMS due to RP# " + rpCause);
                status2 = 4;
            } else {
                Log.d(this.LOG_TAG, "orange, RP# " + rpCause + ", isRoaming is true and DataNetworkType is IWLAN, so CS fallback does not done");
                status2 = 1;
            }
            status = status2;
        } else if (isErrorForSpecificCarrier(mno2, tpCause, rpCause)) {
            status = 2;
        } else if (mno2 == Mno.DOCOMO && rpCause == 21 && tpCause == 197) {
            status = 1;
        } else if (getSmsFallback()) {
            smsResponse.setErrorClass(0);
            smsResponse.setErrorCause(19);
            status = 4;
        } else if (rpCause == 42 || rpCause == 111 || rpCause == 47 || rpCause == 27 || rpCause == 41 || rpCause == 98) {
            status = 3;
        } else {
            status = 2;
        }
        Log.d(this.LOG_TAG, "handleRPError: rpCause= " + rpCause + ", tpCause= " + tpCause + ", status= " + status);
        handleResult(token, messageId, nReasonCode, status, response);
    }

    private boolean isErrorForSpecificCarrier(Mno mno, int tpCause, int rpCause) {
        if (mno == Mno.BELL && (tpCause == 195 || rpCause == 111 || rpCause == 30 || rpCause == 28)) {
            return true;
        }
        if (mno != Mno.KT) {
            return false;
        }
        if (rpCause == 41 || rpCause == 42 || rpCause == 47 || rpCause == 98 || rpCause == 111) {
            return true;
        }
        return false;
    }

    private void handleVzwAck(int token, int messageId, int nReasonCode, SmsResponse response, ImsSmsTracker imsSmsTracker, boolean isCdmaContentType) throws RemoteException {
        int i = nReasonCode;
        SmsResponse smsResponse = response;
        ImsSmsTracker imsSmsTracker2 = imsSmsTracker;
        if (i >= 400 && i <= 599 && imsSmsTracker2 != null) {
            Log.d(this.LOG_TAG, "imsSmsTracker.mRetryCount =  " + imsSmsTracker2.mRetryCount);
            if (imsSmsTracker2.mRetryCount < 1) {
                imsSmsTracker2.mRetryCount++;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker), 30000);
            } else if (isCdmaContentType) {
                response.setErrorClass(9);
                handleResult(token, messageId, nReasonCode, 2, response);
            } else {
                response.setErrorClass(0);
                response.setErrorCause(19);
                handleResult(token, messageId, nReasonCode, 4, response);
            }
        } else if (i != 777 && i != 800) {
            handleResult(token, messageId, nReasonCode, 1, response);
        } else if (isCdmaContentType) {
            response.setErrorClass(9);
            handleResult(token, messageId, nReasonCode, 2, response);
        } else {
            response.setErrorClass(0);
            response.setErrorCause(19);
            handleResult(token, messageId, nReasonCode, 4, response);
        }
    }

    private void handleKddiRakutenAck(int token, int messageId, int nReasonCode, SmsResponse response, ImsSmsTracker imsSmsTracker, int nRetryAfter) throws RemoteException {
        int i = nReasonCode;
        SmsResponse smsResponse = response;
        ImsSmsTracker imsSmsTracker2 = imsSmsTracker;
        int i2 = nRetryAfter;
        if (i2 == -1) {
            if (i != 0) {
                response.setErrorClass(9);
                handleResult(token, messageId, nReasonCode, 2, response);
                return;
            }
            handleResult(token, messageId, nReasonCode, 1, response);
        } else if (i == 403 || i == 404 || i == 408 || i == 500 || i == 503 || i == 504 || i < 100 || i > 699 || imsSmsTracker2 == null) {
            handleResult(token, messageId, nReasonCode, 1, response);
        } else if (imsSmsTracker2.mRetryCount < 4) {
            imsSmsTracker2.mRetryCount++;
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker2), (long) (i2 * 1000));
        } else {
            response.setErrorClass(9);
            handleResult(token, messageId, nReasonCode, 2, response);
        }
    }

    private void handleDocomoAck(int token, int messageId, int nReasonCode, SmsResponse response, ImsSmsTracker imsSmsTracker, int nRetryAfter) throws RemoteException {
        int nRetryAfter2;
        int nRetryAfter3;
        int i = nReasonCode;
        ImsSmsTracker imsSmsTracker2 = imsSmsTracker;
        if (i == 504) {
            nRetryAfter3 = nRetryAfter;
            if (nRetryAfter3 == -1) {
                nRetryAfter2 = 5;
                if (i == 408 && i != 504) {
                    SmsResponse smsResponse = response;
                } else if (nRetryAfter2 != -1 || imsSmsTracker2 == null) {
                    SmsResponse smsResponse2 = response;
                } else if (imsSmsTracker2.mRetryCount < 1) {
                    imsSmsTracker2.mRetryCount++;
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker2), (long) (nRetryAfter2 * 1000));
                    SmsResponse smsResponse3 = response;
                    return;
                } else {
                    SmsResponse smsResponse4 = response;
                    response.setErrorClass(9);
                    handleResult(token, messageId, nReasonCode, 2, response);
                    return;
                }
                handleResult(token, messageId, nReasonCode, 1, response);
            }
        } else {
            nRetryAfter3 = nRetryAfter;
        }
        if (i == 999) {
            Log.e(this.LOG_TAG, "Waiting SMS resend timer. 999 error ignore!");
            return;
        }
        nRetryAfter2 = nRetryAfter3;
        if (i == 408) {
        }
        if (nRetryAfter2 != -1) {
        }
        SmsResponse smsResponse22 = response;
        handleResult(token, messageId, nReasonCode, 1, response);
    }

    private void handleSbmAck(int token, int messageId, int nReasonCode, SmsResponse response) throws RemoteException {
        if (nReasonCode == 0) {
            handleResult(token, messageId, nReasonCode, 1, response);
        } else if (nReasonCode == 415) {
            response.setErrorClass(0);
            response.setErrorCause(19);
            handleResult(token, messageId, nReasonCode, 4, response);
        } else {
            response.setErrorClass(9);
            handleResult(token, messageId, nReasonCode, 2, response);
        }
    }

    private void handleSprAck(int token, int messageId, int nReasonCode, SmsResponse response) throws RemoteException {
        if (nReasonCode < 400 || nReasonCode > 699) {
            handleResult(token, messageId, nReasonCode, 1, response);
            return;
        }
        response.setErrorClass(0);
        response.setErrorCause(19);
        handleResult(token, messageId, nReasonCode, 4, response);
    }

    private void handleBellAck(int token, int messageId, int nReasonCode, SmsResponse response) throws RemoteException {
        if (nReasonCode == 500 || nReasonCode == 503 || nReasonCode == 504 || nReasonCode == 408) {
            response.setErrorClass(0);
            response.setErrorCause(19);
            handleResult(token, messageId, nReasonCode, 4, response);
            return;
        }
        handleResult(token, messageId, nReasonCode, 1, response);
    }

    private void handleOrangeAck(int token, int messageId, int nReasonCode, SmsResponse response) throws RemoteException {
        if (nReasonCode == 403 || nReasonCode == 408 || ((nReasonCode >= 500 && nReasonCode < 600) || nReasonCode == 708)) {
            response.setErrorClass(0);
            response.setErrorCause(19);
            handleResult(token, messageId, nReasonCode, 4, response);
            return;
        }
        handleResult(token, messageId, nReasonCode, 1, response);
    }

    private void handleUpcChAck(int token, int messageId, int nReasonCode, SmsResponse response) throws RemoteException {
        if (nReasonCode == 408 || nReasonCode == 480 || nReasonCode == 503) {
            response.setErrorClass(0);
            response.setErrorCause(19);
            handleResult(token, messageId, nReasonCode, 4, response);
            return;
        }
        handleResult(token, messageId, nReasonCode, 1, response);
    }

    private void handleCmccCuCmhkAck(int token, int messageId, int nReasonCode, SmsResponse response) throws RemoteException {
        if (nReasonCode <= 0 || nReasonCode >= 32768) {
            handleResult(token, messageId, nReasonCode, 1, response);
            return;
        }
        response.setErrorClass(0);
        response.setErrorCause(19);
        handleResult(token, messageId, nReasonCode, 4, response);
    }

    private void handleCTCAck(int token, int messageId, int nReasonCode, SmsResponse response, ImsSmsTracker imsSmsTracker) throws RemoteException {
        if (nReasonCode != 503 || imsSmsTracker == null || imsSmsTracker.mRetryCount >= 1) {
            handleResult(token, messageId, nReasonCode, 1, response);
            return;
        }
        imsSmsTracker.mRetryCount++;
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker), 30000);
    }

    private void handleSwisscomAck(int token, int messageId, int nReasonCode, SmsResponse response, ImsSmsTracker imsSmsTracker) throws RemoteException {
        if ((nReasonCode != 400 && nReasonCode != 403 && nReasonCode != 404 && nReasonCode != 488 && (nReasonCode < 500 || nReasonCode >= 600)) || imsSmsTracker == null) {
            handleResult(token, messageId, nReasonCode, 1, response);
        } else if (imsSmsTracker.mRetryCount < 2) {
            imsSmsTracker.mRetryCount++;
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, imsSmsTracker), 30000);
        } else {
            response.setErrorClass(0);
            response.setErrorCause(19);
            handleResult(token, messageId, nReasonCode, 4, response);
        }
    }

    private void handleResult(int token, int messageId, int reasonCode, int status, SmsResponse response) throws RemoteException {
        if (response.getContentType() == 1) {
            handleCdmaResult(token, messageId, reasonCode, response);
        } else {
            handleGsmResult(token, messageId, status, response);
        }
    }

    private void handleCdmaResult(int token, int messageId, int reasonCode, SmsResponse response) throws RemoteException {
        int errorCause = response.getErrorCause();
        int errorClass = response.getErrorClass();
        int reason = response.getReasonCode();
        if (errorClass != 0) {
            if (errorClass == 9) {
                Log.d(this.LOG_TAG, "Ims failed. Retry to send over 1x");
                if (canFallback(1)) {
                    this.mSmsListener.onSendSmsResponse(token, messageId, 4, reason, errorCause, errorClass);
                } else {
                    this.mSmsListener.onSendSmsResponse(token, messageId, 2, reason, errorCause, errorClass);
                }
            } else if (errorClass == 2) {
                this.mSmsListener.onSendSmsResponse(token, messageId, 3, reason, errorCause, errorClass);
            } else if (errorClass != 3) {
                this.mSmsListener.onSendSmsResponse(token, messageId, 2, reason, errorCause, errorClass);
            } else {
                this.mSmsListener.onSendSmsResponse(token, messageId, 2, reason, errorCause, errorClass);
            }
        } else if (reasonCode == 10004) {
            this.mSmsListener.onSendSmsResponse(token, messageId, 4, 0, errorCause, errorClass);
        } else {
            this.mSmsListener.onSendSmsResponse(token, messageId, 1, 0, errorCause, errorClass);
        }
        SmsLogger smsLogger = this.mSmsLogger;
        String str = this.LOG_TAG;
        smsLogger.logAndAdd(str, "< SEND_SMS_CDMA : token = " + token + " messageId = " + messageId + " reason = " + reason + " errorCause = " + errorCause + " errorClass = " + errorClass);
    }

    private void handleGsmResult(int token, int messageId, int status, SmsResponse response) throws RemoteException {
        int reason = response.getReasonCode();
        if (status != 1) {
            if (status == 3) {
                this.mSmsListener.onSendSmsResult(token, messageId, 3, reason, 2);
            } else if (status != 4) {
                this.mSmsListener.onSendSmsResult(token, messageId, status, reason, 2);
            } else if (canFallback(2)) {
                Log.d(this.LOG_TAG, "Ims failed. Retry SMS Over SGs/CS");
                this.mSmsListener.onSendSmsResult(token, messageId, 4, reason, 1);
            } else {
                this.mSmsListener.onSendSmsResult(token, messageId, 2, reason, 2);
            }
        } else if (response.getErrorClass() == 0) {
            this.mSmsListener.onSendSmsResult(token, messageId, 1, reason, 1);
        } else {
            status = 2;
            this.mSmsListener.onSendSmsResult(token, messageId, 2, reason, 2);
        }
        SmsLogger smsLogger = this.mSmsLogger;
        String str = this.LOG_TAG;
        smsLogger.logAndAdd(str, "< SEND_SMS : token = " + token + " messageId = " + messageId + " reason = " + reason + " status = " + status + " (1:Ok 2:Error 3:Retry 4:Fallback)");
    }

    private boolean canFallback(int smsFormat) {
        try {
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
            Mno mno = SimUtil.getSimMno(this.mPhoneId);
            String simOperator = TelephonyManager.getTelephonyProperty(this.mPhoneId, "gsm.sim.operator.numeric", "00000");
            int iccType = IccUtils.getIccType(this.mPhoneId);
            if (mno == Mno.CMCC && iccType == 2 && (simOperator.equals("46000") || simOperator.equals("46002") || simOperator.equals("46007") || simOperator.equals("46008"))) {
                return true;
            }
            if (mno.isOneOf(Mno.BELL, Mno.SOFTBANK, Mno.SPRINT)) {
                return true;
            }
            if (mno == Mno.VZW) {
                boolean isRoaming = Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(this.mPhoneId, "gsm.operator.isroaming", (String) null));
                if (TelephonyFeatures.getVzwDeviceType(this.mPhoneId) != 3) {
                    if (!isRoaming || tm.getNetworkType() != 13) {
                        if (TelephonyFeatures.getVzwDeviceType(this.mPhoneId) == 2 && smsFormat == 1 && this.mSmsServiceModule.isVolteSupported(this.mPhoneId)) {
                            return false;
                        }
                    }
                }
                Log.d(this.LOG_TAG, "fallback always over NAS (cdmaless / volte roaming)");
                return true;
            } else if (mno == Mno.RJIL) {
                return false;
            }
            String str = this.LOG_TAG;
            Log.d(str, "serviceState.getState() = " + tm.getServiceState().getState());
            if (tm.getServiceState().getState() == 0) {
                return true;
            }
            return false;
        } catch (SecurityException e) {
            Log.e(this.LOG_TAG, "No permission for telephony service");
            return false;
        }
    }

    private boolean canFallbackForTimeout() {
        try {
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
            Mno mno = SimUtil.getSimMno(this.mPhoneId);
            String simOperator = TelephonyManager.getTelephonyProperty(this.mPhoneId, "gsm.sim.operator.numeric", "00000");
            int iccType = IccUtils.getIccType(this.mPhoneId);
            if (mno == Mno.CMCC && iccType == 2 && (simOperator.equals("46000") || simOperator.equals("46002") || simOperator.equals("46007") || simOperator.equals("46008"))) {
                return true;
            }
            if (mno.isOneOf(Mno.BELL, Mno.SPRINT)) {
                return true;
            }
            if (getSmsFallback()) {
                String str = this.LOG_TAG;
                Log.d(str, "serviceState.getState() = " + tm.getServiceState().getState());
                if (tm.getServiceState().getState() == 0) {
                    Log.d(this.LOG_TAG, "CanFallbackForTimeout() : SmsFallbackDefaultSupported");
                    return true;
                }
            }
            Log.d(this.LOG_TAG, "CanFallbackForTimeout() : SmsFallback is not Supported");
            return false;
        } catch (SecurityException e) {
            Log.e(this.LOG_TAG, "No permission for telephony service");
        }
    }

    private static class ImsSmsTracker {
        public String mContentType;
        private final HashMap<String, Object> mData;
        public final String mDestAddress;
        public int mMessageId;
        public byte[] mPdu;
        public int mPhoneId;
        public int mRetryCount;
        public boolean mSentComplete;
        public boolean mStatusReportRequested;
        public int mToken;

        private ImsSmsTracker(int phoneId, HashMap<String, Object> data, int token, int retryCount, int messageId, byte[] pdu, String destAddress, String contentType, boolean StatusReportRequested, boolean sentComplete) {
            this.mPhoneId = phoneId;
            this.mData = data;
            this.mToken = token;
            this.mRetryCount = retryCount;
            this.mMessageId = messageId;
            this.mPdu = pdu;
            this.mDestAddress = destAddress;
            this.mContentType = contentType;
            this.mStatusReportRequested = StatusReportRequested;
            this.mSentComplete = sentComplete;
        }

        public int getToken() {
            return this.mToken;
        }

        public int getRetryCount() {
            return this.mRetryCount;
        }

        public int getMessageId() {
            return this.mMessageId;
        }

        public HashMap<String, Object> getData() {
            return this.mData;
        }
    }

    private static final class LastSentDeliveryAck {
        public int mNetworkType;
        public byte[] mPdu;
        public int mRetryCount = 0;

        public LastSentDeliveryAck(byte[] pdu, int errorCause, int networkType) {
            this.mPdu = pdu;
            this.mNetworkType = networkType;
        }
    }

    private HashMap<String, Object> getImsSmsTrackerMap(int token, int messageId, String destAddr, byte[] pdu, String contentType, int retryCount, boolean statusReportRequest) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("token", Integer.valueOf(token));
        map.put("messageId", Integer.valueOf(messageId));
        map.put(MAP_KEY_DEST_ADDR, destAddr);
        map.put(MAP_KEY_PDU, pdu);
        map.put(MAP_KEY_CONTENT_TYPE, contentType);
        map.put(MAP_KEY_RETRY_COUNT, Integer.valueOf(retryCount));
        map.put(MAP_KEY_STATUS_REPORT, Boolean.valueOf(statusReportRequest));
        return map;
    }

    /* access modifiers changed from: protected */
    public void setSmsListener(IImsSmsListener listener) {
        this.mSmsListener = listener;
    }
}
