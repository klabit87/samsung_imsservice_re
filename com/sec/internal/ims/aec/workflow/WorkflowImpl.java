package com.sec.internal.ims.aec.workflow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.aec.persist.AECStorage;
import com.sec.internal.ims.aec.receiver.fcm.FcmIntentService;
import com.sec.internal.ims.aec.util.CalcEapAka;
import com.sec.internal.ims.aec.util.DataConnectivity;
import com.sec.internal.ims.aec.util.HttpClient;
import com.sec.internal.ims.aec.util.HttpStore;
import com.sec.internal.ims.aec.util.PowerController;
import com.sec.internal.ims.aec.util.PsDataOffExempt;
import com.sec.internal.ims.aec.util.URLExtractor;
import com.sec.internal.ims.aec.util.ValidityTimer;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.interfaces.ims.aec.IWorkflowImpl;
import com.sec.internal.log.AECLog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class WorkflowImpl extends Handler implements IWorkflowImpl {
    private static final int TIMEOUT_PUSH_MSG = 30000;
    private static final int TIMEOUT_WAKELOCK = 90000;
    private final String LOG_TAG;
    protected AECStorage mAECJar;
    protected CalcEapAka mCalcEapAka;
    private final Context mContext;
    protected SimpleEventLog mEventLog;
    private HttpClient mHttpClient;
    protected HttpStore mHttpJar;
    private boolean mIsEntitlementOngoing = false;
    private boolean mIsPsDataRoaming = false;
    private boolean mIsReadyToNotifyApp = false;
    private boolean mIsSharedAkaToken = false;
    private boolean mIsValidEntitlement = false;
    private final Handler mModuleHandler;
    protected String mNotifState = "NOT_READY";
    protected int mPhoneId = 0;
    protected PowerController mPowerCtrl;
    private PsDataOffExempt mPsDataOffExempt;
    private ValidityTimer mValidityTimer;

    protected interface Workflow {
        Workflow run() throws Exception;
    }

    /* access modifiers changed from: package-private */
    public abstract void doWorkflow();

    /* access modifiers changed from: package-private */
    public abstract Workflow handleNotOkResponse(int i);

    WorkflowImpl(Context context, Looper looper, Handler moduleHandler, String logTag) {
        super(looper);
        this.mContext = context;
        this.mModuleHandler = moduleHandler;
        this.LOG_TAG = logTag;
    }

    public void dump() {
        this.mEventLog.dump();
    }

    public void initWorkflow(int phoneId, String imsi, String mno) {
        this.mPhoneId = phoneId;
        this.mAECJar = new AECStorage(this.mContext, phoneId, mno);
        this.mCalcEapAka = new CalcEapAka(this.mPhoneId, imsi);
        this.mEventLog = new SimpleEventLog(this.mContext, this.LOG_TAG, 20);
        this.mHttpClient = new HttpClient(this.mPhoneId);
        this.mHttpJar = new HttpStore(this.mContext, this.mPhoneId);
        this.mPowerCtrl = new PowerController(this.mContext, this.mPhoneId);
        this.mPsDataOffExempt = new PsDataOffExempt(this.mContext, this.mPhoneId, this);
        this.mValidityTimer = new ValidityTimer(this.mContext, this.mPhoneId, this);
        checkSimSwapped(imsi);
    }

    private void checkSimSwapped(String currentImsi) {
        if (currentImsi.equals(this.mAECJar.getImsi())) {
            AECLog.i(this.LOG_TAG, "identical sim, recover to the stored configuration", this.mPhoneId);
        } else {
            this.mAECJar.setDefaultValues("0");
            this.mAECJar.setHttpResponse(0);
            AECLog.i(this.LOG_TAG, "sim swapped, revert to the default configuration", this.mPhoneId);
        }
        this.mAECJar.setImsi(currentImsi);
        this.mModuleHandler.sendMessage(obtainMessage(5, this.mPhoneId, this.mAECJar.getHttpResponse(), this.mAECJar.getStoredConfiguration()));
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1001:
                onStartWorkFlow();
                return;
            case 1002:
                onStopWorkflow(msg);
                return;
            case 1003:
                onCompletedWorkFlow();
                return;
            case 1004:
                onWaitEapAkaResp();
                return;
            case 1005:
                onCompletedEapChallengeResp(msg);
                return;
            case 1006:
                onRequestFcmToken();
                return;
            case 1007:
                this.mPsDataOffExempt.requestNetwork();
                return;
            case 1008:
                performEntitlement((Object) null);
                return;
            case 1009:
                this.mPsDataOffExempt.unregisterNetworkCallback();
                return;
            case 1010:
                setValidEntitlement(false);
                performEntitlement((Object) null);
                return;
            case 1011:
                clearAkaToken();
                return;
            default:
                return;
        }
    }

    private void onStartWorkFlow() {
        if (this.mIsValidEntitlement) {
            AECLog.i(this.LOG_TAG, "onStartWorkFlow: entitlement is valid", this.mPhoneId);
        } else if (DataConnectivity.isDataConnected(this.mContext, this.mPsDataOffExempt.isConnected())) {
            String str = this.LOG_TAG;
            AECLog.i(str, "onStartWorkFlow: " + this.mAECJar.getVersion(), this.mPhoneId);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.add("onStartWorkFlow: " + this.mAECJar.getVersion());
            this.mIsEntitlementOngoing = true;
            this.mAECJar.setHttpResponse(0);
            requestEntitlement(this.mAECJar.getVersion());
        }
    }

    private void onStopWorkflow(Message msg) {
        AECLog.i(this.LOG_TAG, "onStopWorkflow", this.mPhoneId);
        this.mIsEntitlementOngoing = false;
        this.mValidityTimer.stopTokenValidityTimer();
        this.mValidityTimer.stopVersionValidityTimer();
        if (this.mPsDataOffExempt.isConnected()) {
            sendEmptyMessage(1009);
        }
        if (msg != null) {
            this.mModuleHandler.sendMessage(obtainMessage(6, msg.arg1, msg.arg2));
        }
    }

    private void onCompletedWorkFlow() {
        this.mIsEntitlementOngoing = false;
        this.mValidityTimer.stopVersionValidityTimer();
        this.mValidityTimer.stopTokenValidityTimer();
        if (this.mPsDataOffExempt.isConnected()) {
            sendEmptyMessage(1009);
        }
        if (this.mAECJar.getVersion() > 0) {
            if (this.mAECJar.getHttpResponse() == 200) {
                setValidEntitlement(true);
                this.mValidityTimer.startVersionValidityTimer(this.mAECJar.getVersionValidity());
                this.mValidityTimer.startTokenValidityTimer(this.mAECJar.getTokenValidity());
            } else if (this.mAECJar.getHttpResponse() == 403) {
                this.mAECJar.setDefaultValues("0");
            }
        } else if (this.mAECJar.getVersion() == 0) {
            this.mAECJar.setDefaultValues("0");
        } else if (this.mAECJar.getVersion() < 0) {
            AECStorage aECStorage = this.mAECJar;
            aECStorage.setDefaultValues(Integer.toString(aECStorage.getVersion()));
        }
        Bundle bundle = this.mAECJar.getStoredConfiguration();
        String str = this.LOG_TAG;
        AECLog.i(str, "onCompletedWorkFlow: " + bundle.toString(), this.mPhoneId);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("onCompletedWorkFlow: " + bundle.toString());
        this.mModuleHandler.sendMessage(obtainMessage(5, this.mPhoneId, this.mAECJar.getHttpResponse(), bundle));
    }

    private void onWaitEapAkaResp() {
        AECLog.i(this.LOG_TAG, "onWaitEapAkaResp", this.mPhoneId);
        this.mIsEntitlementOngoing = false;
    }

    private void onCompletedEapChallengeResp(Message msg) {
        if (TextUtils.isEmpty((String) msg.obj)) {
            AECLog.i(this.LOG_TAG, "onCompletedEapChallengeResp: no eap challenge response", this.mPhoneId);
            return;
        }
        String str = this.LOG_TAG;
        AECLog.i(str, "onCompletedEapChallengeResp: " + msg.obj, this.mPhoneId);
        this.mHttpJar.setEapChallengeResp((String) msg.obj);
        sendEmptyMessage(1001);
    }

    private void onRequestFcmToken() {
        if (!AECNamespace.NotifState.IN_PROGRESS.equals(this.mNotifState)) {
            AECLog.i(this.LOG_TAG, "onRequestFcmToken", this.mPhoneId);
            this.mNotifState = AECNamespace.NotifState.IN_PROGRESS;
            Intent intent = new Intent(this.mContext, FcmIntentService.class);
            intent.putExtra("phoneId", this.mPhoneId);
            intent.putExtra(AECNamespace.NotifExtras.SENDER_ID, this.mAECJar.getNotifSenderId());
            this.mContext.startService(intent);
        }
    }

    public void updateFcmToken(String token, String message) {
        String str = this.LOG_TAG;
        AECLog.i(str, "updateFcmToken: " + message, this.mPhoneId);
        if (TextUtils.isEmpty(token)) {
            this.mNotifState = "NOT_READY";
            this.mAECJar.setNotifToken("");
            return;
        }
        this.mNotifState = "READY";
        String prevToken = this.mAECJar.getNotifToken();
        this.mAECJar.setNotifToken(token);
        if (!token.equals(prevToken) || !this.mIsValidEntitlement) {
            performEntitlement((Object) null);
        }
    }

    public void refreshFcmToken() {
        AECLog.i(this.LOG_TAG, "refreshFcmToken", this.mPhoneId);
        sendEmptyMessage(1006);
    }

    public void changeConnectivity() {
        if (!this.mPsDataOffExempt.isConnected() && DataConnectivity.isDataConnected(this.mContext, false)) {
            performEntitlement((Object) null);
        }
    }

    public void performEntitlement(Object obj) {
        if (isEntitlementOngoing()) {
            AECLog.i(this.LOG_TAG, "performEntitlement: entitlement in progress", this.mPhoneId);
        } else if (!this.mIsPsDataRoaming || this.mAECJar.getPsDataRoaming()) {
            int response = this.mAECJar.getHttpResponse();
            if (response == 400 || response == 403 || response == 500) {
                String str = this.LOG_TAG;
                AECLog.i(str, "performEntitlement: stored response " + response, this.mPhoneId);
                sendMessage(obtainMessage(1002, this.mPhoneId, response));
            } else if (!DataConnectivity.isDataConnected(this.mContext, this.mPsDataOffExempt.isConnected())) {
                AECLog.i(this.LOG_TAG, "performEntitlement: data unavailable", this.mPhoneId);
                if (!DataConnectivity.isMobileDataOn(this.mContext) && this.mAECJar.getPsDataOff()) {
                    sendMessageDelayed(obtainMessage(1007), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                }
            } else {
                this.mHttpJar.setAppId(TextUtils.isEmpty((String) obj) ? this.mAECJar.getAppId() : (String) obj);
                removeMessages(1001);
                if (TextUtils.isEmpty(this.mAECJar.getNotifSenderId()) || this.mNotifState.equals("READY")) {
                    sendMessageDelayed(obtainMessage(1001), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    return;
                }
                sendEmptyMessage(1006);
                sendMessageDelayed(obtainMessage(1001), 30000);
            }
        } else {
            AECLog.i(this.LOG_TAG, "performEntitlement: not allowed in roaming", this.mPhoneId);
        }
    }

    public boolean isReadyToNotifyApp() {
        return this.mIsReadyToNotifyApp;
    }

    public void setPsDataRoaming(boolean roaming) {
        this.mIsPsDataRoaming = roaming;
    }

    public void setReadyToNotifyApp(boolean ready) {
        this.mIsReadyToNotifyApp = ready;
    }

    public boolean isEntitlementOngoing() {
        return this.mIsEntitlementOngoing;
    }

    public void setValidEntitlement(boolean valid) {
        this.mIsValidEntitlement = valid;
    }

    public boolean isSharedAkaToken() {
        return this.mIsSharedAkaToken;
    }

    public void setSharedAkaToken(boolean shared) {
        this.mIsSharedAkaToken = shared;
    }

    public String getAkaToken() {
        return this.mAECJar.getAkaToken();
    }

    public void clearAkaToken() {
        this.mAECJar.setAkaToken("");
    }

    public void clearResource() {
        this.mHttpClient.closeURLConnection();
        this.mPowerCtrl.release();
        this.mValidityTimer.stopVersionValidityTimer();
        this.mValidityTimer.stopTokenValidityTimer();
    }

    private Date getDate(String timeStamp) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault()).parse(timeStamp);
        } catch (ParseException e) {
            return null;
        }
    }

    public void receivedFcmNotification(String from, String app, String timeStamp) {
        Date dateInConfig = getDate(this.mAECJar.getTimeStamp());
        Date dateInPushMsg = getDate(timeStamp);
        if (dateInPushMsg == null || TextUtils.isEmpty(from) || TextUtils.isEmpty(app)) {
            AECLog.i(this.LOG_TAG, "discard incorrect syntax", this.mPhoneId);
        } else if (dateInConfig == null || dateInPushMsg.after(dateInConfig)) {
            if (this.mAECJar.getNotifSenderId().equals(from)) {
                if (this.mAECJar.getVersion() < 0) {
                    this.mAECJar.setVersion("0");
                }
                setValidEntitlement(false);
                if (getEntitlementInitFromApp()) {
                    setReadyToNotifyApp(true);
                }
                performEntitlement(app);
                return;
            }
            AECLog.i(this.LOG_TAG, "discard incorrect senderId", this.mPhoneId);
        } else if (dateInConfig.after(dateInPushMsg)) {
            AECLog.i(this.LOG_TAG, "discard outdated notification", this.mPhoneId);
        }
    }

    public void receivedSmsNotification(String app) {
        if (this.mAECJar.getVersion() < 0) {
            this.mAECJar.setVersion("0");
        }
        setValidEntitlement(false);
        performEntitlement(app);
    }

    public boolean getEntitlementForVoLte() {
        return this.mAECJar.getEntitlementForVoLte();
    }

    public boolean getEntitlementForVoWiFi() {
        return this.mAECJar.getEntitlementForVoWiFi();
    }

    public boolean getEntitlementInitFromApp() {
        return this.mAECJar.getEntitlementInitFromApp();
    }

    public boolean getSMSoIpEntitlementStatus() {
        return this.mAECJar.getSMSoIPEntitlementStatus() == 1;
    }

    public boolean getVoLteEntitlementStatus() {
        return this.mAECJar.getVoLTEEntitlementStatus() == 1;
    }

    public boolean getVoWiFiEntitlementStatus() {
        return this.mAECJar.getVoWiFiActivationMode() == 3;
    }

    private void requestEntitlement(int version) {
        String httpUrl = URLExtractor.getHttpUrl(this.mPhoneId);
        if (version < 0 || TextUtils.isEmpty(httpUrl) || this.mAECJar.getAppId().isEmpty()) {
            sendMessage(obtainMessage(1002, this.mPhoneId, this.mAECJar.getHttpResponse()));
            return;
        }
        this.mPowerCtrl.lock(90000);
        if (TextUtils.isEmpty(this.mHttpJar.getHttpUrl())) {
            this.mHttpJar.setUserAgent(this.mAECJar.getEntitlementVersion());
            this.mHttpJar.setHostName(URLExtractor.getHostName(httpUrl));
            this.mHttpJar.setHttpUrls(URLExtractor.getIpAddress(this.mPhoneId, httpUrl, DataConnectivity.isWifiConnected(this.mContext) ? null : this.mPsDataOffExempt.getNetwork()));
            HttpStore httpStore = this.mHttpJar;
            httpStore.setHttpUrl((String) httpStore.getHttpUrls().poll());
        }
        doWorkflow();
        this.mPowerCtrl.release();
    }

    /* access modifiers changed from: package-private */
    public HttpClient.Response getHttpGetResponse(String url) throws Exception {
        this.mHttpClient.setHeaders(this.mHttpJar.getHttpHeaders());
        this.mHttpClient.setParams(this.mHttpJar.getHttpParams());
        this.mHttpClient.setNetwork(DataConnectivity.isWifiConnected(this.mContext) ? null : this.mPsDataOffExempt.getNetwork());
        HttpClient.Response response = this.mHttpClient.getURLConnection(url);
        this.mHttpClient.closeURLConnection();
        return response;
    }

    /* access modifiers changed from: package-private */
    public HttpClient.Response getHttpPostResponse(String url) throws Exception {
        this.mHttpClient.setHeaders(this.mHttpJar.getHttpHeaders());
        this.mHttpClient.setPostData(this.mHttpJar.getHttpPostData());
        this.mHttpClient.setNetwork(DataConnectivity.isWifiConnected(this.mContext) ? null : this.mPsDataOffExempt.getNetwork());
        HttpClient.Response response = this.mHttpClient.postURLConnection(url);
        this.mHttpClient.closeURLConnection();
        return response;
    }
}
