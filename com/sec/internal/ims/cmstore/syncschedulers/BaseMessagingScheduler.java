package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.QueryBuilderBase;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;

public class BaseMessagingScheduler extends Handler {
    private static final String TAG = BaseMessagingScheduler.class.getSimpleName();
    protected final IBufferDBEventListener mCallbackMsgApp;
    protected final Context mContext;
    protected int mDbTableContractIndex;
    protected final IDeviceDataChangeListener mDeviceDataChangeListener;
    protected int mMaxNumMsgsNotifyAppInIntent = 20;
    protected final CloudMessageBufferDBEventSchedulingRule mScheduleRule;
    protected SummaryQueryBuilder mSummaryDB;

    public BaseMessagingScheduler(Context context, CloudMessageBufferDBEventSchedulingRule rule, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, Looper looper, SummaryQueryBuilder builder) {
        super(looper);
        this.mContext = context;
        this.mScheduleRule = rule;
        this.mDeviceDataChangeListener = deviceDataListener;
        this.mCallbackMsgApp = callback;
        this.mSummaryDB = builder;
    }

    public String getMessageTypeString(int tableIndex, boolean isRCSFT) {
        if (tableIndex != 1) {
            if (tableIndex == 3) {
                return "SMS";
            }
            if (tableIndex == 4) {
                return "MMS";
            }
            if (tableIndex == 11) {
                return CloudMessageProviderContract.DataTypes.CHAT;
            }
            if (tableIndex == 12) {
                return "FT";
            }
            switch (tableIndex) {
                case 16:
                    return "CALLLOGDATA";
                case 17:
                    return "VVMDATA";
                case 18:
                    return CloudMessageProviderContract.DataTypes.VVMGREETING;
                case 19:
                    return CloudMessageProviderContract.DataTypes.VVMPIN;
                case 20:
                    return CloudMessageProviderContract.DataTypes.VVMPROFILE;
                case 21:
                    return "FAX";
                default:
                    return null;
            }
        } else if (isRCSFT) {
            return "FT";
        } else {
            return CloudMessageProviderContract.DataTypes.CHAT;
        }
    }

    public String getAppTypeString(int tableIndex) {
        if (!(tableIndex == 1 || tableIndex == 14 || tableIndex == 3 || tableIndex == 4 || tableIndex == 11 || tableIndex == 12)) {
            switch (tableIndex) {
                case 16:
                    return "CALLLOGDATA";
                case 17:
                case 18:
                case 19:
                case 20:
                    return "VVMDATA";
                case 21:
                    break;
                default:
                    return null;
            }
        }
        return CloudMessageProviderContract.ApplicationTypes.MSGDATA;
    }

    public void handleOutPutParamSyncFlagSet(ParamSyncFlagsSet setParam, long bufferDbId, int tableIndex, boolean isFt, boolean mIsGoforwardSync, String line, BufferDBChangeParamList changelist) {
        ParamSyncFlagsSet paramSyncFlagsSet = setParam;
        int i = tableIndex;
        boolean z = mIsGoforwardSync;
        BufferDBChangeParamList bufferDBChangeParamList = changelist;
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("handleOutPutParamSyncFlagSet: ");
        sb.append(paramSyncFlagsSet);
        sb.append(" , mIsGoforwardSync: ");
        sb.append(z);
        sb.append("changelist: ");
        sb.append(bufferDBChangeParamList == null ? "null" : "not null");
        Log.d(str, sb.toString());
        if ((paramSyncFlagsSet.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || paramSyncFlagsSet.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) && !z) {
            if (bufferDBChangeParamList == null) {
                BufferDBChangeParamList list = new BufferDBChangeParamList();
                list.mChangelst.add(new BufferDBChangeParam(tableIndex, bufferDbId, mIsGoforwardSync, line, paramSyncFlagsSet.mAction));
                this.mDeviceDataChangeListener.sendDeviceUpdate(list);
                long j = bufferDbId;
                boolean z2 = isFt;
                return;
            }
            bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(tableIndex, bufferDbId, mIsGoforwardSync, line, paramSyncFlagsSet.mAction));
            long j2 = bufferDbId;
            boolean z3 = isFt;
        } else if (paramSyncFlagsSet.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || paramSyncFlagsSet.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
            notifyMsgAppCldNotification(getAppTypeString(i), getMessageTypeString(i, isFt), bufferDbId);
        } else {
            long j3 = bufferDbId;
            boolean z4 = isFt;
        }
    }

    public void notifyMsgAppCldNotification(String appType, String type, long bufferDbId) {
        JsonArray jsonArrayRowIds = new JsonArray();
        JsonObject jsobjct = new JsonObject();
        jsobjct.addProperty("id", String.valueOf(bufferDbId));
        jsonArrayRowIds.add(jsobjct);
        this.mCallbackMsgApp.notifyCloudMessageUpdate(appType, type, jsonArrayRowIds.toString());
    }

    public void notifyInitialSyncStatus(String applicationType, String msgType, String line, CloudMessageBufferDBConstants.InitialSyncStatusFlag status) {
        this.mCallbackMsgApp.notifyAppInitialSyncStatus(applicationType, msgType, line, status);
    }

    public void wipeOutData(int tableindex, String line) {
    }

    public void wipeOutData(int tableindex, String line, QueryBuilderBase bufferDbQuery) {
        bufferDbQuery.deleteAllUsingLineAndTableIndex(tableindex, line);
        this.mSummaryDB.deleteAllUsingLineAndTableIndex(tableindex, line);
        String str = TAG;
        Log.i(str, "deleteAllUsingLineAndType: " + tableindex + " , line = " + IMSLog.checker(line));
    }

    public void deleteMessageFromCloud(int tableindex, long bufferID, String line, QueryBuilderBase bufferDbQuery) {
        QueryBuilderBase queryBuilderBase = bufferDbQuery;
        String str = TAG;
        Log.i(str, "deleteMessageFromCloud: bufferID: " + bufferID);
        if (queryBuilderBase != null) {
            ContentValues cv = new ContentValues();
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId()));
            queryBuilderBase.updateTable(tableindex, cv, "_bufferdbid=?", new String[]{String.valueOf(bufferID)});
            BufferDBChangeParamList changelist = new BufferDBChangeParamList();
            changelist.mChangelst.add(new BufferDBChangeParam(tableindex, bufferID, false, line));
            this.mDeviceDataChangeListener.sendDeviceUpdate(changelist);
        }
    }

    public void msgAppFetchBuffer(Cursor cs, String appType, String type) {
        JsonArray jsonArrayRowIds = new JsonArray();
        do {
            int bufferDBid = cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
            JsonObject jsobjct = new JsonObject();
            jsobjct.addProperty("id", String.valueOf(bufferDBid));
            jsonArrayRowIds.add(jsobjct);
            if (jsonArrayRowIds.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(appType, type, jsonArrayRowIds.toString());
                jsonArrayRowIds = new JsonArray();
            }
        } while (cs.moveToNext() != 0);
        if (jsonArrayRowIds.size() > 0) {
            this.mCallbackMsgApp.notifyCloudMessageUpdate(appType, type, jsonArrayRowIds.toString());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00ad  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onUpdateFromDeviceMsgAppFetch(com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam r24, boolean r25, com.sec.internal.ims.cmstore.querybuilders.QueryBuilderBase r26) {
        /*
            r23 = this;
            r10 = r23
            r11 = r24
            r12 = r26
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onUpdateFromDeviceMsgAppFetch: "
            r1.append(r2)
            r1.append(r11)
            java.lang.String r2 = " tableid: "
            r1.append(r2)
            int r2 = r11.mTableindex
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            r1 = 0
            r2 = 0
            r3 = 0
            int r0 = r11.mTableindex
            long r4 = r11.mBufferRowId
            android.database.Cursor r4 = r12.queryTablewithBufferDbId(r0, r4)
            java.lang.String r0 = "syncaction"
            java.lang.String r5 = "syncdirection"
            if (r4 == 0) goto L_0x0071
            boolean r6 = r4.moveToFirst()     // Catch:{ all -> 0x0063 }
            if (r6 == 0) goto L_0x0071
            int r6 = r4.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x0063 }
            int r6 = r4.getInt(r6)     // Catch:{ all -> 0x0063 }
            r1 = r6
            int r6 = r4.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0063 }
            int r6 = r4.getInt(r6)     // Catch:{ all -> 0x0063 }
            r2 = r6
            java.lang.String r6 = "linenum"
            int r6 = r4.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x0063 }
            java.lang.String r6 = r4.getString(r6)     // Catch:{ all -> 0x0063 }
            r3 = r6
            r13 = r1
            r14 = r2
            r15 = r3
            goto L_0x0074
        L_0x0063:
            r0 = move-exception
            r5 = r0
            if (r4 == 0) goto L_0x0070
            r4.close()     // Catch:{ all -> 0x006b }
            goto L_0x0070
        L_0x006b:
            r0 = move-exception
            r6 = r0
            r5.addSuppressed(r6)
        L_0x0070:
            throw r5
        L_0x0071:
            r13 = r1
            r14 = r2
            r15 = r3
        L_0x0074:
            if (r4 == 0) goto L_0x0079
            r4.close()
        L_0x0079:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r1 = r10.mScheduleRule
            int r2 = r10.mDbTableContractIndex
            long r3 = r11.mBufferRowId
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r20 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r13)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r21 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r14)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = r11.mUpdateType
            r16 = r1
            r17 = r2
            r18 = r3
            r22 = r6
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r9 = r16.getSetFlagsForMsgResponse(r17, r18, r20, r21, r22)
            android.content.ContentValues r1 = new android.content.ContentValues
            r1.<init>()
            r8 = r1
            int r1 = r11.mTableindex
            r2 = 3
            r3 = 1
            java.lang.String r4 = "_id"
            if (r1 != r2) goto L_0x00ad
            long r1 = r11.mTelephonyRowId
            java.lang.Long r1 = java.lang.Long.valueOf(r1)
            r8.put(r4, r1)
            goto L_0x0105
        L_0x00ad:
            int r1 = r11.mTableindex
            r2 = 4
            if (r1 != r2) goto L_0x00bc
            long r1 = r11.mTelephonyRowId
            java.lang.Long r1 = java.lang.Long.valueOf(r1)
            r8.put(r4, r1)
            goto L_0x0105
        L_0x00bc:
            int r1 = r11.mTableindex
            r2 = 17
            if (r1 != r2) goto L_0x00cc
            long r1 = r11.mTelephonyRowId
            java.lang.Long r1 = java.lang.Long.valueOf(r1)
            r8.put(r4, r1)
            goto L_0x0105
        L_0x00cc:
            int r1 = r11.mTableindex
            r2 = 18
            if (r1 != r2) goto L_0x00dc
            long r1 = r11.mTelephonyRowId
            java.lang.Long r1 = java.lang.Long.valueOf(r1)
            r8.put(r4, r1)
            goto L_0x0105
        L_0x00dc:
            int r1 = r11.mTableindex
            r2 = 21
            if (r1 != r2) goto L_0x00ec
            long r1 = r11.mTelephonyRowId
            java.lang.Long r1 = java.lang.Long.valueOf(r1)
            r8.put(r4, r1)
            goto L_0x0105
        L_0x00ec:
            int r1 = r11.mTableindex
            r2 = 16
            if (r1 != r2) goto L_0x00fc
            long r1 = r11.mTelephonyRowId
            java.lang.Long r1 = java.lang.Long.valueOf(r1)
            r8.put(r4, r1)
            goto L_0x0105
        L_0x00fc:
            int r1 = r11.mTableindex
            if (r1 != r3) goto L_0x0105
            boolean r1 = r9.mIsChanged
            if (r1 != 0) goto L_0x0105
            return
        L_0x0105:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r9.mDirection
            int r1 = r1.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r8.put(r5, r1)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r9.mAction
            int r1 = r1.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r8.put(r0, r1)
            java.lang.String r0 = "_bufferdbid=?"
            java.lang.String[] r1 = new java.lang.String[r3]
            r2 = 0
            long r3 = r11.mBufferRowId
            java.lang.String r3 = java.lang.Long.toString(r3)
            r1[r2] = r3
            r7 = r1
            int r1 = r11.mTableindex
            r12.updateTable(r1, r8, r0, r7)
            long r3 = r11.mBufferRowId
            int r5 = r11.mTableindex
            boolean r6 = r11.mIsFT
            r16 = 0
            r1 = r23
            r2 = r9
            r17 = r7
            r7 = r25
            r18 = r8
            r8 = r15
            r19 = r9
            r9 = r16
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.BaseMessagingScheduler.onUpdateFromDeviceMsgAppFetch(com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam, boolean, com.sec.internal.ims.cmstore.querybuilders.QueryBuilderBase):void");
    }

    public void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync, QueryBuilderBase bufferDbQuery) {
        Throwable th;
        ParamSyncFlagsSet flagSet;
        QueryBuilderBase queryBuilderBase = bufferDbQuery;
        String str = TAG;
        Log.i(str, "onCloudUpdateFlagSuccess: " + para);
        Cursor cs = queryBuilderBase.queryTablewithBufferDbId(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                    CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                    String line = cs.getString(cs.getColumnIndexOrThrow("linenum"));
                    if (CloudMessageBufferDBConstants.ActionStatusFlag.Delete.equals(action)) {
                        flagSet = this.mScheduleRule.getSetFlagsForCldResponse(this.mDbTableContractIndex, para.getBufferDBChangeParam().mRowId, direction, action, CloudMessageBufferDBConstants.CloudResponseFlag.SetDelete);
                    } else if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(action)) {
                        CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule = this.mScheduleRule;
                        int i = this.mDbTableContractIndex;
                        flagSet = cloudMessageBufferDBEventSchedulingRule.getSetFlagsForCldResponse(i, para.getBufferDBChangeParam().mRowId, direction, action, CloudMessageBufferDBConstants.CloudResponseFlag.SetRead);
                    } else {
                        ParamSyncFlagsSet flagSet2 = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
                        Log.d(TAG, "onCloudUpdateFlagSuccess: something wrong not processed cloud callback");
                        flagSet = flagSet2;
                    }
                    if (flagSet.mIsChanged) {
                        String[] selectionArgsUpdate = {String.valueOf(para.getBufferDBChangeParam().mRowId)};
                        ContentValues cv = new ContentValues();
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
                        queryBuilderBase.updateTable(para.getBufferDBChangeParam().mDBIndex, cv, "_bufferdbid=?", selectionArgsUpdate);
                    }
                    if (!flagSet.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = action;
                        handleOutPutParamSyncFlagSet(flagSet, para.getBufferDBChangeParam().mRowId, para.getBufferDBChangeParam().mDBIndex, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
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

    public void updateQueryTable(ContentValues cv, long bufferDbId, QueryBuilderBase mBufferDbQuery) {
        mBufferDbQuery.updateTable(this.mDbTableContractIndex, cv, "_bufferdbid=?", new String[]{String.valueOf(bufferDbId)});
    }

    public void handleCloudUploadSuccess(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync, QueryBuilderBase mBufferDbQuery, int type) {
        Throwable th;
        QueryBuilderBase queryBuilderBase = mBufferDbQuery;
        int i = type;
        Cursor cs = queryBuilderBase.queryTablewithBufferDbId(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                    CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                    String line = cs.getString(cs.getColumnIndexOrThrow("linenum"));
                    ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForCldResponse(this.mDbTableContractIndex, para.getBufferDBChangeParam().mRowId, direction, action, CloudMessageBufferDBConstants.CloudResponseFlag.Inserted);
                    if (flagSet.mIsChanged) {
                        ContentValues cv = new ContentValues();
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(para.getReference().resourceURL.toString()));
                        cv.put("path", para.getReference().path);
                        queryBuilderBase.updateTable(para.getBufferDBChangeParam().mDBIndex, cv, "_bufferdbid=?", new String[]{String.valueOf(para.getBufferDBChangeParam().mRowId)});
                        if (i == 3) {
                            this.mSummaryDB.insertResUrlinSummaryIfNonExist(Util.decodeUrlFromServer(para.getReference().resourceURL.toString()), 3);
                        } else if (i == 4) {
                            this.mSummaryDB.insertResUrlinSummaryIfNonExist(Util.decodeUrlFromServer(para.getReference().resourceURL.toString()), 4);
                        } else if (i == 1) {
                            this.mSummaryDB.insertResUrlinSummaryIfNonExist(Util.decodeUrlFromServer(para.getReference().resourceURL.toString()), 1);
                        }
                    }
                    if (!flagSet.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                        handleOutPutParamSyncFlagSet(flagSet, para.getBufferDBChangeParam().mRowId, para.getBufferDBChangeParam().mDBIndex, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
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
}
