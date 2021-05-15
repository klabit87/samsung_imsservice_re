package com.sec.internal.ims.core.cmc;

import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.msc.sa.aidl.ISACallback;
import com.msc.sa.aidl.ISAService;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.ims.servicemodules.presence.SocialPresenceStorage;
import com.sec.internal.log.IMSLog;

public class CmcSAManager {
    private static final String LOG_TAG = "CmcSAManager";
    final int ID_REQUEST_ACCESSTOKEN = 1;
    String mAppId = "8f9l37bswj";
    String mAppSecret = "5AC671E87C25F004543DEC42D8982E02";
    CmcAccountManager mCmcAccountMgr;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public ISAService mISaService = null;
    private boolean mIsLocal = true;
    /* access modifiers changed from: private */
    public String mRegistrationCode = null;
    /* access modifiers changed from: private */
    public ISACallback mSACallback = null;
    private ServiceConnection mSAConnection = null;

    public CmcSAManager(Context context, CmcAccountManager accountMgr) {
        this.mContext = context;
        this.mCmcAccountMgr = accountMgr;
    }

    public void connectToSamsungAccountService(boolean isLocal) {
        IMSLog.i(LOG_TAG, "connect to Samsung Account AIDL() from cache: " + isLocal);
        if (!isSaLogined()) {
            IMSLog.i(LOG_TAG, "connectToSamsungAccountService Not Logined");
            return;
        }
        this.mIsLocal = isLocal;
        Intent intent = new Intent();
        intent.setAction("com.msc.action.samsungaccount.REQUEST_SERVICE");
        intent.setClassName("com.osp.app.signin", "com.msc.sa.service.RequestService");
        AnonymousClass1 r1 = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                ISAService unused = CmcSAManager.this.mISaService = ISAService.Stub.asInterface(service);
                if (CmcSAManager.this.mISaService != null) {
                    ISACallback unused2 = CmcSAManager.this.mSACallback = new SACallback();
                    try {
                        String regiCode = CmcSAManager.this.mISaService.registerCallback(CmcSAManager.this.mAppId, CmcSAManager.this.mAppSecret, CmcSAManager.this.mContext.getPackageName(), CmcSAManager.this.mSACallback);
                        StringBuilder sb = new StringBuilder();
                        sb.append("onServiceConnected to SA : ");
                        sb.append(regiCode == null ? "null" : regiCode);
                        IMSLog.i(CmcSAManager.LOG_TAG, sb.toString());
                        String unused3 = CmcSAManager.this.mRegistrationCode = regiCode;
                        CmcSAManager.this.getAccessTokenInternal();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                IMSLog.i(CmcSAManager.LOG_TAG, "onServiceDisconnected to SA");
                if (CmcSAManager.this.mISaService != null) {
                    try {
                        CmcSAManager.this.mISaService.unregisterCallback(CmcSAManager.this.mRegistrationCode);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    ISAService unused = CmcSAManager.this.mISaService = null;
                }
            }
        };
        this.mSAConnection = r1;
        this.mContext.bindService(intent, r1, 1);
    }

    private class SACallback extends ISACallback.Stub {
        private SACallback() {
        }

        public void onReceiveAccessToken(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
            if (resultData == null) {
                IMSLog.e(CmcSAManager.LOG_TAG, "onReceiveAccessToken: resultData is null");
            } else if (isSuccess) {
                String accessToken = resultData.getString("access_token");
                String userId = resultData.getString("user_id");
                String mcc = resultData.getString("mcc");
                String api_server_url = resultData.getString("api_server_url");
                String auth_server_url = resultData.getString("auth_server_url");
                IMSLog.s(CmcSAManager.LOG_TAG, "Success to get user id: " + userId + " Acess Token: " + accessToken + " api_server_url: " + api_server_url + " auth_server_url: " + auth_server_url + " mcc : " + mcc);
                CmcSAManager.this.mCmcAccountMgr.updateCmcSaInfo(accessToken, api_server_url);
                CmcSAManager.this.disconnectToSamsungAccountService();
            } else {
                if ("SAC_0402".equals(resultData.getString(CloudMessageProviderContract.BufferDBSMS.ERROR_CODE))) {
                    IMSLog.i(CmcSAManager.LOG_TAG, "Need to Sign In");
                }
                IMSLog.i(CmcSAManager.LOG_TAG, "Error case");
                CmcSAManager.this.disconnectToSamsungAccountService();
            }
        }

        public void onReceiveChecklistValidation(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public void onReceiveDisclaimerAgreement(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public void onReceiveAuthCode(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public void onReceiveSCloudAccessToken(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }

        public void onReceivePasswordConfirmation(int requestID, boolean isSuccess, Bundle resultData) throws RemoteException {
        }
    }

    /* access modifiers changed from: private */
    public void getAccessTokenInternal() {
        boolean z = false;
        boolean z2 = this.mAppId == null;
        if (this.mAppSecret == null) {
            z = true;
        }
        if (z2 || z) {
            IMSLog.e(LOG_TAG, "No App Id or Secret");
            return;
        }
        try {
            IMSLog.e(LOG_TAG, "Try to Get Access Token");
            String[] additional = {"user_id", SocialPresenceStorage.PresenceTable.BIRTHDAY, "email_id", "mcc", "server_url", "cc", "api_server_url", "auth_server_url", "device_physical_address_text", "login_id ", "login_id_type"};
            Bundle bundle = new Bundle();
            bundle.putCharSequence(NSDSContractExt.ConnectivityServicesColumns.CLIENT_ID, this.mAppId);
            bundle.putCharSequence("client_secret", this.mAppSecret);
            String curAccessToken = this.mCmcAccountMgr.getAccessTokenFromCmcPref();
            if (!this.mIsLocal && !TextUtils.isEmpty(curAccessToken) && !CmcAccountManager.CMC_SATOKEN_DEFAULT.equals(curAccessToken)) {
                bundle.putString("expired_access_token", curAccessToken);
            }
            bundle.putCharSequenceArray("additional", additional);
            if (this.mISaService != null) {
                this.mISaService.requestAccessToken(1, this.mRegistrationCode, bundle);
                IMSLog.e(LOG_TAG, "Request Access Token");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void disconnectToSamsungAccountService() {
        IMSLog.i(LOG_TAG, "disconnectToSamsungAccountService");
        try {
            this.mISaService.unregisterCallback(this.mRegistrationCode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mContext.unbindService(this.mSAConnection);
    }

    public boolean isSaLogined() {
        if (AccountManager.get(this.mContext).getAccountsByType("com.osp.app.signin").length > 0) {
            return true;
        }
        return false;
    }
}
