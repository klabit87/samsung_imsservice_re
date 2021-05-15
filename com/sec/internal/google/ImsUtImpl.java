package com.sec.internal.google;

import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSsInfo;
import android.text.TextUtils;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.IImsUtListener;
import com.sec.ims.ss.IImsUtEventListener;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;

public class ImsUtImpl extends IImsUt.Stub {
    /* access modifiers changed from: private */
    public IImsUtListener mListener = null;
    private int mPhoneId = -1;
    private IImsUtEventListener mUtEventListener = new IImsUtEventListener.Stub() {
        public void onUtConfigurationUpdateFailed(int reqId, Bundle data) throws RemoteException {
            if (ImsUtImpl.this.mListener != null) {
                int errorCode = data.getInt("errorCode", 0);
                ImsUtImpl.this.mListener.utConfigurationUpdateFailed((IImsUt) null, reqId, new ImsReasonInfo(ImsUtImpl.this.convertErrorReasonToFw(errorCode), 0, data.getString("errorMsg")));
            }
        }

        public void onUtConfigurationQueryFailed(int reqId, Bundle data) throws RemoteException {
            if (ImsUtImpl.this.mListener != null) {
                int errorCode = data.getInt("errorCode", 0);
                ImsUtImpl.this.mListener.utConfigurationQueryFailed((IImsUt) null, reqId, new ImsReasonInfo(ImsUtImpl.this.convertErrorReasonToFw(errorCode), 0, data.getString("errorMsg")));
            }
        }

        public void onUtConfigurationQueried(int reqId, Bundle data) throws RemoteException {
            if (ImsUtImpl.this.mListener != null) {
                ImsUtImpl.this.mListener.utConfigurationQueried((IImsUt) null, reqId, data);
            }
        }

        public void onUtConfigurationUpdated(int reqId) throws RemoteException {
            if (ImsUtImpl.this.mListener != null) {
                ImsUtImpl.this.mListener.utConfigurationUpdated((IImsUt) null, reqId);
            }
        }

        public void onUtConfigurationCallWaitingQueried(int reqId, boolean status) throws RemoteException {
            if (ImsUtImpl.this.mListener != null) {
                ImsSsInfo[] ssInfoList = (ImsSsInfo[]) ImsSsInfo.CREATOR.newArray(1);
                ssInfoList[0] = new ImsSsInfo.Builder(status).build();
                ImsUtImpl.this.mListener.utConfigurationCallWaitingQueried((IImsUt) null, reqId, ssInfoList);
            }
        }

        public void onUtConfigurationCallForwardQueried(int reqId, Bundle[] callForwardList) throws RemoteException {
            Bundle[] bundleArr = callForwardList;
            if (ImsUtImpl.this.mListener != null) {
                ImsCallForwardInfo[] cfInfoList = (ImsCallForwardInfo[]) ImsCallForwardInfo.CREATOR.newArray(bundleArr.length);
                for (int i = 0; i < bundleArr.length; i++) {
                    int status = bundleArr[i].getInt("status", 0);
                    int cfType = bundleArr[i].getInt("condition", 0);
                    int noReplyTimer = bundleArr[i].getInt(SoftphoneNamespaces.SoftphoneCallHandling.NO_REPLY_TIMER, 0);
                    int uriType = bundleArr[i].getInt("ToA", 0);
                    String cfUri = bundleArr[i].getString("number");
                    if (TextUtils.isEmpty(cfUri)) {
                        cfUri = "";
                    }
                    cfInfoList[i] = new ImsCallForwardInfo(cfType, status, uriType, bundleArr[i].getInt("serviceClass", 1), cfUri, noReplyTimer);
                }
                ImsUtImpl.this.mListener.utConfigurationCallForwardQueried((IImsUt) null, reqId, cfInfoList);
                return;
            }
            int i2 = reqId;
        }

        public void onUtConfigurationCallBarringQueried(int reqId, Bundle[] callBarringList) throws RemoteException {
            if (ImsUtImpl.this.mListener != null) {
                ImsSsInfo[] ssInfoList = (ImsSsInfo[]) ImsSsInfo.CREATOR.newArray(callBarringList.length);
                for (int i = 0; i < callBarringList.length; i++) {
                    int status = callBarringList[i].getInt("status", 0);
                    if (callBarringList[i].getInt("condition", 0) == 10) {
                        ssInfoList[i] = new ImsSsInfo.Builder(status).setIncomingCommunicationBarringNumber(callBarringList[i].getString("number")).build();
                    } else {
                        ssInfoList[i] = new ImsSsInfo.Builder(status).setServiceClass(callBarringList[i].getInt("serviceClass", 1)).build();
                    }
                }
                ImsUtImpl.this.mListener.utConfigurationCallBarringQueried((IImsUt) null, reqId, ssInfoList);
            }
        }
    };
    private IUtServiceModule mUtService = null;

    public ImsUtImpl(int phoneId) {
        this.mPhoneId = phoneId;
        IUtServiceModule utServiceModule = ImsRegistry.getServiceModuleManager().getUtServiceModule();
        this.mUtService = utServiceModule;
        if (utServiceModule != null) {
            utServiceModule.registerForUtEvent(this.mPhoneId, this.mUtEventListener);
        }
    }

    public void close() throws RemoteException {
    }

    public int queryCallBarring(int cbType) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCallBarring(this.mPhoneId, cbType, 255);
    }

    public int queryCallForward(int condition, String number) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCallForward(this.mPhoneId, condition, number);
    }

    public int queryCallWaiting() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCallWaiting(this.mPhoneId);
    }

    public int queryCLIR() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCLIR(this.mPhoneId);
    }

    public int queryCLIP() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCLIP(this.mPhoneId);
    }

    public int queryCOLR() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCOLR(this.mPhoneId);
    }

    public int queryCOLP() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCOLP(this.mPhoneId);
    }

    public int transact(Bundle ssInfo) throws RemoteException {
        return this.mUtService == null ? -1 : -1;
    }

    public int updateCallBarring(int cbType, int action, String[] barrList) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallBarring(this.mPhoneId, cbType, action, 255, (String) null, barrList);
    }

    public int updateCallBarringWithPassword(int cbType, int action, String[] barrList, int serviceClass, String password) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallBarring(this.mPhoneId, cbType, action, serviceClass, password, barrList);
    }

    public int updateCallForward(int action, int condition, String number, int serviceClass, int timeSeconds) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallForward(this.mPhoneId, action, condition, number, serviceClass, timeSeconds);
    }

    public int updateCallWaiting(boolean enable, int serviceClass) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallWaiting(this.mPhoneId, enable, serviceClass);
    }

    public int updateCLIR(int clirMode) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCLIR(this.mPhoneId, clirMode);
    }

    public int updateCLIP(boolean enable) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCLIP(this.mPhoneId, enable);
    }

    public int updateCOLR(int presentation) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCOLR(this.mPhoneId, presentation);
    }

    public int updateCOLP(boolean enable) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCOLP(this.mPhoneId, enable);
    }

    public void setListener(IImsUtListener listener) throws RemoteException {
        this.mListener = listener;
    }

    public int queryCallBarringForServiceClass(int cbType, int serviceClass) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCallBarring(this.mPhoneId, cbType, serviceClass);
    }

    public int updateCallBarringForServiceClass(int cbType, int action, String[] barrList, int serviceClass) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallBarring(this.mPhoneId, cbType, action, serviceClass, (String) null, barrList);
    }

    public boolean isUssdEnabled() throws RemoteException {
        return this.mUtService.isUssdEnabled(this.mPhoneId);
    }

    /* access modifiers changed from: private */
    public int convertErrorReasonToFw(int error) {
        if (error == 403) {
            return 803;
        }
        if (error == 404) {
            return 801;
        }
        if (error == 408) {
            return 804;
        }
        if (error != 5001) {
            return 0;
        }
        return 805;
    }
}
