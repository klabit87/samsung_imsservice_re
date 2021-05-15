package com.sec.internal.ims.config;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.config.adapters.StorageAdapter;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigProvider extends ContentProvider {
    public static final String CONFIG_DB_NAME_PREFIX = "config_";
    private static final String LOG_TAG = "ConfigProvider";
    private static final int MAX_SERVER_COUNT = ConfigConstants.APPID_MAP.size();
    private static final int N_PARAMETER = 1;
    private static final IntentFilter SIM_STATE_CHANGED_INTENT_FILTER;
    private static final Map<String, List<String>> mAppIdMap = new TreeMap();
    private final Map<Integer, Map<String, Integer>> mAppIdServerIdMap = new ConcurrentHashMap();
    private Map<String, IReadConfigParam> mConfigTableMap = new ConcurrentHashMap();
    private final IStorageAdapter mEmptyStorage = new StorageAdapter();
    private UriMatcher mMatcher;
    private final Map<Integer, Map<Integer, IStorageAdapter>> mServerIdStorageMap = new HashMap();

    private interface IReadConfigParam {
        Map<String, String> readParam(String str, int i);
    }

    static {
        IntentFilter intentFilter = new IntentFilter();
        SIM_STATE_CHANGED_INTENT_FILTER = intentFilter;
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        for (Map.Entry<String, String> entry : ConfigConstants.APPID_MAP.entrySet()) {
            List<String> list = mAppIdMap.get(entry.getValue());
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(entry.getKey());
            mAppIdMap.put(entry.getValue(), list);
        }
    }

    public boolean onCreate() {
        Log.i(LOG_TAG, "ConfigProvider was created");
        initConfigTable();
        UriMatcher uriMatcher = new UriMatcher(0);
        this.mMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.rcs.autoconfigurationprovider", "parameter/*", 1);
        getContext().registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                    String iccState = intent.getStringExtra("ss");
                    int phoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY, 0);
                    if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(iccState)) {
                        Log.i("ConfigProvider[" + phoneId + "]", "SIM LOADED");
                        IStorageAdapter unused = ConfigProvider.this.initStorage(context, phoneId, (List<String>) null);
                    }
                }
            }
        }, SIM_STATE_CHANGED_INTENT_FILTER);
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Map<String, String> readData;
        String path = uri.toString();
        Log.i(LOG_TAG, "query uri:" + IMSLog.checker(path));
        if (path.matches("^content://com.samsung.rcs.autoconfigurationprovider/[\\.\\w-_/*#]*")) {
            IStorageAdapter storage = getStorageByUri(uri);
            if (path.contains("root/*") || path.contains("root/application/*") || path.contains("content://com.samsung.rcs.autoconfigurationprovider/*")) {
                readData = queryMultipleStorage(uri);
            } else {
                readData = queryStorage(uri, storage);
            }
            if (readData == null) {
                Log.i(LOG_TAG, "can not find readData from mStorage");
                return null;
            }
            String[] columnNames = new String[readData.keySet().size()];
            String[] columnValues = new String[readData.keySet().size()];
            int index = 0;
            for (Map.Entry<String, String> entry : readData.entrySet()) {
                columnNames[index] = entry.getKey();
                columnValues[index] = entry.getValue();
                index++;
            }
            MatrixCursor cursor = new MatrixCursor(columnNames);
            cursor.addRow(columnValues);
            return cursor;
        }
        throw new IllegalArgumentException(path + " is not a correct AutoConfigurationProvider Uri");
    }

    private Map<String, String> queryStorage(Uri uri, IStorageAdapter storage) {
        String path = uri.toString();
        Log.i(LOG_TAG, "queryStorage path " + path);
        if (storage.getState() != 1) {
            Log.i(LOG_TAG, "provider is not ready, return empty!");
            return null;
        } else if (this.mMatcher.match(uri) != 1) {
            return storage.readAll(path.replaceFirst(ConfigConstants.CONFIG_URI, "").replaceAll("#simslot\\d", ""));
        } else {
            return readDataByParam(path.replaceFirst("content://com.samsung.rcs.autoconfigurationprovider/parameter/", "").replaceAll("#simslot\\d", ""), UriUtil.getSimSlotFromUri(uri));
        }
    }

    private Map<String, String> queryMultipleStorage(Uri uri) {
        int phoneId = UriUtil.getSimSlotFromUri(uri);
        Map<Integer, IStorageAdapter> serverIdStorageMap = this.mServerIdStorageMap.get(Integer.valueOf(phoneId));
        String path = uri.toString();
        Map<String, String> readData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Log.i(LOG_TAG, "queryMultipleStorage path " + path + " map " + serverIdStorageMap);
        if (serverIdStorageMap == null) {
            return null;
        }
        String applicationCharacteristicPath = "root/application/*";
        if (path.contains(applicationCharacteristicPath) || path.contains("content://com.samsung.rcs.autoconfigurationprovider/*")) {
            for (IStorageAdapter storage : serverIdStorageMap.values()) {
                readData.putAll(queryStorage(uri, storage));
            }
        } else {
            readData.putAll(queryStorage(uri, serverIdStorageMap.get(0)));
            if (path.contains("root")) {
                applicationCharacteristicPath = "application/*";
            }
            Uri uri2 = Uri.parse(ImsUtil.getPathWithPhoneId(path.replaceAll("\\*#simslot\\d", "") + applicationCharacteristicPath, phoneId));
            for (int i = 1; i < serverIdStorageMap.keySet().size(); i++) {
                if (serverIdStorageMap.get(Integer.valueOf(i)) != null) {
                    readData.putAll(queryStorage(uri2, serverIdStorageMap.get(Integer.valueOf(i))));
                }
            }
        }
        return readData;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String path = uri.toString();
        Log.i(LOG_TAG, "delete uri:" + IMSLog.checker(path));
        if (path.matches("^content://com.samsung.rcs.autoconfigurationprovider/[\\.\\w-_/#]*")) {
            IStorageAdapter storage = getStorageByUri(uri);
            if (storage.getState() != 1) {
                Log.i(LOG_TAG, "provider is not ready, return empty!");
                return 0;
            }
            String path_delete = path.replaceFirst(ConfigConstants.CONFIG_URI, "").replaceAll("#simslot\\d", "");
            int count = storage.delete(path_delete);
            getContext().getContentResolver().notifyChange(Uri.parse(path_delete), (ContentObserver) null);
            return count;
        }
        throw new IllegalArgumentException(path + " is not a correct AutoConfigurationProvider Uri");
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("not supported");
    }

    public Uri insert(Uri uri, ContentValues values) {
        String path = uri.toString();
        Log.i(LOG_TAG, "insert uri:" + uri);
        Log.i(LOG_TAG, "insert uri:" + IMSLog.checker(path));
        if (path.matches("^content://com.samsung.rcs.autoconfigurationprovider/[\\.\\w-_/#]*")) {
            IStorageAdapter storage = getStorageByUri(uri);
            if (storage.getState() != 1) {
                Log.i(LOG_TAG, "provider is not ready, return empty!");
                return null;
            }
            Map<String, String> data = new HashMap<>();
            for (Map.Entry<String, Object> value : values.valueSet()) {
                if (value.getValue() instanceof String) {
                    data.put(path.replaceFirst(ConfigConstants.CONFIG_URI, "").replaceAll("#simslot\\d", "") + value.getKey(), (String) value.getValue());
                }
            }
            storage.writeAll(data);
            getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
            return uri;
        }
        throw new IllegalArgumentException(path + " is not a correct AutoConfigurationProvider Uri");
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        insert(uri, values);
        return values.size();
    }

    /* access modifiers changed from: private */
    public IStorageAdapter initStorage(Context context, int phoneId, List<String> appIdList) {
        IStorageAdapter storage;
        boolean found = true;
        String identity = getIdentityByPhoneId(context, phoneId);
        if (TextUtils.isEmpty(identity)) {
            Log.i(LOG_TAG, "initStorage: phone:" + phoneId + " no identity");
            return this.mEmptyStorage;
        }
        String hashId = CONFIG_DB_NAME_PREFIX + HashManager.generateMD5(identity);
        synchronized (this.mServerIdStorageMap) {
            if (this.mServerIdStorageMap.get(Integer.valueOf(phoneId)) == null) {
                this.mServerIdStorageMap.put(Integer.valueOf(phoneId), new ConcurrentHashMap());
            }
            IConfigModule cm = ImsRegistry.getConfigModule();
            IStorageAdapter storage2 = null;
            if (!(cm == null || (storage2 = cm.getStorage(phoneId)) == null)) {
                Log.i(LOG_TAG, "initStorage: phone:" + phoneId + " get storage from configmodule");
            }
            if (storage2 == null) {
                storage2 = (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(phoneId)).get(0);
            }
            if (storage2 == null) {
                Log.i(LOG_TAG, "initStorage: phone:" + phoneId + " no storage :" + IMSLog.checker(identity));
                found = false;
            } else if (!hashId.equals(storage2.getIdentity())) {
                Log.i(LOG_TAG, "initStorage: phone:" + phoneId + " different identity :" + IMSLog.checker(identity));
                storage2.close();
                this.mServerIdStorageMap.remove(Integer.valueOf(phoneId));
                this.mServerIdStorageMap.put(Integer.valueOf(phoneId), new ConcurrentHashMap());
                this.mAppIdServerIdMap.remove(Integer.valueOf(phoneId));
                found = false;
            }
            if (!found) {
                IStorageAdapter storage3 = new StorageAdapter();
                this.mServerIdStorageMap.get(Integer.valueOf(phoneId)).put(0, storage3);
                storage = storage3;
            } else {
                storage = storage2;
            }
            if (storage.getState() != 1) {
                Log.i(LOG_TAG, "initStorage: phone:" + phoneId + " open storage :" + IMSLog.checker(identity));
                storage.open(getContext(), hashId, phoneId);
            }
            if (storage.read("root/access-control/server/0/app-id/0") != null) {
                storage = initAdditionalStorage(context, phoneId, appIdList, hashId, storage);
            }
        }
        return storage;
    }

    private IStorageAdapter initAdditionalStorage(Context context, int phoneId, List<String> appIdList, String hashId, IStorageAdapter storage) {
        Log.i(LOG_TAG, "initAdditionalStorage: phoneId: " + phoneId);
        if (this.mAppIdServerIdMap.get(Integer.valueOf(phoneId)) == null) {
            this.mAppIdServerIdMap.put(Integer.valueOf(phoneId), new ConcurrentHashMap());
            for (int i = 0; i < MAX_SERVER_COUNT; i++) {
                String key = storage.read("root/access-control/default/app-id/" + i);
                if (key == null) {
                    break;
                }
                this.mAppIdServerIdMap.get(Integer.valueOf(phoneId)).put(key, 0);
            }
            for (int i2 = 0; i2 < MAX_SERVER_COUNT; i2++) {
                for (int j = 0; j < MAX_SERVER_COUNT; j++) {
                    String key2 = storage.read("root/access-control/server/" + i2 + "/app-id/" + j);
                    if (key2 == null) {
                        break;
                    }
                    this.mAppIdServerIdMap.get(Integer.valueOf(phoneId)).put(key2, Integer.valueOf(i2 + 1));
                }
                if (!this.mAppIdServerIdMap.get(Integer.valueOf(phoneId)).containsValue(Integer.valueOf(i2 + 1))) {
                    break;
                }
                IStorageAdapter additionalStorage = new StorageAdapter();
                additionalStorage.open(context, hashId + "_" + (i2 + 1), phoneId);
                this.mServerIdStorageMap.get(Integer.valueOf(phoneId)).put(Integer.valueOf(i2 + 1), additionalStorage);
            }
            Log.i(LOG_TAG, "mAppIdServerIdMap " + this.mAppIdServerIdMap);
            Log.i(LOG_TAG, "mServerIdStorageMap " + this.mServerIdStorageMap);
        }
        if (appIdList == null) {
            return (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(phoneId)).get(0);
        }
        for (String appId : appIdList) {
            Map map = this.mAppIdServerIdMap.get(Integer.valueOf(phoneId));
            IStorageAdapter additionalStorage2 = (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(phoneId)).get((Integer) map.get(appId));
            if (additionalStorage2 != null) {
                return additionalStorage2;
            }
        }
        return (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(phoneId)).get(0);
    }

    private void setConfigTable(String param, IReadConfigParam readConfigParam) {
        this.mConfigTableMap.put(param.toLowerCase(Locale.US), readConfigParam);
    }

    private void initConfigTable() {
        ReadRootParm readRootParm = new ReadRootParm();
        setConfigTable("version", readRootParm);
        setConfigTable("validity", readRootParm);
        setConfigTable("token", readRootParm);
        IReadConfigParam rootAppParm = new ReadRootAppParm();
        setConfigTable(ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME, rootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY, rootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY, rootAppParm);
        setConfigTable("address", rootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE, rootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.KEEP_ALIVE_ENABLED, rootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.TIMER_T1, rootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.TIMER_T2, rootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.TIMER_T4, rootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.REG_RETRY_BASE_TIME, rootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.REG_RETRY_MAX_TIME, rootAppParm);
        IReadConfigParam extParm = new ReadExtParm();
        setConfigTable(ConfigConstants.ConfigTable.EXT_MAX_SIZE_IMAGE_SHARE, extParm);
        setConfigTable("maxtimevideoshare", extParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_Q_VALUE, extParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_INT_URL_FORMAT, extParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_NAT_URL_FORMAT, extParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_RCS_VOLTE_SINGLE_REGISTRATION, extParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_END_USER_CONF_REQID, extParm);
        setConfigTable("uuid_Value", extParm);
        IReadConfigParam appAuthParm = new ReadAppAuthParm();
        setConfigTable("UserName", appAuthParm);
        setConfigTable("UserPwd", appAuthParm);
        setConfigTable("realm", appAuthParm);
        IReadConfigParam serviceParm = new ReadServiceParm();
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SUPPORTED_RCS_VERSIONS, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SUPPORTED_RCS_PROFILE_VERSIONS, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_STATE, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_DISABLED_STATE, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_PRESENCE_PRFL, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_CHAT_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_GROUP_CHAT_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_FT_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SLM_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_GEOPULL_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_VS_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_IS_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_IPVOICECALL_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_IPVIDEOCALL_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_IR94_VIDEO_AUTH, serviceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_ALLOW_RCS_EXTENSIONS, serviceParm);
        IReadConfigParam dataOffParm = new ReadDataOffParm();
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_RCS_MESSAGING, dataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_FILE_TRANSFER, dataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_SMSOIP, dataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_MMS, dataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_VOLTE, dataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_IP_VIDEO, dataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_PROVISIONING, dataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_SYNC, dataOffParm);
        IReadConfigParam capdiscoveryParm = new ReadCapDiscoveryParm();
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_DISABLE_INITIAL_SCAN, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_PERIOD, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE_PERIOD, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_CAPINFO_EXPIRY, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_NON_RCS_CAPINFO_EXPIRY, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_CAP_DISC_COMMON_STACK, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_MAX_ENTRIES_IN_LIST, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_ALLOWED_PREFIXES, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY, capdiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE, capdiscoveryParm);
        IReadConfigParam presenceParm = new ReadPresenceParm();
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_PUBLISH_TIMER, presenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_THROTTLE_PUBLISH, presenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_MAX_SUBSCRIPTION_LIST, presenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_RLS_URI, presenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_LOC_INFO_MAX_VALID_TIME, presenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_CLIENT_OBJ_DATALIMIT, presenceParm);
        IReadConfigParam imftParm = new ReadImFtParm();
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_MSG_TECH, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_CAP_ALWAYS_ON, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_WARN_SF, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_GROUP_CHAT_FULL_STAND_FWD, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_GROUP_CHAT_ONLY_F_STAND_FWD, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_SMS_FALLBACK_AUTH, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_CAP_NON_RCS, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_WARN_IW, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_AUT_ACCEPT, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_SESSION_START, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_AUT_ACCEPT_GROUP_CHAT, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FIRST_MSG_INVITE, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_TIMER_IDLE, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_CONCURRENT_SESSION, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MULTIMEDIA_CHAT, imftParm);
        setConfigTable("MaxSize", imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_1, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_M, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_WARN_SIZE, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR_INCOMING, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_THUMB, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_ST_AND_FW_ENABLED, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_CAP_ALWAYS_ON, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_DL_URI, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_DEFAULT_MECH, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_FALLBACK, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_PRES_SRV_CAP, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_DEFERRED_MSG_FUNC_URI, imftParm);
        setConfigTable("max_adhoc_group_size", imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_CONF_FCTY_URI, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXPLODER_URI, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MASS_FCTY_URI, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_CHAT_REVOKE_TIMER, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_FT_WARN_SIZE, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_MAX_SIZE_FILE_TR, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_MAX_SIZE_FILE_TR_INCOMING, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_ADHOC_OPEN_GROUP_SIZE, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXT_CB_FT_HTTP_CS_URI, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXT_MAX_SIZE_EXTRA_FILE_TR, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXT_FT_HTTP_EXTRA_CS_URI, imftParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXT_MAX_IMDN_AGGREGATION, imftParm);
        IReadConfigParam enrichedCallingParm = new ReadEnrichedCallingParm();
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, enrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH, enrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH, enrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH, enrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_CONTENT_SHARE, enrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_PRE_AND_POST_CALL, enrichedCallingParm);
        ReadRootParm readRootParm2 = readRootParm;
        ReadStandalonMsgParm readStandalonMsgParm = new ReadStandalonMsgParm();
        setConfigTable(ConfigConstants.ConfigTable.CPM_SLM_MAX_MSG_SIZE, readStandalonMsgParm);
        setConfigTable("MaxSize", readStandalonMsgParm);
        setConfigTable(ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE, readStandalonMsgParm);
        ReadCpmMessageStoreParm readCpmMessageStoreParm = new ReadCpmMessageStoreParm();
        setConfigTable(ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_URL, readCpmMessageStoreParm);
        setConfigTable("AuthProt", readCpmMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_NAME, readCpmMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_PWD, readCpmMessageStoreParm);
        setConfigTable("EventRpting", readCpmMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_AUTH_ARCHIVE, readCpmMessageStoreParm);
        setConfigTable("SyncTimer", readCpmMessageStoreParm);
        setConfigTable("DataConnectionSyncTimer", readCpmMessageStoreParm);
        setConfigTable("SMSStore", readCpmMessageStoreParm);
        setConfigTable("MMSStore", readCpmMessageStoreParm);
        ReadStandalonMsgParm readStandalonMsgParm2 = readStandalonMsgParm;
        ReadOtherParm readOtherParm = new ReadOtherParm();
        setConfigTable(ConfigConstants.ConfigTable.OTHER_WARN_SIZE_IMAGE_SHARE, readOtherParm);
        setConfigTable("maxtimevideoshare", readOtherParm);
        setConfigTable(ConfigConstants.ConfigTable.OTHER_EXTENSIONS_MAX_MSRP_SIZE, readOtherParm);
        setConfigTable(ConfigConstants.ConfigTable.OTHER_CALL_COMPOSER_TIMER_IDLE, readOtherParm);
        setConfigTable(ConfigConstants.ConfigTable.XDMS_XCAP_ROOT_URI, new ReadXdmsParm());
        ReadOtherParm readOtherParm2 = readOtherParm;
        ReadTransportProtoParm readTransportProtoParm = new ReadTransportProtoParm();
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_MEDIA, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_MEDIA, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_SIGNALLING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_RT_MEDIA, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING_ROAMING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_MEDIA_ROAMING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_RT_MEDIA_ROAMING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_RT_MEDIA, readTransportProtoParm);
        ReadTransportProtoParm readTransportProtoParm2 = readTransportProtoParm;
        IReadConfigParam publicAccountParm = new ReadPublicAccountParm();
        setConfigTable(ConfigConstants.ConfigTable.PUBLIC_ACCOUNT_ADDR, publicAccountParm);
        setConfigTable(ConfigConstants.ConfigTable.PUBLIC_ACCOUNT_ADDRTYPE, publicAccountParm);
        ReadCpmMessageStoreParm readCpmMessageStoreParm2 = readCpmMessageStoreParm;
        ReadPersonalProfileParm readPersonalProfileParm = new ReadPersonalProfileParm();
        setConfigTable(ConfigConstants.ConfigTable.PERSONAL_PROFILE_ADDR, readPersonalProfileParm);
        setConfigTable(ConfigConstants.ConfigTable.PERSONAL_PROFILE_ADDRTYPE, readPersonalProfileParm);
        ReadPersonalProfileParm readPersonalProfileParm2 = readPersonalProfileParm;
        ReadUxParm readUxParm = new ReadUxParm();
        setConfigTable(ConfigConstants.ConfigTable.UX_MESSAGING_UX, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_USER_ALIAS_AUTH, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_SPAM_NOTIFICATION_TEXT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_TOKEN_LINK_NOTIFICATION_TEXT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_UNAVAILABLE_ENDPOINT_TEXT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_VIDEO_AND_ENCALL_UX, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_IR51_SWITCH_UX, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_MSG_FB_DEFAULT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_FT_FB_DEFAULT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_CALL_LOG_BEARER_DIFFER, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_ALLOW_ENRICHED_CHATBOT_SEARCH_DEFAULT, readUxParm);
        ReadUxParm readUxParm2 = readUxParm;
        ReadClientControlParm readClientControlParm = new ReadClientControlParm();
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_RECONNECT_GUARD_TIMER, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_CFS_TRIGGER, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_FT_HTTP_CAP_ALWAYS_ON, readClientControlParm);
        ReadClientControlParm readClientControlParm2 = readClientControlParm;
        ReadMsisdnParm readMsisdnParm = new ReadMsisdnParm();
        setConfigTable(ConfigConstants.ConfigTable.MSISDN_SKIP_COUNT, readMsisdnParm);
        setConfigTable(ConfigConstants.ConfigTable.MSISDN_MSGUI_DISPLAY, readMsisdnParm);
        ReadMsisdnParm readMsisdnParm2 = readMsisdnParm;
        ReadChatbotParm readChatbotParm = new ReadChatbotParm();
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_CHATBOTDIRECTORY, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_BOTINFOFQDNROOT, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_CHATBOTBLACKLIST, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_MSGHISTORYSELECTABLE, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_SPECIFIC_CHATBOTS_LIST, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_IDENTITY_IN_ENRICHED_SEARCH, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_PRIVACY_DISABLE, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_CHATBOT_MSG_TECH, readChatbotParm);
        ReadChatbotParm readChatbotParm2 = readChatbotParm;
        ReadMessageStoreParm readMessageStoreParm = new ReadMessageStoreParm();
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_URL, readMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_NOTIF_URL, readMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_AUTH, readMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_USER_NAME, readMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_USER_PWD, readMessageStoreParm);
        ReadMessageStoreParm readMessageStoreParm2 = readMessageStoreParm;
        ReadPluginsParm readPluginsParm = new ReadPluginsParm();
        setConfigTable(ConfigConstants.ConfigTable.PLUGINS_CATALOGURI, readPluginsParm);
        ReadPluginsParm readPluginsParm2 = readPluginsParm;
        IReadConfigParam serviceExtParm = new ReadServiceExtParm();
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_STATE, serviceExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_DISABLED_STATE, serviceExtParm);
        IReadConfigParam serviceProviderExtParm = new ReadServiceProviderExtParm();
        setConfigTable(ConfigConstants.ConfigTable.SPG_URL, serviceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SPG_PARAMS_URL, serviceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.NMS_URL, serviceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.NC_URL, serviceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_FTHTTPGROUPCHAT, serviceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_USER_NAME, serviceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_PASSWORD, serviceProviderExtParm);
    }

    private Map<String, String> readDataByParam(String parameter, int phoneId) {
        IReadConfigParam readParam;
        Map<String, String> readData = new HashMap<>();
        if (parameter == null || ((parameter != null && parameter.isEmpty()) || (readParam = this.mConfigTableMap.get(parameter.toLowerCase(Locale.US))) == null)) {
            return readData;
        }
        return readParam.readParam(parameter.toLowerCase(Locale.US), phoneId);
    }

    private class ReadRootParm implements IReadConfigParam {
        private ReadRootParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            Map<String, String> readData = new HashMap<>();
            if ("version".equalsIgnoreCase(param) || "validity".equalsIgnoreCase(param)) {
                IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.VERS_PATH, phoneId);
                return storage.readAll(ConfigConstants.ConfigPath.VERS_PATH + param);
            } else if (!"token".equalsIgnoreCase(param)) {
                return readData;
            } else {
                IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.TOKEN_PATH, phoneId);
                return storage2.readAll(ConfigConstants.ConfigPath.TOKEN_PATH + param);
            }
        }
    }

    /* access modifiers changed from: private */
    public static Map<String, String> getPublicUserIdAndLboPcscfAddr(String path, String param, IStorageAdapter storage) {
        Map<String, String> readData;
        Map<String, String> map;
        String midpath = "";
        if (ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_PATH.equalsIgnoreCase(path) || ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_PATH.equalsIgnoreCase(path)) {
            midpath = "/";
        } else if (ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP20_PATH.equalsIgnoreCase(path) || ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_UP20_PATH.equalsIgnoreCase(path)) {
            midpath = "/node/";
        }
        int index = "".equals(midpath);
        Map<String, String> ret = new HashMap<>();
        if ("".equals(midpath)) {
            readData = storage.readAll(path + param + Integer.toString(index));
        } else {
            readData = storage.readAll(path + Integer.toString(index) + midpath + param);
        }
        while (readData != null && !readData.isEmpty()) {
            ret.putAll(readData);
            index++;
            if ("".equals(midpath)) {
                map = storage.readAll(path + param + Integer.toString(index));
            } else {
                map = storage.readAll(path + Integer.toString(index) + midpath + param);
            }
            readData = map;
        }
        if (ret.isEmpty() && (ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_UP20_PATH.equalsIgnoreCase(path) || ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP20_PATH.equalsIgnoreCase(path))) {
            ret = new HashMap<>();
            int nid = 0;
            while (true) {
                Map<String, String> readData2 = storage.readAll(path + "0/node/" + Integer.toString(nid) + "/" + param);
                if (readData2 == null || readData2.isEmpty()) {
                    break;
                }
                for (Map.Entry<String, String> entry : readData2.entrySet()) {
                    ret.put(entry.getKey(), entry.getValue());
                }
                nid++;
            }
        }
        return ret;
    }

    private class ReadRootAppParm implements IReadConfigParam {
        private ReadRootAppParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            Map<String, String> readData;
            Map<String, String> readData2 = new HashMap<>();
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_PATH, phoneId);
            if (ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.KEEP_ALIVE_ENABLED.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.TIMER_T1.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.TIMER_T2.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.TIMER_T4.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.REG_RETRY_BASE_TIME.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.REG_RETRY_MAX_TIME.equalsIgnoreCase(param)) {
                Map<String, String> readData3 = storage.readAll(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_PATH + param);
                if (readData3 == null || readData3.isEmpty()) {
                    readData = storage.readAll(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_UP20_PATH + param);
                } else {
                    readData = readData3;
                }
                return readData;
            } else if (ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY.equalsIgnoreCase(param)) {
                Map<String, String> readData4 = ConfigProvider.getPublicUserIdAndLboPcscfAddr(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_PATH, param, storage);
                Map<String, String> readData5 = (readData4 == null || readData4.isEmpty()) ? ConfigProvider.getPublicUserIdAndLboPcscfAddr(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP10_PATH, param, storage) : readData4;
                return (readData5 == null || readData5.isEmpty()) ? ConfigProvider.getPublicUserIdAndLboPcscfAddr(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP20_PATH, param, storage) : readData5;
            } else if (!"address".equalsIgnoreCase(param) && !ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE.equalsIgnoreCase(param)) {
                return readData2;
            } else {
                Map<String, String> readData6 = ConfigProvider.getPublicUserIdAndLboPcscfAddr(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_PATH, param, storage);
                Map<String, String> readData7 = (readData6 == null || readData6.isEmpty()) ? ConfigProvider.getPublicUserIdAndLboPcscfAddr(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP10_PATH, param, storage) : readData6;
                Map<String, String> readData8 = (readData7 == null || readData7.isEmpty()) ? ConfigProvider.getPublicUserIdAndLboPcscfAddr(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_UP20_PATH, param, storage) : readData7;
                return (readData8 == null || readData8.isEmpty()) ? ConfigProvider.getPublicUserIdAndLboPcscfAddr(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP20_PATH, param, storage) : readData8;
            }
        }
    }

    private class ReadExtParm implements IReadConfigParam {
        private ReadExtParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, phoneId);
            Map<String, String> readData = storage.readAll(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH + param);
            if (readData != null && !readData.isEmpty()) {
                return readData;
            }
            IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath("root/application/0/3gpp_ims/ext/gsma/", phoneId);
            return storage2.readAll("root/application/0/3gpp_ims/ext/gsma/" + param);
        }
    }

    private class ReadAppAuthParm implements IReadConfigParam {
        private ReadAppAuthParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.APPAUTH_CHARACTERISTIC_PATH, phoneId);
            Map<String, String> readData = storage.readAll(ConfigConstants.ConfigPath.APPAUTH_CHARACTERISTIC_PATH + param);
            if (readData != null && !readData.isEmpty()) {
                return readData;
            }
            IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath("root/application/0/3gpp_ims/ext/gsma/", phoneId);
            return storage2.readAll("root/application/0/3gpp_ims/ext/gsma/" + param);
        }
    }

    private class ReadServiceParm implements IReadConfigParam {
        private ReadServiceParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, phoneId);
            return storage.readAll(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH + param);
        }
    }

    /* access modifiers changed from: private */
    public static Map<String, String> getCapAllowedPrefixes(String path, IStorageAdapter storage) {
        int index = 1;
        Map<String, String> ret = new HashMap<>();
        Map<String, String> readData = storage.readAll(path + Integer.toString(1));
        while (readData != null && !readData.isEmpty()) {
            ret.putAll(readData);
            index++;
            readData = storage.readAll(path + Integer.toString(index));
        }
        return ret;
    }

    private class ReadCapDiscoveryParm implements IReadConfigParam {
        private ReadCapDiscoveryParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, phoneId);
            if (ConfigConstants.ConfigTable.CAPDISCOVERY_ALLOWED_PREFIXES.equalsIgnoreCase(param)) {
                return ConfigProvider.getCapAllowedPrefixes(ConfigConstants.ConfigPath.CAPDISCOVERY_ALLOWED_PREFIXES_PATH, storage);
            }
            if (ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE.equalsIgnoreCase(param)) {
                return storage.readAll(ConfigConstants.ConfigPath.CAPDISCOVERY_EXT_JOYN_PATH + param);
            }
            return storage.readAll(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH + param);
        }
    }

    private class ReadPresenceParm implements IReadConfigParam {
        private ReadPresenceParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.PRESENCE_CHARACTERISTIC_PATH, phoneId);
            if (ConfigConstants.ConfigTable.PRESENCE_LOC_INFO_MAX_VALID_TIME.equalsIgnoreCase(param)) {
                return storage.readAll(ConfigConstants.ConfigPath.PRESENCE_LOCATION_PATH + param);
            }
            return storage.readAll(ConfigConstants.ConfigPath.PRESENCE_CHARACTERISTIC_PATH + param);
        }
    }

    private class ReadImFtParm implements IReadConfigParam {
        private ReadImFtParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, phoneId);
            Map<String, String> readData = storage.readAll(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH + param);
            if (readData != null && !readData.isEmpty()) {
                return readData;
            }
            if (ConfigProvider.this.isImExtraParm(param)) {
                return storage.readAll(ConfigConstants.ConfigPath.IM_EXT_CHARACTERISTIC_PATH + param);
            } else if (ConfigProvider.this.isChatParm(param)) {
                return storage.readAll(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH + param);
            } else if (ConfigProvider.this.isFtExtraParm(param)) {
                return storage.readAll(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH + param);
            } else if (!ConfigConstants.ConfigTable.IM_EXPLODER_URI.equalsIgnoreCase(param)) {
                return readData;
            } else {
                return storage.readAll(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH + param);
            }
        }
    }

    private class ReadEnrichedCallingParm implements IReadConfigParam {
        private ReadEnrichedCallingParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            Map<String, String> readData;
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, phoneId);
            if (ConfigConstants.ConfigTable.DATAOFF_CONTENT_SHARE.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.DATAOFF_PRE_AND_POST_CALL.equalsIgnoreCase(param)) {
                readData = storage.readAll(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH + param);
            } else {
                readData = storage.readAll(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH + param);
            }
            if (readData != null && !readData.isEmpty()) {
                return readData;
            }
            IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.ENRICHED_CALLING_CHARACTERISTIC_PATH, phoneId);
            return storage2.readAll(ConfigConstants.ConfigPath.ENRICHED_CALLING_CHARACTERISTIC_PATH + param);
        }
    }

    /* access modifiers changed from: private */
    public boolean isImExtraParm(String param) {
        if (ConfigConstants.ConfigTable.IM_EXT_CB_FT_HTTP_CS_URI.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_EXT_MAX_SIZE_EXTRA_FILE_TR.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_EXT_FT_HTTP_EXTRA_CS_URI.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_EXT_MAX_IMDN_AGGREGATION.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isChatParm(String param) {
        if (ConfigConstants.ConfigTable.IM_AUT_ACCEPT.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_AUT_ACCEPT_GROUP_CHAT.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_CHAT_REVOKE_TIMER.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_CONF_FCTY_URI.equalsIgnoreCase(param) || "max_adhoc_group_size".equalsIgnoreCase(param) || "MaxSize".equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_TIMER_IDLE.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isFtExtraParm(String param) {
        if (ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_DL_URI.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_HTTP_FALLBACK.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_FT_WARN_SIZE.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR.equalsIgnoreCase(param)) {
            return true;
        }
        return false;
    }

    private class ReadStandalonMsgParm implements IReadConfigParam {
        private ReadStandalonMsgParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SLM_CHARACTERISTIC_PATH, phoneId);
            Map<String, String> readData = storage.readAll(ConfigConstants.ConfigPath.SLM_CHARACTERISTIC_PATH + param);
            if (readData != null && !readData.isEmpty()) {
                return readData;
            }
            if (!ConfigConstants.ConfigTable.CPM_SLM_MAX_MSG_SIZE.equalsIgnoreCase(param) && !"MaxSize".equalsIgnoreCase(param) && !ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE.equalsIgnoreCase(param)) {
                return readData;
            }
            IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH, phoneId);
            return storage2.readAll(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH + param);
        }
    }

    private class ReadCpmMessageStoreParm implements IReadConfigParam {
        private ReadCpmMessageStoreParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CPM_CHARACTERISTIC_PATH, phoneId);
            Map<String, String> readData = storage.readAll(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH + param);
            if (readData != null && !readData.isEmpty()) {
                return readData;
            }
            if (ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_NAME.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_PWD.equalsIgnoreCase(param)) {
                return storage.readAll(ConfigConstants.ConfigPath.CPM_CHARACTERISTIC_PATH + param);
            } else if (!"EventRpting".equalsIgnoreCase(param) && !ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_AUTH_ARCHIVE.equalsIgnoreCase(param) && !"SMSStore".equalsIgnoreCase(param) && !"MMSStore".equalsIgnoreCase(param)) {
                return readData;
            } else {
                IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH, phoneId);
                return storage2.readAll(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH + param);
            }
        }
    }

    private class ReadOtherParm implements IReadConfigParam {
        private ReadOtherParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.OTHER_CHARACTERISTIC_PATH, phoneId);
            return storage.readAll(ConfigConstants.ConfigPath.OTHER_CHARACTERISTIC_PATH + param);
        }
    }

    private class ReadXdmsParm implements IReadConfigParam {
        private ReadXdmsParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.XDMS_CHARACTERISTIC_PATH, phoneId);
            return storage.readAll(ConfigConstants.ConfigPath.XDMS_CHARACTERISTIC_PATH + param);
        }
    }

    private class ReadTransportProtoParm implements IReadConfigParam {
        private ReadTransportProtoParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, phoneId);
            Map<String, String> readData = storage.readAll(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH + param);
            if (readData != null && !readData.isEmpty()) {
                return readData;
            }
            IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP10_PATH, phoneId);
            Map<String, String> readData2 = storage2.readAll(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP10_PATH + param);
            if (readData2 != null && !readData2.isEmpty()) {
                return readData2;
            }
            IStorageAdapter storage3 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP20_PATH, phoneId);
            return storage3.readAll(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP20_PATH + param);
        }
    }

    private class ReadPublicAccountParm implements IReadConfigParam {
        private ReadPublicAccountParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath("root/application/1/", phoneId);
            return storage.readAll("root/application/1/" + param);
        }
    }

    private class ReadPersonalProfileParm implements IReadConfigParam {
        private ReadPersonalProfileParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath("root/", phoneId);
            return storage.readAll("root/" + param);
        }
    }

    private class ReadUxParm implements IReadConfigParam {
        private ReadUxParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, phoneId);
            Map<String, String> readData = storage.readAll(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH + param);
            if (readData != null && !readData.isEmpty()) {
                return readData;
            }
            if (!ConfigConstants.ConfigTable.UX_MESSAGING_UX.equalsIgnoreCase(param) && !ConfigConstants.ConfigTable.UX_MSG_FB_DEFAULT.equalsIgnoreCase(param)) {
                return readData;
            }
            IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.JOYN_UX_PATH, phoneId);
            return storage2.readAll(ConfigConstants.ConfigPath.JOYN_UX_PATH + param);
        }
    }

    private class ReadClientControlParm implements IReadConfigParam {
        private ReadClientControlParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            Map<String, String> readData;
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, phoneId);
            Map<String, String> readData2 = storage.readAll(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH + param);
            if (readData2 != null && !readData2.isEmpty()) {
                return readData2;
            }
            if (ConfigConstants.ConfigTable.CLIENTCONTROL_RECONNECT_GUARD_TIMER.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CLIENTCONTROL_CFS_TRIGGER.equalsIgnoreCase(param)) {
                IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH, phoneId);
                Map<String, String> readData3 = storage2.readAll(ConfigConstants.ConfigPath.JOYN_MESSAGING_CHARACTERISTIC_PATH + param);
                if (readData3 == null || readData3.isEmpty()) {
                    readData = storage2.readAll(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH + param);
                } else {
                    readData = readData3;
                }
                return readData;
            } else if (ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY.equalsIgnoreCase(param)) {
                IStorageAdapter storage3 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, phoneId);
                return storage3.readAll(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH + param);
            } else if (ConfigConstants.ConfigTable.CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS.equalsIgnoreCase(param)) {
                IStorageAdapter storage4 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.MESSAGING_CHARACTERISTIC_PATH, phoneId);
                return storage4.readAll(ConfigConstants.ConfigPath.MESSAGING_CHARACTERISTIC_PATH + param);
            } else if (!ConfigConstants.ConfigTable.CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS.equalsIgnoreCase(param)) {
                return readData2;
            } else {
                IStorageAdapter storage5 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH, phoneId);
                return storage5.readAll(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH + param);
            }
        }
    }

    private class ReadMsisdnParm implements IReadConfigParam {
        private ReadMsisdnParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.MSISDN_PATH, phoneId);
            return storage.readAll(ConfigConstants.ConfigPath.MSISDN_PATH + param);
        }
    }

    private class ReadChatbotParm implements IReadConfigParam {
        private ReadChatbotParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, phoneId);
            return storage.readAll(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH + param);
        }
    }

    private class ReadMessageStoreParm implements IReadConfigParam {
        private ReadMessageStoreParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH, phoneId);
            return storage.readAll(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH + param);
        }
    }

    private class ReadPluginsParm implements IReadConfigParam {
        private ReadPluginsParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.PLUGINS_CHARACTERISTIC_PATH, phoneId);
            return storage.readAll(ConfigConstants.ConfigPath.PLUGINS_CHARACTERISTIC_PATH + param);
        }
    }

    private class ReadDataOffParm implements IReadConfigParam {
        private ReadDataOffParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, phoneId);
            return storage.readAll(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH + param);
        }
    }

    private class ReadServiceExtParm implements IReadConfigParam {
        private ReadServiceExtParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_PATH, phoneId);
            return storage.readAll(ConfigConstants.ConfigPath.SERVICE_PATH + param);
        }
    }

    private class ReadServiceProviderExtParm implements IReadConfigParam {
        private ReadServiceProviderExtParm() {
        }

        public Map<String, String> readParam(String param, int phoneId) {
            if (ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_FTHTTPGROUPCHAT.equalsIgnoreCase(param)) {
                IStorageAdapter storage = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHARACTERISTIC_PATH, phoneId);
                return storage.readAll(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHARACTERISTIC_PATH + param);
            } else if (ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_USER_NAME.equalsIgnoreCase(param) || ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_PASSWORD.equalsIgnoreCase(param)) {
                IStorageAdapter storage2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHATBOT_PATH, phoneId);
                return storage2.readAll(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHARACTERISTIC_PATH + param);
            } else {
                IStorageAdapter storage3 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_PROVIDER_EXT_PATH, phoneId);
                return storage3.readAll(ConfigConstants.ConfigPath.SERVICE_PROVIDER_EXT_PATH + param);
            }
        }
    }

    private static String getIdentityByPhoneId(Context context, int phoneId) {
        if (context.checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            return null;
        }
        return ConfigUtil.buildIdentity(context, phoneId);
    }

    private IStorageAdapter getStorageByUri(Uri uri) {
        int slotId = UriUtil.getSimSlotFromUri(uri);
        int offset = ConfigConstants.CONFIG_URI.length();
        String path = uri.toString();
        List<String> appIdList = null;
        if (path.contains("root/application/")) {
            String temp = path.substring(offset + 17);
            if (temp.indexOf(47) != -1) {
                appIdList = mAppIdMap.get(temp.substring(0, temp.indexOf(47)));
            } else {
                appIdList = mAppIdMap.get("0");
            }
        }
        return initStorage(getContext(), slotId, appIdList);
    }

    /* access modifiers changed from: private */
    public IStorageAdapter getStorageByPath(String path, int phoneId) {
        IStorageAdapter additionalStorage;
        List<String> appIdList = null;
        if (path.contains("root/application/")) {
            String temp = path.substring(17);
            if (temp.indexOf(47) != -1) {
                appIdList = mAppIdMap.get(temp.substring(0, temp.indexOf(47)));
            } else {
                appIdList = null;
            }
        }
        if (appIdList == null || this.mAppIdServerIdMap.get(Integer.valueOf(phoneId)) == null) {
            return (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(phoneId)).get(0);
        }
        for (String appId : appIdList) {
            Integer serverId = (Integer) this.mAppIdServerIdMap.get(Integer.valueOf(phoneId)).get(appId);
            if (serverId != null && (additionalStorage = (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(phoneId)).get(serverId)) != null) {
                return additionalStorage;
            }
        }
        return (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(phoneId)).get(0);
    }
}
