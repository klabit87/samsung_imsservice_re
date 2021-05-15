package com.sec.internal.ims.servicemodules.euc.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;
import com.sec.internal.log.IMSLog;

class EucSQLiteHelper extends SQLiteOpenHelper {
    private static final String CREATE_DIALOG_TABLE = "CREATE TABLE IF NOT EXISTS DIALOG (ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON TEXT,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY(ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI), FOREIGN KEY(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) REFERENCES EUCRDATA(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) ON DELETE CASCADE);";
    private static final String CREATE_EUCR_DATA_TABLE = "CREATE TABLE IF NOT EXISTS EUCRDATA ( ID TEXT,PIN INTEGER,EXTERNAL INTEGER,STATE INTEGER,TYPE INTEGER,REMOTE_URI TEXT,TIMESTAMP INTEGER,TIMEOUT INTEGER,USER_PIN TEXT,SUBSCRIBER_IDENTITY TEXT, PRIMARY KEY (ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI));";
    private static final String DB_NAME = "eucr.db";
    private static final int DB_VERSION = 3;
    static final String DIALOG_COLUMN_ACCEPT_BUTTON = "ACCEPT_BUTTON";
    static final int DIALOG_COLUMN_ACCEPT_BUTTON_INDEX = 4;
    static final String DIALOG_COLUMN_ID = "ID";
    static final int DIALOG_COLUMN_ID_INDEX = 0;
    static final String DIALOG_COLUMN_LANGUAGE = "LANGUAGE";
    static final int DIALOG_COLUMN_LANGUAGE_INDEX = 1;
    static final String DIALOG_COLUMN_REJECT_BUTTON = "REJECT_BUTTON";
    static final int DIALOG_COLUMN_REJECT_BUTTON_INDEX = 5;
    static final String DIALOG_COLUMN_REMOTE_URI = "REMOTE_URI";
    static final int DIALOG_COLUMN_REMOTE_URI_INDEX = 8;
    static final String DIALOG_COLUMN_SUBJECT = "SUBJECT";
    static final int DIALOG_COLUMN_SUBJECT_INDEX = 2;
    static final String DIALOG_COLUMN_SUBSCRIBER_IDENTITY = "SUBSCRIBER_IDENTITY";
    static final int DIALOG_COLUMN_SUBSCRIBER_IDENTITY_INDEX = 6;
    static final String DIALOG_COLUMN_TEXT = "TEXT";
    static final int DIALOG_COLUMN_TEXT_INDEX = 3;
    static final String DIALOG_COLUMN_TYPE = "TYPE";
    static final int DIALOG_COLUMN_TYPE_INDEX = 7;
    static final String DIALOG_TABLE_NAME = "DIALOG";
    private static final String DROP_DIALOG_TABLE = "DROP TABLE IF EXISTS DIALOG;";
    private static final String DROP_EUCR_DATA_TABLE = "DROP TABLE IF EXISTS EUCRDATA;";
    static final String EUCRDATA_COLUMN_EXTERNAL = "EXTERNAL";
    static final int EUCRDATA_COLUMN_EXTERNAL_INDEX = 2;
    static final String EUCRDATA_COLUMN_ID = "ID";
    static final int EUCRDATA_COLUMN_ID_INDEX = 0;
    static final String EUCRDATA_COLUMN_PIN = "PIN";
    static final int EUCRDATA_COLUMN_PIN_INDEX = 1;
    static final String EUCRDATA_COLUMN_REMOTE_URI = "REMOTE_URI";
    static final int EUCRDATA_COLUMN_REMOTE_URI_INDEX = 5;
    static final String EUCRDATA_COLUMN_ROWID = "ROWID";
    static final String EUCRDATA_COLUMN_STATE = "STATE";
    static final int EUCRDATA_COLUMN_STATE_INDEX = 3;
    static final String EUCRDATA_COLUMN_SUBSCRIBER_IDENTITY = "SUBSCRIBER_IDENTITY";
    static final int EUCRDATA_COLUMN_SUBSCRIBER_IDENTITY_INDEX = 9;
    static final String EUCRDATA_COLUMN_TIMEOUT = "TIMEOUT";
    static final int EUCRDATA_COLUMN_TIMEOUT_INDEX = 7;
    static final String EUCRDATA_COLUMN_TIMESTAMP = "TIMESTAMP";
    static final int EUCRDATA_COLUMN_TIMESTAMP_INDEX = 6;
    static final String EUCRDATA_COLUMN_TYPE = "TYPE";
    static final int EUCRDATA_COLUMN_TYPE_INDEX = 4;
    static final String EUCRDATA_COLUMN_USER_PIN = "USER_PIN";
    static final int EUCRDATA_COLUMN_USER_PIN_INDEX = 8;
    static final String EUCRDATA_TABLE_NAME = "EUCRDATA";
    private static final String LOG_TAG = EucSQLiteHelper.class.getSimpleName();
    /* access modifiers changed from: private */
    public static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public static int sOpenCounter;
    private static volatile EucSQLiteHelper sVolatileInstance = null;

    static /* synthetic */ int access$106() {
        int i = sOpenCounter - 1;
        sOpenCounter = i;
        return i;
    }

    public static EucSQLiteHelper getInstance(Context context) {
        if (sVolatileInstance == null) {
            synchronized (mLock) {
                if (sVolatileInstance == null) {
                    sVolatileInstance = new EucSQLiteHelper(context);
                }
            }
        }
        return sVolatileInstance;
    }

    private EucSQLiteHelper(Context context) {
        super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 3);
    }

    private static class EucSQLiteCursor extends SQLiteCursor {
        private static final String LOG_TAG = EucSQLiteCursor.class.getSimpleName();

        EucSQLiteCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            super(driver, editTable, query);
        }

        public void close() {
            String str = LOG_TAG;
            Log.v(str, "Closing cursor, thread=" + Thread.currentThread().getName());
            super.close();
            close(getDatabase());
        }

        private void close(SQLiteDatabase db) {
            String threadName = Thread.currentThread().getName();
            synchronized (EucSQLiteHelper.mLock) {
                EucSQLiteHelper.access$106();
                String str = LOG_TAG;
                Log.v(str, "reference counter=" + EucSQLiteHelper.sOpenCounter + ", thread=" + threadName);
                if (EucSQLiteHelper.sOpenCounter == 0) {
                    String str2 = LOG_TAG;
                    Log.v(str2, "Closing database, thread=" + threadName);
                    if (db != null) {
                        db.close();
                    } else {
                        String str3 = LOG_TAG;
                        Log.e(str3, "Database is already closed!, thread=" + threadName);
                    }
                }
            }
        }
    }

    private static class EucSQLiteCursorFactory implements SQLiteDatabase.CursorFactory {
        private static final String LOG_TAG = EucSQLiteCursorFactory.class.getSimpleName();

        private EucSQLiteCursorFactory() {
        }

        public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver mainQuery, String editTable, SQLiteQuery query) {
            String str = LOG_TAG;
            IMSLog.s(str, "newCursor, thread=" + Thread.currentThread().getName() + ", db=" + db.getPath());
            return new EucSQLiteCursor(mainQuery, editTable, query);
        }
    }

    public SQLiteDatabase getReadableDatabase() {
        throw new UnsupportedOperationException();
    }

    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase sqLiteDatabase;
        synchronized (mLock) {
            sqLiteDatabase = super.getWritableDatabase();
            sOpenCounter++;
            Log.v(LOG_TAG, "Obtaining database, reference counter=" + sOpenCounter + ", thread=" + Thread.currentThread().getName());
        }
        return sqLiteDatabase;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x006c, code lost:
        r2 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void close() {
        /*
            r5 = this;
            monitor-enter(r5)
            java.lang.Thread r0 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x006e }
            java.lang.String r0 = r0.getName()     // Catch:{ all -> 0x006e }
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x006e }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x006e }
            r2.<init>()     // Catch:{ all -> 0x006e }
            java.lang.String r3 = "Close(), thread="
            r2.append(r3)     // Catch:{ all -> 0x006e }
            r2.append(r0)     // Catch:{ all -> 0x006e }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x006e }
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x006e }
            java.lang.Object r1 = mLock     // Catch:{ all -> 0x006e }
            monitor-enter(r1)     // Catch:{ all -> 0x006e }
            int r2 = sOpenCounter     // Catch:{ all -> 0x0069 }
            int r2 = r2 + -1
            sOpenCounter = r2     // Catch:{ all -> 0x0069 }
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x0069 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0069 }
            r3.<init>()     // Catch:{ all -> 0x0069 }
            java.lang.String r4 = "reference counter="
            r3.append(r4)     // Catch:{ all -> 0x0069 }
            int r4 = sOpenCounter     // Catch:{ all -> 0x0069 }
            r3.append(r4)     // Catch:{ all -> 0x0069 }
            java.lang.String r4 = ", thread="
            r3.append(r4)     // Catch:{ all -> 0x0069 }
            r3.append(r0)     // Catch:{ all -> 0x0069 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0069 }
            android.util.Log.v(r2, r3)     // Catch:{ all -> 0x0069 }
            int r2 = sOpenCounter     // Catch:{ all -> 0x0069 }
            if (r2 != 0) goto L_0x0066
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x0069 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0069 }
            r3.<init>()     // Catch:{ all -> 0x0069 }
            java.lang.String r4 = "Closing database, thread="
            r3.append(r4)     // Catch:{ all -> 0x0069 }
            r3.append(r0)     // Catch:{ all -> 0x0069 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0069 }
            android.util.Log.v(r2, r3)     // Catch:{ all -> 0x0069 }
            super.close()     // Catch:{ all -> 0x0069 }
        L_0x0066:
            monitor-exit(r1)     // Catch:{ all -> 0x0069 }
            monitor-exit(r5)
            return
        L_0x0069:
            r2 = move-exception
        L_0x006a:
            monitor-exit(r1)     // Catch:{ all -> 0x006c }
            throw r2     // Catch:{ all -> 0x006e }
        L_0x006c:
            r2 = move-exception
            goto L_0x006a
        L_0x006e:
            r0 = move-exception
            monitor-exit(r5)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.euc.persistence.EucSQLiteHelper.close():void");
    }

    /* access modifiers changed from: package-private */
    public SQLiteDatabase.CursorFactory getCursorFactory() {
        return new EucSQLiteCursorFactory();
    }

    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "DatabaseHelper onCreate() for eucr");
        Log.d(LOG_TAG, "exec SQL:CREATE TABLE IF NOT EXISTS EUCRDATA ( ID TEXT,PIN INTEGER,EXTERNAL INTEGER,STATE INTEGER,TYPE INTEGER,REMOTE_URI TEXT,TIMESTAMP INTEGER,TIMEOUT INTEGER,USER_PIN TEXT,SUBSCRIBER_IDENTITY TEXT, PRIMARY KEY (ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
        db.execSQL(CREATE_EUCR_DATA_TABLE);
        Log.d(LOG_TAG, "exec SQL:CREATE TABLE IF NOT EXISTS DIALOG (ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON TEXT,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY(ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI), FOREIGN KEY(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) REFERENCES EUCRDATA(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) ON DELETE CASCADE);");
        db.execSQL(CREATE_DIALOG_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String str = LOG_TAG;
        Log.i(str, "db upgrade: oldVersion=" + oldVersion + " newVersion=" + newVersion);
        if (oldVersion == 1) {
            Log.d(LOG_TAG, "exec SQL:CREATE TABLE IF NOT EXISTS DIALOG_new ( ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON INTEGER,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY (ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
            db.execSQL("CREATE TABLE IF NOT EXISTS DIALOG_new ( ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON INTEGER,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY (ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
            Log.d(LOG_TAG, "exec SQL:INSERT INTO DIALOG_new (ID,LANGUAGE,SUBJECT,TEXT,ACCEPT_BUTTON,REJECT_BUTTON,SUBSCRIBER_IDENTITY,TYPE) SELECT * FROM DIALOG;");
            db.execSQL("INSERT INTO DIALOG_new (ID,LANGUAGE,SUBJECT,TEXT,ACCEPT_BUTTON,REJECT_BUTTON,SUBSCRIBER_IDENTITY,TYPE) SELECT * FROM DIALOG;");
            db.execSQL(DROP_DIALOG_TABLE);
            db.execSQL("ALTER TABLE DIALOG_new RENAME TO DIALOG;");
            Log.d(LOG_TAG, "exec SQL:UPDATE DIALOG SET REMOTE_URI = (SELECT REMOTE_URI FROM EUCRDATA WHERE(ID=DIALOG.ID AND TYPE=DIALOG.TYPE AND SUBSCRIBER_IDENTITY=DIALOG.SUBSCRIBER_IDENTITY));");
            db.execSQL("UPDATE DIALOG SET REMOTE_URI = (SELECT REMOTE_URI FROM EUCRDATA WHERE(ID=DIALOG.ID AND TYPE=DIALOG.TYPE AND SUBSCRIBER_IDENTITY=DIALOG.SUBSCRIBER_IDENTITY));");
            Log.d(LOG_TAG, "exec SQL:CREATE TABLE IF NOT EXISTS EUCRDATA_new ( ID TEXT,PIN INTEGER,EXTERNAL INTEGER,STATE INTEGER,TYPE INTEGER,REMOTE_URI TEXT,TIMESTAMP INTEGER,TIMEOUT INTEGER,USER_PIN TEXT,SUBSCRIBER_IDENTITY TEXT, PRIMARY KEY (ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
            db.execSQL("CREATE TABLE IF NOT EXISTS EUCRDATA_new ( ID TEXT,PIN INTEGER,EXTERNAL INTEGER,STATE INTEGER,TYPE INTEGER,REMOTE_URI TEXT,TIMESTAMP INTEGER,TIMEOUT INTEGER,USER_PIN TEXT,SUBSCRIBER_IDENTITY TEXT, PRIMARY KEY (ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
            Log.d(LOG_TAG, "exec SQL:INSERT INTO EUCRDATA_new (ID,PIN,EXTERNAL,STATE,TYPE,REMOTE_URI,TIMESTAMP,TIMEOUT,USER_PIN,SUBSCRIBER_IDENTITY) SELECT * FROM EUCRDATA;");
            db.execSQL("INSERT INTO EUCRDATA_new (ID,PIN,EXTERNAL,STATE,TYPE,REMOTE_URI,TIMESTAMP,TIMEOUT,USER_PIN,SUBSCRIBER_IDENTITY) SELECT * FROM EUCRDATA;");
            db.execSQL(DROP_EUCR_DATA_TABLE);
            db.execSQL("ALTER TABLE EUCRDATA_new RENAME TO EUCRDATA;");
            oldVersion = 2;
        }
        if (oldVersion == 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS DIALOG_new (ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON TEXT,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY(ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI), FOREIGN KEY(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) REFERENCES EUCRDATA(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) ON DELETE CASCADE);");
            db.execSQL("INSERT INTO DIALOG_new SELECT * FROM DIALOG;");
            db.execSQL(DROP_DIALOG_TABLE);
            db.execSQL("ALTER TABLE DIALOG_new RENAME TO DIALOG;");
        }
    }
}
