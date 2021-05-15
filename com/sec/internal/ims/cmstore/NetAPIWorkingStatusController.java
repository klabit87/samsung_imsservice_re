package com.sec.internal.ims.cmstore;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.ims.cmstore.adapters.DeviceConfigAdapter;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.ambs.provision.ProvisionController;
import com.sec.internal.ims.cmstore.ambs.provision.ProvisionHelper;
import com.sec.internal.ims.cmstore.helper.SyncParam;
import com.sec.internal.ims.cmstore.omanetapi.FaxHandler;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamNetAPIStatusControl;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CheckCaptivePortal;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.interfaces.ims.cmstore.ILineStatusChangeCallBack;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;
import com.sec.internal.log.IMSLog;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetAPIWorkingStatusController extends Handler implements IWorkingStatusProvisionListener, IDeviceDataChangeListener, ILineStatusChangeCallBack {
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final int EVENT_AIRPLANEMODE_CHANGED = 8;
    private static final int EVENT_CHANGE_MSG_APP_WORKING_STATUS = 3;
    private static final int EVENT_CHANGE_OMANETAPI_WORKING_STATUS = 4;
    private static final int EVENT_DELETE_ACCOUNT = 7;
    private static final int EVENT_MESSAGE_APP_CHANGED = 1;
    private static final int EVENT_NETWORK_CHANGE_DETECTED = 2;
    private static final int EVENT_REGISTER_PHONE_LISTENER = 9;
    private static final int EVENT_SIM_STATE_CHANGED = 10;
    private static final int EVENT_STOP_ALL_TASK = 6;
    public static final String TAG = NetAPIWorkingStatusController.class.getSimpleName();
    private IUIEventCallback mCallbackMsgApp;
    private CloudMessageManagerHelper mCloudMessageManagerHelper;
    Context mContext;
    private DeviceConfigAdapter mDeviceConfigAdapter;
    private FaxHandler mFaxHandler;
    private boolean mHasNotifiedBufferDBProvisionSuccess = false;
    private IRetryStackAdapterHelper mIRetryStackAdapterHelper;
    private boolean mIsAirPlaneModeOn = false;
    private boolean mIsCMNWorkingStarted = false;
    private boolean mIsCmsProfileEnabled = false;
    private boolean mIsDefaultMsgAppNative = true;
    private boolean mIsMsgAppForeground = false;
    private boolean mIsNetworkValid = true;
    private boolean mIsOMAAPIRunning = false;
    private boolean mIsProvisionSuccess = false;
    private boolean mIsUserDeleteAccount = false;
    /* access modifiers changed from: private */
    public boolean mIsUsingMobileHipri = false;
    private LineManager mLineManager;
    private MobileNetowrkCallBack mMobileNetworkCallback = null;
    private OMANetAPIHandler mNetAPIHandler;
    private final ConnectivityManager.NetworkCallback mNetworkStateListener = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            String str = NetAPIWorkingStatusController.TAG;
            Log.i(str, "onAvailable " + network);
            NetAPIWorkingStatusController netAPIWorkingStatusController = NetAPIWorkingStatusController.this;
            netAPIWorkingStatusController.sendMessage(netAPIWorkingStatusController.obtainMessage(2));
        }

        public void onLost(Network network) {
            String str = NetAPIWorkingStatusController.TAG;
            Log.i(str, "onLost + " + network);
            NetAPIWorkingStatusController netAPIWorkingStatusController = NetAPIWorkingStatusController.this;
            netAPIWorkingStatusController.sendMessage(netAPIWorkingStatusController.obtainMessage(2));
        }
    };
    private ProvisionController mProvisionControl;
    private PhoneStateListener mServiceStateListener = null;
    private final TelephonyManager mTelephonyManager;
    private VvmHandler mVvmHandler;
    private final RegistrantList mWorkingStatus = new RegistrantList();

    public void registerForUpdateFromCloud(Handler h, int what, Object obj) {
        this.mNetAPIHandler.registerForUpdateFromCloud(h, what, obj);
        this.mFaxHandler.registerForUpdateFromCloud(h, what, obj);
        this.mVvmHandler.registerForUpdateFromCloud(h, what, obj);
    }

    public void registerForUpdateOfWorkingStatus(Handler h, int what, Object obj) {
        this.mWorkingStatus.add(new Registrant(h, what, obj));
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NetAPIWorkingStatusController(Looper looper, Context context, IUIEventCallback callback, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        super(looper);
        this.mContext = context;
        this.mCallbackMsgApp = callback;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY);
        this.mIRetryStackAdapterHelper = iRetryStackAdapterHelper;
        this.mCloudMessageManagerHelper = new CloudMessageManagerHelper();
        this.mProvisionControl = new ProvisionController(this, looper, context, this.mCallbackMsgApp, this.mTelephonyManager, this.mIRetryStackAdapterHelper, this.mCloudMessageManagerHelper);
        this.mLineManager = new LineManager(this);
        Looper looper2 = looper;
        this.mNetAPIHandler = new OMANetAPIHandler(looper2, this.mContext, this, this.mCallbackMsgApp, this.mLineManager, this.mCloudMessageManagerHelper);
        this.mFaxHandler = new FaxHandler(looper2, context, this.mNetAPIHandler, this.mIRetryStackAdapterHelper, this.mCloudMessageManagerHelper);
        Looper looper3 = looper;
        this.mVvmHandler = new VvmHandler(looper, context, this.mNetAPIHandler, this.mCloudMessageManagerHelper);
        this.mDeviceConfigAdapter = new DeviceConfigAdapter(this.mContext, this.mCloudMessageManagerHelper);
        this.mIsUserDeleteAccount = CloudMessagePreferenceManager.getInstance().hasUserDeleteAccount();
        CookieHandler.setDefault(new CookieManager(new PersistentHttpCookieStore(this.mContext), CookiePolicy.ACCEPT_ALL));
        registerDefaultSmsPackageChangeReceiver(this.mContext);
        registerAirplaneMode(this.mContext);
    }

    public void init() {
        initDeviceID();
        CloudMessageStrategyManager.createStrategy(this.mContext);
        if (CloudMessageStrategyManager.getStrategy().isSupportAtt72HoursRule()) {
            sendMessage(obtainMessage(9));
        }
        if (CloudMessageStrategyManager.getStrategy().isProvisionRequired()) {
            startProvsioningApi();
        } else {
            initSimInfo();
        }
        if (!CloudMessageStrategyManager.getStrategy().isRetryEnabled()) {
            this.mIRetryStackAdapterHelper.clearRetryHistory();
        }
        if (CloudMessageStrategyManager.getStrategy().isDeviceConfigUsed()) {
            String deviceConfigExist = "device config exists";
            if (this.mDeviceConfigAdapter.getDeviceConfig() == null) {
                deviceConfigExist = "no existing device config";
                this.mDeviceConfigAdapter.registerBootCompletedReceiver(this.mContext);
                this.mDeviceConfigAdapter.registerContentObserver(this.mContext);
            } else {
                this.mDeviceConfigAdapter.parseDeviceConfig();
            }
            String str = TAG;
            Log.d(str, "isDeviceConfigUsed: true, " + deviceConfigExist);
        }
        registerNetworkStateListener();
        NetworkInfo activeNetwork = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetwork == null) {
            return;
        }
        if (activeNetwork.getType() == 1) {
            CloudMessageStrategyManager.getStrategy().setProtocol(OMAGlobalVariables.HTTPS);
        } else if (activeNetwork.getType() == 0) {
            CloudMessageStrategyManager.getStrategy().setProtocol(OMAGlobalVariables.HTTP);
        }
    }

    private void initSimInfo() {
        if (isSimChanged()) {
            CloudMessagePreferenceManager.getInstance().clearAll();
        }
        String accountNumber = AmbsUtils.convertPhoneNumberToUserAct(this.mTelephonyManager.getLine1Number());
        String newImsi = this.mTelephonyManager.getSubscriberId();
        String phoneNumberFrom = "== ";
        if (TextUtils.isEmpty(accountNumber)) {
            phoneNumberFrom = "from DB == ";
            accountNumber = CloudMessageStrategyManager.getStrategy().getNativeLine();
        }
        String str = TAG;
        Log.i(str, "Phone number " + phoneNumberFrom + IMSLog.checker(accountNumber) + ", Provision not required");
        CloudMessagePreferenceManager.getInstance().saveSimImsi(newImsi);
        CloudMessagePreferenceManager.getInstance().saveUserCtn(accountNumber, false);
    }

    private void registerDefaultSmsPackageChangeReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Log.w(NetAPIWorkingStatusController.TAG, "registerDefaultSmsPackageChangeReceiver, onReceive: intent is null.");
                    return;
                }
                String action = intent.getAction();
                String str = NetAPIWorkingStatusController.TAG;
                Log.d(str, "registerDefaultSmsPackageChangeReceiver, onReceive: anction = " + action);
                if ("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL".equals(action)) {
                    NetAPIWorkingStatusController.this.sendEmptyMessage(1);
                }
            }
        }, filter);
    }

    private void registerAirplaneMode(Context context) {
        boolean z = Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
        this.mIsAirPlaneModeOn = z;
        if (z) {
            this.mIsNetworkValid = false;
        } else {
            this.mIsNetworkValid = true;
        }
        logCurrentWorkingStatus();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_AIRPLANE_MODE);
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String str = NetAPIWorkingStatusController.TAG;
                Log.d(str, "registerAirplaneMode, BroadcastReceiver, action: " + action);
                if (ImsConstants.Intents.ACTION_AIRPLANE_MODE.equals(action)) {
                    NetAPIWorkingStatusController netAPIWorkingStatusController = NetAPIWorkingStatusController.this;
                    netAPIWorkingStatusController.sendMessage(netAPIWorkingStatusController.obtainMessage(8));
                } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                    NetAPIWorkingStatusController netAPIWorkingStatusController2 = NetAPIWorkingStatusController.this;
                    netAPIWorkingStatusController2.sendMessage(netAPIWorkingStatusController2.obtainMessage(10));
                }
            }
        }, intentFilter);
    }

    private void registerPhoneStateListener(Context mContext2) {
        Log.d(TAG, "registerPhoneStateListener");
        createPhoneServiceListener();
        this.mTelephonyManager.listen(this.mServiceStateListener, 1);
    }

    private void createPhoneServiceListener() {
        this.mServiceStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState serviceState) {
                String str = NetAPIWorkingStatusController.TAG;
                Log.v(str, "onServiceStateChanged " + serviceState.getState());
                if (serviceState.getState() == 0 || Util.isWifiCallingEnabled(NetAPIWorkingStatusController.this.mContext)) {
                    CloudMessagePreferenceManager.getInstance().saveNetworkAvailableTime(System.currentTimeMillis());
                }
            }
        };
    }

    private void handleEventMessageAppChanged() {
        logCurrentWorkingStatus();
        if (!this.mIsCmsProfileEnabled) {
            Log.d(TAG, "handleEventMessageAppChanged: not enabled");
            return;
        }
        this.mIsDefaultMsgAppNative = AmbsUtils.isDefaultMessageAppInUse(this.mContext);
        if (CloudMessageStrategyManager.getStrategy().isGoForwardSyncSupported()) {
            if (!this.mIsDefaultMsgAppNative) {
                Log.d(TAG, "handleEventMessageAppChanged: native message app not default");
                CloudMessagePreferenceManager.getInstance().saveNativeMsgAppIsDefault(false);
                setOMANetAPIWorkingStatus(false);
                this.mNetAPIHandler.deleteNotificationSubscriptionResource();
                pauseProvsioningApi();
                return;
            }
            Log.d(TAG, "handleEventMessageAppChanged native message app default");
            CloudMessagePreferenceManager.getInstance().saveNativeMsgAppIsDefault(true);
            resumeProvsioningApi();
            Log.i(TAG, "notify buffer DB");
            this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.DEFAULT_MSGAPP_CHGTO_NATIVE, (Throwable) null));
            if (shouldEnableOMANetAPIWorking()) {
                Log.d(TAG, "handleEventMessageAppChanged: default msg app, resume cms api working");
                setOMANetAPIWorkingStatus(true);
            }
        }
    }

    public boolean isNativeMsgAppDefault() {
        return this.mIsDefaultMsgAppNative;
    }

    public void handleMessage(Message msg) {
        NetworkInfo ntwkInfo;
        NetworkCapabilities nc;
        super.handleMessage(msg);
        removeMessages(msg.what);
        Log.i(TAG, "message: " + msg.what);
        boolean z = true;
        switch (msg.what) {
            case 1:
                handleEventMessageAppChanged();
                return;
            case 2:
                boolean isWifiConnected = false;
                boolean isMobileConnected = false;
                Network wifiNtwkInstance = null;
                ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                Network[] networks = cm.getAllNetworks();
                if (networks != null) {
                    int i = 0;
                    while (true) {
                        if (i < networks.length) {
                            if (!(networks[i] == null || (ntwkInfo = cm.getNetworkInfo(networks[i])) == null)) {
                                if (ntwkInfo.getType() == 1) {
                                    isWifiConnected = ntwkInfo.isAvailable() && ntwkInfo.isConnected();
                                    wifiNtwkInstance = networks[i];
                                } else if (ntwkInfo.getType() == 0 && (nc = cm.getNetworkCapabilities(networks[i])) != null && nc.hasCapability(12)) {
                                    isMobileConnected = ntwkInfo.isAvailable() && ntwkInfo.isConnected();
                                }
                                Log.d(TAG, "Network info: type[" + ntwkInfo.getType() + "], typeName[" + ntwkInfo.getTypeName() + "], isAvailable[" + ntwkInfo.isAvailable() + "], isConnected[" + ntwkInfo.isConnected() + "]");
                                if (isWifiConnected && isMobileConnected) {
                                    Log.d(TAG, "Wifi and Mobile are both connected, no need to continue scan");
                                }
                            }
                            i++;
                        }
                    }
                }
                boolean z2 = Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
                this.mIsAirPlaneModeOn = z2;
                if (isWifiConnected) {
                    CloudMessageStrategyManager.getStrategy().setProtocol(OMAGlobalVariables.HTTPS);
                    boolean needToCheckCaptive = CloudMessageStrategyManager.getStrategy().isCaptivePortalCheckSupported();
                    Log.d(TAG, "WiFi connected, needToCheckCaptive value: " + needToCheckCaptive);
                    if (needToCheckCaptive) {
                        String wifiStatus = "Good Wifi";
                        if (!checkingWifiGoodOrNot(wifiNtwkInstance)) {
                            wifiStatus = "Bad Wifi";
                            startMobileHipri();
                        } else if (this.mIsUsingMobileHipri) {
                            Log.d(TAG, "is using MOBILE_HIPRI, will change to default network");
                            stopMobileHipri();
                        }
                        Log.d(TAG, wifiStatus);
                    }
                } else if (isMobileConnected && !z2) {
                    CloudMessageStrategyManager.getStrategy().setProtocol(OMAGlobalVariables.HTTP);
                    Log.d(TAG, "WiFi not connected, but Mobile is connected");
                    if (this.mIsUsingMobileHipri) {
                        Log.d(TAG, "is using MOBILE_HIPRI, will change to default network");
                        stopMobileHipri();
                    }
                }
                boolean isNetworkAvailable = isMobileConnected || isWifiConnected;
                Log.d(TAG, "Network available: " + isNetworkAvailable);
                if (this.mIsNetworkValid && !isNetworkAvailable) {
                    Log.d(TAG, "no available network, reset channel state.");
                    this.mNetAPIHandler.resetChannelState();
                }
                setNetworkStatus(isNetworkAvailable);
                if (!isNetworkAvailable) {
                    setOMANetAPIWorkingStatus(false);
                    return;
                } else if (shouldEnableOMANetAPIWorking()) {
                    Log.d(TAG, "shouldEnableOMANetAPIWorking: true");
                    setOMANetAPIWorkingStatus(true);
                    return;
                } else {
                    return;
                }
            case 3:
                logCurrentWorkingStatus();
                boolean booleanValue = ((Boolean) msg.obj).booleanValue();
                this.mIsMsgAppForeground = booleanValue;
                if (!booleanValue) {
                    setOMANetAPIWorkingStatus(false);
                }
                if (shouldEnableOMANetAPIWorking()) {
                    setOMANetAPIWorkingStatus(true);
                    return;
                }
                return;
            case 4:
                boolean enabled = ((Boolean) msg.obj).booleanValue();
                if (!this.mIsCmsProfileEnabled && enabled) {
                    Log.d(TAG, "mIsCmsProfileEnabled: false");
                    return;
                } else if (enabled) {
                    this.mIsOMAAPIRunning = true;
                    resumeCMNWorking();
                    return;
                } else {
                    this.mIsOMAAPIRunning = false;
                    pauseCMNWorking();
                    return;
                }
            case 6:
                stopCMNWorking();
                return;
            case 7:
                this.mIsUserDeleteAccount = ((Boolean) msg.obj).booleanValue();
                logCurrentWorkingStatus();
                if (this.mIsUserDeleteAccount) {
                    stopCMNWorking();
                    return;
                } else if (shouldEnableOMANetAPIWorking()) {
                    setOMANetAPIWorkingStatus(true);
                    return;
                } else {
                    return;
                }
            case 8:
                if (CloudMessageStrategyManager.getStrategy().isAirplaneModeChangeHandled()) {
                    if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0) {
                        z = false;
                    }
                    this.mIsAirPlaneModeOn = z;
                    if (z) {
                        setNetworkStatus(false);
                        setOMANetAPIWorkingStatus(false);
                        return;
                    }
                    Log.d(TAG, "airplane mode changed, mIsAirPlaneModeOn: " + this.mIsAirPlaneModeOn);
                    this.mNetAPIHandler.resetChannelState();
                    sendMessage(obtainMessage(2));
                    return;
                }
                return;
            case 9:
                registerPhoneStateListener(this.mContext);
                return;
            case 10:
                int simState = this.mTelephonyManager.getSimState();
                Log.d(TAG, "sim state changed, state: " + simState);
                TelephonyManager telephonyManager = this.mTelephonyManager;
                if (5 == simState && !ProvisionHelper.isSimChanged(telephonyManager) && ProvisionHelper.isCtnChangedByNetwork(this.mTelephonyManager)) {
                    Log.d(TAG, "ctn changed, restart service");
                    onRestartService();
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    public boolean shouldEnableOMANetAPIWorking() {
        return CloudMessageStrategyManager.getStrategy().shouldEnableNetAPIWorking(this.mIsNetworkValid, this.mIsDefaultMsgAppNative, this.mIsUserDeleteAccount, this.mIsProvisionSuccess);
    }

    private void pauseCMNWorking() {
        Log.d(TAG, "pause cloud message NetAPI");
        this.mNetAPIHandler.pausewithStatusParam(new ParamNetAPIStatusControl(this.mIsMsgAppForeground, this.mIsNetworkValid, this.mIsOMAAPIRunning, this.mIsDefaultMsgAppNative, this.mIsUserDeleteAccount, this.mIsProvisionSuccess));
    }

    private void stopCMNWorking() {
        Log.d(TAG, "stop cloud message NetAPI");
        this.mIsCMNWorkingStarted = false;
        this.mIsProvisionSuccess = false;
        this.mNetAPIHandler.stop();
    }

    private void startCMNWorking() {
        Log.d(TAG, "start cloud message NetAPI");
        this.mNetAPIHandler.start();
    }

    private void startCMNWorkingResetBox() {
        Log.d(TAG, "start cloud message NetAPI: resetBox");
        this.mNetAPIHandler.start_resetBox();
    }

    private void resumeCMNWorking() {
        Log.d(TAG, "resume cloud message NetAPI");
        this.mNetAPIHandler.resumewithStatusParam(new ParamNetAPIStatusControl(this.mIsMsgAppForeground, this.mIsNetworkValid, this.mIsOMAAPIRunning, this.mIsDefaultMsgAppNative, this.mIsUserDeleteAccount, this.mIsProvisionSuccess));
    }

    private void startProvsioningApi() {
        this.mProvisionControl.start();
    }

    private void pauseProvsioningApi() {
        Log.d(TAG, "pauseProvisioningApi");
        this.mProvisionControl.pause();
    }

    private void resumeProvsioningApi() {
        Log.d(TAG, "resumeProvisioningApi");
        if (CloudMessageStrategyManager.getStrategy().isProvisionRequired()) {
            this.mProvisionControl.resume();
        }
    }

    public void setMsgAppForegroundStatus(boolean state) {
        sendMessage(obtainMessage(3, Boolean.valueOf(state)));
    }

    public void setOMANetAPIWorkingStatus(boolean state) {
        sendMessage(obtainMessage(4, Boolean.valueOf(state)));
    }

    /* access modifiers changed from: private */
    public void setNetworkStatus(boolean state) {
        if (this.mIsCmsProfileEnabled || !state) {
            this.mIsNetworkValid = state;
            if (!state || !this.mIsDefaultMsgAppNative) {
                pauseProvsioningApi();
            } else {
                resumeProvsioningApi();
            }
            if (this.mIsNetworkValid && !this.mIsCMNWorkingStarted && this.mIsProvisionSuccess) {
                this.mIsCMNWorkingStarted = true;
                startCMNWorking();
                return;
            }
            return;
        }
        Log.d(TAG, "mIsCmsProfileEnabled: false");
    }

    public void sendDeviceUpdate(BufferDBChangeParamList param) {
        String str = TAG;
        Log.d(str, "sendDeviceUpdate: " + param);
        logCurrentWorkingStatus();
        if (param != null && param.mChangelst.size() > 0 && CloudMessageStrategyManager.getStrategy().isValidOMARequestUrl()) {
            BufferDBChangeParam changeParam = param.mChangelst.get(0);
            if (changeParam.mDBIndex != 19 && changeParam.mDBIndex != 18 && changeParam.mDBIndex != 20) {
                this.mNetAPIHandler.sendUpdate(param);
            } else if (changeParam.mDBIndex != 18 || !CloudMessageBufferDBConstants.ActionStatusFlag.Delete.equals(changeParam.mAction)) {
                this.mVvmHandler.sendVvmUpdate(param);
            } else {
                this.mNetAPIHandler.sendUpdate(param);
            }
        }
    }

    public void onProvisionSuccess() {
        this.mIsProvisionSuccess = true;
        logCurrentWorkingStatus();
        if (shouldEnableOMANetAPIWorking()) {
            setOMANetAPIWorkingStatus(true);
        }
        if (CloudMessageStrategyManager.getStrategy().isProvisionRequired()) {
            if (this.mIsNetworkValid && !this.mIsCMNWorkingStarted) {
                this.mIsCMNWorkingStarted = true;
                startCMNWorking();
            }
            if (!this.mHasNotifiedBufferDBProvisionSuccess) {
                this.mHasNotifiedBufferDBProvisionSuccess = true;
                this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.PROVISION_SUCCESS, (Throwable) null));
            }
        }
    }

    public void onCleanBufferDbRequired() {
        Log.i(TAG, "onCleanBufferDbRequired");
        this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.BUFFERDB_CLEAN, (Throwable) null));
    }

    public void onInitialDBSyncCompleted() {
        this.mProvisionControl.update(EnumProvision.ProvisionEventType.REQ_CREATE_ACCOUNT.getId());
    }

    public void onInitialDBCopyDone() {
        Log.i(TAG, "onInitialDBCopyDone");
        if (CloudMessageStrategyManager.getStrategy().isMultiLineSupported()) {
            this.mLineManager.initLineStatus();
        } else {
            Log.i(TAG, IMSLog.checker(CloudMessagePreferenceManager.getInstance().getUserTelCtn()));
            this.mLineManager.addLine(CloudMessagePreferenceManager.getInstance().getUserTelCtn());
        }
        logCurrentWorkingStatus();
        if (this.mIsNetworkValid && !this.mIsCMNWorkingStarted && this.mIsProvisionSuccess) {
            this.mIsCMNWorkingStarted = true;
            startCMNWorking();
        }
    }

    public void onMailBoxResetBufferDbDone() {
        Log.i(TAG, "onMailBoxResetBufferDbDone");
        if (CloudMessageStrategyManager.getStrategy().isMultiLineSupported()) {
            this.mLineManager.initLineStatus();
        } else {
            Log.i(TAG, IMSLog.checker(CloudMessagePreferenceManager.getInstance().getUserTelCtn()));
            this.mLineManager.addLine(CloudMessagePreferenceManager.getInstance().getUserTelCtn());
        }
        this.mNetAPIHandler.deleteNotificationSubscriptionResource();
        logCurrentWorkingStatus();
        if (this.mIsNetworkValid && this.mIsProvisionSuccess) {
            this.mIsCMNWorkingStarted = true;
            startCMNWorkingResetBox();
        }
        if (shouldEnableOMANetAPIWorking()) {
            setOMANetAPIWorkingStatus(true);
        }
    }

    public boolean onUIButtonProceed(int screenName, String message) {
        if (CloudMessageStrategyManager.getStrategy().isUIButtonUsed()) {
            Log.d(TAG, "UI button is enabled");
            return this.mProvisionControl.onUIButtonProceed(screenName, message);
        }
        Log.d(TAG, "UI button call is disabled");
        return false;
    }

    public void sendAppSync(SyncParam param) {
        String str = TAG;
        Log.i(str, "sendAppSync: " + param);
        if (CloudMessageStrategyManager.getStrategy().isValidOMARequestUrl()) {
            this.mNetAPIHandler.sendAppSync(param);
        }
    }

    public void stopAppSync(SyncParam param) {
        String str = TAG;
        Log.i(str, "sendAppSync: " + param);
        this.mNetAPIHandler.stopAppSync(param);
    }

    public void sendDeviceInitialSyncDownload(BufferDBChangeParamList param) {
        String str = TAG;
        Log.i(str, "sendDeviceInitialSyncDownload: " + param);
        if (CloudMessageStrategyManager.getStrategy().isValidOMARequestUrl()) {
            this.mNetAPIHandler.sendInitialSyncDownload(param);
        }
    }

    public void sendDeviceNormalSyncDownload(BufferDBChangeParamList param) {
        String str = TAG;
        Log.i(str, "sendDeviceNormalDownload: " + param);
        BufferDBChangeParamList vvmProfileList = new BufferDBChangeParamList();
        BufferDBChangeParamList objectList = new BufferDBChangeParamList();
        if (param != null && CloudMessageStrategyManager.getStrategy().isValidOMARequestUrl()) {
            Iterator<BufferDBChangeParam> it = param.mChangelst.iterator();
            while (it.hasNext()) {
                BufferDBChangeParam temp = it.next();
                if (temp.mDBIndex == 20) {
                    vvmProfileList.mChangelst.add(temp);
                } else {
                    objectList.mChangelst.add(temp);
                }
            }
        }
        if (objectList.mChangelst.size() > 0) {
            this.mNetAPIHandler.sendNormalSyncDownload(param);
        }
        if (vvmProfileList.mChangelst.size() > 0) {
            this.mVvmHandler.sendVvmUpdate(vvmProfileList);
        }
    }

    public void sendDeviceUpload(BufferDBChangeParamList param) {
        String str = TAG;
        Log.i(str, "sendDeviceUpload: " + param);
        if (param != null && CloudMessageStrategyManager.getStrategy().isValidOMARequestUrl()) {
            this.mNetAPIHandler.sendUpload(param);
        }
    }

    public void sendDeviceFax(BufferDBChangeParamList param) {
        String str = TAG;
        Log.i(str, "sendDeviceFax: " + param);
        if (param != null && param.mChangelst.size() > 0 && CloudMessageStrategyManager.getStrategy().isValidOMARequestUrl()) {
            BufferDBChangeParam changeParam = param.mChangelst.get(0);
            if (changeParam.mDBIndex == 21) {
                this.mFaxHandler.sendFaxUsingBufferDBTranslation(changeParam);
            }
        }
    }

    public void onOmaProvisionFailed(ParamOMAresponseforBufDB param, long reProvisionDelayInMillis) {
        String str = TAG;
        Log.d(str, "onOmaProvisionFailed: " + param);
        if (CloudMessageStrategyManager.getStrategy().isTokenRequestedFromProvision()) {
            this.mIsProvisionSuccess = false;
            setOMANetAPIWorkingStatus(false);
            String str2 = TAG;
            Log.d(str2, "REQ_SESSION_GEN will be triggered in " + (reProvisionDelayInMillis / 1000) + " seconds");
            this.mCallbackMsgApp.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
            this.mProvisionControl.updateDelay(EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), reProvisionDelayInMillis);
        } else if (param != null) {
            String line = Util.getMsisdn(param.getLine());
            String str3 = TAG;
            Log.i(str3, "msisdn : " + IMSLog.checker(line));
            if (!TextUtils.isEmpty(line) && line.length() > 1) {
                String line2 = line.substring(1);
                String str4 = TAG;
                Log.i(str4, "line: " + IMSLog.checker(line2));
                this.mContext.getContentResolver().update(NSDSContractExt.Lines.buildRefreshSitUri(line2), new ContentValues(), (String) null, (String[]) null);
            }
        }
    }

    public void onDeviceFlagUpdateSchedulerStarted() {
    }

    public void onCloudSyncWorkingStopped() {
        clearData();
        stopCMNWorking();
    }

    public void onUserDeleteAccount(boolean state) {
        String str = TAG;
        Log.d(str, "onUserDeleteAccount: " + state);
        sendMessage(obtainMessage(7, Boolean.valueOf(state)));
    }

    public void onRestartService() {
        setOMANetAPIWorkingStatus(false);
        clearData();
        stopCMNWorking();
        this.mProvisionControl.update(EnumProvision.ProvisionEventType.RESTART_SERVICE.getId());
        this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.RESTART_SERVICE, (Throwable) null));
        initDeviceID();
    }

    public void onChannelStateReset() {
        Log.d(TAG, "onChannelStateReset");
        this.mNetAPIHandler.resetChannelState();
    }

    private void clearData() {
        CloudMessagePreferenceManager.getInstance().clearAll();
        ((CookieManager) CookieHandler.getDefault()).getCookieStore().removeAll();
        this.mIRetryStackAdapterHelper.clearRetryHistory();
        onCleanBufferDbRequired();
        CloudMessageStrategyManager.getStrategy().clearOmaRetryData();
        this.mHasNotifiedBufferDBProvisionSuccess = false;
    }

    public void onNetworkChangeDetected() {
        Log.d(TAG, "onNetworkChangeDetected");
        boolean isCaptivePortalSupported = CloudMessageStrategyManager.getStrategy().isCaptivePortalCheckSupported();
        if (!this.mIsCmsProfileEnabled || !isCaptivePortalSupported) {
            String str = TAG;
            Log.d(str, "onNetworkChangeDetected: CmsProfileEnabled: " + this.mIsCmsProfileEnabled + " or captive portal:" + isCaptivePortalSupported);
            return;
        }
        sendEmptyMessage(2);
    }

    public void setCmsProfileEnabled(boolean value) {
        String str = TAG;
        Log.d(str, "setCmsProfileEnabled: " + value);
        boolean z = this.mIsCmsProfileEnabled;
        if (!z || value != z) {
            if (this.mIsCmsProfileEnabled || !value) {
                this.mIsCmsProfileEnabled = value;
            } else {
                Log.d(TAG, "check networking again,if cms is ready");
                this.mIsCmsProfileEnabled = value;
                onNetworkChangeDetected();
            }
            if (this.mIsCmsProfileEnabled) {
                init();
                this.mIsDefaultMsgAppNative = AmbsUtils.isDefaultMessageAppInUse(this.mContext);
                CloudMessagePreferenceManager.getInstance().saveNativeMsgAppIsDefault(this.mIsDefaultMsgAppNative);
                if (!this.mIsDefaultMsgAppNative) {
                    Log.d(TAG, "setCmsProfileEnabled: non-default app: pause provisioning");
                    pauseProvsioningApi();
                    return;
                }
                return;
            }
            unregisterNetworkStateListener();
            stopCMNWorking();
            pauseProvsioningApi();
        }
    }

    public void setImpuFromImsRegistration(String lineNum) {
        String str = TAG;
        Log.d(str, "setImpuFromImsRegistration: " + IMSLog.checker(lineNum) + ", shouldPersistImsRegNum value: " + CloudMessageStrategyManager.getStrategy().shouldPersistImsRegNum());
        if (CloudMessageStrategyManager.getStrategy().shouldPersistImsRegNum() && lineNum != null && lineNum.length() >= 10 && lineNum.length() <= 12) {
            if (lineNum.length() >= 11) {
                lineNum = lineNum.substring(lineNum.length() - 10, lineNum.length());
            }
            CloudMessagePreferenceManager.getInstance().saveUserCtn(lineNum, false);
        }
    }

    public boolean getCmsProfileEnabled() {
        return this.mIsCmsProfileEnabled;
    }

    private void logCurrentWorkingStatus() {
        String str = TAG;
        Log.d(str, "logCurrentWorkingStatus:  mIsUsingMobileHipri: " + this.mIsUsingMobileHipri + " mIsAmbsRunning: " + this.mIsOMAAPIRunning + " mIsMsgAppForeground: " + this.mIsMsgAppForeground + " mIsNetworkValid: " + this.mIsNetworkValid + " mIsCmsProfileEnabled: " + this.mIsCmsProfileEnabled + " mIsDefaultMsgAppNative: " + this.mIsDefaultMsgAppNative + " mIsUserDeleteAccount: " + this.mIsUserDeleteAccount + " mIsAirPlaneModeOn: " + this.mIsAirPlaneModeOn + " mIsCMNWorkingStarted: " + this.mIsCMNWorkingStarted + " mIsProvisionSuccess: " + this.mIsProvisionSuccess + " mHasNotifiedBufferDBProvisionSuccess: " + this.mHasNotifiedBufferDBProvisionSuccess);
    }

    public List<String> notifyLoadLineStatus() {
        if (CloudMessageStrategyManager.getStrategy().isMultiLineSupported()) {
            List<String> line = new ArrayList<>();
            line.add(CloudMessagePreferenceManager.getInstance().getUserCtn());
            return line;
        }
        List<String> line2 = new ArrayList<>();
        line2.add(CloudMessagePreferenceManager.getInstance().getUserCtn());
        return line2;
    }

    public void onDeviceSITRefreshed(String msisdn) {
        this.mNetAPIHandler.onLineSITRefreshed(Util.getTelUri(msisdn));
    }

    public void onOmaFailExceedMaxCount() {
        Log.d(TAG, "onOmaFailExceedMaxCount");
        if (CloudMessageStrategyManager.getStrategy().isTokenRequestedFromProvision()) {
            this.mIsProvisionSuccess = false;
            setOMANetAPIWorkingStatus(false);
            this.mProvisionControl.onOmaFailExceedMaxCount();
        }
    }

    /* access modifiers changed from: private */
    public boolean bindToNetwork(Network networkInterface) {
        if (networkInterface == null) {
            Log.d(TAG, "bind current process to default network type");
        }
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        boolean result = cm.bindProcessToNetwork(networkInterface);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            Log.d(TAG, activeNetworkInfo.toString());
        }
        return result;
    }

    private boolean checkingWifiGoodOrNot(Network wifi) {
        if (wifi != null) {
            return CheckCaptivePortal.isGoodWifi(wifi);
        }
        Log.d(TAG, "Wifi network instance is null");
        return false;
    }

    private void stopMobileHipri() {
        if (this.mIsUsingMobileHipri && this.mMobileNetworkCallback != null) {
            ((ConnectivityManager) this.mContext.getSystemService("connectivity")).unregisterNetworkCallback(this.mMobileNetworkCallback);
            this.mMobileNetworkCallback = null;
            Log.d(TAG, "Mobile network callback unregistered");
        }
        String bindResult = "failed";
        if (bindToNetwork((Network) null)) {
            bindResult = "successfully";
            this.mIsUsingMobileHipri = false;
        }
        String str = TAG;
        Log.d(str, "stopMobileHipri, bind to default network " + bindResult);
    }

    private void startMobileHipri() {
        Log.v(TAG, "startMobileHipri");
        if (this.mIsUsingMobileHipri) {
            Log.d(TAG, "mobile network is in using");
        } else if (this.mMobileNetworkCallback == null) {
            Log.d(TAG, "register mobile network callback");
            MobileNetowrkCallBack mobileNetowrkCallBack = new MobileNetowrkCallBack();
            this.mMobileNetworkCallback = mobileNetowrkCallBack;
            registerNetworkCallBack(0, mobileNetowrkCallBack);
        }
    }

    private void registerNetworkCallBack(int transportType, ConnectivityManager.NetworkCallback callback) {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(12);
        builder.addTransportType(transportType);
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).requestNetwork(builder.build(), callback);
    }

    class MobileNetowrkCallBack extends ConnectivityManager.NetworkCallback {
        public MobileNetowrkCallBack() {
        }

        public void onAvailable(Network network) {
            Log.i(NetAPIWorkingStatusController.TAG, "mobile network on available");
            if (NetAPIWorkingStatusController.this.bindToNetwork(network)) {
                Log.d(NetAPIWorkingStatusController.TAG, "bind to MOBILE_HIPRI successfully");
                CloudMessageStrategyManager.getStrategy().setProtocol(OMAGlobalVariables.HTTP);
                boolean unused = NetAPIWorkingStatusController.this.mIsUsingMobileHipri = true;
                NetAPIWorkingStatusController.this.setNetworkStatus(true);
                if (NetAPIWorkingStatusController.this.shouldEnableOMANetAPIWorking()) {
                    Log.d(NetAPIWorkingStatusController.TAG, "shouldEnableOMANetAPIWorking: true");
                    NetAPIWorkingStatusController.this.setOMANetAPIWorkingStatus(true);
                    return;
                }
                return;
            }
            Log.d(NetAPIWorkingStatusController.TAG, "bind to MOBILE_HIPRI failed");
        }

        public void onLost(Network network) {
            Log.i(NetAPIWorkingStatusController.TAG, "mobile network on lost");
        }
    }

    private void registerNetworkStateListener() {
        Log.i(TAG, "registerNetworkStateListener");
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(12);
        builder.addTransportType(0);
        NetworkRequest networkRequest = builder.build();
        try {
            ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerNetworkCallback(networkRequest, this.mNetworkStateListener);
        } catch (RuntimeException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void unregisterNetworkStateListener() {
        Log.i(TAG, "unregisterNetworkStateListener");
        try {
            ((ConnectivityManager) this.mContext.getSystemService("connectivity")).unregisterNetworkCallback(this.mNetworkStateListener);
        } catch (RuntimeException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public boolean isSimChanged() {
        Log.i(TAG, "isSimChanged");
        String oldCtn = CloudMessagePreferenceManager.getInstance().getUserCtn();
        if (!TextUtils.isEmpty(CloudMessagePreferenceManager.getInstance().getSimImsi()) || !TextUtils.isEmpty(oldCtn)) {
            return ProvisionHelper.isSimOrCtnChanged(this.mTelephonyManager);
        }
        return false;
    }

    public void hideIndicator() {
        Log.i(TAG, "hideIndicator()");
        this.mCallbackMsgApp.showInitsyncIndicator(false);
    }

    public void updateSubscriptionChannel() {
        Log.i(TAG, "updateSubscriptionChannel()");
        this.mNetAPIHandler.updateSubscriptionChannel();
    }

    public void removeUpdateSubscriptionChannelEvent() {
        Log.i(TAG, "removeUpdateSubscriptionChannelEvent()");
        this.mNetAPIHandler.removeUpdateSubscriptionChannelEvent();
    }

    public void handleLargeDataPolling() {
        this.mNetAPIHandler.handleLargeDataPolling();
    }

    public void onMailBoxMigrationReset() {
        this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.MAILBOX_MIGRATION_RESET, (Throwable) null));
    }

    private void initDeviceID() {
        CloudMessagePreferenceManager.getInstance().saveDeviceId(Util.getImei(this.mContext));
    }
}
