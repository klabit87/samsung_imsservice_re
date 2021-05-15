package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Looper;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceLegacyUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamNmsNotificationList;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import com.sec.internal.omanetapi.nms.data.NmsEvent;
import java.util.ArrayList;

public class SmsScheduler extends BaseMessagingScheduler {
    private static final String TAG = SmsScheduler.class.getSimpleName();
    private final SmsQueryBuilder mBufferDbQuery;
    private final MultiLineScheduler mMultiLineScheduler;

    public SmsScheduler(Context context, CloudMessageBufferDBEventSchedulingRule rule, SummaryQueryBuilder builder, MultiLineScheduler lineStatus, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, Looper looper) {
        super(context, rule, deviceDataListener, callback, looper, builder);
        this.mBufferDbQuery = new SmsQueryBuilder(context, callback);
        this.mMultiLineScheduler = lineStatus;
        this.mDbTableContractIndex = 3;
    }

    public long handleObjectSMSCloudSearch(ParamOMAObject objt) {
        String str = TAG;
        Log.i(str, "handleObjectSMSCloudSearch: " + objt.correlationTag);
        if (CloudMessageStrategyManager.getStrategy().isSmsInitialSearchUsingResUrl()) {
            Cursor cs = querySMSBufferDBwithResUrl(objt.resourceURL.toString());
            try {
                handleObjectSMSCloudSearchFromCursor(cs, objt, -1);
                if (cs != null) {
                    cs.close();
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            Cursor cs2 = this.mBufferDbQuery.searchUnSyncedSMSBufferUsingCorrelationTag(objt.correlationTag);
            try {
                handleObjectSMSCloudSearchFromCursor(cs2, objt, -1);
                if (cs2 != null) {
                    cs2.close();
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        return -1;
        throw th;
        throw th;
    }

    private void handleObjectSMSCloudSearchFromCursor(Cursor cs, ParamOMAObject objt, long rowId) {
        ContentValues cv;
        Cursor cursor = cs;
        ParamOMAObject paramOMAObject = objt;
        if (cursor == null || !cs.moveToFirst()) {
            long rowId2 = this.mBufferDbQuery.insertSMSUsingObject(paramOMAObject, false, 0);
            this.mSummaryDB.insertSummaryDbUsingObjectIfNonExist(paramOMAObject, 3);
            if (CloudMessageStrategyManager.getStrategy().shouldSkipMessage(paramOMAObject)) {
                deleteMessageFromCloud(3, rowId2, paramOMAObject.mLine, this.mBufferDbQuery);
                return;
            }
            long j = rowId2;
            handleOutPutParamSyncFlagSet(new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice, CloudMessageBufferDBConstants.ActionStatusFlag.Insert), j, 3, false, paramOMAObject.mIsGoforwardSync, Util.getLineTelUriFromObjUrl(paramOMAObject.resourceURL.toString()), (BufferDBChangeParamList) null);
            return;
        }
        long id = (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long time = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        String str = TAG;
        Log.d(str, "handleObjectSMSCloudSearch find bufferDB: " + paramOMAObject.correlationTag + " id: " + id + " time: " + time + " body:" + body);
        ContentValues cv2 = new ContentValues();
        cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject.lastModSeq);
        cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
        cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject.parentFolder.toString()));
        cv2.put("path", Util.decodeUrlFromServer(paramOMAObject.path.toString()));
        ParamSyncFlagsSet flagsetresult = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
        flagsetresult.mIsChanged = false;
        if (paramOMAObject.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            flagsetresult.setIsChangedActionAndDirection(true, CloudMessageBufferDBConstants.ActionStatusFlag.Delete, CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice);
            cv = cv2;
        } else if (paramOMAObject.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
            if (cursor.getInt(cursor.getColumnIndexOrThrow("read")) == 0) {
                cv2.put("read", 1);
            }
            cv = cv2;
            ParamSyncFlagsSet paramSyncFlagsSet = flagsetresult;
            flagsetresult = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, id, direction, action, CloudMessageBufferDBConstants.ActionStatusFlag.Update);
        } else {
            cv = cv2;
            ParamSyncFlagsSet paramSyncFlagsSet2 = flagsetresult;
            flagsetresult = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, id, direction, action, CloudMessageBufferDBConstants.ActionStatusFlag.Insert);
        }
        this.mSummaryDB.insertSummaryDbUsingObjectIfNonExist(paramOMAObject, 3);
        if (flagsetresult.mIsChanged) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagsetresult.mAction.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagsetresult.mDirection.getId()));
            updateQueryTable(cv, id, this.mBufferDbQuery);
            ContentValues contentValues = cv;
            String str2 = body;
            long j2 = time;
            long j3 = id;
            handleOutPutParamSyncFlagSet(flagsetresult, id, 3, false, paramOMAObject.mIsGoforwardSync, paramOMAObject.mLine, (BufferDBChangeParamList) null);
        } else {
            String str3 = body;
            long j4 = time;
            updateQueryTable(cv, id, this.mBufferDbQuery);
        }
        long id2 = rowId;
    }

    /* Debug info: failed to restart local var, previous not found, register: 28 */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01e9 A[SYNTHETIC, Splitter:B:53:0x01e9] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x01f7 A[SYNTHETIC, Splitter:B:60:0x01f7] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleNormalSyncObjectSmsDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject r29) {
        /*
            r28 = this;
            r10 = r28
            r11 = r29
            java.lang.String r0 = "read"
            java.lang.String r1 = "syncdirection"
            java.lang.String r2 = "syncaction"
            java.lang.String r3 = TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "handleNormalSyncObjectSmsDownload: "
            r4.append(r5)
            java.lang.String r5 = r11.correlationTag
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            r12 = -1
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r3 = r10.mBufferDbQuery     // Catch:{ NullPointerException -> 0x0203 }
            java.lang.String r4 = r11.correlationTag     // Catch:{ NullPointerException -> 0x0203 }
            android.database.Cursor r3 = r3.searchUnSyncedSMSBufferUsingCorrelationTag(r4)     // Catch:{ NullPointerException -> 0x0203 }
            r14 = r3
            if (r14 == 0) goto L_0x0194
            boolean r5 = r14.moveToFirst()     // Catch:{ all -> 0x018f }
            if (r5 == 0) goto L_0x0194
            java.lang.String r5 = "_bufferdbid"
            int r5 = r14.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x018f }
            int r5 = r14.getInt(r5)     // Catch:{ all -> 0x018f }
            long r6 = (long) r5     // Catch:{ all -> 0x018f }
            java.lang.String r5 = "date"
            int r5 = r14.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x018f }
            long r8 = r14.getLong(r5)     // Catch:{ all -> 0x018f }
            java.lang.String r5 = "_id"
            int r5 = r14.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x018f }
            int r5 = r14.getInt(r5)     // Catch:{ all -> 0x018f }
            long r3 = (long) r5     // Catch:{ all -> 0x018f }
            java.lang.String r5 = "linenum"
            int r5 = r14.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x018f }
            java.lang.String r5 = r14.getString(r5)     // Catch:{ all -> 0x018f }
            r9 = r8
            r8 = r5
            int r5 = r14.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x018b }
            int r5 = r14.getInt(r5)     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r5)     // Catch:{ all -> 0x018b }
            int r15 = r14.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x018b }
            int r15 = r14.getInt(r15)     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r15 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r15)     // Catch:{ all -> 0x018b }
            r16 = r5
            java.lang.String r5 = "body"
            int r5 = r14.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x018b }
            java.lang.String r5 = r14.getString(r5)     // Catch:{ all -> 0x018b }
            r22 = r12
            java.lang.String r12 = TAG     // Catch:{ all -> 0x0186 }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ all -> 0x0186 }
            r13.<init>()     // Catch:{ all -> 0x0186 }
            r17 = r15
            java.lang.String r15 = "handleObjectSMSCloudSearch find bufferDB: "
            r13.append(r15)     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = r11.correlationTag     // Catch:{ all -> 0x0186 }
            r13.append(r15)     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = " id: "
            r13.append(r15)     // Catch:{ all -> 0x0186 }
            r13.append(r6)     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = " time: "
            r13.append(r15)     // Catch:{ all -> 0x0186 }
            r13.append(r9)     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = " body:"
            r13.append(r15)     // Catch:{ all -> 0x0186 }
            r13.append(r5)     // Catch:{ all -> 0x0186 }
            java.lang.String r13 = r13.toString()     // Catch:{ all -> 0x0186 }
            android.util.Log.d(r12, r13)     // Catch:{ all -> 0x0186 }
            android.content.ContentValues r12 = new android.content.ContentValues     // Catch:{ all -> 0x0186 }
            r12.<init>()     // Catch:{ all -> 0x0186 }
            java.lang.String r13 = "lastmodseq"
            java.lang.Long r15 = r11.lastModSeq     // Catch:{ all -> 0x0186 }
            r12.put(r13, r15)     // Catch:{ all -> 0x0186 }
            java.lang.String r13 = "res_url"
            java.net.URL r15 = r11.resourceURL     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = r15.toString()     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r15)     // Catch:{ all -> 0x0186 }
            r12.put(r13, r15)     // Catch:{ all -> 0x0186 }
            java.lang.String r13 = "parentfolder"
            java.net.URL r15 = r11.parentFolder     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = r15.toString()     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r15)     // Catch:{ all -> 0x0186 }
            r12.put(r13, r15)     // Catch:{ all -> 0x0186 }
            java.lang.String r13 = "path"
            java.lang.String r15 = r11.path     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = r15.toString()     // Catch:{ all -> 0x0186 }
            java.lang.String r15 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r15)     // Catch:{ all -> 0x0186 }
            r12.put(r13, r15)     // Catch:{ all -> 0x0186 }
            int r13 = r14.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0186 }
            int r13 = r14.getInt(r13)     // Catch:{ all -> 0x0186 }
            r15 = 1
            if (r13 != r15) goto L_0x010c
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r13 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x0186 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r16 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud     // Catch:{ all -> 0x0186 }
            r24 = r16
            goto L_0x0110
        L_0x010c:
            r13 = r16
            r24 = r17
        L_0x0110:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r15 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x0186 }
            r25 = r5
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = r11.mFlag     // Catch:{ all -> 0x0186 }
            boolean r5 = r15.equals(r5)     // Catch:{ all -> 0x0186 }
            if (r5 == 0) goto L_0x0124
            r5 = 1
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0186 }
            r12.put(r0, r5)     // Catch:{ all -> 0x0186 }
        L_0x0124:
            r26 = r9
            r10 = r28
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r15 = r10.mScheduleRule     // Catch:{ all -> 0x01f1 }
            int r0 = r10.mDbTableContractIndex     // Catch:{ all -> 0x01f1 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = r11.mFlag     // Catch:{ all -> 0x01f1 }
            r16 = r0
            r17 = r6
            r19 = r24
            r20 = r13
            r21 = r5
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r0 = r15.getSetFlagsForCldOperation(r16, r17, r19, r20, r21)     // Catch:{ all -> 0x01f1 }
            boolean r5 = r0.mIsChanged     // Catch:{ all -> 0x01f1 }
            if (r5 == 0) goto L_0x015a
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r5 = r0.mDirection     // Catch:{ all -> 0x01f1 }
            int r5 = r5.getId()     // Catch:{ all -> 0x01f1 }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x01f1 }
            r12.put(r1, r5)     // Catch:{ all -> 0x01f1 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r0.mAction     // Catch:{ all -> 0x01f1 }
            int r1 = r1.getId()     // Catch:{ all -> 0x01f1 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x01f1 }
            r12.put(r2, r1)     // Catch:{ all -> 0x01f1 }
        L_0x015a:
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x01f1 }
            r10.updateQueryTable(r12, r6, r1)     // Catch:{ all -> 0x01f1 }
            r1 = 0
            int r1 = (r3 > r1 ? 1 : (r3 == r1 ? 0 : -1))
            if (r1 <= 0) goto L_0x017d
            r5 = 3
            r9 = 0
            boolean r15 = r11.mIsGoforwardSync     // Catch:{ all -> 0x01f1 }
            r16 = 0
            r1 = r28
            r2 = r0
            r17 = r3
            r3 = r6
            r19 = r25
            r20 = r6
            r6 = r9
            r7 = r15
            r9 = r16
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x01f1 }
            goto L_0x0183
        L_0x017d:
            r17 = r3
            r20 = r6
            r19 = r25
        L_0x0183:
            r12 = r22
            goto L_0x01e7
        L_0x0186:
            r0 = move-exception
            r10 = r28
            goto L_0x01f2
        L_0x018b:
            r0 = move-exception
            r10 = r28
            goto L_0x0190
        L_0x018f:
            r0 = move-exception
        L_0x0190:
            r22 = r12
            r1 = r0
            goto L_0x01f5
        L_0x0194:
            r22 = r12
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x01f1 }
            r1 = 0
            r2 = 0
            long r3 = r0.insertSMSUsingObject(r11, r1, r2)     // Catch:{ all -> 0x01f1 }
            java.lang.String r0 = "OUT"
            java.lang.String r1 = r11.DIRECTION     // Catch:{ all -> 0x01ed }
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ all -> 0x01ed }
            if (r0 != 0) goto L_0x01c5
            java.lang.String r0 = "IN"
            java.lang.String r1 = r11.DIRECTION     // Catch:{ all -> 0x01ed }
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ all -> 0x01ed }
            if (r0 == 0) goto L_0x01e6
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ all -> 0x01ed }
            boolean r0 = r0.isSupportAtt72HoursRule()     // Catch:{ all -> 0x01ed }
            if (r0 == 0) goto L_0x01e6
            java.lang.String r0 = r11.DATE     // Catch:{ all -> 0x01ed }
            boolean r0 = com.sec.internal.ims.cmstore.utils.Util.isOver72Hours(r0)     // Catch:{ all -> 0x01ed }
            if (r0 == 0) goto L_0x01e6
        L_0x01c5:
            boolean r0 = r11.mIsGoforwardSync     // Catch:{ all -> 0x01ed }
            if (r0 != 0) goto L_0x01e6
            java.net.URL r0 = r11.resourceURL     // Catch:{ all -> 0x01ed }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x01ed }
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x01ed }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x01ed }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x01ed }
            r2.<init>(r0, r1)     // Catch:{ all -> 0x01ed }
            r5 = 3
            r6 = 0
            boolean r7 = r11.mIsGoforwardSync     // Catch:{ all -> 0x01ed }
            r9 = 0
            r1 = r28
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x01ed }
        L_0x01e6:
            r12 = r3
        L_0x01e7:
            if (r14 == 0) goto L_0x01ec
            r14.close()     // Catch:{ NullPointerException -> 0x0201 }
        L_0x01ec:
            goto L_0x0209
        L_0x01ed:
            r0 = move-exception
            r1 = r0
            r12 = r3
            goto L_0x01f5
        L_0x01f1:
            r0 = move-exception
        L_0x01f2:
            r1 = r0
            r12 = r22
        L_0x01f5:
            if (r14 == 0) goto L_0x0200
            r14.close()     // Catch:{ all -> 0x01fb }
            goto L_0x0200
        L_0x01fb:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x0201 }
        L_0x0200:
            throw r1     // Catch:{ NullPointerException -> 0x0201 }
        L_0x0201:
            r0 = move-exception
            goto L_0x0206
        L_0x0203:
            r0 = move-exception
            r22 = r12
        L_0x0206:
            r0.printStackTrace()
        L_0x0209:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler.handleNormalSyncObjectSmsDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject):long");
    }

    public void onNmsEventChangedObjSmsBufferDbAvailableUsingCorrTag(Cursor cs, ChangedObject objt, boolean mIsGoforwardSync) {
        onNmsEventChangedObjBufferDbSmsAvailable(cs, objt, mIsGoforwardSync, true);
    }

    public void onNmsEventDeletedObjSmsBufferDbAvailableUsingCorrTag(Cursor cs, DeletedObject objt, boolean mIsGoforwardSync) {
        onNmsEventDeletedObjBufferDbSmsAvailable(cs, objt, mIsGoforwardSync, true);
    }

    public void onNmsEventChangedObjBufferDbSmsAvailableUsingUrl(Cursor smsCs, ChangedObject objt, boolean mIsGoforwardSync) {
        onNmsEventChangedObjBufferDbSmsAvailable(smsCs, objt, mIsGoforwardSync, false);
    }

    public void onNmsEventDeletedObjBufferDbSmsAvailableUsingUrl(Cursor smsCs, DeletedObject objt, boolean mIsGoforwardSync) {
        onNmsEventDeletedObjBufferDbSmsAvailable(smsCs, objt, mIsGoforwardSync, false);
    }

    public void onNmsEventChangedObjBufferDbSmsAvailable(Cursor cs, ChangedObject objt, boolean mIsGoforwardSync, boolean isCorrTag) {
        CloudMessageBufferDBConstants.ActionStatusFlag cldAction;
        Cursor cursor = cs;
        ChangedObject changedObject = objt;
        long bufferDbid = (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long _id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String line = cursor.getString(cursor.getColumnIndexOrThrow("linenum"));
        long time = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        String str = TAG;
        Log.d(str, "handleCloudNotifyChangedObj find bufferDB: " + changedObject.correlationTag + " id: " + bufferDbid + " time: " + time + " body:" + body);
        String[] selectionArgsUpdate = {String.valueOf(bufferDbid)};
        ContentValues cv = new ContentValues();
        String selectUpdate = "_bufferdbid=?";
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, changedObject.lastModSeq);
        if (isCorrTag) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(changedObject.resourceURL.toString()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(changedObject.parentFolder.toString()));
        }
        CloudMessageBufferDBConstants.ActionStatusFlag cldAction2 = this.mBufferDbQuery.getCloudActionPerFlag(changedObject.flags);
        if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(cldAction2)) {
            cldAction = cldAction2;
            cv.put("read", 1);
        } else {
            cldAction = cldAction2;
        }
        ContentValues cv2 = cv;
        String selectUpdate2 = selectUpdate;
        long j = _id;
        String[] selectionArgsUpdate2 = selectionArgsUpdate;
        CloudMessageBufferDBConstants.ActionStatusFlag cldAction3 = cldAction;
        long _id2 = j;
        String str2 = body;
        long j2 = time;
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, bufferDbid, direction, action, cldAction3);
        if (flagSet.mIsChanged) {
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        this.mBufferDbQuery.updateTable(this.mDbTableContractIndex, cv2, selectUpdate2, selectionArgsUpdate2);
        if (_id2 > 0) {
            ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbid, this.mDbTableContractIndex, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
            return;
        }
    }

    public void onNmsEventDeletedObjBufferDbSmsAvailable(Cursor cs, DeletedObject objt, boolean mIsGoforwardSync, boolean isCorrTag) {
        Cursor cursor = cs;
        DeletedObject deletedObject = objt;
        long bufferDbid = (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long _id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String line = cursor.getString(cursor.getColumnIndexOrThrow("linenum"));
        long time = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        String str = TAG;
        Log.d(str, "handleCloudNotifyChangedObj find bufferDB: " + deletedObject.correlationTag + " id: " + bufferDbid + " time: " + time + " body:" + body);
        ContentValues cv = new ContentValues();
        String selectUpdate = "_bufferdbid=?";
        String[] selectionArgsUpdate = {String.valueOf(bufferDbid)};
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(deletedObject.lastModSeq));
        if (isCorrTag) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(deletedObject.resourceURL.toString()));
        }
        String selectUpdate2 = selectUpdate;
        String[] selectionArgsUpdate2 = selectionArgsUpdate;
        long _id2 = _id;
        ContentValues cv2 = cv;
        String str2 = body;
        long j = time;
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, bufferDbid, direction, action, CloudMessageBufferDBConstants.ActionStatusFlag.Delete);
        if (flagSet.mIsChanged) {
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        this.mBufferDbQuery.updateTable(this.mDbTableContractIndex, cv2, selectUpdate2, selectionArgsUpdate2);
        if (_id2 > 0) {
            ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbid, this.mDbTableContractIndex, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
            return;
        }
    }

    public void handleExistingBufferForDeviceLegacyUpdate(Cursor cs, DeviceLegacyUpdateParam para, boolean mIsGoforwardSync, BufferDBChangeParamList changelist) {
        Cursor cursor = cs;
        DeviceLegacyUpdateParam deviceLegacyUpdateParam = para;
        String str = TAG;
        Log.i(str, "handleExistingBufferForDeviceLegacyUpdate: " + deviceLegacyUpdateParam + ", mIsGoforwardSync: " + mIsGoforwardSync + ", changelist: " + changelist);
        ContentValues cv = new ContentValues();
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        long bufferDbId = cursor.getLong(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        String line = cursor.getString(cursor.getColumnIndexOrThrow("linenum"));
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForMsgOperation(this.mDbTableContractIndex, bufferDbId, direction, action, deviceLegacyUpdateParam.mOperation);
        if (flagSet.mIsChanged) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        if (CloudMessageBufferDBConstants.MsgOperationFlag.Read.equals(deviceLegacyUpdateParam.mOperation)) {
            cv.put("read", 1);
        }
        if (deviceLegacyUpdateParam.mTableindex == 3) {
            String[] selectionArgs = {String.valueOf(bufferDbId)};
            if (deviceLegacyUpdateParam.mRowId != ((long) cursor.getInt(cursor.getColumnIndexOrThrow("_id")))) {
                cv.put("_id", Long.valueOf(deviceLegacyUpdateParam.mRowId));
            }
            this.mBufferDbQuery.updateTable(deviceLegacyUpdateParam.mTableindex, cv, "_bufferdbid=?", selectionArgs);
        }
        if (flagSet.mIsChanged) {
            ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, deviceLegacyUpdateParam.mTableindex, false, mIsGoforwardSync, line, changelist);
            return;
        }
    }

    public void handleNonExistingBufferForDeviceLegacyUpdate(DeviceLegacyUpdateParam para) {
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        cv.put("linenum", para.mLine);
        if (para.mTableindex == 3) {
            Cursor smsCs = this.mBufferDbQuery.querySMSUseRowId(Long.valueOf(para.mRowId).longValue());
            if (smsCs != null) {
                try {
                    if (smsCs.moveToFirst()) {
                        this.mBufferDbQuery.insertToSMSBufferDB(smsCs, cv, false);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (smsCs != null) {
                smsCs.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    public void notifyMsgAppFetchBuffer(Cursor cs, int type) {
        if (type == 3) {
            JsonArray jsonArrayRowIdsSMS = new JsonArray();
            do {
                int bufferDBid = cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                JsonObject jsobjct = new JsonObject();
                jsobjct.addProperty("id", String.valueOf(bufferDBid));
                jsonArrayRowIdsSMS.add(jsobjct);
                String str = TAG;
                Log.i(str, "jsonArrayRowIdsSMS.size(): " + jsonArrayRowIdsSMS.size() + ", SMS: " + jsonArrayRowIdsSMS.toString());
                if (jsonArrayRowIdsSMS.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                    this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "SMS", jsonArrayRowIdsSMS.toString());
                    jsonArrayRowIdsSMS = new JsonArray();
                }
            } while (cs.moveToNext() != 0);
            if (jsonArrayRowIdsSMS.size() > 0) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "SMS", jsonArrayRowIdsSMS.toString());
            }
        }
    }

    public Cursor queryToCloudUnsyncedSms() {
        return this.mBufferDbQuery.queryToCloudUnsyncedSms();
    }

    public Cursor queryToDeviceUnsyncedSms() {
        return this.mBufferDbQuery.queryToDeviceUnsyncedSms();
    }

    public Cursor querySMSMessagesToUpload() {
        return this.mBufferDbQuery.querySMSMessagesToUpload();
    }

    public Cursor querySMSBufferDBwithResUrl(String resurl) {
        return this.mBufferDbQuery.querySMSBufferDBwithResUrl(resurl);
    }

    public int deleteSMSBufferDBwithResUrl(String resurl) {
        return this.mBufferDbQuery.deleteSMSBufferDBwithResUrl(resurl);
    }

    public Cursor searchUnSyncedSMSBufferUsingCorrelationTag(String correlationTag) {
        return this.mBufferDbQuery.searchUnSyncedSMSBufferUsingCorrelationTag(correlationTag);
    }

    public Cursor querySMSMessagesBySycnDirection(int tableIndex, String syncDirection) {
        return this.mBufferDbQuery.queryMessageBySyncDirection(tableIndex, syncDirection);
    }

    public Cursor queryAllSMSfromTelephony() {
        return this.mBufferDbQuery.queryAllSMSfromTelephony();
    }

    public Cursor queryDeltaSMSfromTelephony() {
        return this.mBufferDbQuery.queryDeltaSMSfromTelephony();
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public void syncReadSmsFromTelephony() {
        Cursor cssms;
        ArrayList<String> updatelist = new ArrayList<>();
        try {
            cssms = this.mBufferDbQuery.queryReadSMSfromTelephony();
            if (cssms != null) {
                if (cssms.moveToFirst()) {
                    updatelist.add(cssms.getString(cssms.getColumnIndex("_id")));
                }
            }
            if (cssms != null) {
                cssms.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        for (int i = 0; i < updatelist.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
            cv.put("read", 1);
            this.mBufferDbQuery.updateTable(this.mDbTableContractIndex, cv, "_id=? AND read=? AND syncaction<>? AND syncaction<>?", new String[]{updatelist.get(i), String.valueOf(0), String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId()), String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId())});
        }
        return;
        throw th;
    }

    public void insertToSMSBufferDB(Cursor cursor, ContentValues cvFlags, boolean isGoForwardSync) {
        this.mBufferDbQuery.insertToSMSBufferDB(cursor, cvFlags, isGoForwardSync);
    }

    private void handleDeviceLegacyUpdateParam(DeviceLegacyUpdateParam para, boolean isGoForwardSync, BufferDBChangeParamList changelist) {
        String str = TAG;
        Log.i(str, "handleDeviceLegacyUpdateParam: " + para);
        Cursor cs = null;
        if (para.mTableindex == 3 && para.mCorrelationTag != null) {
            try {
                switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[para.mOperation.ordinal()]) {
                    case 1:
                        cs = this.mBufferDbQuery.searchSMSBufferUsingCorrelationTagForEarlierNmsEvent(para.mCorrelationTag, para.mLine);
                        break;
                    case 2:
                        cs = this.mBufferDbQuery.searchSMSBufferUsingRowId(para.mRowId);
                        break;
                    case 3:
                        cs = this.mBufferDbQuery.searchSMSBufferUsingCorrelationTagForEarlierNmsEvent(para.mCorrelationTag, para.mLine);
                        break;
                    case 4:
                        if (cs != null) {
                            cs.close();
                            return;
                        }
                        return;
                    case 5:
                        cs = this.mBufferDbQuery.searchSMSBufferUsingRowId(para.mRowId);
                        break;
                    case 6:
                        cs = this.mBufferDbQuery.searchSMSBufferUsingRowId(para.mRowId);
                        break;
                    case 7:
                        cs = this.mBufferDbQuery.searchSMSBufferUsingRowId(para.mRowId);
                        break;
                }
                if (cs == null || !cs.moveToFirst()) {
                    handleNonExistingBufferForDeviceLegacyUpdate(para);
                } else {
                    handleExistingBufferForDeviceLegacyUpdate(cs, para, isGoForwardSync, changelist);
                }
            } finally {
                if (cs != null) {
                    cs.close();
                }
            }
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;

        static {
            int[] iArr = new int[CloudMessageBufferDBConstants.MsgOperationFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag = iArr;
            try {
                iArr[CloudMessageBufferDBConstants.MsgOperationFlag.Received.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Sending.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Sent.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Receiving.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Read.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Delete.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.SendFail.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
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
            handleCloudUploadSuccess(para, mIsGoforwardSync, this.mBufferDbQuery, 3);
        }
    }

    public void cleanAllBufferDB() {
        this.mBufferDbQuery.cleanAllBufferDB();
    }

    public void onAppOperationReceived(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        String str = TAG;
        Log.i(str, "onAppOperationReceived: " + param);
        handleDeviceLegacyUpdateParam(new DeviceLegacyUpdateParam(param.mDataContractType, param.mOperation, param.mRowId, param.mCorrelationTag, param.mCorrelationId, param.mCorrelationId, param.mLine), false, changelist);
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x009e A[SYNTHETIC, Splitter:B:29:0x009e] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject r14, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r15) {
        /*
            r13 = this;
            r6 = 0
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r13.mSummaryDB     // Catch:{ NullPointerException -> 0x00c0 }
            java.net.URL r1 = r14.resourceURL     // Catch:{ NullPointerException -> 0x00c0 }
            java.lang.String r1 = r1.toString()     // Catch:{ NullPointerException -> 0x00c0 }
            android.database.Cursor r0 = r0.querySummaryDBwithResUrl(r1)     // Catch:{ NullPointerException -> 0x00c0 }
            r7 = r0
            java.net.URL r0 = r14.resourceURL     // Catch:{ all -> 0x00b4 }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x00b4 }
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)     // Catch:{ all -> 0x00b4 }
            r8 = r0
            if (r7 == 0) goto L_0x003f
            boolean r0 = r7.moveToFirst()     // Catch:{ all -> 0x00b4 }
            if (r0 == 0) goto L_0x003f
            java.lang.String r0 = "syncaction"
            int r0 = r7.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x00b4 }
            int r0 = r7.getInt(r0)     // Catch:{ all -> 0x00b4 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x00b4 }
            int r1 = r1.getId()     // Catch:{ all -> 0x00b4 }
            if (r0 != r1) goto L_0x003a
            if (r7 == 0) goto L_0x0039
            r7.close()     // Catch:{ NullPointerException -> 0x00c0 }
        L_0x0039:
            return
        L_0x003a:
            r13.onNmsEventChangedObjSummaryDbAvailableUsingUrl(r7, r14)     // Catch:{ all -> 0x00b4 }
            goto L_0x00ae
        L_0x003f:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r13.mSummaryDB     // Catch:{ all -> 0x00b4 }
            r1 = 3
            long r2 = r0.insertNmsEventChangedObjToSummaryDB(r14, r1)     // Catch:{ all -> 0x00b4 }
            com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler r0 = r13.mMultiLineScheduler     // Catch:{ all -> 0x00b4 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.MESSAGE     // Catch:{ all -> 0x00b4 }
            int r0 = r0.getLineInitSyncStatus(r8, r1)     // Catch:{ all -> 0x00b4 }
            r9 = r0
            java.lang.String r0 = TAG     // Catch:{ all -> 0x00b4 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b4 }
            r1.<init>()     // Catch:{ all -> 0x00b4 }
            java.lang.String r4 = "check initial sync status ::"
            r1.append(r4)     // Catch:{ all -> 0x00b4 }
            r1.append(r9)     // Catch:{ all -> 0x00b4 }
            java.lang.String r4 = ", correlationTag: "
            r1.append(r4)     // Catch:{ all -> 0x00b4 }
            java.lang.String r4 = r14.correlationTag     // Catch:{ all -> 0x00b4 }
            r1.append(r4)     // Catch:{ all -> 0x00b4 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00b4 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x00b4 }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x00b4 }
            int r0 = r0.getId()     // Catch:{ all -> 0x00b4 }
            if (r9 != r0) goto L_0x00ae
            java.lang.String r0 = r14.correlationTag     // Catch:{ all -> 0x00b4 }
            if (r0 == 0) goto L_0x00ae
            java.lang.String r0 = r14.correlationTag     // Catch:{ all -> 0x00b4 }
            android.database.Cursor r0 = r13.searchUnSyncedSMSBufferUsingCorrelationTag(r0)     // Catch:{ all -> 0x00b4 }
            r10 = r0
            if (r10 == 0) goto L_0x008e
            boolean r0 = r10.moveToFirst()     // Catch:{ all -> 0x00a2 }
            if (r0 == 0) goto L_0x008e
            r13.onNmsEventChangedObjSmsBufferDbAvailableUsingCorrTag(r10, r14, r6)     // Catch:{ all -> 0x00a2 }
            goto L_0x009c
        L_0x008e:
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r11 = r15.mChangelst     // Catch:{ all -> 0x00a2 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r12 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x00a2 }
            r1 = 7
            r0 = r12
            r4 = r6
            r5 = r8
            r0.<init>(r1, r2, r4, r5)     // Catch:{ all -> 0x00a2 }
            r11.add(r12)     // Catch:{ all -> 0x00a2 }
        L_0x009c:
            if (r10 == 0) goto L_0x00ae
            r10.close()     // Catch:{ all -> 0x00b4 }
            goto L_0x00ae
        L_0x00a2:
            r0 = move-exception
            if (r10 == 0) goto L_0x00ad
            r10.close()     // Catch:{ all -> 0x00a9 }
            goto L_0x00ad
        L_0x00a9:
            r1 = move-exception
            r0.addSuppressed(r1)     // Catch:{ all -> 0x00b4 }
        L_0x00ad:
            throw r0     // Catch:{ all -> 0x00b4 }
        L_0x00ae:
            if (r7 == 0) goto L_0x00b3
            r7.close()     // Catch:{ NullPointerException -> 0x00c0 }
        L_0x00b3:
            goto L_0x00ca
        L_0x00b4:
            r0 = move-exception
            if (r7 == 0) goto L_0x00bf
            r7.close()     // Catch:{ all -> 0x00bb }
            goto L_0x00bf
        L_0x00bb:
            r1 = move-exception
            r0.addSuppressed(r1)     // Catch:{ NullPointerException -> 0x00c0 }
        L_0x00bf:
            throw r0     // Catch:{ NullPointerException -> 0x00c0 }
        L_0x00c0:
            r0 = move-exception
            java.lang.String r1 = TAG
            java.lang.String r2 = r0.toString()
            android.util.Log.e(r1, r2)
        L_0x00ca:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler.handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    public boolean handleCrossSearchChangedObj(ChangedObject objt, boolean mIsGoforwardSync) {
        updateCorrelationTagExtendedChangedObj(objt);
        Cursor cs = querySMSBufferDBwithResUrl(objt.resourceURL.toString());
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    onNmsEventChangedObjBufferDbSmsAvailableUsingUrl(cs, objt, mIsGoforwardSync);
                    if (cs != null) {
                        cs.close();
                    }
                    return true;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
        }
        Cursor cs2 = searchUnSyncedSMSBufferUsingCorrelationTag(objt.correlationTag);
        if (cs2 != null) {
            try {
                if (cs2.moveToFirst()) {
                    onNmsEventChangedObjSmsBufferDbAvailableUsingCorrTag(cs2, objt, mIsGoforwardSync);
                    if (cs2 != null) {
                        cs2.close();
                    }
                    return true;
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (cs2 == null) {
            return false;
        }
        cs2.close();
        return false;
        throw th;
        throw th;
    }

    public boolean handleCrossSearchObj(ParamOMAObject objt, String line, boolean mIsGoforwardSync) {
        String str = TAG;
        Log.i(str, "handleCrossSearchObj():  line: " + IMSLog.checker(line) + " objt: " + objt);
        Cursor cs = querySMSBufferDBwithResUrl(objt.resourceURL.toString());
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    onCrossObjectSearchSmsAvailableUsingResUrl(cs, objt, line, mIsGoforwardSync);
                    if (cs != null) {
                        cs.close();
                    }
                    return true;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
        }
        Cursor cs2 = searchUnSyncedSMSBufferUsingCorrelationTag(objt.correlationTag);
        if (cs2 != null) {
            try {
                if (cs2.moveToFirst()) {
                    onCrossObjectSearchSmsAvailableUsingCorrTag(cs2, objt, line, mIsGoforwardSync);
                    if (cs2 != null) {
                        cs2.close();
                    }
                    return true;
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (cs2 == null) {
            return false;
        }
        cs2.close();
        return false;
        throw th;
        throw th;
    }

    private void onCrossObjectSearchSmsAvailableUsingResUrl(Cursor smsCs, ParamOMAObject objt, String line, boolean mIsGoforwardSync) {
        onCrossObjectSearchSmsAvailable(smsCs, objt, line, mIsGoforwardSync, false);
    }

    private void onCrossObjectSearchSmsAvailableUsingCorrTag(Cursor cs, ParamOMAObject objt, String line, boolean mIsGoforwardSync) {
        onCrossObjectSearchSmsAvailable(cs, objt, line, mIsGoforwardSync, true);
    }

    private void onCrossObjectSearchSmsAvailable(Cursor cs, ParamOMAObject objt, String line, boolean mIsGoforwardSync, boolean isCorrTag) {
        CloudMessageBufferDBConstants.DirectionFlag direction;
        Cursor cursor = cs;
        ParamOMAObject paramOMAObject = objt;
        long bufferDbid = (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long _id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        long time = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        CloudMessageBufferDBConstants.ActionStatusFlag action2 = action;
        sb.append("handleCrossSearchObj find bufferDB: ");
        sb.append(paramOMAObject.correlationTag);
        sb.append(" id: ");
        sb.append(bufferDbid);
        sb.append(" time: ");
        sb.append(time);
        sb.append(" body:");
        sb.append(body);
        Log.d(str, sb.toString());
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject.lastModSeq);
        if (isCorrTag) {
            direction = direction2;
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
            if (paramOMAObject.parentFolder != null) {
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject.parentFolder.toString()));
            }
            if (paramOMAObject.path != null) {
                cv.put("path", Util.decodeUrlFromServer(paramOMAObject.path.toString()));
            }
            if (cursor.getInt(cursor.getColumnIndexOrThrow("read")) == 1) {
                action2 = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                direction = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
            }
        } else {
            direction = direction2;
        }
        CloudMessageBufferDBConstants.ActionStatusFlag cldAction = this.mBufferDbQuery.getCloudActionPerFlag(paramOMAObject.mFlagList);
        if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(cldAction)) {
            cv.put("read", 1);
        }
        ContentValues cv2 = cv;
        String str2 = body;
        long j = time;
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, bufferDbid, direction, action2, cldAction);
        if (flagSet.mIsChanged) {
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        updateQueryTable(cv2, bufferDbid, this.mBufferDbQuery);
        if (_id > 0) {
            ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbid, 3, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
            return;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0085 A[SYNTHETIC, Splitter:B:23:0x0085] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject r7) {
        /*
            r6 = this;
            r0 = 0
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r6.mSummaryDB
            java.net.URL r2 = r7.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.querySummaryDBwithResUrl(r2)
            if (r1 == 0) goto L_0x0032
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x009b }
            if (r2 == 0) goto L_0x0032
            java.lang.String r2 = "syncaction"
            int r2 = r1.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x009b }
            int r2 = r1.getInt(r2)     // Catch:{ all -> 0x009b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x009b }
            int r3 = r3.getId()     // Catch:{ all -> 0x009b }
            if (r2 != r3) goto L_0x002e
            if (r1 == 0) goto L_0x002d
            r1.close()
        L_0x002d:
            return
        L_0x002e:
            r6.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(r1, r7)     // Catch:{ all -> 0x009b }
            goto L_0x0095
        L_0x0032:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r6.mSummaryDB     // Catch:{ all -> 0x009b }
            r3 = 0
            r2.insertNmsEventDeletedObjToSummaryDB(r7, r3)     // Catch:{ all -> 0x009b }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r2 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x009b }
            int r2 = r2.getId()     // Catch:{ all -> 0x009b }
            java.lang.String r3 = TAG     // Catch:{ all -> 0x009b }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x009b }
            r4.<init>()     // Catch:{ all -> 0x009b }
            java.lang.String r5 = "check initial sync status ::"
            r4.append(r5)     // Catch:{ all -> 0x009b }
            r4.append(r2)     // Catch:{ all -> 0x009b }
            java.lang.String r5 = ", correlationTag: "
            r4.append(r5)     // Catch:{ all -> 0x009b }
            java.lang.String r5 = r7.correlationTag     // Catch:{ all -> 0x009b }
            r4.append(r5)     // Catch:{ all -> 0x009b }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x009b }
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x009b }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x009b }
            int r3 = r3.getId()     // Catch:{ all -> 0x009b }
            if (r2 != r3) goto L_0x0095
            java.lang.String r3 = r7.correlationTag     // Catch:{ all -> 0x009b }
            if (r3 == 0) goto L_0x0095
            java.lang.String r3 = r7.correlationTag     // Catch:{ all -> 0x009b }
            android.database.Cursor r3 = r6.searchUnSyncedSMSBufferUsingCorrelationTag(r3)     // Catch:{ all -> 0x009b }
            if (r3 == 0) goto L_0x007c
            boolean r4 = r3.moveToFirst()     // Catch:{ all -> 0x0089 }
            if (r4 == 0) goto L_0x007c
            r6.onNmsEventDeletedObjSmsBufferDbAvailableUsingCorrTag(r3, r7, r0)     // Catch:{ all -> 0x0089 }
            goto L_0x0083
        L_0x007c:
            java.lang.String r4 = TAG     // Catch:{ all -> 0x0089 }
            java.lang.String r5 = "did not find buffer item to delete"
            android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0089 }
        L_0x0083:
            if (r3 == 0) goto L_0x0095
            r3.close()     // Catch:{ all -> 0x009b }
            goto L_0x0095
        L_0x0089:
            r4 = move-exception
            if (r3 == 0) goto L_0x0094
            r3.close()     // Catch:{ all -> 0x0090 }
            goto L_0x0094
        L_0x0090:
            r5 = move-exception
            r4.addSuppressed(r5)     // Catch:{ all -> 0x009b }
        L_0x0094:
            throw r4     // Catch:{ all -> 0x009b }
        L_0x0095:
            if (r1 == 0) goto L_0x009a
            r1.close()
        L_0x009a:
            return
        L_0x009b:
            r2 = move-exception
            if (r1 == 0) goto L_0x00a6
            r1.close()     // Catch:{ all -> 0x00a2 }
            goto L_0x00a6
        L_0x00a2:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x00a6:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler.handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject):void");
    }

    private void onNmsEventDeletedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, DeletedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventDeletedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor smsCs = querySMSBufferDBwithResUrl(objt.resourceURL.toString());
        if (smsCs != null) {
            try {
                if (smsCs.moveToFirst()) {
                    onNmsEventDeletedObjBufferDbSmsAvailableUsingUrl(smsCs, objt, false);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (smsCs != null) {
            smsCs.close();
            return;
        }
        return;
        throw th;
    }

    private void onNmsEventChangedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, ChangedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventChangedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor smsCs = querySMSBufferDBwithResUrl(objt.resourceURL.toString());
        if (smsCs != null) {
            try {
                if (smsCs.moveToFirst()) {
                    onNmsEventChangedObjBufferDbSmsAvailableUsingUrl(smsCs, objt, false);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (smsCs != null) {
            smsCs.close();
            return;
        }
        return;
        throw th;
    }

    private void updateCorrelationTagExtendedChangedObj(ChangedObject objtChange) {
        String address;
        int type;
        try {
            String body = objtChange.extendedMessage.content[0].content;
            if ("IN".equalsIgnoreCase(objtChange.extendedMessage.direction)) {
                type = 1;
                address = Util.getMsisdn(objtChange.extendedMessage.sender);
            } else {
                address = Util.getMsisdn(objtChange.extendedMessage.recipients[0].uri);
                type = 2;
            }
            objtChange.correlationTag = CloudMessageStrategyManager.getStrategy().getSmsHashTagOrCorrelationTag(address, type, body);
        } catch (Exception e) {
            Log.e(TAG, "updateCorrelationTag: " + e.getMessage());
        }
    }

    public void updateCorrelationTagObject(ParamOMAObject objt) {
        int type;
        String address;
        String str = TAG;
        Log.i(str, "updateCorrelationTagObject: " + objt);
        try {
            String body = objt.TEXT_CONTENT;
            if ("IN".equalsIgnoreCase(objt.DIRECTION)) {
                type = 1;
                address = Util.getMsisdn(objt.FROM);
            } else {
                type = 2;
                address = Util.getMsisdn(objt.TO.get(0));
            }
            objt.correlationTag = CloudMessageStrategyManager.getStrategy().getSmsHashTagOrCorrelationTag(address, type, body);
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "updateCorrelationTagObject: " + e.getMessage());
        }
    }

    public void onNotificationReceived(ParamNmsNotificationList notification) {
        Log.i(TAG, "onNotificationReceived: " + notification);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        if (!(notification.mNmsEventList == null || notification.mNmsEventList.nmsEvent == null)) {
            for (NmsEvent event : notification.mNmsEventList.nmsEvent) {
                if (event.changedObject != null) {
                    updateCorrelationTagExtendedChangedObj(event.changedObject);
                    handleCloudNotifyChangedObj(event.changedObject, downloadlist);
                }
                if (event.deletedObject != null) {
                    handleCloudNotifyDeletedObj(event.deletedObject);
                }
            }
        }
        if (downloadlist.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(downloadlist);
        }
    }

    public void notifyMsgAppDeleteFail(int dbIndex, long bufferDbId, String line) {
        String str = TAG;
        Log.i(str, "notifyMsgAppDeleteFail, dbIndex: " + dbIndex + " bufferDbId: " + bufferDbId + " line: " + IMSLog.checker(line));
        if (dbIndex == 3) {
            JsonArray jsonArrayRowIds = new JsonArray();
            JsonObject jsobjct = new JsonObject();
            jsobjct.addProperty("id", String.valueOf(bufferDbId));
            jsonArrayRowIds.add(jsobjct);
            this.mCallbackMsgApp.notifyAppCloudDeleteFail(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "SMS", jsonArrayRowIds.toString());
        }
    }
}
