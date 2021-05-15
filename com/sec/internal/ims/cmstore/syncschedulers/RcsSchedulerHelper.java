package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceSessionPartcptsUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.servicemodules.im.interfaces.FtIntent;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;

public class RcsSchedulerHelper extends BaseMessagingScheduler {
    private static final String TAG = RcsSchedulerHelper.class.getSimpleName();
    protected final RcsQueryBuilder mBufferDbQuery;
    protected final MmsScheduler mMmsScheduler;
    protected final SmsScheduler mSmsScheduler;

    public RcsSchedulerHelper(Context context, CloudMessageBufferDBEventSchedulingRule rule, SummaryQueryBuilder builder, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, MmsScheduler mmsScheduler, SmsScheduler smsScheduler, Looper looper) {
        super(context, rule, deviceDataListener, callback, looper, builder);
        this.mBufferDbQuery = new RcsQueryBuilder(context, callback);
        this.mDbTableContractIndex = 1;
        this.mMmsScheduler = mmsScheduler;
        this.mSmsScheduler = smsScheduler;
    }

    public void onNmsEventChangedObjBufferDbRcsAvailableUsingUrl(Cursor rcsCs, ChangedObject objt, boolean mIsGoforwardSync) {
        onNmsEventChangedObjRCSBufferDbAvailable(rcsCs, objt, mIsGoforwardSync);
    }

    public void onNmsEventChangedObjRCSBufferDbAvailable(Cursor rcsCs, ChangedObject objt, boolean mIsGoforwardSync) {
        Cursor cursor = rcsCs;
        ChangedObject changedObject = objt;
        long bufferDbId = (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long _id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        boolean isFt = cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) == 1;
        String line = cursor.getString(cursor.getColumnIndexOrThrow("linenum"));
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, changedObject.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(changedObject.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(changedObject.parentFolder.toString()));
        CloudMessageBufferDBConstants.ActionStatusFlag cldAction = this.mBufferDbQuery.getCloudActionPerFlag(changedObject.flags);
        if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(cldAction)) {
            cv.put("status", Integer.valueOf(ImConstants.Status.READ.getId()));
            cv.put(ImContract.CsSession.STATUS, Integer.valueOf(ImConstants.Status.READ.getId()));
        }
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, bufferDbId, direction, action, cldAction);
        if (flagSet.mIsChanged) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        updateQueryTable(cv, bufferDbId, this.mBufferDbQuery);
        if (flagSet.mIsChanged) {
            this.mBufferDbQuery.updateRCSMessageDb((int) _id, cv);
        }
        if (_id > 0) {
            long j = _id;
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = cldAction;
            ContentValues contentValues = cv;
            long j2 = bufferDbId;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, 1, isFt, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
            return;
        }
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = cldAction;
        ContentValues contentValues2 = cv;
        long j3 = bufferDbId;
    }

    public void onNmsEventChangedObjRcsBufferDbAvailableUsingImdnId(Cursor cs, ChangedObject objt, boolean mIsGoforwardSync) {
        onNmsEventChangedObjRCSBufferDbAvailable(cs, objt, mIsGoforwardSync);
    }

    public void onNmsEventDeletedObjBufferDbRcsAvailableUsingImdnId(Cursor rcsCs, DeletedObject objt, boolean mIsGoforwardSync) {
        onNmsEventDeletedObjBufferDbRcsAvailable(rcsCs, objt, mIsGoforwardSync);
    }

    public void onNmsEventDeletedObjBufferDbRcsAvailableUsingUrl(Cursor rcsCs, DeletedObject objt, boolean mIsGoforwardSync) {
        onNmsEventDeletedObjBufferDbRcsAvailable(rcsCs, objt, mIsGoforwardSync);
    }

    public void onNmsEventDeletedObjBufferDbRcsAvailable(Cursor rcsCs, DeletedObject objt, boolean mIsGoforwardSync) {
        Cursor cursor = rcsCs;
        long bufferDbId = (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long _id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        boolean isFt = cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) == 1;
        String line = cursor.getString(cursor.getColumnIndexOrThrow("linenum"));
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(objt.lastModSeq));
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, bufferDbId, direction, action, CloudMessageBufferDBConstants.ActionStatusFlag.Delete);
        if (flagSet.mIsChanged) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        updateQueryTable(cv, bufferDbId, this.mBufferDbQuery);
        if (flagSet.mIsChanged) {
            this.mBufferDbQuery.deleteRCSMessageDb((int) _id);
        }
        if (_id > 0) {
            long j = _id;
            ContentValues contentValues = cv;
            long j2 = bufferDbId;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, 1, isFt, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
            return;
        }
        ContentValues contentValues2 = cv;
        long j3 = bufferDbId;
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x01eb A[SYNTHETIC, Splitter:B:77:0x01eb] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0206 A[Catch:{ all -> 0x026f, all -> 0x0277 }] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x0247 A[Catch:{ all -> 0x026f, all -> 0x0277 }] */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x027f  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:71:0x01c3=Splitter:B:71:0x01c3, B:45:0x0138=Splitter:B:45:0x0138} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onRcsPayloadDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r22, boolean r23) {
        /*
            r21 = this;
            r10 = r21
            java.lang.String r1 = "thumbnail_path"
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r10.mBufferDbQuery
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = r22.getBufferDBChangeParam()
            int r2 = r2.mDBIndex
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3 = r22.getBufferDBChangeParam()
            long r3 = r3.mRowId
            android.database.Cursor r11 = r0.queryTablewithBufferDbId(r2, r3)
            if (r11 == 0) goto L_0x027d
            boolean r0 = r11.moveToFirst()     // Catch:{ all -> 0x026f }
            if (r0 == 0) goto L_0x027d
            java.lang.String r0 = "_bufferdbid"
            int r0 = r11.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x026f }
            int r0 = r11.getInt(r0)     // Catch:{ all -> 0x026f }
            r12 = r0
            java.lang.String r0 = "_id"
            int r0 = r11.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x026f }
            int r0 = r11.getInt(r0)     // Catch:{ all -> 0x026f }
            r13 = r0
            java.lang.String r0 = "linenum"
            int r0 = r11.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x026f }
            java.lang.String r8 = r11.getString(r0)     // Catch:{ all -> 0x026f }
            java.lang.String r0 = "payloadencoding"
            int r0 = r11.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x026f }
            int r0 = r11.getInt(r0)     // Catch:{ all -> 0x026f }
            r14 = r0
            java.lang.String r0 = "file_name"
            int r0 = r11.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x026f }
            java.lang.String r0 = r11.getString(r0)     // Catch:{ all -> 0x026f }
            r15 = r0
            int r0 = r11.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x026f }
            java.lang.String r0 = r11.getString(r0)     // Catch:{ all -> 0x026f }
            r16 = r0
            java.lang.String r0 = "is_filetransfer"
            int r0 = r11.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x026f }
            int r0 = r11.getInt(r0)     // Catch:{ all -> 0x026f }
            r2 = 1
            if (r0 != r2) goto L_0x006e
            goto L_0x006f
        L_0x006e:
            r2 = 0
        L_0x006f:
            r9 = r2
            java.lang.String r0 = "content_type"
            int r0 = r11.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x026f }
            java.lang.String r0 = r11.getString(r0)     // Catch:{ all -> 0x026f }
            r7 = r0
            java.lang.String r0 = TAG     // Catch:{ all -> 0x026f }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x026f }
            r2.<init>()     // Catch:{ all -> 0x026f }
            java.lang.String r4 = "isFileTransfer: "
            r2.append(r4)     // Catch:{ all -> 0x026f }
            r2.append(r9)     // Catch:{ all -> 0x026f }
            java.lang.String r4 = ", contentType: "
            r2.append(r4)     // Catch:{ all -> 0x026f }
            r2.append(r7)     // Catch:{ all -> 0x026f }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x026f }
            android.util.Log.i(r0, r2)     // Catch:{ all -> 0x026f }
            r2 = 0
            android.content.ContentValues r0 = new android.content.ContentValues     // Catch:{ all -> 0x026f }
            r0.<init>()     // Catch:{ all -> 0x026f }
            r6 = r0
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = r22.getBufferDBChangeParam()     // Catch:{ all -> 0x026f }
            boolean r0 = r0.mIsFTThumbnail     // Catch:{ all -> 0x026f }
            java.lang.String r4 = " encoding method: "
            if (r0 == 0) goto L_0x0155
            boolean r0 = android.text.TextUtils.isEmpty(r16)     // Catch:{ all -> 0x026f }
            if (r0 == 0) goto L_0x0155
            r5 = 0
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = r22.getBufferDBChangeParam()     // Catch:{ IOException -> 0x0135 }
            java.lang.String r0 = r0.mFTThumbnailFileName     // Catch:{ IOException -> 0x0135 }
            boolean r0 = android.text.TextUtils.isEmpty(r0)     // Catch:{ IOException -> 0x0135 }
            if (r0 != 0) goto L_0x00ce
            android.content.Context r0 = r10.mContext     // Catch:{ IOException -> 0x0135 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3 = r22.getBufferDBChangeParam()     // Catch:{ IOException -> 0x0135 }
            java.lang.String r3 = r3.mFTThumbnailFileName     // Catch:{ IOException -> 0x0135 }
            r18 = r2
            r2 = 0
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r0, r3, r2)     // Catch:{ IOException -> 0x0131 }
            r5 = r0
            goto L_0x00e2
        L_0x00ce:
            r18 = r2
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ IOException -> 0x0131 }
            java.lang.String r0 = r0.getFileExtension(r7)     // Catch:{ IOException -> 0x0131 }
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getRandomFileName(r0)     // Catch:{ IOException -> 0x0131 }
            android.content.Context r2 = r10.mContext     // Catch:{ IOException -> 0x0131 }
            r3 = 0
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r2, r0, r3)     // Catch:{ IOException -> 0x0131 }
            r5 = r2
        L_0x00e2:
            java.lang.String r0 = TAG     // Catch:{ IOException -> 0x0131 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0131 }
            r2.<init>()     // Catch:{ IOException -> 0x0131 }
            java.lang.String r3 = "generated thumbnail file path: "
            r2.append(r3)     // Catch:{ IOException -> 0x0131 }
            r2.append(r5)     // Catch:{ IOException -> 0x0131 }
            r2.append(r4)     // Catch:{ IOException -> 0x0131 }
            r2.append(r14)     // Catch:{ IOException -> 0x0131 }
            java.lang.String r2 = r2.toString()     // Catch:{ IOException -> 0x0131 }
            android.util.Log.i(r0, r2)     // Catch:{ IOException -> 0x0131 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$PayloadEncoding r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.PayloadEncoding.None     // Catch:{ IOException -> 0x0131 }
            int r0 = r0.getId()     // Catch:{ IOException -> 0x0131 }
            if (r0 != r14) goto L_0x0111
            byte[] r0 = r22.getData()     // Catch:{ IOException -> 0x0131 }
            r2 = r0
            com.sec.internal.ims.cmstore.utils.Util.saveFiletoPath(r2, r5)     // Catch:{ IOException -> 0x010f }
            goto L_0x0130
        L_0x010f:
            r0 = move-exception
            goto L_0x0138
        L_0x0111:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$PayloadEncoding r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.PayloadEncoding.Base64     // Catch:{ IOException -> 0x0131 }
            int r0 = r0.getId()     // Catch:{ IOException -> 0x0131 }
            if (r0 != r14) goto L_0x0127
            byte[] r0 = r22.getData()     // Catch:{ IOException -> 0x0131 }
            r2 = 0
            byte[] r0 = android.util.Base64.decode(r0, r2)     // Catch:{ IOException -> 0x0131 }
            r2 = r0
            com.sec.internal.ims.cmstore.utils.Util.saveFiletoPath(r2, r5)     // Catch:{ IOException -> 0x010f }
            goto L_0x0130
        L_0x0127:
            java.lang.String r0 = TAG     // Catch:{ IOException -> 0x0131 }
            java.lang.String r2 = "no decoding defined: "
            android.util.Log.d(r0, r2)     // Catch:{ IOException -> 0x0131 }
            r2 = r18
        L_0x0130:
            goto L_0x013b
        L_0x0131:
            r0 = move-exception
            r2 = r18
            goto L_0x0138
        L_0x0135:
            r0 = move-exception
            r18 = r2
        L_0x0138:
            r0.printStackTrace()     // Catch:{ all -> 0x026f }
        L_0x013b:
            r6.put(r1, r5)     // Catch:{ all -> 0x026f }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = r22.getBufferDBChangeParam()     // Catch:{ all -> 0x026f }
            r1 = 0
            r0.mIsFTThumbnail = r1     // Catch:{ all -> 0x026f }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = r22.getBufferDBChangeParam()     // Catch:{ all -> 0x026f }
            r1 = 0
            r0.mFTThumbnailFileName = r1     // Catch:{ all -> 0x026f }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = r22.getBufferDBChangeParam()     // Catch:{ all -> 0x026f }
            r0.mPayloadThumbnailUrl = r1     // Catch:{ all -> 0x026f }
            r0 = r2
            goto L_0x01df
        L_0x0155:
            r18 = r2
            r1 = 0
            r2 = 0
            android.content.Context r0 = r10.mContext     // Catch:{ IOException -> 0x01c2 }
            r3 = 0
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r0, r15, r3)     // Catch:{ IOException -> 0x01c2 }
            r1 = r0
            java.lang.String r0 = TAG     // Catch:{ IOException -> 0x01c2 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x01c2 }
            r3.<init>()     // Catch:{ IOException -> 0x01c2 }
            java.lang.String r5 = "generated file path: "
            r3.append(r5)     // Catch:{ IOException -> 0x01c2 }
            r3.append(r1)     // Catch:{ IOException -> 0x01c2 }
            r3.append(r4)     // Catch:{ IOException -> 0x01c2 }
            r3.append(r14)     // Catch:{ IOException -> 0x01c2 }
            java.lang.String r3 = r3.toString()     // Catch:{ IOException -> 0x01c2 }
            android.util.Log.i(r0, r3)     // Catch:{ IOException -> 0x01c2 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$PayloadEncoding r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.PayloadEncoding.None     // Catch:{ IOException -> 0x01c2 }
            int r0 = r0.getId()     // Catch:{ IOException -> 0x01c2 }
            if (r0 != r14) goto L_0x019f
            byte[] r0 = r22.getData()     // Catch:{ IOException -> 0x01c2 }
            com.sec.internal.ims.cmstore.utils.Util.saveFiletoPath(r0, r1)     // Catch:{ IOException -> 0x01c2 }
            byte[] r0 = r22.getData()     // Catch:{ IOException -> 0x01c2 }
            if (r0 == 0) goto L_0x0198
            byte[] r0 = r22.getData()     // Catch:{ IOException -> 0x01c2 }
            int r3 = r0.length     // Catch:{ IOException -> 0x01c2 }
            goto L_0x0199
        L_0x0198:
            r3 = 0
        L_0x0199:
            r2 = r3
            byte[] r0 = r22.getData()     // Catch:{ IOException -> 0x01c2 }
            goto L_0x01c1
        L_0x019f:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$PayloadEncoding r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.PayloadEncoding.Base64     // Catch:{ IOException -> 0x01c2 }
            int r0 = r0.getId()     // Catch:{ IOException -> 0x01c2 }
            if (r0 != r14) goto L_0x01bf
            byte[] r0 = r22.getData()     // Catch:{ IOException -> 0x01c2 }
            r3 = 0
            byte[] r0 = android.util.Base64.decode(r0, r3)     // Catch:{ IOException -> 0x01c2 }
            r4 = r0
            if (r4 == 0) goto L_0x01b9
            int r3 = r4.length     // Catch:{ IOException -> 0x01b5 }
            goto L_0x01b9
        L_0x01b5:
            r0 = move-exception
            r18 = r4
            goto L_0x01c3
        L_0x01b9:
            r2 = r3
            com.sec.internal.ims.cmstore.utils.Util.saveFiletoPath(r4, r1)     // Catch:{ IOException -> 0x01b5 }
            r0 = r4
            goto L_0x01c1
        L_0x01bf:
            r0 = r18
        L_0x01c1:
            goto L_0x01c8
        L_0x01c2:
            r0 = move-exception
        L_0x01c3:
            r0.printStackTrace()     // Catch:{ all -> 0x026f }
            r0 = r18
        L_0x01c8:
            java.lang.String r3 = "file_size"
            java.lang.Integer r4 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x026f }
            r6.put(r3, r4)     // Catch:{ all -> 0x026f }
            java.lang.String r3 = "bytes_transf"
            java.lang.Integer r4 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x026f }
            r6.put(r3, r4)     // Catch:{ all -> 0x026f }
            java.lang.String r3 = "file_path"
            r6.put(r3, r1)     // Catch:{ all -> 0x026f }
        L_0x01df:
            boolean r1 = android.text.TextUtils.isEmpty(r16)     // Catch:{ all -> 0x026f }
            java.lang.String r2 = "syncaction"
            java.lang.String r3 = "syncdirection"
            if (r1 == 0) goto L_0x0206
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x026f }
            int r1 = r1.getId()     // Catch:{ all -> 0x026f }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x026f }
            r6.put(r3, r1)     // Catch:{ all -> 0x026f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x026f }
            int r1 = r1.getId()     // Catch:{ all -> 0x026f }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x026f }
            r6.put(r2, r1)     // Catch:{ all -> 0x026f }
            goto L_0x0220
        L_0x0206:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x026f }
            int r1 = r1.getId()     // Catch:{ all -> 0x026f }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x026f }
            r6.put(r3, r1)     // Catch:{ all -> 0x026f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x026f }
            int r1 = r1.getId()     // Catch:{ all -> 0x026f }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x026f }
            r6.put(r2, r1)     // Catch:{ all -> 0x026f }
        L_0x0220:
            if (r7 == 0) goto L_0x023f
            java.lang.String r1 = "application/vnd.gsma.rcspushlocation+xml"
            boolean r1 = r7.equalsIgnoreCase(r1)     // Catch:{ all -> 0x026f }
            if (r1 == 0) goto L_0x023f
            if (r0 == 0) goto L_0x023f
            java.lang.String r1 = "ext_info"
            com.sec.internal.ims.servicemodules.gls.GlsXmlParser r2 = new com.sec.internal.ims.servicemodules.gls.GlsXmlParser     // Catch:{ all -> 0x026f }
            r2.<init>()     // Catch:{ all -> 0x026f }
            java.lang.String r3 = new java.lang.String     // Catch:{ all -> 0x026f }
            r3.<init>(r0)     // Catch:{ all -> 0x026f }
            java.lang.String r2 = r2.getGlsExtInfo(r3)     // Catch:{ all -> 0x026f }
            r6.put(r1, r2)     // Catch:{ all -> 0x026f }
        L_0x023f:
            long r1 = (long) r12     // Catch:{ all -> 0x026f }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r3 = r10.mBufferDbQuery     // Catch:{ all -> 0x026f }
            r10.updateQueryTable(r6, r1, r3)     // Catch:{ all -> 0x026f }
            if (r13 <= 0) goto L_0x024c
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x026f }
            r1.updateRCSMessageDb(r13, r6)     // Catch:{ all -> 0x026f }
        L_0x024c:
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x026f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x026f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x026f }
            r2.<init>(r1, r3)     // Catch:{ all -> 0x026f }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r22.getBufferDBChangeParam()     // Catch:{ all -> 0x026f }
            long r3 = r1.mRowId     // Catch:{ all -> 0x026f }
            r5 = 1
            r17 = 0
            r1 = r21
            r18 = r6
            r6 = r9
            r19 = r7
            r7 = r23
            r20 = r9
            r9 = r17
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x026f }
            goto L_0x027d
        L_0x026f:
            r0 = move-exception
            r1 = r0
            if (r11 == 0) goto L_0x027c
            r11.close()     // Catch:{ all -> 0x0277 }
            goto L_0x027c
        L_0x0277:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x027c:
            throw r1
        L_0x027d:
            if (r11 == 0) goto L_0x0282
            r11.close()
        L_0x0282:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.onRcsPayloadDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB, boolean):void");
    }

    private void setInlineTextCV(String inlineText, ContentValues cv) {
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(ImContract.ChatItem.IS_FILE_TRANSFER, 0);
        cv.put("content_type", "text/plain");
        cv.put("body", inlineText);
    }

    private void getPayloadCV(String filename, String filepath, String thumbnail, long fileSize, ContentValues cv) {
        cv.put("file_name", filename);
        cv.put("file_size", Long.valueOf(fileSize));
        cv.put(ImContract.CsSession.BYTES_TRANSFERED, Long.valueOf(fileSize));
        cv.put("file_path", filepath);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
        if (TextUtils.isEmpty(thumbnail)) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        } else {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 24 */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x034e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x034f, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0352, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x0353, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x0397, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0398, code lost:
        r1 = r0;
        r8 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x03ad, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x03ae, code lost:
        r10 = r19;
        r1 = r0;
        r8 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x03b6, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x03b7, code lost:
        r6 = r19;
        r1 = r0;
        r8 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x03bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x03c0, code lost:
        r6 = r19;
        r1 = r0;
        r8 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x03c8, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x03c9, code lost:
        r1 = r0;
        r2 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x03cf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x03d0, code lost:
        r20 = r8;
        r6 = r19;
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x0478, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:?, code lost:
        r1.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x0489, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x048a, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:0x04a4, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x04a5, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00fe, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ff, code lost:
        r1 = r0;
        r10 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0104, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0105, code lost:
        r1 = r0;
        r10 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x010c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x010d, code lost:
        r1 = r0;
        r10 = r19;
        r8 = r20;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x03c8 A[ExcHandler: all (r0v13 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:10:0x0069] */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x03e5 A[SYNTHETIC, Splitter:B:140:0x03e5] */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x03f3 A[SYNTHETIC, Splitter:B:145:0x03f3] */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x042b A[SYNTHETIC, Splitter:B:163:0x042b] */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x0474 A[SYNTHETIC, Splitter:B:180:0x0474] */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0485 A[Catch:{ all -> 0x0478, all -> 0x0489 }] */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x048e A[SYNTHETIC, Splitter:B:193:0x048e] */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x04a0 A[SYNTHETIC, Splitter:B:200:0x04a0] */
    /* JADX WARNING: Removed duplicated region for block: B:216:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x02dd A[Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02fc A[Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0315 A[Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0330 A[SYNTHETIC, Splitter:B:99:0x0330] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onRcsAllPayloadsDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r25, boolean r26) {
        /*
            r24 = this;
            r14 = r24
            java.lang.String r1 = ""
            java.lang.String r2 = ";"
            java.lang.String r3 = "onRcsAllPayloadsDownloaded: "
            java.lang.String r5 = "content_type"
            r6 = 0
            r7 = 0
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r14.mBufferDbQuery     // Catch:{ all -> 0x049a }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r8 = r25.getBufferDBChangeParam()     // Catch:{ all -> 0x049a }
            int r8 = r8.mDBIndex     // Catch:{ all -> 0x049a }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r9 = r25.getBufferDBChangeParam()     // Catch:{ all -> 0x049a }
            long r9 = r9.mRowId     // Catch:{ all -> 0x049a }
            android.database.Cursor r4 = r4.queryTablewithBufferDbId(r8, r9)     // Catch:{ all -> 0x049a }
            r13 = r4
            if (r13 == 0) goto L_0x047e
            boolean r4 = r13.moveToFirst()     // Catch:{ all -> 0x046d }
            if (r4 == 0) goto L_0x047e
            java.lang.String r4 = "_id"
            int r4 = r13.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x046d }
            int r4 = r13.getInt(r4)     // Catch:{ all -> 0x046d }
            r12 = r4
            java.lang.String r4 = "linenum"
            int r4 = r13.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x046d }
            java.lang.String r15 = r13.getString(r4)     // Catch:{ all -> 0x046d }
            java.lang.String r4 = "file_name"
            int r4 = r13.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x046d }
            java.lang.String r4 = r13.getString(r4)     // Catch:{ all -> 0x046d }
            r8 = r4
            java.lang.String r4 = "thumbnail_path"
            int r4 = r13.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x046d }
            java.lang.String r4 = r13.getString(r4)     // Catch:{ all -> 0x046d }
            int r9 = r13.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x046d }
            java.lang.String r9 = r13.getString(r9)     // Catch:{ all -> 0x046d }
            r10 = r9
            r9 = 0
            r11 = r1
            r16 = 0
            android.content.ContentValues r18 = new android.content.ContentValues     // Catch:{ all -> 0x046d }
            r18.<init>()     // Catch:{ all -> 0x046d }
            r19 = r18
            r18 = r6
            java.lang.String r6 = TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03cf, all -> 0x03c8 }
            r20 = r8
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03bf, all -> 0x03c8 }
            r8.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03bf, all -> 0x03c8 }
            r21 = r9
            java.lang.String r9 = "multipart payloads, size: "
            r8.append(r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03b6, all -> 0x03c8 }
            java.util.List r9 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03b6, all -> 0x03c8 }
            int r9 = r9.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03b6, all -> 0x03c8 }
            r8.append(r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03b6, all -> 0x03c8 }
            java.lang.String r8 = r8.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03b6, all -> 0x03c8 }
            android.util.Log.i(r6, r8)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03b6, all -> 0x03c8 }
            java.util.List r6 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03b6, all -> 0x03c8 }
            int r6 = r6.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03b6, all -> 0x03c8 }
            r8 = 1
            if (r6 <= r8) goto L_0x0128
            java.lang.String r2 = "multipart/related"
            r6 = r19
            r6.put(r5, r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            javax.mail.internet.MimeMultipart r2 = new javax.mail.internet.MimeMultipart     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            r2.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            r3 = 0
        L_0x00a3:
            java.util.List r5 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            int r5 = r5.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            if (r3 >= r5) goto L_0x00d2
            java.util.List r5 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            java.lang.Object r5 = r5.get(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            javax.mail.BodyPart r5 = (javax.mail.BodyPart) r5     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            r2.addBodyPart(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            java.util.List r5 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            java.lang.Object r5 = r5.get(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            javax.mail.BodyPart r5 = (javax.mail.BodyPart) r5     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            int r5 = r5.getSize()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0116, all -> 0x03c8 }
            r19 = r10
            long r9 = (long) r5
            long r16 = r16 + r9
            int r3 = r3 + 1
            r10 = r19
            goto L_0x00a3
        L_0x00d2:
            r19 = r10
            java.lang.String r1 = com.sec.internal.ims.cmstore.utils.Util.getRandomFileName(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x010c, all -> 0x03c8 }
            r8 = r1
            android.content.Context r1 = r14.mContext     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0104, all -> 0x03c8 }
            r3 = 0
            java.lang.String r1 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r1, r8, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0104, all -> 0x03c8 }
            r9 = r1
            java.lang.String r1 = TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00fe, all -> 0x03c8 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00fe, all -> 0x03c8 }
            r3.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00fe, all -> 0x03c8 }
            java.lang.String r5 = "generated file path: "
            r3.append(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00fe, all -> 0x03c8 }
            r3.append(r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00fe, all -> 0x03c8 }
            java.lang.String r3 = r3.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00fe, all -> 0x03c8 }
            android.util.Log.i(r1, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00fe, all -> 0x03c8 }
            com.sec.internal.ims.cmstore.utils.Util.saveMimeBodyToPath(r2, r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00fe, all -> 0x03c8 }
            r10 = r19
            goto L_0x03a4
        L_0x00fe:
            r0 = move-exception
            r1 = r0
            r10 = r19
            goto L_0x03d7
        L_0x0104:
            r0 = move-exception
            r1 = r0
            r10 = r19
            r9 = r21
            goto L_0x03d7
        L_0x010c:
            r0 = move-exception
            r1 = r0
            r10 = r19
            r8 = r20
            r9 = r21
            goto L_0x03d7
        L_0x0116:
            r0 = move-exception
            r1 = r0
            r8 = r20
            r9 = r21
            goto L_0x03d7
        L_0x011e:
            r0 = move-exception
            r6 = r19
            r1 = r0
            r8 = r20
            r9 = r21
            goto L_0x03d7
        L_0x0128:
            r6 = r19
            r19 = r10
            java.util.List r1 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            int r1 = r1.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            if (r1 != r8) goto L_0x039e
            r1 = 0
            java.lang.String r8 = TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            r9.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            r9.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.util.List r10 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.lang.Object r10 = r10.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            javax.mail.BodyPart r10 = (javax.mail.BodyPart) r10     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.lang.String r10 = r10.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            r9.append(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.lang.String r9 = r9.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            android.util.Log.i(r8, r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.util.List r8 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.lang.Object r8 = r8.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            javax.mail.BodyPart r8 = (javax.mail.BodyPart) r8     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.lang.String r8 = r8.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.lang.String r9 = "text/plain"
            boolean r8 = r8.contains(r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            if (r8 == 0) goto L_0x01b7
            r2 = 1
            java.util.List r3 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            java.lang.Object r3 = r3.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            java.io.InputStream r3 = r3.getInputStream()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            r7 = r3
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.convertStreamToString(r7)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            r11 = r3
            java.lang.String r3 = TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            r5.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            java.lang.String r8 = "converted inlineTxt: "
            r5.append(r8)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            r5.append(r11)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            java.lang.String r5 = r5.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            android.util.Log.i(r3, r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01ab, all -> 0x01a5 }
            r18 = r2
            r10 = r19
            r8 = r20
            r9 = r21
            goto L_0x03a4
        L_0x01a5:
            r0 = move-exception
            r1 = r0
            r6 = r2
            r2 = r13
            goto L_0x0472
        L_0x01ab:
            r0 = move-exception
            r1 = r0
            r18 = r2
            r10 = r19
            r8 = r20
            r9 = r21
            goto L_0x03d7
        L_0x01b7:
            boolean r8 = android.text.TextUtils.isEmpty(r19)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            if (r8 != 0) goto L_0x01f7
            java.util.List r8 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.lang.Object r8 = r8.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            javax.mail.BodyPart r8 = (javax.mail.BodyPart) r8     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            java.lang.String r8 = r8.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x03ad, all -> 0x03c8 }
            r10 = r19
            boolean r8 = r8.contains(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            if (r8 == 0) goto L_0x01f9
            java.lang.String r2 = TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r5.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r5.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.util.List r3 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r3 = r3.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r3 = r3.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r5.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r3 = r5.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            android.util.Log.d(r2, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            goto L_0x02c8
        L_0x01f7:
            r10 = r19
        L_0x01f9:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r8 = r14.mBufferDbQuery     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.util.List r9 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r9 = r9.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r9 = (javax.mail.BodyPart) r9     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r9 = r9.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            boolean r8 = r8.isContentTypeDefined(r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            if (r8 == 0) goto L_0x0244
            java.lang.String r2 = TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r8.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r8.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.util.List r3 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r3 = r3.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r3 = r3.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r8.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r3 = r8.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            android.util.Log.d(r2, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.util.List r2 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r2 = r2.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r2 = (javax.mail.BodyPart) r2     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r2 = r2.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r6.put(r5, r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            goto L_0x02c8
        L_0x0244:
            java.util.List r8 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r8 = r8.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r8 = (javax.mail.BodyPart) r8     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r8 = r8.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            if (r8 == 0) goto L_0x0358
            java.util.List r8 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r8 = r8.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r8 = (javax.mail.BodyPart) r8     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r8 = r8.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String[] r8 = r8.split(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            if (r8 == 0) goto L_0x0358
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r8 = r14.mBufferDbQuery     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.util.List r9 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r9 = r9.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r9 = (javax.mail.BodyPart) r9     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r9 = r9.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String[] r9 = r9.split(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r19 = 0
            r9 = r9[r19]     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            boolean r8 = r8.isContentTypeDefined(r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            if (r8 == 0) goto L_0x0358
            java.lang.String r8 = TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r9.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r9.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.util.List r3 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r3 = r3.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r3 = r3.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String[] r3 = r3.split(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r19 = 0
            r3 = r3[r19]     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r9.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r3 = r9.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            android.util.Log.d(r8, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.util.List r3 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r3 = r3.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r3 = r3.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String[] r2 = r3.split(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r3 = 0
            r2 = r2[r3]     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r6.put(r5, r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
        L_0x02c8:
            java.util.List r2 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r2 = r2.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r2 = (javax.mail.BodyPart) r2     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r2 = r2.getFileName()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r8 = r2
            boolean r2 = android.text.TextUtils.isEmpty(r8)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            if (r2 == 0) goto L_0x02f6
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r14.mBufferDbQuery     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            java.util.List r3 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            java.lang.Object r3 = r3.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            java.lang.String r3 = r3.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            java.lang.String r2 = r2.getFileExtension(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getRandomFileName(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            r8 = r2
        L_0x02f6:
            boolean r2 = android.text.TextUtils.isEmpty(r8)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            if (r2 == 0) goto L_0x030f
            java.util.List r2 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            java.lang.Object r2 = r2.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            javax.mail.BodyPart r2 = (javax.mail.BodyPart) r2     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            java.lang.String r2 = r2.getDisposition()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getFileNamefromContentType(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            r8 = r2
        L_0x030f:
            boolean r2 = android.text.TextUtils.isEmpty(r8)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            if (r2 == 0) goto L_0x0330
            java.lang.String r2 = TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            java.lang.String r3 = "onRcsAllPayloadsDownloaded: no file name"
            android.util.Log.e(r2, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            if (r13 == 0) goto L_0x0322
            r13.close()     // Catch:{ all -> 0x0383 }
        L_0x0322:
            if (r7 == 0) goto L_0x032e
            r7.close()     // Catch:{ IOException -> 0x0328 }
            goto L_0x032e
        L_0x0328:
            r0 = move-exception
            r2 = r0
            r2.printStackTrace()
            goto L_0x032f
        L_0x032e:
        L_0x032f:
            return
        L_0x0330:
            android.content.Context r2 = r14.mContext     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            r3 = 0
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r2, r8, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0352, all -> 0x03c8 }
            r9 = r2
            java.util.List r2 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x034e, all -> 0x03c8 }
            java.lang.Object r2 = r2.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x034e, all -> 0x03c8 }
            javax.mail.BodyPart r2 = (javax.mail.BodyPart) r2     // Catch:{ IOException | NullPointerException | MessagingException -> 0x034e, all -> 0x03c8 }
            java.io.InputStream r2 = r2.getInputStream()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x034e, all -> 0x03c8 }
            r7 = r2
            long r2 = com.sec.internal.ims.cmstore.utils.Util.saveInputStreamtoPath(r7, r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x034e, all -> 0x03c8 }
            r16 = r2
            goto L_0x03a4
        L_0x034e:
            r0 = move-exception
            r1 = r0
            goto L_0x03d7
        L_0x0352:
            r0 = move-exception
            r1 = r0
            r9 = r21
            goto L_0x03d7
        L_0x0358:
            java.lang.String r2 = TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r3.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r5 = "onRcsAllPayloadsDownloaded invalid file type for RCS: "
            r3.append(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.util.List r5 = r25.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.Object r5 = r5.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            javax.mail.BodyPart r5 = (javax.mail.BodyPart) r5     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r5 = r5.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            r3.append(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            java.lang.String r3 = r3.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            android.util.Log.d(r2, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0397, all -> 0x03c8 }
            if (r13 == 0) goto L_0x0389
            r13.close()     // Catch:{ all -> 0x0383 }
            goto L_0x0389
        L_0x0383:
            r0 = move-exception
            r1 = r0
            r6 = r18
            goto L_0x049e
        L_0x0389:
            if (r7 == 0) goto L_0x0395
            r7.close()     // Catch:{ IOException -> 0x038f }
            goto L_0x0395
        L_0x038f:
            r0 = move-exception
            r2 = r0
            r2.printStackTrace()
            goto L_0x0396
        L_0x0395:
        L_0x0396:
            return
        L_0x0397:
            r0 = move-exception
            r1 = r0
            r8 = r20
            r9 = r21
            goto L_0x03d7
        L_0x039e:
            r10 = r19
            r8 = r20
            r9 = r21
        L_0x03a4:
            r19 = r8
            r20 = r9
            r21 = r16
            r17 = r7
            goto L_0x03e3
        L_0x03ad:
            r0 = move-exception
            r10 = r19
            r1 = r0
            r8 = r20
            r9 = r21
            goto L_0x03d7
        L_0x03b6:
            r0 = move-exception
            r6 = r19
            r1 = r0
            r8 = r20
            r9 = r21
            goto L_0x03d7
        L_0x03bf:
            r0 = move-exception
            r21 = r9
            r6 = r19
            r1 = r0
            r8 = r20
            goto L_0x03d7
        L_0x03c8:
            r0 = move-exception
            r1 = r0
            r2 = r13
            r6 = r18
            goto L_0x0472
        L_0x03cf:
            r0 = move-exception
            r20 = r8
            r21 = r9
            r6 = r19
            r1 = r0
        L_0x03d7:
            r9 = 0
            r1.printStackTrace()     // Catch:{ all -> 0x0467 }
            r19 = r8
            r20 = r9
            r21 = r16
            r17 = r7
        L_0x03e3:
            if (r18 == 0) goto L_0x03f3
            r14.setInlineTextCV(r11, r6)     // Catch:{ all -> 0x03ea }
            r8 = r6
            goto L_0x0422
        L_0x03ea:
            r0 = move-exception
            r1 = r0
            r2 = r13
            r7 = r17
            r6 = r18
            goto L_0x0472
        L_0x03f3:
            boolean r1 = android.text.TextUtils.isEmpty(r20)     // Catch:{ all -> 0x045f }
            if (r1 == 0) goto L_0x0415
            if (r13 == 0) goto L_0x0407
            r13.close()     // Catch:{ all -> 0x03ff }
            goto L_0x0407
        L_0x03ff:
            r0 = move-exception
            r1 = r0
            r7 = r17
            r6 = r18
            goto L_0x049e
        L_0x0407:
            if (r17 == 0) goto L_0x0413
            r17.close()     // Catch:{ IOException -> 0x040d }
            goto L_0x0413
        L_0x040d:
            r0 = move-exception
            r1 = r0
            r1.printStackTrace()
            goto L_0x0414
        L_0x0413:
        L_0x0414:
            return
        L_0x0415:
            r1 = r24
            r2 = r19
            r3 = r20
            r8 = r6
            r5 = r21
            r7 = r8
            r1.getPayloadCV(r2, r3, r4, r5, r7)     // Catch:{ all -> 0x045f }
        L_0x0422:
            r1 = 1
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r3 = r14.mBufferDbQuery     // Catch:{ all -> 0x045f }
            r14.updateQueryTable(r8, r1, r3)     // Catch:{ all -> 0x045f }
            if (r12 <= 0) goto L_0x0430
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r14.mBufferDbQuery     // Catch:{ all -> 0x03ea }
            r1.updateRCSMessageDb(r12, r8)     // Catch:{ all -> 0x03ea }
        L_0x0430:
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r9 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x045f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x045f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x045f }
            r9.<init>(r1, r2)     // Catch:{ all -> 0x045f }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r25.getBufferDBChangeParam()     // Catch:{ all -> 0x045f }
            long r1 = r1.mRowId     // Catch:{ all -> 0x045f }
            r3 = 1
            r5 = 0
            r16 = 0
            r6 = r8
            r8 = r24
            r7 = r10
            r23 = r11
            r10 = r1
            r1 = r12
            r12 = r3
            r2 = r13
            r13 = r5
            r14 = r26
            r8.handleOutPutParamSyncFlagSet(r9, r10, r12, r13, r14, r15, r16)     // Catch:{ all -> 0x0458 }
            r7 = r17
            r6 = r18
            goto L_0x0483
        L_0x0458:
            r0 = move-exception
            r1 = r0
            r7 = r17
            r6 = r18
            goto L_0x0472
        L_0x045f:
            r0 = move-exception
            r2 = r13
            r1 = r0
            r7 = r17
            r6 = r18
            goto L_0x0472
        L_0x0467:
            r0 = move-exception
            r2 = r13
            r1 = r0
            r6 = r18
            goto L_0x0472
        L_0x046d:
            r0 = move-exception
            r18 = r6
            r2 = r13
            r1 = r0
        L_0x0472:
            if (r2 == 0) goto L_0x047d
            r2.close()     // Catch:{ all -> 0x0478 }
            goto L_0x047d
        L_0x0478:
            r0 = move-exception
            r3 = r0
            r1.addSuppressed(r3)     // Catch:{ all -> 0x0489 }
        L_0x047d:
            throw r1     // Catch:{ all -> 0x0489 }
        L_0x047e:
            r18 = r6
            r2 = r13
            r6 = r18
        L_0x0483:
            if (r2 == 0) goto L_0x048c
            r2.close()     // Catch:{ all -> 0x0489 }
            goto L_0x048c
        L_0x0489:
            r0 = move-exception
            r1 = r0
            goto L_0x049e
        L_0x048c:
            if (r7 == 0) goto L_0x0498
            r7.close()     // Catch:{ IOException -> 0x0492 }
            goto L_0x0498
        L_0x0492:
            r0 = move-exception
            r1 = r0
            r1.printStackTrace()
            goto L_0x0499
        L_0x0498:
        L_0x0499:
            return
        L_0x049a:
            r0 = move-exception
            r18 = r6
            r1 = r0
        L_0x049e:
            if (r7 == 0) goto L_0x04aa
            r7.close()     // Catch:{ IOException -> 0x04a4 }
            goto L_0x04aa
        L_0x04a4:
            r0 = move-exception
            r2 = r0
            r2.printStackTrace()
            goto L_0x04ab
        L_0x04aa:
        L_0x04ab:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.onRcsAllPayloadsDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB, boolean):void");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00f1 A[Catch:{ all -> 0x014d, all -> 0x0155 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00fe A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDownloadRequestFromApp(com.sec.internal.ims.cmstore.params.DeviceIMFTUpdateParam r22) {
        /*
            r21 = this;
            r1 = r21
            r2 = r22
            java.lang.String r0 = "syncdirection"
            java.lang.String r3 = "syncaction"
            java.lang.String r4 = TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "onDownloadRequestFromApp: "
            r5.append(r6)
            r5.append(r2)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            int r4 = r2.mTableindex
            r5 = 1
            if (r4 != r5) goto L_0x0160
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r1.mBufferDbQuery
            long r6 = r2.mRowId
            java.lang.String r6 = java.lang.String.valueOf(r6)
            android.database.Cursor r4 = r4.searchIMFTBufferUsingRowId(r6)
            if (r4 == 0) goto L_0x015b
            boolean r6 = r4.moveToFirst()     // Catch:{ all -> 0x014d }
            if (r6 == 0) goto L_0x015b
            java.lang.String r6 = "is_filetransfer"
            int r6 = r4.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x014d }
            int r6 = r4.getInt(r6)     // Catch:{ all -> 0x014d }
            r7 = 0
            if (r6 != r5) goto L_0x0048
            r6 = r5
            goto L_0x0049
        L_0x0048:
            r6 = r7
        L_0x0049:
            java.lang.String r8 = "file_path"
            int r8 = r4.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x014d }
            java.lang.String r8 = r4.getString(r8)     // Catch:{ all -> 0x014d }
            java.lang.String r9 = "file_name"
            int r9 = r4.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x014d }
            java.lang.String r9 = r4.getString(r9)     // Catch:{ all -> 0x014d }
            java.lang.String r10 = "_bufferdbid"
            int r10 = r4.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x014d }
            long r10 = r4.getLong(r10)     // Catch:{ all -> 0x014d }
            r12 = 0
            boolean r13 = android.text.TextUtils.isEmpty(r8)     // Catch:{ all -> 0x014d }
            if (r13 != 0) goto L_0x008c
            java.io.File r13 = new java.io.File     // Catch:{ all -> 0x014d }
            r13.<init>(r8)     // Catch:{ all -> 0x014d }
            boolean r14 = r13.exists()     // Catch:{ all -> 0x014d }
            if (r14 == 0) goto L_0x008c
            boolean r14 = r8.endsWith(r9)     // Catch:{ all -> 0x014d }
            if (r14 == 0) goto L_0x008c
            long r14 = r13.length()     // Catch:{ all -> 0x014d }
            r16 = 0
            int r14 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1))
            if (r14 <= 0) goto L_0x008a
            r7 = r5
        L_0x008a:
            r12 = r7
            goto L_0x008d
        L_0x008c:
            r7 = r12
        L_0x008d:
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r12 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ all -> 0x014d }
            boolean r12 = r12.isSupportAtt72HoursRule()     // Catch:{ all -> 0x014d }
            if (r12 == 0) goto L_0x00c6
            if (r6 == 0) goto L_0x00c6
            if (r7 == 0) goto L_0x00c6
            java.lang.String r0 = TAG     // Catch:{ all -> 0x014d }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x014d }
            r3.<init>()     // Catch:{ all -> 0x014d }
            java.lang.String r12 = "file already exist, notify message app directly: "
            r3.append(r12)     // Catch:{ all -> 0x014d }
            r3.append(r8)     // Catch:{ all -> 0x014d }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x014d }
            android.util.Log.i(r0, r3)     // Catch:{ all -> 0x014d }
            int r0 = r2.mTableindex     // Catch:{ all -> 0x014d }
            java.lang.String r0 = r1.getAppTypeString(r0)     // Catch:{ all -> 0x014d }
            int r3 = r2.mTableindex     // Catch:{ all -> 0x014d }
            java.lang.String r3 = r1.getMessageTypeString(r3, r5)     // Catch:{ all -> 0x014d }
            r1.notifyMsgAppCldNotification(r0, r3, r10)     // Catch:{ all -> 0x014d }
            if (r4 == 0) goto L_0x00c5
            r4.close()
        L_0x00c5:
            return
        L_0x00c6:
            int r5 = r4.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x014d }
            int r5 = r4.getInt(r5)     // Catch:{ all -> 0x014d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r5)     // Catch:{ all -> 0x014d }
            int r12 = r4.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x014d }
            int r12 = r4.getInt(r12)     // Catch:{ all -> 0x014d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r12 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r12)     // Catch:{ all -> 0x014d }
            r14 = r12
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r12 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x014d }
            boolean r12 = r12.equals(r5)     // Catch:{ all -> 0x014d }
            if (r12 != 0) goto L_0x00fe
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r12 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x014d }
            boolean r12 = r12.equals(r14)     // Catch:{ all -> 0x014d }
            if (r12 != 0) goto L_0x00fe
            java.lang.String r0 = TAG     // Catch:{ all -> 0x014d }
            java.lang.String r3 = "duplicate download request!"
            android.util.Log.i(r0, r3)     // Catch:{ all -> 0x014d }
            if (r4 == 0) goto L_0x00fd
            r4.close()
        L_0x00fd:
            return
        L_0x00fe:
            android.content.ContentValues r12 = new android.content.ContentValues     // Catch:{ all -> 0x014d }
            r12.<init>()     // Catch:{ all -> 0x014d }
            r15 = r12
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r12 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Downloading     // Catch:{ all -> 0x014d }
            int r12 = r12.getId()     // Catch:{ all -> 0x014d }
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)     // Catch:{ all -> 0x014d }
            r15.put(r0, r12)     // Catch:{ all -> 0x014d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad     // Catch:{ all -> 0x014d }
            int r0 = r0.getId()     // Catch:{ all -> 0x014d }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x014d }
            r15.put(r3, r0)     // Catch:{ all -> 0x014d }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery     // Catch:{ all -> 0x014d }
            r1.updateQueryTable(r15, r10, r0)     // Catch:{ all -> 0x014d }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r0 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList     // Catch:{ all -> 0x014d }
            r0.<init>()     // Catch:{ all -> 0x014d }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r3 = r0.mChangelst     // Catch:{ all -> 0x014d }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r13 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x014d }
            int r12 = r2.mTableindex     // Catch:{ all -> 0x014d }
            r16 = 0
            r18 = r5
            java.lang.String r5 = r2.mLine     // Catch:{ all -> 0x014d }
            r17 = r12
            r12 = r13
            r2 = r13
            r13 = r17
            r19 = r14
            r20 = r15
            r14 = r10
            r17 = r5
            r12.<init>(r13, r14, r16, r17)     // Catch:{ all -> 0x014d }
            r3.add(r2)     // Catch:{ all -> 0x014d }
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r2 = r1.mDeviceDataChangeListener     // Catch:{ all -> 0x014d }
            r2.sendDeviceNormalSyncDownload(r0)     // Catch:{ all -> 0x014d }
            goto L_0x015b
        L_0x014d:
            r0 = move-exception
            r2 = r0
            if (r4 == 0) goto L_0x015a
            r4.close()     // Catch:{ all -> 0x0155 }
            goto L_0x015a
        L_0x0155:
            r0 = move-exception
            r3 = r0
            r2.addSuppressed(r3)
        L_0x015a:
            throw r2
        L_0x015b:
            if (r4 == 0) goto L_0x0160
            r4.close()
        L_0x0160:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.onDownloadRequestFromApp(com.sec.internal.ims.cmstore.params.DeviceIMFTUpdateParam):void");
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag;

        static {
            int[] iArr = new int[CloudMessageBufferDBConstants.ActionStatusFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag = iArr;
            try {
                iArr[CloudMessageBufferDBConstants.ActionStatusFlag.Insert.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[CloudMessageBufferDBConstants.ActionStatusFlag.Delete.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[CloudMessageBufferDBConstants.ActionStatusFlag.Update.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public void onUpdateFromDeviceSessionPartcpts(DeviceSessionPartcptsUpdateParam para) {
        String str = TAG;
        Log.i(str, "onUpdateFromDeviceSessionPartcpts: " + para);
        if (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[para.mUpdateType.ordinal()] == 1) {
            onNewPartcptsInserted(para);
        }
    }

    private void onNewPartcptsInserted(DeviceSessionPartcptsUpdateParam para) {
        Cursor cs = this.mBufferDbQuery.queryParticipantsUsingChatId(para.mChatId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    this.mBufferDbQuery.insertToRCSParticipantsBufferDB(cs);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
            return;
        }
        return;
        throw th;
    }

    public void onUpdateFromDeviceSession(DeviceSessionPartcptsUpdateParam para) {
        String str = TAG;
        Log.i(str, "onUpdateFromDeviceSession: " + para);
        if (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[para.mUpdateType.ordinal()] == 1) {
            onNewSessionInserted(para);
        }
    }

    private void onNewSessionInserted(DeviceSessionPartcptsUpdateParam para) {
        Cursor cs = this.mBufferDbQuery.querySessionUsingChatId(para.mChatId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    this.mBufferDbQuery.insertSingleSessionToRcsBuffer(cs);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
            return;
        }
        return;
        throw th;
    }

    public void notifyMsgAppFetchBuffer(Cursor cs, int type) {
        if (type == 1) {
            JsonArray jsonArrayRowIdsCHAT = new JsonArray();
            JsonArray jsonArrayRowIdsFT = new JsonArray();
            do {
                int bufferDBid = cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                String filepath = cs.getString(cs.getColumnIndexOrThrow("file_path"));
                String filepaththumbnail = cs.getString(cs.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH));
                if ((filepath == null || filepath.length() <= 1) && (filepaththumbnail == null || filepaththumbnail.length() <= 1)) {
                    JsonObject jsobjct = new JsonObject();
                    jsobjct.addProperty("id", String.valueOf(bufferDBid));
                    jsonArrayRowIdsCHAT.add(jsobjct);
                } else {
                    JsonObject jsobjct2 = new JsonObject();
                    jsobjct2.addProperty("id", String.valueOf(bufferDBid));
                    jsonArrayRowIdsFT.add(jsobjct2);
                }
                String str = TAG;
                Log.d(str, "jsonArrayRowIdsCHAT.size(): " + jsonArrayRowIdsCHAT.size() + ",notify message app: CHAT: " + jsonArrayRowIdsCHAT.toString() + ", jsonArrayRowIdsFT.size(): " + jsonArrayRowIdsFT.size() + "notify message app: FT: " + jsonArrayRowIdsFT.toString());
                if (jsonArrayRowIdsCHAT.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                    this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.CHAT, jsonArrayRowIdsCHAT.toString());
                    jsonArrayRowIdsCHAT = new JsonArray();
                }
                if (jsonArrayRowIdsFT.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                    this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FT", jsonArrayRowIdsFT.toString());
                    jsonArrayRowIdsFT = new JsonArray();
                }
            } while (cs.moveToNext() != 0);
            if (jsonArrayRowIdsCHAT.size() > 0) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.CHAT, jsonArrayRowIdsCHAT.toString());
            }
            if (jsonArrayRowIdsFT.size() > 0) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FT", jsonArrayRowIdsFT.toString());
            }
        }
    }

    public void notifyMsgAppFetchBuffer(ContentValues cv, int type) {
        if (type == 10) {
            String chatId = cv.getAsString("chat_id");
            JsonArray jsonArrayRowIdsSession = new JsonArray();
            JsonObject jsobjct = new JsonObject();
            jsobjct.addProperty(FtIntent.Extras.EXTRA_CHAT_ID, chatId);
            jsonArrayRowIdsSession.add(jsobjct);
            String str = TAG;
            Log.i(str, "notifyMsgAppFetchBuffer, chatId : " + chatId + ", jsonArrayRowIdsSession: " + jsonArrayRowIdsSession.toString());
            if (jsonArrayRowIdsSession.size() > 0) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.SESSION, jsonArrayRowIdsSession.toString());
            }
        }
    }

    public Cursor searchIMFTBufferUsingImdn(String imdnId, String line) {
        return this.mBufferDbQuery.searchIMFTBufferUsingImdn(imdnId, line);
    }

    public Cursor queryToDeviceUnDownloadedRcs(String linenum) {
        return this.mBufferDbQuery.queryToDeviceUnDownloadedRcs(linenum);
    }

    public Cursor queryToCloudUnsyncedRcs() {
        return this.mBufferDbQuery.queryToCloudUnsyncedRcs();
    }

    public Cursor queryToDeviceUnsyncedRcs() {
        return this.mBufferDbQuery.queryToDeviceUnsyncedRcs();
    }

    public Cursor queryRCSMessagesToUpload() {
        return this.mBufferDbQuery.queryRCSMessagesToUpload();
    }

    public Cursor queryImdnMessagesToUpload() {
        return this.mBufferDbQuery.queryImdnMessagesToUpload();
    }

    public Cursor queryRCSBufferDBwithResUrl(String url) {
        return this.mBufferDbQuery.queryRCSBufferDBwithResUrl(url);
    }

    public int deleteRCSBufferDBwithResUrl(String url) {
        return this.mBufferDbQuery.deleteRCSBufferDBwithResUrl(url);
    }

    public Cursor queryRCSMessagesBySycnDirection(int tableIndex, String syncDirection) {
        return this.mBufferDbQuery.queryMessageBySyncDirection(tableIndex, syncDirection);
    }

    public Cursor queryAllSession() {
        return this.mBufferDbQuery.queryAllSession();
    }

    public void insertAllSessionToRCSSessionBufferDB(Cursor cursor) {
        this.mBufferDbQuery.insertAllToRCSSessionBufferDB(cursor);
    }

    public void cleanAllBufferDB() {
        this.mBufferDbQuery.cleanAllBufferDB();
    }

    public void onUpdateFromDeviceMsgAppFetch(DeviceMsgAppFetchUpdateParam para, boolean mIsGoforwardSync) {
        onUpdateFromDeviceMsgAppFetch(para, mIsGoforwardSync, this.mBufferDbQuery);
    }

    public void onUpdateFromDeviceMsgAppFetchFailed(DeviceMsgAppFetchUpdateParam para) {
        this.mBufferDbQuery.updateAppFetchingFailed(para.mTableindex, para.mBufferRowId);
    }

    public void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync) {
        onCloudUpdateFlagSuccess(para, mIsGoforwardSync, this.mBufferDbQuery);
    }

    public void onCloudUploadSuccess(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync) {
        if (para.getReference() != null) {
            handleCloudUploadSuccess(para, mIsGoforwardSync, this.mBufferDbQuery, 1);
        }
    }

    /* access modifiers changed from: protected */
    public void onNmsEventChangedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, ChangedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventChangedObjSummaryDbAvailableUsingUrl(), type: " + type);
        if (type == 3) {
            Cursor sms = this.mSmsScheduler.querySMSBufferDBwithResUrl(objt.resourceURL.toString());
            if (sms != null) {
                try {
                    if (sms.moveToFirst()) {
                        this.mSmsScheduler.onNmsEventChangedObjBufferDbSmsAvailableUsingUrl(sms, objt, false);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (sms != null) {
                sms.close();
                return;
            }
            return;
        } else if (type == 4) {
            Cursor mmscs = this.mMmsScheduler.queryMMSBufferDBwithResUrl(objt.resourceURL.toString());
            if (mmscs != null) {
                try {
                    if (mmscs.moveToFirst()) {
                        this.mMmsScheduler.onNmsEventChangedObjBufferDbMmsAvailableUsingUrl(mmscs, objt, false);
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            if (mmscs != null) {
                mmscs.close();
                return;
            }
            return;
        } else {
            Cursor rcsCs = queryRCSBufferDBwithResUrl(objt.resourceURL.toString());
            if (rcsCs != null) {
                try {
                    if (rcsCs.moveToFirst()) {
                        onNmsEventChangedObjBufferDbRcsAvailableUsingUrl(rcsCs, objt, false);
                    }
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
            }
            if (rcsCs != null) {
                rcsCs.close();
                return;
            }
            return;
        }
        throw th;
        throw th;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void onNmsEventDeletedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, DeletedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventDeletedObjSummaryDbAvailableUsingUrl(), type: " + type);
        if (type == 3) {
            Cursor sms = this.mSmsScheduler.querySMSBufferDBwithResUrl(objt.resourceURL.toString());
            if (sms != null) {
                try {
                    if (sms.moveToFirst()) {
                        this.mSmsScheduler.onNmsEventDeletedObjBufferDbSmsAvailableUsingUrl(sms, objt, false);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (sms != null) {
                sms.close();
                return;
            }
            return;
        } else if (type == 4) {
            Cursor mmscs = this.mMmsScheduler.queryMMSBufferDBwithResUrl(objt.resourceURL.toString());
            if (mmscs != null) {
                try {
                    if (mmscs.moveToFirst()) {
                        this.mMmsScheduler.onNmsEventDeletedObjBufferDbMmsAvailableUsingUrl(mmscs, objt, false);
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            if (mmscs != null) {
                mmscs.close();
                return;
            }
            return;
        } else {
            Cursor rcscs = queryRCSBufferDBwithResUrl(objt.resourceURL.toString());
            if (rcscs != null) {
                try {
                    if (rcscs.moveToFirst()) {
                        onNmsEventDeletedObjBufferDbRcsAvailableUsingUrl(rcscs, objt, false);
                    }
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
            }
            if (rcscs != null) {
                rcscs.close();
                return;
            }
            return;
        }
        throw th;
        throw th;
        throw th;
    }

    public void notifyMsgAppDeleteFail(int dbIndex, long bufferDbId, String line) {
        String str = TAG;
        Log.i(str, "notifyMsgAppDeleteFail, dbIndex: " + dbIndex + " bufferDbId: " + bufferDbId + " line: " + IMSLog.checker(line));
        if (dbIndex == 1) {
            JsonArray jsonArrayRowIds = new JsonArray();
            JsonObject jsobjct = new JsonObject();
            jsobjct.addProperty("id", String.valueOf(bufferDbId));
            jsonArrayRowIds.add(jsobjct);
            this.mCallbackMsgApp.notifyAppCloudDeleteFail(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.CHAT, jsonArrayRowIds.toString());
        }
    }

    public void wipeOutData(int tableindex, String line) {
        wipeOutData(tableindex, line, this.mBufferDbQuery);
    }
}
