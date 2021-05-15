package com.sec.internal.google;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.internal.IImsVideoCallCallback;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import java.io.UnsupportedEncodingException;

public class ImsCallSessionEventListener extends IImsCallSessionEventListener.Stub {
    private static final int EVENT_CALL_RETRY = 100;
    private static final int EVENT_RETRY_AFTER_TIMEOUT = 101;
    private static int USSD_MODE_NW_ERROR = -1;
    private static int mEventCallRetryCounter = 0;
    private static int mEventCallRetryTotalTimer = 0;
    private final String LOG_TAG = "ImsCallSessionEventListener";
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    ImsCallSessionImpl mIcsi;

    ImsCallSessionEventListener(ImsCallSessionImpl icsi) {
        this.mIcsi = icsi;
    }

    public void onCalling() {
        try {
            if (this.mIcsi.mSession != null && this.mIcsi.isCmcPrimaryType(this.mIcsi.mSession.getCmcType())) {
                this.mIcsi.updateCallProfile();
                if (this.mIcsi.mListener != null) {
                    ImsStreamMediaProfile imsStreamMediaProfile = new ImsStreamMediaProfile(this.mIcsi.mCallProfile.mMediaProfile.getAudioQuality(), 0, this.mIcsi.mCallProfile.mMediaProfile.getVideoQuality(), -1, this.mIcsi.mCallProfile.mMediaProfile.getRttMode());
                    this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
                    this.mIcsi.mListener.callSessionProgressing(imsStreamMediaProfile);
                }
            }
        } catch (RemoteException e) {
        }
    }

    public void onTrying() throws RemoteException {
        if (this.mIcsi.mSession != null) {
            ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
            if (imsCallSessionImpl.isCmcSecondaryType(imsCallSessionImpl.mSession.getCmcType())) {
                this.mIcsi.updateCallProfile();
                if (this.mIcsi.mListener != null) {
                    this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
                }
            }
        }
    }

    public void onRingingBack() throws RemoteException {
        this.mIcsi.mState = 2;
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            if ("<urn:alert:service:call-waiting>".equals(this.mIcsi.mSession.getCallProfile().getAlertInfo()) && !this.mIcsi.mIsCWNotified) {
                this.mIcsi.mIsCWNotified = true;
                this.mIcsi.onSuppServiceReceived(0, 3);
            }
            ImsStreamMediaProfile imsStreamMediaProfile = new ImsStreamMediaProfile(this.mIcsi.mCallProfile.mMediaProfile.getAudioQuality(), 0, this.mIcsi.mCallProfile.mMediaProfile.getVideoQuality(), -1, this.mIcsi.mCallProfile.mMediaProfile.getRttMode());
            this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
            this.mIcsi.mListener.callSessionProgressing(imsStreamMediaProfile);
        }
    }

    public void onSessionProgress(int audioEarlyMediaDir) throws RemoteException {
        this.mIcsi.mState = 2;
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            ImsStreamMediaProfile imsStreamMediaProfile = new ImsStreamMediaProfile(this.mIcsi.mCallProfile.mMediaProfile.getAudioQuality(), audioEarlyMediaDir, this.mIcsi.mCallProfile.mMediaProfile.getVideoQuality(), -1, this.mIcsi.mCallProfile.mMediaProfile.getRttMode());
            this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
            this.mIcsi.mListener.callSessionProgressing(imsStreamMediaProfile);
        }
    }

    public void onEarlyMediaStarted(int event) throws RemoteException {
        if (SimUtil.getSimMno(this.mIcsi.mSession.getPhoneId()) != Mno.DOCOMO || event == 180) {
            this.mIcsi.mState = 2;
        }
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            if ("<urn:alert:service:call-waiting>".equals(this.mIcsi.mSession.getCallProfile().getAlertInfo()) && !this.mIcsi.mIsCWNotified) {
                this.mIcsi.mIsCWNotified = true;
                this.mIcsi.onSuppServiceReceived(0, 3);
            }
            this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
            if (this.mIcsi.mState == 2) {
                this.mIcsi.mListener.callSessionProgressing(this.mIcsi.getImsStreamMediaProfile());
            }
        }
    }

    public void onEstablished(int callType) throws RemoteException {
        int prevState = this.mIcsi.mState;
        this.mIcsi.mState = 4;
        Mno mno = SimUtil.getSimMno(this.mIcsi.mSession.getPhoneId());
        if (this.mIcsi.isEmergencyCall() && (mno == Mno.VZW || mno == Mno.USCC || (mno == Mno.SPRINT && !this.mIcsi.isWifiCall()))) {
            this.mIcsi.mIsEcbmSupport = true;
        }
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            CallProfile cp = this.mIcsi.mSession.getCallProfile();
            if (cp == null || !cp.isMTCall() || prevState != 4) {
                this.mIcsi.mListener.callSessionInitiated(this.mIcsi.getCallProfile());
            } else {
                this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
            }
            ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
            int ttyModeFromCallType = imsCallSessionImpl.getTtyModeFromCallType(imsCallSessionImpl.mSession.getPhoneId(), callType);
            if (ttyModeFromCallType != 0) {
                this.mIcsi.mListener.callSessionTtyModeReceived(ttyModeFromCallType);
            }
        }
    }

    public void onFailure(int reason) throws RemoteException {
        if (this.mIcsi.mSession != null && this.mIcsi.mListener != null) {
            if (this.mIcsi.mState < 2) {
                this.mIcsi.mListener.callSessionInitiatedFailed(new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(reason), reason));
            } else {
                this.mIcsi.mListener.callSessionTerminated(new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(reason), reason));
            }
            this.mIcsi.mVolteServiceModule.unregisterRttEventListener(this.mIcsi.mSession.getPhoneId(), this.mIcsi.mRttEventListener);
            this.mIcsi.mSession = null;
        }
    }

    public void onSwitched(int callType) throws RemoteException {
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
            ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
            int ttyModeFromCallType = imsCallSessionImpl.getTtyModeFromCallType(imsCallSessionImpl.mSession.getPhoneId(), callType);
            if (ttyModeFromCallType != 0) {
                this.mIcsi.mListener.callSessionTtyModeReceived(ttyModeFromCallType);
            }
        }
    }

    public void onHeld(boolean initiator, boolean localHoldTone) throws RemoteException {
        this.mIcsi.updateCallProfile();
        this.mIcsi.updateHoldToneType(localHoldTone);
        if (this.mIcsi.mListener != null) {
            this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
            if (initiator) {
                this.mIcsi.mListener.callSessionHeld(this.mIcsi.getCallProfile());
            } else {
                this.mIcsi.mListener.callSessionHoldReceived(this.mIcsi.getCallProfile());
            }
        }
    }

    public void onResumed(boolean initiator) throws RemoteException {
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
            if (initiator) {
                this.mIcsi.mListener.callSessionResumed(this.mIcsi.getCallProfile());
            } else {
                this.mIcsi.mListener.callSessionResumeReceived(this.mIcsi.getCallProfile());
            }
        }
    }

    public void onForwarded() throws RemoteException {
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            int callDirection = this.mIcsi.mSession.getCallProfile().getDirection();
            if (callDirection == 0) {
                this.mIcsi.onSuppServiceReceived(callDirection, 2);
            }
            this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
        }
    }

    public void onEnded(int error) throws RemoteException {
        ImsReasonInfo reasonInfo;
        if (this.mIcsi.mSession != null) {
            Mno mno = SimUtil.getSimMno(this.mIcsi.mSession.getPhoneId());
            if (this.mIcsi.mListener != null && (this.mIcsi.mSession.getVideoCrbtSupportType() & 1) == 1) {
                this.mIcsi.updateCallProfile();
                this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
            }
            CallProfile cp = this.mIcsi.mSession.getCallProfile();
            Log.i("ImsCallSessionEventListener", "onEnded(), cmcType: " + this.mIcsi.mSession.getCmcType() + ", sessionState: " + this.mIcsi.mState + ", error: " + error);
            if (this.mIcsi.mListener != null) {
                if (this.mIcsi.getPrevInternalState() == CallConstants.STATE.HeldCall && error == 210) {
                    this.mIcsi.onSuppServiceReceived(1, 5);
                }
                if (this.mIcsi.mState < 2) {
                    if (cp != null && cp.isMTCall()) {
                        this.mIcsi.mListener.callSessionTerminated(new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(error), error));
                    } else if (cp != null && cp.isMOCall() && this.mIcsi.mVolteServiceModule.isVolteRetryRequired(this.mIcsi.mSession.getPhoneId(), cp.getCallType(), new SipError(error))) {
                        if (mno == Mno.TMOUS && error == 2414) {
                            reasonInfo = new ImsReasonInfo(ImSessionEvent.SEND_MESSAGE, error);
                        } else {
                            reasonInfo = new ImsReasonInfo(147, error);
                        }
                        this.mIcsi.mListener.callSessionInitiatedFailed(reasonInfo);
                    } else if (cp != null && cp.hasCSFBError()) {
                        this.mIcsi.mListener.callSessionInitiatedFailed(new ImsReasonInfo(146, error));
                    } else if (this.mIcsi.mSession.getCmcType() <= 0 || error != 603 || this.mIcsi.getCmcCallSessionManager() == null || !this.mIcsi.getCmcCallSessionManager().isReplacedSession()) {
                        this.mIcsi.mListener.callSessionInitiatedFailed(new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(error), error));
                    } else {
                        this.mIcsi.mListener.callSessionTerminated(new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(error), error));
                    }
                } else if (this.mIcsi.mSession.getCmcType() <= 0 || this.mIcsi.mState != 4) {
                    this.mIcsi.mListener.callSessionTerminated(new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(error), error));
                } else {
                    ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
                    this.mIcsi.mListener.callSessionTerminated(imsCallSessionImpl.changeCmcErrorReason(imsCallSessionImpl.mSession.getCmcType(), error));
                }
            } else if (this.mIcsi.mGoogleImsService.hasConferenceHost()) {
                ImsCallSessionImpl host = this.mIcsi.mGoogleImsService.getConferenceHost();
                host.getListener().callSessionMergeFailed(new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(error), error, ""));
            }
            if (this.mIcsi.isMultiparty()) {
                this.mIcsi.mGoogleImsService.setConferenceHost((ImsCallSessionImpl) null);
            }
            if (this.mIcsi.mIsEcbmSupport) {
                this.mIcsi.mGoogleImsService.enterEmergencyCallbackMode(this.mIcsi.mSession.getPhoneId());
            }
            this.mIcsi.mState = 8;
            this.mIcsi.mIsEcbmSupport = false;
            if (this.mIcsi.mSession != null) {
                this.mIcsi.mVolteServiceModule.unregisterRttEventListener(this.mIcsi.mSession.getPhoneId(), this.mIcsi.mRttEventListener);
                this.mIcsi.mSession = null;
            }
            if (this.mIcsi.mImsVideoCallProvider != null) {
                this.mIcsi.mImsVideoCallProvider.setCallback((IImsVideoCallCallback) null);
            }
        }
    }

    public void onSessionUpdateRequested(int type, byte[] data) {
    }

    public void onStopAlertTone() {
    }

    public void onError(int error, String errorString, int retryAfter) throws RemoteException {
        if (this.mIcsi.mSession != null) {
            CallProfile cp = this.mIcsi.mSession.getCallProfile();
            if (this.mIcsi.mSession.getCmcType() > 0) {
                Log.d("ImsCallSessionEventListener", "onError(), cmcType: " + this.mIcsi.mSession.getCmcType() + ", sessionState: " + this.mIcsi.mState);
                if (this.mIcsi.mState >= 2 || this.mIcsi.getCmcCallSessionManager() == null || this.mIcsi.getCmcCallSessionManager().getP2pSessionSize() <= 0) {
                    this.mIcsi.updateCallProfile();
                    if (this.mIcsi.mListener != null) {
                        this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
                    }
                } else {
                    Log.d("ImsCallSessionEventListener", "onError(), ignore error of cmcCall. just return: " + this.mIcsi.getCmcCallSessionManager().getP2pSessionSize());
                    return;
                }
            }
            if (this.mIcsi.mListener != null) {
                if (cp != null && error == 603 && "Outgoing Call Barred".equals(errorString)) {
                    this.mIcsi.onSuppServiceReceived(cp.getDirection(), 5);
                }
                if (this.mIcsi.mState < 2) {
                    if (onErrorBeforeNego(error, errorString, retryAfter)) {
                        return;
                    }
                } else if (onErrorWhileNegoOrLater(error, errorString)) {
                    return;
                }
            } else if (this.mIcsi.mGoogleImsService.hasConferenceHost()) {
                ImsCallSessionImpl host = this.mIcsi.mGoogleImsService.getConferenceHost();
                ImsReasonInfo reasonInfo = new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(error), error, errorString);
                host.getListener().callSessionMergeFailed(reasonInfo);
                if (this.mIcsi.mGoogleImsService.isInitialMerge() && error == 1105) {
                    host.getListener().callSessionTerminated(reasonInfo);
                }
            }
            if (cp != null && cp.isConferenceCall()) {
                this.mIcsi.mGoogleImsService.setConferenceHost((ImsCallSessionImpl) null);
            }
        }
    }

    private boolean onErrorBeforeNego(int error, String errorString, int retryAfter) throws RemoteException {
        int i;
        Mno mno = SimUtil.getSimMno(this.mIcsi.mSession.getPhoneId());
        CallProfile cp = this.mIcsi.mSession.getCallProfile();
        if (cp != null && cp.isMTCall()) {
            ImsCallSessionImpl imsCallSessionImpl = this.mIcsi;
            if (!imsCallSessionImpl.isCmcSecondaryType(imsCallSessionImpl.mSession.getCmcType()) || cp == null || !cp.isPullCall()) {
                this.mIcsi.mListener.callSessionTerminated(new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(error), error));
            } else {
                ImsCallSessionImpl imsCallSessionImpl2 = this.mIcsi;
                this.mIcsi.mListener.callSessionInitiatedFailed(imsCallSessionImpl2.changeCmcErrorReason(imsCallSessionImpl2.mSession.getCmcType(), error, errorString));
            }
        } else if (cp == null || !cp.isMOCall() || !this.mIcsi.mVolteServiceModule.isVolteRetryRequired(this.mIcsi.mSession.getPhoneId(), cp.getCallType(), new SipError(error, errorString), retryAfter)) {
            ImsCallSessionImpl imsCallSessionImpl3 = this.mIcsi;
            if (imsCallSessionImpl3.isCmcSecondaryType(imsCallSessionImpl3.mSession.getCmcType())) {
                ImsCallSessionImpl imsCallSessionImpl4 = this.mIcsi;
                this.mIcsi.mListener.callSessionInitiatedFailed(imsCallSessionImpl4.changeCmcErrorReason(imsCallSessionImpl4.mSession.getCmcType(), error, errorString));
            } else if (cp == null || !cp.hasCSFBError() || error == 381 || error == 382) {
                ImsReasonInfo reasonInfo = new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(error), error, errorString);
                if (error == 381 || error == 382) {
                    reasonInfo.mExtraCode = error;
                    int eccCat = DataTypeConvertor.convertUrnToEccCat(errorString);
                    reasonInfo.mExtraMessage = String.valueOf(eccCat);
                    if (eccCat == 254) {
                        this.mIcsi.mGoogleImsService.setServiceUrn(errorString);
                    }
                }
                this.mIcsi.mListener.callSessionInitiatedFailed(reasonInfo);
            } else {
                this.mIcsi.mListener.callSessionInitiatedFailed(new ImsReasonInfo(146, error, errorString));
            }
        } else {
            ImsReasonInfo reasonInfo2 = new ImsReasonInfo(147, error);
            if (mno == Mno.KDDI) {
                mEventCallRetryTotalTimer += retryAfter;
                HandlerThread handlerThread = new HandlerThread("ImsCallSessionImpl");
                this.mHandlerThread = handlerThread;
                handlerThread.start();
                AnonymousClass1 r4 = new Handler(this.mHandlerThread.getLooper()) {
                    public void handleMessage(Message msg) {
                        if (msg.what == 100) {
                            try {
                                ImsCallSessionEventListener.this.mIcsi.mListener.callSessionInitiatedFailed((ImsReasonInfo) msg.obj);
                            } catch (RemoteException e) {
                            }
                        }
                    }
                };
                this.mHandler = r4;
                if (retryAfter > 0) {
                    this.mHandler.sendMessageDelayed(r4.obtainMessage(101, reasonInfo2), ((long) retryAfter) * 1000);
                }
                int i2 = mEventCallRetryCounter + 1;
                mEventCallRetryCounter = i2;
                if (retryAfter > 0 && i2 < 5 && (i = mEventCallRetryTotalTimer) < 65) {
                    if (error != 503 || i < 45) {
                        Log.e("ImsCallSessionEventListener", "onError: Going to call Retry for SIP Error");
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100, reasonInfo2), ((long) retryAfter) * 1000);
                        return true;
                    }
                    Log.e("ImsCallSessionEventListener", "onError: Not Going to call Retry for SIP Error");
                }
                mEventCallRetryCounter = 0;
                mEventCallRetryTotalTimer = 0;
                this.mHandlerThread.quit();
                reasonInfo2 = new ImsReasonInfo(this.mIcsi.convertErrorReasonToFw(error), error, errorString);
            }
            this.mIcsi.mListener.callSessionInitiatedFailed(reasonInfo2);
        }
        this.mIcsi.mState = 8;
        if (this.mIcsi.mSession != null) {
            this.mIcsi.mVolteServiceModule.unregisterRttEventListener(this.mIcsi.mSession.getPhoneId(), this.mIcsi.mRttEventListener);
            this.mIcsi.mSession = null;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0042, code lost:
        if (r2.isCmcSecondaryType(r2.mSession.getCmcType()) != false) goto L_0x0044;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean onErrorWhileNegoOrLater(int r7, java.lang.String r8) throws android.os.RemoteException {
        /*
            r6 = this;
            com.sec.internal.google.ImsCallSessionImpl r0 = r6.mIcsi
            com.sec.ims.volte2.IImsCallSession r0 = r0.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            android.telephony.ims.ImsReasonInfo r1 = new android.telephony.ims.ImsReasonInfo
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            int r2 = r2.convertErrorReasonToFw(r7)
            r1.<init>(r2, r7, r8)
            r2 = 1105(0x451, float:1.548E-42)
            r3 = 1
            if (r7 == r2) goto L_0x011e
            r2 = 1106(0x452, float:1.55E-42)
            if (r7 == r2) goto L_0x011d
            r2 = 1111(0x457, float:1.557E-42)
            r3 = 0
            if (r7 == r2) goto L_0x0114
            r2 = 1112(0x458, float:1.558E-42)
            if (r7 == r2) goto L_0x010c
            switch(r7) {
                case 1118: goto L_0x0104;
                case 1119: goto L_0x00fc;
                case 1120: goto L_0x00f4;
                case 1121: goto L_0x00ec;
                default: goto L_0x0028;
            }
        L_0x0028:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            com.sec.ims.volte2.IImsCallSession r4 = r2.mSession
            int r4 = r4.getCmcType()
            boolean r2 = r2.isCmcPrimaryType(r4)
            if (r2 != 0) goto L_0x0044
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            com.sec.ims.volte2.IImsCallSession r4 = r2.mSession
            int r4 = r4.getCmcType()
            boolean r2 = r2.isCmcSecondaryType(r4)
            if (r2 == 0) goto L_0x005e
        L_0x0044:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            int r2 = r2.mState
            r4 = 3
            if (r2 == r4) goto L_0x0052
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            int r2 = r2.mState
            r4 = 4
            if (r2 != r4) goto L_0x005e
        L_0x0052:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            com.sec.ims.volte2.IImsCallSession r4 = r2.mSession
            int r4 = r4.getCmcType()
            android.telephony.ims.ImsReasonInfo r1 = r2.changeCmcErrorReason(r4, r7)
        L_0x005e:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            com.sec.ims.volte2.IImsCallSession r4 = r2.mSession
            int r4 = r4.getCmcType()
            boolean r2 = r2.isCmcPrimaryType(r4)
            r4 = 603(0x25b, float:8.45E-43)
            if (r2 == 0) goto L_0x0087
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            int r2 = r2.mState
            r5 = 2
            if (r2 != r5) goto L_0x0087
            r2 = 200(0xc8, float:2.8E-43)
            if (r7 == r2) goto L_0x0087
            r2 = 210(0xd2, float:2.94E-43)
            if (r7 == r2) goto L_0x0087
            if (r7 == r4) goto L_0x0087
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionInitiatedFailed(r1)
            goto L_0x00ae
        L_0x0087:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            com.sec.ims.volte2.IImsCallSession r5 = r2.mSession
            int r5 = r5.getCmcType()
            boolean r2 = r2.isCmcSecondaryType(r5)
            if (r2 == 0) goto L_0x00a7
            if (r0 == 0) goto L_0x00a7
            boolean r2 = r0.isPullCall()
            if (r2 == 0) goto L_0x00a7
            if (r7 != r4) goto L_0x00a7
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionInitiatedFailed(r1)
            goto L_0x00ae
        L_0x00a7:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionTerminated(r1)
        L_0x00ae:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            r4 = 8
            r2.mState = r4
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            boolean r2 = r2.mIsEcbmSupport
            if (r2 == 0) goto L_0x00cd
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            com.sec.internal.google.GoogleImsService r2 = r2.mGoogleImsService
            com.sec.internal.google.ImsCallSessionImpl r4 = r6.mIcsi
            com.sec.ims.volte2.IImsCallSession r4 = r4.mSession
            int r4 = r4.getPhoneId()
            r2.enterEmergencyCallbackMode(r4)
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            r2.mIsEcbmSupport = r3
        L_0x00cd:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            com.sec.ims.volte2.IImsCallSession r2 = r2.mSession
            if (r2 == 0) goto L_0x011c
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r2 = r2.mVolteServiceModule
            com.sec.internal.google.ImsCallSessionImpl r4 = r6.mIcsi
            com.sec.ims.volte2.IImsCallSession r4 = r4.mSession
            int r4 = r4.getPhoneId()
            com.sec.internal.google.ImsCallSessionImpl r5 = r6.mIcsi
            com.sec.ims.IRttEventListener r5 = r5.mRttEventListener
            r2.unregisterRttEventListener(r4, r5)
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            r4 = 0
            r2.mSession = r4
            goto L_0x011c
        L_0x00ec:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionCancelTransferFailed(r1)
            goto L_0x011c
        L_0x00f4:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionCancelTransferred()
            goto L_0x011c
        L_0x00fc:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionTransferFailed(r1)
            goto L_0x011c
        L_0x0104:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionTransferred()
            goto L_0x011c
        L_0x010c:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionResumeFailed(r1)
            goto L_0x011c
        L_0x0114:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionHoldFailed(r1)
        L_0x011c:
            return r3
        L_0x011d:
            return r3
        L_0x011e:
            com.sec.internal.google.ImsCallSessionImpl r2 = r6.mIcsi
            android.telephony.ims.aidl.IImsCallSessionListener r2 = r2.mListener
            r2.callSessionMergeFailed(r1)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.google.ImsCallSessionEventListener.onErrorWhileNegoOrLater(int, java.lang.String):boolean");
    }

    public void onProfileUpdated(MediaProfile src, MediaProfile dst) throws RemoteException {
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
        }
    }

    public void onConferenceEstablished() throws RemoteException {
        this.mIcsi.mState = 4;
        this.mIcsi.updateCallProfile();
    }

    public void onParticipantUpdated(int sessId, String[] participant, int[] status, int[] sipError) throws RemoteException {
        int[] iArr = status;
        CallProfile callProfile = this.mIcsi.mSession.getCallProfile();
        for (int i = 0; i < iArr.length; i++) {
            String userId = participant[i];
            if (userId.startsWith("*23#")) {
                userId = userId.substring(4, userId.length());
            }
            String userId2 = userId.replaceAll("[^0-9]", "");
            if (userId2.startsWith("010")) {
                userId2 = userId2.substring(3, userId2.length());
            }
            if (userId2.length() > 8) {
                userId2 = userId2.substring(0, 8);
            }
            this.mIcsi.mGoogleImsService.putConferenceStateList(Integer.parseInt(userId2), this.mIcsi.mSession.getCallId(), participant[i], Integer.toString(this.mIcsi.mSession.getCallId()), this.mIcsi.participantStatus(iArr[i]), sipError[i], this.mIcsi.getCallProfile());
        }
        this.mIcsi.mGoogleImsService.updateSecConferenceInfo(this.mIcsi.mCallProfile);
        this.mIcsi.mListener.callSessionUpdated(this.mIcsi.mCallProfile);
        this.mIcsi.mListener.callSessionConferenceStateUpdated(this.mIcsi.mGoogleImsService.getImsConferenceState());
    }

    public void onParticipantAdded(int addedSessionId) throws RemoteException {
        ImsCallSessionImpl participant = this.mIcsi.mGoogleImsService.getCallSession(addedSessionId);
        if (this.mIcsi.mGoogleImsService.hasConferenceHost() && participant != null) {
            ImsCallSessionImpl host = this.mIcsi.mGoogleImsService.getConferenceHost();
            IImsCallSession secCallSession = this.mIcsi.mVolteServiceModule.getSessionByCallId(addedSessionId);
            if (secCallSession != null) {
                String user = secCallSession.getCallProfile().getDialingNumber();
                if (!TextUtils.isEmpty(user)) {
                    this.mIcsi.mGoogleImsService.putConferenceState(addedSessionId, user, Integer.toString(addedSessionId), "connected", this.mIcsi.getCallProfile());
                }
                if (this.mIcsi.mListener == null) {
                    this.mIcsi.mGoogleImsService.updateSecConferenceInfo(this.mIcsi.mCallProfile);
                    host.getListener().callSessionUpdated(this.mIcsi.mCallProfile);
                    host.getListener().callSessionConferenceStateUpdated(this.mIcsi.mGoogleImsService.getImsConferenceState());
                } else {
                    this.mIcsi.mGoogleImsService.updateSecConferenceInfo(this.mIcsi.mCallProfile);
                    this.mIcsi.mListener.callSessionUpdated(this.mIcsi.mCallProfile);
                    this.mIcsi.mListener.callSessionConferenceStateUpdated(this.mIcsi.mGoogleImsService.getImsConferenceState());
                }
                participant.mIsConferenceParticipant = true;
            }
            if (participant.mIsConferenceHost) {
                host.getListener().callSessionMergeComplete(this.mIcsi.mImpl);
                this.mIcsi.mGoogleImsService.setConferenceHost(this.mIcsi.mImpl);
                if (this.mIcsi.mListener == null) {
                    host.getListener().callSessionResumed(this.mIcsi.getCallProfile());
                } else {
                    this.mIcsi.mListener.callSessionResumed(this.mIcsi.getCallProfile());
                }
            }
            if (!this.mIcsi.mGoogleImsService.isInitialMerge()) {
                participant.getListener().callSessionMergeComplete((com.android.ims.internal.IImsCallSession) null);
                if (this.mIcsi.mGoogleImsService.getConferenceHost().getInternalState() == CallConstants.STATE.HeldCall) {
                    this.mIcsi.mGoogleImsService.getConferenceHost().resume((ImsStreamMediaProfile) null);
                }
            }
        }
    }

    public void onParticipantRemoved(int removeSessionId) {
        this.mIcsi.updateConferenceStatus(removeSessionId, "disconnected");
        this.mIcsi.mGoogleImsService.removeConferenceState(removeSessionId);
    }

    public void onConfParticipantHeld(int sessionId, boolean initiator) throws RemoteException {
        if (this.mIcsi.mListener != null) {
            this.mIcsi.onSuppServiceReceived(1, 32);
        }
        this.mIcsi.updateConferenceStatus(sessionId, "on-hold");
    }

    public void onConfParticipantResumed(int sessionId, boolean initiator) throws RemoteException {
        if (this.mIcsi.mListener != null) {
            this.mIcsi.onSuppServiceReceived(1, 3);
        }
        this.mIcsi.updateConferenceStatus(sessionId, "connected");
    }

    public void onTtyTextRequest(int event, byte[] data) {
    }

    public void onUssdResponse(int result) throws RemoteException {
    }

    public void onUssdReceived(int status, int dcs, byte[] data) throws RemoteException {
        String ussdMessage = null;
        try {
            ussdMessage = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        if (status == 2) {
            this.mIcsi.mListener.callSessionUssdMessageReceived(1, ussdMessage);
        } else if (ussdMessage == null || !ussdMessage.contains("error-code")) {
            this.mIcsi.mListener.callSessionUssdMessageReceived(0, ussdMessage);
        } else {
            this.mIcsi.mListener.callSessionUssdMessageReceived(USSD_MODE_NW_ERROR, ussdMessage);
        }
    }

    public void onEPdgUnavailable(int reason) {
    }

    public void onEpdgStateChanged() throws RemoteException {
        this.mIcsi.updateCallProfile();
        if (this.mIcsi.mListener != null) {
            this.mIcsi.mListener.callSessionUpdated(this.mIcsi.getCallProfile());
        }
    }

    public void onSessionChanged(int callId) throws RemoteException {
        IImsCallSession session = this.mIcsi.mVolteServiceModule.getSessionByCallId(callId);
        if (session != null) {
            this.mIcsi.mSession = session;
        }
    }
}
