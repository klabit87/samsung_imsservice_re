package com.sec.internal.ims.cmstore.params;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.log.IMSLog;

public class BufferDBChangeParam {
    private static final String TAG = BufferDBChangeParam.class.getSimpleName();
    public final CloudMessageBufferDBConstants.ActionStatusFlag mAction;
    public final int mDBIndex;
    public String mFTThumbnailFileName;
    public boolean mIsFTThumbnail = false;
    public final boolean mIsGoforwardSync;
    public String mLine;
    public String mPayloadThumbnailUrl;
    public final long mRowId;

    public BufferDBChangeParam(int dbindex, long rowid, boolean isGoforward, String line) {
        this.mDBIndex = dbindex;
        this.mRowId = rowid;
        this.mIsGoforwardSync = isGoforward;
        if (line == null) {
            this.mLine = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
            Log.e(TAG, "multiline not supported. or line null value is given, Ingore line param");
        } else {
            this.mLine = line;
        }
        this.mAction = null;
    }

    public BufferDBChangeParam(int dbindex, long rowid, boolean isGoforward, String line, CloudMessageBufferDBConstants.ActionStatusFlag operation) {
        this.mDBIndex = dbindex;
        this.mRowId = rowid;
        this.mIsGoforwardSync = isGoforward;
        if (line == null) {
            this.mLine = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
            Log.e(TAG, "multiline not supported. or line null value is given, Ingore line param");
        } else {
            this.mLine = line;
        }
        this.mAction = operation;
    }

    public String toString() {
        return "BufferDBChangeParam [mDBIndex= " + this.mDBIndex + " mRowId = " + this.mRowId + " mIsGoforwardSync = " + this.mIsGoforwardSync + "mLine = " + IMSLog.checker(this.mLine) + " mFTThumbnailFileName = " + this.mFTThumbnailFileName + " mPayloadThumbnailUrl = " + this.mPayloadThumbnailUrl + " mIsFTThumbnail = " + this.mIsFTThumbnail + "]";
    }
}
