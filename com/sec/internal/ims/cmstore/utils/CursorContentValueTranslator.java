package com.sec.internal.ims.cmstore.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import java.util.ArrayList;

public class CursorContentValueTranslator {
    private static final String LOG_TAG = CursorContentValueTranslator.class.getSimpleName();

    public static ArrayList<ContentValues> convertFaxtoCV(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList<ContentValues> contentvalues = new ArrayList<>();
        Cursor cs = cursor;
        do {
            try {
                ContentValues cv = new ContentValues();
                cv.put(CloudMessageProviderContract.FAXMessages.FAXID, cs.getString(cs.getColumnIndexOrThrow(CloudMessageProviderContract.FAXMessages.FAXID)));
                cv.put("file_name", cs.getString(cs.getColumnIndexOrThrow("file_name")));
                cv.put("file_path", cs.getString(cs.getColumnIndexOrThrow("file_path")));
                cv.put("file_size", Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow("file_size"))));
                cv.put("content_type", cs.getString(cs.getColumnIndexOrThrow("content_type")));
                cv.put("recipients", cs.getString(cs.getColumnIndexOrThrow("recipients")));
                cv.put("date", Long.valueOf(cs.getLong(cs.getColumnIndexOrThrow("date"))));
                contentvalues.add(cv);
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cs.moveToNext());
        if (cs != null) {
            cs.close();
        }
        return contentvalues;
        throw th;
    }

    public static ArrayList<ContentValues> convertRCSimfttoCV(Cursor cursor) {
        ArrayList<ContentValues> contentvalues = new ArrayList<>();
        Cursor cs = cursor;
        do {
            try {
                if (TextUtils.isEmpty(cs.getString(cs.getColumnIndexOrThrow("body")))) {
                    String str = LOG_TAG;
                    Log.d(str, "covertRCSimfttoCV: direction: " + cs.getInt(cs.getColumnIndexOrThrow("direction")) + "covertRCSimfttoCV: status: " + cs.getInt(cs.getColumnIndexOrThrow(ImContract.CsSession.STATUS)));
                    if ((cs.getInt(cs.getColumnIndexOrThrow("direction")) == ImDirection.INCOMING.getId() || cs.getInt(cs.getColumnIndexOrThrow("direction")) == ImDirection.OUTGOING.getId()) && (cs.getInt(cs.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.SENT.getId() || cs.getInt(cs.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.TO_SEND.getId() || cs.getInt(cs.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.SENDING.getId() || cs.getInt(cs.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.UNREAD.getId() || cs.getInt(cs.getColumnIndexOrThrow(ImContract.CsSession.STATUS)) == ImConstants.Status.READ.getId())) {
                        copyRcsMessageCursor(cs, contentvalues);
                    }
                } else if ((cs.getInt(cs.getColumnIndexOrThrow("direction")) == ImDirection.INCOMING.getId() || cs.getInt(cs.getColumnIndexOrThrow("direction")) == ImDirection.OUTGOING.getId()) && (cs.getInt(cs.getColumnIndexOrThrow("status")) == ImConstants.Status.SENT.getId() || cs.getInt(cs.getColumnIndexOrThrow("status")) == ImConstants.Status.UNREAD.getId() || cs.getInt(cs.getColumnIndexOrThrow("status")) == ImConstants.Status.READ.getId())) {
                    copyRcsMessageCursor(cs, contentvalues);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cs.moveToNext());
        if (cs != null) {
            cs.close();
        }
        return contentvalues;
        throw th;
    }

    private static void copyRcsMessageCursor(Cursor cursor, ArrayList<ContentValues> contentvalues) {
        ContentValues cv = new ContentValues();
        cv.put("_id", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("_id"))));
        cv.put(ImContract.ChatItem.IS_FILE_TRANSFER, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER))));
        cv.put("direction", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("direction"))));
        cv.put("chat_id", cursor.getString(cursor.getColumnIndexOrThrow("chat_id")));
        cv.put("remote_uri", cursor.getString(cursor.getColumnIndexOrThrow("remote_uri")));
        cv.put(ImContract.ChatItem.USER_ALIAS, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.USER_ALIAS)));
        cv.put("content_type", cursor.getString(cursor.getColumnIndexOrThrow("content_type")));
        cv.put(ImContract.ChatItem.INSERTED_TIMESTAMP, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.INSERTED_TIMESTAMP))));
        cv.put(ImContract.ChatItem.EXT_INFO, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.EXT_INFO)));
        cv.put(ImContract.ChatItem.EXT_INFO, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.ChatItem.EXT_INFO)));
        cv.put("body", cursor.getString(cursor.getColumnIndexOrThrow("body")));
        cv.put(ImContract.Message.NOTIFICATION_DISPOSITION_MASK, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOTIFICATION_DISPOSITION_MASK))));
        cv.put("notification_status", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("notification_status"))));
        cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS))));
        cv.put(ImContract.Message.SENT_TIMESTAMP, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.SENT_TIMESTAMP))));
        cv.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.ChatItem.DELIVERED_TIMESTAMP))));
        cv.put(ImContract.Message.DISPLAYED_TIMESTAMP, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.Message.DISPLAYED_TIMESTAMP))));
        cv.put("message_type", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("message_type"))));
        cv.put(ImContract.Message.MESSAGE_ISSLM, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.MESSAGE_ISSLM))));
        cv.put("status", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("status"))));
        cv.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.NOT_DISPLAYED_COUNTER))));
        cv.put(ImContract.Message.IMDN_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_MESSAGE_ID)));
        cv.put(ImContract.Message.IMDN_ORIGINAL_TO, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_ORIGINAL_TO)));
        cv.put("conversation_id", cursor.getString(cursor.getColumnIndexOrThrow("conversation_id")));
        cv.put("contribution_id", cursor.getString(cursor.getColumnIndexOrThrow("contribution_id")));
        cv.put("file_path", cursor.getString(cursor.getColumnIndexOrThrow("file_path")));
        cv.put("file_name", cursor.getString(cursor.getColumnIndexOrThrow("file_name")));
        cv.put("file_size", Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("file_size"))));
        cv.put(ImContract.CsSession.FILE_TRANSFER_ID, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_TRANSFER_ID)));
        cv.put("state", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("state"))));
        cv.put("reason", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("reason"))));
        cv.put(ImContract.CsSession.BYTES_TRANSFERED, Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ImContract.CsSession.BYTES_TRANSFERED))));
        cv.put(ImContract.CsSession.STATUS, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.STATUS))));
        cv.put(ImContract.CsSession.THUMBNAIL_PATH, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH)));
        cv.put(ImContract.CsSession.IS_RESUMABLE, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.IS_RESUMABLE))));
        cv.put(ImContract.CsSession.TRANSFER_MECH, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.TRANSFER_MECH))));
        cv.put(ImContract.CsSession.DATA_URL, cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.DATA_URL)));
        cv.put("request_message_id", cursor.getString(cursor.getColumnIndexOrThrow("request_message_id")));
        cv.put("sim_imsi", cursor.getString(cursor.getColumnIndexOrThrow("sim_imsi")));
        cv.put("is_resizable", Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("is_resizable"))));
        contentvalues.add(cv);
    }

    public static ArrayList<ContentValues> convertRCSparticipantstoCV(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList<ContentValues> contentvalues = new ArrayList<>();
        Cursor cs = cursor;
        do {
            try {
                ContentValues cv = new ContentValues();
                cv.put("_id", Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow("_id"))));
                cv.put("chat_id", cs.getString(cs.getColumnIndexOrThrow("chat_id")));
                cv.put("status", Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow("status"))));
                cv.put("type", Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow("type"))));
                cv.put("uri", cs.getString(cs.getColumnIndexOrThrow("uri")));
                cv.put("alias", cs.getString(cs.getColumnIndexOrThrow("alias")));
                contentvalues.add(cv);
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cs.moveToNext());
        if (cs != null) {
            cs.close();
        }
        return contentvalues;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0326 A[SYNTHETIC, Splitter:B:21:0x0326] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.ArrayList<android.content.ContentValues> convertSMStoCV(android.database.Cursor r29) {
        /*
            java.lang.String r0 = "group_id"
            java.lang.String r1 = "hidden"
            java.lang.String r2 = "sim_imsi"
            java.lang.String r3 = "sim_slot"
            java.lang.String r4 = "deletable"
            java.lang.String r5 = "seen"
            java.lang.String r6 = "error_code"
            java.lang.String r7 = "locked"
            java.lang.String r8 = "service_center"
            java.lang.String r9 = "body"
            java.lang.String r10 = "subject"
            java.lang.String r11 = "reply_path_present"
            java.lang.String r12 = "type"
            java.lang.String r13 = "status"
            java.lang.String r14 = "read"
            java.lang.String r15 = "protocol"
            r16 = r0
            java.lang.String r0 = "date_sent"
            r17 = r1
            java.lang.String r1 = "date"
            r18 = r2
            java.lang.String r2 = "person"
            r19 = r3
            java.lang.String r3 = "address"
            r20 = r4
            java.lang.String r4 = "thread_id"
            r21 = r5
            java.lang.String r5 = "_id"
            java.util.ArrayList r22 = new java.util.ArrayList
            r22.<init>()
            r23 = r22
            r22 = r29
        L_0x004d:
            android.content.ContentValues r24 = new android.content.ContentValues     // Catch:{ all -> 0x031e }
            r24.<init>()     // Catch:{ all -> 0x031e }
            r25 = r24
            r24 = r6
            r6 = r22
            r22 = r7
            int r7 = r6.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0319 }
            r26 = r8
            r8 = r25
            r8.put(r5, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r4, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0319 }
            java.lang.String r7 = r6.getString(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r3, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r2, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            long r27 = r6.getLong(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Long r7 = java.lang.Long.valueOf(r27)     // Catch:{ all -> 0x0319 }
            r8.put(r1, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0319 }
            long r27 = r6.getLong(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Long r7 = java.lang.Long.valueOf(r27)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r15)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r15, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r14, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r13)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r13, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r12)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r12, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r11, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x0319 }
            java.lang.String r7 = r6.getString(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r10, r7)     // Catch:{ all -> 0x0319 }
            int r7 = r6.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x0319 }
            java.lang.String r7 = r6.getString(r7)     // Catch:{ all -> 0x0319 }
            r8.put(r9, r7)     // Catch:{ all -> 0x0319 }
            r25 = r0
            r7 = r26
            int r0 = r6.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = r6.getString(r0)     // Catch:{ all -> 0x0319 }
            r8.put(r7, r0)     // Catch:{ all -> 0x0319 }
            r0 = r22
            r22 = r1
            int r1 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            r1 = r24
            r24 = r0
            int r0 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r0 = r6.getInt(r0)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x0319 }
            r8.put(r1, r0)     // Catch:{ all -> 0x0319 }
            r0 = r21
            r21 = r1
            int r1 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            r1 = r20
            r20 = r0
            int r0 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r0 = r6.getInt(r0)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x0319 }
            r8.put(r1, r0)     // Catch:{ all -> 0x0319 }
            r0 = r19
            r19 = r1
            int r1 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            r1 = r18
            r18 = r0
            int r0 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = r6.getString(r0)     // Catch:{ all -> 0x0319 }
            r8.put(r1, r0)     // Catch:{ all -> 0x0319 }
            r0 = r17
            r17 = r1
            int r1 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            r1 = r16
            r16 = r0
            int r0 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r0 = r6.getInt(r0)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x0319 }
            r8.put(r1, r0)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "group_type"
            r26 = r1
            java.lang.String r1 = "group_type"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "delivery_date"
            java.lang.String r1 = "delivery_date"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "app_id"
            java.lang.String r1 = "app_id"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "msg_id"
            java.lang.String r1 = "msg_id"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "callback_number"
            java.lang.String r1 = "callback_number"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r1 = r6.getString(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "reserved"
            java.lang.String r1 = "reserved"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "pri"
            java.lang.String r1 = "pri"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "teleservice_id"
            java.lang.String r1 = "teleservice_id"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "link_url"
            java.lang.String r1 = "link_url"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r1 = r6.getString(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "svc_cmd"
            java.lang.String r1 = "svc_cmd"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r1 = r6.getString(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "svc_cmd_content"
            java.lang.String r1 = "svc_cmd_content"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "roam_pending"
            java.lang.String r1 = "roam_pending"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "spam_report"
            java.lang.String r1 = "spam_report"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "safe_message"
            java.lang.String r1 = "safe_message"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x0319 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r0 = "from_address"
            java.lang.String r1 = "from_address"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0319 }
            java.lang.String r1 = r6.getString(r1)     // Catch:{ all -> 0x0319 }
            r8.put(r0, r1)     // Catch:{ all -> 0x0319 }
            r1 = r23
            r1.add(r8)     // Catch:{ all -> 0x0316 }
            boolean r0 = r6.moveToNext()     // Catch:{ all -> 0x0316 }
            if (r0 != 0) goto L_0x02fb
            if (r6 == 0) goto L_0x02fa
            r6.close()
        L_0x02fa:
            return r1
        L_0x02fb:
            r23 = r1
            r8 = r7
            r1 = r22
            r7 = r24
            r0 = r25
            r22 = r6
            r6 = r21
            r21 = r20
            r20 = r19
            r19 = r18
            r18 = r17
            r17 = r16
            r16 = r26
            goto L_0x004d
        L_0x0316:
            r0 = move-exception
            r2 = r0
            goto L_0x0324
        L_0x0319:
            r0 = move-exception
            r1 = r23
            r2 = r0
            goto L_0x0324
        L_0x031e:
            r0 = move-exception
            r6 = r22
            r1 = r23
            r2 = r0
        L_0x0324:
            if (r6 == 0) goto L_0x032f
            r6.close()     // Catch:{ all -> 0x032a }
            goto L_0x032f
        L_0x032a:
            r0 = move-exception
            r3 = r0
            r2.addSuppressed(r3)
        L_0x032f:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator.convertSMStoCV(android.database.Cursor):java.util.ArrayList");
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x03b9 A[SYNTHETIC, Splitter:B:24:0x03b9] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.ArrayList<android.content.ContentValues> convertPDUtoCV(android.database.Cursor r31) {
        /*
            java.lang.String r0 = "tr_id"
            java.lang.String r1 = "st"
            java.lang.String r2 = "resp_st"
            java.lang.String r3 = "rpt_a"
            java.lang.String r4 = "rr"
            java.lang.String r5 = "pri"
            java.lang.String r6 = "m_size"
            java.lang.String r7 = "v"
            java.lang.String r8 = "m_type"
            java.lang.String r9 = "m_cls"
            java.lang.String r10 = "exp"
            java.lang.String r11 = "ct_l"
            java.lang.String r12 = "ct_t"
            java.lang.String r13 = "sub_cs"
            java.lang.String r14 = "sub"
            java.lang.String r15 = "m_id"
            r16 = r0
            java.lang.String r0 = "read"
            r17 = r1
            java.lang.String r1 = "msg_box"
            r18 = r2
            java.lang.String r2 = "date_sent"
            r19 = r3
            java.lang.String r3 = "date"
            r20 = r4
            java.lang.String r4 = "thread_id"
            r21 = r5
            java.lang.String r5 = "_id"
            if (r31 != 0) goto L_0x0047
            r0 = 0
            return r0
        L_0x0047:
            java.util.ArrayList r22 = new java.util.ArrayList
            r22.<init>()
            r23 = r22
            r22 = r31
        L_0x0050:
            android.content.ContentValues r24 = new android.content.ContentValues     // Catch:{ all -> 0x03b1 }
            r24.<init>()     // Catch:{ all -> 0x03b1 }
            r25 = r24
            r24 = r6
            r6 = r22
            r22 = r7
            int r7 = r6.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x03ac }
            r26 = r8
            r8 = r25
            r8.put(r5, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r4, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x03ac }
            long r27 = r6.getLong(r7)     // Catch:{ all -> 0x03ac }
            r29 = 1000(0x3e8, double:4.94E-321)
            long r27 = r27 * r29
            java.lang.Long r7 = java.lang.Long.valueOf(r27)     // Catch:{ all -> 0x03ac }
            r8.put(r3, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x03ac }
            long r27 = r6.getLong(r7)     // Catch:{ all -> 0x03ac }
            java.lang.Long r7 = java.lang.Long.valueOf(r27)     // Catch:{ all -> 0x03ac }
            r8.put(r2, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r1, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r15)     // Catch:{ all -> 0x03ac }
            java.lang.String r7 = r6.getString(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r15, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x03ac }
            java.lang.String r7 = r6.getString(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r14, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r13)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r13, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r12)     // Catch:{ all -> 0x03ac }
            java.lang.String r7 = r6.getString(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r12, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x03ac }
            java.lang.String r7 = r6.getString(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r11, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r10, r7)     // Catch:{ all -> 0x03ac }
            int r7 = r6.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x03ac }
            java.lang.String r7 = r6.getString(r7)     // Catch:{ all -> 0x03ac }
            r8.put(r9, r7)     // Catch:{ all -> 0x03ac }
            r25 = r0
            r7 = r26
            int r0 = r6.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x03ac }
            int r0 = r6.getInt(r0)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x03ac }
            r8.put(r7, r0)     // Catch:{ all -> 0x03ac }
            r0 = r22
            r22 = r1
            int r1 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            r1 = r24
            r24 = r0
            int r0 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r0 = r6.getInt(r0)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x03ac }
            r8.put(r1, r0)     // Catch:{ all -> 0x03ac }
            r0 = r21
            r21 = r1
            int r1 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            r1 = r20
            r20 = r0
            int r0 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r0 = r6.getInt(r0)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x03ac }
            r8.put(r1, r0)     // Catch:{ all -> 0x03ac }
            r0 = r19
            r19 = r1
            int r1 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            r1 = r18
            r18 = r0
            int r0 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r0 = r6.getInt(r0)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x03ac }
            r8.put(r1, r0)     // Catch:{ all -> 0x03ac }
            r0 = r17
            r17 = r1
            int r1 = r6.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            r1 = r16
            r16 = r0
            int r0 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = r6.getString(r0)     // Catch:{ all -> 0x03ac }
            r8.put(r1, r0)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "retr_st"
            r26 = r1
            java.lang.String r1 = "retr_st"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "retr_txt"
            java.lang.String r1 = "retr_txt"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r1 = r6.getString(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "retr_txt_cs"
            java.lang.String r1 = "retr_txt_cs"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "read_status"
            java.lang.String r1 = "read_status"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "ct_cls"
            java.lang.String r1 = "ct_cls"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "resp_txt"
            java.lang.String r1 = "resp_txt"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r1 = r6.getString(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "d_tm"
            java.lang.String r1 = "d_tm"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "d_rpt"
            java.lang.String r1 = "d_rpt"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "locked"
            java.lang.String r1 = "locked"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "seen"
            java.lang.String r1 = "seen"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "sim_slot"
            java.lang.String r1 = "sim_slot"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "sim_imsi"
            java.lang.String r1 = "sim_imsi"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r1 = r6.getString(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "deletable"
            java.lang.String r1 = "deletable"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "hidden"
            java.lang.String r1 = "hidden"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "app_id"
            java.lang.String r1 = "app_id"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "msg_id"
            java.lang.String r1 = "msg_id"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "callback_set"
            java.lang.String r1 = "callback_set"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "reserved"
            java.lang.String r1 = "reserved"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "text_only"
            java.lang.String r1 = "text_only"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "spam_report"
            java.lang.String r1 = "spam_report"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "safe_message"
            java.lang.String r1 = "safe_message"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x03ac }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r0 = "from_address"
            java.lang.String r1 = "from_address"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x03ac }
            java.lang.String r1 = r6.getString(r1)     // Catch:{ all -> 0x03ac }
            r8.put(r0, r1)     // Catch:{ all -> 0x03ac }
            r1 = r23
            r1.add(r8)     // Catch:{ all -> 0x03a9 }
            boolean r0 = r6.moveToNext()     // Catch:{ all -> 0x03a9 }
            if (r0 != 0) goto L_0x038e
            if (r6 == 0) goto L_0x038d
            r6.close()
        L_0x038d:
            return r1
        L_0x038e:
            r23 = r1
            r8 = r7
            r1 = r22
            r7 = r24
            r0 = r25
            r22 = r6
            r6 = r21
            r21 = r20
            r20 = r19
            r19 = r18
            r18 = r17
            r17 = r16
            r16 = r26
            goto L_0x0050
        L_0x03a9:
            r0 = move-exception
            r2 = r0
            goto L_0x03b7
        L_0x03ac:
            r0 = move-exception
            r1 = r23
            r2 = r0
            goto L_0x03b7
        L_0x03b1:
            r0 = move-exception
            r6 = r22
            r1 = r23
            r2 = r0
        L_0x03b7:
            if (r6 == 0) goto L_0x03c2
            r6.close()     // Catch:{ all -> 0x03bd }
            goto L_0x03c2
        L_0x03bd:
            r0 = move-exception
            r3 = r0
            r2.addSuppressed(r3)
        L_0x03c2:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator.convertPDUtoCV(android.database.Cursor):java.util.ArrayList");
    }

    public static ArrayList<ContentValues> convertADDRtoCV(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList<ContentValues> contentvalues = new ArrayList<>();
        Cursor cs = cursor;
        do {
            try {
                ContentValues cv = new ContentValues();
                cv.put("_id", Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow("_id"))));
                cv.put("msg_id", Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow("msg_id"))));
                cv.put(CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID))));
                cv.put("address", cs.getString(cs.getColumnIndexOrThrow("address")));
                cv.put("type", Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow("type"))));
                cv.put("charset", Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow("charset"))));
                contentvalues.add(cv);
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cs.moveToNext());
        if (cs != null) {
            cs.close();
        }
        return contentvalues;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x011f A[SYNTHETIC, Splitter:B:24:0x011f] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.ArrayList<android.content.ContentValues> convertPARTtoCV(android.database.Cursor r19) {
        /*
            java.lang.String r0 = "text"
            java.lang.String r1 = "_data"
            java.lang.String r2 = "ctt_t"
            java.lang.String r3 = "ctt_s"
            java.lang.String r4 = "cl"
            java.lang.String r5 = "cid"
            java.lang.String r6 = "fn"
            java.lang.String r7 = "cd"
            java.lang.String r8 = "chset"
            java.lang.String r9 = "name"
            java.lang.String r10 = "ct"
            java.lang.String r11 = "seq"
            java.lang.String r12 = "mid"
            java.lang.String r13 = "_id"
            if (r19 != 0) goto L_0x0022
            r0 = 0
            return r0
        L_0x0022:
            java.util.ArrayList r14 = new java.util.ArrayList
            r14.<init>()
            r15 = r19
        L_0x0029:
            android.content.ContentValues r16 = new android.content.ContentValues     // Catch:{ all -> 0x011a }
            r16.<init>()     // Catch:{ all -> 0x011a }
            r17 = r16
            r16 = r14
            int r14 = r15.getColumnIndexOrThrow(r13)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getInt(r14)     // Catch:{ all -> 0x0115 }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ all -> 0x0115 }
            r18 = r0
            r0 = r17
            r0.put(r13, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r12)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getInt(r14)     // Catch:{ all -> 0x0115 }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r12, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getInt(r14)     // Catch:{ all -> 0x0115 }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r11, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x0115 }
            java.lang.String r14 = r15.getString(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r10, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getInt(r14)     // Catch:{ all -> 0x0115 }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r9, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getInt(r14)     // Catch:{ all -> 0x0115 }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r8, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x0115 }
            java.lang.String r14 = r15.getString(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r7, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x0115 }
            java.lang.String r14 = r15.getString(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r6, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x0115 }
            java.lang.String r14 = r15.getString(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r5, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x0115 }
            java.lang.String r14 = r15.getString(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r4, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getInt(r14)     // Catch:{ all -> 0x0115 }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r3, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x0115 }
            java.lang.String r14 = r15.getString(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r2, r14)     // Catch:{ all -> 0x0115 }
            int r14 = r15.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0115 }
            java.lang.String r14 = r15.getString(r14)     // Catch:{ all -> 0x0115 }
            r0.put(r1, r14)     // Catch:{ all -> 0x0115 }
            r17 = r1
            r14 = r18
            int r1 = r15.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x0115 }
            java.lang.String r1 = r15.getString(r1)     // Catch:{ all -> 0x0115 }
            r0.put(r14, r1)     // Catch:{ all -> 0x0115 }
            r1 = r16
            r1.add(r0)     // Catch:{ all -> 0x0112 }
            boolean r0 = r15.moveToNext()     // Catch:{ all -> 0x0112 }
            if (r0 != 0) goto L_0x010c
            if (r15 == 0) goto L_0x010b
            r15.close()
        L_0x010b:
            return r1
        L_0x010c:
            r0 = r14
            r14 = r1
            r1 = r17
            goto L_0x0029
        L_0x0112:
            r0 = move-exception
            r2 = r0
            goto L_0x011d
        L_0x0115:
            r0 = move-exception
            r1 = r16
            r2 = r0
            goto L_0x011d
        L_0x011a:
            r0 = move-exception
            r1 = r14
            r2 = r0
        L_0x011d:
            if (r15 == 0) goto L_0x0128
            r15.close()     // Catch:{ all -> 0x0123 }
            goto L_0x0128
        L_0x0123:
            r0 = move-exception
            r3 = r0
            r2.addSuppressed(r3)
        L_0x0128:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator.convertPARTtoCV(android.database.Cursor):java.util.ArrayList");
    }

    public static ArrayList<ContentValues> convertImdnNotificationtoCV(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList<ContentValues> contentvalues = new ArrayList<>();
        Cursor cs = cursor;
        do {
            try {
                ContentValues cv = new ContentValues();
                cv.put("id", Long.valueOf(cs.getLong(cs.getColumnIndexOrThrow("id"))));
                cv.put("imdn_id", cs.getString(cs.getColumnIndexOrThrow("imdn_id")));
                cv.put(ImContract.MessageNotification.SENDER_URI, cs.getString(cs.getColumnIndexOrThrow(ImContract.MessageNotification.SENDER_URI)));
                cv.put("status", Integer.valueOf(cs.getInt(cs.getColumnIndexOrThrow("status"))));
                cv.put("timestamp", Long.valueOf(cs.getLong(cs.getColumnIndexOrThrow("timestamp"))));
                contentvalues.add(cv);
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } while (cs.moveToNext());
        if (cs != null) {
            cs.close();
        }
        return contentvalues;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x01b8 A[SYNTHETIC, Splitter:B:21:0x01b8] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.ArrayList<android.content.ContentValues> convertRCSSessiontoCV(android.database.Cursor r25) {
        /*
            java.lang.String r0 = "sim_imsi"
            java.lang.String r1 = "icon_timestamp"
            java.lang.String r2 = "icon_participant"
            java.lang.String r3 = "icon_path"
            java.lang.String r4 = "preferred_uri"
            java.lang.String r5 = "session_uri"
            java.lang.String r6 = "contribution_id"
            java.lang.String r7 = "conversation_id"
            java.lang.String r8 = "direction"
            java.lang.String r9 = "imdn_notifications_availability"
            java.lang.String r10 = "max_participants_count"
            java.lang.String r11 = "is_muted"
            java.lang.String r12 = "subject"
            java.lang.String r13 = "status"
            java.lang.String r14 = "is_ft_group_chat"
            java.lang.String r15 = "is_group_chat"
            r16 = r0
            java.lang.String r0 = "chat_type"
            r17 = r1
            java.lang.String r1 = "own_sim_imsi"
            r18 = r2
            java.lang.String r2 = "chat_id"
            r19 = r3
            java.lang.String r3 = "_id"
            java.util.ArrayList r20 = new java.util.ArrayList
            r20.<init>()
            r21 = r20
            r20 = r25
        L_0x003f:
            android.content.ContentValues r22 = new android.content.ContentValues     // Catch:{ all -> 0x01b0 }
            r22.<init>()     // Catch:{ all -> 0x01b0 }
            r23 = r22
            r22 = r4
            r4 = r20
            r20 = r5
            int r5 = r4.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x01ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01ab }
            r24 = r6
            r6 = r23
            r6.put(r3, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x01ab }
            java.lang.String r5 = r4.getString(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r2, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x01ab }
            java.lang.String r5 = r4.getString(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r1, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x01ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r0, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r15)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x01ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r15, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x01ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r14, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r13)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x01ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r13, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r12)     // Catch:{ all -> 0x01ab }
            java.lang.String r5 = r4.getString(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r12, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x01ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r11, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x01ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r10, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x01ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r9, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x01ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r8, r5)     // Catch:{ all -> 0x01ab }
            int r5 = r4.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x01ab }
            java.lang.String r5 = r4.getString(r5)     // Catch:{ all -> 0x01ab }
            r6.put(r7, r5)     // Catch:{ all -> 0x01ab }
            r23 = r0
            r5 = r24
            int r0 = r4.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x01ab }
            java.lang.String r0 = r4.getString(r0)     // Catch:{ all -> 0x01ab }
            r6.put(r5, r0)     // Catch:{ all -> 0x01ab }
            r0 = r20
            r20 = r1
            int r1 = r4.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x01ab }
            java.lang.String r1 = r4.getString(r1)     // Catch:{ all -> 0x01ab }
            r6.put(r0, r1)     // Catch:{ all -> 0x01ab }
            r1 = r22
            r22 = r0
            int r0 = r4.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x01ab }
            java.lang.String r0 = r4.getString(r0)     // Catch:{ all -> 0x01ab }
            r6.put(r1, r0)     // Catch:{ all -> 0x01ab }
            r0 = r19
            r19 = r1
            int r1 = r4.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x01ab }
            java.lang.String r1 = r4.getString(r1)     // Catch:{ all -> 0x01ab }
            r6.put(r0, r1)     // Catch:{ all -> 0x01ab }
            r1 = r18
            r18 = r0
            int r0 = r4.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x01ab }
            java.lang.String r0 = r4.getString(r0)     // Catch:{ all -> 0x01ab }
            r6.put(r1, r0)     // Catch:{ all -> 0x01ab }
            r0 = r17
            r17 = r1
            int r1 = r4.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x01ab }
            java.lang.String r1 = r4.getString(r1)     // Catch:{ all -> 0x01ab }
            r6.put(r0, r1)     // Catch:{ all -> 0x01ab }
            r1 = r16
            r16 = r0
            int r0 = r4.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x01ab }
            java.lang.String r0 = r4.getString(r0)     // Catch:{ all -> 0x01ab }
            r6.put(r1, r0)     // Catch:{ all -> 0x01ab }
            r0 = r1
            r1 = r21
            r1.add(r6)     // Catch:{ all -> 0x01a8 }
            boolean r6 = r4.moveToNext()     // Catch:{ all -> 0x01a8 }
            if (r6 != 0) goto L_0x0191
            if (r4 == 0) goto L_0x0190
            r4.close()
        L_0x0190:
            return r1
        L_0x0191:
            r21 = r1
            r6 = r5
            r1 = r20
            r5 = r22
            r20 = r4
            r4 = r19
            r19 = r18
            r18 = r17
            r17 = r16
            r16 = r0
            r0 = r23
            goto L_0x003f
        L_0x01a8:
            r0 = move-exception
            r2 = r0
            goto L_0x01b6
        L_0x01ab:
            r0 = move-exception
            r1 = r21
            r2 = r0
            goto L_0x01b6
        L_0x01b0:
            r0 = move-exception
            r4 = r20
            r1 = r21
            r2 = r0
        L_0x01b6:
            if (r4 == 0) goto L_0x01c1
            r4.close()     // Catch:{ all -> 0x01bc }
            goto L_0x01c1
        L_0x01bc:
            r0 = move-exception
            r3 = r0
            r2.addSuppressed(r3)
        L_0x01c1:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator.convertRCSSessiontoCV(android.database.Cursor):java.util.ArrayList");
    }
}
