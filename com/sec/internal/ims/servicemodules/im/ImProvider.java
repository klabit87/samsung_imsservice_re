package com.sec.internal.ims.servicemodules.im;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ImProvider extends ContentProvider {
    private static final String[] AUTO_ACCEPT_FT = {"_id", ImContract.AutoAcceptFt.SETTING_VALUE};
    private static final String[] BOT_USER_AGENT_SETTING = {ImContract.BotUserAgent.BOT_USER_AGENT};
    private static final String[] CHAT_COLUMNS = {"_id", "chat_id", "sim_imsi", "is_group_chat", ImContract.ImSession.IS_FT_GROUP_CHAT, "status", "subject", ImContract.ImSession.IS_MUTED, ImContract.ImSession.MAX_PARTICIPANTS_COUNT, ImContract.ImSession.IMDN_NOTIFICATIONS_AVAILABILITY, ImContract.ImSession.PREFERRED_URI, ImContract.ImSession.OWN_PHONE_NUMBER, ImContract.ImSession.IS_CHATBOT_ROLE, "conversation_id", "contribution_id", "session_uri"};
    private static final String[] FILE_TRANSFER_COLUMNS = {"_id", "chat_id", "remote_uri", ImContract.ChatItem.USER_ALIAS, "file_path", "file_size", "state", "reason", "direction", "message_type AS type", ImContract.ChatItem.INSERTED_TIMESTAMP, ImContract.CsSession.BYTES_TRANSFERED, "content_type", ImContract.CsSession.STATUS, ImContract.CsSession.THUMBNAIL_PATH, ImContract.CsSession.IS_RESUMABLE, ImContract.ChatItem.DELIVERED_TIMESTAMP, "file_disposition", "playing_length", ImContract.ChatItem.EXT_INFO, ImContract.Message.IMDN_MESSAGE_ID, ImContract.Message.SENT_TIMESTAMP, ImContract.Message.MESSAGING_TECH, "sim_imsi"};
    private static final String LOG_TAG = ImProvider.class.getSimpleName();
    private static final String[] MESSAGE_COLUMNS = {"_id", ImContract.ChatItem.IS_FILE_TRANSFER, "direction", "chat_id", "remote_uri", ImContract.ChatItem.USER_ALIAS, "content_type", ImContract.ChatItem.INSERTED_TIMESTAMP, "body", ImContract.Message.NOTIFICATION_DISPOSITION_MASK, "notification_status", ImContract.Message.SENT_TIMESTAMP, ImContract.ChatItem.DELIVERED_TIMESTAMP, ImContract.Message.DISPLAYED_TIMESTAMP, "message_type", "status", ImContract.Message.NOT_DISPLAYED_COUNTER, ImContract.Message.IMDN_MESSAGE_ID, "maap_traffic_type", ImContract.Message.MESSAGING_TECH, "sim_imsi", "file_path", "file_size", "state", "reason", ImContract.CsSession.BYTES_TRANSFERED, ImContract.CsSession.STATUS, ImContract.CsSession.THUMBNAIL_PATH, ImContract.CsSession.IS_RESUMABLE, ImContract.ChatItem.EXT_INFO};
    private static final String[] MESSAGE_NOTIFICATIONS_COLUMNS = {"id", "imdn_id", ImContract.MessageNotification.SENDER_URI, "status", "timestamp"};
    private static final String[] PARTICIPANT_COLUMNS = {"_id", "chat_id", "status", "type", "uri", "alias"};
    private static final String PROVIDER_NAME = "com.samsung.rcs.im";
    private static final UriMatcher sUriMatcher;
    private final ImCache mCache = ImCache.getInstance();

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.rcs.im", "messages/*", 0);
        sUriMatcher.addURI("com.samsung.rcs.im", "message/#", 1);
        sUriMatcher.addURI("com.samsung.rcs.im", "messagescount/*", 2);
        sUriMatcher.addURI("com.samsung.rcs.im", "chats", 3);
        sUriMatcher.addURI("com.samsung.rcs.im", "enrichedchats", 13);
        sUriMatcher.addURI("com.samsung.rcs.im", "chat/*", 4);
        sUriMatcher.addURI("com.samsung.rcs.im", "participants/*", 5);
        sUriMatcher.addURI("com.samsung.rcs.im", "unreadmessages/*", 6);
        sUriMatcher.addURI("com.samsung.rcs.im", "unreadmessagescount", 7);
        sUriMatcher.addURI("com.samsung.rcs.im", "unreadmessagescount", 7);
        sUriMatcher.addURI("com.samsung.rcs.im", "unreadmessagescount", 7);
        sUriMatcher.addURI("com.samsung.rcs.im", "unreadmessagescount/*", 8);
        sUriMatcher.addURI("com.samsung.rcs.im", "filetransfers/*", 9);
        sUriMatcher.addURI("com.samsung.rcs.im", "filetransfer/#", 10);
        sUriMatcher.addURI("com.samsung.rcs.im", "messageswithft/*", 11);
        sUriMatcher.addURI("com.samsung.rcs.im", "autoacceptft", 12);
        sUriMatcher.addURI("com.samsung.rcs.im", "messageswithftcount/*", 16);
        sUriMatcher.addURI("com.samsung.rcs.im", "settings", 14);
        sUriMatcher.addURI("com.samsung.rcs.im", "messagenotifications/*", 15);
        sUriMatcher.addURI("com.samsung.rcs.im", "chatidsbycontenttype/*", 17);
        sUriMatcher.addURI("com.samsung.rcs.im", ImDBHelper.SESSION_TABLE, 18);
        sUriMatcher.addURI("com.samsung.rcs.im", "message", 19);
        sUriMatcher.addURI("com.samsung.rcs.im", "participant", 20);
        sUriMatcher.addURI("com.samsung.rcs.im", "clouddeletemessage/#", 23);
        sUriMatcher.addURI("com.samsung.rcs.im", "clouddeleteparticipant/#", 26);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudinsertmessage", 21);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudinsertparticipant", 24);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudupdatemessage/#", 22);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudupdateparticipant/#", 25);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudquerymessagerowid/#", 27);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudquerymessagechatid/*", 28);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudquerymessageimdnid/*", 38);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudqueryparticipant/*", 29);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudquerysessionid/#", 32);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudquerysessionchatid/*", 31);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudinsertsession", 30);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudinsertnotification", 39);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudupdatenotification/*", 40);
        sUriMatcher.addURI("com.samsung.rcs.im", "cloudupdatesession/*", 36);
        sUriMatcher.addURI("com.samsung.rcs.im", "getreliableimage/*", 35);
        sUriMatcher.addURI("com.samsung.rcs.im", "botsetting", 37);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String str = LOG_TAG;
        IMSLog.s(str, "delete " + uri);
        if (!this.mCache.isLoaded()) {
            Log.e(LOG_TAG, "ImCache is not ready yet.");
            return 0;
        }
        int match = sUriMatcher.match(uri);
        if (match == 23) {
            String id = LOG_TAG;
            IMSLog.s(id, "CLOUD_DELETE_MESSAGE " + uri);
            return this.mCache.cloudDeleteMessage(uri.getLastPathSegment());
        } else if (match != 26) {
            return 0;
        } else {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "CLOUD_DELETE_PARTICIPANT " + uri);
            return this.mCache.cloudDeleteParticipant(uri.getLastPathSegment());
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        String str = LOG_TAG;
        IMSLog.s(str, "insert " + uri);
        if (!this.mCache.isLoaded()) {
            Log.e(LOG_TAG, "ImCache is not ready yet.");
            return null;
        }
        int match = sUriMatcher.match(uri);
        if (match == 21) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "CLOUD_INSERT_MESSAGE " + uri);
            return this.mCache.cloudInsertMessage(uri, values);
        } else if (match == 24) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "CLOUD_INSERT_PARTICIPANT " + uri);
            return this.mCache.cloudInsertParticipant(uri, values);
        } else if (match != 39) {
            return null;
        } else {
            String str4 = LOG_TAG;
            IMSLog.s(str4, "BUFFERDB_INSERT_NOTIFICATION " + uri);
            return this.mCache.cloudInsertNotification(uri, values);
        }
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        String str = LOG_TAG;
        IMSLog.s(str, "bulkInsert " + uri);
        if (!this.mCache.isLoaded()) {
            Log.e(LOG_TAG, "ImCache is not ready yet.");
            return 0;
        } else if (sUriMatcher.match(uri) != 30) {
            return 0;
        } else {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "BUFFERDB_INSERT_SESSION " + uri);
            return this.mCache.cloudsearchAndInsertSession(uri, values);
        }
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String str = LOG_TAG;
        IMSLog.s(str, "query " + uri);
        if (!this.mCache.isLoaded() || RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()) == null) {
            Log.e(LOG_TAG, "ImCache is not ready yet or NoSimCard");
            return null;
        }
        int match = sUriMatcher.match(uri);
        if (match == 0 || match == 1) {
            return buildMessageCursor(uri, MESSAGE_COLUMNS);
        }
        if (match == 4) {
            return buildChatCursor(uri);
        }
        if (match == 5) {
            return buildParticipantCursor(uri);
        }
        if (match == 10) {
            return buildMessageCursor(uri, FILE_TRANSFER_COLUMNS);
        }
        if (match == 12) {
            return buildAutoAcceptFtCursor(uri);
        }
        if (match == 15) {
            return buildMessageNotificationsCursor(uri);
        }
        if (match == 31) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "BUFFERDB_QUERY_SESSION_CHATID " + uri);
            return buildSessionCursorForChatId(uri);
        } else if (match == 32) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "BUFFERDB_QUERY_SESSION_ID " + uri);
            return buildSessionCursorForSessionRowId(uri);
        } else if (match == 37) {
            IMSLog.s(LOG_TAG, "BOT_SETTING");
            return buildBotUserAgentCursor();
        } else if (match != 38) {
            switch (match) {
                case 18:
                    String str4 = LOG_TAG;
                    IMSLog.s(str4, "all_session query " + uri);
                    return this.mCache.querySessions(projection, selection, selectionArgs, sortOrder);
                case 19:
                    String str5 = LOG_TAG;
                    IMSLog.s(str5, "all_message query " + uri);
                    return this.mCache.queryMessages(projection, selection, selectionArgs, sortOrder);
                case 20:
                    String str6 = LOG_TAG;
                    IMSLog.s(str6, "all_participant query " + uri);
                    return this.mCache.queryParticipants(projection, selection, selectionArgs, sortOrder);
                default:
                    switch (match) {
                        case 27:
                            String str7 = LOG_TAG;
                            IMSLog.s(str7, "BUFFERDB_QUERY_MESSAGE_ROWID: " + uri);
                            return buildIMFTCursorForBufferDBRowId(uri);
                        case 28:
                            String str8 = LOG_TAG;
                            IMSLog.s(str8, "BUFFERDB_QUERY_MESSAGE_CHATID: " + uri);
                            return buildIMFTCursorForBufferDBChatId(uri);
                        case 29:
                            String str9 = LOG_TAG;
                            IMSLog.s(str9, "BUFFERDB_QUERY_PARTICIPANT: " + uri);
                            return buildParticipantCursorForBufferDB(uri);
                        default:
                            return null;
                    }
            }
        } else {
            String str10 = LOG_TAG;
            IMSLog.s(str10, "BUFFERDB_QUERY_MESSAGE_IMDNID: " + uri);
            return buildIMFTCursorForBufferDBImdnId(uri);
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String str = LOG_TAG;
        IMSLog.s(str, "update " + uri);
        String idString = uri.getLastPathSegment();
        if (!this.mCache.isLoaded()) {
            Log.e(LOG_TAG, "ImCache is not ready yet.");
            return 0;
        }
        int match = sUriMatcher.match(uri);
        if (match == 22) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "CLOUD_UPDATE_MESSAGE " + uri);
            return this.mCache.cloudUpdateMessage(idString, values);
        } else if (match == 25) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "CLOUD_UPDATE_PARTICIPANT " + uri);
            return this.mCache.cloudUpdateParticipant(idString, values);
        } else if (match == 36) {
            String str4 = LOG_TAG;
            IMSLog.s(str4, "CLOUD_UPDATE_SESSION " + uri);
            return this.mCache.cloudUpdateSession(idString, values);
        } else if (match != 40) {
            return 0;
        } else {
            String str5 = LOG_TAG;
            IMSLog.s(str5, "BUFFERDB_UPDATE_NOTIFICATION " + uri);
            return this.mCache.cloudupdateNotification(idString, values);
        }
    }

    private Cursor buildMessageCursor(Uri uri, String[] culumns) {
        String idString = uri.getLastPathSegment();
        if (idString == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        try {
            Integer.parseInt(idString);
            Cursor cursorDb = this.mCache.queryMessages(culumns, "_id= ?", new String[]{idString}, (String) null);
            if (cursorDb.getCount() != 0) {
                return cursorDb;
            }
            Log.e(LOG_TAG, "buildMessageCursor: Message not found.");
            cursorDb.close();
            return null;
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "buildMessageCursor: Invalid ID");
            return null;
        }
    }

    private Cursor buildChatCursor(Uri uri) {
        MatrixCursor cursor = new MatrixCursor(CHAT_COLUMNS);
        String chatId = uri.getLastPathSegment();
        if (chatId == null) {
            return cursor;
        }
        synchronized (this.mCache) {
            ImSession chat = this.mCache.getImSession(chatId);
            if (chat == null) {
                Log.e(LOG_TAG, "buildChatCursor: Session not found " + chatId);
                return cursor;
            }
            long j = 0;
            MatrixCursor.RowBuilder add = cursor.newRow().add(Long.valueOf((long) chat.getId())).add(chat.getChatId()).add(chat.getOwnImsi()).add(Long.valueOf(chat.isGroupChat() ? 1 : 0)).add(1L).add(Long.valueOf((long) chat.getChatStateId())).add(chat.getSubject()).add(Long.valueOf(chat.isMuted() ? 1 : 0)).add(Long.valueOf((long) chat.getMaxParticipantsCount())).add(1L).add(chat.getOwnPhoneNum());
            if (chat.isChatbotRole()) {
                j = 1;
            }
            add.add(Long.valueOf(j)).add(chat.getConversationId()).add(chat.getContributionId()).add(chat.getSessionUri());
            return cursor;
        }
    }

    private Cursor buildIMFTCursorForBufferDBChatId(Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "buildIMFTCursorForBufferDB: " + uri);
        String idString = uri.getLastPathSegment();
        if (idString == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        Cursor cursorDb = this.mCache.queryMessages((String[]) null, "chat_id= ? ", new String[]{idString}, (String) null);
        if (cursorDb.getCount() == 0) {
            Log.e(LOG_TAG, "buildMessageCursor: Message not found.");
        }
        return cursorDb;
    }

    private Cursor buildIMFTCursorForBufferDBRowId(Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "buildIMFTCursorForBufferDB: " + uri);
        String idString = uri.getLastPathSegment();
        if (idString == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        Cursor cursorDb = this.mCache.queryMessages((String[]) null, "_id= ? ", new String[]{idString}, (String) null);
        if (cursorDb.getCount() == 0) {
            Log.e(LOG_TAG, "buildMessageCursor: Message not found.");
        }
        return cursorDb;
    }

    private Cursor buildIMFTCursorForBufferDBImdnId(Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "buildIMFTCursorForBufferDBImdnId: " + uri.toString());
        String imdnIdString = uri.getLastPathSegment();
        if (imdnIdString == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        Cursor cursorDb = this.mCache.queryMessages((String[]) null, "imdn_message_id=? ", new String[]{imdnIdString}, (String) null);
        if (cursorDb != null && cursorDb.getCount() == 0) {
            Log.e(LOG_TAG, "buildMessageCursor: Message not found.");
        }
        return cursorDb;
    }

    private Cursor buildSessionCursorForSessionRowId(Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "buildSessionCursorForSessionRowId: " + uri);
        String idString = uri.getLastPathSegment();
        if (idString == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        Cursor cursorDb = this.mCache.querySessions((String[]) null, "_id= ? ", new String[]{idString}, (String) null);
        if (cursorDb.getCount() == 0) {
            Log.e(LOG_TAG, "buildMessageCursor: Message not found.");
        }
        return cursorDb;
    }

    private Cursor buildSessionCursorForChatId(Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "buildSessionCursorForchatId: " + uri);
        String idString = uri.getLastPathSegment();
        if (idString == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        Cursor cursorDb = this.mCache.querySessions((String[]) null, "chat_id= ? ", new String[]{idString}, (String) null);
        if (cursorDb != null && cursorDb.getCount() == 0) {
            Log.e(LOG_TAG, "buildMessageCursor: Message not found.");
        }
        return cursorDb;
    }

    private Cursor buildParticipantCursorForBufferDB(Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "buildParticipantCursorForBufferDB: " + uri);
        String idString = uri.getLastPathSegment();
        if (idString == null) {
            Log.e(LOG_TAG, "buildParticipantCursor: No last segment.");
            return null;
        }
        Cursor cursorDb = this.mCache.queryParticipants(PARTICIPANT_COLUMNS, "chat_id= ? ", new String[]{idString}, (String) null);
        if (cursorDb != null && cursorDb.getCount() == 0) {
            Log.e(LOG_TAG, "buildParticipantCursorForBufferDB: Message not found.");
        }
        return cursorDb;
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    private Cursor buildParticipantCursor(Uri uri) {
        MatrixCursor cursor = new MatrixCursor(PARTICIPANT_COLUMNS);
        try {
            String chatId = uri.getLastPathSegment();
            if (chatId == null) {
                Log.e(LOG_TAG, "buildParticipantCursor: No last segment.");
                return cursor;
            }
            Set<ImParticipant> participants = this.mCache.getParticipants(chatId);
            synchronized (this.mCache) {
                if (participants == null) {
                    Log.e(LOG_TAG, "buildParticipantCursor: Participant not found.");
                    return cursor;
                }
                String str = LOG_TAG;
                IMSLog.s(str, "buildParticipantCursor: build a cursor for " + participants);
                for (ImParticipant p : participants) {
                    cursor.newRow().add(Long.valueOf((long) p.getId())).add(p.getChatId()).add(Long.valueOf((long) p.getStatus().getId())).add(Long.valueOf((long) p.getType().getId())).add(p.getUri().toString()).add(p.getUserAlias());
                }
                return cursor;
            }
        } catch (Exception e) {
            cursor.close();
            return null;
        }
    }

    private Cursor buildMessageNotificationsCursor(Uri uri) {
        MatrixCursor cursor = new MatrixCursor(MESSAGE_NOTIFICATIONS_COLUMNS);
        String imdnString = uri.getLastPathSegment();
        if (imdnString == null) {
            Log.e(LOG_TAG, "buildMessageNotificationsCursor: No last segment.");
            cursor.close();
            return null;
        }
        String str = LOG_TAG;
        IMSLog.s(str, "imdn_id : " + imdnString);
        Cursor cursorDb = this.mCache.queryMessageNotification((String[]) null, "imdn_id= ? ", new String[]{imdnString}, (String) null);
        if (cursorDb == null) {
            try {
                Log.e(LOG_TAG, "buildMessageNotificationsCursor: Message not found.");
                cursor.close();
                if (cursorDb != null) {
                    cursorDb.close();
                }
                return null;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else if (cursorDb.getCount() == 0) {
            Log.e(LOG_TAG, "buildMessageNotificationsCursor: Message not found.");
            if (cursorDb != null) {
                cursorDb.close();
            }
            return cursor;
        } else {
            while (cursorDb.moveToNext()) {
                cursor.newRow().add(Long.valueOf(cursorDb.getLong(cursorDb.getColumnIndexOrThrow("id")))).add(cursorDb.getString(cursorDb.getColumnIndexOrThrow("imdn_id"))).add(cursorDb.getString(cursorDb.getColumnIndexOrThrow(ImContract.MessageNotification.SENDER_URI))).add(cursorDb.getString(cursorDb.getColumnIndexOrThrow("status"))).add(cursorDb.getString(cursorDb.getColumnIndexOrThrow("timestamp")));
            }
            if (cursorDb != null) {
                cursorDb.close();
            }
            return cursor;
        }
        throw th;
    }

    private Cursor buildAutoAcceptFtCursor(Uri uri) {
        int phoneId = UriUtil.getSimSlotFromUri(uri);
        long value = RcsConfigurationHelper.readLongParam(getContext(), ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT, phoneId), 0L).longValue();
        String simMnoName = (String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(phoneId)).map($$Lambda$_HmGrNbWrkSPdbrf7pQUJqFX84.INSTANCE).orElse("");
        if (value > 0 && simMnoName.equals("GenericIR92_US:CSpire")) {
            value = 2;
        }
        if (!RcsUtils.DualRcs.isDualRcsSettings()) {
            phoneId = SimUtil.getSimSlotPriority();
        }
        int accept = ImUserPreference.getInstance().getFtAutAccept(getContext(), phoneId);
        if (accept >= 0) {
            String str = LOG_TAG;
            Log.i(str, "buildAutoAcceptFtCursor: override with user setting - " + accept);
            value = (long) accept;
        }
        MatrixCursor cursor = new MatrixCursor(AUTO_ACCEPT_FT);
        cursor.newRow().add(0L).add(Long.valueOf(value));
        return cursor;
    }

    public void shutdown() {
        this.mCache.closeDB();
    }

    private Cursor buildBotUserAgentCursor() {
        String version;
        String model = ConfigConstants.BUILD.TERMINAL_MODEL;
        String version2 = ConfigConstants.BUILD.TERMINAL_SW_VERSION;
        ISimManager sm = SimManagerFactory.getSimManager();
        if (sm == null) {
            Log.e(LOG_TAG, "getUserAgent: ISimManager is null, return");
            return null;
        }
        String clientVersion = ImsRegistry.getString(sm.getSimSlotIndex(), GlobalSettingsConstants.RCS.RCS_CLIENT_VERSION, "6.0");
        Mno mno = sm.getSimMno();
        if (Mno.TMOBILE.equals(mno) || Mno.SFR.equals(mno) || Mno.TMOBILE_CZ.equals(mno)) {
            version = version2.length() > 8 ? version2.substring(version2.length() - 8) : version2;
        } else {
            version = version2.length() > 3 ? version2.substring(version2.length() - 3) : version2;
        }
        if (!mno.isEur()) {
            return null;
        }
        String useragent = String.format(ConfigConstants.TEMPLATE.USER_AGENT, new Object[]{model, version, clientVersion});
        MatrixCursor cursor = new MatrixCursor(BOT_USER_AGENT_SETTING);
        cursor.newRow().add(useragent);
        return cursor;
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        List<String> location = uri.getPathSegments();
        if (sUriMatcher.match(uri) != 35 || location.size() < 1) {
            return null;
        }
        File reliableImage = new File(getContext().getFilesDir().getAbsolutePath() + "/rcsreliable_d/" + location.get(location.size() - 1));
        if (!reliableImage.exists()) {
            return null;
        }
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(reliableImage, LogClass.SIM_EVENT);
        reliableImage.setLastModified(System.currentTimeMillis());
        String str = LOG_TAG;
        IMSLog.s(str, "get RELIABLE_IMAGE " + uri);
        return pfd;
    }
}
