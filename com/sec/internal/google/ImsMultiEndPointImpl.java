package com.sec.internal.google;

import android.net.Uri;
import android.os.RemoteException;
import android.telephony.ims.ImsExternalCallState;
import android.text.TextUtils;
import com.android.ims.internal.IImsExternalCallStateListener;
import com.android.ims.internal.IImsMultiEndpoint;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImsMultiEndPointImpl extends IImsMultiEndpoint.Stub {
    private List<ImsExternalCallState> mDialogList = new ArrayList();
    private IImsExternalCallStateListener mImsMultiEndpointListener = null;
    private int mPhoneId = 0;

    public ImsMultiEndPointImpl(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public void setListener(IImsExternalCallStateListener listener) {
        this.mImsMultiEndpointListener = listener;
    }

    public IImsExternalCallStateListener getImsExternalCallStateListener() {
        return this.mImsMultiEndpointListener;
    }

    public void requestImsExternalCallStateInfo() throws RemoteException {
        this.mImsMultiEndpointListener.onImsExternalCallStateUpdate(this.mDialogList);
    }

    public void setDialogInfo(DialogEvent de, int cmcType) {
        int dialogId;
        int i = cmcType;
        this.mDialogList.clear();
        if (de.getDialogList().size() == 0) {
            this.mDialogList.add(new ImsExternalCallState(-1, Uri.parse(""), false, 2, 0, false));
            return;
        }
        for (Dialog info : de.getDialogList()) {
            if (info != null) {
                if (SimUtil.getSimMno(this.mPhoneId) == Mno.VZW) {
                    dialogId = ImsCallUtil.getIdForString(info.getSipCallId());
                } else {
                    try {
                        dialogId = Integer.parseInt(info.getDialogId());
                    } catch (NumberFormatException e) {
                        i = cmcType;
                    }
                }
                String remoteUri = info.getRemoteUri();
                if (TextUtils.isEmpty(remoteUri)) {
                    int i2 = dialogId;
                    i = cmcType;
                } else if (remoteUri.contains(":")) {
                    if (remoteUri.startsWith("tel:")) {
                        remoteUri = remoteUri.replace("tel:", "sip:");
                    }
                    if (!TextUtils.isEmpty(info.getRemoteDispName())) {
                        remoteUri = remoteUri + ";displayName=" + info.getRemoteDispName();
                    }
                    if (i == 2 || i == 4 || i == 8) {
                        String tmpRemoteUri = remoteUri.substring(remoteUri.indexOf(":") + 1);
                        if (!TextUtils.isEmpty(tmpRemoteUri)) {
                            remoteUri = remoteUri + ";oir=" + getOirExtraFromDialingNumber(tmpRemoteUri);
                            if (tmpRemoteUri.contains("Conference call")) {
                                remoteUri = remoteUri + ";cmc_pd_state=" + 1;
                            }
                        }
                        remoteUri = remoteUri + ";cmc_type=" + i;
                    }
                    int i3 = dialogId;
                    ImsExternalCallState imsExternalCallState = r7;
                    ImsExternalCallState imsExternalCallState2 = new ImsExternalCallState(dialogId, Uri.parse(remoteUri), info.isPullAvailable(), info.getState(), DataTypeConvertor.convertToGoogleCallType(info.getCallType()), info.isHeld());
                    this.mDialogList.add(imsExternalCallState);
                    i = cmcType;
                }
            }
        }
        StringBuffer crLogBuf = new StringBuffer("DE=");
        for (ImsExternalCallState iecs : this.mDialogList) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(iecs.getCallId() % 100000);
            sb.append(",");
            String str = "T";
            sb.append(iecs.getCallState() == 1 ? "C" : str);
            sb.append(",");
            sb.append(iecs.isCallHeld() ? "H" : "A");
            sb.append(",");
            if (!iecs.isCallPullable()) {
                str = "F";
            }
            sb.append(str);
            sb.append("]");
            crLogBuf.append(sb.toString());
        }
        IMSLog.c(LogClass.VOLTE_DIALOG_EVENT, crLogBuf.toString());
    }

    public List<ImsExternalCallState> getExternalCallStateList() {
        return this.mDialogList;
    }

    private int getOirExtraFromDialingNumber(String number) {
        if (NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equalsIgnoreCase(number)) {
            return 3;
        }
        if ("RESTRICTED".equalsIgnoreCase(number) || number.toLowerCase(Locale.US).contains("anonymous")) {
            return 1;
        }
        if ("Coin line/payphone".equalsIgnoreCase(number)) {
            return 4;
        }
        return 2;
    }

    public void setP2pPushDialogInfo(DialogEvent de, int cmcType) throws RemoteException {
        this.mDialogList.clear();
        for (Dialog info : de.getDialogList()) {
            if (info != null) {
                try {
                    int dialogId = Integer.parseInt(info.getDialogId());
                    Uri address = Uri.parse("sip:D2D@samsungims.com;d2d.push");
                    this.mDialogList.add(new ImsExternalCallState(dialogId, address, info.isPullAvailable(), info.getState(), DataTypeConvertor.convertToGoogleCallType(info.getCallType()), info.isHeld()));
                } catch (NumberFormatException e) {
                }
            }
        }
        requestImsExternalCallStateInfo();
    }
}
