package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.BulkDeletion;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import com.sec.internal.omanetapi.nms.data.BulkResponseList;
import java.io.IOException;

public class CloudMessageBulkDeletion extends BulkDeletion {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageBulkDeletion.class.getSimpleName();
    private static final long serialVersionUID = 1;
    protected int bulkDeleteRetryCount = 0;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    protected int responseCode;

    public CloudMessageBulkDeletion(IAPICallFlowListener callFlowListener, BulkDelete bulkdelete, String currentLine, SyncMsgType type, BufferDBChangeParamList paramList, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNmsHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), currentLine);
        this.mIAPICallFlowListener = callFlowListener;
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(currentLine));
        initDeleteRequest(bulkdelete, true);
        if (iCloudMessageManagerHelper.isPostMethodForBulkDelete()) {
            setMethod(HttpRequestParams.Method.POST);
        }
        final String str = currentLine;
        final SyncMsgType syncMsgType = type;
        final BufferDBChangeParamList bufferDBChangeParamList = paramList;
        final IAPICallFlowListener iAPICallFlowListener = callFlowListener;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String access$000 = CloudMessageBulkDeletion.TAG;
                Log.i(access$000, "Result code = " + result.getStatusCode());
                ParamOMAresponseforBufDB paramOmaResForBufDbObj = null;
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 400) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 404) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 405) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 450) {
                    result.setStatusCode(200);
                }
                CloudMessageBulkDeletion.this.responseCode = result.getStatusCode();
                if (result.getStatusCode() == 200 || result.getStatusCode() == 204) {
                    ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE).setLine(str).setSyncType(syncMsgType);
                    if (result.getStatusCode() == 200) {
                        OMAApiResponseParam response = CloudMessageBulkDeletion.this.getResponse(result);
                        if (response != null) {
                            builder.setBulkResponseList(response.bulkResponseList);
                            builder.setBufferDBChangeParam(bufferDBChangeParamList);
                        } else {
                            return;
                        }
                    }
                    paramOmaResForBufDbObj = builder.build();
                } else if (result.getStatusCode() == 403) {
                    paramOmaResForBufDbObj = new ParamOMAresponseforBufDB.Builder().setBulkResponseList((BulkResponseList) null).setBufferDBChangeParam(bufferDBChangeParamList).build();
                }
                if (CloudMessageBulkDeletion.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, result, paramOmaResForBufDbObj, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    CloudMessageBulkDeletion.this.mIAPICallFlowListener.onFailedCall(this);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageBulkDeletion.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                CloudMessageBulkDeletion.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    public int getRetryCount() {
        return this.bulkDeleteRetryCount;
    }

    public void increaseRetryCount() {
        this.bulkDeleteRetryCount++;
    }

    public int getResponseCode() {
        return this.responseCode;
    }
}
