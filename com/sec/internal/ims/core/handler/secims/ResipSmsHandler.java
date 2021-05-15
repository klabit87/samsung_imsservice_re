package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.core.handler.SmsHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReceiveSmsNotification;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RrcConnectionEvent;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SmsRpAckNotification;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestMsgSetMsgAppInfoToSipUa;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendSmsResponse;
import com.sec.internal.ims.servicemodules.sms.SmsEvent;
import com.sec.internal.interfaces.ims.IImsFramework;
import java.util.HashMap;
import java.util.Map;

public class ResipSmsHandler extends SmsHandler {
    static final int EVENT_NEW_INCOMING_SMS = 3;
    static final int EVENT_RP_ACK_TIMEOUT = 5;
    static final int EVENT_RRC_CONNECTION = 6;
    static final int EVENT_SEND_SMS = 1;
    static final int EVENT_SEND_SMS_COMPLETE = 2;
    static final int EVENT_SET_MSG_MSGAPP_INFO_TO_SIP_UA = 11;
    static final int EVENT_SET_MSG_MSGAPP_INFO_TO_SIP_UA_RESP = 12;
    static final int EVENT_SMS_RP_ACK_RECEIVED = 4;
    private static final String LOG_TAG = "ResipSmsHandler";
    static final int RP_ACK_TIMEOUT_MILLIS = 600000;
    private final IImsFramework mImsFramework;
    protected Map<String, SmsMessage> mPendingMessage;
    private final RegistrantList mRrcConnectionEventRegistrants = new RegistrantList();
    private final RegistrantList mSmsEventRegistrants = new RegistrantList();

    public static class SmsMessage {
        String callId;
        String contentType;
        int errorCode;
        boolean isDeliveryReport;
        String localuri;
        int msgId;
        byte[] pdu;
        String smsc;
        UserAgent ua;

        public SmsMessage(UserAgent ua2, String smsc2, String localuri2, String contentType2, byte[] pdu2, int msgId2, boolean isDeliveryReport2, String callId2) {
            this.ua = ua2;
            this.smsc = smsc2;
            this.localuri = localuri2;
            this.contentType = contentType2;
            this.pdu = pdu2;
            this.msgId = msgId2;
            this.isDeliveryReport = isDeliveryReport2;
            this.callId = callId2;
        }
    }

    public ResipSmsHandler(Looper looper, IImsFramework imsFramework) {
        super(looper);
        this.mImsFramework = imsFramework;
        StackIF stackIf = StackIF.getInstance();
        stackIf.registerNewIncomingSmsEvent(this, 3, (Object) null);
        stackIf.registerSmsRpAckEvent(this, 4, (Object) null);
        stackIf.registerForRrcConnectionEvent(this, 6, (Object) null);
        this.mPendingMessage = new HashMap();
    }

    public void registerForSMSEvent(Handler h, int what, Object obj) {
        this.mSmsEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void registerForRrcConnectionEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForRrcConnectionEvent:");
        this.mRrcConnectionEventRegistrants.add(new Registrant(h, what, obj));
    }

    public void sendMessage(String smsc, String localuri, String contentType, byte[] data, boolean isDeliveryReport, String callId, int msgId, int regId) {
        UserAgent ua;
        String str = contentType;
        int i = msgId;
        int i2 = regId;
        if (!Debug.isProductShip()) {
            Log.i(LOG_TAG, "sendMessage: smsc " + smsc + " LocalUri : " + localuri + " contentType " + str + " msdId " + i + " regId " + i2);
        } else {
            String str2 = smsc;
            String str3 = localuri;
            Log.i(LOG_TAG, "sendMessage: contentType " + str + " msdId " + i + " regId " + i2);
        }
        if (i2 == 0) {
            ua = getUa("smsip");
        } else {
            ua = getUaByRegId(i2);
        }
        if (ua == null || !ua.isRegistered(true)) {
            Log.i(LOG_TAG, "sendMessage: Not registered.");
            UserAgent userAgent = ua;
            this.mSmsEventRegistrants.notifyResult(new SmsEvent(ua != null ? ua.getImsRegistration() : null, 11, msgId, 999, (String) null, (byte[]) null, contentType, callId, (String) null, -1));
            return;
        }
        sendMessage(obtainMessage(1, new SmsMessage(ua, smsc, localuri, contentType, data, msgId, isDeliveryReport, callId)));
    }

    public void sendSMSResponse(int phoneId, String callId, int statusCode) {
        Log.i(LOG_TAG, "sendSMSResponse(): [Call-ID] " + callId + " [Status] " + statusCode);
        UserAgent ua = getUa(phoneId, "smsip");
        if (ua != null) {
            ua.sendSmsResponse(callId, statusCode);
        }
    }

    public void setMsgAppInfoToSipUa(int phoneId, String info) {
        sendMessage(obtainMessage(11, phoneId, 0, info));
    }

    private void onSendMessage(SmsMessage sms) {
        sms.ua.sendSms(sms.smsc, sms.localuri, sms.contentType, sms.pdu, sms.isDeliveryReport, sms.callId, obtainMessage(2, sms));
    }

    private void onSendSmsResponse(AsyncResult result) {
        SendSmsResponse res = (SendSmsResponse) result.result;
        SmsMessage sms = (SmsMessage) result.userObj;
        UserAgent ua = getUa((int) res.handle());
        Log.i(LOG_TAG, "onSendSmsResponse: statusCode " + res.statusCode() + " callId " + res.callId());
        if (ua == null) {
            Log.e(LOG_TAG, "onSendSmsResponse: UserAgent is null.");
        } else if (res.statusCode() == 202 || res.statusCode() == 200) {
            SmsEvent event = new SmsEvent();
            event.setImsRegistration(ua.getImsRegistration());
            event.setEventType(12);
            event.setMessageID(sms.msgId);
            event.setCallID(res.callId());
            event.setReasonCode((int) res.statusCode());
            event.setReason(res.errStr());
            this.mSmsEventRegistrants.notifyResult(event);
            SmsEvent event2 = new SmsEvent();
            event2.setImsRegistration(ua.getImsRegistration());
            event2.setMessageID(sms.msgId);
            event2.setCallID(res.callId());
            event2.setContentType((String) null);
            event2.setData((byte[]) null);
            event2.setReasonCode((int) res.statusCode());
            event2.setReason(res.errStr());
            this.mSmsEventRegistrants.notifyResult(event2);
            sms.errorCode = (int) res.statusCode();
            this.mPendingMessage.put(res.callId(), sms);
            sendMessageDelayed(obtainMessage(5, res.callId()), 600000);
        } else {
            Log.e(LOG_TAG, "onSendSmsResponse: errorStr " + res.errStr());
            SmsEvent event3 = new SmsEvent();
            event3.setImsRegistration(ua.getImsRegistration());
            event3.setEventType(12);
            event3.setMessageID(sms.msgId);
            event3.setCallID(res.callId());
            this.mSmsEventRegistrants.notifyResult(event3);
            SmsEvent event4 = new SmsEvent();
            event4.setImsRegistration(ua.getImsRegistration());
            event4.setMessageID(sms.msgId);
            event4.setCallID(res.callId());
            if (res.content() == null || "".equals(res.content())) {
                event4.setContentType((String) null);
            } else {
                event4.setContent(res.content());
                event4.setContentType(res.contentType() + "/" + res.contentSubType());
            }
            event4.setData((byte[]) null);
            event4.setReasonCode((int) res.statusCode());
            event4.setReason(res.errStr());
            event4.setRetryAfter((int) res.retryAfter());
            this.mSmsEventRegistrants.notifyResult(event4);
        }
    }

    private void onSmsRpAckReceived(AsyncResult result) {
        SmsRpAckNotification ra = (SmsRpAckNotification) result.result;
        String contentType = ra.contentType() + "/" + ra.contentSubType();
        Log.i(LOG_TAG, "onSmsRpAckReceived: callId " + ra.callId() + " contentType " + contentType);
        SmsEvent event = new SmsEvent();
        UserAgent ua = getUa((int) ra.handle());
        if (ua == null) {
            Log.e(LOG_TAG, "onSmsRpAckReceived: UserAgent is null.");
            return;
        }
        event.setImsRegistration(ua.getImsRegistration());
        event.setCallID(ra.callId());
        event.setContentType(contentType);
        event.setData(StrUtil.hexStringToBytes(ra.ackCode()));
        if ("vnd.3gpp2.sms".equals(ra.contentSubType())) {
            SmsMessage sms = this.mPendingMessage.remove(ra.callId());
            if (sms == null) {
                Log.i(LOG_TAG, "onSmsRpAckReceived: unknown ack message.");
                return;
            } else {
                event.setMessageID(sms.msgId);
                event.setReasonCode(sms.errorCode);
            }
        } else {
            event.setReasonCode(0);
            if (event.getData() != null) {
                event.setMessageID(event.getData()[1] & 255);
            }
        }
        if (!ua.isRegistered(true)) {
            Log.i(LOG_TAG, "onSmsRpAckReceived: Not registered.");
            event.setEventType(11);
            event.setReasonCode(999);
            this.mSmsEventRegistrants.notifyResult(event);
            return;
        }
        this.mSmsEventRegistrants.notifyResult(event);
        ua.sendSmsRpAckResponse(ra.callId());
    }

    private void onRpAckTimeout(String callId) {
        if (this.mPendingMessage.remove(callId) != null) {
            Log.i(LOG_TAG, "onRpAckTimeout: callId " + callId);
        }
    }

    private void onNewIncomingSms(AsyncResult result) {
        ReceiveSmsNotification sn = (ReceiveSmsNotification) result.result;
        if (!Debug.isProductShip()) {
            Log.i(LOG_TAG, "onNewIncomingSms: handle " + sn.handle() + " callId " + sn.callId() + " sca " + sn.scUri() + " contentType " + sn.contentType() + "/" + sn.contentSubType());
        } else {
            Log.i(LOG_TAG, "onNewIncomingSms: handle " + sn.handle() + " callId " + sn.callId() + " contentType " + sn.contentType() + "/" + sn.contentSubType());
        }
        String contentType = sn.contentType() + "/" + sn.contentSubType();
        SmsEvent event = new SmsEvent();
        UserAgent ua = getUa((int) sn.handle());
        if (ua == null || !ua.isRegistered(true)) {
            Log.e(LOG_TAG, "onNewIncomingSms: UserAgent is null or not registered.");
            return;
        }
        event.setImsRegistration(ua.getImsRegistration());
        event.setCallID(sn.callId());
        event.setSmscAddr(sn.scUri());
        event.setContentType(contentType);
        event.setData(StrUtil.hexStringToBytes(sn.content()));
        this.mSmsEventRegistrants.notifyResult(event);
    }

    private void onRrcConnectionEventReceived(AsyncResult result) {
        Log.i(LOG_TAG, "onRrcConnectionEventReceived:");
        RrcConnectionEvent rrcEvent = (RrcConnectionEvent) result.result;
        if (rrcEvent.event() == 1) {
            this.mRrcConnectionEventRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent(RrcConnectionEvent.RrcEvent.REJECTED));
        } else if (rrcEvent.event() == 2) {
            this.mRrcConnectionEventRegistrants.notifyResult(new com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent(RrcConnectionEvent.RrcEvent.TIMER_EXPIRED));
        }
    }

    public void handleMessage(Message msg) {
        Log.i(LOG_TAG, "handleMessage: what " + msg.what);
        int i = msg.what;
        if (i == 11) {
            onSetMsgAppInfoToSipUa(msg.arg1, (String) msg.obj);
        } else if (i != 12) {
            switch (i) {
                case 1:
                    onSendMessage((SmsMessage) msg.obj);
                    return;
                case 2:
                    onSendSmsResponse((AsyncResult) msg.obj);
                    return;
                case 3:
                    onNewIncomingSms((AsyncResult) msg.obj);
                    return;
                case 4:
                    onSmsRpAckReceived((AsyncResult) msg.obj);
                    return;
                case 5:
                    onRpAckTimeout((String) msg.obj);
                    return;
                case 6:
                    onRrcConnectionEventReceived((AsyncResult) msg.obj);
                    return;
                default:
                    return;
            }
        } else {
            onSetMsgAppInfoToSipUaResp((GeneralResponse) ((AsyncResult) msg.obj).result);
        }
    }

    private UserAgent getUa(String service) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(service);
    }

    private UserAgent getUa(int phoneId, String service) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(service, phoneId);
    }

    private UserAgent getUa(int handle) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(handle);
    }

    private UserAgent getUaByRegId(int regId) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByRegId(regId);
    }

    private void onSetMsgAppInfoToSipUa(int phoneId, String info) {
        Log.i(LOG_TAG, "onSetMsgAppInfoToSipUserAgent: " + info);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int valOffset = builder.createString((CharSequence) info);
        RequestMsgSetMsgAppInfoToSipUa.startRequestMsgSetMsgAppInfoToSipUa(builder);
        RequestMsgSetMsgAppInfoToSipUa.addValue(builder, valOffset);
        int msgOffset = RequestMsgSetMsgAppInfoToSipUa.endRequestMsgSetMsgAppInfoToSipUa(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 404);
        Request.addReqType(builder, (byte) 38);
        Request.addReq(builder, msgOffset);
        int requestOffSet = Request.endRequest(builder);
        UserAgent ua = getUa(phoneId, "smsip");
        if (ua != null) {
            ua.sendRequestToStack(new ResipStackRequest(404, builder, requestOffSet, obtainMessage(12)));
        } else {
            Log.e(LOG_TAG, "onSetMsgAppInfoToSipUserAgent: UserAgent is null.");
        }
    }

    private void onSetMsgAppInfoToSipUaResp(GeneralResponse response) {
        Log.i(LOG_TAG, "onSetMsgAppInfoToSipUaResp: " + response);
    }
}
