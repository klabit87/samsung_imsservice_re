package com.sec.internal.ims.servicemodules.im;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImPersister {
    private static final int DATABASE_VERSION = 29;
    private static final String LOG_TAG = ImPersister.class.getSimpleName();
    private final Context mContext;
    private final ImDBHelper mImDBHelper = new ImDBHelper(this.mContext, 29);
    private final ImModule mImModule;
    private final ContentResolver mResolver;

    public ImPersister(Context context, ImModule imServiceModule) {
        Log.i(LOG_TAG, "ImPersister create");
        this.mContext = context;
        this.mImModule = imServiceModule;
        this.mResolver = context.getContentResolver();
        clearDeletedParticipants();
        closeDB();
    }

    public Cursor querySessions(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                cursor = db.query(ImDBHelper.SESSION_TABLE, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
                setTransactionSuccessful(db);
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while querying all sessions. " + e);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    public Cursor queryMessages(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                cursor = db.query("message", projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
                setTransactionSuccessful(db);
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while querying all sessions. " + e);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    private Cursor queryMessagesForTapi(String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            cursor = db.query(table, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
            setTransactionSuccessful(db);
        } catch (SQLException e) {
            String str = LOG_TAG;
            Log.e(str, "SQL exception while queryMessagesForTapi. " + e);
        } catch (Throwable th) {
            endTransaction(db);
            throw th;
        }
        endTransaction(db);
        return cursor;
    }

    public Cursor queryChatMessagesForTapi(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return queryMessagesForTapi(ImDBHelper.CHAT_MESSAGE_VIEW, projection, selection, selectionArgs, sortOrder);
    }

    public Cursor queryFtMessagesForTapi(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return queryMessagesForTapi(ImDBHelper.FILETRANSFER_VIEW, projection, selection, selectionArgs, sortOrder);
    }

    public Cursor queryParticipants(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                cursor = db.query("participant", projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
                setTransactionSuccessful(db);
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while querying all sessions. " + e);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    public Cursor queryMessageNotification(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                cursor = db.query("notification", projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
                setTransactionSuccessful(db);
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while querying all sessions. " + e);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    private Cursor query(String inTable, WhereClauseArgs inWhere, String[] projection, String groupBy, String sortOrder) {
        Cursor cursor = null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String str = inTable;
        qb.setTables(inTable);
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            if (inWhere != null) {
                try {
                    cursor = qb.query(db, projection, inWhere.getWhereClause(), inWhere.getWhereArgs(), groupBy, (String) null, sortOrder);
                } catch (SQLException e) {
                    String str2 = LOG_TAG;
                    Log.e(str2, "SQL exception while querying " + e);
                } catch (Throwable th) {
                    endTransaction(db);
                    throw th;
                }
            } else {
                cursor = qb.query(db, projection, (String) null, (String[]) null, groupBy, (String) null, sortOrder);
            }
            setTransactionSuccessful(db);
            endTransaction(db);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    private Cursor query(String inTable, String inWhere, String[] projection, String groupBy, String sortOrder) {
        return query(inTable, new WhereClauseArgs(inWhere), projection, groupBy, sortOrder);
    }

    private void update(String table, List<Pair<ContentValues, WhereClauseArgs>> contentValueAndWhereList) {
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Pair<ContentValues, WhereClauseArgs> pair : contentValueAndWhereList) {
                    if (pair.second != null) {
                        db.update(table, (ContentValues) pair.first, ((WhereClauseArgs) pair.second).getWhereClause(), ((WhereClauseArgs) pair.second).getWhereArgs());
                    } else {
                        db.update(table, (ContentValues) pair.first, (String) null, (String[]) null);
                    }
                }
                setTransactionSuccessful(db);
            } catch (SQLiteFullException e) {
                String str = LOG_TAG;
                Log.e(str, "SQLiteOutOfMemoryException while update. " + e);
                this.mImModule.notifyDeviceOutOfMemory();
            } catch (SQLException e2) {
                String str2 = LOG_TAG;
                Log.e(str2, "SQL exception while update. " + e2);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
        } catch (SQLiteDiskIOException e3) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e3);
        }
    }

    private void update(String table, ContentValues cv, String where) {
        List<Pair<ContentValues, WhereClauseArgs>> list = new ArrayList<>();
        list.add(new Pair(cv, new WhereClauseArgs(where)));
        update(table, list);
    }

    private void delete(String table, String where) {
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                db.delete(table, where, (String[]) null);
                setTransactionSuccessful(db);
            } catch (SQLiteFullException e) {
                String str = LOG_TAG;
                Log.e(str, "SQLiteOutOfMemoryException while delete. " + e);
                this.mImModule.notifyDeviceOutOfMemory();
            } catch (SQLException e2) {
                String str2 = LOG_TAG;
                Log.e(str2, "SQL exception while delete. " + e2);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
        } catch (SQLiteDiskIOException | IllegalStateException e3) {
            String str3 = LOG_TAG;
            Log.e(str3, "Exception : " + e3);
        }
    }

    private List<MessageBase> queryMessages(String inWhere) {
        List<MessageBase> list = new ArrayList<>();
        Cursor cursor = query("message", inWhere, (String[]) null, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return list;
        }
        while (cursor.moveToNext()) {
            try {
                boolean isFileTransfer = true;
                if (cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) != 1) {
                    isFileTransfer = false;
                }
                if (isFileTransfer) {
                    list.add(this.mImDBHelper.makeFtMessage(cursor, this.mImModule));
                } else {
                    list.add(this.mImDBHelper.makeImMessage(cursor, this.mImModule));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
        throw th;
    }

    public List<ChatData> querySessions(String inWhere) {
        List<ChatData> list = new ArrayList<>();
        Cursor cursor = query(ImDBHelper.SESSION_TABLE, inWhere, (String[]) null, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return list;
        }
        while (cursor.moveToNext()) {
            try {
                list.add(ImDBHelper.makeSession(cursor));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
        throw th;
    }

    private List<ImParticipant> queryParticipants(String inWhere) {
        List<ImParticipant> list = new ArrayList<>();
        Cursor cursor = query("participant", inWhere, (String[]) null, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return list;
        }
        while (cursor.moveToNext()) {
            try {
                list.add(this.mImDBHelper.makeParticipant(cursor));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
        throw th;
    }

    private List<ImImdnRecRoute> queryImImdnRecRoutes(String inWhere) {
        List<ImImdnRecRoute> list = new ArrayList<>();
        Cursor cursor = query(ImDBHelper.IMDNRECROUTE_TABLE, inWhere, (String[]) null, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return list;
        }
        while (cursor.moveToNext()) {
            try {
                list.add(this.mImDBHelper.makeImdnRecRoute(cursor));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
        throw th;
    }

    public MessageBase queryMessage(String msgId) {
        List<MessageBase> ret = queryMessages("_id = '" + msgId + "'");
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    /* access modifiers changed from: protected */
    public void insertSession(ChatData chatData) {
        String str = LOG_TAG;
        IMSLog.s(str, "insertSession: " + chatData);
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            ContentValues cv = ImDBHelper.makeSessionRow(chatData);
            db.beginTransaction();
            try {
                long rowId = db.insert(ImDBHelper.SESSION_TABLE, (String) null, cv);
                if (rowId != -1) {
                    String str2 = LOG_TAG;
                    Log.i(str2, "Set chat id " + rowId + " (" + chatData.getChatId() + ")");
                    chatData.setId((int) rowId);
                    setTransactionSuccessful(db);
                } else {
                    Log.e(LOG_TAG, "SQL exception while inserting a session.");
                }
            } finally {
                endTransaction(db);
            }
        } catch (SQLiteDiskIOException e) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e);
        }
    }

    /* access modifiers changed from: protected */
    public void onSessionUpdated(ChatData chatData) {
        String str = LOG_TAG;
        IMSLog.s(str, "onSessionUpdated: " + chatData);
        update(ImDBHelper.SESSION_TABLE, ImDBHelper.makeSessionRow(chatData), "_id = " + chatData.getId());
        String chatId = chatData.getChatId();
        if (this.mResolver != null && chatId != null) {
            this.mResolver.notifyChange(Uri.parse("content://com.samsung.rcs.im/chat/" + chatId), (ContentObserver) null);
            if (chatData.getIconData() != null && (chatData.getState() == ChatData.State.ACTIVE || chatData.getState() == ChatData.State.NONE)) {
                Uri storeUri = Uri.parse("content://com.samsung.rcs.cmstore/chat/" + chatId);
                String str2 = LOG_TAG;
                Log.i(str2, "onSessionUpdated, storeUri: " + storeUri + ", iconPath: " + chatData.getIconData().getIconLocation());
                this.mResolver.notifyChange(storeUri, (ContentObserver) null);
            }
            String str3 = LOG_TAG;
            Log.i(str3, "onSessionUpdated: notifyChange to " + chatId + "(state=" + chatData.getState() + ")");
        }
    }

    private void deleteSession(ChatData chatData) {
        String str = LOG_TAG;
        IMSLog.s(str, "deleteSession: " + chatData);
        delete(ImDBHelper.SESSION_TABLE, "_id=" + chatData.getId());
    }

    public List<String> querySessionForAutoRejoin() {
        List<String> list = new ArrayList<>();
        Cursor cursor = query(ImDBHelper.SESSION_TABLE, "(status = '1' OR status = '3' OR status = '4') AND chat_type = " + ChatData.ChatType.REGULAR_GROUP_CHAT.getId(), new String[]{"chat_id"}, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return list;
        }
        while (cursor.moveToNext()) {
            try {
                list.add(cursor.getString(0));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
        throw th;
    }

    public List<String> querySessionByChatType(boolean isGroupChat) {
        StringBuilder sb = new StringBuilder();
        sb.append("is_group_chat = ");
        sb.append(isGroupChat ? "'1'" : "'0'");
        List<ChatData> ret = querySessions(sb.toString());
        if (ret.isEmpty()) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (ChatData chat : ret) {
            list.add(chat.getChatId());
        }
        return list;
    }

    public ChatData querySessionByChatId(String cid) {
        List<ChatData> ret = querySessions("chat_id = '" + cid + "'");
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    public ChatData querySessionByContributionId(String imsi, String cid, boolean isGroupChat) {
        StringBuilder sb = new StringBuilder();
        sb.append("contribution_id = '");
        sb.append(cid);
        sb.append("' AND ");
        sb.append("sim_imsi");
        sb.append("='");
        sb.append(imsi);
        sb.append("' AND ");
        sb.append("is_group_chat");
        sb.append(" = ");
        sb.append(isGroupChat ? "'1'" : "'0'");
        List<ChatData> ret = querySessions(sb.toString());
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    public ChatData querySessionByConversationId(String cid, boolean isGroupChat) {
        String str = LOG_TAG;
        IMSLog.s(str, "querySessionByConversationId cid=" + cid);
        StringBuilder sb = new StringBuilder();
        sb.append("conversation_id = '");
        sb.append(cid);
        sb.append("' AND ");
        sb.append("is_group_chat");
        sb.append(" = ");
        sb.append(isGroupChat ? "'1'" : "'0'");
        List<ChatData> ret = querySessions(sb.toString());
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    public List<String> queryAllSessionByParticipant(Set<ImsUri> participants, ChatData.ChatType chatType) {
        String str = LOG_TAG;
        IMSLog.s(str, "queryAllSessionByParticipant chatType=" + chatType + " participants=" + participants);
        String inWhere = String.format("%s.%s=%s.%s and %s=%s", new Object[]{ImDBHelper.SESSION_TABLE, "chat_id", "participant", "chat_id", ImContract.ImSession.CHAT_TYPE, Integer.valueOf(chatType.getId())});
        List<String> list = new ArrayList<>();
        Cursor cursor = query("session, participant", inWhere, new String[]{"DISTINCT session.chat_id", "participant.uri"}, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        while (cursor.moveToNext()) {
            try {
                if (participants.contains(ImsUri.parse(cursor.getString(1)))) {
                    list.add(cursor.getString(0));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        String str2 = LOG_TAG;
        Log.i(str2, "Chats found: " + list);
        if (cursor != null) {
            cursor.close();
        }
        return list;
        throw th;
    }

    public ChatData querySessionByParticipants(Set<ImsUri> participants, ChatData.ChatType chatType, String imsi, ChatMode chatMode) {
        String inWhere;
        Throwable th;
        Set<ImsUri> set = participants;
        IMSLog.s(LOG_TAG, "querySessionByParticipants chatType=" + chatType + " participants=" + set);
        int i = 1;
        String inWhere2 = String.format("%s.%s=%s.%s and %s=%s", new Object[]{ImDBHelper.SESSION_TABLE, "chat_id", "participant", "chat_id", ImContract.ImSession.CHAT_TYPE, Integer.valueOf(chatType.getId())});
        if (!TextUtils.isEmpty(imsi)) {
            inWhere = inWhere2 + String.format(" and %s=%s", new Object[]{"sim_imsi", imsi});
        } else {
            inWhere = inWhere2 + String.format(" and %s=%s", new Object[]{ImContract.ImSession.CHAT_MODE, Integer.valueOf(chatMode.getId())});
        }
        String chatId = null;
        Cursor cursor = query("session, participant", inWhere, new String[]{"session.chat_id", "group_concat(participant.uri)", "session.preferred_uri"}, "session.chat_id", (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        while (true) {
            try {
                if (!cursor.moveToNext()) {
                    break;
                }
                String p = cursor.getString(i);
                if (p != null) {
                    Set<ImsUri> comp = new HashSet<>();
                    for (String str : p.split(",")) {
                        comp.add(ImsUri.parse(str));
                    }
                    IMSLog.s(LOG_TAG, "querySessionByParticipants compare participants=" + comp);
                    if (set.equals(comp)) {
                        chatId = cursor.getString(0);
                        Log.i(LOG_TAG, "Chat found:" + chatId);
                        break;
                    }
                    i = 1;
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if (chatId == null) {
            return null;
        }
        return querySessionByChatId(chatId);
        throw th;
    }

    /* access modifiers changed from: protected */
    public void insertParticipant(ImParticipant participant) {
        List<ImParticipant> participants = new ArrayList<>();
        participants.add(participant);
        insertParticipant((Collection<ImParticipant>) participants);
    }

    private void insertParticipant(Collection<ImParticipant> participants) {
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (ImParticipant participant : participants) {
                    long rowId = db.insert("participant", (String) null, ImDBHelper.makeParticipantRow(participant));
                    if (rowId != -1) {
                        String str = LOG_TAG;
                        Log.i(str, "Set participant id " + rowId);
                        participant.setId((int) rowId);
                    } else {
                        Log.e(LOG_TAG, "SQL exception while inserting a participant.");
                    }
                }
                setTransactionSuccessful(db);
            } finally {
                endTransaction(db);
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e);
        }
    }

    private void deleteParticipant(ImParticipant participant) {
        delete("participant", "_id=" + participant.getId());
    }

    /* access modifiers changed from: protected */
    public void deleteParticipant(Collection<ImParticipant> participants) {
        List<Integer> ids = new ArrayList<>();
        for (ImParticipant p : participants) {
            ids.add(Integer.valueOf(p.getId()));
        }
        delete("participant", "_id in ('" + TextUtils.join("', '", ids) + "')");
    }

    private void onParticipantUpdated(ImParticipant participant) {
        List<ImParticipant> participants = new ArrayList<>();
        participants.add(participant);
        onParticipantUpdated((Collection<ImParticipant>) participants);
    }

    private void onParticipantUpdated(Collection<ImParticipant> participants) {
        List<Pair<ContentValues, WhereClauseArgs>> list = new ArrayList<>();
        for (ImParticipant p : participants) {
            ContentValues makeParticipantRow = ImDBHelper.makeParticipantRow(p);
            list.add(new Pair(makeParticipantRow, new WhereClauseArgs("_id = " + p.getId())));
        }
        update("participant", list);
    }

    private void setTransactionSuccessful(SQLiteDatabase db) {
        try {
            db.setTransactionSuccessful();
        } catch (IllegalStateException e) {
            String str = LOG_TAG;
            Log.e(str, "SQLException while setTransactionSuccessful:" + e);
        }
    }

    private void endTransaction(SQLiteDatabase db) {
        try {
            db.endTransaction();
        } catch (SQLiteFullException e) {
            Log.e(LOG_TAG, "SQLiteOutOfMemoryException endTransaction");
            this.mImModule.notifyDeviceOutOfMemory();
        } catch (SQLException e2) {
            String str = LOG_TAG;
            Log.e(str, "SQLException while endTransaction:" + e2);
        }
    }

    public Set<ImParticipant> queryParticipantSet(String chatId) {
        return new HashSet(queryParticipants("chat_id='" + chatId + "'"));
    }

    private void insertImdnRecRoute(Collection<ImImdnRecRoute> imdnRecRoutes, int messageId) {
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (ImImdnRecRoute imdnRecRoute : imdnRecRoutes) {
                    imdnRecRoute.setMessageId(messageId);
                    long rowId = db.insert(ImDBHelper.IMDNRECROUTE_TABLE, (String) null, this.mImDBHelper.makeImdnRecRouteRow(imdnRecRoute));
                    if (rowId != -1) {
                        String str = LOG_TAG;
                        Log.i(str, "Set imdnrecroute id " + rowId);
                        imdnRecRoute.setId((int) rowId);
                    } else {
                        Log.e(LOG_TAG, "SQL exception while inserting a imdnrecroute.");
                    }
                }
                setTransactionSuccessful(db);
            } finally {
                endTransaction(db);
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public List<ImImdnRecRoute> queryImImdnRecRoute(MessageBase message) {
        if (message == null || message.getId() <= 0 || TextUtils.isEmpty(message.getImdnId())) {
            return new ArrayList();
        }
        return queryImImdnRecRoutes("message_id = " + message.getId() + " OR (" + "imdn_id" + " = '" + message.getImdnId() + "' AND " + "message_id" + " = 0)");
    }

    private void insertMessage(MessageBase message) {
        String str = LOG_TAG;
        IMSLog.s(str, "insertMessage: " + message);
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            ContentValues cv = null;
            if (message instanceof ImMessage) {
                cv = this.mImDBHelper.makeImMessageRow((ImMessage) message);
            } else if (message instanceof FtMessage) {
                cv = this.mImDBHelper.makeFtMessageRow((FtMessage) message);
            }
            if (cv != null) {
                db.beginTransaction();
                try {
                    long rowId = db.insert("message", (String) null, cv);
                    if (rowId != -1) {
                        String str2 = LOG_TAG;
                        Log.i(str2, "Set message id " + rowId + " (" + message.getImdnId() + ")");
                        message.setId((int) rowId);
                        setTransactionSuccessful(db);
                    } else {
                        Log.e(LOG_TAG, "SQL exception while inserting a message.");
                    }
                } finally {
                    endTransaction(db);
                }
            }
        } catch (SQLiteDiskIOException e) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e);
        }
    }

    private void insertMessageNotification(MessageBase message) {
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (ImParticipant participant : queryParticipantSet(message.getChatId())) {
                    if (db.insert("notification", (String) null, this.mImDBHelper.makeMessageNotificationRow(message, participant.getUri().toString())) != -1) {
                        String str = LOG_TAG;
                        IMSLog.s(str, "Set Notification sender_uri " + participant.getUri());
                    } else {
                        Log.e(LOG_TAG, "SQL exception while inserting a notification.");
                    }
                }
                setTransactionSuccessful(db);
            } finally {
                endTransaction(db);
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e);
        }
    }

    private void onMessageUpdated(MessageBase message) {
        IMSLog.s(LOG_TAG, "onMessageUpdated: " + message);
        ContentValues cv = null;
        String where = "_id = " + message.getId();
        if (message instanceof ImMessage) {
            cv = this.mImDBHelper.makeImMessageRow((ImMessage) message);
        } else if (message instanceof FtMessage) {
            cv = this.mImDBHelper.makeFtMessageRow((FtMessage) message);
        }
        if (cv != null) {
            update("message", cv, where);
        }
    }

    private void onMessageNotificationUpdated(MessageBase message) {
        ImsUri participant = message.getNotificationParticipant();
        if (participant == null) {
            Log.e(LOG_TAG, "onMessageNotificationUpdated participant is null");
            return;
        }
        String str = LOG_TAG;
        IMSLog.s(str, "onMessageNotificationUpdated participant : " + participant);
        long timeStamp = 0;
        if (message.getLastNotificationType() == NotificationStatus.DELIVERED || message.getLastNotificationType() == NotificationStatus.INTERWORKING_SMS || message.getLastNotificationType() == NotificationStatus.INTERWORKING_MMS) {
            timeStamp = message.getDeliveredTimestamp();
        } else if (message.getLastNotificationType() == NotificationStatus.DISPLAYED) {
            timeStamp = message.getLastDisplayedTimestamp().longValue();
        }
        String str2 = LOG_TAG;
        Log.i(str2, "onMessageNotificationUpdated status : " + message.getLastNotificationType().getId() + ", timeStamp : " + timeStamp);
        ContentValues cv = this.mImDBHelper.makeMessageNotificationUpdateRow(timeStamp, message.getLastNotificationType().getId());
        update("notification", cv, "imdn_id = '" + message.getImdnId() + "' AND " + ImContract.MessageNotification.SENDER_URI + " = '" + participant + "'");
    }

    /* access modifiers changed from: protected */
    public void deleteMessage(int messageId) {
        deleteMessageNotification(messageId);
        deleteImdnRecRoute(messageId);
        delete("message", "_id = " + messageId);
    }

    /* access modifiers changed from: protected */
    public void deleteMessage(String chatId) {
        List<Integer> messageIds = queryAllMessageIdsByChatId(chatId, false);
        deleteMessageNotification(messageIds);
        deleteImdnRecRoute(messageIds);
        delete("message", "chat_id = '" + chatId + "'");
    }

    private void deleteMessageNotification(int messageId) {
        List<Integer> list = new ArrayList<>();
        list.add(Integer.valueOf(messageId));
        deleteMessageNotification(list);
    }

    private void deleteMessageNotification(List<Integer> messageIds) {
        String idString = "'" + TextUtils.join("', '", messageIds) + "'";
        String[] projection = {ImContract.Message.IMDN_MESSAGE_ID};
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("message");
        StringBuilder sb = new StringBuilder();
        sb.append("message_id in (");
        sb.append(idString);
        sb.append(") OR (");
        sb.append("message_id");
        sb.append(" = 0 AND ");
        sb.append("imdn_id");
        sb.append(" in (");
        sb.append(qb.buildQuery(projection, "_id in (" + idString + ")", (String) null, (String) null, (String) null, (String) null));
        sb.append("))");
        delete("notification", sb.toString());
    }

    private void deleteImdnRecRoute(int messageId) {
        List<Integer> list = new ArrayList<>();
        list.add(Integer.valueOf(messageId));
        deleteImdnRecRoute(list);
    }

    private void deleteImdnRecRoute(List<Integer> messageIds) {
        String idString = "'" + TextUtils.join("', '", messageIds) + "'";
        String[] projection = {ImContract.Message.IMDN_MESSAGE_ID};
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("message");
        StringBuilder sb = new StringBuilder();
        sb.append("message_id in (");
        sb.append(idString);
        sb.append(") OR (");
        sb.append("message_id");
        sb.append(" = 0 AND ");
        sb.append("imdn_id");
        sb.append(" in (");
        sb.append(qb.buildQuery(projection, "_id in (" + idString + ")", (String) null, (String) null, (String) null, (String) null));
        sb.append("))");
        delete(ImDBHelper.IMDNRECROUTE_TABLE, sb.toString());
    }

    public List<MessageBase> queryMessages(Collection<String> messages) {
        return queryMessages("_id in (" + TextUtils.join(", ", messages) + ")");
    }

    public MessageBase queryMessage(String imdn, ImDirection direction, String chatId, String ownImsi) {
        List<MessageBase> ret = queryMessages("imdn_message_id = '" + imdn + "' AND " + "direction" + " = '" + direction.getId() + "' AND " + "chat_id" + " = '" + chatId + "' AND " + "sim_imsi" + " IN ('" + ownImsi + "', '')");
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    public MessageBase queryMessage(String imdn, ImDirection direction) {
        List<MessageBase> ret = queryMessages("imdn_message_id = '" + imdn + "' AND " + "direction" + " = '" + direction.getId() + "'");
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    public FtMessage queryFtMessageByFileTransferId(String ftId, String chatId) {
        List<MessageBase> ret = queryMessages("is_filetransfer = '1' AND file_transfer_id = '" + ftId + "' AND " + "chat_id" + " = '" + chatId + "'");
        if (ret.isEmpty()) {
            return null;
        }
        return (FtMessage) ret.get(0);
    }

    public List<Integer> queryAllMessageIdsByChatId(String chatId, boolean isFtOnly) {
        String inWhere = "chat_id = '" + chatId + "'";
        if (isFtOnly) {
            inWhere = inWhere + " AND is_filetransfer = '1'";
        }
        return queryMessageIds(inWhere);
    }

    public Cursor queryMessagesByChatIdForDump(String chatId, int maxCount) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("message");
        WhereClauseArgs inWhere = new WhereClauseArgs("chat_id = '" + chatId + "'");
        String[] projection = {ImContract.Message.IMDN_MESSAGE_ID, "message_type", "body", "file_name", "status", ImContract.CsSession.BYTES_TRANSFERED, "file_size", "direction", ImContract.Message.SENT_TIMESTAMP, ImContract.ChatItem.DELIVERED_TIMESTAMP, "notification_status"};
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            Cursor cursor = null;
            db.beginTransaction();
            try {
                cursor = qb.query(db, projection, inWhere.getWhereClause(), inWhere.getWhereArgs(), (String) null, (String) null, "sent_timestamp DESC", Integer.toString(maxCount));
                setTransactionSuccessful(db);
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while querying " + e);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
            return cursor;
        } catch (SQLiteDiskIOException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e2);
            return null;
        }
    }

    private List<Integer> queryMessageIds(String inWhere) {
        List<Integer> ret = new ArrayList<>();
        Cursor cursor = query("message", inWhere, new String[]{"_id"}, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return ret;
        }
        while (cursor.moveToNext()) {
            try {
                ret.add(Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("_id"))));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return ret;
        throw th;
    }

    public List<Integer> queryPendingMessageIds(String chatId) {
        String str = LOG_TAG;
        Log.i(str, "queryPendingMessageIds:" + chatId);
        return queryMessageIds("chat_id='" + chatId + "' AND ((IFNULL(" + "status" + ", " + ImConstants.Status.IRRELEVANT.getId() + ") in (" + ImConstants.Status.SENDING.getId() + ", " + ImConstants.Status.TO_SEND.getId() + ") AND IFNULL(" + "direction" + ", " + ImDirection.IRRELEVANT.getId() + ") = " + ImDirection.OUTGOING.getId() + ") OR (IFNULL(" + "state" + ", " + 3 + ") != " + 3 + "))");
    }

    public List<Integer> queryMessagesIdsForRevoke(String chatId) {
        Log.i(LOG_TAG, "queryImMessagesIdsForRevoke:" + chatId);
        List<Integer> needToRevokeMessages = new ArrayList<>();
        Cursor cursor = query("message", "chat_id='" + chatId + "' AND (" + ImContract.Message.REVOCATION_STATUS + " in (" + ImConstants.RevocationStatus.AVAILABLE.getId() + ", " + ImConstants.RevocationStatus.PENDING.getId() + ", " + ImConstants.RevocationStatus.SENDING.getId() + "))", (String[]) null, (String) null, (String) null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    needToRevokeMessages.add(Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("_id"))));
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return needToRevokeMessages;
        throw th;
    }

    public List<Integer> queryMessageIdsForPendingNotification(String chatId) {
        String str = LOG_TAG;
        Log.i(str, "queryMessagesForPendingNotification:" + chatId);
        return queryMessageIds("chat_id='" + chatId + "' AND (IFNULL(" + "status" + ", 4) != " + ImConstants.Status.FAILED.getId() + " OR " + ImContract.ChatItem.IS_FILE_TRANSFER + " = 1) AND IFNULL(" + "direction" + ", 2) = " + ImDirection.INCOMING.getId() + " AND " + "notification_status" + " < " + ImContract.Message.DISPOSITION_NOTIFICATION_STATUS);
    }

    public List<Integer> queryMessageIdsForDisplayAggregation(String chatId, ImDirection direction, Long timestamp) {
        String str = LOG_TAG;
        Log.i(str, "queryMessageIdsForDisplayAggregation: chatId = " + chatId + ", direction = " + direction + ", timestamp = " + timestamp);
        return queryMessageIds("chat_id = '" + chatId + "' AND " + "notification_status" + " = " + NotificationStatus.DELIVERED.getId() + " AND " + ImContract.Message.NOTIFICATION_DISPOSITION_MASK + " & " + NotificationStatus.DISPLAYED.getId() + " != 0 AND " + ImContract.ChatItem.DELIVERED_TIMESTAMP + " <= " + timestamp + " AND " + "direction" + " = " + direction.getId());
    }

    public List<String> queryAllChatIDwithPendingMessages() {
        Log.i(LOG_TAG, "queryAllChatIDwithPendingMessages at bootup");
        List<String> ret = new ArrayList<>();
        Cursor cursor = query("message", "(IFNULL(status, 4) != " + ImConstants.Status.FAILED.getId() + " OR IFNULL(" + ImContract.CsSession.STATUS + ", 4) != " + ImConstants.Status.FAILED.getId() + ") AND IFNULL(" + "direction" + ", 2) != " + ImDirection.IRRELEVANT.getId() + " AND (" + "notification_status" + " < " + NotificationStatus.DISPLAYED.getId() + " OR IFNULL(" + "state" + ", " + 3 + ") != " + 3 + ")", new String[]{"chat_id"}, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return ret;
        }
        while (cursor.moveToNext()) {
            try {
                ret.add(cursor.getString(cursor.getColumnIndexOrThrow("chat_id")));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        String str = LOG_TAG;
        Log.i(str, "queryAllChatIDwithPendingMessages: " + ret);
        return ret;
        throw th;
    }

    public List<String> queryAllChatIDwithFailedFTMessages() {
        Log.i(LOG_TAG, "queryAllChatIDwithFailedFTMessages at bootup");
        List<String> ret = new ArrayList<>();
        String inWhere = "(IFNULL(ft_status, 0) == " + ImConstants.Status.FAILED.getId() + ") AND IFNULL(" + "direction" + ", 0) != " + ImDirection.IRRELEVANT.getId();
        Log.i(LOG_TAG, "queryAllChatIDwithFailedFTMessages lsj, inWhere: " + inWhere);
        Cursor cursor = query("message", inWhere, new String[]{"chat_id"}, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return ret;
        }
        while (cursor.moveToNext()) {
            try {
                ret.add(cursor.getString(cursor.getColumnIndexOrThrow("chat_id")));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        Log.i(LOG_TAG, "queryAllChatIDwithFailedFTMessages: " + ret);
        return ret;
        throw th;
    }

    public NotificationStatus queryNotificationStatus(String imdnId, ImsUri remoteUri) {
        NotificationStatus status = null;
        Cursor cursor = query("notification", "imdn_id = '" + imdnId + "' AND " + ImContract.MessageNotification.SENDER_URI + " = '" + remoteUri + "'", (String[]) null, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        try {
            if (cursor.moveToNext()) {
                status = NotificationStatus.fromId(cursor.getInt(cursor.getColumnIndexOrThrow("status")));
            }
            if (cursor != null) {
                cursor.close();
            }
            return status;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 18 */
    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:118)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:146)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:71)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:43)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:35)
        */
    public java.util.List<android.os.Bundle> queryLastSentMessages(java.util.List<java.lang.String> r19) {
        /*
            r18 = this;
            r1 = r18
            r2 = r19
            java.lang.String r0 = "chat_id"
            java.lang.String r3 = "request_message_id"
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "queryLastSentMessages listRequestMessageId size = "
            r5.append(r6)
            int r6 = r19.size()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            com.sec.internal.ims.servicemodules.im.ImDBHelper r5 = r1.mImDBHelper     // Catch:{ SQLiteDiskIOException -> 0x0152 }
            android.database.sqlite.SQLiteDatabase r5 = r5.getWritableDatabase()     // Catch:{ SQLiteDiskIOException -> 0x0152 }
            r5.beginTransaction()
            android.database.sqlite.SQLiteQueryBuilder r6 = new android.database.sqlite.SQLiteQueryBuilder     // Catch:{ SQLException -> 0x0131 }
            r6.<init>()     // Catch:{ SQLException -> 0x0131 }
            r15 = r6
            java.lang.String r6 = "message"
            r15.setTables(r6)     // Catch:{ SQLException -> 0x0131 }
            r6 = 0
            r7 = 0
            java.lang.String r8 = LOG_TAG     // Catch:{ SQLException -> 0x0131 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ SQLException -> 0x0131 }
            r9.<init>()     // Catch:{ SQLException -> 0x0131 }
            java.lang.String r10 = "list of request message ids"
            r9.append(r10)     // Catch:{ SQLException -> 0x0131 }
            r9.append(r2)     // Catch:{ SQLException -> 0x0131 }
            java.lang.String r9 = r9.toString()     // Catch:{ SQLException -> 0x0131 }
            android.util.Log.i(r8, r9)     // Catch:{ SQLException -> 0x0131 }
            int r8 = r19.size()     // Catch:{ SQLException -> 0x0131 }
            r14 = 1
            if (r8 < r14) goto L_0x0082
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ SQLException -> 0x0131 }
            r8.<init>()     // Catch:{ SQLException -> 0x0131 }
            java.lang.String r9 = "request_message_id IN ("
            r8.append(r9)     // Catch:{ SQLException -> 0x0131 }
            java.lang.String r9 = ", "
            java.lang.String r9 = android.text.TextUtils.join(r9, r2)     // Catch:{ SQLException -> 0x0131 }
            r8.append(r9)     // Catch:{ SQLException -> 0x0131 }
            java.lang.String r9 = ")"
            r8.append(r9)     // Catch:{ SQLException -> 0x0131 }
            java.lang.String r8 = r8.toString()     // Catch:{ SQLException -> 0x0131 }
            r15.appendWhere(r8)     // Catch:{ SQLException -> 0x0131 }
            r16 = r6
            r17 = r7
            goto L_0x008d
        L_0x0082:
            java.lang.String r8 = "sent_timestamp DESC"
            r6 = r8
            java.lang.String r8 = "1"
            r7 = r8
            r16 = r6
            r17 = r7
        L_0x008d:
            r8 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r6 = r15
            r7 = r5
            r13 = r16
            r2 = r14
            r14 = r17
            android.database.Cursor r6 = r6.query(r7, r8, r9, r10, r11, r12, r13, r14)     // Catch:{ SQLException -> 0x0131 }
        L_0x009d:
            boolean r7 = r6.moveToNext()     // Catch:{ all -> 0x0121 }
            if (r7 == 0) goto L_0x0118
            int r7 = r6.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0121 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0121 }
            int r8 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0121 }
            java.lang.String r8 = r6.getString(r8)     // Catch:{ all -> 0x0121 }
            java.lang.String r9 = "is_filetransfer"
            int r9 = r6.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x0121 }
            int r9 = r6.getInt(r9)     // Catch:{ all -> 0x0121 }
            r10 = 0
            if (r9 != 0) goto L_0x00cc
            java.lang.String r11 = "status"
            int r11 = r6.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x0121 }
            int r11 = r6.getInt(r11)     // Catch:{ all -> 0x0121 }
            goto L_0x00e1
        L_0x00cc:
            java.lang.String r11 = "ft_status"
            int r11 = r6.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x0121 }
            int r11 = r6.getInt(r11)     // Catch:{ all -> 0x0121 }
            java.lang.String r12 = "is_resumable"
            int r12 = r6.getColumnIndexOrThrow(r12)     // Catch:{ all -> 0x0121 }
            int r12 = r6.getInt(r12)     // Catch:{ all -> 0x0121 }
            r10 = r12
        L_0x00e1:
            android.os.Bundle r12 = new android.os.Bundle     // Catch:{ all -> 0x0121 }
            r12.<init>()     // Catch:{ all -> 0x0121 }
            r12.putString(r0, r8)     // Catch:{ all -> 0x0121 }
            long r13 = (long) r7     // Catch:{ all -> 0x0121 }
            r12.putLong(r3, r13)     // Catch:{ all -> 0x0121 }
            java.lang.String r13 = "is_file_transfer"
            r12.putInt(r13, r9)     // Catch:{ all -> 0x0121 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r13 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.FAILED     // Catch:{ all -> 0x0121 }
            int r13 = r13.getId()     // Catch:{ all -> 0x0121 }
            java.lang.String r14 = "response_status"
            if (r11 != r13) goto L_0x0102
            r13 = 0
            r12.putBoolean(r14, r13)     // Catch:{ all -> 0x0121 }
            goto L_0x010d
        L_0x0102:
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r13 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.SENT     // Catch:{ all -> 0x0121 }
            int r13 = r13.getId()     // Catch:{ all -> 0x0121 }
            if (r11 != r13) goto L_0x010d
            r12.putBoolean(r14, r2)     // Catch:{ all -> 0x0121 }
        L_0x010d:
            java.lang.String r13 = "resumable_option_code"
            r12.putInt(r13, r10)     // Catch:{ all -> 0x0121 }
            r4.add(r12)     // Catch:{ all -> 0x0121 }
            goto L_0x009d
        L_0x0118:
            if (r6 == 0) goto L_0x011d
            r6.close()     // Catch:{ SQLException -> 0x0131 }
        L_0x011d:
            r1.setTransactionSuccessful(r5)     // Catch:{ SQLException -> 0x0131 }
            goto L_0x0149
        L_0x0121:
            r0 = move-exception
            r2 = r0
            if (r6 == 0) goto L_0x012e
            r6.close()     // Catch:{ all -> 0x0129 }
            goto L_0x012e
        L_0x0129:
            r0 = move-exception
            r3 = r0
            r2.addSuppressed(r3)     // Catch:{ SQLException -> 0x0131 }
        L_0x012e:
            throw r2     // Catch:{ SQLException -> 0x0131 }
        L_0x012f:
            r0 = move-exception
            goto L_0x014e
        L_0x0131:
            r0 = move-exception
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x012f }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x012f }
            r3.<init>()     // Catch:{ all -> 0x012f }
            java.lang.String r6 = "SQL exception while queryAllChatIDwithPendingMessages. "
            r3.append(r6)     // Catch:{ all -> 0x012f }
            r3.append(r0)     // Catch:{ all -> 0x012f }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x012f }
            android.util.Log.e(r2, r3)     // Catch:{ all -> 0x012f }
        L_0x0149:
            r1.endTransaction(r5)
            return r4
        L_0x014e:
            r1.endTransaction(r5)
            throw r0
        L_0x0152:
            r0 = move-exception
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r5 = "SQLiteDiskIOException : "
            r3.append(r5)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            android.util.Log.e(r2, r3)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImPersister.queryLastSentMessages(java.util.List):java.util.List");
    }

    public Collection<ImsUri> queryChatbotRoleUris() {
        List<ImsUri> list = new ArrayList<>();
        Cursor cursor = query("participant, session", "participant.chat_id = session.chat_id AND session.is_group_chat = 0 AND session.is_chatbot_role = 1", new String[]{"uri"}, (String) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return list;
        }
        while (cursor.moveToNext()) {
            try {
                list.add(ImsUri.parse(cursor.getString(cursor.getColumnIndexOrThrow("uri"))));
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        String str = LOG_TAG;
        Log.i(str, "queryChatbotRoleUris: size=" + list.size() + " " + IMSLog.checker(list));
        return list;
        throw th;
    }

    /* JADX INFO: finally extract failed */
    public Uri cloudInsertMessage(Uri uri, ContentValues values) {
        String str = LOG_TAG;
        IMSLog.s(str, "cloudInsertMessage: " + values);
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long rowId = db.insert("message", (String) null, values);
                if (rowId != -1) {
                    String str2 = LOG_TAG;
                    Log.i(str2, "cloudInsertMessage: rowId=" + rowId);
                    setTransactionSuccessful(db);
                } else {
                    Log.e(LOG_TAG, "cloudInsertMessage: SQL exception while inserting a message.");
                }
                endTransaction(db);
                return ContentUris.withAppendedId(uri, rowId);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
        } catch (SQLiteDiskIOException e) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e);
            return null;
        }
    }

    public int cloudUpdateMessage(String msgId, ContentValues values) {
        Log.i(LOG_TAG, "updateCloudMessage");
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            int row = 0;
            try {
                row = db.update("message", values, "_id = " + msgId, (String[]) null);
                setTransactionSuccessful(db);
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while updating a message. " + e);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
            return row;
        } catch (SQLiteDiskIOException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e2);
            return 0;
        }
    }

    public int cloudUpdateSession(String chatId, ContentValues values) {
        Log.i(LOG_TAG, "updateCloudSession");
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            int row = 0;
            try {
                row = db.update(ImDBHelper.SESSION_TABLE, values, "chat_id=?", new String[]{chatId});
                setTransactionSuccessful(db);
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while updating a message. " + e);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
            return row;
        } catch (SQLiteDiskIOException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e2.toString());
            return 0;
        }
    }

    /* JADX INFO: finally extract failed */
    public Uri cloudInsertParticipant(Uri uri, ContentValues values) {
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long rowId = db.insert("participant", (String) null, values);
                if (rowId != -1) {
                    String str = LOG_TAG;
                    Log.i(str, "cloudInsertParticipant: rowId=" + rowId);
                    setTransactionSuccessful(db);
                } else {
                    Log.e(LOG_TAG, "cloudInsertParticipant: SQL exception while inserting a participant.");
                }
                endTransaction(db);
                return ContentUris.withAppendedId(uri, rowId);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
        } catch (SQLiteDiskIOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e.toString());
            return null;
        }
    }

    public int cloudDeleteParticipant(String id) {
        delete("participant", "_id=" + id);
        return 0;
    }

    public int cloudUpdateParticipant(String id, ContentValues values) {
        Log.i(LOG_TAG, "cloudUpdateParticipant");
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            int row = 0;
            try {
                row = db.update("participant", values, "_id=?", new String[]{id});
                setTransactionSuccessful(db);
            } catch (SQLException e) {
                String str = LOG_TAG;
                Log.e(str, "SQL exception while updating a message. " + e);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
            return row;
        } catch (SQLiteDiskIOException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQLiteDiskIOException : " + e2.toString());
            return 0;
        }
    }

    /* JADX INFO: finally extract failed */
    public Uri cloudInsertNotification(Uri uri, ContentValues values) {
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long rowId = db.insert("notification", (String) null, values);
                if (rowId != -1) {
                    setTransactionSuccessful(db);
                } else {
                    Log.e(LOG_TAG, "cloudInsertNotification: SQL exception while inserting a notification.");
                }
                endTransaction(db);
                return ContentUris.withAppendedId(uri, rowId);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
        } catch (SQLiteDiskIOException e) {
            String str = LOG_TAG;
            Log.e(str, "SQLiteDiskIOException : " + e.toString());
            return null;
        }
    }

    public int cloudUpdateNotification(String imdnId, ContentValues values) {
        String str = LOG_TAG;
        Log.i(str, "cloudUpdateNotification imdnId: " + imdnId);
        try {
            SQLiteDatabase db = this.mImDBHelper.getWritableDatabase();
            db.beginTransaction();
            int row = 0;
            try {
                row = db.update("notification", values, "imdn_id=?", new String[]{imdnId});
                setTransactionSuccessful(db);
            } catch (SQLException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "SQL exception while updating a message. " + e);
            } catch (Throwable th) {
                endTransaction(db);
                throw th;
            }
            endTransaction(db);
            return row;
        } catch (SQLiteDiskIOException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "SQLiteDiskIOException : " + e2);
            return 0;
        }
    }

    public void updateDesiredNotificationStatusAsDisplayed(Collection<String> messages, int status, long displayTime) {
        IMSLog.s(LOG_TAG, "updateDesiredNotificationStatusAsDisplayed: messages=" + messages + " status=" + status + " displayTime=" + displayTime);
        ContentValues cv = new ContentValues();
        cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(status));
        cv.put(ImContract.Message.DISPLAYED_TIMESTAMP, Long.valueOf(displayTime));
        update("message", cv, "_id in (" + TextUtils.join(", ", messages) + ")");
        ContentValues cv2 = new ContentValues();
        cv2.put("status", Integer.valueOf(ImConstants.Status.READ.getId()));
        update("message", cv2, "_id in (" + TextUtils.join(", ", messages) + ") AND IFNULL(" + "status" + ", 4) != " + ImConstants.Status.FAILED.getId());
    }

    private void clearDeletedParticipants() {
        String inWhere = "status in (" + ImParticipant.Status.DECLINED.getId() + ", " + ImParticipant.Status.FAILED.getId() + ")";
        if (!this.mImModule.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.REMOVE_FAILED_PARTICIPANT_GROUPCHAT)) {
            inWhere = "status = " + ImParticipant.Status.DECLINED.getId();
        }
        delete("participant", inWhere);
    }

    /* access modifiers changed from: package-private */
    public void closeDB() {
        try {
            Log.i(LOG_TAG, "closeDB()");
            this.mImDBHelper.close();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void updateChat(ChatData chatData, ImCacheAction action) {
        if (action == ImCacheAction.INSERTED) {
            insertSession(chatData);
        } else if (action == ImCacheAction.UPDATED) {
            onSessionUpdated(chatData);
        } else if (action == ImCacheAction.DELETED) {
            deleteSession(chatData);
        }
    }

    public void updateMessage(MessageBase message, ImCacheAction action) {
        if (action == ImCacheAction.INSERTED) {
            insertMessage(message);
            if (message.getDirection() == ImDirection.OUTGOING) {
                insertMessageNotification(message);
            }
            List<ImImdnRecRoute> imdnRecRoutes = message.getImdnRecRouteList();
            if (imdnRecRoutes != null && !imdnRecRoutes.isEmpty()) {
                insertImdnRecRoute(imdnRecRoutes, message.getId());
            }
        } else if (action == ImCacheAction.UPDATED) {
            onMessageUpdated(message);
            if (message.getDirection() == ImDirection.OUTGOING) {
                onMessageNotificationUpdated(message);
            }
        }
    }

    public void updateParticipant(ImParticipant participant, ImCacheAction action) {
        if (action == ImCacheAction.INSERTED) {
            insertParticipant(participant);
        } else if (action == ImCacheAction.DELETED) {
            deleteParticipant(participant);
        } else if (action == ImCacheAction.UPDATED) {
            onParticipantUpdated(participant);
        }
    }

    public void updateMessage(Collection<MessageBase> messages, ImCacheAction action) {
        for (MessageBase message : messages) {
            updateMessage(message, action);
        }
    }

    public void updateParticipant(Collection<ImParticipant> participants, ImCacheAction action) {
        if (action == ImCacheAction.INSERTED) {
            insertParticipant(participants);
        } else if (action == ImCacheAction.DELETED) {
            deleteParticipant(participants);
        } else if (action == ImCacheAction.UPDATED) {
            onParticipantUpdated(participants);
        }
    }

    private static class WhereClauseArgs {
        private final String[] mWhereArgs;
        private final String mWhereClause;

        WhereClauseArgs(String whereClause, String[] whereArgs) {
            this.mWhereClause = whereClause;
            this.mWhereArgs = whereArgs;
        }

        WhereClauseArgs(String whereClause) {
            this(whereClause, (String[]) null);
        }

        /* access modifiers changed from: package-private */
        public String getWhereClause() {
            return this.mWhereClause;
        }

        /* access modifiers changed from: package-private */
        public String[] getWhereArgs() {
            return this.mWhereArgs;
        }
    }
}
