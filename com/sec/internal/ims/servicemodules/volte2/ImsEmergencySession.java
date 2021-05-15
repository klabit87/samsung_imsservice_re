package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.net.Network;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.volte2.data.CallSetupData;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;

public class ImsEmergencySession extends ImsCallSession {
    private static final int CAN_INVITE_TILL_180RINGING_ECALL_FAIL_TIMER = 9;
    private static final String LOG_TAG = "ImsEmergencySession";
    private static final int TMO_EUR_INVITE_TILL_18X_ECALL_FAIL_TIMER = 10;
    private static final int TMO_EUR_T_REG_ECALL_FAIL_TIMER = 6;
    private static final int TMO_US_INVITE_TILL_18X_ECALL_FAIL_TIMER = 25;

    /* access modifiers changed from: protected */
    public int getLte911Fail() {
        return DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_T_LTE_911_FAIL, 20, getPhoneId()).intValue();
    }

    /* access modifiers changed from: protected */
    public int getWlan911Fail() {
        return DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_TWLAN_911_CALLFAIL_TIMER, 10, getPhoneId()).intValue();
    }

    /* access modifiers changed from: protected */
    public int getLte911FailFromGlobalSettings() {
        return ImsRegistry.getInt(getPhoneId(), GlobalSettingsConstants.Call.T_LTE_911_FAIL, 10);
    }

    /* access modifiers changed from: protected */
    public boolean isEmergencyAvailable(Mno mno) {
        boolean isEmergencyAvailable = true;
        ImsProfile emergencyProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (emergencyProfile != null) {
            int currentNetworkType = "VoWIFI".equalsIgnoreCase(this.mCallProfile.getEmergencyRat()) ? 18 : 13;
            Log.e(LOG_TAG, "networktype : " + currentNetworkType);
            if (emergencyProfile.getCommercializedProfile() && !emergencyProfile.hasService("mmtel", currentNetworkType)) {
                isEmergencyAvailable = false;
                Log.e(LOG_TAG, "emergency service unavailable in current RAT");
            }
        } else {
            isEmergencyAvailable = false;
        }
        if (mno.isOneOf(Mno.H3G_SE, Mno.H3G, Mno.TELIA_SWE) && ImsRegistry.getPdnController().getVopsIndication(this.mPhoneId) == VoPsIndication.NOT_SUPPORTED && TelephonyManagerWrapper.getInstance(this.mContext).getDataNetworkType(SimUtil.getSubId(this.mPhoneId)) == 13) {
            Log.e(LOG_TAG, "if VoPS is not supported, do CSFB");
            isEmergencyAvailable = false;
        }
        if (mno != Mno.DOCOMO || !this.mRegistrationManager.isEmergencyCallProhibited(this.mPhoneId)) {
            return isEmergencyAvailable;
        }
        Log.e(LOG_TAG, "if DCM get 503 error in regi, do CSFB");
        return false;
    }

    public class EmergencyCallStateMachine extends CallStateMachine {
        private static final int EVENT_EMERGENCY_REGISTERED = 911;
        /* access modifiers changed from: private */
        public boolean mEmergencyRegistered = false;
        /* access modifiers changed from: private */
        public boolean mHasEstablished = false;
        /* access modifiers changed from: private */
        public boolean mNextPcscfChangedWorking = false;
        /* access modifiers changed from: private */
        public boolean mRequstedStopPDN = false;
        /* access modifiers changed from: private */
        public boolean mStartDelayed = false;
        protected EmergencyCallStateMachine mThisEsm = this;
        final /* synthetic */ ImsEmergencySession this$0;

        /* JADX WARNING: Illegal instructions before constructor call */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected EmergencyCallStateMachine(com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession r17, android.content.Context r18, com.sec.internal.ims.servicemodules.volte2.ImsCallSession r19, com.sec.ims.ImsRegistration r20, com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r21, com.sec.internal.constants.Mno r22, com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r23, android.os.RemoteCallbackList<com.sec.ims.volte2.IImsCallSessionEventListener> r24, com.sec.internal.interfaces.ims.core.IRegistrationManager r25, com.sec.internal.ims.servicemodules.volte2.IImsMediaController r26, java.lang.String r27, android.os.Looper r28) {
            /*
                r16 = this;
                r13 = r16
                r14 = r17
                r13.this$0 = r14
                r0 = r16
                r1 = r18
                r2 = r19
                r3 = r20
                r4 = r21
                r5 = r22
                r6 = r23
                r7 = r24
                r8 = r25
                r9 = r26
                r10 = r27
                r11 = r28
                r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
                r0 = 0
                r13.mHasEstablished = r0
                r13.mRequstedStopPDN = r0
                r13.mEmergencyRegistered = r0
                r13.mNextPcscfChangedWorking = r0
                r13.mStartDelayed = r0
                r13.mThisEsm = r13
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$ReadyToCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$ReadyToCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r1 = r16
                r3 = r19
                r6 = r22
                r12 = r16
                r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mReadyToCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$OutgoingCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$OutgoingCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mOutgoingCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$AlertingCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$AlertingCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mAlertingCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$InCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$InCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mInCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$EndingCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession$EmergencyCallStateMachine$EndingCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mEndingCall = r15
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession.EmergencyCallStateMachine.<init>(com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession, android.content.Context, com.sec.internal.ims.servicemodules.volte2.ImsCallSession, com.sec.ims.ImsRegistration, com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal, com.sec.internal.constants.Mno, com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface, android.os.RemoteCallbackList, com.sec.internal.interfaces.ims.core.IRegistrationManager, com.sec.internal.ims.servicemodules.volte2.IImsMediaController, java.lang.String, android.os.Looper):void");
        }

        public class ReadyToCall extends ImsReadyToCall {
            ImsProfile emergencyProfile = null;
            final /* synthetic */ EmergencyCallStateMachine this$1;

            /* JADX INFO: super call moved to the top of the method (can break code semantics) */
            ReadyToCall(EmergencyCallStateMachine this$12, Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
                this.this$1 = this$12;
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[ReadyToCall] processMessage " + msg.what);
                int i = msg.what;
                if (i != 1) {
                    if (i == 11) {
                        onStart(msg);
                    } else if (i == 14) {
                        return onEmergecyInvite();
                    } else {
                        if (i != 306) {
                            if (i == 402) {
                                boolean unused = this.this$1.mNextPcscfChangedWorking = false;
                                this.this$1.mThisEsm.sendMessage(14);
                            } else if (i == EmergencyCallStateMachine.EVENT_EMERGENCY_REGISTERED) {
                                return onEventEmergencyRegistered(msg);
                            } else {
                                if (i == 3) {
                                    return onEnded(msg);
                                }
                                if (i == 4) {
                                    return onError(msg);
                                }
                                if (!(i == 303 || i == 304)) {
                                    return super.processMessage(msg);
                                }
                            }
                        }
                    }
                    return true;
                }
                return false;
            }

            private void onStart(Message msg) {
                long delay;
                Log.i("CallStateMachine", "cmcType : " + this.this$1.this$0.mCmcType);
                if (this.this$1.this$0.isCmcSecondaryType(this.this$1.this$0.mCmcType)) {
                    Log.e("CallStateMachine", "[ReadyToCall] start: E911 is not allowed on SD.");
                    this.this$1.mThisSm.sendMessage(4, 0, -1, SipErrorBase.E911_NOT_ALLOWED_ON_SD);
                    return;
                }
                ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.this$1.this$0.mPhoneId);
                Mno mno = sm == null ? Mno.DEFAULT : sm.getSimMno();
                if (mno != Mno.TMOUS || this.this$1.mStartDelayed || !this.this$1.this$0.hasInProgressEmergencyTask()) {
                    this.emergencyProfile = this.mRegistrationManager.getImsProfile(this.this$1.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                    String nwOperator = ImsUtil.getSystemProperty("gsm.operator.numeric", this.this$1.this$0.mPhoneId, "00101");
                    if (sm == null || !sm.getDevMno().isAus() || (this.emergencyProfile != null && !"00101".equals(nwOperator))) {
                        if (!this.this$1.this$0.isEmergencyAvailable(mno)) {
                            Log.i("CallStateMachine", "emergency service unavailable. do CSFB");
                            this.this$1.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                            this.this$1.mThisEsm.sendMessage((int) CallStateMachine.ON_LTE_911_FAIL);
                            return;
                        }
                        if (!mno.isOneOf(Mno.VODAFONE_UK, Mno.H3G_DK, Mno.TELENOR_DK) || !"VoWIFI".equalsIgnoreCase(this.this$1.this$0.mCallProfile.getEmergencyRat()) || this.this$1.this$0.getEmergencyRegistration() != null) {
                            setEmergencyRttCall();
                            Message result = this.this$1.obtainMessage(EmergencyCallStateMachine.EVENT_EMERGENCY_REGISTERED);
                            IMSLog.c(LogClass.VOLTE_E911_REGI_START, "" + this.this$1.this$0.mPhoneId);
                            this.mRegistrationManager.startEmergencyRegistration(this.this$1.this$0.mPhoneId, result);
                            if (mno != Mno.KDDI) {
                                if (mno != Mno.VZW) {
                                    delay = ((long) this.this$1.this$0.getLte911FailFromGlobalSettings()) * 1000;
                                } else if (ImsRegistry.getPdnController().isEpdgConnected(this.this$1.this$0.mPhoneId)) {
                                    delay = ((long) this.this$1.this$0.getWlan911Fail()) * 1000;
                                } else {
                                    delay = ((long) this.this$1.this$0.getLte911Fail()) * 1000;
                                }
                                if (mno == Mno.TMOUS) {
                                    Log.i("CallStateMachine", "[ReadyToCall] TMO_E911 start E1 Timer");
                                } else if (this.mMno.isTmobile()) {
                                    Log.i("CallStateMachine", "[ReadyToCall] TMO_EUR_E911 start E1 REG timer");
                                    delay = 6000;
                                } else {
                                    Log.i("CallStateMachine", "[ReadyToCall] start Tlte or TWlan-911fail" + delay + " millis.");
                                }
                                IMSLog.c(LogClass.VOLTE_E911_CALL_TIMER_START, this.this$1.this$0.mPhoneId + "," + delay);
                                this.this$1.mThisEsm.sendMessageDelayed((int) CallStateMachine.ON_LTE_911_FAIL, delay);
                                return;
                            }
                            return;
                        }
                        Log.i("CallStateMachine", "[ReadyToCall] No IMS Registration, Do Call End");
                        this.this$1.mThisEsm.sendMessage(4, 0, -1, new SipError(1001, "No VoWIFI Registration"));
                    } else if (!this.this$1.mStartDelayed) {
                        Log.i("CallStateMachine", "switching network is in progress. retry after 1sec");
                        this.mRegistrationManager.refreshAuEmergencyProfile(this.this$1.this$0.mPhoneId);
                        this.this$1.mThisEsm.sendMessageDelayed(Message.obtain(msg), 1000);
                        boolean unused = this.this$1.mStartDelayed = true;
                    } else {
                        Log.i("CallStateMachine", "no Emergency profile, should CSFB now...");
                        this.this$1.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                        this.this$1.mThisEsm.sendMessage((int) CallStateMachine.ON_LTE_911_FAIL);
                    }
                } else {
                    Log.i("CallStateMachine", "Deregistering is in progress. retry after 1sec");
                    this.this$1.mThisEsm.sendMessageDelayed(Message.obtain(msg), 1000);
                    boolean unused2 = this.this$1.mStartDelayed = true;
                }
            }

            private void setEmergencyRttCall() {
                EmergencyCallStateMachine emergencyCallStateMachine = this.this$1;
                emergencyCallStateMachine.callType = emergencyCallStateMachine.this$0.mCallProfile.getCallType();
                if (this.mModule.getAutomaticMode(this.this$1.this$0.getPhoneId())) {
                    if (this.this$1.callType == 7) {
                        this.this$1.callType = 18;
                    } else if (this.this$1.callType == 8) {
                        this.this$1.callType = 19;
                    }
                    this.this$1.this$0.mCallProfile.setCallType(this.this$1.callType);
                }
            }

            private boolean onEventEmergencyRegistered(Message msg) {
                boolean unused = this.this$1.mEmergencyRegistered = true;
                IMSLog.c(LogClass.VOLTE_E911_REGI_DONE, "" + this.this$1.this$0.mPhoneId);
                if (msg.arg1 == -1) {
                    Log.i("CallStateMachine", "[ReadyToCall] PDN disconnected. do CSFB");
                    this.this$1.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                    this.this$1.mThisEsm.sendMessage((int) CallStateMachine.ON_LTE_911_FAIL);
                    return true;
                }
                ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.this$1.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                this.emergencyProfile = imsProfile;
                if (imsProfile == null) {
                    Log.i("CallStateMachine", "[ReadyToCall] EmergencyProfile is null!");
                    return false;
                }
                this.mMno = Mno.fromName(imsProfile.getMnoName());
                if (this.mMno == Mno.DOCOMO || this.mMno.isChn() || this.mMno == Mno.GCF || this.mMno == Mno.RJIL || this.mMno == Mno.TMOUS || this.mMno.isTmobile() || this.mMno == Mno.ATT || this.mMno == Mno.SPRINT) {
                    this.this$1.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                    if (this.mMno == Mno.TMOUS || this.mMno.isTmobile()) {
                        Log.i("CallStateMachine", "[ReadyToCall] Emergency E1 timer stopped");
                    } else {
                        Log.i("CallStateMachine", "[ReadyToCall] remove ON_LTE_911_FAIL");
                    }
                }
                if (((this.mMno == Mno.ATT || this.mMno == Mno.SPRINT || this.mMno == Mno.ALTICE || this.mMno == Mno.USCC) && ImsRegistry.getPdnController().isEmergencyEpdgConnected(this.this$1.this$0.mPhoneId)) || (this.mMno == Mno.VZW && this.emergencyProfile.getSupportedGeolocationPhase() >= 2 && !this.mModule.isRoaming(this.this$1.this$0.mPhoneId))) {
                    this.this$1.mThisEsm.sendMessage(13);
                } else {
                    this.this$1.mThisEsm.sendMessage(14);
                }
                return true;
            }

            private boolean onEmergecyInvite() {
                ImsUri destUri;
                ImsCallSession boundSession;
                Network emergencyNwk;
                int regId = -1;
                EmergencyCallStateMachine emergencyCallStateMachine = this.this$1;
                emergencyCallStateMachine.callType = emergencyCallStateMachine.this$0.mCallProfile.getCallType();
                if (this.mModule.getAutomaticMode(this.this$1.this$0.getPhoneId())) {
                    if (this.this$1.callType == 7) {
                        this.this$1.callType = 18;
                    } else if (this.this$1.callType == 8) {
                        this.this$1.callType = 19;
                    }
                    this.this$1.this$0.mCallProfile.setCallType(this.this$1.callType);
                }
                String dialedNumer = this.this$1.this$0.mCallProfile.getDialingNumber();
                if (this.this$1.this$0.mCallProfile.getUrn() != null) {
                    destUri = ImsUri.parse(this.this$1.this$0.mCallProfile.getUrn());
                } else {
                    destUri = this.this$1.this$0.buildUri(dialedNumer, (String) null, this.this$1.this$0.mCallProfile.getCallType());
                }
                ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(this.this$1.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                this.emergencyProfile = imsProfile;
                if (imsProfile == null) {
                    Log.i("CallStateMachine", "[ReadyToCall] EmergencyProfile is null!");
                    return false;
                }
                this.mMno = Mno.fromName(imsProfile.getMnoName());
                if (this.mMno == Mno.VZW && "922".equals(dialedNumer)) {
                    destUri = UriGeneratorFactory.getInstance().get(this.this$1.this$0.mCallProfile.getOriginatingUri()).getNetworkPreferredUri(ImsUri.UriType.SIP_URI, dialedNumer);
                    Log.i("CallStateMachine", "[ReadyToCall] makecall target change to " + destUri);
                }
                CallSetupData data = new CallSetupData(destUri, dialedNumer, this.this$1.callType, this.this$1.this$0.mCallProfile.getCLI());
                data.setOriginatingUri(this.this$1.this$0.getOriginatingUri());
                data.setLteEpsOnlyAttached(this.mModule.getLteEpsOnlyAttached(this.this$1.this$0.mPhoneId));
                data.setCmcBoundSessionId(this.this$1.this$0.mCallProfile.getCmcBoundSessionId());
                ImsRegistration reg = this.this$1.this$0.getEmergencyRegistration();
                if (reg != null) {
                    regId = reg.getHandle();
                    Log.i("CallStateMachine", "bind network for MediaEngine " + reg.getNetwork());
                    this.mMediaController.bindToNetwork(reg.getNetwork());
                }
                if (this.mMno == Mno.ATT && ImsRegistry.getPdnController().isEmergencyEpdgConnected(this.this$1.this$0.mPhoneId)) {
                    data.setPEmergencyInfoOfAtt(this.this$1.this$0.getPEmergencyInfoOfAtt((String) null));
                    Log.i("CallStateMachine", "e911Aid = " + data.getPEmergencyInfoOfAtt());
                }
                IUserAgent ua = this.this$1.this$0.getEmergencyUa();
                if (!(ua == null || (emergencyNwk = ua.getNetwork()) == null)) {
                    Log.i("CallStateMachine", "bind network for Emergency VT or RTT " + emergencyNwk);
                    this.mMediaController.bindToNetwork(emergencyNwk);
                }
                startEmergencyFailTimer();
                if (this.mMno != Mno.YTL || ua == null || (!ua.isRegistering() && !ua.isDeregistring())) {
                    int sessionId = this.mVolteSvcIntf.makeCall(regId, data, (HashMap<String, String>) null, this.this$1.this$0.mPhoneId);
                    Log.i("CallStateMachine", "[ReadyToCall] makeCall() returned session id " + sessionId);
                    if (sessionId < 0) {
                        this.this$1.mThisEsm.sendMessage(4, 0, -1, new SipError(1001, "stack return -1."));
                        return true;
                    }
                    int boundSessionId = this.mSession.getCallProfile().getCmcBoundSessionId();
                    if (boundSessionId > 0 && (boundSession = this.mModule.getSession(boundSessionId)) != null) {
                        boundSession.getCallProfile().setCmcBoundSessionId(sessionId);
                        Log.i("CallStateMachine", "[Emergency ReadyToCall] updated boundSessionId : " + boundSession.getCallProfile().getCmcBoundSessionId());
                    }
                    this.this$1.this$0.setSessionId(sessionId);
                    this.this$1.this$0.mCallProfile.setDirection(0);
                    EmergencyCallStateMachine emergencyCallStateMachine2 = this.this$1;
                    emergencyCallStateMachine2.transitionTo(emergencyCallStateMachine2.mOutgoingCall);
                    return true;
                }
                boolean unused = this.this$1.mEmergencyRegistered = false;
                this.this$1.mThisEsm.sendMessage(4, 0, -1, new SipError(1001, "UA is de/registring status"));
                return true;
            }

            private void startEmergencyFailTimer() {
                if ((this.mMno == Mno.BELL || this.mMno == Mno.ROGERS || this.mMno == Mno.TELUS || this.mMno == Mno.KOODO || this.mMno == Mno.VTR || this.mMno == Mno.EASTLINK) && !ImsRegistry.getPdnController().isEmergencyEpdgConnected(this.this$1.this$0.mPhoneId)) {
                    this.this$1.mThisEsm.sendMessageDelayed(307, 9000);
                }
            }

            private boolean onError(Message msg) {
                int errCode = ((SipError) msg.obj).getCode();
                if (this.mMno == Mno.YTL && !this.this$1.mEmergencyRegistered && errCode == 1001) {
                    ImsProfile profile = this.mRegistrationManager.getImsProfile(this.this$1.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                    if (!this.this$1.mRequstedStopPDN && profile != null) {
                        int delay = profile.getDeregTimeout(13);
                        Log.i("CallStateMachine", "Disconnect Emergency PDN.");
                        this.mRegistrationManager.stopEmergencyRegistration(this.this$1.this$0.mPhoneId);
                        boolean unused = this.this$1.mRequstedStopPDN = true;
                        this.this$1.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                        this.this$1.mThisEsm.sendMessageDelayed(Message.obtain(msg), (long) (delay + 500));
                        return true;
                    }
                }
                return super.processMessage(msg);
            }

            private boolean onEnded(Message msg) {
                if (this.mMno == Mno.TMOUS) {
                    Log.i("CallStateMachine", "[ReadyToCall] mNextPcscfChangedWorking=" + this.this$1.mNextPcscfChangedWorking);
                    if (this.this$1.mNextPcscfChangedWorking) {
                        Log.i("CallStateMachine", "[ReadyToCall] TMO_E911 ON_NEXT_PCSCF_CHANGED is running, so just return");
                        return true;
                    }
                }
                return super.processMessage(msg);
            }
        }

        public class OutgoingCall extends ImsOutgoingCall {
            OutgoingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public void enter() {
                ImsProfile emergencyProfile = this.mRegistrationManager.getImsProfile(EmergencyCallStateMachine.this.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
                boolean isNoSim = true;
                if (emergencyProfile == null || emergencyProfile.get100tryingTimer() <= 0) {
                    ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(EmergencyCallStateMachine.this.this$0.mPhoneId);
                    boolean isSimMobility = SimUtil.isSimMobilityFeatureEnabled();
                    if (sm != null) {
                        isNoSim = sm.hasNoSim();
                    }
                    Log.i("CallStateMachine", "isSimMobility : " + isSimMobility + ", isNoSim : " + isNoSim);
                    if (isSimMobility && isNoSim) {
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(208, (long) 10000);
                        Log.i("CallStateMachine", "[OutgoingCall] Start 100 Trying Timer (" + 10000 + " msec).");
                    }
                } else {
                    int timer_100trying = emergencyProfile.get100tryingTimer();
                    if (this.mMno == Mno.USCC && this.mModule.getSessionCount(EmergencyCallStateMachine.this.this$0.mPhoneId) == 1) {
                        Log.i("CallStateMachine", "[OutgoingCall] USCC G30 Timer (12 sec)");
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(208, 12000);
                    } else {
                        if (this.mMno == Mno.TMOUS) {
                            Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 start E2 Timer");
                        } else {
                            Log.i("CallStateMachine", "[OutgoingCall] Start 100 Trying Timer (" + timer_100trying + " msec).");
                        }
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(208, (long) timer_100trying);
                    }
                    if (this.mMno == Mno.TMOUS && !EmergencyCallStateMachine.this.mThisEsm.hasMessages(307)) {
                        Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 start E3 Timer");
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(307, 25000);
                    } else if (this.mMno.isTmobile() && !EmergencyCallStateMachine.this.mThisEsm.hasMessages(307)) {
                        Log.i("CallStateMachine", "[OutgoingCall] TMO_EUR_E911 start E3 Timer");
                        EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed(307, 10000);
                    }
                }
                super.enter();
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[OutgoingCall] processMessage " + msg.what);
                int i = msg.what;
                if (i == 31) {
                    return onTrying(msg);
                }
                if (i == 32 || i == 34 || i == 35) {
                    return onSessionProgress(msg);
                }
                if (i == 41) {
                    return onEstablished(msg);
                }
                if (i == 208) {
                    return on100TryingTimeOut(msg);
                }
                if (i == 306 || i == 303 || i == 304) {
                    return false;
                }
                return super.processMessage(msg);
            }

            private boolean onTrying(Message msg) {
                if (this.mMno == Mno.TMOUS || this.mMno.isTmobile()) {
                    Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 stop E2 Timer");
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(208);
                }
                return super.processMessage(msg);
            }

            private boolean onSessionProgress(Message msg) {
                if (EmergencyCallStateMachine.this.this$0.needRemoveTimerOn18X()) {
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                } else if (this.mMno == Mno.TMOUS || this.mMno.isTmobile()) {
                    Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 stop E2, E3 Timer");
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(208);
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                }
                return super.processMessage(msg);
            }

            private boolean onEstablished(Message msg) {
                if (this.mMno == Mno.TMOUS || this.mMno.isTmobile()) {
                    Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 stop E2, E3 Timer");
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(208);
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                }
                return super.processMessage(msg);
            }

            private boolean on100TryingTimeOut(Message msg) {
                if (this.mMno == Mno.TMOUS) {
                    Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 E2 Timer expired");
                    if (ImsCallUtil.isRttEmergencyCall(EmergencyCallStateMachine.this.this$0.mCallProfile.getCallType())) {
                        Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 RTT stop E3 timer and end call");
                        EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                    } else if (EmergencyCallStateMachine.this.mThisEsm.hasMessages(307)) {
                        Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 E3 Timer active");
                        if (!EmergencyCallStateMachine.this.this$0.isNoNextPcscf(EmergencyCallStateMachine.this.this$0.mPhoneId)) {
                            Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 redial to next p-cscf");
                            this.mRegistrationManager.moveNextPcscf(EmergencyCallStateMachine.this.this$0.mPhoneId, EmergencyCallStateMachine.this.obtainMessage(402));
                            boolean unused = EmergencyCallStateMachine.this.mNextPcscfChangedWorking = true;
                            EmergencyCallStateMachine emergencyCallStateMachine = EmergencyCallStateMachine.this;
                            emergencyCallStateMachine.transitionTo(emergencyCallStateMachine.mReadyToCall);
                            return true;
                        }
                        Log.i("CallStateMachine", "[OutgoingCall] TMO_E911 stop E3 timer and CSFB");
                        EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                    }
                }
                return super.processMessage(msg);
            }
        }

        public class AlertingCall extends ImsAlertingCall {
            AlertingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public void enter() {
                Log.i("CallStateMachine", "[AlertingCall] enter ");
                Mno mno = SimUtil.getSimMno(EmergencyCallStateMachine.this.this$0.mPhoneId);
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                if (mno != Mno.TMOUS && !this.mMno.isTmobile() && EmergencyCallStateMachine.this.mThisEsm.hasMessages(CallStateMachine.ON_LTE_911_FAIL)) {
                    long delay = ((long) EmergencyCallStateMachine.this.this$0.getLte911FailFromGlobalSettings()) * 1000;
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                    Log.i("CallStateMachine", "[AlertingCall] refresh Tlte_911fail timer : " + delay + " millis.");
                    EmergencyCallStateMachine.this.mThisEsm.sendMessageDelayed((int) CallStateMachine.ON_LTE_911_FAIL, delay);
                }
                super.enter();
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[AlertingCall] processMessage " + msg.what);
                int i = msg.what;
                if (i != 32) {
                    if (i == 306) {
                        return false;
                    }
                    if (!(i == 34 || i == 35)) {
                        if (i == 303) {
                            return onLte911Fail(msg);
                        }
                        if (i != 304) {
                            return super.processMessage(msg);
                        }
                        return false;
                    }
                }
                return onSessionProgress(msg);
            }

            private boolean onSessionProgress(Message msg) {
                if (EmergencyCallStateMachine.this.this$0.needRemoveTimerOn18X()) {
                    EmergencyCallStateMachine.this.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                }
                return super.processMessage(msg);
            }

            private boolean onLte911Fail(Message msg) {
                if (!this.mMno.isCanada() && this.mMno != Mno.VODAFONE_UK && !this.mMno.isKor()) {
                    return false;
                }
                Log.i("CallStateMachine", "[AlertingCall] Ignore ON_LTE_911_FAIL");
                return super.processMessage(msg);
            }
        }

        public class EndingCall extends ImsEndingCall {
            EndingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[EndingCall] processMessage " + msg.what);
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(307);
                if (msg.what == 3) {
                    boolean unused = EmergencyCallStateMachine.this.onEnded(msg);
                }
                return super.processMessage(msg);
            }

            public void exit() {
                Mno mno = SimUtil.getSimMno(EmergencyCallStateMachine.this.this$0.mPhoneId);
                if (!this.mModule.isEmergencyRegistered(EmergencyCallStateMachine.this.this$0.mPhoneId)) {
                    if (mno == Mno.VZW && (this.mModule.isEcbmMode(EmergencyCallStateMachine.this.this$0.mPhoneId) || EmergencyCallStateMachine.this.mHasEstablished)) {
                        Log.i("CallStateMachine", "ECBM mode. Keep Emergency PDN.");
                        super.exit();
                        return;
                    } else if (mno != Mno.ATT && !this.mMno.isKor()) {
                        Log.i("CallStateMachine", "Disconnect Emergency PDN.");
                        this.mRegistrationManager.stopEmergencyRegistration(EmergencyCallStateMachine.this.this$0.mPhoneId);
                    }
                }
                if (mno == Mno.ATT && this.mRegistrationManager.isVoWiFiSupported(EmergencyCallStateMachine.this.this$0.mPhoneId)) {
                    ImsRegistration emeregncyRegInfo = EmergencyCallStateMachine.this.this$0.getEmergencyRegistration();
                    ImsRegistration IMSRegInfo = EmergencyCallStateMachine.this.this$0.getIMSRegistration();
                    if (!(IMSRegInfo == null || emeregncyRegInfo == null || IMSRegInfo.getEpdgStatus() == emeregncyRegInfo.getEpdgStatus())) {
                        Log.i("CallStateMachine", "RAT is different between current IMS and Emergencywhich is already made but not de-registered.");
                        this.mRegistrationManager.stopEmergencyRegistration(EmergencyCallStateMachine.this.this$0.mPhoneId);
                    }
                }
                boolean unused = EmergencyCallStateMachine.this.mRequstedStopPDN = false;
                super.exit();
            }
        }

        public class InCall extends ImsInCall {
            InCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public void enter() {
                Log.i("CallStateMachine", "Enter [InCall]");
                EmergencyCallStateMachine.this.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                super.enter();
                boolean unused = EmergencyCallStateMachine.this.mHasEstablished = true;
            }
        }

        /* access modifiers changed from: package-private */
        public void handleE911Fail() {
            IRegistrationGovernor governor;
            Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] handleE911Fail()");
            IMSLog.c(LogClass.VOLTE_E911_CALL_TIMER_ERROR, "" + this.this$0.mPhoneId);
            this.mThisSm.sendMessage(4, 0, EVENT_EMERGENCY_REGISTERED, new SipError(Id.REQUEST_VSH_STOP_SESSION, "Tlte_911fail"));
            if (this.mRegistration != null && (governor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle())) != null) {
                String service = "mmtel";
                if (ImsCallUtil.isVideoCall(this.this$0.mCallProfile.getCallType())) {
                    service = "mmtel-video";
                }
                governor.onSipError(service, new SipError(2507));
            }
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x005a, code lost:
            if (r0 != 307) goto L_0x008b;
         */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0071 A[RETURN] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void unhandledMessage(android.os.Message r4) {
            /*
                r3 = this;
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "[ANY_STATE] unhandledMessage "
                r0.append(r1)
                int r1 = r4.what
                r0.append(r1)
                java.lang.String r0 = r0.toString()
                java.lang.String r1 = "ImsEmergencySession"
                android.util.Log.i(r1, r0)
                com.sec.internal.constants.Mno r0 = r3.mMno
                com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.DEFAULT
                if (r0 != r1) goto L_0x0041
                com.sec.internal.interfaces.ims.core.IRegistrationManager r0 = r3.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession r1 = r3.this$0
                int r1 = r1.mPhoneId
                com.sec.ims.settings.ImsProfile$PROFILE_TYPE r2 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.EMERGENCY
                com.sec.ims.settings.ImsProfile r0 = r0.getImsProfile(r1, r2)
                if (r0 == 0) goto L_0x0037
                java.lang.String r1 = r0.getMnoName()
                com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.fromName(r1)
                r3.mMno = r1
                goto L_0x0041
            L_0x0037:
                com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession r1 = r3.this$0
                int r1 = r1.mPhoneId
                com.sec.internal.constants.Mno r1 = com.sec.internal.helper.SimUtil.getSimMno(r1)
                r3.mMno = r1
            L_0x0041:
                int r0 = r4.what
                r1 = 1
                if (r0 == r1) goto L_0x0087
                r1 = 3
                if (r0 == r1) goto L_0x0080
                r1 = 4
                if (r0 == r1) goto L_0x0079
                r1 = 303(0x12f, float:4.25E-43)
                if (r0 == r1) goto L_0x005d
                r1 = 304(0x130, float:4.26E-43)
                if (r0 == r1) goto L_0x0072
                r1 = 306(0x132, float:4.29E-43)
                if (r0 == r1) goto L_0x006b
                r1 = 307(0x133, float:4.3E-43)
                if (r0 == r1) goto L_0x0064
                goto L_0x008b
            L_0x005d:
                boolean r0 = r3.onLte911Fail()
                if (r0 == 0) goto L_0x0064
                return
            L_0x0064:
                boolean r0 = r3.onE911InviteTill180TimerFail()
                if (r0 == 0) goto L_0x006b
                return
            L_0x006b:
                boolean r0 = r3.onEpdnSetupFail()
                if (r0 == 0) goto L_0x0072
                return
            L_0x0072:
                r3.onLte911FailAfterDelay()
                r3.handleE911Fail()
                return
            L_0x0079:
                boolean r0 = r3.onError(r4)
                if (r0 == 0) goto L_0x008b
                return
            L_0x0080:
                boolean r0 = r3.onEnded(r4)
                if (r0 == 0) goto L_0x008b
                return
            L_0x0087:
                r3.terminate()
            L_0x008b:
                super.unhandledMessage(r4)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession.EmergencyCallStateMachine.unhandledMessage(android.os.Message):void");
        }

        private boolean onLte911Fail() {
            if (this.mMno == Mno.TMOUS || this.mMno.isTmobile()) {
                Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 E1 timer expired");
                this.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
                if (ImsCallUtil.isRttEmergencyCall(this.this$0.mCallProfile.getCallType())) {
                    Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 RTT end call");
                } else {
                    boolean emergencyPdnConnected = this.mRegistrationManager.isPdnConnected(this.mRegistrationManager.getImsProfile(this.this$0.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY), this.this$0.mPhoneId);
                    Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 emergencyPdnConnected =" + emergencyPdnConnected);
                    if (emergencyPdnConnected) {
                        Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 anonymous INVITE to same p-cscf");
                        IUserAgent ua = this.this$0.getEmergencyUa();
                        if (ua != null) {
                            ua.notifyE911RegistrationFailed();
                        }
                        this.mThisEsm.sendMessage(14);
                        return true;
                    } else if (this.mMno.isTmobile()) {
                        Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_EUR epdn not connected, do CSFB");
                        return false;
                    } else if (!this.mModule.isRoaming(this.this$0.mPhoneId) && this.mModule.isRegisteredOverLteOrNr(this.this$0.mPhoneId)) {
                        Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 stopEmergencyRegistration and redial to IMS PDN");
                        this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                        if (this.mModule.triggerPsRedial(this.this$0.mPhoneId, this.this$0.getCallId(), 11)) {
                            Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 redial to IMS PDN success");
                            quit();
                            return true;
                        }
                    }
                }
                Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 CSFB");
            }
            return false;
        }

        private boolean onE911InviteTill180TimerFail() {
            Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] Tlte_911fail expired");
            if (!this.mTryingReceived) {
                return false;
            }
            this.mThisEsm.sendMessage(1, 17);
            this.mThisEsm.sendMessageDelayed((int) CallStateMachine.ON_LTE_911_FAIL_AFTER_DELAY, 500);
            return true;
        }

        private boolean onEpdnSetupFail() {
            if (this.mMno != Mno.TMOUS) {
                return false;
            }
            Log.e(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 EPDN setup fail before E1 expire, stop E1 Timer");
            this.mThisEsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
            if (ImsCallUtil.isRttEmergencyCall(this.this$0.mCallProfile.getCallType())) {
                Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 RTT, mRequstedStopPDN=" + this.mRequstedStopPDN);
                if (this.mRequstedStopPDN) {
                    this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                }
            } else if (!this.mModule.isRoaming(this.this$0.mPhoneId) && this.mModule.isRegisteredOverLteOrNr(this.this$0.mPhoneId)) {
                Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 stopEmergencyRegistration and redial to IMS PDN");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                if (this.mModule.triggerPsRedial(this.this$0.mPhoneId, this.this$0.getCallId(), 11)) {
                    Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 stopEmergencyRegistration");
                    this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                    quit();
                    return true;
                }
            }
            Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 CSFB");
            handleE911Fail();
            return true;
        }

        private void onLte911FailAfterDelay() {
            if (this.mModule.isEmergencyRegistered(this.this$0.mPhoneId)) {
                return;
            }
            if (this.mMno == Mno.ATT || this.mMno == Mno.EE) {
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
            }
        }

        private boolean onError(Message msg) {
            int errCode = ((SipError) msg.obj).getCode();
            if (this.mMno == Mno.VZW && errCode >= 300 && errCode < 700) {
                handleE911Fail();
                return true;
            } else if (this.mMno.isTmobile() && !this.mRequstedStopPDN && errCode == 403) {
                Log.i(ImsEmergencySession.LOG_TAG, "Disconnect Emergency PDN TMOBILE 403 error");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(msg), 500);
                return true;
            } else if (this.mMno.isKor() && !this.mRequstedStopPDN && (errCode == 380 || (errCode >= 400 && errCode < 500))) {
                Log.i(ImsEmergencySession.LOG_TAG, "Disconnect Emergency PDN.");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(msg), 500);
                return true;
            } else if ("TEL".equals(OmcCode.get()) && !this.mRequstedStopPDN && errCode >= 400 && errCode < 600) {
                Log.i(ImsEmergencySession.LOG_TAG, "Disconnect Emergency PDN Telstra 4XX, 5XX error");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(msg), 500);
                return true;
            } else if (this.mMno.isCanada() && !this.mRequstedStopPDN) {
                Log.i(ImsEmergencySession.LOG_TAG, "Disconnect Emergency PDN");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mThisEsm.sendMessageDelayed(Message.obtain(msg), 500);
                this.mRequstedStopPDN = true;
                return true;
            } else if (this.mMno == Mno.TMOUS) {
                return onErrorForTmo(msg, errCode);
            } else {
                return false;
            }
        }

        private boolean onErrorForTmo(Message msg, int errCode) {
            Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 errCode=" + errCode + ", mRequstedStopPDN=" + this.mRequstedStopPDN + ", E2 = " + this.mThisEsm.hasMessages(208) + ", E3 = " + this.mThisEsm.hasMessages(307));
            if (this.this$0.mCallProfile == null || !ImsCallUtil.isRttEmergencyCall(this.this$0.mCallProfile.getCallType())) {
                if (errCode >= 300 && errCode < 400) {
                    Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 stop E2, E3 timer and CSFB");
                    this.mThisEsm.removeMessages(208);
                    this.mThisEsm.removeMessages(307);
                    return false;
                } else if (errCode < 400 || errCode >= 700) {
                    return false;
                } else {
                    if (this.mThisEsm.hasMessages(208)) {
                        ImsEmergencySession imsEmergencySession = this.this$0;
                        if (imsEmergencySession.isNoNextPcscf(imsEmergencySession.mPhoneId)) {
                            Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 stop E2, E3 timer and CSFB");
                            this.mThisEsm.removeMessages(208);
                            this.mThisEsm.removeMessages(307);
                            return false;
                        }
                        Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 stop E2 timer and redial to next p-cscf");
                        this.mThisEsm.removeMessages(208);
                        this.mRegistrationManager.moveNextPcscf(this.this$0.mPhoneId, obtainMessage(402));
                        this.mNextPcscfChangedWorking = true;
                        transitionTo(this.mReadyToCall);
                        return true;
                    } else if (!this.mThisEsm.hasMessages(307)) {
                        return false;
                    } else {
                        Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 stop E3 timer and CSFB");
                        this.mThisEsm.removeMessages(307);
                        return false;
                    }
                }
            } else if (this.mRequstedStopPDN) {
                return false;
            } else {
                Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] TMO_E911 RTT, stopEmergencyRegistration");
                this.mThisEsm.removeMessages(208);
                this.mThisEsm.removeMessages(307);
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.errorCode = 2414;
                this.mThisEsm.sendMessageDelayed(Message.obtain(msg), 5000);
                this.mRequstedStopPDN = true;
                return true;
            }
        }

        /* access modifiers changed from: private */
        public boolean onEnded(Message msg) {
            String ErrorMessage = (String) msg.obj;
            Log.i(ImsEmergencySession.LOG_TAG, "[ANY_STATE] ErrorMessage " + ErrorMessage);
            if ((this.mMno == Mno.TMOUS || this.mMno.isKor() || this.mMno == Mno.ORANGE || this.mMno == Mno.TELSTRA || "TEL".equals(OmcCode.get()) || this.mMno == Mno.TWO_DEGREE || this.mMno.isCanada()) && !this.mRequstedStopPDN) {
                Log.i(ImsEmergencySession.LOG_TAG, "Disconnect Emergency PDN.");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(msg), 500);
                return true;
            }
            if ((this.mMno == Mno.EE || this.mMno == Mno.EE_ESN) && !this.mRequstedStopPDN) {
                IPdnController pdnController = ImsRegistry.getPdnController();
                boolean csOutOfService = pdnController.getVoiceRegState(this.this$0.mPhoneId) != 0;
                boolean isEpdgConnected = pdnController.isEpdgConnected(this.this$0.mPhoneId);
                boolean isEcEpdgConnected = pdnController.isEmergencyEpdgConnected(this.this$0.mPhoneId);
                if (csOutOfService && isEpdgConnected && !isEcEpdgConnected) {
                    Log.i(ImsEmergencySession.LOG_TAG, "Disconnect Emergency PDN in LTE - No CS, Only Epdg");
                    this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                    this.mRequstedStopPDN = true;
                    this.mThisEsm.sendMessageDelayed(Message.obtain(msg), 500);
                    return true;
                }
            }
            if (this.mMno == Mno.DOCOMO && this.this$0.getCallState() != CallConstants.STATE.EndingCall && "RTP Timeout".equalsIgnoreCase(ErrorMessage)) {
                Log.i(ImsEmergencySession.LOG_TAG, "Disconnect Emergency PDN for DCM.");
                this.mRegistrationManager.stopEmergencyRegistration(this.this$0.mPhoneId);
                this.mRequstedStopPDN = true;
                this.mThisEsm.sendMessageDelayed(Message.obtain(msg), 500);
            }
            return false;
        }

        private void terminate() {
            if (this.mMno == Mno.TMOUS) {
                Log.i(ImsEmergencySession.LOG_TAG, "reset mNextPcscfChangedWorking to false");
                this.mNextPcscfChangedWorking = false;
            }
        }
    }

    public ImsRegistration getRegistration() {
        return getEmergencyRegistration();
    }

    /* access modifiers changed from: private */
    public ImsRegistration getEmergencyRegistration() {
        if (this.mRegistrationManager == null) {
            return null;
        }
        boolean imsPdnSupport = false;
        ImsProfile emergencyProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (emergencyProfile != null) {
            imsPdnSupport = emergencyProfile.getPdnType() == 11;
        }
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        for (ImsRegistration r : this.mRegistrationManager.getRegistrationInfo()) {
            if (this.mPhoneId == r.getPhoneId() && (r.getImsProfile().hasEmergencySupport() || (imsPdnSupport && r.getImsProfile().getPdnType() == 11 && (mno == Mno.VODAFONE_UK || mno == Mno.H3G_DK || mno == Mno.TELENOR_DK)))) {
                return r;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public ImsRegistration getIMSRegistration() {
        if (this.mRegistrationManager == null) {
            return null;
        }
        ImsRegistration[] registrationInfo = this.mRegistrationManager.getRegistrationInfo();
        if (registrationInfo.length > 0) {
            return registrationInfo[0];
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public ImsUri getOriginatingUri() {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        Mno mno = sm == null ? Mno.DEFAULT : sm.getSimMno();
        ImsRegistration regInfo = getEmergencyRegistration();
        ImsRegistration IMSRegInfo = getIMSRegistration();
        if (regInfo != null) {
            ImsUri ou = regInfo.getRegisteredImpu();
            if (mno != Mno.ATT) {
                Log.i(LOG_TAG, "getOriginatingUri: emergency call with registration.");
                return regInfo.getPreferredImpu().getUri();
            } else if (regInfo.getImpuList() == null) {
                return ou;
            } else {
                for (NameAddr addr : regInfo.getImpuList()) {
                    if (addr.getUri().getUriType().equals(ImsUri.UriType.TEL_URI)) {
                        Log.i(LOG_TAG, "getOriginatingUri: Found Tel-URI");
                        return addr.getUri();
                    }
                }
                return ou;
            }
        } else if (mno == Mno.VZW) {
            Log.i(LOG_TAG, "getOriginatingUri: emergency call without registration.");
            if (sm == null) {
                return null;
            }
            return ImsUri.parse(sm.getDerivedImpu());
        } else if (!mno.isKor() || IMSRegInfo != null) {
            Log.i(LOG_TAG, "getOriginatingUri: No emergency registration. Use IMEI-based preferred-ID");
            return null;
        } else {
            Log.i(LOG_TAG, "getOriginatingUri: emergency call without SIM");
            return ImsUri.parse("sip:anonymous@anonymous.invalid");
        }
    }

    public ImsEmergencySession(Context context, CallProfile profile, Looper looper, IVolteServiceModuleInternal module) {
        super(context, profile, (ImsRegistration) null, looper, module);
        setPhoneId(profile.getPhoneId());
    }

    public void init(IVolteServiceInterface stackIf, IRegistrationManager rm) {
        this.mVolteSvcIntf = stackIf;
        this.mRegistrationManager = rm;
        this.mRegistrationManager.refreshAuEmergencyProfile(this.mPhoneId);
        if (this.mRegistration != null) {
            this.mMno = Mno.fromName(this.mRegistration.getImsProfile().getMnoName());
        } else {
            ImsProfile emergencyProfile = this.mRegistrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
            if (emergencyProfile != null) {
                this.mMno = Mno.fromName(emergencyProfile.getMnoName());
            }
        }
        EmergencyCallStateMachine emergencyCallStateMachine = r0;
        EmergencyCallStateMachine emergencyCallStateMachine2 = new EmergencyCallStateMachine(this, this.mContext, this, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, "EmergencyCallStateMachine", this.mLooper);
        this.smCallStateMachine = emergencyCallStateMachine;
        this.smCallStateMachine.init();
        this.mMediaController.registerForMediaEvent(this);
        Log.i(LOG_TAG, "start EmergencyCallStateMachine");
        this.smCallStateMachine.start();
        this.mImsCallSessionEventHandler = new ImsCallSessionEventHandler(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine, this.mCallProfile, this.mVolteSvcIntf, this.mMediaController);
        this.mImsCallDedicatedBearer = new ImsCallDedicatedBearer(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine);
        this.mVolteSvcIntf.registerForCallStateEvent(this.mVolteStackEventHandler, 1, (Object) null);
        setIsNrSaMode();
    }

    /* access modifiers changed from: private */
    public IUserAgent getEmergencyUa() {
        IRegistrationManager rm = ImsRegistry.getRegistrationManager();
        if (rm != null) {
            return rm.getUserAgentOnPdn(15, this.mPhoneId);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void setPhoneId(int phoneId) {
        this.mPhoneId = phoneId;
    }

    /* access modifiers changed from: private */
    public boolean isNoNextPcscf(int phoneId) {
        IRegistrationGovernor governor;
        boolean ret = false;
        if (!(this.mRegistrationManager == null || (governor = this.mRegistrationManager.getEmergencyGovernor(this.mPhoneId)) == null)) {
            ret = governor.isNoNextPcscf();
        }
        Log.i(LOG_TAG, "TMO_E911 isNoNextPcscf = " + ret);
        return ret;
    }

    /* access modifiers changed from: private */
    public boolean hasInProgressEmergencyTask() {
        IRegistrationGovernor governor;
        if (this.mRegistrationManager == null || (governor = this.mRegistrationManager.getEmergencyGovernor(this.mPhoneId)) == null) {
            return false;
        }
        RegistrationConstants.RegisterTaskState eRegiState = governor.getState();
        Log.i(LOG_TAG, "emergency Task status : " + eRegiState);
        if (eRegiState == RegistrationConstants.RegisterTaskState.DEREGISTERING) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean needRemoveTimerOn18X() {
        if (!this.mMno.isKor()) {
            if (!this.mMno.isOneOf(Mno.VZW, Mno.TELENOR_NORWAY, Mno.TELIA_NORWAY, Mno.VODAFONE_NETHERLAND, Mno.BELL)) {
                return false;
            }
        }
        Log.e(LOG_TAG, "VZW/KOR/TEL_NO/TEN_NO/VF_NL want E911 Timer removed if 180 / 183 received");
        return true;
    }
}
