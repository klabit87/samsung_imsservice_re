package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.core.handler.EucHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.AckMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.NotificationMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.PersistentMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.SystemMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.VolatileMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestEucSendResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendEucResponseResponse;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.translate.AcknowledgementMessageTranslator;
import com.sec.internal.ims.translate.EucResponseStatusTranslator;
import com.sec.internal.ims.translate.NotificationMessageTranslator;
import com.sec.internal.ims.translate.PersistentMessageTranslator;
import com.sec.internal.ims.translate.SystemRequestMessageTranslator;
import com.sec.internal.ims.translate.VolatileMessageTranslator;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;

public class ResipEucHandler extends EucHandler {
    private static final int EVENT_SEND_RESPONSE = 1;
    private static final int EVENT_SEND_RESPONSE_DONE = 10;
    private static final int EVENT_STACK_NOTIFY = 100;
    private static final String LOG_TAG = ResipEucHandler.class.getSimpleName();
    private final RegistrantList mAckMessageRegistrants = new RegistrantList();
    private final AcknowledgementMessageTranslator mAcknowledgementMessageTranslator;
    private final EucResponseStatusTranslator mEucResponseStatusTranslator;
    private final IImsFramework mImsFramework;
    private final RegistrantList mNotificationMessageRegistrants = new RegistrantList();
    private final NotificationMessageTranslator mNotificationMessageTranslator;
    private final RegistrantList mPersistentMessageRegistrants = new RegistrantList();
    private final PersistentMessageTranslator mPersistentMessageTranslator;
    private final RegistrantList mSystemMessageRegistrants = new RegistrantList();
    private final SystemRequestMessageTranslator mSystemRequestMessageTranslator;
    private final RegistrantList mVolatileMessageRegistrants = new RegistrantList();
    private final VolatileMessageTranslator mVolatileMessageTranslator;

    ResipEucHandler(Looper looper, IImsFramework imsFramework) {
        super(looper);
        this.mImsFramework = imsFramework;
        StackIF stackIf = StackIF.getInstance();
        this.mPersistentMessageTranslator = new PersistentMessageTranslator();
        this.mVolatileMessageTranslator = new VolatileMessageTranslator();
        this.mAcknowledgementMessageTranslator = new AcknowledgementMessageTranslator();
        this.mNotificationMessageTranslator = new NotificationMessageTranslator();
        this.mSystemRequestMessageTranslator = new SystemRequestMessageTranslator();
        this.mEucResponseStatusTranslator = new EucResponseStatusTranslator();
        stackIf.registerEucrEvent(this, 100, (Object) null);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            handleSendResponseRequest((EucResponseData) msg.obj);
        } else if (i == 10) {
            handleSendResponseResponse((SendEucResponseResponse) ((AsyncResult) msg.obj).result, (Message) ((AsyncResult) msg.obj).userObj);
        } else if (i != 100) {
            Log.e(LOG_TAG, "handleMessage: Undefined message, ignoring!");
        } else {
            handleNotify((Notify) ((AsyncResult) msg.obj).result);
        }
    }

    private void handleNotify(Notify notify) {
        if (notify.notiType() != 26) {
            Log.e(LOG_TAG, "Invalid notify, ignoring!");
            return;
        }
        EucMessage msg = (EucMessage) notify.noti(new EucMessage());
        switch (notify.notifyid()) {
            case Id.NOTIFY_EUC_PERSISTENT_MESSAGE /*10030*/:
                handlePersistentMessage(msg);
                return;
            case Id.NOTIFY_EUC_VOLATILE_MESSAGE /*10031*/:
                handleVolatileMessage(msg);
                return;
            case Id.NOTIFY_EUC_ACK_MESSAGE /*10032*/:
                handleAckMessage(msg);
                return;
            case Id.NOTIFY_EUC_NOTIFICATION_MESSAGE /*10033*/:
                handleNotificationMessage(msg);
                return;
            case Id.NOTIFY_EUC_SYSTEM_MESSAGE /*10034*/:
                handleSystemMessage(msg);
                return;
            default:
                Log.e(LOG_TAG, "handleNotify(): unexpected notify id, ignoring!");
                return;
        }
    }

    private void handlePersistentMessage(EucMessage msg) {
        Log.i(LOG_TAG, "handlePersistentMessage");
        try {
            PersistentMessage persistentMessage = (PersistentMessage) msg.message(new PersistentMessage());
            if (persistentMessage == null) {
                Log.e(LOG_TAG, "Invalid message, ignoring!");
                return;
            }
            this.mPersistentMessageRegistrants.notifyRegistrants(new AsyncResult((Object) null, this.mPersistentMessageTranslator.translate(persistentMessage), (Throwable) null));
        } catch (TranslationException e) {
            String str = LOG_TAG;
            Log.e(str, "Invalid message, ignoring! " + e.getMessage());
        }
    }

    private void handleVolatileMessage(EucMessage msg) {
        Log.i(LOG_TAG, "handleVolatileMessage");
        try {
            VolatileMessage volatileMessage = (VolatileMessage) msg.message(new VolatileMessage());
            if (volatileMessage == null) {
                Log.e(LOG_TAG, "Invalid message, ignoring!");
                return;
            }
            this.mVolatileMessageRegistrants.notifyRegistrants(new AsyncResult((Object) null, this.mVolatileMessageTranslator.translate(volatileMessage), (Throwable) null));
        } catch (TranslationException e) {
            String str = LOG_TAG;
            Log.e(str, "Invalid message, ignoring! " + e.getMessage());
        }
    }

    private void handleAckMessage(EucMessage msg) {
        Log.i(LOG_TAG, "handleAckMessage");
        try {
            AckMessage ackMessage = (AckMessage) msg.message(new AckMessage());
            if (ackMessage == null) {
                Log.e(LOG_TAG, "Invalid message, ignoring!");
                return;
            }
            this.mAckMessageRegistrants.notifyRegistrants(new AsyncResult((Object) null, this.mAcknowledgementMessageTranslator.translate(ackMessage), (Throwable) null));
        } catch (TranslationException e) {
            String str = LOG_TAG;
            Log.e(str, "Invalid message, ignoring! " + e.getMessage());
        }
    }

    private void handleNotificationMessage(EucMessage msg) {
        Log.i(LOG_TAG, "handleNotificationMessage");
        try {
            NotificationMessage notificationMessage = (NotificationMessage) msg.message(new NotificationMessage());
            if (notificationMessage == null) {
                Log.e(LOG_TAG, "Invalid message, ignoring!");
                return;
            }
            this.mNotificationMessageRegistrants.notifyRegistrants(new AsyncResult((Object) null, this.mNotificationMessageTranslator.translate(notificationMessage), (Throwable) null));
        } catch (TranslationException e) {
            String str = LOG_TAG;
            Log.e(str, "Invalid message, ignoring! " + e.getMessage());
        }
    }

    private void handleSystemMessage(EucMessage msg) {
        Log.i(LOG_TAG, "handleSystemMessage");
        try {
            SystemMessage systemMessage = (SystemMessage) msg.message(new SystemMessage());
            if (systemMessage == null) {
                Log.e(LOG_TAG, "Invalid message, ignoring!");
                return;
            }
            this.mSystemMessageRegistrants.notifyRegistrants(new AsyncResult((Object) null, this.mSystemRequestMessageTranslator.translate(systemMessage), (Throwable) null));
        } catch (TranslationException e) {
            String str = LOG_TAG;
            Log.e(str, "Invalid message, ignoring! " + e.getMessage());
        }
    }

    public void registerForPersistentMessage(Handler h, int what, Object obj) {
        this.mPersistentMessageRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForPersistentMessage(Handler h) {
        this.mPersistentMessageRegistrants.remove(h);
    }

    public void registerForVolatileMessage(Handler h, int what, Object obj) {
        this.mVolatileMessageRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVolatileMessage(Handler h) {
        this.mVolatileMessageRegistrants.remove(h);
    }

    public void registerForNotificationMessage(Handler h, int what, Object obj) {
        this.mNotificationMessageRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNotificationMessage(Handler h) {
        this.mNotificationMessageRegistrants.remove(h);
    }

    public void registerForAckMessage(Handler h, int what, Object obj) {
        this.mAckMessageRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForAckMessage(Handler h) {
        this.mAckMessageRegistrants.remove(h);
    }

    public void registerForSystemMessage(Handler h, int what, Object obj) {
        this.mSystemMessageRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSystemMessage(Handler h) {
        this.mSystemMessageRegistrants.remove(h);
    }

    public void sendEucResponse(EucResponseData eucResponseData) {
        sendMessage(obtainMessage(1, eucResponseData));
    }

    private void handleSendResponseRequest(EucResponseData eucResponseData) {
        int i;
        String str;
        Log.i(LOG_TAG, "onSendResponse: " + eucResponseData);
        UserAgent ua = getUserAgent(eucResponseData.getOwnIdentity());
        if (ua == null) {
            Log.e(LOG_TAG, "handleSendResponseRequest: EUC UserAgent not found!");
            sendCallback(eucResponseData.getCallback(), new EucSendResponseStatus(eucResponseData.getId(), eucResponseData.getType(), eucResponseData.getRemoteUri(), eucResponseData.getOwnIdentity(), EucSendResponseStatus.Status.FAILURE_INTERNAL));
            return;
        }
        int i2 = 1;
        if (eucResponseData.getType() == EucType.PERSISTENT) {
            i = 0;
        } else {
            i = 1;
        }
        int type = i;
        if (eucResponseData.getValue().equals(EucResponseData.Response.ACCEPT)) {
            i2 = 0;
        }
        int value = i2;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int pinOffset = builder.createString((CharSequence) parseStr(eucResponseData.getPin()));
        int idOffset = builder.createString((CharSequence) parseStr(eucResponseData.getId()));
        if (eucResponseData.getRemoteUri() != null) {
            str = eucResponseData.getRemoteUri().toString();
        } else {
            str = "";
        }
        int remUriOffset = builder.createString((CharSequence) str);
        RequestEucSendResponse.startRequestEucSendResponse(builder);
        RequestEucSendResponse.addHandle(builder, (long) ua.getHandle());
        RequestEucSendResponse.addId(builder, idOffset);
        RequestEucSendResponse.addPin(builder, pinOffset);
        RequestEucSendResponse.addRemoteUri(builder, remUriOffset);
        RequestEucSendResponse.addValue(builder, value);
        RequestEucSendResponse.addType(builder, type);
        int requestEucSendResponse = RequestEucSendResponse.endRequestEucSendResponse(builder);
        Request.startRequest(builder);
        Request.addReq(builder, requestEucSendResponse);
        Request.addReqid(builder, Id.REQUEST_EUC_SEND_RESPONSE);
        Request.addReqType(builder, (byte) 36);
        sendRequestToStack(Id.REQUEST_EUC_SEND_RESPONSE, builder, Request.endRequest(builder), obtainMessage(10, eucResponseData.getCallback()), ua);
    }

    private void handleSendResponseResponse(SendEucResponseResponse response, Message callback) {
        String str = LOG_TAG;
        Log.i(str, "handleSendResponseResponse: " + response);
        sendCallback(callback, this.mEucResponseStatusTranslator.translate(response));
    }

    private UserAgent getUserAgent(String imsi) {
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        return rm != null ? (UserAgent) rm.getUserAgentByImsi("euc", imsi) : null;
    }

    private void sendRequestToStack(int id, FlatBufferBuilder request, int offset, Message callback, UserAgent ua) {
        if (ua == null) {
            Log.e(LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            ua.sendRequestToStack(new ResipStackRequest(id, request, offset, callback));
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
