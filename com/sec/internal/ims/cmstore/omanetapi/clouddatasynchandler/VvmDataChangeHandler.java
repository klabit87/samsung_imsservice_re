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

public class VvmDataChangeHandler extends BaseDataChangeHandler {
    public static final String TAG = VvmDataChangeHandler.class.getSimpleName();

    public VvmDataChangeHandler(Looper mLooper, Context mContext, INetAPIEventListener mINetAPIEventListener, IUIEventCallback mUIInterface, String line, SyncMsgType vm, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(mLooper, mContext, mINetAPIEventListener, mUIInterface, line, vm, iCloudMessageManagerHelper);
    }

    public void setWorkingQueue(BufferDBChangeParam param) {
        String str = TAG;
        Log.i(str, "setWorkingQueue: message type: " + param.mDBIndex);
        if (param.mDBIndex == 17) {
            String objectId = this.mBufferDBTranslation.getVVMObjectIdFromBufDb(param);
            String payloadUrl = this.mBufferDBTranslation.getVVMpayLoadUrlFromBufDb(param);
            String str2 = TAG;
            Log.d(str2, "downloadNextMsgFromQueueObject: objectId: " + objectId + " payloadUrl: " + IMSLog.checker(payloadUrl));
            if (TextUtils.isEmpty(payloadUrl)) {
                this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, objectId, param, this.mICloudMessageManagerHelper));
            } else {
                this.mWorkingQueue.offer(CloudMessageGetAllPayloads.buildFromPayloadUrl(this, payloadUrl, param, this.mICloudMessageManagerHelper));
            }
        } else if (param.mDBIndex == 18) {
            this.mWorkingQueue.offer(CloudMessageGetAllPayloads.buildFromPayloadUrl(this, this.mBufferDBTranslation.getVVMGreetingpayLoadUrlFromBufDb(param), param, this.mICloudMessageManagerHelper));
        } else if (param.mDBIndex == 7) {
            this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, this.mBufferDBTranslation.getSummaryObjectIdFromBufDb(param), param, this.mICloudMessageManagerHelper));
        }
        String str3 = TAG;
        Log.d(str3, "setMsgDownloadQueue size : " + this.mWorkingQueue.size());
    }
}
