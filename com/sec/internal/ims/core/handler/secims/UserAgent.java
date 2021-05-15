package com.sec.internal.ims.core.handler.secims;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.Dialog;
import com.sec.ims.ImsRegistration;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged_.Contact;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse;
import com.sec.internal.ims.servicemodules.im.interfaces.FtIntent;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class UserAgent extends StateMachine implements IUserAgent {
    private static final String ECC_IWLAN = "IWLAN";
    private static final int EVENT_ACCEPT_CALL_TRANSFER = 21;
    public static final int EVENT_AKA_CHALLENGE_TIME_OUT = 46;
    private static final int EVENT_CREATE_UA = 1;
    private static final int EVENT_DELAYED_DEREGISTERED = 800;
    private static final int EVENT_DELETE_UA = 4;
    private static final int EVENT_DEREGISTERED = 12;
    private static final int EVENT_DEREGISTERED_TIMEOUT = 13;
    private static final int EVENT_DEREGISTER_COMPLETE = 11;
    private static final int EVENT_DISCONNECTED = 100;
    private static final int EVENT_EMERGENCY_REGISTRATION_FAILED = 900;
    private static final int EVENT_REGISTERED = 8;
    private static final int EVENT_REGISTER_REQUESTED = 7;
    private static final int EVENT_REG_INFO_NOTIFY = 101;
    private static final int EVENT_REQUEST_ANSWER_CALL = 16;
    private static final int EVENT_REQUEST_CANCEL_TRANSFER_CALL = 45;
    private static final int EVENT_REQUEST_DELETE_TCP_CLIENT_SOCKET = 49;
    private static final int EVENT_REQUEST_DEREGISTER = 10;
    private static final int EVENT_REQUEST_DEREGISTER_INTERNAL = 43;
    private static final int EVENT_REQUEST_END_CALL = 15;
    private static final int EVENT_REQUEST_EXTEND_TO_CONFERENCE = 107;
    private static final int EVENT_REQUEST_HANDLE_CMC_CSFB = 55;
    private static final int EVENT_REQUEST_HANDLE_DTMF = 23;
    private static final int EVENT_REQUEST_HOLD_CALL = 17;
    private static final int EVENT_REQUEST_HOLD_VIDEO = 26;
    private static final int EVENT_REQUEST_MAKE_CALL = 14;
    private static final int EVENT_REQUEST_MAKE_CONF_CALL = 36;
    private static final int EVENT_REQUEST_MERGE_CALL = 19;
    private static final int EVENT_REQUEST_MODIFY_CALL_TYPE = 104;
    private static final int EVENT_REQUEST_MODIFY_VIDEO_QUALITY = 111;
    private static final int EVENT_REQUEST_NETWORK_SUSPENDED = 38;
    private static final int EVENT_REQUEST_PROGRESS_INCOMING_CALL = 25;
    private static final int EVENT_REQUEST_PUBLISH = 41;
    private static final int EVENT_REQUEST_PUBLISH_DIALOG = 47;
    private static final int EVENT_REQUEST_PULLING_CALL = 29;
    private static final int EVENT_REQUEST_REGISTER = 6;
    private static final int EVENT_REQUEST_REJECT_CALL = 22;
    private static final int EVENT_REQUEST_REJECT_MODIFY_CALL_TYPE = 106;
    private static final int EVENT_REQUEST_REPLY_MODIFY_CALL_TYPE = 105;
    private static final int EVENT_REQUEST_RESUME_CALL = 18;
    private static final int EVENT_REQUEST_RESUME_VIDEO = 27;
    private static final int EVENT_REQUEST_SEND_CMC_INFO = 59;
    private static final int EVENT_REQUEST_SEND_INFO = 48;
    private static final int EVENT_REQUEST_SEND_TEXT = 51;
    private static final int EVENT_REQUEST_START_CAMERA = 28;
    private static final int EVENT_REQUEST_START_CMC_RECORD = 58;
    private static final int EVENT_REQUEST_START_RECORD = 56;
    private static final int EVENT_REQUEST_START_VIDEO_EARLYMEDIA = 54;
    private static final int EVENT_REQUEST_STOP_CAMERA = 30;
    private static final int EVENT_REQUEST_STOP_RECORD = 57;
    private static final int EVENT_REQUEST_TRANSFER_CALL = 20;
    private static final int EVENT_REQUEST_UNPUBLISH = 42;
    private static final int EVENT_REQUEST_UPDATE_CALL = 37;
    private static final int EVENT_REQUEST_UPDATE_CALLWAITING_STATUS = 39;
    private static final int EVENT_RETRY_UA_CREATE = 3;
    private static final int EVENT_SEND_AUTH_RESPONSE = 9;
    private static final int EVENT_SEND_MEDIA_EVENT = 1001;
    private static final int EVENT_SEND_REQUEST = 1000;
    private static final int EVENT_SEND_SMS = 31;
    private static final int EVENT_SEND_SMS_RESPONSE = 33;
    private static final int EVENT_SEND_SMS_RP_ACK_RESPONSE = 32;
    private static final int EVENT_START_LOCAL_RINGBACKTONE = 109;
    private static final int EVENT_STOP_LOCAL_RINGBACKTONE = 110;
    private static final int EVENT_UA_CREATED = 2;
    private static final int EVENT_UA_DELETED = 5;
    private static final int EVENT_UPDATE_AUDIO_INTERFACE = 108;
    private static final int EVENT_UPDATE_CONF_CALL = 35;
    private static final int EVENT_UPDATE_GEOLOCATION = 44;
    private static final int EVENT_UPDATE_PANI = 34;
    private static final int EVENT_UPDATE_RAT = 50;
    private static final int EVENT_UPDATE_ROUTE_TABLE = 102;
    private static final int EVENT_UPDATE_TIME_IN_PLANI = 52;
    private static final int EVENT_UPDATE_VCE_CONFIG = 40;
    private static final String LOG_TAG = "UserAgent";
    private static final String PROPERTY_ECC_PATH = "ril.subtype";
    protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    Context mContext = null;
    private final State mDefaultState = new DefaultState();
    private final State mDeregisteringState = new DeregisteringState();
    /* access modifiers changed from: private */
    public UserAgentState mDestState = UserAgentState.INITIAL;
    private List<NameAddr> mDeviceList = new ArrayList();
    /* access modifiers changed from: private */
    public int mEcmpMode = 0;
    /* access modifiers changed from: private */
    public final State mEmergencyState = new EmergencyState();
    private boolean mEpdgStatus = false;
    /* access modifiers changed from: private */
    public SipError mError;
    /* access modifiers changed from: private */
    public int mHandle = -1;
    /* access modifiers changed from: private */
    public List<NameAddr> mImpuList = new ArrayList();
    private final IImsFramework mImsFramework;
    /* access modifiers changed from: private */
    public ImsProfile mImsProfile = null;
    private final State mInitialState = new InitialState();
    /* access modifiers changed from: private */
    public UaEventListener mListener = null;
    private Network mNetwork = null;
    /* access modifiers changed from: private */
    public Set<String> mNotifyServiceList = new HashSet();
    private int mPdn;
    private IPdnController mPdnController = null;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    private final State mProhibitedState = new ProhibitedState();
    private final State mReRegisteringState = new ReRegisteringState();
    /* access modifiers changed from: private */
    public final State mReadyState = new ReadyState();
    /* access modifiers changed from: private */
    public String mRegisterSipResponse = null;
    private final State mRegisteredState = new RegisteredState();
    private final State mRegisteringState = new RegisteringState();
    /* access modifiers changed from: private */
    public ImsRegistration mRegistration = null;
    /* access modifiers changed from: private */
    public int mRetryAfter;
    /* access modifiers changed from: private */
    public ISimManager mSimManager = null;
    /* access modifiers changed from: private */
    public IStackIF mStackIf = null;
    private boolean mSuspendStatus = false;
    ITelephonyManager mTelephonyManager = null;
    private final State mTerminatingState = new TerminatingState();
    /* access modifiers changed from: private */
    public List<String> mThirdPartyFeatureTags = null;
    /* access modifiers changed from: private */
    public UaProfile mUaProfile = null;

    public interface UaEventListener {
        void onContactActivated(UserAgent userAgent, int i);

        void onCreated(UserAgent userAgent);

        void onDeregistered(UserAgent userAgent, boolean z, SipError sipError, int i);

        void onRefreshRegNotification(int i);

        void onRegEventContactUriNotification(int i, List<ImsUri> list, int i2, String str);

        void onRegistered(UserAgent userAgent);

        void onRegistrationError(UserAgent userAgent, SipError sipError, int i);

        void onSubscribeError(UserAgent userAgent, SipError sipError);

        void onUpdatePani(UserAgent userAgent);
    }

    public enum UserAgentState {
        DEFAULT,
        INITIAL,
        READY,
        REGISTERING,
        REGISTERED,
        REREGISTERING,
        DEREGISTERING,
        TERMINATING,
        EMERGENCY,
        PROHIBITTED
    }

    public UserAgent(Context context, Handler handler, IStackIF stackIF, ITelephonyManager telephonyManager, IPdnController pdnController, ISimManager simManager, IImsFramework imsFramework) {
        super("UserAgent - ", handler);
        this.mSimManager = simManager;
        this.mImsFramework = imsFramework;
        this.mPhoneId = simManager.getSimSlotIndex();
        initState();
        this.mContext = context;
        this.mStackIf = stackIF;
        this.mTelephonyManager = telephonyManager;
        this.mPdnController = pdnController;
    }

    public UserAgent(Handler handler, IImsFramework imsFramework) {
        super("UserAgent - ", handler);
        this.mImsFramework = imsFramework;
    }

    public void setImsProfile(ImsProfile profile) {
        this.mImsProfile = profile;
    }

    public void setUaProfile(UaProfile uaProfile) {
        this.mUaProfile = uaProfile;
    }

    public void setPdn(int pdn) {
        this.mPdn = pdn;
    }

    public int getPdn() {
        return this.mPdn;
    }

    public void setNetwork(Network network) {
        this.mNetwork = network;
    }

    public IPdnController getPdnController() {
        return this.mPdnController;
    }

    public boolean isRegistered(boolean includeRegistering) {
        return getCurrentState().equals(this.mRegisteredState) || (includeRegistering && getCurrentState().equals(this.mReRegisteringState));
    }

    public String getStateName() {
        return getCurrentState().getName();
    }

    public void registerListener(UaEventListener listener) {
        this.mListener = listener;
    }

    public void unRegisterListener() {
        this.mListener = null;
    }

    public void setThirdPartyFeatureTags(List<String> featureTags) {
        this.mThirdPartyFeatureTags = featureTags;
    }

    public int create() {
        Log.i("UserAgent[" + this.mPhoneId + "]", "create:");
        sendMessage(1);
        return 0;
    }

    public int register() {
        Log.i("UserAgent[" + this.mPhoneId + "]", "register:");
        if (this.mImsProfile.hasEmergencySupport()) {
            String eccPath = SemSystemProperties.get(PROPERTY_ECC_PATH, "");
            Log.i("UserAgent[" + this.mPhoneId + "]", "eccPath : " + eccPath);
            this.mEpdgStatus = eccPath.equalsIgnoreCase(ECC_IWLAN);
        } else {
            boolean isEpdgConnected = this.mPdnController.isEpdgConnected(this.mPhoneId);
            this.mEpdgStatus = isEpdgConnected;
            ImsRegistration imsRegistration = this.mRegistration;
            if (imsRegistration != null) {
                imsRegistration.setEpdgStatus(isEpdgConnected);
            }
        }
        if (!SimUtil.isDualIMS() || !Mno.fromName(this.mImsProfile.getMnoName()).isChn() || !SemSystemProperties.get("ro.boot.hardware", "").contains("qcom")) {
            sendMessage(6);
            return 0;
        }
        sendMessageDelayed(6, 10);
        return 0;
    }

    public void deregisterInternal(boolean local) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "deregisterInternal: local=" + local);
        sendMessageDelayed(43, local ? 1 : 0, -1, 500);
    }

    public void deregisterLocal() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "deregisterLocal:");
        sendMessage(13);
    }

    public void suspended(boolean state) {
        this.mSuspendStatus = state;
        sendMessage(38, state, -1);
    }

    public boolean getSuspendState() {
        return this.mSuspendStatus;
    }

    public void deleteTcpClientSocket() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "deleteTcpClientSocket:");
        sendMessage(49);
    }

    public void updateAudioInterface(String mode, Message result) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "updateAudioInterface: mode =" + mode);
        Bundle bundle = new Bundle();
        bundle.putString("mode", mode);
        bundle.putParcelable("result", result);
        sendMessage(108, (Object) bundle);
    }

    public void sendRequestToStack(ResipStackRequest request) {
        sendMessage(1000, (Object) request);
    }

    public void makeCall(String destUri, String origUri, int type, String dispName, String dialedNumber, AdditionalContents ac, String cli, String pEmergencyInfoOfAtt, HashMap<String, String> additionalSipHeaders, String alertInfo, boolean isLteEpsOnlyAttached, List<String> p2p, int cmcBoundSessionId, Bundle composerInfo, String replaceCallId, Message result) {
        int i = type;
        HashMap<String, String> hashMap = additionalSipHeaders;
        Bundle bundle = composerInfo;
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "makeCall: destUri=" + IMSLog.checker(destUri) + ", type=" + i + " origUri=" + IMSLog.checker(origUri));
        Bundle bundle2 = new Bundle();
        bundle2.putString("destUri", destUri);
        bundle2.putString("origUri", origUri);
        bundle2.putParcelable("result", result);
        bundle2.putInt("type", i);
        if (ac != null) {
            bundle2.putString("additionalContentsContents", ac.contents());
            bundle2.putString("additionalContentsMime", ac.mimeType());
        }
        bundle2.putString("cli", cli);
        bundle2.putString("dispName", dispName);
        bundle2.putString("alertInfo", alertInfo);
        bundle2.putString("dialedNumber", dialedNumber);
        bundle2.putString("pEmergencyInfoOfAtt", pEmergencyInfoOfAtt);
        bundle2.putBoolean("isLteEpsOnlyAttached", isLteEpsOnlyAttached);
        if (hashMap != null) {
            bundle2.putSerializable("additionalSipHeaders", hashMap);
        }
        if (p2p != null) {
            bundle2.putStringArrayList("p2p", (ArrayList) p2p);
        }
        bundle2.putInt("cmcBoundSessionId", cmcBoundSessionId);
        if (bundle != null && !composerInfo.isEmpty()) {
            bundle2.putBundle(CallConstants.ComposerData.TAG, bundle);
        }
        bundle2.putString("replaceCallId", replaceCallId);
        sendMessage(14, (Object) bundle2);
    }

    public void rejectCall(int sessionId, SipError response) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "rejectCall: sessionId " + sessionId);
        sendMessage(22, sessionId, -1, response);
    }

    public void progressIncomingCall(int sessionId, HashMap<String, String> headers) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "progressIncomingCall: sessionId " + sessionId);
        sendMessage(obtainMessage(25, sessionId, -1, headers));
    }

    public void endCall(int sessionId, SipReason reason) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "endCall: sessionId " + sessionId);
        sendMessage(15, sessionId, -1, reason);
    }

    public void answerCall(int sessionId, int callType, String cmcCallTime) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "answerCall: sessionId " + sessionId + " callType " + callType + " cmcCallEstablishTime " + cmcCallTime);
        sendMessage(16, sessionId, callType, cmcCallTime);
    }

    public void handleDtmf(int sessionId, int code, int mode, int operation, Message result) {
        Log.i("UserAgent[" + this.mPhoneId + "]", "handleDtmf: sessionId " + sessionId + " code " + code + " mode " + mode + " operation " + operation);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putInt(AuthenticationHeaders.HEADER_PARAM_CODE, code);
        bundle.putInt("mode", mode);
        bundle.putInt("operation", operation);
        bundle.putParcelable("result", result);
        sendMessage(23, (Object) bundle);
    }

    public void sendText(int sessionId, String text, int len) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "sendText: sessionId " + sessionId + " text " + text + " len " + len);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putString("text", text);
        bundle.putInt("len", len);
        sendMessage(51, (Object) bundle);
    }

    public void holdCall(int sessionId, Message result) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "holdCall: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putParcelable("result", result);
        sendMessage(17, (Object) bundle);
    }

    public void resumeCall(int sessionId, Message result) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "resumeCall: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putParcelable("result", result);
        sendMessage(18, (Object) bundle);
    }

    public void holdVideo(int sessionId, Message result) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "holdVideo: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putParcelable("result", result);
        sendMessage(26, (Object) bundle);
    }

    public void resumeVideo(int sessionId, Message result) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "resumeVideo: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putParcelable("result", result);
        sendMessage(27, (Object) bundle);
    }

    public void startCamera(int sessionId, int cameraId) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "startCamera: sessionId: " + sessionId + ", cameraId: " + cameraId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putInt("cameraId", cameraId);
        sendMessage(28, (Object) bundle);
    }

    public void stopCamera() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "stopCamera");
        sendMessage(30);
    }

    public void mergeCall(int sessionId1, int sessionId2, String confUri, int callType, String eventSubscribe, String dialogType, String origUri, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd, HashMap<String, String> extraHeaders, Message result) {
        HashMap<String, String> hashMap = extraHeaders;
        IMSLog.i(LOG_TAG, this.mPhoneId, "mergeCall: ");
        Bundle bundle = new Bundle();
        bundle.putInt("session1", sessionId1);
        bundle.putInt("session2", sessionId2);
        bundle.putString("confuri", confUri);
        bundle.putInt("calltype", callType);
        bundle.putString("eventSubscribe", eventSubscribe);
        bundle.putString("dialogType", dialogType);
        bundle.putString("origUri", origUri);
        bundle.putString("referUriType", referUriType);
        bundle.putString("removeReferUriType", removeReferUriType);
        bundle.putString("referUriAsserted", referUriAsserted);
        bundle.putString("useAnonymousUpdate", useAnonymousUpdate);
        bundle.putBoolean("supportPrematureEnd", supportPrematureEnd);
        if (hashMap != null) {
            bundle.putSerializable("extraHeaders", hashMap);
        }
        bundle.putParcelable("result", result);
        sendMessage(19, (Object) bundle);
    }

    public void conference(String[] participants, String confUri, int callType, String eventSubscribe, String dialogType, String origUri, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd, Message result) {
        Bundle bundle = new Bundle();
        bundle.putString("confuri", confUri);
        bundle.putInt("calltype", callType);
        bundle.putString("eventSubscribe", eventSubscribe);
        bundle.putString("dialogType", dialogType);
        bundle.putStringArray("participants", participants);
        bundle.putString("origUri", origUri);
        bundle.putString("referUriType", referUriType);
        bundle.putString("removeReferUriType", removeReferUriType);
        bundle.putString("referUriAsserted", referUriAsserted);
        bundle.putString("useAnonymousUpdate", useAnonymousUpdate);
        bundle.putBoolean("supportPrematureEnd", supportPrematureEnd);
        bundle.putParcelable("result", result);
        sendMessage(36, (Object) bundle);
    }

    public void extendToConfCall(String[] participants, String confUri, int callType, String eventSubscribe, String dialogType, int sessId, String origUri, String referUriType, String removeReferUriType, String referUriAsserted, String useAnonymousUpdate, boolean supportPrematureEnd) {
        Bundle bundle = new Bundle();
        bundle.putString("confuri", confUri);
        bundle.putInt("calltype", callType);
        bundle.putString("eventSubscribe", eventSubscribe);
        bundle.putString("dialogType", dialogType);
        bundle.putStringArray("participants", participants);
        bundle.putInt("sessId", sessId);
        bundle.putString("origUri", origUri);
        bundle.putString("referUriType", referUriType);
        bundle.putString("removeReferUriType", removeReferUriType);
        bundle.putString("referUriAsserted", referUriAsserted);
        bundle.putString("useAnonymousUpdate", useAnonymousUpdate);
        bundle.putBoolean("supportPrematureEnd", supportPrematureEnd);
        sendMessage(107, (Object) bundle);
    }

    public void updateConfCall(int confCallSessionId, int cmd, int participantId, String participant) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "updateConfCall  ConfSession " + confCallSessionId + " cmd " + cmd + " participantId " + participantId);
        Bundle bundle = new Bundle();
        bundle.putInt("confsession", confCallSessionId);
        bundle.putInt("updateCmd", cmd);
        bundle.putInt("participantId", participantId);
        bundle.putString("participant", participant);
        sendMessage(35, (Object) bundle);
    }

    public void transferCall(int sessionId, String targetUri, int replacingSessionId, Message result) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "transferCall: sessionId " + sessionId + " targetUri " + IMSLog.checker(targetUri) + " replacingSessionId " + replacingSessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putString("targetUri", targetUri);
        if (replacingSessionId > 0) {
            bundle.putInt("replacingSessionId", replacingSessionId);
        }
        bundle.putParcelable("result", result);
        sendMessage(20, (Object) bundle);
    }

    public void cancelTransferCall(int sessionId, Message result) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "cancelTransferCall: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putParcelable("result", result);
        sendMessage(45, (Object) bundle);
    }

    public void pullingCall(String pullingUri, String targetUri, String origUri, Dialog targetDialog, List<String> p2p, Message result) {
        int i = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("pullingCall: pullingUri=");
        sb.append(IMSLog.checker(pullingUri));
        sb.append(", targetUri=");
        sb.append(IMSLog.checker(targetUri));
        sb.append(", origUri=");
        sb.append(IMSLog.checker(origUri));
        sb.append(", targetDialog=");
        sb.append(IMSLog.checker(targetDialog + ""));
        IMSLog.i(LOG_TAG, i, sb.toString());
        Bundle bundle = new Bundle();
        bundle.putString("pullingUri", pullingUri);
        bundle.putString("targetUri", targetUri);
        bundle.putString("origUri", origUri);
        bundle.putParcelable("targetDialog", targetDialog);
        if (p2p != null) {
            bundle.putStringArrayList("p2p", (ArrayList) p2p);
        }
        bundle.putParcelable("result", result);
        sendMessage(29, (Object) bundle);
    }

    public void publishDialog(String origUri, String dispName, String xmlBody, int expires, Message result, boolean needDelay) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "publishDialog: origUri=" + IMSLog.checker(origUri) + ", dispName=" + IMSLog.checker(dispName) + ", expires=" + expires + "");
        Bundle bundle = new Bundle();
        bundle.putString("origUri", origUri);
        bundle.putString("dispName", dispName);
        bundle.putString("body", xmlBody);
        bundle.putInt("expires", expires);
        bundle.putParcelable("result", result);
        if (needDelay) {
            sendMessageDelayed(47, (Object) bundle, 500);
        } else {
            sendMessage(47, (Object) bundle);
        }
    }

    public void acceptCallTranfer(int sessionId, boolean accepted, int status, String reason) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "acceptCallTransfer: session " + sessionId + " accepted " + accepted + " status " + status + " reason " + reason);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putBoolean("accepted", accepted);
        if (status > 0) {
            bundle.putInt("status", status);
            bundle.putString("reason", reason);
        }
        sendMessage(21, (Object) bundle);
    }

    public void startRecord(int sessionId, String filePath) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "startRecord: sessionId " + sessionId + " filePath " + filePath);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putString(FtIntent.Extras.EXTRA_FILE_PATH, filePath);
        sendMessage(56, (Object) bundle);
    }

    public void stopRecord(int sessionId) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "stopRecord: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        sendMessage(57, (Object) bundle);
    }

    public void startCmcRecord(int sessionId, int audioSource, int outputFormat, long maxFileSize, int maxDuration, String outputPath, int audioEncodingBR, int audioChannels, int audioSamplingR, int audioEncoder, int durationInterval, long fileSizeInterval, String author) {
        int i = sessionId;
        String str = outputPath;
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "startCmcRecord: sessionId " + i + " filePath " + str);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", i);
        bundle.putInt("audioSource", audioSource);
        bundle.putInt("outputFormat", outputFormat);
        bundle.putLong("maxFileSize", maxFileSize);
        bundle.putInt("maxDuration", maxDuration);
        bundle.putString("outputPath", str);
        bundle.putInt("audioEncodingBR", audioEncodingBR);
        bundle.putInt("audioChannels", audioChannels);
        bundle.putInt("audioSamplingR", audioSamplingR);
        bundle.putInt("audioEncoder", audioEncoder);
        bundle.putInt("durationInterval", durationInterval);
        bundle.putLong("fileSizeInterval", fileSizeInterval);
        bundle.putString("author", author);
        sendMessage(58, (Object) bundle);
    }

    public void sendSms(String scaUri, String localUri, String contentType, byte[] data, boolean isDeliveryReport, String callId, Message result) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "sendSms: scaUri " + IMSLog.checker(scaUri) + " localUri " + IMSLog.checker(localUri) + " contentType " + contentType + " isDeleveryReport " + isDeliveryReport + " callId " + callId);
        Bundle bundle = new Bundle();
        bundle.putString("sca", scaUri);
        bundle.putString("localuri", localUri);
        bundle.putString("contentType", contentType);
        bundle.putByteArray("pdu", data);
        bundle.putBoolean("isDeliveryReport", isDeliveryReport);
        bundle.putParcelable("result", result);
        bundle.putString("callId", callId);
        sendMessage(31, (Object) bundle);
    }

    public void sendSmsRpAckResponse(String callId) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "sendSmsRpAckResponse: callId " + callId);
        sendMessage(32, (Object) callId);
    }

    public void sendSmsResponse(String callId, int status) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "sendSmsResponse: callId " + callId);
        sendMessage(33, status, 0, callId);
    }

    public void modifyCallType(int sessionId, int oldType, int newType) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putInt("oldType", oldType);
        bundle.putInt("newType", newType);
        sendMessage(104, (Object) bundle);
    }

    public void replyModifyCallType(int sessionId, int curType, int repType, int reqType, String cmcCallTime) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putInt("reqType", reqType);
        bundle.putInt("curType", curType);
        bundle.putInt("repType", repType);
        bundle.putString("cmcCallTime", cmcCallTime);
        sendMessage(105, (Object) bundle);
    }

    public void rejectModifyCallType(int sessionId, int reason) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putInt("reason", reason);
        sendMessage(106, (Object) bundle);
    }

    public void updateCall(int sessionId, int action, int codecType, SipReason reason) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "updateCall(): sessionId " + sessionId + ", action " + action);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putInt("action", action);
        bundle.putInt("codecType", codecType);
        bundle.putInt("cause", reason.getCause());
        bundle.putString("reasonText", reason.getText());
        sendMessage(37, (Object) bundle);
    }

    public void sendInfo(int sessionId, int type, int ussdType, AdditionalContents ac, Message result) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "sendInfo: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        bundle.putInt("calltype", type);
        bundle.putInt("ussdtype", ussdType);
        bundle.putParcelable("result", result);
        if (ac != null) {
            bundle.putString("additionalContentsContents", ac.contents());
            bundle.putString("additionalContentsMime", ac.mimeType());
        }
        sendMessage(48, (Object) bundle);
    }

    public void sendCmcInfo(int sessionId, AdditionalContents ac) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "sendCmcInfo: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        if (ac != null) {
            bundle.putString("additionalContentsContents", ac.contents());
            bundle.putString("additionalContentsMime", ac.mimeType());
        }
        sendMessage(59, (Object) bundle);
    }

    public void startVideoEarlyMedia(int sessionId) {
        Log.i(LOG_TAG, "startVideoEarlyMedia: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        sendMessage(54, (Object) bundle);
    }

    public void handleCmcCsfb(int sessionId) {
        Log.i(LOG_TAG, "handleCmcCsfb: sessionId " + sessionId);
        Bundle bundle = new Bundle();
        bundle.putInt("sessionId", sessionId);
        sendMessage(55, (Object) bundle);
    }

    public void updateCallwaitingStatus() {
        if (!this.mImsFramework.getBoolean(this.mPhoneId, GlobalSettingsConstants.SS.CALLWAITING_BY_NETWORK, false)) {
            sendMessage(39);
        }
    }

    public void requestPublish(PresenceInfo presenceInfo, Message result) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("presenceInfo", presenceInfo);
        bundle.putParcelable("result", result);
        sendMessage(41, (Object) bundle);
    }

    public void requestUnpublish() {
        sendMessage(42);
    }

    public void sendMediaEvent(int target, int event, int eventType) {
        Bundle bundle = new Bundle();
        bundle.putInt(SoftphoneNamespaces.SoftphoneCallHandling.TARGET, target);
        bundle.putInt("event", event);
        bundle.putInt("eventType", eventType);
        sendMessage(1001, (Object) bundle);
    }

    private void initState() {
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mReadyState, this.mDefaultState);
        addState(this.mRegisteringState, this.mReadyState);
        addState(this.mRegisteredState, this.mReadyState);
        addState(this.mReRegisteringState, this.mRegisteredState);
        addState(this.mDeregisteringState, this.mReadyState);
        addState(this.mTerminatingState, this.mReadyState);
        addState(this.mProhibitedState, this.mDefaultState);
        addState(this.mEmergencyState, this.mReadyState);
        setInitialState(this.mInitialState);
        start();
    }

    /* access modifiers changed from: private */
    public void setDestState(UserAgentState state) {
        Log.i("UserAgent[" + this.mPhoneId + "]", "setDestState to : " + state);
        this.mDestState = state;
        if (state == UserAgentState.DEFAULT) {
            transitionTo(this.mDefaultState);
        } else if (state == UserAgentState.READY) {
            transitionTo(this.mReadyState);
        } else if (state == UserAgentState.INITIAL) {
            transitionTo(this.mInitialState);
        } else if (state == UserAgentState.REGISTERING) {
            transitionTo(this.mRegisteringState);
        } else if (state == UserAgentState.REGISTERED) {
            transitionTo(this.mRegisteredState);
        } else if (state == UserAgentState.REREGISTERING) {
            transitionTo(this.mReRegisteringState);
        } else if (state == UserAgentState.DEREGISTERING) {
            transitionTo(this.mDeregisteringState);
        } else if (state == UserAgentState.TERMINATING) {
            transitionTo(this.mTerminatingState);
        } else if (state == UserAgentState.EMERGENCY) {
            transitionTo(this.mEmergencyState);
        } else if (state == UserAgentState.PROHIBITTED) {
            transitionTo(this.mProhibitedState);
        } else {
            Log.e(LOG_TAG, "Unexpected State : " + state);
            transitionTo(this.mDefaultState);
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            Log.i(UserAgent.LOG_TAG, UserAgent.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 13) {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_DEREGISTERED_TIMEOUT");
                if (UserAgent.this.mListener == null) {
                    return true;
                }
                UserAgent.this.mListener.onDeregistered(UserAgent.this, true, SipErrorBase.OK, 0);
                return true;
            } else if (i != 41) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "Unexpected event " + msg.what + ". current state is " + UserAgent.this.getCurrentState().getName());
                return false;
            } else {
                Message result = (Message) ((Bundle) msg.obj).getParcelable("result");
                FlatBufferBuilder builder = new FlatBufferBuilder(0);
                GeneralResponse.startGeneralResponse(builder);
                GeneralResponse.addHandle(builder, (long) UserAgent.this.mHandle);
                GeneralResponse.addResult(builder, 1);
                builder.finish(GeneralResponse.endGeneralResponse(builder));
                AsyncResult.forMessage(result, GeneralResponse.getRootAsGeneralResponse(builder.dataBuffer()), (Throwable) null);
                result.sendToTarget();
                return true;
            }
        }
    }

    private class InitialState extends State {
        private InitialState() {
        }

        public void enter() {
            Log.i(UserAgent.LOG_TAG, UserAgent.this.getCurrentState().getName() + " enter.");
            int unused = UserAgent.this.mHandle = -1;
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                UserAgent.this.mStackIf.createUA(UserAgent.this.mUaProfile, UserAgent.this.obtainMessage(2));
                return true;
            } else if (i == 2) {
                AsyncResult ar = (AsyncResult) msg.obj;
                GeneralResponse gr = (GeneralResponse) ar.result;
                if (ar.exception == null && gr != null) {
                    Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "UA created. handle " + gr.handle() + " result " + gr.result() + " reason " + gr.reason());
                    if (gr.result() == 0) {
                        int unused = UserAgent.this.mHandle = (int) gr.handle();
                        if (UserAgent.this.mImsProfile.isUicclessEmergency()) {
                            IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "No need for emergency registration. Move to EmergencyState.");
                            UserAgent userAgent = UserAgent.this;
                            userAgent.transitionTo(userAgent.mEmergencyState);
                        } else {
                            UserAgent userAgent2 = UserAgent.this;
                            userAgent2.transitionTo(userAgent2.mReadyState);
                        }
                        UserAgent.this.mStackIf.registerUaListener(UserAgent.this.mHandle, new EventListener());
                        if (UserAgent.this.mListener != null) {
                            UserAgent.this.mListener.onCreated(UserAgent.this);
                        }
                        return true;
                    } else if (gr.reason() == 6) {
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "create() failed. notify with null agent");
                        if (UserAgent.this.mListener != null) {
                            UserAgent.this.mListener.onCreated((UserAgent) null);
                        }
                        return true;
                    }
                }
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "create() failed. retry 3 seconds later ");
                UserAgent.this.sendMessageDelayed(3, 3000);
                return true;
            } else if (i == 3) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "retry UA creation...");
                UserAgent.this.create();
                return true;
            } else if (i == 4) {
                UserAgent.this.deferMessage(msg);
                return true;
            } else if (i == 5) {
                UserAgent.this.mStackIf.deleteUA(UserAgent.this.mHandle, UserAgent.this.obtainMessage(5));
                UserAgent.this.setDestState(UserAgentState.TERMINATING);
                return true;
            } else if (i != 10) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "Unexpected event " + msg.what + ". current state is " + UserAgent.this.getCurrentState().getName());
                return false;
            } else {
                IMSLog.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "Event " + msg.what + " received in  " + UserAgent.this.getCurrentState().getName() + " This shouldn't be handled here - defer");
                UserAgent.this.deferMessage(msg);
                return true;
            }
        }
    }

    private class ReadyState extends State {
        private ReadyState() {
        }

        public void enter() {
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, access$100, UserAgent.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "UserAgent is already created.");
                return true;
            } else if (i == 12) {
                UserAgent.this.setDestState(UserAgentState.READY);
                return true;
            } else if (i == 15) {
                UserAgent.this.mStackIf.endCall(UserAgent.this.mHandle, msg.arg1, (SipReason) msg.obj, (Message) null);
                return true;
            } else if (i == 34) {
                List<String> paniList = (List) msg.obj;
                if (UserAgent.this.mImsProfile != null && Mno.fromName(UserAgent.this.mImsProfile.getMnoName()).isKor()) {
                    ContentValues cv = new ContentValues();
                    cv.put("real_pani", paniList.get(0));
                    ImsSharedPrefHelper.put(UserAgent.this.mPhoneId, UserAgent.this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, cv);
                    String fakePani = ImsSharedPrefHelper.getString(UserAgent.this.mPhoneId, UserAgent.this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, "fake_pani", "");
                    if (!TextUtils.isEmpty(fakePani)) {
                        paniList.set(0, fakePani);
                    }
                }
                UserAgent.this.mStackIf.updatePani(UserAgent.this.mHandle, paniList);
                return true;
            } else if (i == 44) {
                UserAgent.this.mStackIf.updateGeolocation(UserAgent.this.mHandle, (LocationInfo) msg.obj);
                return true;
            } else if (i == 50) {
                UserAgent.this.mStackIf.updateRat(UserAgent.this.mHandle, msg.arg1);
                return true;
            } else if (i != 52) {
                if (i != 100) {
                    if (i == 108) {
                        Bundle bundle = (Bundle) msg.obj;
                        UserAgent.this.mStackIf.updateAudioInterface(UserAgent.this.mHandle, bundle.getString("mode"), (Message) bundle.getParcelable("result"));
                        return true;
                    } else if (i == 4) {
                        UserAgent.this.mStackIf.deleteUA(UserAgent.this.mHandle, UserAgent.this.obtainMessage(5));
                        UserAgent.this.setDestState(UserAgentState.TERMINATING);
                        return true;
                    } else if (i != 5) {
                        if (i == 6) {
                            ArrayList arrayList = new ArrayList();
                            arrayList.addAll(UserAgent.this.mUaProfile.getServiceList());
                            UserAgent.this.mStackIf.register(UserAgent.this.mHandle, UserAgent.this.mUaProfile.getPcscfIp(), UserAgent.this.mUaProfile.getPcscfPort(), UserAgent.this.mUaProfile.getRegExpires(), arrayList, UserAgent.this.mUaProfile.getLinkedImpuList(), UserAgent.this.mUaProfile.getOwnCapabilities(), UserAgent.this.mThirdPartyFeatureTags, UserAgent.this.mUaProfile.getAccessToken(), UserAgent.this.mUaProfile.getAuthServerUrl(), UserAgent.this.obtainMessage(7));
                            UserAgent.this.setDestState(UserAgentState.REGISTERING);
                            return true;
                        } else if (i == 9) {
                            UserAgent.this.mStackIf.sendAuthResponse(UserAgent.this.mHandle, msg.arg1, (String) msg.obj);
                            return true;
                        } else if (i != 10) {
                            return false;
                        } else {
                            IMSLog.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "Event " + msg.what + " received in  " + UserAgent.this.getCurrentState().getName() + " This shouldn't be handled here - defer");
                            UserAgent.this.deferMessage(msg);
                            return true;
                        }
                    }
                }
                UserAgent.this.setDestState(UserAgentState.INITIAL);
                return true;
            } else {
                UserAgent.this.mStackIf.updateTimeInPlani(UserAgent.this.mHandle, ((Long) msg.obj).longValue());
                return true;
            }
        }
    }

    private class RegisteringState extends State {
        private RegisteringState() {
        }

        public void enter() {
            Log.i(UserAgent.LOG_TAG, UserAgent.this.getCurrentState().getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 4) {
                UserAgent.this.mStackIf.deleteUA(UserAgent.this.mHandle, UserAgent.this.obtainMessage(5));
                UserAgent.this.setDestState(UserAgentState.TERMINATING);
                return true;
            } else if (i != 10) {
                boolean z = false;
                if (i == 38) {
                    IStackIF access$500 = UserAgent.this.mStackIf;
                    int access$000 = UserAgent.this.mHandle;
                    if (msg.arg1 == 1) {
                        z = true;
                    }
                    access$500.networkSuspended(access$000, z);
                    return true;
                } else if (i == 41) {
                    UserAgent.this.deferMessage(msg);
                    return true;
                } else if (i == 43) {
                    Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + "EVENT_REQUEST_DEREGISTER_INTERNAL");
                    IStackIF access$5002 = UserAgent.this.mStackIf;
                    int access$0002 = UserAgent.this.mHandle;
                    if (msg.arg1 == 1) {
                        z = true;
                    }
                    access$5002.deregister(access$0002, z, UserAgent.this.obtainMessage(11));
                    UserAgent.this.setDestState(UserAgentState.DEREGISTERING);
                    return true;
                } else if (i == 46) {
                    IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_AKA_CHALLENGE_TIME_OUT");
                    if (UserAgent.this.mListener == null) {
                        return true;
                    }
                    UserAgent.this.mListener.onRegistrationError(UserAgent.this, SipErrorBase.OK, 2);
                    return true;
                } else if (i == 900) {
                    Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "[Registering] emergency registration failed. move on to emergency state.");
                    UserAgent.this.setDestState(UserAgentState.EMERGENCY);
                    return true;
                } else if (i != 7) {
                    if (i == 8) {
                        UserAgent.this.setDestState(UserAgentState.REGISTERED);
                        return true;
                    } else if (i == 12) {
                        Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " Defer event " + msg.what);
                        UserAgent.this.deferMessage(msg);
                        return true;
                    } else if (i != 13) {
                        return false;
                    } else {
                        Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_DELETE_UA");
                        if (UserAgent.this.mListener == null) {
                            return true;
                        }
                        UserAgent.this.mListener.onRegistrationError(UserAgent.this, SipErrorBase.OK, 2);
                        return true;
                    }
                } else if (((AsyncResult) msg.obj).exception == null) {
                    return true;
                } else {
                    Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "register() failed. retry in 3 seconds.");
                    UserAgent.this.sendMessageDelayed(6, 3000);
                    UserAgent.this.setDestState(UserAgentState.READY);
                    return true;
                }
            } else {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_REQUEST_DEREGISTER");
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " Defer EVENT_REQUEST_DEREGISTER");
                UserAgent.this.setDestState(UserAgentState.DEREGISTERING);
                return true;
            }
        }
    }

    private class RegisteredState extends State {
        private RegisteredState() {
        }

        public void enter() {
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, access$100, getName() + " enter.");
            onRegistered();
        }

        public void exit() {
            if (UserAgent.this.mDestState != UserAgentState.REGISTERED) {
                if (!(UserAgent.this.mDestState == UserAgentState.DEREGISTERING || UserAgent.this.mDestState == UserAgentState.TERMINATING || UserAgent.this.mListener == null)) {
                    if (UserAgent.this.mError == null) {
                        Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "exit: Unknown error.");
                        SipError unused = UserAgent.this.mError = SipErrorBase.UNKNOWN_LOCAL_ERROR;
                    }
                    UaEventListener access$200 = UserAgent.this.mListener;
                    UserAgent userAgent = UserAgent.this;
                    access$200.onDeregistered(userAgent, false, userAgent.mError, UserAgent.this.mRetryAfter);
                }
                if (UserAgent.this.mDestState != UserAgentState.REREGISTERING) {
                    ImsRegistration unused2 = UserAgent.this.mRegistration = null;
                }
                SipError unused3 = UserAgent.this.mError = null;
            }
        }

        public boolean processMessage(Message msg) {
            boolean retVal;
            Message message = msg;
            int i = message.what;
            if (i != 4) {
                if (i == 6) {
                    retVal = true;
                } else if (i == 101) {
                    retVal = true;
                    onRegInfoNotify((RegInfoChanged) message.obj);
                } else if (i == 102) {
                    retVal = true;
                    if (message.arg1 != -1) {
                        UserAgent.this.updateRouteTable(message.arg1, (String) message.obj);
                    }
                } else if (i == 1000) {
                    retVal = true;
                    UserAgent.this.mStackIf.send((ResipStackRequest) message.obj);
                } else if (i != 1001) {
                    retVal = true;
                    String str = "p2p";
                    String str2 = "targetUri";
                    String str3 = "additionalContentsMime";
                    String str4 = "additionalContentsContents";
                    switch (i) {
                        case 6:
                            break;
                        case 8:
                            IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " reRegistered.");
                            onRegistered();
                            break;
                        case 10:
                            UserAgent.this.mStackIf.deregister(UserAgent.this.mHandle, message.arg1 == 1, UserAgent.this.obtainMessage(11));
                            UserAgent.this.setDestState(UserAgentState.DEREGISTERING);
                            break;
                        case 14:
                            String str5 = str;
                            Bundle bundle = (Bundle) message.obj;
                            String contents = bundle.getString(str4);
                            String mime = bundle.getString(str3);
                            AdditionalContents ac = null;
                            if (!(contents == null || mime == null)) {
                                FlatBufferBuilder builder = new FlatBufferBuilder(0);
                                int contentsOffset = builder.createString((CharSequence) contents);
                                int mimeOffset = builder.createString((CharSequence) mime);
                                AdditionalContents.startAdditionalContents(builder);
                                AdditionalContents.addMimeType(builder, mimeOffset);
                                AdditionalContents.addContents(builder, contentsOffset);
                                builder.finish(AdditionalContents.endAdditionalContents(builder));
                                ac = AdditionalContents.getRootAsAdditionalContents(builder.dataBuffer());
                            }
                            UserAgent.this.mStackIf.makeCall(UserAgent.this.mHandle, bundle.getString("destUri"), bundle.getString("origUri"), bundle.getInt("type"), bundle.getString("dispName"), bundle.getString("dialedNumber"), (String) null, -1, ac, bundle.getString("cli"), bundle.getString("pEmergencyInfoOfAtt"), (HashMap) bundle.getSerializable("additionalSipHeaders"), bundle.getString("alertInfo"), bundle.getBoolean("isLteEpsOnlyAttached"), bundle.getStringArrayList(str5), bundle.getInt("cmcBoundSessionId"), bundle.getBundle(CallConstants.ComposerData.TAG), bundle.getString("replaceCallId"), (Message) bundle.getParcelable("result"));
                            break;
                        case 15:
                            UserAgent.this.mStackIf.endCall(UserAgent.this.mHandle, message.arg1, (SipReason) message.obj, (Message) null);
                            break;
                        case 16:
                            UserAgent.this.mStackIf.answerCall(UserAgent.this.mHandle, message.arg1, message.arg2, (String) message.obj);
                            break;
                        case 17:
                            Bundle bundle2 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.holdCall(UserAgent.this.mHandle, bundle2.getInt("sessionId"), (Message) bundle2.getParcelable("result"));
                            break;
                        case 18:
                            Bundle bundle3 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.resumeCall(UserAgent.this.mHandle, bundle3.getInt("sessionId"), (Message) bundle3.getParcelable("result"));
                            break;
                        case 19:
                            Bundle bundle4 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.mergeCall(UserAgent.this.mHandle, bundle4.getInt("session1"), bundle4.getInt("session2"), bundle4.getString("confuri"), bundle4.getInt("calltype"), bundle4.getString("eventSubscribe"), bundle4.getString("dialogType"), bundle4.getString("origUri"), bundle4.getString("referUriType"), bundle4.getString("removeReferUriType"), bundle4.getString("referUriAsserted"), bundle4.getString("useAnonymousUpdate"), bundle4.getBoolean("supportPrematureEnd"), (HashMap) bundle4.getSerializable("extraHeaders"), (Message) bundle4.getParcelable("result"));
                            break;
                        case 20:
                            Bundle bundle5 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.transferCall(UserAgent.this.mHandle, bundle5.getInt("sessionId"), bundle5.getString(str2), bundle5.getInt("replacingSessionId"), (Message) bundle5.getParcelable("result"));
                            break;
                        case 21:
                            Bundle bundle6 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.acceptCallTransfer(UserAgent.this.mHandle, bundle6.getInt("sessionId"), bundle6.getBoolean("accepted"), bundle6.getInt("status"), bundle6.getString("reason"), (Message) null);
                            break;
                        case 22:
                            UserAgent.this.mStackIf.rejectCall(UserAgent.this.mHandle, message.arg1, (SipError) message.obj, (Message) null);
                            break;
                        case 23:
                            Bundle bundle7 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.handleDtmf(UserAgent.this.mHandle, bundle7.getInt("sessionId"), bundle7.getInt(AuthenticationHeaders.HEADER_PARAM_CODE), bundle7.getInt("mode"), bundle7.getInt("operation"), (Message) bundle7.getParcelable("result"));
                            break;
                        case 45:
                            Bundle bundle8 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.cancelTransferCall(UserAgent.this.mHandle, bundle8.getInt("sessionId"), (Message) bundle8.getParcelable("result"));
                            break;
                        case 51:
                            Bundle bundle9 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.sendText(UserAgent.this.mHandle, bundle9.getInt("sessionId"), bundle9.getString("text"), bundle9.getInt("len"));
                            break;
                        default:
                            switch (i) {
                                case 25:
                                    UserAgent.this.mStackIf.progressIncomingCall(UserAgent.this.mHandle, message.arg1, (HashMap) message.obj, (Message) null);
                                    break;
                                case 26:
                                    Bundle bundle10 = (Bundle) message.obj;
                                    UserAgent.this.mStackIf.holdVideo(UserAgent.this.mHandle, bundle10.getInt("sessionId"), (Message) bundle10.getParcelable("result"));
                                    break;
                                case 27:
                                    Bundle bundle11 = (Bundle) message.obj;
                                    UserAgent.this.mStackIf.resumeVideo(UserAgent.this.mHandle, bundle11.getInt("sessionId"), (Message) bundle11.getParcelable("result"));
                                    break;
                                case 28:
                                    Bundle bundle12 = (Bundle) message.obj;
                                    UserAgent.this.mStackIf.startCamera(UserAgent.this.mHandle, bundle12.getInt("sessionId"), bundle12.getInt("cameraId"));
                                    break;
                                case 29:
                                    Bundle bundle13 = (Bundle) message.obj;
                                    UserAgent.this.mStackIf.pullingCall(UserAgent.this.mHandle, bundle13.getString("pullingUri"), bundle13.getString(str2), bundle13.getString("origUri"), bundle13.getParcelable("targetDialog"), bundle13.getStringArrayList(str), (Message) bundle13.getParcelable("result"));
                                    break;
                                case 30:
                                    UserAgent.this.mStackIf.stopCamera(UserAgent.this.mHandle);
                                    break;
                                case 31:
                                    sendSms((Bundle) message.obj);
                                    break;
                                case 32:
                                    UserAgent.this.mStackIf.sendSmsRpAckResponse(UserAgent.this.mHandle, (String) message.obj);
                                    break;
                                case 33:
                                    UserAgent.this.mStackIf.sendSmsResponse(UserAgent.this.mHandle, (String) message.obj, message.arg1);
                                    break;
                                default:
                                    String str6 = "reason";
                                    switch (i) {
                                        case 35:
                                            Bundle bundle14 = (Bundle) message.obj;
                                            UserAgent.this.mStackIf.updateConfCall(UserAgent.this.mHandle, bundle14.getInt("confsession"), bundle14.getInt("updateCmd"), bundle14.getInt("participantId"), bundle14.getString("participant"));
                                            break;
                                        case 36:
                                            Bundle bundle15 = (Bundle) message.obj;
                                            UserAgent.this.mStackIf.conference(UserAgent.this.mHandle, bundle15.getString("confuri"), bundle15.getInt("calltype"), bundle15.getString("eventSubscribe"), bundle15.getString("dialogType"), bundle15.getStringArray("participants"), bundle15.getString("origUri"), bundle15.getString("referUriType"), bundle15.getString("removeReferUriType"), bundle15.getString("referUriAsserted"), bundle15.getString("useAnonymousUpdate"), bundle15.getBoolean("supportPrematureEnd"), (Message) bundle15.getParcelable("result"));
                                            break;
                                        case 37:
                                            Bundle bundle16 = (Bundle) message.obj;
                                            UserAgent.this.mStackIf.updateCall(bundle16.getInt("sessionId"), bundle16.getInt("action"), bundle16.getInt("codecType"), bundle16.getInt("cause"), bundle16.getString("reasonText"));
                                            break;
                                        case 38:
                                            UserAgent.this.mStackIf.networkSuspended(UserAgent.this.mHandle, message.arg1 == 1);
                                            break;
                                        default:
                                            switch (i) {
                                                case 40:
                                                    UserAgent.this.mStackIf.updateVceConfig(UserAgent.this.mHandle, ((Boolean) message.obj).booleanValue());
                                                    break;
                                                case 41:
                                                    Bundle bundle17 = (Bundle) message.obj;
                                                    UserAgent.this.mStackIf.requestPublish(UserAgent.this.mHandle, bundle17.getParcelable("presenceInfo"), (Message) bundle17.getParcelable("result"));
                                                    break;
                                                case 42:
                                                    UserAgent.this.mStackIf.requestUnpublish(UserAgent.this.mHandle);
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case 47:
                                                            Bundle bundle18 = (Bundle) message.obj;
                                                            UserAgent.this.mStackIf.publishDialog(UserAgent.this.mHandle, bundle18.getString("origUri"), bundle18.getString("dispName"), bundle18.getString("body"), bundle18.getInt("expires"), (Message) bundle18.getParcelable("result"));
                                                            break;
                                                        case 48:
                                                            Bundle bundle19 = (Bundle) message.obj;
                                                            String infoContents = bundle19.getString(str4);
                                                            String mimeType = bundle19.getString(str3);
                                                            FlatBufferBuilder builder2 = new FlatBufferBuilder(0);
                                                            int contentsOffset2 = builder2.createString((CharSequence) infoContents);
                                                            int mimeOffset2 = builder2.createString((CharSequence) mimeType);
                                                            AdditionalContents.startAdditionalContents(builder2);
                                                            AdditionalContents.addMimeType(builder2, mimeOffset2);
                                                            AdditionalContents.addContents(builder2, contentsOffset2);
                                                            builder2.finish(AdditionalContents.endAdditionalContents(builder2));
                                                            UserAgent.this.mStackIf.sendInfo(UserAgent.this.mHandle, bundle19.getInt("sessionId"), bundle19.getInt("calltype"), bundle19.getInt("ussdtype"), AdditionalContents.getRootAsAdditionalContents(builder2.dataBuffer()), (Message) bundle19.getParcelable("result"));
                                                            break;
                                                        case 49:
                                                            UserAgent.this.mStackIf.deleteTcpClientSocket(UserAgent.this.mHandle);
                                                            break;
                                                        default:
                                                            switch (i) {
                                                                case 54:
                                                                    UserAgent.this.mStackIf.startVideoEarlyMedia(UserAgent.this.mHandle, ((Bundle) message.obj).getInt("sessionId"));
                                                                    break;
                                                                case 55:
                                                                    UserAgent.this.mStackIf.handleCmcCsfb(UserAgent.this.mHandle, ((Bundle) message.obj).getInt("sessionId"));
                                                                    break;
                                                                case 56:
                                                                    Bundle bundle20 = (Bundle) message.obj;
                                                                    UserAgent.this.mStackIf.startRecord(UserAgent.this.mHandle, bundle20.getInt("sessionId"), bundle20.getString(FtIntent.Extras.EXTRA_FILE_PATH));
                                                                    break;
                                                                case 57:
                                                                    UserAgent.this.mStackIf.stopRecord(UserAgent.this.mHandle, ((Bundle) message.obj).getInt("sessionId"));
                                                                    break;
                                                                case 58:
                                                                    Bundle bundle21 = (Bundle) message.obj;
                                                                    UserAgent.this.mStackIf.startCmcRecord(UserAgent.this.mHandle, bundle21.getInt("sessionId"), bundle21.getInt("audioSource"), bundle21.getInt("outputFormat"), bundle21.getLong("maxFileSize"), bundle21.getInt("maxDuration"), bundle21.getString("outputPath"), bundle21.getInt("audioEncodingBR"), bundle21.getInt("audioChannels"), bundle21.getInt("audioSamplingR"), bundle21.getInt("audioEncoder"), bundle21.getInt("durationInterval"), bundle21.getLong("fileSizeInterval"), bundle21.getString("author"));
                                                                    break;
                                                                case 59:
                                                                    Bundle bundle22 = (Bundle) message.obj;
                                                                    String cmcInfoContents = bundle22.getString(str4);
                                                                    String cmcMimeType = bundle22.getString(str3);
                                                                    FlatBufferBuilder cmcBuilder = new FlatBufferBuilder(0);
                                                                    int cmcContentsOffset = cmcBuilder.createString((CharSequence) cmcInfoContents);
                                                                    int cmcMimeOffset = cmcBuilder.createString((CharSequence) cmcMimeType);
                                                                    AdditionalContents.startAdditionalContents(cmcBuilder);
                                                                    AdditionalContents.addMimeType(cmcBuilder, cmcMimeOffset);
                                                                    AdditionalContents.addContents(cmcBuilder, cmcContentsOffset);
                                                                    cmcBuilder.finish(AdditionalContents.endAdditionalContents(cmcBuilder));
                                                                    UserAgent.this.mStackIf.sendCmcInfo(UserAgent.this.mHandle, bundle22.getInt("sessionId"), AdditionalContents.getRootAsAdditionalContents(cmcBuilder.dataBuffer()));
                                                                    break;
                                                                default:
                                                                    switch (i) {
                                                                        case 104:
                                                                            Bundle bundle23 = (Bundle) message.obj;
                                                                            UserAgent.this.mStackIf.modifyCallType(bundle23.getInt("sessionId"), bundle23.getInt("oldType"), bundle23.getInt("newType"));
                                                                            break;
                                                                        case 105:
                                                                            Bundle bundle24 = (Bundle) message.obj;
                                                                            UserAgent.this.mStackIf.replyModifyCallType(bundle24.getInt("sessionId"), bundle24.getInt("reqType"), bundle24.getInt("curType"), bundle24.getInt("repType"), bundle24.getString("cmcCallTime"));
                                                                            break;
                                                                        case 106:
                                                                            Bundle bundle25 = (Bundle) message.obj;
                                                                            UserAgent.this.mStackIf.rejectModifyCallType(bundle25.getInt("sessionId"), bundle25.getInt(str6));
                                                                            break;
                                                                        case 107:
                                                                            Bundle bundle26 = (Bundle) message.obj;
                                                                            UserAgent.this.mStackIf.extendToConfCall(UserAgent.this.mHandle, bundle26.getString("confuri"), bundle26.getInt("calltype"), bundle26.getString("eventSubscribe"), bundle26.getString("dialogType"), bundle26.getStringArray("participants"), bundle26.getInt("sessId"), bundle26.getString("origUri"), bundle26.getString("referUriType"), bundle26.getString("removeReferUriType"), bundle26.getString("referUriAsserted"), bundle26.getString("useAnonymousUpdate"), bundle26.getBoolean("supportPrematureEnd"));
                                                                            break;
                                                                        default:
                                                                            switch (i) {
                                                                                case 109:
                                                                                    Bundle bundle27 = (Bundle) message.obj;
                                                                                    UserAgent.this.mStackIf.startLocalRingBackTone(UserAgent.this.mHandle, bundle27.getInt("streamType"), bundle27.getInt("volume"), bundle27.getInt("toneType"), (Message) bundle27.getParcelable("result"));
                                                                                    break;
                                                                                case 110:
                                                                                    UserAgent.this.mStackIf.stopLocalRingBackTone(UserAgent.this.mHandle);
                                                                                    break;
                                                                                case 111:
                                                                                    Bundle bundle28 = (Bundle) message.obj;
                                                                                    UserAgent.this.mStackIf.modifyVideoQuality(bundle28.getInt("sessionId"), bundle28.getInt("oldQual"), bundle28.getInt("newQual"));
                                                                                    break;
                                                                                default:
                                                                                    return false;
                                                                            }
                                                                    }
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
                } else {
                    retVal = true;
                    Bundle bundle29 = (Bundle) message.obj;
                    UserAgent.this.mStackIf.sendMediaEvent(UserAgent.this.mHandle, bundle29.getInt(SoftphoneNamespaces.SoftphoneCallHandling.TARGET), bundle29.getInt("event"), bundle29.getInt("eventType"));
                }
                ArrayList arrayList = new ArrayList();
                arrayList.addAll(UserAgent.this.mUaProfile.getServiceList());
                UserAgent.this.mStackIf.register(UserAgent.this.mHandle, UserAgent.this.mUaProfile.getPcscfIp(), UserAgent.this.mUaProfile.getPcscfPort(), UserAgent.this.mUaProfile.getRegExpires(), arrayList, UserAgent.this.mUaProfile.getLinkedImpuList(), UserAgent.this.mUaProfile.getOwnCapabilities(), UserAgent.this.mThirdPartyFeatureTags, UserAgent.this.mUaProfile.getAccessToken(), UserAgent.this.mUaProfile.getAuthServerUrl(), (Message) null);
                UserAgent.this.setDestState(UserAgentState.REREGISTERING);
            } else {
                retVal = true;
                UserAgent.this.mStackIf.deregister(UserAgent.this.mHandle, true, UserAgent.this.obtainMessage(11));
                UserAgent.this.setDestState(UserAgentState.DEREGISTERING);
            }
            return retVal;
        }

        private void onRegistered() {
            UserAgent userAgent = UserAgent.this;
            ImsRegistration unused = userAgent.mRegistration = userAgent.buildImsRegistration();
            UserAgent.this.mStackIf.setPreferredImpu(UserAgent.this.mHandle, UserAgent.this.mRegistration.getPreferredImpu().getUri().toString());
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onRegistered(UserAgent.this);
            }
        }

        private void onRegInfoNotify(RegInfoChanged regInfo) {
            IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "onRegInfoNotify:");
            if (UserAgent.this.mRegistration == null) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "onRegInfoNotify: unexpected RegInfoNotify. mHandle " + UserAgent.this.mHandle);
                return;
            }
            Contact[] contactList = new Contact[regInfo.contactsLength()];
            for (int i = 0; i < contactList.length; i++) {
                contactList[i] = regInfo.contacts(i);
            }
            for (Contact contact : contactList) {
                NameAddr addr = new NameAddr(contact.displayName(), ImsUri.parse(contact.uri()));
                Log.i(UserAgent.LOG_TAG, "onRegInfoNotify: " + addr + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + contact.state());
                if (contact.state() == 1) {
                    UserAgent.this.addImpu(addr);
                    UserAgent.this.addDevice(addr);
                } else if (contact.state() == 2) {
                    UserAgent.this.removeImpu(addr.getUri());
                    UserAgent.this.removeDevice(addr.getUri());
                }
            }
            UserAgent userAgent = UserAgent.this;
            ImsRegistration unused = userAgent.mRegistration = userAgent.buildImsRegistration();
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onRegistered(UserAgent.this);
            }
        }

        private void sendSms(Bundle bundle) {
            Bundle bundle2 = bundle;
            String scaUri = bundle2.getString("sca");
            String localUri = bundle2.getString("localuri");
            String hexStr = UserAgent.bytesToHex(bundle2.getByteArray("pdu"));
            String contentType = bundle2.getString("contentType");
            if (contentType == null) {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "sendSms: null contentType. ");
                return;
            }
            String[] cTypes = contentType.split("/");
            if (cTypes.length < 2) {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "sendSms: invalid contentType. " + contentType);
                return;
            }
            UserAgent.this.mStackIf.sendSms(UserAgent.this.mHandle, scaUri, localUri, hexStr, cTypes[0], cTypes[1], bundle2.getString("callId"), (Message) bundle2.getParcelable("result"));
        }
    }

    private class ReRegisteringState extends State {
        private ReRegisteringState() {
        }

        public void enter() {
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, access$100, getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 8) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_REGISTERD");
                UserAgent.this.setDestState(UserAgentState.REGISTERED);
                return true;
            } else if (i == 10) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_REQUEST_DEREGISTER");
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " Defer EVENT_REQUEST_DEREGISTER");
                UserAgent.this.deferMessage(msg);
                return true;
            } else if (i == 13) {
                Log.i(UserAgent.LOG_TAG, getName() + " EVENT_DEREGISTERED_TIMEOUT");
                if (UserAgent.this.mListener != null) {
                    UserAgent.this.mListener.onDeregistered(UserAgent.this, true, SipErrorBase.OK, 0);
                }
                UserAgent.this.setDestState(UserAgentState.DEREGISTERING);
                return true;
            } else if (i != 31 && i != 41 && i != 42) {
                return false;
            } else {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " Defer event " + msg.what);
                UserAgent.this.deferMessage(msg);
                return true;
            }
        }
    }

    private class DeregisteringState extends State {
        private DeregisteringState() {
        }

        public void enter() {
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, access$100, getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 4) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DELETE_UA");
                UserAgent.this.mStackIf.deleteUA(UserAgent.this.mHandle, UserAgent.this.obtainMessage(5));
                UserAgent.this.setDestState(UserAgentState.TERMINATING);
                return true;
            } else if (i != 800) {
                switch (i) {
                    case 10:
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_REQUEST_DEREGISTER");
                        Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " UA is already being deregisted.");
                        return true;
                    case 11:
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DEREGISTER_COMPELETE");
                        return true;
                    case 12:
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DEREGISTERED");
                        UserAgent.this.mTelephonyManager.setImsRegistrationState(UserAgent.this.mPhoneId, false);
                        if (UserAgent.this.mUaProfile == null || (!UserAgent.this.mUaProfile.getPdn().equals(DeviceConfigManager.IMS) && UserAgent.this.getImsProfile().getCmcType() == 0)) {
                            if (UserAgent.this.mListener != null) {
                                UserAgent.this.mListener.onDeregistered(UserAgent.this, true, SipErrorBase.OK, 0);
                            }
                            UserAgent.this.sendMessage(4);
                            return true;
                        }
                        int delay = 600;
                        Mno mno = UserAgent.this.mUaProfile.getMno();
                        if (mno == Mno.MAGTICOM_GE || mno == Mno.MEGAFON_RUSSIA || mno == Mno.VODAFONE || mno == Mno.CTC || mno == Mno.CTCMO) {
                            delay = 1000;
                        }
                        UserAgent userAgent = UserAgent.this;
                        userAgent.sendMessageDelayed(userAgent.obtainMessage(800), (long) delay);
                        return true;
                    case 13:
                        IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DEREGISTERED_TIMEOUT");
                        if (UserAgent.this.mListener == null) {
                            return true;
                        }
                        UserAgent.this.mListener.onDeregistered(UserAgent.this, true, SipErrorBase.OK, 0);
                        return true;
                    default:
                        return false;
                }
            } else {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DELAYED_DEREGISTERED");
                if (UserAgent.this.mListener != null) {
                    UserAgent.this.mListener.onDeregistered(UserAgent.this, true, SipErrorBase.OK, 0);
                }
                UserAgent.this.sendMessage(4);
                return true;
            }
        }
    }

    private class TerminatingState extends State {
        private TerminatingState() {
        }

        public void enter() {
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, access$100, getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 4) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_DELETE_UA");
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " UA is already being deleted.");
                return true;
            } else if (i == 5) {
                if (UserAgent.this.mHandle != -1) {
                    UserAgent.this.mStackIf.unRegisterUaListener(UserAgent.this.mHandle);
                }
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " EVENT_UA_DELETED");
                UserAgent.this.setDestState(UserAgentState.INITIAL);
                return true;
            } else if (i == 10) {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_REQUEST_DEREGISTER");
                IMSLog.e(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, getName() + " UA is already being deregisted.");
                return true;
            } else if (i != 11) {
                return false;
            } else {
                Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", getName() + " EVENT_DEREGISTERED");
                return true;
            }
        }
    }

    private class EmergencyState extends State {
        private EmergencyState() {
        }

        public void enter() {
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, access$100, getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            Message message = msg;
            int i = message.what;
            if (i == 6) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "register is not required for emergency call.");
                return true;
            } else if (i == 10) {
                IMSLog.i(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "deregister is not required for emergency call. delete UA.");
                UserAgent.this.sendMessage(4);
                return true;
            } else if (i == 23) {
                Bundle bundle = (Bundle) message.obj;
                UserAgent.this.mStackIf.handleDtmf(UserAgent.this.mHandle, bundle.getInt("sessionId"), bundle.getInt(AuthenticationHeaders.HEADER_PARAM_CODE), bundle.getInt("mode"), bundle.getInt("operation"), (Message) bundle.getParcelable("result"));
                return true;
            } else if (i == 37) {
                Bundle bundle2 = (Bundle) message.obj;
                UserAgent.this.mStackIf.updateCall(bundle2.getInt("sessionId"), bundle2.getInt("action"), bundle2.getInt("codecType"), bundle2.getInt("cause"), bundle2.getString("reasonText"));
                return true;
            } else if (i == 51) {
                Bundle bundle3 = (Bundle) message.obj;
                UserAgent.this.mStackIf.sendText(UserAgent.this.mHandle, bundle3.getInt("sessionId"), bundle3.getString("text"), bundle3.getInt("len"));
                return true;
            } else if (i != 102) {
                if (i == 1001) {
                    Bundle bundle4 = (Bundle) message.obj;
                    UserAgent.this.mStackIf.sendMediaEvent(UserAgent.this.mHandle, bundle4.getInt(SoftphoneNamespaces.SoftphoneCallHandling.TARGET), bundle4.getInt("event"), bundle4.getInt("eventType"));
                    return true;
                } else if (i == 14) {
                    Bundle bundle5 = (Bundle) message.obj;
                    UserAgent.this.mStackIf.makeCall(UserAgent.this.mHandle, bundle5.getString("destUri"), bundle5.getString("origUri"), bundle5.getInt("type"), bundle5.getString("dispName"), bundle5.getString("dialedNumber"), UserAgent.this.mUaProfile.getPcscfIp(), UserAgent.this.mUaProfile.getPcscfPort(), (AdditionalContents) null, (String) null, bundle5.getString("PEmergencyInfoOfAtt"), (HashMap<String, String>) null, bundle5.getString("alertInfo"), bundle5.getBoolean("isLteEpsOnlyAttached"), bundle5.getStringArrayList("p2p"), bundle5.getInt("cmcBoundSessionId"), bundle5.getBundle(CallConstants.ComposerData.TAG), bundle5.getString("replaceCallId"), (Message) bundle5.getParcelable("result"));
                    return true;
                } else if (i == 15) {
                    UserAgent.this.mStackIf.endCall(UserAgent.this.mHandle, message.arg1, (SipReason) message.obj, (Message) null);
                    return true;
                } else if (i == 109) {
                    Bundle bundle6 = (Bundle) message.obj;
                    UserAgent.this.mStackIf.startLocalRingBackTone(UserAgent.this.mHandle, bundle6.getInt("streamType"), bundle6.getInt("volume"), bundle6.getInt("toneType"), (Message) bundle6.getParcelable("result"));
                    return true;
                } else if (i != 110) {
                    switch (i) {
                        case 104:
                            Bundle bundle7 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.modifyCallType(bundle7.getInt("sessionId"), bundle7.getInt("oldType"), bundle7.getInt("newType"));
                            return true;
                        case 105:
                            Bundle bundle8 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.replyModifyCallType(bundle8.getInt("sessionId"), bundle8.getInt("reqType"), bundle8.getInt("curType"), bundle8.getInt("repType"), bundle8.getString("cmcCallTime"));
                            return true;
                        case 106:
                            Bundle bundle9 = (Bundle) message.obj;
                            UserAgent.this.mStackIf.rejectModifyCallType(bundle9.getInt("sessionId"), bundle9.getInt("reason"));
                            return true;
                        default:
                            return false;
                    }
                } else {
                    UserAgent.this.mStackIf.stopLocalRingBackTone(UserAgent.this.mHandle);
                    return true;
                }
            } else if (message.arg1 == -1) {
                return true;
            } else {
                UserAgent.this.updateRouteTable(message.arg1, (String) message.obj);
                return true;
            }
        }
    }

    private class ProhibitedState extends State {
        private ProhibitedState() {
        }

        public void enter() {
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, access$100, getName() + " enter.");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.e(UserAgent.LOG_TAG, access$100, "Unexpected event " + msg.what + ". current state is " + UserAgent.this.getCurrentState().getName());
            return false;
        }
    }

    public class EventListener extends StackEventListener {
        public EventListener() {
        }

        public void onISIMAuthRequested(int handle, String nonce, int tid) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onISIMAuthRequested: handle " + handle + " nonce " + nonce + " tid " + tid);
            if (handle != UserAgent.this.mHandle) {
                IMSLog.e(UserAgent.LOG_TAG, UserAgent.this.mPhoneId, "onISIMAuthRequested: handle mismatch. mHandle " + UserAgent.this.mHandle + " handle " + handle + " tid " + tid);
                return;
            }
            Message response = UserAgent.this.obtainMessage(9, tid);
            if (UserAgent.this.mSimManager.hasVsim()) {
                UserAgent.this.mSimManager.requestSoftphoneAuthentication(nonce, UserAgent.this.mImsProfile.getImpi(), response, UserAgent.this.mImsProfile.getId());
            } else {
                UserAgent.this.mSimManager.requestIsimAuthentication(nonce, response);
            }
        }

        public void onRegistered(int handle, List<String> serviceList, List<String> impuList, SipError error, int retryAfter, int ecmpMode, String sipResponse) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onRegistered: handle=" + handle + " error=" + error + " ecmpMode=" + ecmpMode + " serviceList=" + serviceList);
            if (handle == UserAgent.this.mHandle) {
                if (Mno.fromName(UserAgent.this.mImsProfile.getMnoName()) != Mno.TMOUS || !SipErrorBase.OK.equals(error) || !impuList.isEmpty()) {
                    String unused = UserAgent.this.mRegisterSipResponse = sipResponse;
                    UserAgent.this.mImpuList.clear();
                    for (String impu : impuList) {
                        ImsUri uri = ImsUri.parse(impu);
                        if (uri != null) {
                            UserAgent.this.mImpuList.add(new NameAddr(uri));
                        }
                    }
                    int unused2 = UserAgent.this.mEcmpMode = ecmpMode;
                    SipError unused3 = UserAgent.this.mError = error;
                    if (SipErrorBase.OK.equals(error) || SipErrorBase.OK_SMC.equals(error)) {
                        UserAgent.this.mNotifyServiceList.addAll(serviceList);
                        UserAgent.this.sendMessage(8);
                        return;
                    }
                    if (UserAgent.this.mListener != null) {
                        UserAgent.this.mListener.onRegistrationError(UserAgent.this, error, retryAfter);
                    }
                    if (UserAgent.this.mImsProfile.hasEmergencySupport()) {
                        UserAgent.this.sendMessage(900);
                        return;
                    }
                    return;
                }
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "onRegistered: Empty IRS. deregister.");
                UserAgent.this.deregisterInternal(false);
                if (UserAgent.this.mListener != null) {
                    UserAgent.this.mListener.onRegistrationError(UserAgent.this, SipErrorBase.MISSING_P_ASSOCIATED_URI, 2);
                }
            }
        }

        public void onDeregistered(int handle, SipError error, int retryAfter) {
            Log.d("UserAgent[" + UserAgent.this.mPhoneId + "]", "onDeregistered: handle " + handle + " error " + error + " retryAfter " + retryAfter);
            if (handle == UserAgent.this.mHandle) {
                SipError unused = UserAgent.this.mError = error;
                int unused2 = UserAgent.this.mRetryAfter = retryAfter;
                UserAgent.this.sendMessage(12);
            }
        }

        public void onSubscribed(int handle, SipError error) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onSubscribed: handle " + handle + " error " + error);
            if (handle == UserAgent.this.mHandle && UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onSubscribeError(UserAgent.this, error);
            }
        }

        public void onRegInfoNotification(int handle, RegInfoChanged regInfo) {
            if (handle == UserAgent.this.mHandle) {
                UserAgent.this.sendMessage(101, (Object) regInfo);
            }
        }

        public void onUpdateRouteTableRequested(int handle, int operation, String ipAddress) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onUpdateRouteTableRequested:");
            if (handle != UserAgent.this.mHandle) {
                Log.e("UserAgent[" + UserAgent.this.mPhoneId + "]", "onUpdateRouteTableRequested: handle mismatch. mHandle " + UserAgent.this.mHandle + " handle " + handle);
                return;
            }
            UserAgent userAgent = UserAgent.this;
            userAgent.sendMessage(userAgent.obtainMessage(102, operation, 0, ipAddress));
        }

        public void onRegImpuNotification(int handle, String impu) {
            Log.i("UserAgent[" + UserAgent.this.mPhoneId + "]", "onRegImpuNotification: handle(" + handle + ")");
            if (handle == UserAgent.this.mHandle) {
                int phoneId = UserAgent.this.mSimManager.getSimSlotIndex();
                Intent intent = new Intent("com.sec.imsservice.REGISTERED_IMPU");
                intent.putExtra("phoneid", phoneId);
                intent.putExtra("impu", impu);
                intent.setComponent(new ComponentName("com.android.stk", "com.android.stk.StkCmdReceiver"));
                UserAgent.this.mContext.sendBroadcast(intent);
                intent.setComponent(new ComponentName("com.android.stk2", "com.android.stk2.StkCmdReceiver"));
                UserAgent.this.mContext.sendBroadcast(intent);
            }
        }

        public void onUpdatePani() {
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onUpdatePani(UserAgent.this);
            }
        }

        public void onRefreshRegNotification(int handle) {
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onRefreshRegNotification(handle);
            }
        }

        public void onContactActivated(int handle) {
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, access$100, "onContactActivated: handle(" + handle + ")");
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onContactActivated(UserAgent.this, handle);
            }
        }

        public void onRegEventContactUriNotification(int handle, List<String> contactUriInfoList, int isRegi, String contactUriType) {
            int access$100 = UserAgent.this.mPhoneId;
            IMSLog.i(UserAgent.LOG_TAG, access$100, "onRegEventContactUri: handle(" + handle + ")");
            ArrayList<ImsUri> urilist = new ArrayList<>();
            for (String regeveturi : contactUriInfoList) {
                urilist.add(ImsUri.parse(regeveturi));
            }
            if (UserAgent.this.mListener != null) {
                UserAgent.this.mListener.onRegEventContactUriNotification(handle, urilist, isRegi, contactUriType);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateRouteTable(int op, String address) {
        Log.i("UserAgent[" + this.mPhoneId + "]", "UpdateRouteTable: op " + op + " address " + address);
        if (op == 0) {
            this.mPdnController.requestRouteToHostAddress(this.mPdn, address);
        } else if (op == 1) {
            this.mPdnController.removeRouteToHostAddress(this.mPdn, address);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            char[] cArr = hexArray;
            hexChars[j * 2] = cArr[v >>> 4];
            hexChars[(j * 2) + 1] = cArr[v & 15];
        }
        return new String(hexChars);
    }

    private String extractDomain(UaProfile profile, String imsi) {
        String domain = profile.getDomain();
        if (profile.getMno() == Mno.CMCC || profile.getMno() == Mno.CU) {
            Log.i("UserAgent[" + this.mPhoneId + "]", "extractDomain:  don't use phone-context as domain.");
            return domain;
        } else if (TextUtils.isEmpty(imsi) || !profile.getImpu().contains(imsi)) {
            return domain;
        } else {
            for (NameAddr impu : this.mImpuList) {
                if (!TextUtils.isEmpty(impu.getUri().getPhoneContext())) {
                    Log.i("UserAgent[" + this.mPhoneId + "]", "extractDomain: For IMSI-based registration, use phone-context as domain.");
                    return impu.getUri().getPhoneContext();
                }
            }
            return domain;
        }
    }

    /* access modifiers changed from: private */
    public void addImpu(NameAddr addr) {
        boolean needToAdd = true;
        for (NameAddr na : this.mImpuList) {
            if (addr.getUri().equals(na.getUri()) && TextUtils.equals(addr.getUri().getParam("gr"), na.getUri().getParam("gr"))) {
                needToAdd = false;
                na.setDisplayName(addr.getDisplayName());
            }
        }
        if (needToAdd) {
            this.mImpuList.add(addr);
        }
    }

    /* access modifiers changed from: private */
    public void removeImpu(ImsUri uri) {
        Iterator<NameAddr> it = this.mImpuList.iterator();
        while (it.hasNext()) {
            NameAddr addr = it.next();
            if (addr.getUri().equals(uri) && TextUtils.equals(addr.getUri().getParam("gr"), uri.getParam("gr"))) {
                it.remove();
            }
        }
    }

    /* access modifiers changed from: private */
    public void addDevice(NameAddr addr) {
        boolean needToAdd = true;
        for (NameAddr na : this.mDeviceList) {
            if (addr.getUri().equals(na.getUri()) && TextUtils.equals(addr.getUri().getParam("gr"), na.getUri().getParam("gr"))) {
                needToAdd = false;
                na.setDisplayName(addr.getDisplayName());
            }
        }
        if (needToAdd) {
            this.mDeviceList.add(addr);
        }
    }

    /* access modifiers changed from: private */
    public void removeDevice(ImsUri uri) {
        Iterator<NameAddr> it = this.mDeviceList.iterator();
        while (it.hasNext()) {
            NameAddr addr = it.next();
            if (addr.getUri().equals(uri) && TextUtils.equals(addr.getUri().getParam("gr"), uri.getParam("gr"))) {
                it.remove();
            }
        }
    }

    private NameAddr getFirstImpuByUriType(ImsUri.UriType uriType) {
        return (NameAddr) this.mImpuList.stream().filter(new Predicate(uriType) {
            public final /* synthetic */ ImsUri.UriType f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return UserAgent.lambda$getFirstImpuByUriType$0(this.f$0, (NameAddr) obj);
            }
        }).findFirst().orElse((Object) null);
    }

    static /* synthetic */ boolean lambda$getFirstImpuByUriType$0(ImsUri.UriType uriType, NameAddr addr) {
        return addr.getUri().getUriType() == uriType;
    }

    private NameAddr getPreferredImpu(Set<String> services) {
        NameAddr preferredImpu = null;
        Mno mno = this.mUaProfile.getMno();
        if (mno == Mno.VZW) {
            String impi = this.mUaProfile.getImpi();
            ImsUri impu = ImsUri.parse(this.mUaProfile.getImpu());
            int idx = impi.indexOf(64);
            if (idx > 0 && impu != null) {
                String imsi = impi.substring(0, idx);
                String user = impu.getUser();
                if (!TextUtils.isEmpty(user) && !user.contains(imsi)) {
                    preferredImpu = new NameAddr("", impu);
                }
            }
        } else if (mno == Mno.ATT || this.mImsProfile.isSipUriOnly()) {
            preferredImpu = getFirstImpuByUriType(ImsUri.UriType.SIP_URI);
        } else if (mno.isKor() || mno == Mno.RJIL) {
            preferredImpu = getFirstImpuByUriType(ImsUri.UriType.TEL_URI);
        }
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "getPreferredImpu: " + IMSLog.checker(preferredImpu));
        if (preferredImpu != null) {
            return preferredImpu;
        }
        if (this.mImpuList.isEmpty()) {
            return new NameAddr("", this.mUaProfile.getImpu());
        }
        if (Arrays.asList(ImsProfile.getRcsServiceList()).containsAll(services)) {
            preferredImpu = getFirstImpuByUriType(ImsUri.UriType.TEL_URI);
        }
        if (preferredImpu != null || this.mImpuList.isEmpty()) {
            return preferredImpu;
        }
        return this.mImpuList.get(0);
    }

    /* access modifiers changed from: private */
    public ImsRegistration buildImsRegistration() {
        int subId = this.mSimManager.getSubscriptionId();
        String domain = extractDomain(this.mUaProfile, this.mTelephonyManager.getSubscriberId(subId));
        Set<String> serviceList = new HashSet<>();
        if (this.mNotifyServiceList.size() != 0) {
            serviceList.addAll(this.mNotifyServiceList);
            this.mNotifyServiceList.clear();
        } else {
            serviceList.addAll(this.mUaProfile.getServiceList());
        }
        String pcscf = this.mUaProfile.getPcscfIp();
        String pAssociatedUri2nd = "";
        if (OmcCode.isKOROmcCode() && this.mSimManager.getSimMno() == Mno.LGU) {
            pAssociatedUri2nd = getPAssociatedUri2nd(serviceList, this.mImpuList);
        }
        return ImsRegistration.getBuilder().setHandle(this.mHandle).setImsProfile(new ImsProfile(this.mImsProfile)).setServices(serviceList).setPrivateUserId(this.mUaProfile.getImpi()).setPublicUserId(this.mImpuList).setRegisteredPublicUserId(ImsUri.parse(this.mUaProfile.getImpu())).setPreferredPublicUserId(getPreferredImpu(serviceList)).setDomain(domain).setPcscf(pcscf).setEpdgStatus(this.mEpdgStatus).setPdnType(this.mPdn).setUuid(this.mUaProfile.getUuid()).setInstanceId(this.mUaProfile.getInstanceId()).setEcmpStatus(this.mEcmpMode).setDeviceList(this.mDeviceList).setRegisterSipResponse(this.mRegisterSipResponse).setNetwork(this.mNetwork).setPAssociatedUri2nd(pAssociatedUri2nd).setSubscriptionId(subId).setPhoneId(this.mSimManager.getSimSlotIndex()).build();
    }

    private String getPAssociatedUri2nd(Set<String> serviceList, List<NameAddr> mImpuList2) {
        String result = null;
        Log.i("UserAgent[" + this.mPhoneId + "]", "getPAssociatedUri2nd: isVolteRegistered = " + false);
        if (serviceList.contains("mmtel") || serviceList.contains("im")) {
            result = extractPAssociatedUri2nd(mImpuList2);
        }
        Log.i("UserAgent[" + this.mPhoneId + "]", "getPAssociatedUri2nd() : " + result);
        return result;
    }

    private String extractPAssociatedUri2nd(List<NameAddr> mImpuList2) {
        String PassociatedUri2nd = null;
        String tempMyPhoneNumber = this.mSimManager.getLine1Number();
        if (tempMyPhoneNumber != null) {
            tempMyPhoneNumber = tempMyPhoneNumber.replace("+82", "0");
        }
        Log.i("UserAgent[" + this.mPhoneId + "]", "extractPAssociatedUri2nd");
        for (NameAddr addr : mImpuList2) {
            ImsUri uri = addr.getUri();
            if (uri != null) {
                Log.i("UserAgent[" + this.mPhoneId + "]", "extractPAssociatedUri2nd  uri");
                if (uri.getUriType() == ImsUri.UriType.SIP_URI && uri.toString() != null) {
                    Log.i("UserAgent[" + this.mPhoneId + "]", "extractPAssociatedUri2nd: uri.toString() = " + uri.toString());
                    String tempPassociatedUri2nd = getOnlyNumberFromURI(uri.toString());
                    if (!(tempMyPhoneNumber == null || tempPassociatedUri2nd == null || tempPassociatedUri2nd.equals(tempMyPhoneNumber))) {
                        PassociatedUri2nd = tempPassociatedUri2nd;
                    }
                }
            }
        }
        return PassociatedUri2nd;
    }

    private String getOnlyNumberFromURI(String source) {
        String target = source;
        LinkedHashMap<String, String> tokenSetForPrefix = new LinkedHashMap<>();
        tokenSetForPrefix.put("tel:", "tel:");
        tokenSetForPrefix.put("sip:", "sip:");
        tokenSetForPrefix.put("*31#", "[*]31#");
        tokenSetForPrefix.put("#31#", "#31#");
        Set<String> keys = tokenSetForPrefix.keySet();
        Log.i("UserAgent[" + this.mPhoneId + "]", "getOnlyNumberFromURI");
        for (String token : keys) {
            if (source.contains(token)) {
                target = source.split(tokenSetForPrefix.get(token))[1];
            }
        }
        for (String token2 : new String[]{"@", ";"}) {
            if (target.contains(token2)) {
                target = target.split(token2)[0];
            }
        }
        return target;
    }

    public SipError getErrorCode() {
        return this.mError;
    }

    public UaProfile getUaProfile() {
        return this.mUaProfile;
    }

    public boolean isRegistering() {
        return getCurrentState().equals(this.mRegisteringState) || getCurrentState().equals(this.mReRegisteringState);
    }

    public void updateVceConfig(boolean config) {
        sendMessage(40, (Object) Boolean.valueOf(config));
    }

    public void updateGeolocation(LocationInfo geolocation) {
        sendMessage(44, (Object) geolocation);
    }

    public ImsProfile getImsProfile() {
        return this.mImsProfile;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public void updateTimeInPlani(long time) {
        sendMessage(52, (Object) Long.valueOf(time));
    }

    public void updateRat(int network) {
        sendMessage(50, network);
    }

    public void deregister(boolean local, boolean isRcsRegistered) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "deregister: local=" + local + ", isRcsRegistered=" + isRcsRegistered);
        if (isRcsRegistered) {
            sendMessageDelayed(10, local ? 1 : 0, -1, 500);
        } else {
            sendMessage(10, (int) local);
        }
    }

    public int getHandle() {
        return this.mHandle;
    }

    public void updatePani(String pani, String lastPani) {
        List<String> paniList = new ArrayList<>();
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "updatePani: pani=" + pani + ", updatePani: lastPani=" + lastPani);
        if (!TextUtils.isEmpty(pani)) {
            paniList.add(pani);
            if (!TextUtils.isEmpty(lastPani)) {
                paniList.add(lastPani);
            }
            sendMessage(34, (Object) paniList);
        }
    }

    public void terminate() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "terminate:");
        sendMessage(4);
    }

    public void notifyE911RegistrationFailed() {
        sendMessage(900);
    }

    public ImsRegistration getImsRegistration() {
        return this.mRegistration;
    }

    public boolean isDeregistring() {
        return getCurrentState().equals(this.mDeregisteringState);
    }

    public Network getNetwork() {
        return this.mNetwork;
    }
}
