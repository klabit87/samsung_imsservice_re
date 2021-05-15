package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.telephony.CellLocation;
import android.telephony.PreciseDataConnectionState;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.IImsDmConfigListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.PreciseDataConnectionStateWrapper;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.handler.secims.StackIF;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.NetworkStateListener;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import com.squareup.okhttp.internal.tls.OkHostnameVerifier;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class RegistrationManagerHandler extends Handler implements IRegistrationHandlerNotifiable {
    private static final String LOG_TAG = "RegiMgr-Handler";
    private static final int OMADM_TIMEOUT = 30;
    protected PhoneIdKeyMap<Integer> mAdhocProfileCounter;
    protected ICmcAccountManager mCmcAccountManager;
    protected IConfigModule mConfigModule;
    protected Context mContext;
    protected BroadcastReceiver mDsacEventReceiver;
    private BroadcastReceiver mEntitlementStatus;
    protected SimpleEventLog mEventLog;
    private BroadcastReceiver mGvnIntentReceiver;
    protected boolean mHasPendingRecoveryAction;
    protected final RemoteCallbackList<IImsDmConfigListener> mImsDmConfigListener;
    protected IImsFramework mImsFramework;
    protected Looper mLooper;
    protected NetworkEventController mNetEvtCtr;
    protected final NetworkStateListener mNetworkStateListener;
    protected RegistrationObserverManager mObserverManager;
    protected PdnController mPdnController;
    protected int mPhoneCount;
    protected PreciseAlarmManager mPreAlarmMgr;
    protected RegistrationManagerBase mRegMan;
    private final BroadcastReceiver mRetrySetupEventReceiver;
    private final BroadcastReceiver mRilEventReceiver;
    protected List<ISimManager> mSimManagers;
    protected ITelephonyManager mTelephonyManager;
    private final BroadcastReceiver mUserEventReceiver;
    protected UserEventController mUserEvtCtr;
    private BroadcastReceiver mUserSwitchReceiver;
    protected IVolteServiceModule mVolteServiceModule;
    private BroadcastReceiver mVzwEmmIntentReceiver;

    protected RegistrationManagerHandler(Looper looper, Context ctx, RegistrationManagerBase regman, NetworkEventController netEvetCtr) {
        super(looper);
        this.mImsDmConfigListener = new RemoteCallbackList<>();
        this.mHasPendingRecoveryAction = false;
        this.mUserEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(RegistrationManagerHandler.LOG_TAG, "Received Intent : " + action);
                if (ImsConstants.Intents.ACTION_FLIGHT_MODE.equals(intent.getAction())) {
                    RegistrationManagerHandler.this.mUserEvtCtr.onShuttingDown(intent.getIntExtra("powerofftriggered", -1));
                } else if (ImsConstants.Intents.ACTION_DATAUSAGE_REACH_TO_LIMIT.equals(action)) {
                    boolean isDataLimitReached = intent.getBooleanExtra(ImsConstants.Intents.EXTRA_LIMIT_POLICY, false);
                    RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                    registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(Id.REQUEST_PRESENCE_UNSUBSCRIBE, isDataLimitReached, SimUtil.getDefaultPhoneId(), (Object) null));
                }
            }
        };
        this.mUserSwitchReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (Extensions.Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                    int currentUserId = RegistrationManagerHandler.this.mUserEvtCtr.getCurrentUserId();
                    int newUserId = Extensions.ActivityManager.getCurrentUser();
                    if (RegistrationManagerHandler.this.mUserEvtCtr.getCurrentUserId() != newUserId) {
                        Log.i(RegistrationManagerHandler.LOG_TAG, "User Switch " + currentUserId + " to " + newUserId);
                        RegistrationManagerHandler.this.mUserEvtCtr.setCurrentUserId(newUserId);
                        Extensions.Environment.initForCurrentUser();
                        RegistrationManagerHandler.this.removeMessages(1000);
                        RegistrationManagerHandler.this.sendEmptyMessage(1000);
                    }
                }
            }
        };
        this.mRilEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ImsConstants.Intents.ACTION_DCN_TRIGGERED.equals(intent.getAction())) {
                    int phoneId = intent.getIntExtra("phoneId", SimUtil.getDefaultPhoneId());
                    RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                    registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(807, phoneId, -1));
                }
            }
        };
        this.mRetrySetupEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(RegistrationManagerHandler.LOG_TAG, "Received Intent : " + action);
                if (ImsConstants.Intents.ACTION_RETRYTIME_EXPIRED.equals(intent.getAction())) {
                    RegistrationManagerHandler.this.mNetEvtCtr.onRetryTimeExpired(intent.getIntExtra(ImsConstants.Intents.EXTRA_PHONE_ID, 0));
                } else if (ImsConstants.Intents.ACTION_T3396_EXPIRED.equals(intent.getAction())) {
                    RegistrationManagerHandler.this.onT3396Expired(intent.getIntExtra(PhoneConstants.PHONE_KEY, 0));
                }
            }
        };
        this.mDsacEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ImsConstants.Intents.ACTION_DSAC_MODE_SWITCH.equals(intent.getAction())) {
                    boolean needReregi = false;
                    if (RegistrationManagerHandler.this.mVolteServiceModule == null || RegistrationManagerHandler.this.mVolteServiceModule.getSessionCount() == 0 || RegistrationManagerHandler.this.mVolteServiceModule.hasEmergencyCall(SimUtil.getSimSlotPriority())) {
                        int dsacMode = intent.getIntExtra(ImsConstants.Intents.EXTRA_DSAC_MODE, 1);
                        Log.i(RegistrationManagerHandler.LOG_TAG, "DsacEventReceiver, dsac Mode : " + dsacMode);
                        if (RegistrationManagerHandler.this.mRegMan.getVolteAllowedWithDsac() && dsacMode == 1) {
                            RegistrationManagerHandler.this.mRegMan.setVolteAllowedWithDsac(false);
                            needReregi = true;
                        } else if (!RegistrationManagerHandler.this.mRegMan.getVolteAllowedWithDsac() && dsacMode == 2) {
                            RegistrationManagerHandler.this.mRegMan.setVolteAllowedWithDsac(true);
                            needReregi = true;
                        }
                        if (needReregi) {
                            RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                            registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(146));
                        }
                    }
                }
            }
        };
        this.mGvnIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(RegistrationManagerHandler.LOG_TAG, "RegiGoverReceiver: received action " + action);
                if (ImsConstants.Intents.ACTION_WFC_SWITCH_PROFILE.equals(intent.getAction())) {
                    byte[] data = intent.getByteArrayExtra(ImsConstants.Intents.EXTRA_WFC_REQUEST);
                    RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                    registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(RegistrationEvents.EVENT_WFC_SWITCH_PROFILE, SimUtil.getDefaultPhoneId(), 0, data));
                }
            }
        };
        this.mVzwEmmIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ImsConstants.Intents.ACTION_EMM_ERROR.equals(intent.getAction())) {
                    try {
                        String codeStr = intent.getStringExtra("CODE");
                        if (!TextUtils.isEmpty(codeStr)) {
                            int emmCause = Integer.parseInt(codeStr);
                            RegistrationManagerHandler.this.mRegMan.setEmmCause(emmCause);
                            Log.i(RegistrationManagerHandler.LOG_TAG, "EMM Intent cause: " + emmCause);
                        }
                    } catch (Exception e) {
                        Log.e(RegistrationManagerHandler.LOG_TAG, "Exception occurred: " + e.toString());
                    }
                }
            }
        };
        this.mEntitlementStatus = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED.equals(intent.getAction())) {
                    boolean isRequest = intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
                    boolean volteEnabled = intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOLTE, false);
                    int slotId = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
                    IMSLog.i(RegistrationManagerHandler.LOG_TAG, slotId, "ES Status : " + isRequest + ", " + volteEnabled);
                    if (!isRequest || !volteEnabled) {
                        SlotBasedConfig.getInstance(slotId).setEntitlementNsds(false);
                    } else {
                        SlotBasedConfig.getInstance(slotId).setEntitlementNsds(true);
                    }
                }
            }
        };
        this.mNetworkStateListener = new NetworkStateListener() {
            public void onDataConnectionStateChanged(int networkType, boolean isWifiConnected, int phoneId) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, phoneId, "onDataConnectionStateChanged(): networkType [" + TelephonyManagerExt.getNetworkTypeName(networkType) + "], isWifiConnected [" + isWifiConnected + "]");
                Bundle bundle = new Bundle();
                bundle.putInt("networkType", networkType);
                bundle.putInt("isWifiConnected", isWifiConnected);
                bundle.putInt("phoneId", phoneId);
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(3, bundle));
            }

            public void onCellLocationChanged(CellLocation location, int phoneId) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(24, phoneId, -1, location));
            }

            public void onEpdgConnected(int phoneId) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(26, phoneId, -1));
            }

            public void onEpdgDisconnected(int phoneId) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(27, phoneId, -1));
            }

            public void onIKEAuthFAilure(int phoneId) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, phoneId, "onIKEAuthFAilure:");
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(52, Integer.valueOf(phoneId)));
            }

            public void onEpdgIpsecDisconnected(int phoneId) {
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(54, phoneId, -1));
            }

            public void onEpdgDeregisterRequested(int phoneId) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, phoneId, "onEpdgDeregister: epdg deregister requested");
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(124, phoneId, -1));
            }

            public void onEpdgRegisterRequested(int phoneId, boolean cdmaAvailability) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, phoneId, "onEpdgRegister: cdmaAvailability : " + cdmaAvailability);
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(123, phoneId, cdmaAvailability));
            }

            public void onDefaultNetworkStateChanged(int phoneId) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, phoneId, "onDefaultNetworkStateChanged: EVENT_TRY_REGISTER delayed");
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(RegistrationEvents.EVENT_DEFAULT_NETWORK_CHANGED, Integer.valueOf(phoneId)));
            }

            public void onPreciseDataConnectionStateChanged(int phoneId, PreciseDataConnectionState state) {
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, phoneId, "onPreciseDataConnectionStateChanged");
                PreciseDataConnectionStateWrapper stateWrapper = new PreciseDataConnectionStateWrapper(state);
                if (stateWrapper.getDataConnectionFailCause() != 0 && (stateWrapper.getDataConnectionApnTypeBitMask() & 64) == 64) {
                    RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                    registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(129, phoneId, stateWrapper.getDataConnectionFailCause()));
                }
            }
        };
        this.mContext = ctx;
        this.mLooper = looper;
        this.mRegMan = regman;
        this.mNetEvtCtr = netEvetCtr;
        this.mEventLog = new SimpleEventLog(ctx, LOG_TAG, 300);
    }

    protected RegistrationManagerHandler(Looper looper, Context ctx, RegistrationManagerBase regman, IImsFramework imsFramework, PdnController pdnController, List<ISimManager> simManagers, ITelephonyManager telephonyManager, ICmcAccountManager cmcAccountManager, NetworkEventController netEvtCtr, UserEventController userEvtCtr, IVolteServiceModule vsm) {
        this(looper, ctx, regman, netEvtCtr);
        this.mPdnController = pdnController;
        this.mSimManagers = simManagers;
        this.mTelephonyManager = telephonyManager;
        this.mCmcAccountManager = cmcAccountManager;
        this.mImsFramework = imsFramework;
        this.mObserverManager = new RegistrationObserverManager(this.mContext, this.mRegMan, this.mSimManagers, this);
        this.mUserEvtCtr = userEvtCtr;
        this.mVolteServiceModule = vsm;
        this.mPreAlarmMgr = PreciseAlarmManager.getInstance(ctx);
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.mPhoneCount = this.mSimManagers.size();
        registerInternalListeners();
        registerIntentReceivers();
        this.mAdhocProfileCounter = new PhoneIdKeyMap<>(this.mPhoneCount, 10000);
    }

    /* access modifiers changed from: package-private */
    public void setConfigModule(IConfigModule cm) {
        this.mConfigModule = cm;
    }

    public void handleMessage(Message msg) {
        Log.i(LOG_TAG, "handleMessage: " + RegistrationEvents.msgToString(msg.what));
        if (!RegistrationEvents.handleEvent(msg, this, this.mRegMan, this.mNetEvtCtr, this.mUserEvtCtr)) {
            Log.e(LOG_TAG, "handleMessage: unknown event " + msg.what);
        }
    }

    class ImsStubActionReceiver extends BroadcastReceiver {
        protected static final String ACTION_MOCK_NETWORK_EVENT = "com.sec.ims.MOCK_IMS_EVENT";
        protected static final String EXTRA_EVENT = "event";
        protected static final String EXTRA_NETWORK = "network";
        protected static final String EXTRA_OOS = "oos";
        protected static final String EXTRA_PHONEID = "phoneid";
        protected static final String EXTRA_VOPS = "vops";

        ImsStubActionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.i(RegistrationManagerHandler.LOG_TAG, "onReceive: Intent " + intent);
            if (EXTRA_NETWORK.equalsIgnoreCase(intent.getStringExtra(EXTRA_EVENT))) {
                String network = intent.getStringExtra(EXTRA_NETWORK);
                String vops = intent.getStringExtra(EXTRA_VOPS);
                String oos = intent.getStringExtra(EXTRA_OOS);
                int phoneId = Integer.parseInt((String) Optional.ofNullable(intent.getStringExtra(EXTRA_PHONEID)).orElse("0"));
                IMSLog.i(RegistrationManagerHandler.LOG_TAG, phoneId, "ImsStub: network event - network=" + network + " VoPS=" + vops + " OutOfSvc=" + oos);
                if (RegistrationManagerHandler.this.getNetworkEvent(phoneId) == null) {
                    IMSLog.i(RegistrationManagerHandler.LOG_TAG, phoneId, "onReceive, mNetworkEvent is not exist. Return..");
                    return;
                }
                NetworkEvent ne = new NetworkEvent(RegistrationManagerHandler.this.getNetworkEvent(phoneId));
                if ("nr".equalsIgnoreCase(network)) {
                    ne.network = 20;
                } else if ("lte".equalsIgnoreCase(network)) {
                    ne.network = 13;
                } else if ("hspa".equalsIgnoreCase(network)) {
                    ne.network = 10;
                } else if ("ehrpd".equalsIgnoreCase(network)) {
                    ne.network = 14;
                } else if ("cdma".equalsIgnoreCase(network)) {
                    ne.network = 7;
                } else if ("iwlan".equalsIgnoreCase(network)) {
                    ne.network = 18;
                }
                if (intent.hasExtra(EXTRA_VOPS)) {
                    ne.voiceOverPs = VoPsIndication.translateVops(vops);
                }
                if (intent.hasExtra(EXTRA_OOS) && !TextUtils.isEmpty(oos)) {
                    ne.outOfService = Boolean.parseBoolean(oos);
                }
                RegistrationManagerHandler registrationManagerHandler = RegistrationManagerHandler.this;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(701, phoneId, 0, ne));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerIntentReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_FLIGHT_MODE);
        intentFilter.addAction(ImsConstants.Intents.ACTION_DATAUSAGE_REACH_TO_LIMIT);
        this.mContext.registerReceiver(this.mUserEventReceiver, intentFilter);
        ContextExt.registerReceiverAsUser(this.mContext.getApplicationContext(), this.mUserSwitchReceiver, ContextExt.ALL, new IntentFilter(Extensions.Intent.ACTION_USER_SWITCHED), (String) null, (Handler) null);
        this.mContext.registerReceiver(this.mGvnIntentReceiver, new IntentFilter(ImsConstants.Intents.ACTION_WFC_SWITCH_PROFILE));
        this.mContext.registerReceiver(this.mDsacEventReceiver, new IntentFilter(ImsConstants.Intents.ACTION_DSAC_MODE_SWITCH));
        this.mContext.registerReceiver(this.mRilEventReceiver, new IntentFilter(ImsConstants.Intents.ACTION_DCN_TRIGGERED));
        IntentFilter retryIntentfilter = new IntentFilter();
        retryIntentfilter.addAction(ImsConstants.Intents.ACTION_RETRYTIME_EXPIRED);
        retryIntentfilter.addAction(ImsConstants.Intents.ACTION_T3396_EXPIRED);
        this.mContext.registerReceiver(this.mRetrySetupEventReceiver, retryIntentfilter);
        this.mContext.registerReceiver(this.mVzwEmmIntentReceiver, new IntentFilter(ImsConstants.Intents.ACTION_EMM_ERROR));
        this.mContext.registerReceiver(this.mEntitlementStatus, new IntentFilter(NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED));
        if (!IMSLog.isShipBuild()) {
            BroadcastReceiver receiver = new ImsStubActionReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.sec.ims.MOCK_IMS_EVENT");
            this.mContext.registerReceiver(receiver, filter);
        }
    }

    /* access modifiers changed from: protected */
    public void registerInternalListeners() {
        SimManagerFactory.registerForSubIdChange(this, 707, (Object) null);
        this.mSimManagers.forEach(new Consumer() {
            public final void accept(Object obj) {
                RegistrationManagerHandler.this.lambda$registerInternalListeners$0$RegistrationManagerHandler((ISimManager) obj);
            }
        });
        if (this.mSimManagers.size() > 1) {
            SimManagerFactory.registerForDDSChange(this, Id.REQUEST_PRESENCE_UNPUBLISH, (Object) null);
        }
        this.mPdnController.registerForNetworkState(this.mNetworkStateListener);
        this.mObserverManager.init();
    }

    public /* synthetic */ void lambda$registerInternalListeners$0$RegistrationManagerHandler(ISimManager sm) {
        int phoneId = sm.getSimSlotIndex();
        IMSLog.i(LOG_TAG, phoneId, "Register SIM Event");
        sm.registerForSimReady(this, 20, (Object) null);
        sm.registerForUiccChanged(this, 21, Integer.valueOf(phoneId));
        sm.registerForSimRefresh(this, 36, (Object) null);
        sm.registerForSimRemoved(this, 36, (Object) null);
        IMSLog.i(LOG_TAG, phoneId, "Register PhoneStatelistener.");
        this.mPdnController.unRegisterPhoneStateListener(phoneId);
        this.mPdnController.registerPhoneStateListener(phoneId);
    }

    private boolean onEventSimReady(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onEventSimReady:");
        ISimManager sm = this.mSimManagers.get(phoneId);
        if (sm != null) {
            if (this.mRegMan.onSimReady(sm.hasNoSim() || sm.hasVsim(), phoneId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isReadyToStartRegistration(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "isReadyToStartRegistration:");
        ISimManager sm = this.mSimManagers.get(phoneId);
        if (sm == null) {
            return false;
        }
        String operator = sm.getSimOperator();
        if (hasMessages(36)) {
            IMSLog.e(LOG_TAG, phoneId, "Sim refresh is ongoing. SIM readyretry after");
            return false;
        } else if (hasMessages(107)) {
            IMSLog.e(LOG_TAG, phoneId, "Deregistering is not completed");
            return false;
        } else if (SimUtil.getPhoneCount() > 0 && TextUtils.isEmpty(this.mTelephonyManager.getDeviceId())) {
            IMSLog.e(LOG_TAG, phoneId, "IMEI is empty");
            return false;
        } else if (sm.hasIsim() && sm.getSimState() == 5 && TextUtils.isEmpty(operator)) {
            IMSLog.e(LOG_TAG, phoneId, "OperatorCode is empty");
            return false;
        } else if (sm.hasVsim() || !sm.isSimAvailable() || !TextUtils.isEmpty(sm.getImsi())) {
            return true;
        } else {
            IMSLog.e(LOG_TAG, phoneId, "IMSI is not valid");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public NetworkEvent getNetworkEvent(int phoneId) {
        NetworkEvent ret = SlotBasedConfig.getInstance(phoneId).getNetworkEvent();
        if (ret == null) {
            IMSLog.i(LOG_TAG, phoneId, "getNetworkEvent is not exist. Return null..");
        }
        return ret;
    }

    /* access modifiers changed from: private */
    public void onT3396Expired(int phoneId) {
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            if (this.mNetEvtCtr.hasRetryIntentOnPdnFail()) {
                Log.i(LOG_TAG, "Operator default timer is running, No need update T3396 timer");
                return;
            }
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getProfile().getPdnType() == 11) {
                    if (task.getGovernor().isNonVoLteSimByPdnFail()) {
                        Log.i(LOG_TAG, "ignore T3396 expired, it is Non Volte sim");
                    } else {
                        sendMessage(obtainMessage(2, Integer.valueOf(phoneId)));
                    }
                }
            }
        }
    }

    private boolean isDeregisterNeeded(RegisterTask task) {
        IMSLog.i(LOG_TAG, "isDeregisterNeeded:");
        if (task == null) {
            return false;
        }
        if (task.mState != RegistrationConstants.RegisterTaskState.REGISTERED || !task.getGovernor().isDeregisterOnLocationUpdate() || !task.mProfile.getPdn().equals(DeviceConfigManager.IMS)) {
            IMSLog.i(LOG_TAG, task.getPhoneId(), "isDeregisterNeeded: false");
            return false;
        }
        IMSLog.i(LOG_TAG, task.getPhoneId(), "isDeregisterNeeded: true");
        return true;
    }

    /* access modifiers changed from: protected */
    public void handleSimReady(int phoneId, AsyncResult ar) {
        if (!PackageUtils.isProcessRunning(this.mContext, "com.android.phone")) {
            IMSLog.i(LOG_TAG, phoneId, "phone process is not ready.");
            sendMessageDelayed(obtainMessage(20, ar), 500);
        } else if (!isReadyToStartRegistration(phoneId)) {
            sendMessageDelayed(obtainMessage(20, ar), 800);
        } else {
            boolean readiness = onEventSimReady(phoneId);
            this.mCmcAccountManager.startCmcRegistration();
            if (!readiness && this.mCmcAccountManager.getCmcRegisterTask(phoneId) != null) {
                IMSLog.i(LOG_TAG, phoneId, "SimReady: readiness false but CMC exists");
                readiness = true;
            }
            this.mImsFramework.notifyImsReady(readiness, phoneId);
        }
    }

    /* access modifiers changed from: protected */
    public void onRegistered(RegisterTask task) {
        if (hasMessages(134)) {
            if (this.mHasPendingRecoveryAction) {
                this.mEventLog.logAndAdd(task.getPhoneId(), task, "onRegistered : mHasPendingRecoveryAction");
            } else {
                removeMessages(134);
            }
        }
        if (task.getGovernor().isMobilePreferredForRcs()) {
            removeMessages(152);
        } else {
            removeMessages(132);
        }
        this.mRegMan.onRegistered(task);
    }

    /* access modifiers changed from: protected */
    public void onDeregistered(Object obj) {
        Bundle bundle = (Bundle) obj;
        RegisterTask task = this.mRegMan.getRegisterTaskByProfileId(bundle.getInt("profileId"), bundle.getInt("phoneId"));
        if (hasMessages(134)) {
            if (this.mHasPendingRecoveryAction) {
                this.mEventLog.logAndAdd(task.getPhoneId(), task, "onDeregistered : mHasPendingRecoveryAction");
            } else {
                removeMessages(134);
            }
        }
        removeMessages(107, task);
        if (task != null) {
            this.mRegMan.onDeregistered(task, bundle.getBoolean("isRequestedDeregi"), bundle.getParcelable("error"), bundle.getInt("retryAfter"));
        }
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeError(Object obj) {
        Bundle bundle = (Bundle) obj;
        RegisterTask task = this.mRegMan.getRegisterTaskByProfileId(bundle.getInt("profileId"), bundle.getInt("phoneId"));
        if (task != null) {
            this.mRegMan.onSubscribeError(task, bundle.getParcelable("error"));
        }
    }

    /* access modifiers changed from: protected */
    public void onRegisterError(Object obj) {
        Bundle bundle = (Bundle) obj;
        RegisterTask task = this.mRegMan.getRegisterTaskByProfileId(bundle.getInt("profileId"), bundle.getInt("phoneId"));
        if (hasMessages(134)) {
            if (this.mHasPendingRecoveryAction) {
                this.mEventLog.logAndAdd(task.getPhoneId(), task, "onRegisterError : mHasPendingRecoveryAction");
            } else {
                IMSLog.i(LOG_TAG, task.getPhoneId(), "onRegisterError. Remove RegisteringRecovery message");
                removeMessages(134);
            }
        }
        if (hasMessages(107, task) && (!task.isRcsOnly() || !ConfigUtil.isRcsEur(task.getMno()))) {
            IMSLog.i(LOG_TAG, task.getPhoneId(), "onRegisterError. Remove EVENT_DEREGISTER_TIMEOUT");
            removeMessages(107, task);
            task.setReason("");
            task.setDeregiReason(41);
        }
        if (task != null) {
            this.mRegMan.onRegisterError(task, bundle.getInt(EucTestIntent.Extras.HANDLE), bundle.getParcelable("error"), bundle.getInt("retryAfter"));
        }
    }

    /* access modifiers changed from: package-private */
    public void onDeregistrationRequest(RegisterTask task, boolean local, boolean keepPdnConnection) {
        Log.i(LOG_TAG, "onDeregistrationRequest: task=" + task.getProfile().getName());
        this.mRegMan.tryDeregisterInternal(task, local, keepPdnConnection);
    }

    /* access modifiers changed from: package-private */
    public void onUpdateRegistration(ImsProfile profile, int phoneId) {
        Log.i(LOG_TAG, "onUpdateRegistration:");
        RegisterTask task = this.mRegMan.getRegisterTaskByProfileId(profile.getId(), phoneId);
        if (task == null) {
            Log.i(LOG_TAG, "onUpdateRegistration: registration task not found.");
            return;
        }
        SlotBasedConfig.getInstance(task.getPhoneId()).addExtendedProfile(profile.getId(), profile);
        ImsProfile old = task.getProfile();
        if (!old.equals(profile)) {
            Log.i(LOG_TAG, "onUpdateRegistration: imsprofile changed.");
            old.setExtImpuList(profile.getExtImpuList());
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                task.setReason("External IMPU list changed");
                task.setDeregiReason(28);
                this.mRegMan.tryDeregisterInternal(task, false, true);
                sendMessage(obtainMessage(2, Integer.valueOf(task.getPhoneId())));
            }
        } else if (task.getMno() == Mno.KDDI || task.getGovernor().needImsNotAvailable()) {
            Log.i(LOG_TAG, "onUpdateRegistration: For KDDI, LGT(ImsNotAvailable) Send the Refresh Reg even thoug there is no change in services.");
            this.mRegMan.sendReRegister(task);
        }
    }

    /* access modifiers changed from: protected */
    public void handleGlobalSettingsUpdated(int phoneId) {
        if (SimUtil.getPhoneCount() > 1 && SimUtil.getSimMno(phoneId) == Mno.AIS) {
            Intent intent = new Intent(ImsConstants.Intents.ACTION_IMS_ON_SIMLOADED);
            Log.i(LOG_TAG, "send ACTION_IMS_ON_SIMLOADED");
            IntentUtil.sendBroadcast(this.mContext, intent);
        }
    }

    /* access modifiers changed from: protected */
    public void handleMnoMapUpdated(int phoneId) {
        if (this.mRegMan.getTelephonyCallStatus(phoneId) != 0) {
            removeMessages(148);
            sendMessageDelayed(obtainMessage(148, phoneId, 0, (Object) null), 5000);
            return;
        }
        Log.i(LOG_TAG, "imsservice reboot");
        System.exit(0);
    }

    /* access modifiers changed from: protected */
    public void handleDynamicConfigUpdated(int phoneId) {
        if (this.mRegMan.getTelephonyCallStatus(phoneId) != 0) {
            removeMessages(149);
            sendMessageDelayed(obtainMessage(149, phoneId, 0, (Object) null), 5000);
            return;
        }
        removeMessages(15);
        sendMessage(obtainMessage(15, phoneId, 0, (Object) null));
        removeMessages(16);
        sendMessage(obtainMessage(16, phoneId, 0, (Object) null));
    }

    /* access modifiers changed from: protected */
    public void handleUiccChanged(int phoneId) {
        this.mImsFramework.getServiceModuleManager().notifySimChange(phoneId);
    }

    /* access modifiers changed from: protected */
    public void handleDelayedStopPdn(RegisterTask task) {
        this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
        task.setState(RegistrationConstants.RegisterTaskState.IDLE);
        if (!task.getMno().isKor()) {
            this.mRegMan.setOmadmState(RegistrationManager.OmadmConfigState.IDLE);
            this.mConfigModule.getAcsConfig(task.getPhoneId()).setForceAcs(true);
        } else if (!task.isRcsOnly()) {
            this.mRegMan.setOmadmState(RegistrationManager.OmadmConfigState.IDLE);
        } else {
            this.mConfigModule.getAcsConfig(task.getPhoneId()).setForceAcs(true);
        }
        sendEmptyMessage(32);
        sendMessage(obtainMessage(2, Integer.valueOf(task.getPhoneId())));
    }

    /* access modifiers changed from: protected */
    public void onRequestNotifyVolteSettingsOff(RegisterTask task) {
        Log.i(LOG_TAG, "onRequestNotifyVolteSettingsOff");
        removeMessages(131);
        task.getGovernor().notifyVoLteOnOffToRil(false);
    }

    /* access modifiers changed from: protected */
    public void onLocationTimerExpired(RegisterTask task) {
        Log.i(LOG_TAG, "onLocationTimerExpired");
        removeMessages(800);
        task.getGovernor().notifyLocationTimeout();
    }

    /* access modifiers changed from: protected */
    public void onRequestLocation() {
        Log.i(LOG_TAG, "onRequestLocation");
        removeMessages(801);
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getRegistrationRat() == 18 && this.mPdnController.isWifiConnected() && task.mProfile.isEpdgSupported()) {
                    Log.i(LOG_TAG, "onRequestLocation: request location fetch");
                    task.getGovernor().requestLocation(task.getPhoneId());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLocationCacheExpired(RegisterTask task) {
        Log.i(LOG_TAG, "onLocationCacheExpired");
        removeMessages(803);
        task.getGovernor().onLocationCacheExpiry();
    }

    /* access modifiers changed from: protected */
    public void onTimsTimerExpired(RegisterTask task) {
        Log.i(LOG_TAG, "onTimsTimerExpired " + task.getProfile().getName() + "(" + task.getState() + ")");
        if (task.getGovernor().isMobilePreferredForRcs()) {
            removeMessages(152);
        } else {
            removeMessages(132);
        }
        if (task.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
            task.getGovernor().onTimsTimerExpired();
        } else {
            Log.i(LOG_TAG, "Registered. Igonre onTimsTimerExpired.");
        }
    }

    /* access modifiers changed from: protected */
    public void enableIpme(int phoneId) {
        for (ImsRegistration reg : SlotBasedConfig.getInstance(phoneId).getImsRegistrations().values()) {
            if (reg.getImsProfile().hasService("im") || reg.getImsProfile().hasService("ft") || reg.getImsProfile().hasService("ft_http") || reg.getImsProfile().hasService("slm")) {
                Log.i(LOG_TAG, "Update chat service ");
                this.mRegMan.forcedUpdateRegistration(reg.getImsProfile(), phoneId);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onPcoInfo(String pdn, int pcoValue, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onPcoInfo: " + RegistrationGovernor.PcoType.fromType(pcoValue) + "(" + pcoValue + ")");
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getGovernor().onUpdatedPcoInfo(pdn, pcoValue) && hasMessages(22, task)) {
                    removeMessages(22, task);
                    this.mNetEvtCtr.onPdnConnected(task);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onWfcSwitchProfile(byte[] data, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onWfcSwitchProfile:");
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                ((RegisterTask) it.next()).getGovernor().onWfcProfileChanged(data);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRcsDelayedDeregister() {
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && ConfigUtil.isRcsEur(task.getPhoneId()) && task.isRcsOnly()) {
                    task.setDeregiReason(4);
                    this.mRegMan.tryDeregisterInternal(task, true, false);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRCSAllowedChangedbyMDM() {
        Log.i(LOG_TAG, "onRCSAllowedChangedbyMDM:");
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (ImsUtil.isMatchedService(task.getProfile().getAllServiceSetFromAllNetwork(), ChatServiceImpl.SUBJECT)) {
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        task.setReason("RCS allowed changed");
                        this.mRegMan.updateRegistration(task, false);
                    } else if (!task.isRcsOnly() || ((!ConfigUtil.isRcsEur(phoneId) && task.getMno() != Mno.CMCC) || phoneId == SimUtil.getDefaultPhoneId())) {
                        this.mRegMan.tryRegister(task);
                    } else {
                        IMSLog.i(LOG_TAG, phoneId, "skip RCS tryRegister due to non defaultPhoneId : " + SimUtil.getDefaultPhoneId());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onBlockRegistrationRoamingTimer(int handle, int delay) {
        IRegisterTask task = this.mRegMan.getRegisterTaskByRegHandle(handle);
        if (task != null) {
            this.mRegMan.deregister(task, true, false, "Orange Group, VoWIFI Error in Roaming");
            task.getGovernor().addDelay(delay);
        }
    }

    /* access modifiers changed from: protected */
    public void onThirdParyFeatureTagsUpdated(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onThirdParyFeatureTagsUpdated");
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                IMSLog.i(LOG_TAG, phoneId, "onThirdParyFeatureTagsUpdated: force update " + task);
                task.setReason("3rd party feature tag updated");
                this.mRegMan.updateRegistration(task, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSSACRegiRequested(int phoneId, boolean enabled) {
        IMSLog.i(LOG_TAG, phoneId, "onSSACRegiRequested : enabled(" + enabled + ")");
        List<RegisterTask> rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            SlotBasedConfig.getInstance(phoneId).enableSsac(enabled);
            removeMessages(121, Integer.valueOf(phoneId));
            for (RegisterTask task : rtl) {
                if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && task.getProfile().hasService("mmtel")) {
                    IMSLog.i(LOG_TAG, phoneId, "onSSACRegiRequested: force update " + task);
                    task.setReason("SSAC updated");
                    if (!ImsUtil.isCdmalessEnabled(phoneId) || this.mRegMan.getCsfbSupported(phoneId)) {
                        this.mRegMan.updateRegistration(task, true);
                        return;
                    }
                    task.setReason("SSAC barred on PS only area");
                    task.setDeregiReason(76);
                    this.mRegMan.tryDeregisterInternal(task, true, true);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDisconnectPdnByTimeout(RegisterTask task) {
        int phoneId = task.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "onDisconnectPdnByTimeout: " + task.getState());
        ISimManager sm = this.mSimManagers.get(task.getPhoneId());
        List<RegisterTask> rtl = RegistrationUtils.getPendingRegistrationInternal(task.getPhoneId());
        if (sm != null && rtl != null) {
            task.getGovernor().notifyReattachToRil();
            if (task.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
                this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                sendMessageDelayed(obtainMessage(2, Integer.valueOf(task.getPhoneId())), 1000);
                task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                task.getGovernor().resetAllRetryFlow();
            }
            for (RegisterTask t : rtl) {
                if (!t.equals(task) && t.getPdnType() == task.getPdnType()) {
                    int phoneId2 = task.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId2, "onDisconnectPdnByTimeout: " + t.getProfile().getName() + " " + t.getState());
                    if (t.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                        t.setDeregiReason(2);
                    }
                    if (t.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        t.setReason("onDisconnectPdnByTimeout - REGISTERED or REGISTERING");
                        this.mRegMan.tryDeregisterInternal(t, false, false);
                        this.mRegMan.stopPdnConnectivity(t.getPdnType(), t);
                    } else if (t.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY) {
                        t.setReason("onDisconnectPdnByTimeout - EMERGENCY");
                        t.setDeregiReason(3);
                        this.mRegMan.tryDeregisterInternal(t, false, false);
                        this.mRegMan.stopPdnConnectivity(t.getPdnType(), t);
                        rtl.remove(t);
                        SlotBasedConfig.getInstance(task.getPhoneId()).removeExtendedProfile(task.getProfile().getId());
                    } else if (t.getState() == RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                        this.mRegMan.stopPdnConnectivity(t.getPdnType(), t);
                    } else {
                        this.mRegMan.stopPdnConnectivity(t.getPdnType(), t);
                        sendMessageDelayed(obtainMessage(2, Integer.valueOf(t.getPhoneId())), 1000);
                        t.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    }
                    t.getGovernor().resetPcscfList();
                    t.getGovernor().resetAllRetryFlow();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDisconnectPdnByHDvoiceRoamingOff(RegisterTask task) {
        int phoneId = task.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "onDisconnectPdnByHDvoiceRoamingOff: " + task.getState());
        this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
        task.setState(RegistrationConstants.RegisterTaskState.IDLE);
        task.getGovernor().resetAllRetryFlow();
    }

    /* access modifiers changed from: package-private */
    public void onGeoLocationUpdated() {
        Log.i(LOG_TAG, "onGeoLocationUpdated:");
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (isDeregisterNeeded(task)) {
                    this.mRegMan.sendDeregister((IRegisterTask) task, 0);
                } else {
                    this.mRegMan.updatePani(task.getPhoneId());
                    if (task.getState() != RegistrationConstants.RegisterTaskState.REGISTERED || !task.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
                        this.mRegMan.tryRegister(task.getPhoneId());
                    } else {
                        boolean forced = false;
                        NetworkEvent ne = getNetworkEvent(task.getPhoneId());
                        if (ne == null) {
                            continue;
                        } else if (ne.network != 18 || this.mPdnController.isEpdgConnected(task.getPhoneId())) {
                            if (ne.network != task.getRegistrationRat()) {
                                forced = true;
                            }
                            task.setReason("geolocation changed");
                            RegistrationManagerBase registrationManagerBase = this.mRegMan;
                            registrationManagerBase.updateRegistration(task, forced, registrationManagerBase.getTelephonyCallStatus(task.getPhoneId()) != 0);
                        } else {
                            return;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSimSubscribeIdChanged(SubscriptionInfo subInfo) {
        int simSlotIndex = subInfo.getSimSlotIndex();
        IMSLog.i(LOG_TAG, simSlotIndex, "onSimSubscribeIdChanged, SimSlot: , subId: " + subInfo.getSubscriptionId());
        int phoneId = subInfo.getSimSlotIndex();
        this.mPdnController.unRegisterPhoneStateListener(phoneId);
        this.mPdnController.registerPhoneStateListener(phoneId);
        ImsIconManager iconManager = this.mRegMan.getImsIconManager(phoneId);
        if (iconManager != null) {
            iconManager.unRegisterPhoneStateListener();
            iconManager.registerPhoneStateListener();
        }
        for (ISimManager sm : this.mSimManagers) {
            if (sm.getSimSlotIndex() == subInfo.getSimSlotIndex()) {
                sm.setSubscriptionInfo(subInfo);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDsacModeChanged() {
        Iterator it = SlotBasedConfig.getInstance(SimUtil.getDefaultPhoneId()).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && !task.getProfile().hasEmergencySupport()) {
                task.setReason("re-regi by dsac");
                this.mRegMan.updateRegistration(task, false, false);
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRegEventContactUriNotified(Object obj) {
        Bundle bundle = (Bundle) obj;
        IRegisterTask task = this.mRegMan.getRegisterTaskByRegHandle(bundle.getInt(EucTestIntent.Extras.HANDLE));
        List<ImsUri> uris = bundle.getParcelableArrayList("contact_uri_list");
        int isRegi = bundle.getInt("isRegi");
        String contactUriType = bundle.getString("contactUriType");
        if (task != null) {
            task.getGovernor().onRegEventContactUriNotification(uris, isRegi, contactUriType);
        }
        if (task != null) {
            int cmcType = task.getProfile().getCmcType();
            Log.d(LOG_TAG, "cmcType: " + cmcType + ", isRegi: " + isRegi + ", type: " + contactUriType);
            if (cmcType == 8 || (cmcType == 7 && isRegi == 1)) {
                this.mImsFramework.getImsNotifier().onP2pRegCompleteEvent();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDelayedDeregisterInternal(RegisterTask task, boolean local) {
        Log.i(LOG_TAG, "onDelayedDeregisterInternal: task=" + task.getProfile().getName());
        this.mRegMan.deregisterInternal(task, local);
    }

    /* access modifiers changed from: package-private */
    public void onDeregisterTimeout(IRegisterTask task) {
        IMSLog.i(LOG_TAG, task.getPhoneId(), "onDeregisterTimeout:");
        task.clearUpdateRegisteringFlag();
        if (task.getUserAgent() == null) {
            IMSLog.e(LOG_TAG, task.getPhoneId(), "onDeregisterTimeout: no object");
            ImsRegistration reg = task.getImsRegistration();
            if (reg == null) {
                reg = ImsRegistration.getBuilder().setHandle(-1).setImsProfile(new ImsProfile(task.getProfile())).setServices(task.getProfile().getServiceSet(Integer.valueOf(task.getRegistrationRat()))).setEpdgStatus(this.mPdnController.isEpdgConnected(task.getPhoneId())).setPdnType(task.getPdnType()).setUuid(this.mRegMan.getUuid(task.getPhoneId(), task.getProfile())).setInstanceId(this.mRegMan.getInstanceId(task.getPhoneId(), task.getPdnType(), task.getProfile())).setNetwork(task.getNetworkConnected()).setRegiRat(task.getRegistrationRat()).setPhoneId(task.getPhoneId()).build();
            }
            if ((task.getMno() == Mno.KDDI || task.getGovernor().needImsNotAvailable()) && task.getDeregiReason() == 72) {
                this.mRegMan.notifyImsRegistration(reg, false, task, new ImsRegistrationError(0, "", 72, 32));
            } else {
                this.mRegMan.notifyImsRegistration(reg, false, task, new ImsRegistrationError(SipErrorBase.TEMPORARILY_UNAVAIABLE.getCode(), SipErrorBase.TEMPORARILY_UNAVAIABLE.getReason(), 41, 16));
            }
            if (!task.getProfile().hasEmergencySupport()) {
                task.setRecoveryReason(RegistrationConstants.RecoveryReason.NO_USER_AGENT);
                sendMessage(obtainMessage(134, task));
            }
            task.setReason("");
            task.setDeregiReason(41);
            return;
        }
        ImsRegistration reg2 = task.getUserAgent();
        if ("InitialState".equals(reg2.getStateName())) {
            task.setRecoveryReason(RegistrationConstants.RecoveryReason.UA_STATE_MISMATCH);
            sendMessage(obtainMessage(134, task));
        }
        reg2.deregisterLocal();
    }

    /* access modifiers changed from: package-private */
    public void verifyX509Certificate(X509Certificate[] certs) {
        X509Certificate[] x509CertificateArr = certs;
        Log.i(LOG_TAG, "verifyX509Certificate()");
        boolean verified = false;
        boolean nameMatch = false;
        OkHostnameVerifier ohnv = OkHostnameVerifier.INSTANCE;
        String str = null;
        if (x509CertificateArr != null) {
            boolean z = true;
            if (x509CertificateArr.length >= 1) {
                try {
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                    tmf.init((KeyStore) null);
                    ((X509TrustManager) tmf.getTrustManagers()[0]).checkServerTrusted(x509CertificateArr, "RSA");
                    verified = true;
                } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                    Log.e(LOG_TAG, "Verification failed", e);
                }
                int maxSimCount = SimUtil.getPhoneCount();
                int verifyId = 0;
                loop0:
                while (verifyId < maxSimCount) {
                    Log.i(LOG_TAG, "verifyId " + verifyId + ", maxSimCount " + maxSimCount);
                    ISimManager sm = this.mSimManagers.get(verifyId);
                    if (!(sm == null || this.mRegMan.getPendingRegistration(verifyId) == null)) {
                        if (RcsUtils.UiUtils.isPctMode() && sm.getSimMno() == Mno.CMCC) {
                            Log.i(LOG_TAG, "Skip verifY certificate names...");
                            StackIF.getInstance().sendX509CertVerifyResponse(z, str);
                            return;
                        } else if (verified) {
                            Log.i(LOG_TAG, "Verifying certificate names...");
                            for (IRegisterTask task : this.mRegMan.getPendingRegistration(verifyId)) {
                                if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERING || task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                                    String hostname = task.getPcscfHostname();
                                    Log.i(LOG_TAG, "Checking task: " + task.getProfile().getName() + " / " + hostname);
                                    if (!TextUtils.isEmpty(hostname)) {
                                        nameMatch = ohnv.verify(hostname, x509CertificateArr[0]);
                                        if (nameMatch) {
                                            break loop0;
                                        }
                                    }
                                }
                            }
                            continue;
                        } else {
                            continue;
                        }
                    }
                    verifyId++;
                    str = null;
                    z = true;
                }
                if (verified && !nameMatch) {
                    nameMatch = this.mRegMan.verifyCmcCertificate(x509CertificateArr);
                }
                Log.i(LOG_TAG, "verifyId " + verifyId + ", verified " + verified + ", nameMatch " + nameMatch);
                StackIF.getInstance().sendX509CertVerifyResponse(verified && nameMatch, (String) null);
                return;
            }
        }
        Log.i(LOG_TAG, "there is no certificate");
        StackIF.getInstance().sendX509CertVerifyResponse(false, (String) null);
    }

    /* access modifiers changed from: package-private */
    public void onBootCompleted() {
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    this.mRegMan.updateRegistration(task, false);
                } else {
                    this.mRegMan.tryRegister(task);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onTelephonyCallStatusChanged(int phoneId, int callState) {
        this.mRegMan.setCallState(callState);
        IMSLog.i(LOG_TAG, phoneId, "onTelephonyCallStatusChanged: " + callState);
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (!task.getMno().isKor() || !task.isRcsOnly() || callState != 0 || !this.mHasPendingRecoveryAction) {
                task.getGovernor().onTelephonyCallStatusChanged(callState);
            } else {
                this.mEventLog.logAndAdd("onTelephonyCallStatusChanged : do recovery after call end");
                IMSLog.c(LogClass.REGI_DO_RECOVERY_ACTION, task.getPhoneId() + ",DO RECOVERY: CALL END", true);
                task.mRecoveryReason = RegistrationConstants.RecoveryReason.POSTPONED_RECOVERY;
                sendMessage(obtainMessage(134, task));
                return;
            }
        }
        if (callState == 0) {
            if (!hasMessages(32)) {
                sendEmptyMessage(32);
            }
            sendMessage(obtainMessage(2, Integer.valueOf(phoneId)));
            if (SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS())) {
                sendMessage(obtainMessage(2, Integer.valueOf(SimUtil.getOppositeSimSlot(phoneId))));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onFlightModeChanged(boolean isOn) {
        if (isOn) {
            removeMessages(134);
        }
        this.mUserEvtCtr.onFlightModeChanged(isOn);
    }

    /* access modifiers changed from: package-private */
    public void onConfigUpdated(String item, int phoneId) {
        if (!this.mNetEvtCtr.onConfigUpdated(item, phoneId)) {
            return;
        }
        if (this.mSimManagers.get(phoneId).getSimMno().isKor()) {
            if (hasMessages(2, Integer.valueOf(phoneId))) {
                removeMessages(2, Integer.valueOf(phoneId));
            }
            sendMessageDelayed(obtainMessage(2, Integer.valueOf(phoneId)), 500);
            return;
        }
        this.mRegMan.tryRegister(phoneId);
    }

    public void removeRecoveryAction() {
        if (!hasMessages(134)) {
            return;
        }
        if (this.mHasPendingRecoveryAction) {
            this.mEventLog.logAndAdd("Do not remove RecoveryAction while pending");
        } else {
            removeMessages(134);
        }
    }

    /* access modifiers changed from: protected */
    public void doRecoveryAction(RegisterTask task) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("doRecoveryAction; " + task.mRecoveryReason);
        IMSLog.c(LogClass.REGI_DO_RECOVERY_ACTION, task.getPhoneId() + ",RECOVERY:" + task.mRecoveryReason, true);
        if (!task.getMno().isKor() || !task.isRcsOnly() || this.mRegMan.getTelephonyCallStatus(task.getPhoneId()) == 0) {
            this.mHasPendingRecoveryAction = false;
            System.exit(0);
            return;
        }
        this.mHasPendingRecoveryAction = true;
        this.mEventLog.logAndAdd("doRecoveryAction : active call. postpone recovery");
        IMSLog.c(LogClass.REGI_DO_RECOVERY_ACTION, task.getPhoneId() + ",POSTPONE RECOVERY: ACTIVE CALL", true);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0044, code lost:
        if (r9.getMno().isOneOf(com.sec.internal.constants.Mno.OPTUS, com.sec.internal.constants.Mno.TELUS, com.sec.internal.constants.Mno.KOODO) != false) goto L_0x0046;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDeregisterTimeout(com.sec.internal.interfaces.ims.core.IRegisterTask r9) {
        /*
            r8 = this;
            com.sec.internal.constants.Mno r0 = r9.getMno()
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.H3G
            r2 = 107(0x6b, float:1.5E-43)
            if (r0 != r1) goto L_0x001e
            android.os.Message r0 = r8.obtainMessage(r2, r9)
            com.sec.ims.settings.ImsProfile r1 = r9.getProfile()
            r2 = 13
            int r1 = com.sec.internal.interfaces.ims.core.IRegistrationManager.getDeregistrationTimeout(r1, r2)
            long r1 = (long) r1
            r8.sendMessageDelayed(r0, r1)
            goto L_0x00e5
        L_0x001e:
            com.sec.internal.constants.Mno r0 = r9.getMno()
            boolean r0 = r0.isKor()
            r1 = 2
            r3 = 1
            r4 = 0
            java.lang.String r5 = "RegiMgr-Handler"
            if (r0 != 0) goto L_0x0046
            com.sec.internal.constants.Mno r0 = r9.getMno()
            r6 = 3
            com.sec.internal.constants.Mno[] r6 = new com.sec.internal.constants.Mno[r6]
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.OPTUS
            r6[r4] = r7
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.TELUS
            r6[r3] = r7
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.KOODO
            r6[r1] = r7
            boolean r0 = r0.isOneOf(r6)
            if (r0 == 0) goto L_0x0055
        L_0x0046:
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r0 = r9.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r6 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.EMERGENCY
            if (r0 != r6) goto L_0x0055
            java.lang.String r0 = "KOR, OPTUS,KODO TELUS Emergency no need to Dereg Timer"
            android.util.Log.i(r5, r0)
            goto L_0x00e5
        L_0x0055:
            com.sec.internal.constants.Mno r0 = r9.getMno()
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.KDDI
            if (r0 != r6) goto L_0x0077
            com.sec.internal.ims.core.PdnController r0 = r8.mPdnController
            int r6 = r9.getPhoneId()
            boolean r0 = r0.isEpdgConnected(r6)
            if (r0 == 0) goto L_0x0077
            int r0 = r9.getDeregiReason()
            r6 = 72
            if (r0 != r6) goto L_0x0077
            java.lang.String r0 = "block Registration Retries for the T3402 Timer on Epdg"
            android.util.Log.i(r5, r0)
            goto L_0x00e5
        L_0x0077:
            com.sec.internal.constants.Mno r0 = r9.getMno()
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[r1]
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.ORANGE_POLAND
            r1[r4] = r6
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.ORANGE_ROMANIA
            r1[r3] = r4
            boolean r0 = r0.isOneOf(r1)
            if (r0 == 0) goto L_0x00a2
            int r0 = r9.getDeregiReason()
            r1 = 27
            if (r0 != r1) goto L_0x00a2
            java.lang.String r0 = "EPDG Deregister, set as default dereg timeout"
            android.util.Log.i(r5, r0)
            android.os.Message r0 = r8.obtainMessage(r2, r9)
            r1 = 4000(0xfa0, double:1.9763E-320)
            r8.sendMessageDelayed(r0, r1)
            goto L_0x00e5
        L_0x00a2:
            com.sec.internal.constants.Mno r0 = r9.getMno()
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.VZW
            if (r0 != r1) goto L_0x00c1
            int r0 = r9.getDeregiReason()
            r1 = 23
            if (r0 != r1) goto L_0x00c1
            java.lang.String r0 = "APM/PWR OFF case. We don't have much time! Wait 2.5 sec!"
            android.util.Log.i(r5, r0)
            android.os.Message r0 = r8.obtainMessage(r2, r9)
            r1 = 2500(0x9c4, double:1.235E-320)
            r8.sendMessageDelayed(r0, r1)
            goto L_0x00e5
        L_0x00c1:
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r0 = r9.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.EMERGENCY
            if (r0 != r1) goto L_0x00d1
            android.os.Message r0 = r8.obtainMessage(r2, r9)
            r8.sendMessage(r0)
            goto L_0x00e5
        L_0x00d1:
            android.os.Message r0 = r8.obtainMessage(r2, r9)
            com.sec.ims.settings.ImsProfile r1 = r9.getProfile()
            int r2 = r9.getRegistrationRat()
            int r1 = com.sec.internal.interfaces.ims.core.IRegistrationManager.getDeregistrationTimeout(r1, r2)
            long r1 = (long) r1
            r8.sendMessageDelayed(r0, r1)
        L_0x00e5:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerHandler.setDeregisterTimeout(com.sec.internal.interfaces.ims.core.IRegisterTask):void");
    }

    public void notifyImsSettingUpdated(int phoneId) {
        removeMessages(17, Integer.valueOf(phoneId));
        sendMessage(obtainMessage(17, Integer.valueOf(phoneId)));
    }

    public void notifySetupWizardCompleted() {
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (true) {
                if (it.hasNext()) {
                    if (((RegisterTask) it.next()).isRcsOnly() && ConfigUtil.isRcsEur(phoneId)) {
                        sendMessage(obtainMessage(RegistrationEvents.EVENT_SETUP_WIZARD_COMPLETED, Integer.valueOf(phoneId)));
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    public void notifyRcsUserSettingChanged(int userSetting, int phoneId) {
        sendMessage(obtainMessage(147, phoneId, -1, Integer.valueOf(userSetting)));
    }

    public void notifyRoamingDataSettigChanged(int enabled, int phoneId) {
        sendMessage(obtainMessage(44, enabled, phoneId, (Object) null));
    }

    public void notifyImsSettingChanged(Uri uri, int phoneId) {
        if (uri != null) {
            String path = uri.getPath();
            if (path.startsWith(ImsConstants.Uris.SETTINGS_PROVIDER_PROFILE_URI.getPath())) {
                removeMessages(15);
                sendMessage(obtainMessage(15, phoneId, 0, (Object) null));
            } else if (path.startsWith(GlobalSettingsConstants.CONTENT_URI.getPath())) {
                removeMessages(16);
                sendMessage(obtainMessage(16, phoneId, 0, (Object) null));
            } else if (path.startsWith(ImsConstants.Uris.SETTINGS_PROVIDER_DOWNLOAD_CONFIG_URI.getPath()) || path.startsWith(ImsConstants.Uris.SETTINGS_PROVIDER_DOWNLOAD_CONFIG_RESET_URI.getPath())) {
                removeMessages(149);
                sendMessage(obtainMessage(149, phoneId, 0, (Object) null));
            } else if (path.startsWith(ImsConstants.Uris.SETTINGS_PROVIDER_CARRIER_FEATURE_URI.getPath())) {
                removeMessages(408);
                sendMessage(obtainMessage(408, phoneId, 0, (Object) null));
            }
        }
    }

    public void notifyMnoMapUpdated(Uri uri, int phoneId) {
        if (uri != null) {
            removeMessages(148);
            sendMessage(obtainMessage(148, phoneId, 0, (Object) null));
        }
    }

    public void notifyConfigChanged(Uri uri, int phoneId) {
        if (uri == null) {
            sendMessage(obtainMessage(35, phoneId, 0, (Object) null));
            return;
        }
        Log.i(LOG_TAG, "notifyConfigChanged, it should be fixed to include phoneId!");
        sendMessage(obtainMessage(35, phoneId, 0, uri.getLastPathSegment()));
    }

    public void notifyVowifiSettingChanged(int phoneId, long mills) {
        sendMessageDelayed(obtainMessage(122, phoneId, 0, (Object) null), mills);
    }

    public void notifyLteDataNetworkModeSettingChanged(boolean enabled, int phoneId) {
        sendMessage(obtainMessage(139, enabled, phoneId, (Object) null));
    }

    public void notifyLocationModeChanged() {
        sendMessage(obtainMessage(801));
    }

    public void notifyRoamingLteSettigChanged(boolean enabled) {
        sendMessage(obtainMessage(50, Boolean.valueOf(enabled)));
    }

    public void notifyVolteRoamingSettingChanged(boolean enabled, int phoneId) {
        sendMessage(obtainMessage(138, enabled, phoneId, (Object) null));
    }

    public void notifyAirplaneModeChanged(int airPlaneModeOn) {
        sendMessage(obtainMessage(12, airPlaneModeOn, -1));
    }

    public void notifyMobileDataSettingeChanged(int mobileDataOn, int phoneId) {
        sendMessage(obtainMessage(34, mobileDataOn, phoneId, (Object) null));
    }

    public void notifyMobileDataPressedSettingeChanged(int mobileDataPressed, int phoneId) {
        sendMessage(obtainMessage(153, mobileDataPressed, phoneId, (Object) null));
    }

    public void notifyVolteSettingChanged(boolean enabled, boolean isVideo, int phoneId) {
        sendMessage(obtainMessage(isVideo ? 127 : 125, phoneId, -1, Boolean.valueOf(enabled)));
    }

    public void notifyChatbotAgreementChanged(int phoneId) {
        sendMessage(obtainMessage(56, Integer.valueOf(phoneId)));
    }

    public void notifyTriggeringRecoveryAction(IRegisterTask task, long timeOut) {
        sendMessageDelayed(obtainMessage(134, task), timeOut);
    }

    public void notifyVolteSettingOff(IRegisterTask task, long delay) {
        sendMessageDelayed(obtainMessage(131, task), delay);
    }

    public void notifyEmergencyReady(int profileId) {
        sendMessage(obtainMessage(119, profileId, -1));
    }

    public void notifyRegistered(int phoneId, int profileId, ImsRegistration reg) {
        RegisterTask task = this.mRegMan.getRegisterTaskByProfileId(profileId, phoneId);
        if (task != null) {
            task.setImsRegistration(reg);
            sendMessage(obtainMessage(100, task));
        }
    }

    public void notifyDeRegistered(Bundle bundle) {
        removeMessages(100, this.mRegMan.getRegisterTaskByProfileId(bundle.getInt("profileId"), bundle.getInt("phoneId")));
        sendMessage(obtainMessage(101, bundle));
    }

    public void notifyRegistrationError(Bundle bundle) {
        sendMessage(obtainMessage(104, bundle));
    }

    public void notifySubscribeError(Bundle bundle) {
        sendMessage(obtainMessage(108, bundle));
    }

    public void notifyRefreshRegNotification(int handle) {
        sendMessage(obtainMessage(141, handle, -1));
    }

    public void notifyContactActivated(int phoneId, int profileId) {
        sendMessage(obtainMessage(RegistrationEvents.EVENT_CONTACT_ACTIVATED, phoneId, profileId));
    }

    public void notifyRegEventContactUriNotification(Bundle bundle) {
        sendMessage(obtainMessage(RegistrationEvents.EVENT_REGEVENT_CONTACT_URI_NOTIFIED, bundle));
    }

    /* access modifiers changed from: protected */
    public void notifyPdnConnected(RegisterTask task) {
        sendMessage(obtainMessage(22, task));
    }

    /* access modifiers changed from: protected */
    public void notifyPdnDisconnected(RegisterTask task) {
        sendMessage(obtainMessage(23, task));
    }

    public void notifyLocalIpChanged(IRegisterTask task, boolean isStackedIpChanged) {
        int phoneId = task.getPhoneId();
        String rcsAs = ConfigUtil.getAcsServerType(this.mContext, phoneId);
        IMSLog.i(LOG_TAG, phoneId, "notifyLocalIpChanged: isStackedIpChanged [" + isStackedIpChanged + "], RCS AS [" + rcsAs + "]");
        if (!isStackedIpChanged || (task.isRcsOnly() && ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(rcsAs))) {
            sendMessage(obtainMessage(5, task));
            sendMessageDelayed(obtainMessage(2, Integer.valueOf(task.getPhoneId())), 3000);
        }
    }

    public void notifyX509CertVerificationRequested(X509Certificate[] certs) {
        sendMessage(obtainMessage(30, certs));
    }

    public void notifyDnsResponse(List<String> ipAddrList, int port, int phoneId) {
        sendMessage(obtainMessage(57, port, phoneId, ipAddrList));
    }

    /* access modifiers changed from: protected */
    public void notifyManualRegisterRequested(List<Integer> profileIds, int phoneId) {
        for (Integer intValue : profileIds) {
            ImsProfile profile = ImsProfileLoaderInternal.getProfile(this.mContext, intValue.intValue(), phoneId);
            if (profile != null) {
                sendMessage(obtainMessage(9, phoneId, 0, profile));
            }
        }
    }

    /* access modifiers changed from: protected */
    public int notifyManualRegisterRequested(ImsProfile profile, boolean hasVSIM, int phoneId) {
        if (!profile.isProper()) {
            return -1;
        }
        if (hasVSIM) {
            profile.setAppId("D;" + profile.getAppId() + ";" + profile.getDisplayName());
            profile.setDisplayName("");
        }
        if (profile.getCmcType() < 3) {
            profile.setId(allocateAdhocProfileId(phoneId));
        }
        sendMessage(obtainMessage(9, phoneId, 0, profile));
        Log.i(LOG_TAG, "registerProfile: id " + profile.getId());
        return profile.getId();
    }

    private int allocateAdhocProfileId(int phoneId) {
        Integer currentCount = this.mAdhocProfileCounter.get(phoneId);
        if (currentCount.intValue() < 0 || currentCount.intValue() > 19999) {
            currentCount = 10000;
        }
        this.mAdhocProfileCounter.put(phoneId, Integer.valueOf(currentCount.intValue() + 1));
        return currentCount.intValue() + (phoneId * 10000);
    }

    /* access modifiers changed from: protected */
    public void notifyManualDeRegisterRequested(List<Integer> profileIds, boolean disconnectPdn, int phoneId) {
        for (Integer intValue : profileIds) {
            int id = intValue.intValue();
            if (ImsProfileLoaderInternal.getProfile(this.mContext, id, phoneId) != null) {
                Bundle bundle = new Bundle();
                bundle.putInt("id", id);
                bundle.putBoolean("explicitDeregi", disconnectPdn);
                bundle.putInt("phoneId", phoneId);
                sendMessage(obtainMessage(10, bundle));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyManualDeRegisterRequested(int id, int phoneId) {
        notifyManualDeRegisterRequested(id, phoneId, true);
    }

    /* access modifiers changed from: protected */
    public void notifyManualDeRegisterRequested(int id, int phoneId, boolean disconnectPdn) {
        if (SimUtil.isSoftphoneEnabled()) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(phoneId, "deregisterProfile : " + id);
            IMSLog.c(LogClass.REGI_DEREGISTER_PROFILE, phoneId + ",DEREG REQ:" + id);
        }
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putBoolean("explicitDeregi", disconnectPdn);
        bundle.putInt("phoneId", phoneId);
        sendMessage(obtainMessage(10, bundle));
    }

    /* access modifiers changed from: protected */
    public int notifyUpdateRegisterRequested(ImsProfile profile, int phoneId) {
        if (profile == null) {
            return -1;
        }
        sendMessage(obtainMessage(25, phoneId, -1, profile));
        return 0;
    }

    /* access modifiers changed from: protected */
    public void notifySendDeRegisterRequested(Mno mno, int reason, int phoneId) {
        post(new Runnable(phoneId, mno, reason) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ Mno f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                RegistrationManagerHandler.this.lambda$notifySendDeRegisterRequested$1$RegistrationManagerHandler(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$notifySendDeRegisterRequested$1$RegistrationManagerHandler(int phoneId, Mno mno, int reason) {
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (!task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING) || task.getProfile().getPdnType() == 15) {
                if (reason == 130 && task.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                    Log.i(LOG_TAG, "Stop pdn when device shut down...");
                    this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                    task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
            } else if (!RegistrationUtils.ignoreSendDeregister(phoneId, mno, task, reason)) {
                if ("".equals(task.getReason())) {
                    task.setReason("sendDeregister : " + reason);
                }
                task.setDeregiCause(reason);
                if (task.getDeregiReason() == 31) {
                    if (hasMessages(49)) {
                        removeMessages(49);
                    }
                    sendMessageDelayed(obtainMessage(49), 6000);
                }
                if ((reason == 1000 && mno.isKor()) || reason == 143) {
                    this.mRegMan.tryDeregisterInternal(task, false, true);
                } else if (reason == 807) {
                    this.mRegMan.tryDeregisterInternal(task, true, true);
                } else if (reason == 23) {
                    Log.i(LOG_TAG, "Do not disconnect IMS PDN while shutting down!");
                    this.mRegMan.tryDeregisterInternal(task, false, true);
                } else if ((mno == Mno.UMOBILE || mno == Mno.DIGI) && reason == 124 && !getNetworkEvent(phoneId).isWifiConnected) {
                    Log.i(LOG_TAG, "Wifi disconnected, send local deregister");
                    this.mRegMan.tryDeregisterInternal(task, true, false);
                } else {
                    this.mRegMan.tryDeregisterInternal(task, false, false);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifySendReRegisterRequested(RegisterTask task) {
        post(new Runnable(task) {
            public final /* synthetic */ RegisterTask f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                RegistrationManagerHandler.this.lambda$notifySendReRegisterRequested$2$RegistrationManagerHandler(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$notifySendReRegisterRequested$2$RegistrationManagerHandler(RegisterTask task) {
        if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            boolean isRcsForEur = true;
            task.setUpdateRegistering(true);
            if (!ConfigUtil.isRcsEur(task.getPhoneId()) || !task.isRcsOnly()) {
                isRcsForEur = false;
            }
            Set<String> services = this.mRegMan.getServiceForNetwork(task.getProfile(), task.getRegistrationRat(), isRcsForEur, task.getPhoneId());
            if (CollectionUtils.isNullOrEmpty((Collection<?>) services)) {
                Log.i(LOG_TAG, "sendReRegister : deregister task due to empty services");
                this.mRegMan.tryDeregisterInternal(task, false, false);
                return;
            }
            this.mRegMan.registerInternal(task, task.getGovernor().getCurrentPcscfIp(), services);
        }
    }

    /* access modifiers changed from: protected */
    public void requestDelayedDeRegister(IRegisterTask task, boolean local, long mills) {
        sendMessageDelayed(obtainMessage(145, local, -1, task), mills);
    }

    public void registerDmListener(IImsDmConfigListener listener) {
        Log.i(LOG_TAG, "registerListener: " + listener);
        synchronized (this.mImsDmConfigListener) {
            if (listener != null) {
                this.mImsDmConfigListener.register(listener);
            }
        }
    }

    public void unregisterDmListener(IImsDmConfigListener listener) {
        Log.i(LOG_TAG, "unregisterListener: " + listener);
        synchronized (this.mImsDmConfigListener) {
            if (listener != null) {
                this.mImsDmConfigListener.unregister(listener);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyDmValueChanged(String item, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "notifyDmValueChanged:");
        try {
            int length = this.mImsDmConfigListener.beginBroadcast();
            boolean state = onDmState(item, phoneId);
            for (int index = 0; index < length; index++) {
                this.mImsDmConfigListener.getBroadcastItem(index).onChangeDmValue(item, state);
            }
            this.mImsDmConfigListener.finishBroadcast();
        } catch (RemoteException | IllegalStateException | NullPointerException e) {
            e.printStackTrace();
        }
        Context context = this.mContext;
        String dmValue = DmConfigHelper.read(context, "omadm/./3GPP_IMS/" + item, "", phoneId);
        IMSLog.i(LOG_TAG, phoneId, "item : " + item + ", value : " + IMSLog.checker(dmValue));
        if (!TextUtils.isEmpty(item) && !TextUtils.isEmpty(dmValue)) {
            Intent intent = new Intent(ImsConstants.Intents.ACTION_DM_CHANGED);
            intent.putExtra(ImsConstants.Intents.EXTRA_UPDATED_ITEM, item);
            intent.putExtra(ImsConstants.Intents.EXTRA_UPDATED_VALUE, dmValue);
            intent.putExtra("phoneId", phoneId);
            this.mContext.sendBroadcast(intent);
        }
    }

    private boolean onDmState(String item, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onDmState:");
        boolean state = false;
        if ("EAB_SETTING".equalsIgnoreCase(item)) {
            state = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, false, phoneId).booleanValue();
        } else if ("LVC_ENABLED".equalsIgnoreCase(item)) {
            state = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_LVC_ENABLED, false, phoneId).booleanValue();
        } else if ("VOLTE_ENABLED".equalsIgnoreCase(item)) {
            state = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, false, phoneId).booleanValue();
        } else {
            IMSLog.i(LOG_TAG, phoneId, "Ignore DM value");
        }
        IMSLog.i(LOG_TAG, phoneId, "new onDmState: " + item + "- state: " + state);
        return state;
    }

    /* access modifiers changed from: protected */
    public Message startPreciseAlarmTimer(int event, IRegisterTask task, long delay) {
        Message timerMessage = obtainMessage(event, task);
        this.mPreAlarmMgr.sendMessageDelayed(getClass().getSimpleName(), timerMessage, delay);
        return timerMessage;
    }

    public void stopTimer(Message timerMessage) {
        this.mPreAlarmMgr.removeMessage(timerMessage);
    }

    public void sendUpdateRegistration(ImsProfile profile, int phoneId, long delay) {
        sendMessageDelayed(obtainMessage(25, phoneId, -1, profile), delay);
    }

    public void sendDisconnectPdnByHdVoiceRoamingOff(RegisterTask task) {
        sendMessage(obtainMessage(RegistrationEvents.EVENT_DISCONNECT_PDN_BY_HD_VOICE_ROAMING_OFF, task));
    }

    public void sendFinishOmadmProvisioningUpdate(IRegisterTask task, long millis) {
        sendMessageDelayed(obtainMessage(39, task), millis);
    }

    public Message startDisconnectPdnTimer(IRegisterTask task, long millis) {
        return startPreciseAlarmTimer(404, task, millis);
    }

    public Message startDmConfigTimer(RegisterTask task, long delay) {
        return startPreciseAlarmTimer(28, task, delay);
    }

    public void requestForcedUpdateRegistration(IRegisterTask task) {
        sendMessage(obtainMessage(140, task));
    }

    public void requestPendingDeregistration(IRegisterTask task, boolean local, boolean keepPdnConnection, long delay) {
        sendMessageDelayed(obtainMessage(120, local, keepPdnConnection, task), delay);
    }

    public void sendRequestDmConfig() {
        sendEmptyMessage(28);
    }

    public void sendCheckUnprocessedOmadmConfig() {
        sendEmptyMessage(RegistrationEvents.EVENT_CHECK_UNPROCESSED_OMADM_CONFIG);
    }

    public Message startLocationRequestTimer(IRegisterTask task, long delay) {
        return startPreciseAlarmTimer(800, task, delay);
    }

    public boolean hasVolteSettingOffEvent() {
        return hasMessages(131);
    }

    public void removeVolteSettingOffEvent() {
        removeMessages(131);
    }

    public void sendDmConfigTimeout(String tag) {
        PreciseAlarmManager.getInstance(this.mContext).sendMessageDelayed(tag, obtainMessage(43), 30000);
    }

    public void removeDmConfigTimeout() {
        PreciseAlarmManager.getInstance(this.mContext).removeMessage(obtainMessage(43));
    }

    public boolean hasDelayedStopPdnEvent() {
        return hasMessages(133);
    }

    public boolean hasNetworModeChangeEvent() {
        return hasMessages(49);
    }

    public Message startRegistrationTimer(IRegisterTask task, long delay) {
        return startPreciseAlarmTimer(4, task, delay);
    }

    /* access modifiers changed from: protected */
    public Message startTimsEshtablishTimer(RegisterTask task, long delay) {
        if (task.getGovernor().isMobilePreferredForRcs()) {
            return startPreciseAlarmTimer(152, task, delay);
        }
        return startPreciseAlarmTimer(132, task, delay);
    }

    public void sendOmadmProvisioningUpdateStarted(IRegisterTask task) {
        sendMessageAtFrontOfQueue(obtainMessage(38, task));
    }

    public void sendTryRegister(int phoneId) {
        sendMessage(obtainMessage(2, Integer.valueOf(phoneId)));
    }

    public void sendTryRegister(int phoneId, long delay) {
        sendMessageDelayed(obtainMessage(2, Integer.valueOf(phoneId)), delay);
    }

    public void sendSuspend(IRegisterTask task, boolean suspended) {
        sendMessage(obtainMessage(151, suspended, 0, task));
    }
}
