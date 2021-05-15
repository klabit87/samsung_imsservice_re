package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.config.ConfigProvider;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkflowCmcc extends WorkflowBase {
    protected static final int AUTO_CONFIG_MAX_FLOWCOUNT = 20;
    public static final String INTENT_ACTION_RCS_ENABLE = "android.intent.action.RCS_ENABLE";
    public static final String INTENT_PARAM_RCS_ENABLE = "RCS_ENABLE";
    public static final String INTENT_PARAM_RCS_ENABLE_TYPE = "action_type";
    public static final String INTENT_VALUE_RCS_ENABLE_TYPE_ALL_RCS = "ALL_RCS";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = WorkflowCmcc.class.getSimpleName();
    protected static final int MAX_SERVER_COUNT = ConfigConstants.APPID_MAP.size();
    protected boolean hasNotified = false;
    /* access modifiers changed from: private */
    public boolean isWiFiAutoConfig = false;
    protected int mHttpResult = 0;
    protected boolean mIsReceicedXml;
    protected int mMinValidity = Integer.MAX_VALUE;
    protected List<ServerInfo> mNewServerInfoList = new ArrayList();
    protected List<ServerInfo> mOldServerInfoList = new ArrayList();
    protected int mServerCount;
    protected int mServerId = 0;
    protected List<SharedInfo> mSharedInfoList;
    /* access modifiers changed from: private */
    public boolean requestOtpStep = false;

    private class ServerInfo {
        public List<String> appIdList;
        public String fqdn;

        private ServerInfo() {
            this.appIdList = new ArrayList();
        }

        /* synthetic */ ServerInfo(WorkflowCmcc x0, AnonymousClass1 x1) {
            this();
        }
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowCmcc(android.os.Looper r16, android.content.Context r17, android.os.Handler r18, com.sec.internal.constants.Mno r19, int r20) {
        /*
            r15 = this;
            r11 = r15
            r12 = r17
            r13 = r18
            r14 = r20
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceCmcc r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceCmcc
            r5.<init>(r12, r13, r14)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterCmcc r7 = new com.sec.internal.ims.config.adapters.HttpAdapterCmcc
            r7.<init>(r14)
            com.sec.internal.ims.config.adapters.XmlParserAdapterMultipleServer r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapterMultipleServer
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r9.<init>(r12, r13)
            r0 = r15
            r1 = r16
            r2 = r17
            r3 = r18
            r4 = r19
            r10 = r20
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            r0 = 0
            r11.requestOtpStep = r0
            r11.isWiFiAutoConfig = r0
            r11.mHttpResult = r0
            r1 = 2147483647(0x7fffffff, float:NaN)
            r11.mMinValidity = r1
            r11.hasNotified = r0
            r11.mServerId = r0
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            r11.mNewServerInfoList = r1
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            r11.mOldServerInfoList = r1
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            r11.mSharedInfoList = r1
            r11.mIsReceicedXml = r0
            r11.mServerCount = r0
            com.sec.internal.ims.config.SharedInfo r0 = r11.mSharedInfo
            r1.add(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowCmcc.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        Log.i(str, "handleMessage: " + msg.what);
        int i = msg.what;
        if (i == 0) {
            resetStorage();
            this.mStartForce = true;
        } else if (i != 1) {
            super.handleMessage(msg);
            return;
        }
        if (this.sIsConfigOngoing) {
            Log.i(LOG_TAG, "AutoConfig: ongoing");
            return;
        }
        this.sIsConfigOngoing = true;
        Log.i(LOG_TAG, "AutoConfig: start");
        this.mModuleHandler.removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mPowerController.lock();
        if (this.mServerId == 0) {
            ArrayList arrayList = new ArrayList();
            this.mOldServerInfoList = arrayList;
            if (setDefaultServerInfo(arrayList)) {
                setAdditionalServerInfo(this.mOldServerInfoList);
            }
        }
        int oldVersion = getVersion();
        if (needScheduleAutoconfig(this.mPhoneId)) {
            scheduleAutoconfig(oldVersion);
        }
        int newVersion = getVersion();
        boolean hasAdditionalServer = handleAdditionalServer();
        String str2 = LOG_TAG;
        Log.i(str2, "mIsReceicedXml: " + this.mIsReceicedXml);
        this.sIsConfigOngoing = false;
        String str3 = LOG_TAG;
        Log.i(str3, "oldVersion: " + oldVersion + " newVersion: " + newVersion + " next serverID: " + this.mServerId);
        if (!hasAdditionalServer) {
            Log.i(LOG_TAG, "AutoConfig: finish");
            setCompleted(true);
            this.mModuleHandler.sendMessage(obtainMessage(3, oldVersion, newVersion, Integer.valueOf(this.mPhoneId)));
            this.hasNotified = false;
            this.mStartForce = false;
            this.mPowerController.release();
            return;
        }
        if (newVersion <= 0 && !this.hasNotified) {
            Log.i(LOG_TAG, "Notifying ConfigModule");
            this.mModuleHandler.sendMessage(obtainMessage(3, oldVersion, newVersion, Integer.valueOf(this.mPhoneId)));
            this.hasNotified = true;
        }
        sendEmptyMessage(1);
    }

    /* access modifiers changed from: protected */
    public boolean handleAdditionalServer() {
        boolean hasAdditionalServer;
        int i = this.mServerId;
        if (i == 0) {
            ArrayList arrayList = new ArrayList();
            this.mNewServerInfoList = arrayList;
            hasAdditionalServer = setDefaultServerInfo(arrayList);
            if (hasAdditionalServer) {
                Log.i(LOG_TAG, "handleAdditionalServer: Access-control present");
                setAdditionalServerInfo(this.mNewServerInfoList);
                this.mServerCount = this.mNewServerInfoList.size();
            }
            updateTables();
        } else {
            hasAdditionalServer = i < this.mServerCount - 1;
        }
        if (hasAdditionalServer) {
            Log.i(LOG_TAG, "handleAdditionalServer: updating storage");
            this.mServerId++;
            this.mStorage.close();
            this.mStorage.open(this.mContext, ConfigProvider.CONFIG_DB_NAME_PREFIX + HashManager.generateMD5(this.mIdentity) + "_" + this.mServerId, this.mPhoneId);
            if (this.mServerId < this.mSharedInfoList.size()) {
                this.mSharedInfo = this.mSharedInfoList.get(this.mServerId);
            } else {
                this.mSharedInfo = new SharedInfo(this.mContext, this.mSm, this.mRcsProfile, this.mRcsVersion, this.mClientPlatform, this.mClientVersion);
                this.mSharedInfoList.add(this.mSharedInfo);
            }
        } else {
            this.mServerId = 0;
            this.mStorage.close();
            this.mStorage.open(this.mContext, ConfigProvider.CONFIG_DB_NAME_PREFIX + HashManager.generateMD5(this.mIdentity), this.mPhoneId);
        }
        Log.i(LOG_TAG, "hasAdditionalServer: " + hasAdditionalServer);
        return hasAdditionalServer;
    }

    /* access modifiers changed from: protected */
    public boolean setDefaultServerInfo(List<ServerInfo> serverInfoList) {
        if (this.mStorage.read("root/access-control/server/0/app-id/0") == null) {
            return false;
        }
        ServerInfo serverInfo = new ServerInfo(this, (AnonymousClass1) null);
        for (int i = 0; i < MAX_SERVER_COUNT; i++) {
            IStorageAdapter iStorageAdapter = this.mStorage;
            String appId = iStorageAdapter.read("root/access-control/default/app-id/" + i);
            if (appId == null) {
                break;
            }
            serverInfo.appIdList.add(appId);
        }
        serverInfoList.add(serverInfo);
        return true;
    }

    /* access modifiers changed from: protected */
    public void setAdditionalServerInfo(List<ServerInfo> serverInfoList) {
        int i = 0;
        while (i < MAX_SERVER_COUNT) {
            IStorageAdapter iStorageAdapter = this.mStorage;
            String fqdn = iStorageAdapter.read("root/access-control/server/" + i + "/fqdn");
            if (fqdn != null) {
                ServerInfo serverInfo = new ServerInfo(this, (AnonymousClass1) null);
                serverInfo.fqdn = fqdn;
                for (int j = 0; j < MAX_SERVER_COUNT; j++) {
                    IStorageAdapter iStorageAdapter2 = this.mStorage;
                    String appId = iStorageAdapter2.read("root/access-control/server/" + i + "/app-id/" + j);
                    if (appId == null) {
                        break;
                    }
                    serverInfo.appIdList.add(appId);
                }
                serverInfoList.add(serverInfo);
                i++;
            } else {
                return;
            }
        }
    }

    private void updateTables() {
        String str = LOG_TAG;
        Log.i(str, "updateTables: mOldServerInfoList.size() " + this.mOldServerInfoList.size() + " mNewServerInfoList.size() " + this.mNewServerInfoList.size());
        for (int i = 1; i < this.mOldServerInfoList.size(); i++) {
            if ((i < this.mNewServerInfoList.size() && !this.mNewServerInfoList.get(i).fqdn.equals(this.mOldServerInfoList.get(i).fqdn)) || i >= this.mNewServerInfoList.size()) {
                String str2 = LOG_TAG;
                Log.i(str2, "updateTables: delete table " + i);
                this.mStorage.close();
                IStorageAdapter iStorageAdapter = this.mStorage;
                Context context = this.mContext;
                iStorageAdapter.open(context, ConfigProvider.CONFIG_DB_NAME_PREFIX + HashManager.generateMD5(this.mIdentity) + "_" + i, this.mPhoneId);
                this.mStorage.deleteAll();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow next = new Initialize(this, (AnonymousClass1) null);
        int count = 20;
        while (next != null && count > 0) {
            try {
                next = next.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException occur:" + e.getMessage());
                Log.i(LOG_TAG, "wait 10 sec. and retry");
                sleep(10000);
                next = new Initialize(this, (AnonymousClass1) null);
                e.printStackTrace();
            } catch (UnknownStatusException e2) {
                Log.i(LOG_TAG, "UnknownStatusException occur:" + e2.getMessage());
                Log.i(LOG_TAG, "wait 2 sec. and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                next = new Initialize(this, (AnonymousClass1) null);
                e2.printStackTrace();
            } catch (SQLiteFullException e3) {
                Log.i(LOG_TAG, "SQLiteFullException occur:" + e3.getMessage());
                Log.i(LOG_TAG, "finish workflow");
                next = new Finish(this, (AnonymousClass1) null);
                e3.printStackTrace();
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(LOG_TAG, "unknown exception occur:" + e4.getMessage());
                }
                Log.i(LOG_TAG, "wait 1 sec. and retry");
                sleep(1000);
                next = new Initialize(this, (AnonymousClass1) null);
                e4.printStackTrace();
            }
            count--;
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        return null;
    }

    private class Initialize implements WorkflowBase.Workflow {
        private Initialize() {
        }

        /* synthetic */ Initialize(WorkflowCmcc x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowBase.Workflow next = null;
            if (WorkflowCmcc.this.mHttpRedirect) {
                if (WorkflowCmcc.this.mSharedInfo.getUrl() == null) {
                    if (WorkflowCmcc.this.mServerId != 0) {
                        WorkflowCmcc.this.mSharedInfo.setUrl(WorkflowCmcc.this.mParamHandler.initUrl(WorkflowCmcc.this.mNewServerInfoList.get(WorkflowCmcc.this.mServerId).fqdn));
                    } else {
                        WorkflowCmcc.this.mSharedInfo.setUrl(WorkflowCmcc.this.mParamHandler.initUrl(""));
                    }
                }
                WorkflowCmcc.this.mHttpRedirect = false;
            } else if (WorkflowCmcc.this.mServerId != 0) {
                WorkflowCmcc.this.mSharedInfo.setUrl(WorkflowCmcc.this.mParamHandler.initUrl(WorkflowCmcc.this.mNewServerInfoList.get(WorkflowCmcc.this.mServerId).fqdn));
            } else {
                WorkflowCmcc.this.mSharedInfo.setUrl(WorkflowCmcc.this.mParamHandler.initUrl(""));
            }
            WorkflowCmcc.this.mCookieHandler.clearCookie();
            if (WorkflowCmcc.this.mStartForce) {
                next = new FetchHttp(WorkflowCmcc.this, (AnonymousClass1) null);
            } else {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowCmcc.this.getOpMode().ordinal()];
                if (i == 1 || i == 2 || i == 3) {
                    next = new FetchHttp(WorkflowCmcc.this, (AnonymousClass1) null);
                } else if (i == 4 || i == 5) {
                    next = new Finish(WorkflowCmcc.this, (AnonymousClass1) null);
                }
            }
            if (!(next instanceof FetchHttp) || WorkflowCmcc.this.mMobileNetwork) {
                return next;
            }
            Log.i(WorkflowCmcc.LOG_TAG, "now use wifi. try non-ps step directly.");
            return new FetchHttps(WorkflowCmcc.this, (AnonymousClass1) null);
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowCmcc$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        static {
            int[] iArr = new int[WorkflowBase.OpMode.values().length];
            $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode = iArr;
            try {
                iArr[WorkflowBase.OpMode.ACTIVE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_TEMPORARY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DORMANT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_PERMANENTLY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private class FetchHttp implements WorkflowBase.Workflow {
        private FetchHttp() {
        }

        /* synthetic */ FetchHttp(WorkflowCmcc x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            int subId = SimUtil.getSubId(WorkflowCmcc.this.mPhoneId);
            String access$400 = WorkflowCmcc.LOG_TAG;
            Log.i(access$400, "FetchHttp:run() mPhoneId: " + WorkflowCmcc.this.mPhoneId + " subId:" + subId);
            WorkflowCmcc.this.mSharedInfo.setUserImsi(WorkflowCmcc.this.mTelephony.getSubscriberId(subId));
            if (!WorkflowCmcc.this.mMobileNetwork) {
                WorkflowCmcc.this.mSharedInfo.setHttpsCMCC();
                return new FetchHttps(WorkflowCmcc.this, (AnonymousClass1) null);
            }
            WorkflowCmcc.this.mSharedInfo.setHttpCMCC();
            WorkflowCmcc.this.mSharedInfo.setHttpResponse(WorkflowCmcc.this.getHttpResponse());
            WorkflowCmcc workflowCmcc = WorkflowCmcc.this;
            workflowCmcc.mHttpResult = workflowCmcc.mSharedInfo.getHttpResponse().getStatusCode();
            if (WorkflowCmcc.this.mHttpResult == 200 || WorkflowCmcc.this.mHttpResult == 511) {
                return new FetchHttps(WorkflowCmcc.this, (AnonymousClass1) null);
            }
            WorkflowCmcc workflowCmcc2 = WorkflowCmcc.this;
            return workflowCmcc2.handleResponse2(new Initialize(workflowCmcc2, (AnonymousClass1) null), new FetchHttps(WorkflowCmcc.this, (AnonymousClass1) null), new Finish(WorkflowCmcc.this, (AnonymousClass1) null));
        }
    }

    private class FetchHttps implements WorkflowBase.Workflow {
        private FetchHttps() {
        }

        /* synthetic */ FetchHttps(WorkflowCmcc x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            int subId = SimUtil.getSubId(WorkflowCmcc.this.mPhoneId);
            String access$400 = WorkflowCmcc.LOG_TAG;
            Log.i(access$400, "FetchHttps() mPhoneId: " + WorkflowCmcc.this.mPhoneId + " subId:" + subId);
            if (WorkflowCmcc.this.mParamHandler.isConfigProxy()) {
                String access$4002 = WorkflowCmcc.LOG_TAG;
                Log.i(access$4002, "FetchHttps() fake server, use http mPhoneId: " + WorkflowCmcc.this.mPhoneId + " subId:" + subId);
                WorkflowCmcc.this.mSharedInfo.setHttpCMCC();
            } else {
                String access$4003 = WorkflowCmcc.LOG_TAG;
                Log.i(access$4003, "FetchHttps() auto config server, use http mPhoneId: " + WorkflowCmcc.this.mPhoneId + " subId:" + subId);
                WorkflowCmcc.this.mSharedInfo.setHttpsCMCC();
            }
            WorkflowCmcc.this.mCookieHandler.handleCookie(WorkflowCmcc.this.mSharedInfo.getHttpResponse());
            WorkflowCmcc.this.mSharedInfo.addHttpParam("vers", String.valueOf(WorkflowCmcc.this.getVersion()));
            WorkflowCmcc.this.mSharedInfo.addHttpParam("IMSI", WorkflowCmcc.this.mTelephony.getSubscriberId(subId));
            WorkflowCmcc.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowCmcc.this.mTelephony.getDeviceId(WorkflowCmcc.this.mPhoneId));
            WorkflowCmcc.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.PVALUE.TERMINAL_MODEL);
            WorkflowCmcc.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, "1");
            if (ImsProfile.isRcsUpProfile(WorkflowCmcc.this.mRcsProfile)) {
                WorkflowCmcc.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, ConfigConstants.PVALUE.PROVISIONING_VERSION_5_0);
            }
            if (!WorkflowCmcc.this.mMobileNetwork || WorkflowCmcc.this.mHttpResult == 511) {
                if (!TextUtils.isEmpty(WorkflowCmcc.this.mTelephony.getMsisdn(subId))) {
                    WorkflowCmcc.this.mSharedInfo.addHttpParam("msisdn", WorkflowCmcc.this.mParamHandler.encodeRFC3986(WorkflowCmcc.this.mTelephony.getMsisdn(subId)));
                }
                if (!TextUtils.isEmpty(WorkflowCmcc.this.mSharedInfo.getUserMsisdn())) {
                    WorkflowCmcc.this.mSharedInfo.addHttpParam("msisdn", WorkflowCmcc.this.mParamHandler.encodeRFC3986(WorkflowCmcc.this.mSharedInfo.getUserMsisdn()));
                }
                WorkflowCmcc.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, WorkflowCmcc.this.mTelephony.getSmsDestPort());
                WorkflowCmcc.this.mSharedInfo.addHttpParam("token", WorkflowCmcc.this.getToken());
            }
            WorkflowCmcc.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
            WorkflowCmcc.this.mSharedInfo.addHttpParam("terminal_sw_version", WorkflowCmcc.this.mParamHandler.getModelInfoFromBuildVersion(ConfigConstants.PVALUE.TERMINAL_MODEL, ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 10, false));
            if (WorkflowCmcc.this.mStartForce) {
                WorkflowCmcc.this.mSharedInfo.addHttpParam("vers", "0");
            }
            if (WorkflowCmcc.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                String access$4004 = WorkflowCmcc.LOG_TAG;
                Log.i(access$4004, "DORMANT mode. use backup version :" + WorkflowCmcc.this.getVersionBackup());
                WorkflowCmcc workflowCmcc = WorkflowCmcc.this;
                workflowCmcc.addEventLog(WorkflowCmcc.LOG_TAG + "DORMANT mode. use backup version :" + WorkflowCmcc.this.getVersionBackup());
                WorkflowCmcc.this.mSharedInfo.addHttpParam("vers", WorkflowCmcc.this.getVersionBackup());
            }
            WorkflowCmcc.this.mSharedInfo.setHttpResponse(WorkflowCmcc.this.getHttpResponse());
            if (WorkflowCmcc.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                Log.i(WorkflowCmcc.LOG_TAG, "200 OK received. try parsing");
                if (WorkflowCmcc.this.isWiFiAutoConfig) {
                    Log.i(WorkflowCmcc.LOG_TAG, "isWiFiAutoConfig is true");
                    WifiManager wm = (WifiManager) WorkflowCmcc.this.mContext.getSystemService("wifi");
                    if (wm != null) {
                        wm.setWifiEnabled(true);
                        Log.i(WorkflowCmcc.LOG_TAG, "WifiManager.setWifiEnabled: true");
                        WorkflowCmcc workflowCmcc2 = WorkflowCmcc.this;
                        workflowCmcc2.addEventLog(WorkflowCmcc.LOG_TAG + "WifiManager.setWifiEnabled: true");
                    }
                    boolean unused = WorkflowCmcc.this.isWiFiAutoConfig = false;
                }
                return new Parse(WorkflowCmcc.this, (AnonymousClass1) null);
            } else if (WorkflowCmcc.this.mSharedInfo.getHttpResponse().getStatusCode() == 403) {
                return new Finish(WorkflowCmcc.this, (AnonymousClass1) null);
            } else {
                if (WorkflowCmcc.this.mSharedInfo.getHttpResponse().getStatusCode() != 0) {
                    WorkflowCmcc workflowCmcc3 = WorkflowCmcc.this;
                    return workflowCmcc3.handleResponse2(new Initialize(workflowCmcc3, (AnonymousClass1) null), new FetchHttps(), new Finish(WorkflowCmcc.this, (AnonymousClass1) null));
                }
                Log.i(WorkflowCmcc.LOG_TAG, "RCS configuration server is unreachable. retry max times");
                throw new UnknownStatusException("RCS configuration server is unreachable");
            }
        }
    }

    private class FetchOtp implements WorkflowBase.Workflow {
        private FetchOtp() {
        }

        /* synthetic */ FetchOtp(WorkflowCmcc x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowCmcc.this.mSharedInfo.setHttpClean();
            WorkflowCmcc.this.mCookieHandler.handleCookie(WorkflowCmcc.this.mSharedInfo.getHttpResponse());
            WorkflowCmcc.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VENDOR, ConfigConstants.PVALUE.CLIENT_VENDOR);
            SharedInfo sharedInfo = WorkflowCmcc.this.mSharedInfo;
            sharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VERSION, WorkflowCmcc.this.mClientPlatform + WorkflowCmcc.this.mClientVersion);
            WorkflowCmcc.this.mSharedInfo.addHttpParam("IMSI", WorkflowCmcc.this.mTelephony.getImsi());
            WorkflowCmcc.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowCmcc.this.mTelephony.getImei());
            WorkflowCmcc.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
            WorkflowCmcc.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.PVALUE.TERMINAL_MODEL);
            WorkflowCmcc.this.mSharedInfo.addHttpParam("terminal_sw_version", WorkflowCmcc.this.mParamHandler.getModelInfoFromBuildVersion(ConfigConstants.PVALUE.TERMINAL_MODEL, ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 10, false));
            WorkflowCmcc.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, WorkflowCmcc.this.mSharedInfo.getOtp());
            WorkflowCmcc.this.mSharedInfo.setHttpResponse(WorkflowCmcc.this.getHttpResponse());
            if (WorkflowCmcc.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                return new Parse(WorkflowCmcc.this, (AnonymousClass1) null);
            }
            WorkflowCmcc workflowCmcc = WorkflowCmcc.this;
            return workflowCmcc.handleResponse2(new Initialize(workflowCmcc, (AnonymousClass1) null), new FetchHttps(WorkflowCmcc.this, (AnonymousClass1) null), new Finish(WorkflowCmcc.this, (AnonymousClass1) null));
        }
    }

    private class Authorize implements WorkflowBase.Workflow {
        private Authorize() {
        }

        /* synthetic */ Authorize(WorkflowCmcc x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            Log.i(WorkflowCmcc.LOG_TAG, "get OTP & save it to shared info");
            WorkflowCmcc.this.mPowerController.release();
            boolean unused = WorkflowCmcc.this.requestOtpStep = false;
            String otp = WorkflowCmcc.this.mTelephony.getOtp();
            if (otp == null) {
                WorkflowCmcc.this.setValidityTimer(0);
                return new Finish(WorkflowCmcc.this, (AnonymousClass1) null);
            }
            WorkflowCmcc.this.mSharedInfo.setOtp(otp);
            WorkflowCmcc.this.mPowerController.lock();
            return new FetchOtp(WorkflowCmcc.this, (AnonymousClass1) null);
        }
    }

    private class Parse implements WorkflowBase.Workflow {
        private Parse() {
        }

        /* synthetic */ Parse(WorkflowCmcc x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            byte[] body = WorkflowCmcc.this.mSharedInfo.getHttpResponse().getBody();
            if (body == null) {
                body = "".getBytes();
            }
            if (Build.IS_DEBUGGABLE) {
                Util.saveFiletoPath(body, WorkflowCmcc.this.mContext.getExternalCacheDir().getAbsolutePath() + "/AutoConfigFromServer.xml");
            }
            Map<String, String> parsedXml = WorkflowCmcc.this.mXmlParser.parse(new String(body, "utf-8"));
            if (parsedXml != null) {
                String access$400 = WorkflowCmcc.LOG_TAG;
                Log.i(access$400, "requestOtpStep: " + WorkflowCmcc.this.requestOtpStep);
                if (parsedXml.get("root/vers/version") == null || parsedXml.get("root/vers/validity") == null) {
                    Log.i(WorkflowCmcc.LOG_TAG, "config xml must contain atleast 2 items(version & validity).");
                    if (WorkflowCmcc.this.mCookieHandler.isCookie(WorkflowCmcc.this.mSharedInfo.getHttpResponse())) {
                        return new Authorize(WorkflowCmcc.this, (AnonymousClass1) null);
                    }
                    throw new UnknownStatusException("no body & no cookie. something wrong");
                } else if (WorkflowCmcc.this.requestOtpStep && WorkflowCmcc.this.mSharedInfo.getOtp() == null && WorkflowCmcc.this.mSharedInfo.getUserMsisdn() != null && WorkflowCmcc.this.mCookieHandler.isCookie(WorkflowCmcc.this.mSharedInfo.getHttpResponse())) {
                    return new Authorize(WorkflowCmcc.this, (AnonymousClass1) null);
                } else {
                    WorkflowCmcc.this.mSharedInfo.setParsedXml(parsedXml);
                    return new Store(WorkflowCmcc.this, (AnonymousClass1) null);
                }
            } else {
                throw new InvalidXmlException("no parsed xml data.");
            }
        }
    }

    private class Store implements WorkflowBase.Workflow {
        private Store() {
        }

        /* synthetic */ Store(WorkflowCmcc x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (!TextUtils.isEmpty(WorkflowCmcc.this.mSharedInfo.getUserMsisdn())) {
                WorkflowCmcc workflowCmcc = WorkflowCmcc.this;
                if (workflowCmcc.getVersion(workflowCmcc.mSharedInfo.getParsedXml()) == 0) {
                    Log.i(WorkflowCmcc.LOG_TAG, "version is 0. need to be retry");
                    WorkflowCmcc.this.setValidityTimer(300);
                    return new Finish(WorkflowCmcc.this, (AnonymousClass1) null);
                }
            }
            boolean userAccept = WorkflowCmcc.this.mParamHandler.getUserAccept(WorkflowCmcc.this.mSharedInfo.getParsedXml());
            WorkflowCmcc.this.mParamHandler.setOpModeWithUserAccept(userAccept, WorkflowCmcc.this.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE_TEMPORARY);
            if (!userAccept) {
                WorkflowCmcc.this.enableRcs(false);
            }
            if (WorkflowCmcc.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                WorkflowCmcc workflowCmcc2 = WorkflowCmcc.this;
                workflowCmcc2.setValidityTimer(workflowCmcc2.getValidity());
            }
            return new Finish(WorkflowCmcc.this, (AnonymousClass1) null);
        }
    }

    private class Finish implements WorkflowBase.Workflow {
        private Finish() {
        }

        /* synthetic */ Finish(WorkflowCmcc x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowCmcc.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowCmcc workflowCmcc = WorkflowCmcc.this;
                workflowCmcc.setLastErrorCode(workflowCmcc.mSharedInfo.getHttpResponse().getStatusCode());
            }
            Log.i(WorkflowCmcc.LOG_TAG, "all workflow finished");
            WorkflowCmcc.this.createSharedInfo();
            return null;
        }
    }

    public void enableRcs(boolean enable) {
        String str = LOG_TAG;
        Log.i(str, "enableRcs: " + enable);
        if (this.mContext != null) {
            Intent intent = new Intent(INTENT_ACTION_RCS_ENABLE);
            intent.putExtra(INTENT_PARAM_RCS_ENABLE_TYPE, INTENT_VALUE_RCS_ENABLE_TYPE_ALL_RCS);
            intent.putExtra(INTENT_PARAM_RCS_ENABLE, enable);
            this.mContext.sendBroadcast(intent);
            Log.i(LOG_TAG, "enableRcs: Intent has been transmitted sucessfully !!");
        }
    }

    /* access modifiers changed from: protected */
    public void setValidityTimer(int validityPeriod) {
        String str = LOG_TAG;
        Log.i(str, "setValidityTimer: validityPeriod:" + validityPeriod + " mMinValidity:" + this.mMinValidity);
        if (validityPeriod <= this.mMinValidity) {
            this.mMinValidity = validityPeriod;
            super.setValidityTimer(validityPeriod);
        }
    }
}
