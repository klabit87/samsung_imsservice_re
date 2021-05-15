package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.chat.GroupChat;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImSession;
import java.util.Collection;

public class ChatProvider extends ContentProvider {
    private static final int CHATS = 1;
    private static final int CHATS_ID = 6;
    private static final String[] CHAT_COLUMS = {"_id", "chat_id", "state", "subject", "direction", "timestamp", "reason_code", "participants", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT};
    private static final int CHAT_ID = 2;
    private static final String LOG_TAG = ChatProvider.class.getSimpleName();
    private static final int MESSAGES = 3;
    private static final int MESSAGES_CONTACTID = 5;
    private static final String[] MESSAGE_COLUNMS = {"_id", "chat_id", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "msg_id", "mime_type", "content", "status", CloudMessageProviderContract.BufferDBMMSpdu.READ_STATUS, "direction", "timestamp", "timestamp_sent", "timestamp_delivered", "timestamp_displayed", "reason_code", "expired_delivery"};
    private static final int MESSAGE_ID = 4;
    private static final String PROVIDER_NAME = ChatLog.GroupChat.CONTENT_URI.getAuthority();
    private static final UriMatcher uriMatcher;
    private ImCache mCache;

    static {
        UriMatcher uriMatcher2 = new UriMatcher(-1);
        uriMatcher = uriMatcher2;
        uriMatcher2.addURI(PROVIDER_NAME, "groupchat", 1);
        uriMatcher.addURI(PROVIDER_NAME, "groupchat/#", 2);
        uriMatcher.addURI(PROVIDER_NAME, "groupchat/*", 6);
        uriMatcher.addURI(PROVIDER_NAME, "chatmessage", 3);
        uriMatcher.addURI(PROVIDER_NAME, "chatmessage/#", 4);
        uriMatcher.addURI(PROVIDER_NAME, "chatmessage/*", 5);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        this.mCache = ImCache.getInstance();
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String str = LOG_TAG;
        Log.d(str, "query " + uri);
        if (!this.mCache.isLoaded()) {
            Log.e(LOG_TAG, "ImCache is not ready yet.");
            return null;
        }
        switch (uriMatcher.match(uri)) {
            case 1:
                return buildChatCursor();
            case 2:
                return buildChatCursor(uri);
            case 3:
                return buildMessagesCursor((Uri) null, projection, selection, selectionArgs, sortOrder);
            case 4:
                return buildMessagesCursor(uri, projection, selection, selectionArgs, sortOrder);
            case 5:
                return buildMessagesCursor(uri, projection, selection, selectionArgs, sortOrder);
            case 6:
                return buildChatCursor(uri);
            default:
                return null;
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private Cursor buildChatCursor(Uri uri) {
        MatrixCursor cursor = new MatrixCursor(CHAT_COLUMS);
        String chatId = uri.getLastPathSegment();
        if (chatId == null) {
            return cursor;
        }
        synchronized (this.mCache) {
            ImSession chat = this.mCache.getImSession(chatId);
            if (chat != null) {
                if (chat.isGroupChat()) {
                    fillChatCursor(chat, cursor);
                    return cursor;
                }
            }
            String str = LOG_TAG;
            Log.e(str, "buildChatCursor: Session not found " + chatId);
            return cursor;
        }
    }

    private Cursor buildChatCursor() {
        MatrixCursor cursor = new MatrixCursor(CHAT_COLUMS);
        synchronized (this.mCache) {
            Collection<ImSession> chatList = this.mCache.getAllImSessions();
            if (chatList == null) {
                return cursor;
            }
            for (ImSession chat : chatList) {
                if (chat.isGroupChat()) {
                    fillChatCursor(chat, cursor);
                }
            }
            return cursor;
        }
    }

    private void fillChatCursor(ImSession chat, MatrixCursor cursor) {
        int state = GroupChat.State.INITIATING.ordinal();
        int getState = chat.getChatStateId();
        if (ChatData.State.ACTIVE.getId() == getState) {
            state = GroupChat.State.STARTED.ordinal();
        } else if (ChatData.State.CLOSED_BY_USER.getId() == getState) {
            state = GroupChat.State.ABORTED.ordinal();
        }
        cursor.newRow().add(Long.valueOf((long) chat.getId())).add(chat.getChatId()).add(Integer.valueOf(state)).add(chat.getSubject()).add(Integer.valueOf(chat.getDirection().getId())).add((Object) null).add((Object) null).add((Object) null);
    }

    private Cursor buildMessagesCursor(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String idString;
        if (projection == null) {
            projection = MESSAGE_COLUNMS;
        }
        if (uri != null) {
            String idString2 = uri.getLastPathSegment();
            if (idString2 == null) {
                Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
                return null;
            }
            idString = idString2;
        } else {
            idString = null;
        }
        return fillMessageCursor(idString, projection, selection, selectionArgs, sortOrder);
    }

    /* JADX WARNING: Removed duplicated region for block: B:116:0x02d4 A[SYNTHETIC, Splitter:B:116:0x02d4] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.MatrixCursor fillMessageCursor(java.lang.String r21, java.lang.String[] r22, java.lang.String r23, java.lang.String[] r24, java.lang.String r25) {
        /*
            r20 = this;
            r1 = r21
            r0 = r23
            r2 = r25
            java.lang.String r3 = "rcs/groupchat-event"
            java.lang.String r4 = "application/geoloc"
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "fillMessageCursor idString: "
            r6.append(r7)
            r6.append(r1)
            java.lang.String r7 = ", projection: "
            r6.append(r7)
            java.lang.String r7 = java.util.Arrays.toString(r22)
            r6.append(r7)
            java.lang.String r7 = ", selection: "
            r6.append(r7)
            r6.append(r0)
            java.lang.String r7 = ", selectionArgs: "
            r6.append(r7)
            java.lang.String r7 = java.util.Arrays.toString(r24)
            r6.append(r7)
            java.lang.String r7 = ", sortOrder: "
            r6.append(r7)
            r6.append(r2)
            java.lang.String r6 = r6.toString()
            android.util.Log.d(r5, r6)
            java.lang.String r5 = "text/plain"
            if (r0 == 0) goto L_0x005b
            boolean r6 = r0.contains(r5)
            if (r6 == 0) goto L_0x005b
            java.lang.String r6 = "text/plain' OR mime_type ='text/plain;charset=UTF-8"
            java.lang.String r0 = r0.replace(r5, r6)
        L_0x005b:
            if (r1 == 0) goto L_0x009b
            boolean r6 = android.text.TextUtils.isEmpty(r0)
            if (r6 == 0) goto L_0x0076
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "msg_id = "
            r6.append(r7)
            r6.append(r1)
            java.lang.String r0 = r6.toString()
            r6 = r0
            goto L_0x009c
        L_0x0076:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "("
            r6.append(r7)
            r6.append(r0)
            java.lang.String r7 = ") AND "
            r6.append(r7)
            java.lang.String r7 = "msg_id"
            r6.append(r7)
            java.lang.String r7 = " = "
            r6.append(r7)
            r6.append(r1)
            java.lang.String r0 = r6.toString()
            r6 = r0
            goto L_0x009c
        L_0x009b:
            r6 = r0
        L_0x009c:
            r7 = r20
            com.sec.internal.ims.servicemodules.im.ImCache r0 = r7.mCache
            r8 = r22
            r9 = r24
            android.database.Cursor r10 = r0.queryChatMessagesForTapi(r8, r6, r9, r2)
            r11 = 0
            if (r10 != 0) goto L_0x00bf
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00b9 }
            java.lang.String r3 = "buildMessageCursor: Message not found."
            android.util.Log.e(r0, r3)     // Catch:{ all -> 0x00b9 }
            if (r10 == 0) goto L_0x00b8
            r10.close()
        L_0x00b8:
            return r11
        L_0x00b9:
            r0 = move-exception
            r1 = r0
            r19 = r6
            goto L_0x02d2
        L_0x00bf:
            java.lang.String[] r0 = r10.getColumnNames()     // Catch:{ all -> 0x02ce }
            r12 = r0
            android.database.MatrixCursor r0 = new android.database.MatrixCursor     // Catch:{ all -> 0x02ce }
            r0.<init>(r12)     // Catch:{ all -> 0x02ce }
            r13 = r0
        L_0x00ca:
            boolean r0 = r10.moveToNext()     // Catch:{ all -> 0x02ce }
            if (r0 == 0) goto L_0x02b5
            android.database.MatrixCursor$RowBuilder r0 = r13.newRow()     // Catch:{ all -> 0x02ce }
            r14 = r0
            r0 = 0
            int r15 = r12.length     // Catch:{ all -> 0x02ce }
            r16 = 0
            r11 = r0
            r1 = r16
        L_0x00dc:
            if (r1 >= r15) goto L_0x02a3
            r0 = r12[r1]     // Catch:{ all -> 0x02ce }
            r17 = r0
            r2 = r17
            int r0 = r10.getColumnIndex(r2)     // Catch:{ all -> 0x02ce }
            r17 = r0
            java.lang.String r0 = "status"
            boolean r0 = r0.equals(r2)     // Catch:{ all -> 0x02ce }
            if (r0 == 0) goto L_0x0142
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r0 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.DISPLAY_REPORT_REQUESTED     // Catch:{ all -> 0x02ce }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status[] r18 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.values()     // Catch:{ all -> 0x02ce }
            r19 = r6
            r6 = r17
            int r17 = r10.getInt(r6)     // Catch:{ all -> 0x02cb }
            r17 = r18[r17]     // Catch:{ all -> 0x02cb }
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r18 = com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl.translateStatus(r17)     // Catch:{ all -> 0x02cb }
            r0 = r18
            java.lang.String r7 = "timestamp_displayed"
            int r7 = r10.getColumnIndex(r7)     // Catch:{ all -> 0x02cb }
            java.lang.String r8 = "timestamp_delivered"
            int r8 = r10.getColumnIndex(r8)     // Catch:{ all -> 0x02cb }
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r9 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.SENT     // Catch:{ all -> 0x02cb }
            if (r0 != r9) goto L_0x0125
            int r9 = r10.getInt(r7)     // Catch:{ all -> 0x02cb }
            if (r9 <= 0) goto L_0x0125
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r9 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.DISPLAYED     // Catch:{ all -> 0x02cb }
            r0 = r9
            goto L_0x0132
        L_0x0125:
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r9 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.SENT     // Catch:{ all -> 0x02cb }
            if (r0 != r9) goto L_0x0132
            int r9 = r10.getInt(r8)     // Catch:{ all -> 0x02cb }
            if (r9 <= 0) goto L_0x0132
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r9 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.DELIVERED     // Catch:{ all -> 0x02cb }
            r0 = r9
        L_0x0132:
            int r9 = r0.ordinal()     // Catch:{ all -> 0x02cb }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x02cb }
            r14.add(r9)     // Catch:{ all -> 0x02cb }
            r18 = r3
            r2 = 0
            goto L_0x0293
        L_0x0142:
            r19 = r6
            r6 = r17
            java.lang.String r0 = "reason_code"
            boolean r0 = r0.equals(r2)     // Catch:{ all -> 0x02cb }
            if (r0 == 0) goto L_0x0161
            com.gsma.services.rcs.chat.ChatLog$Message$Content$ReasonCode r0 = com.gsma.services.rcs.chat.ChatLog.Message.Content.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x02cb }
            int r0 = r0.ordinal()     // Catch:{ all -> 0x02cb }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x02cb }
            r14.add(r0)     // Catch:{ all -> 0x02cb }
            r18 = r3
            r2 = 0
            goto L_0x0293
        L_0x0161:
            java.lang.String r0 = "read_status"
            boolean r0 = r0.equals(r2)     // Catch:{ all -> 0x02cb }
            if (r0 == 0) goto L_0x0198
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.values()     // Catch:{ all -> 0x02cb }
            int r7 = r10.getInt(r6)     // Catch:{ all -> 0x02cb }
            r0 = r0[r7]     // Catch:{ all -> 0x02cb }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r7 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x02cb }
            if (r7 != r0) goto L_0x0186
            com.gsma.services.rcs.RcsService$ReadStatus r7 = com.gsma.services.rcs.RcsService.ReadStatus.READ     // Catch:{ all -> 0x02cb }
            int r7 = r7.ordinal()     // Catch:{ all -> 0x02cb }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x02cb }
            r14.add(r7)     // Catch:{ all -> 0x02cb }
            goto L_0x0193
        L_0x0186:
            com.gsma.services.rcs.RcsService$ReadStatus r7 = com.gsma.services.rcs.RcsService.ReadStatus.UNREAD     // Catch:{ all -> 0x02cb }
            int r7 = r7.ordinal()     // Catch:{ all -> 0x02cb }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x02cb }
            r14.add(r7)     // Catch:{ all -> 0x02cb }
        L_0x0193:
            r18 = r3
            r2 = 0
            goto L_0x0293
        L_0x0198:
            java.lang.String r0 = "mime_type"
            boolean r0 = r0.equals(r2)     // Catch:{ all -> 0x02cb }
            if (r0 == 0) goto L_0x01d1
            java.lang.String r0 = r10.getString(r6)     // Catch:{ all -> 0x02cb }
            if (r0 == 0) goto L_0x01b0
            boolean r7 = r0.contains(r5)     // Catch:{ all -> 0x02cb }
            if (r7 == 0) goto L_0x01b0
            r14.add(r5)     // Catch:{ all -> 0x02cb }
            goto L_0x01cb
        L_0x01b0:
            if (r0 == 0) goto L_0x01bc
            boolean r7 = r0.contains(r4)     // Catch:{ all -> 0x02cb }
            if (r7 == 0) goto L_0x01bc
            r14.add(r4)     // Catch:{ all -> 0x02cb }
            goto L_0x01cb
        L_0x01bc:
            if (r0 == 0) goto L_0x01c8
            boolean r7 = r0.contains(r3)     // Catch:{ all -> 0x02cb }
            if (r7 == 0) goto L_0x01c8
            r14.add(r3)     // Catch:{ all -> 0x02cb }
            goto L_0x01cb
        L_0x01c8:
            r14.add(r0)     // Catch:{ all -> 0x02cb }
        L_0x01cb:
            r11 = r0
            r18 = r3
            r2 = 0
            goto L_0x0293
        L_0x01d1:
            java.lang.String r0 = "expired_delivery"
            boolean r0 = r0.equals(r2)     // Catch:{ all -> 0x02cb }
            r7 = 1
            if (r0 == 0) goto L_0x01f4
            long r8 = r10.getLong(r6)     // Catch:{ all -> 0x02cb }
            r17 = 0
            int r0 = (r8 > r17 ? 1 : (r8 == r17 ? 0 : -1))
            if (r0 <= 0) goto L_0x01e5
            goto L_0x01e7
        L_0x01e5:
            r7 = r16
        L_0x01e7:
            r0 = r7
            java.lang.Integer r7 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x02cb }
            r14.add(r7)     // Catch:{ all -> 0x02cb }
            r18 = r3
            r2 = 0
            goto L_0x0293
        L_0x01f4:
            java.lang.String r0 = "content"
            boolean r0 = r0.equals(r2)     // Catch:{ all -> 0x02cb }
            if (r0 == 0) goto L_0x024f
            if (r11 == 0) goto L_0x024f
            java.lang.String r0 = "rcspushlocation"
            boolean r0 = r11.contains(r0)     // Catch:{ all -> 0x02cb }
            if (r0 == 0) goto L_0x024a
            r7 = 0
            java.lang.String r0 = r10.getString(r6)     // Catch:{ all -> 0x02cb }
            r8 = r0
            com.sec.internal.ims.servicemodules.gls.GlsXmlParser r0 = new com.sec.internal.ims.servicemodules.gls.GlsXmlParser     // Catch:{ Exception -> 0x021b }
            r0.<init>()     // Catch:{ Exception -> 0x021b }
            java.lang.String r0 = r0.getGeolocString(r8)     // Catch:{ Exception -> 0x021b }
            r17 = r2
            r18 = r3
            goto L_0x0245
        L_0x021b:
            r0 = move-exception
            java.lang.String r9 = LOG_TAG     // Catch:{ all -> 0x02cb }
            r17 = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x02cb }
            r2.<init>()     // Catch:{ all -> 0x02cb }
            r18 = r3
            java.lang.String r3 = "parse error: "
            r2.append(r3)     // Catch:{ all -> 0x02cb }
            java.lang.String r3 = r0.getMessage()     // Catch:{ all -> 0x02cb }
            r2.append(r3)     // Catch:{ all -> 0x02cb }
            java.lang.String r3 = ", Geo location body : "
            r2.append(r3)     // Catch:{ all -> 0x02cb }
            r2.append(r8)     // Catch:{ all -> 0x02cb }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x02cb }
            android.util.Log.e(r9, r2)     // Catch:{ all -> 0x02cb }
            r2 = r8
            r0 = r2
        L_0x0245:
            r14.add(r0)     // Catch:{ all -> 0x02cb }
            r2 = 0
            goto L_0x0293
        L_0x024a:
            r17 = r2
            r18 = r3
            goto L_0x0253
        L_0x024f:
            r17 = r2
            r18 = r3
        L_0x0253:
            int r0 = r10.getType(r6)     // Catch:{ all -> 0x02cb }
            if (r0 == r7) goto L_0x0286
            r2 = 2
            if (r0 == r2) goto L_0x0279
            r2 = 3
            if (r0 == r2) goto L_0x0270
            r2 = 4
            if (r0 == r2) goto L_0x0267
            r2 = 0
            r14.add(r2)     // Catch:{ all -> 0x02cb }
            goto L_0x0293
        L_0x0267:
            r2 = 0
            byte[] r3 = r10.getBlob(r6)     // Catch:{ all -> 0x02cb }
            r14.add(r3)     // Catch:{ all -> 0x02cb }
            goto L_0x0293
        L_0x0270:
            r2 = 0
            java.lang.String r3 = r10.getString(r6)     // Catch:{ all -> 0x02cb }
            r14.add(r3)     // Catch:{ all -> 0x02cb }
            goto L_0x0293
        L_0x0279:
            r2 = 0
            float r3 = r10.getFloat(r6)     // Catch:{ all -> 0x02cb }
            java.lang.Float r3 = java.lang.Float.valueOf(r3)     // Catch:{ all -> 0x02cb }
            r14.add(r3)     // Catch:{ all -> 0x02cb }
            goto L_0x0293
        L_0x0286:
            r2 = 0
            long r7 = r10.getLong(r6)     // Catch:{ all -> 0x02cb }
            java.lang.Long r3 = java.lang.Long.valueOf(r7)     // Catch:{ all -> 0x02cb }
            r14.add(r3)     // Catch:{ all -> 0x02cb }
        L_0x0293:
            int r1 = r1 + 1
            r7 = r20
            r8 = r22
            r9 = r24
            r2 = r25
            r3 = r18
            r6 = r19
            goto L_0x00dc
        L_0x02a3:
            r18 = r3
            r19 = r6
            r2 = 0
            r7 = r20
            r1 = r21
            r8 = r22
            r9 = r24
            r11 = r2
            r2 = r25
            goto L_0x00ca
        L_0x02b5:
            r19 = r6
            android.content.Context r0 = r20.getContext()     // Catch:{ all -> 0x02cb }
            android.content.ContentResolver r0 = r0.getContentResolver()     // Catch:{ all -> 0x02cb }
            android.net.Uri r1 = com.gsma.services.rcs.chat.ChatLog.Message.CONTENT_URI     // Catch:{ all -> 0x02cb }
            r13.setNotificationUri(r0, r1)     // Catch:{ all -> 0x02cb }
            if (r10 == 0) goto L_0x02ca
            r10.close()
        L_0x02ca:
            return r13
        L_0x02cb:
            r0 = move-exception
            r1 = r0
            goto L_0x02d2
        L_0x02ce:
            r0 = move-exception
            r19 = r6
            r1 = r0
        L_0x02d2:
            if (r10 == 0) goto L_0x02dd
            r10.close()     // Catch:{ all -> 0x02d8 }
            goto L_0x02dd
        L_0x02d8:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x02dd:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.provider.ChatProvider.fillMessageCursor(java.lang.String, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String):android.database.MatrixCursor");
    }
}
