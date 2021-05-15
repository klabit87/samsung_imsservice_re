package com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetAllPayloads;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualObject;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;

public class FaxDataChangeHandler extends BaseDataChangeHandler {
    public static final String TAG = FaxDataChangeHandler.class.getSimpleName();

    public FaxDataChangeHandler(Looper mLooper, Context mContext, INetAPIEventListener mINetAPIEventListener, IUIEventCallback mUIInterface, String line, SyncMsgType fax, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(mLooper, mContext, mINetAPIEventListener, mUIInterface, line, fax, iCloudMessageManagerHelper);
    }

    public void setWorkingQueue(BufferDBChangeParam param) {
        if (param.mDBIndex == 21) {
            String objectId = this.mBufferDBTranslation.getFaxObjectIdFromBufDb(param);
            String payloadUrl = this.mBufferDBTranslation.getFaxpayLoadUrlFromBufDb(param);
            String str = TAG;
            Log.d(str, "downloadNextMsgFromQueueObject: objectId: " + objectId + " payloadUrl: " + IMSLog.checker(payloadUrl));
            if (TextUtils.isEmpty(payloadUrl)) {
                this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, objectId, param, this.mICloudMessageManagerHelper));
            } else {
                this.mWorkingQueue.offer(CloudMessageGetAllPayloads.buildFromPayloadUrl(this, payloadUrl, param, this.mICloudMessageManagerHelper));
            }
        } else if (param.mDBIndex == 7) {
            this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, this.mBufferDBTranslation.getSummaryObjectIdFromBufDb(param), param, this.mICloudMessageManagerHelper));
        }
        String str2 = TAG;
        Log.d(str2, "setMsgDownloadQueue size : " + this.mWorkingQueue.size() + " message type: " + param.mDBIndex);
    }
}
