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
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamNmsNotificationList;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import com.sec.internal.omanetapi.nms.data.NmsEvent;

public class CallLogScheduler extends BaseMessagingScheduler {
    private static final String TAG = CallLogScheduler.class.getSimpleName();
    private final CallLogQueryBuilder mBufferDbQuery;

    public CallLogScheduler(Context context, CloudMessageBufferDBEventSchedulingRule rule, SummaryQueryBuilder builder, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, Looper looper) {
        super(context, rule, deviceDataListener, callback, looper, builder);
        this.mBufferDbQuery = new CallLogQueryBuilder(context, callback);
        this.mDbTableContractIndex = 16;
    }

    public void onNotificationReceived(ParamNmsNotificationList notification) {
        Log.i(TAG, "onNotificationReceived: " + notification);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        if (!(notification.mNmsEventList == null || notification.mNmsEventList.nmsEvent == null)) {
            for (NmsEvent event : notification.mNmsEventList.nmsEvent) {
                if (event.changedObject != null) {
                    handleCloudNotifyChangedObj(event.changedObject);
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

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.CallLogScheduler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;

        static {
            int[] iArr = new int[CloudMessageBufferDBConstants.MsgOperationFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag = iArr;
            try {
                iArr[CloudMessageBufferDBConstants.MsgOperationFlag.Read.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Delete.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public void onAppOperationReceived(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        String str = TAG;
        Log.i(str, "onAppOperationReceived: " + param);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[param.mOperation.ordinal()];
        if (i == 1) {
            readCallLog(param, changelist);
        } else if (i == 2) {
            deleteCallLog(param, changelist);
        }
    }

    private void deleteCallLog(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        Throwable th;
        ParamAppJsonValue paramAppJsonValue = param;
        Cursor cs = this.mBufferDbQuery.queryCallLogBufferDBwithAppId((long) paramAppJsonValue.mRowId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    ContentValues cv = new ContentValues();
                    CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                    CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                    long bufferDbId = cs.getLong(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForMsgOperation(this.mDbTableContractIndex, bufferDbId, direction, action, CloudMessageBufferDBConstants.MsgOperationFlag.Delete);
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
                    this.mBufferDbQuery.updateTable(paramAppJsonValue.mDataContractType, cv, "_bufferdbid=?", new String[]{String.valueOf(bufferDbId)});
                    String line = cs.getString(cs.getColumnIndexOrThrow("linenum"));
                    if (flagSet.mIsChanged) {
                        handleOutPutParamSyncFlagSet(flagSet, bufferDbId, paramAppJsonValue.mDataContractType, false, false, line, changelist);
                    }
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (cs != null) {
            cs.close();
            return;
        }
        return;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:21:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readCallLog(com.sec.internal.ims.cmstore.params.ParamAppJsonValue r22, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r23) {
        /*
            r21 = this;
            r10 = r21
            r11 = r22
            java.lang.String r0 = "syncdirection"
            java.lang.String r1 = "syncaction"
            com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder r2 = r10.mBufferDbQuery
            int r3 = r11.mRowId
            long r3 = (long) r3
            android.database.Cursor r12 = r2.queryCallLogBufferDBwithAppId(r3)
            if (r12 == 0) goto L_0x009b
            boolean r2 = r12.moveToFirst()     // Catch:{ all -> 0x00a9 }
            if (r2 == 0) goto L_0x009b
            android.content.ContentValues r2 = new android.content.ContentValues     // Catch:{ all -> 0x00a9 }
            r2.<init>()     // Catch:{ all -> 0x00a9 }
            r13 = r2
            java.lang.String r2 = "_bufferdbid"
            int r2 = r12.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x00a9 }
            long r16 = r12.getLong(r2)     // Catch:{ all -> 0x00a9 }
            int r2 = r12.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x00a9 }
            int r2 = r12.getInt(r2)     // Catch:{ all -> 0x00a9 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r19 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r2)     // Catch:{ all -> 0x00a9 }
            int r2 = r12.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x00a9 }
            int r2 = r12.getInt(r2)     // Catch:{ all -> 0x00a9 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r18 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r2)     // Catch:{ all -> 0x00a9 }
            java.lang.String r2 = "linenum"
            int r2 = r12.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x00a9 }
            java.lang.String r8 = r12.getString(r2)     // Catch:{ all -> 0x00a9 }
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r14 = r10.mScheduleRule     // Catch:{ all -> 0x00a9 }
            int r15 = r10.mDbTableContractIndex     // Catch:{ all -> 0x00a9 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r20 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Read     // Catch:{ all -> 0x00a9 }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = r14.getSetFlagsForMsgOperation(r15, r16, r18, r19, r20)     // Catch:{ all -> 0x00a9 }
            r14 = r2
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = r14.mDirection     // Catch:{ all -> 0x00a9 }
            int r2 = r2.getId()     // Catch:{ all -> 0x00a9 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x00a9 }
            r13.put(r0, r2)     // Catch:{ all -> 0x00a9 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = r14.mAction     // Catch:{ all -> 0x00a9 }
            int r0 = r0.getId()     // Catch:{ all -> 0x00a9 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x00a9 }
            r13.put(r1, r0)     // Catch:{ all -> 0x00a9 }
            java.lang.String r0 = "_bufferdbid=?"
            r1 = 1
            java.lang.String[] r2 = new java.lang.String[r1]     // Catch:{ all -> 0x00a9 }
            r3 = 0
            java.lang.String r4 = java.lang.String.valueOf(r16)     // Catch:{ all -> 0x00a9 }
            r2[r3] = r4     // Catch:{ all -> 0x00a9 }
            r15 = r2
            com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder r2 = r10.mBufferDbQuery     // Catch:{ all -> 0x00a9 }
            int r3 = r11.mDataContractType     // Catch:{ all -> 0x00a9 }
            r2.updateTable(r3, r13, r0, r15)     // Catch:{ all -> 0x00a9 }
            boolean r2 = r14.mIsChanged     // Catch:{ all -> 0x00a9 }
            if (r2 != r1) goto L_0x009a
            int r5 = r11.mDataContractType     // Catch:{ all -> 0x00a9 }
            r6 = 0
            r7 = 0
            r1 = r21
            r2 = r14
            r3 = r16
            r9 = r23
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x00a9 }
        L_0x009a:
            goto L_0x00a3
        L_0x009b:
            java.lang.String r0 = TAG     // Catch:{ all -> 0x00a9 }
            java.lang.String r1 = "readcalllog found no record"
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x00a9 }
        L_0x00a3:
            if (r12 == 0) goto L_0x00a8
            r12.close()
        L_0x00a8:
            return
        L_0x00a9:
            r0 = move-exception
            r1 = r0
            if (r12 == 0) goto L_0x00b6
            r12.close()     // Catch:{ all -> 0x00b1 }
            goto L_0x00b6
        L_0x00b1:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x00b6:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.CallLogScheduler.readCallLog(com.sec.internal.ims.cmstore.params.ParamAppJsonValue, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 26 */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x01a4 A[SYNTHETIC, Splitter:B:47:0x01a4] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectCallLogMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r27) {
        /*
            r26 = this;
            r10 = r26
            r11 = r27
            java.lang.String r0 = "syncdirection"
            java.lang.String r1 = "syncaction"
            java.lang.String r2 = "CALLLOGDATA"
            java.lang.String r3 = TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "handleObjectCallLogMessageCloudSearch: "
            r4.append(r5)
            r4.append(r11)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            r12 = -1
            com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder r3 = r10.mBufferDbQuery     // Catch:{ NullPointerException -> 0x01b0 }
            java.net.URL r4 = r11.resourceURL     // Catch:{ NullPointerException -> 0x01b0 }
            java.lang.String r4 = r4.toString()     // Catch:{ NullPointerException -> 0x01b0 }
            android.database.Cursor r3 = r3.queryCallLogMessageBufferDBwithResUrl(r4)     // Catch:{ NullPointerException -> 0x01b0 }
            r14 = r3
            java.net.URL r3 = r11.resourceURL     // Catch:{ all -> 0x019e }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x019e }
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r3)     // Catch:{ all -> 0x019e }
            r15 = r3
            if (r14 == 0) goto L_0x0179
            boolean r3 = r14.moveToFirst()     // Catch:{ all -> 0x019e }
            if (r3 == 0) goto L_0x0179
            java.lang.String r2 = "_bufferdbid"
            int r2 = r14.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x019e }
            int r2 = r14.getInt(r2)     // Catch:{ all -> 0x019e }
            long r8 = (long) r2     // Catch:{ all -> 0x019e }
            java.lang.String r2 = "_id"
            int r2 = r14.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x019e }
            int r2 = r14.getInt(r2)     // Catch:{ all -> 0x019e }
            int r3 = r14.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x019e }
            int r3 = r14.getInt(r3)     // Catch:{ all -> 0x019e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r21 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r3)     // Catch:{ all -> 0x019e }
            int r3 = r14.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x019e }
            int r3 = r14.getInt(r3)     // Catch:{ all -> 0x019e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r20 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r3)     // Catch:{ all -> 0x019e }
            android.content.ContentValues r3 = new android.content.ContentValues     // Catch:{ all -> 0x019e }
            r3.<init>()     // Catch:{ all -> 0x019e }
            r7 = r3
            java.lang.String r3 = "seen"
            com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder r4 = r10.mBufferDbQuery     // Catch:{ all -> 0x019e }
            com.sec.internal.omanetapi.nms.data.FlagList r5 = r11.mFlagList     // Catch:{ all -> 0x019e }
            int r4 = r4.getIfSeenValueUsingFlag(r5)     // Catch:{ all -> 0x019e }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x019e }
            r7.put(r3, r4)     // Catch:{ all -> 0x019e }
            java.lang.String r3 = "answeredby"
            com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder r4 = r10.mBufferDbQuery     // Catch:{ all -> 0x019e }
            com.sec.internal.omanetapi.nms.data.FlagList r5 = r11.mFlagList     // Catch:{ all -> 0x019e }
            int r4 = r4.getIfAnsweredValueUsingFlag(r5)     // Catch:{ all -> 0x019e }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x019e }
            r7.put(r3, r4)     // Catch:{ all -> 0x019e }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x019e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x019e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x019e }
            r3.<init>(r4, r5)     // Catch:{ all -> 0x019e }
            r5 = r3
            r3 = 0
            r5.mIsChanged = r3     // Catch:{ all -> 0x019e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x019e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = r11.mFlag     // Catch:{ all -> 0x019e }
            boolean r3 = r3.equals(r4)     // Catch:{ all -> 0x019e }
            if (r3 == 0) goto L_0x00cf
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r3 = r10.mScheduleRule     // Catch:{ all -> 0x00cb }
            int r4 = r10.mDbTableContractIndex     // Catch:{ all -> 0x00cb }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r22 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x00cb }
            r16 = r3
            r17 = r4
            r18 = r8
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = r16.getSetFlagsForCldOperation(r17, r18, r20, r21, r22)     // Catch:{ all -> 0x00cb }
            r5 = r3
            r22 = r8
            r18 = r12
            r13 = r5
            r12 = r7
            goto L_0x0113
        L_0x00cb:
            r0 = move-exception
            r1 = r0
            goto L_0x01a2
        L_0x00cf:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x019e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = r11.mFlag     // Catch:{ all -> 0x019e }
            boolean r3 = r3.equals(r4)     // Catch:{ all -> 0x019e }
            if (r3 == 0) goto L_0x00f4
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r3 = r10.mScheduleRule     // Catch:{ all -> 0x019e }
            int r4 = r10.mDbTableContractIndex     // Catch:{ all -> 0x019e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r16 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x019e }
            r17 = r5
            r5 = r8
            r18 = r12
            r12 = r7
            r7 = r20
            r22 = r8
            r8 = r21
            r9 = r16
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = r3.getSetFlagsForCldOperation(r4, r5, r7, r8, r9)     // Catch:{ all -> 0x0199 }
            r5 = r3
            r13 = r5
            goto L_0x0113
        L_0x00f4:
            r17 = r5
            r22 = r8
            r18 = r12
            r12 = r7
            r3 = 1
            if (r2 >= r3) goto L_0x0111
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r3 = r10.mScheduleRule     // Catch:{ all -> 0x0199 }
            int r4 = r10.mDbTableContractIndex     // Catch:{ all -> 0x0199 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r9 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x0199 }
            r5 = r22
            r7 = r20
            r8 = r21
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = r3.getSetFlagsForCldOperation(r4, r5, r7, r8, r9)     // Catch:{ all -> 0x0199 }
            r5 = r3
            r13 = r5
            goto L_0x0113
        L_0x0111:
            r13 = r17
        L_0x0113:
            java.lang.String r3 = TAG     // Catch:{ all -> 0x0199 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0199 }
            r4.<init>()     // Catch:{ all -> 0x0199 }
            java.lang.String r5 = "handleObjectCallLogMessageCloudSearch: callog found: "
            r4.append(r5)     // Catch:{ all -> 0x0199 }
            r8 = r22
            r4.append(r8)     // Catch:{ all -> 0x0199 }
            java.lang.String r5 = "mIsChanged: "
            r4.append(r5)     // Catch:{ all -> 0x0199 }
            boolean r5 = r13.mIsChanged     // Catch:{ all -> 0x0199 }
            r4.append(r5)     // Catch:{ all -> 0x0199 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0199 }
            android.util.Log.d(r3, r4)     // Catch:{ all -> 0x0199 }
            boolean r3 = r13.mIsChanged     // Catch:{ all -> 0x0199 }
            if (r3 == 0) goto L_0x016b
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = r13.mAction     // Catch:{ all -> 0x0199 }
            int r3 = r3.getId()     // Catch:{ all -> 0x0199 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0199 }
            r12.put(r1, r3)     // Catch:{ all -> 0x0199 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r13.mDirection     // Catch:{ all -> 0x0199 }
            int r1 = r1.getId()     // Catch:{ all -> 0x0199 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0199 }
            r12.put(r0, r1)     // Catch:{ all -> 0x0199 }
            com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x0199 }
            r10.updateQueryTable(r12, r8, r0)     // Catch:{ all -> 0x0199 }
            int r5 = r10.mDbTableContractIndex     // Catch:{ all -> 0x0199 }
            r6 = 0
            r7 = 0
            r0 = 0
            r1 = r26
            r16 = r2
            r2 = r13
            r3 = r8
            r24 = r8
            r8 = r15
            r9 = r0
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x0199 }
            goto L_0x0176
        L_0x016b:
            r16 = r2
            r24 = r8
            com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x0199 }
            r1 = r24
            r10.updateQueryTable(r12, r1, r0)     // Catch:{ all -> 0x0199 }
        L_0x0176:
            r12 = r18
            goto L_0x018f
        L_0x0179:
            r18 = r12
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r10.mSummaryDB     // Catch:{ all -> 0x0199 }
            r1 = 16
            r0.insertSummaryDbUsingObjectIfNonExist(r11, r1)     // Catch:{ all -> 0x0199 }
            com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x0199 }
            long r0 = r0.insertCallLogMessageUsingObject(r11, r15)     // Catch:{ all -> 0x0199 }
            r3 = r0
            com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x0195 }
            r0.notifyApplication(r2, r2, r3)     // Catch:{ all -> 0x0195 }
            r12 = r3
        L_0x018f:
            if (r14 == 0) goto L_0x0194
            r14.close()     // Catch:{ NullPointerException -> 0x01ae }
        L_0x0194:
            goto L_0x01bc
        L_0x0195:
            r0 = move-exception
            r1 = r0
            r12 = r3
            goto L_0x01a2
        L_0x0199:
            r0 = move-exception
            r1 = r0
            r12 = r18
            goto L_0x01a2
        L_0x019e:
            r0 = move-exception
            r18 = r12
            r1 = r0
        L_0x01a2:
            if (r14 == 0) goto L_0x01ad
            r14.close()     // Catch:{ all -> 0x01a8 }
            goto L_0x01ad
        L_0x01a8:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x01ae }
        L_0x01ad:
            throw r1     // Catch:{ NullPointerException -> 0x01ae }
        L_0x01ae:
            r0 = move-exception
            goto L_0x01b3
        L_0x01b0:
            r0 = move-exception
            r18 = r12
        L_0x01b3:
            java.lang.String r1 = TAG
            java.lang.String r2 = r0.toString()
            android.util.Log.e(r1, r2)
        L_0x01bc:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.CallLogScheduler.handleObjectCallLogMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject):long");
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0036 A[SYNTHETIC, Splitter:B:15:0x0036] */
    /* JADX WARNING: Removed duplicated region for block: B:31:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject r4) {
        /*
            r3 = this;
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r3.mSummaryDB     // Catch:{ NullPointerException -> 0x0046 }
            java.net.URL r1 = r4.resourceURL     // Catch:{ NullPointerException -> 0x0046 }
            java.lang.String r1 = r1.toString()     // Catch:{ NullPointerException -> 0x0046 }
            android.database.Cursor r0 = r0.querySummaryDBwithResUrl(r1)     // Catch:{ NullPointerException -> 0x0046 }
            if (r0 == 0) goto L_0x0031
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x003a }
            if (r1 == 0) goto L_0x0031
            java.lang.String r1 = "syncaction"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x003a }
            int r1 = r0.getInt(r1)     // Catch:{ all -> 0x003a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x003a }
            int r2 = r2.getId()     // Catch:{ all -> 0x003a }
            if (r1 != r2) goto L_0x002d
            if (r0 == 0) goto L_0x002c
            r0.close()     // Catch:{ NullPointerException -> 0x0046 }
        L_0x002c:
            return
        L_0x002d:
            r3.onNmsEventChangedObjSummaryDbAvailableUsingUrl(r0, r4)     // Catch:{ all -> 0x003a }
            goto L_0x0034
        L_0x0031:
            r3.onNmsEventChangedObjSummaryDbNotAvailableUsingUrl(r4)     // Catch:{ all -> 0x003a }
        L_0x0034:
            if (r0 == 0) goto L_0x0039
            r0.close()     // Catch:{ NullPointerException -> 0x0046 }
        L_0x0039:
            goto L_0x0050
        L_0x003a:
            r1 = move-exception
            if (r0 == 0) goto L_0x0045
            r0.close()     // Catch:{ all -> 0x0041 }
            goto L_0x0045
        L_0x0041:
            r2 = move-exception
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x0046 }
        L_0x0045:
            throw r1     // Catch:{ NullPointerException -> 0x0046 }
        L_0x0046:
            r0 = move-exception
            java.lang.String r1 = TAG
            java.lang.String r2 = r0.toString()
            android.util.Log.e(r1, r2)
        L_0x0050:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.CallLogScheduler.handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:27:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject r4) {
        /*
            r3 = this;
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r3.mSummaryDB
            java.net.URL r1 = r4.resourceURL
            java.lang.String r1 = r1.toString()
            android.database.Cursor r0 = r0.querySummaryDBwithResUrl(r1)
            if (r0 == 0) goto L_0x003a
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0043 }
            if (r1 == 0) goto L_0x003a
            java.lang.String r1 = "syncaction"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0043 }
            int r1 = r0.getInt(r1)     // Catch:{ all -> 0x0043 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x0043 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0043 }
            if (r1 == r2) goto L_0x0034
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x0043 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0043 }
            if (r1 != r2) goto L_0x0030
            goto L_0x0034
        L_0x0030:
            r3.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(r0, r4)     // Catch:{ all -> 0x0043 }
            goto L_0x003d
        L_0x0034:
            if (r0 == 0) goto L_0x0039
            r0.close()
        L_0x0039:
            return
        L_0x003a:
            r3.onNmsEventDeletedObjSummaryDbNotAvailableUsingUrl(r4)     // Catch:{ all -> 0x0043 }
        L_0x003d:
            if (r0 == 0) goto L_0x0042
            r0.close()
        L_0x0042:
            return
        L_0x0043:
            r1 = move-exception
            if (r0 == 0) goto L_0x004e
            r0.close()     // Catch:{ all -> 0x004a }
            goto L_0x004e
        L_0x004a:
            r2 = move-exception
            r1.addSuppressed(r2)
        L_0x004e:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.CallLogScheduler.handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject):void");
    }

    private void onNmsEventChangedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, ChangedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventChangedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor clgCs = this.mBufferDbQuery.queryCallLogMessageBufferDBwithResUrl(objt.resourceURL.toString());
        if (clgCs != null) {
            try {
                if (clgCs.moveToFirst()) {
                    int rowid = clgCs.getInt(clgCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    int seen = clgCs.getInt(clgCs.getColumnIndexOrThrow("seen"));
                    CloudMessageBufferDBConstants.ActionStatusFlag cldAction = this.mBufferDbQuery.getCloudActionPerFlag(objt.flags);
                    if (seen != 1 && CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(cldAction)) {
                        ContentValues cv = new ContentValues();
                        cv.put("seen", 1);
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
                        updateQueryTable(cv, (long) rowid, this.mBufferDbQuery);
                        this.mBufferDbQuery.notifyApplication("CALLLOGDATA", "CALLLOGDATA", (long) rowid);
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (clgCs != null) {
            clgCs.close();
            return;
        }
        return;
        throw th;
    }

    private void onNmsEventDeletedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, DeletedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventDeletedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor clgCs = this.mBufferDbQuery.queryCallLogMessageBufferDBwithResUrl(objt.resourceURL.toString());
        if (clgCs != null) {
            try {
                if (clgCs.moveToFirst()) {
                    int rowid = clgCs.getInt(clgCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    int action = clgCs.getInt(clgCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                    if (!(CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId() == action || CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId() == action)) {
                        ContentValues cv = new ContentValues();
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
                        updateQueryTable(cv, (long) rowid, this.mBufferDbQuery);
                        this.mBufferDbQuery.notifyApplication("CALLLOGDATA", "CALLLOGDATA", (long) rowid);
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (clgCs != null) {
            clgCs.close();
            return;
        }
        return;
        throw th;
    }

    private void onNmsEventChangedObjSummaryDbNotAvailableUsingUrl(ChangedObject objt) {
        this.mSummaryDB.insertNmsEventChangedObjToSummaryDB(objt, 16);
        this.mBufferDbQuery.notifyApplication("CALLLOGDATA", "CALLLOGDATA", this.mBufferDbQuery.insertNewCallLogUsingChangedObject(objt, Util.getLineTelUriFromObjUrl(objt.resourceURL.toString())));
    }

    private void onNmsEventDeletedObjSummaryDbNotAvailableUsingUrl(DeletedObject objt) {
        this.mSummaryDB.insertNmsEventDeletedObjToSummaryDB(objt, 16);
    }

    public void onUpdateFromDeviceMsgAppFetch(DeviceMsgAppFetchUpdateParam para, boolean mIsGoforwardSync) {
        onUpdateFromDeviceMsgAppFetch(para, mIsGoforwardSync, this.mBufferDbQuery);
    }

    public void handleNormalSyncDownloadedCallLog(ParamOMAObject para) {
        String str = TAG;
        Log.d(str, "handleNormalSyncDownloadedCallLog: " + para);
    }

    public Cursor queryCallLogMessageBufferDBwithResUrl(String url) {
        return this.mBufferDbQuery.queryCallLogMessageBufferDBwithResUrl(url);
    }

    public void notifyMsgAppDeleteFail(int dbIndex, long bufferDbId, String line) {
        String str = TAG;
        Log.i(str, "notifyMsgAppDeleteFail, dbIndex: " + dbIndex + " bufferDbId: " + bufferDbId + " line: " + IMSLog.checker(line));
        if (dbIndex == 16) {
            JsonArray jsonArrayRowIds = new JsonArray();
            JsonObject jsobjct = new JsonObject();
            jsobjct.addProperty("id", String.valueOf(bufferDbId));
            jsonArrayRowIds.add(jsobjct);
            this.mCallbackMsgApp.notifyAppCloudDeleteFail("CALLLOGDATA", "CALLLOGDATA", jsonArrayRowIds.toString());
        }
    }

    public void wipeOutData(int tableindex, String line) {
        wipeOutData(tableindex, line, this.mBufferDbQuery);
    }

    public void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync) {
        onCloudUpdateFlagSuccess(para, mIsGoforwardSync, this.mBufferDbQuery);
    }
}
