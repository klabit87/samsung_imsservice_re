package com.sec.internal.ims.servicemodules.im;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImDBHelper extends SQLiteOpenHelper {
    public static final String CHAT_MESSAGE_VIEW = "chatmessageview";
    public static final String CLOUD_MESSAGE_PROVIDER_NAME = "com.samsung.rcs.cmstore";
    public static final String CREATE_CHAT_MESSAGE_VIEW = "CREATE VIEW IF NOT EXISTS chatmessageview AS SELECT _id,_id AS msg_id,chat_id AS chat_id,replace(remote_uri, 'tel:', '') AS contact,body AS content,inserted_timestamp AS timestamp,sent_timestamp AS timestamp_sent,delivered_timestamp AS timestamp_delivered,displayed_timestamp AS timestamp_displayed,content_type AS mime_type,status AS status,null AS reason_code,status AS read_status,direction AS direction,delivered_timestamp AS expired_delivery FROM message WHERE is_filetransfer = 0 AND message_type < 3";
    public static final String CREATE_FILETRANSFER_VIEW = "CREATE VIEW IF NOT EXISTS filetransferview AS SELECT _id,_id AS ft_id,chat_id AS chat_id,replace(remote_uri, 'tel:', '') AS contact,file_path AS file,file_name AS filename,content_type AS mime_type,thumbnail_path AS fileicon,thumbnail_path AS fileicon_mime_type,direction AS direction,file_size AS filesize,bytes_transf AS transferred,inserted_timestamp AS timestamp,sent_timestamp AS timestamp_sent,delivered_timestamp AS timestamp_delivered,displayed_timestamp AS timestamp_displayed,state||';'||direction AS state,reason AS reason_code,status AS read_status,null AS file_expiration,null AS fileicon_expiration,delivered_timestamp AS expired_delivery FROM message WHERE is_filetransfer = 1";
    public static final String CREATE_IMDNRECROUTE_TABLE = "CREATE TABLE imdnrecroute(_id INTEGER PRIMARY KEY AUTOINCREMENT,message_id INTEGER DEFAULT 0,imdn_id TEXT,uri TEXT,alias TEXT);";
    public static final String CREATE_MESSAGE_TABLE = "CREATE TABLE message(_id INTEGER PRIMARY KEY AUTOINCREMENT,is_filetransfer INTEGER,direction INTEGER,chat_id TEXT NOT NULL,remote_uri TEXT,sender_alias TEXT,content_type TEXT,inserted_timestamp LONG,ext_info TEXT,body TEXT,suggestion TEXT,notification_disposition_mask INTEGER,notification_status INTEGER DEFAULT 0,disposition_notification_status INTEGER DEFAULT 0,sent_timestamp LONG,delivered_timestamp LONG,displayed_timestamp LONG,message_type INTEGER,message_isslm INTEGER,status INTEGER,not_displayed_counter INTEGER,imdn_message_id TEXT, imdn_original_to TEXT, conversation_id TEXT, contribution_id TEXT, file_path TEXT,file_name TEXT,file_size LONG,file_transfer_id TEXT,state INTEGER,reason INTEGER,bytes_transf LONG,ft_status INTEGER,thumbnail_path TEXT,is_resumable INTEGER,transfer_mech INTEGER DEFAULT 0,data_url TEXT,request_message_id TEXT,is_resizable INTEGER DEFAULT 0,is_broadcast_msg INTEGER DEFAULT 0,is_vm2txt_msg INTEGER DEFAULT 0,extra_ft INTEGER DEFAULT 0,flag_mask INTEGER DEFAULT 0,revocation_status INTEGER DEFAULT 0,sim_imsi TEXT DEFAULT '',device_id TEXT DEFAULT NULL,file_disposition INTEGER,playing_length INTEGER DEFAULT 0,maap_traffic_type TEXT DEFAULT NULL,reference_id TEXT DEFAULT NULL,reference_type TEXT DEFAULT NULL,reference_value TEXT DEFAULT NULL,messaging_tech INTEGER DEFAULT 0);";
    public static final String CREATE_NOTIFICATION_TABLE = "CREATE TABLE notification(id INTEGER PRIMARY KEY AUTOINCREMENT,message_id INTEGER DEFAULT 0,imdn_id TEXT, sender_uri TEXT,status INTEGER DEFAULT 0,timestamp LONG);";
    public static final String CREATE_PARTICIPANT_TABLE = "CREATE TABLE participant(_id INTEGER PRIMARY KEY AUTOINCREMENT,chat_id TEXT,status INTEGER,type INTEGER,uri TEXT,alias TEXT);";
    public static final String CREATE_SESSION_TABLE = "CREATE TABLE session(_id INTEGER PRIMARY KEY AUTOINCREMENT,chat_id TEXT,own_sim_imsi TEXT,own_group_alias TEXT,direction INTEGER, chat_type INTEGER, conversation_id TEXT, contribution_id TEXT, is_group_chat INTEGER,is_ft_group_chat INTEGER DEFAULT 1, status INTEGER,subject TEXT,is_muted INTEGER,max_participants_count INTEGER,imdn_notifications_availability INTEGER DEFAULT 1, session_uri TEXT DEFAULT NULL,is_broadcast_msg INTEGER, inserted_time_stamp LONG, preferred_uri TEXT DEFAULT NULL,is_reusable INTEGER DEFAULT 1,subject_participant TEXT DEFAULT NULL,subject_timestamp LONG,icon_path TEXT DEFAULT NULL,icon_participant TEXT DEFAULT NULL,icon_timestamp LONG,icon_uri TEXT DEFAULT NULL,sim_imsi TEXT DEFAULT NULL,is_chatbot_role INTEGER DEFAULT 0,chat_mode INTEGER DEFAULT 0);";
    public static final String DATABASE_NAME = "rcsim.db";
    public static final String FILETRANSFER_VIEW = "filetransferview";
    public static final String IMDNRECROUTE_TABLE = "imdnrecroute";
    public static final String LOG_TAG = ImDBHelper.class.getSimpleName();
    public static final String MESSAGE_TABLE = "message";
    public static final String NOTIFICATION_TABLE = "notification";
    public static final String PARTICIPANT_TABLE = "participant";
    public static final String SESSION_TABLE = "session";

    public ImDBHelper(Context context, int version) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, version);
    }

    public static ContentValues makeSessionRow(ChatData chatData) {
        ContentValues cv = new ContentValues();
        cv.put("chat_id", chatData.getChatId());
        cv.put(ImContract.ImSession.OWN_PHONE_NUMBER, chatData.getOwnPhoneNum());
        cv.put("sim_imsi", chatData.getOwnIMSI());
        cv.put(ImContract.ImSession.OWN_GROUP_ALIAS, chatData.getOwnGroupAlias());
        cv.put("is_group_chat", Integer.valueOf(chatData.isGroupChat() ? 1 : 0));
        cv.put(ImContract.ImSession.CHAT_TYPE, Integer.valueOf(chatData.getChatType().getId()));
        cv.put("status", Integer.valueOf(chatData.getState().getId()));
        long j = 0;
        String str = null;
        if (chatData.getSubjectData() != null) {
            cv.put("subject", chatData.getSubjectData().getSubject());
            cv.put("subject_participant", chatData.getSubjectData().getParticipant() != null ? chatData.getSubjectData().getParticipant().toString() : null);
            cv.put("subject_timestamp", Long.valueOf(chatData.getSubjectData().getTimestamp() != null ? chatData.getSubjectData().getTimestamp().getTime() : 0));
        } else {
            cv.put("subject", chatData.getSubject());
            cv.put("subject_participant", (String) null);
            cv.put("subject_timestamp", 0L);
        }
        if (chatData.getIconData() != null) {
            cv.put(ImContract.ImSession.ICON_PATH, chatData.getIconData().getIconLocation());
            cv.put(ImContract.ImSession.ICON_PARTICIPANT, chatData.getIconData().getParticipant() != null ? chatData.getIconData().getParticipant().toString() : null);
            if (chatData.getIconData().getTimestamp() != null) {
                j = chatData.getIconData().getTimestamp().getTime();
            }
            cv.put(ImContract.ImSession.ICON_TIMESTAMP, Long.valueOf(j));
            cv.put(ImContract.ImSession.ICON_URI, chatData.getIconData().getIconUri());
        } else {
            String str2 = null;
            cv.put(ImContract.ImSession.ICON_PATH, str2);
            cv.put(ImContract.ImSession.ICON_PARTICIPANT, str2);
            cv.put(ImContract.ImSession.ICON_TIMESTAMP, 0L);
            cv.put(ImContract.ImSession.ICON_URI, str2);
        }
        cv.put(ImContract.ImSession.IS_MUTED, Boolean.valueOf(chatData.isMuted()));
        cv.put(ImContract.ImSession.MAX_PARTICIPANTS_COUNT, Integer.valueOf(chatData.getMaxParticipantsCount()));
        cv.put("direction", Integer.valueOf(chatData.getDirection().getId()));
        cv.put("conversation_id", chatData.getConversationId());
        cv.put("contribution_id", chatData.getContributionId());
        if (chatData.getSessionUri() != null) {
            str = chatData.getSessionUri().toString();
        }
        cv.put("session_uri", str);
        int i = 0;
        cv.put("is_broadcast_msg", Integer.valueOf(chatData.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT ? 1 : 0));
        cv.put(ImContract.ImSession.IS_REUSABLE, Integer.valueOf(chatData.isReusable() ? 1 : 0));
        cv.put(ImContract.ImSession.IS_CHATBOT_ROLE, Integer.valueOf(chatData.isChatbotRole() ? 1 : 0));
        if (chatData.getChatMode() != null) {
            i = chatData.getChatMode().getId();
        }
        cv.put(ImContract.ImSession.CHAT_MODE, Integer.valueOf(i));
        return cv;
    }

    public static ContentValues makeParticipantRow(ImParticipant participant) {
        ContentValues cv = new ContentValues();
        cv.put("chat_id", participant.getChatId());
        cv.put("status", Integer.valueOf(participant.getStatus().getId()));
        cv.put("type", Integer.valueOf(participant.getType().getId()));
        cv.put("uri", participant.getUri().toString());
        cv.put("alias", participant.getUserAlias());
        return cv;
    }

    public static ChatData makeSession(Cursor cursor) {
        Cursor cursor2 = cursor;
        String sessionUri = cursor2.getString(cursor2.getColumnIndexOrThrow("session_uri"));
        ImsUri subjectParticipant = ImsUri.parse(cursor2.getString(cursor2.getColumnIndexOrThrow("subject_participant")));
        long subjectTime = cursor2.getLong(cursor2.getColumnIndexOrThrow("subject_timestamp"));
        Date subjectTimestamp = new Date(subjectTime);
        ImsUri iconParticipant = ImsUri.parse(cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.ICON_PARTICIPANT)));
        long iconTime = cursor2.getLong(cursor2.getColumnIndexOrThrow(ImContract.ImSession.ICON_TIMESTAMP));
        long j = iconTime;
        long j2 = subjectTime;
        return new ChatData(cursor2.getInt(cursor2.getColumnIndexOrThrow("_id")), cursor2.getString(cursor2.getColumnIndexOrThrow("chat_id")), cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.OWN_PHONE_NUMBER)), cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.OWN_GROUP_ALIAS)), ChatData.ChatType.fromId(cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.CHAT_TYPE))), ChatData.State.fromId(cursor2.getInt(cursor2.getColumnIndexOrThrow("status"))), cursor2.getString(cursor2.getColumnIndexOrThrow("subject")), cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.IS_MUTED)) == 1, cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.MAX_PARTICIPANTS_COUNT)), ImDirection.fromId(cursor2.getInt(cursor2.getColumnIndexOrThrow("direction"))), cursor2.getString(cursor2.getColumnIndexOrThrow("conversation_id")), cursor2.getString(cursor2.getColumnIndexOrThrow("contribution_id")), sessionUri != null ? ImsUri.parse(sessionUri) : null, cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.IS_REUSABLE)) == 1, 0, cursor2.getString(cursor2.getColumnIndexOrThrow("sim_imsi")), subjectParticipant, subjectTimestamp, cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.ICON_PATH)), iconParticipant, new Date(iconTime), cursor2.getString(cursor2.getColumnIndexOrThrow(ImContract.ImSession.ICON_URI)), cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.IS_CHATBOT_ROLE)) == 1, ChatMode.fromId(cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ImSession.CHAT_MODE))));
    }

    public void onOpen(SQLiteDatabase db) {
        Log.i(LOG_TAG, "ImDBHelper onOpen()");
        super.onOpen(db);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.i(LOG_TAG, "ImDBHelper onCreate()");
        db.execSQL(CREATE_SESSION_TABLE);
        db.execSQL(CREATE_MESSAGE_TABLE);
        db.execSQL(CREATE_PARTICIPANT_TABLE);
        db.execSQL(CREATE_IMDNRECROUTE_TABLE);
        db.execSQL(CREATE_NOTIFICATION_TABLE);
        createView(db);
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Cursor cursor;
        String str = LOG_TAG;
        Log.i(str, "db upgrade: oldVersion=" + oldVersion + " newVersion=" + newVersion);
        List<String> columnNames = new ArrayList<>();
        try {
            cursor = db.rawQuery("pragma table_info(session)", (String[]) null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String column = cursor.getString(cursor.getColumnIndex("name"));
                        if (!TextUtils.isEmpty(column)) {
                            columnNames.add(column);
                        }
                    }
                    if (!columnNames.contains(ImContract.ImSession.PREFERRED_URI)) {
                        Log.i(LOG_TAG, "column preferred_uri for ims6 does not exist");
                        oldVersion = 1;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (oldVersion == 17) {
                        try {
                            db.execSQL("ALTER TABLE message ADD COLUMN is_vm2txt_msg INTEGER DEFAULT 0");
                        } catch (SQLException e) {
                            Log.i(LOG_TAG, "is_vm2txt_msg column already exists");
                        }
                        oldVersion = 18;
                    }
                    if (oldVersion == 18) {
                        try {
                            db.execSQL("ALTER TABLE message ADD COLUMN file_disposition INTEGER DEFAULT 0");
                            db.execSQL("ALTER TABLE message ADD COLUMN playing_length INTEGER DEFAULT 0");
                        } catch (SQLiteException e2) {
                            Log.i(LOG_TAG, "file_disposition or playing_length columns already exists");
                        } catch (SQLException e3) {
                            Log.i(LOG_TAG, "file_disposition column already exists");
                        }
                        oldVersion = 19;
                    }
                    if (oldVersion == 19) {
                        try {
                            db.execSQL("ALTER TABLE imdnrecroute ADD COLUMN message_id INTEGER DEFAULT 0");
                        } catch (SQLException e4) {
                            Log.i(LOG_TAG, "message_id column already exists");
                        }
                        try {
                            db.execSQL("ALTER TABLE notification ADD COLUMN message_id INTEGER DEFAULT 0");
                        } catch (SQLException e5) {
                            Log.i(LOG_TAG, "message_id column already exists");
                        }
                        oldVersion = 20;
                    }
                    if (oldVersion == 20) {
                        try {
                            db.execSQL("ALTER TABLE message ADD COLUMN sim_imsi TEXT DEFAULT ''");
                        } catch (SQLException e6) {
                            Log.i(LOG_TAG, "sim_imsi column already exists");
                        }
                        oldVersion = 21;
                    }
                    if (oldVersion == 21) {
                        try {
                            db.execSQL("ALTER TABLE message ADD COLUMN messaging_tech INTEGER DEFAULT 0");
                        } catch (SQLException e7) {
                            Log.i(LOG_TAG, "messaging_tech column already exists");
                        }
                        oldVersion = 22;
                    }
                    if (oldVersion == 22) {
                        try {
                            db.execSQL("ALTER TABLE message ADD COLUMN suggestion TEXT DEFAULT NULL");
                        } catch (SQLException e8) {
                            Log.i(LOG_TAG, "suggestion column already exists");
                        }
                        try {
                            db.execSQL("ALTER TABLE session ADD COLUMN sim_imsi TEXT DEFAULT NULL");
                        } catch (SQLException e9) {
                            Log.i(LOG_TAG, "sim_imsi column already exists");
                        }
                        oldVersion = 23;
                    }
                    if (oldVersion == 23) {
                        try {
                            db.execSQL("ALTER TABLE session ADD COLUMN icon_uri TEXT DEFAULT NULL;");
                        } catch (SQLException e10) {
                            Log.i(LOG_TAG, "icon_uri column already exists");
                        }
                        oldVersion = 24;
                    }
                    if (oldVersion == 24) {
                        try {
                            db.execSQL("ALTER TABLE session ADD COLUMN is_chatbot_role INTEGER DEFAULT 0;");
                        } catch (SQLException e11) {
                            Log.i(LOG_TAG, "is_chatbot_role column already exists");
                        }
                        oldVersion = 25;
                    }
                    if (oldVersion == 25) {
                        try {
                            db.execSQL("ALTER TABLE message ADD COLUMN maap_traffic_type TEXT DEFAULT NULL");
                        } catch (SQLException e12) {
                            Log.i(LOG_TAG, "maap_traffic_type column already exists");
                        }
                        oldVersion = 26;
                    }
                    if (oldVersion == 26) {
                        try {
                            db.execSQL("ALTER TABLE session ADD COLUMN chat_mode INTEGER DEFAULT 0;");
                        } catch (SQLException e13) {
                            Log.i(LOG_TAG, "chat_mode column already exists");
                        }
                        oldVersion = 27;
                    }
                    if (oldVersion == 27) {
                        try {
                            db.execSQL("ALTER TABLE message ADD COLUMN reference_id TEXT DEFAULT NULL");
                        } catch (SQLException e14) {
                            Log.i(LOG_TAG, "reference_id column already exists");
                        }
                        try {
                            db.execSQL("ALTER TABLE message ADD COLUMN reference_type TEXT DEFAULT NULL");
                        } catch (SQLException e15) {
                            Log.i(LOG_TAG, "reference_type column already exists");
                        }
                        oldVersion = 28;
                    }
                    if (oldVersion == 28) {
                        try {
                            db.execSQL("ALTER TABLE message ADD COLUMN reference_value TEXT DEFAULT NULL");
                        } catch (SQLException e16) {
                            Log.i(LOG_TAG, "reference_value column already exists");
                        }
                        return;
                    }
                    return;
                }
            }
            Log.i(LOG_TAG, "SESSION_TABLE doesn't exist");
            if (cursor != null) {
                cursor.close();
                return;
            }
            return;
        } catch (SQLException sqe) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQL Exception while querying pragma : " + sqe);
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String str = LOG_TAG;
        Log.i(str, "db downgrade : oldVersion=" + oldVersion + " newVersion=" + newVersion);
        updateTable(SESSION_TABLE, db);
        updateTable("message", db);
        updateTable("participant", db);
        updateTable(IMDNRECROUTE_TABLE, db);
        updateTable("notification", db);
    }

    private void createView(SQLiteDatabase db) {
        Log.i(LOG_TAG, "createView()");
        db.execSQL(CREATE_CHAT_MESSAGE_VIEW);
        db.execSQL(CREATE_FILETRANSFER_VIEW);
    }

    /* Debug info: failed to restart local var, previous not found, register: 12 */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:11:0x006c=Splitter:B:11:0x006c, B:40:0x013d=Splitter:B:40:0x013d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateTable(java.lang.String r13, android.database.sqlite.SQLiteDatabase r14) {
        /*
            r12 = this;
            java.lang.String r0 = ","
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r13)
            java.lang.String r2 = "_bkp"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "pragma table_info("
            r2.append(r3)
            r2.append(r13)
            java.lang.String r3 = ")"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            java.lang.String r4 = "SELECT "
            r3.<init>(r4)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "INSERT INTO "
            r5.append(r6)
            r5.append(r13)
            java.lang.String r6 = "("
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            r6 = 0
            r14.beginTransaction()
            r7 = 0
            android.database.Cursor r8 = r14.rawQuery(r2, r7)     // Catch:{ SQLException -> 0x0174 }
            if (r8 == 0) goto L_0x013d
            int r9 = r8.getCount()     // Catch:{ all -> 0x0166 }
            if (r9 > 0) goto L_0x0064
            goto L_0x013d
        L_0x0064:
            boolean r9 = r8.moveToFirst()     // Catch:{ all -> 0x0166 }
            java.lang.String r10 = "name"
            if (r9 == 0) goto L_0x007d
        L_0x006c:
            int r9 = r8.getColumnIndex(r10)     // Catch:{ all -> 0x0166 }
            java.lang.String r9 = r8.getString(r9)     // Catch:{ all -> 0x0166 }
            r5.add(r9)     // Catch:{ all -> 0x0166 }
            boolean r9 = r8.moveToNext()     // Catch:{ all -> 0x0166 }
            if (r9 != 0) goto L_0x006c
        L_0x007d:
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x0166 }
            r9.<init>()     // Catch:{ all -> 0x0166 }
            java.lang.String r11 = "ALTER TABLE "
            r9.append(r11)     // Catch:{ all -> 0x0166 }
            r9.append(r13)     // Catch:{ all -> 0x0166 }
            java.lang.String r11 = " RENAME TO "
            r9.append(r11)     // Catch:{ all -> 0x0166 }
            r9.append(r1)     // Catch:{ all -> 0x0166 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0166 }
            r14.execSQL(r9)     // Catch:{ all -> 0x0166 }
            java.lang.String r9 = r12.createTable(r13)     // Catch:{ all -> 0x0166 }
            r14.execSQL(r9)     // Catch:{ all -> 0x0166 }
            android.database.Cursor r7 = r14.rawQuery(r2, r7)     // Catch:{ all -> 0x0166 }
            if (r7 == 0) goto L_0x00da
            boolean r9 = r7.moveToFirst()     // Catch:{ all -> 0x00ce }
            if (r9 == 0) goto L_0x00da
        L_0x00ac:
            int r9 = r7.getColumnIndex(r10)     // Catch:{ all -> 0x00ce }
            java.lang.String r9 = r7.getString(r9)     // Catch:{ all -> 0x00ce }
            boolean r11 = r5.contains(r9)     // Catch:{ all -> 0x00ce }
            if (r11 == 0) goto L_0x00c7
            r6 = 1
            r3.append(r9)     // Catch:{ all -> 0x00ce }
            r3.append(r0)     // Catch:{ all -> 0x00ce }
            r4.append(r9)     // Catch:{ all -> 0x00ce }
            r4.append(r0)     // Catch:{ all -> 0x00ce }
        L_0x00c7:
            boolean r11 = r7.moveToNext()     // Catch:{ all -> 0x00ce }
            if (r11 != 0) goto L_0x00ac
            goto L_0x00da
        L_0x00ce:
            r0 = move-exception
            if (r7 == 0) goto L_0x00d9
            r7.close()     // Catch:{ all -> 0x00d5 }
            goto L_0x00d9
        L_0x00d5:
            r9 = move-exception
            r0.addSuppressed(r9)     // Catch:{ all -> 0x0166 }
        L_0x00d9:
            throw r0     // Catch:{ all -> 0x0166 }
        L_0x00da:
            if (r7 == 0) goto L_0x00df
            r7.close()     // Catch:{ all -> 0x0166 }
        L_0x00df:
            if (r6 == 0) goto L_0x0120
            int r0 = r3.length()     // Catch:{ all -> 0x0166 }
            int r0 = r0 + -1
            r3.deleteCharAt(r0)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = " FROM "
            r3.append(r0)     // Catch:{ all -> 0x0166 }
            r3.append(r1)     // Catch:{ all -> 0x0166 }
            int r0 = r4.length()     // Catch:{ all -> 0x0166 }
            int r0 = r0 + -1
            r4.deleteCharAt(r0)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = ") "
            r4.append(r0)     // Catch:{ all -> 0x0166 }
            r4.append(r3)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0166 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x0166 }
            r7.<init>()     // Catch:{ all -> 0x0166 }
            java.lang.String r9 = "Update table: "
            r7.append(r9)     // Catch:{ all -> 0x0166 }
            r7.append(r4)     // Catch:{ all -> 0x0166 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x0166 }
            android.util.Log.i(r0, r7)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = r4.toString()     // Catch:{ all -> 0x0166 }
            r14.execSQL(r0)     // Catch:{ all -> 0x0166 }
        L_0x0120:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x0166 }
            r0.<init>()     // Catch:{ all -> 0x0166 }
            java.lang.String r7 = "DROP TABLE "
            r0.append(r7)     // Catch:{ all -> 0x0166 }
            r0.append(r1)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x0166 }
            r14.execSQL(r0)     // Catch:{ all -> 0x0166 }
            r14.setTransactionSuccessful()     // Catch:{ all -> 0x0166 }
            if (r8 == 0) goto L_0x013c
            r8.close()     // Catch:{ SQLException -> 0x0174 }
        L_0x013c:
            goto L_0x0194
        L_0x013d:
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0166 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x0166 }
            r7.<init>()     // Catch:{ all -> 0x0166 }
            r7.append(r13)     // Catch:{ all -> 0x0166 }
            java.lang.String r9 = " doesn't exist"
            r7.append(r9)     // Catch:{ all -> 0x0166 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x0166 }
            android.util.Log.i(r0, r7)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = r12.createTable(r13)     // Catch:{ all -> 0x0166 }
            r14.execSQL(r0)     // Catch:{ all -> 0x0166 }
            r14.setTransactionSuccessful()     // Catch:{ all -> 0x0166 }
            if (r8 == 0) goto L_0x0162
            r8.close()     // Catch:{ SQLException -> 0x0174 }
        L_0x0162:
            r14.endTransaction()
            return
        L_0x0166:
            r0 = move-exception
            if (r8 == 0) goto L_0x0171
            r8.close()     // Catch:{ all -> 0x016d }
            goto L_0x0171
        L_0x016d:
            r7 = move-exception
            r0.addSuppressed(r7)     // Catch:{ SQLException -> 0x0174 }
        L_0x0171:
            throw r0     // Catch:{ SQLException -> 0x0174 }
        L_0x0172:
            r0 = move-exception
            goto L_0x0199
        L_0x0174:
            r0 = move-exception
            java.lang.String r7 = LOG_TAG     // Catch:{ all -> 0x0172 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0172 }
            r8.<init>()     // Catch:{ all -> 0x0172 }
            java.lang.String r9 = "SQL Exception while updating "
            r8.append(r9)     // Catch:{ all -> 0x0172 }
            r8.append(r13)     // Catch:{ all -> 0x0172 }
            java.lang.String r9 = ": "
            r8.append(r9)     // Catch:{ all -> 0x0172 }
            r8.append(r0)     // Catch:{ all -> 0x0172 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0172 }
            android.util.Log.e(r7, r8)     // Catch:{ all -> 0x0172 }
        L_0x0194:
            r14.endTransaction()
            return
        L_0x0199:
            r14.endTransaction()
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImDBHelper.updateTable(java.lang.String, android.database.sqlite.SQLiteDatabase):void");
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String createTable(java.lang.String r6) {
        /*
            r5 = this;
            int r0 = r6.hashCode()
            r1 = 4
            r2 = 3
            r3 = 2
            r4 = 1
            switch(r0) {
                case 595233003: goto L_0x0036;
                case 767422259: goto L_0x002b;
                case 954925063: goto L_0x0021;
                case 1984987798: goto L_0x0016;
                case 2145035879: goto L_0x000c;
                default: goto L_0x000b;
            }
        L_0x000b:
            goto L_0x0040
        L_0x000c:
            java.lang.String r0 = "imdnrecroute"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x000b
            r0 = r2
            goto L_0x0041
        L_0x0016:
            java.lang.String r0 = "session"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x000b
            r0 = 0
            goto L_0x0041
        L_0x0021:
            java.lang.String r0 = "message"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x000b
            r0 = r4
            goto L_0x0041
        L_0x002b:
            java.lang.String r0 = "participant"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x000b
            r0 = r3
            goto L_0x0041
        L_0x0036:
            java.lang.String r0 = "notification"
            boolean r0 = r6.equals(r0)
            if (r0 == 0) goto L_0x000b
            r0 = r1
            goto L_0x0041
        L_0x0040:
            r0 = -1
        L_0x0041:
            if (r0 == 0) goto L_0x005a
            if (r0 == r4) goto L_0x0057
            if (r0 == r3) goto L_0x0054
            if (r0 == r2) goto L_0x0051
            if (r0 == r1) goto L_0x004e
            java.lang.String r0 = ""
            return r0
        L_0x004e:
            java.lang.String r0 = "CREATE TABLE notification(id INTEGER PRIMARY KEY AUTOINCREMENT,message_id INTEGER DEFAULT 0,imdn_id TEXT, sender_uri TEXT,status INTEGER DEFAULT 0,timestamp LONG);"
            return r0
        L_0x0051:
            java.lang.String r0 = "CREATE TABLE imdnrecroute(_id INTEGER PRIMARY KEY AUTOINCREMENT,message_id INTEGER DEFAULT 0,imdn_id TEXT,uri TEXT,alias TEXT);"
            return r0
        L_0x0054:
            java.lang.String r0 = "CREATE TABLE participant(_id INTEGER PRIMARY KEY AUTOINCREMENT,chat_id TEXT,status INTEGER,type INTEGER,uri TEXT,alias TEXT);"
            return r0
        L_0x0057:
            java.lang.String r0 = "CREATE TABLE message(_id INTEGER PRIMARY KEY AUTOINCREMENT,is_filetransfer INTEGER,direction INTEGER,chat_id TEXT NOT NULL,remote_uri TEXT,sender_alias TEXT,content_type TEXT,inserted_timestamp LONG,ext_info TEXT,body TEXT,suggestion TEXT,notification_disposition_mask INTEGER,notification_status INTEGER DEFAULT 0,disposition_notification_status INTEGER DEFAULT 0,sent_timestamp LONG,delivered_timestamp LONG,displayed_timestamp LONG,message_type INTEGER,message_isslm INTEGER,status INTEGER,not_displayed_counter INTEGER,imdn_message_id TEXT, imdn_original_to TEXT, conversation_id TEXT, contribution_id TEXT, file_path TEXT,file_name TEXT,file_size LONG,file_transfer_id TEXT,state INTEGER,reason INTEGER,bytes_transf LONG,ft_status INTEGER,thumbnail_path TEXT,is_resumable INTEGER,transfer_mech INTEGER DEFAULT 0,data_url TEXT,request_message_id TEXT,is_resizable INTEGER DEFAULT 0,is_broadcast_msg INTEGER DEFAULT 0,is_vm2txt_msg INTEGER DEFAULT 0,extra_ft INTEGER DEFAULT 0,flag_mask INTEGER DEFAULT 0,revocation_status INTEGER DEFAULT 0,sim_imsi TEXT DEFAULT '',device_id TEXT DEFAULT NULL,file_disposition INTEGER,playing_length INTEGER DEFAULT 0,maap_traffic_type TEXT DEFAULT NULL,reference_id TEXT DEFAULT NULL,reference_type TEXT DEFAULT NULL,reference_value TEXT DEFAULT NULL,messaging_tech INTEGER DEFAULT 0);"
            return r0
        L_0x005a:
            java.lang.String r0 = "CREATE TABLE session(_id INTEGER PRIMARY KEY AUTOINCREMENT,chat_id TEXT,own_sim_imsi TEXT,own_group_alias TEXT,direction INTEGER, chat_type INTEGER, conversation_id TEXT, contribution_id TEXT, is_group_chat INTEGER,is_ft_group_chat INTEGER DEFAULT 1, status INTEGER,subject TEXT,is_muted INTEGER,max_participants_count INTEGER,imdn_notifications_availability INTEGER DEFAULT 1, session_uri TEXT DEFAULT NULL,is_broadcast_msg INTEGER, inserted_time_stamp LONG, preferred_uri TEXT DEFAULT NULL,is_reusable INTEGER DEFAULT 1,subject_participant TEXT DEFAULT NULL,subject_timestamp LONG,icon_path TEXT DEFAULT NULL,icon_participant TEXT DEFAULT NULL,icon_timestamp LONG,icon_uri TEXT DEFAULT NULL,sim_imsi TEXT DEFAULT NULL,is_chatbot_role INTEGER DEFAULT 0,chat_mode INTEGER DEFAULT 0);"
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImDBHelper.createTable(java.lang.String):java.lang.String");
    }

    public ContentValues makeImMessageRow(ImMessage message) {
        ContentValues cv = new ContentValues();
        cv.put("chat_id", message.getChatId());
        cv.put(ImContract.ChatItem.IS_FILE_TRANSFER, 0);
        cv.put("remote_uri", message.getRemoteUri() != null ? message.getRemoteUri().toString() : null);
        cv.put(ImContract.ChatItem.USER_ALIAS, message.getUserAlias());
        cv.put("body", message.getBody());
        cv.put(ImContract.Message.SUGGESTION, message.getSuggestion());
        cv.put(ImContract.Message.NOTIFICATION_DISPOSITION_MASK, Integer.valueOf(NotificationStatus.encode(message.getDispositionNotification())));
        cv.put("notification_status", Integer.valueOf(message.getNotificationStatus().getId()));
        cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(message.getDesiredNotificationStatus().getId()));
        cv.put(ImContract.ChatItem.INSERTED_TIMESTAMP, Long.valueOf(message.getInsertedTimestamp()));
        cv.put(ImContract.Message.SENT_TIMESTAMP, Long.valueOf(message.getSentTimestamp()));
        cv.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(message.getDeliveredTimestamp()));
        if (message.getExtInfo() != null) {
            cv.put(ImContract.ChatItem.EXT_INFO, message.getExtInfo());
        }
        cv.put(ImContract.Message.DISPLAYED_TIMESTAMP, message.getDisplayedTimestamp());
        cv.put("message_type", Integer.valueOf(message.getType().getId()));
        cv.put(ImContract.Message.MESSAGE_ISSLM, Boolean.valueOf(message.getIsSlmSvcMsg()));
        cv.put("status", Integer.valueOf(message.getStatus().getId()));
        cv.put("direction", Integer.valueOf(message.getDirection().getId()));
        cv.put("content_type", message.getContentType());
        cv.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(message.getNotDisplayedCounter()));
        cv.put(ImContract.Message.IMDN_MESSAGE_ID, message.getImdnId());
        cv.put(ImContract.Message.IMDN_ORIGINAL_TO, message.getImdnOriginalTo());
        cv.put("request_message_id", message.getRequestMessageId());
        cv.put("is_broadcast_msg", Integer.valueOf(message.isBroadcastMsg() ? 1 : 0));
        cv.put(ImContract.Message.IS_VM2TXT_MSG, Integer.valueOf(message.isVM2TextMsg() ? 1 : 0));
        cv.put("conversation_id", message.getConversationId());
        cv.put("contribution_id", message.getContributionId());
        cv.put("device_id", message.getDeviceId());
        cv.put(ImContract.Message.FLAG_MASK, Integer.valueOf(message.getFlagMask()));
        cv.put(ImContract.Message.REVOCATION_STATUS, Integer.valueOf(message.getRevocationStatus().getId()));
        cv.put("sim_imsi", message.getOwnIMSI());
        cv.put("maap_traffic_type", message.getMaapTrafficType());
        cv.put(ImContract.Message.MESSAGING_TECH, Integer.valueOf(message.getMessagingTech().getId()));
        cv.put(ImContract.Message.REFERENCE_ID, message.getReferenceId());
        cv.put(ImContract.Message.REFERENCE_TYPE, message.getReferenceType());
        cv.put(ImContract.Message.REFERENCE_VALUE, message.getReferenceValue());
        return cv;
    }

    public ContentValues makeFtMessageRow(FtMessage message) {
        ContentValues cv = new ContentValues();
        cv.put(ImContract.ChatItem.IS_FILE_TRANSFER, 1);
        cv.put("direction", Integer.valueOf(message.getDirection().getId()));
        cv.put("chat_id", message.getChatId());
        cv.put("remote_uri", message.getRemoteUri() != null ? message.getRemoteUri().toString() : null);
        cv.put(ImContract.ChatItem.USER_ALIAS, message.getUserAlias());
        cv.put("content_type", message.getContentType());
        cv.put(ImContract.ChatItem.INSERTED_TIMESTAMP, Long.valueOf(message.getInsertedTimestamp()));
        cv.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(message.getDeliveredTimestamp()));
        if (message.getExtInfo() != null) {
            cv.put(ImContract.ChatItem.EXT_INFO, message.getExtInfo());
        }
        cv.put("file_path", message.getFilePath());
        cv.put("file_name", message.getFileName());
        cv.put("file_size", Long.valueOf(message.getFileSize()));
        if (message.getFileDisposition() != null) {
            cv.put("file_disposition", Integer.valueOf(message.getFileDisposition().toInt()));
        }
        cv.put("playing_length", Integer.valueOf(message.getPlayingLength()));
        cv.put(ImContract.CsSession.FILE_TRANSFER_ID, message.getFileTransferId());
        cv.put("state", Integer.valueOf(message.getStateId()));
        cv.put("reason", Integer.valueOf(message.getReasonId()));
        cv.put(ImContract.CsSession.BYTES_TRANSFERED, Long.valueOf(message.getTransferredBytes()));
        cv.put(ImContract.CsSession.STATUS, Integer.valueOf(message.getStatus().getId()));
        cv.put(ImContract.CsSession.THUMBNAIL_PATH, message.getThumbnailPath());
        cv.put(ImContract.CsSession.IS_RESUMABLE, Integer.valueOf(message.getResumableOptionCode()));
        cv.put(ImContract.CsSession.TRANSFER_MECH, Integer.valueOf(message.getTransferMech()));
        if (message instanceof FtHttpIncomingMessage) {
            cv.put(ImContract.CsSession.DATA_URL, ((FtHttpIncomingMessage) message).getDataUrl());
        }
        cv.put(ImContract.Message.NOTIFICATION_DISPOSITION_MASK, Integer.valueOf(NotificationStatus.encode(message.getDispositionNotification())));
        cv.put("notification_status", Integer.valueOf(message.getNotificationStatus().getId()));
        cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(message.getDesiredNotificationStatus().getId()));
        cv.put(ImContract.Message.IMDN_MESSAGE_ID, message.getImdnId());
        cv.put(ImContract.Message.MESSAGE_ISSLM, Boolean.valueOf(message.getIsSlmSvcMsg()));
        cv.put(ImContract.Message.DISPLAYED_TIMESTAMP, message.getDisplayedTimestamp());
        cv.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(message.getNotDisplayedCounter()));
        cv.put("request_message_id", message.getRequestMessageId());
        cv.put("is_resizable", Boolean.valueOf(message.getIsResizable()));
        cv.put("body", message.getBody());
        cv.put(ImContract.Message.SENT_TIMESTAMP, Long.valueOf(message.getSentTimestamp()));
        cv.put("message_type", Integer.valueOf(message.getType().getId()));
        cv.put("is_broadcast_msg", Integer.valueOf(message.isBroadcastMsg() ? 1 : 0));
        cv.put(ImContract.CsSession.EXTRA_FT, Boolean.valueOf(message.getExtraFt()));
        cv.put("conversation_id", message.getConversationId());
        cv.put("contribution_id", message.getContributionId());
        cv.put("device_id", message.getDeviceId());
        cv.put(ImContract.Message.FLAG_MASK, Integer.valueOf(message.getFlagMask()));
        cv.put(ImContract.Message.REVOCATION_STATUS, Integer.valueOf(message.getRevocationStatus().getId()));
        cv.put("sim_imsi", message.getOwnIMSI());
        cv.put("maap_traffic_type", message.getMaapTrafficType());
        cv.put(ImContract.Message.MESSAGING_TECH, Integer.valueOf(message.getMessagingTech().getId()));
        return cv;
    }

    public ContentValues makeImdnRecRouteRow(ImImdnRecRoute imdnRecRoute) {
        ContentValues cv = new ContentValues();
        cv.put("message_id", Integer.valueOf(imdnRecRoute.getMessageId()));
        cv.put("imdn_id", imdnRecRoute.getImdnMsgId());
        cv.put("uri", imdnRecRoute.getRecordRouteUri());
        cv.put("alias", imdnRecRoute.getRecordRouteDispName());
        return cv;
    }

    public ContentValues makeMessageNotificationRow(MessageBase message, String sender) {
        ContentValues cv = new ContentValues();
        cv.put("message_id", Integer.valueOf(message.getId()));
        cv.put("imdn_id", message.getImdnId());
        cv.put(ImContract.MessageNotification.SENDER_URI, sender);
        cv.put("timestamp", Long.valueOf(message.getSentTimestamp()));
        return cv;
    }

    public ContentValues makeMessageNotificationUpdateRow(long timeStamp, int status) {
        ContentValues cv = new ContentValues();
        cv.put("timestamp", Long.valueOf(timeStamp));
        cv.put("status", Integer.valueOf(status));
        return cv;
    }

    public ImMessage makeImMessage(Cursor cursor, ImModule imModule) {
        String uriStr = cursor.getString(cursor.getColumnIndexOrThrow("remote_uri"));
        String contentType = cursor.getString(cursor.getColumnIndexOrThrow("content_type"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        int phoneId = imModule.getPhoneIdByIMSI(cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")));
        boolean z = false;
        ImMessage.Builder builder = (ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(imModule)).imsService(ImsRegistry.getHandlerFactory().getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(imModule.getImProcessor()).config(imModule.getImConfig(phoneId))).uriGenerator(imModule.getUriGenerator(phoneId))).id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")))).direction(ImDirection.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("direction"))))).chatId(cursor.getString(cursor.getColumnIndexOrThrow("chat_id")))).remoteUri(uriStr != null ? ImsUri.parse(uriStr) : null)).body(body)).suggestion(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.SUGGESTION)))).userAlias(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.USER_ALIAS)))).contentType(contentType)).insertedTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.INSERTED_TIMESTAMP)))).status(ImConstants.Status.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("status"))))).dispNotification(NotificationStatus.decode(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOTIFICATION_DISPOSITION_MASK))))).notificationStatus(NotificationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("notification_status"))))).desiredNotificationStatus(NotificationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS))))).sentTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.SENT_TIMESTAMP)))).deliveredTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.DELIVERED_TIMESTAMP)))).displayedTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.DISPLAYED_TIMESTAMP)))).type(ImConstants.Type.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("message_type"))))).isSlmSvcMsg(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGE_ISSLM)) == 1)).imdnId(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_MESSAGE_ID)))).imdnIdOriginalTo(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_ORIGINAL_TO)))).notDisplayedCounter(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOT_DISPLAYED_COUNTER)))).requestMessageId(cursor.getString(cursor.getColumnIndexOrThrow("request_message_id")))).isBroadcastMsg(cursor.getInt(cursor.getColumnIndexOrThrow("is_broadcast_msg")) == 1);
        if (cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.IS_VM2TXT_MSG)) == 1) {
            z = true;
        }
        return ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) builder.isVM2TextMsg(z)).mnoStrategy(imModule.getRcsStrategy(phoneId))).conversationId(cursor.getString(cursor.getColumnIndexOrThrow("conversation_id")))).contributionId(cursor.getString(cursor.getColumnIndexOrThrow("contribution_id")))).deviceId(cursor.getString(cursor.getColumnIndexOrThrow("device_id")))).flagMask(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.FLAG_MASK)))).revocationStatus(ImConstants.RevocationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.REVOCATION_STATUS))))).simIMSI(cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")))).maapTrafficType(cursor.getString(cursor.getColumnIndexOrThrow("maap_traffic_type")))).messagingTech(ImConstants.MessagingTech.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGING_TECH))))).referenceId(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.REFERENCE_ID)))).referenceType(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.REFERENCE_TYPE)))).referenceValue(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.REFERENCE_VALUE)))).build();
    }

    public FtMessage makeFtMessage(Cursor cursor, ImModule imModule) {
        FtMessage.Builder<?> builder;
        int mech = cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.TRANSFER_MECH));
        ImDirection direction = ImDirection.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("direction")));
        String uriStr = cursor.getString(cursor.getColumnIndexOrThrow("remote_uri"));
        int phoneId = imModule.getPhoneIdByIMSI(cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")));
        if (mech == 0) {
            builder = FtMsrpMessage.builder();
        } else if (direction == ImDirection.OUTGOING) {
            builder = FtHttpOutgoingMessage.builder();
        } else {
            builder = FtHttpIncomingMessage.builder();
        }
        FileDisposition fileDisposition = null;
        boolean z = false;
        FtMessage.Builder builder2 = (FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) builder.module(imModule)).imsService(ImsRegistry.getHandlerFactory().getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(imModule.getFtProcessor()).looper(imModule.getLooper()).config(imModule.getImConfig(phoneId))).thumbnailTool(imModule.getFtProcessor().getThumbnailTool())).uriGenerator(imModule.getUriGenerator(phoneId))).id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")))).direction(ImDirection.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("direction"))))).chatId(cursor.getString(cursor.getColumnIndexOrThrow("chat_id")))).remoteUri(uriStr != null ? ImsUri.parse(uriStr) : null)).userAlias(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.USER_ALIAS)))).contentType(cursor.getString(cursor.getColumnIndexOrThrow("content_type")))).sentTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.SENT_TIMESTAMP)))).insertedTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.INSERTED_TIMESTAMP)))).deliveredTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.DELIVERED_TIMESTAMP)))).displayedTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.DISPLAYED_TIMESTAMP)))).type(ImConstants.Type.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("message_type"))))).isSlmSvcMsg(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGE_ISSLM)) == 1)).filePath(cursor.getString(cursor.getColumnIndexOrThrow("file_path"))).fileName(cursor.getString(cursor.getColumnIndexOrThrow("file_name"))).fileSize((long) cursor.getInt(cursor.getColumnIndexOrThrow("file_size"))).fileTransferId(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_TRANSFER_ID))).transferredBytes((long) cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.BYTES_TRANSFERED))).setCancelReason(cursor.getInt(cursor.getColumnIndexOrThrow("reason"))).status(ImConstants.Status.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS))))).thumbnailPath(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH))).setResumableOptions(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.IS_RESUMABLE))).imdnId(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_MESSAGE_ID)))).imdnIdOriginalTo(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_ORIGINAL_TO)))).dispNotification(NotificationStatus.decode(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOTIFICATION_DISPOSITION_MASK))))).notificationStatus(NotificationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("notification_status"))))).desiredNotificationStatus(NotificationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS))))).setState(cursor.getInt(cursor.getColumnIndexOrThrow("state"))).notDisplayedCounter(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOT_DISPLAYED_COUNTER)))).requestMessageId(cursor.getString(cursor.getColumnIndexOrThrow("request_message_id")))).isResizable(cursor.getInt(cursor.getColumnIndexOrThrow("is_resizable")) == 1).isBroadcastMsg(cursor.getInt(cursor.getColumnIndexOrThrow("is_broadcast_msg")) == 1)).body(cursor.getString(cursor.getColumnIndexOrThrow("body")))).mnoStrategy(imModule.getRcsStrategy(phoneId));
        if (cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.EXTRA_FT)) == 1) {
            z = true;
        }
        FtMessage.Builder builder3 = (FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) ((FtMessage.Builder) builder2.extraFt(z)).conversationId(cursor.getString(cursor.getColumnIndexOrThrow("conversation_id")))).contributionId(cursor.getString(cursor.getColumnIndexOrThrow("contribution_id")))).deviceId(cursor.getString(cursor.getColumnIndexOrThrow("device_id")))).flagMask(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.FLAG_MASK)))).revocationStatus(ImConstants.RevocationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.REVOCATION_STATUS))));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow("file_disposition"))) {
            fileDisposition = FileDisposition.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("file_disposition")));
        }
        ((FtMessage.Builder) ((FtMessage.Builder) builder3.setFileDisposition(fileDisposition).setPlayingLength(cursor.getInt(cursor.getColumnIndexOrThrow("playing_length"))).simIMSI(cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")))).maapTrafficType(cursor.getString(cursor.getColumnIndexOrThrow("maap_traffic_type")))).messagingTech(ImConstants.MessagingTech.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGING_TECH))));
        if (builder instanceof FtHttpIncomingMessage.Builder) {
            ((FtHttpIncomingMessage.Builder) builder).dataUrl(cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.DATA_URL)));
        }
        return builder.build();
    }

    public ImParticipant makeParticipant(Cursor cursor) {
        return new ImParticipant(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getString(cursor.getColumnIndexOrThrow("chat_id")), ImParticipant.Status.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("status"))), ImParticipant.Type.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("type"))), ImsUri.parse(cursor.getString(cursor.getColumnIndexOrThrow("uri"))), cursor.getString(cursor.getColumnIndexOrThrow("alias")));
    }

    public ImImdnRecRoute makeImdnRecRoute(Cursor cursor) {
        return new ImImdnRecRoute(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getInt(cursor.getColumnIndexOrThrow("message_id")), cursor.getString(cursor.getColumnIndexOrThrow("imdn_id")), cursor.getString(cursor.getColumnIndexOrThrow("uri")), cursor.getString(cursor.getColumnIndexOrThrow("alias")));
    }
}
