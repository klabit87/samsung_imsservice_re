package com.sec.internal.ims.servicemodules.volte2;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.ims.volte2.data.VolteConstants;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;

public class ImsExternalCallController {
    public static final String LOG_TAG = ImsExternalCallController.class.getSimpleName();
    IVolteServiceModuleInternal mModule;
    private ImsCallSession mPullingSession = null;
    private int mTransferSessionId = 0;
    private String mTransferTarget;

    public ImsExternalCallController(IVolteServiceModuleInternal vsm) {
        this.mModule = vsm;
    }

    public void pushCall(ImsCallSession callSession, String targetNumber, ImsRegistration regInfo) {
        boolean isSoftphoneEnabled = false;
        if (regInfo != null) {
            isSoftphoneEnabled = regInfo.getImsProfile().isSoftphoneEnabled();
        }
        if (callSession.getCallState() == CallConstants.STATE.HeldCall || isSoftphoneEnabled) {
            transfer(callSession.getSessionId(), targetNumber);
            return;
        }
        try {
            callSession.hold(new MediaProfile(VolteConstants.AudioCodecType.AUDIO_CODEC_AMRWB, -1));
            callSession.setHoldBeforeTransfer(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mTransferSessionId = callSession.getSessionId();
        this.mTransferTarget = targetNumber;
    }

    public void pushCallInternal() {
        Log.i(LOG_TAG, "pushCallInternal");
        transfer(this.mTransferSessionId, this.mTransferTarget);
        this.mTransferSessionId = 0;
        this.mTransferTarget = null;
    }

    public void consultativeTransferCall(ImsCallSession activeSession, ImsCallSession heldSession, ImsRegistration regInfo) {
        Mno mno;
        int phoneId = activeSession.getPhoneId();
        if (regInfo == null) {
            mno = SimUtil.getSimMno(phoneId);
        } else {
            mno = Mno.fromName(regInfo.getImsProfile().getMnoName());
        }
        if (mno == Mno.VODAFONE_CZ || mno == Mno.EDF || mno == Mno.TELEFONICA_SPAIN) {
            Log.i(LOG_TAG, "No need to hold an active call for ECT.");
            this.mTransferSessionId = heldSession.getSessionId();
            this.mTransferTarget = activeSession.getCallProfile().getDialingNumber();
            pushCallInternal();
            return;
        }
        try {
            activeSession.hold(new MediaProfile(VolteConstants.AudioCodecType.AUDIO_CODEC_AMRWB, -1));
            activeSession.setHoldBeforeTransfer(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mno == Mno.VODAFONE_SPAIN || mno == Mno.TELEFONICA_CZ || mno == Mno.TELENOR_DK || mno == Mno.TDC_DK || mno == Mno.H3G_DK || mno == Mno.ORANGE || mno == Mno.DLOG) {
            this.mTransferSessionId = activeSession.getSessionId();
            this.mTransferTarget = heldSession.getCallProfile().getDialingNumber();
        } else {
            this.mTransferSessionId = heldSession.getSessionId();
            this.mTransferTarget = activeSession.getCallProfile().getDialingNumber();
        }
        String str = LOG_TAG;
        Log.i(str, "ConsultativeTrasnfer mTransferSessionId : " + this.mTransferSessionId + ", mTransferTarget : " + this.mTransferTarget);
    }

    public void transfer(int sessionId, String msisdn) {
        ImsCallSession extSession = this.mModule.getSession(sessionId);
        if (extSession != null) {
            extSession.pushCall(msisdn);
        }
    }

    public void transferCall(int phoneId, String msisdn, String dialogId, DialogEvent[] lastDialogEvent) throws RemoteException {
        String str = LOG_TAG;
        Log.i(str, "try to transferCall from " + IMSLog.checker(msisdn) + " to Dialog Id : " + dialogId);
        Dialog targetDialog = null;
        if (TextUtils.isEmpty(dialogId) || TextUtils.isEmpty(msisdn)) {
            Log.e(LOG_TAG, "ignore wrong transfer reqeuset");
            return;
        }
        for (int i = 0; i < lastDialogEvent.length && targetDialog == null; i++) {
            if (lastDialogEvent[i] != null) {
                Iterator it = lastDialogEvent[i].getDialogList().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Dialog dialog = (Dialog) it.next();
                    if (dialogId.equals(dialog.getDialogId()) && msisdn.equals(lastDialogEvent[i].getMsisdn())) {
                        phoneId = i;
                        targetDialog = dialog;
                        break;
                    }
                }
            }
        }
        if (lastDialogEvent[phoneId] != null) {
            ImsRegistration regInfo = this.mModule.getRegInfo(lastDialogEvent[phoneId].getRegId());
            if (regInfo == null) {
                Log.e(LOG_TAG, "can't call transfer without registration");
                return;
            }
            boolean isSamsungMdmnCall = regInfo.getImsProfile().isSamsungMdmnEnabled();
            Iterator it2 = lastDialogEvent[phoneId].getDialogList().iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                Dialog dialog2 = (Dialog) it2.next();
                if (dialogId.equals(dialog2.getDialogId())) {
                    String str2 = LOG_TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("find target Dialog ");
                    sb.append(IMSLog.checker(dialog2 + ""));
                    Log.i(str2, sb.toString());
                    targetDialog = dialog2;
                    break;
                }
            }
            if (targetDialog == null || TextUtils.isEmpty(targetDialog.getSipCallId()) || TextUtils.isEmpty(targetDialog.getSipLocalTag()) || TextUtils.isEmpty(targetDialog.getSipRemoteTag())) {
                Log.e(LOG_TAG, "Can't find proper target dialog");
                return;
            }
            CallProfile profile = new CallProfile();
            MediaProfile media = new MediaProfile(VolteConstants.AudioCodecType.AUDIO_CODEC_AMRWB, -1);
            profile.setPullCall(true);
            profile.setCallType(targetDialog.getCallType());
            profile.setMediaProfile(media);
            profile.setCLI((String) null);
            if (isSamsungMdmnCall) {
                targetDialog.setMdmnExtNumber(targetDialog.getSessionDescription());
            }
            ImsCallSession createSession = this.mModule.createSession(profile, lastDialogEvent[phoneId].getRegId());
            this.mPullingSession = createSession;
            int id = createSession.pulling(msisdn, targetDialog);
            String str3 = LOG_TAG;
            Log.i(str3, "pulling Success : " + id);
            if (isSamsungMdmnCall) {
                this.mPullingSession.getCallProfile().setDialingNumber(targetDialog.getSessionDescription());
            }
            this.mModule.notifyOnPulling(phoneId, this.mPullingSession.getCallId());
            return;
        }
        Log.e(LOG_TAG, "LastDialogEvent is Empty");
    }
}
