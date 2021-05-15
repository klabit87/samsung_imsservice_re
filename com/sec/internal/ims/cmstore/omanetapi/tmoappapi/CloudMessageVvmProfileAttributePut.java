package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import java.io.IOException;
import java.util.List;

public class CloudMessageVvmProfileAttributePut extends IndividualVvmProfile {
    /* access modifiers changed from: private */
    public static String TAG = CloudMessageGetVvmProfile.class.getSimpleName();
    private static final long serialVersionUID = -622276329732847902L;

    public CloudMessageVvmProfileAttributePut(IAPICallFlowListener callFlowListener, VvmServiceProfile vvmprofile, BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNmsHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), dbparam.mLine);
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(dbparam.mLine));
        initPutRequest(vvmprofile, true);
        final BufferDBChangeParam bufferDBChangeParam = dbparam;
        final IAPICallFlowListener iAPICallFlowListener = callFlowListener;
        final VvmServiceProfile vvmServiceProfile = vvmprofile;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                List<String> retryAfterHeader;
                if (result.getStatusCode() == 401) {
                    ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setBufferDBChangeParam(bufferDBChangeParam).setLine(CloudMessageVvmProfileAttributePut.this.getBoxId());
                    Message msg = new Message();
                    msg.obj = builder.build();
                    msg.what = OMASyncEventType.CREDENTIAL_EXPIRED.getId();
                    iAPICallFlowListener.onFixedFlowWithMessage(msg);
                    iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                } else if ((result.getStatusCode() == 429 || result.getStatusCode() == 503) && (retryAfterHeader = result.getHeaders().get(HttpRequest.HEADER_RETRY_AFTER)) != null && retryAfterHeader.size() > 0) {
                    Log.i(CloudMessageVvmProfileAttributePut.TAG, retryAfterHeader.toString());
                    String retryAfter = retryAfterHeader.get(0);
                    String access$000 = CloudMessageVvmProfileAttributePut.TAG;
                    Log.d(access$000, "retryAfter is " + retryAfter + "seconds");
                    try {
                        int retryAfterValue = Integer.parseInt(retryAfter);
                        if (retryAfterValue > 0) {
                            iAPICallFlowListener.onOverRequest(this, CommonErrorName.RETRY_HEADER, retryAfterValue);
                        } else {
                            iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                        }
                    } catch (NumberFormatException ex) {
                        Log.e(CloudMessageVvmProfileAttributePut.TAG, ex.getMessage());
                        iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                    }
                } else if (result.getStatusCode() == 200 || result.getStatusCode() == 201) {
                    ParamOMAresponseforBufDB.Builder builder2 = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_UPLOADED).setVvmServiceProfile(vvmServiceProfile).setBufferDBChangeParam(bufferDBChangeParam);
                    Message msg2 = new Message();
                    msg2.obj = builder2.build();
                    msg2.what = OMASyncEventType.VVM_CHANGE_SUCCEED.getId();
                    iAPICallFlowListener.onFixedFlowWithMessage(msg2);
                } else {
                    iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = CloudMessageVvmProfileAttributePut.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
            }
        });
    }
}
