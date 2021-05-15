package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import com.sec.internal.omanetapi.nms.data.BulkResponseList;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import com.sec.internal.omanetapi.nms.data.Reference;
import java.util.List;
import javax.mail.BodyPart;

public class ParamOMAresponseforBufDB {
    /* access modifiers changed from: private */
    public ActionType mActionType;
    /* access modifiers changed from: private */
    public List<BodyPart> mAllPayloads;
    /* access modifiers changed from: private */
    public BufferDBChangeParam mBufferDbParam;
    /* access modifiers changed from: private */
    public BufferDBChangeParamList mBufferDbParamList;
    /* access modifiers changed from: private */
    public BulkResponseList mBulkResponseList;
    /* access modifiers changed from: private */
    public byte[] mDataString;
    /* access modifiers changed from: private */
    public String mLine;
    /* access modifiers changed from: private */
    public NotificationList[] mNotificaitonList;
    /* access modifiers changed from: private */
    public OMASyncEventType mOMASyncEventType;
    /* access modifiers changed from: private */
    public Object mObject;
    /* access modifiers changed from: private */
    public ObjectList mObjectList;
    /* access modifiers changed from: private */
    public String mPayLoadUrl;
    /* access modifiers changed from: private */
    public Reference mReference;
    /* access modifiers changed from: private */
    public String mSearchCursor;
    /* access modifiers changed from: private */
    public SyncMsgType mType;
    /* access modifiers changed from: private */
    public VvmServiceProfile mVvmServiceProfile;

    public enum ActionType {
        INIT_SYNC_COMPLETE,
        INIT_SYNC_SUMMARY_COMPLETE,
        INIT_SYNC_PARTIAL_SYNC_SUMMARY,
        MATCH_DB,
        SYNC_FAILED,
        ONE_MESSAGE_DOWNLOAD,
        ONE_PAYLOAD_DOWNLOAD,
        ALL_PAYLOAD_DOWNLOAD,
        MESSAGE_DOWNLOAD_COMPLETE,
        ONE_MESSAGE_UPLOADED,
        MESSAGE_UPLOAD_COMPLETE,
        NOTIFICATION_OBJECT_DOWNLOADED,
        NOTIFICATION_PAYLOAD_DOWNLOADED,
        NOTIFICATION_ALL_PAYLOAD_DOWNLOADED,
        NOTIFICATION_OBJECTS_DOWNLOAD_COMPLETE,
        MAILBOX_RESET,
        CLOUD_OBJECT_UPDATE,
        RECEIVE_NOTIFICATION,
        OBJECT_FLAG_UPDATED,
        OBJECT_DELETE_UPDATE_FAILED,
        OBJECT_READ_UPDATE_FAILED,
        OBJECT_FLAGS_UPDATE_COMPLETE,
        OBJECT_FLAGS_BULK_UPDATE_COMPLETE,
        OBJECT_NOT_FOUND,
        VVM_FAX_ERROR_WITH_NO_RETRY,
        VVM_PROFILE_DOWNLOADED,
        BULK_MESSAGES_UPLOADED,
        FALLBACK_MESSAGES_UPLOADED
    }

    private ParamOMAresponseforBufDB() {
    }

    public static class Builder {
        private ParamOMAresponseforBufDB mInstance = new ParamOMAresponseforBufDB();

        public Builder setActionType(ActionType type) {
            ActionType unused = this.mInstance.mActionType = type;
            return this;
        }

        public Builder setLine(String line) {
            String unused = this.mInstance.mLine = line;
            return this;
        }

        public Builder setObjectList(ObjectList objectlist) {
            ObjectList unused = this.mInstance.mObjectList = objectlist;
            return this;
        }

        public Builder setReference(Reference reference) {
            Reference unused = this.mInstance.mReference = reference;
            return this;
        }

        public Builder setBufferDBChangeParam(BufferDBChangeParam param) {
            BufferDBChangeParam unused = this.mInstance.mBufferDbParam = param;
            return this;
        }

        public Builder setBufferDBChangeParam(BufferDBChangeParamList param) {
            BufferDBChangeParamList unused = this.mInstance.mBufferDbParamList = param;
            return this;
        }

        public Builder setNotificationList(NotificationList[] notificaitonList) {
            NotificationList[] unused = this.mInstance.mNotificaitonList = notificaitonList;
            return this;
        }

        public Builder setObject(Object obj) {
            Object unused = this.mInstance.mObject = obj;
            return this;
        }

        public Builder setVvmServiceProfile(VvmServiceProfile profile) {
            VvmServiceProfile unused = this.mInstance.mVvmServiceProfile = profile;
            return this;
        }

        public Builder setByte(byte[] data) {
            byte[] unused = this.mInstance.mDataString = data;
            return this;
        }

        public Builder setPayloadUrl(String payloadurl) {
            String unused = this.mInstance.mPayLoadUrl = payloadurl;
            return this;
        }

        public Builder setCursor(String cursor) {
            String unused = this.mInstance.mSearchCursor = cursor;
            return this;
        }

        public Builder setAllPayloads(List<BodyPart> payloads) {
            List unused = this.mInstance.mAllPayloads = payloads;
            return this;
        }

        public Builder setOMASyncEventType(OMASyncEventType type) {
            OMASyncEventType unused = this.mInstance.mOMASyncEventType = type;
            return this;
        }

        public Builder setSyncType(SyncMsgType type) {
            SyncMsgType unused = this.mInstance.mType = type;
            return this;
        }

        public Builder setBulkResponseList(BulkResponseList list) {
            BulkResponseList unused = this.mInstance.mBulkResponseList = list;
            return this;
        }

        public ParamOMAresponseforBufDB build() {
            if (this.mInstance.mOMASyncEventType != null) {
                CloudMessagePreferenceManager.getInstance().saveInitialSyncStatus(this.mInstance.mOMASyncEventType.getId());
            }
            return this.mInstance;
        }
    }

    public ActionType getActionType() {
        return this.mActionType;
    }

    public String getLine() {
        return this.mLine;
    }

    public ObjectList getObjectList() {
        return this.mObjectList;
    }

    public Reference getReference() {
        return this.mReference;
    }

    public BufferDBChangeParam getBufferDBChangeParam() {
        return this.mBufferDbParam;
    }

    public BufferDBChangeParamList getBufferDBChangeParamList() {
        return this.mBufferDbParamList;
    }

    public Object getObject() {
        return this.mObject;
    }

    public NotificationList[] getNotificationList() {
        return this.mNotificaitonList;
    }

    public byte[] getData() {
        return this.mDataString;
    }

    public String getSearchCursor() {
        return this.mSearchCursor;
    }

    public OMASyncEventType getOMASyncEventType() {
        return this.mOMASyncEventType;
    }

    public List<BodyPart> getAllPayloads() {
        return this.mAllPayloads;
    }

    public SyncMsgType getSyncMsgType() {
        return this.mType;
    }

    public BulkResponseList getBulkResponseList() {
        return this.mBulkResponseList;
    }

    public VvmServiceProfile getVvmServiceProfile() {
        return this.mVvmServiceProfile;
    }

    public String toString() {
        return " mActionType: " + this.mActionType + " mLine: " + IMSLog.checker(this.mLine) + " mReference: " + this.mReference + " mBufferDbParam: " + this.mBufferDbParam + " mPayLoadUrl: " + IMSLog.checker(this.mPayLoadUrl) + " mSearchCursor: " + this.mSearchCursor + " mOMASyncEventType: " + this.mOMASyncEventType + " mType: " + this.mType;
    }
}
