package com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualObject;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;

public class CalllogDataChangeHandler extends BaseDataChangeHandler {
    public static final String TAG = CalllogDataChangeHandler.class.getSimpleName();

    public CalllogDataChangeHandler(Looper mLooper, Context mContext, INetAPIEventListener mINetAPIEventListener, IUIEventCallback mUIInterface, String line, SyncMsgType calllog, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(mLooper, mContext, mINetAPIEventListener, mUIInterface, line, calllog, iCloudMessageManagerHelper);
    }

    public void setWorkingQueue(BufferDBChangeParam param) {
        if (param.mDBIndex == 16) {
            this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, this.mBufferDBTranslation.getCallLogObjectIdFromBufDb(param), param, this.mICloudMessageManagerHelper));
        } else if (param.mDBIndex == 7) {
            this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, this.mBufferDBTranslation.getSummaryObjectIdFromBufDb(param), param, this.mICloudMessageManagerHelper));
        }
        String str = TAG;
        Log.d(str, "setMsgDownloadQueue size : " + this.mWorkingQueue.size());
    }
}
