package com.sec.internal.google.cmc;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.google.DataTypeConvertor;
import com.sec.internal.google.GoogleImsService;
import com.sec.internal.google.ImsCallSessionImpl;
import com.sec.internal.google.ImsVideoCallProviderImpl;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;

public class CmcImsCallSessionImpl extends ImsCallSessionImpl {
    private final String LOG_TAG = "CmcImsCallSessionImpl";
    private CmcCallSessionManager mP2pCSM = null;

    public CmcImsCallSessionImpl(ImsCallProfile profile, CmcCallSessionManager manager, IImsCallSessionListener listener, GoogleImsService googleImsService) {
        super(profile, (IImsCallSession) null, listener, googleImsService);
        this.mP2pCSM = manager;
        if (manager.getMainSession() == null) {
            Log.e("CmcImsCallSessionImpl", "mainSession is null");
            this.mImsVideoCallProvider = null;
            this.mCallId = "1";
            this.mCallIdInt = 1;
            return;
        }
        initP2pImpl();
    }

    public void initP2pImpl() {
        try {
            Log.d("CmcImsCallSessionImpl", "initP2pImpl()");
            this.mSession = this.mP2pCSM.getMainSession();
            this.mP2pCSM.registerSessionEventListener(this.mVolteEventListener);
            this.mVolteServiceModule.registerRttEventListener(this.mP2pCSM.getPhoneId(), this.mRttEventListener);
            this.mCallIdInt = this.mP2pCSM.getCallId();
            if (this.mCallIdInt > 0) {
                this.mCallId = Integer.toString(this.mCallIdInt);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mImsVideoCallProvider = new ImsVideoCallProviderImpl(this.mP2pCSM.getMainSession());
    }

    public CmcCallSessionManager getCmcCallSessionManager() {
        return this.mP2pCSM;
    }

    public String getProperty(String name) {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.getProperty(name);
    }

    public CallConstants.STATE getInternalState() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.getInternalState();
    }

    public CallConstants.STATE getPrevInternalState() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.getPrevInternalState();
    }

    public boolean isInCall() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.isInCall();
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    public void start(String callee, ImsCallProfile profile) throws RemoteException {
        int targetDialogId;
        if (this.mVolteServiceModule != null) {
            this.mState = 1;
            this.mCallProfile = profile;
            setCallProfile(3);
            CallProfile cp = this.mP2pCSM.getCallProfile();
            int phoneId = this.mP2pCSM.getPhoneId();
            cp.setDialingNumber(callee);
            if (isEmergencyCall()) {
                this.mCallProfile.setCallExtra("CallRadioTech", Integer.toString(this.mP2pCSM.getCallProfile().getRadioTech()));
            }
            this.mVolteServiceModule.setAutomaticMode(phoneId, this.mCallProfile.mMediaProfile.isRttCall());
            cp.getMediaProfile().setRttMode(this.mCallProfile.mMediaProfile.getRttMode());
            if (this.mCallProfile.getCallExtraBoolean("CallPull")) {
                Bundle oemExtras = this.mCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
                DialogEvent de = this.mVolteServiceModule.getLastDialogEvent(this.mSession.getPhoneId());
                int pullFailReason = 101;
                if (!(de == null || oemExtras == null)) {
                    int dialogId = oemExtras.getInt("android.telephony.ImsExternalCallTracker.extra.EXTERNAL_CALL_ID");
                    for (Dialog pullTarget : de.getDialogList()) {
                        if (pullTarget != null) {
                            if (SimUtil.getSimMno(this.mSession.getPhoneId()) == Mno.VZW) {
                                targetDialogId = ImsCallUtil.getIdForString(pullTarget.getSipCallId());
                            } else {
                                try {
                                    targetDialogId = Integer.parseInt(pullTarget.getDialogId());
                                } catch (NumberFormatException e) {
                                }
                            }
                            if (dialogId == targetDialogId && !TextUtils.isEmpty(pullTarget.getSipCallId()) && !TextUtils.isEmpty(pullTarget.getSipLocalTag()) && !TextUtils.isEmpty(pullTarget.getSipRemoteTag())) {
                                this.mCallProfile.mCallType = DataTypeConvertor.convertToGoogleCallType(pullTarget.getCallType());
                                cp.setCallType(pullTarget.getCallType());
                                cp.setPullCall(true);
                                try {
                                    this.mSession.pulling(de.getMsisdn(), pullTarget);
                                    return;
                                } catch (RemoteException e2) {
                                    pullFailReason = 1015;
                                }
                            }
                        }
                    }
                }
                if (this.mListener != null) {
                    this.mListener.callSessionInitiatedFailed(new ImsReasonInfo(pullFailReason, 0));
                    return;
                }
                return;
            }
            try {
                if (this.mP2pCSM.start(callee, cp) < 0) {
                    throw new RemoteException("start return -1");
                }
            } catch (RemoteException e3) {
                if (this.mListener != null) {
                    this.mListener.callSessionInitiatedFailed(new ImsReasonInfo(103, 0));
                }
            }
        } else if (this.mListener != null) {
            this.mListener.callSessionInitiatedFailed(new ImsReasonInfo(102, 0));
        }
    }

    public void accept(int callType, ImsStreamMediaProfile profile) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.accept(callType, profile);
    }

    public void reject(int reason) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.reject(reason);
    }

    public void terminate(int reason) throws RemoteException {
        try {
            if (this.mP2pCSM.getCallProfile().getCallType() == 12) {
                this.mP2pCSM.getMainSession().info(3, "1");
            } else if (!this.mP2pCSM.terminate(convertEndReasonFromFW(reason))) {
                this.mListener.callSessionTerminated(new ImsReasonInfo(501, 200));
            }
        } catch (RemoteException e) {
            if (this.mListener != null) {
                this.mListener.callSessionTerminated(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void hold(ImsStreamMediaProfile profile) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.hold(profile);
    }

    public void resume(ImsStreamMediaProfile profile) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.resume(profile);
    }

    public void sendDtmf(char c, Message result) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendDtmf(c, result);
    }

    public void startDtmf(char c) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.startDtmf(c);
    }

    public void stopDtmf() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.stopDtmf();
    }

    public void sendUssd(String ussdMessage) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendUssd(ussdMessage);
    }

    public boolean isMultiparty() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        return super.isMultiparty();
    }

    /* access modifiers changed from: protected */
    public void updateCmcCallExtras(CallProfile cp) throws RemoteException {
        Bundle oemExtras = null;
        if (this.mCallProfile.mCallExtras.containsKey("android.telephony.ims.extra.OEM_EXTRAS") && (oemExtras = this.mCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS")) != null) {
            oemExtras.remove("com.samsung.telephony.extra.CMC_CS_DTMF_KEY");
            this.mCallProfile.mCallExtras.remove("android.telephony.ims.extra.OEM_EXTRAS");
        }
        if (oemExtras == null) {
            oemExtras = new Bundle();
        }
        int cmcType = this.mP2pCSM.getMainSession().getCmcType();
        int sessionId = this.mP2pCSM.getSessionId();
        if (isP2pPrimaryType(cmcType)) {
            cmcType = 1;
        } else if (isCmcSecondaryType(cmcType)) {
            cmcType = 2;
        }
        Log.i("CmcImsCallSessionImpl", "updateCmcCallExtras(), SEM_EXTRA_CMC_TYPE: (" + this.mP2pCSM.getMainSession().getCmcType() + " -> " + cmcType + ")");
        if (oemExtras != null) {
            oemExtras.putInt("com.samsung.telephony.extra.CMC_TYPE", cmcType);
            oemExtras.putInt("com.samsung.telephony.extra.CMC_SESSION_ID", sessionId);
            if (cmcType == 1) {
                oemExtras.putString("com.samsung.telephony.extra.CMC_DIAL_TO", cp.getDialingNumber());
                int dtmfKey = cp.getCmcDtmfKey();
                if (dtmfKey > -1 && dtmfKey < 12) {
                    char keyChar = 0;
                    if (dtmfKey >= 0 && dtmfKey <= 9) {
                        keyChar = (char) (dtmfKey + 48);
                    } else if (dtmfKey == 10) {
                        keyChar = '*';
                    } else if (dtmfKey == 11) {
                        keyChar = '#';
                    }
                    oemExtras.putString("com.samsung.telephony.extra.CMC_CS_DTMF_KEY", Character.toString(keyChar));
                }
            } else if (isCmcSecondaryType(cmcType)) {
                oemExtras.putString("com.samsung.telephony.extra.CMC_PD_CALL_CONNECT_TIME", cp.getCmcCallTime());
            }
            if (!TextUtils.isEmpty(cp.getReplaceSipCallId())) {
                oemExtras.putString("com.samsung.telephony.extra.CMC_DEVICE_ID_BY_SD", cp.getCmcDeviceId());
            } else if (cp.getCmcDeviceId() != null) {
                oemExtras.putString("com.samsung.telephony.extra.CMC_DEVICE_ID", cp.getCmcDeviceId());
            }
        }
        this.mCallProfile.mCallExtras.putBundle("android.telephony.ims.extra.OEM_EXTRAS", oemExtras);
    }

    public void updateCallProfile() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.updateCallProfile();
    }

    public void sendRttMessage(String rttMessage) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendRttMessage(rttMessage);
    }

    public void sendRttModifyRequest(ImsCallProfile to) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendRttModifyRequest(to);
    }

    public void sendRttModifyResponse(boolean response) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.sendRttModifyResponse(response);
    }

    public void transfer(String number, boolean isConfirmationRequired) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.transfer(number, isConfirmationRequired);
    }

    public void consultativeTransfer(com.android.ims.internal.IImsCallSession mImsCallSession) throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.consultativeTransfer(mImsCallSession);
    }

    public void cancelTransferCall() throws RemoteException {
        this.mSession = this.mP2pCSM.getMainSession();
        super.cancelTransferCall();
    }
}
