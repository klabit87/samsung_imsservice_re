package com.sec.internal.ims.cmstore.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.Util;
import java.util.HashMap;
import java.util.Map;

public class CloudMessageBufferDBPersister {
    private static final String CALLLOG_TABLE = "calllog";
    private static final String CREATE_CALLLOG_TABLE = "CREATE TABLE calllog(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,number TEXT, presentation INTEGER,type INTEGER,starttime INTEGER,date INTEGER, duration INTEGER,data_usage INTEGER,countryiso TEXT,geocoded_location TEXT,logtype INTEGER,frequent INTEGER,seen INTEGER,answeredby INTEGER,device_name TEXT,correlation_id TEXT, res_url TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, linenum TEXT, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0);";
    private static final String CREATE_FAX_MESSAGE_TABLE = "CREATE TABLE fax_message(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,sender TEXT,direction INTEGER,transaction_id TEXT,file_name TEXT,content_type TEXT,file_size INTEGER,file_path TEXT,recipients TEXT,date INTEGER,flagRead INTEGER,deliverstatus INTEGER,error_message TEXT,uploadstatus INTEGER,correlation_id TEXT, res_url TEXT, payloadurl TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, linenum TEXT, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0);";
    private static final String CREATE_FAX_RECEIVER_TABLE = "CREATE TABLE fax_receivers(_bufferdbid INTEGER PRIMARY KEY,linenum TEXT, _id INTEGER,receiver TEXT);";
    private static final String CREATE_MMSADDR_MESSAGE_TABLE = "CREATE TABLE addr(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER, msg_id INTEGER, contact_id INTEGER, address TEXT, type INTEGER, charset INTEGER);";
    private static final String CREATE_MMSPART_MESSAGE_TABLE = "CREATE TABLE part(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER, mid INTEGER, seq INTEGER, ct TEXT, name TEXT, chset INTEGER, cd TEXT, fn TEXT, cid TEXT, cl TEXT, ctt_s INTEGER, ctt_t TEXT, _data TEXT, text TEXT, payloadurl TEXT);";
    private static final String CREATE_MMSPDU_MESSAGE_TABLE = "CREATE TABLE pdu(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER, thread_id INTEGER, date INTEGER, date_sent INTEGER, msg_box INTEGER, read INTEGER DEFAULT 0, m_id TEXT, sub TEXT,sub_cs INTEGER, ct_t TEXT, ct_l TEXT, exp INTEGER, m_cls TEXT, m_type INTEGER, v INTEGER, m_size INTEGER, pri INTEGER, rr INTEGER, rpt_a INTEGER, resp_st INTEGER, st INTEGER, tr_id TEXT, retr_st INTEGER, retr_txt TEXT, retr_txt_cs INTEGER, read_status INTEGER, ct_cls INTEGER, resp_txt TEXT, d_tm INTEGER, d_rpt INTEGER, locked INTEGER, seen INTEGER, sim_slot INTEGER, sim_imsi TEXT, deletable INTEGER, hidden INTEGER, app_id INTEGER, msg_id INTEGER, callback_set INTEGER, reserved INTEGER, text_only INTEGER, spam_report INTEGER, safe_message INTEGER, from_address TEXT, correlation_id TEXT, res_url TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0, linenum TEXT DEFAULT NULL);";
    private static final String CREATE_MULT_LINE_STATUS_TABLE = "CREATE TABLE multilinestatus(_bufferdbid INTEGER PRIMARY KEY,linenum TEXT,messagetype INTEGER,initsync_cusor TEXT DEFAULT NULL,initsync_status INTEGER DEFAULT 0);";
    private static final String CREATE_RCS_MESSAGE_TABLE = "CREATE TABLE rcsimft(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,is_filetransfer INTEGER,direction INTEGER,chat_id TEXT,remote_uri TEXT,sender_alias TEXT,content_type TEXT,inserted_timestamp LONG,ext_info TEXT,body TEXT,notification_disposition_mask INTEGER,notification_status INTEGER DEFAULT 0,disposition_notification_status INTEGER DEFAULT 0,sent_timestamp LONG,delivered_timestamp LONG,displayed_timestamp LONG,message_type INTEGER,message_isslm INTEGER,status INTEGER,not_displayed_counter INTEGER,imdn_message_id TEXT, imdn_original_to TEXT, conversation_id TEXT, contribution_id TEXT, sim_imsi TEXT DEFAULT '',file_path TEXT,file_name TEXT,file_size LONG,file_transfer_id TEXT,state INTEGER,reason INTEGER,bytes_transf LONG,ft_status INTEGER,thumbnail_path TEXT,is_resumable INTEGER,transfer_mech INTEGER DEFAULT 0,data_url TEXT,request_message_id TEXT,is_resizable INTEGER DEFAULT 0, correlation_id TEXT, correlation_tag TEXT, res_url TEXT, parentfolder TEXT, payloadurl TEXT, payloadpartThumb TEXT, payloadpartThumb_filename TEXT, payloadpartFull TEXT, payloadencoding INTEGER DEFAULT 0, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0, linenum TEXT DEFAULT NULL);";
    private static final String CREATE_RCS_NOTIFICATION_TABLE = "CREATE TABLE notification(_bufferdbid INTEGER PRIMARY KEY,id INTEGER,imdn_id TEXT, sender_uri TEXT,status INTEGER DEFAULT 0,timestamp LONG, res_url TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0);";
    private static final String CREATE_RCS_PARTICIPANT_TABLE = "CREATE TABLE participant(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,chat_id TEXT,status INTEGER,type INTEGER,uri TEXT,alias TEXT);";
    private static final String CREATE_RCS_SESSION_TABLE = "CREATE TABLE session(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,chat_id TEXT,own_sim_imsi TEXT,direction INTEGER, chat_type INTEGER, conversation_id TEXT, contribution_id TEXT, is_group_chat INTEGER,is_ft_group_chat INTEGER,status INTEGER,subject TEXT,is_muted INTEGER,max_participants_count INTEGER,imdn_notifications_availability INTEGER, session_uri TEXT DEFAULT NULL, preferred_uri TEXT DEFAULT NULL,linenum TEXT DEFAULT NULL,icon_path TEXT,icon_participant TEXT,sim_imsi TEXT DEFAULT NULL,icon_timestamp TEXT);";
    private static final String CREATE_SMS_MESSAGE_TABLE = "CREATE TABLE sms(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,thread_id INTEGER, address TEXT, person INTEGER, date INTEGER, date_sent INTEGER, protocol INTEGER, read INTEGER DEFAULT 0, status INTEGER, type INTEGER, reply_path_present INTEGER, subject TEXT, body TEXT, service_center TEXT, locked INTEGER, error_code INTEGER, seen INTEGER, deletable INTEGER, sim_slot INTEGER, sim_imsi TEXT, hidden INTEGER, group_id INTEGER, group_type INTEGER, delivery_date INTEGER, app_id INTEGER, msg_id INTEGER, callback_number TEXT, reserved INTEGER, pri INTEGER, teleservice_id INTEGER, link_url TEXT, svc_cmd INTEGER, svc_cmd_content TEXT, roam_pending INTEGER, spam_report INTEGER, safe_message INTEGER, from_address TEXT, correlation_tag TEXT, res_url TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0, linenum TEXT DEFAULT NULL);";
    private static final String CREATE_SUMMARY_TABLE = "CREATE TABLE summarytable(_bufferdbid INTEGER PRIMARY KEY,messagetype INTEGER DEFAULT 0, correlation_id TEXT, correlation_tag TEXT, res_url TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, linenum TEXT, lastmodseq INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0);";
    private static final String CREATE_VVM_GREETING_TABLE = "CREATE TABLE vvm_greeting(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,fileName TEXT,mimeType TEXT,size INTEGER,filepath TEXT,flags INTEGER,duration INTEGER,account_number TEXT,messageId TEXT,greetingtype INTEGER,error_message TEXT,uploadstatus INTEGER,correlation_id TEXT, res_url TEXT, payloadurl TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, linenum TEXT, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0);";
    private static final String CREATE_VVM_MESSAGES_TABLE = "CREATE TABLE vvm_messages(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,timeStamp INTEGER,text TEXT,flagRead INTEGER,flags INTEGER,messageId TEXT,sender TEXT,recipient TEXT,fileName TEXT,mimeType INTEGER,size INTEGER,filepath TEXT,messageKey TEXT,correlation_id TEXT, res_url TEXT, payloadurl TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, linenum TEXT, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0);";
    private static final String CREATE_VVM_PIN_TABLE = "CREATE TABLE vvm_pin(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,oldpwd TEXT, newpwd TEXT,error_message TEXT,uploadstatus INTEGER,res_url TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, linenum TEXT, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0);";
    private static final String CREATE_VVM_PROFILE_TABLE = "CREATE TABLE vvm_profile(_bufferdbid INTEGER PRIMARY KEY,_id INTEGER,cos INTEGER,greeting_type TEXT,status TEXT NOT NULL DEFAULT U, password TEXT,nut TEXT,language TEXT,isblocked TEXT,vvmon TEXT,email_addr1 TEXT,email_addr2 TEXT,line_number TEXT,user_authenticated INTEGER NOT NULL DEFAULT 0, profile_changetype INTEGER, error_message TEXT,uploadstatus INTEGER,res_url TEXT, parentfolder TEXT, flagresourceurl TEXT, path TEXT, parentfolderpath TEXT, lastmodseq INTEGER DEFAULT 0, linenum TEXT, syncdirection INTEGER DEFAULT 0, syncaction INTEGER DEFAULT 0);";
    private static final String DATABASE_NAME = "cloudmessagebuffertable.db";
    private static final int DATABASE_VERSION = 9;
    private static final String FAX_MESSAGE_TABLE = "fax_message";
    private static final String FAX_RECEIVER_TABLE = "fax_receivers";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = CloudMessageBufferDBPersister.class.getSimpleName();
    private static final String MMSADDR_MESSAGE_TABLE = "addr";
    private static final String MMSPART_MESSAGE_TABLE = "part";
    private static final String MMSPDU_MESSAGE_TABLE = "pdu";
    private static final String MULT_LINE_STATUS_TABLE = "multilinestatus";
    private static final String NOTIFICATION_TABLE = "notification";
    private static final String PARTICIPANT_TABLE = "participant";
    private static final String RCS_MESSAGE_TABLE = "rcsimft";
    private static final String SESSION_TABLE = "session";
    private static final String SMS_MESSAGE_TABLE = "sms";
    private static final String SUMMARY_TABLE = "summarytable";
    private static final String VVM_GREETING_TABLE = "vvm_greeting";
    private static final String VVM_MESSAGES_TABLE = "vvm_messages";
    private static final String VVM_PIN_TABLE = "vvm_pin";
    private static final String VVM_PROFILE_TABLE = "vvm_profile";
    private static CloudMessageBufferDBPersister sInstance = null;
    public final Context mContext;
    private final DatabaseHelper mDatabaseHelper;
    private Map<Integer, String> mMapUriTableName = new HashMap();

    private CloudMessageBufferDBPersister(Context context) {
        Log.d(LOG_TAG, "onCreate()");
        this.mContext = context;
        this.mDatabaseHelper = new DatabaseHelper(this.mContext);
        this.mMapUriTableName.put(7, "summarytable");
        this.mMapUriTableName.put(3, SMS_MESSAGE_TABLE);
        this.mMapUriTableName.put(5, MMSADDR_MESSAGE_TABLE);
        this.mMapUriTableName.put(4, MMSPDU_MESSAGE_TABLE);
        this.mMapUriTableName.put(6, MMSPART_MESSAGE_TABLE);
        this.mMapUriTableName.put(1, RCS_MESSAGE_TABLE);
        this.mMapUriTableName.put(2, "participant");
        this.mMapUriTableName.put(13, "notification");
        this.mMapUriTableName.put(10, "session");
        this.mMapUriTableName.put(16, "calllog");
        this.mMapUriTableName.put(17, VVM_MESSAGES_TABLE);
        this.mMapUriTableName.put(19, VVM_PIN_TABLE);
        this.mMapUriTableName.put(18, VVM_GREETING_TABLE);
        this.mMapUriTableName.put(20, VVM_PROFILE_TABLE);
        this.mMapUriTableName.put(21, FAX_MESSAGE_TABLE);
        this.mMapUriTableName.put(22, FAX_RECEIVER_TABLE);
        this.mMapUriTableName.put(23, "multilinestatus");
    }

    public static synchronized CloudMessageBufferDBPersister getInstance(Context context) {
        CloudMessageBufferDBPersister cloudMessageBufferDBPersister;
        synchronized (CloudMessageBufferDBPersister.class) {
            if (sInstance == null) {
                sInstance = new CloudMessageBufferDBPersister(context);
            }
            cloudMessageBufferDBPersister = sInstance;
        }
        return cloudMessageBufferDBPersister;
    }

    public void load() {
        Log.d(LOG_TAG, "load");
        this.mDatabaseHelper.getReadableDatabase().close();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private final Context mContext;

        public DatabaseHelper(Context context) {
            super(context, CloudMessageBufferDBPersister.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 9);
            this.mContext = context;
        }

        public void onCreate(SQLiteDatabase db) {
            Log.d(CloudMessageBufferDBPersister.LOG_TAG, "DatabaseHelper onCreate()");
            db.execSQL(CloudMessageBufferDBPersister.CREATE_SMS_MESSAGE_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_MMSPDU_MESSAGE_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_MMSADDR_MESSAGE_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_MMSPART_MESSAGE_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_RCS_MESSAGE_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_RCS_PARTICIPANT_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_RCS_SESSION_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_RCS_NOTIFICATION_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_SUMMARY_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_MULT_LINE_STATUS_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_CALLLOG_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_VVM_MESSAGES_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_VVM_PIN_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_VVM_GREETING_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_VVM_PROFILE_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_FAX_MESSAGE_TABLE);
            db.execSQL(CloudMessageBufferDBPersister.CREATE_FAX_RECEIVER_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(CloudMessageBufferDBPersister.LOG_TAG, "db upgrade: oldVersion=" + oldVersion + " newVersion=" + newVersion);
            String exception = "";
            if (oldVersion == 1) {
                try {
                    dropAllAndReCreateTables(db);
                } catch (SQLiteException e) {
                    exception = "version: " + oldVersion + " " + e.toString();
                }
                oldVersion = 9;
            }
            if (oldVersion == 2) {
                try {
                    db.execSQL("ALTER TABLE calllog ADD COLUMN starttime INTEGER;");
                } catch (SQLiteException e2) {
                    exception = exception + " version:" + oldVersion + " " + e2.toString();
                }
                oldVersion = 3;
            }
            if (oldVersion == 3) {
                try {
                    db.execSQL("ALTER TABLE rcsimft ADD COLUMN contribution_id  TEXT DEFAULT NULL;");
                    db.execSQL("ALTER TABLE rcsimft ADD COLUMN conversation_id  TEXT DEFAULT NULL;");
                } catch (SQLiteException e3) {
                    exception = exception + " version:" + oldVersion + " " + e3.toString();
                }
                oldVersion = 4;
            }
            if (oldVersion == 4) {
                try {
                    db.execSQL("ALTER TABLE session ADD COLUMN chat_type INTEGER;");
                } catch (SQLiteException e4) {
                    exception = exception + " version:" + oldVersion + " " + e4.toString();
                }
                oldVersion = 5;
            }
            if (oldVersion == 5) {
                try {
                    db.execSQL("ALTER TABLE session ADD COLUMN icon_path TEXT;");
                    db.execSQL("ALTER TABLE session ADD COLUMN icon_participant TEXT;");
                    db.execSQL("ALTER TABLE session ADD COLUMN icon_timestamp TEXT;");
                } catch (SQLiteException e5) {
                    exception = exception + " version:" + oldVersion + " " + e5.toString();
                }
                oldVersion = 6;
            }
            if (oldVersion == 6) {
                try {
                    db.execSQL("ALTER TABLE rcsimft ADD COLUMN payloadpartThumb_filename TEXT;");
                } catch (SQLiteException e6) {
                    exception = exception + " version:" + oldVersion + " " + e6.toString();
                }
                oldVersion = 7;
            }
            if (oldVersion == 7) {
                try {
                    db.execSQL("ALTER TABLE rcsimft ADD COLUMN sim_imsi TEXT DEFAULT '';");
                    db.execSQL("ALTER TABLE session ADD COLUMN sim_imsi TEXT DEFAULT NULL;");
                } catch (SQLiteException e7) {
                    exception = exception + " version:" + oldVersion + " " + e7.toString();
                }
                oldVersion = 8;
            }
            if (oldVersion == 8) {
                String simImsi = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getSubscriberId();
                Log.d(CloudMessageBufferDBPersister.LOG_TAG, "set sim imsi : " + simImsi);
                try {
                    db.execSQL("UPDATE rcsimft SET sim_imsi = " + simImsi + " WHERE " + "sim_imsi" + " IS '' ");
                } catch (SQLException e8) {
                    exception = exception + " version:" + oldVersion + " message sim imsi update failed ";
                }
                try {
                    db.execSQL("UPDATE session SET sim_imsi = " + simImsi + " WHERE " + "sim_imsi" + " IS NULL");
                } catch (SQLException e9) {
                    exception = exception + " version:" + oldVersion + " session sim imsi update failed ";
                }
                CloudMessageRCSStorageAdapter mRCSStorage = new CloudMessageRCSStorageAdapter(this.mContext);
                Cursor cursor = mRCSStorage.queryAllSession();
                try {
                    Log.d(CloudMessageBufferDBPersister.LOG_TAG, "queryAllSession.");
                    while (cursor != null && cursor.moveToNext()) {
                        String chatId = cursor.getString(cursor.getColumnIndexOrThrow("chat_id"));
                        String imsi = cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi"));
                        if (!TextUtils.isEmpty(chatId) && TextUtils.isEmpty(imsi)) {
                            ContentValues cv = new ContentValues();
                            cv.put("sim_imsi", simImsi);
                            mRCSStorage.updateSessionFromBufferDbToRCSDb(chatId, cv);
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    Cursor cursor2 = mRCSStorage.queryAllMessage();
                    try {
                        Log.d(CloudMessageBufferDBPersister.LOG_TAG, "queryAllMessage.");
                        while (cursor2 != null && cursor2.moveToNext()) {
                            int rowId = cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
                            if (TextUtils.isEmpty(cursor2.getString(cursor2.getColumnIndexOrThrow("sim_imsi")))) {
                                ContentValues cv2 = new ContentValues();
                                cv2.put("sim_imsi", simImsi);
                                mRCSStorage.updateMessageFromBufferDb(rowId, cv2);
                            }
                        }
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        Log.d(CloudMessageBufferDBPersister.LOG_TAG, "upgrade sim imsi done.");
                        CloudMessagePreferenceManager.getInstance().saveMigrateSuccessFlag(true);
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            if (!TextUtils.isEmpty(exception)) {
                Log.d(CloudMessageBufferDBPersister.LOG_TAG, "OnUpgrade error: " + exception);
                return;
            }
            return;
            throw th;
            throw th;
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String error = null;
            try {
                dropAllAndReCreateTables(db);
            } catch (SQLiteException e) {
                error = e.toString();
            }
            String access$000 = CloudMessageBufferDBPersister.LOG_TAG;
            Log.d(access$000, "db downgrade: oldVersion=" + oldVersion + " newVersion=" + newVersion + " error: " + error);
        }

        private void dropAllAndReCreateTables(SQLiteDatabase db) {
            Log.d(CloudMessageBufferDBPersister.LOG_TAG, "dropAllAndReCreateTables");
            db.execSQL("DROP TABLE sms");
            db.execSQL("DROP TABLE pdu");
            db.execSQL("DROP TABLE addr");
            db.execSQL("DROP TABLE part");
            db.execSQL("DROP TABLE rcsimft");
            db.execSQL("DROP TABLE participant");
            db.execSQL("DROP TABLE session");
            db.execSQL("DROP TABLE notification");
            db.execSQL("DROP TABLE summarytable");
            db.execSQL("DROP TABLE multilinestatus");
            db.execSQL("DROP TABLE calllog");
            db.execSQL("DROP TABLE vvm_messages");
            db.execSQL("DROP TABLE vvm_pin");
            db.execSQL("DROP TABLE vvm_greeting");
            db.execSQL("DROP TABLE vvm_profile");
            db.execSQL("DROP TABLE fax_message");
            db.execSQL("DROP TABLE fax_receivers");
            onCreate(db);
        }
    }

    public Cursor queryRCSMessages(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return null;
        }
        db.beginTransaction();
        try {
            cursor = db.query(RCS_MESSAGE_TABLE, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return cursor;
    }

    public Cursor queryRCSImdnMessages(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return null;
        }
        db.beginTransaction();
        try {
            cursor = db.query("notification", projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return cursor;
    }

    public int updateRCSTable(ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return 0;
        }
        db.beginTransaction();
        try {
            rowsUpdated = db.update(RCS_MESSAGE_TABLE, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowsUpdated;
    }

    public int updateRCSSessionTable(ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return 0;
        }
        db.beginTransaction();
        try {
            rowsUpdated = db.update("session", values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowsUpdated;
    }

    public int updateRCSParticipantsTable(ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return 0;
        }
        db.beginTransaction();
        try {
            rowsUpdated = db.update("participant", values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowsUpdated;
    }

    public Cursor querySMSMessages(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return null;
        }
        db.beginTransaction();
        try {
            cursor = db.query(SMS_MESSAGE_TABLE, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return cursor;
    }

    public Cursor queryMMSPDUMessages(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return null;
        }
        db.beginTransaction();
        try {
            cursor = db.query(MMSPDU_MESSAGE_TABLE, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return cursor;
    }

    public Cursor queryMMSPARTMessages(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return null;
        }
        db.beginTransaction();
        try {
            cursor = db.query(MMSPART_MESSAGE_TABLE, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return cursor;
    }

    public Cursor querySummaryTable(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return null;
        }
        db.beginTransaction();
        try {
            cursor = db.query("summarytable", projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return cursor;
    }

    public int deleteTable(int tableindex, String selection, String[] selectionArgs) {
        String tableName = this.mMapUriTableName.get(Integer.valueOf(tableindex));
        int rowsDeleted = 0;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return 0;
        }
        db.beginTransaction();
        if (tableindex == 6) {
            try {
                Util.deleteFilesinMmsBufferFolder();
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while querying messages " + e);
            } catch (Throwable th) {
                db.endTransaction();
                throw th;
            }
        }
        rowsDeleted = db.delete(tableName, selection, selectionArgs);
        db.setTransactionSuccessful();
        db.endTransaction();
        return rowsDeleted;
    }

    public int updateTable(int tableindex, ContentValues values, String selection, String[] selectionArgs) {
        String tableName = this.mMapUriTableName.get(Integer.valueOf(tableindex));
        int rowsUpdated = 0;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return 0;
        }
        db.beginTransaction();
        try {
            rowsUpdated = db.update(tableName, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return rowsUpdated;
    }

    public long insertTable(int tableindex, ContentValues cv) {
        return commonInsertTable(tableindex, cv);
    }

    public long insertDeviceMsgToBuffer(int tableindex, ContentValues cv) {
        return commonInsertTable(tableindex, cv);
    }

    private long commonInsertTable(int tableindex, ContentValues cv) {
        String tableName = this.mMapUriTableName.get(Integer.valueOf(tableindex));
        long row = 0;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return 0;
        }
        db.beginTransaction();
        try {
            row = db.insertOrThrow(tableName, (String) null, cv);
            db.setTransactionSuccessful();
        } catch (SQLiteFullException e) {
            String str = LOG_TAG;
            Log.e(str, "SQLiteFullException insertTable: " + e);
        } catch (SQLException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQL exception while insertTable " + e2);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return row;
    }

    public Cursor queryTable(Uri uri, int tableindex, String[] projection, String selection, String sortOrder) {
        String idString = uri.getLastPathSegment();
        if (idString == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        return commonQueryTable(tableindex, projection, selection, new String[]{idString}, sortOrder);
    }

    public Cursor queryTable(int tableindex, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return commonQueryTable(tableindex, projection, selection, selectionArgs, sortOrder);
    }

    private Cursor commonQueryTable(int tableindex, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName = this.mMapUriTableName.get(Integer.valueOf(tableindex));
        Cursor cursor = null;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return null;
        }
        db.beginTransaction();
        try {
            cursor = db.query(tableName, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying messages " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return cursor;
    }

    private SQLiteDatabase getDatabase() {
        try {
            return this.mDatabaseHelper.getWritableDatabase();
        } catch (SQLiteDiskIOException e) {
            String str = LOG_TAG;
            Log.e(str, "SQLiteDiskIOException : " + e.toString());
            return null;
        }
    }

    public Cursor queryTablewithBufferDbId(int tableindex, long primarykey) {
        return queryTable(tableindex, (String[]) null, "_bufferdbid=?", new String[]{Long.toString(primarykey)}, (String) null);
    }

    public Cursor queryTablewithResUrl(int tableindex, String url) {
        String objId = Util.extractObjIdFromResUrl(url);
        String line = Util.getLineTelUriFromObjUrl(url);
        return queryTable(tableindex, (String[]) null, "res_url GLOB ? AND linenum=?", new String[]{"*" + objId, line}, (String) null);
    }

    public Cursor queryTableWithSessionUrl(int tableindex, String session_url) {
        return queryTable(tableindex, (String[]) null, "session_uri =?", new String[]{session_url}, (String) null);
    }

    public int deleteTablewithResUrl(int tableindex, String url) {
        String objId = Util.extractObjIdFromResUrl(url);
        String line = Util.getLineTelUriFromObjUrl(url);
        return deleteTable(tableindex, "res_url GLOB ? AND linenum=?", new String[]{"*" + objId, line});
    }

    public int deleteTablewithBufferDbId(int tableindex, long primarykey) {
        return deleteTable(tableindex, "_bufferdbid=?", new String[]{Long.toString(primarykey)});
    }

    public Cursor queryRCSImdnUseImdnId(String id) {
        return queryTable(13, (String[]) null, "imdn_id=?", new String[]{id}, (String) null);
    }

    public Cursor queryRCSImdnUseImdnIdAndTelUri(String id, String telUri) {
        return queryTable(13, (String[]) null, "imdn_id=? AND sender_uri=?", new String[]{id, telUri}, (String) null);
    }

    public void cleanAllBufferDB() {
        Log.d(LOG_TAG, "cleanAllBufferDB");
        SQLiteDatabase db = getDatabase();
        if (db != null) {
            db.beginTransaction();
            try {
                db.execSQL("delete from sms");
                db.execSQL("delete from pdu");
                db.execSQL("delete from addr");
                db.execSQL("delete from part");
                db.execSQL("delete from session");
                db.execSQL("delete from rcsimft");
                db.execSQL("delete from participant");
                db.execSQL("delete from notification");
                db.execSQL("delete from multilinestatus");
                db.execSQL("delete from calllog");
                db.execSQL("delete from vvm_messages");
                db.execSQL("delete from vvm_pin");
                db.execSQL("delete from vvm_greeting");
                db.execSQL("delete from vvm_profile");
                db.execSQL("delete from fax_message");
                db.execSQL("delete from summarytable");
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while deleting messages " + e);
            } catch (Throwable th) {
                db.endTransaction();
                throw th;
            }
            db.endTransaction();
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 16 */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00c1, code lost:
        r9 = r4.getString(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        android.util.Log.d(LOG_TAG, "Chat found:" + r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00db, code lost:
        r12 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00dd, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00de, code lost:
        r5 = r0;
        r12 = r9;
     */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00f6 A[SYNTHETIC, Splitter:B:42:0x00f6] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0137 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0139  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.database.Cursor querySessionByParticipants(java.util.Set<com.sec.ims.util.ImsUri> r17, com.sec.ims.util.ImsUri r18) {
        /*
            r16 = this;
            r1 = r16
            java.lang.String r0 = "chat_id"
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "querySessionByParticipants participants="
            r3.append(r4)
            java.lang.String r4 = com.sec.internal.log.IMSLog.checker(r17)
            r3.append(r4)
            java.lang.String r4 = " preferredUri="
            r3.append(r4)
            java.lang.String r4 = com.sec.internal.log.IMSLog.checker(r18)
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.d(r2, r3)
            android.database.sqlite.SQLiteDatabase r2 = r16.getDatabase()
            r3 = 0
            if (r2 != 0) goto L_0x0033
            return r3
        L_0x0033:
            r2.beginTransaction()
            r12 = 0
            android.database.sqlite.SQLiteQueryBuilder r4 = new android.database.sqlite.SQLiteQueryBuilder     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            r4.<init>()     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            r13 = r4
            java.lang.String r4 = "session, participant"
            r13.setTables(r4)     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            java.lang.String r4 = "%s.%s=%s.%s"
            r5 = 4
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            java.lang.String r6 = "session"
            r14 = 0
            r5[r14] = r6     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            r15 = 1
            r5[r15] = r0     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            java.lang.String r6 = "participant"
            r11 = 2
            r5[r11] = r6     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            r6 = 3
            r5[r6] = r0     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            java.lang.String r0 = java.lang.String.format(r4, r5)     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            r13.appendWhere(r0)     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            java.lang.String r0 = "session.chat_id"
            java.lang.String r4 = "group_concat(participant.uri)"
            java.lang.String r5 = "session.preferred_uri"
            java.lang.String[] r6 = new java.lang.String[]{r0, r4, r5}     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            r7 = 0
            r8 = 0
            java.lang.String r9 = "session.chat_id"
            r10 = 0
            r0 = 0
            r4 = r13
            r5 = r2
            r3 = r11
            r11 = r0
            android.database.Cursor r0 = r4.query(r5, r6, r7, r8, r9, r10, r11)     // Catch:{ SQLException -> 0x0115, all -> 0x010f }
            r4 = r0
            if (r4 == 0) goto L_0x0100
        L_0x007f:
            boolean r0 = r4.moveToNext()     // Catch:{ all -> 0x00ee }
            if (r0 == 0) goto L_0x00e9
            java.lang.String r0 = r4.getString(r3)     // Catch:{ all -> 0x00ee }
            com.sec.ims.util.ImsUri r0 = com.sec.ims.util.ImsUri.parse(r0)     // Catch:{ all -> 0x00ee }
            java.lang.String r5 = r4.getString(r15)     // Catch:{ all -> 0x00ee }
            if (r5 != 0) goto L_0x0094
            goto L_0x007f
        L_0x0094:
            java.util.HashSet r7 = new java.util.HashSet     // Catch:{ all -> 0x00ee }
            r7.<init>()     // Catch:{ all -> 0x00ee }
            java.lang.String r8 = ","
            java.lang.String[] r8 = r5.split(r8)     // Catch:{ all -> 0x00ee }
            int r9 = r8.length     // Catch:{ all -> 0x00ee }
            r10 = r14
        L_0x00a1:
            if (r10 >= r9) goto L_0x00b1
            r11 = r8[r10]     // Catch:{ all -> 0x00ee }
            com.sec.ims.util.ImsUri r3 = com.sec.ims.util.ImsUri.parse(r11)     // Catch:{ all -> 0x00ee }
            r7.add(r3)     // Catch:{ all -> 0x00ee }
            int r10 = r10 + 1
            r3 = 2
            goto L_0x00a1
        L_0x00b1:
            r3 = r17
            boolean r8 = r3.equals(r7)     // Catch:{ all -> 0x00e7 }
            if (r8 == 0) goto L_0x00e3
            r8 = r18
            boolean r9 = r1.matchPreferredUri(r8, r0)     // Catch:{ all -> 0x00e1 }
            if (r9 == 0) goto L_0x00e5
            java.lang.String r9 = r4.getString(r14)     // Catch:{ all -> 0x00e1 }
            java.lang.String r10 = LOG_TAG     // Catch:{ all -> 0x00dd }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x00dd }
            r11.<init>()     // Catch:{ all -> 0x00dd }
            java.lang.String r12 = "Chat found:"
            r11.append(r12)     // Catch:{ all -> 0x00dd }
            r11.append(r9)     // Catch:{ all -> 0x00dd }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x00dd }
            android.util.Log.d(r10, r11)     // Catch:{ all -> 0x00dd }
            r12 = r9
            goto L_0x0104
        L_0x00dd:
            r0 = move-exception
            r5 = r0
            r12 = r9
            goto L_0x00f4
        L_0x00e1:
            r0 = move-exception
            goto L_0x00f3
        L_0x00e3:
            r8 = r18
        L_0x00e5:
            r3 = 2
            goto L_0x007f
        L_0x00e7:
            r0 = move-exception
            goto L_0x00f1
        L_0x00e9:
            r3 = r17
            r8 = r18
            goto L_0x0104
        L_0x00ee:
            r0 = move-exception
            r3 = r17
        L_0x00f1:
            r8 = r18
        L_0x00f3:
            r5 = r0
        L_0x00f4:
            if (r4 == 0) goto L_0x00ff
            r4.close()     // Catch:{ all -> 0x00fa }
            goto L_0x00ff
        L_0x00fa:
            r0 = move-exception
            r7 = r0
            r5.addSuppressed(r7)     // Catch:{ SQLException -> 0x010d }
        L_0x00ff:
            throw r5     // Catch:{ SQLException -> 0x010d }
        L_0x0100:
            r3 = r17
            r8 = r18
        L_0x0104:
            if (r4 == 0) goto L_0x0109
            r4.close()     // Catch:{ SQLException -> 0x010d }
        L_0x0109:
            r2.setTransactionSuccessful()     // Catch:{ SQLException -> 0x010d }
            goto L_0x0131
        L_0x010d:
            r0 = move-exception
            goto L_0x011a
        L_0x010f:
            r0 = move-exception
            r3 = r17
            r8 = r18
            goto L_0x013f
        L_0x0115:
            r0 = move-exception
            r3 = r17
            r8 = r18
        L_0x011a:
            java.lang.String r4 = LOG_TAG     // Catch:{ all -> 0x013e }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x013e }
            r5.<init>()     // Catch:{ all -> 0x013e }
            java.lang.String r6 = "SQL exception while querying session. "
            r5.append(r6)     // Catch:{ all -> 0x013e }
            r5.append(r0)     // Catch:{ all -> 0x013e }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x013e }
            android.util.Log.e(r4, r5)     // Catch:{ all -> 0x013e }
        L_0x0131:
            r2.endTransaction()
            if (r12 != 0) goto L_0x0139
            r4 = 0
            return r4
        L_0x0139:
            android.database.Cursor r0 = r1.querySessionByChatId(r12)
            return r0
        L_0x013e:
            r0 = move-exception
        L_0x013f:
            r2.endTransaction()
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister.querySessionByParticipants(java.util.Set, com.sec.ims.util.ImsUri):android.database.Cursor");
    }

    public Cursor querySessionByConversationId(String conversationId) {
        String str = LOG_TAG;
        Log.d(str, "querySessionByConversationId ConversationId = " + conversationId);
        return querySession("conversation_id=?", new String[]{conversationId});
    }

    private boolean matchPreferredUri(ImsUri prefUri, ImsUri sessionPrefUri) {
        if (prefUri == null && sessionPrefUri == null) {
            return true;
        }
        if (prefUri == null || prefUri.equals(ImsUri.EMPTY) || !prefUri.equals(sessionPrefUri)) {
            return false;
        }
        return true;
    }

    private Cursor querySession(String select, String[] selectionArgs) {
        Cursor cursor = null;
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return null;
        }
        db.beginTransaction();
        try {
            cursor = db.query("session", (String[]) null, select, selectionArgs, (String) null, (String) null, (String) null);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while querying session. " + e);
        } catch (Throwable th) {
            db.endTransaction();
            throw th;
        }
        db.endTransaction();
        return cursor;
    }

    public Cursor querySessionByChatId(String cid) {
        String str = LOG_TAG;
        Log.d(str, "querySessionByChatId: " + cid);
        return querySession("chat_id=?", new String[]{cid});
    }
}
