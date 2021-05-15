package com.sec.internal.ims.servicemodules.euc;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.euc.data.AutoconfUserConsentData;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucAcknowledgment;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucNotification;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucSystemRequest;
import com.sec.internal.ims.servicemodules.euc.dialog.EucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.ims.servicemodules.euc.locale.IDeviceLocale;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistence;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceNotifier;
import com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence;
import com.sec.internal.ims.servicemodules.euc.persistence.UserConsentPersistenceNotifier;
import com.sec.internal.ims.servicemodules.euc.snf.EucStoreAndForward;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.ims.servicemodules.euc.test.EucTestEventsFactory;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import com.sec.internal.ims.servicemodules.euc.test.IEucTestEventsFactory;
import com.sec.internal.ims.servicemodules.euc.workflow.IEucWorkflow;
import com.sec.internal.ims.servicemodules.euc.workflow.NotificationEucWorkflow;
import com.sec.internal.ims.servicemodules.euc.workflow.PersistentEucWorkflow;
import com.sec.internal.ims.servicemodules.euc.workflow.VolatileEucWorkflow;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucModule;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public class EucModule extends ServiceModuleBase implements IEucModule {
    private static final int DEFAULT_EUC_PHONE_ID = ImsConstants.Phone.SLOT_1;
    private static final int EVENT_CONFIGURED = 15;
    private static final int EVENT_DDS_CHANGED = 23;
    private static final int EVENT_DEREGISTERED = 17;
    private static final int EVENT_INIT = 11;
    private static final int EVENT_REGISTERED = 16;
    private static final int EVENT_SERVICE_SWITCHED = 14;
    private static final int EVENT_SIM_READY = 21;
    private static final int EVENT_SIM_REFRESH = 22;
    private static final int EVENT_START = 12;
    private static final int EVENT_STOP = 13;
    private static final int EXPECTED_NUMBER_OF_SIM_SLOTS = 2;
    private static final String LOG_STRING_OWN_IDENTITY = ", ownIdentity = ";
    private static final String LOG_TAG = EucModule.class.getSimpleName();
    private static final String LOG_TEST_REQUEST_FAILURE = "Failure, test request is invalid, skipping ";
    private static final String[] sRequiredServices = {"euc"};
    private final Context mContext;
    private final IDeviceLocale mDeviceLocale;
    private final IEucDisplayManager mDisplayManager;
    private final IEucFactory mEucFactory;
    private final IEucPersistence mEucPersistence;
    private int mEucPhoneId;
    private final IEucServiceInterface mEucService;
    private final SparseBooleanArray mEucServiceSwitches;
    private String mLanguageCode;
    private final SparseBooleanArray mLoadedEucrs;
    private final IEucWorkflow mNotificationWorkflow;
    private final SparseArray<String> mOwnIdentitiesCache;
    private final IEucWorkflow mPersistentWorkflow;
    private boolean mServiceModuleBaseStartCalled;
    private final SparseBooleanArray mSimAvailabilityStatuses;
    private boolean mStartInternalCalled;
    private final IEucStoreAndForward mStoreAndForward;
    private final IEucTestEventsFactory mTestEventsFactory;
    private final UserConsentPersistenceNotifier mUserConsentPersistenceNotifier;
    private final IEucWorkflow mVolatileWorkflow;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public EucModule(Looper looper, Context context, IEucServiceInterface eucServiceInterface) {
        super((Looper) Preconditions.checkNotNull(looper));
        this.mContext = (Context) Preconditions.checkNotNull(context);
        boolean z = true;
        Preconditions.checkState(ImsRegistry.getHandlerFactory() != null, "Could not obtain handler factory!");
        this.mEucService = eucServiceInterface;
        this.mEucFactory = new EucFactory();
        this.mTestEventsFactory = new EucTestEventsFactory(this.mEucFactory);
        UserConsentPersistenceNotifier instance = UserConsentPersistenceNotifier.getInstance();
        this.mUserConsentPersistenceNotifier = instance;
        Preconditions.checkState(instance == null ? false : z, "Could not obtain User Consent persistence notifier!");
        this.mEucPersistence = new EucPersistenceNotifier(new EucPersistence(this.mContext, this.mEucFactory), this.mUserConsentPersistenceNotifier);
        this.mDisplayManager = new EucDisplayManager(this.mContext, this);
        this.mLanguageCode = DeviceLocale.DEFAULT_LANG_VALUE;
        this.mDeviceLocale = new DeviceLocale(this.mContext, this);
        Looper looper2 = looper;
        this.mStoreAndForward = new EucStoreAndForward(this.mEucService, looper);
        this.mPersistentWorkflow = new PersistentEucWorkflow(this.mEucPersistence, this.mDisplayManager, this.mEucFactory, this.mStoreAndForward);
        this.mVolatileWorkflow = new VolatileEucWorkflow(this.mContext, this, this.mEucPersistence, this.mDisplayManager, this.mStoreAndForward, this.mEucFactory);
        this.mNotificationWorkflow = new NotificationEucWorkflow(this.mEucPersistence, this.mDisplayManager, this.mStoreAndForward, this.mEucFactory);
        this.mEucPhoneId = DEFAULT_EUC_PHONE_ID;
        this.mServiceModuleBaseStartCalled = false;
        this.mStartInternalCalled = false;
        this.mEucServiceSwitches = new SparseBooleanArray(2);
        this.mSimAvailabilityStatuses = new SparseBooleanArray(2);
        this.mOwnIdentitiesCache = new SparseArray<>(2);
        this.mLoadedEucrs = new SparseBooleanArray(2);
    }

    public String[] getServicesRequiring() {
        return sRequiredServices;
    }

    public void handleIntent(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories.contains(EucTestIntent.CATEGORY_ACTION)) {
            IMSLog.s(LOG_TAG, "handleIntent, Intent=" + intent);
            dumpExtras(intent.getExtras());
            String action = intent.getAction();
            IMSLog.s(LOG_TAG, "handleIntent, Intent action=" + action);
            if (action == null) {
                Log.e(LOG_TAG, "Failure, cannot handle null action!");
                return;
            }
            char c = 65535;
            switch (action.hashCode()) {
                case -1741654218:
                    if (action.equals(EucTestIntent.Action.INCOMING_VOLATILE_EUCR)) {
                        c = 1;
                        break;
                    }
                    break;
                case -541341157:
                    if (action.equals(EucTestIntent.Action.INCOMING_PERSISTENT_EUCR)) {
                        c = 0;
                        break;
                    }
                    break;
                case -478348366:
                    if (action.equals(EucTestIntent.Action.INCOMING_USER_CONSENT)) {
                        c = 5;
                        break;
                    }
                    break;
                case -441957232:
                    if (action.equals(EucTestIntent.Action.INCOMING_ACKNOWLEDGEMENT_EUCR)) {
                        c = 2;
                        break;
                    }
                    break;
                case -419306681:
                    if (action.equals(EucTestIntent.Action.INCOMING_NOTIFICATION_EUCR)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1492054807:
                    if (action.equals(EucTestIntent.Action.SEND_EUCR_RESPONSE)) {
                        c = 6;
                        break;
                    }
                    break;
                case 2002954115:
                    if (action.equals(EucTestIntent.Action.INCOMING_SYSTEM_EUCR)) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    handleEucTestIncomingRequest(1, this.mTestEventsFactory.createPersistent(intent));
                    return;
                case 1:
                    handleEucTestIncomingRequest(2, this.mTestEventsFactory.createVolatile(intent));
                    return;
                case 2:
                    handleEucTestIncomingRequest(4, this.mTestEventsFactory.createAcknowledgement(intent));
                    return;
                case 3:
                    handleEucTestIncomingRequest(3, this.mTestEventsFactory.createNotification(intent));
                    return;
                case 4:
                    handleEucTestIncomingRequest(5, this.mTestEventsFactory.createSystemRequest(intent));
                    return;
                case 5:
                    sendMessage(obtainMessage(7, this.mTestEventsFactory.createUserConsent(intent)));
                    return;
                case 6:
                    handleEucTestSendResponse(intent);
                    return;
                default:
                    IMSLog.s(LOG_TAG, "handleIntent, unsupported action: " + action);
                    return;
            }
        } else {
            IMSLog.s(LOG_TAG, "handleIntent, unsupported category: " + categories);
        }
    }

    private void handleEucTestSendResponse(Intent intent) {
        EucResponseData.Response response;
        IEucData data = this.mTestEventsFactory.createEucData(intent);
        if (data != null) {
            String pin = intent.getStringExtra(EucTestIntent.Extras.USER_PIN);
            if (intent.getBooleanExtra(EucTestIntent.Extras.USER_ACCEPT, false)) {
                response = EucResponseData.Response.ACCEPT;
            } else {
                response = EucResponseData.Response.DECLINE;
            }
            post(new Runnable(pin, data, response, new IEucStoreAndForward.IResponseCallback() {
                public final void onStatus(EucSendResponseStatus eucSendResponseStatus) {
                    EucModule.lambda$handleEucTestSendResponse$0(IEucData.this, eucSendResponseStatus);
                }
            }) {
                public final /* synthetic */ String f$1;
                public final /* synthetic */ IEucData f$2;
                public final /* synthetic */ EucResponseData.Response f$3;
                public final /* synthetic */ IEucStoreAndForward.IResponseCallback f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    EucModule.this.lambda$handleEucTestSendResponse$1$EucModule(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
            return;
        }
        Log.e(LOG_TAG, "Failure, test request is invalid, skipping com.sec.internal.ims.servicemodules.euc.test.action.SEND_EUCR_RESPONSE");
    }

    static /* synthetic */ void lambda$handleEucTestSendResponse$0(IEucData data, EucSendResponseStatus status) {
        String str = LOG_TAG;
        Log.d(str, "Test send response request key=" + data.getKey() + ", send response status=" + status);
    }

    public /* synthetic */ void lambda$handleEucTestSendResponse$1$EucModule(String pin, IEucData data, EucResponseData.Response response, IEucStoreAndForward.IResponseCallback callback) {
        if (pin != null) {
            this.mStoreAndForward.sendResponse(data, response, pin, callback);
        } else {
            this.mStoreAndForward.sendResponse(data, response, callback);
        }
    }

    private <T> void handleEucTestIncomingRequest(int requestId, T request) {
        if (request != null) {
            sendMessage(obtainMessage(requestId, new AsyncResult((Object) null, request, (Throwable) null)));
            return;
        }
        String str = LOG_TAG;
        Log.e(str, "Failure, test request is invalid, skipping request id=" + requestId);
    }

    private void dumpExtras(Bundle bundle) {
        StringBuilder builder = new StringBuilder("Extras:\n");
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                builder.append(key);
                builder.append(": ");
                builder.append(value);
                builder.append("\n");
            }
        }
        Log.i(LOG_TAG, IMSLog.checker(builder.toString()));
    }

    public void init() {
        Log.d(LOG_TAG, "init");
        super.init();
        sendMessage(obtainMessage(11));
    }

    public void start() {
        super.start();
        sendMessage(obtainMessage(12));
    }

    public void stop() {
        super.stop();
        sendMessage(obtainMessage(13));
    }

    public void onConfigured(int phoneId) {
        String str = LOG_TAG;
        Log.d(str, "onConfigured, phoneId = " + phoneId);
        super.onConfigured(phoneId);
        sendMessage(obtainMessage(15, Integer.valueOf(phoneId)));
    }

    public void onRegistered(ImsRegistration regiInfo) {
        String str = LOG_TAG;
        IMSLog.s(str, "onRegistered() " + regiInfo);
        super.onRegistered(regiInfo);
        sendMessage(obtainMessage(16, regiInfo));
    }

    public void onDeregistering(ImsRegistration regiInfo) {
        Log.d(LOG_TAG, "onDeregistering");
        super.onDeregistering(regiInfo);
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        String str = LOG_TAG;
        IMSLog.s(str, "onDeregistered() " + regiInfo);
        super.onDeregistered(regiInfo, errorCode);
        sendMessage(obtainMessage(17, regiInfo));
    }

    public void onSimChanged(int phoneId) {
        Log.d(LOG_TAG, "onSimChanged");
        super.onSimChanged(phoneId);
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        String str = LOG_TAG;
        Log.i(str, "onServiceSwitched, phoneId = " + phoneId + ", switchStatus = " + switchStatus);
        super.onServiceSwitched(phoneId, switchStatus);
        sendMessage(obtainMessage(14, Integer.valueOf(phoneId)));
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 1:
                handleIncomingPersistentMessage((IEucRequest) ((AsyncResult) msg.obj).result);
                return;
            case 2:
                handleIncomingVolatileMessage((IEucRequest) ((AsyncResult) msg.obj).result);
                return;
            case 3:
                handleIncomingNotificationMessage((IEucNotification) ((AsyncResult) msg.obj).result);
                return;
            case 4:
                handleIncomingAckMessage((IEucAcknowledgment) ((AsyncResult) msg.obj).result);
                return;
            case 5:
                handleIncomingSystemMessage((IEucSystemRequest) ((AsyncResult) msg.obj).result);
                return;
            case 6:
                handleReconfigurationResponse((IEucSystemRequest) msg.obj, msg.arg1);
                return;
            case 7:
                handleIncomingAutoconfUserConsent((AutoconfUserConsentData) msg.obj);
                return;
            case 11:
                handleInit();
                return;
            case 12:
                handleStart();
                return;
            case 13:
                handleStop();
                return;
            case 14:
                handleServiceSwitched(((Integer) msg.obj).intValue());
                return;
            case 15:
                handleConfigured(((Integer) msg.obj).intValue());
                return;
            case 16:
                handleRegistered((ImsRegistration) msg.obj);
                return;
            case 17:
                handleDeregistered((ImsRegistration) msg.obj);
                return;
            case 21:
                handleSimReady((ISimManager) ((AsyncResult) msg.obj).userObj);
                return;
            case 22:
                handleSimRefresh((ISimManager) ((AsyncResult) msg.obj).userObj);
                return;
            case 23:
                handleEucPhoneIdChanged();
                return;
            default:
                return;
        }
    }

    private void handleIncomingPersistentMessage(IEucRequest request) {
        String str = LOG_TAG;
        Log.i(str, "handleIncomingPersistentMessage, id=" + request.getEucId());
        this.mPersistentWorkflow.handleIncomingEuc(this.mEucFactory.createEUC(request));
    }

    private void handleIncomingVolatileMessage(IEucRequest request) {
        String str = LOG_TAG;
        Log.i(str, "handleIncomingVolatileMessage, id=" + request.getEucId());
        this.mVolatileWorkflow.handleIncomingEuc(this.mEucFactory.createEUC(request));
    }

    private void handleIncomingAckMessage(IEucAcknowledgment request) {
        String str = LOG_TAG;
        Log.i(str, "handleIncomingAckMessage, id=" + request.getEucId());
        this.mPersistentWorkflow.handleIncomingEuc(this.mEucFactory.createEUC(request));
    }

    private void handleIncomingNotificationMessage(IEucNotification request) {
        String str = LOG_TAG;
        Log.i(str, "handleIncomingNotificationMessage, id=" + request.getEucId());
        this.mNotificationWorkflow.handleIncomingEuc(this.mEucFactory.createEUC(request));
    }

    private void handleIncomingSystemMessage(IEucSystemRequest request) {
        String str = LOG_TAG;
        Log.d(str, "handleIncomingSystemMessage, id=" + request.getEucId() + ", type=" + request.getType());
        String str2 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("message data=");
        sb.append(IMSLog.checker(request.getMessageData()));
        Log.i(str2, sb.toString());
        if (request.getType().equals(IEucSystemRequest.EucSystemRequestType.RECONFIGURE)) {
            ImsRegistry.getConfigModule().onNewRcsConfigurationNeeded(request.getOwnIdentity(), "euc", obtainMessage(6, 0, 0, request));
        }
    }

    private void handleReconfigurationResponse(IEucSystemRequest request, int response) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("handleReconfigurationResponse, id=");
        sb.append(request.getEucId());
        sb.append(", ownIdentity=");
        sb.append(IMSLog.checker(request.getOwnIdentity()));
        sb.append(", response=");
        sb.append(response == 1 ? "accepted" : "rejected");
        Log.i(str, sb.toString());
    }

    private void handleIncomingAutoconfUserConsent(AutoconfUserConsentData userConsentData) {
        String str = LOG_TAG;
        Log.i(str, "handleIncomingAutoconfUserConsent " + IMSLog.checker(userConsentData));
        if (!this.mStartInternalCalled) {
            try {
                this.mEucPersistence.open();
            } catch (EucPersistenceException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "Failure, unable to open persistence: " + e + ". Abandoning configuration consent!");
                return;
            }
        }
        try {
            this.mEucPersistence.insertAutoconfUserConsent(userConsentData);
        } catch (EucPersistenceException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "Unable to store User Consent in persistence: " + e2);
        }
        if (!this.mStartInternalCalled) {
            this.mEucPersistence.close();
        }
    }

    private void handleRegistered(ImsRegistration regiInfo) {
        int phoneId = regiInfo.getPhoneId();
        if (!isMultiSim() && !isEucPhoneId(phoneId)) {
            String str = LOG_TAG;
            Log.i(str, "handleRegistered, ignoring registration for phoneId = " + phoneId);
        } else if (regiInfo.getServices().containsAll(Arrays.asList(sRequiredServices))) {
            String str2 = LOG_TAG;
            Log.d(str2, "handleRegistered, phoneId = " + phoneId);
            String ownIdentity = getOwnIdentity(phoneId);
            if (ownIdentity != null) {
                this.mStoreAndForward.forward(ownIdentity);
                return;
            }
            String str3 = LOG_TAG;
            Log.e(str3, "Could not obtain own identity, ignore registration for phoneId = " + phoneId);
        } else {
            String str4 = LOG_TAG;
            Log.e(str4, "handleRegistered, phoneId = " + phoneId + ", no registration for required services = " + Arrays.toString(sRequiredServices) + ", ignore!");
        }
    }

    private void handleDeregistered(ImsRegistration regiInfo) {
        int phoneId = regiInfo.getPhoneId();
        if (!isMultiSim() && !isEucPhoneId(phoneId)) {
            String str = LOG_TAG;
            Log.i(str, "handleDeregistered, ignoring de-registration for phoneId = " + phoneId);
        } else if (regiInfo.getServices().containsAll(Arrays.asList(sRequiredServices))) {
            String str2 = LOG_TAG;
            Log.d(str2, "handleDeregistered, phoneId = " + phoneId);
            String ownIdentity = getOwnIdentity(phoneId);
            if (ownIdentity != null) {
                this.mStoreAndForward.store(ownIdentity);
                return;
            }
            String str3 = LOG_TAG;
            Log.e(str3, "Could not obtain own identity, ignore de-registration for phoneId = " + phoneId);
        } else {
            String str4 = LOG_TAG;
            Log.e(str4, "handleDeregistered, phoneId = " + phoneId + ", no registration for required services = " + Arrays.toString(sRequiredServices) + ", ignore!");
        }
    }

    private void handleSimReady(ISimManager sm) {
        int phoneId = sm.getSimSlotIndex();
        boolean isSimAvailable = sm.isSimAvailable();
        String str = LOG_TAG;
        Log.i(str, "handleSimReady, phoneId = " + phoneId + ", isSimAvailable = " + isSimAvailable);
        handleSimAvailability(phoneId, isSimAvailable);
    }

    private void handleSimRefresh(ISimManager sm) {
        int phoneId = sm.getSimSlotIndex();
        boolean isSimAvailable = sm.isSimAvailable();
        String str = LOG_TAG;
        Log.i(str, "handleSimRefresh, phoneId = " + phoneId + ", isSimAvailable = " + isSimAvailable);
        handleSimAvailability(phoneId, isSimAvailable);
    }

    private void handleSimAvailability(int phoneId, boolean isSimAvailable) {
        this.mSimAvailabilityStatuses.put(phoneId, isSimAvailable);
        if (isSimAvailable) {
            startConditionally();
            handleSimAvailable(phoneId);
        } else {
            stopConditionally();
        }
        this.mUserConsentPersistenceNotifier.notifyListener(phoneId);
    }

    private void handleSimAvailable(int phoneId) {
        String LOG_MSG_PHONE_ID = ", phoneId = " + phoneId;
        String oldOwnIdentity = getOwnIdentity(phoneId);
        invalidateOwnIdentity(phoneId);
        String newOwnIdentity = getOwnIdentity(phoneId);
        Log.i(LOG_TAG, "handleSimAvailable" + LOG_MSG_PHONE_ID + ", oldOwnIdentity = " + IMSLog.checker(oldOwnIdentity) + ", newOwnIdentity = " + IMSLog.checker(newOwnIdentity));
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("handleSimAvailable, mStartInternalCalled = ");
        sb.append(this.mStartInternalCalled);
        Log.d(str, sb.toString());
        if (this.mStartInternalCalled) {
            if (!TextUtils.equals(oldOwnIdentity, newOwnIdentity)) {
                boolean areLoaded = this.mLoadedEucrs.get(phoneId);
                IMSLog.s(LOG_TAG, "handleSimAvailable, EUCRs areLoaded = " + areLoaded + ", phoneId = " + phoneId + LOG_STRING_OWN_IDENTITY + oldOwnIdentity);
                if (areLoaded && oldOwnIdentity != null) {
                    discardEucrs(oldOwnIdentity);
                    this.mLoadedEucrs.delete(phoneId);
                }
            }
            boolean isSwitchedOn = this.mEucServiceSwitches.get(phoneId);
            boolean isMultiSim = isMultiSim();
            Log.i(LOG_TAG, "handleSimAvailable" + LOG_MSG_PHONE_ID + ", isSwitchedOn = " + isSwitchedOn + ", isMultiSim = " + isMultiSim);
            if (!isSwitchedOn) {
                return;
            }
            if (isMultiSim || isEucPhoneId(phoneId)) {
                loadPendingEucrsConditionally(phoneId);
            }
        }
    }

    private void handleConfigured(int phoneId) {
        String str = LOG_TAG;
        Log.d(str, "handleConfigured, phoneId = " + phoneId + ", mEucPhoneId = " + this.mEucPhoneId);
    }

    private void handleInit() {
        String str = LOG_TAG;
        Log.i(str, "handleInit, isMultiSimIms=" + isMultiSim());
        this.mEucPhoneId = getEucPhoneId();
        initiateServiceSwitches();
        registerForSimEvents();
        initiateSimAvailabilityStatuses();
        notifyOnInit();
    }

    private void registerForSimEvents() {
        if (SimUtil.getPhoneCount() > 1 && SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS())) {
            SimManagerFactory.registerForDDSChange(this, 23, (Object) null);
        }
        for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
            registerForSimReady(sm);
            registerForSimRefresh(sm);
        }
    }

    private void registerForSimReady(ISimManager sm) {
        String str = LOG_TAG;
        Log.d(str, "registerForSimReady, phoneId = " + sm.getSimSlotIndex());
        sm.registerForSimReady(this, 21, sm);
    }

    private void registerForSimRefresh(ISimManager sm) {
        String str = LOG_TAG;
        Log.d(str, "registerForSimRefresh, phoneId = " + sm.getSimSlotIndex());
        sm.registerForSimRefresh(this, 22, sm);
        sm.registerForSimRemoved(this, 22, sm);
    }

    private void handleStart() {
        Preconditions.checkState(!this.mServiceModuleBaseStartCalled, "Shall not happen! Something wrong with IMS framework lifecycle, Euc module already started!");
        this.mServiceModuleBaseStartCalled = true;
        startConditionally();
    }

    private void handleStop() {
        Preconditions.checkState(this.mServiceModuleBaseStartCalled, "Shall not happen! Something wrong with IMS framework lifecycle, Euc module already stopped!");
        this.mServiceModuleBaseStartCalled = false;
        stopOnServiceModuleBaseStop();
    }

    private void handleServiceSwitched(int phoneId) {
        boolean isEucOn = isEucSwitchedOn(phoneId);
        String str = LOG_TAG;
        Log.i(str, "handleServiceSwitched, phoneId = " + phoneId + ", isEucOn = " + isEucOn);
        this.mEucServiceSwitches.put(phoneId, isEucOn);
        if (isEucOn) {
            startConditionally();
            if ((isMultiSim() || isEucPhoneId(phoneId)) && this.mSimAvailabilityStatuses.valueAt(phoneId) && this.mStartInternalCalled) {
                loadPendingEucrsConditionally(phoneId);
                return;
            }
            return;
        }
        if ((isMultiSim() || isEucPhoneId(phoneId)) && this.mStartInternalCalled) {
            discardEucrsConditionally(phoneId);
        }
        stopConditionally();
    }

    private void handleEucPhoneIdChanged() {
        Log.d(LOG_TAG, "handleEucPhoneIdChanged");
        boolean couldBeStarted = true;
        Preconditions.checkState(!isMultiSim(), "Shall not happen!");
        int newEucPhoneId = getEucPhoneId();
        if (!isEucPhoneId(newEucPhoneId)) {
            discardEucrsConditionally(this.mEucPhoneId);
            this.mEucPhoneId = newEucPhoneId;
            if (!this.mEucServiceSwitches.get(newEucPhoneId) || !this.mSimAvailabilityStatuses.get(this.mEucPhoneId)) {
                couldBeStarted = false;
            }
            String str = LOG_TAG;
            Log.i(str, "handleEucPhoneIdChanged, newEucPhoneId=" + newEucPhoneId + ", couldBeStarted=" + couldBeStarted + ", mStartInternalCalled=" + this.mStartInternalCalled);
            if (this.mStartInternalCalled && couldBeStarted) {
                loadPendingEucrsConditionally(this.mEucPhoneId);
            } else if (this.mStartInternalCalled) {
                stopInternal();
            } else if (couldBeStarted) {
                startInternal();
            }
        }
    }

    private boolean isEucSwitchedOn(int phoneId) {
        Integer eucSwitchStatus = (Integer) DmConfigHelper.getImsSwitchValue(this.mContext, getServicesRequiring(), phoneId).get("euc");
        boolean isOn = true;
        if (eucSwitchStatus == null || eucSwitchStatus.intValue() != 1) {
            isOn = false;
        }
        String str = LOG_TAG;
        Log.d(str, "Euc switch = " + isOn + " for phoneId = " + phoneId);
        return isOn;
    }

    private void loadPendingEucrsConditionally(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "loadPendingEucrsConditionally, phoneId=" + phoneId);
        String ownIdentity = getOwnIdentity(phoneId);
        boolean isLoaded = this.mLoadedEucrs.get(phoneId);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "loadPendingEucrsConditionally, phoneId=" + phoneId + LOG_STRING_OWN_IDENTITY + ownIdentity + ", isLoaded=" + isLoaded);
        if (ownIdentity != null && !isLoaded) {
            loadPendingEucrs(ownIdentity);
            this.mLoadedEucrs.put(phoneId, true);
        }
    }

    private void discardEucrsConditionally(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "discardEucrsConditionally, phoneId=" + phoneId);
        String ownIdentity = getOwnIdentity(phoneId);
        boolean isLoaded = this.mLoadedEucrs.get(phoneId);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "discardEucrsConditionally, phoneId=" + phoneId + LOG_STRING_OWN_IDENTITY + ownIdentity + ", isLoaded=" + isLoaded);
        if (ownIdentity != null && isLoaded) {
            discardEucrs(ownIdentity);
            this.mLoadedEucrs.delete(phoneId);
        }
    }

    private void loadPendingEucrs(String ownIdentity) {
        String str = LOG_TAG;
        Log.i(str, "loadPendingEucrs, ownIdentity = " + IMSLog.checker(ownIdentity));
        this.mPersistentWorkflow.load(ownIdentity);
        this.mVolatileWorkflow.load(ownIdentity);
        this.mNotificationWorkflow.load(ownIdentity);
    }

    private void discardEucrs(String ownIdentity) {
        String str = LOG_TAG;
        Log.i(str, "discardEucrs, ownIdentity = " + IMSLog.checker(ownIdentity));
        this.mPersistentWorkflow.discard(ownIdentity);
        this.mVolatileWorkflow.discard(ownIdentity);
        this.mNotificationWorkflow.discard(ownIdentity);
    }

    private String getOwnIdentityNotCached(int phoneId) {
        String ownIdentity = null;
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm != null) {
            ownIdentity = sm.getImsi();
            if (TextUtils.isEmpty(ownIdentity)) {
                ownIdentity = null;
            }
        } else {
            String str = LOG_TAG;
            Log.e(str, "getOwnIdentityNotCached, Unable to find sim manager related to phoneId = " + phoneId);
        }
        String str2 = LOG_TAG;
        IMSLog.s(str2, "getOwnIdentityNotCached, phoneId = " + phoneId + ", result = " + ownIdentity);
        return ownIdentity;
    }

    private String getOwnIdentity(int phoneId) {
        String ownIdentity = this.mOwnIdentitiesCache.get(phoneId);
        if (ownIdentity == null && (ownIdentity = getOwnIdentityNotCached(phoneId)) != null) {
            this.mOwnIdentitiesCache.put(phoneId, ownIdentity);
        }
        String str = LOG_TAG;
        IMSLog.s(str, "getOwnIdentity, phoneId=" + phoneId + ", result=" + ownIdentity);
        return ownIdentity;
    }

    private void invalidateOwnIdentity(int phoneId) {
        String str = LOG_TAG;
        Log.d(str, "invalidateOwnIdentity, phoneId=" + phoneId);
        this.mOwnIdentitiesCache.delete(phoneId);
    }

    private void invalidateOwnIdentities() {
        Log.d(LOG_TAG, "invalidateOwnIdentities");
        this.mOwnIdentitiesCache.clear();
    }

    private void performStartupRegistrations() {
        Log.d(LOG_TAG, "performStartupRegistrations");
        this.mEucService.registerForPersistentMessage(this, 1, (Object) null);
        this.mEucService.registerForVolatileMessage(this, 2, (Object) null);
        this.mEucService.registerForAckMessage(this, 4, (Object) null);
        this.mEucService.registerForNotificationMessage(this, 3, (Object) null);
        this.mEucService.registerForSystemMessage(this, 5, (Object) null);
        this.mVolatileWorkflow.start();
        this.mDeviceLocale.start(new IDeviceLocale.IDeviceLocaleListener() {
            public final void onLocaleChanged(Locale locale) {
                EucModule.this.lambda$performStartupRegistrations$2$EucModule(locale);
            }
        });
        this.mDisplayManager.start();
    }

    public /* synthetic */ void lambda$performStartupRegistrations$2$EucModule(Locale newLocale) {
        String languageCode = this.mDeviceLocale.getLanguageCode(newLocale);
        if (!this.mLanguageCode.equals(languageCode)) {
            String str = LOG_TAG;
            Log.d(str, "Changing languageCode to " + languageCode);
            this.mLanguageCode = languageCode;
            this.mPersistentWorkflow.changeLanguage(languageCode);
            this.mVolatileWorkflow.changeLanguage(languageCode);
            this.mNotificationWorkflow.changeLanguage(languageCode);
        }
    }

    private void performShutdownDeregistrations() {
        Log.d(LOG_TAG, "performShutdownDeregistrations");
        this.mEucService.unregisterForPersistentMessage(this);
        this.mEucService.unregisterForVolatileMessage(this);
        this.mEucService.unregisterForAckMessage(this);
        this.mEucService.unregisterForNotificationMessage(this);
        this.mEucService.unregisterForSystemMessage(this);
        this.mVolatileWorkflow.stop();
        this.mDeviceLocale.stop();
        this.mDisplayManager.stop();
    }

    private boolean isMultiSim() {
        String config = SimUtil.getConfigDualIMS();
        return !SimConstants.SINGLE.equals(config) && !SimConstants.DSDS_SI_DDS.equals(config);
    }

    private boolean isEucPhoneId(int phoneId) {
        return phoneId == this.mEucPhoneId;
    }

    private boolean isAtLeastOneSimAvailableAndSwitchedOn() {
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            if (this.mEucServiceSwitches.get(i) && this.mSimAvailabilityStatuses.get(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkConditionsForInternalStart() {
        boolean couldBeStarted;
        if (isMultiSim()) {
            couldBeStarted = isAtLeastOneSimAvailableAndSwitchedOn();
        } else {
            couldBeStarted = this.mEucServiceSwitches.get(this.mEucPhoneId) && this.mSimAvailabilityStatuses.get(this.mEucPhoneId);
        }
        String str = LOG_TAG;
        Log.i(str, "checkConditionsForInternalStart: mStartInternalCalled=" + this.mStartInternalCalled + ", couldBeStarted=" + couldBeStarted);
        if (this.mStartInternalCalled || !couldBeStarted) {
            return false;
        }
        return true;
    }

    private boolean checkConditionsForInternalStop() {
        boolean couldBeStopped;
        if (isMultiSim()) {
            couldBeStopped = !isAtLeastOneSimAvailableAndSwitchedOn();
        } else {
            couldBeStopped = !this.mEucServiceSwitches.get(this.mEucPhoneId) || !this.mSimAvailabilityStatuses.get(this.mEucPhoneId);
        }
        Log.i(LOG_TAG, "checkConditionsForInternalStop: mStartInternalCalled=" + this.mStartInternalCalled + ", couldBeStopped=" + couldBeStopped);
        if (!this.mStartInternalCalled || !couldBeStopped) {
            return false;
        }
        return true;
    }

    private void startConditionally() {
        Log.d(LOG_TAG, "startConditionally");
        if (checkConditionsForInternalStart()) {
            startInternal();
        }
    }

    private void stopConditionally() {
        Log.d(LOG_TAG, "stopConditionally");
        if (checkConditionsForInternalStop()) {
            stopInternal();
        }
    }

    private void stopOnServiceModuleBaseStop() {
        String str = LOG_TAG;
        Log.d(str, "stopOnServiceModuleBaseStop, mStartInternalCalled=" + this.mStartInternalCalled);
        if (this.mStartInternalCalled) {
            stopInternal();
        }
    }

    private void startInternal() {
        Log.d(LOG_TAG, "startInternal");
        Preconditions.checkState(!this.mStartInternalCalled, "startInternal was already called!");
        try {
            this.mEucPersistence.open();
            this.mStartInternalCalled = true;
            performStartupRegistrations();
            if (isMultiSim()) {
                for (int i = 0; i < this.mEucServiceSwitches.size(); i++) {
                    int phoneId = this.mEucServiceSwitches.keyAt(i);
                    if (this.mEucServiceSwitches.valueAt(i) && this.mSimAvailabilityStatuses.valueAt(i)) {
                        loadPendingEucrsConditionally(phoneId);
                    }
                }
                return;
            }
            loadPendingEucrsConditionally(this.mEucPhoneId);
        } catch (EucPersistenceException e) {
            String str = LOG_TAG;
            Log.e(str, "Failure, unable to open persistence: " + e + ". Cannot start!");
        }
    }

    private void stopInternal() {
        Log.d(LOG_TAG, "stopInternal");
        Preconditions.checkState(this.mStartInternalCalled, "startInternal was not yet called!");
        if (isMultiSim()) {
            for (int i = 0; i < this.mEucServiceSwitches.size(); i++) {
                int phoneId = this.mEucServiceSwitches.keyAt(i);
                if (this.mEucServiceSwitches.valueAt(i)) {
                    discardEucrsConditionally(phoneId);
                }
            }
        } else {
            discardEucrsConditionally(this.mEucPhoneId);
        }
        performShutdownDeregistrations();
        invalidateOwnIdentities();
        this.mEucPersistence.close();
        this.mStartInternalCalled = false;
    }

    private int getEucPhoneId() {
        String str = LOG_TAG;
        Log.d(str, "ConfigDualIMS is " + SimUtil.getConfigDualIMS());
        int phoneId = DEFAULT_EUC_PHONE_ID;
        if (SimUtil.getPhoneCount() > 1 && SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS())) {
            phoneId = SimUtil.getDefaultPhoneId();
        }
        String str2 = LOG_TAG;
        Log.d(str2, "Euc phoneId = " + phoneId);
        return phoneId;
    }

    private void initiateServiceSwitches() {
        Log.d(LOG_TAG, "initiateServiceSwitches");
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            this.mEucServiceSwitches.put(i, isEucSwitchedOn(i));
        }
    }

    private void initiateSimAvailabilityStatuses() {
        Log.d(LOG_TAG, "initiateSimAvailabilityStatuses");
        for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
            this.mSimAvailabilityStatuses.put(sm.getSimSlotIndex(), sm.isSimAvailable());
        }
    }

    private void notifyOnInit() {
        Log.d(LOG_TAG, "notifyOnInit");
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            this.mUserConsentPersistenceNotifier.notifyListener(i);
        }
    }
}
