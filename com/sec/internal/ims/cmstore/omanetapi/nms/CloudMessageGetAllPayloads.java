package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.nms.AllPayloads;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class CloudMessageGetAllPayloads extends AllPayloads {
    public static final String TAG = CloudMessageGetAllPayloads.class.getSimpleName();
    private static final long serialVersionUID = 1;
    private final transient ICloudMessageManagerHelper mICloudMessageManagerHelper;

    public static CloudMessageGetAllPayloads buildFromPayloadUrl(IAPICallFlowListener callFlowListener, String url, BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        return new CloudMessageGetAllPayloads(url, callFlowListener, dbparam, iCloudMessageManagerHelper);
    }

    private CloudMessageGetAllPayloads(String url, IAPICallFlowListener callFlowListener, BufferDBChangeParam dbparam, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(url);
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, iCloudMessageManagerHelper);
        buildInternal(callFlowListener, dbparam);
    }

    private void buildInternal(final IAPICallFlowListener callFlowListener, final BufferDBChangeParam dbparam) {
        initCommonRequestHeaders(this.mICloudMessageManagerHelper.getContentType(), this.mICloudMessageManagerHelper.getValidTokenByLine(dbparam.mLine));
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                ParamOMAresponseforBufDB paramOMAresforBufDBObj = null;
                if (result.getStatusCode() == 206) {
                    result.setStatusCode(200);
                }
                if (result.getStatusCode() == 404) {
                    paramOMAresforBufDBObj = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND).setBufferDBChangeParam(dbparam).build();
                } else if (result.getStatusCode() == 200) {
                    if (result.getDataBinary() == null || result.getDataString() == null) {
                        callFlowListener.onFailedCall(this, dbparam);
                        return;
                    }
                    List<BodyPart> allPayloads = new ArrayList<>();
                    CloudMessageGetAllPayloads.this.parseResponsePayload(result, allPayloads);
                    if (allPayloads.size() < 1) {
                        callFlowListener.onFailedCall(this, dbparam);
                        return;
                    }
                    ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setAllPayloads(allPayloads).setBufferDBChangeParam(dbparam);
                    IAPICallFlowListener iAPICallFlowListener = callFlowListener;
                    if (iAPICallFlowListener instanceof BaseSyncHandler) {
                        builder.setActionType(ParamOMAresponseforBufDB.ActionType.ALL_PAYLOAD_DOWNLOAD);
                    } else if (iAPICallFlowListener instanceof BaseDataChangeHandler) {
                        builder.setActionType(ParamOMAresponseforBufDB.ActionType.NOTIFICATION_ALL_PAYLOAD_DOWNLOADED);
                    }
                    paramOMAresforBufDBObj = builder.build();
                }
                if (CloudMessageGetAllPayloads.this.shouldCareAfterResponsePreProcess(callFlowListener, result, paramOMAresforBufDBObj, dbparam, Integer.MIN_VALUE)) {
                    callFlowListener.onMoveOnToNext(CloudMessageGetAllPayloads.this, paramOMAresforBufDBObj);
                }
            }

            public void onFail(IOException arg1) {
                String str = CloudMessageGetAllPayloads.TAG;
                Log.e(str, "Http request onFail: " + arg1.getMessage());
                callFlowListener.onFailedCall(this, dbparam);
            }
        });
    }

    /* access modifiers changed from: private */
    public void parseResponsePayload(HttpResponseParams result, List<BodyPart> allPayloads) {
        ByteArrayDataSource ds = new ByteArrayDataSource(result.getDataBinary(), "multipart/related");
        MimeMultipart multipart = null;
        BodyPart mimeBodyPart = null;
        if (result.getHeaders() != null && !result.getHeaders().isEmpty()) {
            boolean isContTypeHasBoundary = false;
            List<String> contentType = result.getHeaders().get("content-type");
            if (contentType == null || contentType.isEmpty()) {
                contentType = result.getHeaders().get("Content-type");
            }
            if (contentType == null || contentType.isEmpty()) {
                contentType = result.getHeaders().get("Content-Type");
            }
            if (contentType != null && !contentType.isEmpty()) {
                int i = 0;
                while (true) {
                    if (i < contentType.size()) {
                        if (contentType.get(i) != null && contentType.get(i).contains("boundary=")) {
                            isContTypeHasBoundary = true;
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
            } else {
                isContTypeHasBoundary = false;
            }
            if (result.getDataString().contains("boundary=") || result.getDataString().contains("--")) {
                isContTypeHasBoundary = true;
            }
            if (isContTypeHasBoundary) {
                try {
                    multipart = new MimeMultipart((DataSource) ds);
                } catch (MessagingException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                int mimebodystart = result.getDataString().indexOf("Content-type:");
                if (mimebodystart < 0) {
                    mimebodystart = result.getDataString().indexOf("Content-Type:");
                }
                if (mimebodystart < 0) {
                    mimebodystart = result.getDataString().indexOf("content-type:");
                }
                String str = TAG;
                Log.i(str, "mimebodystart: " + mimebodystart);
                if (mimebodystart >= 0) {
                    mimeBodyPart = new MimeBodyPart(new ByteArrayInputStream(result.getDataString().substring(mimebodystart).getBytes()));
                } else {
                    return;
                }
            }
            if (multipart != null) {
                int i2 = 0;
                while (i2 < multipart.getCount()) {
                    try {
                        allPayloads.add(multipart.getBodyPart(i2));
                        i2++;
                    } catch (MessagingException e2) {
                        allPayloads.clear();
                        e2.printStackTrace();
                        return;
                    }
                }
                if (allPayloads.size() > 0) {
                }
            } else if (mimeBodyPart != null) {
                try {
                    String str2 = TAG;
                    Log.i(str2, "mimebodypart: " + mimeBodyPart.getContentType());
                    allPayloads.add(mimeBodyPart);
                } catch (MessagingException e3) {
                    allPayloads.clear();
                    e3.printStackTrace();
                }
            } else {
                allPayloads.clear();
            }
        }
    }
}
