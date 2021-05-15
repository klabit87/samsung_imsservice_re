package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.translate.FileExtensionTranslator;
import com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.querybuilders.CallLogQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.FlagList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class QueryBuilderBase {
    private static final String TAG = QueryBuilderBase.class.getSimpleName();
    protected int VALUE_ID_UNFETCHED;
    protected final CloudMessageBufferDBPersister mBufferDB;
    protected final IBufferDBEventListener mCallbackMsgApp;
    protected final Context mContext;
    protected final int mHoursToSendCloudUnsyncMessage = 10;
    protected final int mHoursToUploadMessageInitSync = 2184;
    SimpleDateFormat[] sFormatOfName;

    public QueryBuilderBase(Context context, IBufferDBEventListener callbackMsgApp) {
        SimpleDateFormat[] simpleDateFormatArr = {new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault()), new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.getDefault())};
        this.sFormatOfName = simpleDateFormatArr;
        this.VALUE_ID_UNFETCHED = 0;
        for (SimpleDateFormat format : simpleDateFormatArr) {
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        this.mContext = context;
        this.mBufferDB = CloudMessageBufferDBPersister.getInstance(context);
        this.mCallbackMsgApp = callbackMsgApp;
    }

    public int updateTable(int tableindex, ContentValues values, String selection, String[] selectionArgs) {
        String str = TAG;
        Log.d(str, "updateTable: " + tableindex);
        if (values.size() < 1) {
            return 0;
        }
        return this.mBufferDB.updateTable(tableindex, values, selection, selectionArgs);
    }

    public void deleteAllUsingLineAndTableIndex(int tableindex, String line) {
        int isBufferUpdateSuccess = this.mBufferDB.deleteTable(tableindex, "linenum=?", new String[]{line});
        String str = TAG;
        Log.d(str, "deleteAllUsingLineAndTableIndex isSuccess: " + isBufferUpdateSuccess);
    }

    public long getDateFromDateString(String datevalue) {
        SimpleDateFormat[] simpleDateFormatArr = this.sFormatOfName;
        int length = simpleDateFormatArr.length;
        int i = 0;
        while (i < length) {
            try {
                Date date = simpleDateFormatArr[i].parse(datevalue);
                if (date != null) {
                    return date.getTime();
                }
                return System.currentTimeMillis();
            } catch (ParseException e) {
                String str = TAG;
                Log.e(str, "ParseException: " + e.getMessage());
                i++;
            }
        }
        return System.currentTimeMillis();
    }

    public Cursor queryTablewithBufferDbId(int tableindex, long primarykey) {
        String str = TAG;
        Log.d(str, "queryTablewithBufferDbId, table: " + tableindex + " key: " + primarykey);
        return this.mBufferDB.queryTablewithBufferDbId(tableindex, primarykey);
    }

    public long insertTable(int tableindex, ContentValues cv) {
        return this.mBufferDB.insertTable(tableindex, cv);
    }

    public long insertDeviceMsgToBuffer(int tableindex, ContentValues cv) {
        String str = TAG;
        Log.d(str, "insertDeviceMsgToBuffer, tableindex: " + tableindex);
        return this.mBufferDB.insertDeviceMsgToBuffer(tableindex, cv);
    }

    public void cleanAllBufferDB() {
        Util.deleteFilesinMmsBufferFolder();
        this.mBufferDB.cleanAllBufferDB();
    }

    public CloudMessageBufferDBConstants.ActionStatusFlag getCloudActionPerFlag(FlagList fglist) {
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
        if (fglist == null || fglist.flag == null) {
            return action;
        }
        for (int i = 0; i < fglist.flag.length; i++) {
            if (fglist.flag[i].equalsIgnoreCase(FlagNames.Seen)) {
                if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId() > action.getId()) {
                    action = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                }
            } else if (fglist.flag[i].equalsIgnoreCase(FlagNames.Deleted) && CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId() > action.getId()) {
                action = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
            }
        }
        return action;
    }

    public void notifyApplication(String appType, String dataType, long rowId) {
        JsonArray jsonArrayRowIds = new JsonArray();
        JsonObject jsobjct = new JsonObject();
        jsobjct.addProperty("id", String.valueOf(rowId));
        jsonArrayRowIds.add(jsobjct);
        this.mCallbackMsgApp.notifyCloudMessageUpdate(appType, dataType, jsonArrayRowIds.toString());
    }

    public int getIfSeenValueUsingFlag(FlagList fglist) {
        if (fglist == null || fglist.flag == null) {
            return 0;
        }
        for (String equalsIgnoreCase : fglist.flag) {
            if (equalsIgnoreCase.equalsIgnoreCase(FlagNames.Seen)) {
                return 1;
            }
        }
        return 0;
    }

    public int getIfAnsweredValueUsingFlag(FlagList fglist) {
        if (fglist == null || fglist.flag == null) {
            return CallLogQueryBuilder.CallType.MISSED_TYPE.getId();
        }
        for (String equalsIgnoreCase : fglist.flag) {
            if (FlagNames.Answered.equalsIgnoreCase(equalsIgnoreCase)) {
                return CallLogQueryBuilder.CallType.ANSWERED_TYPE.getId();
            }
        }
        return CallLogQueryBuilder.CallType.MISSED_TYPE.getId();
    }

    public int getIfisGreetingOnUsingFlag(FlagList fglist) {
        if (fglist == null || fglist.flag == null) {
            return ParamVvmUpdate.GreetingOnFlag.GreetingOff.getId();
        }
        for (String equalsIgnoreCase : fglist.flag) {
            if (FlagNames.Cns_Greeting_on.equalsIgnoreCase(equalsIgnoreCase)) {
                return ParamVvmUpdate.GreetingOnFlag.GreetingOn.getId();
            }
        }
        return ParamVvmUpdate.GreetingOnFlag.GreetingOff.getId();
    }

    public CloudMessageBufferDBConstants.PayloadEncoding translatePayloadEncoding(String encodeMethod) {
        String str = TAG;
        Log.d(str, "translatePayloadEncoding: " + encodeMethod);
        if (HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64.equalsIgnoreCase(encodeMethod)) {
            return CloudMessageBufferDBConstants.PayloadEncoding.Base64;
        }
        return CloudMessageBufferDBConstants.PayloadEncoding.None;
    }

    public String getFileExtension(String contentType) {
        String str = TAG;
        Log.d(str, "getFileExtension: " + contentType);
        if (TextUtils.isEmpty(contentType)) {
            return "";
        }
        if (FileExtensionTranslator.isTranslationDefined(contentType)) {
            return FileExtensionTranslator.translate(contentType);
        }
        return contentType.toLowerCase().contains("multipart/related") ? "" : "";
    }

    public boolean isContentTypeDefined(String contentType) {
        String str = TAG;
        Log.d(str, "isContentTypeDefined: " + contentType);
        if (TextUtils.isEmpty(contentType)) {
            return false;
        }
        if (FileExtensionTranslator.isTranslationDefined(contentType) || contentType.toLowerCase().contains("multipart/related")) {
            return true;
        }
        return false;
    }

    public void insertResUrlinSummary(String resUrl, int msgType) {
        ContentValues cvsummary = new ContentValues();
        cvsummary.put("messagetype", Integer.valueOf(msgType));
        cvsummary.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(resUrl));
        cvsummary.put("linenum", Util.getLineTelUriFromObjUrl(resUrl));
        this.mBufferDB.insertTable(7, cvsummary);
    }

    /* access modifiers changed from: protected */
    public void updateSummaryTableMsgType(String resUrl, int msgType) {
        String str = TAG;
        Log.i(str, "updateSummaryTableMsgType: " + IMSLog.checker(resUrl) + " msgType: " + msgType);
        String objId = Util.extractObjIdFromResUrl(resUrl);
        String line = Util.getLineTelUriFromObjUrl(resUrl);
        String[] selectionArgs = {"*" + objId, line};
        ContentValues cvsummary = new ContentValues();
        cvsummary.put("messagetype", Integer.valueOf(msgType));
        this.mBufferDB.updateTable(7, cvsummary, "res_url GLOB ? AND linenum=?", selectionArgs);
    }

    public void updateAppFetchingFailed(int dbIndex, long bufferId) {
        String str = TAG;
        Log.i(str, "updateAppFetchingFailed: " + dbIndex + " bufferId: " + bufferId);
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail.getId()));
        this.mBufferDB.updateTable(dbIndex, cv, "_bufferdbid=?", new String[]{Long.toString(bufferId)});
    }

    public void setMsgDeleted(int dbIndex, long bufferId) {
        String str = TAG;
        Log.i(str, "setMsgDeleted: " + dbIndex + " bufferId: " + bufferId);
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()));
        this.mBufferDB.updateTable(dbIndex, cv, "_bufferdbid=?", new String[]{Long.toString(bufferId)});
    }

    public Cursor queryMessageBySyncDirection(int dbIndex, String syncDirection) {
        String str = TAG;
        Log.i(str, "queryMessageBySyncDirection: " + dbIndex + " syncDirection: " + syncDirection);
        return this.mBufferDB.queryTable(dbIndex, (String[]) null, "(syncdirection=?)", new String[]{syncDirection}, (String) null);
    }
}
