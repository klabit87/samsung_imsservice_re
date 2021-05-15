package com.sec.internal.ims.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.config.params.ACSConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ConfigModule extends Handler implements IConfigModule {
    private static final String AUTOCONF_TAG = "Autoconf";
    private static final int AUTO_CONFIG_IMS_PDN = 1;
    private static final int ERROR_WORKFLOW_IS_NULL = 708;
    private static final int LOCAL_CONFIG_VERS = 59;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ConfigModule.class.getSimpleName();
    private int m403ForbiddenCounter = 0;
    private int mCallState = 0;
    private ConfigComplete mConfigComplete;
    private ConfigTrigger mConfigTrigger;
    /* access modifiers changed from: private */
    public final Context mContext;
    protected SimpleEventLog mEventLog;
    private boolean mIsRcsEnabled = false;
    private boolean mIsReceivedSimRefresh = false;
    private boolean mIsRemoteConfigNeeded = false;
    private IAutoConfigurationListener mListener = null;
    private boolean mMobileNetwork = false;
    private String mMsisdnNumber = null;
    /* access modifiers changed from: private */
    public boolean mNeedRetryOverWifi = false;
    private PhoneIdKeyMap<HashMap<Integer, ConnectivityManager.NetworkCallback>> mNetworkListeners;
    private PhoneIdKeyMap<HashMap<Integer, Network>> mNetworkLists;
    private SparseArray<Message> mOnCompleteList = new SparseArray<>();
    private boolean mPendingAutoComplete = false;
    private boolean mPendingAutoConfig = false;
    private boolean mPendingDeregi = false;
    private PhoneIdKeyMap<Boolean> mReadyNetwork;
    private int mRetryCount = 1;
    private IRegistrationManager mRm;
    private String mVerificationCode = null;
    private boolean mWifiNetwork = false;
    /* access modifiers changed from: private */
    public WorkFlowController mWorkFlowController;
    private SparseArray<HandlerThread> mWorkflowThreadList = new SparseArray<>();

    public ConfigModule(Looper looper, Context context, IRegistrationManager rm) {
        super(looper);
        this.mContext = context;
        this.mRm = rm;
        this.mEventLog = new SimpleEventLog(context, "Autoconfig", 100);
    }

    public void initSequentially() {
        IntentReceiver receiver = new IntentReceiver();
        for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
            if (sm.getSimMno() == Mno.KT) {
                receiver.addActionAirplaneMode();
            }
            sm.registerForSimRefresh(this, 12, (Object) null);
            sm.registerForSimRemoved(this, 12, (Object) null);
            sm.registerForSimReady(this, 11, (Object) null);
        }
        this.mContext.registerReceiver(receiver, receiver.getIntentFilter());
        int phoneCount = SimUtil.getPhoneCount();
        if (phoneCount > 1) {
            Log.d(LOG_TAG, " Registering for DDS");
            SimManagerFactory.registerForDDSChange(this, 10, (Object) null);
        }
        this.mNetworkListeners = new PhoneIdKeyMap<>(phoneCount, null);
        this.mNetworkLists = new PhoneIdKeyMap<>(phoneCount, null);
        for (int i = 0; i < phoneCount; i++) {
            this.mNetworkLists.put(i, new HashMap());
            this.mNetworkListeners.put(i, new HashMap());
        }
        this.mReadyNetwork = new PhoneIdKeyMap<>(phoneCount, false);
        this.mWorkFlowController = new WorkFlowController(this.mContext);
        this.mConfigTrigger = new ConfigTrigger(this.mContext, this.mRm, this, this.mEventLog);
        this.mConfigComplete = new ConfigComplete(this.mContext, this.mRm, this, this.mEventLog);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:144:0x040a, code lost:
        setAcsTryReason(r9, com.sec.internal.constants.ims.DiagnosisConstants.RCSA_ATRE.PUSH_SMS);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x066e, code lost:
        r1 = true;
        r6.mConfigTrigger.setReadyStartForceCmd(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x0674, code lost:
        r6.mConfigTrigger.setReadyStartCmdList(r9, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x0679, code lost:
        if (r10 != null) goto L_0x06c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:223:0x067b, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, "workflow is null");
        r0 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x0687, code lost:
        if (r0 == null) goto L_0x06bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:0x068d, code lost:
        if (r0.hasNoSim() != false) goto L_0x06bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x0697, code lost:
        if (android.text.TextUtils.isEmpty(r0.getSimMnoName()) == false) goto L_0x06a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x069d, code lost:
        if (r0.hasVsim() != false) goto L_0x06a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:231:0x06a0, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, "try init workflow and start again");
        sendMessage(obtainMessage(0, r9, 0, (java.lang.Object) null));
        sendMessage(obtainMessage(2, r9, 0, (java.lang.Object) null));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x06bb, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, "sim is not ready, start config finished");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:0x06c5, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "HANDLE_AUTO_CONFIG_START:");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x06d0, code lost:
        if (isGcEnabledChange(r9) == false) goto L_0x06d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x06d2, code lost:
        r10.clearToken();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x06d9, code lost:
        if (r10.checkNetworkConnectivity() != false) goto L_0x06ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x06db, code lost:
        r6.mConfigTrigger.tryAutoConfig(r10, r9, r6.mWorkFlowController.isSimInfochanged(r9, r6.mIsRemoteConfigNeeded), r6.mMobileNetwork);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:240:0x06ee, code lost:
        if (r6.mIsRemoteConfigNeeded == false) goto L_0x0702;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x06f0, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "need CurrConfig");
        r10.startCurrConfig();
        r6.mConfigTrigger.setReadyStartCmdList(r9, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x070e, code lost:
        if (com.sec.internal.ims.registry.ImsRegistry.getInt(r9, com.sec.internal.constants.ims.settings.GlobalSettingsConstants.RCS.AUTO_CONFIG_PDN, 0) != 1) goto L_0x072e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:245:0x0721, code lost:
        if (r6.mNetworkLists.get(r9).containsKey(2) == false) goto L_0x07cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:246:0x0723, code lost:
        r6.mReadyNetwork.put(r9, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:0x0732, code lost:
        if (isMobileDataOn() == false) goto L_0x076a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:250:0x0738, code lost:
        if (isRoamingMobileDataOn(r9) != false) goto L_0x073b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:251:0x073b, code lost:
        if (r14 == null) goto L_0x07cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:253:0x0741, code lost:
        if (r14.boolSetting(com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicy.PS_ONLY_NETWORK) == false) goto L_0x07cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:255:0x0747, code lost:
        if (isMobileDataOn() == false) goto L_0x07cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:257:0x074d, code lost:
        if (isWifiSwitchOn() == false) goto L_0x07cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:258:0x074f, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "Mobile Data ON & WIFI ON case for PS only network.");
        r0 = r6.mWorkFlowController.getCurrentRcsConfigVersion(r9);
        sendMessage(obtainMessage(3, r0, r0, java.lang.Integer.valueOf(r9)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x076a, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "Mobile Data is off or roaming data off in roaming area");
        r0 = r6.mWorkFlowController.getCurrentRcsConfigVersion(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:260:0x077e, code lost:
        if (r12.contains("wifi") == false) goto L_0x083e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x0784, code lost:
        if (isWifiSwitchOn() != false) goto L_0x0788;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:263:0x0788, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "Mobile Data is off but WiFi is on");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x0791, code lost:
        if (r11 != com.sec.internal.constants.Mno.CMCC) goto L_0x0796;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:265:0x0793, code lost:
        r6.mMobileNetwork = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x0796, code lost:
        if (r14 == null) goto L_0x07b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:268:0x079c, code lost:
        if (r14.boolSetting(com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicy.PS_ONLY_NETWORK) == false) goto L_0x07b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:269:0x079e, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "WiFi is on. Register to VOLTE to receive OTP message for PS only network");
        sendMessage(obtainMessage(3, r0, r0, java.lang.Integer.valueOf(r9)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:270:0x07b3, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "Mobile Data is off but WiFi is on. So wait 20 seconds.");
        removeMessages(3);
        sendMessageDelayed(obtainMessage(3, r0, r0, java.lang.Integer.valueOf(r9)), 20000);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:271:0x07cd, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "Auto Config Start: ReadyNetwork = " + r6.mReadyNetwork.get(r9) + ", Start command = " + r6.mConfigTrigger.getReadyStartCmdList(r9));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:272:0x0803, code lost:
        if (r6.mReadyNetwork.get(r9).booleanValue() == false) goto L_0x0816;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:273:0x0805, code lost:
        r6.mConfigTrigger.tryAutoConfig(r10, r9, r6.mWorkFlowController.isSimInfochanged(r9, r6.mIsRemoteConfigNeeded), r6.mMobileNetwork);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:274:0x0816, code lost:
        if (r14 == null) goto L_0x081e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x081c, code lost:
        if (r14.boolSetting(com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicy.PS_ONLY_NETWORK) != false) goto L_0x0828;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:278:0x0820, code lost:
        if (r11 != com.sec.internal.constants.Mno.BELL) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:280:0x0826, code lost:
        if (getAvailableNetwork(r9) != null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:281:0x0828, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "No conditions satisfied to start Auto Config, proceed to VOLTE REG");
        sendMessage(obtainMessage(3, 0, 0, java.lang.Integer.valueOf(r9)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:282:0x083e, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "Both Mobile Data and WiFi are off, skip autoconfig");
        sendMessage(obtainMessage(3, r0, r0, java.lang.Integer.valueOf(r9)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:353:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:354:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:355:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:356:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:357:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:358:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:359:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:360:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:361:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r20) {
        /*
            r19 = this;
            r6 = r19
            r7 = r20
            super.handleMessage(r20)
            int r0 = r7.arg1
            int r1 = r7.what
            r8 = 3
            if (r1 == r8) goto L_0x0022
            int r1 = r7.what
            r2 = 17
            if (r1 == r2) goto L_0x0022
            int r1 = r7.what
            r2 = 4
            if (r1 == r2) goto L_0x0022
            int r1 = r7.what
            r2 = 21
            if (r1 != r2) goto L_0x0020
            goto L_0x0022
        L_0x0020:
            r9 = r0
            goto L_0x002b
        L_0x0022:
            java.lang.Object r1 = r7.obj
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r0 = r1.intValue()
            r9 = r0
        L_0x002b:
            com.sec.internal.ims.config.WorkFlowController r0 = r6.mWorkFlowController
            com.sec.internal.interfaces.ims.config.IWorkflow r10 = r0.getWorkflow(r9)
            com.sec.internal.constants.Mno r11 = com.sec.internal.helper.SimUtil.getSimMno(r9)
            android.content.Context r0 = r6.mContext
            java.lang.String r12 = com.sec.internal.ims.util.ConfigUtil.getNetworkType(r0, r9)
            android.content.Context r0 = r6.mContext
            java.lang.String r13 = com.sec.internal.ims.util.ConfigUtil.getAcsServerType(r0, r9)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r14 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r9)
            r0 = 1
            r15 = 0
            if (r14 == 0) goto L_0x0051
            boolean r1 = r14.isRemoteConfigNeeded(r9)
            if (r1 == 0) goto L_0x0051
            r1 = r0
            goto L_0x0052
        L_0x0051:
            r1 = r15
        L_0x0052:
            r6.mIsRemoteConfigNeeded = r1
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "handleMessage: msg: "
            r2.append(r3)
            int r3 = r7.what
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r1, r9, r2)
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "rcsNetworkType: "
            r2.append(r3)
            r2.append(r12)
            java.lang.String r3 = " rcsAs: "
            r2.append(r3)
            r2.append(r13)
            java.lang.String r3 = " mIsRemoteConfigNeeded: "
            r2.append(r3)
            boolean r3 = r6.mIsRemoteConfigNeeded
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r1, r9, r2)
            int r1 = r7.what
            r2 = 800(0x320, float:1.121E-42)
            java.lang.String r3 = "phoneId"
            java.lang.String r4 = "lastError"
            r5 = 2
            switch(r1) {
                case 0: goto L_0x0854;
                case 1: goto L_0x066e;
                case 2: goto L_0x066c;
                case 3: goto L_0x0411;
                case 4: goto L_0x03c8;
                case 5: goto L_0x03bf;
                case 6: goto L_0x03b6;
                case 7: goto L_0x03ad;
                case 8: goto L_0x0368;
                case 9: goto L_0x035b;
                case 10: goto L_0x0318;
                case 11: goto L_0x0307;
                case 12: goto L_0x02e5;
                case 13: goto L_0x01fc;
                case 14: goto L_0x01f3;
                case 15: goto L_0x0196;
                case 16: goto L_0x018c;
                case 17: goto L_0x017e;
                case 18: goto L_0x0174;
                case 19: goto L_0x0107;
                case 20: goto L_0x00fe;
                case 21: goto L_0x040a;
                case 22: goto L_0x00f7;
                case 23: goto L_0x00f0;
                case 24: goto L_0x00d6;
                case 25: goto L_0x00ac;
                default: goto L_0x00a2;
            }
        L_0x00a2:
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "unknown message"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            goto L_0x0902
        L_0x00ac:
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.Network>> r0 = r6.mNetworkLists
            java.lang.Object r0 = r0.get(r9)
            java.util.HashMap r0 = (java.util.HashMap) r0
            int r1 = r7.arg2
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r0.remove(r1)
            if (r14 == 0) goto L_0x00d1
            java.lang.String r0 = "dual_simhandling"
            boolean r0 = r14.boolSetting(r0)
            if (r0 == 0) goto L_0x00d1
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "Clear workflow"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            r6.clearWorkFlow(r9)
        L_0x00d1:
            r6.processConnectionChange(r9)
            goto L_0x0902
        L_0x00d6:
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.Network>> r0 = r6.mNetworkLists
            java.lang.Object r0 = r0.get(r9)
            java.util.HashMap r0 = (java.util.HashMap) r0
            int r1 = r7.arg2
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            java.lang.Object r2 = r7.obj
            android.net.Network r2 = (android.net.Network) r2
            r0.put(r1, r2)
            r6.processConnectionChange(r9)
            goto L_0x0902
        L_0x00f0:
            com.sec.internal.ims.config.WorkFlowController r0 = r6.mWorkFlowController
            r0.onBootCompleted()
            goto L_0x0902
        L_0x00f7:
            if (r10 == 0) goto L_0x0902
            r10.clearAutoConfigStorage()
            goto L_0x0902
        L_0x00fe:
            if (r10 == 0) goto L_0x0902
            java.lang.String r0 = r6.mMsisdnNumber
            r10.sendMsisdnNumber(r0)
            goto L_0x0902
        L_0x0107:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "HANDLE_AUTO_CONFIG_RESTART:"
            com.sec.internal.log.IMSLog.i(r1, r9, r2)
            com.sec.internal.ims.config.ConfigTrigger r1 = r6.mConfigTrigger
            r1.setReadyStartCmdList(r9, r0)
            if (r10 != 0) goto L_0x012b
            r0 = 0
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "workflow is null. skip autoconfig"
            com.sec.internal.log.IMSLog.i(r1, r9, r2)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r9)
            android.os.Message r1 = r6.obtainMessage(r8, r0, r0, r1)
            r6.sendMessage(r1)
            goto L_0x0902
        L_0x012b:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "ReadyNetwork: "
            r1.append(r2)
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r2 = r6.mReadyNetwork
            java.lang.Object r2 = r2.get(r9)
            r1.append(r2)
            java.lang.String r2 = ", Start command: "
            r1.append(r2)
            com.sec.internal.ims.config.ConfigTrigger r2 = r6.mConfigTrigger
            boolean r2 = r2.getReadyStartCmdList(r9)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r0 = r6.mReadyNetwork
            java.lang.Object r0 = r0.get(r9)
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            boolean r0 = r0.booleanValue()
            if (r0 == 0) goto L_0x0902
            com.sec.internal.ims.config.ConfigTrigger r0 = r6.mConfigTrigger
            com.sec.internal.ims.config.WorkFlowController r1 = r6.mWorkFlowController
            boolean r2 = r6.mIsRemoteConfigNeeded
            boolean r1 = r1.isSimInfochanged(r9, r2)
            boolean r2 = r6.mMobileNetwork
            r0.tryAutoConfig(r10, r9, r1, r2)
            goto L_0x0902
        L_0x0174:
            if (r10 == 0) goto L_0x0179
            r10.onDefaultSmsPackageChanged()
        L_0x0179:
            r6.notifyDefaultSmsChanged(r9)
            goto L_0x0902
        L_0x017e:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "HANDLE_AUTO_CONFIG_START_WITH_SUITABLE_NETWORK retrigger ACS with best network"
            com.sec.internal.log.IMSLog.i(r1, r9, r2)
            com.sec.internal.ims.config.ConfigTrigger r1 = r6.mConfigTrigger
            r1.setReadyStartCmdList(r9, r0)
            goto L_0x0902
        L_0x018c:
            r6.init(r9)
            if (r10 == 0) goto L_0x0902
            r10.handleMSISDNDialog()
            goto L_0x0902
        L_0x0196:
            int r9 = r7.arg1
            com.sec.internal.ims.config.params.ACSConfig r1 = r6.getAcsConfig(r9)
            r1.resetAcsSettings()
            com.sec.internal.interfaces.ims.core.IRegistrationManager r1 = r6.mRm
            java.util.List r1 = r1.getPendingRegistration(r9)
            com.sec.internal.ims.config.params.ACSConfig r2 = r6.getAcsConfig(r9)
            boolean r2 = r2.isRcsDormantMode()
            if (r2 == 0) goto L_0x01cd
            android.content.Context r2 = r6.mContext
            android.content.res.Resources r3 = r2.getResources()
            r4 = 2131099700(0x7f060034, float:1.781176E38)
            java.lang.String r3 = r3.getString(r4)
            android.widget.Toast r2 = android.widget.Toast.makeText(r2, r3, r0)
            r2.show()
            com.sec.internal.ims.config.ConfigTrigger r2 = r6.mConfigTrigger
            r2.setStateforTriggeringACS(r9)
            r6.triggerAutoConfig(r0, r9, r1)
            goto L_0x0902
        L_0x01cd:
            boolean r2 = r11.isKor()
            if (r2 != 0) goto L_0x01e9
            boolean r2 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r11)
            if (r2 != 0) goto L_0x01e9
            java.lang.String r2 = "jibe"
            boolean r2 = r2.equals(r13)
            if (r2 == 0) goto L_0x0902
            com.sec.internal.interfaces.ims.core.IRegistrationManager r2 = r6.mRm
            boolean r2 = com.sec.internal.ims.util.ConfigUtil.hasChatbotService(r9, r2)
            if (r2 == 0) goto L_0x0902
        L_0x01e9:
            com.sec.internal.ims.config.ConfigTrigger r2 = r6.mConfigTrigger
            r2.setStateforTriggeringACS(r9)
            r6.triggerAutoConfig(r0, r9, r1)
            goto L_0x0902
        L_0x01f3:
            int r0 = r7.arg1
            int r1 = r7.arg2
            r6.onTelephonyCallStatusChanged(r0, r1)
            goto L_0x0902
        L_0x01fc:
            r1 = -1
            java.lang.Object r5 = r7.obj
            if (r5 == 0) goto L_0x0213
            java.lang.Object r5 = r7.obj
            android.os.Bundle r5 = (android.os.Bundle) r5
            int r1 = r5.getInt(r4)
            java.lang.Object r4 = r7.obj
            android.os.Bundle r4 = (android.os.Bundle) r4
            int r3 = r4.getInt(r3)
            r9 = r3
            goto L_0x0215
        L_0x0213:
            r3 = 0
            r9 = r3
        L_0x0215:
            boolean r3 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r11)
            if (r3 != 0) goto L_0x0235
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.CMCC
            if (r11 == r3) goto L_0x0235
            int r3 = r6.mCallState
            if (r3 == 0) goto L_0x0235
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "Pending Autoconfig comlete event on active call"
            com.sec.internal.log.IMSLog.i(r2, r9, r3)
            r6.mPendingAutoComplete = r0
            com.sec.internal.ims.config.params.ACSConfig r0 = r6.getAcsConfig(r9)
            r0.setAcsLastError(r1)
            goto L_0x0902
        L_0x0235:
            com.sec.internal.interfaces.ims.core.IRegistrationManager r0 = r6.mRm
            java.util.List r0 = r0.getPendingRegistration(r9)
            android.content.Context r3 = r6.mContext
            boolean r3 = com.sec.internal.ims.rcs.util.RcsUtils.DualRcs.isRegAllowed(r3, r9)
            if (r3 == 0) goto L_0x02be
            com.sec.internal.ims.config.ConfigComplete r3 = r6.mConfigComplete
            int r4 = r6.m403ForbiddenCounter
            r3.setStateforACSComplete(r1, r9, r0, r4)
            com.sec.internal.ims.config.ConfigComplete r3 = r6.mConfigComplete
            com.sec.internal.ims.config.WorkFlowController r4 = r6.mWorkFlowController
            com.sec.internal.interfaces.ims.config.IWorkflow r4 = r4.getWorkflow(r9)
            r3.handleAutoconfigurationComplete(r9, r0, r1, r4)
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.KT
            if (r11 != r3) goto L_0x02e3
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "mNeedRetryOverWifi = "
            r4.append(r5)
            boolean r5 = r6.mNeedRetryOverWifi
            r4.append(r5)
            java.lang.String r5 = ", mWifiNetwork = "
            r4.append(r5)
            boolean r5 = r6.mWifiNetwork
            r4.append(r5)
            java.lang.String r5 = ", lastErrorCode = "
            r4.append(r5)
            r4.append(r1)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.i(r3, r9, r4)
            boolean r3 = r6.mNeedRetryOverWifi
            if (r3 == 0) goto L_0x02e3
            if (r1 == 0) goto L_0x02aa
            if (r1 == r2) goto L_0x02aa
            r2 = 801(0x321, float:1.122E-42)
            if (r1 == r2) goto L_0x02aa
            r2 = 802(0x322, float:1.124E-42)
            if (r1 == r2) goto L_0x02aa
            r2 = 803(0x323, float:1.125E-42)
            if (r1 == r2) goto L_0x02aa
            r2 = 804(0x324, float:1.127E-42)
            if (r1 == r2) goto L_0x02aa
            r2 = 805(0x325, float:1.128E-42)
            if (r1 != r2) goto L_0x02a0
            goto L_0x02aa
        L_0x02a0:
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "clear mNeedRetryOverWifi to false"
            com.sec.internal.log.IMSLog.i(r2, r9, r3)
            r6.mNeedRetryOverWifi = r15
            goto L_0x02e3
        L_0x02aa:
            boolean r2 = r6.mWifiNetwork
            if (r2 == 0) goto L_0x02e3
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "reset AcsSettings for KT over Wifi"
            com.sec.internal.log.IMSLog.i(r2, r9, r3)
            com.sec.internal.ims.config.params.ACSConfig r2 = r6.getAcsConfig(r9)
            r2.resetAcsSettings()
            goto L_0x02e3
        L_0x02be:
            java.util.Iterator r2 = r0.iterator()
        L_0x02c2:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x02dc
            java.lang.Object r3 = r2.next()
            com.sec.internal.interfaces.ims.core.IRegisterTask r3 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r3
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = r3.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURING
            if (r4 != r5) goto L_0x02db
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            r3.setState(r4)
        L_0x02db:
            goto L_0x02c2
        L_0x02dc:
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "DDS set to other SIM"
            com.sec.internal.log.IMSLog.i(r2, r9, r3)
        L_0x02e3:
            goto L_0x0902
        L_0x02e5:
            java.lang.Object r0 = r7.obj
            com.sec.internal.helper.AsyncResult r0 = (com.sec.internal.helper.AsyncResult) r0
            java.lang.Object r1 = r0.result
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            r6.onSimRefresh(r1)
            boolean r1 = r11.isKor()
            if (r1 == 0) goto L_0x0902
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "sim state changed, reset to MSISDN_FROM_PAU"
            com.sec.internal.log.IMSLog.i(r1, r9, r2)
            r6.resetMsisdnFromPau(r9)
            goto L_0x0902
        L_0x0307:
            java.lang.Object r0 = r7.obj
            com.sec.internal.helper.AsyncResult r0 = (com.sec.internal.helper.AsyncResult) r0
            java.lang.Object r1 = r0.result
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            r6.onSimReady(r1)
            goto L_0x0902
        L_0x0318:
            r6.getAvailableNetwork(r9)
            com.sec.internal.ims.config.ConfigTrigger r0 = r6.mConfigTrigger
            r0.setReadyStartCmdList(r9, r15)
            com.sec.internal.interfaces.ims.core.IRegistrationManager r0 = r6.mRm
            java.util.List r0 = r0.getPendingRegistration(r9)
            java.util.Iterator r0 = r0.iterator()
        L_0x032a:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0352
            java.lang.Object r1 = r0.next()
            com.sec.internal.interfaces.ims.core.IRegisterTask r1 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r1
            boolean r2 = r1.isRcsOnly()
            if (r2 == 0) goto L_0x0351
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = r1.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r3 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURING
            if (r2 != r3) goto L_0x0351
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "task is set as IDLE because of dds change."
            com.sec.internal.log.IMSLog.i(r2, r9, r3)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            r1.setState(r2)
        L_0x0351:
            goto L_0x032a
        L_0x0352:
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "Network configs are reset"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            goto L_0x0902
        L_0x035b:
            r6.setDualSimRcsAutoConfig(r0)
            r0 = 0
            android.os.Message r0 = r6.obtainMessage(r5, r9, r15, r0)
            r6.sendMessage(r0)
            goto L_0x0902
        L_0x0368:
            if (r10 == 0) goto L_0x0902
            java.lang.String r1 = r6.getRcsProfile(r9)
            boolean r1 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r1)
            if (r1 == 0) goto L_0x03a6
            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r11)
            if (r1 == 0) goto L_0x03a6
            boolean r1 = r6.mPendingAutoConfig
            if (r1 != 0) goto L_0x038b
            boolean r1 = r6.mIsRcsEnabled
            if (r1 != 0) goto L_0x0384
            r6.mPendingAutoConfig = r0
        L_0x0384:
            boolean r0 = r6.mIsRcsEnabled
            r10.changeOpMode(r0)
            goto L_0x0902
        L_0x038b:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "pending AutoConfig mIsRcsEnabled: "
            r1.append(r2)
            boolean r2 = r6.mIsRcsEnabled
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            goto L_0x0902
        L_0x03a6:
            boolean r0 = r6.mIsRcsEnabled
            r10.changeOpMode(r0)
            goto L_0x0902
        L_0x03ad:
            if (r10 == 0) goto L_0x0902
            java.lang.String r0 = r6.mVerificationCode
            r10.sendVerificationCode(r0)
            goto L_0x0902
        L_0x03b6:
            if (r10 == 0) goto L_0x0902
            com.sec.ims.IAutoConfigurationListener r0 = r6.mListener
            r10.unregisterAutoConfigurationListener(r0)
            goto L_0x0902
        L_0x03bf:
            if (r10 == 0) goto L_0x0902
            com.sec.ims.IAutoConfigurationListener r0 = r6.mListener
            r10.registerAutoConfigurationListener(r0)
            goto L_0x0902
        L_0x03c8:
            com.sec.internal.ims.config.ConfigTrigger r1 = r6.mConfigTrigger
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.MTS_RUSSIA
            if (r11 == r2) goto L_0x03d0
            r2 = r0
            goto L_0x03d1
        L_0x03d0:
            r2 = r15
        L_0x03d1:
            r1.setNeedResetConfig(r2)
            com.sec.internal.interfaces.ims.core.IRegistrationManager r1 = r6.mRm
            java.util.List r1 = r1.getPendingRegistration(r9)
            java.util.Iterator r2 = r1.iterator()
        L_0x03de:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x0403
            java.lang.Object r3 = r2.next()
            com.sec.internal.interfaces.ims.core.IRegisterTask r3 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r3
            boolean r4 = r3.isRcsOnly()
            if (r4 == 0) goto L_0x0401
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            boolean r4 = r4.getNeedAutoconfig()
            if (r4 == 0) goto L_0x0401
            com.sec.internal.interfaces.ims.core.IRegistrationManager r4 = r6.mRm
            r8 = 143(0x8f, float:2.0E-43)
            r4.sendDeregister((int) r8, (int) r9)
        L_0x0401:
            r8 = 3
            goto L_0x03de
        L_0x0403:
            com.sec.internal.ims.config.params.ACSConfig r2 = r6.getAcsConfig(r9)
            r2.setIsTriggeredByNrcr(r0)
        L_0x040a:
            com.sec.internal.constants.ims.DiagnosisConstants$RCSA_ATRE r1 = com.sec.internal.constants.ims.DiagnosisConstants.RCSA_ATRE.PUSH_SMS
            r6.setAcsTryReason(r9, r1)
            goto L_0x066e
        L_0x0411:
            java.lang.String r1 = LOG_TAG
            java.lang.String r8 = "HANDLE_AUTO_CONFIG_COMPLETE:"
            com.sec.internal.log.IMSLog.i(r1, r9, r8)
            int r1 = r7.arg1
            int r8 = r7.arg2
            if (r10 != 0) goto L_0x0421
            r16 = 708(0x2c4, float:9.92E-43)
            goto L_0x0425
        L_0x0421:
            int r16 = r10.getLastErrorCode()
        L_0x0425:
            r17 = r16
            com.sec.internal.interfaces.ims.core.ISimManager r16 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r9)
            if (r16 == 0) goto L_0x044a
            boolean r18 = r16.isSimAvailable()
            if (r18 == 0) goto L_0x044a
            android.content.Context r15 = r6.mContext
            boolean r15 = com.sec.internal.ims.util.ConfigUtil.isGcForEur(r15, r9)
            if (r15 == 0) goto L_0x044a
            if (r8 != 0) goto L_0x044a
            java.lang.String r15 = LOG_TAG
            java.lang.String r5 = "it needs to perform again Auto-configuration process"
            com.sec.internal.log.IMSLog.i(r15, r9, r5)
            android.content.Context r5 = r6.mContext
            r15 = -1
            com.sec.internal.constants.ims.ImsConstants.SystemSettings.setRcsUserSetting(r5, r15, r9)
        L_0x044a:
            com.sec.internal.helper.SimpleEventLog r5 = r6.mEventLog
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r2 = "Autoconfig complete: old version = "
            r15.append(r2)
            r15.append(r1)
            java.lang.String r2 = ", new version = "
            r15.append(r2)
            r15.append(r8)
            java.lang.String r2 = ", last errorcode = "
            r15.append(r2)
            r2 = r17
            r15.append(r2)
            java.lang.String r15 = r15.toString()
            r5.logAndAdd(r9, r15)
            r5 = 318767110(0x13000006, float:1.6155883E-27)
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            r15.append(r9)
            java.lang.String r0 = ",OV:"
            r15.append(r0)
            r15.append(r1)
            java.lang.String r0 = ",NV:"
            r15.append(r0)
            r15.append(r8)
            java.lang.String r0 = ",LEC:"
            r15.append(r0)
            r15.append(r2)
            java.lang.String r0 = r15.toString()
            com.sec.internal.log.IMSLog.c(r5, r0)
            boolean r0 = r6.mIsRemoteConfigNeeded
            if (r0 == 0) goto L_0x04a9
            if (r8 <= 0) goto L_0x04a9
            r0 = 59
            if (r8 != r0) goto L_0x04a7
            goto L_0x04a9
        L_0x04a7:
            r0 = 0
            goto L_0x04aa
        L_0x04a9:
            r0 = 1
        L_0x04aa:
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r7 = "localConfigUsedState: "
            r15.append(r7)
            r15.append(r0)
            java.lang.String r7 = r15.toString()
            com.sec.internal.log.IMSLog.i(r5, r9, r7)
            r5 = 318767116(0x1300000c, float:1.6155894E-27)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r9)
            java.lang.String r15 = ",LCUS:"
            r7.append(r15)
            r7.append(r0)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.c(r5, r7)
            if (r14 == 0) goto L_0x04df
            r14.updateLocalConfigUsedState(r0)
        L_0x04df:
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r15 = "AcsTryReason: "
            r7.append(r15)
            com.sec.internal.constants.ims.DiagnosisConstants$RCSA_ATRE r15 = r6.getAcsTryReason(r9)
            java.lang.String r15 = r15.toString()
            r7.append(r15)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.i(r5, r9, r7)
            r5 = 987(0x3db, float:1.383E-42)
            if (r2 == r5) goto L_0x0547
            com.sec.internal.ims.config.WorkFlowController r5 = r6.mWorkFlowController
            boolean r5 = r5.getIsAcsFirstTry(r9)
            r7 = 200(0xc8, float:2.8E-43)
            if (r5 != 0) goto L_0x050f
            if (r2 != r7) goto L_0x050f
            if (r8 >= 0) goto L_0x0519
        L_0x050f:
            com.sec.internal.ims.config.ConfigComplete r5 = r6.mConfigComplete
            r5.sendRCSAInfoToHQM(r8, r2, r9)
            com.sec.internal.ims.config.WorkFlowController r5 = r6.mWorkFlowController
            r5.removeIsAcsFirstTry(r9)
        L_0x0519:
            android.content.ContentValues r5 = new android.content.ContentValues
            r5.<init>()
            if (r2 != r7) goto L_0x052b
            r7 = 1
            java.lang.Integer r15 = java.lang.Integer.valueOf(r7)
            java.lang.String r7 = "RACC"
            r5.put(r7, r15)
            goto L_0x0535
        L_0x052b:
            r7 = 1
            java.lang.Integer r15 = java.lang.Integer.valueOf(r7)
            java.lang.String r7 = "RACF"
            r5.put(r7, r15)
        L_0x0535:
            r7 = 1
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            java.lang.String r15 = "overwrite_mode"
            r5.put(r15, r7)
            android.content.Context r7 = r6.mContext
            java.lang.String r15 = "DRCS"
            com.sec.internal.ims.diagnosis.ImsLogAgentUtil.storeLogToAgent(r9, r7, r15, r5)
        L_0x0547:
            r5 = 800(0x320, float:1.121E-42)
            if (r2 != r5) goto L_0x057a
            java.lang.String r5 = LOG_TAG
            java.lang.String r7 = "SSL Handshake failed"
            com.sec.internal.log.IMSLog.i(r5, r9, r7)
            r6.startAcsWithDelay(r9)
            android.util.SparseArray<android.os.Message> r5 = r6.mOnCompleteList
            java.lang.Object r5 = r5.get(r9)
            android.os.Message r5 = (android.os.Message) r5
            if (r5 == 0) goto L_0x0902
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.RJIL
            if (r11 != r7) goto L_0x0902
            android.os.Bundle r7 = new android.os.Bundle
            r7.<init>()
            r7.putInt(r3, r9)
            r7.putInt(r4, r2)
            r5.obj = r7
            r5.sendToTarget()
            android.util.SparseArray<android.os.Message> r3 = r6.mOnCompleteList
            r3.remove(r9)
            goto L_0x0902
        L_0x057a:
            if (r16 == 0) goto L_0x058f
            boolean r5 = r16.isSimAvailable()
            if (r5 != 0) goto L_0x058f
            r5 = 708(0x2c4, float:9.92E-43)
            if (r2 != r5) goto L_0x058f
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "autoconfiguration failed because sim is unavailable."
            com.sec.internal.log.IMSLog.i(r3, r9, r4)
            goto L_0x0902
        L_0x058f:
            android.util.SparseArray<android.os.Message> r5 = r6.mOnCompleteList
            java.lang.Object r5 = r5.get(r9)
            android.os.Message r5 = (android.os.Message) r5
            if (r5 == 0) goto L_0x05c5
            java.lang.String r7 = LOG_TAG
            java.lang.String r15 = "send complete message"
            com.sec.internal.log.IMSLog.i(r7, r9, r15)
            if (r8 == 0) goto L_0x05ad
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.CMCC
            if (r11 != r7) goto L_0x05ad
            com.sec.internal.ims.config.ConfigTrigger r7 = r6.mConfigTrigger
            r15 = 0
            r7.setReadyStartCmdList(r9, r15)
        L_0x05ad:
            android.os.Bundle r7 = new android.os.Bundle
            r7.<init>()
            r7.putInt(r3, r9)
            r7.putInt(r4, r2)
            r5.obj = r7
            r5.sendToTarget()
            r5 = 0
            android.util.SparseArray<android.os.Message> r3 = r6.mOnCompleteList
            r3.remove(r9)
            goto L_0x0640
        L_0x05c5:
            boolean r3 = r6.mIsRemoteConfigNeeded
            if (r3 == 0) goto L_0x05eb
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "complete autoconfiguration and send EVENT_AUTOCONFIGURATION_COMPLETE msg"
            com.sec.internal.log.IMSLog.i(r3, r9, r4)
            r3 = 318767117(0x1300000d, float:1.6155896E-27)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r9)
            java.lang.String r7 = ",CONINFO:CHA"
            r4.append(r7)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.c(r3, r4)
            r6.onNewRcsConfigurationAvailable(r9, r2)
            goto L_0x0640
        L_0x05eb:
            boolean r3 = r11.isKor()
            if (r3 != 0) goto L_0x061f
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.ATT
            if (r11 == r3) goto L_0x061f
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TMOUS
            if (r11 == r3) goto L_0x061f
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.CMCC
            if (r11 == r3) goto L_0x061f
            if (r1 != r8) goto L_0x061f
            r3 = 318767118(0x1300000e, float:1.6155898E-27)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r9)
            java.lang.String r7 = ",CONINFO:NONCHA"
            r4.append(r7)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.c(r3, r4)
            com.sec.internal.helper.SimpleEventLog r3 = r6.mEventLog
            java.lang.String r4 = "same version. no event"
            r3.logAndAdd(r9, r4)
            goto L_0x0640
        L_0x061f:
            r3 = 318767119(0x1300000f, float:1.61559E-27)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r9)
            java.lang.String r7 = ",NEEDRECON"
            r4.append(r7)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.c(r3, r4)
            com.sec.internal.helper.SimpleEventLog r3 = r6.mEventLog
            java.lang.String r4 = "no exist complete message. send EVENT_RECONFIGURATION"
            r3.logAndAdd(r9, r4)
            r6.onNewRcsConfigurationAvailable(r9, r2)
        L_0x0640:
            boolean r3 = r6.mPendingAutoConfig
            if (r3 == 0) goto L_0x0902
            r3 = 0
            r6.mPendingAutoConfig = r3
            boolean r4 = r6.mIsRcsEnabled
            if (r4 == 0) goto L_0x0902
            com.sec.internal.ims.config.params.ACSConfig r4 = r6.getAcsConfig(r9)
            r4.resetAcsSettings()
            r4 = 8
            r6.removeMessages(r4)
            r7 = 0
            android.os.Message r4 = r6.obtainMessage(r4, r9, r3, r7)
            r6.sendMessage(r4)
            r4 = 2
            r6.removeMessages(r4)
            android.os.Message r3 = r6.obtainMessage(r4, r9, r3, r7)
            r6.sendMessage(r3)
            goto L_0x0902
        L_0x066c:
            r1 = 1
            goto L_0x0674
        L_0x066e:
            com.sec.internal.ims.config.ConfigTrigger r0 = r6.mConfigTrigger
            r1 = 1
            r0.setReadyStartForceCmd(r1)
        L_0x0674:
            com.sec.internal.ims.config.ConfigTrigger r0 = r6.mConfigTrigger
            r0.setReadyStartCmdList(r9, r1)
            if (r10 != 0) goto L_0x06c5
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "workflow is null"
            com.sec.internal.log.IMSLog.i(r0, r1)
            com.sec.internal.interfaces.ims.core.ISimManager r0 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r9)
            if (r0 == 0) goto L_0x06bb
            boolean r1 = r0.hasNoSim()
            if (r1 != 0) goto L_0x06bb
            java.lang.String r1 = r0.getSimMnoName()
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 == 0) goto L_0x06a0
            boolean r1 = r0.hasVsim()
            if (r1 != 0) goto L_0x06a0
            goto L_0x06bb
        L_0x06a0:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "try init workflow and start again"
            com.sec.internal.log.IMSLog.i(r1, r2)
            r1 = 0
            r2 = 0
            android.os.Message r3 = r6.obtainMessage(r2, r9, r2, r1)
            r6.sendMessage(r3)
            r3 = 2
            android.os.Message r1 = r6.obtainMessage(r3, r9, r2, r1)
            r6.sendMessage(r1)
            goto L_0x0902
        L_0x06bb:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "sim is not ready, start config finished"
            com.sec.internal.log.IMSLog.i(r1, r2)
            goto L_0x0902
        L_0x06c5:
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "HANDLE_AUTO_CONFIG_START:"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            boolean r0 = r6.isGcEnabledChange(r9)
            if (r0 == 0) goto L_0x06d5
            r10.clearToken()
        L_0x06d5:
            boolean r0 = r10.checkNetworkConnectivity()
            if (r0 != 0) goto L_0x06ec
            com.sec.internal.ims.config.ConfigTrigger r0 = r6.mConfigTrigger
            com.sec.internal.ims.config.WorkFlowController r1 = r6.mWorkFlowController
            boolean r2 = r6.mIsRemoteConfigNeeded
            boolean r1 = r1.isSimInfochanged(r9, r2)
            boolean r2 = r6.mMobileNetwork
            r0.tryAutoConfig(r10, r9, r1, r2)
            goto L_0x0902
        L_0x06ec:
            boolean r0 = r6.mIsRemoteConfigNeeded
            if (r0 == 0) goto L_0x0702
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "need CurrConfig"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            r10.startCurrConfig()
            com.sec.internal.ims.config.ConfigTrigger r0 = r6.mConfigTrigger
            r1 = 0
            r0.setReadyStartCmdList(r9, r1)
            goto L_0x0902
        L_0x0702:
            r1 = 0
            java.lang.String r0 = "rcs_auto_config_pdn"
            int r0 = com.sec.internal.ims.registry.ImsRegistry.getInt(r9, r0, r1)
            java.lang.String r1 = "ps_only_network"
            r2 = 1
            if (r0 != r2) goto L_0x072e
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.Network>> r0 = r6.mNetworkLists
            java.lang.Object r0 = r0.get(r9)
            java.util.HashMap r0 = (java.util.HashMap) r0
            r3 = 2
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            boolean r0 = r0.containsKey(r3)
            if (r0 == 0) goto L_0x07cd
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r0 = r6.mReadyNetwork
            java.lang.Boolean r2 = java.lang.Boolean.valueOf(r2)
            r0.put(r9, r2)
            goto L_0x07cd
        L_0x072e:
            boolean r0 = r19.isMobileDataOn()
            if (r0 == 0) goto L_0x076a
            boolean r0 = r6.isRoamingMobileDataOn(r9)
            if (r0 != 0) goto L_0x073b
            goto L_0x076a
        L_0x073b:
            if (r14 == 0) goto L_0x0769
            boolean r0 = r14.boolSetting(r1)
            if (r0 == 0) goto L_0x0769
            boolean r0 = r19.isMobileDataOn()
            if (r0 == 0) goto L_0x07cd
            boolean r0 = r19.isWifiSwitchOn()
            if (r0 == 0) goto L_0x07cd
            java.lang.String r0 = LOG_TAG
            java.lang.String r2 = "Mobile Data ON & WIFI ON case for PS only network."
            com.sec.internal.log.IMSLog.i(r0, r9, r2)
            com.sec.internal.ims.config.WorkFlowController r0 = r6.mWorkFlowController
            int r0 = r0.getCurrentRcsConfigVersion(r9)
            java.lang.Integer r2 = java.lang.Integer.valueOf(r9)
            r3 = 3
            android.os.Message r2 = r6.obtainMessage(r3, r0, r0, r2)
            r6.sendMessage(r2)
            goto L_0x07cd
        L_0x0769:
            goto L_0x07cd
        L_0x076a:
            java.lang.String r0 = LOG_TAG
            java.lang.String r2 = "Mobile Data is off or roaming data off in roaming area"
            com.sec.internal.log.IMSLog.i(r0, r9, r2)
            com.sec.internal.ims.config.WorkFlowController r0 = r6.mWorkFlowController
            int r0 = r0.getCurrentRcsConfigVersion(r9)
            java.lang.String r2 = "wifi"
            boolean r2 = r12.contains(r2)
            if (r2 == 0) goto L_0x083e
            boolean r2 = r19.isWifiSwitchOn()
            if (r2 != 0) goto L_0x0788
            goto L_0x083e
        L_0x0788:
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "Mobile Data is off but WiFi is on"
            com.sec.internal.log.IMSLog.i(r2, r9, r3)
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.CMCC
            if (r11 != r2) goto L_0x0796
            r2 = 0
            r6.mMobileNetwork = r2
        L_0x0796:
            if (r14 == 0) goto L_0x07b3
            boolean r2 = r14.boolSetting(r1)
            if (r2 == 0) goto L_0x07b3
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "WiFi is on. Register to VOLTE to receive OTP message for PS only network"
            com.sec.internal.log.IMSLog.i(r2, r9, r3)
            java.lang.Integer r2 = java.lang.Integer.valueOf(r9)
            r3 = 3
            android.os.Message r2 = r6.obtainMessage(r3, r0, r0, r2)
            r6.sendMessage(r2)
            goto L_0x0769
        L_0x07b3:
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "Mobile Data is off but WiFi is on. So wait 20 seconds."
            com.sec.internal.log.IMSLog.i(r2, r9, r3)
            r2 = 3
            r6.removeMessages(r2)
            java.lang.Integer r3 = java.lang.Integer.valueOf(r9)
            android.os.Message r3 = r6.obtainMessage(r2, r0, r0, r3)
            r4 = 20000(0x4e20, double:9.8813E-320)
            r6.sendMessageDelayed(r3, r4)
            goto L_0x0769
        L_0x07cd:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Auto Config Start: ReadyNetwork = "
            r2.append(r3)
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r3 = r6.mReadyNetwork
            java.lang.Object r3 = r3.get(r9)
            r2.append(r3)
            java.lang.String r3 = ", Start command = "
            r2.append(r3)
            com.sec.internal.ims.config.ConfigTrigger r3 = r6.mConfigTrigger
            boolean r3 = r3.getReadyStartCmdList(r9)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r9, r2)
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r0 = r6.mReadyNetwork
            java.lang.Object r0 = r0.get(r9)
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            boolean r0 = r0.booleanValue()
            if (r0 == 0) goto L_0x0816
            com.sec.internal.ims.config.ConfigTrigger r0 = r6.mConfigTrigger
            com.sec.internal.ims.config.WorkFlowController r1 = r6.mWorkFlowController
            boolean r2 = r6.mIsRemoteConfigNeeded
            boolean r1 = r1.isSimInfochanged(r9, r2)
            boolean r2 = r6.mMobileNetwork
            r0.tryAutoConfig(r10, r9, r1, r2)
            goto L_0x0902
        L_0x0816:
            if (r14 == 0) goto L_0x081e
            boolean r0 = r14.boolSetting(r1)
            if (r0 != 0) goto L_0x0828
        L_0x081e:
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.BELL
            if (r11 != r0) goto L_0x0902
            android.util.Pair r0 = r6.getAvailableNetwork(r9)
            if (r0 != 0) goto L_0x0902
        L_0x0828:
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "No conditions satisfied to start Auto Config, proceed to VOLTE REG"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            java.lang.Integer r0 = java.lang.Integer.valueOf(r9)
            r1 = 3
            r2 = 0
            android.os.Message r0 = r6.obtainMessage(r1, r2, r2, r0)
            r6.sendMessage(r0)
            goto L_0x0902
        L_0x083e:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "Both Mobile Data and WiFi are off, skip autoconfig"
            com.sec.internal.log.IMSLog.i(r1, r9, r2)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r9)
            r2 = 3
            android.os.Message r1 = r6.obtainMessage(r2, r0, r0, r1)
            r6.sendMessage(r1)
            goto L_0x0902
        L_0x0854:
            if (r10 != 0) goto L_0x08f6
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "HANDLE_AUTO_CONFIG_INIT:"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            boolean r0 = r6.rcsProfileInit(r9)
            if (r0 != 0) goto L_0x086c
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "SIM is not ready. skip init workflow"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            goto L_0x0902
        L_0x086c:
            android.os.HandlerThread r0 = new android.os.HandlerThread
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "workflowThread_"
            r1.append(r2)
            r1.append(r9)
            java.lang.String r1 = r1.toString()
            r0.<init>(r1)
            r7 = r0
            r7.start()
            com.sec.internal.ims.config.CustomizationManager r0 = new com.sec.internal.ims.config.CustomizationManager
            r0.<init>()
            android.os.Looper r1 = r7.getLooper()
            android.content.Context r2 = r6.mContext
            com.sec.internal.interfaces.ims.core.IRegistrationManager r3 = r6.mRm
            boolean r5 = com.sec.internal.ims.util.ConfigUtil.hasChatbotService(r9, r3)
            r3 = r19
            r4 = r9
            com.sec.internal.interfaces.ims.config.IWorkflow r10 = r0.getConfigManager(r1, r2, r3, r4, r5)
            r6.clearWorkFlowThread(r9)
            android.util.SparseArray<android.os.HandlerThread> r0 = r6.mWorkflowThreadList
            r0.put(r9, r7)
            if (r10 != 0) goto L_0x08c4
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "workflow is null. skip init workflow, regard old version and new version as 0"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            com.sec.internal.ims.config.WorkFlowController r0 = r6.mWorkFlowController
            r0.removeWorkFlow(r9)
            java.lang.Integer r0 = java.lang.Integer.valueOf(r9)
            r1 = 3
            r2 = 0
            android.os.Message r0 = r6.obtainMessage(r1, r2, r2, r0)
            r6.sendMessage(r0)
            goto L_0x0902
        L_0x08c4:
            com.sec.internal.ims.config.WorkFlowController r0 = r6.mWorkFlowController
            r0.initWorkflow(r9, r10)
            r0 = 318767115(0x1300000b, float:1.6155893E-27)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r9)
            java.lang.String r2 = ",WF:CR"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.c(r0, r1)
            boolean r0 = r6.mIsRemoteConfigNeeded
            if (r0 == 0) goto L_0x08f5
            boolean r0 = r6.mIsReceivedSimRefresh
            if (r0 == 0) goto L_0x08f5
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "clear config info because of sim refresh"
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            r10.clearAutoConfigStorage()
            r0 = 0
            r6.mIsReceivedSimRefresh = r0
        L_0x08f5:
            goto L_0x0902
        L_0x08f6:
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "re-init Workflow if needed."
            com.sec.internal.log.IMSLog.i(r0, r9, r1)
            r10.reInitIfNeeded()
        L_0x0902:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.ConfigModule.handleMessage(android.os.Message):void");
    }

    /* access modifiers changed from: protected */
    public void onSimRefresh(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onSimRefresh:");
        this.mConfigTrigger.setReadyStartCmdList(phoneId, false);
        getAcsConfig(phoneId).clear();
        this.mReadyNetwork.put(phoneId, false);
        deregisterNetworkCallback(phoneId);
        this.mIsReceivedSimRefresh = true;
        this.mWorkFlowController.onSimRefresh(phoneId);
    }

    /* access modifiers changed from: protected */
    public void onTelephonyCallStatusChanged(int phoneId, int callState) {
        this.mCallState = callState;
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "onTelephonyCallStatusChanged: " + this.mCallState);
        if (this.mCallState == 0) {
            boolean isSmsDefault = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, phoneId) == 1;
            if (this.mPendingAutoComplete) {
                this.mPendingAutoComplete = false;
                Bundle bundle = new Bundle();
                bundle.putInt("lastError", getAcsConfig(phoneId).getAcsLastError());
                sendMessage(obtainMessage(13, bundle));
            } else if (this.mPendingDeregi) {
                this.mPendingDeregi = false;
                List<IRegisterTask> rtl = this.mRm.getPendingRegistration(phoneId);
                if (rtl != null) {
                    for (IRegisterTask task : rtl) {
                        if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && task.getPdnType() != 15) {
                            task.setDeregiReason(36);
                            this.mRm.deregister(task, false, true, "MsgApp is changed");
                        } else if (isSmsDefault) {
                            this.mRm.requestTryRegister(task.getPhoneId());
                        }
                    }
                }
            }
        }
    }

    public ACSConfig getAcsConfig(int phoneId) {
        return this.mWorkFlowController.getAcsConfig(phoneId);
    }

    public void setRegisterFromApp(boolean tryregi, int phoneId) {
        this.mConfigTrigger.setRegisterFromApp(tryregi, phoneId);
    }

    public void updateTelephonyCallStatus(int phoneId, int callStatus) {
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "updateTelephonyCallStatus: " + callStatus);
        sendMessage(obtainMessage(14, phoneId, callStatus, (Object) null));
    }

    public void onNewRcsConfigurationAvailable(int phoneId, int lastErrorCode) {
        Bundle bundle = new Bundle();
        bundle.putInt("phoneId", phoneId);
        bundle.putInt("lastError", lastErrorCode);
        sendMessage(obtainMessage(13, bundle));
    }

    public boolean isWaitAutoconfig(IRegisterTask task) {
        return this.mConfigTrigger.isWaitAutoconfig(task);
    }

    public boolean isSimMoActivatedAndRcsEurSupported(int phoneId, ISimManager sm, IRegistrationManager rm) {
        return ImsUtil.isSimMobilityActivated(phoneId) && ConfigUtil.checkSupportSimMobilityForRcsEur(phoneId, sm, rm);
    }

    public boolean triggerAutoConfig(boolean forceAutoconfig, int phoneId, List<IRegisterTask> regiTaskList) {
        return this.mConfigTrigger.triggerAutoConfig(forceAutoconfig, phoneId, regiTaskList);
    }

    public Message obtainConfigMessage(int what, Bundle bundle) {
        return obtainMessage(what, bundle);
    }

    public void sendConfigMessage(int what, int phoneId) {
        sendMessage(obtainMessage(what, phoneId, 0, (Object) null));
    }

    public void startAutoConfig(boolean forced, Message onComplete, int phoneId) {
        if (onComplete == null) {
            Bundle bundle = new Bundle();
            bundle.putInt("phoneId", phoneId);
            onComplete = obtainMessage(13, bundle);
        }
        this.mOnCompleteList.put(phoneId, onComplete);
        this.mConfigTrigger.startAutoConfig(forced, onComplete, phoneId);
    }

    public void startAutoConfigDualsim(int phoneId, Message onComplete) {
        this.mOnCompleteList.put(phoneId, onComplete);
        this.mConfigTrigger.startAutoConfigDualsim(phoneId, onComplete);
    }

    public void registerAutoConfigurationListener(IAutoConfigurationListener listener, int phoneId) {
        this.mListener = listener;
        this.mConfigTrigger.startConfig(5, (Message) null, phoneId);
    }

    public void unregisterAutoConfigurationListener(IAutoConfigurationListener listener, int phoneId) {
        this.mListener = listener;
        this.mConfigTrigger.startConfig(6, (Message) null, phoneId);
    }

    public void sendVerificationCode(String value, int phoneId) {
        this.mVerificationCode = value;
        this.mConfigTrigger.startConfig(7, (Message) null, phoneId);
    }

    public void sendMsisdnNumber(String value, int phoneId) {
        this.mMsisdnNumber = value;
        this.mConfigTrigger.startConfig(20, (Message) null, phoneId);
    }

    public void changeOpMode(boolean isRcsEnabled, int phoneId, int tcPopupUserAccept) {
        this.mIsRcsEnabled = isRcsEnabled;
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "changeOpMode: mIsRcsEnabled: " + this.mIsRcsEnabled);
        IMSLog.c(LogClass.CM_OP_MODE, phoneId + ",RCSE:" + this.mIsRcsEnabled);
        Mno mno = SimUtil.getSimMno(phoneId);
        String rcsAs = ConfigUtil.getAcsServerType(this.mContext, phoneId);
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        if (mno == Mno.CMCC || ((ImsConstants.RCS_AS.JIBE.equals(rcsAs) && !ConfigUtil.isRcsEur(phoneId)) || (mnoStrategy != null && mnoStrategy.isRemoteConfigNeeded(phoneId)))) {
            IMSLog.i(LOG_TAG, phoneId, "changeOpMode: it is not supported");
            return;
        }
        this.mConfigTrigger.startConfig(8, (Message) null, phoneId);
        if (ImsProfile.isRcsUpProfile(getRcsProfile(phoneId))) {
            getAcsConfig(phoneId).resetAcsSettings();
            if (!isRcsEnabled) {
                IMSLog.i(LOG_TAG, phoneId, "force autoconfig for supporting up profile");
                startAutoConfig(true, (Message) null, phoneId);
            }
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, phoneId, "tcPopupUserAccept: " + tcPopupUserAccept);
        if (tcPopupUserAccept == 0 && isRcsEnabled) {
            getAcsConfig(phoneId).resetAcsSettings();
            IMSLog.i(LOG_TAG, phoneId, "force autoconfig in case tcPopupUserAccept is zero");
            startAutoConfig(true, (Message) null, phoneId);
        }
    }

    public String getRcsProfile(int phoneId) {
        return this.mWorkFlowController.getRcsProfile(phoneId);
    }

    public String getRcsConfigMark(int phoneId) {
        String rcsConfigMark = "";
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno == Mno.DEFAULT) {
            IMSLog.i(LOG_TAG, phoneId, "getRcsConfigMark: no SIM loaded");
            return rcsConfigMark;
        }
        List<ImsProfile> profiles = ImsProfileLoaderInternal.getProfileListWithMnoName(this.mContext, mno.getName(), phoneId);
        if (profiles != null && !profiles.isEmpty()) {
            Iterator<ImsProfile> it = profiles.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ImsProfile p = it.next();
                rcsConfigMark = p.getRcsConfigMark();
                if (p.getEnableStatus() == 2 && !TextUtils.isEmpty(rcsConfigMark)) {
                    String str = LOG_TAG;
                    IMSLog.i(str, phoneId, "getRcsConfigMark: " + rcsConfigMark);
                    break;
                }
            }
        }
        return rcsConfigMark;
    }

    public boolean isValidAcsVersion(int phoneId) {
        return this.mConfigTrigger.isValidAcsVersion(phoneId);
    }

    public Integer getRcsConfVersion(int phoneId) {
        return RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId("version", phoneId), (Integer) null);
    }

    public boolean isValidConfigDb(int phoneId) {
        if (getRcsConfVersion(phoneId) == null || getRcsConfVersion(phoneId).intValue() < 0) {
            return false;
        }
        Date current = new Date();
        long nextAutoConfigTime = 0;
        String time = RcsConfigurationHelper.readStringParamWithPath(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.PATH.NEXT_AUTOCONFIG_TIME, phoneId));
        if (!TextUtils.isEmpty(time)) {
            try {
                nextAutoConfigTime = Long.parseLong(time);
            } catch (NumberFormatException e) {
                String str = LOG_TAG;
                IMSLog.i(str, phoneId, "Invalid next autoconfig time: " + time);
            }
        }
        long remainingValidity = nextAutoConfigTime - current.getTime();
        String str2 = LOG_TAG;
        IMSLog.i(str2, phoneId, "remainingValidity: " + remainingValidity);
        if (remainingValidity > 0) {
            return true;
        }
        return false;
    }

    private boolean rcsProfileInit(int phoneId) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null || sm.hasNoSim()) {
            this.mEventLog.logAndAdd(phoneId, "rcsProfileInit: no SIM loaded");
            IMSLog.c(LogClass.CM_NO_SIM_LOADED, phoneId + ",NOSL");
            return false;
        }
        Integer ConfigDBVer = getRcsConfVersion(sm.getSimSlotIndex());
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "rcsProfileInit: ConfigDBVer = " + ConfigDBVer);
        if (ConfigDBVer != null) {
            getAcsConfig(sm.getSimSlotIndex()).setAcsVersion(ConfigDBVer.intValue());
        }
        String mnoName = sm.getSimMnoName();
        if (!TextUtils.isEmpty(mnoName) || sm.hasVsim()) {
            String rcsProfile = ConfigUtil.getRcsProfileLoaderInternalWithFeature(this.mContext, mnoName, phoneId);
            this.mWorkFlowController.putRcsProfile(phoneId, rcsProfile);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(phoneId, "Autoconfig init: mnoName = " + mnoName + ", rcsProfile = " + rcsProfile);
            IMSLog.c(LogClass.CM_RCS_PROFILE, phoneId + "," + mnoName + "," + rcsProfile);
            return true;
        }
        this.mEventLog.logAndAdd(phoneId, "rcsProfileInit: mnoName is not valid");
        IMSLog.c(LogClass.CM_INVALID_MNONAME, phoneId + ",INVMNO");
        return false;
    }

    public boolean updateMobileNetworkforDualRcs(int phoneId) {
        if (RcsUtils.DualRcs.isDualRcsReg() && SimUtil.getDefaultPhoneId() != phoneId) {
            IMSLog.i(LOG_TAG, phoneId, "tryAutoConfig: getDefaultPhoneId() != phoneId ->mobileNetwork = false");
            this.mMobileNetwork = false;
        }
        return this.mMobileNetwork;
    }

    private void init(int phoneId) {
        sendMessage(obtainMessage(0, phoneId, 0, (Object) null));
    }

    private void onSimReady(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onSimReady:");
        registerNetworkCallback(phoneId);
        boolean isChanged = this.mWorkFlowController.isSimInfochanged(phoneId, this.mIsRemoteConfigNeeded);
        boolean isRcsAvailable = isValidAcsVersion(phoneId);
        IWorkflow workflow = this.mWorkFlowController.getWorkflow(phoneId);
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        Mno mno = SimUtil.getSimMno(phoneId);
        int rcsDefaultEnabled = ImsRegistry.getInt(phoneId, GlobalSettingsConstants.RCS.RCS_DEFAULT_ENABLED, -1);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "isRcsAvailable: " + isRcsAvailable + " isChanged: " + isChanged + " mIsRemoteConfigNeeded: " + this.mIsRemoteConfigNeeded + " mIsReceivedSimRefresh: " + this.mIsReceivedSimRefresh + " rcsDefaultEnabled: " + rcsDefaultEnabled);
        StringBuilder sb = new StringBuilder();
        sb.append(phoneId);
        sb.append(",RCSE:");
        sb.append(isRcsAvailable);
        sb.append(",SIM:");
        sb.append(isChanged);
        IMSLog.c(LogClass.CM_SIM_READY, sb.toString());
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, "isRcsEnabled: " + isRcsAvailable + " isChanged: " + isChanged);
        if (workflow != null || !isRcsAvailable || !this.mIsRemoteConfigNeeded || !this.mIsReceivedSimRefresh) {
            this.mIsReceivedSimRefresh = false;
            if (isChanged) {
                setAcsTryReason(phoneId, DiagnosisConstants.RCSA_ATRE.SIM_SWAP);
                if (mno.isKor()) {
                    IMSLog.i(LOG_TAG, phoneId, "changed sim info, reset to MSISDN_FROM_PAU");
                    resetMsisdnFromPau(phoneId);
                }
            }
            if (workflow == null) {
                if ((mno.isKor() || mno.isEur()) && sm != null && !sm.hasNoSim()) {
                    IMSLog.i(LOG_TAG, phoneId, "init workflow");
                    IMSLog.c(LogClass.CM_INIT_WORKFLOW, phoneId + ",WF:INIT");
                    sendMessage(obtainMessage(0, phoneId, 0, (Object) null));
                }
                if (RcsUtils.DualRcs.isDualRcsReg()) {
                    updateDualRcsNetwork(phoneId);
                }
            } else if (isChanged && isRcsAvailable) {
                IMSLog.i(LOG_TAG, phoneId, "reinit workflow");
                IMSLog.c(LogClass.CM_REINIT_WORKFLOW, phoneId + ",WF:REINIT");
                if (ImsConstants.RCS_AS.JIBE.equals(ConfigUtil.getAcsServerType(this.mContext, phoneId))) {
                    workflow.clearAutoConfigStorage();
                    IMSLog.i(LOG_TAG, phoneId, "setting for starting auto config by Message app is clear");
                    ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, -1, phoneId);
                } else if (mno == Mno.TMOUS) {
                    getAcsConfig(phoneId).setAcsCompleteStatus(false);
                } else if (this.mIsRemoteConfigNeeded) {
                    IMSLog.i(LOG_TAG, phoneId, "sim info is changed and reset acsSettings");
                    IMSLog.c(LogClass.CM_SIMINFO_CHANGED, phoneId + ",SIMINFO:CHA,RACS");
                    ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, rcsDefaultEnabled, phoneId);
                    getAcsConfig(phoneId).resetAcsSettings();
                    this.mConfigTrigger.setReadyStartForceCmd(true);
                    this.mIsReceivedSimRefresh = true;
                }
                workflow.cleanup();
                this.mConfigTrigger.setReadyStartCmdList(phoneId, false);
                this.mWorkFlowController.removeWorkFlow(phoneId);
                clearWorkFlowThread(phoneId);
                IMSLog.i(LOG_TAG, phoneId, "clear WorkFlow/WorkFlowThread and send init msg");
                sendMessage(obtainMessage(0, phoneId, 0, (Object) null));
            }
        } else {
            IMSLog.i(LOG_TAG, phoneId, "sim info is refreshed and reset acsSettings");
            IMSLog.c(LogClass.CM_SIMINFO_REFRESHED, phoneId + ",SIMINFO:REF,RACS");
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, rcsDefaultEnabled, phoneId);
            setAcsTryReason(phoneId, DiagnosisConstants.RCSA_ATRE.SIM_SWAP);
            getAcsConfig(phoneId).resetAcsSettings();
            this.mConfigTrigger.setReadyStartForceCmd(true);
            clearWorkFlowThread(phoneId);
        }
    }

    private void updateDualRcsNetwork(int phoneId) {
        Network availableNetwork = getAvailableNetworkForNetworkType(phoneId == 0 ? 1 : 0, 1);
        if (availableNetwork != null && SimUtil.getDefaultPhoneId() != phoneId) {
            sendMessage(obtainMessage(24, phoneId, 1, availableNetwork));
            IMSLog.d(LOG_TAG, phoneId, "updateDualRcsNetwork : ");
        }
    }

    private void clearWorkFlow(int phoneId) {
        IWorkflow workflow = this.mWorkFlowController.getWorkflow(phoneId);
        HandlerThread workThread = this.mWorkflowThreadList.get(phoneId);
        if (workflow != null && workThread != null && workflow.isConfigOngoing()) {
            IMSLog.i(LOG_TAG, phoneId, "clearWorkFlow started");
            workflow.stopWorkFlow();
            workThread.interrupt();
            this.mConfigTrigger.setReadyStartCmdList(phoneId, true);
            IMSLog.i(LOG_TAG, phoneId, "clearWorkFlow done");
        }
    }

    private void clearWorkFlowThread(int phoneId) {
        HandlerThread workflowThread = this.mWorkflowThreadList.get(phoneId);
        if (workflowThread == null) {
            IMSLog.i(LOG_TAG, phoneId, "clearWorkFlowThread: workflowThread is null");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "clearWorkFlowThread: started");
        workflowThread.interrupt();
        try {
            workflowThread.join(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            this.mWorkflowThreadList.remove(phoneId);
            throw th;
        }
        this.mWorkflowThreadList.remove(phoneId);
        IMSLog.i(LOG_TAG, phoneId, "clearWorkFlowThread: done");
    }

    private boolean isMobileDataOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), Extensions.Settings.Global.MOBILE_DATA, 1) != 0;
    }

    private boolean isRoamingMobileDataOn(int phoneId) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        boolean isDataRoamingOn = false;
        if (sm == null) {
            return false;
        }
        int subId = sm.getSubscriptionId();
        if (!TelephonyManagerWrapper.getInstance(this.mContext).isNetworkRoaming(subId)) {
            IMSLog.i(LOG_TAG, phoneId, "is in Home Network");
            return true;
        }
        if (ImsConstants.SystemSettings.DATA_ROAMING.getbySubId(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN, subId) == ImsConstants.SystemSettings.ROAMING_DATA_ENABLED || ImsConstants.SystemSettings.DATA_ROAMING.get(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN) == ImsConstants.SystemSettings.ROAMING_DATA_ENABLED) {
            isDataRoamingOn = true;
        }
        IMSLog.i(LOG_TAG, phoneId, "Roaming && isDataRoamingOn = " + isDataRoamingOn);
        return isDataRoamingOn;
    }

    private boolean isWifiSwitchOn() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    public void setAcsTryReason(int phoneId, DiagnosisConstants.RCSA_ATRE autoconfigTryReason) {
        this.mConfigTrigger.setAcsTryReason(phoneId, autoconfigTryReason);
    }

    public DiagnosisConstants.RCSA_ATRE getAcsTryReason(int phoneId) {
        return this.mConfigTrigger.getAcsTryReason(phoneId);
    }

    public void resetAcsTryReason(int phoneId) {
        this.mConfigTrigger.resetAcsTryReason(phoneId);
    }

    public void onNewRcsConfigurationNeeded(String ownIdentity, String service, Message callback) throws NullPointerException {
        Preconditions.checkNotNull(ownIdentity);
        Preconditions.checkNotNull(service);
        Preconditions.checkNotNull(callback);
        IRegistrationManager rm = ImsRegistry.getRegistrationManager();
        IUserAgent ua = rm != null ? rm.getUserAgentByImsi(service, ownIdentity) : null;
        if (ua != null) {
            startAcs(ua.getPhoneId());
            callback.arg1 = 1;
        } else {
            callback.arg1 = 0;
        }
        callback.sendToTarget();
    }

    public void startAcs(int phoneId) {
        sendMessage(obtainMessage(15, phoneId, -1));
    }

    private void startAcsWithDelay(int phoneId) {
        int i;
        Integer version = getRcsConfVersion(phoneId);
        if (version != null && version.intValue() == 0 && (i = this.mRetryCount) > 0) {
            this.mRetryCount = i - 1;
            IMSLog.i(LOG_TAG, phoneId, "SSL Handshake failed. delay 5 minutes");
            sendMessageDelayed(obtainMessage(15, phoneId, -1), 300000);
        }
    }

    private void updateMsisdn(ImsRegistration registration) {
        if (SimUtil.getSimMno(registration.getPhoneId()).isKor() && TextUtils.isEmpty(TelephonyManagerWrapper.getInstance(this.mContext).getMsisdn()) && registration.hasVolteService() && !registration.getImsProfile().hasEmergencySupport() && registration.getImsProfile().getCmcType() == 0) {
            IMSLog.i(LOG_TAG, registration.getPhoneId(), "MSISDN is null, SP needs to be set to PAU");
            setMsisdnFromPau(registration);
        }
    }

    public void onRegistrationStatusChanged(boolean registered, int errorCode, ImsRegistration regiInfo) {
        int phoneId = regiInfo.getPhoneId();
        if (registered) {
            updateMsisdn(regiInfo);
        }
        ImsProfile profile = regiInfo.getImsProfile();
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "onRegistrationStatusChanged: [" + profile.getName() + "] registered[" + registered + "], response [" + errorCode + "]");
        StringBuilder sb = new StringBuilder();
        sb.append(phoneId);
        sb.append(",EC:");
        sb.append(errorCode);
        IMSLog.c(LogClass.CM_REGI_ERROR, sb.toString());
        Mno mno = Mno.fromName(profile.getMnoName());
        if (registered) {
            if (mno.isKor() && regiInfo.hasVolteService()) {
                IMSLog.i(LOG_TAG, phoneId, "VoLTE regi. is done. It's time for RCS registration!");
                for (IRegisterTask task : this.mRm.getPendingRegistration(phoneId)) {
                    tryAutoconfiguration(task);
                }
            }
            if (regiInfo.hasRcsService()) {
                this.m403ForbiddenCounter = 0;
            }
        } else if (!ImsConstants.RCS_AS.JIBE.equals(ConfigUtil.getAcsServerType(this.mContext, phoneId)) || !ConfigUtil.hasChatbotService(phoneId, this.mRm)) {
            if (ConfigUtil.isRcsEur(mno) && errorCode == SipErrorBase.UNAUTHORIZED.getCode()) {
                this.mWorkFlowController.deleteConfiguration(regiInfo.getPhoneId());
            }
        } else if (errorCode == SipErrorBase.FORBIDDEN.getCode()) {
            if (mno.isTeliaCo() || mno.isOrange()) {
                this.mWorkFlowController.deleteConfiguration(regiInfo.getPhoneId());
            }
            int i = this.m403ForbiddenCounter + 1;
            this.m403ForbiddenCounter = i;
            if (i >= 2) {
                IMSLog.i(LOG_TAG, phoneId, "Two consecutive 403 errors. Permanently prohibited.");
                this.m403ForbiddenCounter = 0;
                return;
            }
            IMSLog.i(LOG_TAG, phoneId, "403 error. Restart initial ACS");
            this.mWorkFlowController.clearToken(phoneId);
            startAcs(phoneId);
        }
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Autoconfig History:");
        this.mEventLog.dump();
        if (!IMSLog.isShipBuild()) {
            if (this.mContext == null) {
                IMSLog.dump(AUTOCONF_TAG, "Dump of configuration db:", true);
                IMSLog.dump(AUTOCONF_TAG, "  mContext is null!", true);
            } else if (SimUtil.isMultiSimSupported()) {
                IMSLog.dump(AUTOCONF_TAG, "Dump of configuration db for simslot0:", true);
                dumpAutoConfDb(Uri.parse(ConfigConstants.CONTENT_URI + "*#simslot0"));
                IMSLog.dump(AUTOCONF_TAG, "Dump of configuration db for simslot1:", true);
                dumpAutoConfDb(Uri.parse(ConfigConstants.CONTENT_URI + "*#simslot1"));
            } else {
                IMSLog.dump(AUTOCONF_TAG, "Dump of configuration db:", true);
                dumpAutoConfDb(Uri.parse(ConfigConstants.CONTENT_URI + "*"));
            }
        }
        IMSLog.decreaseIndent(LOG_TAG);
        this.mWorkFlowController.dump();
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    private void dumpAutoConfDb(Uri uri) {
        Cursor cursor;
        try {
            cursor = this.mContext.getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    IMSLog.dump(AUTOCONF_TAG, "  " + cursor.getColumnName(i) + ": " + cursor.getString(i), true);
                }
                if (cursor.getColumnCount() < 1) {
                    IMSLog.dump(AUTOCONF_TAG, "  DB is empty", true);
                }
            } else {
                IMSLog.dump(AUTOCONF_TAG, "  DB is not available", true);
            }
            if (cursor != null) {
                cursor.close();
                return;
            }
            return;
        } catch (SQLiteException | SecurityException e) {
            IMSLog.dump(AUTOCONF_TAG, "  Skip dump auto conf db", true);
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    private boolean checkMsisdnSkipCount(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        String rcsAs = ConfigUtil.getAcsServerType(this.mContext, phoneId);
        if (this.mIsRemoteConfigNeeded || mno == Mno.SPRINT || ImsConstants.RCS_AS.JIBE.equals(rcsAs)) {
            IMSLog.i(LOG_TAG, phoneId, "no need to check MsisdnSkipCount");
            return false;
        }
        int skipcount = this.mWorkFlowController.getMsisdnSkipCount(phoneId);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "MsisdnSkipCount : " + skipcount + ", MobileNetwork : " + this.mMobileNetwork);
        if (skipcount != 3 || !this.mMobileNetwork) {
            return false;
        }
        return true;
    }

    public void showMSIDSNDialog() {
        sendEmptyMessage(16);
    }

    public void notifyDefaultSmsChanged(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "notifyDefaultSmsChanged:");
        List<IRegisterTask> rtl = this.mRm.getPendingRegistration(phoneId);
        if (rtl != null) {
            processChatPolicyforSMSAppChange(checkChatPolicyforSMSAppChange(phoneId), phoneId, rtl);
        }
    }

    private int checkChatPolicyforSMSAppChange(int phoneId) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        String rcsAs = ConfigUtil.getAcsServerType(this.mContext, phoneId);
        setAcsTryReason(phoneId, DiagnosisConstants.RCSA_ATRE.CHANGE_MSG_APP);
        boolean isRcsUserSettingNotSet = false;
        int supportchatpolicy = ImsRegistry.getInt(phoneId, GlobalSettingsConstants.RCS.SUPPORT_CHAT_ON_DEFAULT_MMS_APP, 0);
        if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, phoneId) == -1) {
            isRcsUserSettingNotSet = true;
        }
        if (sm != null && sm.isSimAvailable() && ImsConstants.RCS_AS.JIBE.equals(rcsAs) && ConfigUtil.isRcsEur(phoneId) && isRcsUserSettingNotSet) {
            supportchatpolicy = 0;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, "notifyDefaultSmsChanged - SupportChat Type : " + supportchatpolicy);
        return supportchatpolicy;
    }

    private void processChatPolicyforSMSAppChange(int supportchatpolicy, int phoneId, List<IRegisterTask> rtl) {
        boolean isSmsDefault = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, phoneId) == 1;
        IMSLog.c(LogClass.CM_DEFAULT_SMS_CHANGED, phoneId + "," + isSmsDefault + "," + supportchatpolicy);
        if (supportchatpolicy == 1) {
            this.mRm.updateChatService(phoneId);
        } else if (supportchatpolicy == 2) {
            for (IRegisterTask task : rtl) {
                if (task.isRcsOnly()) {
                    if (isSmsDefault) {
                        IMSLog.i(LOG_TAG, phoneId, "notifyDefaultSmsChanged - setStateforACS");
                        getAcsConfig(phoneId).resetAcsSettings();
                        this.mConfigTrigger.setStateforTriggeringACS(phoneId);
                        triggerAutoConfig(false, phoneId, rtl);
                    } else {
                        if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                            task.setDeregiReason(36);
                            this.mRm.deregister(task, false, true, "MsgApp is changed");
                        }
                    }
                }
            }
        } else if (supportchatpolicy != 3) {
            if (supportchatpolicy == 4) {
                removeMessages(15);
                startAcsWithDelay(phoneId);
            } else if (supportchatpolicy == 5) {
                for (IRegisterTask task2 : rtl) {
                    if (task2.getProfile().getNeedAutoconfig()) {
                        getAcsConfig(phoneId).resetAcsSettings();
                        this.mConfigTrigger.setStateforTriggeringACS(phoneId);
                        triggerAutoConfig(false, phoneId, rtl);
                    }
                }
            }
        } else if (this.mCallState != 0) {
            this.mPendingDeregi = true;
            IMSLog.i(LOG_TAG, phoneId, "Pending deregistration on active call when MsgApp is changed");
        } else {
            for (IRegisterTask task3 : rtl) {
                if (task3.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && task3.getPdnType() != 15) {
                    task3.setDeregiReason(36);
                    this.mRm.deregister(task3, false, true, "MsgApp is changed");
                } else if (isSmsDefault) {
                    this.mRm.requestTryRegister(task3.getPhoneId());
                }
            }
        }
    }

    public void setDualSimRcsAutoConfig(boolean isDualSimAcs) {
        this.mConfigTrigger.setDualSimRcsAutoConfig(isDualSimAcs);
    }

    public boolean tryAutoconfiguration(IRegisterTask task) {
        return this.mConfigTrigger.tryAutoconfiguration(task);
    }

    public boolean isRcsEnabled(int phoneId) {
        return DmConfigHelper.isImsSwitchEnabled(this.mContext, DeviceConfigManager.RCS, phoneId);
    }

    public void onDefaultSmsPackageChanged() {
        Log.i(LOG_TAG, "onDefaultSmsPackageChanged");
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            sendMessage(obtainMessage(18, i, 0, (Object) null));
        }
    }

    private class IntentReceiver extends BroadcastReceiver {
        private static final String ACTION_AIRPLANE_MODE = "android.intent.action.AIRPLANE_MODE";
        private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
        private IntentFilter mIntentFilter;

        public IntentReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            this.mIntentFilter = intentFilter;
            intentFilter.addAction(ACTION_BOOT_COMPLETED);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
                    ConfigModule.this.sendEmptyMessage(23);
                } else if (intent.getAction().equals("android.intent.action.AIRPLANE_MODE")) {
                    for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
                        if (sm != null && sm.getSimMno() == Mno.KT) {
                            if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(ConfigModule.this.mContext, 0) != ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                                boolean unused = ConfigModule.this.mNeedRetryOverWifi = false;
                            } else if (ConfigModule.this.mWorkFlowController.getCurrentRcsConfigVersion(sm.getSimSlotIndex()) > 0) {
                                boolean unused2 = ConfigModule.this.mNeedRetryOverWifi = true;
                            }
                        }
                    }
                }
            }
        }

        public void addActionAirplaneMode() {
            this.mIntentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        }

        public IntentFilter getIntentFilter() {
            return this.mIntentFilter;
        }
    }

    public void setMsisdnFromPau(ImsRegistration reg) {
        String msisdnFromPAU = reg.getOwnNumber();
        if (msisdnFromPAU != null) {
            if (msisdnFromPAU.startsWith("0")) {
                msisdnFromPAU = "+82" + msisdnFromPAU.substring(1);
            }
            String imsi = "IMSI_" + SimManagerFactory.getImsiFromPhoneId(reg.getPhoneId());
            if (!msisdnFromPAU.equals(ImsSharedPrefHelper.getString(reg.getPhoneId(), this.mContext, IConfigModule.MSISDN_FROM_PAU, imsi, ""))) {
                this.mEventLog.logAndAdd(reg.getPhoneId(), "setMsisdnFromPau: " + IMSLog.checker(msisdnFromPAU));
                IMSLog.c(LogClass.CM_SET_SP_PAU, reg.getPhoneId() + "SET_SP_PAU");
                ImsSharedPrefHelper.save(reg.getPhoneId(), this.mContext, IConfigModule.MSISDN_FROM_PAU, imsi, msisdnFromPAU);
            }
            this.mRm.requestTryRegister(reg.getPhoneId());
        }
    }

    private void resetMsisdnFromPau(int phoneId) {
        IMSLog.c(LogClass.CM_RES_SP_PAU, phoneId + "RES_SP_PAU");
        this.mEventLog.logAndAdd(phoneId, "reset to MSISDN_FROM_PAU");
        ImsSharedPrefHelper.save(phoneId, this.mContext, IConfigModule.MSISDN_FROM_PAU, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(phoneId), "");
    }

    public void resetReadyStateCommand(int phoneId) {
        this.mConfigTrigger.setReadyStartCmdList(phoneId, true);
    }

    private void createNetworkListener(final int phoneId, final int networkType) {
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "createNetworkListener: " + networkType);
        this.mNetworkListeners.get(phoneId).put(Integer.valueOf(networkType), new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                String access$300 = ConfigModule.LOG_TAG;
                int i = phoneId;
                IMSLog.i(access$300, i, "onAvailable : " + network + " networkType: " + networkType);
                if (!RcsUtils.DualRcs.isDualRcsReg() || networkType != 1) {
                    ConfigModule configModule = ConfigModule.this;
                    configModule.sendMessage(configModule.obtainMessage(24, phoneId, networkType, network));
                    return;
                }
                for (int i2 = 0; i2 < SimUtil.getPhoneCount(); i2++) {
                    ConfigModule configModule2 = ConfigModule.this;
                    configModule2.sendMessage(configModule2.obtainMessage(24, i2, networkType, network));
                }
            }

            public void onLost(Network network) {
                String access$300 = ConfigModule.LOG_TAG;
                int i = phoneId;
                IMSLog.i(access$300, i, "onLost : " + network + " networkType: " + networkType);
                if (!RcsUtils.DualRcs.isDualRcsReg() || networkType != 1) {
                    ConfigModule configModule = ConfigModule.this;
                    configModule.sendMessage(configModule.obtainMessage(25, phoneId, networkType));
                    return;
                }
                for (int i2 = 0; i2 < SimUtil.getPhoneCount(); i2++) {
                    ConfigModule configModule2 = ConfigModule.this;
                    configModule2.sendMessage(configModule2.obtainMessage(25, i2, networkType));
                }
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void registerNetworkCallback(int r17) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "registerNetworkCallback"
            com.sec.internal.log.IMSLog.i(r2, r1, r3)
            android.content.Context r2 = r0.mContext
            java.lang.String r2 = com.sec.internal.ims.util.ConfigUtil.getNetworkType(r2, r1)
            android.content.Context r3 = r0.mContext
            java.lang.String r4 = "connectivity"
            java.lang.Object r3 = r3.getSystemService(r4)
            android.net.ConnectivityManager r3 = (android.net.ConnectivityManager) r3
            int r4 = com.sec.internal.helper.SimUtil.getSubId(r17)
            r5 = -1
            if (r4 != r5) goto L_0x0024
            return
        L_0x0024:
            java.lang.String r6 = ","
            java.lang.String[] r6 = r2.split(r6)
            int r7 = r6.length
            r8 = 0
            r9 = r8
        L_0x002d:
            if (r9 >= r7) goto L_0x00e8
            r10 = r6[r9]
            boolean r11 = android.text.TextUtils.isEmpty(r10)
            r12 = 1
            if (r11 == 0) goto L_0x0042
            if (r1 != 0) goto L_0x003c
            r11 = r12
            goto L_0x003d
        L_0x003c:
            r11 = r8
        L_0x003d:
            r0.registerNetworkCallbackForNetwork(r11, r12)
            goto L_0x00e3
        L_0x0042:
            r11 = 0
            android.net.NetworkRequest$Builder r13 = new android.net.NetworkRequest$Builder
            r13.<init>()
            int r14 = r10.hashCode()
            r15 = 104399(0x197cf, float:1.46294E-40)
            r5 = 2
            if (r14 == r15) goto L_0x0072
            r15 = 3649301(0x37af15, float:5.11376E-39)
            if (r14 == r15) goto L_0x0067
            r15 = 570410817(0x21ffc741, float:1.7332214E-18)
            if (r14 == r15) goto L_0x005d
        L_0x005c:
            goto L_0x007c
        L_0x005d:
            java.lang.String r14 = "internet"
            boolean r14 = r10.equals(r14)
            if (r14 == 0) goto L_0x005c
            r14 = r8
            goto L_0x007d
        L_0x0067:
            java.lang.String r14 = "wifi"
            boolean r14 = r10.equals(r14)
            if (r14 == 0) goto L_0x005c
            r14 = r12
            goto L_0x007d
        L_0x0072:
            java.lang.String r14 = "ims"
            boolean r14 = r10.equals(r14)
            if (r14 == 0) goto L_0x005c
            r14 = r5
            goto L_0x007d
        L_0x007c:
            r14 = -1
        L_0x007d:
            r15 = 12
            if (r14 == 0) goto L_0x00a1
            if (r14 == r12) goto L_0x0098
            if (r14 == r5) goto L_0x0086
            goto L_0x00b2
        L_0x0086:
            r11 = 2
            android.net.NetworkRequest$Builder r5 = r13.addTransportType(r8)
            r14 = 4
            android.net.NetworkRequest$Builder r5 = r5.addCapability(r14)
            java.lang.String r14 = java.lang.Integer.toString(r4)
            r5.setNetworkSpecifier(r14)
            goto L_0x00b2
        L_0x0098:
            r11 = 3
            android.net.NetworkRequest$Builder r5 = r13.addTransportType(r12)
            r5.addCapability(r15)
            goto L_0x00b2
        L_0x00a1:
            r11 = 1
            android.net.NetworkRequest$Builder r5 = r13.addTransportType(r8)
            android.net.NetworkRequest$Builder r5 = r5.addCapability(r15)
            java.lang.String r14 = java.lang.Integer.toString(r4)
            r5.setNetworkSpecifier(r14)
        L_0x00b2:
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.ConnectivityManager$NetworkCallback>> r5 = r0.mNetworkListeners
            java.lang.Object r5 = r5.get(r1)
            java.util.HashMap r5 = (java.util.HashMap) r5
            java.lang.Integer r14 = java.lang.Integer.valueOf(r11)
            boolean r5 = r5.containsKey(r14)
            if (r5 != 0) goto L_0x00e0
            r0.createNetworkListener(r1, r11)
            android.net.NetworkRequest r5 = r13.build()
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.ConnectivityManager$NetworkCallback>> r14 = r0.mNetworkListeners
            java.lang.Object r14 = r14.get(r1)
            java.util.HashMap r14 = (java.util.HashMap) r14
            java.lang.Integer r15 = java.lang.Integer.valueOf(r11)
            java.lang.Object r14 = r14.get(r15)
            android.net.ConnectivityManager$NetworkCallback r14 = (android.net.ConnectivityManager.NetworkCallback) r14
            r3.registerNetworkCallback(r5, r14)
        L_0x00e0:
            r0.registerNetworkCallbackForNetwork(r1, r12)
        L_0x00e3:
            int r9 = r9 + 1
            r5 = -1
            goto L_0x002d
        L_0x00e8:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.ConfigModule.registerNetworkCallback(int):void");
    }

    private void registerNetworkCallbackForNetwork(int phoneId, int networkType) {
        int counterPhoneId = 1;
        if (SimUtil.getPhoneCount() > 1) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (phoneId != 0) {
                counterPhoneId = 0;
            }
            int counterSubId = SimUtil.getSubId(counterPhoneId);
            if (counterSubId != -1 && ImsRegistry.getInt(phoneId, "dual_rcs_policy", 0) == 3 && !this.mNetworkListeners.get(counterPhoneId).containsKey(Integer.valueOf(networkType))) {
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                builder.addTransportType(0).addCapability(12).setNetworkSpecifier(Integer.toString(counterSubId));
                createNetworkListener(counterPhoneId, networkType);
                mConnectivityManager.registerNetworkCallback(builder.build(), (ConnectivityManager.NetworkCallback) this.mNetworkListeners.get(counterPhoneId).get(Integer.valueOf(networkType)));
            }
        }
    }

    private void deregisterNetworkCallback(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "deregisterNetworkCallback");
        ConnectivityManager mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        for (ConnectivityManager.NetworkCallback listener : this.mNetworkListeners.get(phoneId).values()) {
            mConnectivityManager.unregisterNetworkCallback(listener);
        }
        this.mNetworkListeners.get(phoneId).clear();
        this.mNetworkLists.get(phoneId).clear();
    }

    private void processConnectionChange(int phoneId) {
        if (getAvailableNetwork(phoneId) == null) {
            IMSLog.i(LOG_TAG, phoneId, "No Available network");
        } else if (this.mConfigTrigger.getReadyStartCmdListIndexOfKey(phoneId) >= 0) {
            if (this.mConfigTrigger.getReadyStartCmdList(phoneId) || checkMsisdnSkipCount(phoneId)) {
                String str = LOG_TAG;
                IMSLog.i(str, phoneId, "network is ready for phoneId: " + phoneId);
                if (this.mIsRemoteConfigNeeded) {
                    IMSLog.i(LOG_TAG, phoneId, "resend HANDLE_AUTO_CONFIG_RESTART");
                    removeMessages(19, Integer.valueOf(phoneId));
                    sendMessage(obtainMessage(19, phoneId, 0, (Object) null));
                    return;
                }
                IMSLog.i(LOG_TAG, phoneId, "resend HANDLE_AUTO_CONFIG_START");
                sendMessage(obtainMessage(2, phoneId, 0, (Object) null));
            }
        }
    }

    public Pair<Network, Integer> getAvailableNetwork(int phoneId) {
        this.mMobileNetwork = false;
        this.mWifiNetwork = false;
        this.mReadyNetwork.put(phoneId, true);
        if (this.mNetworkLists.get(phoneId).containsKey(1)) {
            this.mMobileNetwork = true;
            return Pair.create((Network) this.mNetworkLists.get(phoneId).get(1), 1);
        } else if (this.mNetworkLists.get(phoneId).containsKey(2)) {
            this.mMobileNetwork = true;
            return Pair.create((Network) this.mNetworkLists.get(phoneId).get(2), 2);
        } else if (this.mNetworkLists.get(phoneId).containsKey(3)) {
            this.mWifiNetwork = true;
            return Pair.create((Network) this.mNetworkLists.get(phoneId).get(3), 3);
        } else {
            this.mReadyNetwork.put(phoneId, false);
            return null;
        }
    }

    public Network getAvailableNetworkForNetworkType(int phoneId, int networkType) {
        if (this.mNetworkLists.get(phoneId).containsKey(Integer.valueOf(networkType))) {
            return (Network) this.mNetworkLists.get(phoneId).get(Integer.valueOf(networkType));
        }
        return null;
    }

    private boolean isGcEnabledChange(int phoneId) {
        boolean isGcEnabledChange = ImsSharedPrefHelper.getSharedPref(phoneId, this.mContext, "imsswitch", 0, false).getBoolean("isGcEnabledChange", false);
        String str = LOG_TAG;
        Log.i(str, "isGcEnabledChange: " + isGcEnabledChange);
        return isGcEnabledChange;
    }

    public IStorageAdapter getStorage(int phoneId) {
        return this.mWorkFlowController.getStorage(phoneId);
    }
}
