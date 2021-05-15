package com.sec.internal.ims.aec;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import com.samsung.android.feature.SemCscFeature;
import com.sec.ims.extensions.ReflectionUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.aec.receiver.SmsNotification;
import com.sec.internal.ims.aec.receiver.fcm.FcmNotification;
import com.sec.internal.ims.aec.util.DefaultNetwork;
import com.sec.internal.ims.aec.workflow.WorkflowFactory;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.aec.IWorkflowImpl;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.AECLog;

public class AECModule extends Handler implements IAECModule {
    private static final String CONFIG_MDMNTYPE = "CscFeature_IMS_ConfigMdmnType";
    private static final String DISABLE_TS43 = "disable_ts43";
    private static final String PROPERTY_ICC_TYPE = "ril.ICC_TYPE";
    private final String LOG_TAG = AECModule.class.getSimpleName();
    private final AECResult mAECResult;
    private final Context mContext;
    private final DefaultNetwork mDefaultNetwork;
    /* access modifiers changed from: private */
    public boolean mIsPsDataRoaming = false;
    /* access modifiers changed from: private */
    public final WorkflowFactory mWorkflowFactory;

    public AECModule(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        this.mAECResult = new AECResult(context);
        this.mDefaultNetwork = new DefaultNetwork(this.mContext, this);
        this.mWorkflowFactory = WorkflowFactory.createWorkflowFactory(this.mContext);
        sendEmptyMessage(0);
    }

    public void initSequentially() {
        for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
            sm.registerForSimReady(this, 1, (Object) null);
            sm.registerForSimRemoved(this, 2, (Object) null);
        }
        registerListenerForDataRoamingState();
        ImsRegistry.getFcmHandler().registerFcmEventListener(new FcmNotification(this));
        SmsNotification smsReceiver = new SmsNotification(this.mContext, this);
        this.mContext.registerReceiver(smsReceiver, smsReceiver.getIntentFilter());
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                onInitWorkflow();
                return;
            case 1:
                onSimReady(((Integer) ((AsyncResult) msg.obj).result).intValue());
                return;
            case 2:
                onSimRemoved(((Integer) ((AsyncResult) msg.obj).result).intValue());
                return;
            case 3:
                onChangeConnectivity();
                return;
            case 4:
                onStartEntitlement(msg);
                return;
            case 5:
                onCompletedEntitlement(msg);
                return;
            case 6:
                onStopEntitlement(msg);
                return;
            case 7:
                onReceivedFcmNotification((Bundle) msg.obj);
                return;
            case 8:
                onReceivedSmsNotification(msg);
                return;
            default:
                return;
        }
    }

    private void onInitWorkflow() {
        int phoneCount = SimUtil.getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            resetHttpResponse(i);
        }
    }

    private void onStartEntitlement(Message msg) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(msg.arg1);
        if (workflow != null) {
            workflow.performEntitlement((Object) null);
        }
    }

    private void onCompletedEntitlement(Message msg) {
        IWorkflowImpl workflow;
        if (msg != null && (workflow = this.mWorkflowFactory.getWorkflow(msg.arg1)) != null) {
            if (workflow.getEntitlementForVoLte()) {
                this.mAECResult.handleCompletedEntitlementVoLTE(msg);
            }
            if (workflow.getEntitlementForVoWiFi()) {
                if (!workflow.getEntitlementInitFromApp()) {
                    this.mAECResult.handleCompletedEntitlementVoWIFI(msg);
                } else if (workflow.isReadyToNotifyApp()) {
                    workflow.setReadyToNotifyApp(false);
                    this.mAECResult.handleCompletedEntitlementVoWIFI(msg);
                }
            }
            this.mAECResult.updateAkaToken(msg.arg1, msg.arg2);
        }
    }

    private void onStopEntitlement(Message msg) {
        if (msg != null) {
            this.mAECResult.updateAkaToken(msg.arg1, msg.arg2);
        }
    }

    private void onChangeConnectivity() {
        SparseArray<IWorkflowImpl> workflowArray = this.mWorkflowFactory.getAllWorkflow();
        for (int i = 0; i < workflowArray.size(); i++) {
            IWorkflowImpl workflow = workflowArray.get(workflowArray.keyAt(i));
            if (workflow != null) {
                workflow.changeConnectivity();
            }
        }
    }

    private void onReceivedFcmNotification(Bundle bundle) {
        String from = bundle.getString("from");
        String app = bundle.getString("app");
        String timeStamp = bundle.getString("timestamp");
        if (from != null) {
            SparseArray<IWorkflowImpl> workflowArray = this.mWorkflowFactory.getAllWorkflow();
            for (int i = 0; i < workflowArray.size(); i++) {
                IWorkflowImpl workflow = workflowArray.get(workflowArray.keyAt(i));
                if (workflow != null) {
                    workflow.receivedFcmNotification(from, app, timeStamp);
                }
            }
        }
    }

    private void onReceivedSmsNotification(Message msg) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(msg.arg1);
        if (workflow != null) {
            workflow.receivedSmsNotification((String) msg.obj);
        }
    }

    private void onSimReady(int phoneId) {
        if (!isSimAbsent(phoneId) && isEntitlementRequired(phoneId)) {
            AECLog.d(this.LOG_TAG, "onSimReady", phoneId);
            IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(phoneId);
            if (workflow != null) {
                workflow.clearResource();
                this.mWorkflowFactory.clearWorkflow(phoneId);
            }
            createWorkflow(phoneId);
            this.mDefaultNetwork.registerDefaultNetworkCallback();
        }
    }

    private void onSimRemoved(int phoneId) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(phoneId);
        if (workflow != null) {
            AECLog.d(this.LOG_TAG, "onSimRemoved", phoneId);
            workflow.clearResource();
            this.mAECResult.setAkaTokenReady(phoneId, false);
            this.mWorkflowFactory.clearWorkflow(phoneId);
            this.mDefaultNetwork.unregisterNetworkCallback();
        }
    }

    private boolean isSimAbsent(int phoneId) {
        ISimManager sm = (ISimManager) SimManagerFactory.getAllSimManagers().get(phoneId);
        return sm != null && (sm.hasNoSim() || sm.hasVsim());
    }

    private void resetHttpResponse(int phoneId) {
        SharedPreferences sp = this.mContext.getSharedPreferences(String.format(AECNamespace.Template.AEC_RESULT, new Object[]{Integer.valueOf(phoneId)}), 0);
        if (sp != null) {
            int httpResponse = Integer.parseInt(sp.getString(AECNamespace.Path.RESPONSE, "0"));
            if (httpResponse == 400 || httpResponse == 403 || httpResponse == 500) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(AECNamespace.Path.RESPONSE, "0");
                editor.commit();
                String str = this.LOG_TAG;
                AECLog.i(str, "resetHttpResponse: " + httpResponse, phoneId);
            }
        }
    }

    private void createWorkflow(int phoneId) {
        ISimManager simManager = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (simManager != null && !TextUtils.isEmpty(simManager.getImsi())) {
            if (this.mWorkflowFactory.createWorkflow(phoneId, simManager.getImsi(), simManager.getSimMnoName(), getEntitlementServer(phoneId), this)) {
                sendMessage(obtainMessage(4, phoneId, 0, 0));
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean getDataRoaming(ServiceState state) {
        try {
            return ((Boolean) ReflectionUtils.invoke2(ServiceState.class.getMethod("getDataRoaming", new Class[0]), state, new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void registerListenerForDataRoamingState() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (tm != null) {
            tm.listen(new PhoneStateListener() {
                public void onServiceStateChanged(ServiceState state) {
                    if (AECModule.this.mIsPsDataRoaming != AECModule.this.getDataRoaming(state)) {
                        AECModule aECModule = AECModule.this;
                        boolean unused = aECModule.mIsPsDataRoaming = aECModule.getDataRoaming(state);
                        SparseArray<IWorkflowImpl> workflowArray = AECModule.this.mWorkflowFactory.getAllWorkflow();
                        for (int i = 0; i < workflowArray.size(); i++) {
                            IWorkflowImpl workflow = workflowArray.get(workflowArray.keyAt(i));
                            if (workflow != null) {
                                workflow.setPsDataRoaming(AECModule.this.mIsPsDataRoaming);
                            }
                        }
                    }
                }
            }, 1);
        }
    }

    private String getEntitlementServer(int phoneId) {
        return ImsRegistry.getString(phoneId, GlobalSettingsConstants.Entitlement.SUPPORT_CONFIGSERVER, "");
    }

    public boolean isEntitlementDisabled(int phoneId) {
        return DISABLE_TS43.equalsIgnoreCase(SemCscFeature.getInstance().getString(phoneId, "CscFeature_IMS_ConfigMdmnType"));
    }

    public boolean isEntitlementRequired(int phoneId) {
        if (isEntitlementDisabled(phoneId)) {
            AECLog.i(this.LOG_TAG, "disabled by csc feature", phoneId);
            return false;
        } else if (!"ts43".equalsIgnoreCase(getEntitlementServer(phoneId))) {
            AECLog.i(this.LOG_TAG, "unsupported entitlement server", phoneId);
            return false;
        } else {
            if (!"2".equals(TelephonyManager.getTelephonyProperty(PROPERTY_ICC_TYPE + phoneId, "0"))) {
                AECLog.i(this.LOG_TAG, "unsupported icc type", phoneId);
                return false;
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, phoneId) == 1) {
                return true;
            } else {
                AECLog.i(this.LOG_TAG, "disabled ImsSwitch", phoneId);
                return false;
            }
        }
    }

    public boolean getSMSoIpEntitlementStatus(int phoneId) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(phoneId);
        if (workflow != null) {
            return workflow.getSMSoIpEntitlementStatus();
        }
        return false;
    }

    public boolean getVoLteEntitlementStatus(int phoneId) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(phoneId);
        if (workflow != null) {
            return workflow.getVoLteEntitlementStatus();
        }
        return false;
    }

    public boolean getVoWiFiEntitlementStatus(int phoneId) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(phoneId);
        if (workflow != null) {
            return workflow.getVoWiFiEntitlementStatus();
        }
        return false;
    }

    public String getAkaToken(int phoneId) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(phoneId);
        if (workflow == null) {
            return AECNamespace.AkaAuthResultType.AKA_NOT_PROCESS;
        }
        String akaToken = workflow.getAkaToken();
        if (workflow.isEntitlementOngoing()) {
            workflow.setSharedAkaToken(false);
            this.mAECResult.setAkaTokenReady(phoneId, true);
            return "InProgress";
        } else if (TextUtils.isEmpty(akaToken) || workflow.isSharedAkaToken()) {
            workflow.clearAkaToken();
            workflow.setSharedAkaToken(false);
            workflow.setValidEntitlement(false);
            workflow.performEntitlement((Object) null);
            this.mAECResult.setAkaTokenReady(phoneId, true);
            return "InProgress";
        } else {
            workflow.setSharedAkaToken(true);
            return akaToken;
        }
    }

    public void dump() {
        SparseArray<IWorkflowImpl> workflowArray = this.mWorkflowFactory.getAllWorkflow();
        for (int i = 0; i < workflowArray.size(); i++) {
            IWorkflowImpl workflow = workflowArray.get(workflowArray.keyAt(i));
            if (workflow != null) {
                workflow.dump();
            }
        }
    }

    public void triggerAutoConfigForApp(int phoneId) {
        IWorkflowImpl workflow = this.mWorkflowFactory.getWorkflow(phoneId);
        if (workflow != null) {
            resetHttpResponse(phoneId);
            workflow.setValidEntitlement(false);
            workflow.setReadyToNotifyApp(true);
            workflow.performEntitlement((Object) null);
        }
    }
}
