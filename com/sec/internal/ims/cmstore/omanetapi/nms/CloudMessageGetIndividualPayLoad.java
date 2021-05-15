package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.IndividualPayload;
import java.io.IOException;

public class CloudMessageGetIndividualPayLoad extends IndividualPayload {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageGetIndividualPayLoad.class.getSimpleName();
    private static final long serialVersionUID = -5816641182872600506L;
    private final transient ICloudMessageManagerHelper mICloudMessageManagerHelper;

    public static CloudMessageGetIndividualPayLoad buildFromPayloadUrl(IAPICallFlowListener callFlowListener, String resUrl, BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        return new CloudMessageGetIndividualPayLoad(callFlowListener, resUrl, dbparam, iCloudMessageManagerHelper);
    }

    private CloudMessageGetIndividualPayLoad(IAPICallFlowListener callFlowListener, String resUrl, BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(resUrl);
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, iCloudMessageManagerHelper);
        buildInternal(callFlowListener, dbparam);
    }

    private void buildInternal(final IAPICallFlowListener callFlowListener, final BufferDBChangeParam dbparam) {
        initCommonRequestHeaders(this.mICloudMessageManagerHelper.getContentType(), this.mICloudMessageManagerHelper.getValidTokenByLine(dbparam.mLine));
        initGetRequest();
        String str = TAG;
        Log.i(str, ImsConstants.FtDlParams.FT_DL_URL + IMSLog.checker(this.mBaseUrl));
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                ParamOMAresponseforBufDB paramOMAresforBufDBObj = null;
                byte[] strbody = result.getDataBinary();
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 404 || result.getStatusCode() == 403) {
                    paramOMAresforBufDBObj = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND).setBufferDBChangeParam(dbparam).build();
                } else if (result.getStatusCode() == 200) {
                    if (strbody == null) {
                        callFlowListener.onFailedCall(this, dbparam);
                        return;
                    }
                    ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setPayloadUrl(CloudMessageGetIndividualPayLoad.this.mBaseUrl).setByte(strbody).setBufferDBChangeParam(dbparam);
                    IAPICallFlowListener iAPICallFlowListener = callFlowListener;
                    if (iAPICallFlowListener instanceof BaseSyncHandler) {
                        builder.setActionType(ParamOMAresponseforBufDB.ActionType.ONE_PAYLOAD_DOWNLOAD);
                    } else if (iAPICallFlowListener instanceof BaseDataChangeHandler) {
                        builder.setActionType(ParamOMAresponseforBufDB.ActionType.NOTIFICATION_PAYLOAD_DOWNLOADED);
                    }
                    paramOMAresforBufDBObj = builder.build();
                }
                if (CloudMessageGetIndividualPayLoad.this.shouldCareAfterResponsePreProcess(callFlowListener, result, paramOMAresforBufDBObj, dbparam, Integer.MIN_VALUE)) {
                    callFlowListener.onMoveOnToNext(CloudMessageGetIndividualPayLoad.this, paramOMAresforBufDBObj);
                }
            }

            public void onFail(IOException arg1) {
                String access$100 = CloudMessageGetIndividualPayLoad.TAG;
                Log.e(access$100, "Http request onFail: " + arg1.getMessage());
                callFlowListener.onFailedCall(this, dbparam);
            }
        });
    }
}
