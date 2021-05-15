package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.os.RemoteException;
import com.sec.epdg.EpdgManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti;
import com.sec.internal.ims.servicemodules.volte2.data.TextInfo;
import com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface;
import java.util.List;

public interface IVolteServiceModuleInternal {
    public static final String ACTION_EMERGENCY_CALLBACK_MODE_INTERNAL = "com.samsung.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED_INTERNAL";
    public static final int CMC_PD_CHECK_TIMER_VALUE = 20;
    public static final int EPDN_DISCONNECT_TIMER_VALUE_KDDI = 300;
    public static final int EVENT_DDS_CHANGED = 26;
    public static final int EVENT_OPTIONS_EVENT = 32;
    public static final int EVENT_RELEASE_WFC_BEFORE_HO = 20;
    public static final int EVENT_SIM_SUBSCRIBE_ID_CHANGED = 24;
    public static final int EVT_CALL_ECBM_CHANGED = 6;
    public static final int EVT_CALL_END_BY_CS_EVENT = 10;
    public static final int EVT_CALL_PS_BARRED = 14;
    public static final int EVT_CALL_STATUS_CHANGE_EVENT = 5;
    public static final int EVT_CMC_PD_CHECK_TIMER = 33;
    public static final int EVT_CMC_RECORDING_EVENT = 34;
    public static final int EVT_CONFIG_UPDATED = 21;
    public static final int EVT_DEDICATED_BEARER_EVENT = 8;
    public static final int EVT_DELAYED_INCOMING_CALL = 11;
    public static final int EVT_DEREGISTERED = 12;
    public static final int EVT_DEREGISTERING = 13;
    public static final int EVT_DTMF_EVENT = 17;
    public static final int EVT_EPDN_DISCONNECT_TIMER = 16;
    public static final int EVT_EPDN_SETUP_FAIL = 19;
    public static final int EVT_IMS_CALL_EVENT = 2;
    public static final int EVT_IMS_DIALOG_EVENT = 3;
    public static final int EVT_IMS_INCOMINGCALL_EVENT = 1;
    public static final int EVT_IQI_STATE_CHNAGED = 28;
    public static final int EVT_RESET_DIALOG_EVENT = 15;
    public static final int EVT_RTP_LOSS_RATE_NOTI = 18;
    public static final int EVT_SCREEN_ONOFF_CHANGED = 23;
    public static final int EVT_SIM_READY = 30;
    public static final int EVT_SIM_REMOVED = 31;
    public static final int EVT_SIPMSG_EVENT = 25;
    public static final int EVT_SRVCC_STATE_CHANGE_EVENT = 27;
    public static final int EVT_TEXT_EVENT = 22;
    public static final int EVT_TRY_DISCONNECT = 9;
    public static final String INTENT_ACTION_IQISERVICE_STATE_CHNAGED = "com.att.iqi.action.SERVICE_STATE_CHANGED";
    public static final String INTENT_ACTION_LTE_BAND_CHANGED = "android.intent.action.LTE_BAND";
    public static final String INTENT_ACTION_PS_BARRED = "com.android.intent.action.PSBARRED_FOR_VOLTE";
    public static final int INVALID_PHONE_ID = -1;
    public static final String LOG_TAG;
    public static final String LTE_DATA_NETWORK_MODE = "lte_mode_on";
    public static final String NAME;

    boolean acceptCallWhileSmsipRegistered(ImsRegistration imsRegistration);

    void checkCmcP2pList(ImsRegistration imsRegistration, CallProfile callProfile);

    ImsCallSession createSession(CallProfile callProfile) throws RemoteException;

    ImsCallSession createSession(CallProfile callProfile, int i) throws RemoteException;

    int[] getCallCount();

    int[] getCallCount(int i);

    ICmcMediaController getCmcMediaController();

    ICmcServiceHelperInternal getCmcServiceHelper();

    CmcServiceHelper getCmcServiceModule();

    int getDefaultPhoneId();

    EpdgManager getEpdgManager();

    ImsCallSession getForegroundSession();

    ImsCallSession getForegroundSession(int i);

    IImsMediaController getImsMediaController();

    int getLastRegiErrorCode(int i);

    boolean getLteEpsOnlyAttached(int i);

    IMediaServiceInterface getMediaSvcIntf();

    int getMergeCallType(int i, boolean z);

    NetworkEvent getNetwork();

    NetworkEvent getNetwork(int i);

    ContentValues getPSDataDetails(int i);

    long getRttDbrTimer(int i);

    ImsCallSession getSession(int i);

    ImsCallSession getSessionByCallId(int i);

    List<ImsCallSession> getSessionByCallType(int i);

    List<ImsCallSession> getSessionByCallType(int i, int i2);

    ImsCallSession getSessionBySipCallId(String str);

    List<ImsCallSession> getSessionByState(int i, CallConstants.STATE state);

    List<ImsCallSession> getSessionByState(CallConstants.STATE state);

    int getSessionCount();

    int getSessionCount(int i);

    List<ImsCallSession> getSessionList();

    List<ImsCallSession> getSessionList(int i);

    int getSrvccVersion(int i);

    boolean hasCsCall(int i);

    boolean hasEmergencyCall(int i);

    boolean hasRingingCall();

    boolean hasRingingCall(int i);

    boolean isCallBarringByNetwork(int i);

    boolean isCallServiceAvailable(int i, String str);

    boolean isCsfbErrorCode(int i, int i2, SipError sipError);

    boolean isCsfbErrorCode(int i, int i2, SipError sipError, int i3);

    boolean isEnableCallWaitingRule();

    boolean isMmtelAcquiredEver();

    boolean isRegisteredOverLteOrNr(int i);

    boolean isRoaming(int i);

    boolean isSilentRedialEnabled(Context context, int i);

    boolean isVowifiEnabled(int i);

    void onCallModifyRequested(int i);

    void onConferenceParticipantAdded(int i, String str);

    void onConferenceParticipantRemoved(int i, String str);

    void onSendRttSessionModifyRequest(int i, boolean z);

    void onSendRttSessionModifyResponse(int i, boolean z, boolean z2);

    void onTextReceived(TextInfo textInfo);

    boolean post(Runnable runnable);

    void pushCallInternal();

    void releaseSessionByState(int i, CallConstants.STATE state);

    void sendMobileCareEvent(int i, int i2, int i3, String str);

    void sendQualityStatisticsEvent();

    void sendRtpLossRate(int i, RtpLossRateNoti rtpLossRateNoti);

    void setDelayedDeregisterTimerRunning(int i, boolean z);

    int startLocalRingBackTone(int i, int i2, int i3);

    int stopLocalRingBackTone();

    void transfer(int i, String str);

    boolean triggerPsRedial(int i, int i2, int i3);

    String updateEccUrn(int i, String str);

    static {
        String simpleName = VolteServiceModule.class.getSimpleName();
        NAME = simpleName;
        LOG_TAG = simpleName;
    }

    Context getContext() {
        return null;
    }

    ImsUri getActiveImpu() {
        return null;
    }

    boolean isEmergencyRegistered(int phoneId) {
        return false;
    }

    boolean isEcbmMode(int phoneId) {
        return false;
    }

    boolean isCallBarredBySSAC(int phoneId, int callType) {
        return false;
    }

    void onCallEnded(int phoneId, int sessionId, int error) {
    }

    boolean isProhibited(int phoneId) {
        return false;
    }

    int getRttMode() {
        return 0;
    }

    int getRttMode(int phoneId) {
        return 0;
    }

    void setRttMode(int phoneId, int mode) {
    }

    int getTtyMode() {
        return 0;
    }

    int getTtyMode(int phoneId) {
        return 0;
    }

    boolean isRttCall(int sessionId) {
        return false;
    }

    boolean getAutomaticMode() {
        return false;
    }

    boolean getAutomaticMode(int phoneId) {
        return false;
    }

    ImsRegistration getRegInfo(int regId) {
        return null;
    }

    void notifyOnPulling(int phoneId, int pullingCallId) {
    }

    void notifyOnCmcRecordingEvent(int phoneId, int event, int extra, int sessionId) {
    }
}
