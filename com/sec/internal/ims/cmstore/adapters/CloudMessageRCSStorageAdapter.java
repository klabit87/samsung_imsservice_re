package com.sec.internal.ims.cmstore.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.ims.cmstore.helper.RCSDBHelper;
import java.util.ArrayList;

public class CloudMessageRCSStorageAdapter {
    public static final String LOG_TAG = CloudMessageRCSStorageAdapter.class.getSimpleName();
    public final String PROVIDER_NAME = ImContract.PROVIDER_NAME;
    private final Context mContext;
    private final RCSDBHelper mRCSDBHelper;

    public CloudMessageRCSStorageAdapter(Context context) {
        this.mContext = context;
        this.mRCSDBHelper = new RCSDBHelper(this.mContext);
    }

    public Uri insertMessageFromBufferDb(ContentValues cv) {
        Uri insertMessageUri = Uri.parse("content://com.samsung.rcs.im/cloudinsertmessage");
        if (cv != null) {
            return this.mRCSDBHelper.insert(insertMessageUri, cv);
        }
        return null;
    }

    public int insertSessionFromBufferDbToRCSDb(ContentValues session, ArrayList<ContentValues> listpart) {
        Uri insertSessionUri = Uri.parse("content://com.samsung.rcs.im/cloudinsertsession");
        String str = LOG_TAG;
        Log.d(str, "insertSessionFromBufferDb: " + listpart.size());
        for (int i = 0; i < listpart.size(); i++) {
            listpart.get(i).putAll(session);
        }
        return this.mRCSDBHelper.insertSingleSessionPartsToDB(insertSessionUri, (ContentValues[]) listpart.toArray(new ContentValues[listpart.size()]));
    }

    public int updateSessionFromBufferDbToRCSDb(String chatId, ContentValues cv) {
        Uri sessionUri = Uri.parse("content://com.samsung.rcs.im/cloudupdatesession/" + chatId);
        return this.mRCSDBHelper.update(sessionUri, cv, "chat_id=?", new String[]{chatId});
    }

    public int updateParticipantsFromBufferDbToRCSDb(long _id, ContentValues cvupdate) {
        Uri partUri = Uri.parse("content://com.samsung.rcs.im/cloudupdateparticipant/" + _id);
        return this.mRCSDBHelper.update(partUri, cvupdate, "_id =?", new String[]{String.valueOf(_id)});
    }

    public Uri insertParticipantsFromBufferDbToRCSDb(ContentValues cvInsert) {
        return this.mRCSDBHelper.insert(Uri.parse("content://com.samsung.rcs.im/cloudinsertparticipant"), cvInsert);
    }

    public int updateMessageFromBufferDb(int rowID, ContentValues cv) {
        Uri updateMessageUri = Uri.parse("content://com.samsung.rcs.im/cloudupdatemessage/" + rowID);
        return this.mRCSDBHelper.update(updateMessageUri, cv, "_id=?", new String[]{String.valueOf(rowID)});
    }

    public Cursor queryAllSession() {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/session"), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryAllMessage() {
        Uri queryMessageUri = Uri.parse("content://com.samsung.rcs.im/message");
        Log.d(LOG_TAG, "queryAllMessage");
        return this.mRCSDBHelper.query(queryMessageUri, (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryIMFTUsingChatId(String chatId) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerymessagechatid/" + chatId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryParticipantsUsingChatId(String chatId) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudqueryparticipant/" + chatId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public int deleteParticipantsUsingRowId(long _id) {
        Uri delParticipantUri = Uri.parse("content://com.samsung.rcs.im/clouddeleteparticipant/" + _id);
        return this.mRCSDBHelper.delete(delParticipantUri, "chat_id =?", new String[]{String.valueOf(_id)});
    }

    public Cursor queryNotificationUsingImdn(String imdnId) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/messagenotifications/" + imdnId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryIMFTUsingRowId(long _id) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerymessagerowid/" + _id), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryRcsDBMessageUsingImdnId(String imdnId) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerymessageimdnid/" + imdnId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public int deleteRCSDBmessageUsingId(int _id) {
        Uri deleteMessageUri = Uri.parse("content://com.samsung.rcs.im/clouddeletemessage/" + _id);
        Integer _ID = Integer.valueOf(_id);
        return this.mRCSDBHelper.delete(deleteMessageUri, "_id=?", new String[]{_ID.toString()});
    }

    public Cursor querySessionUsingId(int _id) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerysessionid/" + _id), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor querySessionUsingChatId(String chatId) {
        return this.mRCSDBHelper.query(Uri.parse("content://com.samsung.rcs.im/cloudquerysessionchatid/" + chatId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Uri insertNotificationFromBufferDb(ContentValues cv) {
        if (cv == null) {
            return null;
        }
        return this.mRCSDBHelper.insert(Uri.parse("content://com.samsung.rcs.im/cloudinsertnotification"), cv);
    }

    public int updateRCSNotificationUsingImsdId(String imdnId, ContentValues cv) {
        if (cv == null) {
            return 0;
        }
        Uri updateMessageUri = Uri.parse("content://com.samsung.rcs.im/cloudupdatenotification/" + imdnId);
        return this.mRCSDBHelper.update(updateMessageUri, cv, "imdn_id=?", new String[]{imdnId});
    }
}
