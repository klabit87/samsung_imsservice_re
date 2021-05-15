package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceIMFTUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamNmsNotificationList;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.NmsEvent;
import java.io.File;

public class RcsScheduler extends RcsSchedulerHelper {
    /* access modifiers changed from: private */
    public static final String TAG = RcsScheduler.class.getSimpleName();
    private RcsDbSessionObserver mRcsDbSessionObserver = null;

    public RcsScheduler(Context context, CloudMessageBufferDBEventSchedulingRule rule, SummaryQueryBuilder builder, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, MmsScheduler mmsScheduler, SmsScheduler smsScheduler, Looper looper) {
        super(context, rule, builder, deviceDataListener, callback, mmsScheduler, smsScheduler, looper);
        registerRcsDbSessionObserver(looper);
    }

    private long insertRcsMessageUseNmsEventwithMessageContent(ParamOMAObject objt, boolean mIsGoforwardSync) {
        String chatId = this.mBufferDbQuery.searchOrCreateSession(objt);
        if (chatId == null) {
            return -1;
        }
        long rowId = this.mBufferDbQuery.insertRCSMessageToBufferDBUsingObject(objt, chatId, false).mBufferId;
        if (objt.mObjectType == 11) {
            notifyMsgAppCldNotification(getAppTypeString(objt.mObjectType), getMessageTypeString(objt.mObjectType, false), rowId);
        } else if (objt.mObjectType == 12) {
            BufferDBChangeParamList list = new BufferDBChangeParamList();
            list.mChangelst.add(new BufferDBChangeParam(1, rowId, mIsGoforwardSync, objt.mLine));
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(list);
        }
        return rowId;
    }

    /* Debug info: failed to restart local var, previous not found, register: 21 */
    private long handleObjectFtDownloadWithThmbEnabled(ParamOMAObject objt) {
        Cursor cs;
        Throwable th;
        ParamOMAObject paramOMAObject = objt;
        long bufferDbId = -1;
        try {
            cs = this.mBufferDbQuery.queryRCSBufferDBwithResUrl(paramOMAObject.resourceURL.toString());
            String line = Util.getLineTelUriFromObjUrl(paramOMAObject.resourceURL.toString());
            if (cs != null && cs.moveToFirst()) {
                bufferDbId = (long) cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                int _id = cs.getInt(cs.getColumnIndexOrThrow("_id"));
                String str = TAG;
                Log.d(str, "handleObjectFtDownloadWithThmbEnabled find bufferDB: " + paramOMAObject.correlationId + " bufferid: " + bufferDbId + " _id: " + _id);
                String selectUpdate = "_bufferdbid=?";
                String[] selectionArgsUpdate = {String.valueOf(bufferDbId)};
                ContentValues cv = new ContentValues();
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, paramOMAObject.payloadURL.toString());
                cv.put("content_type", paramOMAObject.CONTENT_TYPE);
                ContentValues payloadcv = this.mBufferDbQuery.handlePayloadParts(paramOMAObject.payloadPart, paramOMAObject.CONTENT_TYPE);
                cv.putAll(payloadcv);
                String str2 = TAG;
                Log.i(str2, "payloadcv: " + payloadcv);
                if (payloadcv.containsKey(ImContract.CsSession.THUMBNAIL_PATH)) {
                    ParamSyncFlagsSet flagSet = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice, CloudMessageBufferDBConstants.ActionStatusFlag.Insert);
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
                    this.mBufferDbQuery.updateTable(1, cv, selectUpdate, selectionArgsUpdate);
                    this.mBufferDbQuery.updateRCSMessageDb(_id, cv);
                    notifyMsgAppCldNotification(getAppTypeString(1), getMessageTypeString(1, true), bufferDbId);
                } else {
                    ParamSyncFlagsSet flagSet2 = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Downloading, CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad);
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet2.mDirection.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet2.mAction.getId()));
                    this.mBufferDbQuery.updateTable(1, cv, selectUpdate, selectionArgsUpdate);
                    BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
                    BufferDBChangeParam bufferDBChangeParam = r6;
                    ParamSyncFlagsSet paramSyncFlagsSet = flagSet2;
                    BufferDBChangeParam bufferDBChangeParam2 = new BufferDBChangeParam(1, bufferDbId, false, line);
                    downloadlist.mChangelst.add(bufferDBChangeParam);
                    this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(downloadlist);
                }
            }
            if (cs != null) {
                cs.close();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
        return bufferDbId;
        throw th;
    }

    private void updateSyncFlag(int _id, boolean mIsGoforwardSync, String line, long bufferDbId, ParamSyncFlagsSet flagSet, ContentValues cv, boolean payloadPart) {
        ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
        if (_id > 0) {
            if (!paramSyncFlagsSet.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) && !paramSyncFlagsSet.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                ContentValues contentValues = cv;
            } else if (paramSyncFlagsSet.mAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
                this.mBufferDbQuery.updateRCSMessageDb(_id, cv);
            } else {
                ContentValues contentValues2 = cv;
                if (paramSyncFlagsSet.mAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                    this.mBufferDbQuery.deleteRCSMessageDb(_id);
                }
            }
            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, 1, payloadPart, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
            return;
        }
        ContentValues contentValues3 = cv;
    }

    private void updateSyncDirection(ContentValues cv, ParamSyncFlagsSet flagSet, String date, String text) {
        if (TextUtils.isEmpty(text) && CloudMessageStrategyManager.getStrategy().isSupportAtt72HoursRule() && Util.isOver72Hours(date)) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
        } else if (flagSet.mIsChanged) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 26 */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x012a A[Catch:{ all -> 0x0150 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x012d A[Catch:{ all -> 0x0150 }] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01d0 A[Catch:{ all -> 0x0216 }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x020e  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0212 A[SYNTHETIC, Splitter:B:61:0x0212] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x021a A[SYNTHETIC, Splitter:B:66:0x021a] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleNormalSyncObjectRcsMessageDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject r27, boolean r28) {
        /*
            r26 = this;
            r10 = r26
            r11 = r27
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleNormalSyncObjectRcsMessageDownload: "
            r1.append(r2)
            r1.append(r11)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()
            boolean r0 = r0.isThumbNailEnabledForRcsFT()
            if (r0 == 0) goto L_0x002f
            int r0 = r11.mObjectType
            r1 = 11
            if (r0 == r1) goto L_0x002f
            long r0 = r26.handleObjectFtDownloadWithThmbEnabled(r27)
            return r0
        L_0x002f:
            r12 = -1
            java.net.URL r0 = r11.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r14 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0226 }
            java.lang.String r1 = r11.correlationId     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0226 }
            android.database.Cursor r0 = r0.searchIMFTBufferUsingImdn(r1, r14)     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0226 }
            r15 = r0
            r0 = -1
            r9 = 0
            r8 = 1
            if (r15 == 0) goto L_0x0155
            boolean r2 = r15.moveToFirst()     // Catch:{ all -> 0x0150 }
            if (r2 == 0) goto L_0x0155
            java.lang.String r2 = "_bufferdbid"
            int r2 = r15.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x0150 }
            int r2 = r15.getInt(r2)     // Catch:{ all -> 0x0150 }
            long r5 = (long) r2     // Catch:{ all -> 0x0150 }
            java.lang.String r0 = "_id"
            int r0 = r15.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0150 }
            int r2 = r15.getInt(r0)     // Catch:{ all -> 0x0150 }
            java.lang.String r0 = "syncaction"
            int r0 = r15.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0150 }
            int r0 = r15.getInt(r0)     // Catch:{ all -> 0x0150 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r0)     // Catch:{ all -> 0x0150 }
            java.lang.String r1 = "syncdirection"
            int r1 = r15.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0150 }
            int r1 = r15.getInt(r1)     // Catch:{ all -> 0x0150 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r1)     // Catch:{ all -> 0x0150 }
            java.lang.String r3 = "_bufferdbid=?"
            r7 = r3
            java.lang.String[] r3 = new java.lang.String[r8]     // Catch:{ all -> 0x0150 }
            java.lang.String r4 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x0150 }
            r3[r9] = r4     // Catch:{ all -> 0x0150 }
            r4 = r3
            android.content.ContentValues r3 = new android.content.ContentValues     // Catch:{ all -> 0x0150 }
            r3.<init>()     // Catch:{ all -> 0x0150 }
            java.lang.String r8 = "lastmodseq"
            java.lang.Long r9 = r11.lastModSeq     // Catch:{ all -> 0x0150 }
            r3.put(r8, r9)     // Catch:{ all -> 0x0150 }
            java.lang.String r8 = "res_url"
            java.net.URL r9 = r11.resourceURL     // Catch:{ all -> 0x0150 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0150 }
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r9)     // Catch:{ all -> 0x0150 }
            r3.put(r8, r9)     // Catch:{ all -> 0x0150 }
            java.lang.String r8 = "parentfolder"
            java.net.URL r9 = r11.parentFolder     // Catch:{ all -> 0x0150 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0150 }
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r9)     // Catch:{ all -> 0x0150 }
            r3.put(r8, r9)     // Catch:{ all -> 0x0150 }
            java.lang.String r8 = "path"
            java.lang.String r9 = r11.path     // Catch:{ all -> 0x0150 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0150 }
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r9)     // Catch:{ all -> 0x0150 }
            r3.put(r8, r9)     // Catch:{ all -> 0x0150 }
            java.lang.String r8 = "content_type"
            java.lang.String r9 = r11.CONTENT_TYPE     // Catch:{ all -> 0x0150 }
            r3.put(r8, r9)     // Catch:{ all -> 0x0150 }
            java.lang.String r8 = "status"
            int r8 = r15.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x0150 }
            int r8 = r15.getInt(r8)     // Catch:{ all -> 0x0150 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r9 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x0150 }
            int r9 = r9.getId()     // Catch:{ all -> 0x0150 }
            if (r8 == r9) goto L_0x00fb
            java.lang.String r8 = "ft_status"
            int r8 = r15.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x0150 }
            int r8 = r15.getInt(r8)     // Catch:{ all -> 0x0150 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r9 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x0150 }
            int r9 = r9.getId()     // Catch:{ all -> 0x0150 }
            if (r8 != r9) goto L_0x00f8
            goto L_0x00fb
        L_0x00f8:
            r24 = r1
            goto L_0x0103
        L_0x00fb:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x0150 }
            r0 = r8
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud     // Catch:{ all -> 0x0150 }
            r1 = r8
            r24 = r1
        L_0x0103:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r1 = r10.mScheduleRule     // Catch:{ all -> 0x0150 }
            int r8 = r10.mDbTableContractIndex     // Catch:{ all -> 0x0150 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r9 = r11.mFlag     // Catch:{ all -> 0x0150 }
            r16 = r1
            r17 = r8
            r18 = r5
            r20 = r24
            r21 = r0
            r22 = r9
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r1 = r16.getSetFlagsForCldOperation(r17, r18, r20, r21, r22)     // Catch:{ all -> 0x0150 }
            r9 = r1
            java.lang.String r1 = r11.DATE     // Catch:{ all -> 0x0150 }
            java.lang.String r8 = r11.TEXT_CONTENT     // Catch:{ all -> 0x0150 }
            r10.updateSyncDirection(r3, r9, r1, r8)     // Catch:{ all -> 0x0150 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x0150 }
            r1.updateRCSMessageInBufferDBUsingObject(r11, r3, r7, r4)     // Catch:{ all -> 0x0150 }
            com.sec.internal.omanetapi.nms.data.PayloadPartInfo[] r1 = r11.payloadPart     // Catch:{ all -> 0x0150 }
            if (r1 == 0) goto L_0x012d
            r16 = 1
            goto L_0x012f
        L_0x012d:
            r16 = 0
        L_0x012f:
            r1 = r26
            r17 = r3
            r3 = r28
            r18 = r4
            r4 = r14
            r19 = r5
            r21 = r7
            r7 = r9
            r22 = r0
            r0 = 1
            r8 = r17
            r23 = r9
            r9 = r16
            r1.updateSyncFlag(r2, r3, r4, r5, r7, r8, r9)     // Catch:{ all -> 0x0150 }
            r9 = r28
            r0 = r19
            r5 = 0
            goto L_0x01be
        L_0x0150:
            r0 = move-exception
            r9 = r28
            goto L_0x0217
        L_0x0155:
            r1 = r0
            r0 = r8
            r9 = r28
            int r3 = r10.handleObjectDownloadCrossSearch(r11, r14, r9)     // Catch:{ all -> 0x0216 }
            java.lang.String r4 = TAG     // Catch:{ all -> 0x0216 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0216 }
            r5.<init>()     // Catch:{ all -> 0x0216 }
            java.lang.String r6 = "handleNormalSyncObjectRcsMessageDownload: RCS not foundcontractTypeFromLegacy: "
            r5.append(r6)     // Catch:{ all -> 0x0216 }
            r5.append(r3)     // Catch:{ all -> 0x0216 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0216 }
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x0216 }
            r4 = -1
            if (r3 == r0) goto L_0x017e
            if (r15 == 0) goto L_0x017d
            r15.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0224 }
        L_0x017d:
            return r4
        L_0x017e:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r10.mBufferDbQuery     // Catch:{ all -> 0x0216 }
            java.lang.String r6 = r6.searchOrCreateSession(r11)     // Catch:{ all -> 0x0216 }
            if (r6 != 0) goto L_0x018d
            if (r15 == 0) goto L_0x018c
            r15.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0224 }
        L_0x018c:
            return r4
        L_0x018d:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r10.mBufferDbQuery     // Catch:{ all -> 0x0216 }
            r5 = 0
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r4 = r4.insertRCSMessageToBufferDBUsingObject(r11, r6, r5)     // Catch:{ all -> 0x0216 }
            long r7 = r4.mBufferId     // Catch:{ all -> 0x0216 }
            r1 = r7
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r7 = r10.mSummaryDB     // Catch:{ all -> 0x0216 }
            r7.insertSummaryDbUsingObjectIfNonExist(r11, r0)     // Catch:{ all -> 0x0216 }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r7 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ all -> 0x0216 }
            boolean r7 = r7.alwaysInsertMsgWhenNonExist()     // Catch:{ all -> 0x0216 }
            if (r7 == 0) goto L_0x01ba
            int r7 = r11.mObjectType     // Catch:{ all -> 0x0216 }
            java.lang.String r7 = r10.getAppTypeString(r7)     // Catch:{ all -> 0x0216 }
            int r8 = r11.mObjectType     // Catch:{ all -> 0x0216 }
            java.lang.String r8 = r10.getMessageTypeString(r8, r5)     // Catch:{ all -> 0x0216 }
            r16 = r1
            long r0 = r4.mBufferId     // Catch:{ all -> 0x0216 }
            r10.notifyMsgAppCldNotification(r7, r8, r0)     // Catch:{ all -> 0x0216 }
            goto L_0x01bc
        L_0x01ba:
            r16 = r1
        L_0x01bc:
            r0 = r16
        L_0x01be:
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r2 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ all -> 0x0216 }
            boolean r2 = r2.isSupportAtt72HoursRule()     // Catch:{ all -> 0x0216 }
            if (r2 == 0) goto L_0x020e
            java.lang.String r2 = r11.DATE     // Catch:{ all -> 0x0216 }
            boolean r2 = com.sec.internal.ims.cmstore.utils.Util.isOver72Hours(r2)     // Catch:{ all -> 0x0216 }
            if (r2 == 0) goto L_0x020e
            java.lang.String r2 = r11.TEXT_CONTENT     // Catch:{ all -> 0x0216 }
            boolean r2 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x0216 }
            if (r2 != 0) goto L_0x01e7
            int r2 = r11.mObjectType     // Catch:{ all -> 0x0216 }
            java.lang.String r2 = r10.getAppTypeString(r2)     // Catch:{ all -> 0x0216 }
            r3 = 1
            java.lang.String r3 = r10.getMessageTypeString(r3, r5)     // Catch:{ all -> 0x0216 }
            r10.notifyMsgAppCldNotification(r2, r3, r0)     // Catch:{ all -> 0x0216 }
            goto L_0x0210
        L_0x01e7:
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r2 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList     // Catch:{ all -> 0x0216 }
            r2.<init>()     // Catch:{ all -> 0x0216 }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r8 = r2.mChangelst     // Catch:{ all -> 0x0216 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r7 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x0216 }
            r4 = 1
            java.lang.String r5 = r11.mLine     // Catch:{ all -> 0x0216 }
            r3 = r7
            r16 = r5
            r5 = r0
            r25 = r7
            r7 = r28
            r17 = r0
            r0 = r8
            r8 = r16
            r3.<init>(r4, r5, r7, r8)     // Catch:{ all -> 0x0216 }
            r1 = r25
            r0.add(r1)     // Catch:{ all -> 0x0216 }
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r0 = r10.mDeviceDataChangeListener     // Catch:{ all -> 0x0216 }
            r0.sendDeviceNormalSyncDownload(r2)     // Catch:{ all -> 0x0216 }
            goto L_0x0210
        L_0x020e:
            r17 = r0
        L_0x0210:
            if (r15 == 0) goto L_0x0215
            r15.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0224 }
        L_0x0215:
            goto L_0x0243
        L_0x0216:
            r0 = move-exception
        L_0x0217:
            r1 = r0
            if (r15 == 0) goto L_0x0223
            r15.close()     // Catch:{ all -> 0x021e }
            goto L_0x0223
        L_0x021e:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0224 }
        L_0x0223:
            throw r1     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0224 }
        L_0x0224:
            r0 = move-exception
            goto L_0x0229
        L_0x0226:
            r0 = move-exception
            r9 = r28
        L_0x0229:
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "nullpointer or ArrayIndexOutOfBounds Exception: "
            r2.append(r3)
            java.lang.String r3 = r0.getMessage()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r1, r2)
        L_0x0243:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleNormalSyncObjectRcsMessageDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject, boolean):long");
    }

    private int handleObjectDownloadCrossSearch(ParamOMAObject objt, String line, boolean isGoforward) {
        int contractTypeFromLegacy;
        String str = TAG;
        Log.d(str, "handleObjectDownloadCrossSearch: " + objt);
        if (!CloudMessageStrategyManager.getStrategy().requiresInterworkingCrossSearch() || (contractTypeFromLegacy = crossObjectSearchLegacy(objt, line, isGoforward)) == 1) {
            return 1;
        }
        this.mSummaryDB.insertSummaryDbUsingObjectIfNonExist(objt, contractTypeFromLegacy);
        return contractTypeFromLegacy;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x00f2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleNormalSyncObjectRcsImdnDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject r13) {
        /*
            r12 = this;
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleNormalSyncObjectRcsImdnDownload: "
            r1.append(r2)
            r1.append(r13)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            r0 = -1
            java.lang.String r2 = r13.DISPOSITION_ORIGINAL_TO
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getPhoneNum(r2)
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r2)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r3 = r12.mBufferDbQuery
            java.lang.String r4 = r13.DISPOSITION_ORIGINAL_MESSAGEID
            android.database.Cursor r3 = r3.searchBufferNotificationUsingImdnAndTelUri(r4, r2)
            if (r3 == 0) goto L_0x00eb
            boolean r4 = r3.moveToFirst()     // Catch:{ all -> 0x00f6 }
            if (r4 == 0) goto L_0x00eb
            java.lang.String r4 = "_bufferdbid"
            int r4 = r3.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x00f6 }
            int r4 = r3.getInt(r4)     // Catch:{ all -> 0x00f6 }
            long r4 = (long) r4     // Catch:{ all -> 0x00f6 }
            java.lang.String r6 = "_bufferdbid=?"
            r7 = 1
            java.lang.String[] r7 = new java.lang.String[r7]     // Catch:{ all -> 0x00f6 }
            r8 = 0
            java.lang.String r9 = java.lang.String.valueOf(r4)     // Catch:{ all -> 0x00f6 }
            r7[r8] = r9     // Catch:{ all -> 0x00f6 }
            android.content.ContentValues r8 = new android.content.ContentValues     // Catch:{ all -> 0x00f6 }
            r8.<init>()     // Catch:{ all -> 0x00f6 }
            java.lang.String r9 = "lastmodseq"
            java.lang.Long r10 = r13.lastModSeq     // Catch:{ all -> 0x00f6 }
            r8.put(r9, r10)     // Catch:{ all -> 0x00f6 }
            java.lang.String r9 = "res_url"
            java.net.URL r10 = r13.resourceURL     // Catch:{ all -> 0x00f6 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x00f6 }
            java.lang.String r10 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r10)     // Catch:{ all -> 0x00f6 }
            r8.put(r9, r10)     // Catch:{ all -> 0x00f6 }
            java.lang.String r9 = "parentfolder"
            java.net.URL r10 = r13.parentFolder     // Catch:{ all -> 0x00f6 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x00f6 }
            java.lang.String r10 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r10)     // Catch:{ all -> 0x00f6 }
            r8.put(r9, r10)     // Catch:{ all -> 0x00f6 }
            java.lang.String r9 = "path"
            java.lang.String r10 = r13.path     // Catch:{ all -> 0x00f6 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x00f6 }
            java.lang.String r10 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r10)     // Catch:{ all -> 0x00f6 }
            r8.put(r9, r10)     // Catch:{ all -> 0x00f6 }
            java.lang.String r9 = "imdn_id"
            java.lang.String r10 = r13.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x00f6 }
            r8.put(r9, r10)     // Catch:{ all -> 0x00f6 }
            java.lang.String r9 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x00f6 }
            int r10 = r10.getId()     // Catch:{ all -> 0x00f6 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ all -> 0x00f6 }
            r8.put(r9, r10)     // Catch:{ all -> 0x00f6 }
            java.lang.String r9 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r10 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x00f6 }
            int r10 = r10.getId()     // Catch:{ all -> 0x00f6 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ all -> 0x00f6 }
            r8.put(r9, r10)     // Catch:{ all -> 0x00f6 }
            java.lang.String r9 = "timestamp"
            long r10 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x00f6 }
            java.lang.Long r10 = java.lang.Long.valueOf(r10)     // Catch:{ all -> 0x00f6 }
            r8.put(r9, r10)     // Catch:{ all -> 0x00f6 }
            java.lang.String r9 = "displayed"
            java.lang.String r10 = r13.DISPOSITION_STATUS     // Catch:{ all -> 0x00f6 }
            boolean r9 = r9.equalsIgnoreCase(r10)     // Catch:{ all -> 0x00f6 }
            java.lang.String r10 = "status"
            if (r9 == 0) goto L_0x00d5
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r9 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x00f6 }
            int r9 = r9.getId()     // Catch:{ all -> 0x00f6 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x00f6 }
            r8.put(r10, r9)     // Catch:{ all -> 0x00f6 }
            goto L_0x00e2
        L_0x00d5:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r9 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED     // Catch:{ all -> 0x00f6 }
            int r9 = r9.getId()     // Catch:{ all -> 0x00f6 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x00f6 }
            r8.put(r10, r9)     // Catch:{ all -> 0x00f6 }
        L_0x00e2:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r9 = r12.mBufferDbQuery     // Catch:{ all -> 0x00f6 }
            r10 = 13
            r9.updateTable(r10, r8, r6, r7)     // Catch:{ all -> 0x00f6 }
            goto L_0x00f0
        L_0x00eb:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r12.mBufferDbQuery     // Catch:{ all -> 0x00f6 }
            r4.insertRCSimdnToBufferDBUsingObject(r13)     // Catch:{ all -> 0x00f6 }
        L_0x00f0:
            if (r3 == 0) goto L_0x00f5
            r3.close()
        L_0x00f5:
            return r0
        L_0x00f6:
            r4 = move-exception
            if (r3 == 0) goto L_0x0101
            r3.close()     // Catch:{ all -> 0x00fd }
            goto L_0x0101
        L_0x00fd:
            r5 = move-exception
            r4.addSuppressed(r5)
        L_0x0101:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleNormalSyncObjectRcsImdnDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject):long");
    }

    /* Debug info: failed to restart local var, previous not found, register: 33 */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02b0 A[SYNTHETIC, Splitter:B:87:0x02b0] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x02bd A[SYNTHETIC, Splitter:B:94:0x02bd] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectRCSMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r34, boolean r35) {
        /*
            r33 = this;
            r10 = r33
            r11 = r34
            java.lang.String r0 = "status"
            java.lang.String r1 = "syncdirection"
            java.lang.String r2 = "syncaction"
            java.lang.String r3 = TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "handleObjectRCSMessageCloudSearch: "
            r4.append(r5)
            r4.append(r11)
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r3, r4)
            r12 = -1
            java.net.URL r3 = r11.resourceURL
            java.lang.String r3 = r3.toString()
            java.lang.String r14 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r3)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r3 = r10.mBufferDbQuery     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02c9 }
            java.lang.String r4 = r11.correlationId     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02c9 }
            android.database.Cursor r3 = r3.searchIMFTBufferUsingImdn(r4, r14)     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02c9 }
            r15 = r3
            r3 = 1
            if (r15 == 0) goto L_0x01b9
            boolean r4 = r15.moveToFirst()     // Catch:{ all -> 0x01b1 }
            if (r4 == 0) goto L_0x01b9
            java.lang.String r4 = "_bufferdbid"
            int r4 = r15.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x01b1 }
            int r4 = r15.getInt(r4)     // Catch:{ all -> 0x01b1 }
            long r8 = (long) r4     // Catch:{ all -> 0x01b1 }
            int r4 = r15.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x01b1 }
            int r4 = r15.getInt(r4)     // Catch:{ all -> 0x01b1 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r21 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r4)     // Catch:{ all -> 0x01b1 }
            int r4 = r15.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x01b1 }
            int r4 = r15.getInt(r4)     // Catch:{ all -> 0x01b1 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r20 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r4)     // Catch:{ all -> 0x01b1 }
            java.lang.String r4 = "_id"
            int r4 = r15.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x01b1 }
            int r4 = r15.getInt(r4)     // Catch:{ all -> 0x01b1 }
            r7 = r4
            java.lang.String r4 = "_bufferdbid=?"
            r6 = r4
            java.lang.String[] r4 = new java.lang.String[r3]     // Catch:{ all -> 0x01b1 }
            java.lang.String r5 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x01b1 }
            r3 = 0
            r4[r3] = r5     // Catch:{ all -> 0x01b1 }
            r5 = r4
            android.content.ContentValues r4 = new android.content.ContentValues     // Catch:{ all -> 0x01b1 }
            r4.<init>()     // Catch:{ all -> 0x01b1 }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x01b1 }
            r29 = r12
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r12 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x01ac }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r13 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x01ac }
            r3.<init>(r12, r13)     // Catch:{ all -> 0x01ac }
            r12 = 0
            r3.mIsChanged = r12     // Catch:{ all -> 0x01ac }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r12 = r11.mFlag     // Catch:{ all -> 0x01ac }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r13 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x01ac }
            boolean r12 = r12.equals(r13)     // Catch:{ all -> 0x01ac }
            if (r12 == 0) goto L_0x00a8
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x01ac }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r12 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice     // Catch:{ all -> 0x01ac }
            r13 = 1
            r3.setIsChangedActionAndDirection(r13, r0, r12)     // Catch:{ all -> 0x01ac }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x01ac }
            r0.deleteRCSMessageDb(r7)     // Catch:{ all -> 0x01ac }
            r0 = r3
            goto L_0x010a
        L_0x00a8:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r12 = r11.mFlag     // Catch:{ all -> 0x01ac }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r13 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x01ac }
            boolean r12 = r12.equals(r13)     // Catch:{ all -> 0x01ac }
            if (r12 == 0) goto L_0x00f5
            int r12 = r15.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x01ac }
            int r12 = r15.getInt(r12)     // Catch:{ all -> 0x01ac }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r13 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x01ac }
            int r13 = r13.getId()     // Catch:{ all -> 0x01ac }
            if (r12 == r13) goto L_0x00e3
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r12 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x01ac }
            int r12 = r12.getId()     // Catch:{ all -> 0x01ac }
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)     // Catch:{ all -> 0x01ac }
            r4.put(r0, r12)     // Catch:{ all -> 0x01ac }
            java.lang.String r0 = "disposition_notification_status"
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r12 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x01ac }
            int r12 = r12.getId()     // Catch:{ all -> 0x01ac }
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)     // Catch:{ all -> 0x01ac }
            r4.put(r0, r12)     // Catch:{ all -> 0x01ac }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x01ac }
            r0.updateRCSMessageDb(r7, r4)     // Catch:{ all -> 0x01ac }
        L_0x00e3:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r0 = r10.mScheduleRule     // Catch:{ all -> 0x01ac }
            int r12 = r10.mDbTableContractIndex     // Catch:{ all -> 0x01ac }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r22 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x01ac }
            r16 = r0
            r17 = r12
            r18 = r8
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r0 = r16.getSetFlagsForCldOperation(r17, r18, r20, r21, r22)     // Catch:{ all -> 0x01ac }
            r3 = r0
            goto L_0x010a
        L_0x00f5:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r0 = r10.mScheduleRule     // Catch:{ all -> 0x01ac }
            int r12 = r10.mDbTableContractIndex     // Catch:{ all -> 0x01ac }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r28 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x01ac }
            r22 = r0
            r23 = r12
            r24 = r8
            r26 = r20
            r27 = r21
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r0 = r22.getSetFlagsForCldOperation(r23, r24, r26, r27, r28)     // Catch:{ all -> 0x01ac }
            r3 = r0
        L_0x010a:
            java.lang.String r3 = "lastmodseq"
            java.lang.Long r12 = r11.lastModSeq     // Catch:{ all -> 0x01ac }
            r4.put(r3, r12)     // Catch:{ all -> 0x01ac }
            java.lang.String r3 = "res_url"
            java.net.URL r12 = r11.resourceURL     // Catch:{ all -> 0x01ac }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x01ac }
            java.lang.String r12 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r12)     // Catch:{ all -> 0x01ac }
            r4.put(r3, r12)     // Catch:{ all -> 0x01ac }
            java.lang.String r3 = "parentfolder"
            java.net.URL r12 = r11.parentFolder     // Catch:{ all -> 0x01ac }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x01ac }
            java.lang.String r12 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r12)     // Catch:{ all -> 0x01ac }
            r4.put(r3, r12)     // Catch:{ all -> 0x01ac }
            java.lang.String r3 = "path"
            java.lang.String r12 = r11.path     // Catch:{ all -> 0x01ac }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x01ac }
            java.lang.String r12 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r12)     // Catch:{ all -> 0x01ac }
            r4.put(r3, r12)     // Catch:{ all -> 0x01ac }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r3 = r10.mSummaryDB     // Catch:{ all -> 0x01ac }
            r12 = 1
            r3.insertSummaryDbUsingObjectIfNonExist(r11, r12)     // Catch:{ all -> 0x01ac }
            boolean r3 = r0.mIsChanged     // Catch:{ all -> 0x01ac }
            if (r3 == 0) goto L_0x018f
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = r0.mAction     // Catch:{ all -> 0x01ac }
            int r3 = r3.getId()     // Catch:{ all -> 0x01ac }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x01ac }
            r4.put(r2, r3)     // Catch:{ all -> 0x01ac }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = r0.mDirection     // Catch:{ all -> 0x01ac }
            int r2 = r2.getId()     // Catch:{ all -> 0x01ac }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x01ac }
            r4.put(r1, r2)     // Catch:{ all -> 0x01ac }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x01ac }
            r2 = 1
            r1.updateTable(r2, r4, r6, r5)     // Catch:{ all -> 0x01ac }
            r12 = 1
            r13 = 0
            boolean r3 = r11.mIsGoforwardSync     // Catch:{ all -> 0x01ac }
            java.lang.String r2 = r11.mLine     // Catch:{ all -> 0x01ac }
            r16 = 0
            r1 = r33
            r17 = r2
            r2 = r0
            r18 = r3
            r31 = r4
            r3 = r8
            r32 = r5
            r5 = r12
            r12 = r6
            r6 = r13
            r13 = r7
            r7 = r18
            r18 = r8
            r8 = r17
            r9 = r16
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x01ac }
            goto L_0x01a8
        L_0x018f:
            r31 = r4
            r32 = r5
            r12 = r6
            r13 = r7
            r18 = r8
            java.lang.String r1 = TAG     // Catch:{ all -> 0x01ac }
            java.lang.String r2 = "flagsetresult.mIsChanged: false - don't update sync action or direction"
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x01ac }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x01ac }
            r3 = r31
            r2 = r32
            r4 = 1
            r1.updateTable(r4, r3, r12, r2)     // Catch:{ all -> 0x01ac }
        L_0x01a8:
            r12 = r35
            goto L_0x02ae
        L_0x01ac:
            r0 = move-exception
            r12 = r35
            goto L_0x02ba
        L_0x01b1:
            r0 = move-exception
            r29 = r12
            r12 = r35
            r1 = r0
            goto L_0x02bb
        L_0x01b9:
            r29 = r12
            r12 = r35
            int r0 = r10.handleObjectDownloadCrossSearch(r11, r14, r12)     // Catch:{ all -> 0x02b9 }
            r13 = r0
            java.lang.String r0 = TAG     // Catch:{ all -> 0x02b9 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x02b9 }
            r1.<init>()     // Catch:{ all -> 0x02b9 }
            java.lang.String r2 = "handleObjectRCSCloudSearch: RCS not found: contractTypeFromLegacy: "
            r1.append(r2)     // Catch:{ all -> 0x02b9 }
            r1.append(r13)     // Catch:{ all -> 0x02b9 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x02b9 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x02b9 }
            r0 = -1
            r2 = 1
            if (r13 == r2) goto L_0x01e4
            if (r15 == 0) goto L_0x01e3
            r15.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02c7 }
        L_0x01e3:
            return r0
        L_0x01e4:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r10.mBufferDbQuery     // Catch:{ all -> 0x02b9 }
            java.lang.String r2 = r2.searchOrCreateSession(r11)     // Catch:{ all -> 0x02b9 }
            r9 = r2
            if (r9 != 0) goto L_0x01f4
            if (r15 == 0) goto L_0x01f3
            r15.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02c7 }
        L_0x01f3:
            return r0
        L_0x01f4:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x02b9 }
            r1 = 1
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r0 = r0.insertRCSMessageToBufferDBUsingObject(r11, r9, r1)     // Catch:{ all -> 0x02b9 }
            r8 = r0
            long r0 = r8.mBufferId     // Catch:{ all -> 0x02b9 }
            r16 = r0
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r10.mSummaryDB     // Catch:{ all -> 0x02b4 }
            r1 = 1
            r0.insertSummaryDbUsingObjectIfNonExist(r11, r1)     // Catch:{ all -> 0x02b4 }
            r0 = 0
            int r0 = (r16 > r0 ? 1 : (r16 == r0 ? 0 : -1))
            if (r0 <= 0) goto L_0x0265
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice     // Catch:{ all -> 0x02b4 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r8.mDirection     // Catch:{ all -> 0x02b4 }
            boolean r0 = r0.equals(r1)     // Catch:{ all -> 0x02b4 }
            if (r0 != 0) goto L_0x0226
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x02b4 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r8.mDirection     // Catch:{ all -> 0x02b4 }
            boolean r0 = r0.equals(r1)     // Catch:{ all -> 0x02b4 }
            if (r0 == 0) goto L_0x0221
            goto L_0x0226
        L_0x0221:
            r18 = r8
            r19 = r9
            goto L_0x0269
        L_0x0226:
            java.lang.String r0 = r11.TEXT_CONTENT     // Catch:{ all -> 0x02b4 }
            boolean r0 = android.text.TextUtils.isEmpty(r0)     // Catch:{ all -> 0x02b4 }
            if (r0 != 0) goto L_0x024a
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x02b4 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x02b4 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x02b4 }
            r2.<init>(r0, r1)     // Catch:{ all -> 0x02b4 }
            r5 = 1
            r6 = 0
            r0 = 0
            r1 = r33
            r3 = r16
            r7 = r35
            r18 = r8
            r8 = r14
            r19 = r9
            r9 = r0
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x02b4 }
            goto L_0x0269
        L_0x024a:
            r18 = r8
            r19 = r9
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x02b4 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x02b4 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x02b4 }
            r2.<init>(r0, r1)     // Catch:{ all -> 0x02b4 }
            r5 = 1
            r6 = 1
            r9 = 0
            r1 = r33
            r3 = r16
            r7 = r35
            r8 = r14
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x02b4 }
            goto L_0x0269
        L_0x0265:
            r18 = r8
            r19 = r9
        L_0x0269:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x02b4 }
            java.lang.String r1 = r11.correlationId     // Catch:{ all -> 0x02b4 }
            java.util.Set<com.sec.ims.util.ImsUri> r2 = r11.mNomalizedOtherParticipants     // Catch:{ all -> 0x02b4 }
            int r2 = r2.size()     // Catch:{ all -> 0x02b4 }
            r0.queryImdnBufferDBandUpdateRcsMessageBufferDb(r1, r2)     // Catch:{ all -> 0x02b4 }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ all -> 0x02b4 }
            boolean r0 = r0.isStoreImdnEnabled()     // Catch:{ all -> 0x02b4 }
            if (r0 == 0) goto L_0x02ac
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x02b4 }
            java.lang.String r1 = r11.correlationId     // Catch:{ all -> 0x02b4 }
            android.database.Cursor r0 = r0.queryRcsDBMessageUsingImdnId(r1)     // Catch:{ all -> 0x02b4 }
            r1 = r0
            if (r1 == 0) goto L_0x02a7
            boolean r0 = r1.moveToNext()     // Catch:{ all -> 0x0299 }
            if (r0 == 0) goto L_0x02a7
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x0299 }
            java.lang.String r2 = r11.correlationId     // Catch:{ all -> 0x0299 }
            r0.queryBufferDbandUpdateRcsMessageDb(r2)     // Catch:{ all -> 0x0299 }
            goto L_0x02a7
        L_0x0299:
            r0 = move-exception
            r2 = r0
            if (r1 == 0) goto L_0x02a6
            r1.close()     // Catch:{ all -> 0x02a1 }
            goto L_0x02a6
        L_0x02a1:
            r0 = move-exception
            r3 = r0
            r2.addSuppressed(r3)     // Catch:{ all -> 0x02b4 }
        L_0x02a6:
            throw r2     // Catch:{ all -> 0x02b4 }
        L_0x02a7:
            if (r1 == 0) goto L_0x02ac
            r1.close()     // Catch:{ all -> 0x02b4 }
        L_0x02ac:
            r29 = r16
        L_0x02ae:
            if (r15 == 0) goto L_0x02b3
            r15.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02c7 }
        L_0x02b3:
            goto L_0x02e8
        L_0x02b4:
            r0 = move-exception
            r1 = r0
            r29 = r16
            goto L_0x02bb
        L_0x02b9:
            r0 = move-exception
        L_0x02ba:
            r1 = r0
        L_0x02bb:
            if (r15 == 0) goto L_0x02c6
            r15.close()     // Catch:{ all -> 0x02c1 }
            goto L_0x02c6
        L_0x02c1:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02c7 }
        L_0x02c6:
            throw r1     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02c7 }
        L_0x02c7:
            r0 = move-exception
            goto L_0x02ce
        L_0x02c9:
            r0 = move-exception
            r29 = r12
            r12 = r35
        L_0x02ce:
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "nullpointer or ArrayIndexOutOfBoundsException: "
            r2.append(r3)
            java.lang.String r3 = r0.getMessage()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r1, r2)
        L_0x02e8:
            return r29
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleObjectRCSMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject, boolean):long");
    }

    /* Debug info: failed to restart local var, previous not found, register: 14 */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0138 A[SYNTHETIC, Splitter:B:29:0x0138] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0168 A[Catch:{ all -> 0x018d, all -> 0x0194, all -> 0x015a, all -> 0x0161, all -> 0x01a4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0175 A[Catch:{ all -> 0x018d, all -> 0x0194, all -> 0x015a, all -> 0x0161, all -> 0x01a4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01a0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectRCSIMDNCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r15) {
        /*
            r14 = this;
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleObjectRCSIMDNCloudSearch: "
            r1.append(r2)
            r1.append(r15)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            r0 = -1
            java.net.URL r2 = r15.resourceURL
            java.lang.String r2 = r2.toString()
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r2)
            java.lang.String r3 = r15.DISPOSITION_ORIGINAL_TO
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.getPhoneNum(r3)
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r3)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r14.mBufferDbQuery
            java.lang.String r5 = r15.correlationId
            android.database.Cursor r4 = r4.searchBufferNotificationUsingImdnAndTelUri(r5, r3)
            r5 = 13
            if (r4 == 0) goto L_0x0124
            boolean r6 = r4.moveToFirst()     // Catch:{ all -> 0x01a4 }
            if (r6 == 0) goto L_0x0124
            java.lang.String r6 = "delivered"
            java.lang.String r7 = r15.DISPOSITION_STATUS     // Catch:{ all -> 0x01a4 }
            boolean r6 = r6.equalsIgnoreCase(r7)     // Catch:{ all -> 0x01a4 }
            java.lang.String r7 = "status"
            if (r6 == 0) goto L_0x0069
            int r6 = r4.getColumnIndex(r7)     // Catch:{ all -> 0x01a4 }
            int r6 = r4.getInt(r6)     // Catch:{ all -> 0x01a4 }
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r8 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x01a4 }
            int r8 = r8.getId()     // Catch:{ all -> 0x01a4 }
            if (r6 != r8) goto L_0x0069
            java.lang.String r5 = TAG     // Catch:{ all -> 0x01a4 }
            java.lang.String r6 = "delivered comes after displayed, shouldn't update"
            android.util.Log.d(r5, r6)     // Catch:{ all -> 0x01a4 }
            if (r4 == 0) goto L_0x0068
            r4.close()
        L_0x0068:
            return r0
        L_0x0069:
            java.lang.String r6 = "_bufferdbid"
            int r6 = r4.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01a4 }
            int r6 = r4.getInt(r6)     // Catch:{ all -> 0x01a4 }
            long r8 = (long) r6     // Catch:{ all -> 0x01a4 }
            java.lang.String r6 = "_bufferdbid=?"
            r10 = 1
            java.lang.String[] r10 = new java.lang.String[r10]     // Catch:{ all -> 0x01a4 }
            r11 = 0
            java.lang.String r12 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x01a4 }
            r10[r11] = r12     // Catch:{ all -> 0x01a4 }
            android.content.ContentValues r11 = new android.content.ContentValues     // Catch:{ all -> 0x01a4 }
            r11.<init>()     // Catch:{ all -> 0x01a4 }
            java.lang.String r12 = "lastmodseq"
            java.lang.Long r13 = r15.lastModSeq     // Catch:{ all -> 0x01a4 }
            r11.put(r12, r13)     // Catch:{ all -> 0x01a4 }
            java.lang.String r12 = "res_url"
            java.net.URL r13 = r15.resourceURL     // Catch:{ all -> 0x01a4 }
            java.lang.String r13 = r13.toString()     // Catch:{ all -> 0x01a4 }
            java.lang.String r13 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r13)     // Catch:{ all -> 0x01a4 }
            r11.put(r12, r13)     // Catch:{ all -> 0x01a4 }
            java.lang.String r12 = "parentfolder"
            java.net.URL r13 = r15.parentFolder     // Catch:{ all -> 0x01a4 }
            java.lang.String r13 = r13.toString()     // Catch:{ all -> 0x01a4 }
            java.lang.String r13 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r13)     // Catch:{ all -> 0x01a4 }
            r11.put(r12, r13)     // Catch:{ all -> 0x01a4 }
            java.lang.String r12 = "path"
            java.lang.String r13 = r15.path     // Catch:{ all -> 0x01a4 }
            java.lang.String r13 = r13.toString()     // Catch:{ all -> 0x01a4 }
            java.lang.String r13 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r13)     // Catch:{ all -> 0x01a4 }
            r11.put(r12, r13)     // Catch:{ all -> 0x01a4 }
            java.lang.String r12 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r13 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x01a4 }
            int r13 = r13.getId()     // Catch:{ all -> 0x01a4 }
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)     // Catch:{ all -> 0x01a4 }
            r11.put(r12, r13)     // Catch:{ all -> 0x01a4 }
            java.lang.String r12 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r13 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x01a4 }
            int r13 = r13.getId()     // Catch:{ all -> 0x01a4 }
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)     // Catch:{ all -> 0x01a4 }
            r11.put(r12, r13)     // Catch:{ all -> 0x01a4 }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r12 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ all -> 0x01a4 }
            boolean r12 = r12.isStoreImdnEnabled()     // Catch:{ all -> 0x01a4 }
            if (r12 == 0) goto L_0x0118
            java.lang.String r12 = "displayed"
            java.lang.String r13 = r15.DISPOSITION_STATUS     // Catch:{ all -> 0x01a4 }
            boolean r12 = r12.equalsIgnoreCase(r13)     // Catch:{ all -> 0x01a4 }
            if (r12 == 0) goto L_0x00fe
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r12 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x01a4 }
            int r12 = r12.getId()     // Catch:{ all -> 0x01a4 }
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)     // Catch:{ all -> 0x01a4 }
            r11.put(r7, r12)     // Catch:{ all -> 0x01a4 }
            goto L_0x010b
        L_0x00fe:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r12 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED     // Catch:{ all -> 0x01a4 }
            int r12 = r12.getId()     // Catch:{ all -> 0x01a4 }
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)     // Catch:{ all -> 0x01a4 }
            r11.put(r7, r12)     // Catch:{ all -> 0x01a4 }
        L_0x010b:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r7 = r14.mBufferDbQuery     // Catch:{ all -> 0x01a4 }
            r7.updateTable(r5, r11, r6, r10)     // Catch:{ all -> 0x01a4 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r7 = r14.mBufferDbQuery     // Catch:{ all -> 0x01a4 }
            java.lang.String r12 = r15.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x01a4 }
            r7.updateRCSNotificationUsingImsdId(r12, r11)     // Catch:{ all -> 0x01a4 }
            goto L_0x011d
        L_0x0118:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r7 = r14.mBufferDbQuery     // Catch:{ all -> 0x01a4 }
            r7.updateTable(r5, r11, r6, r10)     // Catch:{ all -> 0x01a4 }
        L_0x011d:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r7 = r14.mSummaryDB     // Catch:{ all -> 0x01a4 }
            r7.insertSummaryDbUsingObjectIfNonExist(r15, r5)     // Catch:{ all -> 0x01a4 }
            goto L_0x012e
        L_0x0124:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r14.mBufferDbQuery     // Catch:{ all -> 0x01a4 }
            r6.insertRCSimdnToBufferDBUsingObject(r15)     // Catch:{ all -> 0x01a4 }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r6 = r14.mSummaryDB     // Catch:{ all -> 0x01a4 }
            r6.insertSummaryDbUsingObjectIfNonExist(r15, r5)     // Catch:{ all -> 0x01a4 }
        L_0x012e:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r5 = r14.mBufferDbQuery     // Catch:{ all -> 0x01a4 }
            java.lang.String r6 = r15.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x01a4 }
            android.database.Cursor r5 = r5.searchIMFTBufferUsingImdn(r6, r2)     // Catch:{ all -> 0x01a4 }
            if (r5 == 0) goto L_0x0166
            boolean r6 = r5.moveToFirst()     // Catch:{ all -> 0x015a }
            if (r6 == 0) goto L_0x0166
            java.lang.String r6 = "not_displayed_counter"
            int r6 = r5.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x015a }
            int r6 = r5.getInt(r6)     // Catch:{ all -> 0x015a }
            java.lang.String r7 = "disposition_notification_status"
            int r7 = r5.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x015a }
            int r7 = r5.getInt(r7)     // Catch:{ all -> 0x015a }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r8 = r14.mBufferDbQuery     // Catch:{ all -> 0x015a }
            java.lang.String r9 = r15.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x015a }
            r8.updateRcsMessageBufferDbIfNewIMDNReceived(r9, r6, r7, r15)     // Catch:{ all -> 0x015a }
            goto L_0x0166
        L_0x015a:
            r6 = move-exception
            if (r5 == 0) goto L_0x0165
            r5.close()     // Catch:{ all -> 0x0161 }
            goto L_0x0165
        L_0x0161:
            r7 = move-exception
            r6.addSuppressed(r7)     // Catch:{ all -> 0x01a4 }
        L_0x0165:
            throw r6     // Catch:{ all -> 0x01a4 }
        L_0x0166:
            if (r5 == 0) goto L_0x016b
            r5.close()     // Catch:{ all -> 0x01a4 }
        L_0x016b:
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r5 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ all -> 0x01a4 }
            boolean r5 = r5.isStoreImdnEnabled()     // Catch:{ all -> 0x01a4 }
            if (r5 == 0) goto L_0x019e
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r5 = r14.mBufferDbQuery     // Catch:{ all -> 0x01a4 }
            java.lang.String r6 = r15.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x01a4 }
            android.database.Cursor r5 = r5.queryRcsDBMessageUsingImdnId(r6)     // Catch:{ all -> 0x01a4 }
            if (r5 == 0) goto L_0x0199
            boolean r6 = r5.moveToNext()     // Catch:{ all -> 0x018d }
            if (r6 == 0) goto L_0x0199
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r14.mBufferDbQuery     // Catch:{ all -> 0x018d }
            java.lang.String r7 = r15.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x018d }
            r6.queryBufferDbandUpdateRcsMessageDb(r7)     // Catch:{ all -> 0x018d }
            goto L_0x0199
        L_0x018d:
            r6 = move-exception
            if (r5 == 0) goto L_0x0198
            r5.close()     // Catch:{ all -> 0x0194 }
            goto L_0x0198
        L_0x0194:
            r7 = move-exception
            r6.addSuppressed(r7)     // Catch:{ all -> 0x01a4 }
        L_0x0198:
            throw r6     // Catch:{ all -> 0x01a4 }
        L_0x0199:
            if (r5 == 0) goto L_0x019e
            r5.close()     // Catch:{ all -> 0x01a4 }
        L_0x019e:
            if (r4 == 0) goto L_0x01a3
            r4.close()
        L_0x01a3:
            return r0
        L_0x01a4:
            r5 = move-exception
            if (r4 == 0) goto L_0x01af
            r4.close()     // Catch:{ all -> 0x01ab }
            goto L_0x01af
        L_0x01ab:
            r6 = move-exception
            r5.addSuppressed(r6)
        L_0x01af:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleObjectRCSIMDNCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject):long");
    }

    public void handleExistingBufferForDeviceRCSUpdate(Cursor cs, DeviceIMFTUpdateParam para, boolean mIsGoforwardSync, BufferDBChangeParamList changelist) {
        Throwable th;
        Cursor cursor = cs;
        DeviceIMFTUpdateParam deviceIMFTUpdateParam = para;
        String str = TAG;
        IMSLog.s(str, "handleExistingBufferForDeviceRCSUpdate: " + deviceIMFTUpdateParam);
        ContentValues cv = new ContentValues();
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String line = cursor.getString(cursor.getColumnIndexOrThrow("linenum"));
        long bufferDbId = cursor.getLong(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForMsgOperation(this.mDbTableContractIndex, bufferDbId, direction, action, deviceIMFTUpdateParam.mOperation);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        cv.put("_id", Long.valueOf(deviceIMFTUpdateParam.mRowId));
        boolean isFt = cursor.getInt(cursor.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) == 1;
        String str2 = TAG;
        Log.d(str2, "isFt: " + isFt + " , action: " + deviceIMFTUpdateParam.mUpdateType);
        if (isFt && CloudMessageBufferDBConstants.ActionStatusFlag.Delete.equals(deviceIMFTUpdateParam.mUpdateType)) {
            String filepath = cursor.getString(cursor.getColumnIndex("file_path"));
            String thumbpath = cursor.getString(cursor.getColumnIndex(ImContract.CsSession.THUMBNAIL_PATH));
            String str3 = TAG;
            Log.d(str3, "filepath: " + filepath + " , thumbpath: " + thumbpath);
            if (!TextUtils.isEmpty(filepath)) {
                File file = new File(filepath);
                if (file.exists()) {
                    file.delete();
                }
            }
            if (!TextUtils.isEmpty(thumbpath)) {
                File thumbfile = new File(thumbpath);
                if (thumbfile.exists()) {
                    thumbfile.delete();
                }
            }
        }
        if (CloudMessageStrategyManager.getStrategy().isSupportAtt72HoursRule() && CloudMessageBufferDBConstants.MsgOperationFlag.Received.equals(deviceIMFTUpdateParam.mOperation) && isFt) {
            Cursor rcsCs = this.mBufferDbQuery.queryIMFTUsingRowId(deviceIMFTUpdateParam.mRowId);
            if (rcsCs != null) {
                try {
                    if (rcsCs.moveToFirst()) {
                        cv.put(ImContract.CsSession.THUMBNAIL_PATH, rcsCs.getString(rcsCs.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH)));
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            if (rcsCs != null) {
                rcsCs.close();
            }
        }
        String[] selectionArgs = {String.valueOf(bufferDbId)};
        this.mBufferDbQuery.updateTable(deviceIMFTUpdateParam.mTableindex, cv, "_bufferdbid=?", selectionArgs);
        if (flagSet.mIsChanged) {
            String[] strArr = selectionArgs;
            boolean z = isFt;
            ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, deviceIMFTUpdateParam.mTableindex, isFt, mIsGoforwardSync, line, changelist);
            return;
        }
        boolean z2 = isFt;
        ParamSyncFlagsSet paramSyncFlagsSet2 = flagSet;
        return;
        throw th;
    }

    public void handleNonExistingBufferForDeviceRCSUpdate(DeviceIMFTUpdateParam para) {
        String str = TAG;
        IMSLog.s(str, "handleNonExistingBufferForDeviceRCSUpdate: " + para);
        Cursor rcsCs = this.mBufferDbQuery.queryIMFTUsingRowId(para.mRowId);
        if (rcsCs != null) {
            try {
                if (rcsCs.moveToFirst()) {
                    ContentValues cv = new ContentValues();
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
                    if (this.mBufferDbQuery.insertToRCSMessagesBufferDB(rcsCs, para.mLine, cv) < 1) {
                        Log.e(TAG, "handleNonExistingBufferForDeviceRCSUpdate: insert RCS Buffer DB error or meet blocked number!");
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (rcsCs != null) {
            rcsCs.close();
            return;
        }
        return;
        throw th;
    }

    public void handleDownLoadMessageResponse(ParamOMAresponseforBufDB paramOMAObj, boolean isSuccess) {
        if (!isSuccess && ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND.equals(paramOMAObj.getActionType())) {
            this.mBufferDbQuery.setMsgDeleted(paramOMAObj.getBufferDBChangeParam().mDBIndex, paramOMAObj.getBufferDBChangeParam().mRowId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:58:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onUpdateFromDeviceIMFT(com.sec.internal.ims.cmstore.params.DeviceIMFTUpdateParam r6, boolean r7, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r8) {
        /*
            r5 = this;
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onUpdateFromDeviceIMFT: "
            r1.append(r2)
            r1.append(r6)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            int r0 = r6.mTableindex
            r1 = 1
            if (r0 != r1) goto L_0x00bc
            r0 = 0
            int[] r1 = com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ all -> 0x00b5 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r2 = r6.mOperation     // Catch:{ all -> 0x00b5 }
            int r2 = r2.ordinal()     // Catch:{ all -> 0x00b5 }
            r1 = r1[r2]     // Catch:{ all -> 0x00b5 }
            switch(r1) {
                case 1: goto L_0x0086;
                case 2: goto L_0x007a;
                case 3: goto L_0x006c;
                case 4: goto L_0x0040;
                case 5: goto L_0x003a;
                case 6: goto L_0x003a;
                case 7: goto L_0x003a;
                case 8: goto L_0x002c;
                default: goto L_0x002a;
            }     // Catch:{ all -> 0x00b5 }
        L_0x002a:
            goto L_0x0092
        L_0x002c:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r5.mBufferDbQuery     // Catch:{ all -> 0x00b5 }
            long r2 = r6.mRowId     // Catch:{ all -> 0x00b5 }
            java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00b5 }
            android.database.Cursor r1 = r1.searchIMFTBufferUsingRowId(r2)     // Catch:{ all -> 0x00b5 }
            r0 = r1
            goto L_0x0092
        L_0x003a:
            if (r0 == 0) goto L_0x003f
            r0.close()
        L_0x003f:
            return
        L_0x0040:
            java.lang.String r1 = r6.mChatId     // Catch:{ all -> 0x00b5 }
            if (r1 == 0) goto L_0x004e
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r5.mBufferDbQuery     // Catch:{ all -> 0x00b5 }
            java.lang.String r2 = r6.mChatId     // Catch:{ all -> 0x00b5 }
            android.database.Cursor r1 = r1.searchIMFTBufferUsingChatId(r2)     // Catch:{ all -> 0x00b5 }
            r0 = r1
            goto L_0x0092
        L_0x004e:
            java.lang.String r1 = r6.mImdnId     // Catch:{ all -> 0x00b5 }
            if (r1 == 0) goto L_0x005e
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r5.mBufferDbQuery     // Catch:{ all -> 0x00b5 }
            java.lang.String r2 = r6.mImdnId     // Catch:{ all -> 0x00b5 }
            java.lang.String r3 = r6.mLine     // Catch:{ all -> 0x00b5 }
            android.database.Cursor r1 = r1.searchIMFTBufferUsingImdn(r2, r3)     // Catch:{ all -> 0x00b5 }
            r0 = r1
            goto L_0x0092
        L_0x005e:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r5.mBufferDbQuery     // Catch:{ all -> 0x00b5 }
            long r2 = r6.mRowId     // Catch:{ all -> 0x00b5 }
            java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00b5 }
            android.database.Cursor r1 = r1.searchIMFTBufferUsingRowId(r2)     // Catch:{ all -> 0x00b5 }
            r0 = r1
            goto L_0x0092
        L_0x006c:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r5.mBufferDbQuery     // Catch:{ all -> 0x00b5 }
            long r2 = r6.mRowId     // Catch:{ all -> 0x00b5 }
            java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00b5 }
            android.database.Cursor r1 = r1.searchIMFTBufferUsingRowId(r2)     // Catch:{ all -> 0x00b5 }
            r0 = r1
            goto L_0x0092
        L_0x007a:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r5.mBufferDbQuery     // Catch:{ all -> 0x00b5 }
            java.lang.String r2 = r6.mImdnId     // Catch:{ all -> 0x00b5 }
            java.lang.String r3 = r6.mLine     // Catch:{ all -> 0x00b5 }
            android.database.Cursor r1 = r1.searchIMFTBufferUsingImdn(r2, r3)     // Catch:{ all -> 0x00b5 }
            r0 = r1
            goto L_0x0092
        L_0x0086:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r5.mBufferDbQuery     // Catch:{ all -> 0x00b5 }
            java.lang.String r2 = r6.mImdnId     // Catch:{ all -> 0x00b5 }
            java.lang.String r3 = r6.mLine     // Catch:{ all -> 0x00b5 }
            android.database.Cursor r1 = r1.searchIMFTBufferUsingImdn(r2, r3)     // Catch:{ all -> 0x00b5 }
            r0 = r1
        L_0x0092:
            if (r0 == 0) goto L_0x00a4
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x00b5 }
            if (r1 == 0) goto L_0x00a4
        L_0x009a:
            r5.handleExistingBufferForDeviceRCSUpdate(r0, r6, r7, r8)     // Catch:{ all -> 0x00b5 }
            boolean r1 = r0.moveToNext()     // Catch:{ all -> 0x00b5 }
            if (r1 != 0) goto L_0x009a
            goto L_0x00af
        L_0x00a4:
            long r1 = r6.mRowId     // Catch:{ all -> 0x00b5 }
            r3 = 0
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 <= 0) goto L_0x00af
            r5.handleNonExistingBufferForDeviceRCSUpdate(r6)     // Catch:{ all -> 0x00b5 }
        L_0x00af:
            if (r0 == 0) goto L_0x00eb
            r0.close()
            goto L_0x00eb
        L_0x00b5:
            r1 = move-exception
            if (r0 == 0) goto L_0x00bb
            r0.close()
        L_0x00bb:
            throw r1
        L_0x00bc:
            int r0 = r6.mTableindex
            r1 = 13
            if (r0 != r1) goto L_0x00eb
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r5.mBufferDbQuery
            java.lang.String r1 = r6.mImdnId
            android.database.Cursor r0 = r0.searchBufferNotificationUsingImdn(r1)
            if (r0 == 0) goto L_0x00d6
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x00df }
            if (r1 == 0) goto L_0x00d6
            r5.handleExistingBufferForDeviceIMDNUpdate(r0, r6)     // Catch:{ all -> 0x00df }
            goto L_0x00d9
        L_0x00d6:
            r5.handleNonExistingBufferForDeviceIMDNUpdate(r6)     // Catch:{ all -> 0x00df }
        L_0x00d9:
            if (r0 == 0) goto L_0x00ec
            r0.close()
            goto L_0x00ec
        L_0x00df:
            r1 = move-exception
            if (r0 == 0) goto L_0x00ea
            r0.close()     // Catch:{ all -> 0x00e6 }
            goto L_0x00ea
        L_0x00e6:
            r2 = move-exception
            r1.addSuppressed(r2)
        L_0x00ea:
            throw r1
        L_0x00eb:
        L_0x00ec:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.onUpdateFromDeviceIMFT(com.sec.internal.ims.cmstore.params.DeviceIMFTUpdateParam, boolean, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler$1  reason: invalid class name */
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
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Sent.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Read.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Delete.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.SendFail.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Receiving.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Sending.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Download.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    private void handleExistingBufferForDeviceIMDNUpdate(Cursor cs, DeviceIMFTUpdateParam para) {
    }

    private void handleNonExistingBufferForDeviceIMDNUpdate(DeviceIMFTUpdateParam para) {
    }

    public void onAppOperationReceived(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        String str = TAG;
        IMSLog.s(str, "onAppOperationReceived: " + param);
        CloudMessageBufferDBConstants.ActionStatusFlag actionFlag = CloudMessageBufferDBConstants.ActionStatusFlag.None;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[param.mOperation.ordinal()];
        if (i == 1) {
            actionFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
        } else if (i == 2) {
            actionFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
        } else if (i == 3) {
            actionFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
        } else if (i == 4) {
            actionFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
        } else if (i == 8) {
            actionFlag = CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad;
        }
        DeviceIMFTUpdateParam imftpara = new DeviceIMFTUpdateParam(param.mDataContractType, actionFlag, param.mOperation, (long) param.mRowId, param.mChatId, param.mCorrelationId, param.mLine);
        if (CloudMessageBufferDBConstants.MsgOperationFlag.Download.equals(param.mOperation)) {
            onDownloadRequestFromApp(imftpara);
        } else {
            onUpdateFromDeviceIMFT(imftpara, false, changelist);
        }
    }

    public void onNotificationReceived(ParamNmsNotificationList notification) {
        Log.d(TAG, "onNotificationReceived: " + notification);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        if (!(notification.mNmsEventList == null || notification.mNmsEventList.nmsEvent == null)) {
            for (NmsEvent event : notification.mNmsEventList.nmsEvent) {
                if (event.changedObject != null) {
                    handleCloudNotifyChangedObj(event.changedObject, downloadlist, notification);
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

    public void onNotificationGSOReceived(ParamNmsNotificationList notification) {
        Log.d(TAG, "onNotificationGSOReceived: " + notification);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        if (!(notification.mNmsEventList == null || notification.mNmsEventList.nmsEvent == null)) {
            for (NmsEvent event : notification.mNmsEventList.nmsEvent) {
                if (event.changedObject != null) {
                    handleCloudNotifyGsoObject(event.changedObject, downloadlist, notification);
                }
            }
        }
        Log.i(TAG, "onNotificationGSOReceived: download list size " + downloadlist.mChangelst.size());
        if (downloadlist.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(downloadlist);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 16 */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b6 A[SYNTHETIC, Splitter:B:22:0x00b6] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00c2 A[SYNTHETIC, Splitter:B:29:0x00c2] */
    /* JADX WARNING: Removed duplicated region for block: B:44:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyGsoObject(com.sec.internal.omanetapi.nms.data.ChangedObject r17, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r18, com.sec.internal.ims.cmstore.params.ParamNmsNotificationList r19) {
        /*
            r16 = this;
            r1 = r16
            r2 = r17
            r9 = 0
            java.net.URL r0 = r2.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r10 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r1.mSummaryDB     // Catch:{ NullPointerException -> 0x00ce }
            java.net.URL r3 = r2.resourceURL     // Catch:{ NullPointerException -> 0x00ce }
            java.lang.String r3 = r3.toString()     // Catch:{ NullPointerException -> 0x00ce }
            android.database.Cursor r0 = r0.querySummaryDBwithResUrl(r3)     // Catch:{ NullPointerException -> 0x00ce }
            r11 = r0
            if (r11 == 0) goto L_0x0076
            boolean r0 = r11.moveToFirst()     // Catch:{ all -> 0x00bc }
            if (r0 == 0) goto L_0x0076
            java.lang.String r0 = "syncaction"
            int r0 = r11.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x00bc }
            int r0 = r11.getInt(r0)     // Catch:{ all -> 0x00bc }
            java.lang.String r3 = TAG     // Catch:{ all -> 0x00bc }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00bc }
            r4.<init>()     // Catch:{ all -> 0x00bc }
            java.lang.String r5 = "is found, status: "
            r4.append(r5)     // Catch:{ all -> 0x00bc }
            r4.append(r0)     // Catch:{ all -> 0x00bc }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00bc }
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x00bc }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x00bc }
            int r3 = r3.getId()     // Catch:{ all -> 0x00bc }
            if (r0 != r3) goto L_0x0053
            if (r11 == 0) goto L_0x0052
            r11.close()     // Catch:{ NullPointerException -> 0x00ce }
        L_0x0052:
            return
        L_0x0053:
            java.lang.String r3 = "_bufferdbid"
            int r3 = r11.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x00bc }
            int r3 = r11.getInt(r3)     // Catch:{ all -> 0x00bc }
            long r3 = (long) r3     // Catch:{ all -> 0x00bc }
            java.lang.String r5 = TAG     // Catch:{ all -> 0x00bc }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x00bc }
            r6.<init>()     // Catch:{ all -> 0x00bc }
            java.lang.String r7 = "is found, rowId:"
            r6.append(r7)     // Catch:{ all -> 0x00bc }
            r6.append(r3)     // Catch:{ all -> 0x00bc }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x00bc }
            android.util.Log.d(r5, r6)     // Catch:{ all -> 0x00bc }
            r12 = r3
            goto L_0x00a3
        L_0x0076:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r1.mSummaryDB     // Catch:{ all -> 0x00bc }
            r3 = 34
            long r3 = r0.insertNmsEventChangedObjToSummaryDB(r2, r3)     // Catch:{ all -> 0x00bc }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x00bc }
            int r0 = r0.getId()     // Catch:{ all -> 0x00bc }
            java.lang.String r5 = TAG     // Catch:{ all -> 0x00bc }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x00bc }
            r6.<init>()     // Catch:{ all -> 0x00bc }
            java.lang.String r7 = "not found, insert to summary DB, rowId:"
            r6.append(r7)     // Catch:{ all -> 0x00bc }
            r6.append(r3)     // Catch:{ all -> 0x00bc }
            java.lang.String r7 = ", initSynStatuc: "
            r6.append(r7)     // Catch:{ all -> 0x00bc }
            r6.append(r0)     // Catch:{ all -> 0x00bc }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x00bc }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x00bc }
            r12 = r3
        L_0x00a3:
            r14 = r18
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r0 = r14.mChangelst     // Catch:{ all -> 0x00ba }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r15 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x00ba }
            r4 = 7
            r3 = r15
            r5 = r12
            r7 = r9
            r8 = r10
            r3.<init>(r4, r5, r7, r8)     // Catch:{ all -> 0x00ba }
            r0.add(r15)     // Catch:{ all -> 0x00ba }
            if (r11 == 0) goto L_0x00b9
            r11.close()     // Catch:{ NullPointerException -> 0x00cc }
        L_0x00b9:
            goto L_0x00eb
        L_0x00ba:
            r0 = move-exception
            goto L_0x00bf
        L_0x00bc:
            r0 = move-exception
            r14 = r18
        L_0x00bf:
            r3 = r0
            if (r11 == 0) goto L_0x00cb
            r11.close()     // Catch:{ all -> 0x00c6 }
            goto L_0x00cb
        L_0x00c6:
            r0 = move-exception
            r4 = r0
            r3.addSuppressed(r4)     // Catch:{ NullPointerException -> 0x00cc }
        L_0x00cb:
            throw r3     // Catch:{ NullPointerException -> 0x00cc }
        L_0x00cc:
            r0 = move-exception
            goto L_0x00d1
        L_0x00ce:
            r0 = move-exception
            r14 = r18
        L_0x00d1:
            java.lang.String r3 = TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "null pointer exception: "
            r4.append(r5)
            java.lang.String r5 = r0.getMessage()
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.e(r3, r4)
        L_0x00eb:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleCloudNotifyGsoObject(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList, com.sec.internal.ims.cmstore.params.ParamNmsNotificationList):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:126:0x0393 A[SYNTHETIC, Splitter:B:126:0x0393] */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x049b A[SYNTHETIC, Splitter:B:152:0x049b] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleCloudNotifyGSOChangedObj(com.sec.internal.ims.cmstore.params.ParamOMAObject r37, com.sec.internal.omanetapi.nms.data.Object r38) {
        /*
            r36 = this;
            r1 = r36
            r2 = r38
            java.lang.String r0 = TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "handleCloudNotifyGSOChangedObj(), objt is: "
            r3.append(r4)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            android.util.Log.d(r0, r3)
            r0 = 0
            com.sec.internal.omanetapi.nms.data.AttributeList r3 = r2.attributes
            com.sec.internal.omanetapi.nms.data.Attribute[] r3 = r3.attribute
            int r4 = r3.length
            r5 = 0
            r6 = r5
        L_0x0022:
            if (r6 >= r4) goto L_0x04c7
            r7 = r3[r6]
            java.lang.String[] r8 = r7.value
            int r9 = r8.length
            r10 = r5
        L_0x002a:
            if (r10 >= r9) goto L_0x0051
            r11 = r8[r10]
            java.lang.String r12 = TAG
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            java.lang.String r14 = "Attribute key: "
            r13.append(r14)
            java.lang.String r14 = r7.name
            r13.append(r14)
            java.lang.String r14 = ", value: "
            r13.append(r14)
            r13.append(r11)
            java.lang.String r13 = r13.toString()
            android.util.Log.d(r12, r13)
            int r10 = r10 + 1
            goto L_0x002a
        L_0x0051:
            java.lang.String r8 = r7.name
            java.lang.String r9 = "subject"
            boolean r8 = r9.equals(r8)
            if (r8 == 0) goto L_0x0062
            java.lang.String[] r8 = r7.value
            r0 = r8[r5]
            r8 = r0
            goto L_0x0063
        L_0x0062:
            r8 = r0
        L_0x0063:
            java.lang.String r0 = r7.name
            java.lang.String r10 = "TextContent"
            boolean r0 = r10.equalsIgnoreCase(r0)
            if (r0 == 0) goto L_0x04b0
            java.lang.String[] r0 = r7.value
            int r10 = r0.length
            r11 = r5
        L_0x0071:
            if (r11 >= r10) goto L_0x04a5
            r12 = r0[r11]
            com.sec.internal.omanetapi.nms.data.GroupState r13 = com.sec.internal.omanetapi.nms.XmlParser.parseGroupState(r12)
            java.lang.String r14 = TAG
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r5 = "GroupState after xmlParser: "
            r15.append(r5)
            java.lang.String r5 = r13.toString()
            r15.append(r5)
            java.lang.String r5 = r15.toString()
            android.util.Log.i(r14, r5)
            r13.subject = r8
            android.content.ContentValues r5 = new android.content.ContentValues
            r5.<init>()
            java.lang.String r14 = r13.group_type
            java.lang.String r15 = "open"
            boolean r14 = r15.equalsIgnoreCase(r14)
            java.lang.String r15 = "chat_type"
            if (r14 == 0) goto L_0x00b2
            r14 = 1
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)
            r5.put(r15, r14)
            r16 = r0
            goto L_0x00bc
        L_0x00b2:
            r16 = r0
            r14 = 2
            java.lang.Integer r0 = java.lang.Integer.valueOf(r14)
            r5.put(r15, r0)
        L_0x00bc:
            java.lang.String r0 = r13.subject
            r5.put(r9, r0)
            java.lang.String r0 = r13.lastfocussessionid
            java.lang.String r14 = "session_uri"
            r5.put(r14, r0)
            r15 = 0
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery
            android.database.Cursor r2 = r0.queryAllSession()
            if (r2 != 0) goto L_0x00e9
            if (r2 == 0) goto L_0x00d7
            r2.close()
        L_0x00d7:
            r18 = r3
            r19 = r4
            r31 = r6
            r21 = r7
            r22 = r8
            r23 = r9
            r29 = r10
            r24 = r11
            goto L_0x0477
        L_0x00e9:
            r0 = -1
            r17 = r0
            java.lang.String r0 = r13.lastfocussessionid     // Catch:{ all -> 0x048e }
            if (r0 == 0) goto L_0x0108
            java.lang.String r0 = r13.lastfocussessionid     // Catch:{ all -> 0x00fb }
            r18 = r3
            java.lang.String r3 = "@"
            int r0 = r0.indexOf(r3)     // Catch:{ all -> 0x00fb }
            goto L_0x010c
        L_0x00fb:
            r0 = move-exception
            r3 = r0
            r27 = r5
            r21 = r7
            r22 = r8
            r30 = r12
            r4 = r13
            goto L_0x0499
        L_0x0108:
            r18 = r3
            r0 = r17
        L_0x010c:
            r3 = 0
            if (r0 <= 0) goto L_0x045b
            r17 = r3
            java.lang.String r3 = r13.lastfocussessionid     // Catch:{ all -> 0x048e }
            r19 = r4
            r4 = 0
            java.lang.String r3 = r3.substring(r4, r0)     // Catch:{ all -> 0x048e }
            java.lang.String r4 = "chat_id"
            if (r2 == 0) goto L_0x019a
        L_0x011e:
            boolean r17 = r2.moveToNext()     // Catch:{ all -> 0x018d }
            if (r17 == 0) goto L_0x0184
            r20 = r0
            int r0 = r2.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x018d }
            java.lang.String r0 = r2.getString(r0)     // Catch:{ all -> 0x018d }
            r21 = r7
            java.lang.String r7 = TAG     // Catch:{ all -> 0x0179 }
            r22 = r8
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0170 }
            r8.<init>()     // Catch:{ all -> 0x0170 }
            r23 = r9
            java.lang.String r9 = "session uri: "
            r8.append(r9)     // Catch:{ all -> 0x0170 }
            r8.append(r0)     // Catch:{ all -> 0x0170 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0170 }
            android.util.Log.d(r7, r8)     // Catch:{ all -> 0x0170 }
            if (r0 == 0) goto L_0x0167
            if (r3 == 0) goto L_0x0167
            java.lang.String r7 = r0.toLowerCase()     // Catch:{ all -> 0x0170 }
            java.lang.String r8 = r3.toLowerCase()     // Catch:{ all -> 0x0170 }
            boolean r7 = r7.contains(r8)     // Catch:{ all -> 0x0170 }
            if (r7 == 0) goto L_0x0167
            int r7 = r2.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x0170 }
            java.lang.String r7 = r2.getString(r7)     // Catch:{ all -> 0x0170 }
            r15 = r7
            goto L_0x01a2
        L_0x0167:
            r0 = r20
            r7 = r21
            r8 = r22
            r9 = r23
            goto L_0x011e
        L_0x0170:
            r0 = move-exception
            r3 = r0
            r27 = r5
            r30 = r12
            r4 = r13
            goto L_0x0499
        L_0x0179:
            r0 = move-exception
            r22 = r8
            r3 = r0
            r27 = r5
            r30 = r12
            r4 = r13
            goto L_0x0499
        L_0x0184:
            r20 = r0
            r21 = r7
            r22 = r8
            r23 = r9
            goto L_0x01a2
        L_0x018d:
            r0 = move-exception
            r21 = r7
            r22 = r8
            r3 = r0
            r27 = r5
            r30 = r12
            r4 = r13
            goto L_0x0499
        L_0x019a:
            r20 = r0
            r21 = r7
            r22 = r8
            r23 = r9
        L_0x01a2:
            if (r2 == 0) goto L_0x01a7
            r2.close()
        L_0x01a7:
            java.lang.String r0 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "chat id: "
            r2.append(r3)
            r2.append(r15)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r0, r2)
            if (r15 == 0) goto L_0x044f
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery
            r0.updateSessionBufferDb(r15, r5)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery
            r0.updateRCSSessionDb(r15, r5)
            r5.put(r4, r15)
            r0 = 10
            r1.notifyMsgAppFetchBuffer((android.content.ContentValues) r5, (int) r0)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery
            android.database.Cursor r2 = r0.queryParticipantsUsingChatId(r15)
            java.lang.String r0 = "uri"
            java.lang.String r3 = "Administrator"
            java.lang.String r7 = "alias"
            java.lang.String r8 = "type"
            if (r2 == 0) goto L_0x039d
        L_0x01e3:
            boolean r9 = r2.moveToNext()     // Catch:{ all -> 0x0389 }
            if (r9 == 0) goto L_0x0376
            com.sec.internal.omanetapi.nms.data.Part r9 = new com.sec.internal.omanetapi.nms.data.Part     // Catch:{ all -> 0x0389 }
            r9.<init>()     // Catch:{ all -> 0x0389 }
            int r14 = r2.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x0389 }
            java.lang.String r14 = r2.getString(r14)     // Catch:{ all -> 0x0389 }
            r9.name = r14     // Catch:{ all -> 0x0389 }
            int r14 = r2.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0389 }
            java.lang.String r14 = r2.getString(r14)     // Catch:{ all -> 0x0389 }
            r9.comm_addr = r14     // Catch:{ all -> 0x0389 }
            int r14 = r2.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x0389 }
            java.lang.String r14 = r2.getString(r14)     // Catch:{ all -> 0x0389 }
            r9.role = r14     // Catch:{ all -> 0x0389 }
            java.lang.String r14 = r9.comm_addr     // Catch:{ all -> 0x0389 }
            java.lang.String r14 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r14)     // Catch:{ all -> 0x0389 }
            java.lang.String r17 = com.sec.internal.ims.cmstore.utils.Util.getMsisdn(r14)     // Catch:{ all -> 0x0389 }
            r20 = r17
            r17 = 0
            r24 = 0
            r26 = 0
            r27 = r5
            java.util.ArrayList<com.sec.internal.omanetapi.nms.data.Part> r5 = r13.participantList     // Catch:{ all -> 0x036f }
            java.util.Iterator r5 = r5.iterator()     // Catch:{ all -> 0x036f }
        L_0x0226:
            boolean r28 = r5.hasNext()     // Catch:{ all -> 0x036f }
            r29 = r10
            java.lang.String r10 = "_id"
            if (r28 == 0) goto L_0x028b
            java.lang.Object r28 = r5.next()     // Catch:{ all -> 0x0283 }
            com.sec.internal.omanetapi.nms.data.Part r28 = (com.sec.internal.omanetapi.nms.data.Part) r28     // Catch:{ all -> 0x0283 }
            r30 = r28
            r28 = r5
            r5 = r30
            r30 = r12
            java.lang.String r12 = r5.comm_addr     // Catch:{ all -> 0x027d }
            java.lang.String r12 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r12)     // Catch:{ all -> 0x027d }
            r31 = r6
            r6 = r20
            boolean r20 = r12.contains(r6)     // Catch:{ all -> 0x027d }
            if (r20 == 0) goto L_0x0270
            r17 = 1
            r20 = r12
            int r12 = r2.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x027d }
            long r32 = r2.getLong(r12)     // Catch:{ all -> 0x027d }
            r24 = r32
            r26 = r5
            java.util.ArrayList<com.sec.internal.omanetapi.nms.data.Part> r12 = r13.participantList     // Catch:{ all -> 0x027d }
            r12.remove(r5)     // Catch:{ all -> 0x027d }
            r5 = r17
            r17 = r0
            r0 = r26
            r34 = r24
            r24 = r11
            r11 = r34
            goto L_0x029d
        L_0x0270:
            r20 = r12
            r20 = r6
            r5 = r28
            r10 = r29
            r12 = r30
            r6 = r31
            goto L_0x0226
        L_0x027d:
            r0 = move-exception
            r3 = r0
            r26 = r13
            goto L_0x0391
        L_0x0283:
            r0 = move-exception
            r30 = r12
            r3 = r0
            r26 = r13
            goto L_0x0391
        L_0x028b:
            r31 = r6
            r30 = r12
            r6 = r20
            r5 = r17
            r17 = r0
            r0 = r26
            r34 = r24
            r24 = r11
            r11 = r34
        L_0x029d:
            r25 = r4
            java.lang.String r4 = TAG     // Catch:{ all -> 0x036a }
            r20 = r7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x036a }
            r7.<init>()     // Catch:{ all -> 0x036a }
            r26 = r13
            java.lang.String r13 = "Participant: "
            r7.append(r13)     // Catch:{ all -> 0x0367 }
            java.lang.String r13 = r9.toString()     // Catch:{ all -> 0x0367 }
            r7.append(r13)     // Catch:{ all -> 0x0367 }
            java.lang.String r13 = ", telLine = "
            r7.append(r13)     // Catch:{ all -> 0x0367 }
            java.lang.String r13 = com.sec.internal.log.IMSLog.checker(r14)     // Catch:{ all -> 0x0367 }
            r7.append(r13)     // Catch:{ all -> 0x0367 }
            java.lang.String r13 = ", line = "
            r7.append(r13)     // Catch:{ all -> 0x0367 }
            java.lang.String r13 = com.sec.internal.log.IMSLog.checker(r6)     // Catch:{ all -> 0x0367 }
            r7.append(r13)     // Catch:{ all -> 0x0367 }
            java.lang.String r13 = "isExist: "
            r7.append(r13)     // Catch:{ all -> 0x0367 }
            r7.append(r5)     // Catch:{ all -> 0x0367 }
            java.lang.String r13 = ", tempPart: "
            r7.append(r13)     // Catch:{ all -> 0x0367 }
            if (r0 == 0) goto L_0x02e2
            java.lang.String r13 = r0.toString()     // Catch:{ all -> 0x0367 }
            goto L_0x02e4
        L_0x02e2:
            java.lang.String r13 = ""
        L_0x02e4:
            r7.append(r13)     // Catch:{ all -> 0x0367 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x0367 }
            android.util.Log.i(r4, r7)     // Catch:{ all -> 0x0367 }
            if (r5 != 0) goto L_0x0307
            int r4 = r2.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x0367 }
            long r32 = r2.getLong(r4)     // Catch:{ all -> 0x0367 }
            r10 = r32
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r1.mBufferDbQuery     // Catch:{ all -> 0x0367 }
            r4.deleteParticipantsUsingRowId(r10)     // Catch:{ all -> 0x0367 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r1.mBufferDbQuery     // Catch:{ all -> 0x0367 }
            java.lang.String r7 = r9.comm_addr     // Catch:{ all -> 0x0367 }
            r4.deleteParticipantsFromBufferDb(r7, r15)     // Catch:{ all -> 0x0367 }
            goto L_0x0353
        L_0x0307:
            if (r0 == 0) goto L_0x0353
            android.content.ContentValues r4 = new android.content.ContentValues     // Catch:{ all -> 0x0367 }
            r4.<init>()     // Catch:{ all -> 0x0367 }
            java.lang.String r7 = r0.role     // Catch:{ all -> 0x0367 }
            if (r7 == 0) goto L_0x033a
            java.lang.String r7 = r0.role     // Catch:{ all -> 0x0367 }
            boolean r7 = r7.equalsIgnoreCase(r3)     // Catch:{ all -> 0x0367 }
            if (r7 == 0) goto L_0x033a
            java.lang.String r7 = r0.role     // Catch:{ all -> 0x0367 }
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Type r10 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Type.CHAIRMAN     // Catch:{ all -> 0x0367 }
            int r10 = r10.getId()     // Catch:{ all -> 0x0367 }
            java.lang.String r10 = java.lang.String.valueOf(r10)     // Catch:{ all -> 0x0367 }
            boolean r7 = r7.equals(r10)     // Catch:{ all -> 0x0367 }
            if (r7 != 0) goto L_0x033a
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Type r7 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Type.CHAIRMAN     // Catch:{ all -> 0x0367 }
            int r7 = r7.getId()     // Catch:{ all -> 0x0367 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0367 }
            r4.put(r8, r7)     // Catch:{ all -> 0x0367 }
            goto L_0x0347
        L_0x033a:
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Type r7 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Type.REGULAR     // Catch:{ all -> 0x0367 }
            int r7 = r7.getId()     // Catch:{ all -> 0x0367 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x0367 }
            r4.put(r8, r7)     // Catch:{ all -> 0x0367 }
        L_0x0347:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r7 = r1.mBufferDbQuery     // Catch:{ all -> 0x0367 }
            r7.updateRCSParticipantsDb(r11, r4)     // Catch:{ all -> 0x0367 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r7 = r1.mBufferDbQuery     // Catch:{ all -> 0x0367 }
            java.lang.String r10 = r9.comm_addr     // Catch:{ all -> 0x0367 }
            r7.updateParticipantsBufferDb(r10, r4)     // Catch:{ all -> 0x0367 }
        L_0x0353:
            r0 = r17
            r7 = r20
            r11 = r24
            r4 = r25
            r13 = r26
            r5 = r27
            r10 = r29
            r12 = r30
            r6 = r31
            goto L_0x01e3
        L_0x0367:
            r0 = move-exception
            r3 = r0
            goto L_0x0391
        L_0x036a:
            r0 = move-exception
            r26 = r13
            r3 = r0
            goto L_0x0391
        L_0x036f:
            r0 = move-exception
            r30 = r12
            r26 = r13
            r3 = r0
            goto L_0x0391
        L_0x0376:
            r17 = r0
            r25 = r4
            r27 = r5
            r31 = r6
            r20 = r7
            r29 = r10
            r24 = r11
            r30 = r12
            r26 = r13
            goto L_0x03af
        L_0x0389:
            r0 = move-exception
            r27 = r5
            r30 = r12
            r26 = r13
            r3 = r0
        L_0x0391:
            if (r2 == 0) goto L_0x039c
            r2.close()     // Catch:{ all -> 0x0397 }
            goto L_0x039c
        L_0x0397:
            r0 = move-exception
            r4 = r0
            r3.addSuppressed(r4)
        L_0x039c:
            throw r3
        L_0x039d:
            r17 = r0
            r25 = r4
            r27 = r5
            r31 = r6
            r20 = r7
            r29 = r10
            r24 = r11
            r30 = r12
            r26 = r13
        L_0x03af:
            if (r2 == 0) goto L_0x03b4
            r2.close()
        L_0x03b4:
            r4 = r26
            java.util.ArrayList<com.sec.internal.omanetapi.nms.data.Part> r0 = r4.participantList
            java.util.Iterator r0 = r0.iterator()
        L_0x03bc:
            boolean r2 = r0.hasNext()
            if (r2 == 0) goto L_0x0477
            java.lang.Object r2 = r0.next()
            com.sec.internal.omanetapi.nms.data.Part r2 = (com.sec.internal.omanetapi.nms.data.Part) r2
            java.lang.String r5 = TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "Insert participant : "
            r6.append(r7)
            java.lang.String r7 = r2.toString()
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.d(r5, r6)
            android.content.ContentValues r5 = new android.content.ContentValues
            r5.<init>()
            java.lang.String r6 = r2.role
            java.lang.String r7 = "status"
            if (r6 == 0) goto L_0x0411
            java.lang.String r6 = r2.role
            boolean r6 = r6.equalsIgnoreCase(r3)
            if (r6 == 0) goto L_0x0411
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Type r6 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Type.CHAIRMAN
            int r6 = r6.getId()
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            r5.put(r8, r6)
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r6 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INITIAL
            int r6 = r6.getId()
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            r5.put(r7, r6)
            goto L_0x042b
        L_0x0411:
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Type r6 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Type.REGULAR
            int r6 = r6.getId()
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            r5.put(r8, r6)
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r6 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.ACCEPTED
            int r6 = r6.getId()
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
            r5.put(r7, r6)
        L_0x042b:
            java.lang.String r6 = r2.name
            r7 = r20
            r5.put(r7, r6)
            r6 = r25
            r5.put(r6, r15)
            java.lang.String r9 = r2.comm_addr
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r9)
            r10 = r17
            r5.put(r10, r9)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r11 = r1.mBufferDbQuery
            r11.insertRCSParticipantsDb((android.content.ContentValues) r5)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r11 = r1.mBufferDbQuery
            r12 = 2
            r11.insertDeviceMsgToBuffer(r12, r5)
            goto L_0x03bc
        L_0x044f:
            r27 = r5
            r31 = r6
            r29 = r10
            r24 = r11
            r30 = r12
            r4 = r13
            goto L_0x0477
        L_0x045b:
            r20 = r0
            r17 = r3
            r19 = r4
            r27 = r5
            r31 = r6
            r21 = r7
            r22 = r8
            r23 = r9
            r29 = r10
            r24 = r11
            r30 = r12
            r4 = r13
            if (r2 == 0) goto L_0x0477
            r2.close()
        L_0x0477:
            int r11 = r24 + 1
            r2 = r38
            r0 = r16
            r3 = r18
            r4 = r19
            r7 = r21
            r8 = r22
            r9 = r23
            r10 = r29
            r6 = r31
            r5 = 0
            goto L_0x0071
        L_0x048e:
            r0 = move-exception
            r27 = r5
            r21 = r7
            r22 = r8
            r30 = r12
            r4 = r13
            r3 = r0
        L_0x0499:
            if (r2 == 0) goto L_0x04a4
            r2.close()     // Catch:{ all -> 0x049f }
            goto L_0x04a4
        L_0x049f:
            r0 = move-exception
            r5 = r0
            r3.addSuppressed(r5)
        L_0x04a4:
            throw r3
        L_0x04a5:
            r18 = r3
            r19 = r4
            r31 = r6
            r21 = r7
            r22 = r8
            goto L_0x04ba
        L_0x04b0:
            r18 = r3
            r19 = r4
            r31 = r6
            r21 = r7
            r22 = r8
        L_0x04ba:
            int r6 = r31 + 1
            r2 = r38
            r3 = r18
            r4 = r19
            r0 = r22
            r5 = 0
            goto L_0x0022
        L_0x04c7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleCloudNotifyGSOChangedObj(com.sec.internal.ims.cmstore.params.ParamOMAObject, com.sec.internal.omanetapi.nms.data.Object):void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 16 */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a0 A[SYNTHETIC, Splitter:B:28:0x00a0] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00b4 A[SYNTHETIC, Splitter:B:41:0x00b4] */
    /* JADX WARNING: Removed duplicated region for block: B:59:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject r17, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r18, com.sec.internal.ims.cmstore.params.ParamNmsNotificationList r19) {
        /*
            r16 = this;
            r9 = r16
            r10 = r17
            r11 = 0
            java.net.URL r0 = r10.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r12 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r9.mSummaryDB     // Catch:{ NullPointerException -> 0x00c6 }
            java.net.URL r1 = r10.resourceURL     // Catch:{ NullPointerException -> 0x00c6 }
            java.lang.String r1 = r1.toString()     // Catch:{ NullPointerException -> 0x00c6 }
            android.database.Cursor r0 = r0.querySummaryDBwithResUrl(r1)     // Catch:{ NullPointerException -> 0x00c6 }
            r13 = r0
            if (r13 == 0) goto L_0x0042
            boolean r0 = r13.moveToFirst()     // Catch:{ all -> 0x00b8 }
            if (r0 == 0) goto L_0x0042
            java.lang.String r0 = "syncaction"
            int r0 = r13.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x00b8 }
            int r0 = r13.getInt(r0)     // Catch:{ all -> 0x00b8 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x00b8 }
            int r1 = r1.getId()     // Catch:{ all -> 0x00b8 }
            if (r0 != r1) goto L_0x003d
            if (r13 == 0) goto L_0x003c
            r13.close()     // Catch:{ NullPointerException -> 0x00c6 }
        L_0x003c:
            return
        L_0x003d:
            r9.onNmsEventChangedObjSummaryDbAvailableUsingUrl(r13, r10)     // Catch:{ all -> 0x00b8 }
            goto L_0x00b2
        L_0x0042:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r9.mSummaryDB     // Catch:{ all -> 0x00b8 }
            r1 = 1
            long r4 = r0.insertNmsEventChangedObjToSummaryDB(r10, r1)     // Catch:{ all -> 0x00b8 }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x00b8 }
            int r0 = r0.getId()     // Catch:{ all -> 0x00b8 }
            r14 = r0
            java.lang.String r0 = TAG     // Catch:{ all -> 0x00b8 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b8 }
            r1.<init>()     // Catch:{ all -> 0x00b8 }
            java.lang.String r2 = "check initial sync status ::"
            r1.append(r2)     // Catch:{ all -> 0x00b8 }
            r1.append(r14)     // Catch:{ all -> 0x00b8 }
            java.lang.String r2 = "correlationId"
            r1.append(r2)     // Catch:{ all -> 0x00b8 }
            java.lang.String r2 = r10.correlationId     // Catch:{ all -> 0x00b8 }
            r1.append(r2)     // Catch:{ all -> 0x00b8 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00b8 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x00b8 }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x00b8 }
            int r0 = r0.getId()     // Catch:{ all -> 0x00b8 }
            if (r14 != r0) goto L_0x00b2
            java.lang.String r0 = r10.correlationId     // Catch:{ all -> 0x00b8 }
            if (r0 == 0) goto L_0x00b2
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r9.mBufferDbQuery     // Catch:{ all -> 0x00b8 }
            java.lang.String r1 = r10.correlationId     // Catch:{ all -> 0x00b8 }
            android.database.Cursor r0 = r0.searchIMFTBufferUsingImdn(r1, r12)     // Catch:{ all -> 0x00b8 }
            r15 = r0
            if (r15 == 0) goto L_0x0091
            boolean r0 = r15.moveToFirst()     // Catch:{ all -> 0x00a4 }
            if (r0 == 0) goto L_0x0091
            r9.onNmsEventChangedObjRcsBufferDbAvailableUsingImdnId(r15, r10, r11)     // Catch:{ all -> 0x00a4 }
            goto L_0x009e
        L_0x0091:
            r1 = r16
            r2 = r17
            r3 = r18
            r6 = r11
            r7 = r12
            r8 = r19
            r1.handleNotificationNmsEventUnavailableUsingCorrId(r2, r3, r4, r6, r7, r8)     // Catch:{ all -> 0x00a4 }
        L_0x009e:
            if (r15 == 0) goto L_0x00b2
            r15.close()     // Catch:{ all -> 0x00b8 }
            goto L_0x00b2
        L_0x00a4:
            r0 = move-exception
            r1 = r0
            if (r15 == 0) goto L_0x00b1
            r15.close()     // Catch:{ all -> 0x00ac }
            goto L_0x00b1
        L_0x00ac:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ all -> 0x00b8 }
        L_0x00b1:
            throw r1     // Catch:{ all -> 0x00b8 }
        L_0x00b2:
            if (r13 == 0) goto L_0x00b7
            r13.close()     // Catch:{ NullPointerException -> 0x00c6 }
        L_0x00b7:
            goto L_0x00e1
        L_0x00b8:
            r0 = move-exception
            r1 = r0
            if (r13 == 0) goto L_0x00c5
            r13.close()     // Catch:{ all -> 0x00c0 }
            goto L_0x00c5
        L_0x00c0:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x00c6 }
        L_0x00c5:
            throw r1     // Catch:{ NullPointerException -> 0x00c6 }
        L_0x00c6:
            r0 = move-exception
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "null pointer exception: "
            r2.append(r3)
            java.lang.String r3 = r0.getMessage()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r1, r2)
        L_0x00e1:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList, com.sec.internal.ims.cmstore.params.ParamNmsNotificationList):void");
    }

    private void handleNotificationNmsEventUnavailableUsingCorrId(ChangedObject objt, BufferDBChangeParamList downloadlist, long summaryrowId, boolean isGoforward, String line, ParamNmsNotificationList notification) {
        ChangedObject changedObject = objt;
        BufferDBChangeParamList bufferDBChangeParamList = downloadlist;
        boolean z = isGoforward;
        ParamNmsNotificationList paramNmsNotificationList = notification;
        if (!CloudMessageStrategyManager.getStrategy().isNmsEventHasMessageDetail()) {
            long j = summaryrowId;
            bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(7, summaryrowId, isGoforward, line));
        } else if (CloudMessageStrategyManager.getStrategy().requiresInterworkingCrossSearch()) {
            int contractTypeFromLegacy = crossChangedObjectSearchLegacy(changedObject, z, paramNmsNotificationList);
            if (contractTypeFromLegacy != 1) {
                this.mSummaryDB.updateSummaryDbUsingMessageType(summaryrowId, contractTypeFromLegacy);
                return;
            }
            long j2 = summaryrowId;
            if (isLongSmsPushNotification(changedObject, paramNmsNotificationList.mDataType)) {
                bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(7, summaryrowId, isGoforward, line));
            } else {
                insertRcsMessageUseNmsEventwithMessageContent(new ParamOMAObject(changedObject, z, paramNmsNotificationList.mDataContractType, paramNmsNotificationList.mDataType), z);
            }
        } else {
            long j3 = summaryrowId;
            insertRcsMessageUseNmsEventwithMessageContent(new ParamOMAObject(changedObject, z, paramNmsNotificationList.mDataContractType, paramNmsNotificationList.mDataType), z);
        }
    }

    private boolean isLongSmsPushNotification(ChangedObject objt, String dataType) {
        if (!CloudMessageProviderContract.DataTypes.CHAT.equalsIgnoreCase(dataType) || objt == null || objt.extendedMessage == null || objt.extendedMessage.content == null || !TextUtils.isEmpty(objt.extendedMessage.content[0].content)) {
            return false;
        }
        return true;
    }

    private int crossChangedObjectSearchLegacy(ChangedObject objt, boolean isGoforward, ParamNmsNotificationList notification) {
        if (CloudMessageProviderContract.DataTypes.CHAT.equalsIgnoreCase(notification.mDataType)) {
            if (this.mSmsScheduler.handleCrossSearchChangedObj(objt, isGoforward)) {
                return 3;
            }
            if (this.mMmsScheduler.handleCrossSearchChangedObj(objt, isGoforward)) {
                return 4;
            }
            return 1;
        } else if (!"FT".equalsIgnoreCase(notification.mDataType) || !this.mMmsScheduler.handleCrossSearchChangedObj(objt, isGoforward)) {
            return 1;
        } else {
            return 4;
        }
    }

    private int crossObjectSearchLegacy(ParamOMAObject objt, String line, boolean isGoforward) {
        if (objt.correlationTag == null && objt.TEXT_CONTENT != null) {
            this.mSmsScheduler.updateCorrelationTagObject(objt);
        }
        if (objt.correlationTag != null && this.mSmsScheduler.handleCrossSearchObj(objt, line, isGoforward)) {
            return 3;
        }
        if (objt.correlationTag != null || objt.correlationId == null) {
            return 1;
        }
        if ((objt.TEXT_CONTENT == null || objt.TEXT_CONTENT.isEmpty()) && this.mMmsScheduler.handleCrossSearchObj(objt, line, isGoforward)) {
            return 4;
        }
        return 1;
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:41:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject r6) {
        /*
            r5 = this;
            java.net.URL r0 = r6.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r5.mSummaryDB
            java.net.URL r2 = r6.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.querySummaryDBwithResUrl(r2)
            if (r1 == 0) goto L_0x003b
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x0089 }
            if (r2 == 0) goto L_0x003b
            java.lang.String r2 = "syncaction"
            int r2 = r1.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x0089 }
            int r2 = r1.getInt(r2)     // Catch:{ all -> 0x0089 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x0089 }
            int r3 = r3.getId()     // Catch:{ all -> 0x0089 }
            if (r2 != r3) goto L_0x0037
            if (r1 == 0) goto L_0x0036
            r1.close()
        L_0x0036:
            return
        L_0x0037:
            r5.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(r1, r6)     // Catch:{ all -> 0x0089 }
            goto L_0x0083
        L_0x003b:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r5.mSummaryDB     // Catch:{ all -> 0x0089 }
            r3 = 1
            r2.insertNmsEventDeletedObjToSummaryDB(r6, r3)     // Catch:{ all -> 0x0089 }
            java.lang.String r2 = r6.correlationId     // Catch:{ all -> 0x0089 }
            if (r2 == 0) goto L_0x0083
            java.lang.String r2 = TAG     // Catch:{ all -> 0x0089 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0089 }
            r3.<init>()     // Catch:{ all -> 0x0089 }
            java.lang.String r4 = "handleCloudNotifyDeletedObj MMS RCS CloudUpdate: "
            r3.append(r4)     // Catch:{ all -> 0x0089 }
            java.lang.String r4 = r6.correlationId     // Catch:{ all -> 0x0089 }
            r3.append(r4)     // Catch:{ all -> 0x0089 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0089 }
            android.util.Log.d(r2, r3)     // Catch:{ all -> 0x0089 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r5.mBufferDbQuery     // Catch:{ all -> 0x0089 }
            java.lang.String r3 = r6.correlationId     // Catch:{ all -> 0x0089 }
            android.database.Cursor r2 = r2.searchIMFTBufferUsingImdn(r3, r0)     // Catch:{ all -> 0x0089 }
            if (r2 == 0) goto L_0x007e
            boolean r3 = r2.moveToFirst()     // Catch:{ all -> 0x0072 }
            if (r3 == 0) goto L_0x007e
            r3 = 0
            r5.onNmsEventDeletedObjBufferDbRcsAvailableUsingImdnId(r2, r6, r3)     // Catch:{ all -> 0x0072 }
            goto L_0x007e
        L_0x0072:
            r3 = move-exception
            if (r2 == 0) goto L_0x007d
            r2.close()     // Catch:{ all -> 0x0079 }
            goto L_0x007d
        L_0x0079:
            r4 = move-exception
            r3.addSuppressed(r4)     // Catch:{ all -> 0x0089 }
        L_0x007d:
            throw r3     // Catch:{ all -> 0x0089 }
        L_0x007e:
            if (r2 == 0) goto L_0x0083
            r2.close()     // Catch:{ all -> 0x0089 }
        L_0x0083:
            if (r1 == 0) goto L_0x0088
            r1.close()
        L_0x0088:
            return
        L_0x0089:
            r2 = move-exception
            if (r1 == 0) goto L_0x0094
            r1.close()     // Catch:{ all -> 0x0090 }
            goto L_0x0094
        L_0x0090:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x0094:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject):void");
    }

    private class RcsDbSessionObserver extends ContentObserver {
        public RcsDbSessionObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            String chatId = uri.getLastPathSegment();
            String access$000 = RcsScheduler.TAG;
            Log.d(access$000, "RcsDbSessionObserver chatId: " + chatId);
            ContentValues cvupdate = new ContentValues();
            Cursor cs = RcsScheduler.this.mBufferDbQuery.querySessionUsingChatId(chatId);
            if (cs != null) {
                try {
                    if (cs.moveToFirst()) {
                        String iconPath = cs.getString(cs.getColumnIndexOrThrow(ImContract.ImSession.ICON_PATH));
                        String iconParticipant = cs.getString(cs.getColumnIndexOrThrow(ImContract.ImSession.ICON_PARTICIPANT));
                        String iconTimeStamp = cs.getString(cs.getColumnIndexOrThrow(ImContract.ImSession.ICON_TIMESTAMP));
                        cvupdate.put(ImContract.ImSession.ICON_PATH, iconPath);
                        cvupdate.put(ImContract.ImSession.ICON_PARTICIPANT, iconParticipant);
                        cvupdate.put(ImContract.ImSession.ICON_TIMESTAMP, iconTimeStamp);
                        RcsScheduler.this.mBufferDbQuery.updateSessionBufferDb(chatId, cvupdate);
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
    }

    private void registerRcsDbSessionObserver(Looper looper) {
        if (this.mRcsDbSessionObserver == null) {
            this.mRcsDbSessionObserver = new RcsDbSessionObserver(this);
            this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.samsung.rcs.cmstore/chat/"), true, this.mRcsDbSessionObserver);
        }
    }
}
