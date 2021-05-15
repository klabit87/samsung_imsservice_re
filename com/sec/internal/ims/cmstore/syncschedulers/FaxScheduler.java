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
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamNmsNotificationList;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import com.sec.internal.omanetapi.nms.data.NmsEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.mail.MessagingException;

public class FaxScheduler extends BaseMessagingScheduler {
    private static final String TAG = FaxScheduler.class.getSimpleName();
    private final FaxQueryBuilder mBufferDbQuery;

    public FaxScheduler(Context context, CloudMessageBufferDBEventSchedulingRule rule, SummaryQueryBuilder builder, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, Looper looper) {
        super(context, rule, deviceDataListener, callback, looper, builder);
        this.mBufferDbQuery = new FaxQueryBuilder(context, callback);
        this.mDbTableContractIndex = 21;
    }

    public void onNotificationReceived(ParamNmsNotificationList notification) {
        Log.i(TAG, "onNotificationReceived: " + notification);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        if (notification.mNmsEventList.nmsEvent != null) {
            for (NmsEvent event : notification.mNmsEventList.nmsEvent) {
                if (event.changedObject != null) {
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

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:25:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject r5) {
        /*
            r4 = this;
            java.lang.String r0 = TAG
            java.lang.String r1 = "handleCloudNotifyDeletedObj()"
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r4.mSummaryDB
            java.net.URL r1 = r5.resourceURL
            java.lang.String r1 = r1.toString()
            android.database.Cursor r0 = r0.querySummaryDBwithResUrl(r1)
            if (r0 == 0) goto L_0x0040
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0049 }
            if (r1 == 0) goto L_0x0040
            java.lang.String r1 = "syncaction"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0049 }
            int r1 = r0.getInt(r1)     // Catch:{ all -> 0x0049 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x0049 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0049 }
            if (r1 != r2) goto L_0x003c
            java.lang.String r2 = TAG     // Catch:{ all -> 0x0049 }
            java.lang.String r3 = "this is a deleted object"
            android.util.Log.d(r2, r3)     // Catch:{ all -> 0x0049 }
            if (r0 == 0) goto L_0x003b
            r0.close()
        L_0x003b:
            return
        L_0x003c:
            r4.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(r0, r5)     // Catch:{ all -> 0x0049 }
            goto L_0x0043
        L_0x0040:
            r4.onNmsEventDeletedObjSummaryDbNotAvailableUsingUrl(r5)     // Catch:{ all -> 0x0049 }
        L_0x0043:
            if (r0 == 0) goto L_0x0048
            r0.close()
        L_0x0048:
            return
        L_0x0049:
            r1 = move-exception
            if (r0 == 0) goto L_0x0054
            r0.close()     // Catch:{ all -> 0x0050 }
            goto L_0x0054
        L_0x0050:
            r2 = move-exception
            r1.addSuppressed(r2)
        L_0x0054:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.FaxScheduler.handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject):void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 26 */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0191 A[SYNTHETIC, Splitter:B:43:0x0191] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectFaxMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r27, boolean r28) {
        /*
            r26 = this;
            r10 = r26
            r11 = r27
            java.lang.String r0 = "syncdirection"
            java.lang.String r1 = "syncaction"
            java.lang.String r2 = TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "handleObjectFaxMessageCloudSearch: "
            r3.append(r4)
            r3.append(r11)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
            r12 = -1
            com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder r2 = r10.mBufferDbQuery     // Catch:{ NullPointerException -> 0x019f }
            java.net.URL r3 = r11.resourceURL     // Catch:{ NullPointerException -> 0x019f }
            java.lang.String r3 = r3.toString()     // Catch:{ NullPointerException -> 0x019f }
            android.database.Cursor r2 = r2.queryFaxMessageBufferDBwithResUrl(r3)     // Catch:{ NullPointerException -> 0x019f }
            r14 = r2
            java.net.URL r2 = r11.resourceURL     // Catch:{ all -> 0x018b }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x018b }
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r2)     // Catch:{ all -> 0x018b }
            r15 = r2
            if (r14 == 0) goto L_0x0168
            boolean r2 = r14.moveToFirst()     // Catch:{ all -> 0x018b }
            if (r2 == 0) goto L_0x0168
            java.lang.String r2 = "_bufferdbid"
            int r2 = r14.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x018b }
            int r2 = r14.getInt(r2)     // Catch:{ all -> 0x018b }
            long r8 = (long) r2     // Catch:{ all -> 0x018b }
            java.lang.String r2 = "_id"
            int r2 = r14.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x018b }
            int r2 = r14.getInt(r2)     // Catch:{ all -> 0x018b }
            int r3 = r14.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x018b }
            int r3 = r14.getInt(r3)     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r21 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r3)     // Catch:{ all -> 0x018b }
            int r3 = r14.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x018b }
            int r3 = r14.getInt(r3)     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r20 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r3)     // Catch:{ all -> 0x018b }
            android.content.ContentValues r3 = new android.content.ContentValues     // Catch:{ all -> 0x018b }
            r3.<init>()     // Catch:{ all -> 0x018b }
            r7 = r3
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x018b }
            r3.<init>(r4, r5)     // Catch:{ all -> 0x018b }
            r5 = r3
            r3 = 0
            r5.mIsChanged = r3     // Catch:{ all -> 0x018b }
            java.lang.String r3 = "flagRead"
            com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder r4 = r10.mBufferDbQuery     // Catch:{ all -> 0x018b }
            com.sec.internal.omanetapi.nms.data.FlagList r6 = r11.mFlagList     // Catch:{ all -> 0x018b }
            int r4 = r4.getIfSeenValueUsingFlag(r6)     // Catch:{ all -> 0x018b }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x018b }
            r7.put(r3, r4)     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = r11.mFlag     // Catch:{ all -> 0x018b }
            boolean r3 = r3.equals(r4)     // Catch:{ all -> 0x018b }
            if (r3 == 0) goto L_0x00bd
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r3 = r10.mScheduleRule     // Catch:{ all -> 0x00b7 }
            int r4 = r10.mDbTableContractIndex     // Catch:{ all -> 0x00b7 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r22 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x00b7 }
            r16 = r3
            r17 = r4
            r18 = r8
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = r16.getSetFlagsForCldOperation(r17, r18, r20, r21, r22)     // Catch:{ all -> 0x00b7 }
            r5 = r3
            r22 = r8
            r18 = r12
            r13 = r5
            r12 = r7
            goto L_0x0101
        L_0x00b7:
            r0 = move-exception
            r1 = r0
            r18 = r12
            goto L_0x018f
        L_0x00bd:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = r11.mFlag     // Catch:{ all -> 0x018b }
            boolean r3 = r3.equals(r4)     // Catch:{ all -> 0x018b }
            if (r3 == 0) goto L_0x00e2
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r3 = r10.mScheduleRule     // Catch:{ all -> 0x018b }
            int r4 = r10.mDbTableContractIndex     // Catch:{ all -> 0x018b }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r16 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x018b }
            r17 = r5
            r5 = r8
            r18 = r12
            r12 = r7
            r7 = r20
            r22 = r8
            r8 = r21
            r9 = r16
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = r3.getSetFlagsForCldOperation(r4, r5, r7, r8, r9)     // Catch:{ all -> 0x0188 }
            r5 = r3
            r13 = r5
            goto L_0x0101
        L_0x00e2:
            r17 = r5
            r22 = r8
            r18 = r12
            r12 = r7
            r3 = 1
            if (r2 >= r3) goto L_0x00ff
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r3 = r10.mScheduleRule     // Catch:{ all -> 0x0188 }
            int r4 = r10.mDbTableContractIndex     // Catch:{ all -> 0x0188 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r9 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x0188 }
            r5 = r22
            r7 = r20
            r8 = r21
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = r3.getSetFlagsForCldOperation(r4, r5, r7, r8, r9)     // Catch:{ all -> 0x0188 }
            r5 = r3
            r13 = r5
            goto L_0x0101
        L_0x00ff:
            r13 = r17
        L_0x0101:
            java.lang.String r3 = TAG     // Catch:{ all -> 0x0188 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0188 }
            r4.<init>()     // Catch:{ all -> 0x0188 }
            java.lang.String r5 = "handleObjectFaxMessageCloudSearch: fax found: "
            r4.append(r5)     // Catch:{ all -> 0x0188 }
            r8 = r22
            r4.append(r8)     // Catch:{ all -> 0x0188 }
            java.lang.String r5 = ", mIsChanged: "
            r4.append(r5)     // Catch:{ all -> 0x0188 }
            boolean r5 = r13.mIsChanged     // Catch:{ all -> 0x0188 }
            r4.append(r5)     // Catch:{ all -> 0x0188 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0188 }
            android.util.Log.d(r3, r4)     // Catch:{ all -> 0x0188 }
            boolean r3 = r13.mIsChanged     // Catch:{ all -> 0x0188 }
            if (r3 == 0) goto L_0x015a
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = r13.mAction     // Catch:{ all -> 0x0188 }
            int r3 = r3.getId()     // Catch:{ all -> 0x0188 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0188 }
            r12.put(r1, r3)     // Catch:{ all -> 0x0188 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r13.mDirection     // Catch:{ all -> 0x0188 }
            int r1 = r1.getId()     // Catch:{ all -> 0x0188 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0188 }
            r12.put(r0, r1)     // Catch:{ all -> 0x0188 }
            com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x0188 }
            r10.updateQueryTable(r12, r8, r0)     // Catch:{ all -> 0x0188 }
            r5 = 21
            r6 = 0
            r0 = 0
            r1 = r26
            r16 = r2
            r2 = r13
            r3 = r8
            r7 = r28
            r24 = r8
            r8 = r15
            r9 = r0
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x0188 }
            goto L_0x0165
        L_0x015a:
            r16 = r2
            r24 = r8
            com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x0188 }
            r1 = r24
            r10.updateQueryTable(r12, r1, r0)     // Catch:{ all -> 0x0188 }
        L_0x0165:
            r12 = r18
            goto L_0x017f
        L_0x0168:
            r18 = r12
            java.lang.String r0 = TAG     // Catch:{ all -> 0x0188 }
            java.lang.String r1 = "handleObjectFaxMessageCloudSearch: fax not found: "
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x0188 }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r10.mSummaryDB     // Catch:{ all -> 0x0188 }
            r1 = 21
            r0.insertSummaryDbUsingObjectIfNonExist(r11, r1)     // Catch:{ all -> 0x0188 }
            com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x0188 }
            long r0 = r0.insertFaxMessageUsingObject(r11, r15)     // Catch:{ all -> 0x0188 }
            r12 = r0
        L_0x017f:
            if (r14 == 0) goto L_0x0187
            r14.close()     // Catch:{ NullPointerException -> 0x0185 }
            goto L_0x0187
        L_0x0185:
            r0 = move-exception
            goto L_0x01a2
        L_0x0187:
            goto L_0x01ab
        L_0x0188:
            r0 = move-exception
            r1 = r0
            goto L_0x018f
        L_0x018b:
            r0 = move-exception
            r18 = r12
            r1 = r0
        L_0x018f:
            if (r14 == 0) goto L_0x019a
            r14.close()     // Catch:{ all -> 0x0195 }
            goto L_0x019a
        L_0x0195:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x019b }
        L_0x019a:
            throw r1     // Catch:{ NullPointerException -> 0x019b }
        L_0x019b:
            r0 = move-exception
            r12 = r18
            goto L_0x01a2
        L_0x019f:
            r0 = move-exception
            r18 = r12
        L_0x01a2:
            java.lang.String r1 = TAG
            java.lang.String r2 = r0.toString()
            android.util.Log.e(r1, r2)
        L_0x01ab:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.FaxScheduler.handleObjectFaxMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject, boolean):long");
    }

    private void onNmsEventDeletedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, DeletedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventDeletedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor clgCs = this.mBufferDbQuery.queryFaxMessageBufferDBwithResUrl(objt.resourceURL.toString());
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
                        this.mBufferDbQuery.notifyApplication(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", (long) rowid);
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

    private void onNmsEventDeletedObjSummaryDbNotAvailableUsingUrl(DeletedObject objt) {
        this.mSummaryDB.insertNmsEventDeletedObjToSummaryDB(objt, 21);
    }

    private void onNmsEventChangedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, ChangedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventChangedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor clgCs = this.mBufferDbQuery.queryFaxMessageBufferDBwithResUrl(objt.resourceURL.toString());
        if (clgCs != null) {
            try {
                if (clgCs.moveToFirst()) {
                    int rowid = clgCs.getInt(clgCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    int seen = clgCs.getInt(clgCs.getColumnIndexOrThrow("flagRead"));
                    CloudMessageBufferDBConstants.ActionStatusFlag cldAction = this.mBufferDbQuery.getCloudActionPerFlag(objt.flags);
                    if (seen != 1 && CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(cldAction)) {
                        ContentValues cv = new ContentValues();
                        cv.put("flagRead", 1);
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
                        updateQueryTable(cv, (long) rowid, this.mBufferDbQuery);
                        this.mBufferDbQuery.notifyApplication(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", (long) rowid);
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

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0062 A[SYNTHETIC, Splitter:B:15:0x0062] */
    /* JADX WARNING: Removed duplicated region for block: B:29:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNmsEventChangedObjSummaryDbNotAvailableUsingUrl(com.sec.internal.omanetapi.nms.data.ChangedObject r11, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r12) {
        /*
            r10 = this;
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r10.mSummaryDB
            r1 = 21
            r0.insertNmsEventChangedObjToSummaryDB(r11, r1)
            java.net.URL r0 = r11.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.TmoGcmMessage r1 = r11.extendedMessage     // Catch:{ NullPointerException -> 0x0072 }
            java.lang.String r1 = r1.client_correlator     // Catch:{ NullPointerException -> 0x0072 }
            if (r1 != 0) goto L_0x002d
            com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ NullPointerException -> 0x0072 }
            long r3 = r1.insertNewFaxUsingChangedObject(r11, r0)     // Catch:{ NullPointerException -> 0x0072 }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r7 = r12.mChangelst     // Catch:{ NullPointerException -> 0x0072 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r8 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ NullPointerException -> 0x0072 }
            r2 = 21
            r5 = 0
            r1 = r8
            r6 = r0
            r1.<init>(r2, r3, r5, r6)     // Catch:{ NullPointerException -> 0x0072 }
            r7.add(r8)     // Catch:{ NullPointerException -> 0x0072 }
            return
        L_0x002d:
            com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ NullPointerException -> 0x0072 }
            com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.TmoGcmMessage r2 = r11.extendedMessage     // Catch:{ NullPointerException -> 0x0072 }
            java.lang.String r2 = r2.client_correlator     // Catch:{ NullPointerException -> 0x0072 }
            android.database.Cursor r1 = r1.queryFaxMessageBufferDBwithClientCorrelator(r2)     // Catch:{ NullPointerException -> 0x0072 }
            r7 = r1
            if (r7 == 0) goto L_0x004b
            boolean r1 = r7.moveToFirst()     // Catch:{ all -> 0x0066 }
            if (r1 == 0) goto L_0x004b
            java.lang.String r1 = TAG     // Catch:{ all -> 0x0066 }
            java.lang.String r2 = "handleCloudNotifyChangedObj: changeObj exist"
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0066 }
            r10.handleExistingFaxUsingClientCorrelator(r7, r11)     // Catch:{ all -> 0x0066 }
            goto L_0x0060
        L_0x004b:
            com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x0066 }
            long r3 = r1.insertNewFaxUsingChangedObject(r11, r0)     // Catch:{ all -> 0x0066 }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r8 = r12.mChangelst     // Catch:{ all -> 0x0066 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r9 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x0066 }
            r2 = 21
            r5 = 0
            r1 = r9
            r6 = r0
            r1.<init>(r2, r3, r5, r6)     // Catch:{ all -> 0x0066 }
            r8.add(r9)     // Catch:{ all -> 0x0066 }
        L_0x0060:
            if (r7 == 0) goto L_0x0065
            r7.close()     // Catch:{ NullPointerException -> 0x0072 }
        L_0x0065:
            goto L_0x008e
        L_0x0066:
            r1 = move-exception
            if (r7 == 0) goto L_0x0071
            r7.close()     // Catch:{ all -> 0x006d }
            goto L_0x0071
        L_0x006d:
            r2 = move-exception
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x0072 }
        L_0x0071:
            throw r1     // Catch:{ NullPointerException -> 0x0072 }
        L_0x0072:
            r1 = move-exception
            java.lang.String r2 = TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "onNmsEventChangedObjSummaryDbNotAvailableUsingUrl"
            r3.append(r4)
            java.lang.String r4 = r1.toString()
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.e(r2, r3)
        L_0x008e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.FaxScheduler.onNmsEventChangedObjSummaryDbNotAvailableUsingUrl(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    private void onFaxSendingSuccess(ParamOMAresponseforBufDB paramOMAObj) {
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put(CloudMessageProviderContract.FAXMessages.DELIVER_STATUS, Integer.valueOf(CloudMessageBufferDBConstants.FaxDeliveryStatus.PENDING.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObj.getReference().resourceURL.toString()));
        updateQueryTable(cv, paramOMAObj.getBufferDBChangeParam().mRowId, this.mBufferDbQuery);
        this.mBufferDbQuery.notifyApplication(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", paramOMAObj.getBufferDBChangeParam().mRowId);
    }

    private void onFaxSendingFailure(ParamOMAresponseforBufDB paramOMAObj) {
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.FAILURE.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        updateQueryTable(cv, paramOMAObj.getBufferDBChangeParam().mRowId, this.mBufferDbQuery);
        this.mBufferDbQuery.notifyApplication(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", paramOMAObj.getBufferDBChangeParam().mRowId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:25:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject r5, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r6) {
        /*
            r4 = this;
            java.lang.String r0 = TAG
            java.lang.String r1 = "handleCloudNotifyChangedObj()"
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r4.mSummaryDB
            java.net.URL r1 = r5.resourceURL
            java.lang.String r1 = r1.toString()
            android.database.Cursor r0 = r0.querySummaryDBwithResUrl(r1)
            if (r0 == 0) goto L_0x0040
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0049 }
            if (r1 == 0) goto L_0x0040
            java.lang.String r1 = "syncaction"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0049 }
            int r1 = r0.getInt(r1)     // Catch:{ all -> 0x0049 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x0049 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0049 }
            if (r1 != r2) goto L_0x003c
            java.lang.String r2 = TAG     // Catch:{ all -> 0x0049 }
            java.lang.String r3 = "this is a deleted object"
            android.util.Log.i(r2, r3)     // Catch:{ all -> 0x0049 }
            if (r0 == 0) goto L_0x003b
            r0.close()
        L_0x003b:
            return
        L_0x003c:
            r4.onNmsEventChangedObjSummaryDbAvailableUsingUrl(r0, r5)     // Catch:{ all -> 0x0049 }
            goto L_0x0043
        L_0x0040:
            r4.onNmsEventChangedObjSummaryDbNotAvailableUsingUrl(r5, r6)     // Catch:{ all -> 0x0049 }
        L_0x0043:
            if (r0 == 0) goto L_0x0048
            r0.close()
        L_0x0048:
            return
        L_0x0049:
            r1 = move-exception
            if (r0 == 0) goto L_0x0054
            r0.close()     // Catch:{ all -> 0x0050 }
            goto L_0x0054
        L_0x0050:
            r2 = move-exception
            r1.addSuppressed(r2)
        L_0x0054:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.FaxScheduler.handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    private void handleExistingFaxUsingClientCorrelator(Cursor cs, ChangedObject object) {
        Cursor cursor = cs;
        ChangedObject changedObject = object;
        String str = TAG;
        Log.i(str, "handleExistingFaxUsingClientCorrelator, status: " + changedObject.extendedMessage.status);
        if (changedObject.extendedMessage.status.contains("Delivered")) {
            String[] selectionArgs = {changedObject.extendedMessage.client_correlator};
            long rowid = cursor.getLong(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
            ContentValues cv = new ContentValues();
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, changedObject.resourceURL.toString());
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, changedObject.correlationId);
            cv.put(CloudMessageProviderContract.FAXMessages.DELIVER_STATUS, Integer.valueOf(CloudMessageBufferDBConstants.FaxDeliveryStatus.DELIVERED.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
            this.mBufferDbQuery.updateTable(21, cv, "transaction_id=?", selectionArgs);
            this.mBufferDbQuery.notifyApplication(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", rowid);
        } else if (changedObject.extendedMessage.status.contains("Failed")) {
            String[] selectionArgs2 = {changedObject.extendedMessage.client_correlator};
            long rowid2 = cursor.getLong(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
            ContentValues cv2 = new ContentValues();
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, changedObject.resourceURL.toString());
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, changedObject.correlationId);
            cv2.put(CloudMessageProviderContract.FAXMessages.DELIVER_STATUS, Integer.valueOf(CloudMessageBufferDBConstants.FaxDeliveryStatus.FAILURE.getId()));
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
            this.mBufferDbQuery.updateTable(21, cv2, "transaction_id=?", selectionArgs2);
            this.mBufferDbQuery.notifyApplication(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", rowid2);
        }
    }

    public void sendFax(ParamAppJsonValue param) {
        ArrayList<ContentValues> values = this.mBufferDbQuery.getFaxDataFromTelephony((long) param.mRowId);
        if (values != null && values.size() > 0) {
            values.get(0).put("_id", Integer.valueOf(param.mRowId));
            values.get(0).put("sender", param.mLine);
            values.get(0).put("direction", Integer.valueOf(ImDirection.OUTGOING.getId()));
            values.get(0).put("flagRead", 1);
            values.get(0).put("error_message", "sending fax");
            values.get(0).put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.PENDING.getId()));
            values.get(0).put("linenum", param.mLine);
            values.get(0).put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
            values.get(0).put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
            long rowid = this.mBufferDbQuery.insertFaxtoBufferDB(values.get(0));
            BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
            downloadlist.mChangelst.add(new BufferDBChangeParam(param.mDataContractType, rowid, false, param.mLine));
            this.mDeviceDataChangeListener.sendDeviceFax(downloadlist);
        }
    }

    public void reSendFax(ParamAppJsonValue param, int buffDbId) {
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.PENDING.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        updateQueryTable(cv, (long) buffDbId, this.mBufferDbQuery);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        downloadlist.mChangelst.add(new BufferDBChangeParam(param.mDataContractType, (long) buffDbId, false, param.mLine));
        this.mDeviceDataChangeListener.sendDeviceFax(downloadlist);
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.FaxScheduler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;

        static {
            int[] iArr = new int[CloudMessageBufferDBConstants.MsgOperationFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag = iArr;
            try {
                iArr[CloudMessageBufferDBConstants.MsgOperationFlag.Upload.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Read.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Delete.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public void onAppOperationReceived(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        String str = TAG;
        Log.d(str, "onAppOperationReceived: " + param);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[param.mOperation.ordinal()];
        if (i == 1) {
            handlesendFax(param);
        } else if (i == 2) {
            handlereadFax(param, changelist);
        } else if (i == 3) {
            handledeleteFax(param, changelist);
        }
    }

    private void handledeleteFax(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        Throwable th;
        ParamAppJsonValue paramAppJsonValue = param;
        Cursor cs = this.mBufferDbQuery.queryFaxMessageBufferDBwithAppId((long) paramAppJsonValue.mRowId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    ContentValues cv = new ContentValues();
                    long bufferDbId = cs.getLong(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                    CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                    String filePath = cs.getString(cs.getColumnIndex("file_path"));
                    int faxdirection = cs.getInt(cs.getColumnIndex("direction"));
                    if (ImDirection.INCOMING.getId() == faxdirection) {
                        deleteFilePath(filePath);
                    }
                    String line = cs.getString(cs.getColumnIndexOrThrow("linenum"));
                    ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForMsgOperation(this.mDbTableContractIndex, bufferDbId, direction, action, CloudMessageBufferDBConstants.MsgOperationFlag.Delete);
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
                    updateQueryTable(cv, bufferDbId, this.mBufferDbQuery);
                    if (flagSet.mIsChanged) {
                        int i = faxdirection;
                        long j = bufferDbId;
                        String str = filePath;
                        handleOutPutParamSyncFlagSet(flagSet, bufferDbId, paramAppJsonValue.mDataContractType, false, false, line, changelist);
                    } else {
                        long j2 = bufferDbId;
                        String str2 = filePath;
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

    private void deleteFilePath(String path) {
        String str = TAG;
        Log.i(str, "deleteFilePath: " + path);
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists() && file.delete()) {
                String str2 = TAG;
                Log.i(str2, "deleteFilePath(): " + path);
            }
        }
    }

    private void handlereadFax(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        Throwable th;
        ParamAppJsonValue paramAppJsonValue = param;
        Cursor cs = this.mBufferDbQuery.queryFaxMessageBufferDBwithAppId((long) paramAppJsonValue.mRowId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    ContentValues cv = new ContentValues();
                    long bufferDbId = cs.getLong(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                    CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                    String line = cs.getString(cs.getColumnIndexOrThrow("linenum"));
                    ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForMsgOperation(this.mDbTableContractIndex, bufferDbId, direction, action, CloudMessageBufferDBConstants.MsgOperationFlag.Read);
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
                    updateQueryTable(cv, bufferDbId, this.mBufferDbQuery);
                    if (flagSet.mIsChanged) {
                        long j = bufferDbId;
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

    /* JADX WARNING: Removed duplicated region for block: B:17:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0024  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handlesendFax(com.sec.internal.ims.cmstore.params.ParamAppJsonValue r4) {
        /*
            r3 = this;
            com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder r0 = r3.mBufferDbQuery
            int r1 = r4.mRowId
            long r1 = (long) r1
            android.database.Cursor r0 = r0.queryFaxMessageBufferDBwithAppId(r1)
            if (r0 == 0) goto L_0x001f
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0028 }
            if (r1 == 0) goto L_0x001f
            java.lang.String r1 = "_bufferdbid"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0028 }
            int r1 = r0.getInt(r1)     // Catch:{ all -> 0x0028 }
            r3.reSendFax(r4, r1)     // Catch:{ all -> 0x0028 }
            goto L_0x0022
        L_0x001f:
            r3.sendFax(r4)     // Catch:{ all -> 0x0028 }
        L_0x0022:
            if (r0 == 0) goto L_0x0027
            r0.close()
        L_0x0027:
            return
        L_0x0028:
            r1 = move-exception
            if (r0 == 0) goto L_0x0033
            r0.close()     // Catch:{ all -> 0x002f }
            goto L_0x0033
        L_0x002f:
            r2 = move-exception
            r1.addSuppressed(r2)
        L_0x0033:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.FaxScheduler.handlesendFax(com.sec.internal.ims.cmstore.params.ParamAppJsonValue):void");
    }

    public void handleNormalSyncDownloadedFaxMessage(ParamOMAObject para) {
        String str = TAG;
        Log.i(str, "handleNormalSyncDownloadedFaxMessage: " + para);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        Cursor mmsCs = this.mBufferDbQuery.queryFaxMessageBufferDBwithResUrl(para.resourceURL.toString());
        if (mmsCs != null) {
            try {
                if (mmsCs.moveToFirst()) {
                    int rowid = mmsCs.getInt(mmsCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    ContentValues cv = new ContentValues();
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, para.payloadURL.toString());
                    String line = Util.getLineTelUriFromObjUrl(para.resourceURL.toString());
                    updateQueryTable(cv, (long) rowid, this.mBufferDbQuery);
                    downloadlist.mChangelst.add(new BufferDBChangeParam(21, (long) rowid, false, line));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (mmsCs != null) {
            mmsCs.close();
        }
        if (downloadlist.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(downloadlist);
            return;
        }
        return;
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 20 */
    public void onFaxAllPayloadDownloaded(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync) {
        Throwable th;
        String filename;
        ContentValues cv;
        InputStream inputStream;
        Throwable th2;
        ParamOMAresponseforBufDB paramOMAresponseforBufDB = para;
        String str = TAG;
        Log.d(str, "onFaxAllPayloadDownloaded: " + paramOMAresponseforBufDB);
        if (paramOMAresponseforBufDB != null && para.getAllPayloads() != null && para.getAllPayloads().size() >= 1) {
            try {
                Cursor csfax = this.mBufferDbQuery.queryTablewithBufferDbId(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId);
                if (csfax != null) {
                    try {
                        if (csfax.moveToFirst()) {
                            int id = csfax.getInt(csfax.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                            String line = csfax.getString(csfax.getColumnIndexOrThrow("linenum"));
                            String filename2 = para.getAllPayloads().get(0).getFileName();
                            if (TextUtils.isEmpty(filename2)) {
                                filename = Util.getFileNamefromContentType(para.getAllPayloads().get(0).getDisposition());
                            } else {
                                filename = filename2;
                            }
                            if (TextUtils.isEmpty(filename)) {
                                Log.e(TAG, "onFaxPayloadDownloaded: no file name");
                                if (csfax != null) {
                                    csfax.close();
                                    return;
                                }
                                return;
                            }
                            String filepath = Util.generateUniqueFilePath(this.mContext, filename, false);
                            String filepath2 = TAG;
                            Log.d(filepath2, "generated file path: " + filepath);
                            cv = new ContentValues();
                            inputStream = para.getAllPayloads().get(0).getInputStream();
                            long totalsaved = Util.saveInputStreamtoPath(inputStream, filepath);
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            String str2 = TAG;
                            Log.i(str2, "total filesize: " + para.getAllPayloads().get(0).getSize());
                            cv.put("file_name", filename);
                            cv.put("file_path", filepath);
                            cv.put("file_size", Long.valueOf(totalsaved));
                            ParamSyncFlagsSet param = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice, CloudMessageBufferDBConstants.ActionStatusFlag.Insert);
                            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(param.mAction.getId()));
                            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(param.mDirection.getId()));
                            updateQueryTable(cv, (long) id, this.mBufferDbQuery);
                            ContentValues contentValues = cv;
                            handleOutPutParamSyncFlagSet(param, para.getBufferDBChangeParam().mRowId, 21, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (csfax != null) {
                            csfax.close();
                        }
                        throw th;
                    }
                }
                if (csfax != null) {
                    csfax.close();
                    return;
                }
                return;
            } catch (IOException | NullPointerException | MessagingException e) {
                e.printStackTrace();
                return;
            } catch (Throwable th4) {
                th.addSuppressed(th4);
            }
        } else {
            return;
        }
        throw th2;
    }

    public void handleSendFaxMessageResponse(ParamOMAresponseforBufDB paramOMAObj, boolean isSuccess) {
        String str = TAG;
        Log.d(str, "handleSendFaxMessageResponse: " + paramOMAObj + " isSuccess: " + isSuccess);
        if (!isSuccess || paramOMAObj.getReference().resourceURL == null) {
            onFaxSendingFailure(paramOMAObj);
        } else {
            onFaxSendingSuccess(paramOMAObj);
        }
    }

    public void onUpdateFromDeviceMsgAppFetch(DeviceMsgAppFetchUpdateParam para, boolean mIsGoforwardSync) {
        onUpdateFromDeviceMsgAppFetch(para, mIsGoforwardSync, this.mBufferDbQuery);
    }

    public Cursor queryToDeviceUnDownloadedFax(String linenum) {
        return this.mBufferDbQuery.queryToDeviceUnDownloadedFax(linenum);
    }

    public Cursor queryFaxMessageBufferDBwithResUrl(String url) {
        return this.mBufferDbQuery.queryFaxMessageBufferDBwithResUrl(url);
    }

    public void notifyMsgAppDeleteFail(int dbIndex, long bufferDbId, String line) {
        String str = TAG;
        Log.i(str, "notifyMsgAppDeleteFail, dbIndex: " + dbIndex + " bufferDbId: " + bufferDbId + " line: " + IMSLog.checker(line));
        if (dbIndex == 21) {
            JsonArray jsonArrayRowIds = new JsonArray();
            JsonObject jsobjct = new JsonObject();
            jsobjct.addProperty("id", String.valueOf(bufferDbId));
            jsonArrayRowIds.add(jsobjct);
            this.mCallbackMsgApp.notifyAppCloudDeleteFail(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", jsonArrayRowIds.toString());
        }
    }

    public void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync) {
        onCloudUpdateFlagSuccess(para, mIsGoforwardSync, this.mBufferDbQuery);
    }

    public void wipeOutData(int tableindex, String line) {
        this.mBufferDbQuery.wipeOutIncomingFaxAttachement(tableindex, line);
        this.mBufferDbQuery.deleteAllUsingLineAndTableIndex(tableindex, line);
        this.mSummaryDB.deleteAllUsingLineAndTableIndex(tableindex, line);
    }
}
