package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.Dialog;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.UserConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.volte2.data.CallSetupData;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;

public class ImsReadyToCall extends CallState {
    ImsReadyToCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        this.mCsm.callType = 0;
        this.mCsm.errorCode = -1;
        this.mCsm.errorMessage = "";
        this.mCsm.isLocationAcquiringTriggered = false;
        this.mCsm.mRequestLocation = false;
        this.mCsm.mIsStartCameraSuccess = true;
        this.mSession.setIsEstablished(false);
        this.mCsm.mCallInitTime = SystemClock.elapsedRealtime();
        Log.i("CallStateMachine", "Enter [ReadyToCall]");
    }

    public boolean processMessage(Message msg) {
        int code;
        Log.i("CallStateMachine", "[ReadyToCall] processMessage " + msg.what);
        int i = msg.what;
        if (i != 1) {
            if (i == 21) {
                this.mCsm.transitionTo(this.mCsm.mIncomingCall);
            } else if (i == 31) {
                this.mCsm.removeMessages(208);
            } else if (i == 41) {
                established_ReadyToCall();
            } else if (i == 52) {
                Log.i("CallStateMachine", "[ReadyToCall] Postpone update request till established state");
                this.mCsm.deferMessage(msg);
            } else if (i == 100) {
                return false;
            } else {
                if (i == 208) {
                    tryingTimeout_ReadyToCall();
                } else if (i == 211) {
                    Log.i("CallStateMachine", "[ReadyToCall] registration is not available.");
                    if (this.mMno.isKor()) {
                        code = ImSessionEvent.CONFERENCE_INFO_UPDATED;
                    } else {
                        code = 1601;
                    }
                    this.mCsm.notifyOnError(code, "No registration.", 0);
                    this.mCsm.transitionTo(this.mCsm.mEndingCall);
                    this.mCsm.sendMessage(3);
                } else if (i == 5000) {
                    dbrLost_ReadyToCall(msg);
                } else if (!(i == 3 || i == 4)) {
                    if (i == 500) {
                        locAcqTimeout_ReadyToCall(msg);
                    } else if (i != 501) {
                        switch (i) {
                            case 11:
                                start_ReadyToCall(msg);
                                break;
                            case 12:
                                pulling_ReadyToCall(msg);
                                break;
                            case 13:
                                locAcq_ReadyToCall(msg);
                                break;
                            default:
                                Log.e("CallStateMachine", "[" + getName() + "] msg:" + msg.what + " ignored !!!");
                                break;
                        }
                    } else {
                        locAcqSuccess_ReadyToCall(msg);
                    }
                }
            }
            return true;
        }
        Log.e("CallStateMachine", "[ReadyToCall] Call session got dropped early!");
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v59, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v12, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void start_ReadyToCall(android.os.Message r10) {
        /*
            r9 = this;
            boolean r0 = r9.handleNotREGStatus()
            if (r0 != 0) goto L_0x0007
            return
        L_0x0007:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r9.mCsm
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r9.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r1.mSession
            com.sec.ims.volte2.data.CallProfile r1 = r1.getCallProfile()
            int r1 = r1.getCallType()
            r0.callType = r1
            boolean r0 = r9.handleBreakBeforeToMakeCall(r10)
            if (r0 != 0) goto L_0x001e
            return
        L_0x001e:
            r9.handleAutomaticMode()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            java.lang.String r0 = r0.getDialingNumber()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r1 = r1.getCallProfile()
            java.lang.String r1 = r1.getUrn()
            r2 = 1
            r3 = 0
            java.lang.String r4 = "CallStateMachine"
            if (r1 == 0) goto L_0x004a
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r1 = r1.getCallProfile()
            java.lang.String r1 = r1.getUrn()
            com.sec.ims.util.ImsUri r1 = com.sec.ims.util.ImsUri.parse(r1)
            goto L_0x0097
        L_0x004a:
            r1 = 0
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()
            boolean r5 = r5.isSamsungMdmnCall()
            if (r5 == 0) goto L_0x0071
            java.lang.Object r5 = r10.obj
            r1 = r5
            java.lang.String r1 = (java.lang.String) r1
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "put deviceID as "
            r5.append(r6)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
        L_0x0071:
            boolean r5 = r9.isLGUspecificNumber(r0)
            if (r5 == 0) goto L_0x008c
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            int r6 = r0.length()
            int r6 = r6 - r2
            java.lang.String r6 = r0.substring(r3, r6)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r7 = r9.mCsm
            int r7 = r7.callType
            com.sec.ims.util.ImsUri r5 = r5.buildUri(r6, r1, r7)
            r1 = r5
            goto L_0x0097
        L_0x008c:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r9.mCsm
            int r6 = r6.callType
            com.sec.ims.util.ImsUri r5 = r5.buildUri(r0, r1, r6)
            r1 = r5
        L_0x0097:
            if (r1 != 0) goto L_0x00af
            java.lang.String r2 = "dest Uri couldn't be null!!!!"
            android.util.Log.e(r4, r2)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r2 = r9.mCsm
            r4 = 4
            r5 = -1
            com.sec.ims.util.SipError r6 = new com.sec.ims.util.SipError
            r7 = 1001(0x3e9, float:1.403E-42)
            java.lang.String r8 = "invalid remote uri"
            r6.<init>(r7, r8)
            r2.sendMessage(r4, r3, r5, r6)
            return
        L_0x00af:
            com.sec.internal.ims.servicemodules.volte2.data.CallSetupData r3 = new com.sec.internal.ims.servicemodules.volte2.data.CallSetupData
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r5 = r9.mCsm
            int r5 = r5.callType
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r6 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r6 = r6.getCallProfile()
            java.lang.String r6 = r6.getCLI()
            r3.<init>(r1, r0, r5, r6)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "CLI : "
            r5.append(r6)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r6 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r6 = r6.getCallProfile()
            java.lang.String r6 = r6.getCLI()
            r5.append(r6)
            java.lang.String r6 = " LetteringText : "
            r5.append(r6)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r6 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r6 = r6.getCallProfile()
            java.lang.String r6 = r6.getLetteringText()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r6 = r9.mSession
            int r6 = r6.getCmcType()
            boolean r5 = r5.isCmcPrimaryType(r6)
            if (r5 == 0) goto L_0x012d
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()
            java.lang.String r5 = r5.getCLI()
            java.lang.String r6 = "#31#"
            boolean r5 = r5.equals(r6)
            if (r5 != 0) goto L_0x012d
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()
            java.lang.String r5 = r5.getLetteringText()
            boolean r5 = android.text.TextUtils.isEmpty(r5)
            if (r5 == 0) goto L_0x012d
            java.lang.String r5 = "change cli to unknown"
            android.util.Log.i(r4, r5)
            java.lang.String r5 = "unknown"
            r3.setCli(r5)
        L_0x012d:
            boolean r5 = r9.isLGUspecificNumber(r0)
            if (r5 == 0) goto L_0x013f
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.ImsRegistration r6 = r9.mRegistration
            com.sec.ims.util.ImsUri r5 = r5.getSecondImpu(r6)
            r3.setOriginatingUri(r5)
            goto L_0x0148
        L_0x013f:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.util.ImsUri r5 = r5.getOriginatingUri()
            r3.setOriginatingUri(r5)
        L_0x0148:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()
            java.lang.String r5 = r5.getLetteringText()
            r3.setLetteringText(r5)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()
            java.lang.String r5 = r5.getAlertInfo()
            r3.setAlertInfo(r5)
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r9.mModule
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r6 = r9.mSession
            int r6 = r6.getPhoneId()
            boolean r5 = r5.getLteEpsOnlyAttached(r6)
            r3.setLteEpsOnlyAttached(r5)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()
            java.util.List r5 = r5.getP2p()
            r3.setP2p(r5)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()
            int r5 = r5.getCmcBoundSessionId()
            r3.setCmcBoundSessionId(r5)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()
            android.os.Bundle r5 = r5.getComposerData()
            r3.setComposerData(r5)
            boolean r5 = r9.isATTSoftPhoneCall()
            if (r5 == 0) goto L_0x01c1
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r5 = r9.mCsm
            int r5 = r5.callType
            r6 = 13
            if (r5 != r6) goto L_0x01c1
            com.sec.internal.interfaces.ims.core.IRegistrationManager r5 = r9.mRegistrationManager
            com.sec.ims.ImsRegistration r6 = r9.mRegistration
            com.sec.ims.settings.ImsProfile r6 = r6.getImsProfile()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r7 = r9.mSession
            int r7 = r7.getPhoneId()
            java.lang.String r5 = r5.getImpi(r6, r7)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r6 = r9.mSession
            java.lang.String r6 = r6.getPEmergencyInfoOfAtt(r5)
            r3.setPEmergencyInfoOfAtt(r6)
        L_0x01c1:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            int r5 = r5.getCmcType()
            r6 = 2
            if (r5 != r6) goto L_0x0206
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r5 = r5.getCallProfile()
            java.lang.String r5 = r5.getReplaceSipCallId()
            if (r5 == 0) goto L_0x0206
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "set replace call id "
            r5.append(r6)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r6 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r6 = r6.getCallProfile()
            java.lang.String r6 = r6.getReplaceSipCallId()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r4 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r4 = r4.getCallProfile()
            java.lang.String r4 = r4.getReplaceSipCallId()
            r3.setReplaceCallId(r4)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r4 = r9.mCsm
            r4.mIsCmcHandover = r2
        L_0x0206:
            boolean r2 = r9.handleCallBarring()
            if (r2 != 0) goto L_0x020d
            return
        L_0x020d:
            boolean r2 = r9.handleSessionId(r3)
            if (r2 != 0) goto L_0x0214
            return
        L_0x0214:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r2 = r9.mCsm
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r4 = r9.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsOutgoingCall r4 = r4.mOutgoingCall
            r2.transitionTo(r4)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsReadyToCall.start_ReadyToCall(android.os.Message):void");
    }

    private void pulling_ReadyToCall(Message msg) {
        String str;
        String targetUri;
        String targetUri2;
        Bundle bundle = (Bundle) msg.obj;
        String msisdn = bundle.getString("msisdn");
        Dialog targetDialog = bundle.getParcelable("targetDialog");
        String str2 = null;
        String targetUri3 = this.mSession.buildUri(msisdn, (String) null, targetDialog.getCallType()).encode();
        if (this.mRegistration == null) {
            str = "CallStateMachine";
        } else if (this.mModule.isProhibited(this.mSession.getPhoneId())) {
            str = "CallStateMachine";
        } else {
            if (isATTSoftPhoneCall()) {
                String targetUri4 = this.mRegistration.getImsProfile().getPullingServerUri();
                if (targetDialog.getLocalUri().contains("gr=") || !TextUtils.isEmpty(targetDialog.getLocalDispName())) {
                    targetUri2 = targetUri4.replace("[CALLTYPE]", "SP");
                } else {
                    targetUri2 = targetUri4.replace("[CALLTYPE]", NSDSNamespaces.NSDSRequestServices.REQ_SERVICE_VOLTE);
                }
                if (targetDialog.isHeld()) {
                    targetUri = targetUri2.replace("[CALLSTATE]", "hold");
                } else {
                    targetUri = targetUri2.replace("[CALLSTATE]", SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE);
                }
            } else if (this.mMno != Mno.VZW || this.mRegistration.getPreferredImpu() == null) {
                targetUri = targetUri3;
            } else {
                targetUri = this.mRegistration.getPreferredImpu().toString();
            }
            Log.i("CallStateMachine", "[ReadyToCall] pullingCall targetUri " + IMSLog.checker(targetUri));
            ImsUri origUri = this.mSession.getOriginatingUri();
            if (this.mSession.getCmcType() > 0) {
                targetDialog.setMdmnExtNumber(targetDialog.getSessionDescription());
            }
            if (!this.mSession.isCmcPrimaryType(this.mSession.getCmcType())) {
                Log.i("CallStateMachine", "bindToNetwork for Pulling");
                this.mSession.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
            }
            IVolteServiceInterface iVolteServiceInterface = this.mVolteSvcIntf;
            int handle = this.mRegistration.getHandle();
            if (origUri != null) {
                str2 = origUri.toString();
            }
            String str3 = "CallStateMachine";
            int sessionId = iVolteServiceInterface.pullingCall(handle, targetUri, msisdn, str2, targetDialog, this.mSession.getCallProfile().getP2p());
            Log.i(str3, "[ReadyToCall] pullingCall() returned session id " + sessionId);
            if (sessionId < 0) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(1001, "stack return -1"));
                return;
            }
            this.mSession.setSessionId(sessionId);
            int cameraId = this.mCsm.determineCamera(targetDialog.getCallType(), false);
            if (cameraId >= 0) {
                Log.i(str3, "[ReadyToCall] pullingCall is VT call");
                this.mSession.startCamera(cameraId);
            }
            int callDirection = 2;
            String remoteNumber = targetDialog.getRemoteNumber();
            if (targetDialog.getDirection() == 1) {
                callDirection = 3;
                if (this.mMno == Mno.VZW) {
                    remoteNumber = ImsCallUtil.getRemoteCallerId(new NameAddr("", targetDialog.getRemoteUri()), this.mMno, Debug.isProductShip());
                    if (!ImsRegistry.getPdnController().isVoiceRoaming(this.mSession.getPhoneId())) {
                        remoteNumber = ImsCallUtil.removeUriPlusPrefix(remoteNumber, Debug.isProductShip());
                    }
                } else {
                    remoteNumber = ImsCallUtil.getRemoteCallerId(new NameAddr(targetDialog.getRemoteDispName(), targetDialog.getRemoteUri()), this.mMno, Debug.isProductShip());
                }
            }
            Log.i(str3, "remoteNumber : " + IMSLog.checker(remoteNumber));
            this.mSession.getCallProfile().setDowngradedVideoCall(targetDialog.isVideoPortZero());
            this.mSession.getCallProfile().setDirection(callDirection);
            this.mSession.getCallProfile().setDialogId(targetDialog.getDialogId());
            handleCMCTransferCall(targetDialog, remoteNumber);
            Log.i(str3, "DialingNumber : " + IMSLog.checker(this.mSession.getCallProfile().getDialingNumber()));
            if (this.mSession.isCmcSecondaryType(this.mSession.getCmcType())) {
                startCMC100Timer_ReadyToCall();
                return;
            }
            return;
        }
        Log.e(str, "pulling: registration is not available.");
        this.mCsm.sendMessage(4, 0, -1, new SipError(1001, "No registration."));
    }

    private void established_ReadyToCall() {
        IRegistrationGovernor regGov;
        int i;
        if (this.mSession.getCallProfile().isPullCall()) {
            Log.i("CallStateMachine", "Pulling Call Establish");
            if (!(this.mRegistration == null || (regGov = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle())) == null)) {
                IRegistrationGovernor.CallEvent callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED;
                if (this.mSession.getCallProfile().isDowngradedVideoCall()) {
                    i = 2;
                } else {
                    i = this.mSession.getCallProfile().getCallType();
                }
                regGov.onCallStatus(callEvent, (SipError) null, i);
            }
            this.mCsm.transitionTo(this.mCsm.mInCall);
        }
    }

    private void dbrLost_ReadyToCall(Message msg) {
        if (this.mSession.getCallProfile().getDirection() == 1) {
            this.mCsm.callType = this.mSession.getCallProfile().getCallType();
            if (msg.arg1 != 1) {
                return;
            }
            if (this.mVolteSvcIntf.rejectCall(this.mSession.getSessionId(), this.mCsm.callType, SipErrorBase.PRECONDITION_FAILURE) < 0) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(Id.REQUEST_SIP_DIALOG_OPEN, ""));
                return;
            }
            this.mCsm.notifyOnEnded(Id.REQUEST_SIP_DIALOG_OPEN);
            this.mCsm.transitionTo(this.mCsm.mEndingCall);
        }
    }

    private void locAcq_ReadyToCall(Message msg) {
        Log.i("CallStateMachine", "[ReadyToCall] Enable Location Provider & Request Location Acquiring");
        IGeolocationController geolocationCon = ImsRegistry.getGeolocationController();
        if (geolocationCon != null) {
            this.mCsm.mRequestLocation = geolocationCon.startGeolocationUpdate(this.mSession.getPhoneId(), true);
            int locationAcquireTime = ImsRegistry.getInt(this.mSession.getPhoneId(), GlobalSettingsConstants.Call.T_LOCATION_ACQUIRE_FAIL, 0);
            IMSLog.c(LogClass.VOLTE_GET_GEOLOCATION, this.mSession.getPhoneId() + "," + this.mSession.getSessionId() + "," + (this.mCsm.mRequestLocation ? 1 : 0) + "," + locationAcquireTime);
            if (this.mCsm.mRequestLocation) {
                this.mCsm.sendMessageDelayed(500, (long) locationAcquireTime);
                this.mCsm.isLocationAcquiringTriggered = true;
            } else if (this.mRegistration == null || !this.mRegistration.getImsProfile().isSoftphoneEnabled()) {
                this.mCsm.sendMessage(14);
            } else {
                this.mCsm.sendMessage(11);
            }
        }
    }

    private void locAcqSuccess_ReadyToCall(Message msg) {
        if (this.mCsm.isLocationAcquiringTriggered) {
            Log.i("CallStateMachine", "[ReadyToCall] Location Acquiring Success -> Start Call");
            if (ImsRegistry.getGeolocationController() != null) {
                this.mCsm.removeMessages(500);
                this.mCsm.isLocationAcquiringTriggered = false;
            }
            IMSLog.c(LogClass.VOLTE_GEOLOCATION_SUCCESS, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
            if (this.mRegistration == null || !this.mRegistration.getImsProfile().isSoftphoneEnabled()) {
                this.mCsm.sendMessage(14);
            } else {
                this.mCsm.sendMessage(11);
            }
        }
    }

    private void locAcqTimeout_ReadyToCall(Message msg) {
        if (this.mCsm.isLocationAcquiringTriggered) {
            Log.i("CallStateMachine", "[ReadyToCall] Location Acquiring Timeout & Get Last known Location -> Start Call");
            this.mCsm.isLocationAcquiringTriggered = false;
            IGeolocationController geolocationCon = ImsRegistry.getGeolocationController();
            if (geolocationCon != null) {
                geolocationCon.updateGeolocationFromLastKnown(this.mSession.getPhoneId());
            }
            IMSLog.c(LogClass.VOLTE_GEOLOCATION_FAIL, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
            if (this.mRegistration == null || !this.mRegistration.getImsProfile().isSoftphoneEnabled()) {
                this.mCsm.sendMessage(14);
            } else {
                this.mCsm.sendMessage(11);
            }
        }
    }

    private void startCMC100Timer_ReadyToCall() {
        Log.i("CallStateMachine", "[ReadyToCall] Start 100 Trying Timer (5000 msec) for pulling.");
        this.mCsm.sendMessageDelayed(208, 5000);
    }

    private void tryingTimeout_ReadyToCall() {
        Log.i("CallStateMachine", "[ReadyToCall] 100 Trying Timeout - Call Terminate");
        this.mCsm.notifyOnError(503, "100 Trying Timeout", 0);
        this.mCsm.sendMessage(1, 17);
    }

    private void handleCMCTransferCall(Dialog targetDialog, String remoteNumber) {
        if (this.mSession.getCmcType() > 0) {
            Log.i("CallStateMachine", "Update DialingNumber from sessionDescription for CMC call pull");
            this.mSession.getCallProfile().setDialingNumber(targetDialog.getSessionDescription());
            return;
        }
        this.mSession.getCallProfile().setDialingNumber(remoteNumber);
    }

    private void handleAutomaticMode() {
        if (this.mModule.getAutomaticMode(this.mSession.getPhoneId())) {
            if (this.mCsm.callType == 1) {
                this.mCsm.callType = 14;
            } else if (this.mCsm.callType == 2) {
                if (!(this.mMno == Mno.TMOUS || this.mMno == Mno.VZW || this.mMno == Mno.ATT)) {
                    this.mCsm.callType = 15;
                }
            } else if (this.mCsm.callType == 7) {
                this.mCsm.callType = 18;
            } else if (this.mCsm.callType == 8) {
                this.mCsm.callType = 19;
            } else if (this.mCsm.callType == 5) {
                this.mCsm.callType = 16;
            } else if (this.mCsm.callType == 6) {
                this.mCsm.callType = 17;
            }
            this.mSession.getCallProfile().setCallType(this.mCsm.callType);
            if (ImsCallUtil.isRttCall(this.mCsm.callType) && !ImsRegistry.getPdnController().isEpdgConnected(this.mSession.getPhoneId())) {
                this.mSession.startRttDedicatedBearerTimer(this.mModule.getRttDbrTimer(this.mSession.getPhoneId()));
            }
        }
    }

    private boolean handleBreakBeforeToMakeCall(Message msg) {
        if (this.mSession.isCmcSecondaryType(this.mSession.getCmcType()) && this.mCsm.callType == 2) {
            Log.e("CallStateMachine", "start: SD is not supported VT.");
            this.mSession.getCallProfile().setDirection(0);
            this.mCsm.sendMessage(4, 0, -1, new SipError(AECNamespace.HttpResponseCode.UNSUPPORTED_MEDIA_TYPE, "SD_NOT_SUPPORTED_VT"));
            return false;
        } else if (this.mModule.isCallBarredBySSAC(this.mSession.getPhoneId(), this.mCsm.callType)) {
            Log.e("CallStateMachine", "start: call barred by ssac.");
            if (this.mMno == Mno.KDDI) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(2699, "Call Barred due to SSAC"));
            } else {
                this.mCsm.sendMessage(4, 0, -1, new SipError(1116, "Call Barred due to SSAC"));
            }
            return false;
        } else if (this.mMno == Mno.ATT && this.mCsm.callType == 12 && this.mRegistration.getCurrentRat() == 13 && ImsRegistry.getPdnController().getVopsIndication(this.mSession.getPhoneId()) == VoPsIndication.NOT_SUPPORTED) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(403, "VoPS is not supported"));
            return false;
        } else if (this.mRegistrationManager.isSuspended(this.mRegistration.getHandle())) {
            Log.e("CallStateMachine", "cannot make new call session. currently in suspend");
            this.mCsm.sendMessage(4, 0, -1, new SipError(2511, "Suspended."));
            return false;
        } else if (this.mRegistrationManager.isSuspended(this.mRegistration.getHandle())) {
            if (!this.mSession.mHandOffTimedOut) {
                this.mSession.mHandOffTimedOut = true;
                Log.e("CallStateMachine", "Wait 1.5 sec for the SUSPEND state to change");
                this.mCsm.sendMessageDelayed(Message.obtain(msg), 1500);
            } else {
                Log.e("CallStateMachine", "cannot make new call session. currently in suspend");
                this.mCsm.sendMessage(4, 0, -1, new SipError(2511, "Suspended."));
            }
            return false;
        } else {
            if (ImsCallUtil.isTtyCall(this.mCsm.callType) && (this.mRegistration.getImsProfile().getTtyType() == 1 || this.mRegistration.getImsProfile().getTtyType() == 3)) {
                Log.i("CallStateMachine", "CS TTY Enable so do not allow outgoing IMS TTY call");
                this.mCsm.callType = 1;
            }
            if (ImsCallUtil.isVideoCall(this.mCsm.callType) && !this.mModule.isCallServiceAvailable(this.mSession.getPhoneId(), "mmtel-video") && this.mCsm.callType != 8) {
                Log.i("CallStateMachine", "Call Type change Video to Voice for no video feature tag");
                this.mCsm.callType = 1;
            }
            return true;
        }
    }

    private boolean isLGUspecificNumber(String dialingNumber) {
        if (this.mMno != Mno.LGU || dialingNumber.length() <= 1 || !dialingNumber.endsWith("#")) {
            return false;
        }
        return true;
    }

    private boolean handleSessionId(CallSetupData data) {
        ImsCallSession boundSession;
        int sessionId = -1;
        if (this.mRegistration != null) {
            if (!this.mSession.isCmcPrimaryType(this.mSession.getCmcType())) {
                Log.i("CallStateMachine", "bindToNetwork for MO");
                this.mSession.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
            }
            if (!this.mSession.isCmcPrimaryType(this.mSession.getCmcType()) || (this.mModule.getCmcServiceHelper().getSessionByCmcTypeAndState(1, CallConstants.STATE.InCall) == null && this.mModule.getCmcServiceHelper().getSessionByCmcTypeAndState(3, CallConstants.STATE.InCall) == null && this.mModule.getCmcServiceHelper().getSessionByCmcTypeAndState(7, CallConstants.STATE.InCall) == null && this.mModule.getCmcServiceHelper().getSessionByCmcTypeAndState(5, CallConstants.STATE.InCall) == null)) {
                sessionId = this.mVolteSvcIntf.makeCall(this.mRegistration.getHandle(), data, this.mSession.getCallProfile().getAdditionalSipHeaders(), this.mSession.getPhoneId());
            } else {
                HashMap<String, String> additionalSipHeaders = new HashMap<>();
                additionalSipHeaders.put("Pull-State", "disabled");
                sessionId = this.mVolteSvcIntf.makeCall(this.mRegistration.getHandle(), data, additionalSipHeaders, this.mSession.getPhoneId());
            }
        }
        Log.i("CallStateMachine", "[ReadyToCall] makeCall() returned session id " + sessionId + ", p2p = " + this.mSession.getCallProfile().getP2p());
        if (sessionId < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, "stack return -1"));
            return false;
        }
        int boundSessionId = this.mSession.getCallProfile().getCmcBoundSessionId();
        if (boundSessionId > 0 && (boundSession = this.mModule.getSession(boundSessionId)) != null) {
            boundSession.getCallProfile().setCmcBoundSessionId(sessionId);
            Log.i("CallStateMachine", "[ReadyToCall] updated boundSessionId : " + boundSession.getCallProfile().getCmcBoundSessionId());
        }
        this.mSession.setSessionId(sessionId);
        this.mSession.getCallProfile().setDirection(0);
        return true;
    }

    private boolean isATTSoftPhoneCall() {
        if (this.mRegistration == null || this.mRegistration.getImsProfile() == null || !this.mRegistration.getImsProfile().isSoftphoneEnabled()) {
            return false;
        }
        return true;
    }

    private boolean handleNotREGStatus() {
        if (this.mRegistration == null || this.mModule.isProhibited(this.mSession.getPhoneId())) {
            if (this.mCsm.mIsPendingCall) {
                int timeout = 10000;
                if (this.mMno == Mno.VZW) {
                    if (isCsfbRequired(this.mSession.getPhoneId())) {
                        timeout = 0;
                    } else {
                        timeout = getTimsTimerVzw();
                    }
                }
                if (ImsCallUtil.isE911Call(this.mSession.getCallProfile().getCallType())) {
                    long delay = ((long) ImsRegistry.getInt(this.mSession.getPhoneId(), GlobalSettingsConstants.Call.T_LTE_911_FAIL, 10)) * 1000;
                    Log.i("CallStateMachine", "[ReadyToCall] start Tlte or TWlan-911fail" + delay + " millis.");
                    IMSLog.c(LogClass.VOLTE_E911_CALL_TIMER_START, this.mSession.getPhoneId() + "," + delay);
                    this.mCsm.sendMessageDelayed((int) CallStateMachine.ON_LTE_911_FAIL, delay);
                } else {
                    Log.i("CallStateMachine", "IMS is not registered. Wait to " + timeout + "msec");
                    this.mCsm.sendMessageDelayed(211, (long) timeout);
                }
            } else if (this.mSession.getCallProfile().isForceCSFB()) {
                Log.e("CallStateMachine", "start: Volte not registered. ForceCSFB");
                this.mCsm.sendMessage(4, 0, -1, new SipError(6010, "VOLTE_NOT_REGISTERED"));
            } else if (this.mSession.isCmcSecondaryType(this.mSession.getCmcType())) {
                Log.e("CallStateMachine", "start: SD registration is not available.");
                this.mCsm.sendMessage(4, 0, -1, new SipError(404, "SD_NOT_REGISTERED"));
            } else {
                Log.e("CallStateMachine", "start: registration is not available.");
                this.mCsm.sendMessage(4, 0, -1, new SipError(1001, "No registration."));
            }
            return false;
        } else if (!this.mCsm.hasMessages(211)) {
            return true;
        } else {
            this.mCsm.removeMessages(211);
            return true;
        }
    }

    private boolean isCsfbRequired(int phoneId) {
        ImsProfile profile = this.mRegistrationManager.getImsProfile(phoneId, ImsProfile.PROFILE_TYPE.VOLTE);
        if (profile == null) {
            Log.e("CallStateMachine", "isCsfbRequired : profile null");
            return true;
        }
        IRegistrationGovernor gov = this.mRegistrationManager.getRegistrationGovernorByProfileId(profile.getId(), phoneId);
        if (gov == null) {
            Log.e("CallStateMachine", "isCsfbRequired : RegiGov null");
            return true;
        }
        IPdnController pc = ImsRegistry.getPdnController();
        if (pc == null) {
            Log.e("CallStateMachine", "isCsfbRequired : PdnController null");
            return true;
        }
        VoPsIndication vops = pc.getVopsIndication(phoneId);
        IRegistrationGovernor.ThrottleState throttleState = gov.getThrottleState();
        boolean isSelfActivationReq = this.mRegistrationManager.isSelfActivationRequired(phoneId);
        int lastErrCode = this.mModule.getLastRegiErrorCode(phoneId);
        Log.e("CallStateMachine", "isCsfbRequired : VoPS[" + vops + "], ThrottleState[" + throttleState + "], PCO 5 [" + isSelfActivationReq + "], Last error [" + lastErrCode + "]");
        if (vops == VoPsIndication.SUPPORTED && throttleState != IRegistrationGovernor.ThrottleState.PERMANENTLY_STOPPED && (isSelfActivationReq || lastErrCode == SipErrorBase.FORBIDDEN.getCode() || lastErrCode == SipErrorBase.NOT_FOUND.getCode())) {
            Log.e("CallStateMachine", "isCsfbRequired : Wait for registration");
            return false;
        }
        Log.e("CallStateMachine", "isCsfbRequired : CSFB");
        return true;
    }

    private int getTimsTimerVzw() {
        return DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_VZW_TIMS_TIMER, 120).intValue() * 1000;
    }

    private boolean handleCallBarring() {
        int setValue;
        if (!this.mModule.isCallBarringByNetwork(this.mSession.getPhoneId())) {
            Log.i("CallStateMachine", "checkRejectOutgoingCall: Call barring");
            if (this.mCsm.callType == 2) {
                setValue = UserConfiguration.getUserConfig(this.mContext, this.mSession.getPhoneId(), "ss_video_cb_pref", 0);
            } else {
                setValue = UserConfiguration.getUserConfig(this.mContext, this.mSession.getPhoneId(), "ss_volte_cb_pref", 0);
            }
            if ((setValue & 1) == 1) {
                Log.i("CallStateMachine", "checkRejectOutgoingCall: Outgoing call is barried");
                this.mCsm.sendMessage(4, 0, -1, new SipError(AECNamespace.HttpResponseCode.METHOD_NOT_ALLOWED, "Call is Barred by terminal"));
                return false;
            }
        }
        return true;
    }

    public void exit() {
        this.mCsm.removeMessages(208);
        this.mCsm.setPreviousState(this);
        this.mCsm.isLocationAcquiringTriggered = false;
        this.mCsm.mPreAlerting = false;
    }
}
