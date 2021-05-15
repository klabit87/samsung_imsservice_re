package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.nms.IndividualFlag;
import java.io.IOException;

public class CloudMessagePutObjectFlag extends IndividualFlag {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessagePutObjectFlag.class.getSimpleName();
    private static final long serialVersionUID = -8234485964056243622L;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CloudMessagePutObjectFlag(final IAPICallFlowListener callFlowListener, String objectId, String flagnames, final BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNmsHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), dbparam.mLine, objectId, flagnames);
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(dbparam.mLine));
        initPutRequest(true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                ParamOMAresponseforBufDB paramOMAresForBufDBObj = null;
                if (result.getStatusCode() == 204) {
                    result.setStatusCode(404);
                }
                if (result.getStatusCode() == 404 || result.getStatusCode() == 201) {
                    paramOMAresForBufDBObj = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAG_UPDATED).setBufferDBChangeParam(dbparam).build();
                }
                if (CloudMessagePutObjectFlag.this.shouldCareAfterResponsePreProcess(callFlowListener, result, paramOMAresForBufDBObj, dbparam, Integer.MIN_VALUE)) {
                    callFlowListener.onMoveOnToNext(CloudMessagePutObjectFlag.this, paramOMAresForBufDBObj);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessagePutObjectFlag.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                callFlowListener.onFailedCall(this, dbparam);
            }
        });
    }
}
