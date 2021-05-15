package com.sec.internal.ims.servicemodules.im;

import android.database.Cursor;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class ImDump {
    private static final String LOG_TAG = ImDump.class.getSimpleName();
    private static final int MAX_EVENT_LOGS = 3000;
    private static final int MAX_MESSAGE_DUMP = 50;
    Date date = new Date();
    private final ArrayBlockingQueue<String> mEventLogs = new ArrayBlockingQueue<>(3000);
    private final ImCache mImCache;
    SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");

    ImDump(ImCache imCache) {
        this.mImCache = imCache;
    }

    /* access modifiers changed from: protected */
    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + LOG_TAG + ":");
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Event Logs:");
        IMSLog.increaseIndent(LOG_TAG);
        Iterator<String> it = this.mEventLogs.iterator();
        while (it.hasNext()) {
            IMSLog.dump(LOG_TAG, it.next());
        }
        IMSLog.decreaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Active Sessions:");
        for (ImSession session : this.mImCache.getAllImSessions()) {
            IMSLog.dump(LOG_TAG, session.toString(), false);
            IMSLog.dump(LOG_TAG, "Pending messages:");
            IMSLog.increaseIndent(LOG_TAG);
            for (MessageBase m : this.mImCache.getAllPendingMessages(session.getChatId())) {
                IMSLog.dump(LOG_TAG, m.toString(), false);
            }
            IMSLog.decreaseIndent(LOG_TAG);
        }
        IMSLog.dump(LOG_TAG, "All Sessions:");
        try {
            for (ChatData chat : this.mImCache.getPersister().querySessions((String) null)) {
                ImSession session2 = this.mImCache.getImSession(chat.getChatId());
                if (session2 != null) {
                    IMSLog.dump(LOG_TAG, session2.toStringForDump(), false);
                    IMSLog.increaseIndent(LOG_TAG);
                    Iterator<String> it2 = generateMessagesForDump(this.mImCache.getPersister().queryMessagesByChatIdForDump(session2.getChatId(), 50)).iterator();
                    while (it2.hasNext()) {
                        IMSLog.dump(LOG_TAG, it2.next(), false);
                    }
                    IMSLog.decreaseIndent(LOG_TAG);
                }
            }
            IMSLog.decreaseIndent(LOG_TAG);
        } catch (SecurityException e) {
        }
    }

    /* access modifiers changed from: protected */
    public void addEventLogs(String logs) {
        this.date.setTime(System.currentTimeMillis());
        String datetime = this.timeFormat.format(this.date);
        ArrayBlockingQueue<String> arrayBlockingQueue = this.mEventLogs;
        if (!arrayBlockingQueue.offer(datetime + " " + logs)) {
            this.mEventLogs.poll();
            ArrayBlockingQueue<String> arrayBlockingQueue2 = this.mEventLogs;
            arrayBlockingQueue2.add(datetime + " " + logs);
        }
    }

    /* access modifiers changed from: protected */
    public ArrayList<String> generateMessagesForDump(Cursor cursor) {
        String str;
        String str2;
        if (cursor == null) {
            return null;
        }
        ArrayList<String> logs = new ArrayList<>();
        while (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndexOrThrow("message_type")) == 0) {
                str = "FtMessage [";
            } else if (cursor.getInt(cursor.getColumnIndexOrThrow("message_type")) == 1) {
                str = "ImMessage [";
            } else {
                str = "  Message [";
            }
            String str3 = str + "imdnId=" + cursor.getString(cursor.getColumnIndexOrThrow(ImContract.Message.IMDN_MESSAGE_ID)) + ", type=" + cursor.getString(cursor.getColumnIndexOrThrow("message_type")) + ", status=" + cursor.getInt(cursor.getColumnIndexOrThrow("status")) + ", direction=" + cursor.getInt(cursor.getColumnIndexOrThrow("direction")) + ", sentTime=" + cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.Message.SENT_TIMESTAMP)) + ", deliveredTime=" + cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.ChatItem.DELIVERED_TIMESTAMP)) + ", NotificationStatus=" + cursor.getInt(cursor.getColumnIndexOrThrow("notification_status"));
            if (cursor.getInt(cursor.getColumnIndexOrThrow("message_type")) == 0) {
                str2 = str3 + ", filename=" + IMSLog.checker(cursor.getString(cursor.getColumnIndexOrThrow("file_name"))) + ", transferredByte=" + cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.CsSession.BYTES_TRANSFERED)) + ", fileSize=" + cursor.getInt(cursor.getColumnIndexOrThrow("file_size"));
            } else {
                str2 = str3 + ", body=" + IMSLog.checker(cursor.getString(cursor.getColumnIndexOrThrow("body")));
            }
            logs.add(str2);
        }
        return logs;
    }

    /* access modifiers changed from: protected */
    public void dumpIncomingSession(int phoneId, ImSession session, boolean isDeferred, boolean isForStoredNoti) {
        String str;
        if (session != null) {
            List<String> dumps = new ArrayList<>();
            dumps.add(String.valueOf(session.getChatType().getId()));
            dumps.add(ImsUtil.hideInfo(session.getConversationId(), 4));
            String str2 = "1";
            dumps.add(isDeferred ? str2 : "0");
            if (isForStoredNoti) {
                str = str2;
            } else {
                str = "0";
            }
            dumps.add(str);
            if (!session.isChatbotRole()) {
                str2 = "0";
            }
            dumps.add(str2);
            ImsUtil.listToDumpFormat(LogClass.IM_RECV_SESSION, phoneId, session.getChatId(), dumps);
        }
    }

    /* access modifiers changed from: protected */
    public void dumpMessageSendingFailed(int phoneId, ImSession session, Result result, String imdnId, String response) {
        List<String> dumps = new ArrayList<>();
        dumps.add(ImsUtil.hideInfo(session.getConversationId(), 4));
        dumps.add(ImsUtil.hideInfo(imdnId, 4));
        if (!(result == null || result.getType() == Result.Type.NONE)) {
            dumps.add(result.toCriticalLog());
        }
        dumps.add(response);
        ImsUtil.listToDumpFormat(LogClass.IM_SEND_RES, phoneId, session.getChatId(), dumps);
    }

    /* access modifiers changed from: protected */
    public void dumpIncomingMessageReceived(int phoneId, boolean isGroupChat, String chatId, String imdnId) {
        List<String> dumps = new ArrayList<>();
        dumps.add(isGroupChat ? "1" : "0");
        dumps.add(ImsUtil.hideInfo(imdnId, 4));
        ImsUtil.listToDumpFormat(LogClass.IM_RECV_IM, phoneId, chatId, dumps);
    }
}
