package com.sec.internal.ims.servicemodules.options;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.presence.SocialPresenceStorage;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CapabilityStorage {
    private static final int APPLY_BATCH_MAX_SIZE = 100;
    private static final int APPLY_BATCH_TIMEOUT = 1000;
    private static final String LOG_TAG = "CapabilityStorage";
    private static final String MIMETYPE_RCSE = "vnd.android.cursor.item/rcs_data";
    private CapabilitiesCache mCapabilitiesCache;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final DatabaseHelper mDbHelper;
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog;
    private boolean mIsKor = false;
    private boolean mNeedResetRcsData = false;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    /* access modifiers changed from: private */
    public final SequenceUpdater mUpdater;
    /* access modifiers changed from: private */
    public int mUserId = 0;

    public CapabilityStorage(Context context, CapabilitiesCache capabilitiesCache, int phoneId) {
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 5);
        this.mCapabilitiesCache = capabilitiesCache;
        this.mDbHelper = new DatabaseHelper(this.mContext);
        this.mUpdater = new SequenceUpdater();
        this.mPhoneId = phoneId;
    }

    public int getAmountCapabilities() {
        Cursor cursor = this.mDbHelper.getCursor("capabilities", new String[]{"_id"}, (String) null, (String[]) null);
        int ret = cursor.getCount();
        this.mDbHelper.safeClose(cursor);
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "getAmountCapabilities: Total " + ret + " capabilities records");
        return ret;
    }

    public int getAmountRcsCapabilities() {
        String[] selectionArgs = {Integer.toString(Capabilities.FEATURE_OFFLINE_RCS_USER), Integer.toString(Capabilities.FEATURE_NON_RCS_USER), Integer.toString(Capabilities.FEATURE_NOT_UPDATED)};
        Cursor cursor = this.mDbHelper.getCursor("capabilities", new String[]{"_id"}, "avail_features <> ? AND avail_features <> ? AND avail_features <> ?", selectionArgs);
        int ret = cursor.getCount();
        this.mDbHelper.safeClose(cursor);
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "getAmountRcsCapabilities: " + ret + " RCS capabilities records");
        return ret;
    }

    public void persist() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "persist: start");
        List<ImsUri> urisToUpdate = this.mCapabilitiesCache.getUpdatedUriList();
        List<ImsUri> urisToDelete = this.mCapabilitiesCache.getTrashedUriList();
        List<Capabilities> capListToUpdate = new ArrayList<>();
        try {
            SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
            try {
                db.beginTransaction();
            } catch (SQLiteDatabaseLockedException e) {
                Log.e(LOG_TAG, "persist: SQLiteDatabaseLockedException: " + e.toString());
            } catch (SQLiteException e2) {
                Log.e(LOG_TAG, "persist: SQLiteException: " + e2.toString());
            } catch (SQLException e3) {
                Log.e(LOG_TAG, "persist: SQLException: " + e3.toString());
            }
            if (urisToUpdate != null) {
                try {
                    if (urisToUpdate.size() > 0) {
                        for (ImsUri uri : urisToUpdate) {
                            Capabilities capex = this.mCapabilitiesCache.get(uri);
                            if (capex != null) {
                                capListToUpdate.add(capex);
                            } else {
                                Log.e(LOG_TAG, "persist: not found in cache.");
                            }
                        }
                        update(db, capListToUpdate);
                    }
                } catch (SQLiteDiskIOException e4) {
                    Log.e(LOG_TAG, "persist: SQLiteDiskIOException: " + e4.toString());
                } catch (SQLiteFullException e5) {
                    Log.e(LOG_TAG, "persist: SQLiteFullException: " + e5.toString());
                } catch (SQLException e6) {
                    Log.e(LOG_TAG, "persist: SQLException: " + e6.toString());
                } catch (Throwable th) {
                    endTransaction(db);
                    throw th;
                }
            }
            if (urisToDelete != null && urisToDelete.size() > 0) {
                remove(db, urisToDelete);
            }
            db.setTransactionSuccessful();
            endTransaction(db);
            this.mDbHelper.safeClose(db);
            IMSLog.i(LOG_TAG, this.mPhoneId, "persist: end");
        } catch (SQLiteDiskIOException e7) {
            Log.e(LOG_TAG, "persist: SQLiteDiskIOException: " + e7.toString());
        } catch (SQLiteDatabaseCorruptException e8) {
            Log.e(LOG_TAG, "persist: SQLiteDatabaseCorruptException: " + e8.toString());
        }
    }

    public void reset() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "reset:");
        try {
            SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
            db.delete("capabilities", (String) null, (String[]) null);
            this.mDbHelper.safeClose(db);
            if (this.mIsKor) {
                this.mNeedResetRcsData = true;
            }
        } catch (SQLiteException e) {
            Log.e(LOG_TAG, "reset: SQLiteException: " + e.toString());
        }
    }

    private void remove(SQLiteDatabase db, List<ImsUri> list) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "remove: " + list.size() + " Capabilities");
        for (ImsUri uri : list) {
            db.delete("capabilities", "uri=?", new String[]{uri.toString()});
        }
    }

    private void update(SQLiteDatabase db, List<Capabilities> list) {
        ContentValues values = new ContentValues();
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "update: " + list.size() + " Capabilities");
        for (Capabilities capex : list) {
            values.clear();
            fillCapexInfo(values, capex);
            if (capex.getId() >= 0) {
                db.update("capabilities", values, "_id=" + capex.getId(), (String[]) null);
            } else {
                capex.setId(db.insert("capabilities", (String) null, values));
            }
        }
    }

    private void endTransaction(SQLiteDatabase db) {
        if (db == null) {
            Log.e(LOG_TAG, "endTransaction: db is null");
            return;
        }
        try {
            db.endTransaction();
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "endTransaction: IllegalStateException: " + e.toString());
        } catch (SQLiteFullException e2) {
            Log.e(LOG_TAG, "endTransaction: SQLiteFullException: " + e2.toString());
        } catch (SQLException e3) {
            Log.e(LOG_TAG, "endTransaction: SQLException: " + e3.toString());
        }
    }

    private void fillCapexInfo(ContentValues values, Capabilities capex) {
        values.put(CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, capex.getContactId());
        values.put("uri", capex.getUri().toString());
        values.put("available", Boolean.valueOf(capex.isAvailable()));
        values.put("timestamp", Long.valueOf(capex.getTimestamp().getTime()));
        values.put(SocialPresenceStorage.PresenceTable.DISPLAY_NAME, capex.getDisplayName());
        values.put("number", capex.getNumber());
        values.put("features", Long.valueOf(capex.getFeature()));
        values.put("avail_features", Long.valueOf(capex.getAvailableFeatures()));
        values.put("phone_id", Integer.valueOf(capex.getPhoneId()));
        StringBuilder ext = new StringBuilder();
        Iterator<String> it = capex.getExtFeature().iterator();
        while (it.hasNext()) {
            ext.append(it.next());
            if (!it.hasNext()) {
                ext.append(',');
            }
        }
        values.put("ext_features", ext.toString());
        values.put("presence_support", Boolean.valueOf(capex.hasPresenceSupport()));
        values.put(GlobalSettingsConstants.RCS.LEGACY_LATCHING, Boolean.valueOf(capex.getLegacyLatching()));
    }

    private void setIsKor() {
        if (SimUtil.getSimMno(this.mPhoneId).isKor()) {
            Log.i(LOG_TAG, "setIsKor: true");
            this.mIsKor = true;
        }
    }

    public void load() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "load");
        setIsKor();
        Cursor cursor = null;
        try {
            cursor = this.mDbHelper.getCursor("capabilities", new String[]{"_id", CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, "uri", "available", "timestamp", SocialPresenceStorage.PresenceTable.DISPLAY_NAME, "number", "phone_id", "features", "avail_features", "ext_features", "presence_support", GlobalSettingsConstants.RCS.LEGACY_LATCHING}, "phone_id = ?", new String[]{String.valueOf(this.mPhoneId)});
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "loading " + cursor.getCount() + " capabilities record.");
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    Capabilities capabilities = new Capabilities(ImsUri.parse(cursor.getString(cursor.getColumnIndex("uri"))), cursor.getString(cursor.getColumnIndex("number")), cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID)), (long) cursor.getInt(cursor.getColumnIndex("_id")), cursor.getString(cursor.getColumnIndex(SocialPresenceStorage.PresenceTable.DISPLAY_NAME)));
                    capabilities.setFeatures(cursor.getLong(cursor.getColumnIndex("features")));
                    capabilities.setAvailableFeatures(cursor.getLong(cursor.getColumnIndex("avail_features")));
                    capabilities.setPhoneId(cursor.getInt(cursor.getColumnIndex("phone_id")));
                    capabilities.setTimestamp(new Date(cursor.getLong(cursor.getColumnIndex("timestamp"))));
                    capabilities.setPresenceSupport(cursor.getInt(cursor.getColumnIndex("presence_support")) == 1);
                    capabilities.setAvailiable(cursor.getInt(cursor.getColumnIndex("available")) == 1);
                    capabilities.setLegacyLatching(cursor.getInt(cursor.getColumnIndex(GlobalSettingsConstants.RCS.LEGACY_LATCHING)) == 1);
                    this.mCapabilitiesCache.add(capabilities);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "persist: " + e.toString());
        } catch (Throwable th) {
            this.mDbHelper.safeClose((Closeable) null);
            throw th;
        }
        this.mDbHelper.safeClose(cursor);
        IMSLog.i(LOG_TAG, this.mPhoneId, "load done.");
    }

    static class CapabilitiesTable {
        static final String AVAILABLE = "available";
        static final String AVAIL_FEATURES = "avail_features";
        static final String CONTACT_ID = "contact_id";
        static final String DISPLAY_NAME = "display_name";
        static final String EXT_FEATURES = "ext_features";
        static final String FEATURES = "features";
        static final String LEGACY_LATCHING = "legacy_latching";
        static final String NUMBER = "number";
        static final String PHONE_ID = "phone_id";
        static final String PRESENCE_SUPPORT = "presence_support";
        static final String TABLE_NAME = "capabilities";
        static final String TIMESTAMP = "timestamp";
        static final String URI = "uri";
        static final String _ID = "_id";

        CapabilitiesTable() {
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "capdiscovery.db";
        private static final int DATABASE_VERSION = 7;
        private static final String SQL_CREATE_CAPABILITIES_TABLE = "CREATE TABLE capabilities( _id INTEGER PRIMARY KEY, contact_id TEXT, uri TEXT, available INT, timestamp BIGINT DEFAULT 0, display_name TEXT, number TEXT, features INTEGER DEFAULT 0, avail_features INTEGER DEFAULT 0, ext_features TEXT, presence_support INT DEFAULT 0, legacy_latching INT DEFAULT 0, phone_id INT );";
        private static final String SQL_CREATE_INDEX_URI = "CREATE INDEX idx_uri ON capabilities (uri);";
        private Context mContext;
        private AtomicInteger mRefCount = new AtomicInteger(0);

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 7);
            this.mContext = context;
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i(CapabilityStorage.LOG_TAG, "onCreate: Creating DB.");
            db.execSQL("DROP TABLE IF EXISTS capabilities");
            db.execSQL(SQL_CREATE_CAPABILITIES_TABLE);
            db.execSQL(SQL_CREATE_INDEX_URI);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(CapabilityStorage.LOG_TAG, "onUpgrade() oldVersion [" + oldVersion + "] , newVersion [" + newVersion + "]");
            if (db.getVersion() != newVersion) {
                onCreate(db);
                db.setVersion(7);
                clearCapabilitySharedPreference();
            }
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(CapabilityStorage.LOG_TAG, "onDowngrade() oldVersion [" + oldVersion + "] , newVersion [" + newVersion + "]");
            if (db.getVersion() != newVersion) {
                onCreate(db);
                db.setVersion(7);
                clearCapabilitySharedPreference();
            }
        }

        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            this.mRefCount.getAndIncrement();
        }

        /* access modifiers changed from: package-private */
        public void safeClose(Closeable target) {
            if (this.mRefCount.decrementAndGet() > 0) {
                Log.i(CapabilityStorage.LOG_TAG, "safeClose: Someone uses db (" + this.mRefCount.get() + "). Let it close db later!");
            } else if (target != null) {
                try {
                    target.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public Cursor getCursor(String table, String[] col, String sel, String[] selArgs) {
            try {
                return getReadableDatabase().query(table, col, sel, selArgs, (String) null, (String) null, (String) null);
            } catch (SQLiteException e) {
                Log.e(CapabilityStorage.LOG_TAG, "getCursor: " + e.getMessage());
                return new MatrixCursor(new String[0]);
            } catch (Throwable th) {
                return null;
            }
        }

        private void clearCapabilitySharedPreference() {
            Log.i(CapabilityStorage.LOG_TAG, "clearCapabilitySharedPreference");
            SharedPreferences settings = this.mContext.getSharedPreferences("capdiscovery", 0);
            if (settings != null) {
                settings.edit().clear().apply();
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 11 */
    public void persistToContactDB(Capabilities capex, boolean isNotifyUpdated) {
        Cursor cursor;
        if (this.mIsKor && this.mNeedResetRcsData) {
            this.mNeedResetRcsData = false;
            deleteAllRcsDataFromContactDB();
        }
        if (capex == null) {
            Log.i(LOG_TAG, "persistToContactDB: capex is null");
            return;
        }
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "persistToContactDB: isNotifyUpdated " + isNotifyUpdated);
        this.mUserId = Extensions.ActivityManager.getCurrentUser();
        String normalizedNumber = capex.getNumber();
        Uri remoteUri = getRemoteUriwithUserId(Uri.parse(ContactsContract.AUTHORITY_URI + "/phone_lookup").buildUpon().appendPath(normalizedNumber).build());
        int i2 = this.mPhoneId;
        IMSLog.s(LOG_TAG, i2, "persistToContactDB: remoteUri = " + remoteUri);
        try {
            cursor = this.mContext.getContentResolver().query(remoteUri, new String[]{"number"}, (String) null, (String[]) null, (String) null);
            if (cursor == null) {
                Log.i(LOG_TAG, "persistToContactDB: fail to read contact db");
                if (cursor != null) {
                    cursor.close();
                    return;
                }
                return;
            } else if (cursor.getCount() == 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "persistToContactDB: no contact found");
                if (cursor != null) {
                    cursor.close();
                    return;
                }
                return;
            } else {
                List<String> phoneNumberList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String phoneNumber = cursor.getString(0);
                    if (!phoneNumberList.contains(phoneNumber)) {
                        phoneNumberList.add(phoneNumber);
                        putCapabilityToContactDB(phoneNumber, normalizedNumber, capex, isNotifyUpdated);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                    return;
                }
                return;
            }
        } catch (SQLiteDiskIOException | IllegalArgumentException e) {
            e.printStackTrace();
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x01ad A[SYNTHETIC, Splitter:B:57:0x01ad] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void putCapabilityToContactDB(java.lang.String r25, java.lang.String r26, com.sec.ims.options.Capabilities r27, boolean r28) {
        /*
            r24 = this;
            r8 = r24
            r9 = r25
            r10 = r26
            r11 = r27
            int r0 = r8.mPhoneId
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "putCapabilityToContactDB: phoneNumber = "
            r1.append(r2)
            r1.append(r9)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "CapabilityStorage"
            com.sec.internal.log.IMSLog.s(r2, r0, r1)
            r0 = 0
            java.util.HashMap r1 = new java.util.HashMap
            r1.<init>()
            r12 = r1
            java.util.HashMap r1 = new java.util.HashMap
            r1.<init>()
            r13 = r1
            android.net.Uri r7 = r24.setRemoteUri()
            java.lang.String[] r21 = r24.setProjection()
            java.lang.String r22 = r24.setSelection()
            int r1 = r8.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "putCapabilityToContactDB: remoteUri = "
            r3.append(r4)
            r3.append(r7)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r2, r1, r3)
            r1 = 3
            java.lang.String[] r3 = new java.lang.String[r1]
            r4 = 0
            r3[r4] = r9
            java.lang.String r5 = "vnd.android.cursor.item/phone_v2"
            r6 = 1
            r3[r6] = r5
            r5 = 2
            java.lang.String r15 = "vnd.android.cursor.item/rcs_data"
            r3[r5] = r15
            r18 = r3
            r3 = 0
            r23 = 0
            android.content.Context r0 = r8.mContext
            android.content.ContentResolver r14 = r0.getContentResolver()
            r19 = 0
            r20 = 0
            r0 = r15
            r15 = r7
            r16 = r21
            r17 = r22
            android.database.Cursor r14 = r14.query(r15, r16, r17, r18, r19, r20)
            if (r14 != 0) goto L_0x0090
            java.lang.String r0 = "putCapabilityToContactDB: cursor is null"
            android.util.Log.i(r2, r0)     // Catch:{ all -> 0x008a }
            if (r14 == 0) goto L_0x0089
            r14.close()
        L_0x0089:
            return
        L_0x008a:
            r0 = move-exception
            r1 = r0
            r17 = r7
            goto L_0x01ab
        L_0x0090:
            java.util.ArrayList r15 = new java.util.ArrayList     // Catch:{ all -> 0x01a7 }
            r15.<init>()     // Catch:{ all -> 0x01a7 }
            java.util.ArrayList r3 = new java.util.ArrayList     // Catch:{ all -> 0x01a1 }
            r3.<init>()     // Catch:{ all -> 0x01a1 }
            int r16 = r14.getCount()     // Catch:{ all -> 0x0197 }
            if (r16 <= 0) goto L_0x0112
        L_0x00a0:
            boolean r16 = r14.moveToNext()     // Catch:{ all -> 0x0109 }
            if (r16 == 0) goto L_0x0112
            java.lang.String r16 = r14.getString(r4)     // Catch:{ all -> 0x0109 }
            r17 = r16
            java.lang.String r16 = r14.getString(r6)     // Catch:{ all -> 0x0109 }
            r19 = r16
            java.lang.String r16 = r14.getString(r5)     // Catch:{ all -> 0x0109 }
            r20 = r16
            java.lang.String r16 = r14.getString(r1)     // Catch:{ all -> 0x0109 }
            r23 = r16
            r1 = 4
            java.lang.String r1 = r14.getString(r1)     // Catch:{ all -> 0x0109 }
            r4 = r19
            if (r4 == 0) goto L_0x00fa
            boolean r19 = r4.equals(r0)     // Catch:{ all -> 0x0109 }
            if (r19 == 0) goto L_0x00ee
            boolean r19 = android.text.TextUtils.equals(r1, r10)     // Catch:{ all -> 0x0109 }
            if (r19 == 0) goto L_0x00e5
            r5 = r17
            r3.add(r5)     // Catch:{ all -> 0x0109 }
            r6 = r20
            r12.put(r5, r6)     // Catch:{ all -> 0x0109 }
            r20 = r0
            r0 = r23
            r13.put(r5, r0)     // Catch:{ all -> 0x0109 }
            goto L_0x0102
        L_0x00e5:
            r5 = r17
            r6 = r20
            r20 = r0
            r0 = r23
            goto L_0x0102
        L_0x00ee:
            r5 = r17
            r6 = r20
            r20 = r0
            r0 = r23
            r15.add(r5)     // Catch:{ all -> 0x0109 }
            goto L_0x0102
        L_0x00fa:
            r5 = r17
            r6 = r20
            r20 = r0
            r0 = r23
        L_0x0102:
            r0 = r20
            r1 = 3
            r4 = 0
            r5 = 2
            r6 = 1
            goto L_0x00a0
        L_0x0109:
            r0 = move-exception
            r1 = r0
            r23 = r3
            r17 = r7
            r3 = r15
            goto L_0x01ab
        L_0x0112:
            if (r14 == 0) goto L_0x0117
            r14.close()
        L_0x0117:
            int r0 = r8.mPhoneId
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r4 = "putCapabilityToContactDB: rawIdList = "
            r1.append(r4)
            r1.append(r15)
            java.lang.String r4 = ", rawIdList_rcs = "
            r1.append(r4)
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r2, r0, r1)
            int r0 = r3.size()
            if (r0 <= 0) goto L_0x0153
            int r0 = r8.checkCapability(r11)
            r1 = -1
            if (r0 != r1) goto L_0x0153
            boolean r0 = r8.isOppositeCapexNull(r11)
            if (r0 == 0) goto L_0x0152
            java.lang.String r0 = "putCapabilityToContactDB: delete from contact db"
            android.util.Log.i(r2, r0)
            r24.deleteFromContactDB(r25)
        L_0x0152:
            return
        L_0x0153:
            java.util.Iterator r0 = r3.iterator()
        L_0x0157:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x017e
            java.lang.Object r1 = r0.next()
            r14 = r1
            java.lang.String r14 = (java.lang.String) r14
            r1 = r24
            r2 = r28
            r16 = r3
            r3 = r14
            r4 = r25
            r5 = r27
            r6 = r12
            r17 = r7
            r7 = r13
            r1.needUpdateToContactDB(r2, r3, r4, r5, r6, r7)
            r15.remove(r14)
            r3 = r16
            r7 = r17
            goto L_0x0157
        L_0x017e:
            r16 = r3
            r17 = r7
            java.util.Iterator r0 = r15.iterator()
        L_0x0186:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0196
            java.lang.Object r1 = r0.next()
            java.lang.String r1 = (java.lang.String) r1
            r8.insertToContactDB(r1, r9, r10, r11)
            goto L_0x0186
        L_0x0196:
            return
        L_0x0197:
            r0 = move-exception
            r16 = r3
            r17 = r7
            r1 = r0
            r3 = r15
            r23 = r16
            goto L_0x01ab
        L_0x01a1:
            r0 = move-exception
            r17 = r7
            r1 = r0
            r3 = r15
            goto L_0x01ab
        L_0x01a7:
            r0 = move-exception
            r17 = r7
            r1 = r0
        L_0x01ab:
            if (r14 == 0) goto L_0x01b6
            r14.close()     // Catch:{ all -> 0x01b1 }
            goto L_0x01b6
        L_0x01b1:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x01b6:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityStorage.putCapabilityToContactDB(java.lang.String, java.lang.String, com.sec.ims.options.Capabilities, boolean):void");
    }

    private void needUpdateToContactDB(boolean isNotifyUpdated, String rawId, String phoneNumber, Capabilities capex, Map<String, String> rcsFeatures, Map<String, String> rcsAvailableFeatures) {
        Mno mno = SimUtil.getMno();
        if (isNotifyUpdated) {
            updateToContactDB(rawId, phoneNumber, capex);
        } else if ((mno == Mno.ATT || mno == Mno.VZW || mno.isKor()) && rcsFeatures.get(rawId) != null && !rcsFeatures.get(rawId).equals(Long.toString(capex.getFeature()))) {
            Log.i(LOG_TAG, "needUpdateToContactDB: capex(longFeatures) is different with contact db = " + rcsFeatures.get(rawId));
            IMSLog.c(LogClass.CS_DIFF_FEATURE, "N," + rawId + "," + rcsFeatures.get(rawId) + "," + capex.getFeature());
            updateToContactDB(rawId, phoneNumber, capex);
        } else if (mno == Mno.VZW && rcsAvailableFeatures.get(rawId) != null && !rcsAvailableFeatures.get(rawId).equals(Long.toString(capex.getAvailableFeatures()))) {
            Log.i(LOG_TAG, "needUpdateToContactDB: capex(availableFeatures) is different with contact db = " + rcsAvailableFeatures.get(rawId));
            IMSLog.c(LogClass.CS_DIFF_AVAILABLEFEATURE, "N," + rawId + "," + rcsAvailableFeatures.get(rawId) + "," + capex.getAvailableFeatures());
            updateToContactDB(rawId, phoneNumber, capex);
        }
    }

    private String setSelection() {
        if (this.mUserId == 0) {
            return "data1 = ? AND (mimetype = ? OR mimetype = ?)";
        }
        return "data1 = ? AND (mimetype = ? OR mimetype = ?)";
    }

    private Uri setRemoteUri() {
        if (this.mUserId == 0) {
            return getRemoteUriwithUserId(ContactsContract.Data.CONTENT_URI);
        }
        return getRemoteUriwithUserId(ContactsContract.RawContactsEntity.CONTENT_URI);
    }

    private String[] setProjection() {
        int i;
        int i2;
        if (this.mUserId == 0) {
            if (!RcsUtils.DualRcs.isDualRcsReg() || (i2 = this.mPhoneId) == 0) {
                return new String[]{"raw_contact_id", "mimetype", "data5", "data6", "data2"};
            }
            if (i2 == 1) {
                return new String[]{"raw_contact_id", "mimetype", "data9", "data10", "data2"};
            }
            return null;
        } else if (!RcsUtils.DualRcs.isDualRcsReg() || (i = this.mPhoneId) == 0) {
            return new String[]{"_id", "mimetype", "data5", "data6", "data2"};
        } else {
            if (i == 1) {
                return new String[]{"_id", "mimetype", "data9", "data10", "data2"};
            }
            return null;
        }
    }

    private boolean isOppositeCapexNull(Capabilities capex) {
        if (!RcsUtils.DualRcs.isDualRcsReg()) {
            IMSLog.s(LOG_TAG, this.mPhoneId, "dual rcs is not enabled.");
            return true;
        }
        Capabilities oppositeCapex = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getCapabilitiesCache(capex.getPhoneId() == 1 ? 0 : 1).get(capex.getUri());
        if (oppositeCapex == null) {
            IMSLog.s(LOG_TAG, this.mPhoneId, "oppositeCapex is null.");
            return true;
        } else if (checkCapability(oppositeCapex) == -1) {
            IMSLog.s(LOG_TAG, this.mPhoneId, "oppositeCapex is CAPABLE_NULL.");
            return true;
        } else {
            IMSLog.s(LOG_TAG, this.mPhoneId, "oppositeCapex is not CAPABLE_NULL.");
            return false;
        }
    }

    private void updateToContactDB(String rawContactId, String phoneNumber, Capabilities capex) {
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "updateToContactDB: phoneNumber : " + phoneNumber);
        this.mUpdater.tryPut(ContentProviderOperation.newUpdate(getRemoteUriwithUserId(ContactsContract.Data.CONTENT_URI)).withValues(setContentValues(capex)).withSelection("raw_contact_id = ? AND data1 = ? AND mimetype = ?", new String[]{rawContactId, phoneNumber, MIMETYPE_RCSE}).build());
    }

    private ContentValues setContentValues(Capabilities capex) {
        return setContentValues(capex, checkCapability(capex));
    }

    private ContentValues setContentValues(Capabilities capex, int cap) {
        int i;
        String capa = Integer.toString(cap);
        String time = Long.toString(capex.getTimestamp().getTime());
        String longFeatures = Long.toString(capex.getFeature());
        String longAvailableFeatures = Long.toString(capex.getAvailableFeatures());
        ContentValues values = new ContentValues();
        if (!RcsUtils.DualRcs.isDualRcsReg() || (i = this.mPhoneId) == 0) {
            values.put("data3", capa);
            values.put("data4", time);
            values.put("data5", longFeatures);
            values.put("data6", longAvailableFeatures);
        } else if (i == 1) {
            values.put("data7", capa);
            values.put("data8", time);
            values.put("data9", longFeatures);
            values.put("data10", longAvailableFeatures);
        }
        int i2 = this.mPhoneId;
        IMSLog.s(LOG_TAG, i2, "setContentValues: longFeatures = " + longFeatures + ", longAvailableFeatures = " + longAvailableFeatures);
        return values;
    }

    private void deleteFromContactDB(String phoneNumber) {
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "deleteFromContactDB: phoneNumber : " + phoneNumber);
        try {
            this.mContext.getContentResolver().delete(getRemoteUriwithUserId(ContactsContract.Data.CONTENT_URI), "Data.DATA1 = ? AND mimetype_id = (SELECT _id FROM mimetypes WHERE mimetype = ?)", new String[]{phoneNumber, MIMETYPE_RCSE});
        } catch (SQLiteException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void deleteFromContactDB(String rawContactId, String phoneNumber) {
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "deleteFromContactDB: phoneNumber = " + phoneNumber + ", rawContactId = " + rawContactId);
        try {
            this.mContext.getContentResolver().delete(getRemoteUriwithUserId(ContactsContract.Data.CONTENT_URI), "raw_contact_id = ? AND data1 = ? AND mimetype_id = (SELECT _id FROM mimetypes WHERE mimetype = ?)", new String[]{rawContactId, phoneNumber, MIMETYPE_RCSE});
        } catch (SQLiteException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void deleteAllRcsDataFromContactDB() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "deleteAllRcsDataFromContactDB:");
        try {
            this.mContext.getContentResolver().delete(getRemoteUriwithUserId(ContactsContract.Data.CONTENT_URI), "mimetype = ?", new String[]{MIMETYPE_RCSE});
        } catch (SQLiteException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void deleteNonRcsDataFromContactDB() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "deleteNonRcsDataFromContactDB:");
        int numRows = 0;
        try {
            numRows = this.mContext.getContentResolver().delete(getRemoteUriwithUserId(ContactsContract.Data.CONTENT_URI), "(((data5 = ? OR data5 = ?) AND data9 is null ) OR ((data9 = ? OR data9 = ?) AND data5 is null ) OR ((data5 = ? OR data5 = ?) AND (data9 = ? OR data9 = ?))) AND mimetype_id = (SELECT _id FROM mimetypes WHERE mimetype = ?)", new String[]{Integer.toString(Capabilities.FEATURE_NON_RCS_USER), Integer.toString(Capabilities.FEATURE_NOT_UPDATED), Integer.toString(Capabilities.FEATURE_NON_RCS_USER), Integer.toString(Capabilities.FEATURE_NOT_UPDATED), Integer.toString(Capabilities.FEATURE_NON_RCS_USER), Integer.toString(Capabilities.FEATURE_NOT_UPDATED), Integer.toString(Capabilities.FEATURE_NON_RCS_USER), Integer.toString(Capabilities.FEATURE_NOT_UPDATED), MIMETYPE_RCSE});
        } catch (SQLiteException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        IMSLog.i(LOG_TAG, "deleteNonRcsDataFromContactDB: deleted rows = " + numRows);
        IMSLog.c(LogClass.CS_DEL_NON_RCS_DATA, "N," + numRows);
    }

    private void insertToContactDB(String rawContactId, String phoneNumber, String normalizedNumber, Capabilities capex) {
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "insertToContactDB: phoneNumber = " + phoneNumber + ", rawContactId = " + rawContactId);
        int cap = checkCapability(capex);
        Mno mno = SimUtil.getMno();
        if (cap == -1 || (!mno.isRjil() && cap == 0)) {
            IMSLog.s(LOG_TAG, this.mPhoneId, "insertToContactDB: Ignore inserting CAPABLE_NULL or CAPABLE_NONE");
            return;
        }
        checkAndDeleteGarbageRcsData(rawContactId, phoneNumber);
        ContentValues values = setContentValues(capex, cap);
        values.put("mimetype", MIMETYPE_RCSE);
        values.put("raw_contact_id", rawContactId);
        values.put("data1", phoneNumber);
        values.put("data2", normalizedNumber);
        this.mUpdater.tryPut(ContentProviderOperation.newInsert(getRemoteUriwithUserId(ContactsContract.Data.CONTENT_URI)).withValues(values).build());
    }

    /* Debug info: failed to restart local var, previous not found, register: 20 */
    private void checkAndDeleteGarbageRcsData(String rawContactId, String phoneNumber) {
        Cursor cursor2;
        Throwable th;
        String str = rawContactId;
        Uri remoteUri = getRemoteUriwithUserId(ContactsContract.Data.CONTENT_URI);
        int i = 3;
        int i2 = 0;
        String[] selArgs = {str, phoneNumber, MIMETYPE_RCSE};
        Cursor cursor = this.mContext.getContentResolver().query(remoteUri, new String[]{"data1"}, "raw_contact_id = ? AND data1 <> ? AND mimetype = ?", selArgs, (String) null, (CancellationSignal) null);
        if (cursor != null) {
            try {
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        String garbageNumber = cursor.getString(i2);
                        String[] selArgs2 = new String[i];
                        selArgs2[i2] = str;
                        selArgs2[1] = garbageNumber;
                        selArgs2[2] = MIMETYPE_RCSE;
                        int i3 = i2;
                        cursor2 = this.mContext.getContentResolver().query(remoteUri, new String[]{"raw_contact_id"}, "raw_contact_id = ? AND data1 = ? AND mimetype <> ?", selArgs2, (String) null, (CancellationSignal) null);
                        if (cursor2 != null) {
                            if (cursor2.getCount() > 0) {
                                cursor2.moveToFirst();
                                String rawId = cursor2.getString(i3);
                                IMSLog.s(LOG_TAG, "checkAndDeleteGarbageRcsData: " + garbageNumber + " has rawContactId(" + rawId + "), so this is not garbage data");
                                if (cursor2 != null) {
                                    cursor2.close();
                                }
                                i2 = i3;
                                i = 3;
                            }
                        }
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        IMSLog.s(LOG_TAG, "checkAndDeleteGarbageRcsData: remove garbageNumber(" + garbageNumber + "), rawContactId(" + str + ") from Contact DB");
                        deleteFromContactDB(str, garbageNumber);
                        i2 = i3;
                        i = 3;
                    }
                    if (cursor != null) {
                        cursor.close();
                        return;
                    }
                    return;
                } else if (cursor != null) {
                    cursor.close();
                    return;
                } else {
                    return;
                }
            } catch (Throwable th2) {
                Throwable th3 = th2;
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        } else if (cursor != null) {
            cursor.close();
            return;
        } else {
            return;
        }
        throw th;
    }

    private Uri getRemoteUriwithUserId(Uri contentUri) {
        return Extensions.ContentProvider.maybeAddUserId(contentUri, Extensions.ActivityManager.getCurrentUser());
    }

    private class SequenceUpdater {
        ArrayList<ContentProviderOperation> operationList;
        boolean timeout;

        private SequenceUpdater() {
            this.timeout = false;
            this.operationList = new ArrayList<>();
        }

        /* access modifiers changed from: package-private */
        public void tryPut(ContentProviderOperation op) {
            synchronized (this.operationList) {
                this.operationList.add(op);
            }
            tryApplybatch();
        }

        /* access modifiers changed from: package-private */
        public void tryApplybatch() {
            if (this.operationList.size() >= 100 || this.timeout) {
                IMSLog.i(CapabilityStorage.LOG_TAG, CapabilityStorage.this.mPhoneId, "tryApplybatch: try size = " + this.operationList.size());
                IMSLog.c(LogClass.CS_APPLY_BATCH_SIZE, "N," + this.operationList.size());
                try {
                    String authority = CapabilityStorage.this.mUserId + "@" + "com.android.contacts";
                    IMSLog.s(CapabilityStorage.LOG_TAG, "tryApplybatch: authority = " + authority);
                    CapabilityStorage.this.mContext.getContentResolver().applyBatch(authority, new ArrayList(this.operationList));
                } catch (OperationApplicationException | RemoteException | IllegalStateException e) {
                    e.printStackTrace();
                } catch (SecurityException e2) {
                    e2.printStackTrace();
                    CapabilityStorage.this.mEventLog.logAndAdd("SecurityException in tryApplybatch userId = " + CapabilityStorage.this.mUserId + ", size = " + this.operationList.size());
                    StringBuilder sb = new StringBuilder();
                    sb.append("N,");
                    sb.append(CapabilityStorage.this.mUserId);
                    IMSLog.c(LogClass.CS_SECURITY_E, sb.toString());
                } catch (IllegalArgumentException e3) {
                    e3.printStackTrace();
                    CapabilityStorage.this.mEventLog.logAndAdd("IllegalArgumentException in tryApplybatch userId = " + CapabilityStorage.this.mUserId + ", size = " + this.operationList.size());
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("N,");
                    sb2.append(CapabilityStorage.this.mUserId);
                    IMSLog.c(LogClass.CS_ILLEGALARGU_E, sb2.toString());
                }
                this.operationList.clear();
                this.timeout = false;
            }
            if (this.operationList.size() == 1) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (SequenceUpdater.this.operationList.size() > 0) {
                            int access$100 = CapabilityStorage.this.mPhoneId;
                            IMSLog.s(CapabilityStorage.LOG_TAG, access$100, "tryApplybatch: timeout, try remainder " + SequenceUpdater.this.operationList.size());
                            SequenceUpdater.this.timeout = true;
                            CapabilityStorage.this.mUpdater.tryApplybatch();
                        }
                    }
                }, 1000);
            }
        }
    }

    private static final class ContactCapability {
        static final int CAPABLE_NONE = 0;
        static final int CAPABLE_NULL = -1;
        static final int RCS_CAPABLE_ONLY = 1;
        static final int VIDEO_CAPA_ON_AVA_OFF = 7;
        static final int VIDEO_CAPA_ON_AVA_ON = 6;

        private ContactCapability() {
        }
    }

    private int checkCapability(Capabilities capex) {
        if (capex.hasFeature(Capabilities.FEATURE_MMTEL_VIDEO)) {
            if (capex.isAvailable()) {
                return 6;
            }
            return 7;
        } else if (capex.hasFeature(Capabilities.FEATURE_CHAT_CPM) || capex.hasFeature(Capabilities.FEATURE_CHAT_SIMPLE_IM)) {
            return 1;
        } else {
            if (capex.hasFeature(Capabilities.FEATURE_NON_RCS_USER) || capex.hasFeature(Capabilities.FEATURE_NOT_UPDATED)) {
                return -1;
            }
            if (!this.mIsKor || capex.getFeature() != ((long) Capabilities.FEATURE_OFFLINE_RCS_USER)) {
                return 0;
            }
            return -1;
        }
    }
}
