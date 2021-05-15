package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import android.os.Message;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import java.io.IOException;
import java.util.List;

public class CloudMessageGetVvmProfile extends IndividualVvmProfile {
    private static final long serialVersionUID = 60807758423482299L;

    public CloudMessageGetVvmProfile(final IAPICallFlowListener callFlowListener, final BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iCloudMessageManagerHelper.getNmsHost(), iCloudMessageManagerHelper.getOMAApiVersion(), iCloudMessageManagerHelper.getStoreName(), dbparam.mLine);
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(dbparam.mLine));
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                List<String> retryAfterHeader;
                String strbody = result.getDataString();
                if (result.getStatusCode() == 404 || result.getStatusCode() == 400) {
                    IAPICallFlowListener iAPICallFlowListener = callFlowListener;
                    if (iAPICallFlowListener instanceof BaseSyncHandler) {
                        ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND).setBufferDBChangeParam(dbparam);
                        Message msg = new Message();
                        msg.obj = builder.build();
                        msg.what = OMASyncEventType.VVM_CHANGE_ERROR.getId();
                        callFlowListener.onFixedFlowWithMessage(msg);
                        return;
                    } else if (iAPICallFlowListener instanceof BaseDataChangeHandler) {
                        ParamOMAresponseforBufDB.Builder builder2 = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND).setBufferDBChangeParam(dbparam);
                        Message msg2 = new Message();
                        msg2.obj = builder2.build();
                        msg2.what = OMASyncEventType.VVM_CHANGE_ERROR.getId();
                        callFlowListener.onFixedFlowWithMessage(msg2);
                        return;
                    }
                }
                if (result.getStatusCode() == 401) {
                    ParamOMAresponseforBufDB.Builder builder3 = new ParamOMAresponseforBufDB.Builder().setBufferDBChangeParam(dbparam).setLine(CloudMessageGetVvmProfile.this.getBoxId());
                    Message msg3 = new Message();
                    msg3.obj = builder3.build();
                    msg3.what = OMASyncEventType.CREDENTIAL_EXPIRED.getId();
                    callFlowListener.onFixedFlowWithMessage(msg3);
                    callFlowListener.onFailedCall(this, dbparam);
                } else if ((result.getStatusCode() == 429 || result.getStatusCode() == 503) && (retryAfterHeader = result.getHeaders().get(HttpRequest.HEADER_RETRY_AFTER)) != null && retryAfterHeader.size() > 0) {
                    Log.i(IndividualVvmProfile.TAG, retryAfterHeader.toString());
                    String retryAfter = retryAfterHeader.get(0);
                    String str = IndividualVvmProfile.TAG;
                    Log.d(str, "retryAfter is " + retryAfter + "seconds");
                    try {
                        int retryAfterValue = Integer.parseInt(retryAfter);
                        if (retryAfterValue > 0) {
                            callFlowListener.onOverRequest(this, CommonErrorName.RETRY_HEADER, retryAfterValue);
                        } else {
                            callFlowListener.onFailedCall(this, dbparam);
                        }
                    } catch (NumberFormatException ex) {
                        Log.e(IndividualVvmProfile.TAG, ex.getMessage());
                        callFlowListener.onFailedCall(this, dbparam);
                    }
                } else if (result.getStatusCode() != 200) {
                    callFlowListener.onFailedCall(this, dbparam);
                } else {
                    try {
                        VvmServiceProfile profile = ((OMAApiResponseParam) new Gson().fromJson(strbody, OMAApiResponseParam.class)).vvmserviceProfile;
                        if (profile == null) {
                            callFlowListener.onFailedCall(this, dbparam);
                        } else if (callFlowListener instanceof VvmHandler) {
                            ParamOMAresponseforBufDB.Builder builder4 = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.VVM_PROFILE_DOWNLOADED).setVvmServiceProfile(profile).setBufferDBChangeParam(dbparam);
                            Message msg4 = new Message();
                            msg4.obj = builder4.build();
                            msg4.what = OMASyncEventType.VVM_CHANGE_SUCCEED.getId();
                            callFlowListener.onFixedFlowWithMessage(msg4);
                        }
                    } catch (Exception e) {
                        String str2 = IndividualVvmProfile.TAG;
                        Log.e(str2, e.toString() + " ");
                        e.printStackTrace();
                    }
                }
            }

            public void onFail(IOException arg1) {
                String str = IndividualVvmProfile.TAG;
                Log.e(str, "Http request onFail: " + arg1.getMessage());
                callFlowListener.onFailedCall(this, dbparam);
            }
        });
    }
}
