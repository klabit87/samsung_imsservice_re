package com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.MigrationHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.ConfigElementExtractor;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class NSDSContentProvider extends ContentProvider {
    private static final String ACCESS_STEERING_TABLE = "access_steering";
    private static final String ACCOUNTS_TABLE = "accounts";
    private static final String CONNECTIVITY_PARAMS_TABLE = "connectivity_parameters";
    private static final String CONNECTIVITY_SERVICE_NAME_TABLE = "connectivity_sevice_names";
    private static final String CREATE_ACCOUNT_TABLE = "CREATE TABLE IF NOT EXISTS accounts(_id INTEGER PRIMARY KEY AUTOINCREMENT,account_id TEXT NOT NULL,device_uid TEXT, email TEXT,access_token TEXT,is_active INTEGER DEFAULT 0,is_temporary INTEGER DEFAULT 0, UNIQUE(account_id));";
    private static final String CREATE_CONNECTIVITY_PARAMS_TABLE = "CREATE TABLE IF NOT EXISTS connectivity_parameters(_id INTEGER PRIMARY KEY AUTOINCREMENT,certificate TEXT,epdg_addresses TEXT);";
    private static final String CREATE_CONNECTIVITY_PARAM_SERVICE_NAME_TABLE = "CREATE TABLE IF NOT EXISTS connectivity_sevice_names(_id INTEGER PRIMARY KEY AUTOINCREMENT,connectivity_id INTEGER REFERENCES connectivity_parameters(_id) NOT NULL, service_name TEXT NOT NULL,client_id TEXT NOT NULL,package_name TEXT NOT NULL,appstore_url TEXT NOT NULL);";
    private static final String CREATE_DEVICES_TABLE = "CREATE TABLE IF NOT EXISTS devices(_id INTEGER PRIMARY KEY AUTOINCREMENT,device_uid TEXT NOT NULL,device_account_id INTEGER REFERENCES accounts(_id) NOT NULL,device_name TEXT NOT NULL,is_primary INTEGER DEFAULT 0,device_type INTEGER DEFAULT 0,is_local INTEGER DEFAULT 0, UNIQUE(device_account_id,device_uid));";
    private static final String CREATE_DEVICE_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS device_config(_id INTEGER PRIMARY KEY AUTOINCREMENT,device_id TEXT NOT NULL, version TEXT, device_config TEXT);";
    private static final String CREATE_GCM_TOKENS_TABLE = "CREATE TABLE IF NOT EXISTS gcm_tokens(_id INTEGER PRIMARY KEY AUTOINCREMENT,sender_id TEXT NOT NULL,gcm_token TEXT NOT NULL,protocol_to_server TEXT, device_uid TEXT, UNIQUE( sender_id));";
    private static final String CREATE_LINES_TABLE = "CREATE TABLE IF NOT EXISTS lines(_id INTEGER PRIMARY KEY AUTOINCREMENT,account_id INTEGER REFERENCES accounts(_id) NOT NULL,msisdn TEXT NOT NULL,friendly_name TEXT NOT NULL,status INTEGER DEFAULT 0,line_res_package TEXT, icon INTEGER,color INTEGER,type TEXT DEFAULT regular,is_owner INTEGER DEFAULT 1,service_attributes TEXT, is_device_default INTEGER DEFAULT 0, location_status INTEGER , tc_status INTEGER , e911_address_id TEXT, e911_aid_expiration TEXT, e911_server_data TEXT, e911_server_url TEXT, cab_status INTEGER DEFAULT 0, reg_status INTEGER DEFAULT 0, ring_tone TEXT, UNIQUE( account_id,msisdn));";
    private static final String CREATE_NSDS_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS nsds_configs(_id INTEGER PRIMARY KEY AUTOINCREMENT,imsi TEXT, pname TEXT NOT NULL,pvalue TEXT);";
    private static final String CREATE_PROVISIONING_PARAMS_TABLE = "CREATE TABLE IF NOT EXISTS provisioning_parameters(_id INTEGER PRIMARY KEY AUTOINCREMENT,apn TEXT NOT NULL,pcscf_address TEXT NOT NULL,sip_uri TEXT NOT NULL,impu TEXT NOT NULL,sip_username TEXT,sip_password TEXT NOT NULL);";
    private static final String CREATE_SERVICES_TABLE = "CREATE TABLE IF NOT EXISTS services(_id INTEGER PRIMARY KEY AUTOINCREMENT,line_id INTEGER REFERENCES lines(_id),device_id INTEGER REFERENCES devices(_id),is_native INTEGER DEFAULT 0,service_name TEXT,service_instance_id TEXT,expiration_time INTEGER DEFAULT 0,service_msisdn TEXT,is_owner INTEGER,msisdn_friendly_name TEXT,service_fingerprint TEXT DEFAULT NULL,service_instance_token TEXT, service_token_expire_time TEXT, provisioning_params_id INTEGER REFERENCES provisioning_parameters(_id),config_parameters TEXT);";
    private static final String CREATE_SIM_SWAP_NSDS_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS sim_swap_nsds_configs(_id INTEGER PRIMARY KEY AUTOINCREMENT,imsi TEXT NOT NULL, pname TEXT NOT NULL,pvalue TEXT);";
    private static final String CREATE_SIM_SWAP_SERVICES_TABLE = "CREATE TABLE IF NOT EXISTS sim_swap_services(_id INTEGER PRIMARY KEY AUTOINCREMENT,line_id INTEGER REFERENCES lines(_id),device_id INTEGER REFERENCES devices(_id),is_native INTEGER ,service_name TEXT,service_instance_id TEXT,expiration_time INTEGER DEFAULT 0,service_msisdn TEXT,is_owner INTEGER,msisdn_friendly_name TEXT,service_fingerprint TEXT DEFAULT NULL,service_instance_token TEXT, service_token_expire_time TEXT, provisioning_params_id INTEGER REFERENCES provisioning_parameters(_id),config_parameters TEXT);";
    private static final String DATABASE_NAME = "ericsson_nsds.db";
    private static final int DATABASE_VERSION = 3;
    private static final String DEVICES_TABLE = "devices";
    private static final String DEVICE_CONFIG_TABLE = "device_config";
    private static final String GCM_TOKENS_TABLE = "gcm_tokens";
    private static final String LINES_TABLE = "lines";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = NSDSContentProvider.class.getSimpleName();
    private static final String NSDS_CONFIG_TABLE = "nsds_configs";
    private static final String PENDING_LINES_TABLE = "pending_lines";
    private static final String PROVIDER_NAME = "com.samsung.ims.nsds.provider";
    private static final String PROVISIONING_PARAMS_TABLE = "provisioning_parameters";
    private static final String REMOVE_ALL_TABLES_AND_INDICES = "PRAGMA writable_schema = 1; DELETE FROM sqlite_master WHERE TYPE IN ('table', 'index'); PRAGMA writable_schema = 0; ";
    private static final String SERVICES_TABLE = "services";
    private static final String SIM_SWAP_NSDS_CONFIG_TABLE = "sim_swap_nsds_configs";
    private static final String SIM_SWAP_SERVICES_TABLE = "sim_swap_services";
    private static final String SQL_WHERE_ACCOUNT_ID = "_id = ?";
    private static final String SQL_WHERE_ACTIVE_ACCOUNT = "is_active = 1";
    private static final String SQL_WHERE_ALL_LINES = " (lines.account_id != 0 OR is_native = 1)";
    private static final String SQL_WHERE_DEVICES_FOR_LINE_ID = "devices._id IN(SELECT services.device_id from lines, devices, services  where device_id = devices._id AND line_id = lines._id AND status = 1 AND line_id = ?)";
    private static final String SQL_WHERE_DEVICE_ID = "_id = ?";
    private static final String SQL_WHERE_LINES_FOR_ACTIVE_ACCOUNT = "services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ? AND  (lines.account_id = 0 OR lines.account_id = (SELECT _id from accounts where is_active = 1))";
    private static final String SQL_WHERE_LINE_ENTITIY_BASE = "line_id = lines._id AND device_id = devices._id";
    private static final String SQL_WHERE_LINE_ENTITIY_ID = "line_id = lines._id AND device_id = devices._id AND lines._id= ?";
    private static final String SQL_WHERE_LINE_ENTITY_ACTIVE_ACCOUNT = "line_id = lines._id AND device_id = devices._id AND services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ? AND  (lines.account_id = 0 OR lines.account_id = (SELECT _id from accounts where is_active = 1))";
    private static final String SQL_WHERE_LINE_ID = "lines._id = ?";
    private static final String SQL_WHERE_LINE_STATUS_ACTIVE = "services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ? AND  (lines.account_id = 0 OR lines.account_id = (SELECT _id from accounts where is_active = 1)) AND status = ?";
    private static final String SQL_WHERE_LOCAL_LINES_WITH_SERVICES = "services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ? AND status = ?";
    private static final String SQL_WHERE_LOCAL_LINES_WITH_SERVICES_BASE = "services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ?";
    public static final String TABLE_JOIN_FOR_ALL_LINES = "lines LEFT OUTER JOIN services on services.line_id = lines._id ";
    public static final String TABLE_JOIN_LINES_SERVICES = "lines, services,devices";
    private static final HashMap<String, String> sLineEntityProjectionMap;
    private static final HashMap<String, String> sLineWithServicesProjectionMap;
    private static final UriMatcher sUriMatcher;
    protected Context mContext = null;
    protected DatabaseHelper mDatabaseHelper = null;
    protected Messenger mNsdsService;
    protected ServiceConnection mNsdsSvcConn;

    private interface LineEntityQuery {
        public static final String TABLE = "lines, devices, services";
    }

    private interface LinesColumns {
        public static final String ACCOUNT_ID = "lines.account_id";
        public static final String CONCRETE_ID = "lines._id";
        public static final String IS_NATIVE = "services.is_native";
        public static final String IS_OWNER = "lines.is_owner";
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "lines/#/enable_cab", 49);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "lines/#/disable_cab", 50);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", LINES_TABLE, 0);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "lines/#/devices", 43);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/services", 6);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", DEVICES_TABLE, 2);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/add_services", 17);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/remove_services", 18);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/acitvate_services", 19);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/deactivate_services", 20);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "accounts/disable_active_account", 48);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "activate_sim_device", 30);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "deactivate_sim_device", 31);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "update_e911_address", 46);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "vowifi_toggle_on", 32);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "vowifi_toggle_off", 33);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "accounts/upload_all_contacts", 23);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "accounts/download_all_contacts", 24);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "accounts/upload_updated_contact/#", 25);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/set_primary", 26);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/own_activation_status", 28);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/own_login_status", 41);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/own_ready_status", 60);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/own_nsds_service_status", 61);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/push_token", 67);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "device_config", 39);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "device_config/element", 62);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", NSDS_CONFIG_TABLE, 40);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "nsds_configs/entitlement_url", 73);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/services", 42);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "all_lines_in_current_account", 44);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "all_lines", 45);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "all_lines_internal", 77);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "services", 63);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", SIM_SWAP_NSDS_CONFIG_TABLE, 71);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", SIM_SWAP_SERVICES_TABLE, 72);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", GCM_TOKENS_TABLE, 74);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "retrieve_aka_token", 80);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "reconnect_db", 81);
        sUriMatcher.addURI("com.samsung.ims.nsds.provider", "binding_service", 82);
        HashMap<String, String> hashMap = new HashMap<>();
        sLineWithServicesProjectionMap = hashMap;
        hashMap.put("_id", LinesColumns.CONCRETE_ID);
        sLineWithServicesProjectionMap.put("account_id", LinesColumns.ACCOUNT_ID);
        sLineWithServicesProjectionMap.put("msisdn", "msisdn");
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.FRIENDLY_NAME, NSDSContractExt.LineColumns.FRIENDLY_NAME);
        sLineWithServicesProjectionMap.put("type", "type");
        sLineWithServicesProjectionMap.put("status", "status");
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.LINE_RES_PACKAGE, NSDSContractExt.LineColumns.LINE_RES_PACKAGE);
        sLineWithServicesProjectionMap.put("icon", "icon");
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.COLOR, NSDSContractExt.LineColumns.COLOR);
        sLineWithServicesProjectionMap.put("is_native", LinesColumns.IS_NATIVE);
        sLineWithServicesProjectionMap.put("is_owner", LinesColumns.IS_OWNER);
        sLineWithServicesProjectionMap.put("is_native", "is_native");
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.SERVICE_ATTRIBUTES, NSDSContractExt.LineColumns.SERVICE_ATTRIBUTES);
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.IS_DEVICE_DEFAULT, NSDSContractExt.LineColumns.IS_DEVICE_DEFAULT);
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.LOCATION_STATUS, NSDSContractExt.LineColumns.LOCATION_STATUS);
        sLineWithServicesProjectionMap.put("tc_status", "tc_status");
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.E911_ADDRESS_ID, NSDSContractExt.LineColumns.E911_ADDRESS_ID);
        sLineWithServicesProjectionMap.put("e911_aid_expiration", "e911_aid_expiration");
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.E911_SERVER_DATA, NSDSContractExt.LineColumns.E911_SERVER_DATA);
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.E911_SERVER_URL, NSDSContractExt.LineColumns.E911_SERVER_URL);
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.CAB_STATUS, NSDSContractExt.LineColumns.CAB_STATUS);
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.REG_STATUS, NSDSContractExt.LineColumns.REG_STATUS);
        sLineWithServicesProjectionMap.put(NSDSContractExt.LineColumns.RING_TONE, NSDSContractExt.LineColumns.RING_TONE);
        sLineWithServicesProjectionMap.put("service_name", "service_name");
        sLineWithServicesProjectionMap.put(NSDSContractExt.ServiceColumns.SERVICE_FINGERPRINT, NSDSContractExt.ServiceColumns.SERVICE_FINGERPRINT);
        sLineWithServicesProjectionMap.put(NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_ID, NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_ID);
        sLineWithServicesProjectionMap.put(NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_TOKEN, NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_TOKEN);
        sLineWithServicesProjectionMap.put(NSDSContractExt.ServiceColumns.SERVICE_TOKEN_EXPIRE_TIME, NSDSContractExt.ServiceColumns.SERVICE_TOKEN_EXPIRE_TIME);
        HashMap<String, String> hashMap2 = new HashMap<>();
        sLineEntityProjectionMap = hashMap2;
        hashMap2.put("_id", LinesColumns.CONCRETE_ID);
        sLineEntityProjectionMap.put("account_id", LinesColumns.ACCOUNT_ID);
        sLineEntityProjectionMap.put("msisdn", "msisdn");
        sLineEntityProjectionMap.put(NSDSContractExt.LineColumns.FRIENDLY_NAME, NSDSContractExt.LineColumns.FRIENDLY_NAME);
        sLineEntityProjectionMap.put("is_owner", "Lines.is_owner");
        sLineEntityProjectionMap.put("is_native", "is_native");
        sLineEntityProjectionMap.put("is_native", "is_native");
        sLineEntityProjectionMap.put("icon", "icon");
        sLineEntityProjectionMap.put(NSDSContractExt.LineColumns.COLOR, NSDSContractExt.LineColumns.COLOR);
        sLineEntityProjectionMap.put("device_uid", "device_uid");
        sLineEntityProjectionMap.put("device_name", "device_name");
        sLineEntityProjectionMap.put("is_primary", "is_primary");
        sLineEntityProjectionMap.put("device_type", "device_type");
    }

    protected static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, NSDSContentProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 3);
        }

        public void onCreate(SQLiteDatabase db) {
            IMSLog.i(NSDSContentProvider.LOG_TAG, "DatabaseHelper onCreate()");
            db.execSQL(NSDSContentProvider.REMOVE_ALL_TABLES_AND_INDICES);
            db.execSQL(NSDSContentProvider.CREATE_LINES_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_GCM_TOKENS_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_DEVICES_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_ACCOUNT_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_SERVICES_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_CONNECTIVITY_PARAMS_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_CONNECTIVITY_PARAM_SERVICE_NAME_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_PROVISIONING_PARAMS_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_DEVICE_CONFIG_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_NSDS_CONFIG_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_SIM_SWAP_NSDS_CONFIG_TABLE);
            db.execSQL(NSDSContentProvider.CREATE_SIM_SWAP_SERVICES_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String access$000 = NSDSContentProvider.LOG_TAG;
            IMSLog.i(access$000, "onUpgrade: oldVersion " + oldVersion + " newVersion " + newVersion);
            if (newVersion == 2 && oldVersion == 1) {
                NSDSContentProvider.renameDeviceAccountIdColumn(db);
            }
            if (newVersion == 3) {
                db.execSQL("DROP TABLE IF EXISTS pending_lines");
                db.execSQL("DROP TABLE IF EXISTS access_steering");
            }
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String access$000 = NSDSContentProvider.LOG_TAG;
            IMSLog.i(access$000, "onDowngrade: oldVersion " + oldVersion + " newVersion " + newVersion);
        }
    }

    /* access modifiers changed from: private */
    public static void renameDeviceAccountIdColumn(SQLiteDatabase db) {
        IMSLog.i(LOG_TAG, "renameDeviceAccountIdColumn()");
        String tempTableName = DEVICES_TABLE + "_temp";
        db.execSQL("ALTER TABLE " + DEVICES_TABLE + " RENAME TO " + tempTableName);
        db.execSQL(CREATE_DEVICES_TABLE);
        db.execSQL("INSERT INTO " + DEVICES_TABLE + " select * from " + tempTableName);
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE ");
        sb.append(tempTableName);
        db.execSQL(sb.toString());
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String str = LOG_TAG;
        IMSLog.i(str, "delete:" + uri);
        int numRows = 0;
        if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return 0;
        }
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        boolean notify = true;
        try {
            int match = sUriMatcher.match(uri);
            if (match == 0) {
                numRows = db.delete(LINES_TABLE, selection, selectionArgs);
            } else if (match == 2) {
                numRows = db.delete(DEVICES_TABLE, selection, selectionArgs);
            } else if (match == 6) {
                numRows = deleteFromServices(uri.getPathSegments().get(1), uri.getPathSegments().get(3));
            } else if (match == 42) {
                numRows = db.delete("services", "device_id = ?", new String[]{uri.getPathSegments().get(1)});
            } else if (match == 47) {
                numRows = db.delete(ACCOUNTS_TABLE, selection, selectionArgs);
            } else if (match == 71) {
                notify = false;
                numRows = db.delete(SIM_SWAP_NSDS_CONFIG_TABLE, selection, selectionArgs);
            } else if (match == 74) {
                numRows = db.delete(GCM_TOKENS_TABLE, selection, selectionArgs);
            } else if (match == 39) {
                notify = false;
                numRows = db.delete("device_config", selection, selectionArgs);
            } else if (match != 40) {
                String str2 = LOG_TAG;
                IMSLog.i(str2, "None of the Uri's match for insert:" + uri);
            } else {
                notify = false;
                numRows = db.delete(NSDS_CONFIG_TABLE, selection, selectionArgs);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "Could not update LINES table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        if (numRows > 0 && notify) {
            notifyChange(uri);
        }
        return numRows;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        String str = LOG_TAG;
        IMSLog.s(str, "insert: " + uri);
        if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return null;
        }
        Uri retUri = null;
        int match = sUriMatcher.match(uri);
        if (match == 0) {
            retUri = NSDSContractExt.Lines.buildLineUri(insertIntoLines(values));
        } else if (match == 2) {
            retUri = NSDSContractExt.Devices.buildDeviceUri(insertIntoDevices(values));
        } else if (match == 6) {
            long paramDeviceId = Long.valueOf(uri.getPathSegments().get(1)).longValue();
            long paramLineId = Long.valueOf(uri.getPathSegments().get(3)).longValue();
            values.put("device_id", Long.valueOf(paramDeviceId));
            values.put("line_id", Long.valueOf(paramLineId));
            retUri = NSDSContractExt.Services.buildServiceUri(insertIntoServices(values));
        } else if (match == 9) {
            retUri = NSDSContractExt.Accounts.buildAccountUri(insertIntoAccounts(values));
        } else if (match == 71) {
            retUri = NSDSContractExt.SimSwapNsdsConfigs.buildNsdsConfigUri(insertIntoSimSwapNsdsConfig(values));
        } else if (match == 74) {
            retUri = NSDSContractExt.GcmTokens.buildGcmTokensUri(insertIntoGcmTokens(values));
        } else if (match == 39) {
            retUri = NSDSContractExt.DeviceConfig.buildDeviceConfigUri(insertDeviceConfig(values));
        } else if (match == 40) {
            retUri = NSDSContractExt.NsdsConfigs.buildNsdsConfigUri(insertIntoNsdsConfig(values));
        }
        if (retUri != null) {
            notifyChange(uri);
        }
        return retUri;
    }

    public void notifyChange(Uri uri) {
        this.mContext.getContentResolver().notifyChange(uri, (ContentObserver) null);
    }

    private long insertIntoLines(ContentValues values) {
        ContentValues contentValues = values;
        long lineId = -1;
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String resPackage = contentValues.getAsString(NSDSContractExt.LineColumns.LINE_RES_PACKAGE);
            Resources resources = this.mContext.getResources();
            if (!TextUtils.isEmpty(resPackage)) {
                try {
                    resources = this.mContext.getPackageManager().getResourcesForApplication(resPackage);
                } catch (PackageManager.NameNotFoundException e) {
                    PackageManager.NameNotFoundException e2 = e;
                    String str = LOG_TAG;
                    IMSLog.s(str, "Lines resource package not found: " + resPackage + e2.getMessage());
                }
            }
            Integer colorResourceId = contentValues.getAsInteger(NSDSContractExt.LineColumns.COLOR);
            String colorResource = getResourceName(resources, NSDSContractExt.LineColumns.COLOR, colorResourceId);
            if (colorResource == null) {
                contentValues.remove(NSDSContractExt.LineColumns.COLOR);
                String str2 = LOG_TAG;
                IMSLog.e(str2, "Color resource is null, removing: " + colorResourceId + " from values");
            }
            Integer iconResourceId = contentValues.getAsInteger("icon");
            String iconResource = getResourceName(resources, "drawable", iconResourceId);
            if (iconResource == null) {
                String str3 = LOG_TAG;
                StringBuilder sb = new StringBuilder();
                Integer num = colorResourceId;
                sb.append("Icon resource is null, removing: ");
                sb.append(iconResourceId);
                sb.append(" from values");
                IMSLog.e(str3, sb.toString());
                contentValues.remove("icon");
            }
            if (colorResource == null && iconResource == null) {
                contentValues.remove(NSDSContractExt.LineColumns.LINE_RES_PACKAGE);
                String str4 = LOG_TAG;
                IMSLog.e(str4, "Both color and icon resource are null, removing: " + resPackage + " from values");
            }
            lineId = db.insert(LINES_TABLE, (String) null, contentValues);
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str5 = LOG_TAG;
            IMSLog.s(str5, "Could not insert into LINES:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return lineId;
    }

    private long insertIntoDevices(ContentValues values) {
        long rowId = -1;
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            rowId = db.insert(DEVICES_TABLE, (String) null, values);
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into DEVICES table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowId;
    }

    private long insertIntoAccounts(ContentValues values) {
        long rowId = -1;
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            rowId = db.insert(ACCOUNTS_TABLE, (String) null, values);
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into DEVICES table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowId;
    }

    private long insertIntoServices(ContentValues values) {
        long rowId = -1;
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            rowId = db.insert("services", (String) null, values);
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into SERVICES table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowId;
    }

    private long insertIntoGcmTokens(ContentValues values) {
        long rowId = -1;
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            rowId = db.insert(GCM_TOKENS_TABLE, (String) null, values);
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into GCM Tokens table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowId;
    }

    private int deleteFromServices(String deviceId, String lineId) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        int noRows = 0;
        db.beginTransaction();
        try {
            noRows = db.delete("services", "device_id = ? AND line_id = ?", new String[]{deviceId, lineId});
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not delete from Services table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return noRows;
    }

    private long insertDeviceConfig(ContentValues values) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        long rowId = -1;
        try {
            rowId = db.insert("device_config", (String) null, values);
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into device_config table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowId;
    }

    private int updateDeviceConfig(ContentValues values) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        int numRows = 0;
        try {
            numRows = db.update("device_config", values, (String) null, (String[]) null);
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not update connectivity_parameters table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return numRows;
    }

    private long insertIntoNsdsConfig(ContentValues values) {
        long rowId = -1;
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            rowId = db.insert(NSDS_CONFIG_TABLE, (String) null, values);
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into nsds_configs table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowId;
    }

    private long insertIntoSimSwapNsdsConfig(ContentValues values) {
        long rowId = -1;
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            rowId = db.insert(SIM_SWAP_NSDS_CONFIG_TABLE, (String) null, values);
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into nsds_configs table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowId;
    }

    public boolean onCreate() {
        this.mContext = getContext().createCredentialProtectedStorageContext();
        return true;
    }

    private synchronized void connectToNSDSMultiSimService() {
        IMSLog.i(LOG_TAG, "connectToNSDSMultiSimService()");
        Intent intent = new Intent();
        intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.entitlement.nsds.NSDSMultiSimService");
        AnonymousClass1 r1 = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                IMSLog.i(NSDSContentProvider.LOG_TAG, "onServiceConnected: Connected to NSDSMultiSimService.");
                if (MigrationHelper.checkMigrateDB(NSDSContentProvider.this.mContext)) {
                    IMSLog.i(NSDSContentProvider.LOG_TAG, "Connect DB");
                    NSDSContentProvider.this.mDatabaseHelper = new DatabaseHelper(NSDSContentProvider.this.mContext);
                }
                NSDSContentProvider.this.mNsdsService = new Messenger(service);
            }

            public void onServiceDisconnected(ComponentName name) {
                IMSLog.i(NSDSContentProvider.LOG_TAG, "onServiceDisconnected: Disconnected.");
                NSDSContentProvider.this.mNsdsService = null;
            }
        };
        this.mNsdsSvcConn = r1;
        ContextExt.bindServiceAsUser(this.mContext, intent, r1, 1, ContextExt.CURRENT_OR_SELF);
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db;
        String newSelection;
        SQLiteQueryBuilder queryBuilder;
        String newSelection2;
        String newSelection3;
        String newSelection4;
        String newSelection5;
        Uri uri2 = uri;
        String str = selection;
        String[] strArr = selectionArgs;
        String str2 = LOG_TAG;
        IMSLog.s(str2, "query " + uri2);
        if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return null;
        }
        SQLiteDatabase db2 = this.mDatabaseHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder2 = new SQLiteQueryBuilder();
        String newSelection6 = selection;
        int match = sUriMatcher.match(uri2);
        if (match == 0) {
            newSelection = newSelection6;
            db = db2;
            queryBuilder = queryBuilder2;
        } else if (match == 2) {
            SQLiteDatabase db3 = db2;
            SQLiteQueryBuilder queryBuilder3 = queryBuilder2;
            queryBuilder3.setTables(DEVICES_TABLE);
            SQLiteQueryBuilder sQLiteQueryBuilder = queryBuilder3;
            return queryBuilder3.query(db3, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
        } else if (match == 29) {
            SQLiteDatabase db4 = db2;
            SQLiteQueryBuilder queryBuilder4 = queryBuilder2;
            queryBuilder4.setTables(TABLE_JOIN_LINES_SERVICES);
            queryBuilder4.appendWhere(SQL_WHERE_LOCAL_LINES_WITH_SERVICES);
            queryBuilder4.setProjectionMap(sLineWithServicesProjectionMap);
            SQLiteQueryBuilder sQLiteQueryBuilder2 = queryBuilder4;
            return queryBuilder4.query(db4, projection, selection, insertSelectionArg(insertSelectionArg(strArr, "1"), getDeviceUidFromQueryParam(uri)), (String) null, (String) null, sortOrder);
        } else if (match == 67) {
            SQLiteDatabase sQLiteDatabase = db2;
            SQLiteQueryBuilder sQLiteQueryBuilder3 = queryBuilder2;
            return getDevicePushToken();
        } else if (match == 74) {
            String str3 = newSelection6;
            SQLiteDatabase db5 = db2;
            SQLiteQueryBuilder queryBuilder5 = queryBuilder2;
            queryBuilder5.setTables(GCM_TOKENS_TABLE);
            SQLiteQueryBuilder sQLiteQueryBuilder4 = queryBuilder5;
            return queryBuilder5.query(db5, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
        } else if (match == 77) {
            String newSelection7 = newSelection6;
            SQLiteDatabase db6 = db2;
            SQLiteQueryBuilder queryBuilder6 = queryBuilder2;
            queryBuilder6.setTables(TABLE_JOIN_FOR_ALL_LINES);
            queryBuilder6.setProjectionMap(sLineWithServicesProjectionMap);
            if (TextUtils.isEmpty(selection) || !str.startsWith("_id")) {
                newSelection3 = newSelection7;
            } else {
                newSelection3 = str.replace("_id", LinesColumns.CONCRETE_ID);
            }
            SQLiteQueryBuilder sQLiteQueryBuilder5 = queryBuilder6;
            return queryBuilder6.query(db6, projection, newSelection3, selectionArgs, (String) null, (String) null, sortOrder);
        } else if (match == 39) {
            SQLiteDatabase sQLiteDatabase2 = db2;
            SQLiteQueryBuilder sQLiteQueryBuilder6 = queryBuilder2;
            return this.mContext.getContentResolver().query(EntitlementConfigContract.DeviceConfig.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        } else if (match == 40) {
            SQLiteDatabase db7 = db2;
            SQLiteQueryBuilder queryBuilder7 = queryBuilder2;
            queryBuilder7.setTables(NSDS_CONFIG_TABLE);
            SQLiteQueryBuilder sQLiteQueryBuilder7 = queryBuilder7;
            return queryBuilder7.query(db7, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
        } else if (match != 62) {
            newSelection = newSelection6;
            if (match != 63) {
                String str4 = "services";
                switch (match) {
                    case 6:
                        SQLiteDatabase db8 = db2;
                        SQLiteQueryBuilder queryBuilder8 = queryBuilder2;
                        queryBuilder8.setTables(str4);
                        queryBuilder8.appendWhere("device_id = " + uri.getPathSegments().get(1) + " AND " + "line_id" + " = " + uri.getPathSegments().get(3));
                        SQLiteQueryBuilder sQLiteQueryBuilder8 = queryBuilder8;
                        String str5 = newSelection;
                        return queryBuilder8.query(db8, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
                    case 7:
                        SQLiteQueryBuilder queryBuilder9 = queryBuilder2;
                        queryBuilder9.setTables(ACCOUNTS_TABLE);
                        queryBuilder9.appendWhere(SQL_WHERE_ACTIVE_ACCOUNT);
                        SQLiteQueryBuilder sQLiteQueryBuilder9 = queryBuilder9;
                        String str6 = newSelection;
                        return queryBuilder9.query(db2, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
                    case 8:
                        queryBuilder2.setTables(TABLE_JOIN_LINES_SERVICES);
                        queryBuilder2.appendWhere(SQL_WHERE_LINE_STATUS_ACTIVE);
                        String[] selectionArgs2 = insertSelectionArg(insertSelectionArg(strArr, "1"), getDeviceUidFromQueryParam(uri));
                        if (TextUtils.isEmpty(selection) || !str.startsWith("_id")) {
                            newSelection4 = newSelection;
                        } else {
                            newSelection4 = str.replace("_id", LinesColumns.CONCRETE_ID);
                        }
                        SQLiteDatabase sQLiteDatabase3 = db2;
                        String[] strArr2 = selectionArgs2;
                        String str7 = newSelection4;
                        SQLiteQueryBuilder sQLiteQueryBuilder10 = queryBuilder2;
                        return queryBuilder2.query(db2, projection, newSelection4, selectionArgs2, (String) null, (String) null, sortOrder);
                    case 9:
                        queryBuilder2.setTables(ACCOUNTS_TABLE);
                        SQLiteQueryBuilder sQLiteQueryBuilder11 = queryBuilder2;
                        SQLiteDatabase sQLiteDatabase4 = db2;
                        String str8 = newSelection;
                        return queryBuilder2.query(db2, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
                    default:
                        switch (match) {
                            case 43:
                                queryBuilder2.setTables(DEVICES_TABLE);
                                queryBuilder2.appendWhere(SQL_WHERE_DEVICES_FOR_LINE_ID);
                                SQLiteQueryBuilder sQLiteQueryBuilder12 = queryBuilder2;
                                SQLiteDatabase sQLiteDatabase5 = db2;
                                String str9 = newSelection;
                                return queryBuilder2.query(db2, projection, selection, insertSelectionArg(strArr, uri.getPathSegments().get(1)), (String) null, (String) null, sortOrder);
                            case 44:
                                db = db2;
                                queryBuilder = queryBuilder2;
                                break;
                            case 45:
                                queryBuilder2.setTables(TABLE_JOIN_FOR_ALL_LINES);
                                queryBuilder2.appendWhere(SQL_WHERE_ALL_LINES);
                                queryBuilder2.setProjectionMap(sLineWithServicesProjectionMap);
                                if (TextUtils.isEmpty(selection) || !str.startsWith("_id")) {
                                    newSelection5 = newSelection;
                                } else {
                                    newSelection5 = str.replace("_id", LinesColumns.CONCRETE_ID);
                                }
                                SQLiteQueryBuilder sQLiteQueryBuilder13 = queryBuilder2;
                                SQLiteDatabase sQLiteDatabase6 = db2;
                                return queryBuilder2.query(db2, projection, newSelection5, selectionArgs, (String) null, (String) null, sortOrder);
                            default:
                                switch (match) {
                                    case 70:
                                        queryBuilder2.setTables(TABLE_JOIN_LINES_SERVICES);
                                        queryBuilder2.appendWhere(SQL_WHERE_LOCAL_LINES_WITH_SERVICES);
                                        queryBuilder2.setProjectionMap(sLineWithServicesProjectionMap);
                                        SQLiteQueryBuilder sQLiteQueryBuilder14 = queryBuilder2;
                                        SQLiteDatabase sQLiteDatabase7 = db2;
                                        String str10 = newSelection;
                                        return queryBuilder2.query(db2, projection, selection, insertSelectionArg(insertSelectionArg(strArr, "0"), getDeviceUidFromQueryParam(uri)), (String) null, (String) null, sortOrder);
                                    case 71:
                                        queryBuilder2.setTables(SIM_SWAP_NSDS_CONFIG_TABLE);
                                        SQLiteQueryBuilder sQLiteQueryBuilder15 = queryBuilder2;
                                        SQLiteDatabase sQLiteDatabase8 = db2;
                                        String str11 = newSelection;
                                        return queryBuilder2.query(db2, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
                                    case 72:
                                        queryBuilder2.setTables(SIM_SWAP_SERVICES_TABLE);
                                        SQLiteQueryBuilder sQLiteQueryBuilder16 = queryBuilder2;
                                        SQLiteDatabase sQLiteDatabase9 = db2;
                                        String str12 = newSelection;
                                        return queryBuilder2.query(db2, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
                                    default:
                                        SQLiteQueryBuilder sQLiteQueryBuilder17 = queryBuilder2;
                                        SQLiteDatabase sQLiteDatabase10 = db2;
                                        String str13 = newSelection;
                                        return queryInternalWithService(db2, uri2);
                                }
                        }
                }
            } else {
                SQLiteDatabase db9 = db2;
                SQLiteQueryBuilder queryBuilder10 = queryBuilder2;
                queryBuilder10.setTables("services");
                SQLiteQueryBuilder sQLiteQueryBuilder18 = queryBuilder10;
                String str14 = newSelection;
                return queryBuilder10.query(db9, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
            }
        } else {
            SQLiteDatabase sQLiteDatabase11 = db2;
            SQLiteQueryBuilder sQLiteQueryBuilder19 = queryBuilder2;
            return getDeviceConfigElement(uri);
        }
        queryBuilder.setTables(TABLE_JOIN_LINES_SERVICES);
        queryBuilder.appendWhere(SQL_WHERE_LINES_FOR_ACTIVE_ACCOUNT);
        queryBuilder.setProjectionMap(sLineWithServicesProjectionMap);
        String[] selectionArgs3 = insertSelectionArg(strArr, getDeviceUidFromQueryParam(uri));
        if (TextUtils.isEmpty(selection) || !str.startsWith("_id")) {
            newSelection2 = newSelection;
        } else {
            newSelection2 = str.replace("_id", LinesColumns.CONCRETE_ID);
        }
        SQLiteQueryBuilder sQLiteQueryBuilder20 = queryBuilder;
        String[] strArr3 = selectionArgs3;
        String str15 = newSelection2;
        return queryBuilder.query(db, projection, newSelection2, selectionArgs3, (String) null, (String) null, sortOrder);
    }

    private String getDeviceUidFromQueryParam(Uri uri) {
        String deviceUid = uri.getQueryParameter("device_uid");
        if (TextUtils.isEmpty(deviceUid)) {
            deviceUid = DeviceIdHelper.getDeviceIdIfExists(this.mContext, 0);
        }
        if (TextUtils.isEmpty(deviceUid)) {
            return "dummy.txt.txt";
        }
        return deviceUid;
    }

    private Cursor queryInternalWithService(SQLiteDatabase db, Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "queryInternalWithService: uri:" + uri);
        if (this.mNsdsService == null) {
            IMSLog.e(LOG_TAG, "query: NSDS service is not connected");
            return null;
        }
        int slotid = 0;
        String slotStr = uri.getQueryParameter(NSDSContractExt.QueryParams.SLOT_ID);
        if (!TextUtils.isEmpty(slotStr)) {
            slotid = Integer.parseInt(slotStr);
        }
        int match = sUriMatcher.match(uri);
        if (match == 28) {
            return getDeviceOwnActivationStatus(slotid);
        }
        if (match != 60) {
            return null;
        }
        return getDeviceOwnReadyStatus(slotid);
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String str = LOG_TAG;
        IMSLog.s(str, "update " + uri);
        int numRows = 0;
        if (sUriMatcher.match(uri) == 82) {
            IMSLog.i(LOG_TAG, "Binding to NSDSMultiSimService");
            connectToNSDSMultiSimService();
            return 0;
        } else if (sUriMatcher.match(uri) == 81) {
            IMSLog.e(LOG_TAG, "Reconnect DB for DatabaseHelper null");
            if (this.mDatabaseHelper != null) {
                IMSLog.i(LOG_TAG, "Reconnect DB after closing the previous DB");
                this.mDatabaseHelper.close();
            }
            this.mDatabaseHelper = new DatabaseHelper(this.mContext);
            return 0;
        } else if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return 0;
        } else {
            SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
            if (sUriMatcher.match(uri) == 0) {
                db.beginTransactionNonExclusive();
            } else {
                db.beginTransaction();
            }
            try {
                new StringBuilder();
                int match = sUriMatcher.match(uri);
                if (match == 0) {
                    numRows = db.update(LINES_TABLE, values, selection, selectionArgs);
                    if (numRows <= 0) {
                        IMSLog.i(LOG_TAG, "Updating lines failed");
                    }
                } else if (match == 2) {
                    numRows = db.update(DEVICES_TABLE, values, selection, selectionArgs);
                } else if (match == 6) {
                    numRows = db.update("services", values, "device_id = ? AND line_id = ?", new String[]{uri.getPathSegments().get(1), uri.getPathSegments().get(3)});
                    if (numRows > 0) {
                        String str2 = LOG_TAG;
                        IMSLog.i(str2, "Updated Services table for deviceId " + uri.getPathSegments().get(1) + " and lineId :" + uri.getPathSegments().get(3));
                    }
                } else if (match == 9) {
                    numRows = db.update(ACCOUNTS_TABLE, values, selection, selectionArgs);
                } else if (match == 26) {
                    numRows = setDevicePrimary(db, uri);
                } else if (match == 74) {
                    numRows = db.update(GCM_TOKENS_TABLE, values, selection, selectionArgs);
                } else if (match == 39) {
                    numRows = updateDeviceConfig(values);
                } else if (match != 40) {
                    switch (match) {
                        case 47:
                            StringBuilder sb1 = new StringBuilder();
                            sb1.append("_id = ?");
                            numRows = db.update(ACCOUNTS_TABLE, values, appendSelection(sb1, selection), insertSelectionArg(selectionArgs, uri.getPathSegments().get(1)));
                            if (numRows <= 0) {
                                IMSLog.i(LOG_TAG, "Updating the account failed");
                                break;
                            }
                            break;
                        case 48:
                            numRows = disableActiveAccount(db, uri.getQueryParameter("account_id"));
                            break;
                        case 49:
                            numRows = updateCabStatus(db, uri, 1);
                            break;
                        case 50:
                            numRows = updateCabStatus(db, uri, 0);
                            break;
                        default:
                            numRows = updateInternalWithService(uri);
                            break;
                    }
                }
                db.setTransactionSuccessful();
            } catch (SQLiteException sqe) {
                String str3 = LOG_TAG;
                IMSLog.s(str3, "Could not update table:" + sqe.getMessage());
            } catch (Throwable th) {
                db.endTransaction();
                throw th;
            }
            db.endTransaction();
            if (numRows != 0) {
                notifyChange(uri);
            }
            return numRows;
        }
    }

    private int updateInternalWithService(Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "updateInternalWithService: uri " + uri);
        if (this.mNsdsService == null) {
            IMSLog.e(LOG_TAG, "update: NSDS service is not connected");
            return 0;
        }
        int slotId = 0;
        String slotStr = uri.getQueryParameter(NSDSContractExt.QueryParams.SLOT_ID);
        if (!TextUtils.isEmpty(slotStr)) {
            slotId = Integer.parseInt(slotStr);
        }
        if (NSDSSharedPrefHelper.isSimSwapPending(this.mContext, DeviceIdHelper.getDeviceIdIfExists(this.mContext, slotId))) {
            IMSLog.e(LOG_TAG, "SimSwap process is in progress. Ignore operations now");
            return 0;
        }
        int match = sUriMatcher.match(uri);
        if (match == 46) {
            updateE911Address(slotId);
            return 0;
        } else if (match == 73) {
            updateEntitlementUrl(uri);
            return 0;
        } else if (match != 80) {
            switch (match) {
                case 17:
                    addServicesToLine(uri);
                    return 0;
                case 18:
                    return removeServicesFromLine(uri);
                case 19:
                    return updateServicesStatusForLine(uri, true);
                case 20:
                    return updateServicesStatusForLine(uri, false);
                default:
                    switch (match) {
                        case 30:
                            activateSimDevice(slotId);
                            return 0;
                        case 31:
                            deactivateSimDevice(uri);
                            return 0;
                        case 32:
                            handleVoWiFiToggleOnEvent(slotId);
                            return 0;
                        case 33:
                            handleVoWiFiToggleOffEvent(slotId);
                            return 0;
                        default:
                            return 0;
                    }
            }
        } else {
            retrieveAkaToken(uri);
            return 0;
        }
    }

    private int disableActiveAccount(SQLiteDatabase db, String queryParamAccountId) {
        disableLinesAndServices(db);
        ContentValues values = new ContentValues();
        values.put(NSDSContractExt.AccountColumns.IS_ACTIVE, 0);
        int numRows = db.update(ACCOUNTS_TABLE, values, "is_active = ?  AND _id = ?", new String[]{"1", queryParamAccountId});
        if (numRows <= 0) {
            IMSLog.i(LOG_TAG, "disabling the account failed");
        }
        return numRows;
    }

    private void disableLinesAndServices(SQLiteDatabase db) {
        ContentValues lineValues = new ContentValues();
        lineValues.put("status", "0");
        int numLinesUpdated = db.update(LINES_TABLE, lineValues, (String) null, (String[]) null);
        String str = LOG_TAG;
        IMSLog.i(str, "disableLinesAndServices: de-activated :" + numLinesUpdated + " lines for logout");
        if (numLinesUpdated > 0) {
            notifyChange(NSDSContractExt.Lines.CONTENT_URI);
        }
        IMSLog.i(LOG_TAG, "disableLinesAndServices: de-activated lines for logout");
        db.delete("services", (String) null, (String[]) null);
    }

    private Cursor getDeviceOwnActivationStatus(int slotid) {
        String deviceState;
        String deviceUid = DeviceIdHelper.getDeviceIdIfExists(this.mContext, slotid);
        boolean pendingSimSwap = NSDSSharedPrefHelper.isSimSwapPending(this.mContext, deviceUid);
        if (pendingSimSwap) {
            deviceState = NSDSNamespaces.NSDSDeviceState.ACTIVATION_IN_PROGRESS;
        } else {
            deviceState = NSDSSharedPrefHelper.get(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE);
        }
        String str = LOG_TAG;
        IMSLog.i(str, "getDeviceState: onSimSwapEvt " + pendingSimSwap + " state " + deviceState);
        MatrixCursor cursor = new MatrixCursor(new String[]{NSDSContractExt.Devices.OWN_ACTIVATION_STATUS});
        cursor.newRow().add(deviceState);
        return cursor;
    }

    private Cursor getDeviceOwnReadyStatus(int slotid) {
        String deviceUid = DeviceIdHelper.getDeviceIdIfExists(this.mContext, slotid);
        boolean readyStatus = true;
        if (NSDSSharedPrefHelper.isSimSwapPending(this.mContext, deviceUid)) {
            readyStatus = false;
        }
        if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, deviceUid) || NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, deviceUid)) {
            activateSimDevice(slotid);
            readyStatus = false;
        }
        String str = LOG_TAG;
        IMSLog.s(str, "own_ready_status:" + readyStatus);
        MatrixCursor cursor = new MatrixCursor(new String[]{NSDSContractExt.Devices.OWN_READY_STATUS});
        cursor.newRow().add(Boolean.valueOf(readyStatus));
        return cursor;
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    private Cursor getDeviceConfigElement(Uri uri) {
        Cursor dbCursor;
        String deviceConfigXml = null;
        String elementName = null;
        try {
            elementName = uri.getQueryParameter("tag_name");
            if (TextUtils.isEmpty(elementName)) {
                IMSLog.e(LOG_TAG, "Empty tag name. Return null");
                return null;
            }
            dbCursor = this.mContext.getContentResolver().query(EntitlementConfigContract.DeviceConfig.CONTENT_URI, new String[]{"device_config"}, (String) null, (String[]) null, (String) null);
            if (dbCursor != null) {
                if (dbCursor.moveToFirst()) {
                    deviceConfigXml = dbCursor.getString(0);
                }
            }
            if (dbCursor != null) {
                dbCursor.close();
            }
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"element_name", "element_value"});
            if (deviceConfigXml != null) {
                Map<String, String> mapElements = ConfigElementExtractor.getAllElements(deviceConfigXml, elementName);
                for (String key : mapElements.keySet()) {
                    matrixCursor.addRow(new String[]{key, mapElements.get(key)});
                }
            } else {
                IMSLog.e(LOG_TAG, "Device Config is null: ");
            }
            return matrixCursor;
        } catch (Exception sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "SQL exception while parseDeviceConfig " + sqe.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    private void updateE911Address(int slotid) {
        try {
            Message msg = new Message();
            msg.what = 19;
            Bundle data = new Bundle();
            data.putInt("SLOT_ID", slotid);
            msg.setData(data);
            this.mNsdsService.send(msg);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "openLoginPage: failed to open login page" + e.getMessage());
        }
    }

    private void activateSimDevice(int slotid) {
        Context context = this.mContext;
        NSDSSharedPrefHelper.save(context, DeviceIdHelper.getDeviceIdIfExists(context, 0), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
        try {
            Message msg = Message.obtain();
            msg.what = 3;
            Bundle data = new Bundle();
            data.putInt("SLOT_ID", slotid);
            data.putInt("EVENT_TYPE", 11);
            data.putInt("RETRY_COUNT", 0);
            msg.setData(data);
            this.mNsdsService.send(msg);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "activateSIMDevice: failed to activate" + e.getMessage());
        }
    }

    private void deactivateSimDevice(Uri uri) {
        NSDSSharedPrefHelper.save(this.mContext, DeviceIdHelper.getDeviceIdIfExists(this.mContext, 0), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
        try {
            String imsi = uri.getQueryParameter("imsi");
            Message msg = new Message();
            msg.what = 4;
            Bundle data = new Bundle();
            data.putString("IMSI", imsi);
            msg.setData(data);
            this.mNsdsService.send(msg);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "openLoginPage: failed to open login page" + e.getMessage());
        }
    }

    private void handleVoWiFiToggleOnEvent(int slotid) {
        NSDSSharedPrefHelper.save(this.mContext, DeviceIdHelper.getDeviceIdIfExists(this.mContext, 0), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
        try {
            Message msg = new Message();
            msg.what = 220;
            Bundle data = new Bundle();
            data.putInt("SLOT_ID", slotid);
            msg.setData(data);
            this.mNsdsService.send(msg);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "handleVoWiFiToggleOnEvent: failed to toggle on" + e.getMessage());
        }
    }

    private void handleVoWiFiToggleOffEvent(int slotid) {
        NSDSSharedPrefHelper.save(this.mContext, DeviceIdHelper.getDeviceIdIfExists(this.mContext, 0), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
        try {
            Message msg = new Message();
            msg.what = 221;
            Bundle data = new Bundle();
            data.putInt("SLOT_ID", slotid);
            msg.setData(data);
            this.mNsdsService.send(msg);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "handleVoWiFiToggleOffEvent: failed to toggle off" + e.getMessage());
        }
    }

    private int setDevicePrimary(SQLiteDatabase db, Uri uri) {
        ContentValues values = new ContentValues();
        String isPrimary = uri.getQueryParameter("is_primary");
        if (TextUtils.isEmpty(isPrimary)) {
            IMSLog.i(LOG_TAG, "Can not update isPrimary since Query parameter:is_primary is null or empty");
        }
        values.put("is_primary", isPrimary);
        int numRows = db.update(DEVICES_TABLE, values, "_id = ?", new String[]{uri.getPathSegments().get(1)});
        if (numRows == 1) {
            IMSLog.i(LOG_TAG, "setDevicePrimary is successful:");
            broadcastPrimaryDeviceSettingChanged(isPrimary);
        }
        return numRows;
    }

    private void updateEntitlementUrl(Uri uri) {
        String url = uri.getQueryParameter("entitlement_url");
        String imsi = uri.getQueryParameter("imsi");
        try {
            Message msg = new Message();
            msg.what = 212;
            Bundle bundle = new Bundle();
            bundle.putString("URL", url);
            if (!TextUtils.isEmpty(imsi)) {
                bundle.putString("IMSI", imsi);
            }
            msg.setData(bundle);
            this.mNsdsService.send(msg);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "updateEntitlementUrl: failed to request" + e.getMessage());
        }
    }

    private void retrieveAkaToken(Uri uri) {
        try {
            String imsi = uri.getQueryParameter("imsi");
            Message msg = Message.obtain();
            msg.what = 49;
            Bundle data = new Bundle();
            data.putString("IMSI", imsi);
            data.putInt("EVENT_TYPE", 19);
            data.putInt("RETRY_COUNT", 0);
            msg.setData(data);
            this.mNsdsService.send(msg);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "retrieveAkaToken: failed to retrieve aka" + e.getMessage());
        }
    }

    public Cursor getDevicePushToken() {
        String token = PushTokenHelper.getPushToken(getContext(), DeviceIdHelper.getDeviceIdIfExists(this.mContext, 0));
        MatrixCursor cursor = new MatrixCursor(new String[]{"device_push_token"});
        cursor.newRow().add(token);
        String str = LOG_TAG;
        IMSLog.s(str, "getDevicePushToken: " + token);
        return cursor;
    }

    private void broadcastPrimaryDeviceSettingChanged(String status) {
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.IS_PRIMARY_ACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.IS_PRIMARY_DEVICE, status != null && Integer.valueOf(status).intValue() > 0);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private int updateCabStatus(SQLiteDatabase db, Uri uri, int status) {
        ContentValues values = new ContentValues();
        values.put(NSDSContractExt.LineColumns.CAB_STATUS, Integer.valueOf(status));
        String lineId = uri.getPathSegments().get(1);
        int numRows = db.update(LINES_TABLE, values, SQL_WHERE_LINE_ID, new String[]{lineId});
        if (numRows > 0) {
            String str = LOG_TAG;
            IMSLog.i(str, "updateCabStatus: cab status successfully updated for lineId :" + lineId + " to :" + status);
        }
        return numRows;
    }

    private long addServicesToLine(Uri uri) {
        long totalInserts = 0;
        Long deviceId = Long.valueOf(uri.getPathSegments().get(1));
        Long lineId = Long.valueOf(uri.getPathSegments().get(3));
        String strServiceNames = uri.getQueryParameter(NSDSContractExt.Lines.QUERY_PARAM_SERVICE_NAMES);
        for (String serviceName : strServiceNames.split(",")) {
            ContentValues values = new ContentValues();
            values.put("device_id", deviceId);
            values.put("line_id", lineId);
            values.put("service_name", serviceName);
            totalInserts += insertIntoServices(values);
        }
        if (totalInserts == 0) {
            IMSLog.i(LOG_TAG, "Could not add services:" + strServiceNames + " to line Id" + lineId);
        }
        return totalInserts;
    }

    private int removeServicesFromLine(Uri uri) {
        String lineId = uri.getPathSegments().get(3);
        int totalDeletes = deleteFromServices(uri.getPathSegments().get(1), lineId);
        if (totalDeletes == 0) {
            String str = LOG_TAG;
            IMSLog.e(str, "Could not delete services for device Id" + lineId);
        }
        return totalDeletes;
    }

    private int updateServicesStatusForLine(Uri uri, boolean activate) {
        int status = 0;
        if (activate) {
            status = 1;
        }
        int totalUpdates = 0;
        String deviceId = uri.getPathSegments().get(1);
        String lineId = uri.getPathSegments().get(3);
        String strServiceIds = uri.getQueryParameter(NSDSContractExt.Lines.QUERY_PARAM_SERVICE_IDS);
        for (String serviceId : strServiceIds.split(" ")) {
            totalUpdates += updateStatusInServices(deviceId, lineId, serviceId, status);
        }
        if (totalUpdates == 0) {
            IMSLog.e(LOG_TAG, "Could not add services:" + strServiceIds + " to line Id" + lineId);
        }
        return totalUpdates;
    }

    private int updateStatusInServices(String deviceId, String lineId, String serviceId, int status) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        int noRows = 0;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(NSDSContractExt.ServiceColumns.SERVICE_STATUS, Integer.valueOf(status));
            noRows = db.update("services", values, "device_id= ? AND line_id= ? AND _id = ?", new String[]{deviceId, lineId, serviceId});
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "updateStatusInServices: Could not update Services table:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return noRows;
    }

    private String appendSelection(StringBuilder sb, String selection) {
        if (!TextUtils.isEmpty(selection)) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }

    private String[] insertSelectionArg(String[] selectionArgs, String arg) {
        if (selectionArgs == null) {
            return new String[]{arg};
        }
        String[] newSelectionArgs = new String[(selectionArgs.length + 1)];
        newSelectionArgs[0] = arg;
        System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
        return newSelectionArgs;
    }

    private String getResourceName(Resources resources, String expectedType, Integer resourceId) {
        if (resourceId != null) {
            try {
                if (resourceId.intValue() != 0) {
                    String resourceEntryName = resources.getResourceEntryName(resourceId.intValue());
                    String resourceTypeName = resources.getResourceTypeName(resourceId.intValue());
                    if (expectedType.equals(resourceTypeName)) {
                        return resourceEntryName;
                    }
                    String str = LOG_TAG;
                    IMSLog.e(str, "Resource " + resourceId + " (" + resourceEntryName + ") is of type " + resourceTypeName + " but " + expectedType + " is required.");
                    return null;
                }
            } catch (Resources.NotFoundException e) {
                return null;
            }
        }
        return null;
    }

    /* Debug info: failed to restart local var, previous not found, register: 12 */
    public int bulkInsert(Uri uri, ContentValues[] values) {
        DatabaseHelper databaseHelper;
        String table = null;
        int uriType = sUriMatcher.match(uri);
        String imsi = uri.getQueryParameter("imsi");
        if (uriType == 40) {
            table = NSDS_CONFIG_TABLE;
        } else if (uriType != 71) {
            String str = LOG_TAG;
            IMSLog.i(str, "None of the Uri's match for bulkInsert:" + uri);
        } else {
            table = SIM_SWAP_NSDS_CONFIG_TABLE;
        }
        if (!MigrationHelper.checkMigrateDB(this.mContext)) {
            IMSLog.s(LOG_TAG, "ignoring nsds_config inserts since migration is not done yet");
            return -1;
        } else if (table == null || (databaseHelper = this.mDatabaseHelper) == null) {
            return 0;
        } else {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            if (table == SIM_SWAP_NSDS_CONFIG_TABLE) {
                try {
                    db.execSQL("DELETE FROM sim_swap_nsds_configs");
                    if (TextUtils.isEmpty(imsi)) {
                        db.execSQL("INSERT INTO " + table + " SELECT * from nsds_configs");
                    } else {
                        db.execSQL("INSERT INTO " + table + " SELECT * from nsds_configs WHERE IMSI = '" + imsi + "'");
                    }
                    db.execSQL("DELETE FROM sim_swap_services");
                    db.execSQL("INSERT INTO sim_swap_services SELECT * from services");
                } catch (Throwable th) {
                    db.endTransaction();
                    throw th;
                }
            }
            int length = values.length;
            int i = 0;
            while (i < length) {
                if (db.insertOrThrow(table, (String) null, values[i]) > 0) {
                    i++;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            db.setTransactionSuccessful();
            int numInserted = values.length;
            db.endTransaction();
            notifyChange(uri);
            return numInserted;
        }
    }
}
