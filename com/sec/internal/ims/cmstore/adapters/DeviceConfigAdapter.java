package com.sec.internal.ims.cmstore.adapters;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.cmstore.adapter.DeviceConfigAdapterConstants;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig.DeviceConfig;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig.parser.DeviceMstoreConfigParser;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class DeviceConfigAdapter {
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String APPLICATION = "APPLICATION";
    private static final String FAX = "FAX";
    private static final String MESSAGE_STORE = "MessageStore";
    /* access modifiers changed from: private */
    public static final String TAG = DeviceConfigAdapter.class.getSimpleName();
    private BroadcastReceiver mBootCompletedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DeviceConfigAdapter.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                Log.d(DeviceConfigAdapter.TAG, "ACTION_BOOT_COMPLETED received");
                DeviceConfigAdapter.this.parseDeviceConfig();
            }
        }
    };
    private ContentObserver mContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean selfChange, Uri uri) {
            String access$000 = DeviceConfigAdapter.TAG;
            Log.d(access$000, "Uri changed:" + uri);
            if (EntitlementConfigContract.DeviceConfig.CONTENT_URI.equals(uri)) {
                DeviceConfigAdapter.this.parseDeviceConfig();
            }
        }
    };
    private final Context mContext;
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private ContentResolver mResolver;
    public Map<String, String> mStoreDataMap = new HashMap();

    private void setParmsMStoreDataMap(DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic.Parm parm) {
        if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.SIT_URL) || parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.AKA_URL)) {
            String str = TAG;
            Log.d(str, parm.mName + ": " + IMSLog.checker(parm.mValue));
        } else {
            String str2 = TAG;
            Log.d(str2, parm.mName + ": " + parm.mValue);
        }
        if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.SIT_URL)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.SIT_URL, parm.mValue);
        } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.AKA_URL)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.AKA_URL, parm.mValue);
        } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.PUSH_SYNC_DELAY)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.PUSH_SYNC_DELAY, parm.mValue);
        } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.SERVER_ROOT)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.SERVER_ROOT, parm.mValue);
        } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.API_VERSION)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.API_VERSION, parm.mValue);
        } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.STORE_NAME)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.STORE_NAME, parm.mValue);
        } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.DISABLE_DIRECTION_HEADER)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.DISABLE_DIRECTION_HEADER, parm.mValue);
        } else if (parm.mName.equals("SyncTimer")) {
            this.mStoreDataMap.put("SyncTimer", parm.mValue);
        } else if (parm.mName.equals("DataConnectionSyncTimer")) {
            this.mStoreDataMap.put("DataConnectionSyncTimer", parm.mValue);
        } else if (parm.mName.equals("AuthProt")) {
            this.mStoreDataMap.put("AuthProt", parm.mValue);
        } else if (parm.mName.equals("UserName")) {
            this.mStoreDataMap.put("UserName", parm.mValue);
        } else if (parm.mName.equals("UserPwd")) {
            this.mStoreDataMap.put("UserPwd", parm.mValue);
        } else if (parm.mName.equals("EventRpting")) {
            this.mStoreDataMap.put("EventRpting", parm.mValue);
        } else if (parm.mName.equals("SMSStore")) {
            this.mStoreDataMap.put("SMSStore", parm.mValue);
        } else if (parm.mName.equals("MMSStore")) {
            this.mStoreDataMap.put("MMSStore", parm.mValue);
        } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.MAX_BULK_DELETE)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.MAX_BULK_DELETE, parm.mValue);
        } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.MAX_SEARCH)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.MAX_SEARCH, parm.mValue);
        }
    }

    private void setTmoFolderIdMStoreDataMap(DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic characteristic) {
        String str = TAG;
        Log.d(str, "setTmoFolderIdMStoreDataMap: " + characteristic.mParms);
        for (DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic.Parm parm : characteristic.mParms) {
            if (!TextUtils.isEmpty(parm.mName)) {
                if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.RCS_MESSAGE_STORE)) {
                    this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.RCS_MESSAGE_STORE, parm.mValue);
                } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.CALL_HISTORY)) {
                    this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.CALL_HISTORY, parm.mValue);
                } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.MEDIA_FAX)) {
                    this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.MEDIA_FAX, parm.mValue);
                } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_GREETINGS)) {
                    this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_GREETINGS, parm.mValue);
                } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_INBOX)) {
                    this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoFolderId.VM_INBOX, parm.mValue);
                }
            }
        }
    }

    private void setTmoSyncFromDaysMStoreDataMap(DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic characteristic) {
        String str = TAG;
        Log.d(str, "setTmoSyncFromDaysMStoreDataMap: " + characteristic.mParms);
        for (DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic.Parm parm : characteristic.mParms) {
            if (!TextUtils.isEmpty(parm.mName)) {
                if (parm.mName.equals("VVM")) {
                    this.mStoreDataMap.put("VVM", parm.mValue);
                } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.MESSAGES)) {
                    this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.MESSAGES, parm.mValue);
                } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.CALL_LOG)) {
                    this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.CALL_LOG, parm.mValue);
                } else if (parm.mName.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.VVM_GREETING)) {
                    this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.TmoSyncFromDays.VVM_GREETING, parm.mValue);
                } else if (parm.mName.equals("FAX")) {
                    this.mStoreDataMap.put("FAX", parm.mValue);
                }
            }
        }
    }

    public DeviceConfigAdapter(Context context, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        Log.d(TAG, "onCreate()");
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
    }

    public void registerBootCompletedReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BOOT_COMPLETED);
        context.registerReceiver(this.mBootCompletedReceiver, filter);
    }

    public void registerContentObserver(Context context) {
        Log.d(TAG, "registerContentObserver");
        context.getContentResolver().registerContentObserver(EntitlementConfigContract.DeviceConfig.CONTENT_URI, false, this.mContentObserver);
    }

    public void parseDeviceConfig() {
        String deviceConfigXml = getDeviceConfig();
        if (deviceConfigXml != null) {
            try {
                DeviceConfig deviceConfigration = DeviceMstoreConfigParser.parseDeviceConfig(deviceConfigXml);
                if (deviceConfigration == null) {
                    Log.d(TAG, "deviceConfiguration is null");
                    return;
                }
                if (deviceConfigration.mJanskyConfig != null && !TextUtils.isEmpty(deviceConfigration.mJanskyConfig.mWsgUri)) {
                    String wsgUri = deviceConfigration.mJanskyConfig.mWsgUri;
                    String str = TAG;
                    Log.d(str, "janskyConfig.WSG_URI: " + wsgUri);
                    this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoMstoreServerValues.WSG_URI, wsgUri);
                }
                if (deviceConfigration.mFaxConfig != null) {
                    setFaxDataMap(deviceConfigration.mFaxConfig);
                }
                if (deviceConfigration.mRCSConfig != null && deviceConfigration.mRCSConfig.mWapProvisioningDoc != null) {
                    if (deviceConfigration.mRCSConfig.mWapProvisioningDoc.mCharacteristics != null) {
                        for (DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic characteristic : deviceConfigration.mRCSConfig.mWapProvisioningDoc.mCharacteristics) {
                            if (characteristic.mType.equals(APPLICATION)) {
                                for (DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic chrstic : characteristic.mCharacteristics) {
                                    if (chrstic.mType.equals(MESSAGE_STORE)) {
                                        setMstoreDataMap(chrstic);
                                    }
                                }
                            }
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "parseDeviceConfig: malformed device config xml");
            }
        } else {
            Log.d(TAG, "!!!!Device Config XML is NULL!!!!");
        }
        this.mICloudMessageManagerHelper.setDeviceConfigUsed(this.mStoreDataMap);
    }

    private void setFaxDataMap(DeviceConfig.FaxConfig faxConfig) {
        String rootUrl = faxConfig.mRootUrl;
        if (!TextUtils.isEmpty(rootUrl)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoFax.ROOT_URL, rootUrl);
        }
        String apiVersion = faxConfig.mApiVersion;
        if (!TextUtils.isEmpty(apiVersion)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.FAX_API_VERSION, apiVersion);
        }
        String serviceName = faxConfig.mServiceName;
        if (!TextUtils.isEmpty(serviceName)) {
            this.mStoreDataMap.put(DeviceConfigAdapterConstants.TmoFax.SERVICE_NAME, serviceName);
        }
        String str = TAG;
        Log.d(str, "getFaxDataMap TmoFax.ROOT_URL: " + IMSLog.checker(rootUrl) + "TmoFax.API_VERSION: " + apiVersion + " TmoFax.SERVICE_NAME: " + serviceName);
    }

    public String getDeviceConfig() {
        Cursor cursor = this.mResolver.query(EntitlementConfigContract.DeviceConfig.CONTENT_URI, new String[]{"device_config"}, (String) null, (String[]) null, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String string = cursor.getString(0);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return string;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor == null) {
            return null;
        }
        cursor.close();
        return null;
        throw th;
    }

    public Map<String, String> setMstoreDataMap(DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic mStoreCharacteristic) {
        String str = TAG;
        Log.d(str, "setMstoreDataMap:" + mStoreCharacteristic.mParms);
        if (mStoreCharacteristic.mParms != null && mStoreCharacteristic.mParms.size() > 0) {
            for (DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic.Parm parm : mStoreCharacteristic.mParms) {
                if (!(parm == null || parm.mName == null)) {
                    setParmsMStoreDataMap(parm);
                }
            }
        }
        if (mStoreCharacteristic.mCharacteristics != null && mStoreCharacteristic.mCharacteristics.size() > 0) {
            for (DeviceConfig.RCSConfig.WapProvisioningDoc.Characteristic characteristic : mStoreCharacteristic.mCharacteristics) {
                if (!TextUtils.isEmpty(characteristic.mType)) {
                    if (characteristic.mType.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.FOLDER_ID)) {
                        setTmoFolderIdMStoreDataMap(characteristic);
                    } else if (characteristic.mType.equals(DeviceConfigAdapterConstants.TmoMstoreServerValues.SYNC_FROM_DAYS)) {
                        setTmoSyncFromDaysMStoreDataMap(characteristic);
                    }
                }
            }
        }
        return this.mStoreDataMap;
    }
}
