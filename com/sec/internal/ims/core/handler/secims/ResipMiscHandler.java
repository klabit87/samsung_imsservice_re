package com.sec.internal.ims.core.handler.secims;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.MiscHandler;
import com.sec.internal.ims.core.handler.secims.StackIF;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_.EcholocateRtpMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EcholocateMsg_.EcholocateSignalMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage_.XqContent;
import com.sec.internal.ims.servicemodules.volte2.data.EcholocateEvent;
import com.sec.internal.ims.xq.att.data.XqEvent;
import com.sec.internal.interfaces.ims.IImsFramework;

public class ResipMiscHandler extends MiscHandler implements StackIF.MiscEventListener {
    /* access modifiers changed from: private */
    public static String ATCMD_CHECK_OMADM = "AT+VOLTECON=1,0,1,0";
    /* access modifiers changed from: private */
    public static String ATCMD_CHECK_SMS_FORMAT = "AT+IMSSTEST=0,0,0";
    /* access modifiers changed from: private */
    public static String ATCMD_COMMAND_EXTRA = "command";
    /* access modifiers changed from: private */
    public static String ATCMD_IMSTEST_RESULT_PREFIX = "\r\n+IMSSTEST:0,";
    /* access modifiers changed from: private */
    public static String ATCMD_INTENT = "com.sec.factory.RECEIVED_FROM_RIL";
    /* access modifiers changed from: private */
    public static String ATCMD_RESET_OMADM = "AT+VOLTECON=0,0,0,0";
    private static String ATCMD_RESULT_ACTION = "com.sec.factory.SEND_TO_RIL";
    private static String ATCMD_RESULT_KEY = "message";
    /* access modifiers changed from: private */
    public static String ATCMD_RESULT_NG = "NG";
    /* access modifiers changed from: private */
    public static String ATCMD_RESULT_OK = "OK";
    /* access modifiers changed from: private */
    public static String ATCMD_RESULT_SUFFIX = "\r\n\r\nOK\r\n";
    /* access modifiers changed from: private */
    public static String ATCMD_VOLTECON_RESULT_PREFIX = "\r\n+VOLTECON:0,";
    private static final int EVENT_ALARM_CANCELLED = 2;
    private static final int EVENT_ALARM_FIRED = 3;
    private static final int EVENT_ALARM_REQUESTED = 1;
    private static final int EVENT_ECHOLOCATE_RECEIVED = 4;
    private static final int EVENT_XQ_MTRIP_RECEIVED = 5;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ResipMiscHandler.class.getSimpleName();
    private PreciseAlarmManager mAlarmManager = null;
    private final SparseArray<Message> mAlarmMessageList = new SparseArray<>();
    private ATCmdReceiver mAtCmdReceiver = null;
    private final Context mContext;
    private final RegistrantList mEcholocateEventRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public final IImsFramework mImsFramework;
    private final StackIF mStackIF;
    private final RegistrantList mXqMtripEventRegistrants = new RegistrantList();

    private class ATCmdReceiver extends BroadcastReceiver {
        private ATCmdReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String resultString;
            String resultString2;
            if (context == null || intent == null) {
                Log.e(ResipMiscHandler.LOG_TAG, "Wrong Event Ignore.");
                return;
            }
            String action = intent.getAction();
            Log.i(ResipMiscHandler.LOG_TAG, "Receive Action " + action);
            if (!ResipMiscHandler.ATCMD_INTENT.equals(action)) {
                return;
            }
            if (!intent.hasExtra(ResipMiscHandler.ATCMD_COMMAND_EXTRA)) {
                Log.e(ResipMiscHandler.LOG_TAG, "Factory intent doesn't have [" + ResipMiscHandler.ATCMD_COMMAND_EXTRA + "]");
                return;
            }
            String command = intent.getStringExtra(ResipMiscHandler.ATCMD_COMMAND_EXTRA);
            if (TextUtils.isEmpty(command)) {
                Log.e(ResipMiscHandler.LOG_TAG, "Factory intent doesn't have value");
                return;
            }
            Mno mno = SimUtil.getMno();
            Log.i(ResipMiscHandler.LOG_TAG, "Factory intent command " + command);
            boolean result = true;
            if (ResipMiscHandler.ATCMD_CHECK_SMS_FORMAT.equals(command)) {
                if (mno == Mno.VZW) {
                    result = ResipMiscHandler.this.mImsFramework.isDefaultDmValue(ConfigConstants.ATCMD.SMS_SETTING, 0);
                }
                resultString = ResipMiscHandler.ATCMD_IMSTEST_RESULT_PREFIX;
            } else if (ResipMiscHandler.ATCMD_CHECK_OMADM.equals(command)) {
                if (mno == Mno.VZW) {
                    result = ResipMiscHandler.this.mImsFramework.isDefaultDmValue(ConfigConstants.ATCMD.OMADM_VALUE, 0);
                }
                resultString = ResipMiscHandler.ATCMD_VOLTECON_RESULT_PREFIX;
            } else if (ResipMiscHandler.ATCMD_RESET_OMADM.equals(command)) {
                if (mno == Mno.VZW) {
                    result = ResipMiscHandler.this.mImsFramework.setDefaultDmValue(ConfigConstants.ATCMD.OMADM_VALUE, 0);
                }
                resultString = ResipMiscHandler.ATCMD_VOLTECON_RESULT_PREFIX;
            } else {
                return;
            }
            if (result) {
                resultString2 = resultString + ResipMiscHandler.ATCMD_RESULT_OK;
            } else {
                resultString2 = resultString + ResipMiscHandler.ATCMD_RESULT_NG;
            }
            ResipMiscHandler.this.sendATCmdResponse(resultString2 + ResipMiscHandler.ATCMD_RESULT_SUFFIX);
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            int id = msg.arg1;
            if (this.mAlarmMessageList.get(id) != null) {
                String str = LOG_TAG;
                Log.e(str, "Already reigstered id " + id);
                return;
            }
            Message add = obtainMessage(3, id, -1);
            this.mAlarmMessageList.put(id, add);
            this.mAlarmManager.sendMessageDelayed(getClass().getSimpleName(), add, (long) msg.arg2);
        } else if (i == 2) {
            int id2 = msg.arg1;
            Message remove = this.mAlarmMessageList.get(id2);
            if (remove == null) {
                String str2 = LOG_TAG;
                Log.e(str2, "Not reigstered id " + id2);
                return;
            }
            this.mAlarmManager.removeMessage(remove);
            this.mAlarmMessageList.remove(id2);
        } else if (i == 3) {
            int id3 = msg.arg1;
            String str3 = LOG_TAG;
            Log.i(str3, "ALARM_WAKE_UP id=" + id3);
            this.mStackIF.sendAlarmWakeUp(id3);
            this.mAlarmMessageList.remove(id3);
        } else if (i == 4) {
            EcholocateMsg noti = (EcholocateMsg) ((AsyncResult) msg.obj).result;
            EcholocateEvent echolocateEvent = new EcholocateEvent();
            if (noti.msgtype() == 0) {
                echolocateEvent.setType(EcholocateEvent.EcholocateType.signalMsg);
                EcholocateSignalMsg signalMsg = noti.echolocateSignalData();
                if (signalMsg != null) {
                    echolocateEvent.setSignalData(signalMsg.origin(), signalMsg.line1(), signalMsg.callid(), signalMsg.cseq(), signalMsg.sessionid(), signalMsg.reason(), signalMsg.contents(), signalMsg.isEpdgCall());
                }
            } else {
                echolocateEvent.setType(EcholocateEvent.EcholocateType.rtpMsg);
                EcholocateRtpMsg rtpMsg = noti.echolocateRtpData();
                if (rtpMsg != null) {
                    echolocateEvent.setRtpData(rtpMsg.dir(), rtpMsg.id(), rtpMsg.lossrate(), rtpMsg.delay(), rtpMsg.jitter(), rtpMsg.measuredperiod());
                }
            }
            this.mEcholocateEventRegistrants.notifyResult(echolocateEvent);
        } else if (i != 5) {
            super.handleMessage(msg);
        } else {
            Log.i(LOG_TAG, "XqMessage");
            XqMessage metric = (XqMessage) ((AsyncResult) msg.obj).result;
            XqEvent xqMsg = new XqEvent();
            xqMsg.setXqMtrips(metric.mtrip());
            for (int i2 = 0; i2 < metric.mContentLength(); i2++) {
                XqContent xqContent = metric.mContent(i2);
                if (xqContent != null) {
                    xqMsg.setContent(xqContent.type(), (int) xqContent.intVal(), xqContent.strVal() != null ? xqContent.strVal() : "");
                }
            }
            this.mXqMtripEventRegistrants.notifyResult(xqMsg);
        }
    }

    protected ResipMiscHandler(Looper looper, Context context, IImsFramework imsFramework) {
        super(looper);
        StackIF instance = StackIF.getInstance();
        this.mStackIF = instance;
        this.mContext = context;
        this.mImsFramework = imsFramework;
        instance.registerMiscListener(this);
        this.mStackIF.registerEcholocateEvent(this, 4, (Object) null);
        this.mStackIF.registerXqMtrip(this, 5, (Object) null);
        this.mAlarmManager = PreciseAlarmManager.getInstance(context);
        this.mAtCmdReceiver = new ATCmdReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ATCMD_INTENT);
        this.mContext.registerReceiver(this.mAtCmdReceiver, filter);
    }

    public void init() {
        super.init();
    }

    public void registerForEcholocateEvent(Handler h, int what, Object obj) {
        Log.i(LOG_TAG, "registerForEcholocateEvent:");
        this.mEcholocateEventRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForEcholocateEvent(Handler h) {
        Log.i(LOG_TAG, "unregisterForEcholocateEvent:");
        this.mEcholocateEventRegistrants.remove(h);
    }

    public void registerForXqMtripEvent(Handler h, int what, Object obj) {
        this.mXqMtripEventRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForXqMtripEvent(Handler h) {
        this.mXqMtripEventRegistrants.remove(h);
    }

    public void onAlarmRequested(int id, int delay) {
        String str = LOG_TAG;
        Log.i(str, "onAlarmRequested: delay=" + delay + " id=" + id);
        sendMessage(obtainMessage(1, id, delay));
    }

    public void onAlarmCancelled(int id) {
        String str = LOG_TAG;
        Log.i(str, "onAlarmCancelled: id=" + id);
        sendMessage(obtainMessage(2, id, 0));
    }

    /* access modifiers changed from: private */
    public void sendATCmdResponse(String result) {
        String str = LOG_TAG;
        Log.i(str, "send AT CMD response : " + result);
        Intent intent = new Intent(ATCMD_RESULT_ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(ATCMD_RESULT_KEY, result);
        intent.putExtras(bundle);
        this.mContext.sendBroadcast(intent);
    }
}
