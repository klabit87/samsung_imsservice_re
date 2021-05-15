package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BlockedNumberContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.ims.cmstore.adapters.CloudMessageRCSStorageAdapter;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.PayloadPartInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RcsQueryBuilder extends QueryBuilderBase {
    private static final String TAG = RcsQueryBuilder.class.getSimpleName();
    private final CloudMessageRCSStorageAdapter mRCSStorage;

    public RcsQueryBuilder(Context context, IBufferDBEventListener callbackapp) {
        super(context, callbackapp);
        this.mRCSStorage = new CloudMessageRCSStorageAdapter(context);
    }

    public Cursor searchIMFTBufferUsingImdn(String imdnId, String line) {
        String str = TAG;
        IMSLog.s(str, "searchIMFTBufferUsingImdn: " + IMSLog.checker(imdnId) + " line:" + IMSLog.checker(line));
        if (CloudMessageStrategyManager.getStrategy().isMultiLineSupported()) {
            String[] selectionArgs = {imdnId, line};
            return this.mBufferDB.queryRCSMessages((String[]) null, "imdn_message_id=? AND linenum=?", selectionArgs, (String) null);
        }
        return this.mBufferDB.queryRCSMessages((String[]) null, "imdn_message_id=?", new String[]{imdnId}, (String) null);
    }

    public Cursor searchBufferNotificationUsingImdn(String imdnId) {
        String str = TAG;
        Log.d(str, "searchBufferNotificationUsingImdn: " + IMSLog.checker(imdnId));
        return this.mBufferDB.queryRCSImdnUseImdnId(imdnId);
    }

    public Cursor searchBufferNotificationUsingImdnAndTelUri(String imdnId, String telUri) {
        String str = TAG;
        Log.d(str, "searchBufferNotificationUsingImdn: " + IMSLog.checker(imdnId) + ", telUri=" + IMSLog.checker(telUri));
        return this.mBufferDB.queryRCSImdnUseImdnIdAndTelUri(imdnId, telUri);
    }

    public Cursor searchIMFTBufferUsingRowId(String rowId) {
        return this.mBufferDB.queryRCSMessages((String[]) null, "_id=?", new String[]{rowId}, (String) null);
    }

    public Cursor searchIMFTBufferUsingChatId(String chatId) {
        return this.mBufferDB.queryRCSMessages((String[]) null, "chat_id=?", new String[]{chatId}, (String) null);
    }

    public Cursor queryRCSMessagesToUpload() {
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncdirection=? AND (res_url IS NULL OR res_url = '') AND inserted_timestamp > ?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2184))}, (String) null);
    }

    public Cursor queryRCSMessagesByChatId(String _id, String orderBy) {
        return this.mBufferDB.queryRCSMessages((String[]) null, "chat_id=?", new String[]{_id}, orderBy);
    }

    public Cursor queryImdnMessagesToUpload() {
        Log.d(TAG, "queryImdnMessagesToUpload()");
        return this.mBufferDB.queryRCSImdnMessages((String[]) null, "syncdirection=? AND (res_url IS NULL OR res_url = '') AND timestamp > ?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2184))}, (String) null);
    }

    public long insertToRCSMessagesBufferDB(Cursor cursor, String line, ContentValues cvFlags) {
        String str = TAG;
        IMSLog.s(str, "insertToRCSMessagesBufferDB(): " + IMSLog.checker(line) + "we do get something from RCS messages: " + cursor.getCount());
        long row = 0;
        ArrayList<ContentValues> cvs = CursorContentValueTranslator.convertRCSimfttoCV(cursor);
        String str2 = TAG;
        Log.d(str2, "insertToRCSMessagesBufferDB() size: " + cvs.size());
        for (int i = 0; i < cvs.size(); i++) {
            ContentValues cv = cvs.get(i);
            String remoteUri = PhoneUtils.extractNumberFromUri(cv.getAsString("remote_uri"));
            if (!CloudMessageStrategyManager.getStrategy().isNeedCheckBlockedNumberBeforeCopyRcsDb() || TextUtils.isEmpty(remoteUri) || !BlockedNumberContract.isBlocked(this.mContext, remoteUri)) {
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, cvFlags.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION));
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, cvFlags.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                cv.put("linenum", line);
                row = this.mBufferDB.insertDeviceMsgToBuffer(1, cv);
                Cursor csimdn = queryImdnUsingImdnId(cv.getAsString(ImContract.Message.IMDN_MESSAGE_ID));
                if (csimdn != null) {
                    try {
                        if (csimdn.moveToFirst()) {
                            insertToImdnNotificationBufferDB(csimdn, cvFlags);
                        }
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                }
                if (csimdn != null) {
                    csimdn.close();
                }
            } else {
                String str3 = TAG;
                Log.i(str3, "The number [" + IMSLog.checker(remoteUri) + "] has been add to block list. This message should avoid to save to BuffedDB!");
            }
        }
        if (cvs.size() == 1) {
            return row;
        }
        return row;
        throw th;
    }

    public long insertToImdnNotificationBufferDB(Cursor cursor, ContentValues cvFlags) {
        long row = 0;
        ArrayList<ContentValues> cvs = CursorContentValueTranslator.convertImdnNotificationtoCV(cursor);
        if (cvs == null) {
            return 0;
        }
        String str = TAG;
        Log.d(str, "insertToImdnNotificationBufferDB size: " + cvs.size());
        for (int i = 0; i < cvs.size(); i++) {
            ContentValues cv = cvs.get(i);
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, cvFlags.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, cvFlags.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
            row = this.mBufferDB.insertDeviceMsgToBuffer(13, cv);
        }
        String str2 = TAG;
        Log.d(str2, "row: " + row);
        if (cvs.size() == 1) {
            return row;
        }
        return row;
    }

    public void insertToRCSParticipantsBufferDB(Cursor cursor) {
        ArrayList<ContentValues> cvs = CursorContentValueTranslator.convertRCSparticipantstoCV(cursor);
        if (cvs != null) {
            String str = TAG;
            Log.d(str, "insertToRCSParticipantsBufferDB size: " + cvs.size());
            for (int i = 0; i < cvs.size(); i++) {
                insertDeviceMsgToBuffer(2, cvs.get(i));
            }
        }
    }

    public Cursor queryAllSession() {
        return this.mRCSStorage.queryAllSession();
    }

    public Cursor queryParticipantsUsingChatId(String chatId) {
        return this.mRCSStorage.queryParticipantsUsingChatId(chatId);
    }

    public int deleteParticipantsUsingRowId(long _id) {
        return this.mRCSStorage.deleteParticipantsUsingRowId(_id);
    }

    public int deleteParticipantsFromBufferDb(String uri, String chatid) {
        return this.mBufferDB.deleteTable(2, "uri=? AND chat_id=?", new String[]{uri, chatid});
    }

    public int updateRCSParticipantsDb(long _id, ContentValues cvupdate) {
        if (cvupdate.size() > 0) {
            return this.mRCSStorage.updateParticipantsFromBufferDbToRCSDb(_id, cvupdate);
        }
        return 0;
    }

    public int updateParticipantsBufferDb(String uri, ContentValues cvupdate) {
        String[] selectionArgs = {uri};
        if (cvupdate.size() > 0) {
            return this.mBufferDB.updateRCSParticipantsTable(cvupdate, "uri=?", selectionArgs);
        }
        return 0;
    }

    public Uri insertRCSParticipantsDb(ContentValues cvInsert) {
        if (cvInsert.size() > 0) {
            return this.mRCSStorage.insertParticipantsFromBufferDbToRCSDb(cvInsert);
        }
        return null;
    }

    public void insertRCSParticipantsDb(ArrayList<ContentValues> contentValuesList) {
        Iterator<ContentValues> it = contentValuesList.iterator();
        while (it.hasNext()) {
            ContentValues cvInsert = it.next();
            if (cvInsert.size() > 0) {
                this.mRCSStorage.insertParticipantsFromBufferDbToRCSDb(cvInsert);
            }
        }
    }

    public Cursor queryIMFTUsingChatId(String chatId) {
        return this.mRCSStorage.queryIMFTUsingChatId(chatId);
    }

    public Cursor queryImdnUsingImdnId(String imdnId) {
        return this.mRCSStorage.queryNotificationUsingImdn(imdnId);
    }

    public Cursor queryIMFTUsingRowId(long _id) {
        return this.mRCSStorage.queryIMFTUsingRowId(_id);
    }

    public Cursor queryRcsDBMessageUsingImdnId(String imdnId) {
        return this.mRCSStorage.queryRcsDBMessageUsingImdnId(imdnId);
    }

    public void insertAllToRCSSessionBufferDB(Cursor cursor) {
        ArrayList<ContentValues> cvs = CursorContentValueTranslator.convertRCSSessiontoCV(cursor);
        String str = TAG;
        Log.d(str, "insertAllToRCSSessionBufferDB size: " + cvs.size());
        String ownNumber = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
        for (int i = 0; i < cvs.size(); i++) {
            ContentValues cv = cvs.get(i);
            String line = cv.getAsString(ImContract.ImSession.PREFERRED_URI);
            if (line == null) {
                line = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
            }
            String chatId = cv.getAsString("chat_id");
            cv.put("linenum", line);
            ImsUri telUri = Util.getNormalizedTelUri(cv.getAsString(ImContract.ImSession.OWN_PHONE_NUMBER));
            if (telUri != null && !TextUtils.equals(telUri.toString(), ownNumber)) {
                String newConversationId = StringIdGenerator.generateConversationId();
                cv.put("conversation_id", newConversationId);
                String str2 = TAG;
                Log.d(str2, "new conv id====" + newConversationId);
            }
            insertDeviceMsgToBuffer(10, cv);
            Cursor csPart = queryParticipantsUsingChatId(chatId);
            if (csPart != null) {
                try {
                    if (csPart.moveToFirst()) {
                        insertToRCSParticipantsBufferDB(csPart);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (csPart != null) {
                csPart.close();
            }
            ContentValues cvFlags = new ContentValues();
            cvFlags.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
            cvFlags.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
            Cursor csMsg = queryIMFTUsingChatId(chatId);
            if (csMsg != null) {
                try {
                    if (csMsg.moveToFirst()) {
                        insertToRCSMessagesBufferDB(csMsg, line, cvFlags);
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            if (csMsg != null) {
                csMsg.close();
            }
        }
        return;
        throw th;
        throw th;
    }

    public void insertSingleSessionToRcsBuffer(Cursor cursor) {
        ArrayList<ContentValues> cvs = CursorContentValueTranslator.convertRCSSessiontoCV(cursor);
        for (int i = 0; i < cvs.size(); i++) {
            ContentValues cv = cvs.get(i);
            String line = cv.getAsString(ImContract.ImSession.PREFERRED_URI);
            if (line == null) {
                line = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
            }
            cv.put("linenum", line);
            insertDeviceMsgToBuffer(10, cv);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 26 */
    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:118)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:146)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:71)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:43)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:35)
        */
    public java.lang.String searchOrCreateSession(com.sec.internal.ims.cmstore.params.ParamOMAObject r27) {
        /*
            r26 = this;
            r1 = r26
            r2 = r27
            java.lang.String r0 = "participant = "
            r3 = 0
            java.util.Set<com.sec.ims.util.ImsUri> r4 = r2.mNomalizedOtherParticipants
            if (r4 == 0) goto L_0x045a
            java.util.Set<com.sec.ims.util.ImsUri> r4 = r2.mNomalizedOtherParticipants
            boolean r4 = r4.isEmpty()
            if (r4 != 0) goto L_0x045a
            java.lang.String r4 = r2.CONVERSATION_ID
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 == 0) goto L_0x001e
            goto L_0x045a
        L_0x001e:
            r4 = 0
            r5 = 0
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r6 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()
            boolean r6 = r6.isMultiLineSupported()
            if (r6 == 0) goto L_0x0044
            java.lang.String r6 = r2.mLine
            if (r6 == 0) goto L_0x0044
            java.lang.String r6 = r2.mLine
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r7 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
            java.lang.String r7 = r7.getUserTelCtn()
            boolean r6 = r6.equalsIgnoreCase(r7)
            if (r6 != 0) goto L_0x0044
            java.lang.String r6 = r2.mLine
            com.sec.ims.util.ImsUri r5 = com.sec.ims.util.ImsUri.parse(r6)
        L_0x0044:
            java.util.Set<com.sec.ims.util.ImsUri> r6 = r2.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            int r6 = r6.size()     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            r7 = 1
            if (r6 != r7) goto L_0x0061
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r6 = r1.mBufferDB     // Catch:{ NullPointerException -> 0x005c, all -> 0x0057 }
            java.util.Set<com.sec.ims.util.ImsUri> r8 = r2.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x005c, all -> 0x0057 }
            android.database.Cursor r6 = r6.querySessionByParticipants(r8, r5)     // Catch:{ NullPointerException -> 0x005c, all -> 0x0057 }
            r4 = r6
            goto L_0x0083
        L_0x0057:
            r0 = move-exception
            r18 = r5
            goto L_0x0454
        L_0x005c:
            r0 = move-exception
            r18 = r5
            goto L_0x0434
        L_0x0061:
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r6 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            boolean r6 = r6.querySessionByConversation()     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            if (r6 != 0) goto L_0x007a
            boolean r6 = r2.IS_OPEN_GROUP     // Catch:{ NullPointerException -> 0x005c, all -> 0x0057 }
            if (r6 == 0) goto L_0x0070
            goto L_0x007a
        L_0x0070:
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r6 = r1.mBufferDB     // Catch:{ NullPointerException -> 0x005c, all -> 0x0057 }
            java.util.Set<com.sec.ims.util.ImsUri> r8 = r2.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x005c, all -> 0x0057 }
            android.database.Cursor r6 = r6.querySessionByParticipants(r8, r5)     // Catch:{ NullPointerException -> 0x005c, all -> 0x0057 }
            r4 = r6
            goto L_0x0083
        L_0x007a:
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r6 = r1.mBufferDB     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            java.lang.String r8 = r2.CONVERSATION_ID     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            android.database.Cursor r6 = r6.querySessionByConversationId(r8)     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            r4 = r6
        L_0x0083:
            java.lang.String r6 = "phone"
            java.lang.String r8 = "chat_id"
            java.lang.String r9 = "sim_imsi"
            java.lang.String r10 = "subject"
            if (r4 == 0) goto L_0x02c8
            boolean r11 = r4.moveToFirst()     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            if (r11 == 0) goto L_0x02c8
            int r7 = r4.getColumnIndexOrThrow(r8)     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            java.lang.String r7 = r4.getString(r7)     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            r3 = r7
            java.lang.String r7 = TAG     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            r8.<init>()     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            java.lang.String r11 = "searchOrCreateSession, chatId found = "
            r8.append(r11)     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            r8.append(r3)     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            java.lang.String r8 = r8.toString()     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            android.util.Log.i(r7, r8)     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            java.lang.String r7 = "inserted_timestamp DESC"
            android.database.Cursor r8 = r1.queryRCSMessagesByChatId(r3, r7)     // Catch:{ NullPointerException -> 0x0431, all -> 0x042d }
            r11 = 0
            if (r8 == 0) goto L_0x00dd
            boolean r13 = r8.moveToFirst()     // Catch:{ all -> 0x00d3 }
            if (r13 == 0) goto L_0x00dd
            java.lang.String r13 = "inserted_timestamp"
            int r13 = r8.getColumnIndexOrThrow(r13)     // Catch:{ all -> 0x00d3 }
            long r13 = r8.getLong(r13)     // Catch:{ all -> 0x00d3 }
            r11 = r13
            goto L_0x00dd
        L_0x00d3:
            r0 = move-exception
            r18 = r5
            r16 = r7
            r17 = r8
            r5 = r0
            goto L_0x02bc
        L_0x00dd:
            java.lang.String r13 = TAG     // Catch:{ all -> 0x02b4 }
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ all -> 0x02b4 }
            r14.<init>()     // Catch:{ all -> 0x02b4 }
            java.lang.String r15 = "getDateFromDateString(objt.DATE)="
            r14.append(r15)     // Catch:{ all -> 0x02b4 }
            java.lang.String r15 = r2.DATE     // Catch:{ all -> 0x02b4 }
            r16 = r7
            r17 = r8
            long r7 = r1.getDateFromDateString(r15)     // Catch:{ all -> 0x02af }
            r14.append(r7)     // Catch:{ all -> 0x02af }
            java.lang.String r7 = ", timeStamp="
            r14.append(r7)     // Catch:{ all -> 0x02af }
            r14.append(r11)     // Catch:{ all -> 0x02af }
            java.lang.String r7 = "objt.IS_CPM_GROUP = "
            r14.append(r7)     // Catch:{ all -> 0x02af }
            boolean r7 = r2.IS_CPM_GROUP     // Catch:{ all -> 0x02af }
            r14.append(r7)     // Catch:{ all -> 0x02af }
            java.lang.String r7 = r14.toString()     // Catch:{ all -> 0x02af }
            android.util.Log.d(r13, r7)     // Catch:{ all -> 0x02af }
            int r7 = r4.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x02af }
            java.lang.String r7 = r4.getString(r7)     // Catch:{ all -> 0x02af }
            android.content.Context r8 = r1.mContext     // Catch:{ all -> 0x02af }
            java.lang.Object r6 = r8.getSystemService(r6)     // Catch:{ all -> 0x02af }
            android.telephony.TelephonyManager r6 = (android.telephony.TelephonyManager) r6     // Catch:{ all -> 0x02af }
            java.lang.String r8 = r6.getSubscriberId()     // Catch:{ all -> 0x02af }
            java.lang.String r13 = TAG     // Catch:{ all -> 0x02af }
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ all -> 0x02af }
            r14.<init>()     // Catch:{ all -> 0x02af }
            r14.append(r7)     // Catch:{ all -> 0x02af }
            java.lang.String r15 = " | update session sim imsi : "
            r14.append(r15)     // Catch:{ all -> 0x02af }
            r14.append(r8)     // Catch:{ all -> 0x02af }
            java.lang.String r14 = r14.toString()     // Catch:{ all -> 0x02af }
            android.util.Log.d(r13, r14)     // Catch:{ all -> 0x02af }
            boolean r13 = android.text.TextUtils.isEmpty(r7)     // Catch:{ all -> 0x02af }
            if (r13 == 0) goto L_0x015d
            boolean r13 = android.text.TextUtils.isEmpty(r8)     // Catch:{ all -> 0x0157 }
            if (r13 != 0) goto L_0x015d
            android.content.ContentValues r13 = new android.content.ContentValues     // Catch:{ all -> 0x0157 }
            r13.<init>()     // Catch:{ all -> 0x0157 }
            r13.put(r9, r8)     // Catch:{ all -> 0x0157 }
            r1.updateSessionBufferDb(r3, r13)     // Catch:{ all -> 0x0157 }
            r1.updateRCSSessionDb(r3, r13)     // Catch:{ all -> 0x0157 }
            goto L_0x015d
        L_0x0157:
            r0 = move-exception
            r18 = r5
            r5 = r0
            goto L_0x02bc
        L_0x015d:
            boolean r9 = r2.IS_CPM_GROUP     // Catch:{ all -> 0x02af }
            if (r9 == 0) goto L_0x02a2
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r9 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ all -> 0x02af }
            boolean r9 = r9.shouldCareGroupChatAttribute()     // Catch:{ all -> 0x02af }
            if (r9 == 0) goto L_0x02a2
            java.lang.String r9 = r2.DATE     // Catch:{ all -> 0x02af }
            long r13 = r1.getDateFromDateString(r9)     // Catch:{ all -> 0x02af }
            int r9 = (r13 > r11 ? 1 : (r13 == r11 ? 0 : -1))
            if (r9 <= 0) goto L_0x02a2
            int r9 = r4.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x02af }
            java.lang.String r9 = r4.getString(r9)     // Catch:{ all -> 0x02af }
            android.content.ContentValues r13 = new android.content.ContentValues     // Catch:{ all -> 0x02af }
            r13.<init>()     // Catch:{ all -> 0x02af }
            if (r9 == 0) goto L_0x019e
            java.lang.String r14 = r2.SUBJECT     // Catch:{ all -> 0x0157 }
            if (r14 == 0) goto L_0x019e
            java.lang.String r14 = r2.SUBJECT     // Catch:{ all -> 0x0157 }
            boolean r14 = r9.equals(r14)     // Catch:{ all -> 0x0157 }
            if (r14 != 0) goto L_0x019e
            java.lang.String r14 = TAG     // Catch:{ all -> 0x0157 }
            java.lang.String r15 = "subject has been changed, update it"
            android.util.Log.d(r14, r15)     // Catch:{ all -> 0x0157 }
            java.lang.String r14 = r2.SUBJECT     // Catch:{ all -> 0x0157 }
            r13.put(r10, r14)     // Catch:{ all -> 0x0157 }
        L_0x019e:
            int r10 = r13.size()     // Catch:{ all -> 0x02af }
            if (r10 <= 0) goto L_0x01aa
            r1.updateSessionBufferDb(r3, r13)     // Catch:{ all -> 0x0157 }
            r1.updateRCSSessionDb(r3, r13)     // Catch:{ all -> 0x0157 }
        L_0x01aa:
            java.util.HashSet r10 = new java.util.HashSet     // Catch:{ all -> 0x02af }
            java.util.Set<com.sec.ims.util.ImsUri> r14 = r2.mNomalizedOtherParticipants     // Catch:{ all -> 0x02af }
            r10.<init>(r14)     // Catch:{ all -> 0x02af }
            android.database.Cursor r14 = r1.queryParticipantsUsingChatId(r3)     // Catch:{ all -> 0x02af }
        L_0x01b5:
            boolean r15 = r14.moveToNext()     // Catch:{ all -> 0x028b }
            if (r15 == 0) goto L_0x0253
            java.lang.String r15 = "uri"
            int r15 = r14.getColumnIndexOrThrow(r15)     // Catch:{ all -> 0x028b }
            java.lang.String r15 = r14.getString(r15)     // Catch:{ all -> 0x028b }
            com.sec.ims.util.ImsUri r18 = com.sec.internal.ims.cmstore.utils.Util.getNormalizedTelUri(r15)     // Catch:{ all -> 0x028b }
            r19 = r18
            r18 = r5
            java.lang.String r5 = TAG     // Catch:{ all -> 0x024c }
            r20 = r6
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0247 }
            r6.<init>()     // Catch:{ all -> 0x0247 }
            r6.append(r0)     // Catch:{ all -> 0x0247 }
            r6.append(r15)     // Catch:{ all -> 0x0247 }
            r21 = r7
            java.lang.String r7 = ", telUri = "
            r6.append(r7)     // Catch:{ all -> 0x0288 }
            java.lang.String r7 = com.sec.internal.log.IMSLog.checker(r19)     // Catch:{ all -> 0x0288 }
            r6.append(r7)     // Catch:{ all -> 0x0288 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0288 }
            android.util.Log.e(r5, r6)     // Catch:{ all -> 0x0288 }
            r5 = r19
            if (r5 != 0) goto L_0x01fd
            r5 = r18
            r6 = r20
            r7 = r21
            goto L_0x01b5
        L_0x01fd:
            java.lang.String r6 = " is deleted from DB."
            boolean r7 = r10.contains(r5)     // Catch:{ all -> 0x0288 }
            if (r7 == 0) goto L_0x020d
            r10.remove(r5)     // Catch:{ all -> 0x0288 }
            java.lang.String r7 = " contains."
            r6 = r7
            r7 = r5
            goto L_0x0226
        L_0x020d:
            java.lang.String r7 = "_id"
            int r7 = r14.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x0288 }
            long r22 = r14.getLong(r7)     // Catch:{ all -> 0x0288 }
            r24 = r22
            r1.deleteParticipantsFromBufferDb(r15, r3)     // Catch:{ all -> 0x0288 }
            r7 = r5
            r19 = r6
            r5 = r24
            r1.deleteParticipantsUsingRowId(r5)     // Catch:{ all -> 0x0288 }
            r6 = r19
        L_0x0226:
            java.lang.String r5 = TAG     // Catch:{ all -> 0x0288 }
            r19 = r7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x0288 }
            r7.<init>()     // Catch:{ all -> 0x0288 }
            r7.append(r0)     // Catch:{ all -> 0x0288 }
            r7.append(r15)     // Catch:{ all -> 0x0288 }
            r7.append(r6)     // Catch:{ all -> 0x0288 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x0288 }
            android.util.Log.d(r5, r7)     // Catch:{ all -> 0x0288 }
            r5 = r18
            r6 = r20
            r7 = r21
            goto L_0x01b5
        L_0x0247:
            r0 = move-exception
            r21 = r7
            r5 = r0
            goto L_0x0293
        L_0x024c:
            r0 = move-exception
            r20 = r6
            r21 = r7
            r5 = r0
            goto L_0x0293
        L_0x0253:
            r18 = r5
            r20 = r6
            r21 = r7
            int r0 = r10.size()     // Catch:{ all -> 0x0288 }
            if (r0 <= 0) goto L_0x0282
            java.util.ArrayList r0 = r1.insertNewParticipantToBufferDB(r10, r3)     // Catch:{ all -> 0x0288 }
            r1.insertRCSParticipantsDb((java.util.ArrayList<android.content.ContentValues>) r0)     // Catch:{ all -> 0x0288 }
            java.lang.String r5 = TAG     // Catch:{ all -> 0x0288 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0288 }
            r6.<init>()     // Catch:{ all -> 0x0288 }
            java.lang.String r7 = "participants = "
            r6.append(r7)     // Catch:{ all -> 0x0288 }
            r6.append(r10)     // Catch:{ all -> 0x0288 }
            java.lang.String r7 = " are added into DB"
            r6.append(r7)     // Catch:{ all -> 0x0288 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0288 }
            android.util.Log.d(r5, r6)     // Catch:{ all -> 0x0288 }
        L_0x0282:
            if (r14 == 0) goto L_0x02a8
            r14.close()     // Catch:{ all -> 0x029f }
            goto L_0x02a8
        L_0x0288:
            r0 = move-exception
            r5 = r0
            goto L_0x0293
        L_0x028b:
            r0 = move-exception
            r18 = r5
            r20 = r6
            r21 = r7
            r5 = r0
        L_0x0293:
            if (r14 == 0) goto L_0x029e
            r14.close()     // Catch:{ all -> 0x0299 }
            goto L_0x029e
        L_0x0299:
            r0 = move-exception
            r6 = r0
            r5.addSuppressed(r6)     // Catch:{ all -> 0x029f }
        L_0x029e:
            throw r5     // Catch:{ all -> 0x029f }
        L_0x029f:
            r0 = move-exception
            r5 = r0
            goto L_0x02bc
        L_0x02a2:
            r18 = r5
            r20 = r6
            r21 = r7
        L_0x02a8:
            if (r17 == 0) goto L_0x02ad
            r17.close()     // Catch:{ NullPointerException -> 0x042b }
        L_0x02ad:
            goto L_0x0425
        L_0x02af:
            r0 = move-exception
            r18 = r5
            r5 = r0
            goto L_0x02bc
        L_0x02b4:
            r0 = move-exception
            r18 = r5
            r16 = r7
            r17 = r8
            r5 = r0
        L_0x02bc:
            if (r17 == 0) goto L_0x02c7
            r17.close()     // Catch:{ all -> 0x02c2 }
            goto L_0x02c7
        L_0x02c2:
            r0 = move-exception
            r6 = r0
            r5.addSuppressed(r6)     // Catch:{ NullPointerException -> 0x042b }
        L_0x02c7:
            throw r5     // Catch:{ NullPointerException -> 0x042b }
        L_0x02c8:
            r18 = r5
            android.content.ContentValues r0 = new android.content.ContentValues     // Catch:{ NullPointerException -> 0x042b }
            r0.<init>()     // Catch:{ NullPointerException -> 0x042b }
            java.util.Set<com.sec.ims.util.ImsUri> r5 = r2.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x042b }
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r11 = com.sec.internal.constants.ims.servicemodules.im.ChatMode.OFF     // Catch:{ NullPointerException -> 0x042b }
            int r11 = r11.getId()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = com.sec.internal.ims.util.StringIdGenerator.generateChatId(r5, r7, r11)     // Catch:{ NullPointerException -> 0x042b }
            r3 = r5
            r0.put(r8, r3)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = "own_sim_imsi"
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r8 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r8 = r8.getUserCtn()     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r8)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = "is_group_chat"
            java.util.Set<com.sec.ims.util.ImsUri> r8 = r2.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x042b }
            int r8 = r8.size()     // Catch:{ NullPointerException -> 0x042b }
            r11 = 0
            if (r8 <= r7) goto L_0x02fa
            r8 = r7
            goto L_0x02fb
        L_0x02fa:
            r8 = r11
        L_0x02fb:
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r8)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = "is_ft_group_chat"
            java.lang.Integer r8 = java.lang.Integer.valueOf(r7)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r8)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = "status"
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r8 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.INACTIVE     // Catch:{ NullPointerException -> 0x042b }
            int r8 = r8.getId()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r8)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = r2.SUBJECT     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r10, r5)     // Catch:{ NullPointerException -> 0x042b }
            java.util.Set<com.sec.ims.util.ImsUri> r5 = r2.mNomalizedOtherParticipants     // Catch:{ NullPointerException -> 0x042b }
            int r5 = r5.size()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r8 = "chat_type"
            if (r5 <= r7) goto L_0x0370
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r5 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ NullPointerException -> 0x042b }
            boolean r5 = r5.shouldCareGroupChatAttribute()     // Catch:{ NullPointerException -> 0x042b }
            if (r5 == 0) goto L_0x034a
            boolean r5 = r2.IS_OPEN_GROUP     // Catch:{ NullPointerException -> 0x042b }
            if (r5 == 0) goto L_0x033b
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.REGULAR_GROUP_CHAT     // Catch:{ NullPointerException -> 0x042b }
            goto L_0x033d
        L_0x033b:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT     // Catch:{ NullPointerException -> 0x042b }
        L_0x033d:
            int r10 = r5.getId()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r8, r10)     // Catch:{ NullPointerException -> 0x042b }
            goto L_0x037d
        L_0x034a:
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r5 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ NullPointerException -> 0x042b }
            boolean r5 = r5.querySessionByConversation()     // Catch:{ NullPointerException -> 0x042b }
            if (r5 == 0) goto L_0x0362
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.REGULAR_GROUP_CHAT     // Catch:{ NullPointerException -> 0x042b }
            int r5 = r5.getId()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r8, r5)     // Catch:{ NullPointerException -> 0x042b }
            goto L_0x037d
        L_0x0362:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT     // Catch:{ NullPointerException -> 0x042b }
            int r5 = r5.getId()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r8, r5)     // Catch:{ NullPointerException -> 0x042b }
            goto L_0x037d
        L_0x0370:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.ONE_TO_ONE_CHAT     // Catch:{ NullPointerException -> 0x042b }
            int r5 = r5.getId()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r8, r5)     // Catch:{ NullPointerException -> 0x042b }
        L_0x037d:
            java.lang.String r5 = "is_muted"
            java.lang.Integer r8 = java.lang.Integer.valueOf(r11)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r8)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = "max_participants_count"
            r8 = 100
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r8)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = "imdn_notifications_availability"
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r7)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = "direction"
            java.lang.String r7 = "IN"
            java.lang.String r8 = r2.DIRECTION     // Catch:{ NullPointerException -> 0x042b }
            boolean r7 = r7.equalsIgnoreCase(r8)     // Catch:{ NullPointerException -> 0x042b }
            if (r7 == 0) goto L_0x03ad
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r7 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING     // Catch:{ NullPointerException -> 0x042b }
            int r7 = r7.getId()     // Catch:{ NullPointerException -> 0x042b }
            goto L_0x03b3
        L_0x03ad:
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r7 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ NullPointerException -> 0x042b }
            int r7 = r7.getId()     // Catch:{ NullPointerException -> 0x042b }
        L_0x03b3:
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r7)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = "conversation_id"
            java.lang.String r7 = r2.CONVERSATION_ID     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r7)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = "contribution_id"
            java.lang.String r7 = r2.CONTRIBUTION_ID     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r7)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r5 = r2.mLine     // Catch:{ NullPointerException -> 0x042b }
            if (r5 == 0) goto L_0x03e4
            java.lang.String r5 = r2.mLine     // Catch:{ NullPointerException -> 0x042b }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r7 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r7 = r7.getUserTelCtn()     // Catch:{ NullPointerException -> 0x042b }
            boolean r5 = r5.equalsIgnoreCase(r7)     // Catch:{ NullPointerException -> 0x042b }
            if (r5 != 0) goto L_0x03e4
            java.lang.String r5 = "preferred_uri"
            java.lang.String r7 = r2.mLine     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r7)     // Catch:{ NullPointerException -> 0x042b }
        L_0x03e4:
            java.lang.String r5 = "linenum"
            java.lang.String r7 = r2.mLine     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r5, r7)     // Catch:{ NullPointerException -> 0x042b }
            android.content.Context r5 = r1.mContext     // Catch:{ NullPointerException -> 0x042b }
            java.lang.Object r5 = r5.getSystemService(r6)     // Catch:{ NullPointerException -> 0x042b }
            android.telephony.TelephonyManager r5 = (android.telephony.TelephonyManager) r5     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r6 = r5.getSubscriberId()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r7 = TAG     // Catch:{ NullPointerException -> 0x042b }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ NullPointerException -> 0x042b }
            r8.<init>()     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r10 = "session sim imsi : "
            r8.append(r10)     // Catch:{ NullPointerException -> 0x042b }
            r8.append(r6)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r8 = r8.toString()     // Catch:{ NullPointerException -> 0x042b }
            android.util.Log.d(r7, r8)     // Catch:{ NullPointerException -> 0x042b }
            r0.put(r9, r6)     // Catch:{ NullPointerException -> 0x042b }
            r7 = 10
            r1.insertTable(r7, r0)     // Catch:{ NullPointerException -> 0x042b }
            java.util.ArrayList r7 = r1.insertRCSParticipantToBufferDBUsingObject(r2, r3)     // Catch:{ NullPointerException -> 0x042b }
            com.sec.internal.ims.cmstore.adapters.CloudMessageRCSStorageAdapter r8 = r1.mRCSStorage     // Catch:{ NullPointerException -> 0x042b }
            int r8 = r8.insertSessionFromBufferDbToRCSDb(r0, r7)     // Catch:{ NullPointerException -> 0x042b }
            java.lang.String r9 = r1.updateBufferDbChatIdFromRcsDb(r3, r8)     // Catch:{ NullPointerException -> 0x042b }
            r3 = r9
        L_0x0425:
            if (r4 == 0) goto L_0x0452
        L_0x0427:
            r4.close()
            goto L_0x0452
        L_0x042b:
            r0 = move-exception
            goto L_0x0434
        L_0x042d:
            r0 = move-exception
            r18 = r5
            goto L_0x0454
        L_0x0431:
            r0 = move-exception
            r18 = r5
        L_0x0434:
            java.lang.String r5 = TAG     // Catch:{ all -> 0x0453 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0453 }
            r6.<init>()     // Catch:{ all -> 0x0453 }
            java.lang.String r7 = "nullpointer exception: "
            r6.append(r7)     // Catch:{ all -> 0x0453 }
            java.lang.String r7 = r0.getMessage()     // Catch:{ all -> 0x0453 }
            r6.append(r7)     // Catch:{ all -> 0x0453 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0453 }
            android.util.Log.e(r5, r6)     // Catch:{ all -> 0x0453 }
            if (r4 == 0) goto L_0x0452
            goto L_0x0427
        L_0x0452:
            return r3
        L_0x0453:
            r0 = move-exception
        L_0x0454:
            if (r4 == 0) goto L_0x0459
            r4.close()
        L_0x0459:
            throw r0
        L_0x045a:
            java.lang.String r0 = TAG
            java.lang.String r4 = "searchOrCreateSession, invalid OMA param issue"
            android.util.Log.e(r0, r4)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder.searchOrCreateSession(com.sec.internal.ims.cmstore.params.ParamOMAObject):java.lang.String");
    }

    private String updateBufferDbChatIdFromRcsDb(String bufferChatId, int sessionId) {
        if (sessionId < 1) {
            return bufferChatId;
        }
        String chatId = null;
        Cursor cursorSession = this.mRCSStorage.querySessionUsingId(sessionId);
        if (cursorSession != null) {
            try {
                if (cursorSession.moveToFirst()) {
                    chatId = cursorSession.getString(cursorSession.getColumnIndexOrThrow("chat_id"));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursorSession != null) {
            cursorSession.close();
        }
        if (bufferChatId.equalsIgnoreCase(chatId) || chatId == null) {
            return bufferChatId;
        }
        updateChatId(bufferChatId, chatId);
        return chatId;
        throw th;
    }

    private void updateChatId(String old, String newId) {
        String str = TAG;
        Log.d(str, "updateChatId: " + old + " chatid: " + newId);
        ContentValues cvsession = new ContentValues();
        cvsession.put("chat_id", newId);
        this.mBufferDB.updateRCSSessionTable(cvsession, "chat_id=?", new String[]{old});
        ContentValues cvparticipants = new ContentValues();
        cvparticipants.put("chat_id", newId);
        this.mBufferDB.updateRCSParticipantsTable(cvparticipants, "chat_id=?", new String[]{old});
    }

    public ArrayList<ContentValues> insertRCSParticipantToBufferDBUsingObject(ParamOMAObject objt, String chatId) {
        ArrayList<ContentValues> list = new ArrayList<>();
        for (ImsUri uri : objt.mNomalizedOtherParticipants) {
            ContentValues cv = new ContentValues();
            cv.put("chat_id", chatId);
            cv.put("type", Integer.valueOf(ImParticipant.Type.REGULAR.getId()));
            cv.put("status", Integer.valueOf(ImParticipant.Status.ACCEPTED.getId()));
            cv.put("uri", uri.toString());
            insertTable(2, cv);
            list.add(cv);
        }
        return list;
    }

    public ArrayList<ContentValues> insertNewParticipantToBufferDB(Set<ImsUri> participantsInObject, String chatId) {
        ArrayList<ContentValues> list = new ArrayList<>();
        for (ImsUri uri : participantsInObject) {
            ContentValues cv = new ContentValues();
            cv.put("chat_id", chatId);
            cv.put("type", Integer.valueOf(ImParticipant.Type.REGULAR.getId()));
            cv.put("status", Integer.valueOf(ImParticipant.Status.ACCEPTED.getId()));
            cv.put("uri", uri.toString());
            insertTable(2, cv);
            list.add(cv);
        }
        return list;
    }

    public int queryImdnBufferDBandUpdateRcsMessageBufferDb(String imdnId, int notDisplayedCnt) {
        String str = TAG;
        Log.i(str, "queryImdnBufferDBandUpdateRcsMessageBufferDb: " + IMSLog.checker(imdnId) + ", notDisplayedCnt=" + notDisplayedCnt);
        int rowId = 0;
        Cursor csimdn = this.mBufferDB.queryRCSImdnUseImdnId(imdnId);
        if (csimdn != null) {
            try {
                if (csimdn.moveToFirst()) {
                    int displayedCnt = 0;
                    int deliveredCnt = 0;
                    ContentValues cv = new ContentValues();
                    do {
                        int imdnStatus = csimdn.getInt(csimdn.getColumnIndexOrThrow("status"));
                        if (imdnStatus == NotificationStatus.DISPLAYED.getId()) {
                            displayedCnt++;
                            cv.put("notification_status", Integer.valueOf(NotificationStatus.DISPLAYED.getId()));
                            cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(NotificationStatus.DISPLAYED.getId()));
                            cv.put(ImContract.Message.DISPLAYED_TIMESTAMP, Integer.valueOf(csimdn.getInt(csimdn.getColumnIndexOrThrow("timestamp"))));
                        } else if (imdnStatus == NotificationStatus.DELIVERED.getId()) {
                            deliveredCnt++;
                        }
                    } while (csimdn.moveToNext() != 0);
                    String str2 = TAG;
                    Log.d(str2, "queryImdnBufferDBandUpdateRcsMessageBufferDb: displayedCnt=" + displayedCnt + ", deliveredCnt=" + deliveredCnt);
                    if (displayedCnt == 0 && deliveredCnt > 0) {
                        csimdn.moveToFirst();
                        cv.put("notification_status", Integer.valueOf(NotificationStatus.DELIVERED.getId()));
                        cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(NotificationStatus.DELIVERED.getId()));
                        cv.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(csimdn.getLong(csimdn.getColumnIndexOrThrow("timestamp"))));
                    }
                    if (displayedCnt > 0) {
                        int newNotDisplayedCnt = notDisplayedCnt - displayedCnt;
                        cv.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(newNotDisplayedCnt >= 0 ? newNotDisplayedCnt : 0));
                    }
                    if (cv.size() > 0) {
                        rowId = this.mBufferDB.updateRCSTable(cv, "imdn_message_id=?", new String[]{imdnId});
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (csimdn != null) {
            csimdn.close();
        }
        return rowId;
        throw th;
    }

    public int updateRcsMessageBufferDbIfNewIMDNReceived(String imdnId, int notDisplayedCnt, int rcsMsgDisplayStatus, ParamOMAObject imdnObjt) {
        int newIMDNStatus;
        Log.d(TAG, "updateRcsMessageBufferDbIfNewIMDNReceived: " + IMSLog.checker(imdnId) + ", notDisplayedCnt = " + notDisplayedCnt + ", msgStatus=" + rcsMsgDisplayStatus);
        int newNotDisplayedCnt = notDisplayedCnt;
        if (ATTConstants.ATTDispositionStatus.DISPLAYED.equalsIgnoreCase(imdnObjt.DISPOSITION_STATUS)) {
            newIMDNStatus = NotificationStatus.DISPLAYED.getId();
        } else {
            newIMDNStatus = NotificationStatus.DELIVERED.getId();
        }
        ContentValues cv = new ContentValues();
        if (newIMDNStatus == NotificationStatus.DISPLAYED.getId()) {
            if (rcsMsgDisplayStatus == NotificationStatus.NONE.getId() || rcsMsgDisplayStatus == NotificationStatus.DELIVERED.getId()) {
                cv.put("notification_status", Integer.valueOf(NotificationStatus.DISPLAYED.getId()));
                cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(NotificationStatus.DISPLAYED.getId()));
            }
            int newNotDisplayedCnt2 = newNotDisplayedCnt - 1;
            cv.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(newNotDisplayedCnt2 >= 0 ? newNotDisplayedCnt2 : 0));
        } else if (newIMDNStatus == NotificationStatus.DELIVERED.getId() && rcsMsgDisplayStatus == NotificationStatus.NONE.getId()) {
            cv.put("notification_status", Integer.valueOf(NotificationStatus.DELIVERED.getId()));
            cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(NotificationStatus.DELIVERED.getId()));
        }
        if (cv.size() <= 0) {
            return 0;
        }
        return this.mBufferDB.updateRCSTable(cv, "imdn_message_id=?", new String[]{imdnId});
    }

    public int queryBufferDbandUpdateRcsMessageDb(String imdnId) {
        String str = TAG;
        Log.d(str, "queryBufferDbandUpdateRcsMessageDb: " + IMSLog.checker(imdnId));
        int rowId = 0;
        Cursor csimdn = this.mBufferDB.queryRCSMessages((String[]) null, "imdn_message_id=?", new String[]{imdnId}, (String) null);
        if (csimdn != null) {
            try {
                if (csimdn.moveToFirst()) {
                    ContentValues cv = new ContentValues();
                    cv.put("notification_status", csimdn.getString(csimdn.getColumnIndex("notification_status")));
                    cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, csimdn.getString(csimdn.getColumnIndex(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS)));
                    cv.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(csimdn.getLong(csimdn.getColumnIndex(ImContract.ChatItem.DELIVERED_TIMESTAMP))));
                    rowId = this.mRCSStorage.updateMessageFromBufferDb(csimdn.getInt(csimdn.getColumnIndex("_id")), cv);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (csimdn != null) {
            csimdn.close();
        }
        return rowId;
        throw th;
    }

    public long insertRCSimdnToBufferDBUsingObject(ParamOMAObject objt) {
        int message_id;
        String str = TAG;
        Log.i(str, "insertRCSimdnToBufferDBUsingObject: " + objt);
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, objt.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(objt.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(objt.parentFolder.toString()));
        if (!TextUtils.isEmpty(objt.path)) {
            cv.put("path", Util.decodeUrlFromServer(objt.path.toString()));
        }
        cv.put("imdn_id", objt.DISPOSITION_ORIGINAL_MESSAGEID);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        if ("IN".equalsIgnoreCase(objt.DIRECTION)) {
            cv.put(ImContract.MessageNotification.SENDER_URI, Util.getTelUri(Util.getPhoneNum(objt.DISPOSITION_ORIGINAL_TO)));
        }
        if (ATTConstants.ATTDispositionStatus.DISPLAYED.equalsIgnoreCase(objt.DISPOSITION_STATUS)) {
            cv.put("status", Integer.valueOf(NotificationStatus.DISPLAYED.getId()));
        } else {
            cv.put("status", Integer.valueOf(NotificationStatus.DELIVERED.getId()));
        }
        if (CloudMessageStrategyManager.getStrategy().isStoreImdnEnabled() && (message_id = insertRCSNotificationDbfromBufferDB(new ContentValues(cv))) > 0) {
            cv.put("id", Integer.valueOf(message_id));
        }
        return insertTable(13, cv);
    }

    public int updateRCSNotificationUsingImsdId(String imdnId, ContentValues cvupdate) {
        ContentValues cv = removeExtensionColumns(cvupdate);
        if (cv.size() > 0) {
            return this.mRCSStorage.updateRCSNotificationUsingImsdId(imdnId, cv);
        }
        return 0;
    }

    public ParamSyncFlagsSet insertRCSMessageToBufferDBUsingObject(ParamOMAObject objt, String chatId, boolean isInitialSync) {
        int i;
        ParamOMAObject paramOMAObject = objt;
        String str = chatId;
        boolean z = isInitialSync;
        String str2 = TAG;
        Log.i(str2, "insertRCSMessageToBufferDBUsingObject: " + paramOMAObject + " chatid: " + str + " isInitialSync: " + z);
        boolean isConvertedFromChatToFt = false;
        ParamSyncFlagsSet resultParam = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
        resultParam.mBufferId = -1;
        ContentValues cv = new ContentValues();
        ContentValues cvPayload = new ContentValues();
        if (paramOMAObject.mNomalizedOtherParticipants == null || paramOMAObject.mNomalizedOtherParticipants.isEmpty() || paramOMAObject.CONVERSATION_ID == null) {
            Log.e(TAG, "insertRCSMessageToBufferDBUsingObject, invalid OMA param issue");
            return resultParam;
        } else if (paramOMAObject.payloadPart == null || paramOMAObject.payloadPart.length > 0) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, paramOMAObject.correlationId);
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, paramOMAObject.correlationTag);
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject.lastModSeq);
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject.parentFolder.toString()));
            if (paramOMAObject.payloadURL != null) {
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, paramOMAObject.payloadURL.toString());
            }
            if (!CloudMessageStrategyManager.getStrategy().isNmsEventHasMessageDetail() && !TextUtils.isEmpty(paramOMAObject.path)) {
                cv.put("path", Util.decodeUrlFromServer(paramOMAObject.path.toString()));
            }
            cv.put(ImContract.ChatItem.IS_FILE_TRANSFER, Integer.valueOf(paramOMAObject.mObjectType == 12 ? 1 : 0));
            if (paramOMAObject.mObjectType == 14) {
                cv.put(ImContract.ChatItem.IS_FILE_TRANSFER, Integer.valueOf((paramOMAObject.payloadURL != null || (paramOMAObject.payloadPart != null && paramOMAObject.payloadPart.length > 0)) ? 1 : 0));
            }
            if ("IN".equalsIgnoreCase(paramOMAObject.DIRECTION)) {
                i = ImDirection.INCOMING.getId();
            } else {
                i = ImDirection.OUTGOING.getId();
            }
            cv.put("direction", Integer.valueOf(i));
            cv.put("chat_id", str);
            if ("IN".equalsIgnoreCase(paramOMAObject.DIRECTION)) {
                cv.put("remote_uri", paramOMAObject.FROM);
            } else if (paramOMAObject.TO.size() == 1) {
                cv.put("remote_uri", paramOMAObject.TO.get(0));
            }
            if (!TextUtils.isEmpty(paramOMAObject.MULTIPARTCONTENTTYPE)) {
                cv.put("content_type", paramOMAObject.MULTIPARTCONTENTTYPE);
            } else if (!TextUtils.isEmpty(paramOMAObject.CONTENT_TYPE)) {
                cv.put("content_type", paramOMAObject.CONTENT_TYPE);
            }
            if (!TextUtils.isEmpty(paramOMAObject.CONTENT_TYPE) && !TextUtils.isEmpty(paramOMAObject.TEXT_CONTENT) && isContentTypeDefined(paramOMAObject.CONTENT_TYPE)) {
                String fileextension = getFileExtension(paramOMAObject.CONTENT_TYPE);
                if ("txt".equals(fileextension)) {
                    Log.d(TAG, "no change, just save as txt");
                } else {
                    String filename = Util.getRandomFileName(fileextension);
                    try {
                        String filePath = Util.generateUniqueFilePath(this.mContext, filename, false);
                        Util.saveFiletoPath(paramOMAObject.TEXT_CONTENT.getBytes(), filePath);
                        cv.put("file_name", filename);
                        cv.put("file_path", filePath);
                        cv.put("file_size", Integer.valueOf(paramOMAObject.TEXT_CONTENT.length()));
                        cv.put(ImContract.CsSession.BYTES_TRANSFERED, Integer.valueOf(paramOMAObject.TEXT_CONTENT.length()));
                        cv.put("content_type", paramOMAObject.CONTENT_TYPE);
                        isConvertedFromChatToFt = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            cv.put(ImContract.ChatItem.INSERTED_TIMESTAMP, Long.valueOf(getDateFromDateString(paramOMAObject.DATE)));
            cv.put("body", paramOMAObject.TEXT_CONTENT);
            int disposition = NotificationStatus.encode(EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED));
            cv.put(ImContract.Message.NOTIFICATION_DISPOSITION_MASK, Integer.valueOf(disposition));
            cv.put("notification_status", Integer.valueOf(NotificationStatus.NONE.getId()));
            cv.put(ImContract.Message.DISPOSITION_NOTIFICATION_STATUS, Integer.valueOf(NotificationStatus.NONE.getId()));
            cv.put(ImContract.Message.SENT_TIMESTAMP, Long.valueOf(getDateFromDateString(paramOMAObject.DATE)));
            if ("IN".equalsIgnoreCase(paramOMAObject.DIRECTION)) {
                cv.put(ImContract.ChatItem.DELIVERED_TIMESTAMP, Long.valueOf(getDateFromDateString(paramOMAObject.DATE)));
                String readStatus = "insertRCSMessage is unread: ";
                if (getIfSeenValueUsingFlag(paramOMAObject.mFlagList) == 1) {
                    readStatus = "insertRCSMessage is read: ";
                    cv.put("status", Integer.valueOf(ImConstants.Status.READ.getId()));
                    cv.put(ImContract.CsSession.STATUS, Integer.valueOf(ImConstants.Status.READ.getId()));
                } else {
                    cv.put("status", Integer.valueOf(ImConstants.Status.UNREAD.getId()));
                    cv.put(ImContract.CsSession.STATUS, Integer.valueOf(ImConstants.Status.UNREAD.getId()));
                }
                Log.d(TAG, readStatus);
            } else {
                cv.put("status", Integer.valueOf(ImConstants.Status.SENT.getId()));
                cv.put(ImContract.CsSession.STATUS, Integer.valueOf(ImConstants.Status.SENT.getId()));
            }
            if (paramOMAObject.TEXT_CONTENT != null) {
                cv.put("message_type", Integer.valueOf(ImConstants.Type.TEXT.getId()));
            } else {
                cv.put("state", 3);
                cv.put("message_type", Integer.valueOf(ImConstants.Type.MULTIMEDIA.getId()));
            }
            cv.put(ImContract.Message.MESSAGE_ISSLM, Integer.valueOf(paramOMAObject.mObjectType == 14 ? 1 : 0));
            cv.put(ImContract.Message.NOT_DISPLAYED_COUNTER, Integer.valueOf(paramOMAObject.mNomalizedOtherParticipants.size()));
            cv.put(ImContract.Message.IMDN_MESSAGE_ID, paramOMAObject.correlationId);
            cv.put(ImContract.Message.IMDN_ORIGINAL_TO, paramOMAObject.DISPOSITION_ORIGINAL_TO);
            cv.put("conversation_id", paramOMAObject.CONVERSATION_ID);
            cv.put("contribution_id", paramOMAObject.CONTRIBUTION_ID);
            if (CloudMessageStrategyManager.getStrategy().isThumbNailEnabledForRcsFT()) {
                String str3 = TAG;
                Log.d(str3, "objt.payloadURL : " + paramOMAObject.payloadURL);
                if (paramOMAObject.payloadPart != null) {
                    cvPayload = handlePayloadParts(paramOMAObject.payloadPart, paramOMAObject.CONTENT_TYPE);
                }
            } else if (paramOMAObject.payloadPart != null && paramOMAObject.payloadPart.length > 0) {
                ArrayList<PayloadPartInfo> validPayload = new ArrayList<>();
                if (paramOMAObject.payloadPart.length > 1) {
                    validPayload = getValidPayload(paramOMAObject.payloadPart, MIMEContentType.BOT_SUGGESTION);
                }
                if (validPayload.size() == 0) {
                    Log.d(TAG, "no visible payload!");
                    validPayload.add(paramOMAObject.payloadPart[0]);
                }
                cvPayload = handlePayloadWithThumbnail(validPayload, paramOMAObject);
            }
            if (paramOMAObject.mIsGoforwardSync) {
                if (paramOMAObject.mReassembled) {
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
                    resultParam.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
                    resultParam.mDirection = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
                    int i2 = disposition;
                } else {
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
                    resultParam.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.None;
                    resultParam.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                    int i3 = disposition;
                }
            } else if (cvPayload.containsKey(ImContract.CsSession.THUMBNAIL_PATH)) {
                String str4 = TAG;
                int i4 = disposition;
                Log.d(str4, "saves valid thumbnail: from downloaded object: " + cvPayload.getAsString(ImContract.CsSession.THUMBNAIL_PATH));
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
                resultParam.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
                resultParam.mDirection = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
            } else {
                if (paramOMAObject.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
                    resultParam.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Deleted;
                    resultParam.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                } else {
                    if (paramOMAObject.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                        String str5 = TAG;
                        Log.d(str5, "insertRCSMessageToBufferDBUsingObject: " + CloudMessageBufferDBConstants.ActionStatusFlag.Update);
                    } else {
                        Log.d(TAG, "insertRCSMessageToBufferDBUsingObject: no read flag");
                    }
                    if (paramOMAObject.TEXT_CONTENT != null) {
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
                        resultParam.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
                        resultParam.mDirection = CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice;
                    } else {
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
                        resultParam.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad;
                        resultParam.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Downloading;
                    }
                }
            }
            if (isConvertedFromChatToFt) {
                paramOMAObject.TEXT_CONTENT = null;
                cv.put("body", "");
                cv.put("state", 3);
                cv.put("message_type", Integer.valueOf(ImConstants.Type.MULTIMEDIA.getId()));
            }
            cv.put("linenum", Util.getLineTelUriFromObjUrl(paramOMAObject.resourceURL.toString()));
            cv.putAll(cvPayload);
            String simImsi = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getSubscriberId();
            String str6 = TAG;
            Log.d(str6, "sim imsi : " + simImsi);
            cv.put("sim_imsi", simImsi);
            if (z || paramOMAObject.mIsFromChangedObj || CloudMessageStrategyManager.getStrategy().alwaysInsertMsgWhenNonExist() || (CloudMessageStrategyManager.getStrategy().isSupportAtt72HoursRule() && Util.isOver72Hours(paramOMAObject.DATE))) {
                String str7 = TAG;
                Log.d(str7, "initial sync insert RCS db or normal sync from extended changed object: " + resultParam);
                resultParam.mBufferId = insertTable(1, cv);
                insertRCSMessageDbfromBufferDB(resultParam.mBufferId, cv);
            } else {
                String str8 = TAG;
                Log.d(str8, "normal sync insert RCS db: set action as: " + CloudMessageBufferDBConstants.ActionStatusFlag.None + " " + CloudMessageBufferDBConstants.DirectionFlag.Done);
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
                resultParam.mAction = CloudMessageBufferDBConstants.ActionStatusFlag.None;
                resultParam.mDirection = CloudMessageBufferDBConstants.DirectionFlag.Done;
                resultParam.mBufferId = insertTable(1, cv);
            }
            return resultParam;
        } else {
            Log.e(TAG, "insertRCSMessageToBufferDBUsingObject, invalid payloadPart");
            return resultParam;
        }
    }

    public int updateRCSMessageInBufferDBUsingObject(ParamOMAObject objt, ContentValues cv, String selection, String[] selectionArgs) {
        ContentValues cvPayload = new ContentValues();
        if (objt.payloadPart != null && objt.payloadPart.length > 0) {
            ArrayList<PayloadPartInfo> validPayload = new ArrayList<>();
            if (objt.payloadPart.length > 1) {
                validPayload = getValidPayload(objt.payloadPart, MIMEContentType.BOT_SUGGESTION);
            }
            if (validPayload != null && validPayload.size() == 0) {
                Log.d(TAG, "no visible payload!");
                validPayload.add(objt.payloadPart[0]);
            }
            cvPayload = handlePayloadWithThumbnail(validPayload, objt);
        }
        cv.putAll(cvPayload);
        return updateTable(1, cv, selection, selectionArgs);
    }

    private ArrayList<PayloadPartInfo> getValidPayload(PayloadPartInfo[] parts, String exceptionCt) {
        if (parts == null) {
            return null;
        }
        ArrayList<PayloadPartInfo> payloadPartInfoList = new ArrayList<>();
        for (PayloadPartInfo part : parts) {
            if (part != null && !part.contentType.toUpperCase().contains(exceptionCt.toUpperCase())) {
                payloadPartInfoList.add(part);
            }
        }
        return payloadPartInfoList;
    }

    public ContentValues handlePayloadParts(PayloadPartInfo[] parts, String contenttype) {
        ContentValues cv = new ContentValues();
        if (parts == null) {
            return cv;
        }
        if (contenttype.contains("start")) {
            for (PayloadPartInfo part : parts) {
                String str = null;
                if (part.contentId != null && part.contentEncoding != null && contenttype.contains(part.contentId) && HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64.equalsIgnoreCase(part.contentEncoding)) {
                    try {
                        if (part.href != null) {
                            str = part.href.toString();
                        }
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB, str);
                        byte[] decodedthumbnail = Base64.decode(part.content, 0);
                        String filename = Util.getRandomFileName(getFileExtension(part.contentType));
                        String filepath = Util.generateUniqueFilePath(this.mContext, filename, false);
                        Util.saveFiletoPath(decodedthumbnail, filepath);
                        cv.put("content_type", part.contentType);
                        cv.put(ImContract.CsSession.THUMBNAIL_PATH, filepath);
                        cv.put("file_name", filename);
                    } catch (IOException e) {
                        Log.e(TAG, "IOException: " + e.getMessage());
                        e.printStackTrace();
                        return cv;
                    } catch (NullPointerException e0) {
                        Log.e(TAG, "nullpointer: " + e0.getMessage());
                        e0.printStackTrace();
                        return cv;
                    }
                } else if (part.contentType != null && isContentTypeDefined(part.contentType)) {
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADENCODING, Integer.valueOf(translatePayloadEncoding(part.contentEncoding).getId()));
                    if (part.href != null) {
                        str = part.href.toString();
                    }
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL, str);
                    String filename2 = Util.getRandomFileName(getFileExtension(part.contentType));
                    cv.put("content_type", part.contentType);
                    cv.put("file_name", filename2);
                }
            }
        } else {
            for (PayloadPartInfo part2 : parts) {
                if (isContentTypeDefined(part2.contentType)) {
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADENCODING, Integer.valueOf(translatePayloadEncoding(part2.contentEncoding).getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL, part2.href.toString());
                    String filename3 = Util.getRandomFileName(getFileExtension(part2.contentType));
                    cv.put("content_type", part2.contentType);
                    cv.put("file_name", filename3);
                }
            }
        }
        return cv;
    }

    public ContentValues handlePayloadWithThumbnail(ArrayList<PayloadPartInfo> parts, ParamOMAObject objt) {
        ContentValues cv = new ContentValues();
        if (parts == null) {
            return cv;
        }
        if (parts.size() > 1) {
            String fileIconCId = null;
            Iterator<PayloadPartInfo> it = parts.iterator();
            while (it.hasNext()) {
                PayloadPartInfo part = it.next();
                if (part.fileIcon != null) {
                    fileIconCId = part.fileIcon.toString();
                    String[] splitFileIconCId = fileIconCId.split(":");
                    if (splitFileIconCId != null && splitFileIconCId.length > 1) {
                        fileIconCId = splitFileIconCId[1];
                    }
                    String str = TAG;
                    Log.d(str, "fileIconCId : " + fileIconCId);
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL, part.href.toString());
                    if (ATTGlobalVariables.isAmbsPhaseIV()) {
                        cv.put("file_name", Util.generateUniqueFileName(part));
                    } else {
                        cv.put("file_name", Util.generateLocation(part));
                    }
                    cv.put("file_size", Long.valueOf(part.size));
                    cv.put("content_type", part.contentType.split(";")[0]);
                } else if (!(part.contentId == null || fileIconCId == null || !fileIconCId.equals(part.contentId))) {
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB, part.href != null ? part.href.toString() : null);
                    if (ATTGlobalVariables.isAmbsPhaseIV()) {
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB_FILENAME, Util.generateUniqueFileName(part));
                    } else {
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB_FILENAME, Util.generateLocation(part));
                    }
                }
            }
            ParamOMAObject paramOMAObject = objt;
        } else {
            Iterator<PayloadPartInfo> it2 = parts.iterator();
            while (it2.hasNext()) {
                PayloadPartInfo part2 = it2.next();
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL, part2.href.toString());
                cv.put("file_name", Util.generateLocation(part2));
                cv.put("file_size", Long.valueOf(part2.size));
                String[] contentTypeList = part2.contentType.split(";");
                cv.put("content_type", contentTypeList[0]);
                if (objt.mObjectType == 14 && contentTypeList[0].trim().equalsIgnoreCase("text/plain")) {
                    Log.d(TAG, "this message should be large message, not fileTransfer");
                    cv.put(ImContract.ChatItem.IS_FILE_TRANSFER, 0);
                }
            }
            ParamOMAObject paramOMAObject2 = objt;
        }
        return cv;
    }

    private ContentValues removeExtensionColumns(ContentValues cv) {
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB_FILENAME);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADENCODING);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.FLAGRESOURCEURL);
        cv.remove("path");
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDERPATH);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION);
        cv.remove(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION);
        cv.remove("linenum");
        return cv;
    }

    public void insertRCSMessageDbfromBufferDB(long rowid, ContentValues cvinsert) {
        Uri messageUri = this.mRCSStorage.insertMessageFromBufferDb(removeExtensionColumns(cvinsert));
        if (messageUri != null) {
            String str = TAG;
            Log.d(str, "insert RCS message into ImProvider result: " + IMSLog.checker(messageUri.toString()));
            String lastsegment = messageUri.getLastPathSegment();
            ContentValues cvupdate = new ContentValues();
            int message_id = Integer.valueOf(lastsegment).intValue();
            if (message_id > 0) {
                cvupdate.put("_id", Integer.valueOf(message_id));
                this.mBufferDB.updateRCSTable(cvupdate, "_bufferdbid=?", new String[]{String.valueOf(rowid)});
            }
        }
    }

    public int deleteRCSMessageDb(int _id) {
        return this.mRCSStorage.deleteRCSDBmessageUsingId(_id);
    }

    public int updateRCSMessageDb(int _id, ContentValues cvupdate) {
        String str = TAG;
        Log.d(str, "updateRCSMessageDb: " + _id);
        ContentValues cv = removeExtensionColumns(cvupdate);
        if (cv.size() > 0) {
            return this.mRCSStorage.updateMessageFromBufferDb(_id, cv);
        }
        return 0;
    }

    public int insertRCSNotificationDbfromBufferDB(ContentValues cvinsert) {
        if (cvinsert == null) {
            Log.d(TAG, "insertRCSNotificationDbfromBufferDB null input");
            return 0;
        }
        Uri messageUri = this.mRCSStorage.insertNotificationFromBufferDb(removeExtensionColumns(cvinsert));
        if (messageUri == null) {
            return 0;
        }
        String str = TAG;
        Log.d(str, "insert RCS notification into ImProvider result: " + IMSLog.checker(messageUri.toString()));
        return Integer.valueOf(messageUri.getLastPathSegment()).intValue();
    }

    public int updateRCSSessionDb(String _id, ContentValues cvupdate) {
        ContentValues cv = removeExtensionColumns(cvupdate);
        if (cv.size() > 0) {
            return this.mRCSStorage.updateSessionFromBufferDbToRCSDb(_id, cv);
        }
        return 0;
    }

    public int updateSessionBufferDb(String _id, ContentValues cvupdate) {
        String str = TAG;
        Log.i(str, "updateSessionBufferDb: " + _id);
        String[] selectionArgssession = {_id};
        if (cvupdate.size() > 0) {
            return this.mBufferDB.updateRCSSessionTable(cvupdate, "chat_id=?", selectionArgssession);
        }
        return 0;
    }

    public Cursor queryRCSBufferDBwithResUrl(String url) {
        String str = TAG;
        Log.d(str, "queryRCSBufferDBwithResUrl: " + IMSLog.checker(url));
        return this.mBufferDB.queryTablewithResUrl(1, url);
    }

    public int deleteRCSBufferDBwithResUrl(String url) {
        String str = TAG;
        Log.d(str, "deleteRCSBufferDBwithResUrl: " + IMSLog.checker(url));
        return this.mBufferDB.deleteTablewithResUrl(1, url);
    }

    public Cursor queryToDeviceUnDownloadedRcs(String linenum) {
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncaction=? AND linenum=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()), linenum}, (String) null);
    }

    public Cursor queryToCloudUnsyncedRcs() {
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncdirection=? AND res_url IS NOT NULL AND inserted_timestamp > ?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(10))}, (String) null);
    }

    public Cursor queryToDeviceUnsyncedRcs() {
        return this.mBufferDB.queryRCSMessages((String[]) null, "syncdirection=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())}, (String) null);
    }

    public Cursor querySessionUsingChatId(String chatId) {
        return this.mRCSStorage.querySessionUsingChatId(chatId);
    }
}
