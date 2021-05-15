package com.sec.internal.ims.entitlement.config.app.nsdsconfig;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.entitlement.config.EntitlementConfigModuleBase;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.MnoNsdsConfigStrategyCreator;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.EntitlementConfigDBHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.IntentScheduler;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.entitlement.config.IEntitlementConfig;
import com.sec.internal.interfaces.ims.entitlement.config.IMnoNsdsConfigStrategy;

public class NSDSConfigModule extends EntitlementConfigModuleBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = NSDSConfigModule.class.getSimpleName();
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            String access$000 = NSDSConfigModule.LOG_TAG;
            Log.i(access$000, "Uri changed:" + uri);
            if (EntitlementConfigContract.DeviceConfig.CONTENT_URI.equals(uri)) {
                IMnoNsdsConfigStrategy mnoNsdsStrategy = NSDSConfigModule.this.getMnoNsdsStrategy();
                if (mnoNsdsStrategy != null) {
                    mnoNsdsStrategy.scheduleRefreshDeviceConfig(0);
                }
            } else if (Settings.Global.getUriFor(Extensions.Settings.Global.DEVICE_PROVISIONED).equals(uri)) {
                Log.i(NSDSConfigModule.LOG_TAG, "OOBE setup complete: trigger boot up process");
                NSDSConfigModule.this.onDeviceReady();
            }
        }
    };
    private Context mContext;
    private IEntitlementConfig mEntitlementConfigImpl;
    private ISimManager mSimManager;

    public NSDSConfigModule(Looper looper, Context context, ISimManager simManager) {
        super(looper);
        this.mContext = context;
        this.mSimManager = simManager;
        this.mEntitlementConfigImpl = new NSDSDeviceConfigImpl(getLooper(), this.mContext, this.mSimManager);
        registerContentObserver();
        init();
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(EntitlementConfigContract.DeviceConfig.CONTENT_URI, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(Extensions.Settings.Global.DEVICE_PROVISIONED), false, this.mContentObserver);
    }

    public void onSimReady(boolean isSwapped) {
        String str = LOG_TAG;
        Log.i(str, "onSimReady: isSwapped " + isSwapped);
        if (isSwapped) {
            String prevImsi = NSDSSharedPrefHelper.getPrefForSlot(this.mContext, this.mSimManager.getSimSlotIndex(), NSDSNamespaces.NSDSSharedPref.PREF_PREV_IMSI);
            if (!TextUtils.isEmpty(prevImsi)) {
                EntitlementConfigDBHelper.deleteConfig(this.mContext, prevImsi);
            }
        }
    }

    public void onDeviceReady() {
        start();
        IMnoNsdsConfigStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null || mnoNsdsStrategy.isDeviceProvisioned()) {
            int slotIndex = this.mSimManager.getSimSlotIndex();
            String deviceId = DeviceIdHelper.getDeviceId(this.mContext, slotIndex);
            boolean isSwapped = NSDSSharedPrefHelper.isSimSwapPending(this.mContext, deviceId);
            if (NSDSSharedPrefHelper.get(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE) != null) {
                Log.i(LOG_TAG, "onDeviceReady... reset deviceconfig_state");
                NSDSSharedPrefHelper.remove(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE);
            }
            if (otherSimInProgress(slotIndex)) {
                Log.i(LOG_TAG, "Waiting for other SIM operation until 5sec");
                Bundle extras = new Bundle();
                extras.putInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, slotIndex);
                IntentScheduler.scheduleTimer(this.mContext, slotIndex, EntitlementNamespaces.EntitlementActions.ACTION_RETRY_DEVICE_CONFIG, extras, 5000);
                return;
            }
            NSDSSharedPrefHelper.save(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE, NSDSNamespaces.NSDSDeviceState.DEVICECONFIG_IN_PROGRESS);
            if (isSwapped) {
                NSDSSharedPrefHelper.clearSimSwapPending(this.mContext, deviceId);
            }
            this.mEntitlementConfigImpl.getDeviceConfig(this.mSimManager.getImsi(), 0);
            return;
        }
        Log.i(LOG_TAG, "Waiting for OOBE setup complete...");
    }

    public void forceConfigUpdate() {
        this.mEntitlementConfigImpl.getDeviceConfig(this.mSimManager.getImsi(), 18);
    }

    public void retriveAkaToken() {
        String deviceId = DeviceIdHelper.getDeviceId(this.mContext, this.mSimManager.getSimSlotIndex());
        if (NSDSSharedPrefHelper.isSimSwapPending(this.mContext, deviceId)) {
            NSDSSharedPrefHelper.clearSimSwapPending(this.mContext, deviceId);
        }
        this.mEntitlementConfigImpl.getDeviceConfig(this.mSimManager.getImsi(), 19);
    }

    /* access modifiers changed from: private */
    public IMnoNsdsConfigStrategy getMnoNsdsStrategy() {
        return MnoNsdsConfigStrategyCreator.getMnoStrategy(this.mSimManager.getSimSlotIndex());
    }

    private boolean otherSimInProgress(int simSlot) {
        String devicestate;
        Log.i(LOG_TAG, "Check otherSimInProgress");
        for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
            int simindex = sm.getSimSlotIndex();
            if (simindex != simSlot && (devicestate = NSDSSharedPrefHelper.get(this.mContext, DeviceIdHelper.getDeviceId(this.mContext, simindex), NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE)) != null && NSDSNamespaces.NSDSDeviceState.DEVICECONFIG_IN_PROGRESS.equals(devicestate)) {
                Log.i(LOG_TAG, "otherSimInProgress... pending device config");
                return true;
            }
        }
        return false;
    }
}
