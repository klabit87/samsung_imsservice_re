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

public class CloudMessageDeleteObjectFlag extends IndividualFlag {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageDeleteObjectFlag.class.getSimpleName();
    private static final long serialVersionUID = 8158555957984259234L;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CloudMessageDeleteObjectFlag(final IAPICallFlowListener callFlowListener, String objectId, String flagnames, final BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNmsHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), dbparam.mLine, objectId, flagnames);
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(dbparam.mLine));
        initDeleteRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                ParamOMAresponseforBufDB paramOmaResForBugDbObj = null;
                if (result.getStatusCode() != 401) {
                    result.setStatusCode(204);
                }
                if (result.getStatusCode() == 204) {
                    paramOmaResForBugDbObj = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_UPDATE_COMPLETE).setBufferDBChangeParam(dbparam).build();
                }
                if (CloudMessageDeleteObjectFlag.this.shouldCareAfterResponsePreProcess(callFlowListener, result, paramOmaResForBugDbObj, dbparam, Integer.MIN_VALUE)) {
                    callFlowListener.onMoveOnToNext(CloudMessageDeleteObjectFlag.this, paramOmaResForBugDbObj);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageDeleteObjectFlag.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                callFlowListener.onFailedCall(this, dbparam);
            }
        });
    }
}
