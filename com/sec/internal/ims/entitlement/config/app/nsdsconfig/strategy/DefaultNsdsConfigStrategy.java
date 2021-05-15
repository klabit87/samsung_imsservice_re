package com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.provider.Settings;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.operation.DefaultNsdsOperation;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.operation.TmoNsdsOperation;
import com.sec.internal.ims.entitlement.storagehelper.EntitlementConfigDBHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.IntentScheduler;
import com.sec.internal.interfaces.ims.entitlement.config.IMnoNsdsConfigStrategy;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class DefaultNsdsConfigStrategy implements IMnoNsdsConfigStrategy {
    private static final long DEFAULT_REFRESH_TIME_IN_SECS = 86400;
    private static final String LOG_TAG = DefaultNsdsConfigStrategy.class.getSimpleName();
    protected Context mContext;
    protected NsdsConfigStrategyType mStrategyType = NsdsConfigStrategyType.DEFAULT;
    protected final Map<String, Integer> sMapEntitlementServices = new HashMap();

    public DefaultNsdsConfigStrategy(Context ctx) {
        this.mContext = ctx;
        this.sMapEntitlementServices.put("vowifi", 1);
    }

    public final boolean isDeviceProvisioned() {
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            z = true;
        }
        boolean isProvisioned = z;
        String str = LOG_TAG;
        IMSLog.i(str, "isDeviceProvisioned: " + isProvisioned);
        return isProvisioned;
    }

    public final String getEntitlementServerUrl(String deviceUid) {
        if (!this.mStrategyType.isOneOf(NsdsConfigStrategyType.TMOUS)) {
            return NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, deviceUid, "http://ses.ericsson-magic.net:10080/generic_devices");
        }
        String url = NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, deviceUid, (String) null);
        String str = LOG_TAG;
        IMSLog.i(str, "getEntitlementServerUrl: url in sp " + url);
        if (url == null) {
            return EntitlementConfigDBHelper.getNsdsUrlFromDeviceConfig(this.mContext, "https://eas3.msg.t-mobile.com/generic_devices");
        }
        return url;
    }

    public final int getNextOperation(int deviceEventType, int prevNsdsBaseOperation) {
        if (this.mStrategyType.isOneOf(NsdsConfigStrategyType.TMOUS)) {
            return TmoNsdsOperation.getOperation(deviceEventType, prevNsdsBaseOperation);
        }
        return DefaultNsdsOperation.getOperation(deviceEventType, prevNsdsBaseOperation);
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    public final void scheduleRefreshDeviceConfig(int slotid) {
        Cursor cursor;
        long refreshTime = DEFAULT_REFRESH_TIME_IN_SECS;
        if (this.mStrategyType.isOneOf(NsdsConfigStrategyType.TMOUS)) {
            try {
                cursor = this.mContext.getContentResolver().query(EntitlementConfigContract.DeviceConfig.buildXPathExprUri("//configInfo/configRefreshTime"), (String[]) null, (String) null, (String[]) null, (String) null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        long time = cursor.getLong(1);
                        if (time > 0) {
                            refreshTime = time;
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLException sqe) {
                String str = LOG_TAG;
                IMSLog.s(str, "Ignore sqlexception:" + sqe.getMessage());
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        String xpathExpr = LOG_TAG;
        IMSLog.i(xpathExpr, "scheduleRefreshDeviceConfig: " + refreshTime);
        if (refreshTime > 0) {
            IntentScheduler.scheduleTimer(this.mContext, slotid, EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG, 1000 * refreshTime);
            return;
        }
        return;
        throw th;
    }

    protected enum NsdsConfigStrategyType {
        DEFAULT,
        TMOUS,
        END_OF_NSDSCONFIGSTRATEGY;

        /* access modifiers changed from: protected */
        public boolean isOneOf(NsdsConfigStrategyType... types) {
            for (NsdsConfigStrategyType type : types) {
                if (this == type) {
                    return true;
                }
            }
            return false;
        }
    }
}
