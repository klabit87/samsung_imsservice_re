package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.nms.IndividualObject;
import java.io.IOException;

public class CloudMessageDeleteIndividualObject extends IndividualObject {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageDeleteIndividualObject.class.getSimpleName();
    private static final long serialVersionUID = 8158555957984259234L;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CloudMessageDeleteIndividualObject(final IAPICallFlowListener callFlowListener, String objectId, final BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNmsHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), dbparam.mLine, objectId);
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(dbparam.mLine));
        initDeleteRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                ParamOMAresponseforBufDB paramOmaResForBugDbObj = null;
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 204) {
                    result.setStatusCode(404);
                }
                if (result.getStatusCode() == 200 || result.getStatusCode() == 404) {
                    paramOmaResForBugDbObj = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_UPDATE_COMPLETE).setBufferDBChangeParam(dbparam).build();
                }
                if (CloudMessageDeleteIndividualObject.this.shouldCareAfterResponsePreProcess(callFlowListener, result, paramOmaResForBugDbObj, dbparam, Integer.MIN_VALUE)) {
                    callFlowListener.onMoveOnToNext(CloudMessageDeleteIndividualObject.this, paramOmaResForBugDbObj);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageDeleteIndividualObject.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                callFlowListener.onFailedCall(this, dbparam);
            }
        });
    }
}
