package com.sec.internal.ims.entitlement;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.entitlement.config.EntitlementConfigService;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;

public class EntitlementContentProvider extends ContentProvider {
    private static final String AKA_TOKEN = "aka_token";
    private static final String LOG_TAG = EntitlementContentProvider.class.getSimpleName();
    private static final String PROVIDER_NAME = "com.samsung.ims.entitlement.provider";
    private static final int RETRIEVE_AKA_TOKEN = 1;
    private static final int RETRIEVE_VOWIFI_ENTITLEMENT_REQUIRED = 2;
    private static final int RETRIEVE_VOWIFI_ENTITLEMENT_STATUS = 3;
    private static final String SLOT_ID = "slot";
    private static final String VOWIFI_ENTITLEMENT_REQUIRED = "vowifi_entitlement_required";
    private static final String VOWIFI_ENTITLEMENT_STATUS = "vowifi_entitlement_status";
    private static final UriMatcher sUriMatcher;
    private Context mContext = null;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(PROVIDER_NAME, "aka_token", 1);
        sUriMatcher.addURI(PROVIDER_NAME, VOWIFI_ENTITLEMENT_REQUIRED, 2);
        sUriMatcher.addURI(PROVIDER_NAME, VOWIFI_ENTITLEMENT_STATUS, 3);
    }

    public boolean onCreate() {
        this.mContext = getContext();
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int slotId = Integer.parseInt(uri.getQueryParameter("slot"));
        String str = LOG_TAG;
        IMSLog.s(str, slotId, "query uri:" + uri);
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            return getAkaToken(slotId);
        }
        if (match == 2) {
            return isVoWiFiEntitlementRequired(slotId);
        }
        if (match != 3) {
            return null;
        }
        return getVoWiFiEntitlementStatus(slotId);
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    private void activateSimDevice(int slotId) {
        IMSLog.i(LOG_TAG, slotId, "activateSimDevice()");
        String entitlementServer = NSDSConfigHelper.getConfigServer(slotId);
        if (!"Nsds".equalsIgnoreCase(entitlementServer) && !"Nsdsconfig".equalsIgnoreCase(entitlementServer)) {
            return;
        }
        if (SimUtil.getSimMno(slotId) == Mno.TMOUS) {
            IMSLog.i(LOG_TAG, slotId, "retrieve aka token for config");
            Intent intent = new Intent(this.mContext, EntitlementConfigService.class);
            intent.setAction(EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG);
            intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 19);
            intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, slotId);
            this.mContext.startService(intent);
            return;
        }
        IMSLog.i(LOG_TAG, slotId, "retrieve aka token for nsds");
        this.mContext.getContentResolver().update(Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "retrieve_aka_token"), new ContentValues(), (String) null, (String[]) null);
    }

    private Cursor getAkaToken(int slotId) {
        String aka_token;
        String entitlementServer = NSDSConfigHelper.getConfigServer(slotId);
        if (!supportEntitlementSlot(slotId)) {
            aka_token = NSDSNamespaces.AkaAuthResultType.AKA_NOT_SUPPORTED;
        } else if ("Nsds".equalsIgnoreCase(entitlementServer) || "Nsdsconfig".equalsIgnoreCase(entitlementServer)) {
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(slotId);
            aka_token = NSDSSharedPrefHelper.getAkaToken(this.mContext, sm == null ? "" : sm.getImsi());
            if (TextUtils.isEmpty(aka_token)) {
                aka_token = "InProgress";
                activateSimDevice(slotId);
            }
        } else if ("ts43".equalsIgnoreCase(entitlementServer)) {
            IAECModule aecModule = ImsRegistry.getAECModule();
            if (aecModule == null || !aecModule.isEntitlementRequired(slotId)) {
                aka_token = NSDSNamespaces.AkaAuthResultType.AKA_NOT_SUPPORTED;
            } else {
                aka_token = aecModule.getAkaToken(slotId);
            }
        } else {
            aka_token = NSDSNamespaces.AkaAuthResultType.AKA_NOT_SUPPORTED;
        }
        String str = LOG_TAG;
        IMSLog.s(str, slotId, "getAkaToken(): " + aka_token);
        MatrixCursor cursor = new MatrixCursor(new String[]{"aka_token"});
        cursor.addRow(new String[]{aka_token});
        return cursor;
    }

    private boolean supportEntitlementSlot(int slotId) {
        String entitlementServer = NSDSConfigHelper.getConfigServer(slotId);
        if (!TextUtils.isEmpty(entitlementServer)) {
            String str = LOG_TAG;
            IMSLog.i(str, slotId, "supportEntitlementSlot: " + entitlementServer);
            return true;
        }
        IMSLog.i(LOG_TAG, slotId, "supportEntitlementSlot : Not Support");
        return false;
    }

    private Cursor isVoWiFiEntitlementRequired(int slotId) {
        String entitlementServer = NSDSConfigHelper.getConfigServer(slotId);
        MatrixCursor cursor = new MatrixCursor(new String[]{VOWIFI_ENTITLEMENT_REQUIRED});
        boolean entitlementRequired = false;
        if (SimUtil.getSimMno(slotId).isEur() && ("ts43".equalsIgnoreCase(entitlementServer) || "Nsds".equalsIgnoreCase(entitlementServer))) {
            entitlementRequired = true;
        }
        String str = LOG_TAG;
        IMSLog.i(str, slotId, "isVoWiFiEntitlementRequired: " + entitlementRequired);
        cursor.addRow(new Object[]{Integer.valueOf(entitlementRequired)});
        return cursor;
    }

    private Cursor getVoWiFiEntitlementStatus(int slotId) {
        String entitlementServer = NSDSConfigHelper.getConfigServer(slotId);
        MatrixCursor cursor = new MatrixCursor(new String[]{VOWIFI_ENTITLEMENT_STATUS});
        boolean entitlementStatus = false;
        if ("ts43".equalsIgnoreCase(entitlementServer)) {
            IAECModule aecModule = ImsRegistry.getAECModule();
            if (aecModule != null) {
                entitlementStatus = aecModule.isEntitlementDisabled(slotId) ? true : aecModule.getVoWiFiEntitlementStatus(slotId);
            }
        } else if ("Nsds".equalsIgnoreCase(entitlementServer)) {
            entitlementStatus = NSDSSharedPrefHelper.getEntitlementCompleted(this.mContext, NSDSNamespaces.NSDSExtras.SERVICE_VOWIFI, DeviceIdHelper.getDeviceId(this.mContext, slotId));
        }
        String str = LOG_TAG;
        IMSLog.i(str, slotId, "getVoWiFiEntitlementStatus: " + entitlementStatus);
        cursor.addRow(new Object[]{Integer.valueOf(entitlementStatus)});
        return cursor;
    }
}
