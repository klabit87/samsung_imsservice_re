package com.sec.internal.ims.entitlement.nsds.app.flow.o2uentitlement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus;
import com.sec.internal.constants.ims.entitilement.data.ServiceEntitlement;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BulkEntitlementCheck;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class O2UEntitlementCheckFlow extends NSDSAppFlowBase implements IEntitlementCheck {
    private static final int DEFAULT_POLL_INTERVAL = 24;
    private static final int INIT_ENTITLEMENT_CHECK = 0;
    private static final String LOG_TAG = O2UEntitlementCheckFlow.class.getSimpleName();
    private static final int SERVICE_ENTITLEMENT_CHECK = 1;
    private AtomicBoolean mIsDeviceInEntitlementProgress = new AtomicBoolean(false);
    private boolean mIsVolteEntitled = false;
    private boolean mIsVowifiEntitled = false;
    private int mPollInterval = 24;

    private boolean isDeviceInEntitlementProgress() {
        return this.mIsDeviceInEntitlementProgress.get();
    }

    public O2UEntitlementCheckFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper, context, baseFlowImpl, databaseHelper);
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleEntitlementCheckResponse(Bundle bundleNSDSResponses) {
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS, -1);
        if (bundleNSDSResponses == null) {
            return nsdsResponseStatus;
        }
        ResponseServiceEntitlementStatus entitlementStatus = (ResponseServiceEntitlementStatus) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS);
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus2 = handleResponseEntitlementStatus(entitlementStatus);
        setEntitlementInfo(entitlementStatus);
        setPollIntervalInfo(entitlementStatus);
        return nsdsResponseStatus2;
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleResponseEntitlementStatus(ResponseServiceEntitlementStatus response) {
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS, -1);
        if (response != null) {
            if (response.responseCode != 1000 || response.serviceEntitlementList == null) {
                nsdsResponseStatus.responseCode = response.responseCode;
            } else {
                ArrayList<ServiceEntitlement> list = response.serviceEntitlementList;
                String str = LOG_TAG;
                IMSLog.i(str, "serviceEntitlementList:" + list.toString());
                for (T se : emptyIfNull(list)) {
                    if (se.entitlementStatus == 1000) {
                        nsdsResponseStatus.responseCode = 1000;
                    } else if (se.entitlementStatus == 1048) {
                        nsdsResponseStatus.responseCode = NSDSNamespaces.NSDSResponseCode.ERROR_SERVICE_NOT_ENTITLED;
                    }
                }
            }
        }
        return nsdsResponseStatus;
    }

    private void setPollIntervalInfo(ResponseServiceEntitlementStatus entitlementStatus) {
        if (entitlementStatus != null && entitlementStatus.pollInterval != null) {
            this.mPollInterval = entitlementStatus.pollInterval.intValue();
        }
    }

    private void setEntitlementInfo(ResponseServiceEntitlementStatus entitlementStatus) {
        this.mIsVowifiEntitled = false;
        this.mIsVolteEntitled = false;
        if (entitlementStatus != null) {
            IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
            if (entitlementStatus.responseCode == 1000) {
                for (T se : emptyIfNull(entitlementStatus.serviceEntitlementList)) {
                    int serviceId = -1;
                    if (!(mnoStrategy == null || mnoStrategy.getEntitlementServicesMap().get(se.serviceName) == null)) {
                        serviceId = mnoStrategy.getEntitlementServicesMap().get(se.serviceName).intValue();
                    }
                    boolean z = true;
                    if (serviceId == 1) {
                        if (se.entitlementStatus != 1000) {
                            z = false;
                        }
                        this.mIsVowifiEntitled = z;
                    } else if (serviceId == 2) {
                        if (se.entitlementStatus != 1000) {
                            z = false;
                        }
                        this.mIsVolteEntitled = z;
                    }
                }
            }
        }
    }

    public void performEntitlementCheck(int deviceEventType, int retryCount) {
        this.mRetryCount = retryCount;
        if (isDeviceInEntitlementProgress()) {
            deferMessage(obtainMessage(0, deviceEventType, retryCount));
        } else {
            sendMessage(obtainMessage(0, deviceEventType, retryCount));
        }
    }

    private void performNextOperation(int deviceEventType, int retryCount) {
        this.mIsDeviceInEntitlementProgress.set(true);
        this.mDeviceEventType = deviceEventType;
        this.mRetryCount = retryCount;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(1000, NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS, -1), (Bundle) null);
    }

    private void checkServiceEntitlement() {
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null) {
            if (this.mDeviceEventType == 10) {
                this.mRetryCount++;
            }
            new BulkEntitlementCheck(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), mnoStrategy.getNSDSApiVersion()).checkBulkEntitlement(new ArrayList<>(Arrays.asList(mnoStrategy.getNSDSServices())), mnoStrategy.needGetMSISDNForEntitlement());
        }
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nextOperation, Bundle dataMap) {
        if (nextOperation == 2) {
            Message message = obtainMessage(1);
            message.setData(dataMap);
            sendMessage(message);
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 0) {
            performNextOperation(msg.arg1, msg.arg2);
        } else if (i == 1) {
            checkServiceEntitlement();
        } else if (i == 101) {
            performNextOperationIf(2, handleEntitlementCheckResponse(msg.getData()), (Bundle) null);
        }
    }

    private void resetEntitlementStatus() {
        this.mIsDeviceInEntitlementProgress.set(false);
        NSDSSharedPrefHelper.removePrefForSlot(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex(), NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
        clearDeferredMessage();
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String method, int operation, int code) {
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, success);
        intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOWIFI, this.mIsVowifiEntitled);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOLTE, this.mIsVolteEntitled);
        intent.putExtra(NSDSNamespaces.NSDSExtras.POLL_INTERVAL, this.mPollInterval);
        intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
        intent.putExtra("retry_count", this.mRetryCount);
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQ_TOGGLE_OFF_OP, false);
        intent.putExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, code);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        intent.putExtra("phoneId", this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        postDelayed(new Runnable(intent) {
            public final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                O2UEntitlementCheckFlow.this.lambda$notifyNSDSFlowResponse$0$O2UEntitlementCheckFlow(this.f$1);
            }
        }, 1000);
        String str = LOG_TAG;
        IMSLog.i(str, "notifyNSDSFlowResponse: isSuccess: " + success + ", isVowifiEntitled: " + this.mIsVowifiEntitled + ", isVolteEntitled: " + this.mIsVolteEntitled + ", pollInterval: " + this.mPollInterval + ", mRetryCount: " + this.mRetryCount + ", ErrorCode: " + code);
        NSDSSharedPrefHelper.setEntitlementCompleted(this.mContext, NSDSNamespaces.NSDSExtras.SERVICE_VOWIFI, this.mIsVowifiEntitled, this.mBaseFlowImpl.getDeviceId());
        NSDSSharedPrefHelper.setEntitlementCompleted(this.mContext, NSDSNamespaces.NSDSExtras.SERVICE_VOLTE, this.mIsVolteEntitled, this.mBaseFlowImpl.getDeviceId());
        resetEntitlementStatus();
    }

    public /* synthetic */ void lambda$notifyNSDSFlowResponse$0$O2UEntitlementCheckFlow(Intent intent) {
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    public void performE911AddressUpdate(int deviceEventType) {
    }

    public void performRemovePushToken(int deviceEventType) {
    }
}
