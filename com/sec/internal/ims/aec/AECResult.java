package com.sec.internal.ims.aec;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.log.AECLog;
import java.util.concurrent.atomic.AtomicBoolean;

public class AECResult {
    private final String LOG_TAG = AECResult.class.getSimpleName();
    private AtomicBoolean[] mAkaTokenReady = {new AtomicBoolean(false), new AtomicBoolean(false)};
    private final Context mContext;

    AECResult(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public void sendTryRegister(int phoneId) {
        ImsRegistry.getRegistrationManager().requestTryRegister(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void sendDeRegister(int phoneId) {
        ImsRegistry.getRegistrationManager().sendDeregister(144, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void handleCompletedEntitlementVoLTE(Message msg) {
        int autoOn = ((Bundle) msg.obj).getInt(AECNamespace.BundleData.VOLTE_AUTO_ON, 0);
        int status = ((Bundle) msg.obj).getInt(AECNamespace.BundleData.VOLTE_ENTITLEMENT_STATUS, 0);
        int version = ((Bundle) msg.obj).getInt("version", 0);
        IUtServiceModule usm = ImsRegistry.getServiceModuleManager().getUtServiceModule();
        if (version <= 0 || status != 1) {
            AECLog.i(this.LOG_TAG, "handleCompletedEntitlementVoLTE: off", msg.arg1);
            if (usm != null) {
                usm.enableUt(msg.arg1, false);
            }
            if (DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), msg.arg1) == 0) {
                sendDeRegister(msg.arg1);
                return;
            }
            return;
        }
        if (autoOn == 1) {
            AECLog.i(this.LOG_TAG, "handleCompletedEntitlementVoLTE: auto on", msg.arg1);
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, msg.arg1);
        } else {
            int callType = DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), msg.arg1);
            if (callType == -1) {
                AECLog.i(this.LOG_TAG, "handleCompletedEntitlementVoLTE: default on", msg.arg1);
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, msg.arg1);
            } else {
                String str = this.LOG_TAG;
                AECLog.i(str, "handleCompletedEntitlementVoLTE: " + callType, msg.arg1);
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, callType, msg.arg1);
            }
        }
        if (usm != null) {
            usm.enableUt(msg.arg1, true);
        }
        if (DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), msg.arg1) == 0) {
            sendTryRegister(msg.arg1);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleCompletedEntitlementVoWIFI(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        if (bundle != null) {
            Intent intent = new Intent(AECNamespace.Action.COMPLETED_ENTITLEMENT);
            intent.putExtra("phoneId", bundle.getInt("phoneId"));
            intent.putExtra("version", bundle.getInt("version"));
            intent.putExtra(AECNamespace.BundleData.VOWIFI_AUTO_ON, bundle.getInt(AECNamespace.BundleData.VOWIFI_AUTO_ON));
            intent.putExtra(AECNamespace.BundleData.VOWIFI_ACTIVATION_MODE, bundle.getInt(AECNamespace.BundleData.VOWIFI_ACTIVATION_MODE));
            intent.putExtra(AECNamespace.BundleData.VOWFIFI_SERVICEFLOW_URL, bundle.getString(AECNamespace.BundleData.VOWFIFI_SERVICEFLOW_URL));
            intent.putExtra(AECNamespace.BundleData.VOWFIFI_SERVICEFLOW_USERDATA, bundle.getString(AECNamespace.BundleData.VOWFIFI_SERVICEFLOW_USERDATA));
            intent.putExtra(AECNamespace.BundleData.VOWIFI_MESSAGE_FOR_INCOMPATIBLE, bundle.getString(AECNamespace.BundleData.VOWIFI_MESSAGE_FOR_INCOMPATIBLE));
            IntentUtil.sendBroadcast(this.mContext, intent);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getAkaTokenReady(int phoneId) {
        return this.mAkaTokenReady[phoneId].get();
    }

    /* access modifiers changed from: package-private */
    public void setAkaTokenReady(int phoneId, boolean set) {
        this.mAkaTokenReady[phoneId].set(set);
    }

    /* access modifiers changed from: package-private */
    public void updateAkaToken(int phoneId, int response) {
        if (getAkaTokenReady(phoneId)) {
            setAkaTokenReady(phoneId, false);
            Intent intent = new Intent("com.samsung.nsds.action.AKA_TOKEN_RETRIEVED");
            if (response == 200) {
                intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, true);
            } else {
                intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
            }
            intent.setPackage("com.samsung.android.geargplugin");
            this.mContext.sendBroadcast(intent, "com.sec.imsservice.permission.RECEIVE_AKA_TOKEN");
        }
    }
}
