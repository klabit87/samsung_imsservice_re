package com.sec.internal.ims.cmstore.omanetapi.synchandler;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkCreation;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetAllPayloads;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualPayLoad;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamBulkCreation;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MessageSyncHandler extends BaseSyncHandler {
    public static final int MAX_PAYLOAD_SIZE = 104857600;
    public static final String TAG = MessageSyncHandler.class.getSimpleName();
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;

    public MessageSyncHandler(Looper looper, Context context, INetAPIEventListener APIEventListener, IUIEventCallback uicallback, String line, SyncMsgType type, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper, context, APIEventListener, uicallback, line, type, iCloudMessageManagerHelper);
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
    }

    /* access modifiers changed from: protected */
    public void setWorkingQueue(BufferDBChangeParam param, BaseSyncHandler.SyncOperation syncoperation) {
        if (BaseSyncHandler.SyncOperation.DOWNLOAD.equals(syncoperation)) {
            setDownloadQueueInternal(param);
        } else if (BaseSyncHandler.SyncOperation.UPLOAD.equals(syncoperation)) {
            setUploadQueueInternal(param);
        }
    }

    /* access modifiers changed from: protected */
    public HttpRequestParams peekDownloadQueue() {
        Pair<String, String> pairpayload;
        BufferDBChangeParam param = (BufferDBChangeParam) this.mWorkingDownloadQueue.peek();
        String str = TAG;
        Log.i(str, "peekDownloadQueue: " + param);
        if (param == null) {
            return null;
        }
        if (param.mDBIndex == 4) {
            Pair<String, List<String>> pair = this.mBufferDBTranslation.getObjectIdPartIdFromMmsBufDb(param);
            List<String> payloadUrls = this.mBufferDBTranslation.getPayloadPartUrlFromMmsBufDb(param);
            String objId = (String) pair.first;
            if (payloadUrls != null && payloadUrls.size() > 0) {
                String str2 = TAG;
                Log.i(str2, "peekDownloadQueue PDU Payload Part partUrl: " + payloadUrls);
                for (String next : payloadUrls) {
                }
            } else if (!TextUtils.isEmpty(objId)) {
                Log.i(TAG, "peekDownloadQueue PDU Object");
                return new CloudMessageGetIndividualObject(this, objId, param, this.mICloudMessageManagerHelper);
            }
        } else if (param.mDBIndex == 6) {
            String payloadUrl = this.mBufferDBTranslation.getPayloadPartUrlFromMmsPartUsingPartBufferId(param);
            String str3 = TAG;
            Log.i(str3, "peekDownloadQueue part: payloadUrl: " + payloadUrl);
            if (!TextUtils.isEmpty(payloadUrl)) {
                return CloudMessageGetIndividualPayLoad.buildFromPayloadUrl(this, payloadUrl, param, this.mICloudMessageManagerHelper);
            }
        } else if (param.mDBIndex == 1) {
            Pair<String, List<String>> pair2 = this.mBufferDBTranslation.getObjectIdPartIdFromRCSBufDb(param);
            if (this.mIsFTThumbnailDownload) {
                pairpayload = this.mBufferDBTranslation.getPayloadPartandAllPayloadUrlFromRCSBufDb(param);
                this.mIsFTThumbnailDownload = false;
            } else {
                Pair<String, String> pairpayload2 = this.mBufferDBTranslation.getAllPayloadUrlFromRCSBufDb(param);
                if (param.mIsFTThumbnail) {
                    this.mIsFTThumbnailDownload = true;
                }
                String str4 = TAG;
                Log.i(str4, "param.mPayloadThumbnailUrl : " + param.mPayloadThumbnailUrl + ", mIsFTThumbnailDownload = " + this.mIsFTThumbnailDownload);
                pairpayload = pairpayload2;
            }
            String payloadpartUrl = (String) pairpayload.first;
            String payloadUrl2 = (String) pairpayload.second;
            String objId2 = (String) pair2.first;
            String str5 = TAG;
            Log.i(str5, "payloadpartUrl: " + payloadpartUrl + " payloadUrl: " + payloadUrl2 + " objId: " + objId2);
            if (this.mIsFTThumbnailDownload) {
                return CloudMessageGetIndividualPayLoad.buildFromPayloadUrl(this, param.mPayloadThumbnailUrl, param, this.mICloudMessageManagerHelper);
            }
            if (!TextUtils.isEmpty(payloadpartUrl)) {
                return CloudMessageGetIndividualPayLoad.buildFromPayloadUrl(this, payloadpartUrl, param, this.mICloudMessageManagerHelper);
            }
            if (!TextUtils.isEmpty(payloadUrl2)) {
                return CloudMessageGetAllPayloads.buildFromPayloadUrl(this, payloadUrl2, param, this.mICloudMessageManagerHelper);
            }
            if (!TextUtils.isEmpty(objId2)) {
                return new CloudMessageGetIndividualObject(this, objId2, param, this.mICloudMessageManagerHelper);
            }
        } else if (param.mDBIndex == 17) {
            return CloudMessageGetAllPayloads.buildFromPayloadUrl(this, this.mBufferDBTranslation.getVVMpayLoadUrlFromBufDb(param), param, this.mICloudMessageManagerHelper);
        } else {
            if (param.mDBIndex == 21) {
                return CloudMessageGetAllPayloads.buildFromPayloadUrl(this, this.mBufferDBTranslation.getFaxpayLoadUrlFromBufDb(param), param, this.mICloudMessageManagerHelper);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public HttpRequestParams peekUploadQueue() {
        Pair<Object, HttpPostBody> pair;
        BufferDBChangeParam param = (BufferDBChangeParam) this.mWorkingUploadQueue.peek();
        String str = TAG;
        Log.i(str, "peekUploadQueue: " + param);
        if (param == null || (pair = getPairFromCursor(param)) == null) {
            return null;
        }
        if (param.mDBIndex == 13 && pair.first == null) {
            return null;
        }
        if (param.mDBIndex == 13 || (pair.first != null && pair.second != null)) {
            return new CloudMessageCreateAllObjects(this, new ParamObjectUpload(pair, param), this.mICloudMessageManagerHelper);
        }
        return null;
    }

    private Pair<Object, HttpPostBody> getPairFromCursor(BufferDBChangeParam param) {
        String str = TAG;
        Log.i(str, "getPairFromCursor param: " + param);
        if (param.mDBIndex == 3) {
            return this.mBufferDBTranslation.getSmsObjectPairFromCursor(param);
        }
        if (param.mDBIndex == 4) {
            return this.mBufferDBTranslation.getMmsObjectPairFromCursor(param);
        }
        if (param.mDBIndex == 1) {
            return this.mBufferDBTranslation.getRCSObjectPairFromCursor(param);
        }
        if (param.mDBIndex == 13) {
            return this.mBufferDBTranslation.getImdnObjectPair(param);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> makeBulkUploadBody(BufferDBChangeParam param) {
        String str = TAG;
        Log.e(str, "makeBulkUploadBody: " + param);
        if (param == null) {
            return null;
        }
        Pair<Object, HttpPostBody> pair = getPairFromCursor(param);
        if (pair != null) {
            if (param.mDBIndex == 13 && pair.first == null) {
                return null;
            }
            if (param.mDBIndex == 13 || (pair.first != null && pair.second != null)) {
                return pair;
            }
            return null;
        }
        return pair;
    }

    private void setDownloadQueueInternal(BufferDBChangeParam param) {
        this.mWorkingDownloadQueue.offer(param);
        String str = TAG;
        Log.d(str, "setWorkingQueue :: " + param + " size : " + this.mWorkingDownloadQueue.size());
    }

    private void setUploadQueueInternal(BufferDBChangeParam param) {
        this.mWorkingUploadQueue.offer(param);
        String str = TAG;
        Log.d(str, "setUploadQueueInternal: " + param + " size : " + this.mWorkingUploadQueue.size());
    }

    /* access modifiers changed from: protected */
    public void setBulkUploadQueue(BufferDBChangeParamList param) {
        String str = TAG;
        Log.d(str, "setBulkUploadQueue param: " + param);
        Iterator<BufferDBChangeParam> it = param.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam changeParam = it.next();
            if (changeParam != null) {
                this.mBulkUploadQueue.add(changeParam);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void makeBulkUploadparameter() {
        int postBodySize = 0;
        List<Object> objects = new ArrayList<>();
        List<HttpPostBody> postBodys = new ArrayList<>();
        String telCtn = this.mICloudMessageManagerHelper.getUserTelCtn();
        int maxEntryBulkUpload = this.mICloudMessageManagerHelper.getMaxBulkDeleteEntry();
        String str = TAG;
        Log.d(str, "setBulkUploadQueue maxEntryBulkUpload: " + maxEntryBulkUpload + " listsize: " + this.mBulkUploadQueue.size());
        if (maxEntryBulkUpload <= 1) {
            maxEntryBulkUpload = 100;
        }
        BufferDBChangeParamList changeList = new BufferDBChangeParamList();
        while (!this.mBulkUploadQueue.isEmpty()) {
            BufferDBChangeParam changeParam = (BufferDBChangeParam) this.mBulkUploadQueue.peek();
            if (changeParam == null) {
                this.mBulkUploadQueue.poll();
            } else {
                Pair<Object, HttpPostBody> changePair = makeBulkUploadBody(changeParam);
                if (changePair == null) {
                    this.mBulkUploadQueue.poll();
                } else {
                    Object obj = (Object) changePair.first;
                    HttpPostBody postBody = (HttpPostBody) changePair.second;
                    if (obj == null || postBody == null) {
                        this.mBulkUploadQueue.poll();
                    } else if (((long) postBodySize) + postBody.getBodySize() > 104857600) {
                        break;
                    } else {
                        postBodySize = (int) (((long) postBodySize) + postBody.getBodySize());
                        String str2 = TAG;
                        Log.d(str2, "postBodySize is: " + postBodySize);
                        changeList.mChangelst.add(changeParam);
                        objects.add((Object) changePair.first);
                        postBodys.add((HttpPostBody) changePair.second);
                        this.mBulkUploadQueue.poll();
                        if (objects.size() >= maxEntryBulkUpload) {
                            break;
                        }
                    }
                }
            }
        }
        if (!changeList.mChangelst.isEmpty()) {
            ObjectList objList = new ObjectList();
            objList.object = (Object[]) objects.toArray(new Object[objects.size()]);
            this.mBulkCreation = new ParamBulkCreation(new Pair<>(objList, postBodys), changeList, telCtn);
            String str3 = TAG;
            Log.e(str3, "bulk upload count:" + objects.size());
        }
    }

    /* access modifiers changed from: protected */
    public HttpRequestParams peekBulkUploadQueue() {
        if (this.mBulkCreation == null) {
            return null;
        }
        return new CloudMessageBulkCreation(this, this.mBulkCreation, this.mICloudMessageManagerHelper);
    }
}
