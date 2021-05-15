package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.samsung.android.ims.cmc.SemCmcRecordingInfo;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IRttEventListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.cmc.CmcCallInfo;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.extensions.WiFiManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IVolteServiceEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.EmcBsIndication;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.Registration;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;
import com.sec.internal.ims.servicemodules.volte2.data.DedicatedBearerEvent;
import com.sec.internal.ims.servicemodules.volte2.data.DtmfInfo;
import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;
import com.sec.internal.ims.servicemodules.volte2.data.SIPDataEvent;
import com.sec.internal.ims.servicemodules.volte2.data.TextInfo;
import com.sec.internal.ims.settings.DmProfileLoader;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.ims.xq.att.ImsXqReporter;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;

public class VolteServiceModule extends VolteServiceModuleInternal implements IVolteServiceModule {
    public /* bridge */ /* synthetic */ IImsCallSession getForegroundSession() {
        return super.getForegroundSession();
    }

    public /* bridge */ /* synthetic */ IImsCallSession getSessionByCallId(int i) {
        return super.getSessionByCallId(i);
    }

    public VolteServiceModule(Looper looper, Context context, IRegistrationManager rm, IPdnController pc, IVolteServiceInterface volteServiceInterface, IMediaServiceInterface mediaServiceInterface, IOptionsServiceInterface optionsServiceInterface) {
        super(looper, context, rm, pc, volteServiceInterface, mediaServiceInterface, optionsServiceInterface);
    }

    public void setUpTest(ImsCallSessionManager manager, IVolteServiceInterface stackIf, ImsMediaController media) {
        Log.i(LOG_TAG, "setUpTest:");
        this.mVolteSvcIntf.unregisterForIncomingCallEvent(this);
        this.mVolteSvcIntf.unregisterForCallStateEvent(this);
        this.mVolteSvcIntf.unregisterForDialogEvent(this);
        this.mVolteSvcIntf.unregisterForDedicatedBearerNotifyEvent(this);
        this.mVolteSvcIntf.unregisterForRtpLossRateNoti(this);
        this.mImsCallSessionManager = manager;
        this.mVolteSvcIntf = stackIf;
        this.mMediaController = media;
        this.mVolteSvcIntf.registerForIncomingCallEvent(this, 1, (Object) null);
        this.mVolteSvcIntf.registerForCallStateEvent(this, 2, (Object) null);
        this.mVolteSvcIntf.registerForDialogEvent(this, 3, (Object) null);
        this.mVolteSvcIntf.registerForDedicatedBearerNotifyEvent(this, 8, (Object) null);
        this.mVolteSvcIntf.registerForRtpLossRateNoti(this, 18, (Object) null);
    }

    public Context getContext() {
        return this.mContext;
    }

    public CmcServiceHelper getCmcServiceHelper() {
        return this.mCmcServiceModule;
    }

    public String[] getServicesRequiring() {
        return new String[]{"mmtel", "mmtel-video", "mmtel-call-composer", "cdpn"};
    }

    public void onConfigured(int phoneId) {
        Log.i(LOG_TAG, "onConfigured:");
        updateFeature(phoneId);
    }

    public void onSimReady(int phoneId) {
        Log.i(LOG_TAG, "onSimReady:");
    }

    private void onEventSimReady(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "onEventSimReady<" + phoneId + ">");
        updateFeature(phoneId);
        if (this.mEcholocateIntentBroadcaster != null) {
            Mno mno = SimUtil.getSimMno(phoneId);
            if (mno.equalsWithSalesCode(Mno.TMOUS, OmcCode.get()) || mno.equalsWithSalesCode(Mno.SPRINT, OmcCode.get())) {
                this.mEcholocateIntentBroadcaster.start();
            } else {
                this.mEcholocateIntentBroadcaster.stop();
            }
        }
        if (this.mImsXqReporter == null) {
            return;
        }
        if (ImsXqReporter.isXqEnabled(this.mContext)) {
            this.mImsXqReporter.start();
        } else {
            this.mImsXqReporter.stop();
        }
    }

    public boolean isVolteServiceStatus() {
        return isVolteServiceStatus(this.mDefaultPhoneId);
    }

    public boolean isVolteServiceStatus(int phoneId) {
        boolean isVolteServiceStatus = true;
        ImsRegistration regInfo = getImsRegistration(phoneId);
        if (regInfo != null) {
            isVolteServiceStatus = DmProfileLoader.getProfile(this.mContext, regInfo.getImsProfile(), phoneId).isVolteServiceStatus();
        }
        ImsProfile imsDmProfile = LOG_TAG;
        Log.i(imsDmProfile, "VolteServiceStatus : " + isVolteServiceStatus);
        return isVolteServiceStatus;
    }

    public boolean isVolteSupportECT() {
        return isVolteSupportECT(this.mDefaultPhoneId);
    }

    public boolean isVolteSupportECT(int phoneId) {
        boolean isVolteEnableECT = false;
        ImsRegistration regInfo = getImsRegistration(phoneId);
        if (!(regInfo == null || regInfo.getImsProfile() == null || !regInfo.getImsProfile().getSupportEct())) {
            if (hasEmergencyCall(regInfo.getPhoneId())) {
                Log.i(LOG_TAG, "Has emergenacy call");
            } else {
                isVolteEnableECT = true;
            }
        }
        String str = LOG_TAG;
        Log.i(str, "isVolteSupportECT : " + isVolteEnableECT);
        return isVolteEnableECT;
    }

    private void updateFeature(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "phoneId : " + phoneId);
        this.mEnabledFeatures[phoneId] = 0;
        updateFeatureMmtel(phoneId);
        updateFeatureMmtelVideo(phoneId);
        int val = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, phoneId), DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, -1, phoneId)).intValue();
        if ((val == 2 || val == 3) && DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-call-composer", phoneId) == 1) {
            long[] jArr = this.mEnabledFeatures;
            jArr[phoneId] = jArr[phoneId] | Capabilities.FEATURE_MMTEL_CALL_COMPOSER;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("Update Feature " + this);
    }

    private void updateFeatureMmtel(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        boolean isVowifiEnabled = isVowifiEnabled(phoneId);
        if (mno.isOneOf(Mno.SKT, Mno.KT, Mno.LGU)) {
            long[] jArr = this.mEnabledFeatures;
            jArr[phoneId] = jArr[phoneId] | ((long) Capabilities.FEATURE_MMTEL);
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel", phoneId) == 1 && DmConfigHelper.readSwitch(this.mContext, "mmtel", true, phoneId)) {
            long[] jArr2 = this.mEnabledFeatures;
            jArr2[phoneId] = jArr2[phoneId] | ((long) Capabilities.FEATURE_MMTEL);
        }
        if ((mno == Mno.VZW || mno.isEur() || mno.isSea() || mno.isMea() || mno.isOce() || mno.isSwa()) && isVowifiEnabled) {
            long[] jArr3 = this.mEnabledFeatures;
            jArr3[phoneId] = jArr3[phoneId] | ((long) Capabilities.FEATURE_MMTEL);
        }
        if (mno == Mno.SPRINT && VowifiConfig.isEnabled(this.mContext, phoneId)) {
            long[] jArr4 = this.mEnabledFeatures;
            jArr4[phoneId] = jArr4[phoneId] | ((long) Capabilities.FEATURE_MMTEL);
        }
        if (ImsRegistry.getCmcAccountManager().isCmcEnabled()) {
            long[] jArr5 = this.mEnabledFeatures;
            jArr5[phoneId] = jArr5[phoneId] | ((long) Capabilities.FEATURE_MMTEL);
        }
    }

    private void updateFeatureMmtelVideo(int phoneId) {
        if (SimUtil.getSimMno(phoneId).isOneOf(Mno.SKT, Mno.KT, Mno.LGU)) {
            boolean isVolteSettingEnabled = isVolteSettingEnabled();
            boolean isVolteServiceStatus = isVolteServiceStatus();
            boolean isLTEDataModeEnabled = isLTEDataModeEnabled();
            if (isVolteSettingEnabled && isVolteServiceStatus && isLTEDataModeEnabled) {
                long[] jArr = this.mEnabledFeatures;
                jArr[phoneId] = jArr[phoneId] | ((long) Capabilities.FEATURE_MMTEL_VIDEO);
            }
        }
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-video", phoneId) == 1 && DmConfigHelper.readSwitch(this.mContext, "mmtel-video", true, phoneId)) {
            long[] jArr2 = this.mEnabledFeatures;
            jArr2[phoneId] = jArr2[phoneId] | ((long) Capabilities.FEATURE_MMTEL_VIDEO);
        }
    }

    public void onVoWiFiSwitched(int phoneId) {
        Log.i(LOG_TAG, "onVoWiFiSwitched:");
        updateFeature(phoneId);
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        Log.i(LOG_TAG, "onServiceSwitched");
        updateFeature(phoneId);
    }

    /* access modifiers changed from: protected */
    public void startEpdnDisconnectTimer(int phoneId, long millis) {
        stopEpdnDisconnectTimer(phoneId);
        String str = LOG_TAG;
        Log.i(str, "startRetryTimer: millis " + millis);
        PreciseAlarmManager am = PreciseAlarmManager.getInstance(this.mContext);
        Message msg = obtainMessage(16, phoneId, -1);
        this.mEpdnDisconnectTimeOut.put(Integer.valueOf(phoneId), msg);
        am.sendMessageDelayed(getClass().getSimpleName(), msg, millis);
    }

    /* access modifiers changed from: protected */
    public void stopEpdnDisconnectTimer(int phoneId) {
        if (this.mEpdnDisconnectTimeOut.containsKey(Integer.valueOf(phoneId))) {
            String str = LOG_TAG;
            Log.i(str, "stopEpdnDisconnectTimer[" + phoneId + "]");
            this.mEpdnDisconnectTimeOut.remove(Integer.valueOf(phoneId));
            PreciseAlarmManager.getInstance(this.mContext).removeMessage((Message) this.mEpdnDisconnectTimeOut.get(Integer.valueOf(phoneId)));
        }
    }

    public void onRegistered(ImsRegistration regiInfo) {
        if (regiInfo != null && regiInfo.getImsProfile() != null) {
            ImsProfile profile = regiInfo.getImsProfile();
            int phoneId = regiInfo.getPhoneId();
            IMSLog.c(LogClass.VOLTE_REGISTERED, "" + phoneId);
            ImsRegistration oldRegiInfo = getImsRegistration(phoneId);
            this.mLastRegiErrorCode[phoneId] = SipErrorBase.OK.getCode();
            super.onRegistered(regiInfo);
            Mno mno = Mno.fromName(profile.getMnoName());
            if (profile.hasEmergencySupport()) {
                SimpleEventLog simpleEventLog = this.mEventLog;
                simpleEventLog.add("Emergency Registered Feature " + this.mEnabledFeatures[phoneId]);
                if (mno == Mno.KDDI) {
                    startEpdnDisconnectTimer(phoneId, 300000);
                    return;
                }
                return;
            }
            if (this.mWfcEpdgMgr.isEpdgServiceConnected()) {
                boolean allowReleaseWfcBeforeHO = ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.Call.ALLOW_RELEASE_WFC_BEFORE_HO, false);
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.logAndAdd(mno + " is allow release call " + allowReleaseWfcBeforeHO);
                this.mWfcEpdgMgr.getEpdgMgr().setReleaseCallBeforeHO(phoneId, allowReleaseWfcBeforeHO);
            }
            if (!(this.mRegMan == null || !this.mRegMan.isVoWiFiSupported(phoneId) || oldRegiInfo == null || oldRegiInfo.getEpdgStatus() == regiInfo.getEpdgStatus())) {
                ImsRegistration oldEmergencyRegiInfo = getImsRegistration(phoneId, true);
                if (mno == Mno.ATT && oldEmergencyRegiInfo != null && regiInfo.getEpdgStatus() != oldEmergencyRegiInfo.getEpdgStatus() && !hasEmergencyCall(phoneId)) {
                    this.mRegMan.stopEmergencyRegistration(phoneId);
                }
                this.mImsCallSessionManager.handleEpdgHandover(phoneId, regiInfo, mno);
            }
            terminateMoWfcWhenWfcSettingOff(phoneId);
            String str = LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Registered to VOLTE service. ");
            sb.append(IMSLog.checker(regiInfo + ""));
            sb.append(" TTYMode ");
            sb.append(this.mTtyMode[phoneId]);
            Log.i(str, sb.toString());
            SimpleEventLog simpleEventLog3 = this.mEventLog;
            simpleEventLog3.logAndAdd("Registered Feature " + this.mEnabledFeatures[phoneId] + " with handle " + regiInfo.getHandle());
            if (!(profile.getTtyType() == 1 || profile.getTtyType() == 3)) {
                this.mVolteSvcIntf.setTtyMode(phoneId, 0, this.mTtyMode[phoneId]);
            }
            if (isCmcPrimaryType(profile.getCmcType())) {
                this.mCmcMediaController.connectToSve(phoneId);
            }
            if (regiInfo.hasService("mmtel")) {
                this.mMmtelAcquiredEver = true;
                this.mProhibited[phoneId] = false;
            } else {
                Log.i(LOG_TAG, "Registration Without MMTEL has DialogList notify empty dialog");
                clearDialogList(phoneId, regiInfo.getHandle());
            }
            this.mImsCallSessionManager.onRegistered(regiInfo);
            this.mCmcServiceModule.onRegistered(regiInfo);
        }
    }

    public void onDeregistering(ImsRegistration regiInfo) {
        Log.i(LOG_TAG, "onDeregistering");
        this.mCmcServiceModule.onDeregistering(regiInfo);
        removeMessages(13);
        sendMessage(obtainMessage(13, regiInfo));
    }

    private void handleDeregistering(ImsRegistration regiInfo) {
        super.onDeregistering(regiInfo);
        String str = LOG_TAG;
        Log.i(str, "handleDeregistering " + IMSLog.checker(regiInfo));
        int deRegisteringRegId = regiInfo.getHandle();
        if (Mno.fromName(regiInfo.getImsProfile().getMnoName()) == Mno.TMOUS && regiInfo.getDeregiReason() == 11) {
            Log.i(LOG_TAG, "TMO_E911, deregReason is MOVE_NEXT_PCSCF, just return");
        } else if (isRunning()) {
            removeMessages(9);
            if (regiInfo.getImsProfile().getCmcType() != 2 || this.mCmcServiceModule.getSessionCountByCmcType(regiInfo.getPhoneId(), regiInfo.getImsProfile().getCmcType()) <= 0) {
                this.mImsCallSessionManager.endCallByDeregistered(regiInfo);
            } else {
                this.mCmcServiceModule.startCmcHandoverTimer(regiInfo);
            }
            clearDialogList(regiInfo.getPhoneId(), deRegisteringRegId);
        }
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        Log.i(LOG_TAG, "onDeregistered");
        IMSLog.c(LogClass.VOLTE_DEREGISTERED, regiInfo.getPhoneId() + "," + errorCode);
        this.mCmcServiceModule.onDeregistered(regiInfo, errorCode);
        this.mLastRegiErrorCode[regiInfo.getPhoneId()] = errorCode;
        removeMessages(12);
        sendMessage(obtainMessage(12, errorCode, 0, regiInfo));
    }

    private void handleDeregistered(ImsRegistration regiInfo, int errorCode) {
        super.onDeregistered(regiInfo, errorCode);
        Log.i(LOG_TAG, "handleDeregistered");
        if (isCmcPrimaryType(regiInfo.getImsProfile().getCmcType())) {
            this.mCmcMediaController.disconnectToSve();
        }
        this.mImsCallSessionManager.handleDeregistered(regiInfo.getPhoneId(), errorCode, Mno.fromName(regiInfo.getImsProfile().getMnoName()));
        if (regiInfo.getImsProfile().hasEmergencySupport()) {
            String str = LOG_TAG;
            Log.i(str, "Deregistered emergency profile = " + errorCode + ", reason = " + regiInfo.getDeregiReason());
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("Emergency Deregistered reason " + errorCode + " with handle " + regiInfo.getHandle());
            if (this.mEcbmMode[regiInfo.getPhoneId()]) {
                return;
            }
            if (Mno.fromName(regiInfo.getImsProfile().getMnoName()) == Mno.ATT && errorCode != 200 && errorCode != 1606) {
                Log.i(LOG_TAG, "Do not stopEmergencyRegistration It's ATT and error Code is not 200 nor 1606");
                this.mEventLog.add("Do not stopEmergencyRegistration It's ATT and error Code is not 200");
            } else if (Mno.fromName(regiInfo.getImsProfile().getMnoName()) == Mno.TMOUS && regiInfo.getDeregiReason() == 11) {
                Log.i(LOG_TAG, "TMO_E911, deregReason is MOVE_NEXT_PCSCF, just return");
            } else {
                this.mRegMan.stopEmergencyRegistration(regiInfo.getPhoneId());
            }
        } else {
            String str2 = LOG_TAG;
            Log.i(str2, "Deregistered from VOLTE service. reason " + errorCode);
            int deRegisteredRegId = regiInfo.getHandle();
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("Deregistered reason " + errorCode + " with handle " + deRegisteredRegId);
            if (isRunning()) {
                if (regiInfo.getImsProfile().getCmcType() != 2 || this.mCmcServiceModule.getSessionCountByCmcType(regiInfo.getPhoneId(), regiInfo.getImsProfile().getCmcType()) <= 0) {
                    this.mImsCallSessionManager.endCallByDeregistered(regiInfo);
                } else {
                    this.mCmcServiceModule.startCmcHandoverTimer(regiInfo);
                }
                clearDialogList(regiInfo.getPhoneId(), deRegisteredRegId);
            }
        }
    }

    public void onNetworkChanged(NetworkEvent event, int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "onNetworkChanged: " + event);
        NetworkEvent oldEvent = (NetworkEvent) this.mNetworks.get(Integer.valueOf(phoneId));
        if (!(event == null || oldEvent == null || event.network == oldEvent.network)) {
            IMSLog.c(LogClass.VOLTE_RAT_CHANGE, phoneId + "," + ((NetworkEvent) this.mNetworks.get(Integer.valueOf(phoneId))).network + "->" + event.network);
        }
        this.mNetworks.put(Integer.valueOf(phoneId), event);
        removeMessages(9);
        sendMessage(obtainMessage(9, 100, phoneId));
    }

    private void tryDisconnect(int delay, int phoneId) {
        int rat = ((NetworkEvent) this.mNetworks.get(Integer.valueOf(phoneId))).network;
        String str = LOG_TAG;
        Log.i(str, "tryDisconnect(" + phoneId + ") delay " + delay);
        ImsRegistration regiInfo = getImsRegistration(phoneId);
        if (regiInfo != null) {
            Mno mno = Mno.fromName(regiInfo.getImsProfile().getMnoName());
            if (mno.isKor() && TelephonyManagerExt.getNetworkClass(rat) == 2) {
                Log.i(LOG_TAG, "to do nothing");
            } else if (mno == Mno.ATT && regiInfo.getImsProfile().isSoftphoneEnabled() && rat != 0) {
                Log.i(LOG_TAG, "to do nothing");
            } else if (hasActiveCall(phoneId) && this.mPdnController.isEpdgConnected(phoneId) && this.mPdnController.isWifiConnected()) {
                Log.i(LOG_TAG, "to do nothing - Continue Wifi call");
            } else if (ImsCallUtil.isMultiPdnRat(rat)) {
                if (this.mRegMan.isSuspended(regiInfo.getHandle())) {
                    if (delay > 2000) {
                        Log.e(LOG_TAG, "isSuspended(), waited enough...");
                    } else {
                        Log.e(LOG_TAG, "isSuspended(), retrying...");
                        sendMessageDelayed(obtainMessage(9, delay * 2, phoneId), (long) delay);
                        return;
                    }
                }
                this.mRatChanged[phoneId] = true;
                this.mImsCallSessionManager.endcallByNwHandover(regiInfo);
            }
        }
    }

    public int getParticipantIdForMerge(int phoneId, int hostId) {
        return this.mImsCallSessionManager.getParticipantIdForMerge(phoneId, hostId);
    }

    public CallProfile createCallProfile(int serviceType, int callType) {
        CallProfile profile = new CallProfile();
        profile.setCallType(callType);
        return profile;
    }

    public ImsRegistration getRegInfo(int regId) {
        if (regId == -1) {
            return getImsRegistration();
        }
        Iterator it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration reg = (Registration) it.next();
            if (regId == reg.getImsRegi().getHandle()) {
                String str = LOG_TAG;
                Log.i(str, "getRegInfo: found regId=" + reg.getImsRegi().getHandle());
                return reg.getImsRegi();
            }
        }
        return getImsRegistration();
    }

    private ImsRegistration getRegInfo(String msisdn) {
        if (msisdn == null) {
            return getImsRegistration();
        }
        Iterator it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            Registration reg = (Registration) it.next();
            if (msisdn.equals(reg.getImsRegi().getImpi())) {
                return reg.getImsRegi();
            }
        }
        return getImsRegistration();
    }

    public ImsCallSession createSession(CallProfile profile) throws RemoteException {
        return this.mImsCallSessionManager.createSession(profile, profile == null ? null : getImsRegistration(profile.getPhoneId()));
    }

    public ImsCallSession createSession(CallProfile profile, int regId) throws RemoteException {
        return this.mImsCallSessionManager.createSession(profile, getRegInfo(regId));
    }

    public void checkCmcP2pList(ImsRegistration regInfo, CallProfile profile) {
        this.mCmcServiceModule.checkCmcP2pList(regInfo, profile);
    }

    public synchronized int sendRttSessionModifyRequest(int callId, boolean mode) {
        return this.mImsCallSessionManager.sendRttSessionModifyRequest(callId, mode);
    }

    public void setAutomaticMode(int phoneId, boolean mode) {
        boolean prevMode = this.mAutomaticMode[phoneId];
        this.mAutomaticMode[phoneId] = mode;
        String str = LOG_TAG;
        Log.i(str, "setAutomaticMode: " + prevMode + " -> " + mode);
        if (prevMode == mode) {
            Log.e(LOG_TAG, "setAutomaticMode: ignored");
        } else {
            this.mVolteSvcIntf.setAutomaticMode(phoneId, mode);
        }
    }

    public boolean getAutomaticMode() {
        return getAutomaticMode(this.mDefaultPhoneId);
    }

    public boolean getAutomaticMode(int phoneId) {
        return this.mAutomaticMode[phoneId];
    }

    public synchronized void sendRttSessionModifyResponse(int callId, boolean accept) {
        this.mImsCallSessionManager.sendRttSessionModifyResponse(callId, accept);
    }

    public IImsMediaController getImsMediaController() {
        return this.mMediaController;
    }

    public ICmcMediaController getCmcMediaController() {
        return this.mCmcMediaController;
    }

    public ImsCallSession getPendingSession(String callId) {
        String str = LOG_TAG;
        Log.i(str, "getPendingSession: callId " + callId);
        return this.mImsCallSessionManager.getIncomingCallSession();
    }

    public boolean getExtMoCall() {
        return this.mImsCallSessionManager.getExtMoCall();
    }

    public void registerForVolteServiceEvent(int phoneId, IVolteServiceEventListener listener) {
        this.mVolteNotifier.registerForVolteServiceEvent(phoneId, listener);
    }

    public void deRegisterForVolteServiceEvent(int phoneId, IVolteServiceEventListener listener) {
        this.mVolteNotifier.deRegisterForVolteServiceEvent(phoneId, listener);
    }

    public void registerRttEventListener(int phoneId, IRttEventListener listener) {
        this.mVolteNotifier.registerRttEventListener(phoneId, listener);
    }

    public void unregisterRttEventListener(int phoneId, IRttEventListener listener) {
        this.mVolteNotifier.unregisterRttEventListener(phoneId, listener);
    }

    public void registerDialogEventListener(int phoneId, IDialogEventListener listener) {
        this.mVolteNotifier.registerDialogEventListener(phoneId, listener);
    }

    public void unregisterDialogEventListener(int phoneId, IDialogEventListener listener) {
        this.mVolteNotifier.unregisterDialogEventListener(phoneId, listener);
    }

    public void registerForCallStateEvent(IImsCallEventListener listener) {
        registerForCallStateEvent(this.mDefaultPhoneId, listener);
    }

    public void registerForCallStateEvent(int phoneId, IImsCallEventListener listener) {
        this.mVolteNotifier.registerForCallStateEvent(phoneId, listener);
    }

    public void deregisterForCallStateEvent(IImsCallEventListener listener) {
        deregisterForCallStateEvent(this.mDefaultPhoneId, listener);
    }

    public void deregisterForCallStateEvent(int phoneId, IImsCallEventListener listener) {
        this.mVolteNotifier.deregisterForCallStateEvent(phoneId, listener);
    }

    public void registerCmcRecordingListener(int phoneId, ISemCmcRecordingListener listener) {
        this.mVolteNotifier.registerCmcRecordingListener(phoneId, listener);
    }

    public void unregisterCmcRecordingListener(int phoneId, ISemCmcRecordingListener listener) {
        this.mVolteNotifier.unregisterCmcRecordingListener(phoneId, listener);
    }

    public void setUiTTYMode(int phoneId, int mode, Message onComplete) {
        String str = LOG_TAG;
        Log.i(str, "setUiTTYMode: phoneId = " + phoneId + ", mode = " + mode + ", do nothing");
        if (onComplete != null && onComplete.replyTo != null) {
            try {
                onComplete.replyTo.send(onComplete);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int getTtyMode() {
        return this.mTtyMode[this.mDefaultPhoneId];
    }

    public int getTtyMode(int phoneId) {
        return this.mTtyMode[phoneId];
    }

    public boolean isRttCall(int sessionId) {
        return this.mImsCallSessionManager.isRttCall(sessionId);
    }

    public void setRttMode(int mode) {
        super.setRttMode(mode);
    }

    public void setRttMode(int phoneId, int mode) {
        super.setRttMode(phoneId, mode);
    }

    public int getRttMode() {
        return this.mRttMode[this.mDefaultPhoneId];
    }

    public int getRttMode(int phoneId) {
        return this.mRttMode[phoneId];
    }

    public void sendRttMessage(String msg) {
        String str = LOG_TAG;
        Log.i(str, "sendRttMessage: " + msg);
        this.mImsCallSessionManager.sendRttMessage(msg);
    }

    public synchronized void onSendRttSessionModifyRequest(int callId, boolean mode) {
        int i;
        int phoneId = this.mImsCallSessionManager.getPhoneIdByCallId(callId);
        int phoneId2 = phoneId != -1 ? phoneId : this.mDefaultPhoneId;
        MediaProfile mediaProfile = getSessionByCallId(callId).getCallProfile().getMediaProfile();
        if (mode) {
            i = 1;
        } else {
            i = 0;
        }
        mediaProfile.setRttMode(i);
        this.mVolteNotifier.onSendRttSessionModifyRequest(phoneId2, getSessionByCallId(callId), mode);
    }

    public synchronized void onSendRttSessionModifyResponse(int callId, boolean mode, boolean result) {
        int i;
        int phoneId = this.mImsCallSessionManager.getPhoneIdByCallId(callId);
        int phoneId2 = phoneId != -1 ? phoneId : this.mDefaultPhoneId;
        MediaProfile mediaProfile = getSessionByCallId(callId).getCallProfile().getMediaProfile();
        if (mode == result) {
            i = 1;
        } else {
            i = 0;
        }
        mediaProfile.setRttMode(i);
        this.mVolteNotifier.onSendRttSessionModifyResponse(phoneId2, getSessionByCallId(callId), mode, result);
    }

    private void onCallStatusChange(int phoneId, int callEvent) {
        Mno mno;
        if (this.mRegMan != null) {
            this.mRegMan.updateTelephonyCallStatus(phoneId, callEvent);
        }
        IConfigModule cm = ImsRegistry.getConfigModule();
        if (cm != null) {
            cm.updateTelephonyCallStatus(phoneId, callEvent);
        }
        ImsRegistration regInfo = getImsRegistration(phoneId);
        if (regInfo == null) {
            mno = SimUtil.getSimMno(phoneId);
        } else {
            mno = Mno.fromName(regInfo.getImsProfile().getMnoName());
        }
        if (mno == Mno.VZW && callEvent == 0) {
            this.mSsacManager.sendMessage(obtainMessage(callEvent, Integer.valueOf(phoneId)));
        }
    }

    private void onImsDialogEvent(DialogEvent de) {
        if (de == null) {
            Log.e(LOG_TAG, "ignoring dialog list is null");
            return;
        }
        ImsRegistration regiInfo = getRegInfo(de.getRegId());
        if (!(regiInfo == null || regiInfo.getImsProfile() == null)) {
            if (Mno.fromName(regiInfo.getImsProfile().getMnoName()) == Mno.VZW) {
                for (Dialog d : de.getDialogList()) {
                    if (d.isExclusive()) {
                        d.setIsPullAvailable(false);
                        Log.i(LOG_TAG, "Exclusive call can't pulling");
                    } else if (d.isHeld()) {
                        d.setIsPullAvailable(false);
                        Log.i(LOG_TAG, "Held call can't pulling");
                    } else if (d.isVideoPortZero()) {
                        d.setIsPullAvailable(true);
                        d.setCallType(1);
                        Log.i(LOG_TAG, "Downgraded video call can pulling and change callType to Voice");
                    } else if (ImsCallUtil.isVideoCall(d.getCallType()) && d.getVideoDirection() == 1) {
                        d.setIsPullAvailable(false);
                        Log.i(LOG_TAG, "Backgrounded Video call can't pulling");
                    } else if (!ImsCallUtil.isVideoCall(d.getCallType()) || regiInfo.hasService("mmtel-video")) {
                        d.setIsPullAvailable(true);
                    } else {
                        d.setIsPullAvailable(false);
                        Log.i(LOG_TAG, "video call can't pulling with video feature");
                    }
                }
            }
            if (isCmcPrimaryType(regiInfo.getImsProfile().getCmcType())) {
                Log.i(LOG_TAG, "Ignore DialogEvent");
                return;
            } else if (isCmcSecondaryType(regiInfo.getImsProfile().getCmcType())) {
                de = this.mCmcServiceModule.onCmcImsDialogEvent(regiInfo, de);
            }
        }
        this.mLastDialogEvent[de.getPhoneId()] = de;
        this.mVolteNotifier.notifyOnDialogEvent(de);
        ImsRegistry.getImsNotifier().onDialogEvent(de);
        String str = LOG_TAG;
        Log.i(str, "Last Notified Dialog Event : " + this.mLastDialogEvent[de.getPhoneId()]);
    }

    private void onEcbmStateChanged(int phoneId, boolean on) {
        String str = LOG_TAG;
        Log.i(str, "onEcbmStateChanged: ecbm=" + on + " oldEcbm[" + phoneId + "]=" + this.mEcbmMode[phoneId]);
        boolean oldEcbm = this.mEcbmMode[phoneId];
        this.mEcbmMode[phoneId] = on;
        IMSLog.c(LogClass.VOLTE_CHANGE_ECBM, phoneId + "," + oldEcbm + "," + this.mEcbmMode[phoneId]);
        if (!this.mEcbmMode[phoneId] && oldEcbm) {
            this.mRegMan.stopEmergencyRegistration(phoneId);
        }
    }

    private void onSreenOnOffChanged(int on) {
        String str = LOG_TAG;
        Log.i(str, "onSreenOnOffChanged: on =" + on);
        if (this.mVolteSvcIntf != null) {
            this.mVolteSvcIntf.updateScreenOnOff(this.mDefaultPhoneId, on);
        }
    }

    public int updateSSACInfo(int voiceFactor, int voiceTime, int videoFactor, int videoTime) {
        return updateSSACInfo(this.mDefaultPhoneId, voiceFactor, voiceTime, videoFactor, videoTime);
    }

    public int updateSSACInfo(int phoneId, int voiceFactor, int voiceTime, int videoFactor, int videoTime) {
        if (this.mSsacManager == null) {
            Log.e(LOG_TAG, "mSsacManager was not exist!");
            return 0;
        }
        this.mSsacManager.updateSSACInfo(phoneId, voiceFactor, voiceTime, videoFactor, videoTime);
        return 0;
    }

    public void updateAudioInterface(int phoneId, int direction) {
        ImsRegistration reg;
        String str = LOG_TAG;
        Log.i(str, "updateAudioInterface, phoneId :" + phoneId + ", direction: " + direction);
        String mode = ImsCallUtil.getAudioMode(direction);
        if (direction == 5 || direction == 8) {
            reg = this.mCmcServiceModule.updateAudioInterfaceByCmc(phoneId, direction);
        } else {
            reg = getImsRegistration(phoneId);
            if (reg == null && this.mCmcServiceModule.isCmcRegExist(phoneId)) {
                for (int cmcType = 2; cmcType <= 8; cmcType += 2) {
                    reg = this.mCmcServiceModule.getCmcRegistration(phoneId, false, cmcType);
                    if (reg != null) {
                        break;
                    }
                }
            }
        }
        if (reg == null) {
            Log.e(LOG_TAG, "There is no IMS Registration take Emergency Regi");
            reg = getImsRegistration(phoneId, true);
        }
        if (reg != null) {
            this.mVolteSvcIntf.updateAudioInterface(reg.getHandle(), mode);
        }
        if (!"STOP".equals(mode)) {
            this.mImsCallSessionManager.forceNotifyCurrentCodec();
        }
    }

    public void enableCallWaitingRule(boolean enableRule) {
        this.mEnableCallWaitingRule = enableRule;
    }

    public boolean isCallBarredBySSAC(int phoneId, int calltype) {
        if (this.mPdnController.isEpdgConnected(phoneId)) {
            return false;
        }
        boolean result = this.mSsacManager.isCallBarred(phoneId, calltype);
        String str = LOG_TAG;
        Log.i(str, "isCallBarredBySSAC[" + phoneId + "]: result for call type " + calltype + " is " + result);
        return result;
    }

    public DialogEvent getLastDialogEvent() {
        return this.mLastDialogEvent[this.mDefaultPhoneId];
    }

    public DialogEvent getLastDialogEvent(int phoneId) {
        return this.mLastDialogEvent[phoneId];
    }

    public void pushCall(int callId, String targetNumber) {
        String str = LOG_TAG;
        Log.i(str, "pushCall: callId : " + callId + ", targetNumber : " + IMSLog.checker(targetNumber));
        ImsCallSession callSession = getSessionByCallId(callId);
        if (callSession == null) {
            String str2 = LOG_TAG;
            Log.i(str2, "callId(" + callId + ") is invalid");
            return;
        }
        this.mImsExternalCallController.pushCall(callSession, targetNumber, getImsRegistration(callSession.getPhoneId()));
    }

    public void consultativeTransferCall(int fgCallId, int bgCallId) {
        ImsCallSession activeSession = getSessionByCallId(fgCallId);
        ImsCallSession heldSession = getSessionByCallId(bgCallId);
        if (activeSession == null) {
            String str = LOG_TAG;
            Log.i(str, "fgCallId(" + fgCallId + ") is invalid");
        } else if (heldSession == null) {
            String str2 = LOG_TAG;
            Log.i(str2, "bgCallId(" + bgCallId + ") is invalid");
        } else {
            this.mImsExternalCallController.consultativeTransferCall(activeSession, heldSession, getImsRegistration(activeSession.getPhoneId()));
        }
    }

    public void transferCall(String msisdn, String dialogId) throws RemoteException {
        this.mImsExternalCallController.transferCall(this.mDefaultPhoneId, msisdn, dialogId, this.mLastDialogEvent);
    }

    public void notifyOnPulling(int phoneId, int pullingCallId) {
        this.mVolteNotifier.notifyOnPulling(phoneId, pullingCallId);
    }

    public void notifyOnCmcRecordingEvent(int phoneId, int event, int extra, int sessionId) {
        this.mVolteNotifier.notifyOnCmcRecordingEvent(phoneId, event, extra);
        this.mCmcServiceModule.forwardCmcRecordingEventToSD(phoneId, event, extra, sessionId);
    }

    /* access modifiers changed from: protected */
    public void onDedicatedBearerEvent(DedicatedBearerEvent event) {
        ImsCallSession session = getSession(event.getBearerSessionId());
        if (session == null) {
            String str = LOG_TAG;
            Log.i(str, "onDedicatedBearerEvent: unknown session " + event.getBearerSessionId());
            return;
        }
        session.setDedicatedBearerState(event.getQci(), event.getBearerState());
        String str2 = LOG_TAG;
        Log.i(str2, "onDedicatedBearerEvent: received for session : " + session + " ,bearer state : " + event.getBearerState() + " ,qci : " + event.getQci());
        this.mVolteNotifier.onDedicatedBearerEvent(session, event);
    }

    public void setActiveImpu(int phoneId, String impu) {
        if (TextUtils.isEmpty(impu)) {
            this.mActiveImpu[phoneId] = null;
        } else {
            this.mActiveImpu[phoneId] = ImsUri.parse(impu);
        }
    }

    public ImsUri getActiveImpu() {
        return getActiveImpu(this.mDefaultPhoneId);
    }

    public ImsUri getActiveImpu(int phoneId) {
        if (this.mActiveImpu == null) {
            return null;
        }
        return this.mActiveImpu[phoneId];
    }

    private void onReleaseWfcBeforeHO(int phoneId) {
        this.mImsCallSessionManager.onReleaseWfcBeforeHO(phoneId);
        this.mReleaseWfcBeforeHO[phoneId] = true;
    }

    /* access modifiers changed from: protected */
    /* renamed from: onImsCallEvent */
    public void lambda$handleMessage$1$VolteServiceModule(CallStateEvent event) {
        ImsRegistration regiInfo;
        ImsCallSession session = getSession(event.getSessionID());
        if (session == null) {
            regiInfo = getImsRegistration();
        } else {
            regiInfo = getImsRegistration(session.getPhoneId());
        }
        boolean z = true;
        if (regiInfo != null) {
            Mno mno = Mno.fromName(regiInfo.getImsProfile().getMnoName());
            if (mno == Mno.VZW && ImsCallUtil.isImsOutageError(event.getErrorCode())) {
                this.mProhibited[regiInfo.getPhoneId()] = true;
                String str = LOG_TAG;
                Log.i(str, "onImsCallEvent: Receive 503 Outage Error session " + event.getSessionID());
            } else if (mno == Mno.TELEFONICA_UK && session == null && event.getErrorCode() != null && event.getErrorCode().equals(SipErrorBase.SIP_TIMEOUT)) {
                Log.i(LOG_TAG, "onImsCallEvent: Notify 708 to RegiGvn even if session null");
                IRegistrationGovernor regGov = this.mRegMan.getRegistrationGovernor(regiInfo.getHandle());
                if (regGov != null) {
                    regGov.onSipError("mmtel", SipErrorBase.SIP_TIMEOUT);
                    return;
                }
                return;
            }
        }
        if (event.getParams() != null) {
            String audioCodec = event.getParams().getAudioCodec();
            String audioBitRate = event.getParams().getAudioBitRate();
            if (!(event.getState() == CallStateEvent.CALL_STATE.ENDED || event.getState() == CallStateEvent.CALL_STATE.ERROR)) {
                z = false;
            }
            sendAudioCodecInfo(audioCodec, audioBitRate, z);
        }
        if (session == null) {
            String str2 = LOG_TAG;
            Log.i(str2, "onImsCallEvent: unknown session " + event.getSessionID());
            return;
        }
        String str3 = LOG_TAG;
        Log.i(str3, "onImsCallEvent: session=" + event.getSessionID() + " state=" + event.getState());
        onImsCallEventForState(regiInfo, session, event);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.VolteServiceModule$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE;

        static {
            int[] iArr = new int[CallStateEvent.CALL_STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE = iArr;
            try {
                iArr[CallStateEvent.CALL_STATE.ESTABLISHED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.MODIFIED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.HELD_LOCAL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.HELD_REMOTE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.HELD_BOTH.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.CONFERENCE_ADDED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.CONFERENCE_REMOVED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.MODIFY_REQUESTED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.ENDED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.ERROR.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    private void onImsCallEventForState(ImsRegistration regiInfo, ImsCallSession session, CallStateEvent event) {
        ImsRegistration sdRegi;
        boolean needToNotify = true;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[event.getState().ordinal()]) {
            case 1:
                onImsCallEventForEstablish(regiInfo, session, event);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                this.mCmcServiceModule.onImsCallEventWithHeldBoth(session, regiInfo);
                break;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                if (isCmcSecondaryType(session.getCmcType()) && event.getState() == CallStateEvent.CALL_STATE.ERROR && (sdRegi = this.mCmcServiceModule.getCmcRegistration(session.getPhoneId(), false, session.getCmcType())) != null) {
                    clearDialogList(session.getPhoneId(), sdRegi.getHandle());
                }
                needToNotify = false;
                break;
        }
        if (needToNotify) {
            this.mVolteNotifier.notifyCallStateEvent(event, session);
        }
    }

    public boolean isProhibited(int phoneId) {
        return this.mProhibited[phoneId];
    }

    /* access modifiers changed from: protected */
    public void onRtpLossRateNoti(RtpLossRateNoti noti) {
        this.mVolteNotifier.notifyOnRtpLossRate(this.mDefaultPhoneId, noti);
    }

    public void onCallEnded(int phoneId, int sessionId, int error) {
        String str = LOG_TAG;
        Log.i(str, "onCallEnded[" + phoneId + "]: sessionId " + sessionId + ", error=" + error);
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno == Mno.KDDI && hasEmergencyCall(phoneId) && isEmergencyRegistered(phoneId)) {
            startEpdnDisconnectTimer(phoneId, 300000);
        }
        if (sessionId == -1 && error != 911) {
            Log.e(LOG_TAG, "Stack Return -1 release all session in F/W layer");
            this.mImsCallSessionManager.releaseAllSession(phoneId);
        }
        this.mCmcServiceModule.onCallEndedWithSendPublish(phoneId, getSession(sessionId));
        ImsCallSession session = this.mImsCallSessionManager.removeSession(sessionId);
        if (session != null) {
            onCallSessionEnded(session, mno);
        }
        if (this.mReleaseWfcBeforeHO[phoneId] && getSessionCount(phoneId) == 0) {
            Log.i(LOG_TAG, "All calls are release before HO, trigger HO to EPDG");
            if (this.mWfcEpdgMgr.isEpdgServiceConnected()) {
                this.mWfcEpdgMgr.getEpdgMgr().triggerHOAfterReleaseCall(phoneId);
            }
            this.mReleaseWfcBeforeHO[phoneId] = false;
        }
        ImsRegistration regiInfo = getImsRegistration(phoneId);
        if (!(regiInfo == null || Mno.fromName(regiInfo.getImsProfile().getMnoName()) == Mno.VZW)) {
            this.mMediaController.startCameraForActiveExcept(sessionId);
        }
        this.mImsCallSessionManager.onCallEnded();
    }

    private void onCallSessionEnded(ImsCallSession session, Mno mno) {
        IRegistrationGovernor regGov;
        int sessionId = session.getSessionId();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("Call End - " + sessionId + "(" + session.getCallId() + ") Reason(" + session.getEndType() + " - " + session.getEndReason() + "), Error(" + session.getErrorCode() + " - " + session.getErrorMessage() + ") " + this);
        int callType = session.getCallProfile().getCallType();
        boolean isEmergency = callType == 7 || callType == 8;
        String str = LOG_TAG;
        Log.i(str, "onCallEnded: callType: " + callType + ", isEmergency: " + isEmergency);
        if (mno == Mno.KDDI && isEmergency && this.mRegMan.isEpdnRequestPending(session.getPhoneId())) {
            Log.i(LOG_TAG, "EPDN request is still pending, need to stop EPDN to avoid retry");
            this.mRegMan.stopEmergencyPdnOnly(session.getPhoneId());
        }
        this.mVolteNotifier.notifyCallStateEvent(new CallStateEvent(CallStateEvent.CALL_STATE.ENDED), session);
        ImsRegistration registration = session.getRegistration();
        if (!(registration == null || hasActiveCall(session.getPhoneId()) || (regGov = this.mRegMan.getRegistrationGovernor(registration.getHandle())) == null)) {
            regGov.onCallStatus(IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END, (SipError) null, session.getCallProfile().getCallType());
        }
        if (this.mRegMan.isVoWiFiSupported(session.getPhoneId()) && isVowifiEnabled(session.getPhoneId()) && getCallCount(session.getPhoneId())[0] == 0) {
            WiFiManagerExt.setImsCallEstablished(this.mContext, false);
        }
        if (isCmcPrimaryType(session.getCmcType()) && session.getErrorCode() == 6007) {
            int boundSessionId = session.getCallProfile().getCmcBoundSessionId();
            String str2 = LOG_TAG;
            Log.i(str2, "call end due to call pull from SD to PD. bound session id = " + boundSessionId);
            if (boundSessionId > 0) {
                ImsCallSession boundSession = getSession(boundSessionId);
                if (boundSession != null) {
                    Log.i(LOG_TAG, "Ext session is PS. need to start audio engine internally");
                    updateAudioInterface(boundSession.getPhoneId(), 4);
                }
            } else {
                Log.i(LOG_TAG, "Ext session is CS");
            }
        }
        if (ImsRegistry.getP2pCC().isEnabledWifiDirectFeature() && session.getCmcType() == 0) {
            ImsRegistry.getP2pCC().changeWifiDirectConnection(false);
        }
    }

    public void onConferenceParticipantAdded(int sessionId, String participant) {
        String str = LOG_TAG;
        Log.i(str, "onConferenceParticipantAdded: sessionId " + sessionId);
        ImsCallSession session = getSession(sessionId);
        if (session != null && session.getCallProfile().isConferenceCall()) {
            CallStateEvent callState = new CallStateEvent(CallStateEvent.CALL_STATE.CONFERENCE_ADDED);
            callState.addUpdatedParticipantsList(participant, 0, 0, 0);
            this.mVolteNotifier.notifyCallStateEvent(callState, session);
        }
    }

    public void onConferenceParticipantRemoved(int sessionId, String participant) {
        String str = LOG_TAG;
        Log.i(str, "onConferenceParticipantRemoved: sessionId " + sessionId);
        ImsCallSession session = getSession(sessionId);
        if (session != null && session.getCallProfile().isConferenceCall()) {
            CallStateEvent callState = new CallStateEvent(CallStateEvent.CALL_STATE.CONFERENCE_REMOVED);
            callState.addUpdatedParticipantsList(participant, 0, 0, 0);
            this.mVolteNotifier.notifyCallStateEvent(callState, session);
        }
    }

    public void onUpdateGeolocation() {
        Log.i(LOG_TAG, "onUpdateGeolocation: ");
        this.mImsCallSessionManager.onUpdateGeolocation();
    }

    public void handleMessage(Message msg) {
        boolean z = true;
        if (this.mCheckRunningState || isRunning()) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    post(new Runnable((IncomingCallEvent) ((AsyncResult) msg.obj).result) {
                        public final /* synthetic */ IncomingCallEvent f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            VolteServiceModule.this.lambda$handleMessage$0$VolteServiceModule(this.f$1);
                        }
                    });
                    return;
                case 2:
                    postDelayed(new Runnable((CallStateEvent) ((AsyncResult) msg.obj).result) {
                        public final /* synthetic */ CallStateEvent f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            VolteServiceModule.this.lambda$handleMessage$1$VolteServiceModule(this.f$1);
                        }
                    }, 50);
                    return;
                case 3:
                    onImsDialogEvent((DialogEvent) ((AsyncResult) msg.obj).result);
                    return;
                case 5:
                    onCallStatusChange(msg.arg1, msg.arg2);
                    return;
                case 6:
                    if (!DeviceUtil.getGcfMode()) {
                        int i = msg.arg1;
                        if (msg.arg2 != 1) {
                            z = false;
                        }
                        onEcbmStateChanged(i, z);
                        return;
                    }
                    return;
                case 8:
                    onDedicatedBearerEvent((DedicatedBearerEvent) ((AsyncResult) msg.obj).result);
                    return;
                case 9:
                    tryDisconnect(msg.arg1, msg.arg2);
                    return;
                case 10:
                    this.mImsCallSessionManager.onCallEndByCS(msg.arg1);
                    return;
                case 11:
                    onImsIncomingCallEvent((IncomingCallEvent) msg.obj, true);
                    return;
                case 12:
                    handleDeregistered((ImsRegistration) msg.obj, msg.arg1);
                    return;
                case 13:
                    handleDeregistering((ImsRegistration) msg.obj);
                    return;
                case 14:
                    ImsCallSessionManager imsCallSessionManager = this.mImsCallSessionManager;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    imsCallSessionManager.onPSBarred(z);
                    return;
                case 15:
                    onImsDialogEvent((DialogEvent) msg.obj);
                    return;
                case 16:
                    if (getSessionCount(msg.arg1) > 0 && hasEmergencyCall(msg.arg1)) {
                        return;
                    }
                    if (isEmergencyRegistered(msg.arg1)) {
                        this.mRegMan.stopEmergencyRegistration(msg.arg1);
                        return;
                    } else {
                        this.mRegMan.stopEmergencyPdnOnly(msg.arg1);
                        return;
                    }
                case 17:
                    onDtmfInfo((DtmfInfo) ((AsyncResult) msg.obj).result);
                    return;
                case 18:
                    onRtpLossRateNoti((RtpLossRateNoti) ((AsyncResult) msg.obj).result);
                    return;
                case 19:
                    this.mImsCallSessionManager.handleEpdnSetupFail(msg.arg1);
                    return;
                case 20:
                    onReleaseWfcBeforeHO(msg.arg1);
                    return;
                case 21:
                    onConfigUpdated(msg.arg1, (String) msg.obj);
                    return;
                case 22:
                    onTextInfo((TextInfo) ((AsyncResult) msg.obj).result);
                    return;
                case 23:
                    onSreenOnOffChanged(msg.arg1);
                    return;
                case 24:
                    onSimSubscribeIdChanged((SubscriptionInfo) ((AsyncResult) msg.obj).result);
                    return;
                case 25:
                    this.mImsCallSessionManager.getSIPMSGInfo((SIPDataEvent) ((AsyncResult) msg.obj).result);
                    return;
                case 26:
                    onDefaultDataSubscriptionChanged();
                    return;
                case 27:
                    onSrvccStateChange(msg.arg1, ((Integer) msg.obj).intValue());
                    return;
                case 28:
                    int defaultPhoneId = getDefaultPhoneId();
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    onIQIServiceStateChanged(defaultPhoneId, z);
                    return;
                case 30:
                    onEventSimReady(((Integer) ((AsyncResult) msg.obj).result).intValue());
                    return;
                case 31:
                    Log.i(LOG_TAG, "Removed Call State set to Idle");
                    onCallStatusChange(((Integer) ((AsyncResult) msg.obj).result).intValue(), 0);
                    return;
                default:
                    return;
            }
        } else {
            this.mCheckRunningState = true;
            sendMessageDelayed(Message.obtain(msg), 500);
            Log.i(LOG_TAG, "VolteServiceModule not ready, retransmitting event " + msg.what);
        }
    }

    public /* synthetic */ void lambda$handleMessage$0$VolteServiceModule(IncomingCallEvent incoming) {
        onImsIncomingCallEvent(incoming, false);
    }

    /* access modifiers changed from: protected */
    public void onConfigUpdated(int phoneId, String item) {
        String str = LOG_TAG;
        Log.i(str, "onConfigUpdated[" + phoneId + "] : " + item);
        if ("VOLTE_ENABLED".equalsIgnoreCase(item) || "LVC_ENABLED".equalsIgnoreCase(item)) {
            onServiceSwitched(phoneId, (ContentValues) null);
        }
    }

    private void onDtmfInfo(DtmfInfo dtmfInfo) {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "isBikeMode", 0) == 1) {
            String str = LOG_TAG;
            Log.i(str, "BikeMode Active - Dtmf Val = " + dtmfInfo.getEvent());
            Intent intent = new Intent("com.samsung.ims.DTMF_RX_DIGI");
            intent.putExtra("dtmf_digit", dtmfInfo.getEvent());
            intent.setPackage(ImsConstants.Packages.PACKAGE_BIKE_MODE);
            this.mContext.sendBroadcast(intent);
            return;
        }
        Log.i(LOG_TAG, "Bike Mode is disabled discarding event");
    }

    private synchronized void onTextInfo(TextInfo textInfo) {
        this.mVolteNotifier.notifyOnRttEvent(this.mDefaultPhoneId, textInfo);
    }

    public boolean isVolteRetryRequired(int phoneId, int callType, SipError error) {
        return isVolteRetryRequired(phoneId, callType, error, 10);
    }

    public boolean isVolteRetryRequired(int phoneId, int callType, SipError error, int retryAfter) {
        Mno mno;
        if (error == null) {
            Log.e(LOG_TAG, "SipError was null!!");
            return false;
        } else if (!isSilentRedialEnabled(this.mContext, phoneId)) {
            Log.e(LOG_TAG, "isSilentRedialEnabled was false!");
            return false;
        } else {
            ImsRegistration regiInfo = getImsRegistration(phoneId);
            if (regiInfo == null) {
                mno = SimManagerFactory.getSimManager().getSimMno();
            } else {
                mno = Mno.fromName(regiInfo.getImsProfile().getMnoName());
            }
            boolean isVolteRetryRequired = false;
            try {
                String[] volteRetryErrorCodes = ImsRegistry.getStringArray(phoneId, GlobalSettingsConstants.Call.ALL_VOLTE_RETRY_ERROR_CODE_LIST, (String[]) null);
                String str = LOG_TAG;
                Log.i(str, "all_volte_retry_error_code_list " + Arrays.asList(volteRetryErrorCodes));
                isVolteRetryRequired = this.mImsCallSessionManager.isMatchWithErrorCodeList(volteRetryErrorCodes, error.getCode());
                if (!isVolteRetryRequired && ImsCallUtil.isVideoCall(callType)) {
                    String[] vilteRetryErrorCodes = ImsRegistry.getStringArray(phoneId, GlobalSettingsConstants.Call.VIDEO_VOLTE_RETRY_ERROR_CODE_LIST, (String[]) null);
                    String str2 = LOG_TAG;
                    Log.i(str2, "video_volte_retry_error_code_list " + Arrays.asList(vilteRetryErrorCodes));
                    isVolteRetryRequired = this.mImsCallSessionManager.isMatchWithErrorCodeList(vilteRetryErrorCodes, error.getCode());
                }
            } catch (JSONException e) {
            }
            if (mno != Mno.DOCOMO || this.mPdnController.getEmcBsIndication(phoneId) == EmcBsIndication.SUPPORTED) {
                return isVolteRetryRequired;
            }
            Log.e(LOG_TAG, "do not volte retry under eb not supported N/W");
            return false;
        }
    }

    public int getSignalLevel() {
        return getSignalLevel(this.mDefaultPhoneId);
    }

    public int getSignalLevel(int phoneId) {
        return this.mMobileCareController.getSignalLevel(phoneId);
    }

    public String getTrn(String srcMsisdn, String dstMsisdn) {
        return null;
    }

    public ImsUri getNormalizedUri(int phoneId, String msisdn) {
        UriGenerator ug = UriGeneratorFactory.getInstance().get(phoneId);
        if (ug == null) {
            Log.e(LOG_TAG, "getNormalizedUri: FATAL - no UriGenerator found.");
            return null;
        }
        ImsUri uri = ug.getNormalizedUri(msisdn);
        if (uri == null) {
            String str = LOG_TAG;
            Log.e(str, "getNormalizedUri: invalid msisdn=" + IMSLog.checker(msisdn));
            return null;
        }
        ImsRegistration reg = getImsRegistration(phoneId);
        if (reg == null) {
            Log.e(LOG_TAG, "getNormalizedUri: not registered!!");
            return null;
        }
        for (NameAddr addr : reg.getImpuList()) {
            if (uri.equals(ug.getNormalizedUri(UriUtil.getMsisdnNumber(addr.getUri())))) {
                return addr.getUri();
            }
        }
        return null;
    }

    private void onIQIServiceStateChanged(int phoneId, boolean enable) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, "onXqServiceStateChanged: " + enable);
        if (this.mImsXqReporter != null) {
            boolean xqEnable = enable && ImsXqReporter.isXqEnabled(this.mContext);
            if (xqEnable) {
                this.mImsXqReporter.start();
            } else {
                this.mImsXqReporter.stop();
            }
            if (this.mVolteSvcIntf != null) {
                this.mVolteSvcIntf.updateXqEnable(this.mDefaultPhoneId, xqEnable);
            }
        }
    }

    public ImsCallInfo[] getImsCallInfos(int phoneId) {
        List<ImsCallInfo> list = new ArrayList<>();
        for (ImsCallSession s : getSessionList(phoneId)) {
            list.add(new ImsCallInfo(s.getCallId(), s.getCallProfile().getCallType(), s.getCallProfile().isDowngradedVideoCall(), s.getCallProfile().isDowngradedAtEstablish(), s.getDedicatedBearerState(1), s.getDedicatedBearerState(2), s.getDedicatedBearerState(8), s.getErrorCode(), s.getErrorMessage(), s.getCallProfile().getDialingNumber(), s.getCallProfile().getDirection(), s.getCallProfile().isConferenceCall()));
        }
        return (ImsCallInfo[]) list.toArray(new ImsCallInfo[list.size()]);
    }

    public int getVoWIFIEmergencyCallRat(int phoneId) {
        for (ImsCallSession s : this.mImsCallSessionManager.getEmergencySession()) {
            if (phoneId == s.getPhoneId() && "VoWIFI".equalsIgnoreCase(s.getCallProfile().getEmergencyRat())) {
                return 18;
            }
        }
        return -1;
    }

    public void sendCmcRecordingEvent(int phoneId, int event, SemCmcRecordingInfo info) {
        getCmcMediaController().sendCmcRecordingEvent(phoneId, event, info);
    }

    public CmcCallInfo getCmcCallInfo() {
        return getCmcServiceHelper().getCmcCallInfo();
    }

    private void sendAudioCodecInfo(String codecName, String bitRate, boolean isEndCall) {
        Intent intent = new Intent("com.samsung.ims.imsservice.handler.secims.audio_info");
        intent.putExtra("IS_ENDCALL", isEndCall);
        intent.putExtra("CODEC_NAME", codecName);
        intent.putExtra("BIT_RATE", bitRate);
        IntentUtil.sendBroadcast(this.mContext, intent);
    }
}
