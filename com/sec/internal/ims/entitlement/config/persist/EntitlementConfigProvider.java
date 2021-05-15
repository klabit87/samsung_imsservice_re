package com.sec.internal.ims.entitlement.config.persist;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
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
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.ims.entitlement.storagehelper.MigrationHelper;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class EntitlementConfigProvider extends ContentProvider {
    private static final String CREATE_DEVICE_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS entitlement_config(_id INTEGER PRIMARY KEY AUTOINCREMENT,version TEXT, imsi TEXT NOT NULL, device_config TEXT,backup_version TEXT,validity TEXT,next_config_time TEXT,token TEXT,completed TEXT,tc_popup_user_accept TEXT);";
    private static final String DATABASE_NAME = "entitlement_config.db";
    private static final int DATABASE_VERSION = 1;
    private static final int DEFAULT_SIM_SLOT_IDX = 0;
    private static final String DEVICE_CONFIG_TABLE = "entitlement_config";
    private static final long ENTITLEMENT_FORCE_UPDATE_EXPIRATION_TIME = 300000;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = EntitlementConfigProvider.class.getSimpleName();
    private static final String PROVIDER_NAME = "com.samsung.ims.entitlementconfig.provider";
    private static final UriMatcher sUriMatcher;
    private Date configUpdateDate = null;
    protected Context mContext = null;
    /* access modifiers changed from: private */
    public DatabaseHelper mDatabaseHelper = null;
    protected Messenger mService;
    private ServiceConnection mSvcConn;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config", 1);
        sUriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/xpath", 3);
        sUriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/jansky_config", 2);
        sUriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/rcs_config", 4);
        sUriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/force_update", 5);
        sUriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/entitlement_url", 6);
        sUriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "reconnect_db", 7);
        sUriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "binding_service", 8);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, EntitlementConfigProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            IMSLog.i(EntitlementConfigProvider.LOG_TAG, "DatabaseHelper onCreate()");
            db.execSQL(EntitlementConfigProvider.CREATE_DEVICE_CONFIG_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String access$000 = EntitlementConfigProvider.LOG_TAG;
            IMSLog.i(access$000, "db downgrade: oldVersion=" + oldVersion + " newVersion=" + newVersion);
            onCreate(db);
            db.setVersion(newVersion);
        }
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
        try {
            if (sUriMatcher.match(uri) == 1) {
                numRows = db.delete(DEVICE_CONFIG_TABLE, selection, selectionArgs);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException sqe) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "Could not delete:" + sqe.getMessage());
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return numRows;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        String str = LOG_TAG;
        IMSLog.s(str, "insert:" + uri);
        if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return null;
        }
        Uri retUri = null;
        if (sUriMatcher.match(uri) == 1) {
            retUri = EntitlementConfigContract.DeviceConfig.buildDeviceConfigUri(insertDeviceConfig(values));
        }
        if (retUri != null) {
            notifyChange(uri);
        }
        return retUri;
    }

    public void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
    }

    private long insertDeviceConfig(ContentValues values) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        long rowId = -1;
        try {
            rowId = db.insert(DEVICE_CONFIG_TABLE, (String) null, values);
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
            numRows = db.update(DEVICE_CONFIG_TABLE, values, (String) null, (String[]) null);
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

    public void forceConfigUpdate() {
        IMSLog.i(LOG_TAG, "forceConfigUpdate()");
        if (this.configUpdateDate == null || new Date().getTime() - this.configUpdateDate.getTime() > ENTITLEMENT_FORCE_UPDATE_EXPIRATION_TIME) {
            try {
                this.configUpdateDate = new Date(System.currentTimeMillis());
                Message msg = new Message();
                msg.what = 108;
                msg.obj = 0;
                this.mService.send(msg);
            } catch (Exception re) {
                String str = LOG_TAG;
                IMSLog.s(str, "Could not force update config" + re.getMessage());
            }
        }
    }

    private void updateEntitlementUrl(Uri uri) {
        String url = uri.getQueryParameter("entitlement_url");
        try {
            Message msg = new Message();
            msg.what = 201;
            Bundle bundle = new Bundle();
            bundle.putString("URL", url);
            msg.setData(bundle);
            this.mService.send(msg);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "updateEntitlementUrl: failed to request" + e.getMessage());
        }
    }

    public boolean onCreate() {
        this.mContext = getContext().createCredentialProtectedStorageContext();
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri uri2 = uri;
        String str = LOG_TAG;
        IMSLog.s(str, "query " + uri);
        if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return null;
        }
        SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            queryBuilder.setTables(DEVICE_CONFIG_TABLE);
            return queryBuilder.query(db, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
        } else if (match == 2) {
            return getJanskyConfigXmlBlock();
        } else {
            if (match == 3) {
                return getNsdsElementsWithXPath(uri);
            }
            if (match != 4) {
                return null;
            }
            return getRcsConfigXmlBlock();
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String str = LOG_TAG;
        IMSLog.s(str, "update " + uri);
        int numRows = 0;
        if (sUriMatcher.match(uri) == 8) {
            IMSLog.i(LOG_TAG, "Binding to EntitlementConfigService");
            connectToEntitlementConfigService();
            return 0;
        } else if (sUriMatcher.match(uri) == 7) {
            IMSLog.e(LOG_TAG, "Reconnect DB for DatabaseHelper");
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
            db.beginTransaction();
            try {
                int match = sUriMatcher.match(uri);
                if (match == 1) {
                    numRows = updateDeviceConfig(values);
                } else if (match == 5) {
                    forceConfigUpdate();
                } else if (match == 6) {
                    updateEntitlementUrl(uri);
                }
                db.setTransactionSuccessful();
            } catch (SQLiteException sqe) {
                String str2 = LOG_TAG;
                IMSLog.s(str2, "Could not update table:" + sqe.getMessage());
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

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0045, code lost:
        if (r0 != null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0047, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0068, code lost:
        if (r0 == null) goto L_0x006b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006b, code lost:
        r4 = new android.database.MatrixCursor(new java.lang.String[]{"element_name", "element_value"});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0078, code lost:
        if (r1 == null) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007a, code lost:
        r5 = com.sec.internal.ims.entitlement.util.ConfigElementExtractor.getAllElements(r1, r2);
        r6 = r5.keySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008a, code lost:
        if (r6.hasNext() == false) goto L_0x00ac;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008c, code lost:
        r7 = r6.next();
        r4.addRow(new java.lang.String[]{r7, r5.get(r7)});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a5, code lost:
        com.sec.internal.log.IMSLog.e(LOG_TAG, "Device Config is null: ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00ac, code lost:
        return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.Cursor getNsdsElementsWithXPath(android.net.Uri r12) {
        /*
            r11 = this;
            r0 = 0
            r1 = 0
            r2 = 0
            r3 = 0
            java.lang.String r4 = "tag_name"
            java.lang.String r4 = r12.getQueryParameter(r4)     // Catch:{ Exception -> 0x004d }
            r2 = r4
            boolean r4 = android.text.TextUtils.isEmpty(r2)     // Catch:{ Exception -> 0x004d }
            if (r4 == 0) goto L_0x0020
            java.lang.String r4 = LOG_TAG     // Catch:{ Exception -> 0x004d }
            java.lang.String r5 = "Empty tag name. Return null"
            com.sec.internal.log.IMSLog.i(r4, r5)     // Catch:{ Exception -> 0x004d }
            r3 = 0
            if (r0 == 0) goto L_0x001f
            r0.close()
        L_0x001f:
            return r3
        L_0x0020:
            java.lang.String r4 = "device_config"
            java.lang.String[] r7 = new java.lang.String[]{r4}     // Catch:{ Exception -> 0x004d }
            android.content.Context r4 = r11.getContext()     // Catch:{ Exception -> 0x004d }
            android.content.ContentResolver r5 = r4.getContentResolver()     // Catch:{ Exception -> 0x004d }
            android.net.Uri r6 = com.sec.internal.constants.ims.entitilement.EntitlementConfigContract.DeviceConfig.CONTENT_URI     // Catch:{ Exception -> 0x004d }
            r8 = 0
            r9 = 0
            r10 = 0
            android.database.Cursor r4 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ Exception -> 0x004d }
            r0 = r4
            if (r0 == 0) goto L_0x0045
            boolean r4 = r0.moveToFirst()     // Catch:{ Exception -> 0x004d }
            if (r4 == 0) goto L_0x0045
            java.lang.String r4 = r0.getString(r3)     // Catch:{ Exception -> 0x004d }
            r1 = r4
        L_0x0045:
            if (r0 == 0) goto L_0x006b
        L_0x0047:
            r0.close()
            goto L_0x006b
        L_0x004b:
            r3 = move-exception
            goto L_0x00ad
        L_0x004d:
            r4 = move-exception
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x004b }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x004b }
            r6.<init>()     // Catch:{ all -> 0x004b }
            java.lang.String r7 = "SQL exception while parseDeviceConfig "
            r6.append(r7)     // Catch:{ all -> 0x004b }
            java.lang.String r7 = r4.getMessage()     // Catch:{ all -> 0x004b }
            r6.append(r7)     // Catch:{ all -> 0x004b }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x004b }
            com.sec.internal.log.IMSLog.s(r5, r6)     // Catch:{ all -> 0x004b }
            if (r0 == 0) goto L_0x006b
            goto L_0x0047
        L_0x006b:
            android.database.MatrixCursor r4 = new android.database.MatrixCursor
            java.lang.String r5 = "element_name"
            java.lang.String r6 = "element_value"
            java.lang.String[] r5 = new java.lang.String[]{r5, r6}
            r4.<init>(r5)
            if (r1 == 0) goto L_0x00a5
            java.util.Map r5 = com.sec.internal.ims.entitlement.util.ConfigElementExtractor.getAllElements(r1, r2)
            java.util.Set r6 = r5.keySet()
            java.util.Iterator r6 = r6.iterator()
        L_0x0086:
            boolean r7 = r6.hasNext()
            if (r7 == 0) goto L_0x00a4
            java.lang.Object r7 = r6.next()
            java.lang.String r7 = (java.lang.String) r7
            r8 = 2
            java.lang.String[] r8 = new java.lang.String[r8]
            r8[r3] = r7
            r9 = 1
            java.lang.Object r10 = r5.get(r7)
            java.lang.String r10 = (java.lang.String) r10
            r8[r9] = r10
            r4.addRow(r8)
            goto L_0x0086
        L_0x00a4:
            goto L_0x00ac
        L_0x00a5:
            java.lang.String r3 = LOG_TAG
            java.lang.String r5 = "Device Config is null: "
            com.sec.internal.log.IMSLog.e(r3, r5)
        L_0x00ac:
            return r4
        L_0x00ad:
            if (r0 == 0) goto L_0x00b2
            r0.close()
        L_0x00b2:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.config.persist.EntitlementConfigProvider.getNsdsElementsWithXPath(android.net.Uri):android.database.Cursor");
    }

    private Cursor getJanskyConfigXmlBlock() {
        return getXmlConfigByTag("//janskyConfig");
    }

    private Cursor getRcsConfigXmlBlock() {
        return getXmlConfigByTag("//RCSConfig/wap-provisioningdoc|//wap-provisioningdoc");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004b, code lost:
        if (r0 == null) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004e, code lost:
        r3 = new android.database.MatrixCursor(new java.lang.String[]{com.sec.internal.constants.ims.entitilement.EntitlementConfigContract.DeviceConfig.XML_CONFIG});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005a, code lost:
        if (r1 == null) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005c, code lost:
        r3.addRow(new java.lang.String[]{com.sec.internal.ims.entitlement.util.CompleteXMLBlockExtractor.getXmlBlockForElement(r1, r11)});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0069, code lost:
        com.sec.internal.log.IMSLog.e(LOG_TAG, "Device Config is null: ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0070, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0028, code lost:
        if (r0 != null) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002a, code lost:
        r0.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.Cursor getXmlConfigByTag(java.lang.String r11) {
        /*
            r10 = this;
            r0 = 0
            r1 = 0
            r2 = 0
            java.lang.String r3 = "device_config"
            java.lang.String[] r6 = new java.lang.String[]{r3}     // Catch:{ Exception -> 0x0030 }
            android.content.Context r3 = r10.getContext()     // Catch:{ Exception -> 0x0030 }
            android.content.ContentResolver r4 = r3.getContentResolver()     // Catch:{ Exception -> 0x0030 }
            android.net.Uri r5 = com.sec.internal.constants.ims.entitilement.EntitlementConfigContract.DeviceConfig.CONTENT_URI     // Catch:{ Exception -> 0x0030 }
            r7 = 0
            r8 = 0
            r9 = 0
            android.database.Cursor r3 = r4.query(r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x0030 }
            r0 = r3
            if (r0 == 0) goto L_0x0028
            boolean r3 = r0.moveToFirst()     // Catch:{ Exception -> 0x0030 }
            if (r3 == 0) goto L_0x0028
            java.lang.String r3 = r0.getString(r2)     // Catch:{ Exception -> 0x0030 }
            r1 = r3
        L_0x0028:
            if (r0 == 0) goto L_0x004e
        L_0x002a:
            r0.close()
            goto L_0x004e
        L_0x002e:
            r2 = move-exception
            goto L_0x0071
        L_0x0030:
            r3 = move-exception
            java.lang.String r4 = LOG_TAG     // Catch:{ all -> 0x002e }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x002e }
            r5.<init>()     // Catch:{ all -> 0x002e }
            java.lang.String r6 = "SQL exception while parseDeviceConfig "
            r5.append(r6)     // Catch:{ all -> 0x002e }
            java.lang.String r6 = r3.toString()     // Catch:{ all -> 0x002e }
            r5.append(r6)     // Catch:{ all -> 0x002e }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x002e }
            com.sec.internal.log.IMSLog.s(r4, r5)     // Catch:{ all -> 0x002e }
            if (r0 == 0) goto L_0x004e
            goto L_0x002a
        L_0x004e:
            android.database.MatrixCursor r3 = new android.database.MatrixCursor
            java.lang.String r4 = "xml_config"
            java.lang.String[] r4 = new java.lang.String[]{r4}
            r3.<init>(r4)
            if (r1 == 0) goto L_0x0069
            java.lang.String r4 = com.sec.internal.ims.entitlement.util.CompleteXMLBlockExtractor.getXmlBlockForElement(r1, r11)
            r5 = 1
            java.lang.String[] r5 = new java.lang.String[r5]
            r5[r2] = r4
            r3.addRow(r5)
            goto L_0x0070
        L_0x0069:
            java.lang.String r2 = LOG_TAG
            java.lang.String r4 = "Device Config is null: "
            com.sec.internal.log.IMSLog.e(r2, r4)
        L_0x0070:
            return r3
        L_0x0071:
            if (r0 == 0) goto L_0x0076
            r0.close()
        L_0x0076:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.config.persist.EntitlementConfigProvider.getXmlConfigByTag(java.lang.String):android.database.Cursor");
    }

    private synchronized void connectToEntitlementConfigService() {
        IMSLog.i(LOG_TAG, "connectToEntitlementConfigService()");
        Intent intent = new Intent();
        intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.entitlement.config.EntitlementConfigService");
        this.mSvcConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                IMSLog.i(EntitlementConfigProvider.LOG_TAG, "onServiceConnected: Connected to EntitlementConfigService.");
                if (MigrationHelper.checkMigrateDB(EntitlementConfigProvider.this.mContext)) {
                    IMSLog.i(EntitlementConfigProvider.LOG_TAG, "Connect DB");
                    DatabaseHelper unused = EntitlementConfigProvider.this.mDatabaseHelper = new DatabaseHelper(EntitlementConfigProvider.this.mContext);
                }
                EntitlementConfigProvider.this.mService = new Messenger(service);
            }

            public void onServiceDisconnected(ComponentName name) {
                IMSLog.i(EntitlementConfigProvider.LOG_TAG, "onServiceDisconnected: Disconnected.");
                EntitlementConfigProvider.this.mService = null;
            }
        };
        ContextExt.bindServiceAsUser(getContext(), intent, this.mSvcConn, 1, ContextExt.CURRENT_OR_SELF);
    }
}
