package com.sec.internal.ims.cmstore.ambs.provision;

import android.content.Context;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.settings.RcsConfigurationReader;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqRetireSession;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqToken;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqZCode;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccountEligibility;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestCreateAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestDeleteAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestHUIToken;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestPat;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestTC;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.ambs.receiver.DataSMSReceiver;
import com.sec.internal.ims.cmstore.ambs.receiver.SmsReceiver;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRuleHandling;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessfulCallHandling;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.FailedAPICallResponseParam;
import com.sec.internal.ims.cmstore.params.SuccessfullAPICallResponseParam;
import com.sec.internal.ims.cmstore.params.UIEventParam;
import com.sec.internal.ims.cmstore.receiver.NetworkChangeReceiver;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;
import com.sec.internal.log.IMSLog;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.regex.Pattern;

public class ProvisionController extends Handler implements IAPICallFlowListener, IControllerCommonInterface {
    protected static final int EVENT_PAUSE = 6;
    protected static final int EVENT_PROVISIONAPI_FAIL = 4;
    protected static final int EVENT_PROVISIONAPI_SUCCESS = 3;
    protected static final int EVENT_PROVISION_API = 1;
    protected static final int EVENT_RESUME = 5;
    protected static final int EVENT_STOP = 7;
    protected static final int EVENT_UI_ACTIONS = 2;
    public static final String TAG = ProvisionController.class.getSimpleName();
    private final long DELAY = 10000;
    private final long INTERNAL_WAITING = 5000;
    private final AmbsPhoneStateListener mAmbsPhoneStateListener;
    private final Context mContext;
    private DataSMSReceiver mDataSmsReceiver;
    private boolean mHasUserDeleteAccount = false;
    private boolean mHasUserOptedIn = false;
    private final ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private final IRetryStackAdapterHelper mIRetryStackAdapterHelper;
    private final IWorkingStatusProvisionListener mIWorkingStatusProvisionListener;
    private boolean mIfSteadyState = false;
    private int mLastSavedMessageIdAfterStop = -1;
    private ATTConstants.AttAmbsUIScreenNames mLastScreenUserStopBackup;
    private ATTConstants.AttAmbsUIScreenNames mLastUIScreen;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    private int mNewUserOptInCase;
    private boolean mPaused = false;
    private SmsReceiver mSmsReceiver;
    private final TelephonyManager mTelephonyManager;
    private final IUIEventCallback mUIInterface;

    public ProvisionController(IWorkingStatusProvisionListener controller, Looper looper, Context context, IUIEventCallback uicallback, TelephonyManager telephony, IRetryStackAdapterHelper retryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mIWorkingStatusProvisionListener = controller;
        this.mUIInterface = uicallback;
        this.mContext = context;
        this.mTelephonyManager = telephony;
        this.mAmbsPhoneStateListener = new AmbsPhoneStateListener(this.mTelephonyManager, this, this.mContext);
        this.mIRetryStackAdapterHelper = retryStackAdapterHelper;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mLastSavedMessageIdAfterStop = -1;
        this.mPaused = false;
        initPrefenceValues();
    }

    private void initPrefenceValues() {
        this.mNewUserOptInCase = CloudMessagePreferenceManager.getInstance().getNewUserOptInCase();
        this.mIfSteadyState = CloudMessagePreferenceManager.getInstance().ifSteadyState();
        this.mHasUserOptedIn = CloudMessagePreferenceManager.getInstance().hasUserOptedIn();
        this.mLastUIScreen = ATTConstants.AttAmbsUIScreenNames.valueOf(CloudMessagePreferenceManager.getInstance().getLastScreen());
        this.mLastScreenUserStopBackup = ATTConstants.AttAmbsUIScreenNames.valueOf(CloudMessagePreferenceManager.getInstance().getLastScreenUserStopBackup());
        this.mHasUserDeleteAccount = CloudMessagePreferenceManager.getInstance().hasUserDeleteAccount();
    }

    private void readNcNmsHost() {
        readNcHost();
        readNmsHost();
    }

    private static boolean isBase64(String str) {
        return Pattern.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$", str);
    }

    /* access modifiers changed from: private */
    public boolean readNcHost() {
        String nc = new RcsConfigurationReader(this.mContext).getString("root/application/1/serviceproviderext/nc_url");
        String str = TAG;
        Log.d(str, "readNcHost() nc=" + nc);
        if (TextUtils.isEmpty(nc)) {
            return false;
        }
        String nc2 = nc.trim();
        if (isBase64(nc2)) {
            try {
                nc2 = new String(Base64.decode(nc2, 0));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to decrypt the NC");
            }
        }
        String oldNc = CloudMessagePreferenceManager.getInstance().getNcHost();
        String str2 = TAG;
        Log.d(str2, "oldnc=" + oldNc + " nc=" + nc2);
        if (nc2.equals(oldNc)) {
            return false;
        }
        CloudMessagePreferenceManager.getInstance().saveNcHost(nc2);
        return true;
    }

    /* access modifiers changed from: private */
    public boolean readNmsHost() {
        String nms = new RcsConfigurationReader(this.mContext).getString("root/application/1/serviceproviderext/nms_url");
        String str = TAG;
        Log.d(str, "readNmsHost() nms=" + nms);
        if (TextUtils.isEmpty(nms)) {
            return false;
        }
        try {
            nms = new String(Base64.decode(nms, 0));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to decrypt the NMS");
        }
        CloudMessagePreferenceManager.getInstance().saveAcsNmsHost(nms);
        String oldNms = CloudMessagePreferenceManager.getInstance().getNmsHost();
        String str2 = TAG;
        Log.d(str2, "oldNms=" + oldNms + " nms=" + nms);
        if (TextUtils.isEmpty(oldNms)) {
            return true;
        }
        return false;
    }

    private void registerConfigurationObserver() {
        this.mContext.getContentResolver().registerContentObserver(RcsConfigurationReader.AUTO_CONFIGURATION_URI, true, new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                String str = ProvisionController.TAG;
                Log.d(str, "changed in DB. uri=" + IMSLog.checker(uri));
                if (uri.toString().contains("root/application/1/serviceproviderext/nc_url")) {
                    if (ProvisionController.this.readNcHost()) {
                        Log.d(ProvisionController.TAG, "nc host changed, send REQ_SESSION_GEN event");
                    }
                } else if (uri.toString().contains("root/application/1/serviceproviderext/nms_url") && ProvisionController.this.readNmsHost()) {
                    Log.d(ProvisionController.TAG, "nms host changed, send REQ_SESSION_GEN event");
                }
            }
        });
    }

    private void registerNetworkChangeReceiver() {
        Log.d(TAG, "registerNetworkChangeReceiver");
        if (this.mNetworkChangeReceiver == null) {
            IntentFilter localIntentFilter = new IntentFilter();
            localIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
            localIntentFilter.setPriority(Integer.MAX_VALUE);
            NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver(this.mIWorkingStatusProvisionListener);
            this.mNetworkChangeReceiver = networkChangeReceiver;
            this.mContext.registerReceiver(networkChangeReceiver, localIntentFilter);
        }
    }

    private void registerSmsReceiver() {
        IntentFilter localIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        localIntentFilter.setPriority(Integer.MAX_VALUE);
        if (this.mSmsReceiver == null) {
            SmsReceiver smsReceiver = new SmsReceiver(this);
            this.mSmsReceiver = smsReceiver;
            this.mContext.registerReceiver(smsReceiver, localIntentFilter);
        }
        Log.d(TAG, "registerSmsReceiver");
    }

    private void unregisterSmsReceiver() {
        SmsReceiver smsReceiver = this.mSmsReceiver;
        if (smsReceiver != null) {
            this.mContext.unregisterReceiver(smsReceiver);
            this.mSmsReceiver = null;
        }
    }

    private void registerDataSmsReceiver() {
        Log.d(TAG, "registerDataSmsReceiver");
        IntentFilter filter = new IntentFilter(AECNamespace.Action.RECEIVED_SMS_NOTIFICATION);
        filter.addDataAuthority("*", ATTGlobalVariables.ATT_DATA_MESSAGE_PORT);
        filter.addDataScheme("sms");
        if (this.mDataSmsReceiver == null) {
            DataSMSReceiver dataSMSReceiver = new DataSMSReceiver(this);
            this.mDataSmsReceiver = dataSMSReceiver;
            this.mContext.registerReceiver(dataSMSReceiver, filter);
        }
    }

    private void unregisterDataSmsReceiver() {
        Log.d(TAG, "unregisterDataSmsReceiver");
        DataSMSReceiver dataSMSReceiver = this.mDataSmsReceiver;
        if (dataSMSReceiver != null) {
            this.mContext.unregisterReceiver(dataSMSReceiver);
            this.mDataSmsReceiver = null;
        }
    }

    private void startPhoneStateListener() {
        this.mAmbsPhoneStateListener.startListen();
    }

    private void stopPhoneStateListener() {
        this.mAmbsPhoneStateListener.stopListen();
    }

    public boolean onUIButtonProceed(int screenName, String message) {
        String str = TAG;
        Log.d(str, "message: " + message);
        sendMessage(obtainMessage(2, new UIEventParam(ATTConstants.AttAmbsUIScreenNames.valueOf(screenName), message)));
        return true;
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String str = TAG;
        Log.i(str, "message: " + msg.what);
        logCurrentWorkingStatus();
        switch (msg.what) {
            case 1:
                EnumProvision.ProvisionEventType type = (EnumProvision.ProvisionEventType) msg.obj;
                if (type != null) {
                    handleProvisionEvent(type);
                    return;
                }
                return;
            case 2:
                UIEventParam uieventParam = (UIEventParam) msg.obj;
                if (uieventParam != null) {
                    handleUIEvent(uieventParam.mUIScreen, uieventParam.mMessage);
                    return;
                }
                return;
            case 3:
                SuccessfullAPICallResponseParam successParam = (SuccessfullAPICallResponseParam) msg.obj;
                if (successParam != null) {
                    onProvisionAPISuccess(successParam);
                    return;
                }
                return;
            case 4:
                FailedAPICallResponseParam failParam = (FailedAPICallResponseParam) msg.obj;
                if (failParam != null) {
                    onProvisionAPIFail(failParam);
                    return;
                }
                return;
            case 5:
                if (this.mPaused) {
                    this.mPaused = false;
                    int i = this.mLastSavedMessageIdAfterStop;
                    if (i != -1) {
                        sendMessage(obtainMessage(1, EnumProvision.ProvisionEventType.valueOf(i)));
                        this.mLastSavedMessageIdAfterStop = -1;
                        Log.i(TAG, "resume successfully");
                        return;
                    }
                    Log.i(TAG, "no saved event");
                    return;
                }
                return;
            case 6:
                this.mPaused = true;
                return;
            case 7:
                this.mPaused = true;
                this.mLastSavedMessageIdAfterStop = -1;
                return;
            default:
                return;
        }
    }

    private void handleUIEvent(ATTConstants.AttAmbsUIScreenNames screenName, String message) {
        String str = TAG;
        Log.i(str, "handleUIEvent: " + screenName + " messge: " + message);
        if (screenName == null) {
            Log.d(TAG, "screenName is null");
            return;
        }
        switch (AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames[screenName.ordinal()]) {
            case 1:
                unregisterDataSmsReceiver();
                saveUserOptedIn(true);
                saveLastScreen(screenName.getId());
                this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg11.getId(), IUIEventCallback.NON_POP_UP, 0);
                int newUserOptInCase = this.mNewUserOptInCase;
                String str2 = TAG;
                Log.d(str2, "newUserOptInCase: " + newUserOptInCase);
                if (newUserOptInCase == EnumProvision.NewUserOptInCase.ERR.getId()) {
                    if (!TextUtils.isEmpty(CloudMessagePreferenceManager.getInstance().getAtsToken())) {
                        update(EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId());
                    } else {
                        update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
                    }
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DEFAULT.getId());
                    return;
                } else if (newUserOptInCase == EnumProvision.NewUserOptInCase.DELETE.getId()) {
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DEFAULT.getId());
                    update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
                    return;
                } else {
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DEFAULT.getId());
                    update(EnumProvision.ProvisionEventType.REQ_GET_TC.getId());
                    return;
                }
            case 2:
            case 3:
                unregisterDataSmsReceiver();
                saveUserOptedIn(true);
                saveLastScreenUserStopBackup(screenName.getId());
                saveLastScreen(screenName.getId());
                this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                update(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId());
                return;
            case 4:
                this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                update(EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT.getId());
                return;
            case 5:
                saveLastScreen(screenName.getId());
                this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                if (CloudMessagePreferenceManager.getInstance().isLastAPIRequestCreateAccount()) {
                    Log.d(TAG, "HUIToken 6014 case");
                    this.mIRetryStackAdapterHelper.clearRetryHistory();
                    update(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId());
                    return;
                }
                IHttpAPICommonInterface lastApi = this.mIRetryStackAdapterHelper.getLastFailedRequest();
                if (lastApi != null) {
                    Log.d(TAG, "SteadyStateError - retry api");
                    IRetryStackAdapterHelper iRetryStackAdapterHelper = this.mIRetryStackAdapterHelper;
                    iRetryStackAdapterHelper.retryApi(lastApi, this, this.mICloudMessageManagerHelper, iRetryStackAdapterHelper);
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
                    return;
                }
                Log.d(TAG, "last api is null");
                notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
                this.mIRetryStackAdapterHelper.clearRetryHistory();
                return;
            case 6:
                saveLastScreen(screenName.getId());
                this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                IHttpAPICommonInterface api = this.mIRetryStackAdapterHelper.getLastFailedRequest();
                if (api != null) {
                    Log.d(TAG, "SteadyStateError - retry api");
                    IRetryStackAdapterHelper iRetryStackAdapterHelper2 = this.mIRetryStackAdapterHelper;
                    iRetryStackAdapterHelper2.retryApi(api, this, this.mICloudMessageManagerHelper, iRetryStackAdapterHelper2);
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
                    return;
                }
                Log.d(TAG, "retry stack is empty");
                update(EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId());
                this.mIRetryStackAdapterHelper.clearRetryHistory();
                return;
            case 7:
                this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg11.getId(), IUIEventCallback.NON_POP_UP, 0);
                CloudMessagePreferenceManager.getInstance().increaseUserInputNumberCount();
                saveUserOptedIn(true);
                if (!TextUtils.isEmpty(message)) {
                    CloudMessagePreferenceManager.getInstance().saveUserCtn(message, true);
                    onFixedFlow(EnumProvision.ProvisionEventType.CHECK_PHONE_STATE.getId());
                    return;
                }
                Log.e(TAG, "phone number null");
                this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg9.getId(), IUIEventCallback.NON_POP_UP, 0);
                update(EnumProvision.ProvisionEventType.REQ_INPUT_CTN.getId());
                return;
            case 8:
                ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames = this.mLastScreenUserStopBackup;
                if (attAmbsUIScreenNames != null) {
                    notifyMsgAppNonPopup(attAmbsUIScreenNames.getId(), 0);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void handleProvisionEvent(EnumProvision.ProvisionEventType type) {
        String str = TAG;
        Log.i(str, "handleProvisionEvent: " + type + " mHasUserOptedIn:" + this.mHasUserOptedIn + " mIfSteadyState:" + this.mIfSteadyState);
        if (type.getId() == EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT.getId() || type.getId() == EnumProvision.ProvisionEventType.RESTART_SERVICE.getId() || this.mHasUserDeleteAccount || !this.mPaused) {
            switch (AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[type.ordinal()]) {
                case 1:
                    registerNetworkChangeReceiver();
                    registerDataSmsReceiver();
                    if (ATTGlobalVariables.isGcmReplacePolling()) {
                        registerConfigurationObserver();
                    }
                    if (ProvisionHelper.isSimOrCtnChanged(this.mTelephonyManager) || ProvisionHelper.isOOBE() || TextUtils.isEmpty(CloudMessagePreferenceManager.getInstance().getUserCtn())) {
                        Log.i(TAG, "isSimOrCtnChanged || OOBE || empty CTN");
                        startOOBE();
                        CloudMessagePreferenceManager.getInstance().saveAppVer(ATTGlobalVariables.VERSION_NAME);
                        return;
                    }
                    CloudMessagePreferenceManager.getInstance().saveAppVer(ATTGlobalVariables.VERSION_NAME);
                    if (CloudMessagePreferenceManager.getInstance().hasShownPopupOptIn() && !this.mHasUserOptedIn) {
                        String str2 = TAG;
                        Log.i(str2, "has shown popup before, will not bother user and server, non_popup screen : " + this.mLastUIScreen);
                        ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames = this.mLastUIScreen;
                        if (attAmbsUIScreenNames != null) {
                            notifyMsgAppNonPopup(attAmbsUIScreenNames.getId(), 0);
                            return;
                        } else {
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                            return;
                        }
                    } else if (this.mIRetryStackAdapterHelper.isRetryTimesFinished()) {
                        Log.i(TAG, "isRetryTimesFinished");
                        ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames2 = this.mLastUIScreen;
                        if (attAmbsUIScreenNames2 != null) {
                            notifyMsgAppNonPopup(attAmbsUIScreenNames2.getId(), 0);
                            return;
                        }
                        return;
                    } else {
                        IHttpAPICommonInterface lastFailApi = this.mIRetryStackAdapterHelper.getLastFailedRequest();
                        if (lastFailApi != null) {
                            Log.i(TAG, "retryLastApi");
                            if (lastFailApi instanceof ReqZCode) {
                                Log.d(TAG, "in order to Auth Z code, register sms receiver");
                                registerSmsReceiver();
                            }
                            IRetryStackAdapterHelper iRetryStackAdapterHelper = this.mIRetryStackAdapterHelper;
                            iRetryStackAdapterHelper.retryLastApi(this, this.mICloudMessageManagerHelper, iRetryStackAdapterHelper);
                            ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames3 = this.mLastUIScreen;
                            if (attAmbsUIScreenNames3 != null) {
                                notifyMsgAppNonPopup(attAmbsUIScreenNames3.getId(), 0);
                                return;
                            }
                            return;
                        } else if (TextUtils.isEmpty(CloudMessagePreferenceManager.getInstance().getAtsToken())) {
                            update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
                            return;
                        } else if (TextUtils.isEmpty(CloudMessagePreferenceManager.getInstance().getValidPAT())) {
                            this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                            update(EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId());
                            return;
                        } else if (!TextUtils.isEmpty(CloudMessagePreferenceManager.getInstance().getValidPAT())) {
                            Log.i(TAG, "PAT VALID");
                            onProvisionReady();
                            return;
                        } else {
                            Log.i(TAG, "TODO");
                            return;
                        }
                    }
                case 2:
                    ProvisionHelper.readAndSaveSimInformation(this.mTelephonyManager);
                    if (TextUtils.isEmpty(CloudMessagePreferenceManager.getInstance().getUserCtn())) {
                        Log.d(TAG, "empty CTN");
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), ATTConstants.AttAmbsUIScreenNames.MsisdnEntry_ErrMsg6.getId());
                        return;
                    }
                    Log.d(TAG, "CTN was successfully read");
                    update(EnumProvision.ProvisionEventType.CHECK_PHONE_STATE.getId());
                    return;
                case 3:
                    startPhoneStateListener();
                    return;
                case 4:
                    removeMessages(1, EnumProvision.ProvisionEventType.valueOf(EnumProvision.ProvisionEventType.EVENT_AUTH_ZCODE_TIMEOUT.getId()));
                    String bufferedCtn = CloudMessagePreferenceManager.getInstance().getUserCtn();
                    String accountNumber = AmbsUtils.convertPhoneNumberToUserAct(this.mTelephonyManager.getLine1Number());
                    if (TextUtils.isEmpty(bufferedCtn)) {
                        String str3 = TAG;
                        Log.i(str3, "empty CTN, phone number:" + IMSLog.checker(accountNumber));
                        if (TextUtils.isEmpty(accountNumber)) {
                            accountNumber = this.mICloudMessageManagerHelper.getNativeLine();
                            String str4 = TAG;
                            Log.i(str4, "Phone number from DB == " + IMSLog.checker(accountNumber));
                        }
                        CloudMessagePreferenceManager.getInstance().saveUserCtn(accountNumber, false);
                    } else if (!TextUtils.isEmpty(accountNumber) && !bufferedCtn.equals(accountNumber)) {
                        Log.i(TAG, "Phone number was changed!!");
                        if (this.mICloudMessageManagerHelper.needToHandleSimSwap()) {
                            this.mIWorkingStatusProvisionListener.onRestartService();
                            return;
                        }
                        return;
                    }
                    registerSmsReceiver();
                    updateDelay(EnumProvision.ProvisionEventType.EVENT_AUTH_ZCODE_TIMEOUT.getId(), 900000);
                    HttpController.getInstance().execute(new ReqZCode(this, this.mICloudMessageManagerHelper));
                    return;
                case 5:
                    removeMessages(1, EnumProvision.ProvisionEventType.valueOf(EnumProvision.ProvisionEventType.EVENT_AUTH_ZCODE_TIMEOUT.getId()));
                    unregisterSmsReceiver();
                    stopPhoneStateListener();
                    HttpController.getInstance().execute(new ReqToken(this, this.mICloudMessageManagerHelper));
                    return;
                case 6:
                    if (ATTGlobalVariables.isGcmReplacePolling()) {
                        readNcNmsHost();
                        initSharedPreference();
                    }
                    this.mIWorkingStatusProvisionListener.onChannelStateReset();
                    HttpController.getInstance().execute(new ReqSession(this, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper));
                    return;
                case 7:
                    HttpController.getInstance().execute(new RequestAccount(this));
                    return;
                case 8:
                    HttpController.getInstance().execute(new RequestAccountEligibility(this, this.mICloudMessageManagerHelper));
                    return;
                case 9:
                    HttpController.getInstance().execute(new RequestTC(this, this.mICloudMessageManagerHelper));
                    return;
                case 10:
                    HttpController.getInstance().execute(new RequestCreateAccount(this, this.mICloudMessageManagerHelper));
                    return;
                case 11:
                    saveUserDeleteAccount(true);
                    this.mIWorkingStatusProvisionListener.onUserDeleteAccount(true);
                    HttpController.getInstance().execute(new RequestDeleteAccount(this, this.mICloudMessageManagerHelper));
                    return;
                case 12:
                    if (!this.mIfSteadyState) {
                        saveIfSteadyState(true);
                    }
                    HttpController.getInstance().execute(new RequestHUIToken(this, this.mICloudMessageManagerHelper));
                    return;
                case 13:
                    HttpController.getInstance().execute(new RequestPat(this, this.mICloudMessageManagerHelper));
                    return;
                case 15:
                    HttpController.getInstance().execute(new ReqRetireSession(this, this.mICloudMessageManagerHelper));
                    onProvisionReady();
                    return;
                case 16:
                    if (!this.mHasUserOptedIn && !this.mIfSteadyState) {
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else if (!this.mHasUserOptedIn || this.mIfSteadyState) {
                        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                        return;
                    } else {
                        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.EligibilityError_ErrMsg1.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    }
                case 17:
                    if (CloudMessagePreferenceManager.getInstance().getIsUserInputCtn()) {
                        update(EnumProvision.ProvisionEventType.REQ_INPUT_CTN.getId());
                        return;
                    }
                    ProvisionHelper.readAndSaveSimInformation(this.mTelephonyManager);
                    if (CloudMessagePreferenceManager.getInstance().isZCodeMax2Tries()) {
                        Log.d(TAG, "No more chance. Show error screen");
                        update(EnumProvision.ProvisionEventType.AUTH_ERR.getId());
                        CloudMessagePreferenceManager.getInstance().removeZCodeCounter();
                        return;
                    }
                    CloudMessagePreferenceManager.getInstance().increazeZCodeCounter();
                    update(EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE.getId());
                    return;
                case 18:
                    if (CloudMessagePreferenceManager.getInstance().getIsUserInputCtn()) {
                        CloudMessagePreferenceManager.getInstance().clearInvalidUserCtn();
                    }
                    if (CloudMessagePreferenceManager.getInstance().isNoMoreChanceUserInputNumber()) {
                        Log.d(TAG, "No more chance. Show error screen");
                        update(EnumProvision.ProvisionEventType.AUTH_ERR.getId());
                        return;
                    }
                    Log.d(TAG, "user still has a chance to input the number");
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.MsisdnEntry_ErrMsg6.getId(), 0);
                    return;
                case 19:
                    if (!this.mHasUserOptedIn && !this.mIfSteadyState) {
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else if (!this.mHasUserOptedIn || this.mIfSteadyState) {
                        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                        return;
                    } else {
                        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.ProvisioningBlockedError_ErrMsg8.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    }
                case 20:
                    if (!this.mHasUserOptedIn) {
                        if (!this.mIfSteadyState) {
                            saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                            return;
                        }
                        handleProvisionErr();
                        return;
                    } else if (!this.mIfSteadyState) {
                        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.ProvisioningError_ErrMsg4.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else {
                        handleProvisionErr();
                        return;
                    }
                case 21:
                    boolean isInputCTNWrong = false;
                    if (CloudMessagePreferenceManager.getInstance().isNoMoreChanceUserInputNumber()) {
                        Log.d(TAG, "max 2 tries reached");
                        CloudMessagePreferenceManager.getInstance().removeUserInputNumberCount();
                        this.mIRetryStackAdapterHelper.clearRetryHistory();
                        CloudMessagePreferenceManager.getInstance().saveUserCtn("", false);
                        isInputCTNWrong = true;
                    }
                    if (!this.mHasUserOptedIn && !this.mIfSteadyState) {
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else if (!this.mHasUserOptedIn || this.mIfSteadyState) {
                        removeMessages(EnumProvision.ProvisionEventType.EVENT_AUTH_ZCODE_TIMEOUT.getId());
                        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                        return;
                    } else {
                        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.AuthenticationError_ErrMsg2.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        if (isInputCTNWrong) {
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), ATTConstants.AttAmbsUIScreenNames.MsisdnEntry_ErrMsg6.getId());
                            return;
                        } else {
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                            return;
                        }
                    }
                case 22:
                    if (CloudMessagePreferenceManager.getInstance().isHUI6014Err()) {
                        CloudMessagePreferenceManager.getInstance().saveIfHUI6014Err(false);
                    }
                    if (!this.mHasUserOptedIn && !this.mIfSteadyState) {
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else if (!this.mHasUserOptedIn || this.mIfSteadyState) {
                        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg5.getId(), IUIEventCallback.POP_UP, 0);
                        return;
                    } else {
                        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg5.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    }
                case 23:
                    if (this.mHasUserDeleteAccount) {
                        saveUserDeleteAccount(false);
                        this.mIWorkingStatusProvisionListener.onUserDeleteAccount(false);
                    }
                    this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.StopBackupError_ErrMsg10.getId(), IUIEventCallback.POP_UP, 0);
                    notifyMsgAppNonPopup(this.mLastScreenUserStopBackup.getId(), 0);
                    return;
                case 24:
                    String oldCTN = CloudMessagePreferenceManager.getInstance().getUserCtn();
                    String oldIMSI = CloudMessagePreferenceManager.getInstance().getSimImsi();
                    boolean isOptin = CloudMessagePreferenceManager.getInstance().hasUserOptedIn();
                    boolean isUserInputCTN = CloudMessagePreferenceManager.getInstance().getIsUserInputCtn();
                    this.mIWorkingStatusProvisionListener.onCloudSyncWorkingStopped();
                    this.mIWorkingStatusProvisionListener.onUserDeleteAccount(false);
                    stopProvisioningAPIs();
                    CloudMessagePreferenceManager.getInstance().saveIfHasShownPopupOptIn(true);
                    CloudMessagePreferenceManager.getInstance().saveSimImsi(oldIMSI);
                    CloudMessagePreferenceManager.getInstance().saveUserCtn(oldCTN, isUserInputCTN);
                    saveUserOptedIn(false);
                    this.mIRetryStackAdapterHelper.clearRetryHistory();
                    this.mLastSavedMessageIdAfterStop = -1;
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DELETE.getId());
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                    initPrefenceValues();
                    if (isOptin) {
                        registerDataSmsReceiver();
                        return;
                    }
                    return;
                case 25:
                    stopProvisioningAPIs();
                    saveUserOptedIn(false);
                    this.mLastSavedMessageIdAfterStop = -1;
                    registerNetworkChangeReceiver();
                    registerDataSmsReceiver();
                    update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.ordinal());
                    this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                    initPrefenceValues();
                    return;
                case 26:
                    updateDelay(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId(), 10000);
                    return;
                case 27:
                    if (CloudMessagePreferenceManager.getInstance().getIsUserInputCtn()) {
                        Log.d(TAG, "Wrong CTN, clear user input");
                        CloudMessagePreferenceManager.getInstance().clearInvalidUserCtn();
                    }
                    if (!CloudMessagePreferenceManager.getInstance().isZCodeMax2Tries()) {
                        Log.d(TAG, "isZCodeMax2Tries: false");
                        CloudMessagePreferenceManager.getInstance().increazeZCodeCounter();
                        update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
                    } else {
                        String str5 = TAG;
                        Log.d(str5, "isZCodeMax2Tries: true, mHasUserOptedIn:" + this.mHasUserOptedIn);
                        CloudMessagePreferenceManager.getInstance().removeZCodeCounter();
                        if (this.mHasUserOptedIn) {
                            this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.AuthenticationError_ErrMsg2.getId(), IUIEventCallback.POP_UP, 0);
                            saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        } else {
                            saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        }
                    }
                    unregisterSmsReceiver();
                    stopPhoneStateListener();
                    return;
                case 28:
                    onMailBoxMigrationReset();
                    return;
                default:
                    return;
            }
        } else {
            this.mLastSavedMessageIdAfterStop = type.getId();
            String str6 = TAG;
            Log.i(str6, "handleMessage stop! Pending Message is " + type);
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.ambs.provision.ProvisionController$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType;

        static {
            int[] iArr = new int[EnumProvision.ProvisionEventType.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType = iArr;
            try {
                iArr[EnumProvision.ProvisionEventType.CHK_INITIAL_STATE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.CHECK_PHONE_STATE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_ATS_TOKEN.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_SESSION_GEN.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_SERVICE_ACCOUNT.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_ACCOUNT_ELIGIBILITY.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_GET_TC.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_CREATE_ACCOUNT.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_PAT.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_RETIRE_SESSION.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.READY_PAT.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.ACCOUNT_NOT_ELIGIBLE.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.ZCODE_ERROR_201.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.REQ_INPUT_CTN.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.CPS_PROVISION_SHUTDOWN.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.PROVISION_ERR.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.AUTH_ERR.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.ACCESS_ERR.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.STOP_BACKUP_ERR.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.DELETE_ACCOUNT_SUCCESS.ordinal()] = 24;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.RESTART_SERVICE.ordinal()] = 25;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.LAST_RETRY_CREATE_ACCOUNT.ordinal()] = 26;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.EVENT_AUTH_ZCODE_TIMEOUT.ordinal()] = 27;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[EnumProvision.ProvisionEventType.MAILBOX_MIGRATION_RESET.ordinal()] = 28;
            } catch (NoSuchFieldError e28) {
            }
            int[] iArr2 = new int[ATTConstants.AttAmbsUIScreenNames.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames = iArr2;
            try {
                iArr2[ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.ordinal()] = 1;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames[ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWithTerms_PrmptMsg3.ordinal()] = 2;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames[ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4.ordinal()] = 3;
            } catch (NoSuchFieldError e31) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames[ATTConstants.AttAmbsUIScreenNames.StopBackup_PrmptMsg13.ordinal()] = 4;
            } catch (NoSuchFieldError e32) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames[ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg5.ordinal()] = 5;
            } catch (NoSuchFieldError e33) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames[ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.ordinal()] = 6;
            } catch (NoSuchFieldError e34) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames[ATTConstants.AttAmbsUIScreenNames.MsisdnEntry_ErrMsg6.ordinal()] = 7;
            } catch (NoSuchFieldError e35) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames[ATTConstants.AttAmbsUIScreenNames.StopBackupError_ErrMsg10.ordinal()] = 8;
            } catch (NoSuchFieldError e36) {
            }
        }
    }

    private void startOOBE() {
        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
        ((CookieManager) CookieHandler.getDefault()).getCookieStore().removeAll();
        CloudMessagePreferenceManager.getInstance().clearAll();
        this.mIWorkingStatusProvisionListener.onCleanBufferDbRequired();
        initPrefenceValues();
        update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
    }

    private void onProvisionReady() {
        Log.i(TAG, "onProvisionReady");
        saveIfSteadyState(true);
        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
        this.mIWorkingStatusProvisionListener.onProvisionSuccess();
        unregisterDataSmsReceiver();
    }

    private void onProvisionAPISuccess(SuccessfullAPICallResponseParam param) {
        String str = TAG;
        Log.i(str, "onProvisionAPISuccess: " + param);
        handlerUIonSuccessProvisionAPI(param);
        if (param.mCallFlow != null) {
            SuccessfulCallHandling.callHandling(this, param.mRequest, param.mCallFlow, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
        } else {
            SuccessfulCallHandling.callHandling(this, param.mRequest, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
        }
    }

    private void handlerUIonSuccessProvisionAPI(SuccessfullAPICallResponseParam param) {
        String str = TAG;
        Log.i(str, "handlerUIonSuccessProvisionAPI: " + param);
        if (RequestAccount.class.getSimpleName().equals(param.getApiName())) {
            if (!this.mHasUserOptedIn && !this.mIfSteadyState) {
                String str2 = TAG;
                Log.d(str2, "handlerUIonSuccessProvisionAPI: User has NOT opted in: isOOBE?: " + ProvisionHelper.isOOBE() + " hasUserOptedIn: " + this.mHasUserOptedIn);
                if (ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TC.equalsIgnoreCase(param.mCallFlow)) {
                    saveLastScreenUserStopBackup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId());
                    displayOptIn(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId());
                } else if (ReqConstant.HAPPY_PATH_REQACCOUNT_EXIST_USER.equalsIgnoreCase(param.mCallFlow)) {
                    saveLastScreenUserStopBackup(ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4.getId());
                    displayOptIn(ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4.getId());
                } else if (ReqConstant.HAPPY_PATH_BINARY_SMS_PROVISIONED.equalsIgnoreCase(param.mCallFlow)) {
                    saveLastScreenUserStopBackup(ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4.getId());
                    CloudMessagePreferenceManager.getInstance().saveIfHasShownPopupOptIn(false);
                    displayOptIn(ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4.getId());
                } else if (ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TBS_TC.equalsIgnoreCase(param.mCallFlow)) {
                    CloudMessagePreferenceManager.getInstance().saveUserTbsRquired(true);
                    saveLastScreenUserStopBackup(ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWithTerms_PrmptMsg3.getId());
                    displayOptIn(ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWithTerms_PrmptMsg3.getId());
                } else {
                    Log.i(TAG, "illegal returned callflow name");
                }
            } else if (ReqConstant.HAPPY_PATH_REQACCOUNT_EXIST_USER.equalsIgnoreCase(param.mCallFlow)) {
                Log.d(TAG, "handlerUIonSuccessProvisionAPI: HAPPY_PATH_REQ_ACCOUNT_EXIST_USER");
                update(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId());
            } else if (ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TC.equalsIgnoreCase(param.mCallFlow)) {
                Log.d(TAG, "handlerUIonSuccessProvisionAPI: NEW_USER");
                if (CloudMessagePreferenceManager.getInstance().isHUI6014Err()) {
                    Log.d(TAG, "handlerUIonSuccessProvisionAPI: SOC removal");
                    this.mIWorkingStatusProvisionListener.onCloudSyncWorkingStopped();
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DELETE.getId());
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                } else {
                    update(EnumProvision.ProvisionEventType.REQ_GET_TC.getId());
                }
            } else {
                Log.d(TAG, "handlerUIonSuccessProvisionAPI: TBS_TC");
                update(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId());
            }
        }
        if (RequestHUIToken.class.getSimpleName().equals(param.getApiName())) {
            Log.i(TAG, "handlerUIonSuccessProvisionAPI: RequestHUIToken API success");
            if (this.mIfSteadyState && !this.mHasUserDeleteAccount) {
                notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
            }
        }
    }

    private void displayOptIn(int optInScreen) {
        boolean hasShownPopUpOptIn = CloudMessagePreferenceManager.getInstance().hasShownPopupOptIn();
        String str = TAG;
        Log.d(str, "displayOptIn: hasShownPopUpOptIn? : " + hasShownPopUpOptIn + " mHasUserOptedIn:" + this.mHasUserOptedIn);
        if (!hasShownPopUpOptIn) {
            if (ProvisionHelper.isOOBE() || !this.mHasUserOptedIn) {
                this.mUIInterface.notifyUIScreen(optInScreen, IUIEventCallback.POP_UP, 0);
                CloudMessagePreferenceManager.getInstance().saveIfHasShownPopupOptIn(true);
                notifyMsgAppNonPopup(optInScreen, 0);
                return;
            }
            Log.d(TAG, "handlerUIonSuccessProvisionAPI: !isOOBE && UserHasOptedIn - impossible here");
        } else if (ProvisionHelper.isOOBE() || !this.mHasUserOptedIn) {
            notifyMsgAppNonPopup(optInScreen, 0);
        } else {
            Log.d(TAG, "handlerUIonSuccessProvisionAPI: !OOBE && UserOptedIn");
        }
    }

    private void onProvisionAPIFail(FailedAPICallResponseParam param) {
        String str = TAG;
        Log.i(str, "onProvisionAPIFail: " + param);
        handlerUIonFailedProvisionAPI(param);
        if (param.mErrorCode != null) {
            ErrorRuleHandling.handleErrorCode(this, param.mRequest, param.mErrorCode, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
        } else {
            ErrorRuleHandling.handleErrorCode(this, param.mRequest, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
        }
    }

    private void handlerUIonFailedProvisionAPI(FailedAPICallResponseParam param) {
        String str = TAG;
        Log.i(str, "handlerUIonFailedProvisionAPI: all failed APIs should go here. param: " + param);
    }

    private void notifyMsgAppNonPopup(int screen, int nextScreen) {
        String str = TAG;
        Log.d(str, "screen to display: " + screen);
        if (screen == ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWithTerms_PrmptMsg3.getId() || screen == ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4.getId() || screen == ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId()) {
            saveLastScreenUserStopBackup(screen);
        }
        saveLastScreen(screen);
        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg9.getId(), IUIEventCallback.NON_POP_UP, 0);
        if (nextScreen > 0) {
            this.mUIInterface.notifyUIScreen(screen, IUIEventCallback.NON_POP_UP, nextScreen);
        } else {
            this.mUIInterface.notifyUIScreen(screen, IUIEventCallback.NON_POP_UP, 0);
        }
    }

    private void stopProvisioningAPIs() {
        Log.d(TAG, "stopProvisioningAPIs");
        for (int i = 1; i <= 4; i++) {
            removeMessages(i);
        }
    }

    private void saveNewUserOptInCase(int optinCase) {
        CloudMessagePreferenceManager.getInstance().saveNewUserOptInCase(optinCase);
        this.mNewUserOptInCase = optinCase;
    }

    private void saveUserOptedIn(boolean ifoptin) {
        CloudMessagePreferenceManager.getInstance().saveUserOptedIn(ifoptin);
        this.mHasUserOptedIn = ifoptin;
    }

    private void saveIfSteadyState(boolean steadystate) {
        CloudMessagePreferenceManager.getInstance().saveIfSteadyState(steadystate);
        this.mIfSteadyState = steadystate;
    }

    private void saveLastScreen(int screen) {
        CloudMessagePreferenceManager.getInstance().saveLastScreen(screen);
        this.mLastUIScreen = ATTConstants.AttAmbsUIScreenNames.valueOf(screen);
    }

    private void saveLastScreenUserStopBackup(int screenId) {
        CloudMessagePreferenceManager.getInstance().saveLastScreenUserStopBackup(screenId);
        this.mLastScreenUserStopBackup = ATTConstants.AttAmbsUIScreenNames.valueOf(screenId);
    }

    private void saveUserDeleteAccount(boolean hasUserDeleteAccout) {
        CloudMessagePreferenceManager.getInstance().saveUserDeleteAccount(hasUserDeleteAccout);
        this.mHasUserDeleteAccount = hasUserDeleteAccout;
    }

    private void logCurrentWorkingStatus() {
        String str = TAG;
        Log.d(str, "logCurrentWorkingStatus: [mLastSavedMessageIdAfterStop: " + this.mLastSavedMessageIdAfterStop + " mPaused: " + this.mPaused + " mNewUserOptInCase: " + this.mNewUserOptInCase + " mIfSteadyState: " + this.mIfSteadyState + " mHasUserOptedIn: " + this.mHasUserOptedIn + " mLastUIScreen: " + this.mLastUIScreen + " mLastScreenUserStopBackup: " + this.mLastScreenUserStopBackup + " mHasUserDeleteAccount: " + this.mHasUserDeleteAccount + "]");
    }

    public void onGoToEvent(int event, Object param) {
        EnumProvision.ProvisionEventType eventType = EnumProvision.ProvisionEventType.valueOf(event);
        String str = TAG;
        Log.i(str, "onGoToEvent: " + eventType);
        sendMessage(obtainMessage(1, eventType));
    }

    public void onMoveOnToNext(IHttpAPICommonInterface request, Object param) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request, String callFlow) {
        sendMessage(obtainMessage(3, new SuccessfullAPICallResponseParam(request, callFlow)));
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request) {
        sendMessage(obtainMessage(3, new SuccessfullAPICallResponseParam(request, (String) null)));
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface request, int event, Object param) {
    }

    public void onFailedCall(IHttpAPICommonInterface request, String errorCode) {
        sendMessage(obtainMessage(4, new FailedAPICallResponseParam(request, errorCode)));
    }

    public void onFailedCall(IHttpAPICommonInterface request, BufferDBChangeParam newParam) {
        sendMessage(obtainMessage(4, new FailedAPICallResponseParam(request, (String) null)));
    }

    public void onFailedCall(IHttpAPICommonInterface request) {
        sendMessage(obtainMessage(4, new FailedAPICallResponseParam(request, (String) null)));
    }

    public void onOverRequest(IHttpAPICommonInterface request, String errorCode, int retryAfter) {
        ErrorRuleHandling.handleErrorHeader(this, request, errorCode, retryAfter, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
    }

    public void onFixedFlow(int event) {
        EnumProvision.ProvisionEventType eventType = EnumProvision.ProvisionEventType.valueOf(event);
        String str = TAG;
        Log.i(str, "onFixedFlow: " + eventType);
        sendMessage(obtainMessage(1, eventType));
    }

    public void onFailedEvent(int event, Object param) {
    }

    public void onFixedFlowWithMessage(Message msg) {
    }

    public void start() {
        Log.i(TAG, "start");
        updateDelay(EnumProvision.ProvisionEventType.CHK_INITIAL_STATE.getId(), 5000);
    }

    public void resume() {
        Log.i(TAG, "resume");
        sendMessage(obtainMessage(5));
    }

    public void pause() {
        Log.i(TAG, "pause");
        sendMessage(obtainMessage(6));
    }

    public void stop() {
        sendMessage(obtainMessage(7));
    }

    public boolean update(int eventType) {
        EnumProvision.ProvisionEventType event = EnumProvision.ProvisionEventType.valueOf(eventType);
        String str = TAG;
        Log.i(str, "update: " + event);
        return sendMessage(obtainMessage(1, event));
    }

    public boolean updateDelay(int eventType, long delay) {
        EnumProvision.ProvisionEventType event = EnumProvision.ProvisionEventType.valueOf(eventType);
        String str = TAG;
        Log.i(str, "update with " + event + " delayed " + delay);
        return sendMessageDelayed(obtainMessage(1, event), delay);
    }

    public boolean updateDelayRetry(int eventType, long delay) {
        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg11.getId(), IUIEventCallback.NON_POP_UP, 0);
        EnumProvision.ProvisionEventType event = EnumProvision.ProvisionEventType.valueOf(eventType);
        String str = TAG;
        Log.i(str, "update with " + event + " delayed retry " + delay);
        return sendMessageDelayed(obtainMessage(1, event), delay);
    }

    public boolean updateMessage(Message msg) {
        return false;
    }

    public void onOmaFailExceedMaxCount() {
        this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
        this.mUIInterface.showInitsyncIndicator(false);
        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), 0);
        saveLastScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId());
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener listener) {
    }

    private void initSharedPreference() {
        CloudMessagePreferenceManager preference = CloudMessagePreferenceManager.getInstance();
        preference.saveOMAChannelResURL("");
        preference.saveOMAChannelURL("");
        preference.saveOMACallBackURL("");
        preference.saveOMAChannelCreateTime(0);
        preference.saveOMAChannelLifeTime(0);
        preference.clearOMASubscriptionChannelDuration();
        preference.clearOMASubscriptionTime();
    }

    public void onMailBoxMigrationReset() {
        Log.i(TAG, "onMailBoxMigrationReset.");
        this.mIWorkingStatusProvisionListener.onMailBoxMigrationReset();
    }

    private void handleProvisionErr() {
        String str = TAG;
        Log.d(str, "handleProvisionErr, TBS Case:" + CloudMessagePreferenceManager.getInstance().getUserTbs());
        if (!CloudMessagePreferenceManager.getInstance().getUserTbs()) {
            this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
        } else {
            CloudMessagePreferenceManager.getInstance().saveUserTbsRquired(false);
        }
    }
}
