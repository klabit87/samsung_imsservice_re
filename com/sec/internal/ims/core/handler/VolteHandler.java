package com.sec.internal.ims.core.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.Dialog;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.ims.servicemodules.volte2.data.CallSetupData;
import com.sec.internal.ims.servicemodules.volte2.data.ConfCallSetupData;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import java.util.HashMap;
import java.util.List;

public abstract class VolteHandler extends BaseHandler implements IVolteServiceInterface {
    protected VolteHandler(Looper looper) {
        super(looper);
    }

    public void registerForUssdEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForUssdEvent(Handler h) {
    }

    public void registerForIncomingCallEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForIncomingCallEvent(Handler h) {
    }

    public void registerForCallStateEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForCallStateEvent(Handler h) {
    }

    public void registerForReferStatus(Handler h, int what, Object obj) {
    }

    public void unregisterForReferStatus(Handler h) {
    }

    public void registerForDialogEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForDialogEvent(Handler h) {
    }

    public void registerForCmcInfoEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForCmcInfoEvent(Handler h) {
    }

    public void registerForDialogSubscribeStatus(Handler h, int what, Object obj) {
    }

    public void unregisterForDialogSubscribeStatus(Handler h) {
    }

    public void registerForCdpnInfoEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForCdpnInfoEvent(Handler h) {
    }

    public void registerForDedicatedBearerNotifyEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForDedicatedBearerNotifyEvent(Handler h) {
    }

    public void registerForRrcConnectionEvent(Handler h, int what, Object obj) {
    }

    public void unregisterForRrcConnectionEvent(Handler h) {
    }

    public void setTtyMode(String ttyMode) {
    }

    public int makeCall(int regId, CallSetupData data, HashMap<String, String> hashMap, int phoneId) {
        return -1;
    }

    public void registerForDtmfEvent(Handler handler, int what, Object obj) {
    }

    public void unregisterForDtmfEvent(Handler handler) {
    }

    public void registerForTextEvent(Handler handler, int what, Object obj) {
    }

    public void unregisterForTextEvent(Handler handler) {
    }

    public void registerForSIPMSGEvent(Handler handler, int what, Object obj) {
    }

    public void unregisterForSIPMSGEvent(Handler handler) {
    }

    public void registerForRtpLossRateNoti(Handler h, int what, Object obj) {
    }

    public void unregisterForRtpLossRateNoti(Handler h) {
    }

    public int rejectCall(int sessionID, int callType, SipError response) {
        return -1;
    }

    public int endCall(int sessionID, int callType, SipReason reason) {
        return -1;
    }

    public int holdCall(int sessionID) {
        return -1;
    }

    public int resumeCall(int sessionID) {
        return -1;
    }

    public int proceedIncomingCall(int sessionId, HashMap<String, String> hashMap) {
        return -1;
    }

    public int answerCallWithCallType(int sessionID, int callType) {
        return -1;
    }

    public int answerCallWithCallType(int sessionID, int callType, String cmcCallTime) {
        return -1;
    }

    public int startNWayConferenceCall(int regId, ConfCallSetupData data) {
        return -1;
    }

    public int addParticipantToNWayConferenceCall(int confCallSessionId, int participantId) {
        return -1;
    }

    public int removeParticipantFromNWayConferenceCall(int confCallSessionId, int participantId) {
        return -1;
    }

    public int addParticipantToNWayConferenceCall(int confCallSessionId, String participant) {
        return -1;
    }

    public int removeParticipantFromNWayConferenceCall(int confCallSessionId, String participant) {
        return -1;
    }

    public int addUserForConferenceCall(int sessionId, ConfCallSetupData data, boolean create) {
        return -1;
    }

    public int modifyCallType(int sessionId, int oldType, int newType) {
        return -1;
    }

    public int replyModifyCallType(int sessionId, int curType, int repType, int reqType) {
        return -1;
    }

    public int replyModifyCallType(int sessionId, int curType, int repType, int reqType, String cmcCallTime) {
        return -1;
    }

    public int rejectModifyCallType(int sessionId, int reason) {
        return -1;
    }

    public int handleDtmf(int sessionID, int code, int mode, int operation, Message result) {
        return -1;
    }

    public int sendText(int sessionID, String text, int len) {
        return -1;
    }

    public int sendReInvite(int sessionID, SipReason reason) {
        return -1;
    }

    public int sendTtyData(int sessionID, byte[] data) {
        return -1;
    }

    public int setTtyMode(int phoneId, int sessionID, int ttyMode) {
        return -1;
    }

    public void setAutomaticMode(int phoneId, boolean mode) {
    }

    public void setRttMode(int phoneId, int mode) {
    }

    public int transferCall(int sessionId, String targetUri) {
        Log.i(this.LOG_TAG, "transferCall: not implemented.");
        return -1;
    }

    public int cancelTransferCall(int sessionId) {
        Log.i(this.LOG_TAG, "cancelTransferCall: not implemented.");
        return -1;
    }

    public int pullingCall(int regId, String targetUri, String msisdn, String origUri, Dialog targetDialog, List<String> list) {
        Log.i(this.LOG_TAG, "pullingCall: not implemented.");
        return -1;
    }

    public int publishDialog(int regId, String origUri, String dispName, String xmlBody, int expires, boolean needDelay) {
        Log.i(this.LOG_TAG, "publishDialog: not implemented.");
        return -1;
    }

    public void updateAudioInterface(int regId, String mode) {
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + msg.what);
    }

    public int sendInfo(int sessionID, int callType, String request, int ussdType) {
        Log.i(this.LOG_TAG, "sendInfo: not implemented.");
        return -1;
    }

    public int sendCmcInfo(int sessionID, Bundle cmcInfoData) {
        Log.i(this.LOG_TAG, "sendCmcInfo: not implemented.");
        return -1;
    }

    public int startVideoEarlyMedia(int sessionID) {
        Log.i(this.LOG_TAG, "startVideoEarlyMedia: not implemented.");
        return -1;
    }

    public void updateScreenOnOff(int phoneId, int on) {
    }

    public void updateXqEnable(int phoneId, boolean enable) {
    }

    public int handleCmcCsfb(int sessionId) {
        Log.i(this.LOG_TAG, "handleCmcCsfb: not implemented.");
        return -1;
    }

    public int DeleteTcpSocket(int sessionID, int callType) {
        return -1;
    }

    public void replaceSipCallId(int sessionId, String sipCallId) {
    }

    public void replaceUserAgent(int replaceSessionId, int newSessionId) {
    }

    public void clearAllCallInternal(int cmcType) {
    }
}
