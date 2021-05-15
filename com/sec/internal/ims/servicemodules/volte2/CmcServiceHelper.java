package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.telephony.PublishDialog;
import com.samsung.android.cmcp2phelper.MdmnNsdWrapper;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.cmc.CmcCallInfo;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.servicemodules.Registration;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import com.sec.internal.ims.servicemodules.volte2.data.DtmfInfo;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.volte2.ICmcServiceHelper;
import com.sec.internal.log.IMSLog;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CmcServiceHelper extends Handler implements ICmcServiceHelper, ICmcServiceHelperInternal {
    private static final int CMC_HANDOVER_TIMER_VALUE = 5000;
    private static final int CMC_PD_CHECK_TIMER_VALUE = 20;
    private static final int DIVIDABLE64 = 63;
    private static final int DUMMY_CALL_DOMAIN = 9;
    private static final long DUPLICATED_PUBLISH_DENY_TIME_IN_MILLI = 500;
    private static final int EVENT_OPTIONS_EVENT = 32;
    private static final int EVENT_P2P_OPTIONS_EVENT = 31;
    private static final int EVT_CMC_HANDOVER_TIMER = 34;
    private static final int EVT_CMC_INFO_EVENT = 35;
    private static final int EVT_CMC_PD_CHECK_TIMER = 33;
    private static final String LOG_TAG = CmcServiceHelper.class.getSimpleName();
    private final Map<Integer, Long> mCmcCallEstablishTimeMap = new ConcurrentHashMap();
    private Message mCmcHandoverTimer = null;
    private final Map<Integer, Message> mCmcPdCheckTimeOut = new ArrayMap();
    private boolean mCmcTotalMnoPullable = true;
    private final Context mContext;
    private final Map<Integer, PublishDialog> mCsPublishDialogMap = new ConcurrentHashMap();
    private int mExtConfirmedCsCallCnt = 0;
    private ImsCallSessionManager mImsCallSessionManager;
    private final Map<Integer, Boolean> mIsCmcPdCheckRespRecevied = new ArrayMap();
    private DialogEvent[] mLastCmcDialogEvent;
    private IImsMediaController mMediaController;
    private MdmnNsdWrapper mNsd;
    private MdmnServiceInfo mNsdServiceInfo;
    private IOptionsServiceInterface mOptionsSvcIntf;
    private CopyOnWriteArrayList<Registration> mRegistrationList;
    private MessageDigest mSendPublishDigest;
    private byte[] mSendPublishHashedXml;
    private int mSendPublishInvokeCount = 0;
    private long mSendPublishInvokeTime = 0;
    private IVolteServiceInterface mVolteSvcIntf;

    public CmcServiceHelper(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
    }

    public CmcServiceHelper(Looper looper, Context context, CopyOnWriteArrayList<Registration> registrationList, IVolteServiceInterface volteServiceInterface, IImsMediaController mediaController, ImsCallSessionManager imsCallSessionManager, IOptionsServiceInterface optionsServiceInterface, int phoneCount) {
        super(looper);
        this.mContext = context;
        this.mVolteSvcIntf = volteServiceInterface;
        this.mOptionsSvcIntf = optionsServiceInterface;
        this.mMediaController = mediaController;
        this.mRegistrationList = registrationList;
        this.mImsCallSessionManager = imsCallSessionManager;
        this.mLastCmcDialogEvent = new DialogEvent[phoneCount];
        try {
            this.mSendPublishDigest = MessageDigest.getInstance(Constants.DIGEST_ALGORITHM_SHA1);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public void init() {
        this.mOptionsSvcIntf.registerForCmcOptionsEvent(this, 32, (Object) null);
        this.mOptionsSvcIntf.registerForP2pOptionsEvent(this, 31, (Object) null);
        this.mVolteSvcIntf.registerForCmcInfoEvent(this, 35, (Object) null);
    }

    public void onRegistered(ImsRegistration regiInfo) {
        int phoneId = regiInfo.getPhoneId();
        int cmcType = regiInfo.getImsProfile().getCmcType();
        if (isCmcPrimaryType(cmcType)) {
            if (hasActiveCmcCallsession(cmcType)) {
                Log.i(LOG_TAG, "exist Active PD callsession. do not send PUBLISH msg.");
            } else if (this.mCsPublishDialogMap.containsKey(Integer.valueOf(phoneId))) {
                Log.i(LOG_TAG, "Send Publish for CS call after CMC PD registration.");
                sendPublishDialog(phoneId, this.mCsPublishDialogMap.get(Integer.valueOf(phoneId)), cmcType);
            } else if (hasActiveCmcCallsession(0)) {
                sendPublishDialogInternal(phoneId, regiInfo);
            } else {
                Log.i(LOG_TAG, "sendDummyPublishDialog because do not have active VoLTE Call.");
                sendDummyPublishDialog(phoneId, cmcType);
            }
        } else if (!isCmcSecondaryType(cmcType)) {
            String str = LOG_TAG;
            Log.i(str, "mmtel Registered ? " + regiInfo.hasService("mmtel"));
            if (regiInfo.hasService("mmtel")) {
                this.mCsPublishDialogMap.remove(Integer.valueOf(phoneId));
            }
        } else if (this.mCmcHandoverTimer != null) {
            Log.i(LOG_TAG, "do cmc handover");
            PreciseAlarmManager.getInstance(this.mContext).removeMessage(this.mCmcHandoverTimer);
            this.mCmcHandoverTimer = null;
            ImsCallSession previousSession = getSessionByCmcType(cmcType);
            if (previousSession != null) {
                CallProfile profile = makeReplaceProfile(previousSession.getCallProfile());
                try {
                    this.mImsCallSessionManager.createSession(profile, regiInfo).start(profile.getLetteringText(), profile);
                    previousSession.replaceRegistrationInfo(regiInfo);
                } catch (RemoteException e) {
                    this.mVolteSvcIntf.clearAllCallInternal(cmcType);
                    e.printStackTrace();
                }
            }
        }
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        if (regiInfo.getImsProfile().getCmcType() > 0 && this.mNsd != null) {
            Log.i(LOG_TAG, "stop Nsd");
            this.mNsd.stop();
        }
    }

    public void onDeregistering(ImsRegistration regiInfo) {
        if (isCmcPrimaryType(regiInfo.getImsProfile().getCmcType()) && regiInfo.getDeregiReason() != 2) {
            Log.d(LOG_TAG, "onDeregistering: Send dummy publish dialog before deregistered");
            sendDummyPublishDialog(regiInfo.getPhoneId(), regiInfo.getImsProfile().getCmcType());
        }
    }

    public void sendDummyPublishDialog(int phoneId, int cmcType) {
        PublishDialog tmpDialog = new PublishDialog();
        tmpDialog.setCallCount(1);
        tmpDialog.addCallId(9999);
        tmpDialog.addCallDomain(9);
        tmpDialog.addCallStatus(0);
        tmpDialog.addCallType(1);
        tmpDialog.addCallDirection(0);
        tmpDialog.addCallRemoteUri("");
        tmpDialog.addCallPullable(true);
        tmpDialog.addCallNumberPresentation(0);
        tmpDialog.addCallCnapNamePresentation(0);
        tmpDialog.addCallCnapName("");
        tmpDialog.addCallMpty(false);
        tmpDialog.addConnectedTime(0);
        sendPublishDialog(phoneId, tmpDialog, cmcType);
    }

    public ImsCallSession getSessionByCmcType(int cmcType) {
        ImsCallSession returnSession = null;
        for (ImsCallSession s : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (cmcType == s.getCmcType()) {
                returnSession = s;
            }
        }
        return returnSession;
    }

    public ImsCallSession getSessionByCmcTypeAndState(int cmcType, CallConstants.STATE state) {
        ImsCallSession returnSession = null;
        for (ImsCallSession s : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (cmcType == s.getCmcType() && s.getCallState() == state) {
                returnSession = s;
            }
        }
        return returnSession;
    }

    public boolean hasActiveCmcCallsession(int cmcType) {
        List<CallConstants.STATE> activeCallStates = new ArrayList<>();
        activeCallStates.add(CallConstants.STATE.InCall);
        activeCallStates.add(CallConstants.STATE.HoldingCall);
        activeCallStates.add(CallConstants.STATE.HeldCall);
        activeCallStates.add(CallConstants.STATE.ResumingCall);
        activeCallStates.add(CallConstants.STATE.ModifyingCall);
        activeCallStates.add(CallConstants.STATE.ModifyRequested);
        activeCallStates.add(CallConstants.STATE.HoldingVideo);
        activeCallStates.add(CallConstants.STATE.VideoHeld);
        activeCallStates.add(CallConstants.STATE.ResumingVideo);
        for (ImsCallSession s : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (cmcType == s.getCmcType() && activeCallStates.contains(s.getCallState())) {
                return true;
            }
        }
        return false;
    }

    public boolean isCmcRegExist(int phoneId) {
        ImsProfile imsProfile;
        boolean returnBoolean = false;
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration reg = it.next();
            if (!(reg == null || reg.getImsRegi().getPhoneId() != phoneId || (imsProfile = reg.getImsRegi().getImsProfile()) == null || imsProfile.getCmcType() == 0)) {
                returnBoolean = true;
            }
        }
        return returnBoolean;
    }

    public int getSessionCountByCmcType(int phoneId, int cmcType) {
        int count = 0;
        for (ImsCallSession s : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            ImsRegistration reg = s.getRegistration();
            if (reg != null) {
                ImsProfile imsProfile = reg.getImsProfile();
                if (s.getPhoneId() == phoneId && imsProfile.getCmcType() == cmcType) {
                    count++;
                }
            }
        }
        return count;
    }

    public void onCmcDtmfInfo(DtmfInfo dtmfInfo) {
        Log.i(LOG_TAG, "onCmcDtmfInfo");
        int cmcType = 5;
        if (ImsRegistry.getP2pCC().isEnabledWifiDirectFeature()) {
            cmcType = 7;
        }
        for (int type = 1; type <= cmcType; type += 2) {
            ImsCallSession pdActiveSession = getSessionByCmcTypeAndState(type, CallConstants.STATE.InCall);
            if (pdActiveSession != null) {
                pdActiveSession.notifyCmcDtmfEvent(dtmfInfo.getEvent());
            }
        }
    }

    public void startP2pDiscovery(List<String> hostList) {
        if (this.mNsd == null) {
            Log.i(LOG_TAG, "mNsd is null!!");
            return;
        }
        if (hostList == null) {
            Log.i(LOG_TAG, "find hostlist");
            hostList = ImsRegistry.getCmcAccountManager().getRegiEventNotifyHostInfo();
        }
        if (hostList != null && hostList.size() > 0) {
            int ret = this.mNsd.startDiscovery(new ArrayList(hostList));
            String str = LOG_TAG;
            Log.i(str, "startDiscovery result = " + ret + "hostlist " + hostList);
        }
    }

    public void startP2p(String deviceId, String lineId) {
        if (this.mNsd == null) {
            String str = LOG_TAG;
            Log.i(str, "startP2p lineId : " + lineId);
            String str2 = LOG_TAG;
            Log.i(str2, "startP2p deviceId : " + deviceId);
            this.mNsdServiceInfo = new MdmnServiceInfo(deviceId, lineId);
            this.mNsd = new MdmnNsdWrapper(this.mContext, this.mNsdServiceInfo);
        }
        Log.i(LOG_TAG, "start Nsd");
        this.mNsd.start();
    }

    public void setP2pServiceInfo(String deviceId, String lineId) {
        String str = LOG_TAG;
        Log.i(str, "set lineId " + lineId);
        String str2 = LOG_TAG;
        Log.i(str2, "set deviceId " + deviceId);
        if (this.mNsd != null) {
            this.mNsd.setServiceInfo(new MdmnServiceInfo(deviceId, lineId));
        }
    }

    public void sendPublishDialog(int phoneId, PublishDialog publishDialog, int cmcType) {
        long[] callEstablishTime;
        String str;
        String str2;
        String str3;
        int regId;
        String publicId;
        int conferenceCallState;
        String str4;
        List<Dialog> dialogList;
        int conferenceCallState2;
        int audioDir;
        int extCsCallCount;
        int audioDir2;
        int[] callStates;
        boolean mCmcPreviousTotalMnoPullable;
        String str5;
        List<Dialog> dialogList2;
        int videoDir;
        int audioDir3;
        boolean isExclusive;
        int callState;
        int callType;
        int direction;
        int dialogState;
        String sessionDesc;
        String sessionDesc2;
        int i;
        int i2 = phoneId;
        int i3 = cmcType;
        int callCnt = publishDialog.getCallCount();
        int[] callIds = publishDialog.getCallId();
        int[] domains = publishDialog.getCallDomain();
        int[] callStates2 = publishDialog.getCallStatus();
        int[] callTypes = publishDialog.getCallType();
        int[] callDirections = publishDialog.getCallDirection();
        String[] remoteUris = publishDialog.getCallRemoteUri();
        boolean[] pullable = publishDialog.getCallPullable();
        int[] numberPresentations = publishDialog.getCallNumberPresentation();
        long[] callEstablishTime2 = publishDialog.getConnectedTime();
        boolean[] isConferences = publishDialog.getCallMpty();
        String str6 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        String[] remoteUris2 = remoteUris;
        sb.append("sendPublishDialog() callCnt: ");
        sb.append(callCnt);
        Log.i(str6, sb.toString());
        List<Dialog> arrayList = new ArrayList<>();
        if (callIds != null) {
            Object obj = "";
            if (callIds.length < 1 || domains == null) {
                int[] iArr = callStates2;
                int[] iArr2 = callTypes;
                List<Dialog> list = arrayList;
                boolean[] zArr = pullable;
                int[] iArr3 = numberPresentations;
                long[] jArr = callEstablishTime2;
                boolean[] zArr2 = isConferences;
                int i4 = i3;
            } else if (domains.length < 1) {
                int[] iArr4 = domains;
                int[] iArr5 = callStates2;
                int[] iArr6 = callTypes;
                ArrayList arrayList2 = arrayList;
                boolean[] zArr3 = pullable;
                int[] iArr7 = numberPresentations;
                long[] jArr2 = callEstablishTime2;
                boolean[] zArr4 = isConferences;
                int i5 = i3;
            } else {
                List<Dialog> dialogList3 = arrayList;
                if (domains[0] == 1) {
                    Log.i(LOG_TAG, "Save publishDialog for CS call, callCnt : " + callCnt);
                    this.mCsPublishDialogMap.put(Integer.valueOf(phoneId), publishDialog);
                    int extConfirmedCsCallCnt = 0;
                    int i6 = 0;
                    while (i6 < callCnt) {
                        long[] callEstablishTime3 = callEstablishTime2;
                        if (ImsCallUtil.convertCsCallStateToDialogState(callStates2[i6]) == 2) {
                            extConfirmedCsCallCnt++;
                        }
                        i6++;
                        PublishDialog publishDialog2 = publishDialog;
                        callEstablishTime2 = callEstablishTime3;
                    }
                    callEstablishTime = callEstablishTime2;
                    Log.i(LOG_TAG, "mExtConfirmedCsCallCnt: " + this.mExtConfirmedCsCallCnt + ", extConfirmedCsCallCnt: " + extConfirmedCsCallCnt);
                    if (this.mExtConfirmedCsCallCnt != extConfirmedCsCallCnt) {
                        this.mOptionsSvcIntf.updateCmcExtCallCount(i2, extConfirmedCsCallCnt);
                    }
                    this.mExtConfirmedCsCallCnt = extConfirmedCsCallCnt;
                } else {
                    callEstablishTime = callEstablishTime2;
                }
                Log.d(LOG_TAG, "cmcType: " + i3);
                ImsRegistration cmcRegi = getCmcRegistration(i2, false, i3);
                if (cmcRegi != null) {
                    int i7 = 0;
                    while (true) {
                        str = ",";
                        if (i7 >= callCnt) {
                            break;
                        }
                        IMSLog.c(LogClass.CMC_PUBLISH_DETAIL, "#" + i7 + str + callIds[i7] + str + domains[i7] + str + callStates2[i7] + str + callTypes[i7] + str + callDirections[i7] + str + pullable[i7] + str + numberPresentations[i7] + str + isConferences[i7]);
                        i7++;
                        int i8 = phoneId;
                    }
                    this.mCmcCallEstablishTimeMap.clear();
                    int i9 = 0;
                    while (true) {
                        str2 = str;
                        str3 = "[CallInfo #";
                        if (i9 >= callCnt) {
                            break;
                        }
                        boolean[] isConferences2 = isConferences;
                        int[] numberPresentations2 = numberPresentations;
                        Log.i(LOG_TAG, str3 + i9 + "] callId: " + callIds[i9] + ", callState: " + callStates2[i9] + ", callEstablishTime: " + callEstablishTime[i9]);
                        if (callEstablishTime != null && callStates2[i9] == 1) {
                            this.mCmcCallEstablishTimeMap.put(Integer.valueOf(callIds[i9]), Long.valueOf(callEstablishTime[i9]));
                        }
                        i9++;
                        str = str2;
                        isConferences = isConferences2;
                        numberPresentations = numberPresentations2;
                    }
                    int[] numberPresentations3 = numberPresentations;
                    boolean[] isConferences3 = isConferences;
                    if (hasInternalCallToIgnorePublishDialog(phoneId) == 0) {
                        boolean needDelay = isNeedDelayToSendPublishDialog(phoneId);
                        int regId2 = cmcRegi.getHandle();
                        StringBuilder sb2 = new StringBuilder();
                        boolean needDelay2 = needDelay;
                        sb2.append("sip:");
                        String str7 = "sip:";
                        sb2.append(cmcRegi.getImpi());
                        String publicId2 = sb2.toString();
                        ImsRegistration cmcRegi2 = cmcRegi;
                        Log.i(LOG_TAG, "regId: " + regId2 + ", publicId: " + publicId2);
                        boolean mCmcPreviousTotalMnoPullable2 = this.mCmcTotalMnoPullable;
                        this.mCmcTotalMnoPullable = true;
                        if (pullable != null) {
                            int i10 = 0;
                            while (true) {
                                if (i10 >= pullable.length) {
                                    break;
                                } else if (!pullable[i10]) {
                                    this.mCmcTotalMnoPullable = false;
                                    break;
                                } else {
                                    i10++;
                                }
                            }
                        }
                        boolean hasCsConference = false;
                        int conferenceCallState3 = 1;
                        int extCsCallCount2 = 0;
                        int endedCallCnt = 0;
                        boolean[] zArr5 = pullable;
                        int i11 = 0;
                        while (true) {
                            regId = regId2;
                            if (i11 >= callCnt) {
                                publicId = publicId2;
                                int[] iArr8 = callStates2;
                                conferenceCallState = conferenceCallState3;
                                str4 = str7;
                                dialogList = dialogList3;
                                conferenceCallState2 = cmcType;
                                boolean z = mCmcPreviousTotalMnoPullable2;
                                break;
                            }
                            String dialogId = "primary_device_dialog_id";
                            String sipCallId = "";
                            String deviceId = "";
                            String localUri = "";
                            String remoteUri = "";
                            int callState2 = 0;
                            boolean isEachCallPullable = true;
                            int audioDir4 = 0;
                            int videoDir2 = 0;
                            publicId = publicId2;
                            String publicId3 = LOG_TAG;
                            conferenceCallState = conferenceCallState3;
                            StringBuilder sb3 = new StringBuilder();
                            sb3.append(str3);
                            sb3.append(i11);
                            sb3.append("] callId: ");
                            String str8 = str3;
                            sb3.append(callIds[i11]);
                            sb3.append(", domain: ");
                            sb3.append(domains[i11]);
                            sb3.append(", callState: ");
                            sb3.append(callStates2[i11]);
                            sb3.append(", callType: ");
                            sb3.append(callTypes[i11]);
                            sb3.append(", callDirections: ");
                            sb3.append(callDirections[i11]);
                            sb3.append(", remoteUris");
                            sb3.append(remoteUris2[i11]);
                            Log.i(publicId3, sb3.toString());
                            if (domains[i11] != 2) {
                                int i12 = phoneId;
                                int i13 = cmcType;
                                if (domains[i11] != 2) {
                                    sipCallId = String.valueOf(callIds[i11]);
                                    dialogId = sipCallId;
                                    StringBuilder sb4 = new StringBuilder();
                                    str5 = str7;
                                    sb4.append(str5);
                                    mCmcPreviousTotalMnoPullable = mCmcPreviousTotalMnoPullable2;
                                    sb4.append(cmcRegi2.getImpi());
                                    sb4.append(";gr=");
                                    sb4.append(cmcRegi2.getInstanceId());
                                    deviceId = sb4.toString();
                                    localUri = str5 + cmcRegi2.getImpi();
                                    remoteUri = str5 + remoteUris2[i11];
                                    if (numberPresentations3[i11] == 2) {
                                        sessionDesc2 = "anonymous";
                                    } else {
                                        sessionDesc2 = remoteUris2[i11];
                                    }
                                    int callType2 = callTypes[i11];
                                    int direction2 = callDirections[i11];
                                    int dialogState2 = ImsCallUtil.convertCsCallStateToDialogState(callStates2[i11]);
                                    if (callType2 == 911) {
                                        Log.i(LOG_TAG, "ignore publish dialog when call type is 911 (Emergency)");
                                    } else if (isConferences3[i11]) {
                                        if (callStates2[i11] == 2) {
                                            hasCsConference = true;
                                            callStates = callStates2;
                                            conferenceCallState = 2;
                                            dialogList2 = dialogList3;
                                        } else if (callStates2[i11] == 0 || callStates2[i11] == 7 || callStates2[i11] == 8) {
                                            hasCsConference = true;
                                            callStates = callStates2;
                                            conferenceCallState = 0;
                                            dialogList2 = dialogList3;
                                        } else {
                                            hasCsConference = true;
                                            callStates = callStates2;
                                            dialogList2 = dialogList3;
                                        }
                                        i11++;
                                        dialogList3 = dialogList2;
                                        mCmcPreviousTotalMnoPullable2 = mCmcPreviousTotalMnoPullable;
                                        regId2 = regId;
                                        publicId2 = publicId;
                                        conferenceCallState3 = conferenceCallState;
                                        callStates2 = callStates;
                                        str7 = str5;
                                        str3 = str8;
                                    } else {
                                        if (callType2 == 0 || callType2 == 911) {
                                            isEachCallPullable = callType2 != 911;
                                            i = 1;
                                            if (callStates2[i11] == 1) {
                                                audioDir4 = 3;
                                            } else {
                                                audioDir4 = 0;
                                            }
                                        } else if (callType2 == 1 || callType2 == 2 || callType2 == 3) {
                                            isEachCallPullable = false;
                                            if (callStates2[i11] == 1) {
                                                audioDir4 = 3;
                                                videoDir2 = 3;
                                                i = 1;
                                            } else {
                                                audioDir4 = 0;
                                                videoDir2 = 0;
                                                i = 1;
                                            }
                                        } else {
                                            i = 1;
                                        }
                                        if (callStates2[i11] == i) {
                                            callState2 = 1;
                                        } else if (callStates2[i11] == 2) {
                                            callState2 = 2;
                                            isEachCallPullable = false;
                                        } else {
                                            isEachCallPullable = false;
                                        }
                                        boolean isExclusive2 = !isEachCallPullable || !this.mCmcTotalMnoPullable;
                                        if (callStates2[i11] == 0 || callStates2[i11] == 7 || callStates2[i11] == 8) {
                                            endedCallCnt++;
                                        }
                                        extCsCallCount2++;
                                        callType = callType2;
                                        sessionDesc = sessionDesc2;
                                        dialogState = dialogState2;
                                        direction = direction2;
                                        callState = callState2;
                                        boolean z2 = isEachCallPullable;
                                        isExclusive = isExclusive2;
                                        audioDir3 = audioDir4;
                                        videoDir = videoDir2;
                                    }
                                } else {
                                    str5 = str7;
                                    mCmcPreviousTotalMnoPullable = mCmcPreviousTotalMnoPullable2;
                                    sessionDesc = "";
                                    dialogState = 0;
                                    direction = 0;
                                    callType = 0;
                                    callState = 0;
                                    isExclusive = false;
                                    audioDir3 = 0;
                                    videoDir = 0;
                                }
                                Dialog dlg = new Dialog(dialogId, deviceId, sipCallId, "test_local_tag", "test_remote_tag", localUri, remoteUri, "", "", sessionDesc, dialogState, direction, callType, callState, audioDir3, videoDir, isExclusive, false);
                                String str9 = sessionDesc;
                                String sessionDesc3 = LOG_TAG;
                                int i14 = dialogState;
                                StringBuilder sb5 = new StringBuilder();
                                callStates = callStates2;
                                sb5.append("[");
                                sb5.append(i11);
                                sb5.append("] ");
                                sb5.append(dlg.toString());
                                Log.i(sessionDesc3, sb5.toString());
                                dialogList2 = dialogList3;
                                dialogList2.add(dlg);
                                i11++;
                                dialogList3 = dialogList2;
                                mCmcPreviousTotalMnoPullable2 = mCmcPreviousTotalMnoPullable;
                                regId2 = regId;
                                publicId2 = publicId;
                                conferenceCallState3 = conferenceCallState;
                                callStates2 = callStates;
                                str7 = str5;
                                str3 = str8;
                            } else if (mCmcPreviousTotalMnoPullable2 != this.mCmcTotalMnoPullable) {
                                Log.i(LOG_TAG, String.format("Trying call sendPublishDialogInternal(). CmcTotalMnoPullable changed : %s ==> %s", new Object[]{Boolean.valueOf(mCmcPreviousTotalMnoPullable2), Boolean.valueOf(this.mCmcTotalMnoPullable)}));
                                conferenceCallState2 = cmcType;
                                sendPublishDialogInternal(phoneId, conferenceCallState2);
                                int[] iArr9 = callStates2;
                                str4 = str7;
                                dialogList = dialogList3;
                                boolean z3 = mCmcPreviousTotalMnoPullable2;
                                break;
                            } else {
                                int i15 = phoneId;
                                int i16 = cmcType;
                                str5 = str7;
                                mCmcPreviousTotalMnoPullable = mCmcPreviousTotalMnoPullable2;
                            }
                            callStates = callStates2;
                            dialogList2 = dialogList3;
                            i11++;
                            dialogList3 = dialogList2;
                            mCmcPreviousTotalMnoPullable2 = mCmcPreviousTotalMnoPullable;
                            regId2 = regId;
                            publicId2 = publicId;
                            conferenceCallState3 = conferenceCallState;
                            callStates2 = callStates;
                            str7 = str5;
                            str3 = str8;
                        }
                        if (domains[0] == 2 || callCnt < 2 || !hasCsConference) {
                            extCsCallCount = extCsCallCount2;
                            audioDir = endedCallCnt;
                        } else {
                            String deviceId2 = str4 + cmcRegi2.getImpi() + ";gr=" + cmcRegi2.getInstanceId();
                            int videoDir3 = 0;
                            int[] iArr10 = domains;
                            if (callTypes[0] == 1 || callTypes[0] == 2 || callTypes[0] == 3) {
                                audioDir2 = 3;
                                videoDir3 = 3;
                            } else {
                                audioDir2 = 3;
                            }
                            if (conferenceCallState == 0) {
                                endedCallCnt++;
                            }
                            Dialog dialog = new Dialog("999", deviceId2, "999", "test_local_tag", "test_remote_tag", str4 + cmcRegi2.getImpi(), "Conference call", "", "", "Conference call", 2, 0, 0, conferenceCallState, audioDir2, videoDir3, true, false);
                            String str10 = LOG_TAG;
                            StringBuilder sb6 = new StringBuilder();
                            Object obj2 = "test_local_tag";
                            sb6.append("conference: ");
                            sb6.append(dialog.toString());
                            Log.i(str10, sb6.toString());
                            dialogList.add(dialog);
                            extCsCallCount = extCsCallCount2 + 1;
                            audioDir = endedCallCnt;
                        }
                        String origUri = publicId;
                        String xml = "<?xml version=\"1.0\"?>\n\t<dialog-info xmlns=\"urn:ietf:params:xml:ns:dialog-info\" xmlns:sa=\"urn:ietf:params:xml:ns:sa-dialog-info\"\n\t\tversion=\"0\" state=\"full\" entity=\"" + publicId + "\">\n";
                        for (Dialog dialog2 : dialogList) {
                            if (extCsCallCount - audioDir > 1) {
                                dialog2.setIsExclusive(true);
                            }
                            xml = xml + dialog2.toXmlString();
                        }
                        String xml2 = xml + "</dialog-info>";
                        int cmcPdCallCnt = 0;
                        if (getSessionByCmcTypeAndState(conferenceCallState2, CallConstants.STATE.InCall) != null) {
                            cmcPdCallCnt = 0 + 1;
                        }
                        String str11 = LOG_TAG;
                        StringBuilder sb7 = new StringBuilder();
                        int[] iArr11 = callTypes;
                        sb7.append("cmcPdCallCnt: ");
                        sb7.append(cmcPdCallCnt);
                        sb7.append(", extCsCallCount: ");
                        sb7.append(extCsCallCount);
                        sb7.append(", endedCallCnt: ");
                        sb7.append(audioDir);
                        Log.i(str11, sb7.toString());
                        boolean isDummyDialog = false;
                        if (callCnt == 1 && callIds[0] == 9999) {
                            Log.i(LOG_TAG, "This is dummy Publish dialog");
                            isDummyDialog = true;
                        }
                        if (isDuplicatedPublishDialog(xml2) && !isDummyDialog) {
                            return;
                        }
                        if (cmcPdCallCnt != 0 || dialogList.size() <= 0) {
                            int i17 = extCsCallCount;
                            return;
                        }
                        this.mVolteSvcIntf.publishDialog(regId, origUri, "displayName", xml2, 6000, needDelay2);
                        for (Dialog dialog3 : dialogList) {
                            StringBuilder sb8 = new StringBuilder();
                            List<Dialog> dialogList4 = dialogList;
                            sb8.append(dialog3.getCallType());
                            String str12 = str2;
                            sb8.append(str12);
                            sb8.append(dialog3.getCallState());
                            sb8.append(str12);
                            sb8.append(dialog3.isExclusive());
                            sb8.append(str12);
                            sb8.append(dialog3.getSipCallId());
                            IMSLog.c(LogClass.CMC_SEND_PUBLISH, sb8.toString());
                            extCsCallCount = extCsCallCount;
                            dialogList = dialogList4;
                        }
                        int i18 = extCsCallCount;
                        return;
                    }
                    return;
                } else if (i3 == 1) {
                    Log.e(LOG_TAG, "Ignore sendPublishDialog : CMC PD is not registered");
                    return;
                } else {
                    Log.e(LOG_TAG, "Ignore sendPublishDialog : P2P PD is not registered: " + i3);
                    return;
                }
            }
        } else {
            int[] iArr12 = callStates2;
            int[] iArr13 = callTypes;
            List<Dialog> list2 = arrayList;
            boolean[] zArr6 = pullable;
            int[] iArr14 = numberPresentations;
            long[] jArr3 = callEstablishTime2;
            boolean[] zArr7 = isConferences;
            Object obj3 = "";
            int i19 = i3;
        }
        Log.e(LOG_TAG, "Ignore sendPublishDialog : Array parameters are empty!");
    }

    public void sendPublishDialogInternal(int phoneId, int cmcType) {
        ImsRegistration cmcPdRegi = getCmcRegistration(phoneId, cmcType);
        if (cmcPdRegi != null) {
            sendPublishDialogInternal(phoneId, cmcPdRegi);
        }
    }

    public void setCallEstablishTimeExtra(long callEstablishTimeExtra) {
        this.mCmcCallEstablishTimeMap.put(-1, Long.valueOf(callEstablishTimeExtra));
    }

    public long getCmcCallEstablishTime(String callId) {
        if (callId == null) {
            Log.i(LOG_TAG, "callid is null");
            return getActiveCmcCallEstablishTime();
        } else if (!this.mCmcCallEstablishTimeMap.isEmpty()) {
            return this.mCmcCallEstablishTimeMap.get(Integer.valueOf(Integer.parseInt(callId))).longValue();
        } else {
            Log.i(LOG_TAG, "mCmcCallEstablishTimeMap is empty");
            return 0;
        }
    }

    private long getActiveCmcCallEstablishTime() {
        Iterator<Long> it = this.mCmcCallEstablishTimeMap.values().iterator();
        if (!it.hasNext()) {
            return 0;
        }
        long time = it.next().longValue();
        String str = LOG_TAG;
        Log.i(str, "getActiveCmcCallEstablishTime " + time);
        return time;
    }

    /* access modifiers changed from: package-private */
    public int getSessionCountByCmcType(int phoneId, ImsRegistration curReg) {
        if (curReg != null) {
            int curCmcType = curReg.getImsProfile().getCmcType();
            String str = LOG_TAG;
            Log.i(str, "curCmcType : " + curCmcType);
            return getSessionCountByCmcType(phoneId, curCmcType);
        }
        Log.i(LOG_TAG, "curReg null");
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void checkCmcP2pList(ImsRegistration regInfo, CallProfile profile) {
        if (this.mNsd != null && regInfo.getCurrentRat() == 18) {
            Collection<MdmnServiceInfo> deviceList = this.mNsd.getSupportDevices();
            int p2pSize = 0;
            if (deviceList != null) {
                p2pSize = deviceList.size();
                String str = LOG_TAG;
                Log.i(str, "P2P list size : " + p2pSize);
                if (profile != null) {
                    if (regInfo.getImsProfile().getCmcType() != 2 || profile.getReplaceSipCallId() == null) {
                        ArrayList<String> p2pList = new ArrayList<>();
                        for (MdmnServiceInfo info : deviceList) {
                            String impu = info.getLineId();
                            String deviceId = info.getDeviceId();
                            p2pList.add("sip:" + impu + "@samsungims.com;gr=" + deviceId);
                        }
                        profile.setP2p(p2pList);
                    } else {
                        Log.i(LOG_TAG, "Do not set p2p list in case of CMC handover");
                    }
                }
            }
            if (p2pSize < 2) {
                Log.i(LOG_TAG, "need p2p discovery");
                startP2pDiscovery((List<String>) null);
            }
        }
    }

    private boolean isDuplicatedPublishDialog(String xml) {
        if (this.mSendPublishDigest != null) {
            long previousInvokeTime = this.mSendPublishInvokeTime;
            this.mSendPublishInvokeTime = System.currentTimeMillis();
            this.mSendPublishDigest.reset();
            this.mSendPublishDigest.update(xml.getBytes(StandardCharsets.UTF_8));
            byte[] hashedXml = this.mSendPublishDigest.digest();
            if (this.mSendPublishInvokeTime - previousInvokeTime >= DUPLICATED_PUBLISH_DENY_TIME_IN_MILLI || !Arrays.equals(this.mSendPublishHashedXml, hashedXml)) {
                this.mSendPublishInvokeCount = 0;
                this.mSendPublishHashedXml = hashedXml;
            } else {
                int i = this.mSendPublishInvokeCount;
                if ((i & 63) == 0) {
                    Log.i(LOG_TAG, String.format("[%d] sendPublishDialog duplicated.", new Object[]{Integer.valueOf(i)}));
                }
                int i2 = this.mSendPublishInvokeCount + 1;
                this.mSendPublishInvokeCount = i2;
                if (i2 <= 50 || Debug.isProductShip()) {
                    return true;
                }
                throw new RuntimeException("Too many sendPublishDialog is called in very short time!\n" + xml);
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void sendPublishDialogInternal(int phoneId, ImsRegistration cmcRegi) {
        sendPublishDialogInternal(phoneId, cmcRegi, false);
    }

    /* JADX WARNING: type inference failed for: r8v3 */
    /* JADX WARNING: type inference failed for: r8v4, types: [boolean] */
    /* JADX WARNING: type inference failed for: r8v13 */
    /* access modifiers changed from: package-private */
    public void sendPublishDialogInternal(int phoneId, ImsRegistration cmcRegi, boolean needDelay) {
        ImsRegistration imsRegistration = cmcRegi;
        Log.i(LOG_TAG, "sendPublishDialogInternal()");
        List<Dialog> dialogList = new ArrayList<>();
        if (imsRegistration == null) {
            Log.e(LOG_TAG, "CMC PD is not registered");
            return;
        }
        int regId = cmcRegi.getHandle();
        String publicId = "sip:" + cmcRegi.getImpi();
        Log.i(LOG_TAG, "regId: " + regId + ", publicId: " + publicId);
        int[] getCallCount = getCallCountForSendPublishDialog(phoneId, imsRegistration, dialogList, this.mCmcTotalMnoPullable);
        int extPsCallCount = getCallCount[0];
        ? r8 = 1;
        int validCallCnt = getCallCount[1];
        int endedCallCnt = getCallCount[2];
        String origUri = publicId;
        String xml = "<?xml version=\"1.0\"?>\n\t<dialog-info xmlns=\"urn:ietf:params:xml:ns:dialog-info\" xmlns:sa=\"urn:ietf:params:xml:ns:sa-dialog-info\"\n\t\tversion=\"0\" state=\"full\" entity=\"" + publicId + "\">\n";
        for (Dialog dialog : dialogList) {
            if (extPsCallCount - endedCallCnt > r8) {
                dialog.setIsExclusive(r8);
            }
            xml = xml + dialog.toXmlString();
            IMSLog.c(LogClass.CMC_SEND_PUBLISH_INTERNAL, dialog.getCallType() + "," + dialog.getCallState() + "," + dialog.isExclusive() + "," + dialog.getSipCallId());
            ImsRegistration imsRegistration2 = cmcRegi;
            r8 = 1;
        }
        String xml2 = xml + "</dialog-info>";
        Log.i(LOG_TAG, "extPsCallCount: " + extPsCallCount + ", validCallCnt: " + validCallCnt + ", endedCallCnt: " + endedCallCnt);
        if (dialogList.size() > 0) {
            int i = endedCallCnt;
            int i2 = validCallCnt;
            this.mVolteSvcIntf.publishDialog(regId, origUri, "displayName", xml2, 6000, needDelay);
            return;
        }
        int i3 = validCallCnt;
    }

    /* access modifiers changed from: package-private */
    public int[] getCallCountForSendPublishDialog(int phoneId, ImsRegistration cmcRegi, List<Dialog> dialogList, boolean cmcTotalMnoPullable) {
        int videoDir;
        int audioDir;
        String sessionDesc;
        boolean isInternalCallPullable;
        int i = phoneId;
        int[] ret = new int[3];
        for (ImsCallSession session : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (session == null) {
                List<Dialog> list = dialogList;
            } else if (i == -1 || session.getPhoneId() == i) {
                ImsRegistration reg = session.getRegistration();
                if (reg != null) {
                    ImsProfile imsProfile = reg.getImsProfile();
                    if (imsProfile == null || imsProfile.getCmcType() != 0) {
                        List<Dialog> list2 = dialogList;
                    } else {
                        String sipCallId = session.getCallProfile().getSipCallId();
                        String deviceId = "sip:" + cmcRegi.getImpi() + ";gr=" + cmcRegi.getInstanceId();
                        String localUri = "sip:" + cmcRegi.getImpi();
                        String remoteUri = "sip:" + session.getCallProfile().getDialingNumber();
                        String sessionDesc2 = session.getCallProfile().getDialingNumber();
                        String dialogId = sipCallId;
                        boolean isInternalCallPullable2 = true;
                        CallConstants.STATE psCallState = getCallstateForPublishDialog(session.getCallState(), session.mIsEstablished);
                        int direction = session.getCallProfile().isMOCall() ? 0 : 1;
                        int callType = session.getCallProfile().getCallType();
                        boolean isAudioEmergency = ImsCallUtil.isEmergencyAudioCall(callType);
                        boolean isVideoEmergency = ImsCallUtil.isEmergencyVideoCall(callType);
                        if (!checkIgnorePublishDialogCase(callType, isAudioEmergency, isVideoEmergency)) {
                            if (callType == 1) {
                                isInternalCallPullable2 = true;
                                audioDir = getDialogDirection(psCallState);
                                videoDir = 0;
                            } else if (callType > 1) {
                                isInternalCallPullable2 = false;
                                audioDir = getDialogDirection(psCallState);
                                videoDir = getDialogDirection(psCallState);
                            } else {
                                audioDir = 0;
                                videoDir = 0;
                            }
                            if (session.getCallProfile().isConferenceCall()) {
                                isInternalCallPullable2 = false;
                                sessionDesc = "Conference call";
                            } else {
                                sessionDesc = sessionDesc2;
                            }
                            int callState = getDialogCallState(psCallState, session.isRemoteHeld());
                            if (callState != 1) {
                                isInternalCallPullable = false;
                            } else {
                                isInternalCallPullable = isInternalCallPullable2;
                            }
                            boolean isExclusive = !isInternalCallPullable || !cmcTotalMnoPullable;
                            if (ImsCallUtil.isDuringCallState(psCallState)) {
                                ret[1] = ret[1] + 1;
                            } else if (psCallState == CallConstants.STATE.Idle || ImsCallUtil.isEndCallState(psCallState)) {
                                ret[2] = ret[2] + 1;
                            }
                            boolean z = isVideoEmergency;
                            boolean z2 = isAudioEmergency;
                            CallConstants.STATE state = psCallState;
                            Dialog dlg = new Dialog(dialogId, deviceId, sipCallId, "test_local_tag", "test_remote_tag", localUri, remoteUri, "", "", sessionDesc, ImsCallUtil.convertImsCallStateToDialogState(psCallState), direction, callType, callState, audioDir, videoDir, isExclusive, false);
                            Log.i(LOG_TAG, "[" + ret[0] + "] " + dlg.toString());
                            if (!TextUtils.isEmpty(dialogId)) {
                                dialogList.add(dlg);
                            } else {
                                List<Dialog> list3 = dialogList;
                            }
                            ret[0] = ret[0] + 1;
                        }
                    }
                } else {
                    List<Dialog> list4 = dialogList;
                }
            }
        }
        List<Dialog> list5 = dialogList;
        return ret;
    }

    private boolean checkIgnorePublishDialogCase(int callType, boolean isAudioEmergency, boolean isVideoEmergency) {
        if (callType == 0) {
            Log.i(LOG_TAG, "CallType is unknown");
            return true;
        } else if (!isAudioEmergency && !isVideoEmergency) {
            return false;
        } else {
            Log.i(LOG_TAG, "ignore publish dialog when call type is 911 (Emergency)");
            return true;
        }
    }

    private CallConstants.STATE getCallstateForPublishDialog(CallConstants.STATE psCallState, boolean isEstablished) {
        CallConstants.STATE callstate = psCallState;
        if (psCallState != CallConstants.STATE.IncomingCall || !isEstablished) {
            return callstate;
        }
        Log.i(LOG_TAG, "forced InCall state change for fast establishment [delayed ACK case]");
        return CallConstants.STATE.InCall;
    }

    private int getDialogDirection(CallConstants.STATE psCallState) {
        if (psCallState == CallConstants.STATE.InCall) {
            return 3;
        }
        return 0;
    }

    private int getDialogCallState(CallConstants.STATE psCallState, boolean isRemoteHeld) {
        String str = LOG_TAG;
        Log.i(str, "session.mRemoteHeld : " + isRemoteHeld);
        if (ImsCallUtil.isHoldCallState(psCallState) || (psCallState == CallConstants.STATE.InCall && isRemoteHeld)) {
            return 2;
        }
        if (ImsCallUtil.isActiveCallState(psCallState)) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasInternalCallToIgnorePublishDialog(int phoneId) {
        ImsRegistration reg;
        ImsProfile imsProfile;
        boolean returnBoolean = false;
        for (ImsCallSession session : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (session != null && ((phoneId == -1 || session.getPhoneId() == phoneId) && (reg = session.getRegistration()) != null && (imsProfile = reg.getImsProfile()) != null && isCmcPrimaryType(imsProfile.getCmcType()))) {
                if (session.getCallState() == CallConstants.STATE.IncomingCall || session.getCallState() == CallConstants.STATE.InCall || (session.getCallState() == CallConstants.STATE.AlertingCall && session.getEndReason() != 5)) {
                    returnBoolean = true;
                }
            }
        }
        String str = LOG_TAG;
        Log.i(str, "sendPublishDialog, returnBoolean: " + returnBoolean);
        return returnBoolean;
    }

    /* access modifiers changed from: package-private */
    public boolean isNeedDelayToSendPublishDialog(int phoneId) {
        ImsRegistration reg;
        ImsProfile imsProfile;
        boolean returnBoolean = false;
        for (ImsCallSession session : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
            if (session != null && ((phoneId == -1 || session.getPhoneId() == phoneId) && (reg = session.getRegistration()) != null && (imsProfile = reg.getImsProfile()) != null && imsProfile.getCmcType() == 1 && session.getEndReason() == 20)) {
                returnBoolean = true;
            }
        }
        return returnBoolean;
    }

    /* access modifiers changed from: package-private */
    public ImsRegistration updateAudioInterfaceByCmc(int phoneId, int direction) {
        ImsRegistration reg = null;
        if (direction == 5) {
            int cmcType = 5;
            if (ImsRegistry.getP2pCC().isEnabledWifiDirectFeature()) {
                cmcType = 7;
            }
            for (int type = 1; type <= cmcType; type += 2) {
                reg = getCmcRegistration(phoneId, false, type);
                if (reg != null) {
                    this.mMediaController.bindToNetwork(reg.getNetwork());
                    return reg;
                }
            }
            return reg;
        } else if (direction != 8) {
            return null;
        } else {
            Log.i(LOG_TAG, "updateAudioInterface for CMC SD call.");
            return getCmcRegistration(phoneId, false, 2);
        }
    }

    public boolean isCallServiceAvailableOnSecondary(int phoneId, String service, boolean isRunning) {
        int cmcType = 2;
        while (cmcType <= 8) {
            ImsRegistration sdRegInfo = getCmcRegistration(phoneId, cmcType);
            if (!isRunning || sdRegInfo == null) {
                cmcType += 2;
            } else {
                String str = LOG_TAG;
                Log.i(str, "isCallServiceAvailableOnSecondary phoneId: " + phoneId + ", service=" + service);
                return sdRegInfo.hasService(service);
            }
        }
        Log.e(LOG_TAG, "disallow Call Service");
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onImsIncomingCallEventWithSendPublish(int phoneId, int cmcType) {
        int p2pType = 5;
        if (ImsRegistry.getP2pCC().isEnabledWifiDirectFeature()) {
            p2pType = 7;
        }
        for (int type = 1; type <= p2pType; type += 2) {
            ImsRegistration cmcRegi = getCmcRegistration(phoneId, false, type);
            if (cmcRegi != null && cmcType == 0) {
                int validPdCallCount = 0;
                ImsCallSession pdActiveSession = getSessionByCmcTypeAndState(type, CallConstants.STATE.InCall);
                ImsCallSession pdHeldSession = getSessionByCmcTypeAndState(type, CallConstants.STATE.HeldCall);
                if (pdActiveSession != null) {
                    validPdCallCount = 0 + 1;
                }
                if (pdHeldSession != null) {
                    validPdCallCount++;
                }
                if (validPdCallCount == 0) {
                    sendPublishDialogInternal(phoneId, cmcRegi);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasDialingOrIncomingCall() {
        if (getSessionByCmcTypeAndState(0, CallConstants.STATE.IncomingCall) == null && getSessionByCmcTypeAndState(0, CallConstants.STATE.OutGoingCall) == null && getSessionByCmcTypeAndState(0, CallConstants.STATE.AlertingCall) == null) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onImsCallEventWhenEstablished(int phoneId, ImsCallSession session, ImsRegistration regiInfo) {
        int cmcType = 5;
        if (ImsRegistry.getP2pCC().isEnabledWifiDirectFeature()) {
            cmcType = 7;
        }
        for (int type = 1; type <= cmcType; type += 2) {
            ImsRegistration cmcRegi = getCmcRegistration(phoneId, false, type);
            if (cmcRegi != null && session.getCmcType() == 0) {
                int validPdCallCount = 0;
                ImsCallSession pdSession = getSessionByCmcType(type);
                if (pdSession != null) {
                    if (session.getCallProfile().isMOCall()) {
                        if (pdSession.getCallState() != CallConstants.STATE.Idle) {
                            validPdCallCount = 0 + 1;
                        }
                        if (validPdCallCount == 0) {
                            sendPublishDialogInternal(phoneId, cmcRegi);
                        }
                    }
                } else if (session.getCallProfile().isMOCall()) {
                    sendPublishDialogInternal(phoneId, cmcRegi);
                } else {
                    int extCallCnt = getSessionCountByCmcType(phoneId, regiInfo);
                    if (session.getCallProfile().getCallType() == 2 || session.getCallProfile().getCallType() == 1 || extCallCnt > 1) {
                        sendPublishDialogInternal(phoneId, cmcRegi);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onImsCallEventWithHeldBoth(ImsCallSession session, ImsRegistration regiInfo) {
        if (regiInfo != null) {
            int phoneId = regiInfo.getPhoneId();
            int cmcType = 5;
            if (ImsRegistry.getP2pCC().isEnabledWifiDirectFeature()) {
                cmcType = 7;
            }
            for (int type = 1; type <= cmcType; type += 2) {
                ImsRegistration cmcRegi = getCmcRegistration(phoneId, false, type);
                if (cmcRegi != null && session.getCmcType() == 0) {
                    int validPdCallCount = 0;
                    ImsCallSession pdSession = getSessionByCmcType(type);
                    if (!(pdSession == null || pdSession.getCallState() == CallConstants.STATE.Idle)) {
                        validPdCallCount = 0 + 1;
                    }
                    if (validPdCallCount == 0) {
                        sendPublishDialogInternal(phoneId, cmcRegi);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onCallEndedWithSendPublish(int phoneId, ImsCallSession endedSession) {
        if (isCmcRegExist(phoneId)) {
            int cmcType = 5;
            if (ImsRegistry.getP2pCC().isEnabledWifiDirectFeature()) {
                cmcType = 7;
            }
            for (int type = 1; type <= cmcType; type += 2) {
                ImsRegistration cmcRegi = getCmcRegistration(phoneId, false, type);
                int validPdCallCount = 0;
                ImsCallSession pdActiveSession = getSessionByCmcTypeAndState(type, CallConstants.STATE.InCall);
                ImsCallSession pdHeldSession = getSessionByCmcTypeAndState(type, CallConstants.STATE.HeldCall);
                if (pdActiveSession != null) {
                    validPdCallCount = 0 + 1;
                }
                if (pdHeldSession != null) {
                    validPdCallCount++;
                }
                if (cmcRegi != null && endedSession != null && endedSession.getCmcType() == 0) {
                    int boundSessionId = endedSession.getCallProfile().getCmcBoundSessionId();
                    for (ImsCallSession s : this.mImsCallSessionManager.getUnmodifiableSessionMap().values()) {
                        if (s.getCmcType() == type && s.getSessionId() != boundSessionId) {
                            if (s.getCallState() == CallConstants.STATE.OutGoingCall || s.getCallState() == CallConstants.STATE.AlertingCall) {
                                validPdCallCount++;
                            }
                        }
                    }
                    if (validPdCallCount == 0) {
                        sendPublishDialogInternal(phoneId, cmcRegi);
                    }
                } else if (!(cmcRegi == null || endedSession == null || endedSession.getCmcType() != type)) {
                    if (this.mImsCallSessionManager.getActiveExtCallCount() > 0 && validPdCallCount == 0 && (!endedSession.mIsEstablished || endedSession.getErrorCode() == 6007)) {
                        sendPublishDialogInternal(phoneId, cmcRegi);
                    }
                    if (endedSession.getCmcType() == 1) {
                        sendCmcCallStateForRcs(endedSession.getPhoneId(), ImsConstants.CmcInfo.CMC_DUMMY_TEL_NUMBER, false);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendCmcCallStateForRcs(int phoneId, String phoneNumber, boolean isCmcConnected) {
        if (getCmcRegistration(phoneId, false, 1) != null) {
            Log.i(LOG_TAG, "sendCmcCallStateForRcs");
            Intent intent = new Intent(ImsConstants.Intents.ACTION_CALL_STATE_CHANGED);
            intent.putExtra(ImsConstants.Intents.EXTRA_IS_INCOMING, false);
            intent.putExtra(ImsConstants.Intents.EXTRA_TEL_NUMBER, phoneNumber);
            intent.putExtra(ImsConstants.Intents.EXTRA_PHONE_ID, phoneId);
            intent.putExtra(ImsConstants.Intents.EXTRA_CALL_EVENT, isCmcConnected ? 2 : 1);
            intent.putExtra(ImsConstants.Intents.EXTRA_IS_CMC_CALL, true);
            intent.putExtra(ImsConstants.Intents.EXTRA_IS_CMC_CONNECTED, isCmcConnected);
            this.mContext.sendBroadcast(intent);
        }
    }

    /* access modifiers changed from: package-private */
    public DialogEvent filterOngoingDialogFromDialogEvent(DialogEvent de) {
        List<Dialog> dialogList = new ArrayList<>();
        for (Dialog d : de.getDialogList()) {
            if (d != null && !this.mImsCallSessionManager.hasSipCallId(d.getSipCallId())) {
                dialogList.add(d);
            }
        }
        DialogEvent tmpDe = new DialogEvent(de.getMsisdn(), dialogList);
        tmpDe.setPhoneId(de.getPhoneId());
        tmpDe.setRegId(de.getRegId());
        return tmpDe;
    }

    /* access modifiers changed from: package-private */
    public DialogEvent onCmcImsDialogEvent(ImsRegistration regiInfo, DialogEvent de) {
        ImsProfile profile = regiInfo.getImsProfile();
        if (profile != null) {
            DialogEvent tmpDe = filterOngoingDialogFromDialogEvent(de);
            if (tmpDe != null) {
                Log.i(LOG_TAG, "Filter DialogEvent");
                de = tmpDe;
            }
            boolean hasConfirmedDialogState = false;
            for (Dialog d : de.getDialogList()) {
                if (d != null && d.getState() == 1) {
                    hasConfirmedDialogState = true;
                }
            }
            if (hasConfirmedDialogState) {
                this.mIsCmcPdCheckRespRecevied.put(Integer.valueOf(de.getPhoneId()), false);
                startCmcPdCheckTimer(de.getPhoneId(), 20000, regiInfo.getHandle(), "sip:" + de.getMsisdn() + "@samsungims.com;gr=" + profile.getPriDeviceIdWithURN(), true);
            } else {
                Log.i(LOG_TAG, "No cofirmed Dilaog in nofity");
                stopCmcPdCheckTimer(de.getPhoneId());
            }
            this.mLastCmcDialogEvent[de.getPhoneId()] = de;
        }
        return de;
    }

    /* access modifiers changed from: protected */
    public void startCmcPdCheckTimer(int phoneId, long millis, int regId, String uriStr, boolean isFirstCheck) {
        stopCmcPdCheckTimer(phoneId);
        String str = LOG_TAG;
        Log.i(str, "startCmcPdCheckTimer: millis " + millis);
        Bundle pdCheckData = new Bundle();
        pdCheckData.putInt("reg_id", regId);
        pdCheckData.putString("uri", uriStr);
        pdCheckData.putBoolean("is_first_check", isFirstCheck);
        PreciseAlarmManager am = PreciseAlarmManager.getInstance(this.mContext);
        Message msg = obtainMessage(33, phoneId, -1, pdCheckData);
        this.mCmcPdCheckTimeOut.put(Integer.valueOf(phoneId), msg);
        am.sendMessageDelayed(getClass().getSimpleName(), msg, millis);
    }

    /* access modifiers changed from: protected */
    public void stopCmcPdCheckTimer(int phoneId) {
        if (this.mCmcPdCheckTimeOut.containsKey(Integer.valueOf(phoneId))) {
            String str = LOG_TAG;
            Log.i(str, "stopCmcPdCheckTimer[" + phoneId + "]");
            PreciseAlarmManager.getInstance(this.mContext).removeMessage(this.mCmcPdCheckTimeOut.remove(Integer.valueOf(phoneId)));
        }
    }

    private void checkPdAvailability(int phoneId, Bundle pdCheckData) {
        String uriStr = pdCheckData.getString("uri");
        int regId = pdCheckData.getInt("reg_id");
        boolean isFirstCheck = pdCheckData.getBoolean("is_first_check");
        String str = LOG_TAG;
        Log.i(str, "checkPdAvailability(), isFirstCheck: " + isFirstCheck);
        if (!this.mIsCmcPdCheckRespRecevied.containsKey(Integer.valueOf(phoneId))) {
            return;
        }
        if (this.mIsCmcPdCheckRespRecevied.get(Integer.valueOf(phoneId)).booleanValue() || isFirstCheck) {
            this.mOptionsSvcIntf.requestSendCmcCheckMsg(phoneId, regId, uriStr);
            startCmcPdCheckTimer(phoneId, 20000, regId, uriStr, false);
            this.mIsCmcPdCheckRespRecevied.put(Integer.valueOf(phoneId), false);
            return;
        }
        Log.i(LOG_TAG, "no 200 OK(OPTION) response from PD, remove pulling UI");
        stopCmcPdCheckTimer(phoneId);
        DialogEvent de = this.mLastCmcDialogEvent[phoneId];
        de.clearDialogList();
        ImsRegistry.getImsNotifier().onDialogEvent(de);
    }

    public void forwardCmcRecordingEventToSD(int phoneId, int event, int extra, int sessionId) {
        ImsCallSession pdSession;
        ImsCallSession extSession;
        String str = LOG_TAG;
        Log.i(str, "forwardCmcRecordingEventToSD, recordEvent: " + event + ", extra: " + extra + ", sessionId: " + sessionId);
        if (isCmcRegExist(phoneId) && (pdSession = this.mImsCallSessionManager.getSession(sessionId)) != null && pdSession.getCmcType() == 1 && (extSession = this.mImsCallSessionManager.getSession(pdSession.getCallProfile().getCmcBoundSessionId())) != null) {
            Log.i(LOG_TAG, "send CmcRecordingEvent to SD during cmc call relay");
            String extSipCallId = extSession.getCallProfile().getSipCallId();
            Bundle cmcInfoData = new Bundle();
            cmcInfoData.putInt("record_event", event);
            cmcInfoData.putInt("extra", extra);
            cmcInfoData.putString("sip_call_id", extSipCallId);
            this.mVolteSvcIntf.sendCmcInfo(sessionId, cmcInfoData);
        }
    }

    public void onCmcRecordingInfo(CmcInfoEvent cmcInfoEvent) {
        Log.i(LOG_TAG, "onCmcRecordingInfo");
        ImsCallSession sdAcitiveSession = getSessionByCmcTypeAndState(2, CallConstants.STATE.InCall);
        if (sdAcitiveSession != null) {
            sdAcitiveSession.notifyCmcInfoEvent(cmcInfoEvent);
        }
    }

    /* access modifiers changed from: package-private */
    public ImsRegistration getCmcRegistration(int phoneId, boolean isEmergency, int cmcType) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration reg = it.next();
            if (reg != null && reg.getImsRegi().getPhoneId() == phoneId && reg.getImsRegi().getImsProfile().hasEmergencySupport() == isEmergency && reg.getImsRegi().getImsProfile().getCmcType() == cmcType) {
                return reg.getImsRegi();
            }
        }
        return null;
    }

    private ImsRegistration getCmcRegistration(int phoneId, int cmcType) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration reg = it.next();
            if (reg != null && reg.getImsRegi().getPhoneId() == phoneId && reg.getImsRegi().getImsProfile().getCmcType() == cmcType) {
                return reg.getImsRegi();
            }
        }
        return null;
    }

    public ImsRegistration getCmcRegistration(int regId) {
        Iterator<Registration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration reg = it.next();
            if (regId == reg.getImsRegi().getHandle()) {
                String str = LOG_TAG;
                Log.i(str, "getCmcRegistration: found regId=" + reg.getImsRegi().getHandle());
                return reg.getImsRegi();
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void startCmcHandoverTimer(ImsRegistration regiInfo) {
        if (this.mCmcHandoverTimer != null) {
            Log.i(LOG_TAG, "already start cmc handover timer");
            return;
        }
        Log.i(LOG_TAG, "start cmc handover timer");
        PreciseAlarmManager am = PreciseAlarmManager.getInstance(this.mContext);
        this.mCmcHandoverTimer = obtainMessage(34, regiInfo);
        am.sendMessageDelayed(getClass().getSimpleName(), this.mCmcHandoverTimer, 5000);
    }

    private void onCmcHandoverTimerExpired(ImsRegistration regiInfo) {
        String str = LOG_TAG;
        Log.i(str, "onCmcHandoverTimerExpired handle : " + regiInfo.getHandle());
        this.mCmcHandoverTimer = null;
        this.mVolteSvcIntf.clearAllCallInternal(regiInfo.getImsProfile().getCmcType());
    }

    private CallProfile makeReplaceProfile(CallProfile previousProfile) {
        CallProfile profile = new CallProfile();
        profile.setReplaceSipCallId(previousProfile.getSipCallId());
        if (previousProfile.getDirection() == 0) {
            profile.setLetteringText(previousProfile.getLetteringText());
        } else {
            profile.setLetteringText(previousProfile.getDialingNumber());
        }
        profile.setCallType(previousProfile.getCallType());
        profile.setPhoneId(previousProfile.getPhoneId());
        profile.setAlertInfo(previousProfile.getAlertInfo());
        profile.setEmergencyRat(previousProfile.getEmergencyRat());
        profile.setUrn(previousProfile.getUrn());
        profile.setCLI(previousProfile.getCLI());
        profile.setConferenceCall(previousProfile.getConferenceType());
        profile.setMediaProfile(previousProfile.getMediaProfile());
        profile.setLineMsisdn(previousProfile.getLineMsisdn());
        profile.setOriginatingUri(previousProfile.getOriginatingUri());
        profile.setCmcBoundSessionId(previousProfile.getCmcBoundSessionId());
        profile.setCmcType(previousProfile.getCmcType());
        profile.setForceCSFB(previousProfile.isForceCSFB());
        profile.setDialingNumber(previousProfile.getDialingNumber());
        profile.setNetworkType(previousProfile.getNetworkType());
        profile.setSamsungMdmnCall(previousProfile.isSamsungMdmnCall());
        return profile;
    }

    private boolean isCmcPrimaryType(int cmcType) {
        if (cmcType == 1 || cmcType == 3 || cmcType == 5 || cmcType == 7) {
            return true;
        }
        return false;
    }

    private boolean isCmcSecondaryType(int cmcType) {
        if (cmcType == 2 || cmcType == 4 || cmcType == 8) {
            return true;
        }
        return false;
    }

    private boolean isP2pPrimaryType(int cmcType) {
        if (cmcType == 3 || cmcType == 5 || cmcType == 7) {
            return true;
        }
        return false;
    }

    public CmcCallInfo getCmcCallInfo() {
        Log.i(LOG_TAG, "getCmcCallInfo");
        int lineSlotId = ImsRegistry.getCmcAccountManager().getCurrentLineSlotIndex();
        int cmcType = 0;
        int cmcCallState = 0;
        String pdDeviceId = ImsRegistry.getCmcAccountManager().getCurrentLineOwnerDeviceId();
        Iterator<ImsCallSession> it = this.mImsCallSessionManager.getUnmodifiableSessionMap().values().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ImsCallSession s = it.next();
            if (s.getCmcType() > 0) {
                cmcType = isCmcPrimaryType(s.getCmcType()) ? 1 : 2;
                if (s.getCallState() == CallConstants.STATE.IncomingCall) {
                    cmcCallState = 1;
                } else if (s.getCallState() == CallConstants.STATE.OutGoingCall || s.getCallState() == CallConstants.STATE.AlertingCall) {
                    cmcCallState = 2;
                } else if (s.getCallState() != CallConstants.STATE.Idle && s.getCallState() != CallConstants.STATE.ReadyToCall && s.getCallState() != CallConstants.STATE.EndingCall && s.getCallState() != CallConstants.STATE.EndedCall) {
                    cmcCallState = 3;
                }
            }
        }
        return new CmcCallInfo.Builder().setLineSlotId(lineSlotId).setCmcType(cmcType).setCallState(cmcCallState).setPdDeviceId(pdDeviceId).build();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 31:
                OptionsEvent event = (OptionsEvent) ((AsyncResult) msg.obj).result;
                String str = LOG_TAG;
                Log.i(str, "Received EVENT_P2P_OPTIONS_EVENT: " + event.getPhoneId());
                ImsRegistry.getImsNotifier().onP2pPushCallEvent(this.mLastCmcDialogEvent[event.getPhoneId()]);
                return;
            case 32:
                OptionsEvent optionEvent = (OptionsEvent) ((AsyncResult) msg.obj).result;
                String str2 = LOG_TAG;
                Log.i(str2, "Received EVENT_OPTIONS_EVENT, isSuccess: " + optionEvent.isSuccess());
                ImsRegistration cmcRegi = getCmcRegistration(optionEvent.getSessionId());
                int cmcType = 0;
                if (cmcRegi != null) {
                    cmcType = cmcRegi.getImsProfile().getCmcType();
                }
                String str3 = LOG_TAG;
                Log.i(str3, "optionEvent regi handle: " + optionEvent.getSessionId() + ", cmcType: " + cmcType);
                if (isCmcPrimaryType(cmcType)) {
                    sendDummyPublishDialog(optionEvent.getPhoneId(), cmcType);
                    return;
                } else if (!isCmcSecondaryType(cmcType)) {
                    return;
                } else {
                    if (!this.mCmcPdCheckTimeOut.containsKey(Integer.valueOf(optionEvent.getPhoneId()))) {
                        Log.e(LOG_TAG, "CmcPdCheckTimer is not running");
                        return;
                    } else if (optionEvent.isSuccess()) {
                        this.mIsCmcPdCheckRespRecevied.put(Integer.valueOf(optionEvent.getPhoneId()), true);
                        return;
                    } else {
                        String str4 = LOG_TAG;
                        Log.e(str4, "ERROR Resopnse, remove pulling UI, optionFailReason: " + optionEvent.getReason());
                        stopCmcPdCheckTimer(optionEvent.getPhoneId());
                        DialogEvent de = this.mLastCmcDialogEvent[optionEvent.getPhoneId()];
                        de.clearDialogList();
                        ImsRegistry.getImsNotifier().onDialogEvent(de);
                        return;
                    }
                }
            case 33:
                checkPdAvailability(msg.arg1, (Bundle) msg.obj);
                return;
            case 34:
                onCmcHandoverTimerExpired((ImsRegistration) msg.obj);
                return;
            case 35:
                Log.i(LOG_TAG, "Received EVT_CMC_INFO_EVENT");
                onCmcRecordingInfo((CmcInfoEvent) ((AsyncResult) msg.obj).result);
                return;
            default:
                return;
        }
    }
}
