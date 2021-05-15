package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.omanetapi.nms.data.ChangedObject;

public class VVMQueryBuilder extends QueryBuilderBase {
    private static final String TAG = VVMQueryBuilder.class.getSimpleName();

    public VVMQueryBuilder(Context context, IBufferDBEventListener callbackapp) {
        super(context, callbackapp);
    }

    public long insertVvmMessageUsingChangedObject(ChangedObject object, String line) {
        ContentValues cv = new ContentValues();
        if (object.extendedMessage != null) {
            cv.put("messageId", Integer.getInteger(object.extendedMessage.id));
            cv.put("flagRead", Integer.valueOf(getIfSeenValueUsingFlag(object.flags)));
            cv.put("sender", object.extendedMessage.sender);
            cv.put(CloudMessageProviderContract.VVMMessageColumns.RECIPIENT, object.extendedMessage.recipients[0].uri);
            cv.put(CloudMessageProviderContract.VVMMessageColumns.TIMESTAMP, Long.valueOf(getDateFromDateString(object.extendedMessage.message_time)));
        }
        cv.put("linenum", line);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(object.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
        return this.mBufferDB.insertTable(17, cv);
    }

    public long insertVvmMessageUsingObject(ParamOMAObject objt, String line) {
        ContentValues cv = createDataVvm(objt, line);
        cv.put("messageId", objt.MESSAGE_ID);
        cv.put("flagRead", Integer.valueOf(getIfSeenValueUsingFlag(objt.mFlagList)));
        cv.put("sender", objt.FROM);
        cv.put(CloudMessageProviderContract.VVMMessageColumns.RECIPIENT, objt.TO.get(0));
        cv.put(CloudMessageProviderContract.VVMMessageColumns.TIMESTAMP, Long.valueOf(getDateFromDateString(objt.DATE)));
        return this.mBufferDB.insertTable(17, cv);
    }

    public long insertVvmGreetingUsingObject(ParamOMAObject objt, String line) {
        ContentValues cv = createDataVvm(objt, line);
        cv.put("mimeType", objt.CONTENT_TYPE);
        cv.put("duration", objt.CONTENT_DURATION);
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put(CloudMessageProviderContract.VVMGreetingColumns.ACCOUNT_NUMBER, line);
        cv.put("messageId", objt.MESSAGE_ID);
        cv.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(translateGreetingType(objt.X_CNS_Greeting_Type).getId()));
        cv.put("flags", Integer.valueOf(getIfisGreetingOnUsingFlag(objt.mFlagList)));
        return this.mBufferDB.insertTable(18, cv);
    }

    private ContentValues createDataVvm(ParamOMAObject objt, String line) {
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, objt.correlationId);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, objt.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(objt.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(objt.parentFolder.toString()));
        cv.put("path", Util.decodeUrlFromServer(objt.path.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, objt.payloadURL.toString());
        cv.put("linenum", line);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
        return cv;
    }

    public Cursor queryVvmMessageBufferDBwithResUrl(String url) {
        return this.mBufferDB.queryTablewithResUrl(17, url);
    }

    public Cursor queryVvmGreetingBufferDBwithResUrl(String url) {
        return this.mBufferDB.queryTablewithResUrl(18, url);
    }

    public Cursor queryVvmMessageBufferDBwithAppId(long id) {
        String str = TAG;
        Log.i(str, "queryVvmMessageBufferDBwithAppId: " + id);
        return this.mBufferDB.queryTable(17, (String[]) null, "_id=?", new String[]{String.valueOf(id)}, (String) null);
    }

    public Cursor queryVvmGreetingBufferDBwithAppId(long id) {
        String str = TAG;
        Log.i(str, "queryVvmGreetingBufferDBwithAppId: " + id);
        return this.mBufferDB.queryTable(18, (String[]) null, "_id=?", new String[]{String.valueOf(id)}, (String) null);
    }

    public long insertVvmNewPinDeviceUpdate(ParamVvmUpdate vvmpin) {
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.VVMPin.OLDPWD, vvmpin.mOldPwd);
        cv.put(CloudMessageProviderContract.VVMPin.NEWPWD, vvmpin.mNewPwd);
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put("linenum", vvmpin.mLine);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        return this.mBufferDB.insertTable(19, cv);
    }

    public long insertVvmNewGreetingDeviceUpdate(ParamVvmUpdate vvmgreeting) {
        ContentValues cv = new ContentValues();
        cv.put("filepath", vvmgreeting.mGreetingUri);
        cv.put(CloudMessageProviderContract.VVMGreetingColumns.ACCOUNT_NUMBER, vvmgreeting.mLine);
        cv.put("duration", Integer.valueOf(vvmgreeting.mDuration));
        cv.put("mimeType", vvmgreeting.mMimeType);
        cv.put("fileName", vvmgreeting.mfileName);
        cv.put("_id", Integer.valueOf(vvmgreeting.mId));
        if ("name".equalsIgnoreCase(vvmgreeting.mGreetingType)) {
            cv.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(ParamVvmUpdate.VvmGreetingType.Name.getId()));
        } else if ("custom".equalsIgnoreCase(vvmgreeting.mGreetingType)) {
            cv.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(ParamVvmUpdate.VvmGreetingType.Custom.getId()));
        } else {
            cv.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(ParamVvmUpdate.VvmGreetingType.Default.getId()));
        }
        cv.put("linenum", vvmgreeting.mLine);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        return this.mBufferDB.insertTable(18, cv);
    }

    public long insertDefaultGreetingValues(String line) {
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(ParamVvmUpdate.VvmGreetingType.Default.getId()));
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put(CloudMessageProviderContract.VVMGreetingColumns.ACCOUNT_NUMBER, line);
        cv.put("linenum", line);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        return this.mBufferDB.insertTable(18, cv);
    }

    public long insertVvmNewProfileDeviceUpdate(ParamVvmUpdate vvmprofile) {
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR1, vvmprofile.mEmail1);
        cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR2, vvmprofile.mEmail2);
        cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.LINE_NUMBER, vvmprofile.mLine);
        cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.PROFILE_CHANGETYPE, Integer.valueOf(vvmprofile.mVvmChange.getId()));
        cv.put("linenum", vvmprofile.mLine);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        return this.mBufferDB.insertTable(20, cv);
    }

    public long insertDownloadNewProfileRequest(ParamVvmUpdate vvmprofile) {
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.PROFILE_CHANGETYPE, Integer.valueOf(ParamVvmUpdate.VvmTypeChange.FULLPROFILE.getId()));
        cv.put("linenum", vvmprofile.mLine);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
        return this.mBufferDB.insertTable(20, cv);
    }

    public long insertVvmNewEmailProfileCloudUpdate(ParamVvmUpdate vvmprofile) {
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR1, vvmprofile.mEmail1);
        cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.LINE_NUMBER, vvmprofile.mLine);
        cv.put(CloudMessageProviderContract.VVMAccountInfoColumns.PROFILE_CHANGETYPE, Integer.valueOf(ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT.getId()));
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put("linenum", vvmprofile.mLine);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        return this.mBufferDB.insertTable(20, cv);
    }

    public long insertVvmNewPinCloudUpdate(ParamVvmUpdate vvmprofile) {
        ContentValues cv = new ContentValues();
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put(CloudMessageProviderContract.VVMPin.NEWPWD, vvmprofile.mNewPwd);
        cv.put("linenum", vvmprofile.mLine);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        return this.mBufferDB.insertTable(19, cv);
    }

    public Cursor queryToDeviceUnDownloadedVvm(String linenum) {
        return this.mBufferDB.queryTable(17, (String[]) null, "syncaction=? AND linenum=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()), linenum}, (String) null);
    }

    public Cursor queryToDeviceUnDownloadedGreeting(String linenum) {
        return this.mBufferDB.queryTable(18, (String[]) null, "syncaction=? AND linenum=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()), linenum}, (String) null);
    }

    private ParamVvmUpdate.VvmGreetingType translateGreetingType(String value) {
        if ("normal-greeting".equalsIgnoreCase(value)) {
            return ParamVvmUpdate.VvmGreetingType.Custom;
        }
        if ("voice-signature".equalsIgnoreCase(value)) {
            return ParamVvmUpdate.VvmGreetingType.Name;
        }
        if ("busy-greeting".equalsIgnoreCase(value)) {
            return ParamVvmUpdate.VvmGreetingType.Busy;
        }
        if ("extended-absence-greeting".equalsIgnoreCase(value)) {
            return ParamVvmUpdate.VvmGreetingType.ExtendAbsence;
        }
        if ("fun-greeting".equalsIgnoreCase(value)) {
            return ParamVvmUpdate.VvmGreetingType.Fun;
        }
        return ParamVvmUpdate.VvmGreetingType.Default;
    }
}
