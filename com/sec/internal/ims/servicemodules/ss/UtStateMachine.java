package com.sec.internal.ims.servicemodules.ss;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.telephony.PreciseDataConnectionState;
import android.text.TextUtils;
import com.sec.ims.extensions.ConnectivityManagerExt;
import com.sec.ims.settings.UserConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.XmlCreator;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.httpclient.DnsController;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.ss.CallBarringData;
import com.sec.internal.ims.servicemodules.ss.CallForwardingData;
import com.sec.internal.ims.servicemodules.ss.SsRuleData;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.PdnEventListener;
import com.sec.internal.log.IMSLog;
import com.squareup.okhttp.Dns;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.net.SocketFactory;
import org.xbill.DNS.KEYRecord;

public class UtStateMachine extends StateMachine {
    public static final int DOCUMENT_CACHE_RESET_TIMEOUT = 1000;
    public static final int EVENT_CACHE_RESULT_PARSE = 13;
    public static final int EVENT_DISCONNECT_PDN = 2;
    public static final int EVENT_DOCUMENT_CACHE_RESET = 5;
    public static final int EVENT_GET_RESULT_FAIL = 11;
    public static final int EVENT_GET_RESULT_SUCCESS = 10;
    public static final int EVENT_INIT_SS_403 = 14;
    public static final int EVENT_PDN_CONNECTED = 1;
    public static final int EVENT_PDN_DISCONNECTED = 3;
    public static final int EVENT_REQUEST_FAIL = 12;
    public static final int EVENT_REQUEST_TIMEOUT = 15;
    public static final int EVENT_SEPARATE_CFNL = 6;
    public static final int EVENT_SEPARATE_CFNRY = 7;
    public static final int EVENT_SEPARATE_CF_ALL = 8;
    public static final int EVENT_SEPARATE_MEDIA = 9;
    public static final int EVENT_TERMINAL_REQUEST = 4;
    public static final int HOUR = 3600000;
    public static final long HTTP_CONNECTION_TIMEOUT = 10000;
    public static final long HTTP_READ_TIMEOUT = 10000;
    public static final long HTTP_READ_TIMEOUT_GCF = 2000;
    public static final String LOG_TAG = UtServiceModule.class.getSimpleName();
    public static final int MAX_RETRY_COUNT_412 = 3;
    public static final int MINUTE = 60000;
    public static final int PDN_LINGER_TIMEOUT = 5000;
    public static final int QUERY_FIRST = 101;
    public static final int REQUEST_PDN = 100;
    private static int mCBIdCounter = 0;
    public boolean isGetAfter412 = false;
    public boolean isGetBeforePut = false;
    public boolean isRetryingCreatePdn = false;
    /* access modifiers changed from: private */
    public ApnSettings mApn = null;
    public int mBsfRetryCounter = 0;
    protected CallForwardingData mCFCache = null;
    /* access modifiers changed from: private */
    public UtConfigData mConfig = null;
    /* access modifiers changed from: private */
    public Context mContext;
    public int mCount412RetryDone = 0;
    protected List<InetAddress> mDnsAddresses = new ArrayList();
    public UtFeatureData mFeature = null;
    public boolean mForce403Error = false;
    protected boolean mHasCFCache = false;
    protected boolean mHasICBCache = false;
    protected boolean mHasOCBCache = false;
    protected CallBarringData mICBCache = null;
    private final IImsFramework mImsFramework;
    public boolean mIsFailedBySuspended = false;
    public boolean mIsGetSdBy404 = false;
    private boolean mIsRunningRequest = false;
    public boolean mIsSuspended = false;
    public boolean mIsUtConnectionError = false;
    protected InetAddress mLocalAddress = null;
    public int mMainCondition = -1;
    public int mNafRetryCounter = 0;
    /* access modifiers changed from: private */
    public Network mNetwork = null;
    protected CallBarringData mOCBCache = null;
    public IPdnController mPdnController = null;
    PdnEventListener mPdnListener = new PdnEventListener() {
        public void onConnected(int networkType, Network network) {
            String str = UtStateMachine.LOG_TAG;
            int i = UtStateMachine.this.mPhoneId;
            IMSLog.i(str, i, "onConnected " + networkType + " with " + network + " mPdnType " + UtStateMachine.this.mPdnType);
            if (networkType == UtStateMachine.this.mPdnType && network != null) {
                UtStateMachine.this.mSocketFactory = network.getSocketFactory();
                Network unused = UtStateMachine.this.mNetwork = network;
                String apnName = null;
                NetworkInfo networkInfo = ((ConnectivityManager) UtStateMachine.this.mContext.getSystemService("connectivity")).getNetworkInfo(network);
                if (networkInfo != null) {
                    apnName = networkInfo.getExtraInfo();
                }
                UtStateMachine utStateMachine = UtStateMachine.this;
                ApnSettings unused2 = utStateMachine.mApn = ApnSettings.load(utStateMachine.mContext, apnName, UtStateMachine.this.mConfig.apnSelection, SimUtil.getSubId(UtStateMachine.this.mPhoneId));
                UtStateMachine.this.UpdateDnsInfo();
                UtStateMachine utStateMachine2 = UtStateMachine.this;
                utStateMachine2.sendMessage(utStateMachine2.obtainMessage(1));
            }
        }

        public void onDisconnected(int networkType, boolean isPdnUp) {
            String str = UtStateMachine.LOG_TAG;
            int i = UtStateMachine.this.mPhoneId;
            IMSLog.i(str, i, "onDisconnected " + networkType + " with " + isPdnUp);
            UtStateMachine.this.mSocketFactory = null;
            Network unused = UtStateMachine.this.mNetwork = null;
            UtStateMachine.this.disconnectPdn();
        }

        public void onSuspended(int networkType) {
            if (networkType == UtStateMachine.this.mPdnType) {
                String str = UtStateMachine.LOG_TAG;
                int i = UtStateMachine.this.mPhoneId;
                IMSLog.i(str, i, "onSuspended " + networkType);
                UtStateMachine.this.mIsSuspended = true;
            }
        }

        public void onResumed(int networkType) {
            if (networkType == UtStateMachine.this.mPdnType) {
                String str = UtStateMachine.LOG_TAG;
                int i = UtStateMachine.this.mPhoneId;
                IMSLog.i(str, i, "onResumed " + networkType);
                UtStateMachine.this.mIsSuspended = false;
                if (UtStateMachine.this.mIsFailedBySuspended) {
                    UtStateMachine.this.mIsFailedBySuspended = false;
                    UtStateMachine utStateMachine = UtStateMachine.this;
                    utStateMachine.sendMessage(utStateMachine.obtainMessage(1));
                }
            }
        }
    };
    public int mPdnRetryCounter = 0;
    public int mPdnType = -1;
    private List<UtProfile> mPendingRequests;
    public int mPhoneId = -1;
    public int mPrevGetType = -1;
    protected CallForwardingData mPreviousCFCache = new CallForwardingData();
    protected UtProfile mProfile = null;
    protected RequestState mRequestState = null;
    protected ResponseState mResponseState = null;
    public boolean mSentSimServDoc = true;
    public boolean mSeparatedCFNL = false;
    public boolean mSeparatedCFNRY = false;
    public boolean mSeparatedCfAll = false;
    public boolean mSeparatedMedia = false;
    public SocketFactory mSocketFactory = null;
    protected UtStateMachine mThisSm = this;
    HttpRequestParams.HttpRequestCallback mUtCallback = new HttpRequestParams.HttpRequestCallback() {
        public void onComplete(HttpResponseParams httpResponseParams) {
            UtStateMachine.this.sendMessage(10, (Object) httpResponseParams);
        }

        public void onFail(IOException e) {
            UtStateMachine.this.sendMessage(11, (Object) e.getMessage());
        }
    };
    public int mUtHttpRetryCounter = 0;
    public int mUtRetryCounter = 0;
    public final UtServiceModule mUtServiceModule;
    public boolean needPdnRequestForCW = true;

    protected UtStateMachine(String name, Looper looper, UtServiceModule module, IImsFramework imsFramework, Context ctx) {
        super(name, looper);
        this.mUtServiceModule = module;
        this.mImsFramework = imsFramework;
        this.mContext = ctx;
        this.mRequestState = new RequestState(this);
        this.mResponseState = new ResponseState(this);
    }

    /* access modifiers changed from: protected */
    public void init(int phoneId) {
        addState(this.mRequestState);
        addState(this.mResponseState);
        this.mPhoneId = phoneId;
        this.mPdnController = this.mImsFramework.getPdnController();
        setInitialState(this.mRequestState);
        this.mIsRunningRequest = false;
        this.mPendingRequests = new ArrayList();
        removeMessages(14);
    }

    /* access modifiers changed from: protected */
    public void enqueueProfile(UtProfile profile) {
        this.mPendingRequests.add(profile);
    }

    /* access modifiers changed from: protected */
    public UtProfile dequeueProfile() {
        UtProfile retProfile = this.mPendingRequests.get(0);
        this.mPendingRequests.remove(0);
        return retProfile;
    }

    /* access modifiers changed from: protected */
    public void query(UtProfile profile) {
        enqueueProfile(profile);
        if (this.mIsRunningRequest) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Other request is processing now...");
        } else {
            processUtRequest();
        }
    }

    /* access modifiers changed from: protected */
    public void processUtRequest() {
        this.mIsRunningRequest = true;
        this.mProfile = dequeueProfile();
        removeMessages(2);
        removeMessages(100);
        initializeUtParameters();
        int utInternalError = checkUtInternalError();
        if (utInternalError != 0) {
            sendMessageDelayed(12, utInternalError, 100);
        } else if (isPutRequestBlocked()) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "Insertion of new rule is prohibited.");
            sendMessageDelayed(12, 1012, 100);
        } else {
            UtConfigData utConfigData = this.mConfig;
            if (utConfigData != null) {
                utConfigData.impu = this.mUtServiceModule.getPublicId(this.mPhoneId);
            }
            if (this.mUtServiceModule.isTerminalRequest(this.mPhoneId, this.mProfile)) {
                sendMessageDelayed(4, 100);
            } else {
                sendMessageDelayed(100, 100);
            }
            removeMessages(15);
            sendMessageDelayed(15, 1017, 32500);
        }
    }

    public boolean isPutRequestBlocked() {
        if (this.mFeature.insertNewRule) {
            return false;
        }
        if (this.mProfile.type == 101) {
            if (this.mCFCache == null || hasConditionOnCfCache()) {
                return false;
            }
            return true;
        } else if (this.mProfile.type == 105) {
            if (this.mOCBCache == null || hasConditionOnCbCache()) {
                return false;
            }
            return true;
        } else if (this.mProfile.type != 103 || this.mICBCache == null || hasConditionOnCbCache()) {
            return false;
        } else {
            return true;
        }
    }

    private void initializeUtParameters() {
        this.mUtHttpRetryCounter = 0;
        this.mUtRetryCounter = 0;
        this.mBsfRetryCounter = 0;
        this.mNafRetryCounter = 0;
        this.mSeparatedCFNL = false;
        this.mIsUtConnectionError = false;
        this.mIsFailedBySuspended = false;
        this.mIsSuspended = false;
        this.mSeparatedMedia = false;
        this.mSeparatedCfAll = false;
        this.mSeparatedCFNRY = false;
        this.mMainCondition = -1;
    }

    private int checkUtInternalError() {
        if (this.mProfile.type == 116 && !this.mUtServiceModule.needToGetSimservDocOnBootup(this.mPhoneId) && !isGetSDby404()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "No need to request GetSimservDoc");
            return 1001;
        } else if (isForbidden()) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "SS request is blocked by previous 403 error");
            return 1003;
        } else if (!UtUtils.isCallBarringType(this.mProfile.type) || this.mProfile.condition != 7) {
            int availability = this.mUtServiceModule.checkAvailabilityError(this.mPhoneId);
            if (availability != 0) {
                return availability;
            }
            if (this.mUtServiceModule.isInvalidUtRequest(this.mPhoneId, this.mProfile)) {
                return 1008;
            }
            if (!this.mUtServiceModule.isTerminalRequest(this.mPhoneId, this.mProfile) && !this.mUtServiceModule.checkXcapApn(this.mPhoneId)) {
                return 1009;
            }
            Mno mno = SimUtil.getSimMno(this.mPhoneId);
            if ((mno == Mno.KOODO || mno == Mno.TELUS) && UtUtils.isCallBarringType(this.mProfile.type)) {
                return 1010;
            }
            if (mno != Mno.WIND_GREECE || !isServiceActive()) {
                return 0;
            }
            IMSLog.e(LOG_TAG, this.mPhoneId, "Service is disabled on network side");
            return 1011;
        } else {
            IMSLog.e(LOG_TAG, this.mPhoneId, "not support All CB over IMS. CSFB.");
            return 1002;
        }
    }

    /* access modifiers changed from: protected */
    public void completeUtRequest() {
        completeUtRequest((Bundle[]) null);
    }

    /* access modifiers changed from: protected */
    public void completeUtRequest(boolean result) {
        Bundle[] response = {new Bundle()};
        response[0].putBoolean("status", result);
        completeUtRequest(response);
    }

    /* access modifiers changed from: protected */
    public void completeUtRequest(Bundle result) {
        completeUtRequest(new Bundle[]{result});
    }

    /* access modifiers changed from: protected */
    public void completeUtRequest(Bundle[] response) {
        int requestType = this.mProfile.type;
        int requestId = this.mProfile.requestId;
        printCompleteLog(response, requestType, requestId);
        if (SimUtil.getSimMno(this.mPhoneId).isChn()) {
            DnsController.correctServerAddr(this.mNafRetryCounter, this.mBsfRetryCounter);
        }
        removeMessages(15);
        if (this.mFeature.isDisconnectXcapPdn) {
            sendDisconnectPdnWithDelay();
        }
        this.mProfile = null;
        transitionTo(this.mRequestState);
        this.mUtServiceModule.notifySuccessResult(this.mPhoneId, requestType, requestId, response);
        if (requestType == 101 || requestType == 103 || requestType == 105 || requestType == 115) {
            this.mCount412RetryDone = 0;
        } else if (requestType == 116) {
            this.mSentSimServDoc = true;
        }
        if (!this.mPendingRequests.isEmpty()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Process next request...");
            processUtRequest();
            return;
        }
        this.mIsRunningRequest = false;
    }

    private void notifyFailResult(int requestId, Bundle error) {
        boolean isGet412 = this.isGetAfter412;
        int requestType = this.mProfile.type;
        if (!(requestType == 101 || requestType == 103 || requestType == 105 || requestType == 119)) {
            switch (requestType) {
                case 114:
                    if (isGet412) {
                        this.mCount412RetryDone = 0;
                        error.putInt("errorCode", UtError.PRECONDITION_FAILED);
                        error.putString("errorMsg", "Precondition Failed");
                        requestType = 115;
                        break;
                    }
                    break;
                case 115:
                    break;
                case 116:
                    return;
            }
        }
        this.mCount412RetryDone = 0;
        this.mUtServiceModule.notifyFailResult(this.mPhoneId, requestType, requestId, error);
    }

    /* access modifiers changed from: protected */
    public void failUtRequest(Bundle error) {
        int requestType = this.mProfile.type;
        int requestId = this.mProfile.requestId;
        printFailLog(error, requestType, requestId);
        this.isGetAfter412 = false;
        this.isGetBeforePut = false;
        removeMessages(15);
        UtFeatureData utFeatureData = this.mFeature;
        if (utFeatureData == null || (utFeatureData != null && utFeatureData.isDisconnectXcapPdn)) {
            sendDisconnectPdnWithDelay();
        }
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        if ((mno == Mno.CTC || mno == Mno.CTCMO) && error.getInt("errorCode", 0) == 403) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "CTC have to retry to CDMA dial");
            error.putInt("errorCode", 5001);
        }
        notifyFailResult(requestId, error);
        this.mProfile = null;
        transitionTo(this.mRequestState);
        if (!this.mPendingRequests.isEmpty()) {
            processUtRequest();
        } else {
            this.mIsRunningRequest = false;
        }
    }

    public boolean isServiceActive() {
        if (this.mProfile.type == 101) {
            CallForwardingData callForwardingData = this.mCFCache;
            if (callForwardingData == null || callForwardingData.active) {
                return false;
            }
            return true;
        } else if (this.mProfile.type == 103) {
            CallBarringData callBarringData = this.mICBCache;
            if (callBarringData == null || callBarringData.active) {
                return false;
            }
            return true;
        } else if (this.mProfile.type != 105) {
            return true;
        } else {
            CallBarringData callBarringData2 = this.mOCBCache;
            if (callBarringData2 == null || callBarringData2.active) {
                return false;
            }
            return true;
        }
    }

    public boolean hasConditionOnCfCache() {
        if (this.mProfile.condition == 7) {
            return true;
        }
        if (this.mProfile.condition == 4 || this.mProfile.condition == 5) {
            int startCond = 0;
            if (this.mProfile.condition == 5) {
                startCond = 1;
            }
            for (int cond = startCond; cond < 4; cond++) {
                if (!this.mCFCache.isExist(cond)) {
                    String str = LOG_TAG;
                    int i = this.mPhoneId;
                    IMSLog.e(str, i, "The network doesn't have CF condition " + cond);
                    return false;
                }
            }
        } else if (!this.mCFCache.isExist(this.mProfile.condition)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.e(str2, i2, "The network doesn't have CF condition " + this.mProfile.condition);
            return false;
        }
        return true;
    }

    public boolean hasConditionOnCbCache() {
        if (this.mProfile.type == 105) {
            if (this.mOCBCache.isExist(this.mProfile.condition)) {
                return true;
            }
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.e(str, i, "The network doesn't have OCB condition " + this.mProfile.condition);
            return false;
        } else if (this.mICBCache.isExist(this.mProfile.condition)) {
            return true;
        } else {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.e(str2, i2, "The network doesn't have ICB condition " + this.mProfile.condition);
            return false;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v8, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v9, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v15, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v16, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v16, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v17, resolved type: int} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void processTerminalRequest() {
        /*
            r11 = this;
            java.lang.String r0 = LOG_TAG
            int r1 = r11.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "process terminal request "
            r2.append(r3)
            com.sec.internal.ims.servicemodules.ss.UtProfile r3 = r11.mProfile
            int r3 = r3.type
            r2.append(r3)
            java.lang.String r3 = ", needPdnRequestForCW : "
            r2.append(r3)
            boolean r3 = r11.needPdnRequestForCW
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r11.mProfile
            int r0 = r0.type
            r1 = 114(0x72, float:1.6E-43)
            r2 = 100
            java.lang.String r3 = "enable_call_wait"
            if (r0 == r1) goto L_0x01ba
            r1 = 115(0x73, float:1.61E-43)
            if (r0 == r1) goto L_0x0189
            java.lang.String r1 = "ss_clir_pref"
            java.lang.String r2 = "ss_clip_pref"
            r3 = 1
            r4 = 0
            switch(r0) {
                case 102: goto L_0x0117;
                case 103: goto L_0x00f7;
                case 104: goto L_0x0117;
                case 105: goto L_0x00f7;
                case 106: goto L_0x00c2;
                case 107: goto L_0x00b4;
                case 108: goto L_0x007b;
                case 109: goto L_0x006d;
                default: goto L_0x0042;
            }
        L_0x0042:
            java.lang.String r0 = LOG_TAG
            int r1 = r11.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "no matched type "
            r2.append(r3)
            com.sec.internal.ims.servicemodules.ss.UtProfile r3 = r11.mProfile
            int r3 = r3.type
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            java.lang.String r1 = "errorCode"
            r0.putInt(r1, r4)
            r11.failUtRequest(r0)
            goto L_0x01ef
        L_0x006d:
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r11.mProfile
            int r0 = r0.condition
            int r2 = r11.mPhoneId
            r11.setUserSet((int) r2, (java.lang.String) r1, (int) r0)
            r11.completeUtRequest()
            goto L_0x01ef
        L_0x007b:
            r0 = 2
            int[] r0 = new int[r0]
            int r2 = r11.mPhoneId
            int r1 = r11.getUserSetToInt(r2, r1, r4)
            r0[r4] = r1
            r1 = 4
            r0[r3] = r1
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
            java.lang.String r2 = "queryClir"
            r1.putIntArray(r2, r0)
            java.lang.String r2 = LOG_TAG
            int r3 = r11.mPhoneId
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "terminal CLIR = "
            r5.append(r6)
            r4 = r0[r4]
            r5.append(r4)
            java.lang.String r4 = r5.toString()
            com.sec.internal.log.IMSLog.i(r2, r3, r4)
            r11.completeUtRequest((android.os.Bundle) r1)
            goto L_0x01ef
        L_0x00b4:
            int r0 = r11.mPhoneId
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r11.mProfile
            boolean r1 = r1.enable
            r11.setUserSet((int) r0, (java.lang.String) r2, (int) r1)
            r11.completeUtRequest()
            goto L_0x01ef
        L_0x00c2:
            int r0 = r11.mPhoneId
            int r0 = r11.getUserSetToInt(r0, r2, r3)
            android.telephony.ims.ImsSsInfo r1 = new android.telephony.ims.ImsSsInfo
            java.lang.String r2 = ""
            r1.<init>(r0, r2)
            android.os.Bundle r2 = new android.os.Bundle
            r2.<init>()
            java.lang.String r3 = "imsSsInfo"
            r2.putParcelable(r3, r1)
            java.lang.String r3 = LOG_TAG
            int r4 = r11.mPhoneId
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "terminal CLIP = "
            r5.append(r6)
            r5.append(r0)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r3, r4, r5)
            r11.completeUtRequest((android.os.Bundle) r2)
            goto L_0x01ef
        L_0x00f7:
            com.sec.internal.ims.servicemodules.ss.UtProfile r0 = r11.mProfile
            int r0 = r0.condition
            int r0 = com.sec.internal.ims.servicemodules.ss.UtUtils.convertCbTypeToBitMask(r0)
            com.sec.internal.ims.servicemodules.ss.UtProfile r1 = r11.mProfile
            int r1 = r1.action
            if (r1 != r3) goto L_0x0106
            goto L_0x0107
        L_0x0106:
            r3 = r4
        L_0x0107:
            r1 = r3
            com.sec.internal.ims.servicemodules.ss.MEDIA r2 = com.sec.internal.ims.servicemodules.ss.MEDIA.AUDIO
            r11.setCbUserConfig(r2, r1, r0)
            com.sec.internal.ims.servicemodules.ss.MEDIA r2 = com.sec.internal.ims.servicemodules.ss.MEDIA.VIDEO
            r11.setCbUserConfig(r2, r1, r0)
            r11.completeUtRequest()
            goto L_0x01ef
        L_0x0117:
            int r0 = r11.mPhoneId
            java.lang.String r1 = "ss_volte_cb_pref"
            int r0 = r11.getUserSetToInt(r0, r1, r4)
            int r1 = r11.mPhoneId
            java.lang.String r2 = "ss_video_cb_pref"
            int r1 = r11.getUserSetToInt(r1, r2, r4)
            r2 = r0 & r1
            com.sec.internal.ims.servicemodules.ss.UtProfile r5 = r11.mProfile
            int r5 = r5.condition
            int r5 = com.sec.internal.ims.servicemodules.ss.UtUtils.convertCbTypeToBitMask(r5)
            android.os.Bundle[] r6 = new android.os.Bundle[r3]
            r7 = r2 & r5
            if (r7 != r5) goto L_0x013b
            r7 = r3
            goto L_0x013c
        L_0x013b:
            r7 = r4
        L_0x013c:
            android.os.Bundle r8 = new android.os.Bundle
            r8.<init>()
            if (r7 == 0) goto L_0x0144
            goto L_0x0145
        L_0x0144:
            r3 = r4
        L_0x0145:
            java.lang.String r9 = "status"
            r8.putInt(r9, r3)
            com.sec.internal.ims.servicemodules.ss.UtProfile r3 = r11.mProfile
            int r3 = r3.condition
            java.lang.String r9 = "condition"
            r8.putInt(r9, r3)
            com.sec.internal.ims.servicemodules.ss.UtProfile r3 = r11.mProfile
            int r3 = r3.serviceClass
            java.lang.String r9 = "serviceClass"
            r8.putInt(r9, r3)
            r6[r4] = r8
            java.lang.String r3 = LOG_TAG
            int r4 = r11.mPhoneId
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "terminal CallBarring "
            r9.append(r10)
            com.sec.internal.ims.servicemodules.ss.UtProfile r10 = r11.mProfile
            int r10 = r10.condition
            r9.append(r10)
            java.lang.String r10 = " "
            r9.append(r10)
            r9.append(r7)
            java.lang.String r9 = r9.toString()
            com.sec.internal.log.IMSLog.i(r3, r4, r9)
            r11.completeUtRequest((android.os.Bundle[]) r6)
            goto L_0x01ef
        L_0x0189:
            int r0 = r11.mPhoneId
            com.sec.internal.interfaces.ims.core.ISimManager r0 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r0)
            if (r0 != 0) goto L_0x0194
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.DEFAULT
            goto L_0x0198
        L_0x0194:
            com.sec.internal.constants.Mno r1 = r0.getSimMno()
        L_0x0198:
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TELSTRA
            if (r1 != r4) goto L_0x01ad
            boolean r4 = r11.needPdnRequestForCW
            if (r4 == 0) goto L_0x01ad
            java.lang.String r3 = LOG_TAG
            int r4 = r11.mPhoneId
            java.lang.String r5 = "Telstra needs to connect xcap pdn for call waiting to check non VoLTE SIM."
            com.sec.internal.log.IMSLog.i(r3, r4, r5)
            r11.sendMessage((int) r2)
            return
        L_0x01ad:
            com.sec.internal.ims.servicemodules.ss.UtProfile r2 = r11.mProfile
            boolean r2 = r2.enable
            int r4 = r11.mPhoneId
            r11.setUserSet((int) r4, (java.lang.String) r3, (boolean) r2)
            r11.completeUtRequest()
            goto L_0x01ef
        L_0x01ba:
            int r0 = r11.mPhoneId
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.TELSTRA
            if (r0 != r1) goto L_0x01cc
            boolean r1 = r11.needPdnRequestForCW
            if (r1 == 0) goto L_0x01cc
            r11.sendMessage((int) r2)
            return
        L_0x01cc:
            int r1 = r11.mPhoneId
            boolean r1 = r11.getUserSetToBoolean(r1, r3)
            java.lang.String r2 = LOG_TAG
            int r3 = r11.mPhoneId
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "terminal CallWaiting "
            r4.append(r5)
            r4.append(r1)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.i(r2, r3, r4)
            r11.completeUtRequest((boolean) r1)
        L_0x01ef:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.processTerminalRequest():void");
    }

    private void setCbUserConfig(MEDIA mediaType, boolean activate, int bit) {
        int result;
        int setValue = 0;
        String target = null;
        if (mediaType == MEDIA.AUDIO) {
            setValue = getUserSetToInt(this.mPhoneId, "ss_volte_cb_pref", 0);
            target = "ss_volte_cb_pref";
        } else if (mediaType == MEDIA.VIDEO) {
            setValue = getUserSetToInt(this.mPhoneId, "ss_video_cb_pref", 0);
            target = "ss_video_cb_pref";
        }
        if (activate) {
            result = setValue | bit;
        } else {
            result = setValue & (~bit);
        }
        setUserSet(this.mPhoneId, target, result);
    }

    /* access modifiers changed from: protected */
    public boolean isSentSimServ() {
        return this.mSentSimServDoc;
    }

    /* access modifiers changed from: protected */
    public void setSentSimServ(boolean val) {
        this.mSentSimServDoc = val;
    }

    /* access modifiers changed from: protected */
    public boolean isGetSDby404() {
        return this.mIsGetSdBy404;
    }

    /* access modifiers changed from: protected */
    public void updateConfig(UtConfigData config, UtFeatureData feature) {
        this.mConfig = config;
        this.mFeature = feature;
        UtServiceModule utServiceModule = this.mUtServiceModule;
        int i = this.mPhoneId;
        utServiceModule.writeDump(i, "mConfig = " + this.mConfig.toString() + " mFeature = " + this.mFeature.toString() + " ssDomain = " + this.mUtServiceModule.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.DOMAIN, "CS") + " ussdDomain = " + this.mUtServiceModule.getSetting(this.mPhoneId, GlobalSettingsConstants.Call.USSD_DOMAIN, "CS"));
        this.needPdnRequestForCW = true;
        this.isRetryingCreatePdn = false;
        this.isGetBeforePut = false;
        clearCachedSsData(-1);
        setForce403Error(false);
        removeMessages(14);
    }

    /* access modifiers changed from: protected */
    public UtConfigData getConfig() {
        return this.mConfig;
    }

    public void clearCachedSsData(int type) {
        if (type == 101) {
            this.mCFCache = null;
            this.mHasCFCache = false;
        } else if (type == 103) {
            this.mICBCache = null;
            this.mHasICBCache = false;
        } else if (type != 105) {
            this.mCFCache = null;
            this.mICBCache = null;
            this.mOCBCache = null;
            this.mHasICBCache = false;
            this.mHasOCBCache = false;
            this.mHasCFCache = false;
        } else {
            this.mOCBCache = null;
            this.mHasOCBCache = false;
        }
    }

    /* access modifiers changed from: protected */
    public void onAirplaneModeChanged(int airplaneMode) {
        if (airplaneMode == 1) {
            removeMessages(2);
            transitionTo(this.mRequestState);
            disconnectPdn();
        }
    }

    public boolean isPutRequest() {
        return this.mProfile.type % 2 != 0;
    }

    /* access modifiers changed from: protected */
    public ImsUri.UriType getPreferredUriType() {
        if ("TEL".equalsIgnoreCase(this.mFeature.cfUriType)) {
            return ImsUri.UriType.TEL_URI;
        }
        return ImsUri.UriType.SIP_URI;
    }

    private boolean isUsePhoneContext(Mno mno) {
        return mno.isOneOf(Mno.VODAFONE_UK, Mno.SFR, Mno.SOFTBANK, Mno.TELSTRA, Mno.ETISALAT_UAE);
    }

    private boolean isGCF(Mno mno) {
        String salesCode = OmcCode.get();
        if (mno != Mno.GCF) {
            return false;
        }
        if ("CHM".equalsIgnoreCase(salesCode) || "CBK".equalsIgnoreCase(salesCode) || "CHC".equalsIgnoreCase(salesCode)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String getNetworkPreferredUri(String number) {
        ImsUri uri;
        String imsDomainName;
        int index;
        String impu = this.mUtServiceModule.getPublicId(this.mPhoneId);
        ImsUri.UriType uriType = getPreferredUriType();
        String domain = null;
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        if (impu != null && (index = impu.indexOf("@")) > 0) {
            domain = impu.substring(index + 1);
        }
        String number2 = number.replaceAll("\\p{Z}|\\p{Space}", "");
        if (isGCF(mno)) {
            uri = ImsUri.parse("tel:" + number2);
        } else if (domain == null || uriType != ImsUri.UriType.SIP_URI) {
            if (!number2.startsWith("+")) {
                if (mno == Mno.SINGTEL) {
                    number2 = number2 + ";phone-context=+65";
                } else if (domain != null && isUsePhoneContext(mno)) {
                    number2 = number2 + ";phone-context=" + domain;
                } else if (mno == Mno.SMART_CAMBODIA) {
                    number2 = "+855" + number2.substring(1);
                } else if (mno == Mno.EASTLINK) {
                    if (number2.length() == 11) {
                        number2 = "+" + number2;
                    } else if (number2.length() == 10) {
                        number2 = "+1" + number2;
                    }
                } else if ((mno == Mno.CTC || mno == Mno.CTCMO || mno == Mno.ETISALAT_UAE) && (imsDomainName = UtUtils.generate3GPPDomain(SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId))) != null) {
                    number2 = number2 + ";phone-context=" + imsDomainName;
                }
            }
            uri = ImsUri.parse("tel:" + number2);
        } else {
            if (mno != Mno.TELENOR_SWE && !number2.startsWith("+")) {
                if (mno.isTmobile() || mno == Mno.TELEKOM_ALBANIA) {
                    String imsDomainName2 = UtUtils.generate3GPPDomain(SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId));
                    if (imsDomainName2 != null) {
                        number2 = number2 + ";phone-context=" + imsDomainName2;
                    } else {
                        number2 = number2 + ";phone-context=" + domain;
                    }
                } else if (mno == Mno.VODAFONE_QATAR) {
                    number2 = UtUtils.makeInternationNumber(number2, "+974");
                } else {
                    number2 = number2 + ";phone-context=" + domain;
                }
            }
            uri = ImsUri.parse("sip:" + number2 + "@" + domain);
            uri.setUserParam(PhoneConstants.PHONE_KEY);
        }
        return uri.toString();
    }

    private int getPdnType() {
        String pdnType = this.mUtServiceModule.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.APN_SELECTION, "");
        if ("cbs".equalsIgnoreCase(pdnType)) {
            return ConnectivityManagerExt.TYPE_MOBILE_CBS;
        }
        if ("default".equalsIgnoreCase(pdnType)) {
            return 0;
        }
        if ("wifi".equalsIgnoreCase(pdnType)) {
            return 1;
        }
        return ConnectivityManagerExt.TYPE_MOBILE_XCAP;
    }

    /* access modifiers changed from: protected */
    public boolean hasConnection() {
        if (this.mPdnType == -1) {
            this.mPdnType = getPdnType();
        }
        return this.mPdnController.isConnected(this.mPdnType, this.mPdnListener);
    }

    private void sendDisconnectPdnWithDelay() {
        removeMessages(2);
        int loadedSim = 0;
        int delay = 5000;
        if (this.mProfile.type == 116) {
            delay = 0;
        } else {
            UtFeatureData utFeatureData = this.mFeature;
            if (utFeatureData != null && (delay = utFeatureData.delay_disconnect_pdn) > 5000) {
                for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
                    if (sm.isSimAvailable()) {
                        loadedSim++;
                    }
                }
                if (loadedSim >= 2) {
                    delay = 5000;
                }
            }
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "sendDisconnectPdnWithDelay: " + delay + "ms, loadedSim : " + loadedSim);
        sendMessageDelayed(2, (long) delay);
    }

    public void handlePdnFail(PreciseDataConnectionState state) {
        Message pdnFailMessage;
        if (this.mProfile != null) {
            int failCause = state.getDataConnectionFailCause();
            int apnType = getApnTypeFromPdnType(this.mPdnType);
            if ((state.getDataConnectionApnTypeBitMask() & apnType) == apnType && !isRetryPdnFailCause(failCause)) {
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "XCAP PDN setup failed. failCause = " + failCause + ", mPdnRetryCounter : " + this.mPdnRetryCounter);
                Mno mno = SimUtil.getSimMno(this.mPhoneId);
                if ((mno == Mno.CHT || mno == Mno.SINGTEL) && (failCause == 55 || failCause == 38)) {
                    IMSLog.e(LOG_TAG, this.mPhoneId, "MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED or NETWORK_FAILURE need retry.");
                    this.isRetryingCreatePdn = true;
                    removeMessages(2);
                    removeMessages(100);
                    sendMessageDelayed(obtainMessage(2), 1000);
                    sendMessageDelayed(obtainMessage(100), 1500);
                    return;
                }
                if (mno == Mno.VODAFONE_UK && failCause == 27) {
                    IMSLog.e(LOG_TAG, this.mPhoneId, "Vodafone UK returns MISSING_UNKNOWN_APN for non VoLTE SIM.");
                    failCause = 33;
                }
                if (failCause == 33) {
                    IMSLog.e(LOG_TAG, this.mPhoneId, "This SIM is not subscribed for xcap");
                    pdnFailMessage = obtainMessage(12, 403);
                } else {
                    IMSLog.e(LOG_TAG, this.mPhoneId, "Disconnect xcap pdn");
                    pdnFailMessage = obtainMessage(12, failCause + 10000);
                }
                UtServiceModule utServiceModule = this.mUtServiceModule;
                int i2 = this.mPhoneId;
                utServiceModule.writeDump(i2, "PDN failCause : " + failCause);
                IMSLog.c(LogClass.UT_PDN_FAILURE, this.mPhoneId + "," + failCause);
                this.needPdnRequestForCW = false;
                removeMessages(2);
                sendMessage(2);
                sendMessage(pdnFailMessage);
            }
        }
    }

    private boolean isRetryPdnFailCause(int failCause) {
        if (failCause != 0 && failCause != 14 && failCause != 65537) {
            return false;
        }
        String str = LOG_TAG;
        IMSLog.i(str, "isRetryFailCause: " + failCause);
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        if ((mno == Mno.CTC || mno == Mno.CU) && failCause == 0) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "pdnRetryCounter: " + this.mPdnRetryCounter);
            int i = this.mPdnRetryCounter;
            if (i > 1) {
                return false;
            }
            this.mPdnRetryCounter = i + 1;
        }
        return true;
    }

    private int getApnTypeFromPdnType(int pdnType) {
        if (pdnType == 0) {
            return 17;
        }
        if (pdnType == 12) {
            return 128;
        }
        if (pdnType != 27) {
            return -1;
        }
        return KEYRecord.Flags.FLAG4;
    }

    public void handleEpdgAvailabilityChanged(boolean isEpdgAvailable) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleEpdgAvailabilityChanged: to " + isEpdgAvailable);
        if (!isEpdgAvailable && this.mProfile == null && hasConnection()) {
            removeMessages(2);
            sendMessage(2);
        }
    }

    /* access modifiers changed from: protected */
    public void disconnectPdn() {
        removeMessages(2);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "disconnectPdn: mPdnType " + this.mPdnType);
        int i2 = this.mPdnType;
        if (i2 != -1) {
            this.mPdnController.stopPdnConnectivity(i2, this.mPhoneId, this.mPdnListener);
            sendMessage(3);
        }
    }

    public boolean isForbidden() {
        return this.mForce403Error;
    }

    public void setForce403Error(boolean force403Error) {
        this.mForce403Error = force403Error;
    }

    private String updateCallforwardingInfo(Mno mno) {
        String mBody;
        if (!this.mFeature.support_media) {
            this.mProfile.serviceClass = 255;
        } else if (mno != Mno.RJIL && UtUtils.convertToMedia(this.mProfile.serviceClass) == MEDIA.ALL) {
            this.mProfile.serviceClass = 1;
        }
        if (this.mProfile.action == 0 && TextUtils.isEmpty(this.mProfile.number)) {
            UtProfile utProfile = this.mProfile;
            utProfile.number = this.mPreviousCFCache.getRule(utProfile.condition, UtUtils.convertToMedia(this.mProfile.serviceClass)).fwdElm.target;
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "previous activated number set " + IMSLog.checker(this.mProfile.number));
        }
        if (!this.mFeature.isCFSingleElement || this.mProfile.condition == 5 || this.mProfile.condition == 4) {
            mBody = XmlCreator.toXcapXml(UtUtils.makeMultipleXml(getCfRuleSet(), mno));
        } else if (this.mProfile.condition == 7) {
            mBody = XmlCreator.toXcapXml(UtUtils.makeNoReplyTimerXml(this.mProfile.timeSeconds, this.mPhoneId));
        } else if (mno == Mno.SINGTEL && !this.mSeparatedCFNRY && this.mProfile.condition == 2) {
            mBody = XmlCreator.toXcapXml(UtUtils.makeSingleXml(getCallForwardRule(this.mProfile.condition, UtUtils.convertToMedia(this.mProfile.serviceClass)), this.mFeature.support_ss, mno, this.mProfile.timeSeconds));
        } else {
            mBody = XmlCreator.toXcapXml(UtUtils.makeSingleXml(getCallForwardRule(this.mProfile.condition, UtUtils.convertToMedia(this.mProfile.serviceClass)), this.mFeature.support_ss, mno));
        }
        if (this.mProfile.action == 4) {
            this.mPreviousCFCache.getRule(this.mProfile.condition, UtUtils.convertToMedia(this.mProfile.serviceClass)).clear();
        }
        return mBody;
    }

    /* access modifiers changed from: protected */
    public String updateUtDetailInfo() {
        String mBody = "";
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        int i = this.mProfile.type;
        if (i != 101) {
            boolean z = true;
            if (i == 103 || i == 105) {
                if (!this.mFeature.support_media || this.mFeature.noMediaForCB) {
                    this.mProfile.serviceClass = 255;
                } else if (mno != Mno.RJIL && UtUtils.convertToMedia(this.mProfile.serviceClass) == MEDIA.ALL) {
                    this.mProfile.serviceClass = 1;
                }
                if (mno == Mno.VODAFONE_AUSTRALIA && this.mProfile.serviceClass == 8) {
                    this.mProfile.serviceClass = 1;
                }
                mBody = !this.mFeature.isCBSingleElement ? XmlCreator.toXcapXml(UtUtils.makeMultipleXml(getCbRuleSet(this.mProfile.type), this.mProfile.type, mno)) : XmlCreator.toXcapXml(UtUtils.makeSingleXml(getCallBarringRule(this.mProfile.type, UtUtils.convertToMedia(this.mProfile.serviceClass)), mno));
            } else if (i == 107) {
                mBody = XmlCreator.toXcapXml(UtUtils.makeSingleXml(UtElement.ELEMENT_OIP, this.mProfile.enable));
            } else if (i != 109) {
                if (i == 115) {
                    mBody = XmlCreator.toXcapXml(UtUtils.makeSingleXml("communication-waiting", this.mProfile.enable));
                }
            } else if (mno == Mno.VINAPHONE) {
                if (this.mProfile.condition != 1) {
                    z = false;
                }
                mBody = XmlCreator.toXcapXml(UtUtils.makeSingleXml(UtElement.ELEMENT_OIR, z));
            } else {
                mBody = XmlCreator.toXcapXml(UtUtils.makeSingleXml(UtElement.ELEMENT_OIR, this.mProfile.condition, this.mFeature.support_ss));
            }
        } else {
            mBody = updateCallforwardingInfo(mno);
        }
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "Print Body : " + IMSLog.numberChecker(mBody));
        return mBody;
    }

    private boolean isSupportfwd(Mno mno) {
        if (mno == Mno.KOODO || mno == Mno.VIVACOM_BULGARIA || mno == Mno.WIND_GREECE || mno == Mno.CLARO_DOMINICAN || mno == Mno.TELUS) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public CallForwardingData getCfRuleSet() {
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        boolean changes_need = false;
        if (this.mCFCache == null || this.mProfile.condition == 5 || this.mProfile.condition == 4) {
            CallForwardingData ruleset = new CallForwardingData();
            if (this.mProfile.condition == 5 || this.mProfile.condition == 4) {
                for (int i = 0; i <= 3; i++) {
                    if (this.mProfile.condition != 5 || i != 0) {
                        ruleset.rules.add(getCallForwardRule(i, UtUtils.convertToMedia(this.mProfile.serviceClass)));
                    }
                }
                if (mno == Mno.ATT) {
                    ruleset.rules.add(getCallForwardRule(6, MEDIA.ALL));
                }
                if (mno == Mno.ETISALAT_UAE || mno.isCanada()) {
                    CallForwardingData callForwardingData = this.mCFCache;
                    if (callForwardingData != null) {
                        callForwardingData.replyTimer = 0;
                    }
                    ruleset.replyTimer = 0;
                }
                if (this.mProfile.timeSeconds > 0) {
                    ruleset.replyTimer = this.mProfile.timeSeconds;
                } else {
                    CallForwardingData callForwardingData2 = this.mCFCache;
                    if (callForwardingData2 != null && callForwardingData2.replyTimer > 0) {
                        ruleset.replyTimer = this.mCFCache.replyTimer;
                    }
                }
            } else {
                ruleset.setRule(getCallForwardRule(this.mProfile.condition, UtUtils.convertToMedia(this.mProfile.serviceClass)));
            }
            if (mno == Mno.GCF) {
                CallForwardingData callForwardingData3 = this.mCFCache;
                if (callForwardingData3 != null) {
                    callForwardingData3.replyTimer = 0;
                }
            } else if (this.mProfile.condition == 2 && this.mProfile.timeSeconds > 0) {
                ruleset.replyTimer = this.mProfile.timeSeconds;
            }
            this.mCFCache = ruleset;
            return ruleset;
        }
        CallForwardingData CFCache = this.mCFCache.clone();
        for (SsRuleData.SsRule tempRule : CFCache.rules) {
            CallForwardingData.Rule rule = (CallForwardingData.Rule) tempRule;
            if (rule.fwdElm.fwdElm != null && rule.fwdElm.fwdElm.size() > 0 && isSupportfwd(mno)) {
                rule.fwdElm.fwdElm.clear();
            }
            if (rule.conditions.condition == this.mProfile.condition && (mno == Mno.BELL || rule.conditions.media.contains(UtUtils.convertToMedia(this.mProfile.serviceClass)) || (mno == Mno.BEELINE_RUSSIA && rule.conditions.media.contains(MEDIA.ALL)))) {
                if (this.mProfile.action == 3) {
                    rule.conditions.state = true;
                    rule.fwdElm.target = this.mProfile.number;
                } else if (this.mProfile.action == 1) {
                    rule.conditions.state = true;
                    if (!TextUtils.isEmpty(this.mProfile.number)) {
                        rule.fwdElm.target = this.mProfile.number;
                    }
                } else {
                    rule.conditions.state = false;
                    if (this.mProfile.action == 4) {
                        rule.fwdElm.target = "";
                    }
                }
                rule.conditions.action = this.mProfile.action;
                if (!TextUtils.isEmpty(rule.fwdElm.target) && !rule.fwdElm.target.startsWith("sip:") && !rule.fwdElm.target.startsWith("tel:") && !rule.fwdElm.target.startsWith("voicemail:")) {
                    rule.fwdElm.target = getNetworkPreferredUri(rule.fwdElm.target);
                }
                changes_need = true;
            }
        }
        if (this.mProfile.condition == 0 && changes_need && mno == Mno.BELL) {
            return CFCache;
        }
        if (!CFCache.isExist(this.mProfile.condition, UtUtils.convertToMedia(this.mProfile.serviceClass))) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "CF rule is not present. Make new rule.");
            CFCache.setRule(makeCFRule(this.mProfile.condition, this.mProfile.serviceClass, this.mProfile.action, this.mProfile.number));
            if (mno == Mno.GCF && this.mProfile.condition == 6) {
                CFCache.rules.add(makeCFRule(3, this.mProfile.serviceClass, this.mProfile.action, this.mProfile.number));
            }
        }
        if (mno == Mno.GCF) {
            CFCache.replyTimer = 0;
        } else if (this.mProfile.condition == 2 && this.mProfile.timeSeconds > 0) {
            CFCache.replyTimer = this.mProfile.timeSeconds;
        }
        return CFCache;
    }

    /* access modifiers changed from: protected */
    public ArrayList<CallBarringData.Rule> parseSIBtarget(String[] barringList) {
        ArrayList<CallBarringData.Rule> ruleList = new ArrayList<>();
        if (barringList == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Empty password");
            return ruleList;
        }
        for (String tokens : barringList) {
            CallBarringData.Rule tempRule = new CallBarringData.Rule();
            String[] tempTokenList = tokens.split(",");
            tempRule.ruleId = tempTokenList[0];
            tempRule.conditions.condition = 10;
            int i = 1;
            tempRule.target.add(UtUtils.cleanBarringNum(tempTokenList[1]));
            tempRule.allow = false;
            tempRule.conditions.state = tempTokenList[2].equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE);
            Condition condition = tempRule.conditions;
            if (!tempRule.conditions.state) {
                i = 3;
            }
            condition.action = i;
            ruleList.add(tempRule);
        }
        return ruleList;
    }

    private CallBarringData addKddiCbRules(CallBarringData CBCache) {
        if (CBCache == null) {
            CBCache = new CallBarringData();
        }
        if (this.mProfile.condition == 10) {
            CallBarringData newCBCache = new CallBarringData();
            Iterator<CallBarringData.Rule> it = parseSIBtarget(this.mProfile.valueList).iterator();
            while (it.hasNext()) {
                CallBarringData.Rule r = it.next();
                newCBCache.rules.add(r);
                String str = LOG_TAG;
                IMSLog.d(str, "KDDI_UT added rule id = " + r.ruleId + " conditions = " + r.conditions + " media = " + r.conditions.media);
            }
            if (CBCache.isExist(6)) {
                newCBCache.rules.add(CBCache.getRule(6, MEDIA.ALL));
            }
            return newCBCache;
        }
        if (this.mProfile.condition == 6) {
            CallBarringData.Rule newAcrRule = makeCBRule(this.mProfile.condition, this.mProfile.serviceClass, this.mProfile.action);
            newAcrRule.ruleId = this.mUtServiceModule.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.ICB_ANONYMOUS_RULEID, "");
            CBCache.setRule(newAcrRule);
            String str2 = LOG_TAG;
            IMSLog.d(str2, "KDDI_UT added rule id = " + newAcrRule.ruleId + " conditions = " + newAcrRule.conditions + " media = " + newAcrRule.conditions.media);
        }
        return CBCache;
    }

    /* access modifiers changed from: protected */
    public CallBarringData getCbRuleSet(int type) {
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        CallBarringData CBCache = this.mICBCache;
        if (type == 105) {
            CBCache = this.mOCBCache;
        }
        if (mno == Mno.KDDI) {
            return addKddiCbRules(CBCache);
        }
        if (CBCache != null) {
            CallBarringData CBCache2 = CBCache.clone();
            boolean matched = false;
            for (SsRuleData.SsRule rule : CBCache2.rules) {
                boolean z = false;
                if (mno.isOneOf(Mno.ELISA_FINLAND, Mno.TELEFONICA_CZ, Mno.VODAFONE_NEWZEALAND, Mno.CU) && rule.conditions.condition == this.mProfile.condition) {
                    rule.conditions.media.clear();
                    rule.conditions.media.add(UtUtils.convertToMedia(this.mProfile.serviceClass));
                }
                if (rule.conditions.condition == this.mProfile.condition && (rule.conditions.media.contains(UtUtils.convertToMedia(this.mProfile.serviceClass)) || (this.mFeature.supportAlternativeMediaForCb && rule.conditions.media.contains(MEDIA.ALL)))) {
                    matched = true;
                    Condition condition = rule.conditions;
                    if (this.mProfile.action == 1 || this.mProfile.action == 3) {
                        z = true;
                    }
                    condition.state = z;
                    rule.conditions.action = this.mProfile.action;
                }
            }
            if (!matched) {
                CBCache2.setRule(makeCBRule(this.mProfile.condition, this.mProfile.serviceClass, this.mProfile.action));
            }
            return CBCache2;
        }
        CallBarringData ruleset = new CallBarringData();
        ruleset.setRule(getCallBarringRule(type, UtUtils.convertToMedia(this.mProfile.serviceClass)));
        return ruleset;
    }

    /* access modifiers changed from: protected */
    public CallBarringData.Rule getCallBarringRule(int type, MEDIA media) {
        MEDIA m;
        CallBarringData CBCache = this.mICBCache;
        if (type == 105) {
            CBCache = this.mOCBCache;
        }
        boolean z = false;
        if (CBCache == null || (m = getMatchedMediaForCB(CBCache, media)) == null) {
            CallBarringData.Rule rule = new CallBarringData.Rule();
            rule.allow = false;
            rule.ruleId = getCbRuleId();
            rule.conditions = new Condition();
            rule.conditions.condition = this.mProfile.condition;
            Condition condition = rule.conditions;
            if (this.mProfile.action == 3 || this.mProfile.action == 1) {
                z = true;
            }
            condition.state = z;
            rule.conditions.action = this.mProfile.action;
            if (CBCache == null) {
                CBCache = new CallBarringData();
            }
            rule.conditions.media = new ArrayList();
            rule.conditions.media.add(media);
            CBCache.setRule(rule);
            return rule;
        }
        CallBarringData.Rule temp = CBCache.getRule(this.mProfile.condition, m);
        if (temp.conditions.media.contains(m)) {
            Condition condition2 = temp.conditions;
            if (this.mProfile.action == 3 || this.mProfile.action == 1) {
                z = true;
            }
            condition2.state = z;
            temp.conditions.action = this.mProfile.action;
        }
        return temp;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0049, code lost:
        if (r8.mCFCache.isExist(r9) != false) goto L_0x004b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.ss.CallForwardingData.Rule getCallForwardRule(int r9, com.sec.internal.ims.servicemodules.ss.MEDIA r10) {
        /*
            r8 = this;
            int r0 = r8.mPhoneId
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            r2 = 4
            r3 = 0
            if (r1 == 0) goto L_0x01b2
            boolean r1 = r1.isExist(r9, r10)
            r4 = 3
            r5 = 2
            r6 = 1
            if (r1 != 0) goto L_0x004b
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[r4]
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.CMCC
            r1[r3] = r7
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.H3G_SE
            r1[r6] = r7
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.BEELINE_RUSSIA
            r1[r5] = r7
            boolean r1 = r0.isOneOf(r1)
            if (r1 == 0) goto L_0x0033
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r7 = com.sec.internal.ims.servicemodules.ss.MEDIA.ALL
            boolean r1 = r1.isExist(r9, r7)
            if (r1 != 0) goto L_0x004b
        L_0x0033:
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[r5]
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.CU
            r1[r3] = r7
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.CTC
            r1[r6] = r7
            boolean r1 = r0.isOneOf(r1)
            if (r1 == 0) goto L_0x01b2
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            boolean r1 = r1.isExist(r9)
            if (r1 == 0) goto L_0x01b2
        L_0x004b:
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[r5]
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.CMCC
            r1[r3] = r7
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.H3G_SE
            r1[r6] = r7
            boolean r1 = r0.isOneOf(r1)
            if (r1 == 0) goto L_0x007e
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            boolean r1 = r1.isExist(r9, r10)
            if (r1 != 0) goto L_0x007e
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r5 = com.sec.internal.ims.servicemodules.ss.MEDIA.ALL
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r1 = r1.getRule((int) r9, (com.sec.internal.ims.servicemodules.ss.MEDIA) r5)
            com.sec.internal.ims.servicemodules.ss.Condition r5 = r1.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r5 = r5.media
            com.sec.internal.ims.servicemodules.ss.MEDIA r7 = com.sec.internal.ims.servicemodules.ss.MEDIA.ALL
            r5.remove(r7)
            com.sec.internal.ims.servicemodules.ss.Condition r5 = r1.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r5 = r5.media
            com.sec.internal.ims.servicemodules.ss.MEDIA r7 = com.sec.internal.ims.servicemodules.ss.MEDIA.AUDIO
            r5.add(r7)
            goto L_0x00d6
        L_0x007e:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.BEELINE_RUSSIA
            if (r0 != r1) goto L_0x0095
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r7 = com.sec.internal.ims.servicemodules.ss.MEDIA.ALL
            boolean r1 = r1.isExist(r9, r7)
            if (r1 == 0) goto L_0x0095
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r5 = com.sec.internal.ims.servicemodules.ss.MEDIA.ALL
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r1 = r1.getRule((int) r9, (com.sec.internal.ims.servicemodules.ss.MEDIA) r5)
            goto L_0x00d6
        L_0x0095:
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[r5]
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.CU
            r1[r3] = r5
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.CTC
            r1[r6] = r5
            boolean r1 = r0.isOneOf(r1)
            if (r1 == 0) goto L_0x00d0
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            boolean r1 = r1.isExist(r9, r10)
            if (r1 != 0) goto L_0x00d0
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r5 = com.sec.internal.ims.servicemodules.ss.MEDIA.AUDIO
            boolean r1 = r1.isExist(r9, r5)
            if (r1 == 0) goto L_0x00d0
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.MEDIA r5 = com.sec.internal.ims.servicemodules.ss.MEDIA.AUDIO
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r1 = r1.getRule((int) r9, (com.sec.internal.ims.servicemodules.ss.MEDIA) r5)
            com.sec.internal.ims.servicemodules.ss.Condition r5 = r1.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r5 = r5.media
            com.sec.internal.ims.servicemodules.ss.MEDIA r7 = com.sec.internal.ims.servicemodules.ss.MEDIA.AUDIO
            r5.remove(r7)
            com.sec.internal.ims.servicemodules.ss.Condition r5 = r1.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r5 = r5.media
            r5.add(r10)
            goto L_0x00d6
        L_0x00d0:
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r1 = r8.mCFCache
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r1 = r1.getRule((int) r9, (com.sec.internal.ims.servicemodules.ss.MEDIA) r10)
        L_0x00d6:
            com.sec.internal.ims.servicemodules.ss.UtProfile r5 = r8.mProfile
            java.lang.String r5 = r5.number
            boolean r5 = android.text.TextUtils.isEmpty(r5)
            if (r5 != 0) goto L_0x010e
            com.sec.internal.ims.servicemodules.ss.UtProfile r2 = r8.mProfile
            int r2 = r2.action
            if (r2 != 0) goto L_0x00f4
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.WIND_GREECE
            if (r0 != r2) goto L_0x00f4
            java.lang.String r2 = LOG_TAG
            int r5 = r8.mPhoneId
            java.lang.String r7 = "number change prevented for deactivation"
            com.sec.internal.log.IMSLog.i(r2, r5, r7)
            goto L_0x00fc
        L_0x00f4:
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            com.sec.internal.ims.servicemodules.ss.UtProfile r5 = r8.mProfile
            java.lang.String r5 = r5.number
            r2.target = r5
        L_0x00fc:
            com.sec.internal.ims.servicemodules.ss.Condition r2 = r1.conditions
            com.sec.internal.ims.servicemodules.ss.UtProfile r5 = r8.mProfile
            int r5 = r5.action
            if (r5 == r6) goto L_0x010a
            com.sec.internal.ims.servicemodules.ss.UtProfile r5 = r8.mProfile
            int r5 = r5.action
            if (r5 != r4) goto L_0x010b
        L_0x010a:
            r3 = r6
        L_0x010b:
            r2.state = r3
            goto L_0x013a
        L_0x010e:
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r8.mProfile
            int r4 = r4.action
            if (r4 != r6) goto L_0x0125
            com.sec.internal.ims.servicemodules.ss.Condition r2 = r1.conditions
            r2.state = r6
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.ATT
            if (r0 != r2) goto L_0x013a
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            com.sec.internal.ims.servicemodules.ss.UtProfile r3 = r8.mProfile
            java.lang.String r3 = r3.number
            r2.target = r3
            goto L_0x013a
        L_0x0125:
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r8.mProfile
            int r4 = r4.action
            if (r4 != r2) goto L_0x0136
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            java.lang.String r4 = ""
            r2.target = r4
            com.sec.internal.ims.servicemodules.ss.Condition r2 = r1.conditions
            r2.state = r3
            goto L_0x013a
        L_0x0136:
            com.sec.internal.ims.servicemodules.ss.Condition r2 = r1.conditions
            r2.state = r3
        L_0x013a:
            com.sec.internal.ims.servicemodules.ss.Condition r2 = r1.conditions
            com.sec.internal.ims.servicemodules.ss.UtProfile r3 = r8.mProfile
            int r3 = r3.action
            r2.action = r3
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            java.lang.String r2 = r2.target
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 != 0) goto L_0x0194
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            java.lang.String r2 = r2.target
            java.lang.String r3 = "sip:"
            boolean r2 = r2.startsWith(r3)
            if (r2 != 0) goto L_0x0180
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            java.lang.String r2 = r2.target
            java.lang.String r3 = "tel:"
            boolean r2 = r2.startsWith(r3)
            if (r2 != 0) goto L_0x0180
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            java.lang.String r2 = r2.target
            java.lang.String r3 = "voicemail:"
            boolean r2 = r2.startsWith(r3)
            if (r2 != 0) goto L_0x0180
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            com.sec.internal.ims.servicemodules.ss.ForwardTo r3 = r1.fwdElm
            java.lang.String r3 = r3.target
            java.lang.String r3 = r8.getNetworkPreferredUri(r3)
            r2.target = r3
            goto L_0x0194
        L_0x0180:
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.TMOBILE_PL
            if (r0 != r2) goto L_0x0194
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            com.sec.internal.ims.servicemodules.ss.ForwardTo r3 = r1.fwdElm
            java.lang.String r3 = r3.target
            java.lang.String r3 = com.sec.internal.ims.servicemodules.ss.UtUtils.getNumberFromURI(r3)
            java.lang.String r3 = r8.getNetworkPreferredUri(r3)
            r2.target = r3
        L_0x0194:
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            java.util.List<com.sec.internal.ims.servicemodules.ss.ForwardElm> r2 = r2.fwdElm
            if (r2 == 0) goto L_0x01b1
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            java.util.List<com.sec.internal.ims.servicemodules.ss.ForwardElm> r2 = r2.fwdElm
            int r2 = r2.size()
            if (r2 <= 0) goto L_0x01b1
            boolean r2 = r8.isSupportfwd(r0)
            if (r2 == 0) goto L_0x01b1
            com.sec.internal.ims.servicemodules.ss.ForwardTo r2 = r1.fwdElm
            java.util.List<com.sec.internal.ims.servicemodules.ss.ForwardElm> r2 = r2.fwdElm
            r2.clear()
        L_0x01b1:
            return r1
        L_0x01b2:
            com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule r1 = new com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule
            r1.<init>()
            com.sec.internal.ims.servicemodules.ss.ForwardTo r4 = new com.sec.internal.ims.servicemodules.ss.ForwardTo
            r4.<init>()
            r1.fwdElm = r4
            com.sec.internal.ims.servicemodules.ss.Condition r4 = new com.sec.internal.ims.servicemodules.ss.Condition
            r4.<init>()
            r1.conditions = r4
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r8.mProfile
            java.lang.String r4 = r4.number
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x01dd
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r8.mProfile
            java.lang.String r5 = r4.number
            java.lang.String r5 = com.sec.internal.ims.servicemodules.ss.UtUtils.getNumberFromURI(r5)
            java.lang.String r5 = r8.getNetworkPreferredUri(r5)
            r4.number = r5
        L_0x01dd:
            com.sec.internal.ims.servicemodules.ss.ForwardTo r4 = r1.fwdElm
            com.sec.internal.ims.servicemodules.ss.UtProfile r5 = r8.mProfile
            java.lang.String r5 = r5.number
            r4.target = r5
            java.lang.String r4 = r8.getCfRuleId(r9)
            r1.ruleId = r4
            com.sec.internal.ims.servicemodules.ss.Condition r4 = r1.conditions
            r4.condition = r9
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r8.mProfile
            int r4 = r4.action
            if (r4 == 0) goto L_0x01fb
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r8.mProfile
            int r4 = r4.action
            if (r4 != r2) goto L_0x01ff
        L_0x01fb:
            com.sec.internal.ims.servicemodules.ss.Condition r2 = r1.conditions
            r2.state = r3
        L_0x01ff:
            com.sec.internal.ims.servicemodules.ss.Condition r2 = r1.conditions
            com.sec.internal.ims.servicemodules.ss.UtProfile r3 = r8.mProfile
            int r3 = r3.action
            r2.action = r3
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r2 = r8.mCFCache
            if (r2 != 0) goto L_0x0212
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r2 = new com.sec.internal.ims.servicemodules.ss.CallForwardingData
            r2.<init>()
            r8.mCFCache = r2
        L_0x0212:
            com.sec.internal.ims.servicemodules.ss.Condition r2 = r1.conditions
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            r2.media = r3
            com.sec.internal.ims.servicemodules.ss.Condition r2 = r1.conditions
            java.util.List<com.sec.internal.ims.servicemodules.ss.MEDIA> r2 = r2.media
            r2.add(r10)
            com.sec.internal.ims.servicemodules.ss.CallForwardingData r2 = r8.mCFCache
            r2.setRule(r1)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.getCallForwardRule(int, com.sec.internal.ims.servicemodules.ss.MEDIA):com.sec.internal.ims.servicemodules.ss.CallForwardingData$Rule");
    }

    private CallForwardingData.Rule makeCFRule(int condition, int serviceClass, int action, String target) {
        CallForwardingData.Rule r = CallForwardingData.makeRule(condition, UtUtils.convertToMedia(serviceClass));
        r.ruleId = getCfRuleId(condition);
        if (action == 1 || action == 3) {
            r.conditions.state = true;
            r.fwdElm.target = target;
        } else {
            r.conditions.state = false;
            if (action == 4) {
                r.fwdElm.target = "";
            }
        }
        r.conditions.action = action;
        if (!TextUtils.isEmpty(r.fwdElm.target)) {
            r.fwdElm.target = getNetworkPreferredUri(UtUtils.getNumberFromURI(r.fwdElm.target));
        }
        return r;
    }

    private CallBarringData.Rule makeCBRule(int condition, int serviceClass, int action) {
        CallBarringData.Rule r = CallBarringData.makeRule(condition, UtUtils.convertToMedia(serviceClass));
        r.ruleId = getCbRuleId();
        Condition condition2 = r.conditions;
        boolean z = true;
        if (!(action == 1 || action == 3)) {
            z = false;
        }
        condition2.state = z;
        r.conditions.action = action;
        return r;
    }

    private MEDIA getMatchedMediaForCB(CallBarringData cache, MEDIA media) {
        if (cache.isExist(this.mProfile.condition, media)) {
            return media;
        }
        if (!this.mFeature.supportAlternativeMediaForCb || !cache.isExist(this.mProfile.condition, MEDIA.ALL)) {
            return null;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "no exact CB rule media match -> media ALL should be used");
        return MEDIA.ALL;
    }

    /* access modifiers changed from: protected */
    public String getCbRuleId() {
        int i = this.mProfile.type;
        if (i != 103) {
            if (i != 105) {
                return "";
            }
            return "OCB" + createCBRequestId();
        } else if (this.mProfile.condition == 5 && this.mFeature.cbbicwr.length() > 0) {
            return this.mFeature.cbbicwr;
        } else {
            if (this.mProfile.condition == 1 && this.mFeature.cbbaic.length() > 0) {
                return this.mFeature.cbbaic;
            }
            return "ICB" + createCBRequestId();
        }
    }

    /* access modifiers changed from: protected */
    public String getCfRuleId(int condition) {
        String ruleId;
        if (condition == 1) {
            ruleId = this.mFeature.cfb;
        } else if (condition == 2) {
            ruleId = this.mFeature.cfnr;
        } else if (condition == 3) {
            ruleId = this.mFeature.cfnrc;
        } else if (condition != 6) {
            ruleId = this.mFeature.cfu;
        } else {
            ruleId = this.mFeature.cfni;
        }
        if (!this.mFeature.support_media || !UtUtils.convertToMedia(this.mProfile.serviceClass).equals(MEDIA.VIDEO)) {
            return ruleId;
        }
        return ruleId + "_video";
    }

    /* access modifiers changed from: protected */
    public String getCfURL() {
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        if (this.mProfile.condition == 5 || this.mProfile.condition == 4) {
            if (mno == Mno.CHT) {
                return "?xmlns(ss=http://uri.etsi.org/ngn/params/xml/simservs/xcap)";
            }
            return "?xmlns(cp=urn:ietf:params:xml:ns:common-policy)";
        } else if (this.mProfile.condition == 7) {
            return UtUrl.NOREPLY_URL;
        } else {
            if (this.mCFCache != null) {
                MEDIA media = UtUtils.convertToMedia(this.mProfile.serviceClass);
                String cacheRuleId = this.mCFCache.getRule(this.mProfile.condition, media).ruleId;
                if ((mno == Mno.CU || mno == Mno.CTC) && !this.mCFCache.isExist(this.mProfile.condition, media) && this.mCFCache.isExist(this.mProfile.condition, MEDIA.AUDIO)) {
                    cacheRuleId = this.mCFCache.getRule(this.mProfile.condition, MEDIA.AUDIO).ruleId;
                }
                if (cacheRuleId != null) {
                    return UtUrl.DIV_START_URL + cacheRuleId + UtUrl.DIV_END_URL;
                }
            }
            return UtUrl.DIV_START_URL + getCfRuleId(this.mProfile.condition) + UtUrl.DIV_END_URL;
        }
    }

    /* access modifiers changed from: protected */
    public String getCbURL() {
        MEDIA m;
        MEDIA m2;
        if (this.mProfile.type == 105) {
            CallBarringData callBarringData = this.mOCBCache;
            if (!(callBarringData == null || (m2 = getMatchedMediaForCB(callBarringData, UtUtils.convertToMedia(this.mProfile.serviceClass))) == null)) {
                return UtUrl.DIV_START_URL + this.mOCBCache.getRule(this.mProfile.condition, m2).ruleId + UtUrl.DIV_END_URL;
            }
        } else {
            CallBarringData callBarringData2 = this.mICBCache;
            if (!(callBarringData2 == null || (m = getMatchedMediaForCB(callBarringData2, UtUtils.convertToMedia(this.mProfile.serviceClass))) == null)) {
                return UtUrl.DIV_START_URL + this.mICBCache.getRule(this.mProfile.condition, m).ruleId + UtUrl.DIV_END_URL;
            }
        }
        return UtUrl.DIV_START_URL + getCbRuleId() + UtUrl.DIV_END_URL;
    }

    /* access modifiers changed from: protected */
    public HashMap<String, String> makeHeader() {
        HashMap<String, String> header = new HashMap<>();
        header.put(HttpController.HEADER_HOST, this.mConfig.nafServer);
        header.put("Accept-Encoding", UtUtils.getAcceptEncoding(this.mPhoneId));
        header.put("Accept", "*/*");
        header.put("X-3GPP-Intended-Identity", "\"" + this.mConfig.impu + "\"");
        if (SimUtil.getSimMno(this.mPhoneId).isTmobile()) {
            header.put("User-Agent", this.mConfig.xdmUserAgent + " " + OmcCode.getNWCode(this.mPhoneId) + " 3gpp-gba");
        } else {
            header.put("User-Agent", HttpController.VAL_3GPP_GBA);
        }
        if (isPutRequest()) {
            header.put("Content-Type", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        }
        return header;
    }

    /* access modifiers changed from: protected */
    public int createCBRequestId() {
        if (mCBIdCounter >= 255) {
            mCBIdCounter = 0;
        }
        int i = mCBIdCounter + 1;
        mCBIdCounter = i;
        return i;
    }

    public String makeUri() {
        StringBuilder uri = new StringBuilder();
        if (this.mConfig.nafPort == 443) {
            uri.append("https://");
        } else {
            uri.append("http://");
        }
        uri.append(this.mConfig.nafServer);
        if (this.mConfig.nafPort != 80) {
            uri.append(":");
            uri.append(this.mConfig.nafPort);
        }
        if (!this.mConfig.xcapRootUri.isEmpty()) {
            uri.append(this.mConfig.xcapRootUri);
        }
        uri.append(UtUrl.REQUEST_USER_URL);
        uri.append(this.mConfig.impu);
        uri.append(UtUrl.REQUEST_SERVICE_URL);
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        switch (this.mProfile.type) {
            case 100:
                if (mno != Mno.CHT) {
                    uri.append(UtUrl.DIV_URL);
                    break;
                } else {
                    uri.append(UtUrl.DIV_URL_SS);
                    break;
                }
            case 101:
                if (mno == Mno.CHT || mno == Mno.SPRINT) {
                    uri.append(UtUrl.DIV_URL_SS);
                } else {
                    uri.append(UtUrl.DIV_URL);
                }
                if (this.mFeature.isCFSingleElement) {
                    uri.append(getCfURL());
                    break;
                }
                break;
            case 102:
                if (mno != Mno.CHT) {
                    uri.append(UtUrl.ICB_URL);
                    break;
                } else {
                    uri.append(UtUrl.ICB_URL_SS);
                    break;
                }
            case 103:
                if (mno == Mno.CHT) {
                    uri.append(UtUrl.ICB_URL_SS);
                } else {
                    uri.append(UtUrl.ICB_URL);
                }
                if (this.mFeature.isCBSingleElement) {
                    uri.append(getCbURL());
                    break;
                }
                break;
            case 104:
                if (mno != Mno.CHT) {
                    uri.append(UtUrl.OCB_URL);
                    break;
                } else {
                    uri.append(UtUrl.OCB_URL_SS);
                    break;
                }
            case 105:
                if (mno == Mno.CHT) {
                    uri.append(UtUrl.OCB_URL_SS);
                } else {
                    uri.append(UtUrl.OCB_URL);
                }
                if (this.mFeature.isCBSingleElement) {
                    uri.append(getCbURL());
                    break;
                }
                break;
            case 106:
            case 107:
                if (mno != Mno.CLARO_PUERTO) {
                    uri.append(UtUrl.OIP_URL_SIMSERVS);
                    break;
                } else {
                    uri.append(UtUrl.OIP_URL);
                    break;
                }
            case 108:
            case 109:
                if (mno != Mno.CLARO_PUERTO) {
                    uri.append(UtUrl.OIR_URL_SIMSERVS);
                    break;
                } else {
                    uri.append(UtUrl.OIR_URL);
                    break;
                }
            case 110:
            case 111:
                uri.append(UtUrl.TIP_URL);
                break;
            case 112:
            case 113:
                uri.append(UtUrl.TIR_URL);
                break;
            case 114:
            case 115:
                uri.append(UtUrl.CW_URL);
                break;
        }
        int cpIndex = uri.indexOf("cp:");
        int ssIndex = uri.indexOf("ss:");
        if (cpIndex > 0 || ssIndex > 0) {
            if (mno == Mno.CHT && ((this.mProfile.condition == 5 || this.mProfile.condition == 4) && this.mProfile.type == 101)) {
                uri.append("xmlns(cp=urn:ietf:params:xml:ns:common-policy)");
            } else {
                uri.append("?");
                if (cpIndex > 0) {
                    uri.append("xmlns(cp=urn:ietf:params:xml:ns:common-policy)");
                }
                if (ssIndex > 0 || (mno == Mno.SFR && this.mProfile.type == 101)) {
                    uri.append(UtUrl.XMLNS_SS_URL);
                }
            }
        }
        return uri.toString();
    }

    /* access modifiers changed from: protected */
    public HttpRequestParams makeHttpParams() {
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        HttpRequestParams requestParams = new HttpRequestParams();
        HashMap<String, String> header = makeHeader();
        SocketFactory socketFactory = this.mSocketFactory;
        if (socketFactory != null) {
            requestParams.setSocketFactory(socketFactory);
        }
        if (this.mNetwork != null) {
            if (this.mFeature.ip_version > 0) {
                if (!mno.isChn()) {
                    this.mBsfRetryCounter = this.mNafRetryCounter;
                }
                requestParams.setDns(new DnsController(this.mNafRetryCounter, this.mBsfRetryCounter, this.mNetwork, this.mDnsAddresses, this.mFeature.ip_version, true, mno));
            } else {
                requestParams.setDns(new Dns() {
                    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                        if (hostname != null) {
                            try {
                                return Arrays.asList(UtStateMachine.this.mNetwork.getAllByName(hostname));
                            } catch (NullPointerException e) {
                                throw new UnknownHostException("android.net.Network.getAllByName returned null");
                            }
                        } else {
                            throw new UnknownHostException("hostname == null");
                        }
                    }
                });
            }
        }
        requestParams.setCallback(this.mUtCallback).setHeaders(header);
        if (isPutRequest()) {
            requestParams.setMethod(HttpRequestParams.Method.PUT);
            requestParams.setPostBody(new HttpPostBody(updateUtDetailInfo().getBytes()));
        } else {
            requestParams.setMethod(HttpRequestParams.Method.GET);
        }
        requestParams.setUrl(makeUri()).setBsfUrl(this.mConfig.bsfServer).setPhoneId(this.mPhoneId);
        if (this.mConfig.username.isEmpty()) {
            requestParams.setUserName(this.mConfig.impu);
        } else {
            requestParams.setUserName(this.mConfig.username);
        }
        requestParams.setPassword(this.mConfig.passwd).setUseTls(this.mFeature.support_tls).setConnectionTimeout(10000);
        if (mno == Mno.GCF) {
            requestParams.setReadTimeout(HTTP_READ_TIMEOUT_GCF);
        } else {
            requestParams.setReadTimeout(10000);
        }
        requestParams.setIpVersion(this.mFeature.ip_version);
        if (mno == Mno.ORANGE) {
            String proxyAddress = null;
            int proxyPort = 80;
            ApnSettings apnSettings = this.mApn;
            if (apnSettings != null) {
                proxyAddress = apnSettings.getProxyAddress();
                proxyPort = this.mApn.getProxyPort();
            }
            Proxy proxy = Proxy.NO_PROXY;
            try {
                if (!TextUtils.isEmpty(proxyAddress)) {
                    String str = LOG_TAG;
                    int i = this.mPhoneId;
                    IMSLog.i(str, i, "proxyAddress : " + proxyAddress + " ProxyPort : " + proxyPort);
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.mNetwork.getByName(proxyAddress), proxyPort));
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            requestParams.setProxy(proxy).setUseProxy(true);
        }
        if (mno == Mno.CU) {
            requestParams.setProxy(Proxy.NO_PROXY).setUseProxy(true);
        }
        if (mno == Mno.TMOUS) {
            requestParams.setUseImei(true);
        }
        return requestParams;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: java.lang.String} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unhandledMessage(android.os.Message r5) {
        /*
            r4 = this;
            java.lang.String r0 = LOG_TAG
            int r1 = r4.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "handleMessage "
            r2.append(r3)
            int r3 = r5.what
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            int r0 = r5.what
            r1 = 12
            r2 = 0
            if (r0 == r1) goto L_0x0053
            r1 = 14
            r3 = 0
            if (r0 == r1) goto L_0x003a
            r1 = 15
            if (r0 == r1) goto L_0x002b
            goto L_0x0063
        L_0x002b:
            boolean r0 = r4.isRetryingCreatePdn
            if (r0 == 0) goto L_0x0036
            r0 = 100
            r4.removeMessages(r0)
            r4.isRetryingCreatePdn = r3
        L_0x0036:
            r4.disconnectPdn()
            goto L_0x0053
        L_0x003a:
            r4.mProfile = r2
            r4.setForce403Error(r3)
            com.sec.internal.ims.servicemodules.ss.UtServiceModule r0 = r4.mUtServiceModule
            int r1 = r4.mPhoneId
            r0.unregisterCwdbObserver(r1)
            com.sec.internal.ims.servicemodules.ss.UtServiceModule r0 = r4.mUtServiceModule
            int r1 = r4.mPhoneId
            r0.updateCapabilities(r1)
            com.sec.internal.ims.servicemodules.ss.RequestState r0 = r4.mRequestState
            r4.transitionTo(r0)
            goto L_0x0063
        L_0x0053:
            int r0 = r5.arg1
            java.lang.Object r1 = r5.obj
            if (r1 == 0) goto L_0x005e
            java.lang.Object r1 = r5.obj
            r2 = r1
            java.lang.String r2 = (java.lang.String) r2
        L_0x005e:
            r1 = r2
            r4.requestFailed(r0, r1)
        L_0x0063:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.unhandledMessage(android.os.Message):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ff  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void requestFailed(int r17, java.lang.String r18) {
        /*
            r16 = this;
            r0 = r16
            r1 = r18
            int r2 = com.sec.internal.helper.httpclient.DnsController.getNafAddrSize()
            int r3 = com.sec.internal.helper.httpclient.DnsController.getBsfAddrSize()
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r0.mProfile
            int r4 = r4.type
            boolean r4 = com.sec.internal.ims.servicemodules.ss.UtUtils.isCallBarringType(r4)
            r5 = 32500(0x7ef4, double:1.6057E-319)
            r7 = 1017(0x3f9, float:1.425E-42)
            r8 = 15
            r9 = 1
            if (r4 == 0) goto L_0x002f
            com.sec.internal.ims.servicemodules.ss.UtProfile r4 = r0.mProfile
            int r4 = r4.condition
            r10 = 7
            if (r4 != r10) goto L_0x002f
            java.lang.String r4 = LOG_TAG
            int r10 = r0.mPhoneId
            java.lang.String r11 = "performing CSFB for CB_BA_ALL, ignoring handling NAPTR IP type"
            com.sec.internal.log.IMSLog.e(r4, r10, r11)
            goto L_0x0070
        L_0x002f:
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r4 = r0.mFeature
            if (r4 == 0) goto L_0x0070
            int r4 = r4.ip_version
            r10 = 3
            if (r4 != r10) goto L_0x0070
            int r4 = r0.mNafRetryCounter
            int r10 = r4 + 1
            if (r10 >= r2) goto L_0x0070
            int r4 = r4 + r9
            r0.mNafRetryCounter = r4
            java.lang.String r4 = LOG_TAG
            int r10 = r0.mPhoneId
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "mNafRetryCounter: "
            r11.append(r12)
            int r12 = r0.mNafRetryCounter
            r11.append(r12)
            java.lang.String r11 = r11.toString()
            com.sec.internal.log.IMSLog.i(r4, r10, r11)
            r0.removeMessages(r8)
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r4 = r0.mThisSm
            r4.sendMessageDelayed((int) r8, (int) r7, (long) r5)
            com.sec.internal.ims.servicemodules.ss.RequestState r4 = r0.mRequestState
            r0.transitionTo(r4)
            android.os.Message r4 = r0.obtainMessage(r9)
            r0.sendMessage((android.os.Message) r4)
            return
        L_0x0070:
            int r4 = r0.mPhoneId
            com.sec.internal.constants.Mno r4 = com.sec.internal.helper.SimUtil.getSimMno(r4)
            r10 = r17
            com.sec.internal.constants.Mno r11 = com.sec.internal.constants.Mno.TELEKOM_SVN
            if (r4 != r11) goto L_0x0085
            r11 = 404(0x194, float:5.66E-43)
            r12 = r17
            if (r12 != r11) goto L_0x0087
            r11 = 403(0x193, float:5.65E-43)
            goto L_0x0088
        L_0x0085:
            r12 = r17
        L_0x0087:
            r11 = r12
        L_0x0088:
            com.sec.internal.constants.Mno r12 = com.sec.internal.constants.Mno.CHT
            r13 = 1009(0x3f1, float:1.414E-42)
            r14 = 0
            if (r4 != r12) goto L_0x00b9
            r12 = 503(0x1f7, float:7.05E-43)
            if (r11 == r12) goto L_0x00b4
            r12 = 1002(0x3ea, float:1.404E-42)
            if (r11 == r12) goto L_0x00b4
            if (r11 == r13) goto L_0x00b4
            r12 = 1004(0x3ec, float:1.407E-42)
            if (r11 == r12) goto L_0x00b4
            r12 = 1006(0x3ee, float:1.41E-42)
            if (r11 == r12) goto L_0x00b4
            r12 = 1007(0x3ef, float:1.411E-42)
            if (r11 == r12) goto L_0x00b4
            r12 = 1013(0x3f5, float:1.42E-42)
            if (r11 == r12) goto L_0x00b4
            r12 = 1014(0x3f6, float:1.421E-42)
            if (r11 == r12) goto L_0x00b4
            r12 = 10000(0x2710, float:1.4013E-41)
            if (r11 < r12) goto L_0x00b2
            goto L_0x00b4
        L_0x00b2:
            r12 = r14
            goto L_0x00b5
        L_0x00b4:
            r12 = r9
        L_0x00b5:
            if (r12 == 0) goto L_0x00b9
            r11 = 403(0x193, float:5.65E-43)
        L_0x00b9:
            r12 = 403(0x193, float:5.65E-43)
            if (r11 != r12) goto L_0x00ff
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r15 = r0.mFeature
            if (r15 == 0) goto L_0x00ff
            boolean r15 = r15.isBlockUntilReboot
            if (r15 == 0) goto L_0x00ff
            boolean r15 = r16.isForbidden()
            if (r15 != 0) goto L_0x00ff
            r0.removeMessages(r8)
            java.lang.String r5 = LOG_TAG
            int r6 = r0.mPhoneId
            java.lang.String r7 = "By 403 Error, SS request will block"
            com.sec.internal.log.IMSLog.e(r5, r6, r7)
            r0.setForce403Error(r9)
            com.sec.internal.ims.servicemodules.ss.UtServiceModule r5 = r0.mUtServiceModule
            int r6 = r0.mPhoneId
            r5.registerCwdbObserver(r6)
            com.sec.internal.ims.servicemodules.ss.UtServiceModule r5 = r0.mUtServiceModule
            int r6 = r0.mPhoneId
            java.lang.String r7 = "set force CSFB by 403 Error "
            r5.writeDump(r6, r7)
            com.sec.internal.ims.servicemodules.ss.UtServiceModule r5 = r0.mUtServiceModule
            int r6 = r0.mPhoneId
            r5.updateCapabilities(r6)
            r0.sendInit403MessageDelayed(r1, r4)
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r5 = r0.mThisSm
            r6 = 12
            r7 = 150(0x96, double:7.4E-322)
            r5.sendMessageDelayed((int) r6, (int) r12, (long) r7)
            return
        L_0x00ff:
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r15 = r0.mFeature
            if (r15 == 0) goto L_0x0107
            boolean r15 = r15.isCsfbWithImserror
            if (r15 != 0) goto L_0x010d
        L_0x0107:
            if (r11 == r13) goto L_0x010d
            r13 = 1003(0x3eb, float:1.406E-42)
            if (r11 != r13) goto L_0x010f
        L_0x010d:
            r11 = 403(0x193, float:5.65E-43)
        L_0x010f:
            java.lang.String r13 = LOG_TAG
            int r15 = r0.mPhoneId
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r5 = "errorCode "
            r12.append(r5)
            r12.append(r10)
            java.lang.String r5 = " is converted to "
            r12.append(r5)
            r12.append(r11)
            java.lang.String r5 = r12.toString()
            com.sec.internal.log.IMSLog.e(r13, r15, r5)
            boolean r5 = r4.isChn()
            if (r5 == 0) goto L_0x0225
            boolean r5 = r0.mIsUtConnectionError
            r6 = 100
            r12 = 2
            if (r5 == 0) goto L_0x01f3
            if (r1 == 0) goto L_0x01f3
            r0.mIsUtConnectionError = r14
            java.lang.String r5 = LOG_TAG
            int r13 = r0.mPhoneId
            java.lang.String r15 = "UT connection failed."
            com.sec.internal.log.IMSLog.e(r5, r13, r15)
            r5 = 0
            int r13 = r0.mNafRetryCounter
            int r13 = r13 + r9
            if (r13 < r2) goto L_0x0154
            int r13 = r0.mBsfRetryCounter
            int r13 = r13 + r9
            if (r13 >= r3) goto L_0x0188
        L_0x0154:
            java.lang.String r13 = "failed to connect"
            boolean r15 = r1.contains(r13)
            if (r15 == 0) goto L_0x0170
            java.lang.String r15 = "xcap"
            boolean r15 = r1.contains(r15)
            if (r15 == 0) goto L_0x0170
            int r15 = r0.mNafRetryCounter
            int r14 = r15 + 1
            if (r14 >= r2) goto L_0x0170
            int r15 = r15 + r9
            r0.mNafRetryCounter = r15
            r5 = 1
            goto L_0x0188
        L_0x0170:
            boolean r13 = r1.contains(r13)
            if (r13 == 0) goto L_0x0188
            java.lang.String r13 = "bsf"
            boolean r13 = r1.contains(r13)
            if (r13 == 0) goto L_0x0188
            int r13 = r0.mBsfRetryCounter
            int r14 = r13 + 1
            if (r14 >= r3) goto L_0x0188
            int r13 = r13 + r9
            r0.mBsfRetryCounter = r13
            r5 = 1
        L_0x0188:
            if (r5 != 0) goto L_0x019b
            java.lang.String r13 = "timeout"
            boolean r13 = r1.contains(r13)
            if (r13 == 0) goto L_0x019b
            int r13 = r0.mUtHttpRetryCounter
            if (r13 >= r12) goto L_0x019b
            int r13 = r13 + r9
            r0.mUtHttpRetryCounter = r13
            r5 = 1
        L_0x019b:
            java.lang.String r9 = LOG_TAG
            int r12 = r0.mPhoneId
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            java.lang.String r14 = "errStr: "
            r13.append(r14)
            r13.append(r1)
            java.lang.String r14 = ", needRetry = "
            r13.append(r14)
            r13.append(r5)
            java.lang.String r14 = ", mNafRetryCounter: "
            r13.append(r14)
            int r14 = r0.mNafRetryCounter
            r13.append(r14)
            java.lang.String r14 = ", mBsfRetryCounter: "
            r13.append(r14)
            int r14 = r0.mBsfRetryCounter
            r13.append(r14)
            java.lang.String r14 = ", mUtHttpRetryCounter: "
            r13.append(r14)
            int r14 = r0.mUtHttpRetryCounter
            r13.append(r14)
            java.lang.String r13 = r13.toString()
            com.sec.internal.log.IMSLog.e(r9, r12, r13)
            if (r5 == 0) goto L_0x01f0
            r0.removeMessages(r8)
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r9 = r0.mThisSm
            r12 = 32500(0x7ef4, double:1.6057E-319)
            r9.sendMessageDelayed((int) r8, (int) r7, (long) r12)
            com.sec.internal.ims.servicemodules.ss.RequestState r7 = r0.mRequestState
            r0.transitionTo(r7)
            r7 = 100
            r0.sendMessageDelayed((int) r6, (long) r7)
            return
        L_0x01f0:
            r5 = 403(0x193, float:5.65E-43)
            goto L_0x0227
        L_0x01f3:
            boolean r5 = r0.mIsUtConnectionError
            if (r5 != 0) goto L_0x0222
            r5 = 403(0x193, float:5.65E-43)
            if (r10 == r5) goto L_0x0227
            int r13 = r0.mUtRetryCounter
            if (r13 >= r12) goto L_0x0227
            java.lang.String r5 = LOG_TAG
            int r12 = r0.mPhoneId
            java.lang.String r13 = "CHN operator UT failed, retry after 5s"
            com.sec.internal.log.IMSLog.e(r5, r12, r13)
            int r5 = r0.mUtRetryCounter
            int r5 = r5 + r9
            r0.mUtRetryCounter = r5
            r0.removeMessages(r8)
            com.sec.internal.ims.servicemodules.ss.UtStateMachine r5 = r0.mThisSm
            r12 = 32500(0x7ef4, double:1.6057E-319)
            r5.sendMessageDelayed((int) r8, (int) r7, (long) r12)
            com.sec.internal.ims.servicemodules.ss.RequestState r5 = r0.mRequestState
            r0.transitionTo(r5)
            r7 = 5000(0x1388, double:2.4703E-320)
            r0.sendMessageDelayed((int) r6, (long) r7)
            return
        L_0x0222:
            r5 = 403(0x193, float:5.65E-43)
            goto L_0x0227
        L_0x0225:
            r5 = 403(0x193, float:5.65E-43)
        L_0x0227:
            boolean r6 = r16.isPutRequest()
            if (r6 == 0) goto L_0x0239
            r6 = 0
            r0.mSeparatedMedia = r6
            r0.mSeparatedCfAll = r6
            r0.mSeparatedCFNRY = r6
            r0.mSeparatedCFNL = r6
            r6 = -1
            r0.mMainCondition = r6
        L_0x0239:
            android.os.Bundle r6 = new android.os.Bundle
            r6.<init>()
            if (r11 <= 0) goto L_0x0242
            r12 = r11
            goto L_0x0243
        L_0x0242:
            r12 = r5
        L_0x0243:
            java.lang.String r5 = "errorCode"
            r6.putInt(r5, r12)
            java.lang.String r5 = "originErrorCode"
            r6.putInt(r5, r10)
            if (r1 == 0) goto L_0x0266
            com.sec.internal.ims.servicemodules.ss.UtFeatureData r5 = r0.mFeature
            if (r5 == 0) goto L_0x0266
            boolean r5 = r5.isErrorMsgDisplay
            if (r5 == 0) goto L_0x0266
            com.sec.internal.ims.servicemodules.ss.UtXmlParse r5 = new com.sec.internal.ims.servicemodules.ss.UtXmlParse
            r5.<init>()
            java.lang.String r5 = r5.parseError(r1)
            java.lang.String r7 = "errorMsg"
            r6.putString(r7, r5)
        L_0x0266:
            r0.failUtRequest(r6)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtStateMachine.requestFailed(int, java.lang.String):void");
    }

    private void sendInit403MessageDelayed(String errStr, Mno mno) {
        if (mno == Mno.ORANGE) {
            sendMessageDelayed(14, 14400000);
        } else if (!TextUtils.isEmpty(errStr) && errStr.contains("10 minutes")) {
            sendMessageDelayed(14, 600000);
        }
    }

    /* access modifiers changed from: private */
    public void UpdateDnsInfo() {
        List<LinkAddress> linkAddrList;
        List<String> dnses = this.mPdnController.getDnsServers(this.mPdnListener);
        if (dnses == null || dnses.size() <= 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Dns Service List is null");
            sendMessage(obtainMessage(12, 1018));
            return;
        }
        try {
            this.mDnsAddresses.clear();
            for (String itdns : dnses) {
                this.mDnsAddresses.add(this.mNetwork.getByName(itdns));
            }
        } catch (UnknownHostException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "UnknownHostException");
        }
        LinkPropertiesWrapper lp = this.mPdnController.getLinkProperties(this.mPdnListener);
        if (lp != null && (linkAddrList = lp.getLinkAddresses()) != null) {
            for (LinkAddress la : linkAddrList) {
                InetAddress address = la.getAddress();
                if (address != null) {
                    try {
                        this.mLocalAddress = this.mNetwork.getByName(address.getHostAddress());
                    } catch (UnknownHostException e2) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean getUserSetToBoolean(int phoneId, String config) {
        return UserConfiguration.getUserConfig(this.mContext, phoneId, config, true);
    }

    /* access modifiers changed from: protected */
    public int getUserSetToInt(int phoneId, String config, int defaultVal) {
        return UserConfiguration.getUserConfig(this.mContext, phoneId, config, defaultVal);
    }

    /* access modifiers changed from: protected */
    public void setUserSet(int phoneId, String config, int val) {
        UserConfiguration.setUserConfig(this.mContext, phoneId, config, val);
    }

    /* access modifiers changed from: protected */
    public void setUserSet(int phoneId, String config, boolean val) {
        UserConfiguration.setUserConfig(this.mContext, phoneId, config, val);
    }

    public boolean hasProfile() {
        if (this.mProfile != null) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "mProfile is null. so ignore");
        return false;
    }

    private void printCompleteLog(Bundle[] response, int requestType, int requestId) {
        String log = "ImsUt[" + requestId + "]< " + UtLog.extractLogFromResponse(requestType, response);
        IMSLog.i(LOG_TAG, this.mPhoneId, log);
        this.mUtServiceModule.writeDump(this.mPhoneId, log);
        IMSLog.c(LogClass.UT_RESPONSE, this.mPhoneId + "," + requestId + ",<,T" + UtLog.extractCrLogFromResponse(requestType, response));
    }

    private void printFailLog(Bundle error, int requestType, int requestId) {
        String log = "ImsUt[" + requestId + "]< [!ERROR]" + UtLog.extractLogFromError(requestType, error);
        IMSLog.i(LOG_TAG, this.mPhoneId, log);
        this.mUtServiceModule.writeDump(this.mPhoneId, log);
        IMSLog.c(LogClass.UT_RESPONSE, this.mPhoneId + "," + requestId + ",<,F," + error.getInt("originErrorCode") + "," + error.getInt("errorCode"));
    }
}
