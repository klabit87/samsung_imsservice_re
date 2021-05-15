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
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.NotifyObject;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamNmsNotificationList;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import com.sec.internal.omanetapi.nms.data.NmsEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.mail.MessagingException;

public class VVMScheduler extends BaseMessagingScheduler {
    private static final String TAG = VVMScheduler.class.getSimpleName();
    private final VVMQueryBuilder mBufferDbQuery;

    public VVMScheduler(Context context, CloudMessageBufferDBEventSchedulingRule rule, SummaryQueryBuilder builder, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, Looper looper) {
        super(context, rule, deviceDataListener, callback, looper, builder);
        this.mBufferDbQuery = new VVMQueryBuilder(context, callback);
        this.mDbTableContractIndex = 17;
    }

    public void onNotificationReceived(ParamNmsNotificationList notification) {
        Log.i(TAG, "onNotificationReceived: " + notification);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        if (!(notification.mNmsEventList == null || notification.mNmsEventList.nmsEvent == null)) {
            for (NmsEvent event : notification.mNmsEventList.nmsEvent) {
                if (event.changedObject != null) {
                    if (notification.mDataContractType == 17) {
                        handleCloudNotifyChangedObjVVM(event.changedObject, downloadlist);
                    } else if (notification.mDataContractType == 18) {
                        handleCloudNotifyChangedObjGreeting(event.changedObject, downloadlist);
                    }
                }
                if (event.deletedObject != null) {
                    handleCloudNotifyDeletedObj(event.deletedObject, notification.mDataContractType);
                }
                if (event.notifyObject != null) {
                    handleCloudNotifyObject(event.notifyObject, notification.mDataContractType, notification.mLine);
                }
            }
        }
        if (downloadlist.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(downloadlist);
        }
    }

    public void onNotifyObjectReceived(String line) {
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMGREETING, this.mBufferDbQuery.insertDefaultGreetingValues(line));
    }

    public void handleVvmProfileDownloaded(ParamOMAresponseforBufDB para) {
        VvmProfileAttributes attributes;
        if (para.getBufferDBChangeParam() != null && para.getVvmServiceProfile() != null && (attributes = parseDownloadedVvmAttributes(para.getVvmServiceProfile())) != null) {
            String[] selectionArgs = {String.valueOf(para.getBufferDBChangeParam().mRowId)};
            ContentValues cv = new ContentValues();
            cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.VVMON, attributes.VVMOn);
            cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.ISBLOCKED, attributes.IsBlocked);
            cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.COS, attributes.COSName);
            cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.LANGUAGE, attributes.Language);
            cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.NUT, attributes.NUT);
            if (attributes.EmailAddress.size() == 1) {
                cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR1, attributes.EmailAddress.get(0));
            } else if (attributes.EmailAddress.size() == 2) {
                cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR1, attributes.EmailAddress.get(0));
                cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR2, attributes.EmailAddress.get(1));
            }
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
            this.mBufferDbQuery.updateTable(20, cv, "_bufferdbid=?", selectionArgs);
            this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPROFILE, para.getBufferDBChangeParam().mRowId);
        }
    }

    private static class VvmProfileAttributes {
        String COSName;
        ArrayList<String> EmailAddress;
        String IsBlocked;
        String Language;
        String NUT;
        String VVMOn;

        VvmProfileAttributes() {
            this.VVMOn = null;
            this.IsBlocked = null;
            this.COSName = null;
            this.Language = "eng";
            this.NUT = null;
            this.EmailAddress = null;
            this.EmailAddress = new ArrayList<>();
        }
    }

    private VvmProfileAttributes parseDownloadedVvmAttributes(VvmServiceProfile profile) {
        if (profile.attributes == null || profile.attributes.attribute == null) {
            Log.i(TAG, "parseDownloadedVvmAttributes: invalid profile");
            return null;
        }
        VvmProfileAttributes attribute = new VvmProfileAttributes();
        for (int i = 0; i < profile.attributes.attribute.length; i++) {
            if (!(profile.attributes.attribute[i].name == null || profile.attributes.attribute[i].value[0] == null)) {
                if ("cosname".equalsIgnoreCase(profile.attributes.attribute[i].name)) {
                    attribute.COSName = profile.attributes.attribute[i].value[0];
                } else if (CloudMessageProviderContract.VVMAccountInfoColumns.ISBLOCKED.equalsIgnoreCase(profile.attributes.attribute[i].name)) {
                    attribute.IsBlocked = profile.attributes.attribute[i].value[0];
                } else if (CloudMessageProviderContract.VVMAccountInfoColumns.LANGUAGE.equalsIgnoreCase(profile.attributes.attribute[i].name)) {
                    attribute.Language = profile.attributes.attribute[i].value[0];
                } else if (CloudMessageProviderContract.VVMAccountInfoColumns.NUT.equalsIgnoreCase(profile.attributes.attribute[i].name)) {
                    attribute.NUT = profile.attributes.attribute[i].value[0];
                } else if (CloudMessageProviderContract.VVMAccountInfoColumns.VVMON.equalsIgnoreCase(profile.attributes.attribute[i].name)) {
                    attribute.VVMOn = profile.attributes.attribute[i].value[0];
                } else if ("EmailAddress".equalsIgnoreCase(profile.attributes.attribute[i].name)) {
                    for (String add : profile.attributes.attribute[i].value) {
                        attribute.EmailAddress.add(add);
                    }
                }
            }
        }
        return attribute;
    }

    private void handleCloudNotifyObject(NotifyObject objt, int dataContractType, String line) {
        String str = TAG;
        Log.i(str, "handleCloudNotifyObject: " + dataContractType + " line: " + IMSLog.checker(line));
        if (dataContractType == 19) {
            ParamVvmUpdate updatepin = new ParamVvmUpdate();
            updatepin.mLine = line;
            updatepin.mNewPwd = objt.extendedMessage.vvmPin;
            onUpdateNewPin(updatepin);
        } else if (dataContractType == 20) {
            try {
                ParamVvmUpdate updateemail = new ParamVvmUpdate();
                updateemail.mLine = line;
                updateemail.mEmail1 = objt.extendedMessage.emailAddress;
                onUpdateNewEmailProfile(updateemail);
            } catch (NullPointerException e) {
                String str2 = TAG;
                Log.e(str2, "NullPointerException: " + e.getMessage());
            }
        }
    }

    private void onUpdateNewPin(ParamVvmUpdate update) {
        String str = TAG;
        Log.i(str, "onUpdateNewPin: " + update);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPIN, this.mBufferDbQuery.insertVvmNewPinCloudUpdate(update));
    }

    private void onUpdateNewEmailProfile(ParamVvmUpdate update) {
        String str = TAG;
        Log.i(str, "onUpdateNewEmailProfile: " + update);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPROFILE, this.mBufferDbQuery.insertVvmNewEmailProfileCloudUpdate(update));
    }

    public void handleUpdateVVMResponse(ParamOMAresponseforBufDB paramOMAObj, boolean isSuccess) {
        String str = TAG;
        Log.i(str, "handleUpdateVVMResponse: " + paramOMAObj + ", isSuccess: " + isSuccess);
        if (isSuccess) {
            switch (paramOMAObj.getBufferDBChangeParam().mDBIndex) {
                case 18:
                    onVvmGreetingUpdateSuccess(paramOMAObj);
                    return;
                case 19:
                    onVvmPINUpdateSuccess(paramOMAObj);
                    return;
                case 20:
                    onVvmProfileUpdateSuccess(paramOMAObj);
                    return;
                default:
                    return;
            }
        } else {
            switch (paramOMAObj.getBufferDBChangeParam().mDBIndex) {
                case 18:
                    onVvmGreetingUpdateFailure(paramOMAObj);
                    return;
                case 19:
                    onVvmPINUpdateFailure(paramOMAObj);
                    return;
                case 20:
                    onVvmProfileUpdateFailure(paramOMAObj);
                    return;
                default:
                    return;
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 32 */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0196 A[SYNTHETIC, Splitter:B:46:0x0196] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectVvmMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r33, boolean r34) {
        /*
            r32 = this;
            r10 = r32
            r11 = r33
            java.lang.String r0 = "syncdirection"
            java.lang.String r1 = "syncaction"
            java.lang.String r2 = TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "handleObjectVvmMessageCloudSearch: "
            r3.append(r4)
            r3.append(r11)
            java.lang.String r4 = ", mIsGoforwardSync: "
            r3.append(r4)
            r12 = r34
            r3.append(r12)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
            r13 = -1
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r2 = r10.mBufferDbQuery     // Catch:{ NullPointerException -> 0x01a4 }
            java.net.URL r3 = r11.resourceURL     // Catch:{ NullPointerException -> 0x01a4 }
            java.lang.String r3 = r3.toString()     // Catch:{ NullPointerException -> 0x01a4 }
            android.database.Cursor r2 = r2.queryVvmMessageBufferDBwithResUrl(r3)     // Catch:{ NullPointerException -> 0x01a4 }
            r15 = r2
            java.net.URL r2 = r11.resourceURL     // Catch:{ all -> 0x0190 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0190 }
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r2)     // Catch:{ all -> 0x0190 }
            r9 = r2
            if (r15 == 0) goto L_0x016c
            boolean r3 = r15.moveToFirst()     // Catch:{ all -> 0x0190 }
            if (r3 == 0) goto L_0x016c
            java.lang.String r3 = "_bufferdbid"
            int r3 = r15.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0190 }
            int r3 = r15.getInt(r3)     // Catch:{ all -> 0x0190 }
            long r7 = (long) r3     // Catch:{ all -> 0x0190 }
            java.lang.String r3 = "_id"
            int r3 = r15.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0190 }
            int r3 = r15.getInt(r3)     // Catch:{ all -> 0x0190 }
            r6 = r3
            int r3 = r15.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0190 }
            int r3 = r15.getInt(r3)     // Catch:{ all -> 0x0190 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r21 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r3)     // Catch:{ all -> 0x0190 }
            int r3 = r15.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0190 }
            int r3 = r15.getInt(r3)     // Catch:{ all -> 0x0190 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r20 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r3)     // Catch:{ all -> 0x0190 }
            java.lang.String r3 = "_bufferdbid=?"
            r5 = r3
            r3 = 1
            java.lang.String[] r4 = new java.lang.String[r3]     // Catch:{ all -> 0x0190 }
            java.lang.String r16 = java.lang.String.valueOf(r7)     // Catch:{ all -> 0x0190 }
            r2 = 0
            r4[r2] = r16     // Catch:{ all -> 0x0190 }
            android.content.ContentValues r16 = new android.content.ContentValues     // Catch:{ all -> 0x0190 }
            r16.<init>()     // Catch:{ all -> 0x0190 }
            r29 = r16
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x0190 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x0190 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r12 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x0190 }
            r3.<init>(r2, r12)     // Catch:{ all -> 0x0190 }
            r2 = r3
            r3 = 0
            r2.mIsChanged = r3     // Catch:{ all -> 0x0190 }
            java.lang.String r3 = "flagRead"
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r12 = r10.mBufferDbQuery     // Catch:{ all -> 0x0190 }
            r30 = r2
            com.sec.internal.omanetapi.nms.data.FlagList r2 = r11.mFlagList     // Catch:{ all -> 0x0190 }
            int r2 = r12.getIfSeenValueUsingFlag(r2)     // Catch:{ all -> 0x0190 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x0190 }
            r12 = r29
            r12.put(r3, r2)     // Catch:{ all -> 0x0190 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x0190 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = r11.mFlag     // Catch:{ all -> 0x0190 }
            boolean r2 = r2.equals(r3)     // Catch:{ all -> 0x0190 }
            if (r2 == 0) goto L_0x00d4
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r2 = r10.mScheduleRule     // Catch:{ all -> 0x00ce }
            int r3 = r10.mDbTableContractIndex     // Catch:{ all -> 0x00ce }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r22 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x00ce }
            r16 = r2
            r17 = r3
            r18 = r7
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = r16.getSetFlagsForCldOperation(r17, r18, r20, r21, r22)     // Catch:{ all -> 0x00ce }
            r3 = r2
            goto L_0x010f
        L_0x00ce:
            r0 = move-exception
            r1 = r0
            r18 = r13
            goto L_0x0194
        L_0x00d4:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x0190 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = r11.mFlag     // Catch:{ all -> 0x0190 }
            boolean r2 = r2.equals(r3)     // Catch:{ all -> 0x0190 }
            if (r2 == 0) goto L_0x00f4
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r2 = r10.mScheduleRule     // Catch:{ all -> 0x00ce }
            int r3 = r10.mDbTableContractIndex     // Catch:{ all -> 0x00ce }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r28 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x00ce }
            r22 = r2
            r23 = r3
            r24 = r7
            r26 = r20
            r27 = r21
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = r22.getSetFlagsForCldOperation(r23, r24, r26, r27, r28)     // Catch:{ all -> 0x00ce }
            r3 = r2
            goto L_0x010f
        L_0x00f4:
            r2 = 1
            if (r6 >= r2) goto L_0x010d
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r2 = r10.mScheduleRule     // Catch:{ all -> 0x00ce }
            int r3 = r10.mDbTableContractIndex     // Catch:{ all -> 0x00ce }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r28 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x00ce }
            r22 = r2
            r23 = r3
            r24 = r7
            r26 = r20
            r27 = r21
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = r22.getSetFlagsForCldOperation(r23, r24, r26, r27, r28)     // Catch:{ all -> 0x00ce }
            r3 = r2
            goto L_0x010f
        L_0x010d:
            r3 = r30
        L_0x010f:
            boolean r2 = r3.mIsChanged     // Catch:{ all -> 0x0190 }
            if (r2 == 0) goto L_0x0155
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = r3.mAction     // Catch:{ all -> 0x0190 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0190 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x0190 }
            r12.put(r1, r2)     // Catch:{ all -> 0x0190 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r3.mDirection     // Catch:{ all -> 0x0190 }
            int r1 = r1.getId()     // Catch:{ all -> 0x0190 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0190 }
            r12.put(r0, r1)     // Catch:{ all -> 0x0190 }
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x0190 }
            r1 = 17
            r0.updateTable(r1, r12, r5, r4)     // Catch:{ all -> 0x0190 }
            r0 = 17
            r16 = 0
            r17 = 0
            r1 = r32
            r2 = r3
            r30 = r3
            r31 = r4
            r3 = r7
            r18 = r13
            r13 = r5
            r5 = r0
            r0 = r6
            r6 = r16
            r22 = r7
            r7 = r34
            r8 = r9
            r14 = r9
            r9 = r17
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x018d }
            goto L_0x0169
        L_0x0155:
            r30 = r3
            r31 = r4
            r0 = r6
            r22 = r7
            r18 = r13
            r13 = r5
            r14 = r9
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x018d }
            r2 = r31
            r3 = 17
            r1.updateTable(r3, r12, r13, r2)     // Catch:{ all -> 0x018d }
        L_0x0169:
            r13 = r18
            goto L_0x0184
        L_0x016c:
            r18 = r13
            r14 = r9
            java.lang.String r0 = TAG     // Catch:{ all -> 0x018d }
            java.lang.String r1 = "handleObjectVvmMessageCloudSearch: vvm not found: "
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x018d }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r10.mSummaryDB     // Catch:{ all -> 0x018d }
            r1 = 17
            r0.insertSummaryDbUsingObjectIfNonExist(r11, r1)     // Catch:{ all -> 0x018d }
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x018d }
            long r0 = r0.insertVvmMessageUsingObject(r11, r14)     // Catch:{ all -> 0x018d }
            r13 = r0
        L_0x0184:
            if (r15 == 0) goto L_0x018c
            r15.close()     // Catch:{ NullPointerException -> 0x018a }
            goto L_0x018c
        L_0x018a:
            r0 = move-exception
            goto L_0x01a7
        L_0x018c:
            goto L_0x01b0
        L_0x018d:
            r0 = move-exception
            r1 = r0
            goto L_0x0194
        L_0x0190:
            r0 = move-exception
            r18 = r13
            r1 = r0
        L_0x0194:
            if (r15 == 0) goto L_0x019f
            r15.close()     // Catch:{ all -> 0x019a }
            goto L_0x019f
        L_0x019a:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x01a0 }
        L_0x019f:
            throw r1     // Catch:{ NullPointerException -> 0x01a0 }
        L_0x01a0:
            r0 = move-exception
            r13 = r18
            goto L_0x01a7
        L_0x01a4:
            r0 = move-exception
            r18 = r13
        L_0x01a7:
            java.lang.String r1 = TAG
            java.lang.String r2 = r0.toString()
            android.util.Log.e(r1, r2)
        L_0x01b0:
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.handleObjectVvmMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject, boolean):long");
    }

    public long handleObjectVvmGreetingCloudSearch(ParamOMAObject objt) {
        String str = TAG;
        Log.i(str, "handleObjectVvmGreetingCloudSearch: " + objt);
        try {
            String line = Util.getLineTelUriFromObjUrl(objt.resourceURL.toString());
            this.mSummaryDB.insertSummaryDbUsingObjectIfNonExist(objt, 18);
            return this.mBufferDbQuery.insertVvmGreetingUsingObject(objt, line);
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x00c7 A[SYNTHETIC, Splitter:B:20:0x00c7] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleNormalSyncDownloadedVVMGreeting(com.sec.internal.ims.cmstore.params.ParamOMAObject r12) {
        /*
            r11 = this;
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleNormalSyncDownloadedVVMGreeting: "
            r1.append(r2)
            r1.append(r12)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r0 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList
            r0.<init>()
            r1 = 0
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r3 = r11.mBufferDbQuery
            java.net.URL r4 = r12.resourceURL
            java.lang.String r4 = r4.toString()
            android.database.Cursor r3 = r3.queryVvmGreetingBufferDBwithResUrl(r4)
            java.net.URL r4 = r12.resourceURL     // Catch:{ all -> 0x00c4 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00c4 }
            java.lang.String r4 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r4)     // Catch:{ all -> 0x00c4 }
            if (r3 == 0) goto L_0x0098
            boolean r5 = r3.moveToFirst()     // Catch:{ all -> 0x00c4 }
            if (r5 == 0) goto L_0x0098
            java.lang.String r5 = "_bufferdbid"
            int r5 = r3.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x00c4 }
            long r5 = r3.getLong(r5)     // Catch:{ all -> 0x00c4 }
            r1 = r5
            android.content.ContentValues r5 = new android.content.ContentValues     // Catch:{ all -> 0x00c4 }
            r5.<init>()     // Catch:{ all -> 0x00c4 }
            java.lang.String r6 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x00c4 }
            int r7 = r7.getId()     // Catch:{ all -> 0x00c4 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x00c4 }
            r5.put(r6, r7)     // Catch:{ all -> 0x00c4 }
            java.lang.String r6 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice     // Catch:{ all -> 0x00c4 }
            int r7 = r7.getId()     // Catch:{ all -> 0x00c4 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x00c4 }
            r5.put(r6, r7)     // Catch:{ all -> 0x00c4 }
            java.lang.String r6 = "uploadstatus"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$UploadStatusFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS     // Catch:{ all -> 0x00c4 }
            int r7 = r7.getId()     // Catch:{ all -> 0x00c4 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x00c4 }
            r5.put(r6, r7)     // Catch:{ all -> 0x00c4 }
            java.lang.String r6 = "_bufferdbid=?"
            r7 = 1
            java.lang.String[] r7 = new java.lang.String[r7]     // Catch:{ all -> 0x00c4 }
            r8 = 0
            java.lang.String r9 = java.lang.String.valueOf(r1)     // Catch:{ all -> 0x00c4 }
            r7[r8] = r9     // Catch:{ all -> 0x00c4 }
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r8 = r11.mBufferDbQuery     // Catch:{ all -> 0x00c4 }
            r9 = 18
            r8.updateTable(r9, r5, r6, r7)     // Catch:{ all -> 0x00c4 }
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r8 = r11.mBufferDbQuery     // Catch:{ all -> 0x00c4 }
            java.lang.String r9 = "VVMDATA"
            java.lang.String r10 = "GREETING"
            r8.notifyApplication(r9, r10, r1)     // Catch:{ all -> 0x00c4 }
            goto L_0x00ae
        L_0x0098:
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r5 = r11.mBufferDbQuery     // Catch:{ all -> 0x00c4 }
            long r7 = r5.insertVvmGreetingUsingObject(r12, r4)     // Catch:{ all -> 0x00c4 }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r1 = r0.mChangelst     // Catch:{ all -> 0x00c1 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x00c1 }
            r6 = 18
            r9 = 0
            r5 = r2
            r10 = r4
            r5.<init>(r6, r7, r9, r10)     // Catch:{ all -> 0x00c1 }
            r1.add(r2)     // Catch:{ all -> 0x00c1 }
            r1 = r7
        L_0x00ae:
            if (r3 == 0) goto L_0x00b3
            r3.close()
        L_0x00b3:
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r3 = r0.mChangelst
            int r3 = r3.size()
            if (r3 <= 0) goto L_0x00c0
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r3 = r11.mDeviceDataChangeListener
            r3.sendDeviceNormalSyncDownload(r0)
        L_0x00c0:
            return
        L_0x00c1:
            r4 = move-exception
            r1 = r7
            goto L_0x00c5
        L_0x00c4:
            r4 = move-exception
        L_0x00c5:
            if (r3 == 0) goto L_0x00cf
            r3.close()     // Catch:{ all -> 0x00cb }
            goto L_0x00cf
        L_0x00cb:
            r5 = move-exception
            r4.addSuppressed(r5)
        L_0x00cf:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.handleNormalSyncDownloadedVVMGreeting(com.sec.internal.ims.cmstore.params.ParamOMAObject):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyChangedObjGreeting(com.sec.internal.omanetapi.nms.data.ChangedObject r11, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r12) {
        /*
            r10 = this;
            java.lang.String r0 = TAG
            java.lang.String r1 = "handleCloudNotifyChangedObjGreeting()"
            android.util.Log.i(r0, r1)
            java.net.URL r0 = r11.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r10.mSummaryDB
            java.net.URL r2 = r11.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r7 = r1.querySummaryDBwithResUrl(r2)
            if (r7 == 0) goto L_0x0047
            boolean r1 = r7.moveToFirst()     // Catch:{ all -> 0x0063 }
            if (r1 == 0) goto L_0x0047
            java.lang.String r1 = "syncaction"
            int r1 = r7.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0063 }
            int r1 = r7.getInt(r1)     // Catch:{ all -> 0x0063 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x0063 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0063 }
            if (r1 != r2) goto L_0x0046
            java.lang.String r2 = TAG     // Catch:{ all -> 0x0063 }
            java.lang.String r3 = "this is a deleted object"
            android.util.Log.d(r2, r3)     // Catch:{ all -> 0x0063 }
            if (r7 == 0) goto L_0x0045
            r7.close()
        L_0x0045:
            return
        L_0x0046:
            goto L_0x005d
        L_0x0047:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r10.mSummaryDB     // Catch:{ all -> 0x0063 }
            r2 = 18
            long r3 = r1.insertNmsEventChangedObjToSummaryDB(r11, r2)     // Catch:{ all -> 0x0063 }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r8 = r12.mChangelst     // Catch:{ all -> 0x0063 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r9 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x0063 }
            r2 = 7
            r5 = 0
            r1 = r9
            r6 = r0
            r1.<init>(r2, r3, r5, r6)     // Catch:{ all -> 0x0063 }
            r8.add(r9)     // Catch:{ all -> 0x0063 }
        L_0x005d:
            if (r7 == 0) goto L_0x0062
            r7.close()
        L_0x0062:
            return
        L_0x0063:
            r1 = move-exception
            if (r7 == 0) goto L_0x006e
            r7.close()     // Catch:{ all -> 0x006a }
            goto L_0x006e
        L_0x006a:
            r2 = move-exception
            r1.addSuppressed(r2)
        L_0x006e:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.handleCloudNotifyChangedObjGreeting(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:25:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyChangedObjVVM(com.sec.internal.omanetapi.nms.data.ChangedObject r5, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r6) {
        /*
            r4 = this;
            java.lang.String r0 = TAG
            java.lang.String r1 = "handleCloudNotifyChangedObjVVM()"
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
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.handleCloudNotifyChangedObjVVM(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:25:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject r5, int r6) {
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
            r4.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(r0, r5, r6)     // Catch:{ all -> 0x0049 }
            goto L_0x0043
        L_0x0040:
            r4.onNmsEventDeletedObjSummaryDbNotAvailableUsingUrl(r5, r6)     // Catch:{ all -> 0x0049 }
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
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject, int):void");
    }

    private void onNmsEventChangedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, ChangedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventChangedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor vvmCs = queryVVMwithResUrl(objt.resourceURL.toString());
        if (vvmCs != null) {
            try {
                if (vvmCs.moveToFirst()) {
                    int rowid = vvmCs.getInt(vvmCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    int action = vvmCs.getInt(vvmCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                    int localSeen = vvmCs.getInt(vvmCs.getColumnIndexOrThrow("flagRead"));
                    if (CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId() != action) {
                        if (CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId() != action) {
                            int serverSeen = this.mBufferDbQuery.getIfSeenValueUsingFlag(objt.flags);
                            String str2 = TAG;
                            Log.i(str2, "local seen: " + localSeen + ", server seen: " + serverSeen);
                            if (localSeen != serverSeen) {
                                ContentValues cv = new ContentValues();
                                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
                                cv.put("flagRead", Integer.valueOf(serverSeen));
                                this.mBufferDbQuery.updateTable(17, cv, "_bufferdbid=?", new String[]{String.valueOf(rowid)});
                                this.mBufferDbQuery.notifyApplication("VVMDATA", "VVMDATA", (long) rowid);
                            }
                        }
                    }
                    Log.d(TAG, "onNmsEventChangedObjSummaryDbAvailableUsingUrl: delete vvm ignore");
                    if (vvmCs != null) {
                        vvmCs.close();
                        return;
                    }
                    return;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (vvmCs != null) {
            vvmCs.close();
            return;
        }
        return;
        throw th;
    }

    public Cursor queryVVMwithResUrl(String url) {
        return this.mBufferDbQuery.queryVvmMessageBufferDBwithResUrl(url);
    }

    private void onNmsEventChangedObjSummaryDbNotAvailableUsingUrl(ChangedObject objt, BufferDBChangeParamList downloadlist) {
        this.mSummaryDB.insertNmsEventChangedObjToSummaryDB(objt, 17);
        String line = Util.getLineTelUriFromObjUrl(objt.resourceURL.toString());
        downloadlist.mChangelst.add(new BufferDBChangeParam(17, this.mBufferDbQuery.insertVvmMessageUsingChangedObject(objt, line), false, line));
    }

    private void onNmsEventDeletedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, DeletedObject objt, int dataContractType) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventDeletedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor clgCs = null;
        if (dataContractType == 17) {
            try {
                clgCs = this.mBufferDbQuery.queryVvmMessageBufferDBwithResUrl(objt.resourceURL.toString());
            } catch (Throwable th) {
                if (clgCs != null && !clgCs.isClosed()) {
                    clgCs.close();
                }
                throw th;
            }
        } else if (dataContractType == 18) {
            clgCs = this.mBufferDbQuery.queryVvmGreetingBufferDBwithResUrl(objt.resourceURL.toString());
        } else {
            String str2 = TAG;
            Log.e(str2, "onNmsEventDeletedObjSummaryDbAvailableUsingUrl, unrecognized datatype: " + dataContractType);
            if (clgCs != null && !clgCs.isClosed()) {
                clgCs.close();
                return;
            }
            return;
        }
        if (clgCs != null && clgCs.moveToFirst()) {
            int rowid = clgCs.getInt(clgCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
            int action = clgCs.getInt(clgCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
            if (!(CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId() == action || CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId() == action)) {
                ContentValues cv = new ContentValues();
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId()));
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
                String[] selectionArgs = {String.valueOf(rowid)};
                if (dataContractType == 17) {
                    this.mBufferDbQuery.updateTable(17, cv, "_bufferdbid=?", selectionArgs);
                    this.mBufferDbQuery.notifyApplication("VVMDATA", "VVMDATA", (long) rowid);
                } else if (dataContractType == 18) {
                    this.mBufferDbQuery.updateTable(18, cv, "_bufferdbid=?", selectionArgs);
                    this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMGREETING, (long) rowid);
                }
            }
        }
        if (clgCs != null && !clgCs.isClosed()) {
            clgCs.close();
        }
    }

    private void onNmsEventDeletedObjSummaryDbNotAvailableUsingUrl(DeletedObject objt, int dataContractType) {
        this.mSummaryDB.insertNmsEventDeletedObjToSummaryDB(objt, dataContractType);
    }

    public void handleNormalSyncDownloadedVVMMessage(ParamOMAObject para) {
        String str = TAG;
        Log.i(str, "handleNormalSyncDownloadedVVMMessage: " + para);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        Cursor mmsCs = queryVVMwithResUrl(para.resourceURL.toString());
        if (mmsCs != null) {
            try {
                if (mmsCs.moveToFirst()) {
                    int rowid = mmsCs.getInt(mmsCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    ContentValues cv = new ContentValues();
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, para.payloadURL.toString());
                    String[] selectionArgs = {String.valueOf(rowid)};
                    String line = Util.getLineTelUriFromObjUrl(para.resourceURL.toString());
                    this.mBufferDbQuery.updateTable(17, cv, "_bufferdbid=?", selectionArgs);
                    downloadlist.mChangelst.add(new BufferDBChangeParam(17, (long) rowid, false, line));
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

    public void onUpdateFromDeviceMsgAppFetch(DeviceMsgAppFetchUpdateParam para, boolean mIsGoforwardSync) {
        onUpdateFromDeviceMsgAppFetch(para, mIsGoforwardSync, this.mBufferDbQuery);
    }

    /* Debug info: failed to restart local var, previous not found, register: 17 */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01a9 A[SYNTHETIC, Splitter:B:66:0x01a9] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01dc A[SYNTHETIC, Splitter:B:89:0x01dc] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onVvmAllPayloadDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r18, boolean r19) {
        /*
            r17 = this;
            r10 = r17
            java.lang.String r0 = "text"
            if (r18 == 0) goto L_0x01ef
            java.util.List r1 = r18.getAllPayloads()
            if (r1 == 0) goto L_0x01ef
            java.util.List r1 = r18.getAllPayloads()
            int r1 = r1.size()
            r2 = 1
            if (r1 >= r2) goto L_0x001a
            goto L_0x01ef
        L_0x001a:
            r1 = 0
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r3 = r10.mBufferDbQuery     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01cf }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r4 = r18.getBufferDBChangeParam()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01cf }
            int r4 = r4.mDBIndex     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01cf }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r5 = r18.getBufferDBChangeParam()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01cf }
            long r5 = r5.mRowId     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01cf }
            android.database.Cursor r3 = r3.queryTablewithBufferDbId(r4, r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01cf }
            r11 = r3
            if (r11 == 0) goto L_0x01b3
            boolean r3 = r11.moveToFirst()     // Catch:{ all -> 0x01a5 }
            if (r3 == 0) goto L_0x01b3
            java.lang.String r3 = "_bufferdbid"
            int r3 = r11.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x01a5 }
            int r3 = r11.getInt(r3)     // Catch:{ all -> 0x01a5 }
            r12 = r3
            java.lang.String r3 = "linenum"
            int r3 = r11.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x01a5 }
            java.lang.String r8 = r11.getString(r3)     // Catch:{ all -> 0x01a5 }
            java.lang.String r3 = "_bufferdbid= ?"
            r13 = r3
            java.lang.String[] r2 = new java.lang.String[r2]     // Catch:{ all -> 0x01a5 }
            java.lang.String r3 = java.lang.String.valueOf(r12)     // Catch:{ all -> 0x01a5 }
            r4 = 0
            r2[r4] = r3     // Catch:{ all -> 0x01a5 }
            r14 = r2
            android.content.ContentValues r2 = new android.content.ContentValues     // Catch:{ all -> 0x01a5 }
            r2.<init>()     // Catch:{ all -> 0x01a5 }
            r15 = r2
            r2 = 0
            r16 = r1
        L_0x0061:
            java.util.List r1 = r18.getAllPayloads()     // Catch:{ all -> 0x01a0 }
            int r1 = r1.size()     // Catch:{ all -> 0x01a0 }
            if (r2 >= r1) goto L_0x015b
            java.util.List r1 = r18.getAllPayloads()     // Catch:{ all -> 0x01a0 }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x01a0 }
            javax.mail.BodyPart r1 = (javax.mail.BodyPart) r1     // Catch:{ all -> 0x01a0 }
            java.lang.String r1 = r1.getContentType()     // Catch:{ all -> 0x01a0 }
            java.lang.String r3 = TAG     // Catch:{ all -> 0x01a0 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a0 }
            r5.<init>()     // Catch:{ all -> 0x01a0 }
            java.lang.String r6 = "ContentType: "
            r5.append(r6)     // Catch:{ all -> 0x01a0 }
            r5.append(r1)     // Catch:{ all -> 0x01a0 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x01a0 }
            android.util.Log.d(r3, r5)     // Catch:{ all -> 0x01a0 }
            boolean r3 = r1.contains(r0)     // Catch:{ all -> 0x01a0 }
            if (r3 == 0) goto L_0x00b3
            java.util.List r3 = r18.getAllPayloads()     // Catch:{ all -> 0x01a0 }
            java.lang.Object r3 = r3.get(r2)     // Catch:{ all -> 0x01a0 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ all -> 0x01a0 }
            java.io.InputStream r3 = r3.getInputStream()     // Catch:{ all -> 0x01a0 }
            java.lang.String r5 = r10.getTextDatafromInputStream(r3)     // Catch:{ all -> 0x00ae }
            r15.put(r0, r5)     // Catch:{ all -> 0x00ae }
            r16 = r3
            goto L_0x0157
        L_0x00ae:
            r0 = move-exception
            r2 = r0
            r1 = r3
            goto L_0x01a7
        L_0x00b3:
            java.lang.String r3 = "audio"
            boolean r3 = r1.contains(r3)     // Catch:{ all -> 0x01a0 }
            if (r3 == 0) goto L_0x0157
            java.util.List r3 = r18.getAllPayloads()     // Catch:{ all -> 0x01a0 }
            java.lang.Object r3 = r3.get(r2)     // Catch:{ all -> 0x01a0 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ all -> 0x01a0 }
            java.lang.String r3 = r3.getFileName()     // Catch:{ all -> 0x01a0 }
            boolean r5 = android.text.TextUtils.isEmpty(r3)     // Catch:{ all -> 0x01a0 }
            if (r5 == 0) goto L_0x00e3
            java.util.List r5 = r18.getAllPayloads()     // Catch:{ all -> 0x01a0 }
            java.lang.Object r5 = r5.get(r2)     // Catch:{ all -> 0x01a0 }
            javax.mail.BodyPart r5 = (javax.mail.BodyPart) r5     // Catch:{ all -> 0x01a0 }
            java.lang.String r5 = r5.getDisposition()     // Catch:{ all -> 0x01a0 }
            java.lang.String r5 = com.sec.internal.ims.cmstore.utils.Util.getFileNamefromContentType(r5)     // Catch:{ all -> 0x01a0 }
            r3 = r5
        L_0x00e3:
            boolean r5 = android.text.TextUtils.isEmpty(r3)     // Catch:{ all -> 0x01a0 }
            if (r5 == 0) goto L_0x0117
            java.lang.String r0 = TAG     // Catch:{ all -> 0x01a0 }
            java.lang.String r4 = "onVvmPayloadDownloaded: no file name"
            android.util.Log.e(r0, r4)     // Catch:{ all -> 0x01a0 }
            if (r11 == 0) goto L_0x0102
            r11.close()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00fd, all -> 0x00f7 }
            goto L_0x0102
        L_0x00f7:
            r0 = move-exception
            r2 = r0
            r1 = r16
            goto L_0x01da
        L_0x00fd:
            r0 = move-exception
            r1 = r16
            goto L_0x01d0
        L_0x0102:
            if (r16 == 0) goto L_0x0115
            r16.close()     // Catch:{ IOException -> 0x0108 }
            goto L_0x0115
        L_0x0108:
            r0 = move-exception
            r4 = r0
            r0 = r4
            java.lang.String r4 = TAG
            java.lang.String r5 = r0.toString()
            android.util.Log.e(r4, r5)
            goto L_0x0116
        L_0x0115:
        L_0x0116:
            return
        L_0x0117:
            r5 = 0
            android.content.Context r6 = r10.mContext     // Catch:{ all -> 0x01a0 }
            java.lang.String r6 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r6, r3, r4)     // Catch:{ all -> 0x01a0 }
            r5 = r6
            java.lang.String r6 = TAG     // Catch:{ all -> 0x01a0 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a0 }
            r7.<init>()     // Catch:{ all -> 0x01a0 }
            java.lang.String r9 = "generated file path: "
            r7.append(r9)     // Catch:{ all -> 0x01a0 }
            r7.append(r5)     // Catch:{ all -> 0x01a0 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x01a0 }
            android.util.Log.d(r6, r7)     // Catch:{ all -> 0x01a0 }
            java.util.List r6 = r18.getAllPayloads()     // Catch:{ all -> 0x01a0 }
            java.lang.Object r6 = r6.get(r2)     // Catch:{ all -> 0x01a0 }
            javax.mail.BodyPart r6 = (javax.mail.BodyPart) r6     // Catch:{ all -> 0x01a0 }
            java.io.InputStream r6 = r6.getInputStream()     // Catch:{ all -> 0x01a0 }
            com.sec.internal.ims.cmstore.utils.Util.saveInputStreamtoPath(r6, r5)     // Catch:{ all -> 0x0153 }
            java.lang.String r7 = "fileName"
            r15.put(r7, r3)     // Catch:{ all -> 0x0153 }
            java.lang.String r7 = "filepath"
            r15.put(r7, r5)     // Catch:{ all -> 0x0153 }
            r16 = r6
            goto L_0x0157
        L_0x0153:
            r0 = move-exception
            r2 = r0
            r1 = r6
            goto L_0x01a7
        L_0x0157:
            int r2 = r2 + 1
            goto L_0x0061
        L_0x015b:
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r0 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x01a0 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x01a0 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x01a0 }
            r0.<init>(r1, r2)     // Catch:{ all -> 0x01a0 }
            java.lang.String r1 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = r0.mDirection     // Catch:{ all -> 0x01a0 }
            int r2 = r2.getId()     // Catch:{ all -> 0x01a0 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x01a0 }
            r15.put(r1, r2)     // Catch:{ all -> 0x01a0 }
            java.lang.String r1 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = r0.mAction     // Catch:{ all -> 0x01a0 }
            int r2 = r2.getId()     // Catch:{ all -> 0x01a0 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x01a0 }
            r15.put(r1, r2)     // Catch:{ all -> 0x01a0 }
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x01a0 }
            r2 = 17
            r1.updateTable(r2, r15, r13, r14)     // Catch:{ all -> 0x01a0 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r18.getBufferDBChangeParam()     // Catch:{ all -> 0x01a0 }
            long r3 = r1.mRowId     // Catch:{ all -> 0x01a0 }
            r5 = 17
            r6 = 0
            r9 = 0
            r1 = r17
            r2 = r0
            r7 = r19
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x01a0 }
            r1 = r16
            goto L_0x01b3
        L_0x01a0:
            r0 = move-exception
            r2 = r0
            r1 = r16
            goto L_0x01a7
        L_0x01a5:
            r0 = move-exception
            r2 = r0
        L_0x01a7:
            if (r11 == 0) goto L_0x01b2
            r11.close()     // Catch:{ all -> 0x01ad }
            goto L_0x01b2
        L_0x01ad:
            r0 = move-exception
            r3 = r0
            r2.addSuppressed(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01cf }
        L_0x01b2:
            throw r2     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01cf }
        L_0x01b3:
            if (r11 == 0) goto L_0x01b8
            r11.close()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01cf }
        L_0x01b8:
            if (r1 == 0) goto L_0x01cb
            r1.close()     // Catch:{ IOException -> 0x01be }
            goto L_0x01cb
        L_0x01be:
            r0 = move-exception
            r2 = r0
            r0 = r2
            java.lang.String r2 = TAG
            java.lang.String r3 = r0.toString()
            android.util.Log.e(r2, r3)
            goto L_0x01d9
        L_0x01cb:
            goto L_0x01d9
        L_0x01cc:
            r0 = move-exception
            r2 = r0
            goto L_0x01da
        L_0x01cf:
            r0 = move-exception
        L_0x01d0:
            r0.printStackTrace()     // Catch:{ all -> 0x01cc }
            if (r1 == 0) goto L_0x01cb
            r1.close()     // Catch:{ IOException -> 0x01be }
            goto L_0x01cb
        L_0x01d9:
            return
        L_0x01da:
            if (r1 == 0) goto L_0x01ed
            r1.close()     // Catch:{ IOException -> 0x01e0 }
            goto L_0x01ed
        L_0x01e0:
            r0 = move-exception
            r3 = r0
            r0 = r3
            java.lang.String r3 = TAG
            java.lang.String r4 = r0.toString()
            android.util.Log.e(r3, r4)
            goto L_0x01ee
        L_0x01ed:
        L_0x01ee:
            throw r2
        L_0x01ef:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.onVvmAllPayloadDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB, boolean):void");
    }

    private String getTextDatafromInputStream(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (is != null) {
            try {
                byte[] buffer = new byte[256];
                int len = is.read(buffer);
                while (len >= 0) {
                    baos.write(buffer, 0, len);
                    len = is.read(buffer);
                }
            } catch (IOException e) {
                String str = TAG;
                Log.e(str, "getTextDatafromInputStream error: " + e);
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                        String str2 = TAG;
                        Log.e(str2, "getTextDatafromInputStream: error:" + e2);
                    }
                }
                return null;
            } catch (Throwable th) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e3) {
                        String str3 = TAG;
                        Log.e(str3, "getTextDatafromInputStream: error:" + e3);
                    }
                }
                throw th;
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e4) {
                String str4 = TAG;
                Log.e(str4, "getTextDatafromInputStream: error:" + e4);
            }
        }
        String str5 = TAG;
        Log.d(str5, "getTextDatafromInputStream: " + baos.toString());
        return baos.toString();
    }

    /* Debug info: failed to restart local var, previous not found, register: 20 */
    public void onGreetingAllPayloadDownloaded(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync) {
        Throwable th;
        InputStream inputStream;
        Throwable th2;
        String filename;
        if (para != null && para.getAllPayloads() != null && para.getAllPayloads().size() >= 1) {
            try {
                Cursor greetigcs = this.mBufferDbQuery.queryTablewithBufferDbId(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId);
                try {
                    inputStream = para.getAllPayloads().get(0).getInputStream();
                    if (greetigcs != null) {
                        if (greetigcs.moveToFirst()) {
                            int id = greetigcs.getInt(greetigcs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                            String line = greetigcs.getString(greetigcs.getColumnIndexOrThrow("linenum"));
                            String selection = "_bufferdbid= ?";
                            String[] selectionArgs = {String.valueOf(id)};
                            String filename2 = para.getAllPayloads().get(0).getFileName();
                            if (TextUtils.isEmpty(filename2)) {
                                filename = Util.getFileNamefromContentType(para.getAllPayloads().get(0).getDisposition());
                            } else {
                                filename = filename2;
                            }
                            if (TextUtils.isEmpty(filename)) {
                                Log.e(TAG, "onGreetingPayloadDownloaded: no file name");
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                if (greetigcs != null) {
                                    greetigcs.close();
                                    return;
                                }
                                return;
                            }
                            String filepath = Util.generateUniqueFilePath(this.mContext, filename, false);
                            String filepath2 = TAG;
                            Log.d(filepath2, "generated file path: " + filepath);
                            Util.saveFiletoPath(para.getData(), filepath);
                            ContentValues cv = new ContentValues();
                            Util.saveInputStreamtoPath(inputStream, filepath);
                            cv.put("mimeType", para.getAllPayloads().get(0).getContentType());
                            cv.put("fileName", filename);
                            cv.put("filepath", filepath);
                            ParamSyncFlagsSet param = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice, CloudMessageBufferDBConstants.ActionStatusFlag.Insert);
                            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(param.mDirection.getId()));
                            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(param.mAction.getId()));
                            this.mBufferDbQuery.updateTable(18, cv, selection, selectionArgs);
                            ParamSyncFlagsSet paramSyncFlagsSet = param;
                            ContentValues contentValues = cv;
                            String str = filepath;
                            handleOutPutParamSyncFlagSet(param, para.getBufferDBChangeParam().mRowId, 18, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
                        }
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (greetigcs != null) {
                        greetigcs.close();
                        return;
                    }
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    if (greetigcs != null) {
                        greetigcs.close();
                    }
                    throw th;
                }
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

    public void handleDownLoadMessageResponse(ParamOMAresponseforBufDB paramOMAObj, boolean isSuccess) {
        String str = TAG;
        Log.d(str, "handleDownLoadMessageResponse: " + paramOMAObj + ", isSuccess: " + isSuccess);
        if (!isSuccess && ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND.equals(paramOMAObj.getActionType())) {
            this.mBufferDbQuery.setMsgDeleted(paramOMAObj.getBufferDBChangeParam().mDBIndex, paramOMAObj.getBufferDBChangeParam().mRowId);
        }
    }

    public void onAppOperationReceived(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        String str = TAG;
        Log.i(str, "onAppOperationReceived: " + param);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[param.mOperation.ordinal()];
        if (i == 1) {
            handleUploadVvm(param);
        } else if (i == 2) {
            handleReadVvm(param, changelist);
        } else if (i == 3) {
            handleUnReadVvm(param, changelist);
        } else if (i == 4) {
            handledeleteVvm(param, changelist);
        } else if (i == 5) {
            onDownloadRequestFromApp(param);
        }
    }

    private void onDownloadRequestFromApp(ParamAppJsonValue param) {
        if (param != null && param.mVvmUpdate.mVvmChange != null && ParamVvmUpdate.VvmTypeChange.FULLPROFILE.equals(param.mVvmUpdate.mVvmChange)) {
            long rowId = this.mBufferDbQuery.insertDownloadNewProfileRequest(param.mVvmUpdate);
            if (rowId > 0) {
                BufferDBChangeParamList list = new BufferDBChangeParamList();
                list.mChangelst.add(new BufferDBChangeParam(20, rowId, false, param.mLine));
                this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(list);
            }
        }
    }

    private void handleReadVvm(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        Cursor cs = this.mBufferDbQuery.queryVvmMessageBufferDBwithAppId((long) param.mRowId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    ContentValues cv = new ContentValues();
                    String line = cs.getString(cs.getColumnIndexOrThrow("linenum"));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                    cv.put("flagRead", 1);
                    long bufferDbId = cs.getLong(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    this.mBufferDbQuery.updateTable(param.mDataContractType, cv, "_bufferdbid=?", new String[]{String.valueOf(bufferDbId)});
                    changelist.mChangelst.add(new BufferDBChangeParam(param.mDataContractType, bufferDbId, false, line));
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

    private void handleUnReadVvm(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        Cursor cs = this.mBufferDbQuery.queryVvmMessageBufferDBwithAppId((long) param.mRowId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    ContentValues cv = new ContentValues();
                    String line = cs.getString(cs.getColumnIndexOrThrow("linenum"));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                    cv.put("flagRead", 0);
                    long bufferDbId = cs.getLong(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    this.mBufferDbQuery.updateTable(param.mDataContractType, cv, "_bufferdbid=?", new String[]{String.valueOf(bufferDbId)});
                    changelist.mChangelst.add(new BufferDBChangeParam(param.mDataContractType, bufferDbId, false, line));
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

    private void handledeleteVvm(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        Cursor cs;
        ParamAppJsonValue paramAppJsonValue = param;
        Cursor cs2 = null;
        try {
            if (paramAppJsonValue.mDataContractType == 17) {
                cs = this.mBufferDbQuery.queryVvmMessageBufferDBwithAppId((long) paramAppJsonValue.mRowId);
            } else if (paramAppJsonValue.mDataContractType == 18) {
                cs = this.mBufferDbQuery.queryVvmGreetingBufferDBwithAppId((long) paramAppJsonValue.mRowId);
            } else {
                String str = TAG;
                Log.e(str, "handledeleteVvm, unrecognized datatype: " + paramAppJsonValue.mDataContractType);
                if (cs2 != null && !cs2.isClosed()) {
                    cs2.close();
                    return;
                }
                return;
            }
            if (cs != null) {
                try {
                    if (cs.moveToFirst()) {
                        ContentValues cv = new ContentValues();
                        long bufferDbId = cs.getLong(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                        String line = cs.getString(cs.getColumnIndexOrThrow("linenum"));
                        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForMsgOperation(paramAppJsonValue.mDataContractType, bufferDbId, direction, action, CloudMessageBufferDBConstants.MsgOperationFlag.Delete);
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
                        this.mBufferDbQuery.updateTable(paramAppJsonValue.mDataContractType, cv, "_bufferdbid=?", new String[]{String.valueOf(bufferDbId)});
                        if (flagSet.mIsChanged) {
                            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, paramAppJsonValue.mDataContractType, false, false, line, changelist);
                        }
                    }
                } catch (Throwable th) {
                    th = th;
                    cs2 = cs;
                    if (cs2 != null && !cs2.isClosed()) {
                        cs2.close();
                    }
                    throw th;
                }
            }
            if (cs != null && !cs.isClosed()) {
                cs.close();
            }
        } catch (Throwable th2) {
            th = th2;
            cs2.close();
            throw th;
        }
    }

    private void onVvmPINUpdateSuccess(ParamOMAresponseforBufDB paramOMAObj) {
        String[] selectionArgs = {String.valueOf(paramOMAObj.getBufferDBChangeParam().mRowId)};
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        this.mBufferDbQuery.updateTable(19, cv, "_bufferdbid=?", selectionArgs);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPIN, paramOMAObj.getBufferDBChangeParam().mRowId);
    }

    private void onVvmPINUpdateFailure(ParamOMAresponseforBufDB paramOMAObj) {
        String[] selectionArgs = {String.valueOf(paramOMAObj.getBufferDBChangeParam().mRowId)};
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.FAILURE.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        this.mBufferDbQuery.updateTable(19, cv, "_bufferdbid=?", selectionArgs);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPIN, paramOMAObj.getBufferDBChangeParam().mRowId);
    }

    private void onVvmGreetingUpdateSuccess(ParamOMAresponseforBufDB paramOMAObj) {
        String[] selectionArgs = {String.valueOf(paramOMAObj.getBufferDBChangeParam().mRowId)};
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        if (!(paramOMAObj.getReference() == null || paramOMAObj.getReference().resourceURL == null)) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObj.getReference().resourceURL.toString()));
        }
        this.mBufferDbQuery.updateTable(18, cv, "_bufferdbid=?", selectionArgs);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMGREETING, paramOMAObj.getBufferDBChangeParam().mRowId);
    }

    private void onVvmGreetingUpdateFailure(ParamOMAresponseforBufDB paramOMAObj) {
        String[] selectionArgs = {String.valueOf(paramOMAObj.getBufferDBChangeParam().mRowId)};
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.FAILURE.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        this.mBufferDbQuery.updateTable(18, cv, "_bufferdbid=?", selectionArgs);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMGREETING, paramOMAObj.getBufferDBChangeParam().mRowId);
    }

    private void onVvmProfileUpdateSuccess(ParamOMAresponseforBufDB paramOMAObj) {
        String[] selectionArgs = {String.valueOf(paramOMAObj.getBufferDBChangeParam().mRowId)};
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        this.mBufferDbQuery.updateTable(20, cv, "_bufferdbid=?", selectionArgs);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPROFILE, paramOMAObj.getBufferDBChangeParam().mRowId);
    }

    private void onVvmProfileUpdateFailure(ParamOMAresponseforBufDB paramOMAObj) {
        String[] selectionArgs = {String.valueOf(paramOMAObj.getBufferDBChangeParam().mRowId)};
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.FAILURE.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        this.mBufferDbQuery.updateTable(20, cv, "_bufferdbid=?", selectionArgs);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPROFILE, paramOMAObj.getBufferDBChangeParam().mRowId);
    }

    private void handleUploadVvm(ParamAppJsonValue param) {
        if (param.mVvmUpdate != null && param.mVvmUpdate.mLine != null) {
            BufferDBChangeParamList list = new BufferDBChangeParamList();
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange[param.mVvmUpdate.mVvmChange.ordinal()];
            if (i == 1) {
                list.mChangelst.add(new BufferDBChangeParam(18, this.mBufferDbQuery.insertVvmNewGreetingDeviceUpdate(param.mVvmUpdate), false, param.mVvmUpdate.mLine));
                this.mDeviceDataChangeListener.sendDeviceUpdate(list);
            } else if (i == 2) {
                list.mChangelst.add(new BufferDBChangeParam(19, this.mBufferDbQuery.insertVvmNewPinDeviceUpdate(param.mVvmUpdate), false, param.mVvmUpdate.mLine));
                this.mDeviceDataChangeListener.sendDeviceUpdate(list);
            } else if (i == 3 || i == 4 || i == 5) {
                list.mChangelst.add(new BufferDBChangeParam(20, this.mBufferDbQuery.insertVvmNewProfileDeviceUpdate(param.mVvmUpdate), false, param.mVvmUpdate.mLine));
                this.mDeviceDataChangeListener.sendDeviceUpdate(list);
            }
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange;

        static {
            int[] iArr = new int[ParamVvmUpdate.VvmTypeChange.values().length];
            $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange = iArr;
            try {
                iArr[ParamVvmUpdate.VvmTypeChange.GREETING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange[ParamVvmUpdate.VvmTypeChange.PIN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange[ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange[ParamVvmUpdate.VvmTypeChange.ACTIVATE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange[ParamVvmUpdate.VvmTypeChange.DEACTIVATE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            int[] iArr2 = new int[CloudMessageBufferDBConstants.MsgOperationFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag = iArr2;
            try {
                iArr2[CloudMessageBufferDBConstants.MsgOperationFlag.Upload.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Read.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.UnRead.ordinal()] = 3;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Delete.ordinal()] = 4;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Download.ordinal()] = 5;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    public Cursor queryToDeviceUnDownloadedVvm(String linenum) {
        return this.mBufferDbQuery.queryToDeviceUnDownloadedVvm(linenum);
    }

    public Cursor queryToDeviceUnDownloadedGreeting(String linenum) {
        return this.mBufferDbQuery.queryToDeviceUnDownloadedGreeting(linenum);
    }

    public void notifyMsgAppDeleteFail(int dbIndex, long bufferDbId, String line) {
        String str = TAG;
        Log.i(str, "notifyMsgAppDeleteFail, dbIndex: " + dbIndex + " bufferDbId: " + bufferDbId + " line: " + IMSLog.checker(line));
        if (dbIndex == 17) {
            JsonArray jsonArrayRowIds = new JsonArray();
            JsonObject jsobjct = new JsonObject();
            jsobjct.addProperty("id", String.valueOf(bufferDbId));
            jsonArrayRowIds.add(jsobjct);
            this.mCallbackMsgApp.notifyAppCloudDeleteFail("VVMDATA", "VVMDATA", jsonArrayRowIds.toString());
        }
    }

    public void wipeOutData(int tableindex, String line) {
        wipeOutData(tableindex, line, this.mBufferDbQuery);
    }

    public void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync) {
        onCloudUpdateFlagSuccess(para, mIsGoforwardSync, this.mBufferDbQuery);
    }
}
