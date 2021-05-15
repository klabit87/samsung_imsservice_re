package com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetAllPayloads;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualPayLoad;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import java.util.List;

public class MessageDataChangeHandler extends BaseDataChangeHandler {
    public static final String TAG = MessageDataChangeHandler.class.getSimpleName();

    public MessageDataChangeHandler(Looper mLooper, Context mContext, INetAPIEventListener mINetAPIEventListener, IUIEventCallback mUIInterface, String line, SyncMsgType message, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(mLooper, mContext, mINetAPIEventListener, mUIInterface, line, message, iCloudMessageManagerHelper);
    }

    public void setWorkingQueue(BufferDBChangeParam param) {
        String str = TAG;
        Log.i(str, "setWorkingQueue: message type: " + param.mDBIndex);
        if (param.mDBIndex == 3) {
            this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, this.mBufferDBTranslation.getSmsObjectIdFromBufDb(param), param, this.mICloudMessageManagerHelper));
        } else if (param.mDBIndex == 4) {
            Pair<String, List<String>> pair = this.mBufferDBTranslation.getObjectIdPartIdFromMmsBufDb(param);
            List<String> payloadUrls = this.mBufferDBTranslation.getPayloadPartUrlFromMmsBufDb(param);
            String objId = (String) pair.first;
            if (payloadUrls != null && payloadUrls.size() > 0) {
                String str2 = TAG;
                Log.i(str2, "setWorkingQueue payloadUrls size: " + payloadUrls.size());
                for (String partUrl : payloadUrls) {
                    this.mWorkingQueue.offer(CloudMessageGetIndividualPayLoad.buildFromPayloadUrl(this, partUrl, param, this.mICloudMessageManagerHelper));
                }
            } else if (!TextUtils.isEmpty(objId)) {
                this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, objId, param, this.mICloudMessageManagerHelper));
            }
        } else if (param.mDBIndex == 6) {
            String payloadUrl = this.mBufferDBTranslation.getPayloadPartUrlFromMmsPartUsingPartBufferId(param);
            if (!TextUtils.isEmpty(payloadUrl)) {
                this.mWorkingQueue.offer(CloudMessageGetIndividualPayLoad.buildFromPayloadUrl(this, payloadUrl, param, this.mICloudMessageManagerHelper));
            }
        } else if (param.mDBIndex == 1) {
            Pair<String, List<String>> pair2 = this.mBufferDBTranslation.getObjectIdPartIdFromRCSBufDb(param);
            Pair<String, String> pairpayload = this.mBufferDBTranslation.getPayloadPartandAllPayloadUrlFromRCSBufDb(param);
            String payloadpartUrl = (String) pairpayload.first;
            String payloadUrl2 = (String) pairpayload.second;
            String objId2 = (String) pair2.first;
            String str3 = TAG;
            Log.d(str3, "payloadpartUrl: " + payloadpartUrl + " payloadUrl: " + payloadUrl2 + " objId: " + objId2);
            if (!TextUtils.isEmpty(payloadpartUrl)) {
                this.mWorkingQueue.offer(CloudMessageGetIndividualPayLoad.buildFromPayloadUrl(this, payloadpartUrl, param, this.mICloudMessageManagerHelper));
            } else if (!TextUtils.isEmpty(payloadUrl2)) {
                this.mWorkingQueue.offer(CloudMessageGetAllPayloads.buildFromPayloadUrl(this, payloadUrl2, param, this.mICloudMessageManagerHelper));
            } else if (!TextUtils.isEmpty(objId2)) {
                this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, objId2, param, this.mICloudMessageManagerHelper));
            }
        } else if (param.mDBIndex == 7) {
            this.mWorkingQueue.offer(new CloudMessageGetIndividualObject(this, this.mBufferDBTranslation.getSummaryObjectIdFromBufDb(param), param, this.mICloudMessageManagerHelper));
        }
        String str4 = TAG;
        Log.d(str4, "setMsgDownloadQueue size : " + this.mWorkingQueue.size());
    }
}
