package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import com.sec.epdg.EpdgManager;
import com.sec.ims.IEpdgListener;
import com.sec.ims.ISimMobilityStatusListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WfcEpdgManager extends StateMachine implements ISequentialInitializable {
    private static final String COLUMN_WIFI_CALL_ENABLE = "wifi_call_enable";
    private static final int EPDG_AVAILABLE = 1;
    private static final int EPDG_TERMINATED = 2;
    private static final int EPDG_UNAVAILABLE = 0;
    private static final String INTENT_EPDG_FQDN_NAME = "com.sec.imsservice.intent.action.EPDG_NAME";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = WfcEpdgManager.class.getSimpleName();
    private static final int ON_CARRIER_UPDATE = 14;
    private static final int ON_ENTITLEMENT_EVENT = 12;
    private static final int ON_EPDG_CONNECTED = 8;
    private static final int ON_EPDG_DISCONNECTED = 9;
    private static final int ON_EPDG_FQDN_EVENT = 13;
    private static final int ON_SETTING_RESET = 11;
    private static final int ON_WFC_UPDATED = 4;
    private static final int SIM_ABSENT = 6;
    private static final int SIM_READY = 5;
    private static final int SLOT_0 = 0;
    private static final int SLOT_1 = 1;
    private static final int STATE_TIMEOUT = 10;
    private static final int TRY_EPDG_CONNECT = 7;
    private static final int WIFI_CONNECTED = 3;
    /* access modifiers changed from: private */
    public Connected mConnected;
    /* access modifiers changed from: private */
    public Connecting mConnecting;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    /* access modifiers changed from: private */
    public boolean[] mCurrentSimMobilityState = {false, false};
    /* access modifiers changed from: private */
    public Disconnected mDisconnected;
    /* access modifiers changed from: private */
    public Disconnecting mDisconnecting;
    private final BroadcastReceiver mEntitlementReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String access$500 = WfcEpdgManager.LOG_TAG;
            Log.d(access$500, action + " intent received.");
            if (NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED.equals(action) || AECNamespace.Action.COMPLETED_ENTITLEMENT.equals(action)) {
                WfcEpdgManager.this.sendMessage(12);
            } else if (WfcEpdgManager.INTENT_EPDG_FQDN_NAME.equals(action)) {
                WfcEpdgManager.this.sendMessage(13, (Object) intent);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean[] mEpdgAvailable = {false, false};
    /* access modifiers changed from: private */
    public EpdgManager.ConnectionListener mEpdgConnection;
    /* access modifiers changed from: private */
    public EpdgManager.EpdgListener mEpdgHandoverListener;
    private EpdgManager mEpdgMgr = null;
    /* access modifiers changed from: private */
    public boolean mIsEpdgReqTerminate = false;
    /* access modifiers changed from: private */
    public boolean mIsWIFIConnected = false;
    /* access modifiers changed from: private */
    public final ArrayList<IEpdgListener> mListeners = new ArrayList<>();
    /* access modifiers changed from: private */
    public Intent mReasonIntent = null;
    private ISimMobilityStatusListener mSimMobilityStatusListener;
    VoWifiSettingObserver mVoWifiSettingObserver;
    /* access modifiers changed from: private */
    public List<WfcEpdgConnectionListener> mWfcEpdgConnectionListenerList;
    private final ConnectivityManager.NetworkCallback mWifiStateListener = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            if (WfcEpdgManager.this.isWifiConnected()) {
                boolean unused = WfcEpdgManager.this.mIsWIFIConnected = true;
                WfcEpdgManager.this.sendMessage(3);
            }
        }

        public void onLost(Network network) {
            boolean unused = WfcEpdgManager.this.mIsWIFIConnected = false;
        }
    };

    public interface WfcEpdgConnectionListener {
        void onEpdgServiceConnected();

        void onEpdgServiceDisconnected();
    }

    public WfcEpdgManager(Looper looper) {
        super("WfcEpdgManager", looper);
        Context context = ImsServiceStub.getInstance().getContext();
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mWfcEpdgConnectionListenerList = new ArrayList();
        IntentFilter intent = new IntentFilter();
        intent.addAction(NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED);
        intent.addAction(AECNamespace.Action.COMPLETED_ENTITLEMENT);
        intent.addAction(INTENT_EPDG_FQDN_NAME);
        this.mContext.registerReceiver(this.mEntitlementReceiver, intent);
        this.mDisconnected = new Disconnected();
        this.mConnecting = new Connecting();
        this.mConnected = new Connected();
        this.mDisconnecting = new Disconnecting();
        init();
        super.start();
    }

    private void init() {
        addState(this.mDisconnected);
        addState(this.mConnecting);
        addState(this.mConnected);
        addState(this.mDisconnecting);
        setInitialState(this.mDisconnected);
    }

    public void initSequentially() {
        this.mConnectivityManager.registerNetworkCallback(new NetworkRequest.Builder().addTransportType(1).addCapability(12).build(), this.mWifiStateListener);
        this.mEpdgHandoverListener = makeEpdgHandoverListener();
        this.mVoWifiSettingObserver = new VoWifiSettingObserver(super.getHandler());
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("wifi_call_enable" + 1), false, this.mVoWifiSettingObserver);
        if (SimUtil.getPhoneCount() > 1) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("wifi_call_enable" + 2), false, this.mVoWifiSettingObserver);
        }
        for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
            sm.registerForSimReady(super.getHandler(), 5, sm);
            sm.registerForSimRemoved(super.getHandler(), 6, sm);
        }
    }

    public synchronized void registerEpdgHandoverListener(IEpdgListener listener) {
        Log.i(LOG_TAG, "registerEpdgHandoverListener..");
        if (!this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
        }
        int phoneId = 0;
        while (phoneId < SimUtil.getPhoneCount()) {
            try {
                int i = 0;
                int i2 = this.mEpdgAvailable[phoneId] ? 1 : 0;
                if (this.mIsWIFIConnected) {
                    i = 1;
                }
                listener.onEpdgAvailable(phoneId, i2, i);
                phoneId++;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public synchronized void unRegisterEpdgHandoverListener(IEpdgListener listener) {
        Log.i(LOG_TAG, "unRegisterEpdgHandoverListener..");
        this.mListeners.remove(listener);
    }

    public boolean isWifiConnected() {
        NetworkInfo ni;
        Network[] allNetworks = this.mConnectivityManager.getAllNetworks();
        int length = allNetworks.length;
        int i = 0;
        while (i < length) {
            Network network = allNetworks[i];
            NetworkCapabilities nc = this.mConnectivityManager.getNetworkCapabilities(network);
            if (nc == null || !nc.hasTransport(1) || ((!nc.hasCapability(12) && !nc.hasCapability(4)) || (ni = this.mConnectivityManager.getNetworkInfo(network)) == null)) {
                i++;
            } else {
                Log.i(LOG_TAG, "isWifiConnected: " + ni);
                return ni.isConnected();
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isReadyForEpdgConnect() {
        if (!isAnySimAvailableWithWFCEnabled()) {
            return false;
        }
        for (int phoneId = 0; phoneId < SimUtil.getPhoneCount(); phoneId++) {
            if (SimUtil.getSimMno(phoneId).isUSA() || this.mIsWIFIConnected) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnySimAvailableWithWFCEnabled() {
        for (int phoneId = 0; phoneId < SimUtil.getPhoneCount(); phoneId++) {
            if (isSimAvailable(phoneId) && isWFCEnabled(phoneId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnySimAvailable() {
        for (int phoneId = 0; phoneId < SimUtil.getPhoneCount(); phoneId++) {
            if (isSimAvailable(phoneId)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isSimAvailable(int phoneId) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        return sm != null && (sm.isSimAvailable() || sm.hasVsim());
    }

    private boolean isWFCEnabled(int phoneId) {
        int slotId = phoneId + 1;
        Mno mno = SimUtil.getSimMno(phoneId);
        if (isVowifiSupported(phoneId)) {
            if (mno.isCanada() || mno.isUSA()) {
                return true;
            }
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (Settings.System.getInt(contentResolver, "wifi_call_enable" + slotId, -1) == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isVowifiSupported(int phoneId) {
        ImsServiceStub imsServiceStub = ImsServiceStub.getInstance();
        if (imsServiceStub.isServiceAvailable("mmtel", 18, phoneId) || imsServiceStub.isServiceAvailable("mmtel-video", 18, phoneId) || imsServiceStub.isServiceAvailable("smsip", 18, phoneId)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public EpdgManager getEpdgManager() {
        EpdgManager epdgManager = this.mEpdgMgr;
        if (epdgManager != null) {
            return epdgManager;
        }
        EpdgManager epdgManager2 = new EpdgManager(this.mContext, this.mEpdgConnection);
        this.mEpdgMgr = epdgManager2;
        return epdgManager2;
    }

    public EpdgManager getEpdgMgr() {
        if (isEpdgServiceConnecting() || isEpdgServiceConnected()) {
            return this.mEpdgMgr;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public EpdgManager.ConnectionListener makeEpdgConnection() {
        return new EpdgManager.ConnectionListener() {
            public void onConnected() {
                Log.i(WfcEpdgManager.LOG_TAG, "Bind EpdgService success.");
                WfcEpdgManager.this.sendMessage(8);
                for (int phoneId = 0; phoneId < SimUtil.getPhoneCount(); phoneId++) {
                    WfcEpdgManager.this.getEpdgManager().addListenerBySim(WfcEpdgManager.this.mEpdgHandoverListener, phoneId);
                }
            }

            public void onDisconnected() {
                Log.i(WfcEpdgManager.LOG_TAG, "Unbind EpdgService success.");
                for (WfcEpdgConnectionListener listener : WfcEpdgManager.this.mWfcEpdgConnectionListenerList) {
                    listener.onEpdgServiceDisconnected();
                }
                WfcEpdgManager.this.sendMessageDelayed(9, 500);
            }
        };
    }

    private EpdgManager.EpdgListener makeEpdgHandoverListener() {
        return new EpdgManager.EpdgListener() {
            public void onEpdgAvailable(int phoneId, int isAvailable, int wifiState) {
                boolean z = true;
                if (isAvailable == 2) {
                    boolean unused = WfcEpdgManager.this.mIsEpdgReqTerminate = true;
                    WfcEpdgManager.this.sendMessageAtFrontOfQueue(2);
                    isAvailable = 0;
                }
                if (isAvailable != 1) {
                    z = false;
                }
                boolean notifiedAvailability = z;
                if (WfcEpdgManager.this.mEpdgAvailable[phoneId] != notifiedAvailability) {
                    WfcEpdgManager.this.mEpdgAvailable[phoneId] = notifiedAvailability;
                    Iterator<IEpdgListener> itr = WfcEpdgManager.this.mListeners.iterator();
                    while (itr.hasNext()) {
                        try {
                            itr.next().onEpdgAvailable(phoneId, isAvailable, wifiState);
                        } catch (DeadObjectException e) {
                            Log.e(WfcEpdgManager.LOG_TAG, " DeadObjectException remove dead listener.");
                            itr.remove();
                        } catch (RemoteException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }

            public void onEpdgHandoverResult(int phoneId, int isL2WHandover, int result, String apnType) {
                Iterator<IEpdgListener> itr = WfcEpdgManager.this.mListeners.iterator();
                while (itr.hasNext()) {
                    try {
                        itr.next().onEpdgHandoverResult(phoneId, isL2WHandover, result, apnType);
                    } catch (DeadObjectException e) {
                        Log.e(WfcEpdgManager.LOG_TAG, " DeadObjectException remove dead listener.");
                        itr.remove();
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    }
                }
            }

            public void onEpdgDeregister(int phoneId) {
                Iterator<IEpdgListener> itr = WfcEpdgManager.this.mListeners.iterator();
                while (itr.hasNext()) {
                    try {
                        itr.next().onEpdgDeregister(phoneId);
                    } catch (DeadObjectException e) {
                        Log.e(WfcEpdgManager.LOG_TAG, " DeadObjectException remove dead listener.");
                        itr.remove();
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    }
                }
            }

            public void onEpdgIpsecConnection(int phoneId, String apnType, int ikeError, int throttleCount) {
                Iterator<IEpdgListener> itr = WfcEpdgManager.this.mListeners.iterator();
                while (itr.hasNext()) {
                    try {
                        itr.next().onEpdgIpsecConnection(phoneId, apnType, ikeError, throttleCount);
                    } catch (DeadObjectException e) {
                        Log.e(WfcEpdgManager.LOG_TAG, " DeadObjectException remove dead listener.");
                        itr.remove();
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    }
                }
            }

            public void onEpdgIpsecDisconnection(int phoneId, String apnType) {
                Iterator<IEpdgListener> itr = WfcEpdgManager.this.mListeners.iterator();
                while (itr.hasNext()) {
                    try {
                        itr.next().onEpdgIpsecDisconnection(phoneId, apnType);
                    } catch (DeadObjectException e) {
                        Log.e(WfcEpdgManager.LOG_TAG, " DeadObjectException remove dead listener.");
                        itr.remove();
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    }
                }
            }

            public void onEpdgRegister(int phoneId, boolean cdmaAvailability) {
                Iterator<IEpdgListener> itr = WfcEpdgManager.this.mListeners.iterator();
                while (itr.hasNext()) {
                    try {
                        itr.next().onEpdgRegister(phoneId, cdmaAvailability);
                    } catch (DeadObjectException e) {
                        Log.e(WfcEpdgManager.LOG_TAG, " DeadObjectException remove dead listener.");
                        itr.remove();
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    }
                }
            }

            public void onEpdgShowPopup(int phoneId, int popupType) {
                Iterator<IEpdgListener> itr = WfcEpdgManager.this.mListeners.iterator();
                while (itr.hasNext()) {
                    try {
                        itr.next().onEpdgShowPopup(phoneId, popupType);
                    } catch (DeadObjectException e) {
                        Log.e(WfcEpdgManager.LOG_TAG, " DeadObjectException remove dead listener.");
                        itr.remove();
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    }
                }
            }

            public void onEpdgReleaseCall(int phoneId) {
                Iterator<IEpdgListener> itr = WfcEpdgManager.this.mListeners.iterator();
                while (itr.hasNext()) {
                    try {
                        itr.next().onEpdgReleaseCall(phoneId);
                    } catch (DeadObjectException e) {
                        Log.e(WfcEpdgManager.LOG_TAG, " DeadObjectException remove dead listener.");
                        itr.remove();
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        };
    }

    public void registerWfcEpdgConnectionListener(WfcEpdgConnectionListener listener) {
        this.mWfcEpdgConnectionListenerList.add(listener);
    }

    private class Disconnected extends State {
        private Disconnected() {
        }

        public void enter() {
            Log.i(WfcEpdgManager.LOG_TAG, "Enter [Disconnected]");
            if (WfcEpdgManager.this.isReadyForEpdgConnect() || WfcEpdgManager.this.mReasonIntent != null) {
                WfcEpdgManager.this.removeMessages(7);
                WfcEpdgManager wfcEpdgManager = WfcEpdgManager.this;
                wfcEpdgManager.transitionTo(wfcEpdgManager.mConnecting);
            }
        }

        public boolean processMessage(Message msg) {
            String access$500 = WfcEpdgManager.LOG_TAG;
            Log.i(access$500, "[Disconnected] processMessage " + WfcEpdgManager.this.eventAsString(msg.what));
            switch (msg.what) {
                case 3:
                case 4:
                    if (!WfcEpdgManager.this.isReadyForEpdgConnect()) {
                        return true;
                    }
                    WfcEpdgManager wfcEpdgManager = WfcEpdgManager.this;
                    wfcEpdgManager.transitionTo(wfcEpdgManager.mConnecting);
                    return true;
                case 5:
                    if (!WfcEpdgManager.this.onSimReady((ISimManager) ((AsyncResult) msg.obj).userObj)) {
                        return true;
                    }
                    WfcEpdgManager wfcEpdgManager2 = WfcEpdgManager.this;
                    wfcEpdgManager2.transitionTo(wfcEpdgManager2.mConnecting);
                    return true;
                case 6:
                    if (!WfcEpdgManager.this.onSimRemoved((ISimManager) ((AsyncResult) msg.obj).userObj)) {
                        return true;
                    }
                    WfcEpdgManager wfcEpdgManager3 = WfcEpdgManager.this;
                    wfcEpdgManager3.transitionTo(wfcEpdgManager3.mConnecting);
                    return true;
                case 7:
                case 12:
                    break;
                case 8:
                    Log.i(WfcEpdgManager.LOG_TAG, "EPDG CONNECTED in disconnected state, STRANGE, please check...");
                    WfcEpdgManager wfcEpdgManager4 = WfcEpdgManager.this;
                    wfcEpdgManager4.transitionTo(wfcEpdgManager4.mConnected);
                    return true;
                case 9:
                    Log.i(WfcEpdgManager.LOG_TAG, "ON_EPDG_DISCONNECTED IN ReadyToConnect INVALID EVENT ");
                    return true;
                case 11:
                case 13:
                case 14:
                    Intent unused = WfcEpdgManager.this.mReasonIntent = (Intent) msg.obj;
                    break;
                default:
                    return true;
            }
            WfcEpdgManager wfcEpdgManager5 = WfcEpdgManager.this;
            wfcEpdgManager5.transitionTo(wfcEpdgManager5.mConnecting);
            return true;
        }
    }

    private class Connecting extends State {
        private Connecting() {
        }

        public void enter() {
            Log.i(WfcEpdgManager.LOG_TAG, "Enter [Connecting] connecting epdg service");
            if (WfcEpdgManager.this.mEpdgConnection == null) {
                WfcEpdgManager wfcEpdgManager = WfcEpdgManager.this;
                EpdgManager.ConnectionListener unused = wfcEpdgManager.mEpdgConnection = wfcEpdgManager.makeEpdgConnection();
            }
            WfcEpdgManager.this.getEpdgManager().startService(WfcEpdgManager.this.mReasonIntent);
            WfcEpdgManager.this.getEpdgManager().connectService();
            WfcEpdgManager.this.sendMessageDelayed(10, 5000);
        }

        public boolean processMessage(Message msg) {
            String access$500 = WfcEpdgManager.LOG_TAG;
            Log.i(access$500, "[Connecting] processMessage " + WfcEpdgManager.this.eventAsString(msg.what));
            int i = msg.what;
            if (i != 2) {
                switch (i) {
                    case 5:
                        boolean unused = WfcEpdgManager.this.onSimReady((ISimManager) ((AsyncResult) msg.obj).userObj);
                        return true;
                    case 6:
                        boolean unused2 = WfcEpdgManager.this.onSimRemoved((ISimManager) ((AsyncResult) msg.obj).userObj);
                        return true;
                    case 7:
                        Log.i(WfcEpdgManager.LOG_TAG, "[Connecting] TRY_EPDG_CONNECT already in progress ");
                        return true;
                    case 8:
                        WfcEpdgManager wfcEpdgManager = WfcEpdgManager.this;
                        wfcEpdgManager.transitionTo(wfcEpdgManager.mConnected);
                        return true;
                    case 9:
                        Log.i(WfcEpdgManager.LOG_TAG, "EPDG disconnect in [Connecting] state, may be crash has happenened, need to recover..");
                        WfcEpdgManager wfcEpdgManager2 = WfcEpdgManager.this;
                        wfcEpdgManager2.transitionTo(wfcEpdgManager2.mDisconnected);
                        return true;
                    case 10:
                        WfcEpdgManager wfcEpdgManager3 = WfcEpdgManager.this;
                        wfcEpdgManager3.transitionTo(wfcEpdgManager3.mDisconnected);
                        return true;
                    default:
                        return true;
                }
            } else {
                WfcEpdgManager wfcEpdgManager4 = WfcEpdgManager.this;
                wfcEpdgManager4.transitionTo(wfcEpdgManager4.mDisconnecting);
                return true;
            }
        }

        public void exit() {
            WfcEpdgManager.this.removeMessages(10);
        }
    }

    private class Connected extends State {
        private Connected() {
        }

        public void enter() {
            Log.i(WfcEpdgManager.LOG_TAG, "Enter [Connected]");
            Intent unused = WfcEpdgManager.this.mReasonIntent = null;
            for (WfcEpdgConnectionListener listener : WfcEpdgManager.this.mWfcEpdgConnectionListenerList) {
                listener.onEpdgServiceConnected();
            }
        }

        public boolean processMessage(Message msg) {
            String access$500 = WfcEpdgManager.LOG_TAG;
            Log.i(access$500, "[Connected] processMessage " + WfcEpdgManager.this.eventAsString(msg.what));
            switch (msg.what) {
                case 2:
                    WfcEpdgManager wfcEpdgManager = WfcEpdgManager.this;
                    wfcEpdgManager.transitionTo(wfcEpdgManager.mDisconnecting);
                    return true;
                case 5:
                    if (WfcEpdgManager.this.mIsEpdgReqTerminate) {
                        WfcEpdgManager.this.deferMessage(msg);
                        return true;
                    }
                    boolean unused = WfcEpdgManager.this.onSimReady((ISimManager) ((AsyncResult) msg.obj).userObj);
                    WfcEpdgManager.this.sendMessageDelayed(7, 200);
                    return true;
                case 6:
                    if (WfcEpdgManager.this.mIsEpdgReqTerminate) {
                        WfcEpdgManager.this.deferMessage(msg);
                        return true;
                    }
                    boolean unused2 = WfcEpdgManager.this.onSimRemoved((ISimManager) ((AsyncResult) msg.obj).userObj);
                    WfcEpdgManager.this.sendMessageDelayed(7, 200);
                    return true;
                case 7:
                    Intent unused3 = WfcEpdgManager.this.mReasonIntent = null;
                    return true;
                case 8:
                    Log.i(WfcEpdgManager.LOG_TAG, "[Connected] ON_EPDG_CONNECTED already in connected state.... ");
                    return true;
                case 9:
                    Log.i(WfcEpdgManager.LOG_TAG, "EPDG disconnect in [Connected] state, may be crash has happenened, need to recover..");
                    WfcEpdgManager wfcEpdgManager2 = WfcEpdgManager.this;
                    wfcEpdgManager2.transitionTo(wfcEpdgManager2.mDisconnected);
                    return true;
                case 11:
                case 13:
                case 14:
                    Intent unused4 = WfcEpdgManager.this.mReasonIntent = (Intent) msg.obj;
                    break;
                case 12:
                    break;
                default:
                    return true;
            }
            if (WfcEpdgManager.this.mIsEpdgReqTerminate) {
                WfcEpdgManager.this.deferMessage(msg);
                return true;
            }
            WfcEpdgManager.this.sendMessageDelayed(7, 200);
            return true;
        }
    }

    private class Disconnecting extends State {
        private Disconnecting() {
        }

        public void enter() {
            Log.i(WfcEpdgManager.LOG_TAG, "Enter [Disconnecting]");
            WfcEpdgManager.this.mEpdgAvailable[0] = false;
            WfcEpdgManager.this.mEpdgAvailable[1] = false;
            boolean unused = WfcEpdgManager.this.mIsEpdgReqTerminate = false;
            for (int phoneId = 0; phoneId < SimUtil.getPhoneCount(); phoneId++) {
                WfcEpdgManager.this.getEpdgManager().removeListenerBySim(WfcEpdgManager.this.mEpdgHandoverListener, phoneId);
            }
            WfcEpdgManager.this.getEpdgManager().disconnectService();
            WfcEpdgManager.this.getEpdgManager().stopService();
            WfcEpdgManager.this.sendMessageDelayed(10, 5000);
        }

        public boolean processMessage(Message msg) {
            String access$500 = WfcEpdgManager.LOG_TAG;
            Log.i(access$500, "[Disconnecting] processMessage " + WfcEpdgManager.this.eventAsString(msg.what));
            switch (msg.what) {
                case 5:
                case 6:
                case 7:
                case 11:
                case 12:
                case 13:
                case 14:
                    WfcEpdgManager.this.deferMessage(msg);
                    return true;
                case 8:
                    Log.i(WfcEpdgManager.LOG_TAG, "INVALID STATE ON EPDG CONNECTED IN DISCONNECTING STATE for EPDG");
                    return true;
                case 9:
                case 10:
                    WfcEpdgManager wfcEpdgManager = WfcEpdgManager.this;
                    wfcEpdgManager.transitionTo(wfcEpdgManager.mDisconnected);
                    return true;
                default:
                    return true;
            }
        }

        public void exit() {
            WfcEpdgManager.this.removeMessages(10);
        }
    }

    public boolean isEpdgServiceConnected() {
        return this.mConnected.equals(getCurrentState());
    }

    public boolean isEpdgServiceConnecting() {
        return this.mConnecting.equals(getCurrentState());
    }

    /* access modifiers changed from: private */
    public boolean onSimReady(ISimManager sm) {
        int phoneId = sm.getSimSlotIndex();
        if (sm.isSimAvailable()) {
            String str = LOG_TAG;
            Log.i(str, "on SIM Ready: phoneId=" + phoneId);
            this.mCurrentSimMobilityState[phoneId] = ImsServiceStub.getInstance().isSimMobilityActivated(phoneId);
            if (this.mSimMobilityStatusListener != null) {
                return true;
            }
            this.mSimMobilityStatusListener = makeSimMobilityListener();
            ImsServiceStub.getInstance().registerSimMobilityStatusListener(this.mSimMobilityStatusListener, false, -1);
            return true;
        }
        String str2 = LOG_TAG;
        Log.i(str2, "SIM ABSENT|LOCKED|NOT READY: phoneId=" + phoneId);
        sendMessage(obtainMessage(6, (Object) new AsyncResult(sm, Integer.valueOf(phoneId), (Throwable) null)));
        return false;
    }

    public void onResetSetting(Intent intent) {
        sendMessage(11, (Object) intent);
    }

    public void onCarrierUpdate(Intent intent) {
        sendMessage(14, (Object) intent);
    }

    /* access modifiers changed from: private */
    public String eventAsString(int msg) {
        switch (msg) {
            case 2:
                return "EPDG_TERMINATED";
            case 3:
                return "WIFI_CONNECTED";
            case 4:
                return "ON_WFC_UPDATED";
            case 5:
                return "SIM_READY";
            case 6:
                return "SIM_ABSENT";
            case 7:
                return "TRY_EPDG_CONNECT";
            case 8:
                return "ON_EPDG_CONNECTED";
            case 9:
                return "ON_EPDG_DISCONNECTED";
            case 10:
                return "STATE_TIMEOUT";
            case 11:
                return "ON_SETTING_RESET";
            case 12:
                return "ON_ENTITLEMENT_EVENT";
            case 13:
                return "ON_EPDG_FQDN_EVENT";
            case 14:
                return "ON_CARRIER_UPDATE";
            default:
                return "UNKNOWN";
        }
    }

    /* access modifiers changed from: private */
    public boolean onSimRemoved(ISimManager sm) {
        if (sm.isSimAvailable()) {
            return false;
        }
        int phoneId = sm.getSimSlotIndex();
        String str = LOG_TAG;
        Log.i(str, "SIM Absent: phoneId=" + phoneId);
        this.mCurrentSimMobilityState[phoneId] = false;
        return true;
    }

    class VoWifiSettingObserver extends ContentObserver {
        public VoWifiSettingObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, (Uri) null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                String key = uri.toString();
                String access$500 = WfcEpdgManager.LOG_TAG;
                Log.i(access$500, " on Vowifi SETTING Changed, key : " + key);
                if (key.contains("wifi_call_enable" + 1)) {
                    WfcEpdgManager.this.sendMessage(4, 0);
                    return;
                }
                if (key.contains("wifi_call_enable" + 2)) {
                    WfcEpdgManager.this.sendMessage(4, 1);
                }
            }
        }
    }

    private ISimMobilityStatusListener makeSimMobilityListener() {
        return new ISimMobilityStatusListener.Stub() {
            public void onSimMobilityStateChanged(boolean simMobility) throws RemoteException {
                for (int phoneId = 0; phoneId < SimUtil.getPhoneCount(); phoneId++) {
                    boolean simMobility2 = ImsServiceStub.getInstance().isSimMobilityActivated(phoneId);
                    if (WfcEpdgManager.this.mCurrentSimMobilityState[phoneId] != simMobility2 && WfcEpdgManager.this.isSimAvailable(phoneId)) {
                        String access$500 = WfcEpdgManager.LOG_TAG;
                        Log.i(access$500, "onSimMobilityStateChanged: simMobility " + simMobility2 + " phoneID " + phoneId);
                        WfcEpdgManager.this.mCurrentSimMobilityState[phoneId] = simMobility2;
                        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
                        WfcEpdgManager wfcEpdgManager = WfcEpdgManager.this;
                        wfcEpdgManager.sendMessage(wfcEpdgManager.obtainMessage(5, (Object) new AsyncResult(sm, Integer.valueOf(phoneId), (Throwable) null)));
                    }
                }
            }
        };
    }
}
