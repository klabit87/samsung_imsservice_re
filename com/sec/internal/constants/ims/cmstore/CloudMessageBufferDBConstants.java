package com.sec.internal.constants.ims.cmstore;

public class CloudMessageBufferDBConstants {

    public enum CloudResponseFlag {
        Inserted,
        SetRead,
        SetDelete
    }

    public enum InitialSyncStatusFlag {
        START,
        FINISHED,
        FAIL
    }

    public enum MsgOperationFlag {
        Receiving,
        Received,
        Sending,
        Sent,
        Read,
        UnRead,
        Delete,
        SendFail,
        Upload,
        Download,
        StartFullSync,
        StopSync,
        WipeOut
    }

    public enum PayloadEncoding {
        None(0),
        Base64(1);
        
        private final int mId;

        private PayloadEncoding(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum ActionStatusFlag {
        None(0),
        Insert(1),
        Update(2),
        Delete(3),
        Deleted(4),
        DownLoad(5);
        
        private final int mId;

        private ActionStatusFlag(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }

        public static ActionStatusFlag valueOf(int id) {
            for (ActionStatusFlag r : values()) {
                if (r.mId == id) {
                    return r;
                }
            }
            return null;
        }
    }

    public enum DirectionFlag {
        Done(0),
        ToSendCloud(1),
        ToSendDevice(2),
        NmsEvent(3),
        UpdatingCloud(4),
        UpdatingDevice(5),
        Downloading(6),
        FetchingFail(7);
        
        private final int mId;

        private DirectionFlag(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }

        public static DirectionFlag valueOf(int id) {
            for (DirectionFlag r : values()) {
                if (r.mId == id) {
                    return r;
                }
            }
            return null;
        }
    }

    public enum UploadStatusFlag {
        FAILURE(0),
        SUCCESS(1),
        PENDING(2);
        
        private final int mId;

        private UploadStatusFlag(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum FaxDeliveryStatus {
        FAILURE(0),
        DELIVERED(1),
        PENDING(2);
        
        private final int mId;

        private FaxDeliveryStatus(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }
}
