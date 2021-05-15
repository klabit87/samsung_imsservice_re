package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.IndividualObject;
import com.sec.internal.omanetapi.nms.data.Object;
import java.io.IOException;

public class CloudMessageGetIndividualObject extends IndividualObject {
    public static final String TAG = CloudMessageGetIndividualObject.class.getSimpleName();
    private static final long serialVersionUID = 8158555957984259234L;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CloudMessageGetIndividualObject(final IAPICallFlowListener callFlowListener, String objectId, final BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNmsHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), dbparam.mLine, objectId);
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(dbparam.mLine));
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                ParamOMAresponseforBufDB paramOmaResForBugDbObj = null;
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 404) {
                    paramOmaResForBugDbObj = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND).setBufferDBChangeParam(dbparam).build();
                } else if (result.getStatusCode() == 200) {
                    OMAApiResponseParam response = CloudMessageGetIndividualObject.this.getResponse(result);
                    if (response == null) {
                        callFlowListener.onFailedCall(this, dbparam);
                        return;
                    }
                    Object object = response.object;
                    if (object == null) {
                        callFlowListener.onFailedCall(this, dbparam);
                        return;
                    }
                    ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setObject(object);
                    IAPICallFlowListener iAPICallFlowListener = callFlowListener;
                    if (iAPICallFlowListener instanceof BaseSyncHandler) {
                        builder.setActionType(ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_DOWNLOAD);
                    } else if (iAPICallFlowListener instanceof BaseDataChangeHandler) {
                        builder.setActionType(ParamOMAresponseforBufDB.ActionType.NOTIFICATION_OBJECT_DOWNLOADED);
                    }
                    builder.setBufferDBChangeParam(dbparam);
                    paramOmaResForBugDbObj = builder.build();
                }
                if (CloudMessageGetIndividualObject.this.shouldCareAfterResponsePreProcess(callFlowListener, result, paramOmaResForBugDbObj, dbparam, Integer.MIN_VALUE)) {
                    callFlowListener.onMoveOnToNext(CloudMessageGetIndividualObject.this, paramOmaResForBugDbObj);
                }
            }

            public void onFail(IOException arg1) {
                String str = CloudMessageGetIndividualObject.TAG;
                Log.e(str, "Http request onFail: " + arg1.getMessage());
                callFlowListener.onFailedCall(this, dbparam);
            }
        });
    }
}
