package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.omanetapi.nms.data.ChangedObject;

public class CallLogQueryBuilder extends QueryBuilderBase {
    private static final String TAG = CallLogQueryBuilder.class.getSimpleName();

    public enum CallType {
        INCOMING_TYPE(1),
        OUTGOING_TYPE(2),
        MISSED_TYPE(3),
        ANSWERED_TYPE(16);
        
        private final int mId;

        private CallType(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum CallPresentation {
        PRESENTATION_ALLOWED(1);
        
        private final int mId;

        private CallPresentation(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum CallLogType {
        LOG_TYPE_CALL(100),
        LOG_TYPE_VIDEO(500);
        
        private final int mId;

        private CallLogType(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public CallLogQueryBuilder(Context context, IBufferDBEventListener callbackapp) {
        super(context, callbackapp);
    }

    public long insertNewCallLogUsingChangedObject(ChangedObject object, String line) {
        ContentValues cv = new ContentValues();
        if (object.extendedMessage != null) {
            String str = TAG;
            Log.d(str, "insertNewCallLogUsingChangedObject: " + object.extendedMessage.call_timestamp + " " + object.extendedMessage.message_time + " " + object.extendedMessage.call_duration);
            cv.put("duration", Integer.valueOf(object.extendedMessage.call_duration != null ? Integer.valueOf(object.extendedMessage.call_duration).intValue() : -1));
            cv.put(CloudMessageProviderContract.BufferCallLog.STARTTIME, Long.valueOf(getDateFromDateString(object.extendedMessage.call_timestamp)));
            cv.put("date", Long.valueOf(getDateFromDateString(object.extendedMessage.message_time)));
            cv.put(CloudMessageProviderContract.BufferCallLog.LOGTYPE, Integer.valueOf(getCallLogType(object.extendedMessage.call_type).getId()));
            cv.put(CloudMessageProviderContract.BufferCallLog.PRESENTATION, Integer.valueOf(CallPresentation.PRESENTATION_ALLOWED.getId()));
            cv.put("device_name", object.extendedMessage.participating_device);
            cv.put("seen", Integer.valueOf(getIfSeenValueUsingFlag(object.flags)));
            cv.put(CloudMessageProviderContract.BufferCallLog.ANSWERED_BY, Integer.valueOf(getIfAnsweredValueUsingFlag(object.flags)));
            if ("In".equalsIgnoreCase(object.extendedMessage.direction)) {
                cv.put("number", object.extendedMessage.sender);
                cv.put("type", Integer.valueOf(CallType.INCOMING_TYPE.getId()));
            } else {
                cv.put("number", object.extendedMessage.recipients[0].uri);
                cv.put("type", Integer.valueOf(CallType.OUTGOING_TYPE.getId()));
            }
        }
        cv.put("linenum", line);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(object.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        return this.mBufferDB.insertTable(16, cv);
    }

    public long insertCallLogMessageUsingObject(ParamOMAObject objt, String line) {
        String str = TAG;
        Log.d(str, "insertCallLogMessageUsingObject: " + objt.CALL_STARTTIMESTAMP + " " + objt.DATE + " " + objt.CALL_DURATION);
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, objt.correlationId);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, objt.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(objt.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(objt.parentFolder.toString()));
        if (!CloudMessageStrategyManager.getStrategy().isNmsEventHasMessageDetail()) {
            cv.put("path", Util.decodeUrlFromServer(objt.path.toString()));
        }
        cv.put("linenum", line);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        if (objt.CALL_DURATION != null) {
            cv.put("duration", Integer.valueOf(Integer.valueOf(objt.CALL_DURATION).intValue()));
        } else {
            cv.put("duration", -1);
        }
        cv.put("device_name", objt.PARTICIPATING_DEVICE);
        cv.put(CloudMessageProviderContract.BufferCallLog.STARTTIME, Long.valueOf(getDateFromDateString(objt.CALL_STARTTIMESTAMP)));
        cv.put("date", Long.valueOf(getDateFromDateString(objt.DATE)));
        cv.put(CloudMessageProviderContract.BufferCallLog.LOGTYPE, Integer.valueOf(getCallLogType(objt.CALL_TYPE).getId()));
        cv.put(CloudMessageProviderContract.BufferCallLog.PRESENTATION, Integer.valueOf(CallPresentation.PRESENTATION_ALLOWED.getId()));
        cv.put("seen", Integer.valueOf(getIfSeenValueUsingFlag(objt.mFlagList)));
        cv.put(CloudMessageProviderContract.BufferCallLog.ANSWERED_BY, Integer.valueOf(getIfAnsweredValueUsingFlag(objt.mFlagList)));
        if ("In".equalsIgnoreCase(objt.DIRECTION)) {
            cv.put("number", objt.FROM);
            cv.put("type", Integer.valueOf(CallType.INCOMING_TYPE.getId()));
        } else {
            if (objt.TO != null && objt.TO.size() > 0) {
                cv.put("number", objt.TO.get(0));
            }
            cv.put("type", Integer.valueOf(CallType.OUTGOING_TYPE.getId()));
        }
        return this.mBufferDB.insertTable(16, cv);
    }

    public Cursor queryCallLogMessageBufferDBwithResUrl(String url) {
        return this.mBufferDB.queryTablewithResUrl(16, url);
    }

    public Cursor queryCallLogBufferDBwithAppId(long id) {
        String str = TAG;
        Log.i(str, "queryCallLogBufferDBwithAppId: " + id);
        return this.mBufferDB.queryTable(16, (String[]) null, "_id=?", new String[]{String.valueOf(id)}, (String) null);
    }

    public CallLogType getCallLogType(String value) {
        if (TMOConstants.CallLogTypes.VIDEO.equalsIgnoreCase(value)) {
            return CallLogType.LOG_TYPE_VIDEO;
        }
        if (TMOConstants.CallLogTypes.AUDIO.equalsIgnoreCase(value)) {
            return CallLogType.LOG_TYPE_CALL;
        }
        return CallLogType.LOG_TYPE_CALL;
    }
}
