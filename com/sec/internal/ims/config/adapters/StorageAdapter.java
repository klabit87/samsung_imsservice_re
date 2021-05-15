package com.sec.internal.ims.config.adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StorageAdapter implements IStorageAdapter {
    public static final String LOG_TAG = StorageAdapter.class.getSimpleName();
    public static final int STATE_DEFAULT = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_READY = 1;
    private static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public int mDBTableMax = 10;
    /* access modifiers changed from: private */
    public String mIdentity;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    /* access modifiers changed from: private */
    public SQLiteAdapter mSQLite = null;
    /* access modifiers changed from: private */
    public State mState = new IdleState();

    public StorageAdapter() {
        IMSLog.i(LOG_TAG, 0, "Init StorageAdapter");
    }

    public void forceDeleteALL(Context context) {
        new SQLiteAdapter(context, (String) null, this.mDBTableMax).forceDeleteAllConfig();
    }

    public String getIdentity() {
        return this.mIdentity;
    }

    public void setDBTableMax(int tableMax) {
        this.mDBTableMax = tableMax;
    }

    public int getState() {
        int state;
        synchronized (mLock) {
            state = this.mState.getState();
        }
        return state;
    }

    public void open(Context context, String identity, int phoneId) {
        synchronized (mLock) {
            this.mState.open(context, identity, phoneId);
        }
    }

    public String read(String path) {
        String read;
        synchronized (mLock) {
            read = this.mState.read(path);
        }
        return read;
    }

    public Map<String, String> readAll(String path) {
        Map<String, String> readAll;
        synchronized (mLock) {
            readAll = this.mState.readAll(path);
        }
        return readAll;
    }

    public boolean write(String path, String value) {
        boolean write;
        synchronized (mLock) {
            write = this.mState.write(path, value);
        }
        return write;
    }

    public boolean writeAll(Map<String, String> data) {
        boolean writeAll;
        synchronized (mLock) {
            writeAll = this.mState.writeAll(data);
        }
        return writeAll;
    }

    public int delete(String path) {
        int delete;
        synchronized (mLock) {
            delete = this.mState.delete(path);
        }
        return delete;
    }

    public boolean deleteAll() {
        boolean deleteAll;
        synchronized (mLock) {
            deleteAll = this.mState.deleteAll();
        }
        return deleteAll;
    }

    public void close() {
        synchronized (mLock) {
            this.mState.close();
        }
    }

    public Cursor query(String[] projection) {
        Cursor query;
        synchronized (mLock) {
            query = this.mState.query(projection);
        }
        return query;
    }

    private abstract class State {
        protected String LOG_TAG = (StorageAdapter.LOG_TAG + this.mStateName);
        protected String mStateName;

        public State(String name) {
            this.mStateName = name;
        }

        public int getState() {
            return -1;
        }

        public void open(Context context, String identity, int phoneId) {
        }

        public String read(String path) {
            return null;
        }

        public Map<String, String> readAll(String path) {
            return null;
        }

        public boolean write(String path, String value) {
            return false;
        }

        public boolean writeAll(Map<String, String> map) {
            return false;
        }

        public int delete(String path) {
            return 0;
        }

        public boolean deleteAll() {
            return false;
        }

        public void close() {
        }

        public Cursor query(String[] projection) {
            return null;
        }
    }

    private class IdleState extends State {
        public IdleState() {
            super("IDLE");
        }

        public int getState() {
            return 0;
        }

        public void open(Context context, String identity, int phoneId) {
            String str = this.LOG_TAG;
            IMSLog.i(str, phoneId, "open storage : " + identity);
            int unused = StorageAdapter.this.mPhoneId = phoneId;
            String unused2 = StorageAdapter.this.mIdentity = identity;
            StorageAdapter storageAdapter = StorageAdapter.this;
            StorageAdapter storageAdapter2 = StorageAdapter.this;
            SQLiteAdapter unused3 = storageAdapter.mSQLite = new SQLiteAdapter(context, identity, storageAdapter2.mDBTableMax);
            StorageAdapter storageAdapter3 = StorageAdapter.this;
            State unused4 = storageAdapter3.mState = new ReadyState();
        }
    }

    private class ReadyState extends State {
        public ReadyState() {
            super("Ready");
        }

        public int getState() {
            return 1;
        }

        public void close() {
            if (StorageAdapter.this.mSQLite != null) {
                StorageAdapter.this.mSQLite.close();
            }
            String unused = StorageAdapter.this.mIdentity = "";
            StorageAdapter storageAdapter = StorageAdapter.this;
            State unused2 = storageAdapter.mState = new IdleState();
        }

        public String read(String path) {
            Map<String, String> readData = StorageAdapter.this.mSQLite.read(path);
            if (readData.size() == 1) {
                return readData.get(path);
            }
            return null;
        }

        public Map<String, String> readAll(String path) {
            return StorageAdapter.this.mSQLite.read(path);
        }

        public boolean write(String path, String value) {
            Map<String, String> writeData = new TreeMap<>();
            writeData.put(path, value);
            return StorageAdapter.this.mSQLite.write(writeData);
        }

        public boolean writeAll(Map<String, String> data) {
            return StorageAdapter.this.mSQLite.write(data);
        }

        public int delete(String path) {
            return StorageAdapter.this.mSQLite.delete(path);
        }

        public boolean deleteAll() {
            return StorageAdapter.this.mSQLite.deleteAll();
        }

        public Cursor query(String[] projection) {
            return StorageAdapter.this.mSQLite.query(projection);
        }
    }

    private class SQLiteAdapter extends SQLiteOpenHelper {
        private static final String COLUMN1_NAME = "PATH";
        private static final String COLUMN2_NAME = "VALUE";
        private static final String DB_NAME = "config.db";
        private static final int DB_VERSION = 32;
        private static final String PATH_BACKUP = "backup";
        private static final String PATH_INFO = "info";
        private static final String PATH_METADATA_TIMESTAMP = "metadata/timestamp";
        private static final String PATH_OMADM = "omadm";
        private static final String PATH_ROOT = "root";
        private static final String TIMESTAMP_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZ";
        private final String[] COLUMNS = {COLUMN1_NAME, COLUMN2_NAME};
        private int DB_TABLE_MAX = 10;
        private Context mContext = null;
        private String mTableName = null;

        public SQLiteAdapter(Context context, String tableName, int tableMax) {
            super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 32);
            this.mContext = context;
            this.mTableName = tableName;
            this.DB_TABLE_MAX = tableMax;
            String str = StorageAdapter.LOG_TAG;
            int access$000 = StorageAdapter.this.mPhoneId;
            IMSLog.i(str, access$000, "config.db: " + this.mTableName + ", DB_TABLE_MAX: " + this.DB_TABLE_MAX);
        }

        public void onCreate(SQLiteDatabase db) {
            if (db == null) {
                Log.i(StorageAdapter.LOG_TAG, "db is null. return.");
                return;
            }
            try {
                IMSLog.i(StorageAdapter.LOG_TAG, StorageAdapter.this.mPhoneId, "create table:" + this.mTableName);
                db.execSQL("CREATE TABLE IF NOT EXISTS " + this.mTableName + " ( " + COLUMN1_NAME + " TEXT PRIMARY KEY," + COLUMN2_NAME + " TEXT )");
                Calendar rightNow = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault());
                IMSLog.i(StorageAdapter.LOG_TAG, StorageAdapter.this.mPhoneId, "timestamp:" + dateFormat.format(rightNow.getTime()));
                ContentValues values = new ContentValues();
                values.put(COLUMN1_NAME, PATH_METADATA_TIMESTAMP);
                values.put(COLUMN2_NAME, String.valueOf(rightNow.getTimeInMillis()));
                db.insertWithOnConflict(this.mTableName, (String) null, values, 5);
            } catch (SQLiteException e) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e.printStackTrace();
            }
        }

        public void onOpen(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                if (!isTable(db, this.mTableName)) {
                    onCreate(db);
                }
                List<String> tables = getTables(db);
                if (tables.size() > this.DB_TABLE_MAX) {
                    deleteOldTables(db, tables);
                }
                db.setTransactionSuccessful();
            } catch (SQLiteCantOpenDatabaseException e) {
                e.printStackTrace();
                Log.i(StorageAdapter.LOG_TAG, "unable to open database file");
                onCreate(db);
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.i(StorageAdapter.LOG_TAG, "delete all tables");
                for (String table : getTables(db)) {
                    deleteTable(db, table);
                }
                onCreate(db);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
            String str = StorageAdapter.LOG_TAG;
            Log.i(str, "onUpgrade(): [" + oldVer + "] -> [" + newVer + "]");
        }

        /* Debug info: failed to restart local var, previous not found, register: 13 */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0075 A[Catch:{ all -> 0x0098, all -> 0x009f, SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }] */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x007b A[SYNTHETIC, Splitter:B:24:0x007b] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.util.Map<java.lang.String, java.lang.String> read(java.lang.String r14) {
            /*
                r13 = this;
                java.util.TreeMap r0 = new java.util.TreeMap
                java.util.Comparator r1 = java.lang.String.CASE_INSENSITIVE_ORDER
                r0.<init>(r1)
                if (r14 != 0) goto L_0x000a
                return r0
            L_0x000a:
                java.util.Locale r1 = java.util.Locale.US
                java.lang.String r14 = r14.toLowerCase(r1)
                r1 = 0
                android.database.sqlite.SQLiteDatabase r2 = r13.getReadableDatabase()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00b7, SQLiteException -> 0x00a8 }
                r1 = 0
                java.lang.String r3 = "root"
                boolean r3 = r14.startsWith(r3)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                if (r3 != 0) goto L_0x003d
                java.lang.String r3 = "info"
                boolean r3 = r14.startsWith(r3)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                if (r3 != 0) goto L_0x003d
                java.lang.String r3 = "backup"
                boolean r3 = r14.startsWith(r3)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                if (r3 != 0) goto L_0x003d
                java.lang.String r3 = "omadm"
                boolean r3 = r14.startsWith(r3)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                if (r3 == 0) goto L_0x0038
                goto L_0x003d
            L_0x0038:
                java.lang.String r3 = "root/"
                r1 = r3
                goto L_0x0040
            L_0x003d:
                java.lang.String r3 = ""
                r1 = r3
            L_0x0040:
                java.lang.String r3 = r13.mTableName     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                java.lang.String[] r4 = r13.COLUMNS     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                java.lang.String r5 = "PATH LIKE ?  ESCAPE '\\'"
                r10 = 1
                java.lang.String[] r6 = new java.lang.String[r10]     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                r7.<init>()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                r7.append(r1)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                java.lang.String r8 = "*"
                java.lang.String r9 = "%"
                java.lang.String r8 = r14.replace(r8, r9)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                java.lang.String r9 = "_"
                java.lang.String r11 = "\\_"
                java.lang.String r8 = r8.replace(r9, r11)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                r7.append(r8)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                java.lang.String r7 = r7.toString()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                r11 = 0
                r6[r11] = r7     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                r7 = 0
                r8 = 0
                r9 = 0
                android.database.Cursor r3 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                if (r3 != 0) goto L_0x007b
                if (r3 == 0) goto L_0x007a
                r3.close()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
            L_0x007a:
                return r0
            L_0x007b:
                boolean r4 = r3.moveToFirst()     // Catch:{ all -> 0x0098 }
                if (r4 == 0) goto L_0x0092
            L_0x0081:
                java.lang.String r4 = r3.getString(r11)     // Catch:{ all -> 0x0098 }
                java.lang.String r5 = r3.getString(r10)     // Catch:{ all -> 0x0098 }
                r0.put(r4, r5)     // Catch:{ all -> 0x0098 }
                boolean r4 = r3.moveToNext()     // Catch:{ all -> 0x0098 }
                if (r4 != 0) goto L_0x0081
            L_0x0092:
                if (r3 == 0) goto L_0x00c2
                r3.close()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
                goto L_0x00c2
            L_0x0098:
                r4 = move-exception
                if (r3 == 0) goto L_0x00a3
                r3.close()     // Catch:{ all -> 0x009f }
                goto L_0x00a3
            L_0x009f:
                r5 = move-exception
                r4.addSuppressed(r5)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
            L_0x00a3:
                throw r4     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a6, SQLiteException -> 0x00a4 }
            L_0x00a4:
                r1 = move-exception
                goto L_0x00ac
            L_0x00a6:
                r1 = move-exception
                goto L_0x00bb
            L_0x00a8:
                r2 = move-exception
                r12 = r2
                r2 = r1
                r1 = r12
            L_0x00ac:
                java.lang.String r3 = com.sec.internal.ims.config.adapters.StorageAdapter.LOG_TAG
                java.lang.String r4 = "SQLiteException!"
                android.util.Log.i(r3, r4)
                r1.printStackTrace()
                goto L_0x00c3
            L_0x00b7:
                r2 = move-exception
                r12 = r2
                r2 = r1
                r1 = r12
            L_0x00bb:
                java.lang.String r3 = com.sec.internal.ims.config.adapters.StorageAdapter.LOG_TAG
                java.lang.String r4 = "Can not read DB now!"
                android.util.Log.i(r3, r4)
            L_0x00c2:
            L_0x00c3:
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.adapters.StorageAdapter.SQLiteAdapter.read(java.lang.String):java.util.Map");
        }

        public boolean write(Map<String, String> data) {
            if (data == null) {
                Log.i(StorageAdapter.LOG_TAG, "data is null!");
                return false;
            }
            try {
                SQLiteDatabase db = getWritableDatabase();
                try {
                    SQLiteStatement statement = db.compileStatement("INSERT OR REPLACE INTO " + this.mTableName + " VALUES (?,?);");
                    db.beginTransaction();
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        statement.clearBindings();
                        statement.bindString(1, entry.getKey());
                        statement.bindString(2, entry.getValue());
                        statement.execute();
                    }
                    db.setTransactionSuccessful();
                } catch (SQLiteException e) {
                    Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                    e.printStackTrace();
                } catch (Throwable th) {
                    endTransaction(db);
                    throw th;
                }
                endTransaction(db);
                for (Map.Entry<String, String> entry2 : data.entrySet()) {
                    if (this.mTableName.startsWith("OMADM")) {
                        ContentResolver contentResolver = this.mContext.getContentResolver();
                        contentResolver.notifyChange(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/" + entry2.getKey(), StorageAdapter.this.mPhoneId), (ContentObserver) null);
                    } else {
                        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(ConfigConstants.CONTENT_URI.buildUpon().appendPath(entry2.getKey()).build().toString(), StorageAdapter.this.mPhoneId), (ContentObserver) null);
                    }
                }
                return true;
            } catch (SQLiteDiskIOException e2) {
                String str = StorageAdapter.LOG_TAG;
                Log.i(str, "SQLiteDiskIOException : " + e2.toString());
                return false;
            } catch (SQLiteException e3) {
                e3.printStackTrace();
                return false;
            }
        }

        public int delete(String path) {
            String str = StorageAdapter.LOG_TAG;
            Log.i(str, "delete: " + path);
            try {
                return getWritableDatabase().delete(this.mTableName, "PATH = ?", new String[]{path});
            } catch (SQLiteDiskIOException e) {
                String str2 = StorageAdapter.LOG_TAG;
                Log.i(str2, "SQLiteDiskIOException : " + e.toString());
                return 0;
            } catch (SQLiteException e2) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e2.printStackTrace();
                return 0;
            }
        }

        public boolean deleteAll() {
            String str = StorageAdapter.LOG_TAG;
            int access$000 = StorageAdapter.this.mPhoneId;
            IMSLog.i(str, access$000, "drop table:" + this.mTableName);
            SQLiteDatabase db = null;
            try {
                db = getWritableDatabase();
                db.execSQL("DROP TABLE IF EXISTS " + this.mTableName);
            } catch (SQLiteDiskIOException e) {
                String str2 = StorageAdapter.LOG_TAG;
                Log.i(str2, "SQLiteDiskIOException : " + e.toString());
                return false;
            } catch (SQLiteException e2) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e2.printStackTrace();
            }
            onCreate(db);
            return true;
        }

        public boolean forceDeleteAllConfig() {
            List<String> removedTables = new ArrayList<>();
            try {
                SQLiteDatabase db = getWritableDatabase();
                for (String table : getTables(db)) {
                    db.execSQL("DROP TABLE IF EXISTS " + table);
                    db.execSQL("CREATE TABLE IF NOT EXISTS " + table + " ( " + COLUMN1_NAME + " TEXT PRIMARY KEY," + COLUMN2_NAME + " TEXT )");
                    Calendar rightNow = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault());
                    String str = StorageAdapter.LOG_TAG;
                    int access$000 = StorageAdapter.this.mPhoneId;
                    StringBuilder sb = new StringBuilder();
                    sb.append("timestamp:");
                    sb.append(dateFormat.format(rightNow.getTime()));
                    IMSLog.i(str, access$000, sb.toString());
                    ContentValues values = new ContentValues();
                    values.put(COLUMN1_NAME, PATH_METADATA_TIMESTAMP);
                    values.put(COLUMN2_NAME, String.valueOf(rightNow.getTimeInMillis()));
                    db.insertWithOnConflict(this.mTableName, (String) null, values, 5);
                    removedTables.add(table);
                }
                String str2 = StorageAdapter.LOG_TAG;
                Log.i(str2, "forceDeleteAllConfig: removed tables: " + removedTables);
                return true;
            } catch (SQLiteDiskIOException e) {
                String str3 = StorageAdapter.LOG_TAG;
                Log.i(str3, "SQLiteDiskIOException : " + e.toString());
                return false;
            } catch (SQLiteException e2) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e2.printStackTrace();
                return false;
            }
        }

        public Cursor query(String[] projection) {
            StringBuffer sb = new StringBuffer();
            if (projection != null) {
                sb.append("PATH=?");
                for (int i = 1; i < projection.length; i++) {
                    sb.append(" OR PATH=?");
                }
            }
            try {
                return getReadableDatabase().query(this.mTableName, this.COLUMNS, sb.toString(), projection, (String) null, (String) null, (String) null);
            } catch (SQLiteException e) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e.printStackTrace();
                return null;
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 5 */
        private boolean isTable(SQLiteDatabase db, String tableName) {
            Cursor cursor;
            boolean result = false;
            try {
                boolean z = false;
                cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type=? AND name=?", new String[]{"table", tableName});
                if (cursor != null) {
                    cursor.moveToFirst();
                    if (cursor.getInt(0) != 0) {
                        z = true;
                    }
                    result = z;
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e.printStackTrace();
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
            return result;
            throw th;
        }

        private List<String> getTables(SQLiteDatabase db) {
            List<String> tables = new ArrayList<>();
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type=?", new String[]{"table"});
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        while (cursor.moveToNext()) {
                            tables.add(cursor.getString(0));
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return tables;
            throw th;
        }

        private void deleteTable(SQLiteDatabase db, String tableName) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + tableName);
            } catch (SQLiteException e) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e.printStackTrace();
            }
        }

        /* Debug info: failed to restart local var, previous not found, register: 10 */
        private String readTable(SQLiteDatabase db, String tableName, String key) {
            Cursor cursor;
            String value = null;
            try {
                cursor = db.query(tableName, this.COLUMNS, "PATH = ?", new String[]{key}, (String) null, (String) null, (String) null);
                if (cursor.moveToFirst()) {
                    value = cursor.getString(1);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
            return value;
            throw th;
        }

        private void deleteOldTables(SQLiteDatabase db, List<String> tables) {
            Log.i(StorageAdapter.LOG_TAG, "over table limit. remove old tables");
            Map<Long, String> sortData = new TreeMap<>();
            Log.i(StorageAdapter.LOG_TAG, "deleteOldTables: current tables: " + tables);
            for (String table : tables) {
                String time = readTable(db, table, PATH_METADATA_TIMESTAMP);
                if (time != null && !table.startsWith("OMADM")) {
                    sortData.put(Long.valueOf(time), table);
                }
            }
            int deleteCount = sortData.size() - this.DB_TABLE_MAX;
            if (deleteCount >= 1) {
                List<String> removedTable = new ArrayList<>();
                Iterator<Map.Entry<Long, String>> it = sortData.entrySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    String tableName = it.next().getValue();
                    if (!this.mTableName.equals(tableName)) {
                        removedTable.add(tableName);
                        deleteTable(db, tableName);
                        int deleteCount2 = deleteCount - 1;
                        if (deleteCount <= 0) {
                            int i = deleteCount2;
                            break;
                        }
                        deleteCount = deleteCount2;
                    }
                }
                Log.i(StorageAdapter.LOG_TAG, "deleteOldTables: removed tables: " + removedTable);
            }
        }

        private void endTransaction(SQLiteDatabase db) {
            if (db == null) {
                Log.i(StorageAdapter.LOG_TAG, "endTransaction: db is null");
                return;
            }
            try {
                db.endTransaction();
            } catch (SQLException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
}
